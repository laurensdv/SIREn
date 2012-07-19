/**
 * Copyright (c) 2009-2011 Sindice Limited. All Rights Reserved.
 *
 * Project and contact information: http://www.siren.sindice.com/
 *
 * This file is part of the SIREn project.
 *
 * SIREn is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * SIREn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with SIREn. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * @project siren
 * @author Renaud Delbru [ 10 Dec 2009 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.search.primitive;

import static org.sindice.siren.search.AbstractTestSirenScorer.NodeTermQueryBuilder.ntq;

import java.io.IOException;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.Weight;
import org.junit.Test;
import org.sindice.siren.index.codecs.RandomSirenCodec.PostingsFormatType;
import org.sindice.siren.util.BasicSirenTestCase;

public class TestNodeTermQuery extends BasicSirenTestCase {

  @Override
  protected void configure() throws IOException {
    this.setAnalyzer(AnalyzerType.TUPLE);
    this.setPostingsFormat(PostingsFormatType.RANDOM);
  }

  /**
   * Ensures simple term queries match all the documents
   */
  @Test
  public void testSimpleMatch() throws Exception {
    this.addDocument("\"Renaud Delbru\" . ");
    this.addDocument("\"Renaud\" . ");

    Query query = ntq("renaud").getDocumentQuery();
    TopDocs hits = searcher.search(query, 100);
    assertEquals(2, hits.totalHits);

    query = ntq("delbru").getDocumentQuery();
    hits = searcher.search(query, 100);
    assertEquals(1, hits.totalHits);
  }

  @Test
  public void testSimpleMatchWithConstraint() throws Exception {
    this.addDocument("\"Renaud Delbru\" . ");
    this.addDocument("\"Delbru\" \"Renaud\" . ");
    this.addDocument("\"Delbru\" . \"Renaud\" . ");

    Query query = ntq("renaud").level(1).getDocumentQuery();
    TopDocs hits = searcher.search(query, 100);
    assertEquals(0, hits.totalHits);

    query = ntq("renaud").bound(0,0).getDocumentQuery();
    hits = searcher.search(query, 100);
    assertEquals(2, hits.totalHits);
  }

  /**
   * Ensures simple term queries match all the documents
   * <br>
   * Test with no norms [SRN-44]
   */
  @Test
  public void testSimpleMatchWithNoNorms() throws Exception {
    this.addDocumentNoNorms("\"Renaud Delbru\" . ");
    this.addDocumentNoNorms("\"Renaud\" . ");

    Query query = ntq("renaud").getDocumentQuery();
    TopDocs hits = searcher.search(query, 100);
    assertEquals(2, hits.totalHits);

    query = ntq("delbru").getDocumentQuery();
    hits = searcher.search(query, 100);
    assertEquals(1, hits.totalHits);
  }

  /**
   * Ensures simple term queries does not match
   */
  @Test
  public void testSimpleDontMatch() throws Exception {
    this.addDocument("\"Renaud Delbru\" . ");

    final Query query = ntq("nomatch").getDocumentQuery();
    final TopDocs hits = searcher.search(query, 100);
    assertEquals(0, hits.totalHits);
  }

  @Test
  public void testExplain() throws IOException {
    this.addDocumentNoNorms("<http://renaud.delbru.fr/rdf/foaf#me> <http://xmlns.com/foaf/0.1/name> \"Renaud Delbru\" . ");

    final Query query = ntq("renaud").getDocumentQuery();
    final Weight w = searcher.createNormalizedWeight(query);
    assertTrue(searcher.getTopReaderContext() instanceof AtomicReaderContext);
    final AtomicReaderContext context = (AtomicReaderContext) searcher.getTopReaderContext();

    // Explain entity 0
    Explanation explanation = w.explain(context, 0);
    assertNotNull("explanation is null and it shouldn't be", explanation);

    // TODO: the similarity is random
    // All this Explain does is return the term frequency
//    final float termFreq = explanation.getDetails()[0].getDetails()[0].getValue();
//    assertEquals("term frq is not 2", 2f, termFreq, 0f);

    // Explain non existing entity
    explanation = w.explain(context, 1);
    assertNotNull("explanation is null and it shouldn't be", explanation);
    //All this Explain does is return the term frequency
    assertEquals("term frq is not 0", 0f, explanation.getValue(), 0f);
  }

}

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
 * @project siren-core
 * @author Renaud Delbru [ 27 Sep 2011 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.search.primitive;

import static org.sindice.siren.search.AbstractTestSirenScorer.dq;

import java.io.IOException;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.ScoreDoc;
import org.sindice.siren.analysis.AnyURIAnalyzer;
import org.sindice.siren.analysis.TupleAnalyzer;
import org.sindice.siren.index.codecs.RandomSirenCodec.PostingsFormatType;
import org.sindice.siren.util.BasicSirenTestCase;

/**
 * Tests {@link NodePrefixQuery} class.
 *
 * <p> Code taken from {@link TestPrefixQuery} and adapted for SIREn.
 */
public class TestNodePrefixQuery extends BasicSirenTestCase {

  @Override
  protected void configure() throws IOException {
    this.setAnalyzer(
      new TupleAnalyzer(TEST_VERSION_CURRENT,
        new WhitespaceAnalyzer(TEST_VERSION_CURRENT),
        new AnyURIAnalyzer(TEST_VERSION_CURRENT))
    );
    this.setPostingsFormat(PostingsFormatType.RANDOM);
  }

  public void testPrefixQuery() throws Exception {
    this.addDocument("</computers>");
    this.addDocument("</computers/mac>");
    this.addDocument("</computers/windows>");

    NodePrefixQuery query = new NodePrefixQuery(new Term(DEFAULT_TEST_FIELD, "/computers"));
    ScoreDoc[] hits = searcher.search(dq(query), null, 1000).scoreDocs;
    assertEquals("All documents in /computers category and below", 3, hits.length);

    query = new NodePrefixQuery(new Term(DEFAULT_TEST_FIELD, "/computers/mac"));
    hits = searcher.search(dq(query), null, 1000).scoreDocs;
    assertEquals("One in /computers/mac", 1, hits.length);

    query = new NodePrefixQuery(new Term(DEFAULT_TEST_FIELD, "/computers"));
    query.setNodeConstraint(0);
    hits = searcher.search(dq(query), null, 1000).scoreDocs;
    assertEquals("No documents in /computers category and below in node 0", 0, hits.length);

    query = new NodePrefixQuery(new Term(DEFAULT_TEST_FIELD, "/computers"));
    query.setNodeConstraint(2);
    hits = searcher.search(dq(query), null, 1000).scoreDocs;
    assertEquals("All documents in /computers category and below in node 2", 3, hits.length);
  }

}
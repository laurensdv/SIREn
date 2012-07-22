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
 * @author Renaud Delbru [ 7 Jul 2009 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.search.node;

import static org.sindice.siren.search.AbstractTestSirenScorer.NodeBooleanClauseBuilder.must;
import static org.sindice.siren.search.AbstractTestSirenScorer.NodeBooleanClauseBuilder.should;
import static org.sindice.siren.search.AbstractTestSirenScorer.NodeBooleanQueryBuilder.nbq;
import static org.sindice.siren.search.AbstractTestSirenScorer.TupleQueryBuilder.tuple;

import java.io.IOException;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.junit.Test;
import org.sindice.siren.index.codecs.RandomSirenCodec.PostingsFormatType;
import org.sindice.siren.search.AbstractTestSirenScorer;


public class TestTupleQuery extends AbstractTestSirenScorer {

  @Override
  protected void configure() throws IOException {
    this.setAnalyzer(AnalyzerType.TUPLE);
    this.setPostingsFormat(PostingsFormatType.RANDOM);
  }

  @Test
  public void testEquality() throws Exception {
    final NodeQuery query1 = tuple().optional(
      nbq(should("value1"), should("value2"))
    ).getNodeQuery();

    final NodeQuery query2 = tuple().optional(
      nbq(should("value1"), should("value2"))
    ).getNodeQuery();

    assertEquals(query1, query2);
  }

  @Test
  public void testUnaryClause() throws IOException {
    this.addDocument("\"aaa ccc\" \"bbb ccc\" . \"aaa bbb\" \"ccc eee\" . ");

    // {[aaa]}
    Query query = tuple().optional(nbq(should("aaa"))).getDocumentQuery();
    TopDocs hits = searcher.search(query, 100);
    assertEquals(1, hits.totalHits);

    // {[ccc]}
    query = tuple().optional(nbq(should("ccc"))).getDocumentQuery();
    hits = searcher.search(query, 100);
    assertEquals(1, hits.totalHits);

    // {[+ccc +aaa]}
    query = tuple().optional(nbq(must("aaa"), must("ccc"))).getDocumentQuery();
    hits = searcher.search(query, 100);
    assertEquals(1, hits.totalHits);

    // {[ddd]}
    query = tuple().optional(nbq(should("ddd"))).getDocumentQuery();
    hits = searcher.search(query, 100);
    assertEquals(0, hits.totalHits);

    // {[+bbb +eee]}
    query = tuple().optional(nbq(must("bbb"), must("eee"))).getDocumentQuery();
    hits = searcher.search(query, 100);
    assertEquals(0, hits.totalHits);
  }

  /**
   * <code>aaa ({[+ddd] [+eee]})</code>
   * Here, the keywords are mandatory in the cell, but each cell is optional in
   * the tuple. So, even the first document (having only one matching cell)
   * should match.
   */
  @Test
  public void testParenthesisMust() throws IOException {
    this.addDocument("\"bbb\" . \"ccc\" \"ddd bbb\" . ");
    this.addDocument("\"bbb\" . \"ccc eee\" \"ddd bbb\" . ");
    this.addDocument("\"bbb\" . ");

    final Query query = tuple()
      .optional(nbq(must("ddd")), nbq(must("eee")))
      .getDocumentQuery();

    final BooleanQuery q = new BooleanQuery();
    q.add(new TermQuery(new Term(DEFAULT_TEST_FIELD, "aaa")), BooleanClause.Occur.SHOULD);
    q.add(query, BooleanClause.Occur.SHOULD);
    assertEquals(2, searcher.search(q, 100).totalHits);
  }

  /**
   * <code>bbb ({[+ddd] [+eee]})</code>
   */
  @Test
  public void testParenthesisMust2() throws IOException {
    this.addDocument("\"bbb\" . ");

    final Query query = tuple()
      .optional(nbq(must("ddd")), nbq(must("eee")))
      .getDocumentQuery();

    final BooleanQuery q = new BooleanQuery();
    q.add(new TermQuery(new Term(DEFAULT_TEST_FIELD, "bbb")), BooleanClause.Occur.SHOULD);
    q.add(query, BooleanClause.Occur.SHOULD);
    assertEquals(1, searcher.search(q, 100).totalHits);
  }

  /**
   * <code>bbb +({[ddd] [eee]})</code>
   * Here, the keywords are mandatory in the cell, but each cell is optional in
   * the tuple. So, even the first document (having only one cell) should match.
   */
  @Test
  public void testParenthesisMust3() throws IOException {
    this.addDocument("\"aaa\" . \"eee\" \"bbb\" . ");
    this.addDocument("\"bbb\" . \"ccc\" \"ddd bbb\" . ");
    this.addDocument("\"bbb\" . \"ccc eee\" \"ddd bbb\" . ");

    final Query query = tuple()
      .optional(nbq(should("ddd")), nbq(should("eee")))
      .getDocumentQuery();

    final BooleanQuery q = new BooleanQuery();
    q.add(new TermQuery(new Term(DEFAULT_TEST_FIELD, "aaa")), BooleanClause.Occur.SHOULD);
    q.add(query, BooleanClause.Occur.MUST);
    assertEquals(3, searcher.search(q, 100).totalHits);
  }

  /**
   * <code>{[bbb eee]} {[ccc eee]}</code>
   */
  @Test
  public void testParenthesisShould() throws IOException {
    this.addDocument("\"bbb eee\" . \"ccc eee\" . ");
    this.addDocument("\"bbb\" . \"aaa\" . ");
    this.addDocument("\"eee\" . \"aaa\" . ");
    this.addDocument("\"aaa\" . \"ccc\" . ");
    this.addDocument("\"aaa\" . \"eee\" . ");

    final Query tq1 = tuple()
      .optional(nbq(should("bbb"), should("eee")))
      .getDocumentQuery();

    final Query tq2 = tuple()
      .optional(nbq(should("ccc"), should("eee")))
      .getDocumentQuery();

    final BooleanQuery q = new BooleanQuery();
    q.add(tq1, BooleanClause.Occur.SHOULD);
    q.add(tq2, BooleanClause.Occur.SHOULD);
    assertEquals(5, searcher.search(q, 100).totalHits);
  }

  /**
   * <code>+{+[+actor]} +{+[+actor]}</code>
   */
  @Test
  public void testLuceneBooleanMust() throws IOException {
    this.addDocument("<actor> \"actor 1\" <birthdate> \"456321\" . " +
    		"<actor> \"actor 2\" <birthdate> \"456321\" . ");
    this.addDocument("<actor> \"actor 1\" . <actor> \"actor 2\" . ");

    final Query tq1 = tuple()
      .with(nbq(must("actor")).bound(0,0))
      .getDocumentQuery();

    final Query tq2 = tuple()
      .with(nbq(must("actor")).bound(1,1))
      .getDocumentQuery();

    final BooleanQuery bq = new BooleanQuery();
    bq.add(tq1, Occur.MUST);
    bq.add(tq2, Occur.MUST);

    assertEquals(2, searcher.search(bq, 100).totalHits);
  }

}

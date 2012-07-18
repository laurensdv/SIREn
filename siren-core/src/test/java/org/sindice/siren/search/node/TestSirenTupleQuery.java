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
import org.apache.lucene.search.TopDocs;
import org.junit.Test;
import org.sindice.siren.index.codecs.RandomSirenCodec.PostingsFormatType;
import org.sindice.siren.search.AbstractTestSirenScorer;
import org.sindice.siren.search.primitive.NodeTermQuery;


public class TestSirenTupleQuery extends AbstractTestSirenScorer {

  @Override
  protected void configure() throws IOException {
    this.setAnalyzer(AnalyzerType.TUPLE);
    this.setPostingsFormat(PostingsFormatType.RANDOM);
  }

  @Test
  public void testEquality() throws Exception {
    final NodeQuery query1 = tuple().optional(
      nbq(should("value1")), nbq(should("value2"))
    ).getNodeQuery();

    final NodeQuery query2 = tuple().optional(
      nbq(should("value1")), nbq(should("value2"))
    ).getNodeQuery();

    assertEquals(query1, query2);
  }

  @Test
  public void testUnaryClause() throws IOException {
    this.addDocument("\"aaa ccc\" \"bbb ccc\" . \"aaa bbb\" \"ccc eee\" . ");

    // {[aaa]}
    NodeQuery query = tuple().optional(nbq(should("aaa"))).getNodeQuery();
    TopDocs hits = searcher.search(query, 100);
    assertEquals(1, hits.totalHits);

    // {[ccc]}
    query = tuple().optional(nbq(should("ccc"))).getNodeQuery();
    hits = searcher.search(query, 100);
    assertEquals(1, hits.totalHits);

    // {[+ccc +aaa]}
    query = tuple().optional(nbq(must("aaa"), must("ccc"))).getNodeQuery();
    hits = searcher.search(query, 100);
    assertEquals(1, hits.totalHits);

    // {[ddd]}
    query = tuple().optional(nbq(should("ddd"))).getNodeQuery();
    hits = searcher.search(query, 100);
    assertEquals(0, hits.totalHits);

    // {[+bbb +eee]}
    query = tuple().optional(nbq(must("bbb"), must("eee"))).getNodeQuery();
    hits = searcher.search(query, 100);
    assertEquals(0, hits.totalHits);
  }

  /**
   * <code>bbb ({[+ddd] [+eee]})</code>
   * Here, the keywords are mandatory in the cell, but each cell is optional in
   * the tuple. So, even the first document (having only one matching cell)
   * should match.
   */
  @Test
  public void testParenthesisMust() throws IOException {
    _helper.addDocument("\"bbb\" . \"ccc\" \"ddd bbb\" . ");
    _helper.addDocument("\"bbb\" . \"ccc eee\" \"ddd bbb\" . ");
    _helper.addDocument("\"bbb\" . ");

    final NodeBooleanQuery cq1 = new NodeBooleanQuery();
    cq1.add(ddd, NodeBooleanClause.Occur.MUST);
    final NodeBooleanQuery cq2 = new NodeBooleanQuery();
    cq2.add(eee, NodeBooleanClause.Occur.MUST);

    final TupleQuery tq = new TupleQuery();
    tq.add(new SirenCellQuery(cq1), SirenTupleClause.Occur.SHOULD);
    tq.add(new SirenCellQuery(cq2), SirenTupleClause.Occur.SHOULD);

    final BooleanQuery q = new BooleanQuery();
    q.add(aaa, BooleanClause.Occur.SHOULD);
    q.add(tq, BooleanClause.Occur.SHOULD);
    assertEquals(2, _helper.search(q).length);
  }

  /**
   * <code>bbb ({[+ddd] [+eee]})</code>
   * Here, the keywords are mandatory in the cell, but each cell is optional in
   * the tuple. So, even the first document (having only one matching cell)
   * should match.
   */
  @Test
  public void testParenthesisMust2() throws IOException {
    _helper.addDocument("\"bbb\" . ");

    final NodeBooleanQuery cq1 = new NodeBooleanQuery();
    cq1.add(ddd, NodeBooleanClause.Occur.MUST);
    final NodeBooleanQuery cq2 = new NodeBooleanQuery();
    cq2.add(eee, NodeBooleanClause.Occur.MUST);

    final TupleQuery tq = new TupleQuery();
    tq.add(new SirenCellQuery(cq1), SirenTupleClause.Occur.SHOULD);
    tq.add(new SirenCellQuery(cq2), SirenTupleClause.Occur.SHOULD);

    final BooleanQuery q = new BooleanQuery();
    q.add(bbb, BooleanClause.Occur.SHOULD);
    q.add(tq, BooleanClause.Occur.SHOULD);
    assertEquals(1, _helper.search(q).length);
  }

  /**
   * <code>bbb +({[ddd] [eee]})</code>
   * Here, the keywords are mandatory in the cell, but each cell is optional in
   * the tuple. So, even the first document (having only one cell) should match.
   */
  @Test
  public void testParenthesisMust3() throws IOException {
    _helper.addDocument("\"aaa\" . \"eee\" \"bbb\" . ");
    _helper.addDocument("\"bbb\" . \"ccc\" \"ddd bbb\" . ");
    _helper.addDocument("\"bbb\" . \"ccc eee\" \"ddd bbb\" . ");

    final NodeBooleanQuery cq1 = new NodeBooleanQuery();
    cq1.add(ddd, NodeBooleanClause.Occur.SHOULD);
    final NodeBooleanQuery cq2 = new NodeBooleanQuery();
    cq2.add(eee, NodeBooleanClause.Occur.SHOULD);

    final TupleQuery tq = new TupleQuery();
    tq.add(new SirenCellQuery(cq1), SirenTupleClause.Occur.SHOULD);
    tq.add(new SirenCellQuery(cq2), SirenTupleClause.Occur.SHOULD);

    final BooleanQuery q = new BooleanQuery();
    q.add(aaa, BooleanClause.Occur.SHOULD);
    q.add(tq, BooleanClause.Occur.MUST);
    assertEquals(3, _helper.search(q).length);
  }

  /**
   * <code>{[bbb eee]} {[ccc eee]}</code>
   */
  @Test
  public void testParenthesisShould() throws IOException {
    _helper.addDocument("\"bbb eee\" . \"ccc eee\" . ");
    _helper.addDocument("\"bbb\" . \"aaa\" . ");
    _helper.addDocument("\"eee\" . \"aaa\" . ");
    _helper.addDocument("\"aaa\" . \"ccc\" . ");
    _helper.addDocument("\"aaa\" . \"eee\" . ");

    final NodeBooleanQuery cq1 = new NodeBooleanQuery();
    cq1.add(bbb, NodeBooleanClause.Occur.SHOULD);
    cq1.add(eee, NodeBooleanClause.Occur.SHOULD);

    final TupleQuery tq1 = new TupleQuery();
    tq1.add(new SirenCellQuery(cq1), SirenTupleClause.Occur.SHOULD);

    final NodeBooleanQuery cq2 = new NodeBooleanQuery();
    cq2.add(ccc, NodeBooleanClause.Occur.SHOULD);
    cq2.add(eee, NodeBooleanClause.Occur.SHOULD);

    final TupleQuery tq2 = new TupleQuery();
    tq2.add(new SirenCellQuery(cq2), SirenTupleClause.Occur.SHOULD);

    final BooleanQuery q = new BooleanQuery();
    q.add(tq1, BooleanClause.Occur.SHOULD);
    q.add(tq2, BooleanClause.Occur.SHOULD);
    assertEquals(5, _helper.search(q).length);
  }

  /**
   * <code>+{+[+actor]} +{+[+actor]}</code>
   */
  @Test
  public void testLuceneBooleanMust() throws IOException {
    this.addDocument("<actor> \"actor 1\" <birthdate> \"456321\" . <actor> \"actor 2\" <birthdate> \"456321\" . ");
    this.addDocument("<actor> \"actor 1\" . <actor> \"actor 2\" . ");

    final NodeBooleanQuery bq1 = new NodeBooleanQuery();
    bq1.add(new NodeTermQuery(new Term(QueryTestingHelper.DEFAULT_FIELD, "actor")), NodeBooleanClause.Occur.MUST);
    final SirenCellQuery cq1 = new SirenCellQuery(bq1);
    cq1.setConstraint(0);
    final TupleQuery tq1 = new TupleQuery();
    tq1.add(cq1, SirenTupleClause.Occur.MUST);

    final NodeBooleanQuery bq2 = new NodeBooleanQuery();
    bq2.add(new NodeTermQuery(new Term(QueryTestingHelper.DEFAULT_FIELD, "actor")), NodeBooleanClause.Occur.MUST);
    final SirenCellQuery cq2 = new SirenCellQuery(bq1);
    cq2.setConstraint(0);
    final TupleQuery tq2 = new TupleQuery();
    tq2.add(cq2, SirenTupleClause.Occur.MUST);

    final BooleanQuery bq = new BooleanQuery();
    bq.add(tq1, Occur.MUST);
    bq.add(tq2, Occur.MUST);

    assertEquals(2, _helper.search(bq).length);
  }

}

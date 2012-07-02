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

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TopDocs;
import org.junit.Test;
import org.sindice.siren.index.codecs.RandomSirenCodec.PostingsFormatType;
import org.sindice.siren.search.AbstractTestSirenScorer;
import org.sindice.siren.search.node.NodeBooleanClause;
import org.sindice.siren.search.node.NodeBooleanQuery;
import org.sindice.siren.search.node.TupleQuery;
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
    ).getQuery();

    final NodeQuery query2 = tuple().optional(
      nbq(should("value1")), nbq(should("value2"))
    ).getQuery();

    assertEquals(query1, query2);
  }

  @Test
  public void testUnaryClause() throws IOException {
    this.addDocument("\"aaa ccc\" \"bbb ccc\" . \"aaa bbb\" \"ccc eee\" . ");

    // {[aaa]}
    NodeQuery query = tuple().optional(nbq(should("aaa"))).getQuery();
    TopDocs hits = searcher.search(query, 100);
    assertEquals(1, hits.totalHits);

    // {[ccc]}
    query = tuple().optional(nbq(should("ccc"))).getQuery();
    hits = searcher.search(query, 100);
    assertEquals(1, hits.totalHits);

    // {[+ccc +aaa]}
    query = tuple().optional(nbq(must("aaa"), must("ccc"))).getQuery();
    hits = searcher.search(query, 100);
    assertEquals(1, hits.totalHits);

    // {[ddd]}
    query = tuple().optional(nbq(should("ddd"))).getQuery();
    hits = searcher.search(query, 100);
    assertEquals(0, hits.totalHits);

    // {[+bbb +eee]}
    query = tuple().optional(nbq(must("bbb"), must("eee"))).getQuery();
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
   * <code>{+[ddd] +[eee]}</code>
   */
  @Test
  public void testMust() throws IOException {
    _helper.addDocument("\"eee\" . \"ddd\" . ");
    _helper.addDocument("\"bbb\" . \"ddd eee\" . ");

    final NodeBooleanQuery cq1 = new NodeBooleanQuery();
    cq1.add(ddd, NodeBooleanClause.Occur.SHOULD);
    final NodeBooleanQuery cq2 = new NodeBooleanQuery();
    cq2.add(eee, NodeBooleanClause.Occur.SHOULD);

    final TupleQuery tq = new TupleQuery();
    tq.add(new SirenCellQuery(cq1), SirenTupleClause.Occur.MUST);
    tq.add(new SirenCellQuery(cq2), SirenTupleClause.Occur.MUST);

    assertEquals(1, _helper.search(tq).length);
  }

  /**
   * <code>{+[ddd] [eee]}</code>
   */
  @Test
  public void testMustShould() throws IOException {
    _helper.addDocument("\"eee\" \"ddd\" . ");
    _helper.addDocument("\"bbb\" . \"ddd\" . ");
    _helper.addDocument("\"bbb\" . \"eee\" . ");

    final NodeBooleanQuery cq1 = new NodeBooleanQuery();
    cq1.add(ddd, NodeBooleanClause.Occur.SHOULD);
    final NodeBooleanQuery cq2 = new NodeBooleanQuery();
    cq2.add(eee, NodeBooleanClause.Occur.SHOULD);

    final TupleQuery tq = new TupleQuery();
    tq.add(new SirenCellQuery(cq1), SirenTupleClause.Occur.MUST);
    tq.add(new SirenCellQuery(cq2), SirenTupleClause.Occur.SHOULD);

    assertEquals(2, _helper.search(tq).length);
  }

  /**
   * <code>{+[ddd] -[eee]}</code>
   */
  @Test
  public void testMustMustNot() throws IOException {
    _helper.addDocument("\"eee\" \"ddd aaa\" . ");
    _helper.addDocument("\"bbb\" \"ddd eee\" . ");
    _helper.addDocument("\"bbb\" \"ddd\" . ");

    final NodeBooleanQuery cq1 = new NodeBooleanQuery();
    cq1.add(ddd, NodeBooleanClause.Occur.SHOULD);
    final NodeBooleanQuery cq2 = new NodeBooleanQuery();
    cq2.add(eee, NodeBooleanClause.Occur.SHOULD);

    final TupleQuery tq = new TupleQuery();
    tq.add(new SirenCellQuery(cq1), SirenTupleClause.Occur.MUST);
    tq.add(new SirenCellQuery(cq2), SirenTupleClause.Occur.MUST_NOT);

    assertEquals(1, _helper.search(tq).length);
  }

  /**
   * <code>{[ddd] [eee]}</code>
   */
  @Test
  public void testShould() throws IOException {
    _helper.addDocument("\"eee\" \"ddd\" . ");
    _helper.addDocument("\"bbb\" \"ddd\" . ");

    final NodeBooleanQuery cq1 = new NodeBooleanQuery();
    cq1.add(ddd, NodeBooleanClause.Occur.SHOULD);
    final NodeBooleanQuery cq2 = new NodeBooleanQuery();
    cq2.add(eee, NodeBooleanClause.Occur.SHOULD);

    final TupleQuery tq = new TupleQuery();
    tq.add(new SirenCellQuery(cq1), SirenTupleClause.Occur.SHOULD);
    tq.add(new SirenCellQuery(cq2), SirenTupleClause.Occur.SHOULD);

    assertEquals(2, _helper.search(tq).length);
  }

  /**
   * <code>{[ddd] -[eee]}</code>
   */
  @Test
  public void testShouldMustNot() throws IOException {
    _helper.addDocument("\"eee\" . \"ddd\" . ");
    _helper.addDocument("\"bbb\" . \"ddd eee\" . ");

    final NodeBooleanQuery cq1 = new NodeBooleanQuery();
    cq1.add(ddd, NodeBooleanClause.Occur.SHOULD);
    final NodeBooleanQuery cq2 = new NodeBooleanQuery();
    cq2.add(eee, NodeBooleanClause.Occur.SHOULD);

    final TupleQuery tq = new TupleQuery();
    tq.add(new SirenCellQuery(cq1), SirenTupleClause.Occur.SHOULD);
    tq.add(new SirenCellQuery(cq2), SirenTupleClause.Occur.MUST_NOT);

    assertEquals(1, _helper.search(tq).length);
  }

  /**
   * <code>+{+[+actor]} +{+[+actor]}</code>
   */
  @Test
  public void testMust2() throws IOException {
    _helper.addDocument("<actor> \"actor 1\" <birthdate> \"456321\" . <actor> \"actor 2\" <birthdate> \"456321\" . ");
    _helper.addDocument("<actor> \"actor 1\" . <actor> \"actor 2\" . ");

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

  /**
   * <code>+{+[+actor]} +{+[+actor]}</code>
   */
  @Test
  public void testTupleConstraintOneClause() throws IOException {
    _helper.addDocument("<aaa> <bbb> . <ccc> <ddd> . ");
    _helper.addDocument("<ccc> . <aaa> <bbb> <ddd> . ");

    final NodeBooleanQuery cq = new NodeBooleanQuery();
    cq.add(new NodeTermQuery(new Term(QueryTestingHelper.DEFAULT_FIELD, "ccc")), NodeBooleanClause.Occur.MUST);
    final TupleQuery tq = new TupleQuery();
    tq.add(new SirenCellQuery(cq), SirenTupleClause.Occur.MUST);
    tq.setConstraint(1); // constraint to match only in tuple 1

    assertEquals(1, _helper.search(tq).length);
  }

  /**
   * <code>+{+[+actor]} +{+[+actor]}</code>
   */
  @Test
  public void testTupleConstraintTwoClauses() throws IOException {
    _helper.addDocument("<aaa> <bbb> . <ccc> <ddd> . ");
    _helper.addDocument("<ccc> <ddd> . <aaa> <bbb> <ddd> . ");

    final NodeBooleanQuery bq1 = new NodeBooleanQuery();
    bq1.add(new NodeTermQuery(new Term(QueryTestingHelper.DEFAULT_FIELD, "ccc")), NodeBooleanClause.Occur.MUST);
    final SirenCellQuery cq1 = new SirenCellQuery(bq1);
    cq1.setConstraint(0);

    final NodeBooleanQuery bq2 = new NodeBooleanQuery();
    bq2.add(new NodeTermQuery(new Term(QueryTestingHelper.DEFAULT_FIELD, "ddd")), NodeBooleanClause.Occur.MUST);
    final SirenCellQuery cq2 = new SirenCellQuery(bq2);
    cq2.setConstraint(1);

    final TupleQuery tq = new TupleQuery();
    tq.add(cq1, SirenTupleClause.Occur.MUST);
    tq.add(cq2, SirenTupleClause.Occur.MUST);
    tq.setConstraint(1); // constraint to match only in tuple 1

    assertEquals(1, _helper.search(tq).length);
  }

  /**
   * Test conjunction with exhausted scorer.
   * The scorer of ddd got exhausted, and
   * {@link SirenCellConjunctionScorer#doNext()} was trying to retrieve the
   * entity id from the exhausted scorer.
   */
  @Test
  public void testConjunctionWithExhaustedScorer() throws IOException {
    _helper.addDocument("\"ccc\" . <aaa> \"ddd\" . ");
    _helper.addDocument("\"ccc\" . <aaa> \"ddd eee\" . ");

    final NodeBooleanQuery bq1 = new NodeBooleanQuery();
    bq1.add(aaa, NodeBooleanClause.Occur.MUST);
    final SirenCellQuery cq1 = new SirenCellQuery(bq1);
    cq1.setConstraint(0);

    final NodeBooleanQuery bq2 = new NodeBooleanQuery();
    bq2.add(eee, NodeBooleanClause.Occur.MUST_NOT);
    bq2.add(ddd, NodeBooleanClause.Occur.MUST);
    final SirenCellQuery cq2 = new SirenCellQuery(bq2);
    cq2.setConstraint(1, Integer.MAX_VALUE);

    final TupleQuery tq = new TupleQuery();
    tq.add(cq1, SirenTupleClause.Occur.MUST);
    tq.add(cq2, SirenTupleClause.Occur.MUST);

    assertEquals(1, _helper.search(tq).length);
  }

  @Test
  public void testMultiValuedPredicate() throws CorruptIndexException, IOException {
    _helper.addDocument("<aaa> \"ddd eee\" \"ddd ccc\" \"ccc eee\" \"eee bbb\" . ");
    _helper.addDocument("<aaa> \"ddd bbb\" \"ddd bbb\" \"eee bbb\" \"eee ccc\" . ");
    _helper.addDocument("<aaa> \"ddd ccc\" \"ddd bbb eee\" \"eee ccc bbb\" \"eee ccc\" . ");
    _helper.addDocument("<aaa> \"ddd eee\" \"ddd eee\" \"eee ccc bbb\" \"eee ccc\" . ");
    _helper.addDocument("<bbb> \"ddd eee\" \"ddd eee\" \"eee ccc ddd\" \"eee ccc\" . ");
    _helper.addDocument("<aaa> \"ddd eee\" \"ddd eee\" \"eee ccc bbb\" \"eee ccc\" . \n" +
    		"<bbb> \"ddd ccc\" \"ddd bbb eee\" \"eee ccc bbb\" \"eee ccc\" .\n" +
    		"<ccc> \"aaa eee ccc\" \"bbb eee ccc\" . ");

    final NodeBooleanQuery bq1 = new NodeBooleanQuery();
    bq1.add(aaa, NodeBooleanClause.Occur.MUST);
    final SirenCellQuery cq1 = new SirenCellQuery(bq1);
    cq1.setConstraint(0);

    final NodeBooleanQuery bq2 = new NodeBooleanQuery();
    bq2.add(ddd, NodeBooleanClause.Occur.MUST);
    bq2.add(ccc, NodeBooleanClause.Occur.MUST);
    final SirenCellQuery cq2 = new SirenCellQuery(bq2);
    cq2.setConstraint(1, Integer.MAX_VALUE);

    final TupleQuery tq = new TupleQuery();
    tq.add(cq1, SirenTupleClause.Occur.MUST);
    tq.add(cq2, SirenTupleClause.Occur.MUST);

    assertEquals(2, _helper.search(tq).length);
  }

  @Test
  public void testTuple2ReqCell1Excl() throws CorruptIndexException, IOException {
    _helper.addDocument("<aaa> <bbb> <ddd> <eee> . ");
    _helper.addDocument("<aaa> <ccc> <eee> . ");
    _helper.addDocument("<aaa> <ccc> <ddd> . ");
    _helper.addDocument("<aaa> <ccc> <eee> <ddd> . ");

    final NodeBooleanQuery bq1 = new NodeBooleanQuery();
    bq1.add(aaa, NodeBooleanClause.Occur.MUST);
    final SirenCellQuery cq1 = new SirenCellQuery(bq1);
    cq1.setConstraint(0);

    final NodeBooleanQuery bq2 = new NodeBooleanQuery();
    bq2.add(eee, NodeBooleanClause.Occur.MUST);
    final SirenCellQuery cq2 = new SirenCellQuery(bq2);
    cq2.setConstraint(1, Integer.MAX_VALUE);

    final NodeBooleanQuery bq3 = new NodeBooleanQuery();
    bq3.add(ddd, NodeBooleanClause.Occur.MUST);
    final SirenCellQuery cq3 = new SirenCellQuery(bq3);
    cq3.setConstraint(1, Integer.MAX_VALUE);

    final TupleQuery tq = new TupleQuery();
    tq.add(cq1, SirenTupleClause.Occur.MUST);
    tq.add(cq2, SirenTupleClause.Occur.MUST);
    tq.add(cq3, SirenTupleClause.Occur.MUST_NOT);

    assertEquals(1, _helper.search(tq).length);
  }

  @Test
  public void testMultiValuedPredicate2() throws CorruptIndexException, IOException {
    for (int i = 0; i < 100; i++) {
      _helper.addDocument("<http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#publicationTag> \"data data figure obtained\" \"belief tln parameters graphical\" \"incorrect rose proportions feature\" .");
      _helper.addDocument("<http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#publicationTag> \"statistical determining data ylx\" \"presented assumed mit factors\" \"jolla developed positive functions\" .");
      _helper.addDocument("<http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#publicationTag> \"data accuracy minutes applying\" \"focus perceive em parameterization\" \"yield learning separation rule\" .");
    }

    final NodeBooleanQuery bq1 = new NodeBooleanQuery();
    bq1.add(new NodeTermQuery(new Term(QueryTestingHelper.DEFAULT_FIELD,
      "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#publicationtag")),
      NodeBooleanClause.Occur.MUST);
    final SirenCellQuery cq1 = new SirenCellQuery(bq1);
    cq1.setConstraint(0);

    final NodeBooleanQuery bq2 = new NodeBooleanQuery();
    bq2.add(new NodeTermQuery(new Term(QueryTestingHelper.DEFAULT_FIELD, "data")),
      NodeBooleanClause.Occur.MUST);
    bq2.add(new NodeTermQuery(new Term(QueryTestingHelper.DEFAULT_FIELD, "accuracy")),
      NodeBooleanClause.Occur.MUST);
    final SirenCellQuery cq2 = new SirenCellQuery(bq2);
    cq2.setConstraint(1, Integer.MAX_VALUE);

    final TupleQuery tq = new TupleQuery();
    tq.add(cq1, SirenTupleClause.Occur.MUST);
    tq.add(cq2, SirenTupleClause.Occur.MUST);

    assertEquals(100, _helper.search(tq).length);
  }

}

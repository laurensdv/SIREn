/**
 * Copyright (c) 2009-2012 National University of Ireland, Galway. All Rights Reserved.
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
 * @author Renaud Delbru [ 13 Mar 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.search.node;

import static org.sindice.siren.analysis.MockSirenToken.node;
import static org.sindice.siren.search.AbstractTestSirenScorer.NodeBooleanClauseBuilder.must;
import static org.sindice.siren.search.AbstractTestSirenScorer.NodeBooleanQueryBuilder.nbq;
import static org.sindice.siren.search.AbstractTestSirenScorer.TupleQueryBuilder.tuple;

import java.io.IOException;

import org.junit.Test;
import org.sindice.siren.index.codecs.RandomSirenCodec.PostingsFormatType;
import org.sindice.siren.search.AbstractTestSirenScorer;

public class TestTupleScorer extends AbstractTestSirenScorer {

  @Override
  protected void configure() throws IOException {
    this.setAnalyzer(AnalyzerType.TUPLE);
    this.setPostingsFormat(PostingsFormatType.RANDOM);
  }

  @Test
  public void testUnaryClause() throws IOException {
    this.addDocument("\"aaa ccc\" \"bbb ccc\" . \"aaa bbb\" \"ccc eee\" . ");

    // {[aaa]}
    NodeScorer scorer = this.getScorer(
      tuple().optional(nbq(must("aaa"), must("ccc")))
    );

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(0, scorer.doc());
    assertTrue(scorer.nextNode());
    assertEquals(node(0), scorer.node());
    assertEndOfStream(scorer);

    scorer = this.getScorer(
      tuple().optional(nbq(must("aaa"), must("bbb")))
    );

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(0, scorer.doc());
    assertTrue(scorer.nextNode());
    assertEquals(node(1), scorer.node());
    assertEndOfStream(scorer);

    scorer = this.getScorer(
      tuple().optional(nbq(must("aaa"), must("eee")))
    );

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(0, scorer.doc());
    assertFalse(scorer.nextNode());
    assertEndOfStream(scorer);
  }

  @Test
  public void testMoreThanOneClause() throws IOException {
    this.addDocument("\"aaa ccc\" \"bbb ccc\" . \"aaa bbb\" \"ccc eee\" . ");

    // {[aaa]}
    final NodeScorer scorer = this.getScorer(
      tuple().with(nbq(must("aaa"), must("ccc")))
             .with(nbq(must("aaa"), must("bbb")))
    );

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(0, scorer.doc());
    assertTrue(scorer.nextNode());
    assertEquals(node(0), scorer.node());
    assertEndOfStream(scorer);
  }


}

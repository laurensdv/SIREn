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
 * @author Renaud Delbru [ 17 Jul 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.search.node;

import java.io.IOException;

import org.apache.lucene.index.Term;
import org.junit.Test;
import org.sindice.siren.search.node.NodeBooleanClause.Occur;
import org.sindice.siren.util.SirenTestCase;

public class TestTwigQuery extends SirenTestCase {

  @Test
  public void testSetLevelConstraint() {
    final TwigQuery tq1 = new TwigQuery(2);
    tq1.addDescendant(2, new NodeTermQuery(new Term("field", "value")), Occur.MUST);
    assertEquals(2, tq1.getLevelConstraint());
    // Descendant node level must be relative to the twig level
    assertEquals(4, tq1.clauses().get(0).getQuery().getLevelConstraint());

    tq1.setLevelConstraint(3);
    assertEquals(3, tq1.getLevelConstraint());
    // level of descendant node must have been updated
    assertEquals(5, tq1.clauses().get(0).getQuery().getLevelConstraint());

    final TwigQuery tq2 = new TwigQuery();
    tq2.addChild(tq1, Occur.MUST);
    // level of tq1 must have been updated
    assertEquals(2, tq1.getLevelConstraint());
    // level of descendant node must have been updated
    assertEquals(4, tq1.clauses().get(0).getQuery().getLevelConstraint());

    new TwigQuery(3, tq2);
    // level of tq2 must have been updated
    assertEquals(3, tq2.getLevelConstraint());
    // level of tq1 must have been updated
    assertEquals(4, tq1.getLevelConstraint());
    // level of descendant node must have been updated
    assertEquals(6, tq1.clauses().get(0).getQuery().getLevelConstraint());
  }

  @Test
  public void testSetAncestorPointer() {
    final NodeTermQuery term = new NodeTermQuery(new Term("field", "value"));
    final TwigQuery tq1 = new TwigQuery();
    tq1.addDescendant(2, term, Occur.MUST);
    // ancestor of term query must be the root of the twig
    assertSame(tq1.getRoot(), term.ancestor);
    // ancestor of the twig must be null
    assertNull(tq1.ancestor);

    final TwigQuery tq2 = new TwigQuery();
    tq2.addChild(tq1, Occur.MUST);
    // ancestor of tq1 and of its root must have been updated
    assertSame(tq2.getRoot(), tq1.ancestor);
    assertSame(tq2.getRoot(), tq1.getRoot().ancestor);
    // ancestor of tq1's descendant must have not changed
    assertEquals(4, tq1.clauses().get(0).getQuery().getLevelConstraint());

    final TwigQuery tq3 = new TwigQuery(3, tq2);
    // ancestor of tq2 and of its root must be the ancestor of tq3
    assertSame(tq3.ancestor, tq2.ancestor);
    assertSame(tq2.ancestor, tq2.getRoot().ancestor);
  }

  @Test
  public void testRewriteEmptyRoot() throws IOException {
    TwigQuery tq = new TwigQuery(2);
    tq.setBoost(0.5f);

    // with only one clause, it must be rewritten into an AncestorFilterQuery
    // wrapping the descendant query
    final NodeTermQuery ntq = new NodeTermQuery(new Term("field", "value"));
    tq.addDescendant(2, ntq, Occur.MUST);
    NodeQuery q = (NodeQuery) tq.rewrite(null);
    assertTrue(q instanceof AncestorFilterQuery);
    assertEquals(2, q.getLevelConstraint());
    assertEquals(tq.getBoost(), q.getBoost(), 0);
    assertEquals(ntq, ((AncestorFilterQuery) q).getQuery());
    assertEquals(q, ((AncestorFilterQuery) q).getQuery().ancestor);
    assertSame(tq.ancestor, q.ancestor);

    // if more than one clause, it must not be rewritten
    tq.addDescendant(2, new NodeTermQuery(new Term("field", "value")), Occur.MUST);
    q = (NodeQuery) tq.rewrite(null);
    assertSame(tq, q);

    // with only one clause and with node constraints, it must be rewritten
    tq = new TwigQuery(2);
    tq.setNodeConstraint(3);
    tq.addDescendant(2, new NodeTermQuery(new Term("field", "value")), Occur.MUST);
    q = (NodeQuery) tq.rewrite(null);
    assertTrue(q instanceof AncestorFilterQuery);
    assertEquals(3, q.lowerBound);
    assertEquals(3, q.upperBound);
  }

  @Test
  public void testRewriteEmptyClauses() throws IOException {
    final TwigQuery tq = new TwigQuery(2, new NodeTermQuery(new Term("field", "value")));
    tq.setBoost(0.5f);

    // it must be rewritten into the root query
    final NodeQuery q = (NodeQuery) tq.rewrite(null);
    assertTrue(q instanceof NodeTermQuery);
    assertEquals(2, q.getLevelConstraint());
    assertEquals(tq.getBoost(), q.getBoost(), 0);
    assertSame(tq.ancestor, q.ancestor);
  }

  @Test
  public void testRewriteTwigQueryRoot() throws IOException {
    // if the root node of a twig query is another twig query, then they must
    // be merged

    final TwigQuery tq2 = new TwigQuery(2, new NodeTermQuery(new Term("root2", "root2")));
    tq2.addChild(new NodeTermQuery(new Term("child2", "child2")), Occur.MUST);
    tq2.setBoost(0.5f);

    final TwigQuery tq1 = new TwigQuery(tq2);
    tq1.addChild(new NodeTermQuery(new Term("child1", "child1")), Occur.MUST);
    tq1.setBoost(0.5f);

    final NodeQuery q = (NodeQuery) tq1.rewrite(null);
    assertTrue(q instanceof TwigQuery);
    final TwigQuery tq = (TwigQuery) q;
    // level and boosst must be the same than for tq1
    assertEquals(1, tq.getLevelConstraint());
    assertEquals(tq.getBoost(), tq1.getBoost(), 0);
    // root must not be the same than for tq2 (it has been cloned)
    assertNotSame(tq.getRoot(), tq2.getRoot());
    // root must be equal to the root of tq2
    assertEquals(tq.getRoot(), tq2.getRoot());
    // clauses must be merged
    assertEquals(2, tq.clauses().size());
    for (final NodeBooleanClause clause : tq.clauses()) {
      assertEquals(2, clause.getQuery().getLevelConstraint());
      assertSame(tq.getRoot(), clause.getQuery().ancestor);
    }
  }

}

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
package org.sindice.siren.search.node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.Bits;

/**
 * A query that matches tuples, i.e., a boolean combination of node having the
 * same parent node.
 */
public class TupleQuery extends NodeBooleanQuery {

  /**
   * Constructs an empty boolean query. {@link Similarity#coord(int,int)} may be
   * disabled in scoring, as appropriate. For example, this score factor does
   * not make sense for most automatically generated queries, like
   * {@link WildcardQuery} and {@link FuzzyQuery}.
   *
   * @param disableCoord
   *          disables {@link Similarity#coord(int,int)} in scoring.
   */
  public TupleQuery(final boolean disableCoord) {
    this(1, disableCoord); // by default, level at 1 as in SIREn 0.2
  }

  /**
   * Constructs an empty boolean query. {@link Similarity#coord(int,int)} may be
   * disabled in scoring, as appropriate. For example, this score factor does
   * not make sense for most automatically generated queries, like
   * {@link WildcardQuery} and {@link FuzzyQuery}.
   *
   * @param disableCoord
   *          disables {@link Similarity#coord(int,int)} in scoring.
   */
  public TupleQuery(final int level, final boolean disableCoord) {
    super(disableCoord);
    this.setLevelConstraint(level);
  }

  /**
   * Adds a clause to a tuple query.
   *
   * @throws TooManyClauses
   *           if the new number of clauses exceeds the maximum clause number
   * @see #getMaxClauseCount()
   */
  @Override
  public void add(final NodeQuery query, final NodeBooleanClause.Occur occur) {
    // set the level constraint on the query
    query.setLevelConstraint(levelConstraint + 1);
    // set the ancestor pointer
    query.setAncestorPointer(this);
    super.add(new NodeBooleanClause(query, occur));
  }

  /**
   * Adds a clause to a tuple query.
   *
   * @throws TooManyClauses
   *           if the new number of clauses exceeds the maximum clause number
   * @see #getMaxClauseCount()
   */
  @Override
  public void add(final NodeBooleanClause clause) {
    final NodeQuery query = clause.getQuery();
    // set the level constraint on the query
    query.setLevelConstraint(levelConstraint + 1);
    // set the ancestor pointer
    query.setAncestorPointer(this);
    super.add(clause);
  }

  protected class TupleWeight extends NodeBooleanWeight {

    public TupleWeight(final IndexSearcher searcher, final boolean disableCoord)
    throws IOException {
      super(searcher, disableCoord);
    }

    @Override
    protected void initWeights(final IndexSearcher searcher) throws IOException {
      weights = new ArrayList<Weight>(clauses.size());
      for (int i = 0; i < clauses.size(); i++) {
        final NodeBooleanClause c = clauses.get(i);
        final NodeQuery q = c.getQuery();
        weights.add(q.createWeight(searcher));
        if (!c.isProhibited()) maxCoord++;
      }
    }

    @Override
    public Scorer scorer(final AtomicReaderContext context,
                         final boolean scoreDocsInOrder,
                         final boolean topScorer, final Bits acceptDocs)
    throws IOException {
      final List<NodeScorer> required = new ArrayList<NodeScorer>();
      final List<NodeScorer> prohibited = new ArrayList<NodeScorer>();
      final List<NodeScorer> optional = new ArrayList<NodeScorer>();
      final Iterator<NodeBooleanClause> cIter = clauses.iterator();
      for (final Weight w  : weights) {
        final NodeBooleanClause c =  cIter.next();
        final NodeScorer subScorer = (NodeScorer) w.scorer(context, true, false, acceptDocs);
        if (subScorer == null) {
          if (c.isRequired()) {
            return null;
          }
        } else if (c.isRequired()) {
          required.add(subScorer);
        } else if (c.isProhibited()) {
          prohibited.add(subScorer);
        } else {
          optional.add(subScorer);
        }
      }

      return new TupleScorer(this, disableCoord, levelConstraint, required,
        prohibited, optional, maxCoord);
    }

  }

  @Override
  public Query rewrite(final IndexReader reader) throws IOException {
    // 1 clause queries are optimised by the underlying NodeBooleanScorer by
    // creating a SingleMatchScorer

    TupleQuery clone = null;                    // recursively rewrite
    for (int i = 0 ; i < clauses.size(); i++) {
      final NodeBooleanClause c = clauses.get(i);
      final NodeQuery query = (NodeQuery) c.getQuery().rewrite(reader);
      if (query != c.getQuery()) {                     // clause rewrote: must clone
        if (clone == null) {
          clone = (TupleQuery) this.clone();
        }

        // transfer constraints
        final int[] constraint = c.getQuery().getNodeConstraint();
        query.setNodeConstraint(constraint[0], constraint[1]);
        query.setLevelConstraint(c.getQuery().getLevelConstraint());

        // transfer ancestor pointer
        query.setAncestorPointer(this);

        clone.clauses.set(i, new NodeBooleanClause(query, c.getOccur()));
      }
    }
    if (clone != null) {
      return clone;                               // some clauses rewrote
    }
    else {
      return this;                                // no clauses rewrote
    }
  }

  @Override
  public Weight createWeight(final IndexSearcher searcher) throws IOException {
    return new TupleWeight(searcher, disableCoord);
  }

}

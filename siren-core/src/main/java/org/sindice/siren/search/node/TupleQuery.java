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

import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.similarities.Similarity;

/**
 * A query that matches tuples, i.e., a boolean combination of node having the
 * same parent node.
 */
public class TupleQuery extends TwigQuery {

  /**
   * Constructs an empty tuple. By default, the level of the tuple query is 1.
   * <p>
   * {@link Similarity#coord(int,int)} is disabled by default.
   */
  public TupleQuery() {
    super(1);
  }

  /**
   * Constructs an empty tuple query at a given level.
   *
   * @param disableCoord
   *          disables {@link Similarity#coord(int,int)} in scoring.
   */
  public TupleQuery(final int level) {
    super(level);
  }

  /**
   * Constructs an empty tuple query. {@link Similarity#coord(int,int)} may be
   * disabled in scoring, as appropriate. For example, this score factor does
   * not make sense for most automatically generated queries, like
   * {@link WildcardQuery} and {@link FuzzyQuery}.
   *
   * @param disableCoord
   *          disables {@link Similarity#coord(int,int)} in scoring.
   */
  public TupleQuery(final boolean disableCoord) {
    super(1, disableCoord); // by default, level at 1 as in SIREn 0.2
  }

  /**
   * Constructs an empty tuple query. {@link Similarity#coord(int,int)} may be
   * disabled in scoring, as appropriate. For example, this score factor does
   * not make sense for most automatically generated queries, like
   * {@link WildcardQuery} and {@link FuzzyQuery}.
   *
   * @param disableCoord
   *          disables {@link Similarity#coord(int,int)} in scoring.
   */
  public TupleQuery(final int level, final boolean disableCoord) {
    super(level, disableCoord);
  }

  /**
   * Adds a clause to a tuple query.
   *
   * @throws TooManyClauses
   *           if the new number of clauses exceeds the maximum clause number
   * @see #getMaxClauseCount()
   */
  public void add(final NodeQuery query, final NodeBooleanClause.Occur occur) {
    super.addChild(query, occur);
  }

  /**
   * Adds a clause to a tuple query.
   *
   * @throws TooManyClauses
   *           if the new number of clauses exceeds the maximum clause number
   * @see #getMaxClauseCount()
   */
  public void add(final NodeBooleanClause clause) {
    super.addChild(clause.getQuery(), clause.getOccur());
  }

  @Override
  public Weight createWeight(final IndexSearcher searcher) throws IOException {
    return new TwigWeight(searcher, disableCoord);
  }

}

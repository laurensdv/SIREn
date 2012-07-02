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
 * @author Renaud Delbru [ 6 May 2009 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.search.node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.search.similarities.Similarity;
import org.sindice.siren.search.node.TupleQuery.TupleWeight;

/**
 * A scorer that matches a boolean combination of scorers having the same
 * parent node.
 * <p>
 * The {@link TupleScorer} subclasses the {@link NodeBooleanScorer}. A tuple
 * query is rewritten into a pure boolean query. To achieve this, the scorers
 * are filtered so that they return potential common ancestors. Such a filtering
 * is performed by {@link AncestorFilterNodeScorer}.
 */
public class TupleScorer extends NodeBooleanScorer {

  /**
   * Creates a {@link TupleScorer} with the given lists of
   * required, prohibited and optional scorers.
   *
   * @param weight
   *          The BooleanWeight to be used.
   * @param disableCoord
   *          If this parameter is true, coordination level matching
   *          ({@link Similarity#coord(int, int)}) is not used.
   * @param required
   *          the list of required scorers.
   * @param prohibited
   *          the list of prohibited scorers.
   * @param optional
   *          the list of optional scorers.
   */
  public TupleScorer(final TupleWeight weight,
                     final boolean disableCoord,
                     final int rootLevel,
                     final List<NodeScorer> required,
                     final List<NodeScorer> prohibited,
                     final List<NodeScorer> optional,
                     final int maxCoord)
  throws IOException {
    super(weight, disableCoord,
      addAncestorFilter(required, rootLevel),
      addAncestorFilter(prohibited, rootLevel),
      addAncestorFilter(optional, rootLevel), maxCoord);
  }

  private static final List<NodeScorer> addAncestorFilter(final List<NodeScorer> scorers,
                                                          final int ancestorLevel) {
    final ArrayList<NodeScorer> filteredScorers = new ArrayList<NodeScorer>();
    for (final NodeScorer scorer : scorers) {
      filteredScorers.add(new AncestorFilterNodeScorer(scorer, ancestorLevel));
    }
    return filteredScorers;
  }

  @Override
  public String toString() {
    return "TupleScorer(" + this.weight + "," + this.doc() + "," +
      this.node() + ")";
  }

}

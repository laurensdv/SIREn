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
 * @author Renaud Delbru [ 1 Feb 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.search.twig;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.lucene.search.Weight;
import org.sindice.siren.search.base.NodeScorer;
import org.sindice.siren.search.node.NodeConjunctionScorer;
import org.sindice.siren.util.ArrayUtils;
import org.sindice.siren.util.NodeUtils;

public class TwigConjunctionScorer extends NodeConjunctionScorer {

  private final NodeScorer root;
  private final NodeScorer[] descendants;

  public TwigConjunctionScorer(final Weight weight, final float coord,
                               final NodeScorer root,
                               final Collection<NodeScorer> scorers)
  throws IOException {
    this(weight, coord, root, scorers.toArray(new NodeScorer[scorers.size()]));
  }

  public TwigConjunctionScorer(final Weight weight, final float coord,
                               final NodeScorer root,
                               final NodeScorer ... scorers)
  throws IOException {
    super(weight, coord, ((NodeScorer[]) ArrayUtils.add(scorers, root)));
    this.root = root;
    this.descendants = scorers;
  }

  @Override
  public int doc() {
    return root.doc();
  }

  @Override
  public int[] node() {
    return root.node();
  }

  @Override
  public boolean nextNode() throws IOException {
  root: // label statement for the beginning of the loop
    while (root.nextNode()) {
      for (int i = 0; i < descendants.length; i++) {
        int c;
        while ((c = NodeUtils.compareAncestor(root.node(), descendants[i].node())) > 0) {
          if (!descendants[i].nextNode()) {
            return false;
          }
        }
        if (c > 0) {
          // continue to the label statement and move to the next root's node
          continue root;
        }
      }
      // all equals
      return true;
    }
    return false;
  }

  @Override
  public String toString() {
    return "TwigConjunctionScorer(" + weight + "," + this.doc() + "," +
      Arrays.toString(this.node()) + ")";
  }

}

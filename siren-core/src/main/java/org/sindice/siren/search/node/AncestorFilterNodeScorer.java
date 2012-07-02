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
 * @author Renaud Delbru [ 1 Jun 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.search.node;

import java.io.IOException;

import org.apache.lucene.util.IntsRef;
import org.sindice.siren.index.DocsAndNodesIterator;

/**
 * Scorer that filters node path and output the ancestor node path.
 * <p>
 * The level of the ancestor must be provided and used to modify the
 * {@link IntsRef#length} of the node path returned by the inner scorer.
 */
public class AncestorFilterNodeScorer extends NodeScorer {

  private final NodeScorer scorer;
  private final int ancestorLevel;

  public AncestorFilterNodeScorer(final NodeScorer scorer, final int ancestorLevel) {
    super(scorer.getWeight());
    this.scorer = scorer;
    this.ancestorLevel = ancestorLevel;
  }

  protected NodeScorer getScorer() {
    return scorer;
  }

  @Override
  public float score() throws IOException {
    return scorer.score();
  }

  @Override
  public String toString() {
    return "AncestorFilterScorer(" + weight + "," + this.doc() + "," +
      this.node() + ")";
  }

  @Override
  public boolean nextCandidateDocument() throws IOException {
    return scorer.nextCandidateDocument();
  }

  @Override
  public boolean nextNode() throws IOException {
    return scorer.nextNode();
  }

  @Override
  public boolean skipToCandidate(final int target) throws IOException {
    return scorer.skipToCandidate(target);
  }

  @Override
  public int doc() {
    return scorer.doc();
  }

  @Override
  public IntsRef node() {
    final IntsRef node = scorer.node();
    // resize node array only if node is not a sentinel value
    if (node.length > ancestorLevel &&
        node.ints[node.offset] != -1 &&
        node != DocsAndNodesIterator.NO_MORE_NOD) {
      node.length = ancestorLevel;
    }
    return node;
  }

}

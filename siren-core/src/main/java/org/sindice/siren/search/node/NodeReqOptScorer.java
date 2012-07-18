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

import org.apache.lucene.util.IntsRef;
import org.sindice.siren.util.NodeUtils;

/**
 * A Scorer for queries with a required part and an optional part. Delays
 * advance() on the optional part until a score() is needed.
 * <p>
 * Code taken from {@link ReqOptSumScorer} and adapted for the Siren use
 * case.
 */
public class NodeReqOptScorer extends NodeScorer {

  /**
   * The required scorer passed from the constructor.
   */
  private final NodeScorer reqScorer;

  /**
   * The optional scorer passed from the constructor, and used for boosting
   * score.
   */
  private NodeScorer optScorer;

  /**
   * Construct a {@link NodeReqOptScorer}.
   *
   * @param reqScorer
   *          The required scorer. This must match.
   * @param optScorer
   *          The optional scorer. This is used for scoring only.
   */
  public NodeReqOptScorer(final NodeScorer reqScorer,
                          final NodeScorer optScorer) {
    super(reqScorer.getWeight());
    this.reqScorer = reqScorer;
    this.optScorer = optScorer;
  }

  @Override
  public boolean nextCandidateDocument() throws IOException {
    return reqScorer.nextCandidateDocument();
  }

  @Override
  public boolean nextNode() throws IOException {
    return reqScorer.nextNode();
  }

  @Override
  public boolean skipToCandidate(final int target) throws IOException {
    return reqScorer.skipToCandidate(target);
  }

  @Override
  public int doc() {
    return reqScorer.doc();
  }

  @Override
  public IntsRef node() {
    return reqScorer.node();
  }

  @Override
  public float scoreInNode()
  throws IOException {
    final float reqScore = reqScorer.scoreInNode();
    final int doc = this.doc();
  
    if (optScorer == null) {
      return reqScore;
    } else if (optScorer.doc() < doc && // if it is the first call, optScorer.doc() returns -1
               !optScorer.skipToCandidate(doc)) {
      optScorer = null;
      return reqScore;
    }

    final IntsRef reqNode = this.node();
    /*
     * the optional scorer can be in a node that is before the one where
     * the required scorer is in.
     */
    int cmp = 1;
    while ((cmp = NodeUtils.compare(optScorer.node(), reqNode)) < 0) {
      if (!optScorer.nextNode()) {
        return reqScore;
      }
    }
    // If the optional scorer matches the same node, increase the score
    return (optScorer.doc() == doc && cmp == 0)
           ? reqScore + optScorer.scoreInNode()
           : reqScore;
  }

  @Override
  public String toString() {
    return "NodeReqOptScorer(" + weight + "," +
      this.doc() + "," + this.node() + ")";
  }

}

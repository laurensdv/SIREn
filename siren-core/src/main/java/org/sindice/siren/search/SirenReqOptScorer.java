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
package org.sindice.siren.search;

import java.io.IOException;
import java.util.Arrays;

/**
 * A Scorer for queries with a required part and an optional part. Delays
 * advance() on the optional part until a score() is needed. <br>
 * <p> Code taken from {@link ReqOptsumScorer} and adapted for the Siren use
 * case.
 */
class SirenReqOptScorer
extends SirenScorer {

  /**
   * The required scorer passed from the constructor. It is set to null as soon
   * as its nextDoc() or advance() returns false.
   */
  private final SirenScorer reqScorer;

  /**
   * The optional scorer passed from the constructor, and used for boosting score.
   * It is set to null as soon as its nextDoc() or advance() returns false.
   */
  private SirenScorer optScorer;

  private boolean firstTimeOptScorer = true;

  /**
   * Construct a <code>SirenReqOptScorer</code>.
   *
   * @param reqScorer
   *          The required scorer. This must match.
   * @param optScorer
   *          The optional scorer. This is used for scoring only.
   */
  public SirenReqOptScorer(final SirenScorer reqScorer, final SirenScorer optScorer) {
    super(null); // No similarity used.
//    super(reqScorer.getWeight());
    this.reqScorer = reqScorer;
    this.optScorer = optScorer;
  }

  @Override
  public int nextDocument() throws IOException {
    return reqScorer.nextDocument();
  }

  @Override
  public int nextPosition() throws IOException {
    return reqScorer.nextPosition();
  }

  @Override
  public int skipTo(final int entity) throws IOException {
    return reqScorer.skipTo(entity);
  }

  @Override
  public int skipTo(int docID, int[] nodes)
  throws IOException {
    return reqScorer.skipTo(docID, nodes);
  }

  @Override
  public int[] node() {
    return reqScorer.node();
  }
  
  @Override
  public int doc() {
    return reqScorer.doc();
  }

  @Override
  public int pos() {
    return reqScorer.pos();
  }

  /**
   * Returns the score of the current entity matching the query. Initially
   * invalid, until {@link #nextDocument()} is called the first time.
   *
   * @return The score of the required scorer, eventually increased by the score
   *         of the optional scorer when it also matches the current entity.
   */
  @Override
  public float score()
  throws IOException {
//    final int curEntity = reqScorer.entity();
//    final int curTuple = reqScorer.tuple();
//    final int curCell= reqScorer.cell();
    final float reqScore = reqScorer.score();

    if (firstTimeOptScorer) {
      firstTimeOptScorer = false;
      // Advance to the matching cell
      if (optScorer.skipTo(doc(), node()) == NO_MORE_DOCS) {
        optScorer = null;
        return reqScore;
      }
    }
    else if (optScorer == null) {
      return reqScore;
    }
    else if ((optScorer.doc() < doc()) &&
             (optScorer.skipTo(doc()) == NO_MORE_DOCS)) {
      optScorer = null;
      return reqScore;
    }

    // If the optional scorer matches the same cell, increase the score
    return (optScorer.doc() == doc() && Arrays.equals(optScorer.node(), node()))
           ? reqScore + optScorer.score()
           : reqScore;
  }

  @Override
  public String toString() {
    return "SirenReqOptScorer(" + this.doc() + "," + Arrays.toString(node()) + ")";
  }

}

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

/**
 * <p> A Scorer for queries within a cell with a required subscorer and an excluding
 * (prohibited) subscorer.
 * <p> Only cells matching the required subscorer and not matching the prohibited
 * subscorer are kept.
 * <p> The subscorer are not constrained to be {@link SirenPrimitiveScorer}
 * since a conjunction or disjunction of terms can be the required or excluded
 * scorers.
 *
 * <p>
 * Code taken from {@link ReqExclScorer} and adapted for the Siren use
 * case.
 */
class SirenCellReqExclScorer
extends SirenScorer {

  /** Flag to know if {@link #nextDocument()} or {@link #skipTo(int)} has been called */
  private boolean firstTime = true;

  /**
   * The required and excluded primitive Siren scorers.
   */
  private SirenScorer reqScorer, exclScorer;

  /**
   * Construct a <code>ReqExclScorer</code>.
   *
   * @param reqScorer
   *          The scorer that must match, except where
   * @param exclScorer
   *          indicates exclusion.
   */
  public SirenCellReqExclScorer(final SirenScorer reqScorer,
                                final SirenScorer exclScorer) {
    super(null); // No weight used.
    this.reqScorer = reqScorer;
    this.exclScorer = exclScorer;
  }

  @Override
  public int nextDocument() throws IOException {
    if (firstTime) {
      if (exclScorer.nextDocument() == NO_MORE_DOCS) {
        exclScorer = null; // exhausted at start
      }
      firstTime = false;
    }
    if (reqScorer == null) {
      return NO_MORE_DOCS;
    }
    if (reqScorer.nextDocument() == NO_MORE_DOCS) {
      reqScorer = null; // exhausted, nothing left
      return NO_MORE_DOCS;
    }
    if (exclScorer == null) {
      return reqScorer.doc(); // reqScorer.nextDoc() already returned != NO_MORE_DOCS
    }
    return this.toNonExcluded();
  }

  /**
   * Advance to an entity with a non excluded cell. <br>
   * On entry:
   * <ul>
   * <li>reqScorer != null,
   * <li>exclScorer != null,
   * <li>reqScorer was advanced once via nextDoc() or advance() and
   * reqScorer.entity() may still be excluded.
   * </ul>
   * Advances reqScorer a non excluded required entity, if any, and positioning
   * the pointer to the first matching cell.
   *
   * @return true iff there is an entity with a non excluded required cell.
   */
  private int toNonExcluded()
  throws IOException {
    int exclEntity = exclScorer.doc();
    do {
      final int reqEntity = reqScorer.doc(); // may be excluded
      if (reqEntity < exclEntity) {
        return reqEntity; // reqScorer advanced to before exclScorer, ie. not excluded
      }
      else if (reqEntity > exclEntity) {
        if (exclScorer.skipTo(reqEntity) == NO_MORE_DOCS) {
          exclScorer = null; // exhausted, no more exclusions
          return reqEntity;
        }
        exclEntity = exclScorer.doc();
        if (reqEntity < exclEntity) {
          return reqEntity; // not excluded
        }
      }
      // reqEntity == exclEntity
      if (this.toNonExcludedPosition() != NO_MORE_POS) {
        return reqEntity; // found one non excluded position
      }
    } while (reqScorer.nextDocument() != NO_MORE_DOCS);
    reqScorer = null; // exhausted, nothing left
    return NO_MORE_DOCS;
  }

  @Override
  public int nextPosition()
  throws IOException {
    if (reqScorer.nextPosition() == NO_MORE_POS) { // Move to the next matching cell
      return NO_MORE_POS; // exhausted, nothing left
    }
    return this.toNonExcludedPosition();
  }

  /**
   * Advance to an excluded cell. <br>
   * On entry:
   * <ul>
   * <li>reqScorer != null,
   * <li>exclScorer != null,
   * <li>reqScorer and exclScorer were advanced once via {@link #nextDocument()} or
   * {@link #skipTo(int)} and be positioned on the same entity number
   * <li> reqScorer.entity() and reqScorer.cell() may still be excluded.
   * </ul>
   * Advances reqScorer to the next non excluded required cell, if any.
   *
   * @return true iff there is an entity with a non excluded required cell.
   */
  private int toNonExcludedPosition() throws IOException {
    do {
      final int reqTuple = reqScorer.node()[0]; // may be excluded

      // exclScorer is maybe exhausted (set to null)
      if (exclScorer == null) {
        return 0; // position is invalid in this scorer, return 0.
      }

      final int exclTuple = exclScorer.node()[0];

      // exclScorer entity number cannot be inferior to reqScorer entity number
      if (reqScorer.doc() < exclScorer.doc()) {
        return 0; // position is invalid in this scorer, return 0.
      }
      else if (reqTuple < exclTuple) {
        return 0; // reqScorer advanced to before exclScorer, ie. not
                  // excluded. position is invalid in this scorer, return 0.
      }
      else if (reqTuple > exclTuple) {
        if (exclScorer.nextPosition() == NO_MORE_POS) {
          return 0; // exhausted, nothing left. position is invalid in this scorer, return 0.
        }
      }
      // reqScorer and exclScorer on same tuple: advance to next position
      else if (reqTuple == exclTuple) {
        if (reqScorer.nextPosition() == NO_MORE_POS) {
          return NO_MORE_POS; // exhausted, nothing left
        }
      }
    } while (true);
  }

  @Override
  public int doc() {
    return reqScorer.doc(); // reqScorer may be null when nextDoc() or advance()
                              // already return false
  }

  @Override
  public int[] node() {
    /**
     * Cell is invalid in high-level scorers. It will always return
     * {@link Integer.MAX_VALUE}.
     */
    return new int[] { reqScorer.node()[0], Integer.MAX_VALUE };
  }

  /**
   * Position is invalid in high-level scorers. It will always return
   * {@link Integer.MAX_VALUE}.
   */
  @Override
  public int pos() {
    return Integer.MAX_VALUE;
  }

  /**
   * Returns the score of the current document matching the query. Initially
   * invalid, until {@link #next()} is called the first time.
   *
   * @return The score of the required scorer.
   */
  @Override
  public float score()
  throws IOException {
    return reqScorer.score(); // reqScorer may be null when next() or skipTo()
                              // already return false
  }

  /**
   * Skips to the first match beyond the current whose document number is
   * greater than or equal to a given target. <br>
   * When this method is used the {@link #explain(int)} method should not be
   * used.
   *
   * @param entityID
   *          The target entity number.
   * @return true iff there is such a match.
   */
  @Override
  public int skipTo(final int entityID) throws IOException {
    if (firstTime) {
      firstTime = false;
      if (exclScorer.skipTo(entityID) == NO_MORE_DOCS) {
        exclScorer = null; // exhausted
      }
    }
    if (reqScorer == null) {
      return NO_MORE_DOCS;
    }
    if (exclScorer == null) {
      return reqScorer.skipTo(entityID);
    }
    if (reqScorer.skipTo(entityID) == NO_MORE_DOCS) {
      reqScorer = null;
      return NO_MORE_DOCS;
    }
    return this.toNonExcluded();
  }

  @Override
  public int skipTo(int docID, int[] nodes)
  throws IOException {
    if (nodes.length != 1) {
      throw new UnsupportedOperationException();  
    }
    if (firstTime) {
      firstTime = false;
      if (exclScorer.skipTo(docID, nodes) == NO_MORE_DOCS) {
        exclScorer = null; // exhausted
      }
    }
    if (reqScorer == null) {
      return NO_MORE_DOCS;
    }
    if (exclScorer == null) {
      return reqScorer.skipTo(docID, nodes);
    }
    if (reqScorer.skipTo(docID, nodes) == NO_MORE_DOCS) {
      reqScorer = null;
      return NO_MORE_DOCS;
    }
    return this.toNonExcluded();
  }

}

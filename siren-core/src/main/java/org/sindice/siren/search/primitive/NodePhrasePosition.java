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
package org.sindice.siren.search.primitive;

import java.io.IOException;

import org.sindice.siren.index.DocsNodesAndPositionsEnum;
import org.sindice.siren.index.PositionsIterator;

/**
 * Code taken from {@link PhrasePositions} and adapted for the Siren use case.
 */
class NodePhrasePosition {

  /**
   * Current position
   * <p>
   * Sentinel value is equal to {@link Integer.MIN_VALUE} since a position can
   * be negative.
   */
  int pos = Integer.MIN_VALUE;

  final int offset;            // position in phrase

  private final DocsNodesAndPositionsEnum docsEnum; // stream of positions
  protected NodePhrasePosition next;                // used to make lists

  NodePhrasePosition(final DocsNodesAndPositionsEnum docsEnum, final int offset) {
    this.docsEnum = docsEnum;
    this.offset = offset;
  }

  void init() throws IOException {
    pos = Integer.MIN_VALUE;
  }

  /**
   * Go to next location of this term current document, and set
   * <code>position</code> as <code>location - offset</code>, so that a
   * matching exact phrase is easily identified when all PhrasePositions
   * have exactly the same <code>position</code>.
   */
  public final boolean nextPosition() throws IOException {
    if (docsEnum.nextPosition()) {          // read subsequent pos's
      pos = docsEnum.pos() - offset;
      return true;
    }
    else {
      pos = PositionsIterator.NO_MORE_POS;
      return false;
    }
  }

  @Override
  public String toString() {
    return "NodePhrasePositions(d:"+docsEnum.doc()+" n:"+docsEnum.node()+" o:"+offset+" p:"+pos+")";
  }

}

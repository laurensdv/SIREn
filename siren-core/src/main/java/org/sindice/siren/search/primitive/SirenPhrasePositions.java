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

import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.sindice.siren.index.DocsNodesAndPositionsEnum;
import org.sindice.siren.index.DocsAndNodesIterator;
import org.sindice.siren.index.NodesConfig;
import org.sindice.siren.index.codecs.siren020.Siren020DocNodAndPosEnum;

/**
 * Code taken from {@link PhrasePositions} and adapted for the Siren use case.
 */
class SirenPhrasePositions implements DocsAndNodesIterator {

  /**
   * Flag to know if {@link #skipTo(int, int)} or {@link #advance(int, int, int)}
   * has been called. If yes, tuples, cells or positions have been skipped
   * and {@link #firstPosition()} should not be called.
   **/
  protected boolean             _hasSkippedPosition = false;

//  private int dataset = -1;           // current dataset
//  private int entity = -1;            // current entity
//  private int tuple = -1;             // current tuple
//  private int cell = -1;              // current cell
  private int pos = -1;               // current position
  private final int offset;           // position in phrase

  private final DocsNodesAndPositionsEnum napEnum; // stream of positions
  protected SirenPhrasePositions next;            // used to make lists

  SirenPhrasePositions(final DocsAndPositionsEnum t, final int o) {
    // TODO: don't instantiate the enum here! this should be done by the specific codec.
    napEnum = new Siren020DocNodAndPosEnum(new NodesConfig(2), t);
    offset = o;
  }

  public int nextDocument()
  throws IOException {
    // increments to next entity
    if (napEnum.nextDocument() == DocIdSetIterator.NO_MORE_DOCS) {
      return DocIdSetIterator.NO_MORE_DOCS;
    }
//    entity = napEnum.doc();
//    dataset = tuple = cell = pos = -1;
    pos = -1;
    _hasSkippedPosition = false;
    return napEnum.doc();
  }

  public final void firstPosition()
  throws IOException {
    if (!_hasSkippedPosition)
      this.nextPosition();
  }

  /**
   * Go to next location of this term current document, and set
   * <code>position</code> as <code>location - offset</code>, so that a
   * matching exact phrase is easily identified when all PhrasePositions
   * have exactly the same <code>position</code>.
   */
  public final int nextPosition() throws IOException {
    if (napEnum.nextPosition() != NO_MORE_POS) { // read subsequent pos's
      pos = napEnum.pos() - offset;
//      tuple = napEnum.tuple();
//      cell = napEnum.cell();
      return pos;
    }
    return NO_MORE_POS;
  }

  public int skipTo(final int entityID) throws IOException {
    if (napEnum.skipTo(entityID) == DocIdSetIterator.NO_MORE_DOCS) {
      return DocIdSetIterator.NO_MORE_DOCS;
    }
//    entity = napEnum.doc();
    _hasSkippedPosition = false;
    return napEnum.doc();
  }

  @Override
  public int skipTo(int docID, int[] nodes)
  throws IOException {
    if (napEnum.skipTo(docID, nodes) == DocIdSetIterator.NO_MORE_DOCS) {
      return DocIdSetIterator.NO_MORE_DOCS;
    }
    pos = napEnum.pos() - offset;
    _hasSkippedPosition = true;
    return napEnum.doc();
  }

  @Override
  public int pos() {
    return pos;
  }
  public int offset() {
    return offset;
  }

  @Override
  public String toString() {
    return "PhrasePosition(" + napEnum.doc() + ", " + napEnum.toString() + ", " + pos + ")";
  }

  @Override
  public int doc() {
    return napEnum.doc();
  }

  @Override
  public int[] node() {
    return napEnum.node();
  }

}

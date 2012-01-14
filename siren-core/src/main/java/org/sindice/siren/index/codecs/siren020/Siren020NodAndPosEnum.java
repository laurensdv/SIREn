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
 * @project siren-core
 * @author Campinas Stephane [ 28 Nov 2011 ]
 * @link stephane.campinas@deri.org
 */
package org.sindice.siren.index.codecs.siren020;

import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.ArrayUtil;
import org.apache.lucene.util.IntsRef;
import org.sindice.siren.index.DocsNodesAndPositionsEnum;
import org.sindice.siren.util.NodeUtils;

/**
 * SIREn 0.2.x implementation of {@link DocsNodesAndPositionsEnum} based on a
 * {@link DocsAndPositionsEnum}. Node path information are encoded into the
 * term's payload.
 */
public class Siren020NodAndPosEnum extends DocsNodesAndPositionsEnum {

  protected DocsAndPositionsEnum e;

  protected int[] curNode = new int[2];
  protected int pos = -1;

  // for node and position lookahead
  protected boolean lookahead = false;
  protected int[] nextNode = new int[2];
  protected int nextPos = -1;

  /** index of the next element to be read */
  protected int _posPtr = -1;

  private final VIntPayloadCodec codec = new VIntPayloadCodec();

  public Siren020NodAndPosEnum(final DocsAndPositionsEnum e) {
    this.e = e;
  }

  @Override
  public int doc() {
    return e.docID();
  }

  @Override
  public int termFreqInDoc() {
    return e.freq();
  }

  @Override
  public int nodeFreqInDoc() {
    throw new UnsupportedOperationException("Not supported in Siren020 codec");
  }

  @Override
  public int[] node() {
    return curNode;
  }

  @Override
  public int termFreqInNode() {
    throw new UnsupportedOperationException("Not supported in Siren020 codec");
  }

  @Override
  public int pos() {
    return pos;
  }

  @Override
  public boolean nextDocument()
  throws IOException {
    if (e.nextDoc() == DocIdSetIterator.NO_MORE_DOCS) {
      this.setNodAndPosToSentinel(); // sentinel value
      return false;
    }
    this.resetNodAndPos();
    _posPtr = -1;
    lookahead = false;
    nextPos = -1;
    return true;
  }

  @Override
  public boolean skipTo(final int target)
  throws IOException {
    if (e.advance(target) == DocIdSetIterator.NO_MORE_DOCS) {
      this.setNodAndPosToSentinel(); // sentinel value
      return false;
    }
    this.resetNodAndPos();
    _posPtr = -1;
    lookahead = false;
    nextPos = -1;
    return true;
  }

  @Override
  public boolean skipTo(final int target, final int[] node)
  throws IOException {
    // optimisation: if current entity is the right one, don't call advance
    // and avoid to reset buffer
    if (target == e.docID() || this.skipTo(target)) {
      // If we skipped to the right entity, load the nodes and let's try to
      // find the right one
      if (target == e.docID()) {
        // If nodes are not found, just move to the next entity, and to the
        // first node
        if (!this.nextNodeEqualOrDescendant(node)) {
          if (this.nextDocument()) {
            this.nextNode(); // advance to the first node
            return true;
          }
          // else stream exhausted
          this.setNodAndPosToSentinel(); // sentinel value
          return false;
        }
      }
      return true;
    }
    // else stream exhausted
    this.setNodAndPosToSentinel(); // sentinel value
    return false;
  }

  /**
   * Advance to the node right after the one passed in argument.
   * Returns true if the current node is still before the one passed in argument.
   * Returns true if nodes is empty
   */
  protected boolean nextNodeEqualOrDescendant(final int[] node)
  throws IOException {
    while (++_posPtr < e.freq()) {
      pos = -1; // reset pos
      nextPos = e.nextPosition(); // backup position in nextPos
      this.decodeNodePath(curNode);
      if (NodeUtils.isAncestorOrEqual(node, curNode)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean nextNode() throws IOException {
    pos = -1; // reset pos

    if (lookahead) { // if lookahead, just switch the reference (avoid copy)
      this.switchNodeReference();
      lookahead = false;
      return true;
    }

    // find the next node path that is different from the current node path
    do {
      if (++_posPtr < e.freq()) {
        nextPos = e.nextPosition(); // backup position in nextPos
        this.decodeNodePath(nextNode);
      }
      else {
        // stream exhausted
        this.setNodAndPosToSentinel(); // sentinel value
        return false;
      }
    } while (Arrays.equals(curNode, nextNode));

    // just switch the reference (avoid copy)
    this.switchNodeReference();

    return true;
  }

  private final void switchNodeReference() {
    final int[] tmp = curNode;
    curNode = nextNode;
    nextNode = tmp;
  }

  @Override
  public boolean nextPosition() throws IOException {
    if (nextPos != -1) {
      pos = nextPos;
      nextPos = -1;
      return true;
    }

    if (++_posPtr < e.freq()) {
      lookahead = true;
      nextPos = e.nextPosition();
      this.decodeNodePath(nextNode);

      // if lookahead node is equal to the current node, then we have a new position
      if (Arrays.equals(curNode, nextNode)) {
        pos = nextPos;
        nextPos = -1;
        lookahead = false;
        return true;
      }
      // set pos to sentinel value
      pos = NO_MORE_POS;
      return false;
    }
    // stream exhausted
    this.setNodAndPosToSentinel(); // sentinel value
    return false;
  }

  private void decodeNodePath(int[] dst) throws IOException {
    final IntsRef nodePath = this.decodePayload();

    // Ensure we have enough space to store the node path
    dst = ArrayUtil.grow(dst, nodePath.length);

    // Delta decoding
    // we assume that there is always at least one node encoded
    dst[0] = curNode[0] == -1 ? nodePath.ints[nodePath.offset] : curNode[0] + nodePath.ints[nodePath.offset];

    for (int i = nodePath.offset + 1; i < nodePath.length; i++) {
      dst[i] = (curNode[i] == -1 || nodePath.ints[i-1] != 0) ? nodePath.ints[i] : curNode[i] + nodePath.ints[i];
    }
  }

  private IntsRef decodePayload() throws IOException {
    if (e.hasPayload()) {
      return codec.decode(e.getPayload());
    }
    else { // no payload, should never happen
      throw new IOException("No payload found");
    }
  }

  /**
   * Set the current nodes and position to the sentinel values, indicating that
   * there is no more occurrences to read.
   * <br>
   * Reset the lookahead position.
   */
  private void setNodAndPosToSentinel() {
    nextPos = -1;
    Arrays.fill(curNode, NO_MORE_NOD);
    pos = NO_MORE_POS;
  }

  /**
   * Set the current nodes and position to -1.
   */
  private void resetNodAndPos() {
    Arrays.fill(curNode, -1);
    pos = -1;
  }

}

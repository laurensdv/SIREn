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
import org.apache.lucene.util.ArrayUtil;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IntsRef;
import org.sindice.siren.index.NodAndPosEnum;

/**
 *
 */
public class Siren020NodAndPosEnum extends NodAndPosEnum {

  protected DocsAndPositionsEnum e;

  protected int[] curNode = new int[2];
  protected int pos = -1;

  /** index of the next element to be read */
  protected int _posPtr = -1;

  private final VIntPayloadCodec codec = new VIntPayloadCodec();
  private IntsRef nodePath;

  public Siren020NodAndPosEnum(final DocsAndPositionsEnum e) {
    this.e = e;
  }

  @Override
  public int docID() {
    return e.docID();
  }

  @Override
  public int freq() {
    return e.freq();
  }

  @Override
  public int[] node() {
    return curNode;
  }

  @Override
  public int pos() {
    return pos;
  }

  @Override
  public BytesRef getPayload()
  throws IOException {
    return e.getPayload();
  }

  @Override
  public boolean hasPayload() {
    return e.hasPayload();
  }

  @Override
  public int nextDoc()
  throws IOException {
    final int docID;
    if ((docID = e.nextDoc()) == NO_MORE_DOCS) {
      this.setNodAndPosToSentinel(); // sentinel value
      return NO_MORE_DOCS;
    }
    this.resetNodAndPos();
    _posPtr = -1;
    return docID;
  }

  @Override
  public int advance(final int target)
  throws IOException {
    if (target == e.docID()) { // optimised case: reset buffer
      this.resetNodAndPos();
      _posPtr = -1;
      return target;
    }

    final int docID;
    if ((docID = e.advance(target)) == NO_MORE_DOCS) {
      this.setNodAndPosToSentinel(); // sentinel value
      return NO_MORE_DOCS;
    }
    this.resetNodAndPos();
    _posPtr = -1;
    return docID;
  }

  @Override
  public int advance(final int target, final int[] nodes)
  throws IOException {
//    if (nodes.length > curNode.length) {
//      throw new RuntimeException("Invalid argument, received array with size=" +
//      nodes.length + ", should be no more than " + curNode.length);
//    }

    // optimisation: if current entity is the right one, don't call advance
    // and avoid to reset buffer
    if (target == e.docID() || this.advance(target) != NO_MORE_DOCS) {
      // If we skipped to the right entity, load the nodes and let's try to
      // find the right one
      if (target == e.docID()) {
        // If nodes are not found, just move to the next entity
        // (SRN-17), and to the next branch (SRN-24)
        if (!this.findNode(nodes)) {
          if (this.nextDoc() != NO_MORE_DOCS) {
            this.nextPosition(); // advance to the first position (SRN-24)
            return this.docID();
          }
          // position stream exhausted
          this.setNodAndPosToSentinel(); // sentinel value
          return NO_MORE_DOCS;
        }
      }
      return this.docID();
    }
    // position stream exhausted
    this.setNodAndPosToSentinel(); // sentinel value
    return NO_MORE_DOCS;
  }

  /**
   * Advance to the node right after the one passed in argument.
   * Returns true if the current node is still before the one passed in argument.
   * Returns true if nodes is empty
   * @param nodes
   * @return
   * @throws IOException
   */
  protected boolean findNode(final int[] nodes)
  throws IOException {
    while (++_posPtr < this.freq()) {
      this.decodeNodePath();
      boolean match = true;
      for (int i = 0; i < nodes.length; i++) {
        if (this.isBefore(nodes, i)) {
          match = false;
          break;
        }
      }
      if (match) {
        return true;
      }
    }
    return false;
  }

  /**
   * Return true if the searched node hasn't been reached yet.
   * @param nodes
   * @param index
   * @return
   */
  private boolean isBefore(final int[] nodes, int index) {
    boolean res = curNode[index] < nodes[index];

    while (--index >= 0) {
      res = curNode[index] == nodes[index] && res;
    }

    return res;
  }

  @Override
  public boolean nextNode() throws IOException {
    if (++_posPtr < this.freq()) {
      e.nextPosition(); // does not set pos variable
      this.decodeNodePath();
      return true;
    }

    return false;
  }

  @Override
  public int nextPosition() throws IOException {
    if (++_posPtr < this.freq()) {
      pos = e.nextPosition();
      this.decodeNodePath();
      return pos;
    }

    return NO_MORE_POS;
  }

  private void decodeNodePath() throws IOException {
    this.decodePayload();

    // Ensure we have enough space to store the node path
    ArrayUtil.grow(curNode, nodePath.length);

    // Delta decoding
    // we assume that there is always at least one node encoded
    curNode[0] = curNode[0] == -1 ? nodePath.ints[nodePath.offset] : curNode[0] + nodePath.ints[nodePath.offset];

    for (int i = nodePath.offset + 1; i < nodePath.length; i++) {
      curNode[i] = (curNode[i] == -1 || nodePath.ints[i-1] != 0) ? nodePath.ints[i] : curNode[i] + nodePath.ints[i];
    }
  }

  private void decodePayload() throws IOException {
    if (this.hasPayload()) {
      nodePath = codec.decode(this.getPayload());
    }
    else { // no payload, should never happen
      throw new IOException("No payload found");
    }
  }

  /**
   * Set the current nodes and position to the sentinel values, indicating that
   * there is no more occurrences to read.
   */
  private void setNodAndPosToSentinel() {
    Arrays.fill(curNode, NOD_SENTINEL_VAL);
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

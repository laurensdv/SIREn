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
import org.apache.lucene.util.BytesRef;
import org.sindice.siren.index.AbstractSirenPayload;
import org.sindice.siren.index.NodAndPosEnum;
import org.sindice.siren.index.NodesConfig;
import org.sindice.siren.index.PackedIntSirenPayload;

/**
 * 
 */
public class Siren020NodAndPosEnum extends NodAndPosEnum {

  private final DocsAndPositionsEnum _dAndpEnum;
  private final PackedIntSirenPayload payload = new PackedIntSirenPayload();
  private final byte[] firstNode = new byte[1]; // cannot directly access the attributes of the siren payload
  private final int[] _curNode;
  
  /** 
   * Flag to know if {@link #nextDoc()}, {@link #advance(int)}
   * or {@link #advance(int, int[])} has been called
   */
//  private boolean             _isFirstTime = true;

  /** index of the next element to be read or written */
  private int                 _posPtr = -1;
  
  public Siren020NodAndPosEnum(final NodesConfig config, final DocsAndPositionsEnum e) {
    _curNode = new int[2 + config.getNbLayers()];
    _dAndpEnum = e;
    firstNode[0] = 0;
  }

  @Override
  public int freq() {
    return _dAndpEnum.freq();
  }

  @Override
  public int docID() {
    return _curNode[NodesConfig.DOC_INDEX];
  }
  
  @Override
  public BytesRef getPayload()
  throws IOException {
    return _dAndpEnum.getPayload();
  }

  @Override
  public boolean hasPayload() {
    return _dAndpEnum.hasPayload();
  }

  protected AbstractSirenPayload getSirenPayload()
  throws IOException {
    if (hasPayload()) {
      final BytesRef ref = getPayload();
      payload.setData(ref.bytes, ref.offset, ref.length);
      payload.decode();
    }
    else { // no payload, special case where tuple and cell == 0
      payload.setData(firstNode);
    }
    return payload;
  }
  
  @Override
  public int advance(int target)
  throws IOException {
    if (target == _curNode[NodesConfig.DOC_INDEX]) { // optimised case: reset buffer
      Arrays.fill(_curNode, -1);
      _posPtr = -1;
      return target;
    }
    
    final int nodeID;
    if ((nodeID = _dAndpEnum.advance(target)) == NO_MORE_DOCS) {
      Arrays.fill(_curNode, NO_MORE_DOCS); // sentinel value
      return NO_MORE_DOCS;
    }
    Arrays.fill(_curNode, -1);
    _curNode[NodesConfig.DOC_INDEX] = nodeID;
//    _isFirstTime = false;
    _posPtr = -1;
    return nodeID;
  }
  
  @Override
  public int advance(int target, int[] nodes)
  throws IOException {
    if (nodes.length + 2 != _curNode.length) // 2 == the document ID and the position: they are here by default
      throw new RuntimeException("Invalid argument, received array with size=" + nodes.length + ", should be " + (_curNode.length - 2));
    
    // optimisation: if current entity is the right one, don't call advance
    // and avoid to reset buffer
    if (target == _curNode[NodesConfig.DOC_INDEX] || advance(target) != NO_MORE_DOCS) {
      // If we skipped to the right entity, load the tuples and let's try to
      // find the right one
      if (target == _curNode[NodesConfig.DOC_INDEX]) {
        // If tuple and cell are not found, just move to the next entity
        // (SRN-17), and to the next cell (SRN-24)
        if (!findNode(nodes)) {
          if (this.nextDoc() != NO_MORE_DOCS) {
            this.nextPosition(); // advance to the first position (SRN-24)
            return this.docID();
          }
          // position stream exhausted
          Arrays.fill(_curNode, NO_MORE_DOCS); // sentinel value
          return NO_MORE_DOCS;
        }
      }
      return this.docID();
    }
    // position stream exhausted
    Arrays.fill(_curNode, NO_MORE_DOCS); // sentinel value
    return NO_MORE_DOCS;
  }
  
  /**
   * Return true if the searched node hasn't been reached yet.
   * @param nodes
   * @param index
   * @return
   */
  private boolean compare(int[] nodes, int index) {
    boolean res = _curNode[NodesConfig.START_INDEX + index] < nodes[index];
    
    while (--index >= 0) {
      res = _curNode[NodesConfig.START_INDEX + index] == nodes[index] && res;
    }
    return res;
  }
  
  /**
   * Advance to the node right after the one passed in argument
   * @param nodes
   * @return
   * @throws IOException
   */
  private boolean findNode(int[] nodes)
  throws IOException {
    if (nodes[0] == -1) {
      return true;
    }
    
    int maxIndex = 0;
    for (int i = 0; i < nodes.length; i++) {
      if (nodes[i] == -1) {
        break;
      }
      maxIndex = i;
    }
    while (++_posPtr < freq()) {
      this.loadTuple();
      boolean match = true;
      for (int i = 0; i <= maxIndex; i++) {
        if (compare(nodes, i)) {
          match = false;
          break;
        }
      }
      if (!match) {
        continue;
      } else {
        return true;
      }
    }
    return false;
  }
  
  @Override
  public int nextPosition()
  throws IOException {
//    if (_isFirstTime)
//      throw new RuntimeException("Invalid call, nextDoc should be called first.");

    if (++_posPtr < freq()) {
      this.loadTuple();
      return _curNode[NodesConfig.POS_INDEX];
    }

    return NO_MORE_POS;
  }

  protected void loadTuple() throws IOException {
    _curNode[NodesConfig.POS_INDEX] = _dAndpEnum.nextPosition();
    final AbstractSirenPayload payload = getSirenPayload();
    _curNode[NodesConfig.START_INDEX] = _curNode[NodesConfig.START_INDEX] == -1 ? payload.getTupleID() : _curNode[NodesConfig.START_INDEX] + payload.getTupleID();
    if (_curNode[NodesConfig.START_INDEX + 1] == -1 || payload.getTupleID() != 0) {
      _curNode[NodesConfig.START_INDEX + 1] = payload.getCellID();
    }
    else { // if (payload.getTupleID() == 0)
      _curNode[NodesConfig.START_INDEX + 1] += payload.getCellID();
    }
  }

  @Override
  public int nextDoc()
  throws IOException {
    final int nodeID;
    if ((nodeID = _dAndpEnum.nextDoc()) == NO_MORE_DOCS) {
      Arrays.fill(_curNode, NO_MORE_DOCS); // sentinel value
      return NO_MORE_DOCS;
    }
    Arrays.fill(_curNode, -1);
    _curNode[NodesConfig.DOC_INDEX] = nodeID;
//    _isFirstTime = false;
    _posPtr = -1;
    return nodeID;
  }

  @Override
  public int[] node() {
    return _curNode;
  }

}

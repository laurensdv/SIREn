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
import org.sindice.siren.index.AbstractNodAndPos;
import org.sindice.siren.index.AbstractSirenPayload;
import org.sindice.siren.index.NodesConfig;
import org.sindice.siren.index.PackedIntSirenPayload;

/**
 * 
 */
public class Siren020NodAndPosEnum extends AbstractNodAndPos {

  private final DocsAndPositionsEnum _dAndpEnum;
  private final PackedIntSirenPayload payload = new PackedIntSirenPayload();
  private final byte[] firstNode = new byte[1]; // cannot directly access the attributes of the siren payload
  
  /** 
   * Flag to know if {@link #nextDoc()}, {@link #advance(int)}
   * or {@link #advance(int, int[])} has been called
   */
//  private boolean             _isFirstTime = true;
  
  public Siren020NodAndPosEnum(final NodesConfig config, final DocsAndPositionsEnum e) {
    super(config);
    _dAndpEnum = e;
    firstNode[0] = 0;
  }

  @Override
  public int freq() {
    return _dAndpEnum.freq();
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
    if (target == _docID) { // optimised case: reset buffer
      Arrays.fill(_curNode, -1);
      _pos = -1;
      _posPtr = -1;
      return target;
    }
    
    final int nodeID;
    if ((nodeID = _dAndpEnum.advance(target)) == NO_MORE_DOCS) {
      Arrays.fill(_curNode, NO_MORE_DOCS); // sentinel value
      return NO_MORE_DOCS;
    }
    Arrays.fill(_curNode, -1);
    _pos = -1;
    _docID = nodeID;
//    _isFirstTime = false;
    _posPtr = -1;
    return nodeID;
  }
  
  @Override
  public int advance(int target, int[] nodes)
  throws IOException {
    if (nodes.length != _curNode.length)
      throw new RuntimeException("Invalid argument, received array with size=" + nodes.length + ", should be " + _curNode.length);
    
    // optimisation: if current entity is the right one, don't call advance
    // and avoid to reset buffer
    if (target == _docID || advance(target) != NO_MORE_DOCS) {
      // If we skipped to the right entity, load the tuples and let's try to
      // find the right one
      if (target == _docID) {
        // If tuple and cell are not found, just move to the next entity
        // (SRN-17), and to the next cell (SRN-24)
        if (!findNode(nodes)) {
          if (this.nextDoc() != NO_MORE_DOCS) {
            this.nextPosition(); // advance to the first position (SRN-24)
            return this.docID();
          }
          // position stream exhausted
          Arrays.fill(_curNode, NO_MORE_DOCS); // sentinel value
          _docID = _pos = NO_MORE_DOCS;
          return NO_MORE_DOCS;
        }
      }
      return this.docID();
    }
    // position stream exhausted
    Arrays.fill(_curNode, NO_MORE_DOCS); // sentinel value
    _docID = _pos = NO_MORE_DOCS;
    return NO_MORE_DOCS;
  }
  
  @Override
  public int nextPosition()
  throws IOException {
//    if (_isFirstTime)
//      throw new RuntimeException("Invalid call, nextDoc should be called first.");

    if (++_posPtr < freq()) {
      this.loadBranch();
      return _pos;
    }

    return NO_MORE_POS;
  }

  @Override
  protected void loadBranch()
  throws IOException {
    _pos = _dAndpEnum.nextPosition();
    final AbstractSirenPayload payload = getSirenPayload();
    _curNode[0] = _curNode[0] == -1 ? payload.getTupleID() : _curNode[0] + payload.getTupleID();
    if (_curNode[1] == -1 || payload.getTupleID() != 0) {
      _curNode[1] = payload.getCellID();
    }
    else { // if (payload.getTupleID() == 0)
      _curNode[1] += payload.getCellID();
    }
  }

  @Override
  public int nextDoc()
  throws IOException {
    final int nodeID;
    if ((nodeID = _dAndpEnum.nextDoc()) == NO_MORE_DOCS) {
      Arrays.fill(_curNode, NO_MORE_DOCS); // sentinel value
      _docID = _pos = NO_MORE_DOCS;
      return NO_MORE_DOCS;
    }
    Arrays.fill(_curNode, -1);
    _pos = -1;
    _docID = nodeID;
//    _isFirstTime = false;
    _posPtr = -1;
    return nodeID;
  }

}

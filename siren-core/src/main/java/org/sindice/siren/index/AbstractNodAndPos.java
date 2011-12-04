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
 * @author Campinas Stephane [ 1 Dec 2011 ]
 * @link stephane.campinas@deri.org
 */
package org.sindice.siren.index;

import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.util.AttributeSource;


/**
 * 
 */
public abstract class AbstractNodAndPos extends NodAndPosEnum {
  
  private AttributeSource atts = null;
  
  protected int _docID = -1;
  protected int _pos = -1;
  /** index of the next element to be read or written */
  protected int _posPtr = -1;
  
  /**
   * @param config
   */
  public AbstractNodAndPos(NodesConfig config) {
    super(config);
  }
  
  /** Returns the related attributes. */
  public AttributeSource attributes() {
    if (atts == null) atts = new AttributeSource();
    return atts;
  }
  
  @Override
  public int docID() {
    return _docID;
  }
  
  @Override
  public int pos() {
    return _pos;
  }
  
  @Override
  public int[] node() {
    return _curNode;
  }
  
  /**
   * Return true if the searched node hasn't been reached yet.
   * @param nodes
   * @param index
   * @return
   */
  private boolean isBefore(int[] nodes, int index) {
    boolean res = _curNode[index] < nodes[index];
    
    while (--index >= 0) {
      res = _curNode[index] == nodes[index] && res;
    }
    return res;
  }
  
  /**
   * Advance to the node right after the one passed in argument
   * @param nodes
   * @return
   * @throws IOException
   */
  protected boolean findNode(int[] nodes)
  throws IOException {
    if (nodes[0] == -1) {
      return true;
    }
    
    // Index of the deepest node we can go
    int maxIndex = 0;
    for (int i = 0; i < nodes.length; i++) {
      if (nodes[i] == -1) {
        break;
      }
      maxIndex = i;
    }
    while (++_posPtr < freq()) {
      this.loadBranch();
      boolean match = true;
      for (int i = 0; i <= maxIndex; i++) {
        if (isBefore(nodes, i)) {
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
  
  protected abstract void loadBranch() throws IOException;


  @Override
  public void setToSentinel() {
    _pos = _docID = NO_MORE_DOCS;
    Arrays.fill(_curNode, NO_MORE_DOCS);
  }


  @Override
  public void setLayersToSentinel() {
    _docID = NO_MORE_DOCS;
    Arrays.fill(_curNode, NO_MORE_DOCS);
  }

}

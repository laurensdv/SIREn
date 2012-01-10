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
 * @author Renaud Delbru [ 8 Dec 2009 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.analysis.attributes;

import java.io.Serializable;
import java.nio.IntBuffer;

import org.apache.lucene.util.ArrayUtil;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.IntsRef;

/**
 * The node path of a token.
 */
public class NodeAttributeImpl extends AttributeImpl implements NodeAttribute, Cloneable, Serializable {

  private final IntsRef ref = new IntsRef(2);
  protected IntBuffer node = IntBuffer.allocate(2);
  private boolean isFlipped = false;

  private static final long serialVersionUID = -2226786769372232588L;

  /**
   * Returns this Token's node path.
   */
  public IntsRef node() {
    if (!isFlipped) {
      node.flip();
      isFlipped = true;
    }

    ref.ints = ArrayUtil.grow(ref.ints, node.limit());
    System.arraycopy(node.array(), node.position(), ref.ints, 0, node.limit());
    ref.offset = node.position();
    ref.length = node.limit();

    return ref;
  }

  @Override
  public void clear() {
    node.clear();
    isFlipped = false;
  }

  @Override
  public boolean equals(final Object other) {
    if (other == this) {
      return true;
    }

    if (other instanceof NodeAttributeImpl) {
      final NodeAttributeImpl o = (NodeAttributeImpl) other;
      return node.equals(o.node);
    }

    return false;
  }

  @Override
  public int hashCode() {
    return node.hashCode();
  }

  @Override
  public void copyTo(final AttributeImpl target) {
    final NodeAttributeImpl t = (NodeAttributeImpl) target;
    t.node.clear();
    t.node.put(node);
  }

  @Override
  public void append(final int nodeID) {
    this.ensureIntBufferCapacity(node.position() + 1);
    node.put(nodeID);
  }

  /**
   * Increases the capacity of the <tt>IntBuffer</tt> instance, if
   * necessary, to ensure that it can hold at least the number of elements.
   */
  private void ensureIntBufferCapacity(final int target) {
    if (node.capacity() < target) {
      final IntBuffer buffer = IntBuffer.allocate(target);
      node.flip();
      buffer.put(node);
      node = buffer;
    }
  }

  @Override
  public void copyNode(final IntsRef nodePath) {
    node.clear();
    this.ensureIntBufferCapacity(nodePath.length);
    node.put(nodePath.ints, nodePath.offset, nodePath.length);
  }

}

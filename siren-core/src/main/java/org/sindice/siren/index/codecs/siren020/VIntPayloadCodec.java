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
 * @author Renaud Delbru [ 5 Feb 2009 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.index.codecs.siren020;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IntsRef;
import org.sindice.siren.util.CodecUtils;

/**
 * An implementation of the SIREn payload using Variable Int encoding.
 */
public class VIntPayloadCodec extends PayloadCodec {

  ByteBuffer bb = ByteBuffer.allocate(10);

  /**
   * Used in {@link #decode()} and {@link #encode()}
   */
  BytesRef bytes = new BytesRef();

  IntBuffer ib = IntBuffer.allocate(2);

  /**
   * Used in {@link #decode()} and {@link #encode()}
   */
  IntsRef ints = new IntsRef();

  @Override
  public IntsRef decode(final BytesRef data) {
    // max case : 1 byte = 1 int
    this.ensureIntBufferSize(data.length);

    ib.clear();

    this.setBytesRef(data.bytes, data.offset, data.length);

    while (bytes.offset < bytes.length) {
      ib.put(CodecUtils.byteArrayToVInt(bytes));
    }

    ib.flip();

    this.setIntsRef(ib.array(), ib.position(), ib.limit());

    return ints;
  }

  @Override
  public BytesRef encode(final IntsRef data) {
    // max case : 1 int = 5 bytes
    this.ensureByteBufferSize(data.length * 5);

    bb.clear();

    this.setIntsRef(data.ints, data.offset, data.length);

    for (int i = ints.offset; i < ints.length; i++) {
      CodecUtils.vIntToByteArray(ints.ints[i], bb);
    }

    bb.flip();

    this.setBytesRef(bb.array(), bb.position(), bb.limit());

    return bytes;
  }

  private void setBytesRef(final byte[] data, final int offset, final int length) {
    bytes.bytes = data;
    bytes.length = length;
    bytes.offset = offset;
  }

  private void setIntsRef(final int[] data, final int offset, final int length) {
    ints.ints = data;
    ints.length = length;
    ints.offset = offset;
  }

  private void ensureByteBufferSize(final int size) {
    if (bb.capacity() < size) {
      bb = ByteBuffer.allocate(size);
    }
  }

  private void ensureIntBufferSize(final int size) {
    if (ib.capacity() < size) {
      ib = IntBuffer.allocate(size);
    }
  }

}

/**
 * Copyright (c) 2009-2012 National University of Ireland, Galway. All Rights Reserved.
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
 * @author Renaud Delbru [ 19 Apr 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.index.codecs.block;

import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IntsRef;

public class VIntBlockDecompressor extends BlockDecompressor {

  @Override
  public void decompress(final BytesRef input, final IntsRef output) {
    final byte[] compressedData = input.bytes;
    final int[] unCompressedData = output.ints;

    while (input.offset < input.length) {
      byte b = compressedData[input.offset++];
      int i = b & 0x7F;
      for (int shift = 7; (b & 0x80) != 0; shift += 7) {
        b = compressedData[input.offset++];
        i |= (b & 0x7F) << shift;
      }
      unCompressedData[output.offset++] = i;
    }

    input.offset = 0;
    output.length = output.offset;
    output.offset = 0;
  }

}

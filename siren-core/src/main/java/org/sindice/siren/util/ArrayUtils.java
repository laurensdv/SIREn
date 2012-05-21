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
 * @author Renaud Delbru [ 28 Mar 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.util;

import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IntsRef;

public class ArrayUtils {

  /**
   * Increase the size of the array if needed. Do not copy the content of the
   * original array into the new one.
   */
  public static final int[] grow(final int[] array, final int minSize) {
    assert minSize >= 0: "size must be positive (got " + minSize + "): likely integer overflow?";
    if (array.length < minSize) {
      final int[] newArray = new int[minSize];
      return newArray;
    } else {
      return array;
    }
  }

  /**
   * Increase the size of the array if needed. Do not copy the content of the
   * original array into the new one.
   */
  public static final IntsRef grow(final IntsRef ref, final int minSize) {
    ref.ints = grow(ref.ints, minSize);
    return ref;
  }

  /**
   * Increase the size of the array if needed. Do not copy the content of the
   * original array into the new one.
   */
  public static final byte[] grow(final byte[] array, final int minSize) {
    assert minSize >= 0: "size must be positive (got " + minSize + "): likely integer overflow?";
    if (array.length < minSize) {
      final byte[] newArray = new byte[minSize];
      return newArray;
    } else {
      return array;
    }
  }

  /**
   * Increase the size of the array if needed. Do not copy the content of the
   * original array into the new one.
   */
  public static final BytesRef grow(final BytesRef ref, final int minSize) {
    ref.bytes = grow(ref.bytes, minSize);
    return ref;
  }

}

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
 * @project siren-benchmark
 * @author Renaud Delbru [ 9 Jul 2012 ]
 * @link http://renaud.delbru.fr/
 * @author Campinas Stephane [ 3 Jun 2011 ]
 * @link stephane.campinas@deri.org
 */
package org.sindice.siren.benchmark.util;

public class StringUtil {

  private static final int BYTE_RANGE = (1 + Byte.MAX_VALUE) - Byte.MIN_VALUE;
  private static byte[] ALL_BYTES = new byte[BYTE_RANGE];

  /**
   * Efficient byte to char conversion
   */
  public static char[] BYTE_TO_CHARS = new char[BYTE_RANGE];

  static {
    for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
      ALL_BYTES[i - Byte.MIN_VALUE] = (byte) i;
    }
    final String allBytesString = new String(ALL_BYTES, 0, Byte.MAX_VALUE - Byte.MIN_VALUE);
    for (int i = 0; i < (Byte.MAX_VALUE - Byte.MIN_VALUE); i++) {
      BYTE_TO_CHARS[i] = allBytesString.charAt(i);
    }
  }

}

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
 * @author Renaud Delbru [ 24 Jul 2010 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.index.codecs.siren020;


import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IntsRef;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestVIntSirenPayload {

  VIntPayloadCodec codec = new VIntPayloadCodec();

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp()
  throws Exception {}

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown()
  throws Exception {}

  @Test
  public void testSimpleVInt()
  throws Exception {
    IntsRef ints = new IntsRef(new int[] { 12,43 }, 0, 2);
    BytesRef bytes = codec.encode(ints);
    IntsRef result = codec.decode(bytes);

    assertEquals(ints.ints[0], result.ints[0]);
    assertEquals(ints.ints[1], result.ints[1]);

    ints = new IntsRef(new int[] { 3, 2 }, 0, 2);
    bytes = codec.encode(ints);
    result = codec.decode(bytes);

    assertEquals(ints.ints[0], result.ints[0]);
    assertEquals(ints.ints[1], result.ints[1]);

    ints = new IntsRef(new int[] { 0, 1 }, 0, 2);
    bytes = codec.encode(ints);
    result = codec.decode(bytes);

    assertEquals(ints.ints[0], result.ints[0]);
    assertEquals(ints.ints[1], result.ints[1]);
  }

  @Test
  public void testRandomVInt2()
  throws Exception {
    final Random r = new Random(42);
    for (int i = 0; i < 10000; i++) {
      final int value1 = r.nextInt(Integer.MAX_VALUE);
      final int value2 = r.nextInt(Integer.MAX_VALUE);

      final IntsRef ints = new IntsRef(new int[] { value1,value2 }, 0, 2);
      final BytesRef bytes = codec.encode(ints);
      final IntsRef result = codec.decode(bytes);

      assertEquals(ints.ints[0], result.ints[0]);
      assertEquals(ints.ints[1], result.ints[1]);
    }
  }

  @Test
  public void testRandomVInt3()
  throws Exception {
    final Random r = new Random(42);
    for (int i = 0; i < 10000; i++) {
      final int value1 = r.nextInt(Integer.MAX_VALUE);
      final int value2 = r.nextInt(Integer.MAX_VALUE);
      final int value3 = r.nextInt(Integer.MAX_VALUE);

      final IntsRef ints = new IntsRef(new int[] { value1,value2,value3 }, 0, 3);
      final BytesRef bytes = codec.encode(ints);
      final IntsRef result = codec.decode(bytes);

      assertEquals(ints.ints[0], result.ints[0]);
      assertEquals(ints.ints[1], result.ints[1]);
      assertEquals(ints.ints[2], result.ints[2]);
    }
  }

}

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
package org.sindice.siren.index.codecs;

import org.apache.lucene.store.Directory;
import org.apache.lucene.util._TestUtil;
import org.junit.After;
import org.junit.Before;
import org.sindice.siren.util.SirenTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CodecTestCase extends SirenTestCase {

  private static final int MIN_LIST_SIZE = 32768;

  protected Directory directory;

  @Override
  @Before
  public void setUp()
  throws Exception {
    super.setUp();
    directory = newDirectory();
  }

  @Override
  @After
  public void tearDown()
  throws Exception {
    directory.close();
    super.tearDown();
  }

  /**
   * The different block sizes to test
   */
  protected static final int[] BLOCK_SIZES = {32, 256, 512, 2048};

  protected static final Logger logger = LoggerFactory.getLogger(CodecTestCase.class);

  /**
   * Generate a random long value uniformly distributed between
   * <code>lower</code> and <code>upper</code>, inclusive.
   *
   * @param lower
   *            the lower bound.
   * @param upper
   *            the upper bound.
   * @return the random integer.
   * @throws IllegalArgumentException if {@code lower >= upper}.
   */
  private long nextLong(final long lower, final long upper) {
      if (lower >= upper) {
        throw new IllegalArgumentException();
      }
      final double r = random().nextDouble();
      return (long) ((r * upper) + ((1.0 - r) * lower) + r);
  }

  public void doTestIntegerRange(final int minBits, final int maxBits, final int[] blockSizes) throws Exception {
    for (int i = minBits; i <= maxBits; i++) {
      // different length for each run
      final int length = _TestUtil.nextInt(random(), MIN_LIST_SIZE, MIN_LIST_SIZE * 2);
      final int[] input = new int[length];

      final long min = i == 1 ? 0 : (1L << (i - 1));
      final long max = ((1L << i) - 1);

      for (int j = 0; j < input.length; j++) {
        input[j] = (int) this.nextLong(min, max);
      }

      for (final int blockSize : blockSizes) {
        logger.debug("Perform Integer Range Test: length = {}, bits = {}, block size = {}",
          new Object[]{input.length, i, blockSize});
        this.doTest(input, blockSize);
      }
    }
  }

  public void doTestIntegerRange(final int minBits, final int maxBits) throws Exception {
    this.doTestIntegerRange(minBits, maxBits, BLOCK_SIZES);
  }

  protected abstract void doTest(int[] input, int blockSize) throws Exception;

}

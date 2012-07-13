/**
 * Copyright (c) 2009-2012 National University of Ireland, Galway. All Rights Reserved.
 *
 * Project and contact information: http://www.siren.sindice.com/
 *
 * This file is part of the SIREn project and is derived from the "benchmarking
 * framework" of Elliptic Group, Inc. You can find the original source code on
 * <http://www.ellipticgroup.com/html/benchmarkingArticle.html>.
 *
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * @project siren-benchmark
 * @author Renaud Delbru [ 6 Jul 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.benchmark.util;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* Provides static utility methods relating to {@link Random}.
* <p>
* This class is multithread safe: all of its fields are multithread safe types.
* <p>
* @author Brent Boyer
* @see <a href="http://en.wikipedia.org/wiki/Hardware_random-number_generator">Wikipedia article on random numbers</a>
* @see <a href="http://www.softpanorama.org/Algorithms/random_generators.shtml">Random Generators</a>
* @see <a href="http://burtleburtle.net/bob/rand/testsfor.html">Tests for Random Number Generators</a>
* @see <a href="http://burtleburtle.net/bob/hash/hashfaq.html#dist">The chi-squared test</a>
* @see <a href="http://www.cs.berkeley.edu/~daw/rnd/java-spinner">Java code using thread behavior to generate random numbers</a>
*/
public final class RandomUtil {

  private static final Logger logger = LoggerFactory.getLogger(RandomUtil.class);

  // -------------------- static fields --------------------

  private static final AtomicLong id = new AtomicLong();

  private static final ThreadLocal<MersenneTwisterFast> threadLocal = new ThreadLocal<MersenneTwisterFast>() {
    @Override
    protected MersenneTwisterFast initialValue() {
      //return new Random( makeSeed() );
      return new MersenneTwisterFast( makeSeed() );
    }
  };

  // -------------------- makeSeedXXX --------------------

  /**
  * This method first attempts to generate a high quality seed by calling {@link #makeSeedSecure() makeSeedSecure}.
  * This result should be of extremely high, cryptographic quality
  * (i.e. possibly come in part from a low level hardware source like diode noise, or at least from something like /dev/random,
  * as well as perhaps also satisfy some uniqueness aspects).
  * If any Exception is thrown by this step, it is caught and a value of 0 is assigned to the seed.
  * <p>
  * This method then generates an additional seed by calling {@link #makeSeedUnique makeSeedUnique}
  * which is highly likely to satisfy some uniqueness requirements.
  * This second seed defends against potential problems in certain implementations of SecureRandom (or lack thereof) on some platforms.
  * <p>
  * A bitwise XOR of the two seeds is finally returned.
  * <p>
  * @see <a href="http://forum.java.sun.com/thread.jspa?threadID=590499">My forum posting</a>
  */
  public static long makeSeed() {
    long seedPossiblyHighQuality;
    try {
      seedPossiblyHighQuality = makeSeedSecure();
      logger.info("Good: makeSeedSecure worked, so makeSeed will use a cryptographically strong seed as part of its initialization");
    }
    catch (final Throwable t) {
      logger.info("Note: makeSeedSecure generated the following Throwable, so makeSeed will fall back on another algorithm", t);
      seedPossiblyHighQuality = 0L;
    }

    return seedPossiblyHighQuality ^ makeSeedUnique();
  }

  /**
  * Returns <code>{@link #makeSeedSecure(String) makeSeedSecure}("SHA1PRNG")</code>.
  * <p>
  * @throws NoSuchAlgorithmException if the SHA1PRNG algorithm is not available in the caller's environment
  */
  public static long makeSeedSecure() throws NoSuchAlgorithmException {
    return makeSeedSecure("SHA1PRNG");
  }

  /**
  * Returns <code>{@link #makeSeedSecure(SecureRandom) makeSeedSecure}( {@link SecureRandom#getInstance(String) SecureRandom.getInstance}(algorithm) )</code>.
  * <p>
  * @throws NoSuchAlgorithmException if the requested algorithm is not available in the caller's environment
  */
  public static long makeSeedSecure(final String algorithm) throws NoSuchAlgorithmException {
    return makeSeedSecure( SecureRandom.getInstance(algorithm) );
  }

  /**
  * Returns <code>{@link #makeSeedSecure(SecureRandom) makeSeedSecure}( {@link SecureRandom#getInstance(String, String) SecureRandom.getInstance}(algorithm, provider) )</code>.
  * <p>
  * @throws NoSuchAlgorithmException if the requested algorithm is not available from the provider
  * @throws NoSuchProviderException if the provider has not been configured
  * @throws IllegalArgumentException if the provider name is null or empty
  */
  public static long makeSeedSecure(final String algorithm, final String provider) throws NoSuchAlgorithmException, NoSuchProviderException, IllegalArgumentException {
    return makeSeedSecure( SecureRandom.getInstance(algorithm, provider) );
  }

  /**
  * Returns <code>{@link NumberUtil#bytesBigEndianToLong NumberUtil.bytesBigEndianToLong}( {@link SecureRandom#generateSeed random.generateSeed}(8) )</code>.
  * <p>
  * @throws IllegalArgumentException if random == null
  */
  public static long makeSeedSecure(final SecureRandom random) throws IllegalArgumentException {
    if (random == null) {
      throw new IllegalArgumentException();
    }

    return bytesBigEndianToLong( random.generateSeed(8) );
  }

  /**
  * Converts an array of 8 bytes in big endian order (i.e. bytes[0] is the most significant byte) to a long which is returned.
  * Just like {@link DataInputStream#readLong DataInputStream.readLong}, the bytes are treated as unsigned bit groups.
  * <p>
  * @throws IllegalArgumentException if bytes == null; bytes.length != 8
  */
  public static long bytesBigEndianToLong(final byte[] bytes) throws IllegalArgumentException {
    if (bytes == null || bytes.length != 8) {
      throw new IllegalArgumentException();
    }

    final long bitsHigh =
      ((bytes[0] & 0xFFL) << 56) |  // performance optimization: use bitwise or instead of addition, since in theory should be faster to implement in hardware (no carry over to worry about)
      ((bytes[1] & 0xFFL) << 48) |
      ((bytes[2] & 0xFFL) << 40) |
      ((bytes[3] & 0xFFL) << 32);

    final long bitsLow = 0xFFFFFFFFL & (  // performance optimization: do a single conversion of all the low order bits to a long here
      ((bytes[4] & 0xFF) << 24) |
      ((bytes[5] & 0xFF) << 16) |
      ((bytes[6] & 0xFF) <<  8) |
      (bytes[7] & 0xFF)
    );

    return bitsHigh | bitsLow;
    // see also the code in DataInputStream.readLong; I find my version to be more readable; benchmarking indicates identical performance
  }

  /**
  * This seed value generating function <i>attempts</i> to satisfy these goals:
  * <ol>
  *  <li>return a unique result for each call of this method</li>
  *  <li>return a unique series of results for each different JVM invocation</li>
  *  <li>return results which are uniformly spread around the range of all possible long values</li>
  * </ol>
  * It uses the following techniques:
  * <ol>
  *  <li>
  *   an internal serial id field is incremented upon each call, so each call is guaranteed a different value;
  *   this field determines the high order bits of the result
  *  </li>
  *  <li>
  *   each call uses the result of {@link System#nanoTime System.nanoTime}
  *   to determine the low order bits of the result;
  *   this should be different each time the JVM is run (assuming that the system time is different)
  *  </li>
  *  <li>a hash algorithm is applied to the above numbers before putting them into the high and low order parts of the result</li>
  * </ol>
  * <p>
  * <b>Warnings:</b>
  * <ol>
  *  <li>
  *   the uniqueness goals cannot be guaranteed because the hash algorithm, while it is of high quality,
  *   is not guaranteed to be a 1-1 function (i.e. 2 different input ints might get mapped to the same output int).
  *  </li>
  *  <li>
  *   the result returned by this method is not cryptographically strong because there is insufficient entropy
  *   in the sources used (serial number and system time)
  *  </li>
  * </ol>
  */
  public static long makeSeedUnique() {
    final long hashSerialNumber = HashUtil.hash( id.incrementAndGet() );
    final long hashTime = HashUtil.hash( System.nanoTime() );

    final long bitsHigh = (hashSerialNumber << 32);
    final long bitsLow = hashTime;

    return bitsHigh | bitsLow;
  }

  // -------------------- get --------------------

  /** Returns a MersenneTwisterFast instance that is local to the calling thread, so thread contention is guaranteed to never occur. */
  public static MersenneTwisterFast get() {
    return threadLocal.get();
  }

  // -------------------- constructor --------------------

  /** This private constructor suppresses the default (public) constructor, ensuring non-instantiability. */
  private RandomUtil() {}

}


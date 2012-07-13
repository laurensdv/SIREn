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

import java.security.MessageDigest;

/**
* Provides various static utility methods for dealing with hashes.
* <p>
* This class is multithread safe: it is immutable (both its immediate state, as well as the deep state of its fields).
* <p>
* @author Brent Boyer
*/
public final class HashUtil {

  // -------------------- constants --------------------

  /** A 31-bit prime number. */
  private static final int prime1 = 1491735241; // hex: 0x58EA12C9

  /** A 31-bit prime number. */
  private static final int prime2 = 2041543619; // hex: 0x79AF7BC3

  // -------------------- enhance --------------------

  /**
  * Attempts to return a very high quality hash function on h
  * (i.e. one that is uniformly distributed among all possible int values, .
  * <p>
  * This method is needed if h is initially a poor quality hash.
  * Prime example: {@link Integer#hashCode Integer.hashCode} simply returns the int value, which is an extremely bad hash.
  * <p>
  * The implementation here first attempts to use the extremely strong SHA-1 hash algorithm on h.
* If any problem occurs (e.g. the algorithm is unavailable), {@link #enhanceFallback4 enhanceFallback4}(h) is returned.
  * <p>
  * @see <a href="http://java.sun.com/j2se/1.5.0/docs/guide/security/CryptoSpec.html#MessageDigest">Sun documentation on MessageDigest</a>
  * @see <a href="http://java.sun.com/j2se/1.5.0/docs/guide/security/CryptoSpec.html#MDEx">Sun documentation on MessageDigest</a>
  * @see <a href="http://webcat.sourceforge.net/javadocs/pt/tumba/parser/RabinHashFunction.html">Rabin fingerprint hash method</a>
  * @see <a href="http://forum.java.sun.com/thread.jspa?threadID=590499">My forum posting</a>
  */
  public static int enhance(final int h) {
    try {
      final MessageDigest md = MessageDigest.getInstance("SHA-1");
      md.update( (byte) (h >>> 24) );
      md.update( (byte) (h >>> 16) );
      md.update( (byte) (h >>>  8) );
      md.update( (byte) (h >>>  0) );
      final byte[] digest = md.digest();
      int digestAsInt = 0;
      for (int i = 0; i < digest.length; i++) {
        final int shift = (i % 4) * 8;  // produces 0, 8, 16, 24 (i.e. with the << operator below, this puts the byte's bits in the correct slot in digestAsInt)
        digestAsInt ^= ((digest[i] & 0xFF) << shift);
      }
      return digestAsInt;
    }
    catch (final Exception e) {
      return enhanceFallback4(h); // seems to produce better results; see reply 28 of http://forum.java.sun.com/thread.jspa?threadID=590499
    }
  }

  /**
  * Returns a quick to compute hash of the input h.
  * Only used by {@link #enhance enhance} as a fallback algorithm in the event of a problem.
  */
  public static int enhanceFallback1(int h) {
// +++ the code below was taken from java.util.HashMap.hash--is this a published, known algorithm?  is there a better one?
/*
Personal email from Doug Lea:
  Josh Bloch (mostly) and I arrived at this by considering all
  transformations less than some number (that I forget) of instructions
  (and not considering offsets by constants etc), and then checked how
  they filled commonly sized power-of-two-sized hashtables for all
  possible inputs. The one we kept fared best. Someday we intend
  to write this up somewhere. The balance between getting good spread
  and low overhead (and inlinability) of the scrambling function seems
  to be about right.  There's no pretense that this works well for other
  purposes or even other kinds of tables, so I'm not surprised it
  doesn't look as good as some others using other metrics.
*/
    h += ~(h << 9);
    h ^=  (h >>> 14);
    h +=  (h << 4);
    h ^=  (h >>> 10);
    return h;
  }

  /**
  * Returns a quick to compute hash of the input h.
  * Only used by {@link #enhance enhance} as a fallback algorithm in the event of a problem.
  */
  private static int enhanceFallback2(int h) {
// +++ the code below was taken from MersenneTwisterFast (http://www.cs.umd.edu/users/seanl/gp/mersenne/MersenneTwisterFast.java)--is this a published, known algorithm?  is there a better one?
    h ^= (h >>> 11);
    h ^= (h << 7) & 0x9D2C5680;
    h ^= (h << 15) & 0xEFC60000;
    h ^= (h >>> 18);
    return h;
  }

  /**
  * Returns a quick to compute hash of the input h.
  * Only used by {@link #enhance enhance} as a fallback algorithm in the event of a problem.
  */
  private static int enhanceFallback3(final int h) {
// +++ the code below was taken from reply #27 of http://forum.java.sun.com/thread.jspa?threadID=590499&start=15&tstart=0
    return (prime1 * h) + prime2;
  }

  private static int enhanceFallback4(final int h) {
// +++ this code is a concatenation of all the fast algorithms; it seems to be better than any individual one
    return enhanceFallback2( enhanceFallback3( enhanceFallback1( h ) ) );
  }

  // -------------------- hash --------------------

  /** Returns a high quality hash for the double arg d. */
  public static int hash(final double d) {
    final long v = Double.doubleToLongBits(d);
    return enhance(
      (int) (v ^ (v >>> 32))  // the algorithm on this line is the same as that used in Double.hashCode
    );
  }

  /** Returns a high quality hash for the long arg l. */
  public static int hash(final long l) {
    return enhance(
      (int) (l ^ (l >>> 32))  // the algorithm on this line is the same as that used in Long.hashCode
    );
  }

  // -------------------- constructor --------------------

  /** This private constructor suppresses the default (public) constructor, ensuring non-instantiability. */
  private HashUtil() {}

}


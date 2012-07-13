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

import java.lang.management.ManagementFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JvmUtils {

  static final Logger logger = LoggerFactory.getLogger(JvmUtils.class);

  /**
   * Specifies the maximum number of loops that {@link #restoreJvm restoreJvm}
   * will execute.
   **/
  private static final int maxRestoreJvmLoops = 100;

  /**
   * Tries to restore the JVM to as clean a state as possible.
   * <p>
   * The first technique is a request for object finalizers to run
   * (via a call to {@link System#runFinalization System.runFinalization}).
   * The second technique is a request for garbage collection to run
   * (via a call to {@link System#gc System.gc}).
   * <p>
   * These calls are done in a loop that executes at least once,
   * and at most {@link #maxRestoreJvmLoops} times,
   * but will execute fewer times if no more objects remain to be finalized and
   * heap memory usage becomes constant.
   * <p>
   * <a href="http://www.javaworld.com/javaworld/javatips/jw-javatip130.html">This article</a>
   * suggested the idea to aggressively call for garbage collection many times,
   * and to use heap memory as a metric for deciding when can stop garbage collecting.
   */
   public static void cleanJvm() {
     long memUsedPrev = memoryUsed();
     for (int i = 0; i < maxRestoreJvmLoops; i++) {
       // see also: http://java.sun.com/developer/technicalArticles/javase/finalization/
       System.runFinalization();
       System.gc();

       final long memUsedNow = memoryUsed();
       final int oCount = ManagementFactory.getMemoryMXBean().getObjectPendingFinalizationCount();
       // break early if have no more finalization and get constant mem used
       if ((oCount == 0) && (memUsedNow >= memUsedPrev)) {
         logger.debug("ZERO objects pending finalization and ACHIEVED STABLE " +
         		"MEMORY (memUsedNow = " + memUsedNow + " >= memUsedPrev = " + memUsedPrev + ") at i = " + i);
         break;
       }
       else {
         logger.debug(oCount + " objects pending finalization and memory not " +
         		"stable (memUsedNow = " + memUsedNow + " < memUsedPrev = " + memUsedPrev + ") at i = " + i);
         memUsedPrev = memUsedNow;
       }
     }
   }

   /** Returns how much memory on the heap is currently being used. */
   public static long memoryUsed() {
     final Runtime rt = Runtime.getRuntime();
     return rt.totalMemory() - rt.freeMemory();
   }

}

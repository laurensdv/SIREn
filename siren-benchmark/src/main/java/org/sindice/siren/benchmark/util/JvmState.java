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

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.ManagementFactory;

public class JvmState {

  /** Count of the total number of classes that have been loaded by this JVM. */
  protected long countClassesLoaded;

  /** Count of the total number of classes that have been unloaded by this JVM. */
  protected long countClassesUnloaded;

  /** Records the accumlated elapsed time (in milliseconds) spent in compilation. */
  protected long compilationTimeTotal;

  /** Constructor. */
  public JvmState() {
    final ClassLoadingMXBean loadBean = ManagementFactory.getClassLoadingMXBean();
    countClassesLoaded = loadBean.getTotalLoadedClassCount();
    countClassesUnloaded = loadBean.getUnloadedClassCount();

    final CompilationMXBean compBean = ManagementFactory.getCompilationMXBean();
    if (compBean.isCompilationTimeMonitoringSupported()) {
      compilationTimeTotal = compBean.getTotalCompilationTime();
    }
    else {
      compilationTimeTotal = -1;
    }
  }

  /**
  * Determines equality based on whether or not obj is a <code>JvmState</code>
  * instance whose every field equals that of this instance.
  */
  @Override
  public final boolean equals(final Object obj) { // for why is final, see the essay stored in the file equalsImplementation.txt
    if (this == obj) return true;
    if (!(obj instanceof JvmState)) return false;

    final JvmState other = (JvmState) obj;
    return
      (this.countClassesLoaded == other.countClassesLoaded) &&
      (this.countClassesUnloaded == other.countClassesUnloaded) &&
      (this.compilationTimeTotal == other.compilationTimeTotal);
  }

  /** Returns a value based on all of the fields. */
  @Override
  public final int hashCode() { // for why is final, see the essay stored in the file equalsImplementation.txt
    return
      HashUtil.hash(countClassesLoaded) ^
      HashUtil.hash(countClassesUnloaded) ^
      HashUtil.hash(compilationTimeTotal);
  }

  /**
  * Returns a <code>String</code> report of the differences, if any, between
  * this instance and other.
  * <p>
  * Contract: the result is never <code>null</code>, but will be zero-length if
  * there is no difference.
  */
  public String difference(final JvmState other) {
    final StringBuilder sb = new StringBuilder(64);

    final long loadedDiff = this.countClassesLoaded - other.countClassesLoaded;
    if (loadedDiff < 0) {
      if (sb.length() > 0) sb.append(", ");
      sb.append("class load count DECREASED by ").append(-loadedDiff).append(" (IS THIS AN ERROR?)");
    }
    else if (loadedDiff > 0) {
      if (sb.length() > 0) sb.append(", ");
      sb.append(loadedDiff).append(" classes loaded");
    }

    final long unloadedDiff = this.countClassesUnloaded - other.countClassesUnloaded;
    if (unloadedDiff < 0) {
      if (sb.length() > 0) sb.append(", ");
      sb.append("class unload count DECREASED by ").append(-unloadedDiff).append(" (IS THIS AN ERROR?)");
    }
    else if (unloadedDiff > 0) {
      if (sb.length() > 0) sb.append(", ");
      sb.append(unloadedDiff).append(" classes unloaded");
    }

    final long compTimeDiff = this.compilationTimeTotal - other.compilationTimeTotal;
    if (compTimeDiff < 0) {
      if (sb.length() > 0) sb.append(", ");
      sb.append("compilation time DECREASED by ").append(-compTimeDiff).append(" ms (IS THIS AN ERROR?)");
    }
    else if (compTimeDiff > 0) {
      if (sb.length() > 0) sb.append(", ");
      sb.append(compTimeDiff).append(" ms of compilation occured");
    }

    return sb.toString();
  }

}

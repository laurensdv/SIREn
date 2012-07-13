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
 * @author Renaud Delbru [ 6 Jul 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.benchmark.util;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class BenchmarkTimer {

  private long startCpuTime, stopCpuTime;
  private long startUserTime, stopUserTime;
  private long startWallTime, stopWallTime;

  private final ThreadMXBean bean;

  public BenchmarkTimer() {
    bean = ManagementFactory.getThreadMXBean();
    if (!bean.isCurrentThreadCpuTimeSupported()) {
      throw new RuntimeException("The JVM does not support CPU time measurement for the current thread.");
    }
  }

  /**
   * Start recording time.
   */
  public void start() {
    startWallTime = System.nanoTime();
    startCpuTime = bean.getCurrentThreadCpuTime();
    startUserTime = bean.getCurrentThreadUserTime();
  }

  /**
   * Stop recording time.
   */
  public void stop() {
    stopUserTime = bean.getCurrentThreadUserTime();
    stopCpuTime = bean.getCurrentThreadCpuTime();
    stopWallTime = System.nanoTime();
  }

  /**
   * Return the last CPU recorded time.
   */
  public long getRecordedCpuTime() {
    return stopCpuTime - startCpuTime;
  }

  /**
   * Return the last system recorded time.
   */
  public long getRecordedSystemTime() {
    return (stopCpuTime - stopUserTime) - (startCpuTime - startUserTime);
  }

  /**
   * Return the last user time recorded.
   */
  public long getRecordedUserTime() {
    return stopUserTime - startUserTime;
  }

  /**
   * Return the last wall time recorded.
   */
  public long getRecordedWallTime() {
    return stopWallTime - startWallTime;
  }

  /** Get CPU time in nanoseconds. */
  public long getCpuTime() {
    return bean.getCurrentThreadCpuTime();
  }

  /** Get user time in nanoseconds. */
  public long getUserTime() {
    return bean.getCurrentThreadUserTime();
  }

  /** Get system time in nanoseconds. */
  public long getSystemTime() {
    return (bean.getCurrentThreadCpuTime() - bean.getCurrentThreadUserTime());
  }

}

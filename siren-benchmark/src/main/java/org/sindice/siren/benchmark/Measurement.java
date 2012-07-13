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
package org.sindice.siren.benchmark;

import org.sindice.siren.benchmark.util.BenchmarkTimer;
import org.sindice.siren.benchmark.util.JvmState;

public class Measurement {

  private long cpuTime = 0;
  private long userTime = 0;
  private long systemTime = 0;
  private long wallTime = 0;

  private long nHits = 0;

  private long nExecutions = 0;

  /**
   * Records {@link JvmState} just <i>after</i> the measurement ended.
   */
  protected JvmState jvmState;

  public Measurement() {}

  public Measurement(final long nHits) {
    this.setHits(nHits);
  }

  public void setJvmState(final JvmState state) {
    this.jvmState = state;
  }

  public JvmState getJvmState() {
    return this.jvmState;
  }

  public void setCPUTime(final long cpuTime) {
    this.cpuTime = cpuTime;
  }

  public long getCPUTime() {
    return this.cpuTime;
  }

  public void setUserTime(final long userTime) {
    this.userTime = userTime;
  }

  public long getUserTime() {
    return this.userTime;
  }

  public void setSystemTime(final long systemTime) {
    this.systemTime = systemTime;
  }

  public long getSystemTime() {
    return this.systemTime;
  }

  public void setWallTime(final long wallTime) {
    this.wallTime = wallTime;
  }

  public long getWallTime() {
    return this.wallTime;
  }

  public void setHits(final long nHits) {
    this.nHits = nHits;
  }

  public long getHits() {
    return this.nHits;
  }

  public long getHitsPerExecution() {
    return (this.nHits / nExecutions);
  }

  public void setNumberExecutions(final long nExecutions) {
    this.nExecutions = nExecutions;
  }

  public long getNumberExecutions() {
    return this.nExecutions;
  }

  public void setTimes(final BenchmarkTimer timer) {
    this.setWallTime(timer.getRecordedWallTime());
    this.setCPUTime(timer.getRecordedCpuTime());
    this.setUserTime(timer.getRecordedUserTime());
    this.setSystemTime(timer.getRecordedSystemTime());
  }

  public void add(final Measurement measurement) {
    this.nHits += measurement.getHits();
    this.wallTime += measurement.getWallTime();
    this.cpuTime += measurement.getCPUTime();
    this.systemTime += measurement.getSystemTime();
    this.userTime += measurement.getUserTime();
    this.nExecutions += measurement.getNumberExecutions();
  }

  @Override
  public final String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("executions=");
    builder.append(this.nExecutions);
    builder.append(",cpu=");
    builder.append(this.cpuTime);
    builder.append(",user=");
    builder.append(this.userTime);
    builder.append(",system=");
    builder.append(this.systemTime);
    builder.append(",wall=");
    builder.append(this.wallTime);
    builder.append(",hits=");
    builder.append(this.nHits);
    return builder.toString();
  }

}

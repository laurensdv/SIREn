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
package org.sindice.siren.benchmark.generator.viz.query;

import org.sindice.siren.benchmark.Stats;
import org.sindice.siren.benchmark.generator.viz.BenchmarkResults;

/**
 * Specific measures for a query
 * @author Stephane Campinas [15 Aug 2012]
 * @email stephane.campinas@deri.org
 *
 */
public class QueryBenchmarkResults
extends BenchmarkResults {

  private String  querySpec;
  private boolean isWarm;
  private Stats   cpuTime;
  private Stats   userTime;
  private Stats   systemTime;
  private Stats   rate;
  private long    hits;

  public QueryBenchmarkResults(String querySpec,
                               boolean isWarm,
                               Stats cpuTime,
                               Stats userTime,
                               Stats systemTime,
                               Stats rate,
                               long hits) {
    this.isWarm = isWarm;
    this.querySpec = querySpec;
    this.cpuTime = cpuTime;
    this.userTime = userTime;
    this.systemTime = systemTime;
    this.rate = rate;
    this.hits = hits;
  }

  public QueryBenchmarkResults() {
  }

  public boolean isWarm() {
    return isWarm;
  }

  public Stats getCpuTime() {
    return cpuTime;
  }

  /**
   * Can be null
   * 
   * @return The user time {@link Stats}
   */
  public Stats getUserTime() {
    return userTime;
  }

  /**
   * Can be null
   * 
   * @return the system time {@link Stats}
   */
  public Stats getSystemTime() {
    return systemTime;
  }

  public String getQuerySpec() {
    return querySpec;
  }

  public Stats getRate() {
    return rate;
  }

  public long getHits() {
    return hits;
  }

  public void setQuerySpec(String querySpec) {
    this.querySpec = querySpec;
  }

  public void setWarm(boolean isWarm) {
    this.isWarm = isWarm;
  }

  public void setCpuTime(Stats cpuTime) {
    this.cpuTime = cpuTime;
  }

  public void setUserTime(Stats userTime) {
    this.userTime = userTime;
  }

  public void setSystemTime(Stats systemTime) {
    this.systemTime = systemTime;
  }

  public void setRate(Stats rate) {
    this.rate = rate;
  }

  public void setHits(long hits) {
    this.hits = hits;
  }

}

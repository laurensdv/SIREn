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
package org.sindice.siren.benchmark.generator.viz;

import org.sindice.siren.benchmark.RatesStats;
import org.sindice.siren.benchmark.Stats;
import org.sindice.siren.benchmark.wrapper.IndexWrapperFactory.IndexWrapperType;

/**
 * Contains all measurements gathered during the benchmarking of an index
 * @author Stephane Campinas [27 Jul 2012]
 * @email stephane.campinas@deri.org
 *
 */
public class BenchmarkResults {

  /**
   * The type of results this instance contains
   */
  public enum ResultsType {
    INDEX, QUERY
  }

  private final ResultsType      resutlsType;
  private final IndexWrapperType index;
  // index
  private final Stats            commitTime;
  private final double           optimiseTime;
  private final long             sizeInBytes;
  // query
  private final String           querySpec;
  private final boolean          isWarm;
  private final Stats            cpuTime;
  private final Stats            userTime;
  private final Stats            systemTime;
  private final Stats            rate;
  private final long             hits;

  private BenchmarkResults(ResultsType resutlsType,
                          IndexWrapperType index,
                          Stats commitTime,
                          double optimiseTime,
                          long sizeInBytes,
                          String querySpec,
                          boolean isWarm,
                          Stats cpuTime,
                          Stats userTime,
                          Stats systemTime,
                          Stats rate,
                          long hits) {
    this.resutlsType = resutlsType;
    this.isWarm = isWarm;
    this.querySpec = querySpec;
    this.cpuTime = cpuTime;
    this.userTime = userTime;
    this.systemTime = systemTime;
    this.rate = rate;
    this.hits = hits;
    this.index = index;
    this.commitTime = commitTime;
    this.optimiseTime = optimiseTime;
    this.sizeInBytes = sizeInBytes;
  }

  /**
   * Creates a {@link BenchmarkResults} for Query results
   * @param index The {@link IndexWrapperType} this results are about
   * @param querySpec The query specification name
   * @param isWarm <code>true</code> if these results were gathered with WARM cache
   * @param cpuTime The CPU query time {@link Stats}
   * @param userTime The user query time {@link Stats}
   * @param systemTime the System query time {@link Stats}
   * @param rate The Query per Second measure {@link RatesStats}
   * @param hits The number of hits for the set of queries
   * @return a {@link BenchmarkResults} for a query measurement
   */
  public static BenchmarkResults newQBenchmarkResult(IndexWrapperType index,
                                                     String querySpec,
                                                     boolean isWarm,
                                                     Stats cpuTime,
                                                     Stats userTime,
                                                     Stats systemTime,
                                                     Stats rate,
                                                     long hits) {
    return new BenchmarkResults(ResultsType.QUERY, index, null, -1, -1, querySpec, isWarm, cpuTime, userTime, systemTime, rate, hits);
  }

  /**
   * Creates a {@link BenchmarkResults} for Indexing time + size measures.
   * @param index The {@link IndexWrapperType} this results are about
   * @param commitTime The time taken to commit the batches of documents at indexing time.
   * @param optimiseTime The time taken to optimise the index
   * @param sizeInBytes The size of the index after optimisation
   * @return a {@link BenchmarkResults} for index related measures
   */
  public static BenchmarkResults newIndexBenchmarkResult(IndexWrapperType index,
                                                         Stats commitTime,
                                                         double optimiseTime,
                                                         long sizeInBytes) {
    return new BenchmarkResults(ResultsType.INDEX, index, commitTime, optimiseTime, sizeInBytes, null, true, null, null, null, null, -1);
  }

  public ResultsType getResutlsType() {
    return resutlsType;
  }

  public boolean isWarm() {
    return isWarm;
  }

  public Stats getCpuTime() {
    return cpuTime;
  }

  /**
   * Can be null
   * @return The user time {@link Stats}
   */
  public Stats getUserTime() {
    return userTime;
  }

  /**
   * Can be null
   * @return the system time {@link Stats}
   */
  public Stats getSystemTime() {
    return systemTime;
  }

  public IndexWrapperType getIndex() {
    return index;
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

  public Stats getCommitTime() {
    return commitTime;
  }

  public double getOptimiseTime() {
    return optimiseTime;
  }

  public long getSizeInBytes() {
    return sizeInBytes;
  }

}

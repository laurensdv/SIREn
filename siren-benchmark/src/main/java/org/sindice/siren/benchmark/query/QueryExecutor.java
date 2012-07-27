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
package org.sindice.siren.benchmark.query;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.sindice.siren.benchmark.Measurement;
import org.sindice.siren.benchmark.generator.lexicon.TermLexiconReader;
import org.sindice.siren.benchmark.query.provider.Query;
import org.sindice.siren.benchmark.query.provider.QueryProvider;
import org.sindice.siren.benchmark.query.provider.QuerySpecificationParser;
import org.sindice.siren.benchmark.query.provider.QuerySpecificationParser.QuerySpecification;
import org.sindice.siren.benchmark.query.task.QueryTask;
import org.sindice.siren.benchmark.util.BenchmarkTimer;
import org.sindice.siren.benchmark.util.JvmState;
import org.sindice.siren.benchmark.util.JvmUtils;
import org.sindice.siren.benchmark.wrapper.IndexWrapper;
import org.sindice.siren.benchmark.wrapper.IndexWrapperFactory;
import org.sindice.siren.benchmark.wrapper.IndexWrapperFactory.IndexWrapperType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cold cache benchmarks should always been performed using multi-query
 * tasks with enough (random) queries so that a single task execution takes
 * more than one second. Otherwise, the results could be biased.
 */
public class QueryExecutor extends AbstractQueryExecutor {

  private final IndexWrapper wrapper;
  private final QueryProvider provider;
  private final String querySpecName;

  protected boolean coldCache = false;

  protected boolean resetIfJvmStateChanges = false;

  final Logger logger = LoggerFactory.getLogger(QueryExecutor.class);

  public QueryExecutor(final IndexWrapperType wrapperType,
                       final File indexDirectory,
                       final File querySpec,
                       final File lexiconDir) throws IOException {
    this.wrapper = IndexWrapperFactory.createIndexWrapper(wrapperType, indexDirectory);
    final QuerySpecificationParser parser = new QuerySpecificationParser(lexiconDir);
    final QuerySpecification spec = parser.parse(querySpec);
    provider = spec.getQueryProvider();
    querySpecName = querySpec.getName().replace(".txt", "");
  }

  /**
   * Activate or not cold cache benchmark.
   */
  public void setColdCache(final boolean b) {
    this.coldCache = b;
  }

  /**
   * Set the seed for the {@link TermLexiconReader}
   */
  public void setSeed(final int seed) {
    this.provider.setSeed(seed);
  }

  /**
   * Execute the query benchmark until the query provider does not return
   * anymore query.
   * <br>
   * Flush the cache before each query execution if coldCache is set to true.
   */
  @Override
  public void execute() throws Exception {
    logger.info("Execution: x{} - cold cache={}", this.provider.getNbQueries(), this.coldCache);

    // flush the cache of the wrapper before issuing the queries
    this.flushCache();

    final List<Query> queries = new ArrayList<Query>(this.provider.getNbQueries());
    while (provider.hasNext()) {
      queries.add(provider.next());
    }
    final QueryTask task = wrapper.issueQueries(queries);
    this.doBenchmark(task);
    task.close(); // close the task, and free the associated resources
  }

  /**
   * Execute the query, and record query information such as time and number
   * of hits.
   */
  protected void doBenchmark(final QueryTask task) throws Exception {
    // warmup the jvm
    this.warmup(task, WARMUP_TIME);

    // determine how many task executions is necessary for one measurement
    final long nExecutions = this.determineNumberExecutions(task);

    // do the measurements
    measurements = this.doMeasurements(NUMBER_MEASUREMENTS, task, nExecutions);
    // and compute stats
    cpuTime = this.calculateMeanTimeStats(this.getCpuTimes(measurements), nExecutions);
    userTime = this.calculateMeanTimeStats(this.getUserTimes(measurements), nExecutions);
    systemTime = this.calculateMeanTimeStats(this.getSystemTimes(measurements), nExecutions);
    hits = measurements[0].getHitsPerExecution();
    queryRates = this.calculateRateStats(this.getCpuTimes(measurements), nExecutions, this.provider.getNbQueries());
  }

  /**
   * In order to give hotspot optimization a chance to complete, {@link #task}
   * is executed many times (with no recording of the execution time).
   * @throws Exception
   */
  protected void warmup(final QueryTask task, final long warmupTime)
  throws Exception {
    JvmUtils.cleanJvm();
    final long start = System.nanoTime();
    long end = System.nanoTime();
    logger.info("Warming up JVM");
    for (int n = 1; end - start < warmupTime; n *= 2) {
      for (long i = 0; i < n; i++) {
        task.call();
      }
      end = System.nanoTime();
    }
    logger.info("JVM warmed up for {} seconds", (end - start) / (double) (1000 * 1000 * 1000));
  }

  /**
   * Determines how many executions of {@link #task} are required in order for the sum
   * of their execution times to equal {@link #params}.{@link Params#getExecutionTimeGoal getExecutionTimeGoal}.
   * The result is stored in {@link #numberExecutions}.
   * <p>
   * @throws Exception
   * @throws Exception (or some subclass) if <code>task</code> is a <code>Callable</code> and <code>task.call</code> throws it
   */
   protected long determineNumberExecutions(final QueryTask task)
   throws Exception {
     JvmUtils.cleanJvm();

     logger.info("Determining how many executions of task are required");
     long n = 1;
     while (true) {
       if (coldCache) { // flush fs cache before starting measurement
         this.flushFSCache();
       }
       final Measurement m = this.measure(task, n);
       // n is not large enough to cause ExecutionTimeGoal to be met
       if (m.getWallTime() <= EXECUTION_TIME_GOAL) {
         n *= 2; // so double n and retry; note: if n overflows then measure will detect it
         continue;
       }
       // have obtained a reliable estimate of n
       else {
         logger.info("Determined that {} executions of task with {} queries " +
         		"are required for executionTimeGoal to be met",
         		n, this.provider.getNbQueries());
         return n;
       }
     }
   }

  /**
   * Measures {@link #nMeasurements} times the execution of {@link #task}.
   * Each measurement is over a block of {@link #nExecutions} calls to
   * <code>task</code>.
   * @throws Exception
   */
   protected Measurement[] doMeasurements(final int nMeasurements,
                                          final QueryTask task,
                                          final long nExecutions)
   throws Exception {
     JvmUtils.cleanJvm();
     JvmState jvmState = new JvmState();

     logger.info("Performing {} measurements of {} executions", nMeasurements, nExecutions);
     final Measurement[] measurements = new Measurement[nMeasurements];
     for (int i = 0; i < nMeasurements; i++) {
       if (coldCache) { // flush fs cache before starting measurement
         this.flushFSCache();
       }
       measurements[i] = this.measure(task, nExecutions);
       logger.debug("Measurement {}: {}", i, measurements[i]);

       if (resetIfJvmStateChanges && !measurements[i].getJvmState().equals(jvmState)) {
         logger.info("Reset measurement loop. Detected JVM state change: {}", measurements[i].getJvmState().difference(jvmState));
         jvmState = measurements[i].getJvmState();  // reset to the latest JvmState
         i = -1; // causes the loop to restart at i = 0 on its next iteration
       }
     }
     return measurements;
   }

  /**
   * Measures the execution time of <code>n</code> calls of {@link #task}.
   * <p>
   * Units: nanoseconds.
   * @throws Exception
   */
  protected Measurement measure(final QueryTask task, final long n)
  throws Exception {
    final Measurement total = new Measurement();
    final BenchmarkTimer timer = new BenchmarkTimer();
    timer.start();

    Measurement callResult;
    for (long i = 0; i < n; i++) {
      callResult = task.call();
      total.setHits(total.getHits() + callResult.getHits());
    }

    timer.stop();
    total.setTimes(timer);
    total.setNumberExecutions(n);
    total.setJvmState(new JvmState());
    return total;
  }

  @Override
  protected void flushWrapperCache() throws IOException {
    this.wrapper.flushCache();
  }

  @Override
  protected String getFilename(final String prefix) {
    if (coldCache) {
      return prefix + "-COLD";
    }
    else {
      return prefix + "-WARM";
    }
  }

  @Override
  protected String getQuerySpecName() {
   return querySpecName;
  }

  @Override
  public void close() {
    // do nothing
  }

}

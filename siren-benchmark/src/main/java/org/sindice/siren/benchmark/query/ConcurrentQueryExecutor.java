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
package org.sindice.siren.benchmark.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.sindice.siren.benchmark.Measurement;
import org.sindice.siren.benchmark.query.provider.Query;
import org.sindice.siren.benchmark.query.provider.QueryProvider;
import org.sindice.siren.benchmark.query.task.QueryTask;
import org.sindice.siren.benchmark.util.BenchmarkTimer;
import org.sindice.siren.benchmark.util.JvmState;
import org.sindice.siren.benchmark.util.JvmUtils;
import org.sindice.siren.benchmark.wrapper.IndexWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConcurrentQueryExecutor extends AbstractQueryExecutor {

  final int poolSize;

  final int keepAliveTime = 5;

  final ThreadPoolExecutor executor;

  final ArrayBlockingQueue<Runnable> queue;

  final Logger logger = LoggerFactory.getLogger(ConcurrentQueryExecutor.class);

  public ConcurrentQueryExecutor(final IndexWrapper wrapper,
                                 final QueryProvider provider,
                                 final int nThreads) {
    super(wrapper, provider);
    poolSize = nThreads;
    queue  = new ArrayBlockingQueue<Runnable>(poolSize);
    executor = new ThreadPoolExecutor(poolSize, poolSize,
      keepAliveTime, TimeUnit.SECONDS, queue);
    executor.prestartAllCoreThreads();
  }

  /**
   * Close the concurrent query executor, and the underlying resources.
   */
  @Override
  public void close() throws IOException {
    executor.shutdown();
  }

  /**
   * Execute the concurrent query benchmark.<br>
   * First create the list of {@code QueryExecutorTask}, and submit them all
   * at once.
   * @throws Exception
   * @throws InterruptedException
   */
  @Override
  public void execute() throws Exception {
    logger.info("Execution: x{} - {} threads", poolSize, provider.getNbQueries());

    // flush the cache of the wrapper before issuing the queries
    this.flushCache();

    // Create a query task for each thread
    final QueryTask[] tasks = new QueryTask[poolSize];

    // Retrieve the random queries
    final List<Query> queries = new ArrayList<Query>(this.provider.getNbQueries());
    for (int j = 0; j < this.provider.getNbQueries(); j++) {
      provider.hasNext();
      queries.add(provider.next());
    }

    // Create the query tasks
    for (int i = 0; i < poolSize; i++) {
      tasks[i] = wrapper.issueQueries(queries);
    }

    // Run the benchmark
    this.doBenchmark(tasks);

    // close the tasks, and free the associated resources
    for (int i = 0; i < poolSize; i++) {
      tasks[i].close();
    }
  }

  /**
   * Execute the query, and record query information such as time and number
   * of hits.
   * @throws Exception
   */
  protected void doBenchmark(final QueryTask[] tasks) throws Exception {
    // warmup the jwm
    this.warmup(tasks, WARMUP_TIME);
    // determine how many task executions is necessary for one measurement
    final long nExecutions = this.determineNumberExecutions(tasks);
    // do the measurements
    measurements = this.doMeasurements(NUMBER_MEASUREMENTS, tasks, nExecutions);
    // Compute stats for nExecutions * poolSize to take into
    // consideration the total number of concurrent executions
    cpuTime = this.calculateRateStats(this.getCpuTimes(measurements), nExecutions * poolSize, provider.getNbQueries());
    // divide by the number of queries to have an approximation of hits and
    // bytes read per queries
    hits = measurements[0].getHitsPerExecution() / provider.getNbQueries();
  }

  /**
   * In order to give hotspot optimization a chance to complete, {@link #task}
   * is executed many times (with no recording of the execution time).
   * @throws Exception
   */
  protected void warmup(final QueryTask[] tasks, final long warmupTime)
  throws Exception {
    JvmUtils.cleanJvm();
    final List<ConcurrentMeasureTask> concurrentMeasures = this.getTasks(tasks, 1);
    final long start = System.nanoTime();
    long end = System.nanoTime();
    logger.info("Warming up JVM");
    for (int n = 1; end - start < warmupTime; n *= 2) {
      for (long i = 0; i < n; i++) {
        executor.invokeAll(concurrentMeasures);
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
  protected long determineNumberExecutions(final QueryTask[] tasks)
  throws Exception {
    JvmUtils.cleanJvm();

    logger.info("Determining how many executions of task are required");
    boolean loop = true;
    long n = 1;

    List<Future<Measurement>> results = null;
    while (loop) {
      results = executor.invokeAll(this.getTasks(tasks, n));
      for (final Future<Measurement> result : results) {
        // n is not large enough to cause ExecutionTimeGoal to be met
        if (result.get().getWallTime() <= EXECUTION_TIME_GOAL) {
          n *= 2; // so double n and retry; note: if n overflows then measure will detect it
          loop = true; // continue to execute the while loop
          break;
        }
        else {
          loop = false; // stop to execute the while loop
        }
      }
    }
    logger.info("Determined that {} executions of task are required for executionTimeGoal to be met", n);
    return n;
  }

  /**
   * Measures {@link #nMeasurements} times the execution of {@link #task}.
   * Each measurement is over a block of {@link #nExecutions} calls to
   * <code>task</code>.
   * @throws Exception
   */
  protected Measurement[] doMeasurements(final int nMeasurements,
                                         final QueryTask[] tasks,
                                         final long nExecutions)
  throws Exception {
    JvmUtils.cleanJvm();
    JvmState jvmState = new JvmState();

    logger.info("Performing {} measurements of {} executions", nMeasurements, nExecutions);
    final Measurement[] measurements = new Measurement[nMeasurements];
    final List<ConcurrentMeasureTask> concurrentMeasures = this.getTasks(tasks, nExecutions);

    for (int i = 0; i < nMeasurements; i++) {
      // execute all measurement tasks, and merge their results in one measurement
      measurements[i] = this.merge(executor.invokeAll(concurrentMeasures));
      logger.debug("Measurement: {}", measurements[i]);
      measurements[i].setJvmState(new JvmState());

      if (!measurements[i].getJvmState().equals(jvmState)) {
        logger.info("Reset measurement loop. Detected JVM state change: {}", measurements[i].getJvmState().difference(jvmState));
        jvmState = measurements[i].getJvmState();  // reset to the latest JvmState
        i = -1; // causes the loop to restart at i = 0 on its next iteration
      }
    }
    return measurements;
  }

  /**
   * Create a list of concurrent measurement tasks.
   */
  private List<ConcurrentMeasureTask> getTasks(final QueryTask[] tasks, final long nExecutions) {
    final List<ConcurrentMeasureTask> concurrentMeasures = new ArrayList<ConcurrentMeasureTask>(poolSize);

    // Create the tasks
    for (int i = 0; i < poolSize; i++) {
      concurrentMeasures.add(new ConcurrentMeasureTask(tasks[i], nExecutions));
    }

    return concurrentMeasures;
  }

  /**
   * Merge a list of measurement (returned by the thread pool executor) into
   * one measurement.
   */
  private Measurement merge(final List<Future<Measurement>> results)
  throws InterruptedException, ExecutionException {
    final Measurement measurement = new Measurement();

    for (final Future<Measurement> result : results) {
      measurement.add(result.get());
    }

    return measurement;
  }

  @Override
  protected String generateFilename(final String prefix) {
    return prefix + "-T" + poolSize + "-WARM-" +  provider.toString();
  }

  /**
   * Wrap the execution and measurment of a measurement into a callable
   * in order to be able to execute more than one of them in parallel.
   * <p>
   * Each concurrent mesurement task will return its own measurement object,
   * containing all the information, i.e., times, hits, bytes read.
   * <p>
   * The concurrent measurement task object can be reused and submitted more
   * than one time.
   */
  protected class ConcurrentMeasureTask implements Callable<Measurement> {

    final QueryTask task;
    final long n;

    final Logger logger = LoggerFactory.getLogger(ConcurrentMeasureTask.class);

    public ConcurrentMeasureTask(final QueryTask task, final long n) {
      this.task = task;
      this.n = n;
    }

    @Override
    public Measurement call()
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
      return total;
    }
  }

}

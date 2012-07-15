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

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.sindice.siren.benchmark.Measurement;
import org.sindice.siren.benchmark.RatesStats;
import org.sindice.siren.benchmark.Stats;
import org.sindice.siren.benchmark.util.Bootstrap;
import org.sindice.siren.benchmark.util.Bootstrap.EstimatorSd;
import org.sindice.siren.benchmark.util.HarmonicMeanEstimator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractQueryExecutor implements Closeable {

  /**
   * Determine how many executions of {@link #task} to perform for each timing.
   * Specifically, for each measurement, <code>task</code> must be executed
   * enough times to meet or exceed the amount of time specified here.
   * <p>
   * This value should be large enough that inaccuracy in timing measurements
   * are small. Since this class uses {@link System#nanoTime System.nanoTime}
   * for its timings, and this clock does not (across many platforms) reliably
   * have better than a few 10s of ms accuracy, this value should probably be at
   * least 1 second in order to guarantee measurement error under 1%.
   * <i>Only use a smaller value if you know that System.nanoTime
   * on all the platforms that you deploy on has greater accuracy.</i>
   * <p>
   * Default value is 1 (second).
   */
  protected static long   EXECUTION_TIME_GOAL = 1000000000l;

  /**
   * Specifies the minimum amount of time that should execute {@link #task}
   * before start recording measurements.
   * <p>
   * Recommended value is 10.0 (seconds),
   * which is the minimum time recomended by Cliff Click, the HotSpot architect
   * (see p. 33 of <a href="http://www.azulsystems.com/events/javaone_2002/microbenchmarks.pdf">his 2002 JavaOne talk</a>).
   */
  protected static long   WARMUP_TIME = 10000000000l;

  /**
   * If {@link #manyExecutions} is <code>true</code>,
   * then once {@link #numberExecutions} has been determined, specifies the number of timing measurements to perform.
   * <p>
   * Contract: must be > 0.
   * <p>
   * Default value is 60.  The following considerations determined this value:
   * <ol>
   *  <li>
   *   15-25 is about the optimum number of samples for an accuracy versus time tradeoff,
   *   according to this <a href="http://en.wikipedia.org/wiki/T-distribution">one-sided confidence interval table</a>.
   *   Now, that table is only valid for mean confidence intervals on a Gaussian distribution.
   *   So, in order to accurately handle general distributions (which are likely to require more samples, especially skewed ones)
   *   and statistics besides the mean (also likely to require more samples)
   *   using the bootstrap technique (which is known to have coverage issues, especially with small numbers of samples),
   *   the number of samples should be at least 30.
   *  </li>
   *  <li>
   *   it, along with {@link #executionTimeGoal}, determines how long all of the measurements will take.
   *   As discussed <a href="http://www.ibm.com/developerworks/java/library/j-benchmark1.html#rr">here</a>,
   *   you want the total benchmarking process to last at least 1 minute
   *   in order to accurately sample garbage collection and object finalization behavior.
   *   Given <code>executionTimeGoal</code>'s default value of 1 second, that means this value should be at least 60.
   *  </li>
   *  <li>need at least 50 samples to do the autocorrelation tests in {@link #diagnoseSerialCorrelation diagnoseSerialCorrelation}</li>
   * </ol>
   * Conclusion: need at least max(30, 60, 50) = 60.
   */
  protected static int NUMBER_MEASUREMENTS = 100;

  /**
   * Specify the confidence level to use when calculating the confidence
   * intervals for the statistics.
   * <p>
   * Default value is 0.95, the standard value used in statistics.
   * <p>
   * @see <a href="http://en.wikipedia.org/wiki/Confidence_level">article on
   * confidence intervals</a>
   */
   protected static final double CONFIDENCE_LEVEL = 0.95;

   protected       Measurement[] measurements;

   protected Stats     cpuTime;
   protected Stats     userTime;
   protected Stats     systemTime;
   protected Stats     queryRates;
   protected Long      hits;

   final Logger logger = LoggerFactory.getLogger(AbstractQueryExecutor.class);

   public Measurement[] getMeasurements() {
     return measurements;
   }

   protected double[] getCpuTimes(final Measurement[] measurements) {
     final double[] times = new double[measurements.length];
     for (int i = 0; i < times.length; i++) {
       times[i] = measurements[i].getCPUTime() / (double) (1000 * 1000 * 1000); // transform in seconds
     }
     return times;
   }

   protected double[] getUserTimes(final Measurement[] measurements) {
     final double[] times = new double[measurements.length];
     for (int i = 0; i < times.length; i++) {
       times[i] = measurements[i].getUserTime() / (double) (1000 * 1000 * 1000); // transform in seconds
     }
     return times;
   }

   protected double[] getSystemTimes(final Measurement[] measurements) {
     final double[] times = new double[measurements.length];
     for (int i = 0; i < times.length; i++) {
       times[i] = measurements[i].getSystemTime() / (double) (1000 * 1000 * 1000); // transform in seconds
     }
     return times;
   }

   /**
    * Execute the query benchmark, and compute the statistics.
    */
   public abstract void execute() throws Exception;

   /**
    * Calculates {@link #statsBlock} from {@link #times}.
    * Then derives {@link #statsAction} from <code>statsBlock</code>.
    */
   protected Stats calculateMeanTimeStats(final double[] times, final long nExecutions)
   throws IllegalStateException {
     logger.info("calculating the block statistics (each data point comes from {} executions)...", nExecutions);
     final Bootstrap bootstrap = new Bootstrap(times, Bootstrap.numberResamples_default,
       CONFIDENCE_LEVEL);
     final Bootstrap.Estimate mean = bootstrap.getEstimate("mean");
     final Bootstrap.Estimate sd = bootstrap.getEstimate("sd");
     final Stats statsBlock = new Stats(mean.getPoint(), mean.getLower(),
       mean.getUpper(), sd.getPoint(), sd.getLower(), sd.getUpper(), null);
     final Stats statsAction = statsBlock.forActions(nExecutions);
     return statsAction;
   }

   /**
    * Calculates {@link #statsBlock} from {@link #times}.
    * Then derives {@link #statsAction} from <code>statsBlock</code>.
    */
   protected Stats calculateMedianTimeStats(final double[] times, final long nExecutions)
   throws IllegalStateException {
     logger.info("calculating the block statistics (each data point comes from {} executions)...", nExecutions);
     final Bootstrap bootstrap = new Bootstrap(times, Bootstrap.numberResamples_default,
       CONFIDENCE_LEVEL);
     final Bootstrap.Estimate median = bootstrap.getEstimate("median");
     final Bootstrap.Estimate sd = bootstrap.getEstimate("sd");
     final Stats statsBlock = new Stats(median.getPoint(), median.getLower(),
       median.getUpper(), sd.getPoint(), sd.getLower(), sd.getUpper(), null);
     final Stats statsAction = statsBlock.forActions(nExecutions);
     return statsAction;
   }

   /**
    * Calculates {@link #statsBlock} from {@link #times}.
    * Then derives {@link #statsAction} from <code>statsBlock</code>.
    */
   protected Stats calculateRateStats(final double[] times, final long nExecutions, final long nQueries)
   throws IllegalStateException {
     logger.debug("Calculate rates stats for {} executions and {} queries", nExecutions, nQueries);
     // compute query rates
     final double[] rates = new double[times.length];
     for (int i = 0; i < times.length; i++) {
       rates[i] = (nExecutions * nQueries) / times[i];
     }

     logger.info("calculating the block statistics (each data point comes from {} executions)...", nExecutions);
     final Bootstrap bootstrap = new Bootstrap(rates, Bootstrap.numberResamples_default,
       CONFIDENCE_LEVEL, new HarmonicMeanEstimator(), new EstimatorSd());
     final Bootstrap.Estimate mean = bootstrap.getEstimate("harmonic mean");
     final Bootstrap.Estimate sd = bootstrap.getEstimate("sd");
     return new RatesStats(mean.getPoint(), mean.getLower(),
       mean.getUpper(), sd.getPoint(), sd.getLower(), sd.getUpper(), null);
   }

   /**
    * Flush the cache of
    * <ul>
    * <li> the wrapper;
    * <li> the OS filesystem cache
    * <ul>
    */
   protected void flushCache() throws IOException {
     this.flushWrapperCache();
     this.flushFSCache();
   }

   protected abstract void flushWrapperCache() throws IOException;

   /** command to flush system fs caches */
   private static final String FLUSH_FS_CACHE_COMMAND = "sudo /usr/sbin/flush-fs-cache.sh";

   /**
    * Flush the OS filesystem cache.
    */
   protected void flushFSCache() {
     logger.info("Flush OS filesystem cache");
     try {
       final Process flush = Runtime.getRuntime().exec(FLUSH_FS_CACHE_COMMAND);
       if (flush.waitFor() != 0) {
         logger.error("Could not flush system filesystem caches, ignoring!");
         logger.error("Subsequent queries now may influence each others performance!");
       } else {
         //wait some
         try {
           Thread.sleep(1000);
         }
         catch (final InterruptedException e) {}
       }
     } catch (final IOException e) {
       logger.error("could not run filesystem cache flush command '" + FLUSH_FS_CACHE_COMMAND + "':");
       e.printStackTrace();
     } catch (final InterruptedException e) {
       logger.error("interruption while waiting for filesystem cache flush command '" + FLUSH_FS_CACHE_COMMAND + "' to terminate:");
       e.printStackTrace();
     }
   }

   /**
    * Export the list of measurement in a file.
    * <br>
    * Each measurement is exported on a line.
    *
    * @param output The output directory where the query times will be exported.
    */
   public void exportMeasurementTimes(final File output) throws IOException {
     final File outputFile = this.generateOutputFile(output, "measurement");
     final FileWriter writer = new FileWriter(outputFile);
     for (final Measurement measurement : measurements) {
       writer.append(Long.toString(measurement.getCPUTime() / measurement.getNumberExecutions()));
       writer.append('\n');
     }
     writer.close();
   }


   public void exportQueryRates(final File output) throws IOException {
     final File outputFile = this.generateOutputFile(output, "rate");
     final FileWriter writer = new FileWriter(outputFile);
     writer.append(queryRates.toString());
     writer.close();
   }

   /**
    * Export the list of query times in a file.
    * <br>
    * Each query times is exported on a line.
    *
    * @param output The output directory where the query times will be exported.
    */
   public void exportQueryTimes(final File output) throws IOException {
     final File outputFile = this.generateOutputFile(output, "time");
     final FileWriter writer = new FileWriter(outputFile);
     writer.append(cpuTime.toString());
     writer.append('\n');
     if (userTime != null && systemTime != null) {
       writer.append(userTime.toString());
       writer.append('\n');
       writer.append(systemTime.toString());
       writer.append('\n');
     }
     writer.close();
   }

   /**
    * Export the list of hits per query in a file.
    * <br>
    * Each query hits is exported on a line.
    *
    * @param output The output directory where the query hits will be exported.
    */
   public void exportHits(final File output) throws IOException {
     final File outputFile = this.generateOutputFile(output, "hits");
     final FileWriter writer = new FileWriter(outputFile);
     writer.append(Long.toString(hits));
     writer.append('\n');
     writer.close();
   }

   /**
    * Generate the directory and file that will be used for output.
    * <br>
    * Create the directory tree if it does not exist.
    */
   protected File generateOutputFile(final File output, final String prefix) {
     if (!output.exists()) {
       output.mkdirs();
       logger.info("Created output directory {}", output);
     }
     final String filename = this.generateFilename(prefix);
     return new File(output, filename);
   }

   /**
    * Generate the filenames that will be used to store the benchmark results.
    */
   protected abstract String generateFilename(final String prefix);

}

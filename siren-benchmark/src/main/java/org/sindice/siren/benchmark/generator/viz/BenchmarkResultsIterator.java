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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Stack;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.math.stat.StatUtils;
import org.sindice.siren.benchmark.RatesStats;
import org.sindice.siren.benchmark.Stats;
import org.sindice.siren.benchmark.generator.viz.BenchmarkResults.ResultsType;
import org.sindice.siren.benchmark.wrapper.IndexWrapperFactory.IndexWrapperType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Iterate through the list of benchmarked query specification over a set of
 * indexes. It also provides index related measures such as the indexing time.
 * @author Stephane Campinas [27 Jul 2012]
 * @email stephane.campinas@deri.org
 *
 */
public class BenchmarkResultsIterator
implements Iterator<BenchmarkResults> {

  private static final Logger logger = LoggerFactory.getLogger(BenchmarkResultsIterator.class);

  /**
   * Pair of Index type and related benchmark results
   */
  private class IndexResultsPair {
    final ResultsType      resType;
    final long             indexSizeInBytes;
    final IndexWrapperType index;
    final File[]           res;

    public IndexResultsPair(ResultsType resType,
                            IndexWrapperType index,
                            long indexSizeInBytes,
                            File[] res) {
      this.resType = resType;
      this.indexSizeInBytes = indexSizeInBytes;
      this.index = index;
      this.res = res;
    }

    @Override
    public String toString() {
      return "index=" + index + " dirs=" + Arrays.toString(res);
    }
  }

  /**
   * Benchmark results
   */
  private final Stack<IndexResultsPair> results  = new Stack<IndexResultsPair>();
  /**
   * The set of results run over an index being processed
   */
  private IndexResultsPair              benchResults;
  /**
   * The pos in results for the current index being processed
   */
  private int                            pos      = 0;
  /**
   * The set of measures files
   */
  private File[]                         measures = null;
  /**
   * Whether or not to consider also WARM measures
   */
  private boolean                        withWarm = true;
  /**
   * Whether or not to consider also COLD measures
   */
  private boolean                        withCold = true;

  /**
   * The qResultsDir folder contains query measures from multiple indexes. The
   * hierarchy is then as follows:
   * <p>
   * &lt;index&gt;/<br/>
   * &lt;index&gt;/&lt;q-spec&gt;/<br/>
   * &lt;index&gt;/&lt;q-spec&gt;/hits-{WARM,COLD}<br/>
   * &lt;index&gt;/&lt;q-spec&gt;/time-{WARM,COLD}<br/>
   * &lt;index&gt;/&lt;q-spec&gt;/rate-{WARM,COLD}<br/>
   * &lt;index&gt;/&lt;q-spec&gt;/measurement-{WARM,COLD}<br/>
   * <p>
   * The indexTimesDir folder contains indexing times. The hierarchy is then as
   * follows:
   * <p>
   * &lt;index&gt;/<br/>
   * &lt;index&gt;/time-logs/<br/>
   * &lt;index&gt;/time-logs/commit.out<br/>
   * &lt;index&gt;/time-logs/optimise.out<br/>
   * &lt;index&gt;/index/<br/>
   * <p>
   * &lt;index&gt; is a value of the {@link IndexWrapperType} enum.
   * &lt;index&gt;/index/ contains the index.
   * @param The path to the directory with index is, along with indexing times.
   *        It is expected to have &lt;index&gt; subfolders too.
   * @param qResultsDir The path to the directory with query times
   */
  public BenchmarkResultsIterator(File indexDir,
                                  File qResultsDir) {
    this(null, indexDir, qResultsDir, null);
  }

  /**
   * The qResultsDir folder contains query measures from one index. The hierarchy
   * is then as follows:
   * <p>
   * &lt;q-spec&gt;/<br/>
   * &lt;q-spec&gt;/hits-{WARM,COLD}<br/>
   * &lt;q-spec&gt;/time-{WARM,COLD}<br/>
   * &lt;q-spec&gt;/rate-{WARM,COLD}<br/>
   * &lt;q-spec&gt;/measurement-{WARM,COLD}<br/>
   * <p>
   * The indexTimesDir folder contains indexing times. The hierarchy is then as
   * follows:
   * <p>
   * time-logs/<br/>
   * time-logs/commit.out<br/>
   * time-logs/optimise.out<br/>
   * index/<br/>
   * <p>
   * index/ contains the index.
   * @param indexDir The path to the directory with index is, along with indexing times
   * @param qResultsDir The path to the directory with query times
   */
  public BenchmarkResultsIterator(IndexWrapperType index,
                                  File indexDir,
                                  File qResultsDir) {
    this(index, indexDir, qResultsDir, null);
  }

  /**
   * Filters results which query spec name matches the regular expression. If
   * the expression is <code>null</code>, nothing is filtered.
   * If <code>index</code> is <code>null</code>, the behaviour is the same as in
   * {@link #BenchmarkResultsIterator(File, File)}.
   * @param index
   * @param indexDir The path to the directory with index is, along with indexing times.
   * @param qResultsDir The path to the directory with query times
   * @param qSpecRegex The regex to filter query spec
   */
  public BenchmarkResultsIterator(IndexWrapperType index,
                                  File indexDir,
                                  File qResultsDir,
                                  final String qSpecRegex) {
    // Query Results
    if (qResultsDir == null || !qResultsDir.isDirectory()) {
      logger.error("Wrong query benchmark results directory: {}", qResultsDir);
      throw new IllegalArgumentException();
    }
    // Index Times + Index Size
    if (indexDir == null || !indexDir.isDirectory()) {
      logger.error("Wrong index directory: {}", indexDir);
      throw new IllegalArgumentException();
    }

    if (qSpecRegex == null) {
      getResults(index, indexDir, qResultsDir, new FileFilter() {
        @Override
        public boolean accept(File pathname) {
          return pathname.isDirectory();
        }
      });
    } else { // keep only query spec subfolders that match the regexp
      getResults(index, indexDir, qResultsDir, new FileFilter() {
        @Override
        public boolean accept(File pathname) {
          return pathname.isDirectory() && pathname.getName().matches(qSpecRegex);
        }
      });
    }

    final Iterator<IndexResultsPair> it = results.iterator();
    int nResToprocess = 0;
    while (it.hasNext()) {
      nResToprocess += it.next().res.length;
    }
    logger.info("Processing {} results: {}", nResToprocess, results.toString());

    // init iteration
    benchResults = results.pop();
  }

  /**
   * Get the path to each measurement folder.
   * @param index The type of index. If null, it considers the specific file
   *        structure described in {@link #BenchmarkResultsIterator(File, File)}
   * @param indexDir The path to the Index directory
   * @param qResultsDir The path to the results of benchmarked queries
   * @param qSpecfilter filter unwanted query specification results
   */
  private void getResults(final IndexWrapperType index,
                          final File indexDir,
                          final File qResultsDir,
                          final FileFilter qSpecfilter) {
    if (index != null) {
      // get query times
      results.add(new IndexResultsPair(ResultsType.QUERY, index, -1, qResultsDir.listFiles(qSpecfilter)));
      // get indexing times
      final long size = FileUtils.sizeOfDirectory(indexDir);
      results.add(new IndexResultsPair(ResultsType.INDEX, index, size, new File[] { new File(indexDir, "time-logs") }));
    } else { // results over multiple indexes
      // get query times
      final File[] qIndexes = qResultsDir.listFiles(new FileFilter() {
        @Override
        public boolean accept(File pathname) {
          try {
            IndexWrapperType.valueOf(pathname.getName());
          } catch (IllegalArgumentException e) {
            return false;
          }
          return pathname.isDirectory();
        }
      });
      for (File i: qIndexes) {
        results.add(new IndexResultsPair(ResultsType.QUERY, IndexWrapperType.valueOf(i.getName()), -1, i.listFiles(qSpecfilter)));
      }
      // get indexing times
      final File[] iIndexes = indexDir.listFiles(new FileFilter() {
        @Override
        public boolean accept(File pathname) {
          try {
            IndexWrapperType.valueOf(pathname.getName());
          } catch (IllegalArgumentException e) {
            return false;
          }
          return pathname.isDirectory();
        }
      });
      for (File i: iIndexes) {
        final long size = FileUtils.sizeOfDirectory(i);
        results.add(new IndexResultsPair(ResultsType.INDEX, IndexWrapperType.valueOf(i.getName()), size, new File[] { new File(i, "time-logs") }));
      }
    }
  }

  public void setWithWarm(boolean withWarm) {
    this.withWarm = withWarm;
  }

  public void setWithCold(boolean withCold) {
    this.withCold = withCold;
  }

  @Override
  public boolean hasNext() {
    while (true) {
      if (measures == null || isDone(measures)) {
        if (pos >= benchResults.res.length) { // no more files
          if (results.empty()) {
            return false;
          }
          // go to the next index results set
          benchResults = results.pop();
          pos = 0;
          measures = null;
          continue;
        }
        try {
          switch (benchResults.resType) {
            case INDEX:
              measures = benchResults.res[pos].getCanonicalFile().listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                  return name.equals("commit.out") || name.equals("optimise.out");
                }
              });
              break;
            case QUERY:
              measures = benchResults.res[pos].getCanonicalFile().listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                  return (withWarm && name.equals("hits-WARM")) ||
                         (withCold && name.equals("hits-COLD")) ||
                         (withWarm && name.equals("time-WARM")) ||
                         (withCold && name.equals("time-COLD")) ||
                         // measurements are not reported
//                           name.equals("measurement-WARM") || name.equals("measurement-COLD") ||
                         (withWarm && name.equals("rate-WARM")) ||
                         (withCold && name.equals("rate-COLD"));
                }
              });
              break;
            default:
              throw new IllegalArgumentException("Unknown ResultsType: got " + benchResults.resType);
          }
        } catch (IOException e) {
          logger.error("", e);
          throw new RuntimeException();
        }
      }
      if (measures.length == 0) {
        continue;
      }
      logger.info("Processing results: {}", benchResults.res[pos]);
      checkMeasures(measures);
      break;
    }
    return true;
  }

  /**
   * Return true if the set of measures is finished being processed.
   */
  private boolean isDone(File[] measures) {
    switch (benchResults.resType) {
      case INDEX:
        pos++;
        return true;
      case QUERY:
        for (int i = 0; i < measures.length; i++) {
          if (measures[i] != null) {
            return false;
          }
        }
        /*
         * this measures set has been processed or there is nothing inside,
         * go to the next one
         */
        pos++;
        return true;
      default:
        throw new IllegalArgumentException("Unknown ResultsType: got " + benchResults.resType);
    }
  }

  /**
   * Verify the measures set
   */
  private void checkMeasures(File[] measures) {
    switch (benchResults.resType) {
      case INDEX:
        if (measures.length != 2) {
          logger.error("Unexpected set of index times results: {} should " +
                       "contain optimise.out and commit.out.", benchResults.res[pos]);
          throw new IllegalStateException();
        }
        break;
      case QUERY:
        if (measures.length == 0 || measures.length % 3 != 0) {
          logger.error("Unexpected set of query benchmark results: {} should " +
                       "contain hits-{WARM,COLD}, time-{WARM,COLD} and " +
                       "rate-{WARM,COLD}.", benchResults.res[pos]);
          throw new IllegalStateException();
        }
        int remaining = 0;
        for (int i = 0; i < measures.length; i++) {
          if (measures[i] != null) {
            remaining++;
          }
        }
        if (remaining == 0 || remaining % 3 != 0) {
          logger.error("Unexpected set of query benchmark results: {} has the following " +
                       "files left to process: {}",
                       benchResults.res[pos], Arrays.toString(measures));
          throw new IllegalStateException();
        }
        break;
      default:
        throw new IllegalArgumentException("Unknown ResultsType: got " + benchResults.resType);
    }
  }

  @Override
  public BenchmarkResults next() {
    BenchmarkResults res = null;

    switch (benchResults.resType) {
      case INDEX:
        if ((res = getIndexMeasures()) == null) {
          checkMeasures(measures);
          throw new IllegalStateException();
        }
        break;
      case QUERY:
        if ((res = getQMeasures("-COLD", false)) == null && // Get cold cache results
            (res = getQMeasures("-WARM", true)) == null) { // Get warm cache results
          checkMeasures(measures);
          throw new IllegalStateException();
        }
        break;
      default:
        throw new IllegalArgumentException("Unknown ResultsType: got " + benchResults.resType);
    }
    return res;
  }

  /**
   * Extract Index related values
   * @return {@link BenchmarkResults#newIndexBenchmarkResult(IndexWrapperType, Stats, double, long)}
   */
  private BenchmarkResults getIndexMeasures() {
    Stats commitTime = null;
    double optimiseTime = -1;

    try {
      for (int i = 0; i < measures.length; i++) { // for each measurement output
        if (measures[i] != null) {
          final BufferedReader r = new BufferedReader(new FileReader(measures[i]));
          if (measures[i].getName().equals("commit.out")) {
            /*
             * Commit
             */
            if (commitTime != null) {
              checkMeasures(measures);
              r.close();
              logger.error("Wrong set of index times: {}", Arrays.toString(measures));
              return null;
            }
            final ArrayList<Double> times = new ArrayList<Double>();
            String line;
            while ((line = r.readLine()) != null) {
              times.add(Double.valueOf(line));
            }
            final double[] timesArr = new double[times.size()];
            for (int j = 0; j < times.size(); j++) {
              timesArr[j] = times.get(j);
            }
            final double mean = StatUtils.mean(timesArr);
            final double sd = Math.sqrt(StatUtils.variance(timesArr));
            commitTime = new Stats(mean, 0, 0, sd, 0, 0, null);
          } else if (measures[i].getName().equals("optimise.out")) {
            /*
             * Optimise
             */
            if (optimiseTime != -1) {
              checkMeasures(measures);
              r.close();
              logger.error("Wrong set of index times: {}", Arrays.toString(measures));
              return null;
            }
            optimiseTime = Double.valueOf(r.readLine());
          } else {
            r.close();
            // Should not happen
            throw new RuntimeException();
          }
          r.close();
          measures[i] = null;
        }
      }
    } catch(FileNotFoundException e) {
      logger.error("", e);
      return null;
    } catch (NumberFormatException e) {
      logger.error("", e);
      return null;
    } catch (IOException e) {
      logger.error("", e);
      return null;
    }
    return BenchmarkResults.newIndexBenchmarkResult(benchResults.index, commitTime, optimiseTime, benchResults.indexSizeInBytes);
  }

  /**
   * Extract the measurements gathered during the query benchmark
   * @param suffix
   * @param isWarm
   * @return {@link BenchmarkResults#newQBenchmarkResult(IndexWrapperType, String, boolean, Stats, Stats, Stats, Stats, long)}
   */
  private BenchmarkResults getQMeasures(String suffix, boolean isWarm) {
    final String querySpec = benchResults.res[pos].getName();
    Stats cpuTime = null;
    Stats userTime = null; // optional
    Stats systemTime = null; // optional
    Stats rate = null;
    long hits = -1;

    try {
      for (int i = 0; i < measures.length; i++) { // for each measurement output
        if (measures[i] != null) {
          final BufferedReader r = new BufferedReader(new FileReader(measures[i]));
          if (measures[i].getName().equals("hits" + suffix)) {
            /*
             * Hits
             */
            if (hits != -1) {
              checkMeasures(measures);
              r.close();
              logger.error("Wrong set of query benchmark results: {}", Arrays.toString(measures));
              return null;
            }
            hits = Long.valueOf(r.readLine());
          } else if (measures[i].getName().equals("time" + suffix)) {
            /*
             * Time
             */
            if (cpuTime != null) {
              checkMeasures(measures);
              r.close();
              logger.error("Wrong set of query benchmark results: {}", Arrays.toString(measures));
              return null;
            }
            // cpu time
            final String cpuTimeStr = r.readLine();
            cpuTime = getQStats(cpuTimeStr);
            // user time + system time
            final String userTimeStr = r.readLine();
            if (userTimeStr != null) {
              userTime = getQStats(userTimeStr);
              final String systemTimeStr = r.readLine();
              systemTime = getQStats(systemTimeStr);
            }
          } else if (measures[i].getName().equals("rate" + suffix)) {
            /*
             * Rate
             */
            if (rate != null) {
              checkMeasures(measures);
              r.close();
              logger.error("Wrong set of query benchmark results: {}", Arrays.toString(measures));
              return null;
            }
            final String rateStr = r.readLine();
            final Stats s = getQStats(rateStr);
            rate = new RatesStats(s.getMean(), s.getMeanLower(), s.getMeanUpper(), s.getSd(), s.getSdLower(), s.getSdUpper(), null);
          } else {
            continue;
          }
          r.close();
          measures[i] = null;
        }
      }
    } catch(FileNotFoundException e) {
      logger.error("", e);
      return null;
    } catch (NumberFormatException e) {
      logger.error("", e);
      return null;
    } catch (IOException e) {
      logger.error("", e);
      return null;
    }
    if (cpuTime == null && rate == null && hits == -1) {
      return null;
    }
    return BenchmarkResults.newQBenchmarkResult(benchResults.index, querySpec, isWarm, cpuTime, userTime, systemTime, rate, hits);
  }

  /**
   * Create a {@link Stats} from the output of {@link Stats#toString()}
   * @param measure
   * @return
   */
  private Stats getQStats(final String measure) {
    // mean
    final int equal = measure.indexOf('=');
    final int space1 = measure.indexOf(' ', equal + 2);
    final double mean = Double.parseDouble(measure.substring(equal + 1, space1));
    // mean - CI
    final double[] meanCI = getQConfidenceInterval(measure, space1);
    final double meanCiLower = meanCI[0];
    final double meanCiUpper = meanCI[1];
    // std dev
    final int equal2 = measure.indexOf('=', (int) meanCI[2]);
    final int space2 = measure.indexOf(' ', equal2 + 2);
    final double sd = Double.parseDouble(measure.substring(equal2 + 1, space2));
    // std dev - CI
    final double[] sdCI = getQConfidenceInterval(measure, space2);
    final double sdCiLower = sdCI[0];
    final double sdCiUpper = sdCI[1];
    return new Stats(mean, mean + meanCiLower, mean + meanCiUpper, sd, sd + sdCiLower, sd + sdCiUpper, null);
  }

  /**
   * Extract the lower and upper bounds of the Confidence Interval.
   * @param measure
   * @param startOffset
   * @return A double array with the lower bound at 0, the upper bound at 1, and
   *         the position in the measure String at 2.
   */
  private double[] getQConfidenceInterval(final String measure,
                                          final int startOffset) {
    final double[] ci = new double[3];
    final int colon = measure.indexOf(':', startOffset);
    final int plusMinus = measure.indexOf("+-", colon);
    if (plusMinus == -1) {
      final int comma = measure.indexOf(',', colon);
      ci[0] = Double.parseDouble(measure.substring(colon + 2, comma).replace("q/s", ""));
      final int rparen = measure.indexOf(')', comma);
      ci[1] = Double.parseDouble(measure.substring(comma + 1, rparen).replace("q/s", ""));
      ci[2] = rparen;
    } else { // lower and upper bound are the same
      final int rparen = measure.indexOf(')', colon);
      ci[1] = Double.parseDouble(measure.substring(colon + 2, rparen)
                                        .replace("q/s", "")
                                        .replace("+-", ""));
      ci[0] = - ci[1];
      ci[2] = rparen;
    }
    return ci;
  }

  @Override
  public void remove() {
    throw new NotImplementedException();
  }

}

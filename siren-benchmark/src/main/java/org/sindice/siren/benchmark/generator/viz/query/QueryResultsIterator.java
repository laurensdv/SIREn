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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import org.sindice.siren.benchmark.RatesStats;
import org.sindice.siren.benchmark.Stats;
import org.sindice.siren.benchmark.generator.viz.BenchmarkResults;
import org.sindice.siren.benchmark.generator.viz.ResultsIterator;
import org.sindice.siren.benchmark.generator.viz.VizException;

/**
 * Iterates through the directories files and extracts query benchmark results
 * @author Stephane Campinas [15 Aug 2012]
 * @email stephane.campinas@deri.org
 *
 */
public class QueryResultsIterator
extends ResultsIterator {

  private final static String   QUERY_RESULTS_DIR = "benchmark";
  private File[]                querySpecFiles;
  private int                   pos;
  private final ArrayList<File> queryTimes        = new ArrayList<File>();

  @Override
  public void init(File directory)  {
    super.init(directory);
    pos = -1;
    try {
      // Get the new set of query results.
      querySpecFiles = new File(directory, QUERY_RESULTS_DIR).getCanonicalFile().listFiles(new FileFilter() {
        @Override
        public boolean accept(File pathname) {
          return pathname.isDirectory();
        }
      });
    } catch (IOException e) {
      logger.error("", e);
      throw new VizException(e);
    }
  }

  @Override
  public boolean hasNext() {
    if (!queryTimes.isEmpty()) {
      return true;
    }
    pos++;
    while (pos < querySpecFiles.length) {
      try {
        final File[] measures = querySpecFiles[pos].getCanonicalFile().listFiles(new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return name.equals("hits-WARM") || name.equals("hits-COLD") ||
                   name.equals("time-WARM") || name.equals("time-COLD") ||
                   // measurements are not reported
//                   name.equals("measurement-WARM") ||
//                   name.equals("measurement-COLD") ||
                   name.equals("rate-WARM") || name.equals("rate-COLD");
          }
        });
        if (measures == null || measures.length % 3 != 0) {
          logger.error("Missing query benchmark results: {}", querySpecFiles[pos]);
          pos++;
          continue;
        }
        queryTimes.clear();
        for (File f: measures) {
          queryTimes.add(f);
        }
        return true;
      } catch (IOException e) {
        logger.error("", e);
        throw new VizException(e);
      }
    }
    return false;
  }

  @Override
  public BenchmarkResults next() {
    final QueryBenchmarkResults res = new QueryBenchmarkResults();

    res.setDirectoryName(directory.getName());
    res.setQuerySpec(querySpecFiles[pos].getName());
    if (getResults(res, "-WARM")) {
      res.setWarm(true);
    } else if (getResults(res, "-COLD")) {
      res.setWarm(false);
    } else {
      throw new VizException("Missing query results files: " + querySpecFiles[pos]);
    }
    return res;
  }

  private boolean getResults(QueryBenchmarkResults res, String suffix) {
    for (File f: queryTimes) {
      if (f.getName().endsWith(suffix)) {
        logger.info("Processing file: {}", f);
        try {
          final BufferedReader r = new BufferedReader(new FileReader(f));
          if (f.getName().equals("hits" + suffix)) {
            res.setHits(Long.valueOf(r.readLine()));
          } else if (f.getName().equals("time" + suffix)) {
            // cpu time
            final String cpuTimeStr = r.readLine();
            res.setCpuTime(getQStats(cpuTimeStr));
            // user time + system time
            final String userTimeStr = r.readLine();
            if (userTimeStr != null) {
              res.setUserTime(getQStats(userTimeStr));
              final String systemTimeStr = r.readLine();
              res.setSystemTime(getQStats(systemTimeStr));
            }
          } else if (f.getName().equals("rate" + suffix)) {
            final String rateStr = r.readLine();
            final Stats s = getQStats(rateStr);
            res.setRate(new RatesStats(s.getMean(), s.getMeanLower(), s.getMeanUpper(), s.getSd(), s.getSdLower(), s.getSdUpper(), null));
          } else {
            r.close();
            continue;
          }
          r.close();
        } catch (IOException e) {
          logger.error("", e);
          throw new VizException(e);
        }
      }
    }
    // Remove processed results
    boolean hasProcessed = false;
    for (int i = queryTimes.size() - 1; i >= 0; i--) {
      if (queryTimes.get(i).getName().endsWith(suffix)) {
        queryTimes.remove(i);
        hasProcessed = true;
      }
    }
    return hasProcessed;
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

}

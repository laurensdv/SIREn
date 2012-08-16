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
package org.sindice.siren.benchmark.generator.viz.query.diff;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.sindice.siren.benchmark.generator.viz.AbstractFormatter;
import org.sindice.siren.benchmark.generator.viz.BenchmarkResults;
import org.sindice.siren.benchmark.generator.viz.VizException;
import org.sindice.siren.benchmark.generator.viz.query.QueryBenchmarkResults;

/**
 * Collects a set of query results, sort them by query spec, OS cache and then
 * by the directory name. The currently processed directory is displayed first.
 * @author Stephane Campinas [15 Aug 2012]
 * @email stephane.campinas@deri.org
 *
 */
public abstract class QueryDiffFormatter
extends AbstractFormatter {

  final ArrayList<BenchmarkResults> brQueryList = new ArrayList<BenchmarkResults>();
  final HashMap<String, Integer>    qSpecs      = new HashMap<String, Integer>();
  final HashMap<String, Integer>    qSpecsCache = new HashMap<String, Integer>();

  @Override
  public void collect(BenchmarkResults br) {
    final QueryBenchmarkResults qbr = (QueryBenchmarkResults) br;
    final String qSpec = qbr.getQuerySpec();
    final String qSpecCache = qSpec + qbr.isWarm();

    qSpecs.put(qSpec, qSpecs.containsKey(qSpec) ? qSpecs.get(qSpec) + 1 : 1);
    qSpecsCache.put(qSpecCache, qSpecsCache.containsKey(qSpecCache) ? qSpecsCache.get(qSpecCache) + 1 : 1);
    brQueryList.add(br);
  }

  @Override
  public List<BenchmarkResults> getSortedList() {
    final String dirName = directoryName;
    Collections.sort(brQueryList, new Comparator<BenchmarkResults>() {
      @Override
      public int compare(BenchmarkResults o1, BenchmarkResults o2) {
        final QueryBenchmarkResults qbr1 = (QueryBenchmarkResults) o1;
        final QueryBenchmarkResults qbr2 = (QueryBenchmarkResults) o2;
        final int dirCmp;
        final int qSpecCmp;

        if ((qSpecCmp = qbr1.getQuerySpec().compareTo(qbr2.getQuerySpec())) == 0) {
          if (qbr1.isWarm() == qbr2.isWarm()) {
            if ((dirCmp = o1.getDirectoryName().toString().compareTo(o2.getDirectoryName().toString())) != 0) {
              if ((dirCmp > 0 && o1.getDirectoryName().equals(dirName)) ||
                  (dirCmp < 0 && o2.getDirectoryName().equals(dirName))) {
                return - dirCmp;
              }
            }
            return dirCmp;
          }
          return qbr1.isWarm() ? 1 : -1;
        }
        return qSpecCmp;
      }
    });
    return brQueryList;
  }

  @Override
  protected void addBenchmarkResult(Writer out, BenchmarkResults br)
  throws IOException {
    final QueryBenchmarkResults qbr = (QueryBenchmarkResults) br;
    final String qSpec = qbr.getQuerySpec();
    final String qSpecCache = qSpec + qbr.isWarm();
    addBenchmarkResult(out, qSpecs.get(qSpec), qSpecsCache.get(qSpecCache), qbr);
  }

  protected abstract void addBenchmarkResult(final Writer out,
                                             int nbQSpecs,
                                             int nbQSpecsCache,
                                             QueryBenchmarkResults br)
  throws IOException;

  /**
   * Check that compared index has the same number of hits as the baseline.
   * @param newHits new index hits
   * @param baseHits baseline index hits
   */
  protected void checkHits(long newHits, long baseHits) {
    if (newHits != baseHits) {
      throw new VizException("baseline found " + baseHits + " hits but new found " + newHits + " hits");
    }
  }

  /**
   * Computes the difference percentage between the baseline and compared algorithms.
   * <p>
   * @see <a href="http://code.google.com/a/apache-extras.org/p/luceneutil/source/browse/benchUtil.py#928">benchUtils.py</a>
   * of luceneutil
   * @param qpsBase
   * @param qpsStdDevBase
   * @param qpsCmp
   * @param qpsStdDevCmp
   * @return
   */
  protected int[] computePctDiff(double qpsBase,
                                 double qpsStdDevBase,
                                 double qpsCmp,
                                 double qpsStdDevCmp) {
    final double qpsBaseBest = qpsBase + qpsStdDevBase;
    final double qpsBaseWorst = qpsBase - qpsStdDevBase;

    final double qpsCmpBest = qpsCmp + qpsStdDevCmp;
    final double qpsCmpWorst = qpsCmp - qpsStdDevCmp;

    final int psBest;
    final int psWorst;

    if (qpsBaseWorst == 0.0 ||
        (qpsBaseBest == qpsCmpBest && qpsBaseWorst == qpsCmpWorst)) {
      psBest = psWorst = 0;
    } else {
      psBest = (int) (100.0 * (qpsCmpBest - qpsBaseWorst)/qpsBaseWorst);
      psWorst = (int) (100.0 * (qpsCmpWorst - qpsBaseBest)/qpsBaseBest);
    }
    return new int[] { psWorst, psBest };
  }

}

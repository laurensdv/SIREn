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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.sindice.siren.benchmark.wrapper.IndexWrapperFactory.IndexWrapperType;

/**
 * Export the benchmark results into a human readable format.
 * @author Stephane Campinas [27 Jul 2012]
 * @email stephane.campinas@deri.org
 *
 */
public abstract class BenchmarkResultsFormatter {

  protected final BufferedWriter         out;
  private final BenchmarkResultsIterator bri;

  /**
   * Write formatted results into out.
   */
  public BenchmarkResultsFormatter(Writer out,
                                   BenchmarkResultsIterator bri) {
    this.out = new BufferedWriter(out);
    this.bri = bri;
  }

  /**
   * Process and format the set of results
   * @throws IOException
   */
  public void format()
  throws IOException {
    final ArrayList<BenchmarkResults> brQueryList = new ArrayList<BenchmarkResults>();
    final ArrayList<BenchmarkResults> brIndexList = new ArrayList<BenchmarkResults>();
    final HashMap<IndexWrapperType, Integer> indexes = new HashMap<IndexWrapperType, Integer>();

    try {
      while (bri.hasNext()) {
        final BenchmarkResults br = bri.next();
        final IndexWrapperType index = br.getIndex();
        switch (br.getResutlsType()) {
          case INDEX:
            brIndexList.add(br);
            break;
          case QUERY:
            indexes.put(index, indexes.containsKey(index) ? indexes.get(index) + 1 : 1);
            brQueryList.add(br);
            break;
          default:
            throw new IllegalArgumentException("Unknown ResultsType: got " + br.getResutlsType());
        }
      }
      /*
       * Export Query benchmark results
       */
      formatQueryResults(brQueryList, indexes);
      out.newLine();
      out.append("*************************");
      out.newLine();
      out.newLine();
      /*
       * Export Index Times
       */
      formatIndexTimes(brIndexList);
    } finally {
      out.close();
    }
  }

  /**
   * Process the query benchmark results
   * @param brQueryList a list of results
   * @param indexes the number of query results for each Index type
   */
  private void formatQueryResults(final ArrayList<BenchmarkResults> brQueryList,
                                  final HashMap<IndexWrapperType, Integer> indexes)
                                  throws IOException {
    // sort results
    Collections.sort(brQueryList, new Comparator<BenchmarkResults>() {
      @Override
      public int compare(BenchmarkResults o1, BenchmarkResults o2) {
        final int indexCmp;
        final int qSpecCmp;

        if ((indexCmp = o1.getIndex().toString().compareTo(o2.getIndex().toString())) != 0) {
          return indexCmp;
        }
        if ((qSpecCmp = o1.getQuerySpec().compareTo(o2.getQuerySpec())) != 0) {
          return qSpecCmp;
        }
        return o1.isWarm() ? 1 : -1;
      }
    });
    // format results
    startQuery();
    for (BenchmarkResults br: brQueryList) {
      addBenchmarkQueryResult(indexes.remove(br.getIndex()), br);
    }
    endQuery();
  }

  /**
   * Process the Index related measures.
   * @param brIndexList a list results
   */
  private void formatIndexTimes(final ArrayList<BenchmarkResults> brIndexList)
  throws IOException {
    // sort results
    Collections.sort(brIndexList, new Comparator<BenchmarkResults>() {
      @Override
      public int compare(BenchmarkResults o1, BenchmarkResults o2) {
        return o1.getIndex().toString().compareTo(o2.getIndex().toString());
      }
    });
    // format results
    startIndex();
    for (BenchmarkResults br: brIndexList) {
      addBenchmarkIndexResult(br);
    }
    endIndex();
  }

  protected abstract void startQuery() throws IOException;
  protected abstract void startIndex() throws IOException;

  protected abstract void addBenchmarkQueryResult(Integer nbIndexRes, BenchmarkResults br) throws IOException;
  protected abstract void addBenchmarkIndexResult(BenchmarkResults br) throws IOException;

  protected abstract void setCaption(String caption);

  protected abstract void setRound(int round);

  /**
   * The URL of the query spec. It is prepended to the query spec folder name.
   * In case where the url contains the substring "{}", it is replaced by the
   * folder name.
   * @param url
   */
  protected abstract void setQuerySpecUrl(String url);

  protected abstract void endQuery() throws IOException;
  protected abstract void endIndex() throws IOException;

}

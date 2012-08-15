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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.sindice.siren.benchmark.generator.viz.AbstractFormatter;
import org.sindice.siren.benchmark.generator.viz.BenchmarkResults;

/**
 * Collects a set of query results, sort them by directory name, query spec,
 * OS Cache.
 * @author Stephane Campinas [15 Aug 2012]
 * @email stephane.campinas@deri.org
 *
 */
public abstract class QueryFormatter
extends AbstractFormatter {

  final ArrayList<BenchmarkResults> brQueryList = new ArrayList<BenchmarkResults>();
  final HashMap<String, Integer>    indexes     = new HashMap<String, Integer>();

  @Override
  public void collect(BenchmarkResults br) {
    final String dirName = br.getDirectoryName();

    indexes.put(dirName, indexes.containsKey(dirName) ? indexes.get(dirName) + 1 : 1);
    brQueryList.add(br);
  }

  @Override
  public List<BenchmarkResults> getSortedList() {
    Collections.sort(brQueryList, new Comparator<BenchmarkResults>() {
      @Override
      public int compare(BenchmarkResults o1, BenchmarkResults o2) {
        final int indexCmp;
        final int qSpecCmp;

        if ((indexCmp = o1.getDirectoryName().toString().compareTo(o2.getDirectoryName().toString())) != 0) {
          return indexCmp;
        }
        if ((qSpecCmp = ((QueryBenchmarkResults) o1).getQuerySpec().compareTo(((QueryBenchmarkResults) o2).getQuerySpec())) != 0) {
          return qSpecCmp;
        }
        return ((QueryBenchmarkResults) o1).isWarm() ? 1 : -1;
      }
    });
    return brQueryList;
  }

  @Override
  protected void addBenchmarkResult(Writer out, BenchmarkResults br)
  throws IOException {
    addBenchmarkResult(out, indexes.remove(br.getDirectoryName()), br);
  }

  protected abstract void addBenchmarkResult(final Writer out, Integer nbIndexRes, BenchmarkResults br) throws IOException;

}

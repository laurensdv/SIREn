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
package org.sindice.siren.benchmark.generator.viz.index.diff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.sindice.siren.benchmark.generator.viz.AbstractFormatter;
import org.sindice.siren.benchmark.generator.viz.BenchmarkResults;

/**
 * Collects a set of Index measures, sort them by the directory name. The currently
 * processed directory is always displayed first.
 * @author Stephane Campinas [15 Aug 2012]
 * @email stephane.campinas@deri.org
 *
 */
public abstract class IndexDiffFormatter
extends AbstractFormatter {

  protected final ArrayList<BenchmarkResults> brIndexList = new ArrayList<BenchmarkResults>();

  @Override
  public void collect(BenchmarkResults br) {
    brIndexList.add(br);
  }

  @Override
  public List<BenchmarkResults> getSortedList() {
    final String dirName = directoryName;
    Collections.sort(brIndexList, new Comparator<BenchmarkResults>() {
      @Override
      public int compare(BenchmarkResults o1, BenchmarkResults o2) {
        final int cmp = o1.getDirectoryName().compareTo(o2.getDirectoryName());
        if (cmp != 0) {
          if ((cmp > 0 && o1.getDirectoryName().equals(dirName)) ||
              (cmp < 0 && o2.getDirectoryName().equals(dirName))) {
            return - cmp;
          }
        }
        return cmp;
      }
    });
    return brIndexList;
  }

}

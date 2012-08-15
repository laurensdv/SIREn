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

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Generic format implementation
 * @author Stephane Campinas [14 Aug 2012]
 * @email stephane.campinas@deri.org
 *
 */
public abstract class AbstractFormatter
implements Formatter {

  protected String directoryName;

  /** To round the measure value */
  private static int round = 2;

  @Override
  public void setDirectoryName(String name) {
    this.directoryName = name;
  }

  @Override
  public void format(final Writer out)
  throws IOException {
    final List<BenchmarkResults> brs = getSortedList();

    start(out);
    for (BenchmarkResults br: brs) {
      addBenchmarkResult(out, br);
    }
    end(out);
  }

  protected double diffAsPercentage(double a, double b) {
    return b == 0 ? 0 : ((a - b) / b) * 100;
  }

  protected String addNumericValue(Number v) {
    return String.format("%." + round + "f", v);
  }

  protected abstract List<BenchmarkResults> getSortedList();

  protected abstract void start(final Writer out) throws IOException;

  protected abstract void addBenchmarkResult(final Writer out, BenchmarkResults br) throws IOException;

  protected abstract void end(final Writer out) throws IOException;

}

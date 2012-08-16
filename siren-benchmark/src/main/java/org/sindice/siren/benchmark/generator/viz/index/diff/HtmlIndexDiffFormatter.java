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

import java.io.IOException;
import java.io.Writer;

import org.sindice.siren.benchmark.generator.viz.BenchmarkResults;
import org.sindice.siren.benchmark.generator.viz.index.IndexBenchmarkResults;

/**
 * Export the Index results as a HTML table.
 * @author Stephane Campinas [14 Aug 2012]
 * @email stephane.campinas@deri.org
 *
 */
public class HtmlIndexDiffFormatter
extends IndexDiffFormatter {

  private IndexBenchmarkResults baseline;

  @Override
  public void setDirectoryName(String name) {
    super.setDirectoryName(name);
    baseline = null;
  }

  @Override
  public void start(final Writer out)
  throws IOException {
    out.append("<table width=\"100%\">\n");
    out.append("  <tr>\n");
    out.append("    <th rowspan=\"2\">Index</th><th colspan=\"4\">Index Size (MB)</th>" +
    "<th rowspan=\"2\">Commit (ms)</th><th rowspan=\"2\">Optimise (ms)</th>\n");
    out.append("  </tr>");

    out.append("  <tr>\n");
    out.append("    <th>Doc</th><th>Nod</th><th>Pos</th><th>Skp</th>\n");
    out.append("  </tr>\n");
  }

  @Override
  public void addBenchmarkResult(final Writer out, BenchmarkResults br)
  throws IOException {
    IndexBenchmarkResults ibr = (IndexBenchmarkResults) br;

    if (baseline == null) {
      baseline = ibr;
      doBaseline(out);
    } else {
      doDeltas(out, ibr);
    }
  }

  private void doBaseline(final Writer out)
  throws IOException {
    out.append("  <tr>")
    .append("    <td style=\"text-align: left;\">")
    .append(baseline.getDirectoryName()).append("</td>")
    .append("<td style=\"text-align: right;\">")
    .append(addNumericValue(baseline.getDocSizeInBytes() / (1024d * 1024d))).append("</td>")
    .append("<td style=\"text-align: right;\">")
    .append(addNumericValue(baseline.getNodSizeInBytes() / (1024d * 1024d))).append("</td>")
    .append("<td style=\"text-align: right;\">")
    .append(addNumericValue(baseline.getPosSizeInBytes() / (1024d * 1024d))).append("</td>")
    .append("<td style=\"text-align: right;\">")
    .append(addNumericValue(baseline.getSkpSizeInBytes() / (1024d * 1024d))).append("</td>")
    .append("<td style=\"text-align: right;\">")
    .append(Double.toString(baseline.getCommitTime())).append("</td>")
    .append("<td style=\"text-align: right;\">")
    .append(Double.toString(baseline.getOptimiseTime())).append("</td>\n");
    out.append("  </tr>\n");
  }

  private void doDeltas(final Writer out, IndexBenchmarkResults ibr)
  throws IOException {
    out.append("  <tr>")
       .append("    <td style=\"font-style:italic; text-align: left;\">")
       .append(ibr.getDirectoryName()).append("</td>");
    diffRow(out, ibr.getDocSizeInBytes(), baseline.getDocSizeInBytes());
    diffRow(out, ibr.getNodSizeInBytes(), baseline.getNodSizeInBytes());
    diffRow(out, ibr.getPosSizeInBytes(), baseline.getPosSizeInBytes());
    diffRow(out, ibr.getSkpSizeInBytes(), baseline.getSkpSizeInBytes());
    diffRow(out, ibr.getCommitTime(), baseline.getCommitTime());
    diffRow(out, ibr.getOptimiseTime(), baseline.getOptimiseTime());
    out.append("  </tr>\n");
  }

  private void diffRow(final Writer out, double newValue, double baseValue)
  throws IOException {
    final double delta = diffAsPercentage(newValue, baseValue);
    final String color = delta >= 0 ? "red" : "green";
    out.append("<td style=\"color:" + color + "; font-style:italic; text-align: right;\">")
       .append(addNumericValue(newValue) + " (" + (delta >= 0 ? "+" : "-") + addNumericValue(delta))
       .append("%)</td>");
  }

  @Override
  public void end(final Writer out)
  throws IOException {
    out.append("</table>\n");
  }

}

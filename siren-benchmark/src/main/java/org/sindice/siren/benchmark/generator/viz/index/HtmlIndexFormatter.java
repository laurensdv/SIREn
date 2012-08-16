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
package org.sindice.siren.benchmark.generator.viz.index;

import java.io.IOException;
import java.io.Writer;

import org.sindice.siren.benchmark.generator.viz.BenchmarkResults;

/**
 * Export the Index results as a HTML table.
 * @author Stephane Campinas [14 Aug 2012]
 * @email stephane.campinas@deri.org
 *
 */
public class HtmlIndexFormatter
extends IndexFormatter {

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

    out.append("  <tr>")
       .append("    <td style=\"text-align: left;\">")
       .append(ibr.getDirectoryName()).append("</td>")
       .append("<td style=\"text-align: right;\">")
       .append(addNumericValue(ibr.getDocSizeInBytes() / (1024d * 1024d))).append("</td>")
       .append("<td style=\"text-align: right;\">")
       .append(addNumericValue(ibr.getNodSizeInBytes() / (1024d * 1024d))).append("</td>")
       .append("<td style=\"text-align: right;\">")
       .append(addNumericValue(ibr.getPosSizeInBytes() / (1024d * 1024d))).append("</td>")
       .append("<td style=\"text-align: right;\">")
       .append(addNumericValue(ibr.getSkpSizeInBytes() / (1024d * 1024d))).append("</td>")
       .append("<td style=\"text-align: right;\">")
       .append(Double.toString(ibr.getCommitTime())).append("</td>")
       .append("<td style=\"text-align: right;\">")
       .append(Double.toString(ibr.getOptimiseTime())).append("</td>\n");
   out.append("  </tr>\n");
  }

  @Override
  public void end(final Writer out)
  throws IOException {
    out.append("</table>\n");
  }

}

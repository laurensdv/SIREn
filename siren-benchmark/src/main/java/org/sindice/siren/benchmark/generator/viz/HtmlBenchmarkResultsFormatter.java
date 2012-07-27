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

/**
 * Outputs the results as HTML tables
 * @author Stephane Campinas [27 Jul 2012]
 * @email stephane.campinas@deri.org
 *
 */
public class HtmlBenchmarkResultsFormatter
extends BenchmarkResultsFormatter {

  /** The table caption */
  private String caption;
  /** To round the measure value */
  private int    round = 2;
  /** To create an anchor link to the query spec file */
  private String querySpecUrl = "";

  public HtmlBenchmarkResultsFormatter(Writer out,
                                       BenchmarkResultsIterator bri) {
    super(out, bri);
  }

  @Override
  protected void setCaption(String caption) {
    this.caption = caption;
  }

  @Override
  protected void setQuerySpecUrl(String querySpecRelativeUrl) {
    this.querySpecUrl = querySpecRelativeUrl;
  }

  @Override
  protected void setRound(int round) {
    this.round = round;
  }

  @Override
  protected void startQuery()
  throws IOException {
    out.append("<table width=\"100%\">");
    out.newLine();

    if (caption != null) {
      out.append("  <caption align=\"top\">")
         .append("<em>").append(caption).append("</em>")
         .append("</caption>");
      out.newLine();
    }
    out.append("  <tr>");
    out.newLine();
    out.append("    <th rowspan=\"2\"></th><th rowspan=\"2\">Query Spec</th>" +
               "<th rowspan=\"2\">OS Cache</th><th colspan=\"2\">" +
               "Query Rate (q/s)</th><th rowspan=\"2\">Hits</th>");
    out.newLine();
    out.append("  </tr>");

    out.append("  <tr>");
    out.newLine();
    out.append("    <th>Mean</th><th>SD</th>");
    out.newLine();
    out.append("  </tr>");

    out.newLine();
  }

  @Override
  protected void startIndex()
  throws IOException {
    out.append("<table width=\"100%\">");
    out.newLine();

    out.append("  <tr>");
    out.newLine();
    out.append("    <th rowspan=\"2\"></th><th rowspan=\"2\">Index Size (MB)</th>" +
               "<th rowspan=\"2\">Commit Time</th><th colspan=\"2\">Optimise Time</th>");
    out.newLine();
    out.append("  </tr>");

    out.append("  <tr>");
    out.newLine();
    out.append("    <th>Mean</th><th>SD</th>");
    out.newLine();
    out.append("  </tr>");

    out.newLine();
  }

  @Override
  protected void addBenchmarkQueryResult(Integer nbIndexRes, BenchmarkResults br)
  throws IOException {
    out.append("  <tr>");
    out.newLine();
    if (nbIndexRes != null) {
      out.append("    <td style=\"text-align: left;\" rowspan=\"" + nbIndexRes + "\">")
         .append(br.getIndex().toString()).append("</td>");
    }
    out.append("<td style=\"text-align: left;\">")
       .append(getQSpecUrl(br.getQuerySpec())).append("</td>")
       .append("<td style=\"text-align: left;\">")
       .append(br.isWarm() ? "WARM" : "COLD").append("</td>")
       .append("<td style=\"text-align: right;\">")
       .append(addNumericValue(br.getRate().getMean())).append("</td>")
       .append("<td style=\"text-align: right;\">")
       .append(addNumericValue(br.getRate().getSd())).append("</td>")
       .append("<td style=\"text-align: right;\">")
       .append(Long.toString(br.getHits())).append("</td>");
    out.newLine();
    out.append("  </tr>");
    out.newLine();
  }

  @Override
  protected void addBenchmarkIndexResult(BenchmarkResults br)
  throws IOException {
    out.append("  <tr>")
       .append("    <td style=\"text-align: left;\">")
       .append(br.getIndex().toString()).append("</td>")
       .append("<td style=\"text-align: right;\">")
       .append(Double.toString(br.getSizeInBytes() / (1024 * 1024))).append("</td>")
       .append("<td style=\"text-align: right;\">")
       .append(addNumericValue(br.getCommitTime().getMean())).append("</td>")
       .append("<td style=\"text-align: right;\">")
       .append(addNumericValue(br.getCommitTime().getSd())).append("</td>")
       .append("<td style=\"text-align: right;\">")
       .append(Double.toString(br.getOptimiseTime())).append("</td>");
    out.newLine();
    out.append("  </tr>");
    out.newLine();
  }

  private String getQSpecUrl(String qspec) {
    if (querySpecUrl.isEmpty()) {
      return qspec;
    }
    if (querySpecUrl.contains("{}")) {
      return "<a href=\"" + querySpecUrl.replace("{}", qspec) + "\" target=\"_blank\">"
             + qspec + "</a>";
    }
    return "<a href=\"" + querySpecUrl + qspec + "\" target=\"_blank\">"
           + qspec + "</a>";
  }

  private String addNumericValue(Number v) {
    return String.format("%." + round + "f", v);
  }

  @Override
  protected void endQuery()
  throws IOException {
    out.append("</table>");
    out.newLine();
  }

  @Override
  protected void endIndex()
  throws IOException {
    out.append("</table>");
    out.newLine();
  }

}

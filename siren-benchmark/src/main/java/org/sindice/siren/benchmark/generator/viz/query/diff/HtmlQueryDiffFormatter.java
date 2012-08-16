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

import org.sindice.siren.benchmark.generator.viz.query.QueryBenchmarkResults;

/**
 * Export the results of query as a HTML table
 * @author Stephane Campinas [15 Aug 2012]
 * @email stephane.campinas@deri.org
 *
 */
public class HtmlQueryDiffFormatter
extends QueryDiffFormatter {

  private QueryBenchmarkResults baseline;
  private int                   cntQspec = 0;
  private int                   cntCache = 0;

  @Override
  public void start(final Writer out)
  throws IOException {
    out.append("<table width=\"100%\">\n");

    out.append("  <tr>\n");
    out.append("    <th rowspan=\"2\"></th><th rowspan=\"2\">OS Cache</th>" +
               "<th rowspan=\"2\">Index</th><th colspan=\"2\">" +
               "Query Rate (q/s)</th><th rowspan=\"2\">Hits</th>\n");
    out.append("  </tr>");

    out.append("  <tr>\n");
    out.append("    <th>Mean</th><th>Pct Diff</th>\n");
    out.append("  </tr>\n");
  }

  @Override
  public void addBenchmarkResult(final Writer out, int nbQSpecs, int nbQSpecsCache, QueryBenchmarkResults qbr)
  throws IOException {
    if (cntCache == 0 || cntQspec == 0) {
      baseline = qbr;
      out.append("  <tr>\n");
      if (cntQspec == 0) {
        cntQspec = nbQSpecs;
        out.append("    <td style=\"text-align: left;\" rowspan=\"" + nbQSpecs + "\">")
           .append(qbr.getQuerySpec()).append("</td>");
      }
      if (cntCache == 0) {
        cntCache = nbQSpecsCache;
        out.append("<td style=\"text-align: left;\" rowspan=\"" + nbQSpecsCache + "\">")
           .append(qbr.isWarm() ? "WARM" : "COLD").append("</td>");
      }
      out.append("<td style=\"text-align: left;\">")
         .append(qbr.getDirectoryName()).append("</td>")
         .append("<td style=\"text-align: right;\">")
         .append(addNumericValue(qbr.getRate().getMean())).append("</td>")
         .append("<td></td><td style=\"text-align: right;\">")
         .append(Long.toString(qbr.getHits())).append("</td>\n");
      out.append("  </tr>\n");
    } else {
      checkHits(qbr.getHits(), baseline.getHits());
      doDeltas(out, qbr);
    }
    cntCache--;
    cntQspec--;
  }

  private void doDeltas(final Writer out, QueryBenchmarkResults qbr)
  throws IOException {
    out.append("  <tr>\n")
       .append("<td style=\"font-style:italic; text-align: left;\">")
       .append(qbr.getDirectoryName()).append("</td>")
       .append("<td style=\"font-style:italic; text-align: right;\">")
       .append(addNumericValue(qbr.getRate().getMean()) + "</td>");
    htmlPctDiff(out, baseline.getRate().getMean(), baseline.getRate().getSd(),
      qbr.getRate().getMean(), qbr.getRate().getSd());
    out.append("<td style=\"font-style:italic; text-align: right;\">")
       .append(Long.toString(qbr.getHits())).append("</td>\n");
    out.append("  </tr>\n");
  }

  private void htmlPctDiff(Writer out,
                           double qpsBase,
                           double qpsStdDevBase,
                           double qpsCmp,
                           double qpsStdDevCmp)
  throws IOException {
    final int[] pctDiff = computePctDiff(qpsBase, qpsStdDevBase, qpsCmp, qpsStdDevCmp);

    out.append("<td style=\"font-style:italic; text-align: right;\">(");
    // ps worst
    if (pctDiff[0] >= 0) {
      out.append("<font color=\"green\">").append(pctDiff[0] + "%</font>   -   ");
    } else {
      out.append(pctDiff[0] + "% - ");
    }
    // ps best
    if (pctDiff[1] < 0) {
      out.append("<font color=\"red\">").append(pctDiff[1] + "%</font>");
    } else {
      out.append(pctDiff[1] + "%");
    }
    out.append(")</td>");
  }

  @Override
  public void end(final Writer out)
  throws IOException {
    out.append("</table>\n");
  }

}

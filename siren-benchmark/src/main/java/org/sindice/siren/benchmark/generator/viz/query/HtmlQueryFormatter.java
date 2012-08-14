package org.sindice.siren.benchmark.generator.viz.query;

import java.io.IOException;
import java.io.Writer;

import org.sindice.siren.benchmark.generator.viz.BenchmarkResults;

public class HtmlQueryFormatter
extends QueryFormatter {

  @Override
  public void start(final Writer out)
  throws IOException {
    out.append("<table width=\"100%\">\n");

    out.append("  <tr>\n");
    out.append("    <th rowspan=\"2\"></th><th rowspan=\"2\">Query Spec</th>" +
               "<th rowspan=\"2\">OS Cache</th><th colspan=\"2\">" +
               "Query Rate (q/s)</th><th rowspan=\"2\">Hits</th>\n");
    out.append("  </tr>");

    out.append("  <tr>\n");
    out.append("    <th>Mean</th><th>SD</th>\n");
    out.append("  </tr>\n");
  }

  @Override
  public void addBenchmarkResult(final Writer out, Integer nbIndexRes, BenchmarkResults br)
  throws IOException {
    final QueryBenchmarkResults qbr = (QueryBenchmarkResults) br;

    out.append("  <tr>\n");
    if (nbIndexRes != null) {
      out.append("    <td style=\"text-align: left;\" rowspan=\"" + nbIndexRes + "\">")
         .append(br.getDirectoryName().toString()).append("</td>");
    }
    out.append("<td style=\"text-align: left;\">")
       .append(qbr.getQuerySpec()).append("</td>")
       .append("<td style=\"text-align: left;\">")
       .append(qbr.isWarm() ? "WARM" : "COLD").append("</td>")
       .append("<td style=\"text-align: right;\">")
       .append(addNumericValue(qbr.getRate().getMean())).append("</td>")
       .append("<td style=\"text-align: right;\">")
       .append(addNumericValue(qbr.getRate().getSd())).append("</td>")
       .append("<td style=\"text-align: right;\">")
       .append(Long.toString(qbr.getHits())).append("</td>\n");
    out.append("  </tr>\n");
  }

  @Override
  public void end(final Writer out)
  throws IOException {
    out.append("</table>\n");
  }

}

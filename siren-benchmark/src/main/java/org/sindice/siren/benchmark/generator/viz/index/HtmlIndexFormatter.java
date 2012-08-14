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
    out.append("    <th rowspan=\"2\"></th><th colspan=\"4\">Index Size (MB)</th>" +
    "<th rowspan=\"2\">Commit Time (ms)</th><th rowspan=\"2\">Optimise Time (ms)</th>\n");
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

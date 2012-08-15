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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.sindice.siren.benchmark.generator.viz.index.IndexResultsExporter;
import org.sindice.siren.benchmark.generator.viz.index.diff.IndexDiffResultsExporter;
import org.sindice.siren.benchmark.generator.viz.query.QueryResultsExporter;
import org.sindice.siren.benchmark.generator.viz.query.diff.QueryDiffResultsExporter;

/**
 * Export the set of results using the given formatter type and the set of exporters.
 * @author Stephane Campinas [14 Aug 2012]
 * @email stephane.campinas@deri.org
 */
public class Exporter {

  @SuppressWarnings("serial")
  private final ArrayList<ResultsExporter> resExporters  = new ArrayList<ResultsExporter>() {
                                                           {
                                                             add(new QueryResultsExporter());
                                                             add(new IndexResultsExporter());
                                                           }
                                                         };

  @SuppressWarnings("serial")
  private final ArrayList<ResultsExporter> diffExporters = new ArrayList<ResultsExporter>() {
                                                           {
                                                             add(new QueryDiffResultsExporter());
                                                             add(new IndexDiffResultsExporter());
                                                           }
                                                         };

  /**
   * Iterates through each directories results, then export them using the given
   * {@link FormatterType}. The display is written in each folder of
   * directories, into <code>viz/results.html</code>.
   */
  public void export(FormatterType ft, List<File> directories, Writer o)
  throws IOException {
    Writer out = o;

    for (File dir : directories) {
      if (o == null) {
        final File vizDir = new File(dir, "viz");
        vizDir.mkdir();
        out = new BufferedWriter(new FileWriter(new File(vizDir, "results.html")));
      }
      try {
        for (ResultsExporter re : resExporters) {
          final ResultsIterator ri = re.getResultsIterator();
          final Formatter formatter = re.getFormatter(ft);

          ri.init(dir);
          while (ri.hasNext()) {
            formatter.collect(ri.next());
          }
          formatter.format(out);
          out.append("\n*************************\n\n");
        }
      }
      finally {
        out.close();
      }
    }
  }

  /**
   * Iterates through each directories results, then export them using the given
   * {@link FormatterType}. The display is written in each folder of
   * directories, into <code>viz/results.html</code>.
   */
  public void diff(FormatterType ft, List<File> directories, Writer o)
  throws IOException {
    Writer out = o;

    for (File dir : directories) {
      if (o == null) {
        final File vizDir = new File(dir, "viz");
        if (vizDir.isDirectory()) {
          FileUtils.deleteQuietly(new File(vizDir, "diff.html"));
        }
      }
    }
    for (ResultsExporter re : diffExporters) {
      final ResultsIterator ri = re.getResultsIterator();
      final Formatter formatter = re.getFormatter(ft);

      for (File dir : directories) {
        ri.init(dir);
        while (ri.hasNext()) {
          formatter.collect(ri.next());
        }
      }
      for (File dir : directories) {
        if (o == null) {
          final File vizDir = new File(dir, "viz");
          vizDir.mkdir();
          out = new BufferedWriter(new FileWriter(new File(vizDir, "diff.html"), true));
        }
        try {
          formatter.setDirectoryName(dir.getName());
          formatter.format(out);
          out.append("\n*************************\n\n");
        }
        finally {
          out.close();
        }
      }
    }
  }

}

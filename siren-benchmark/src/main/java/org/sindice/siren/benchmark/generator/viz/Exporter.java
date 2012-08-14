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
import java.util.Arrays;
import java.util.List;

import org.sindice.siren.benchmark.generator.viz.index.IndexResultsExporter;
import org.sindice.siren.benchmark.generator.viz.query.QueryResultsExporter;

/**
 * 
 * @author Stephane Campinas [14 Aug 2012]
 * @email stephane.campinas@deri.org
 *
 */
public class Exporter {

  @SuppressWarnings("serial")
  private final ArrayList<ResultsExporter> resExporters = new ArrayList<ResultsExporter>() {{
    add(new QueryResultsExporter());
    add(new IndexResultsExporter());
  }};

  /**
   * Iterates through each <code>directories</code> results, then export them
   * using the given {@link FormatterType} <code>ft</code>. The display is
   * written to the given {@link Writer} <code>o</code>.
   */
  public void export(FormatterType ft, File[] directories, Writer o)
  throws IOException {
    final BufferedWriter out = new BufferedWriter(o);

    try {
      for (ResultsExporter re: resExporters) {
        final Formatter formatter = re.getFormatter(ft);
        for (File dir: directories) {
          final ResultsIterator ri = re.getResultsIterator();
          ri.init(dir);
          while (ri.hasNext()) {
            formatter.collect(ri.next());
          }
        }
        formatter.format(out);
        out.append("\n*************************\n\n");
      }
    } finally {
      out.close();
    }
  }

  public void export(FormatterType ft, File[] directories)
  throws IOException {
    this.export(ft, Arrays.asList(directories));
  }

  /**
   * Iterates through each directories results, then export them using the given
   * {@link FormatterType}. The display is written in each folder of directories,
   * into <code>viz/results.html</code>.
   */
  public void export(FormatterType ft, List<File> directories)
  throws IOException {
    for (File dir: directories) {
      final File vizDir = new File(dir, "viz");
      vizDir.mkdir();
      final BufferedWriter out = new BufferedWriter(new FileWriter(new File(vizDir, "results.html")));
      try {
        for (ResultsExporter re: resExporters) {
          final ResultsIterator ri = re.getResultsIterator();
          final Formatter formatter = re.getFormatter(ft);
  
          ri.init(dir);
          while (ri.hasNext()) {
            formatter.collect(ri.next());
          }
          formatter.format(out);
          out.append("\n*************************\n\n");
        }
      } finally {
        out.close();
      }
    }
  }

}

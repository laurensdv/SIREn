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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestExporter {

  private final File          dir = new File("./src/test/resources/benchmark/complete");
  private final FormatterType ft;

  public TestExporter(FormatterType ft) {
    this.ft = ft;
  }

  @Parameters
  public static Collection<Object[]> formatters() {
    Object[][] data = new Object[][] { { FormatterType.HTML } };
    return Arrays.asList(data);
  }

  @Test
  public void testOutput()
  throws IOException {
    final Exporter ex = new Exporter();
    final File[] directories = new File[] {
      new File(dir, "sindice-afor-20"),
      new File(dir, "sindice-vint-20")
    };

    ex.export(ft, directories, new PrintWriter(System.out));
  }

  @Test
  public void testOutputToFile()
  throws IOException {
    final File[] directories = new File[] {
      new File(dir, "sindice-afor-20"),
      new File(dir, "sindice-vint-20")
    };

    try {
      final Exporter ex = new Exporter();
      ex.export(ft, directories);
    } finally {
      for (File d: directories) {
        FileUtils.deleteDirectory(new File(d, "viz"));
      }
    }
  }

}

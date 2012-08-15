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
import java.io.StringWriter;

import org.junit.Test;
import org.sindice.siren.benchmark.wrapper.IndexWrapperFactory.IndexWrapperType;

public class TestBenchmarkResultsFormatter {

  private final File             indexTimes = new File("./src/test/resources/benchmark/indexes/one");
  private final File             benchmark = new File("./src/test/resources/benchmark/query/complete");
  private final IndexWrapperType siren10   = IndexWrapperType.Siren10;

  @Test
  public void testTableOutput()
  throws IOException {
    final StringWriter w = new StringWriter();
    final BenchmarkResultsIterator it = new BenchmarkResultsIterator(siren10, indexTimes, benchmark);
    final HtmlBenchmarkResultsFormatter formatter = new HtmlBenchmarkResultsFormatter(w, it);

    formatter.format();
    System.out.println(w.toString());
  }

  @Test
  public void testTableCaption()
  throws IOException {
    final StringWriter w = new StringWriter();
    final BenchmarkResultsIterator it = new BenchmarkResultsIterator(siren10, indexTimes, benchmark);
    final HtmlBenchmarkResultsFormatter formatter = new HtmlBenchmarkResultsFormatter(w, it);

    formatter.setCaption("Amazing results");
    formatter.format();
    System.out.println(w.toString());
  }

  @Test
  public void testQSpecRelativeUrl()
  throws IOException {
    final StringWriter w = new StringWriter();
    final BenchmarkResultsIterator it = new BenchmarkResultsIterator(siren10, indexTimes, benchmark);
    final HtmlBenchmarkResultsFormatter formatter = new HtmlBenchmarkResultsFormatter(w, it);

    formatter.setQuerySpecUrl("http://toto/{}.txt");
    formatter.format();
    System.out.println(w.toString());
  }

  @Test
  public void testSeveralIndexesTableOutput()
  throws IOException {
    final StringWriter w = new StringWriter();
    final BenchmarkResultsIterator it = new BenchmarkResultsIterator(
      new File("src/test/resources/benchmark/indexes/several"),
      new File("./src/test/resources/benchmark/query/several-indexes")
    );
    final HtmlBenchmarkResultsFormatter formatter = new HtmlBenchmarkResultsFormatter(w, it);

    formatter.format();
    System.out.println(w.toString());
  }

}

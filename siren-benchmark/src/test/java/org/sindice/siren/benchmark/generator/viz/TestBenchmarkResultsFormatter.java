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

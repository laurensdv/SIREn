package org.sindice.siren.benchmark.generator.viz;

import java.io.Writer;

public class BenchmarkResultsFormatterFactory {

  public enum FormatterType {
    HTML
  }

  public static BenchmarkResultsFormatter getFormatter(FormatterType ft,
                                                       Writer out,
                                                       BenchmarkResultsIterator bri) {
    switch (ft) {
      case HTML:
        return new HtmlBenchmarkResultsFormatter(out, bri);
      default:
        throw new EnumConstantNotPresentException(FormatterType.class, ft.toString());
    }
  }

}

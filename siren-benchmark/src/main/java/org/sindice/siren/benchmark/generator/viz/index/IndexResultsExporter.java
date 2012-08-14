package org.sindice.siren.benchmark.generator.viz.index;

import org.sindice.siren.benchmark.generator.viz.Formatter;
import org.sindice.siren.benchmark.generator.viz.FormatterType;
import org.sindice.siren.benchmark.generator.viz.ResultsExporter;
import org.sindice.siren.benchmark.generator.viz.ResultsIterator;

public class IndexResultsExporter
implements ResultsExporter {

  @Override
  public ResultsIterator getResultsIterator() {
    return new IndexResultsIterator();
  }

  @Override
  public Formatter getFormatter(FormatterType ft) {
    switch (ft) {
      case HTML:
        return new HtmlIndexFormatter();
      default:
        throw new EnumConstantNotPresentException(FormatterType.class, ft.toString());
    }
  }

}

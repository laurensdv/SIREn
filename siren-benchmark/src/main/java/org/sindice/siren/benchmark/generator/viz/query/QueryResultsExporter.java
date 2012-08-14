package org.sindice.siren.benchmark.generator.viz.query;

import org.sindice.siren.benchmark.generator.viz.Formatter;
import org.sindice.siren.benchmark.generator.viz.FormatterType;
import org.sindice.siren.benchmark.generator.viz.ResultsExporter;
import org.sindice.siren.benchmark.generator.viz.ResultsIterator;

public class QueryResultsExporter
implements ResultsExporter {

  @Override
  public ResultsIterator getResultsIterator() {
    return new QueryResultsIterator();
  }

  @Override
  public Formatter getFormatter(FormatterType ft) {
    switch (ft) {
      case HTML:
        return new HtmlQueryFormatter();
      default:
        throw new EnumConstantNotPresentException(FormatterType.class, ft.toString());
    }
  }

}

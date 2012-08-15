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
package org.sindice.siren.benchmark.generator.viz.query.diff;

import org.sindice.siren.benchmark.generator.viz.Formatter;
import org.sindice.siren.benchmark.generator.viz.FormatterType;
import org.sindice.siren.benchmark.generator.viz.ResultsExporter;
import org.sindice.siren.benchmark.generator.viz.ResultsIterator;
import org.sindice.siren.benchmark.generator.viz.query.QueryResultsIterator;

/**
 * Query results exporter specifications for Diff-ing
 * @author Stephane Campinas [15 Aug 2012]
 * @email stephane.campinas@deri.org
 *
 */
public class QueryDiffResultsExporter
implements ResultsExporter {

  @Override
  public ResultsIterator getResultsIterator() {
    return new QueryResultsIterator();
  }

  @Override
  public Formatter getFormatter(FormatterType ft) {
    switch (ft) {
      case HTML:
        return new HtmlQueryDiffFormatter();
      default:
        throw new EnumConstantNotPresentException(FormatterType.class, ft.toString());
    }
  }

}

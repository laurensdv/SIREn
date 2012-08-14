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

import org.sindice.siren.benchmark.generator.viz.index.HtmlIndexFormatter;
import org.sindice.siren.benchmark.generator.viz.index.IndexResultsIterator;

/**
 * Contains the actual implementations to export the set of results
 * @author Stephane Campinas [14 Aug 2012]
 * @email stephane.campinas@deri.org
 *
 */
public interface ResultsExporter {

  /**
   * Return the {@link ResultsIterator} implementation which extracts the
   * desired measurements.
   */
  public ResultsIterator getResultsIterator();

  /**
   * Returns the {@link Formatter} implementation, which expects results to be
   * extracted using the associated {@link ResultsIterator}, e.g.,
   * {@link IndexResultsIterator} and {@link HtmlIndexFormatter}.
   * @param ft
   * @return
   */
  public Formatter getFormatter(FormatterType ft);

}

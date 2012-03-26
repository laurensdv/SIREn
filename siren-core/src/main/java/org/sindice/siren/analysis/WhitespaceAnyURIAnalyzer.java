/**
 * Copyright (c) 2009-2011 Sindice Limited. All Rights Reserved.
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
/**
 * @project siren-core_rdelbru
 * @author Campinas Stephane [ 3 Oct 2011 ]
 * @link stephane.campinas@deri.org
 */
package org.sindice.siren.analysis;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.util.Version;
import org.sindice.siren.analysis.filter.AssignTokenTypeFilter;

/**
 * Analyzer designed to deal with any kind of URIs. It does not perform any
 * post-processing on URIs. Only the {@link LowerCaseFilter} is used.
 */
public class WhitespaceAnyURIAnalyzer extends Analyzer {

  private final Version matchVersion;

  public WhitespaceAnyURIAnalyzer(final Version version) {
    matchVersion = version;
  }

  @Override
  protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
    final WhitespaceTokenizer source = new WhitespaceTokenizer(matchVersion, reader);
    TokenStream sink = new LowerCaseFilter(matchVersion, source);
    sink = new AssignTokenTypeFilter(sink, TupleTokenizer.URI);
    return new TokenStreamComponents(source, sink);
  }

}

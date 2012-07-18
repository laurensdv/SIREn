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
/**
 * @project siren-core
 * @author Renaud Delbru [ 13 Mar 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.analysis;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.sindice.siren.analysis.filter.PositionAttributeFilter;
import org.sindice.siren.analysis.filter.SirenPayloadFilter;

public class MockSirenAnalyzer extends Analyzer {

  public MockSirenAnalyzer() {}

  @Override
  protected TokenStreamComponents createComponents(final String fieldName,
                                                   final Reader reader) {
    final MockSirenReader mockReader = (MockSirenReader) reader;
    final MockSirenTokenizer tokenizer = new MockSirenTokenizer(mockReader);

    TokenStream sink = new PositionAttributeFilter(tokenizer);
    sink = new SirenPayloadFilter(sink);
    return new TokenStreamComponents(tokenizer, sink);
  }

  public TokenStream tokenStream() throws IOException {
    return this.tokenStream("", new StringReader(""));
  }

}

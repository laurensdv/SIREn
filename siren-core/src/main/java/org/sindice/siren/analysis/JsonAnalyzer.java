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
 * @author Stephane Campinas [ 12 Jul 2012 ]
 * @email stephane.campinas@deri.org
 */
package org.sindice.siren.analysis;

import java.io.Reader;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.CharArrayMap;
import org.apache.lucene.util.Version;
import org.sindice.siren.analysis.filter.DatatypeAnalyzerFilter;
import org.sindice.siren.analysis.filter.PositionAttributeFilter;
import org.sindice.siren.analysis.filter.SirenDeltaPayloadFilter;
import org.sindice.siren.analysis.filter.SirenPayloadFilter;

/**
 * The JsonAnalyzer is especially designed to process Json data. It applies
 * various post-processing on URIs and Literals.
 */
public class JsonAnalyzer extends Analyzer {

  private Analyzer                     stringAnalyzer;
  private Analyzer                     anyURIAnalyzer;

  private boolean                      deltaPayload = true;

  private final Version                matchVersion;

  private final CharArrayMap<Analyzer> regLitAnalyzers;

  /**
   * Create a {@link JsonAnalyzer} with the default {@link Analyzer} for Literals and URIs.
   * @param version
   * @param stringAnalyzer default Literal {@link Analyzer}
   * @param anyURIAnalyzer default URI {@link Analyzer}
   */
  public JsonAnalyzer(final Version version, final Analyzer stringAnalyzer, final Analyzer anyURIAnalyzer) {
    matchVersion = version;
    this.stringAnalyzer = stringAnalyzer;
    this.anyURIAnalyzer = anyURIAnalyzer;
    regLitAnalyzers = new CharArrayMap<Analyzer>(version, 64, false);

  }

  public void setLiteralAnalyzer(final Analyzer analyzer) {
    stringAnalyzer = analyzer;
  }

  public void setAnyURIAnalyzer(final Analyzer analyzer) {
    anyURIAnalyzer = analyzer;
  }

  public void setDeltaPayload(final boolean deltaPayload) {
    this.deltaPayload = deltaPayload;
  }

  /**
   * Assign an {@link Analyzer} to be used with that key. That analyzer is used
   * to process tokens outputed from the {@link JsonTokenizer}.
   * @param datatype
   * @param a
   */
  public void registerLiteralAnalyzer(final char[] datatype, final Analyzer a) {
    if (!regLitAnalyzers.containsKey(datatype)) {
      regLitAnalyzers.put(datatype, a);
    }
  }

  /**
   * Remove all registered {@link Analyzer}s.
   */
  public void clearRegisterLiteralAnalyzers() {
    regLitAnalyzers.clear();
  }

  @Override
  protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
    JsonTokenizer source = new JsonTokenizer(reader);

    final DatatypeAnalyzerFilter tt = new DatatypeAnalyzerFilter(matchVersion, source, stringAnalyzer, anyURIAnalyzer);
    for (final Entry<Object, Analyzer> e : regLitAnalyzers.entrySet()) {
      tt.register((char[]) e.getKey(), e.getValue());
    }
    TokenStream sink = new PositionAttributeFilter(tt);
    sink = deltaPayload ? new SirenDeltaPayloadFilter(sink) : new SirenPayloadFilter(sink);
    return new TokenStreamComponents(source, sink);
  }

}

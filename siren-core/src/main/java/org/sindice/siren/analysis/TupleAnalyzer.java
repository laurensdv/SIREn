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
 * @project siren
 * @author Renaud Delbru [ 8 Dec 2009 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
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
import org.sindice.siren.analysis.filter.TokenTypeFilter;

/**
 * The TupleAnalyzer is especially designed to process RDF data. It applies
 * various post-processing on URIs and Literals.
 */
public class TupleAnalyzer extends Analyzer {

  private Analyzer stringAnalyzer;
  private Analyzer anyURIAnalyzer;

  private boolean deltaPayload = false;

  private final Version matchVersion;

  private final CharArrayMap<Analyzer> regLitAnalyzers;

  /**
   * Create a {@link TupleAnalyzer} with the default {@link Analyzer} for Literals and URIs.
   * @param version
   * @param stringAnalyzer default Literal {@link Analyzer}
   * @param anyURIAnalyzer default URI {@link Analyzer}
   */
  public TupleAnalyzer(final Version version, final Analyzer stringAnalyzer, final Analyzer anyURIAnalyzer) {
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
   * to process tokens outputed from the {@link TupleTokenizer}.
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
    final TupleTokenizer source = new TupleTokenizer(reader);

    TokenStream sink = new TokenTypeFilter(source, new int[] {TupleTokenizer.BNODE,
                                                              TupleTokenizer.DOT});
    final DatatypeAnalyzerFilter tt = new DatatypeAnalyzerFilter(matchVersion, sink, stringAnalyzer, anyURIAnalyzer);
    for (final Entry<Object, Analyzer> e : regLitAnalyzers.entrySet()) {
      tt.register((char[]) e.getKey(), e.getValue());
    }
    sink = new PositionAttributeFilter(tt);
    sink = deltaPayload ? new SirenDeltaPayloadFilter(sink) : new SirenPayloadFilter(sink);
    return new TokenStreamComponents(source, sink);
  }

}

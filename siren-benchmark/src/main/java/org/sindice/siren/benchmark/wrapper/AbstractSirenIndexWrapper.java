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
 * @project siren-benchmark
 * @author Renaud Delbru [ 12 Jul 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.benchmark.wrapper;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.miscellaneous.LengthFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.util.Version;
import org.sindice.siren.analysis.JsonTokenizer;
import org.sindice.siren.analysis.filter.DatatypeAnalyzerFilter;
import org.sindice.siren.analysis.filter.PositionAttributeFilter;
import org.sindice.siren.analysis.filter.SirenPayloadFilter;
import org.sindice.siren.benchmark.generator.lexicon.TermFreqIterator;
import org.sindice.siren.benchmark.query.provider.Query;
import org.sindice.siren.benchmark.query.task.QueryTask;
import org.sindice.siren.benchmark.query.task.SirenMultiQueryTask;
import org.sindice.siren.benchmark.query.task.SirenSingleQueryTask;

public abstract class AbstractSirenIndexWrapper extends AbstractIndexWrapper {

  public AbstractSirenIndexWrapper(final File path) throws IOException {
    super(path);
  }

  @Override
  public TermFreqIterator getTermFreqIterator(final int minFreq)
  throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  protected Analyzer initializeAnalyzer() {
    return new Analyzer() {

      @Override
      protected TokenStreamComponents createComponents(final String fieldName,
                                                       final Reader reader) {
        final Version matchVersion = Version.LUCENE_40;
        final JsonTokenizer src = new JsonTokenizer(reader);
        TokenStream tok = new DatatypeAnalyzerFilter(matchVersion, src,
          new StandardAnalyzer(matchVersion), new StandardAnalyzer(matchVersion));
        tok = new LengthFilter(true, tok, 3, StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH);
        tok = new LowerCaseFilter(matchVersion, tok);
        tok = new StopFilter(matchVersion, tok, StandardAnalyzer.STOP_WORDS_SET);
        tok = new PositionAttributeFilter(tok);
        tok = new SirenPayloadFilter(tok);
        return new TokenStreamComponents(src, tok);
      }

    };
  }

  @Override
  public QueryTask issueQuery(final Query query) throws IOException {
    if (mgr == null) {
      mgr = new SearcherManager(dir, null);
    }
    return new SirenSingleQueryTask(query, mgr);
  }

  @Override
  public QueryTask issueQueries(final List<Query> queries) throws IOException {
    if (mgr == null) {
      mgr = new SearcherManager(dir, null);
    }
    return new SirenMultiQueryTask(queries, mgr);
  }

}

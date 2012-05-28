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
 * @author Renaud Delbru [ 14 Jan 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.util;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Collection;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.codecs.PostingsFormat;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.RandomIndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.junit.After;
import org.junit.Before;
import org.sindice.siren.analysis.MockSirenAnalyzer;
import org.sindice.siren.analysis.MockSirenDocument;
import org.sindice.siren.index.codecs.RandomSirenCodec;
import org.sindice.siren.index.codecs.RandomSirenCodec.PostingsFormatType;
import org.sindice.siren.index.codecs.siren02.Siren02PostingsFormat;

public abstract class BasicSirenTestCase extends SirenTestCase {

  protected Directory directory;
  protected RandomIndexWriter writer;
  protected IndexReader reader;
  protected IndexSearcher searcher;
  protected Analyzer analyzer;
  protected RandomSirenCodec codec;

  private AnalyzerType analyzerType;

  public enum AnalyzerType {
    MOCK, TUPLE
  }

  /**
   * Default configuration for the tests.
   * <p>
   * Overrides must call {@link #setAnalyzer(AnalyzerType)} and
   * {@link #setPostingsFormat(PostingsFormatType)} or
   * {@link #setPostingsFormat(PostingsFormat)}
   */
  protected abstract void configure() throws IOException;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    this.configure();
  }

  private void init() throws IOException {
    directory = newDirectory();
    writer = this.newRandomIndexWriter(directory, analyzer, codec);
    this.deleteAll(writer);
    reader = this.newIndexReader(writer);
    searcher = newSearcher(reader);
  }

  @Override
  @After
  public void tearDown() throws Exception {
    this.close();
    super.tearDown();
  }

  private void close() throws IOException {
    if (reader != null) {
      reader.close();
      reader = null;
    }
    if (writer != null) {
      writer.close();
      writer = null;
    }
    if (directory != null) {
      directory.close();
      directory = null;
    }
  }

  /**
   * Set a new postings format for a single test
   */
  protected void setPostingsFormat(final PostingsFormatType format)
  throws IOException {
    codec = new RandomSirenCodec(random(), format);
    if (analyzerType != null) {
      analyzer = this.initAnalyzer(analyzerType, codec);
      this.close();
      this.init();
    }
  }

  /**
   * Set a new postings format for a single test
   * @throws IOException
   */
  protected void setPostingsFormat(final PostingsFormat format)
  throws IOException {
    codec = new RandomSirenCodec(random(), format);
    if (analyzerType != null) {
      analyzer = this.initAnalyzer(analyzerType, codec);
      this.close();
      this.init();
    }
  }

  /**
   * Set a new analyzer for a single test
   */
  protected void setAnalyzer(final AnalyzerType analyzerType) throws IOException {
    this.analyzerType = analyzerType;
    if (codec != null) {
      analyzer = this.initAnalyzer(analyzerType, codec);
      this.close();
      this.init();
    }
  }

  protected void refreshReaderAndSearcher() throws IOException {
    reader.close();
    reader = this.newIndexReader(writer);
    searcher = newSearcher(reader);
  }

  protected void addDocument(final String data)
  throws IOException {
    this.addDocument(writer, data);
    this.refreshReaderAndSearcher();
  }

  protected void addDocumentNoNorms(final String data)
  throws IOException {
    this.addDocumentNoNorms(writer, data);
    this.refreshReaderAndSearcher();
  }

  protected void addDocuments(final Collection<String> docs)
  throws IOException {
    this.addDocuments(writer, docs.toArray(new String[docs.size()]));
    this.refreshReaderAndSearcher();
  }

  protected void addDocuments(final String ... docs)
  throws IOException {
    this.addDocuments(writer, docs);
    this.refreshReaderAndSearcher();
  }

  protected void addDocuments(final MockSirenDocument ... sdocs)
  throws IOException {
    this.addDocuments(writer, sdocs);
    this.refreshReaderAndSearcher();
  }

  protected void deleteAll() throws IOException {
    this.deleteAll(writer);
    this.refreshReaderAndSearcher();
  }

  public void forceMerge() throws IOException {
    this.forceMerge(writer);
    this.refreshReaderAndSearcher();
  }

  private Analyzer initAnalyzer(final AnalyzerType analyzerType, final RandomSirenCodec codec) {
    final PostingsFormat format = codec.getPostingsFormatForField(SirenTestCase.DEFAULT_TEST_FIELD);
    switch (analyzerType) {
      case MOCK:
        if (format instanceof Siren02PostingsFormat) {
          return new MockSirenAnalyzer(true);
        }
        return new MockSirenAnalyzer(false);

      case TUPLE:
        if (format instanceof Siren02PostingsFormat) {
          return SirenTestCase.newTupleAnalyzer(true);
        }
        return SirenTestCase.newTupleAnalyzer(false);

      default:
        throw new InvalidParameterException();
    }
  }

}
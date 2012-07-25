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
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.codecs.PostingsFormat;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.RandomIndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.junit.After;
import org.junit.Before;
import org.sindice.siren.analysis.MockSirenAnalyzer;
import org.sindice.siren.analysis.MockSirenDocument;
import org.sindice.siren.index.codecs.RandomSirenCodec;
import org.sindice.siren.index.codecs.RandomSirenCodec.PostingsFormatType;

public abstract class BasicSirenTestCase extends SirenTestCase {

  protected Directory directory;
  protected RandomIndexWriter writer;
  protected IndexReader reader;
  protected IndexSearcher searcher;
  protected Analyzer analyzer;
  protected RandomSirenCodec codec;
  private IndexWriterConfig config;

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
    if (config == null) {
      writer = newRandomIndexWriter(directory, analyzer, codec);
    } else {
      writer = newRandomIndexWriter(directory, analyzer, codec, config);
    }
    this.deleteAll(writer);
    reader = newIndexReader(writer);
    searcher = newSearcher(reader);
  }

  @Override
  @After
  public void tearDown() throws Exception {
    this.close();
    config = null;
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

  protected void setIndexWriterConfig(final IndexWriterConfig config)
  throws IOException {
    this.config = config;
    if (analyzer != null || codec != null) {
      this.close();
      this.init();
    }
  }

  /**
   * Set a new postings format for a single test
   */
  protected void setPostingsFormat(final PostingsFormatType format)
  throws IOException {
    codec = new RandomSirenCodec(random(), format);
    if (analyzer != null) {
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
    if (analyzer != null) {
      this.close();
      this.init();
    }
  }

  /**
   * Set a new analyzer for a single test
   */
  protected void setAnalyzer(final AnalyzerType analyzerType) throws IOException {
    this.analyzer = this.initAnalyzer(analyzerType);
    if (codec != null) {
      this.close();
      this.init();
    }
  }

  /**
   * Set a new analyzer for a single test
   */
  protected void setAnalyzer(final Analyzer analyzer) throws IOException {
    this.analyzer = analyzer;
    if (codec != null) {
      this.close();
      this.init();
    }
  }

  protected void refreshReaderAndSearcher() throws IOException {
    reader.close();
    reader = newIndexReader(writer);
    searcher = newSearcher(reader);
  }

  protected void addDocument(final String data)
  throws IOException {
    addDocument(writer, data);
    this.refreshReaderAndSearcher();
  }

  protected void addDocument(final Document doc)
  throws IOException {
    writer.addDocument(doc);
    writer.commit();
    this.refreshReaderAndSearcher();
  }

  protected void addDocuments(final List<Document> docs)
  throws IOException {
    writer.addDocuments(docs);
    writer.commit();
    this.refreshReaderAndSearcher();
  }

  protected void addDocumentNoNorms(final String data)
  throws IOException {
    this.addDocumentNoNorms(writer, data);
    this.refreshReaderAndSearcher();
  }

  protected void addDocuments(final Collection<String> docs)
  throws IOException {
    addDocuments(writer, docs.toArray(new String[docs.size()]));
    this.refreshReaderAndSearcher();
  }

  protected void addDocuments(final String ... docs)
  throws IOException {
    addDocuments(writer, docs);
    this.refreshReaderAndSearcher();
  }

  protected void addDocuments(final MockSirenDocument ... sdocs)
  throws IOException {
    addDocuments(writer, sdocs);
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

  private Analyzer initAnalyzer(final AnalyzerType analyzerType) {
    switch (analyzerType) {
      case MOCK:
        return new MockSirenAnalyzer();

      case TUPLE:
        return SirenTestCase.newTupleAnalyzer();

      default:
        throw new InvalidParameterException();
    }
  }

}

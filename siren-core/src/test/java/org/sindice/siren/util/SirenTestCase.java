/**
 * Copyright (c) 2009-2011 National University of Ireland, Galway. All Rights Reserved.
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
 * @author Renaud Delbru [ 11 Jan 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.util;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.Codec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.RandomIndexWriter;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.LuceneTestCase;
import org.sindice.siren.analysis.AnyURIAnalyzer;
import org.sindice.siren.analysis.AnyURIAnalyzer.URINormalisation;
import org.sindice.siren.analysis.MockSirenAnalyzer;
import org.sindice.siren.analysis.MockSirenDocument;
import org.sindice.siren.analysis.MockSirenReader;
import org.sindice.siren.analysis.TupleAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SirenTestCase extends LuceneTestCase {

  protected static final Logger logger = LoggerFactory.getLogger(SirenTestCase.class);

  public static final String DEFAULT_TEST_FIELD = "content";

  public static Analyzer newTupleAnalyzer() {
    final AnyURIAnalyzer uriAnalyzer = new AnyURIAnalyzer(TEST_VERSION_CURRENT);
    uriAnalyzer.setUriNormalisation(URINormalisation.FULL);
    final TupleAnalyzer analyzer = new TupleAnalyzer(TEST_VERSION_CURRENT,
      new StandardAnalyzer(TEST_VERSION_CURRENT), uriAnalyzer);
    return analyzer;
  }

  public static Analyzer newMockAnalyzer() {
    return new MockSirenAnalyzer();
  }

  private FieldType newFieldType() {
    final FieldType ft = new FieldType();
    ft.setStored(false);
    ft.setOmitNorms(false);
    ft.setIndexed(true);
    ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
    ft.setTokenized(true);
    return ft;
  }

  private FieldType newStoredFieldType() {
    final FieldType ft = this.newFieldType();
    ft.setStored(true);
    return ft;
  }

  private FieldType newStoredNoNormFieldType() {
    final FieldType ft = this.newStoredFieldType();
    ft.setOmitNorms(true);
    return ft;
  }

  protected RandomIndexWriter newRandomIndexWriter(final Directory dir,
                                                   final Analyzer analyzer,
                                                   final Codec codec)
  throws IOException {
    final RandomIndexWriter writer = new RandomIndexWriter(random(), dir,
      newIndexWriterConfig(TEST_VERSION_CURRENT, analyzer)
      .setCodec(codec)
      .setMergePolicy(newLogMergePolicy())
      .setSimilarity(new DefaultSimilarity()));
    writer.setDoRandomForceMergeAssert(true);
    return writer;
  }

  protected IndexReader newIndexReader(final RandomIndexWriter writer)
  throws IOException {
    return SlowCompositeReaderWrapper.wrap(writer.getReader());
  }

  protected IndexSearcher newIndexSearcher(final RandomIndexWriter writer)
  throws IOException {
    final IndexReader indexReader = SlowCompositeReaderWrapper.wrap(writer.getReader());
    final IndexSearcher indexSearcher = newSearcher(indexReader);
    indexSearcher.setSimilarity(new DefaultSimilarity());
    return indexSearcher;
  }

  protected void addDocument(final RandomIndexWriter writer, final String data)
  throws IOException {
    final Document doc = new Document();
    doc.add(new Field(DEFAULT_TEST_FIELD, data, this.newStoredFieldType()));
    writer.addDocument(doc);
    writer.commit();
  }

  protected void addDocumentNoNorms(final RandomIndexWriter writer, final String data)
  throws IOException {
    final Document doc = new Document();
    doc.add(new Field(DEFAULT_TEST_FIELD, data, this.newStoredNoNormFieldType()));
    writer.addDocument(doc);
    writer.commit();
  }

  /**
   * Atomically adds a block of documents with sequentially
   * assigned document IDs.
   * <br>
   * See also {@link IndexWriter#addDocuments(Iterable)}
   */
  protected void addDocuments(final RandomIndexWriter writer,
                              final String[] data)
  throws IOException {
    final ArrayList<Document> docs = new ArrayList<Document>();

    for (final String entry : data) {
      final Document doc = new Document();
      doc.add(new Field(DEFAULT_TEST_FIELD, entry, this.newStoredFieldType()));
      docs.add(doc);
    }
    writer.addDocuments(docs);
    writer.commit();
  }

  protected void addDocuments(final RandomIndexWriter writer,
                              final MockSirenDocument ... sdocs)
  throws IOException {
    final ArrayList<Document> docs = new ArrayList<Document>(sdocs.length);
    for (final MockSirenDocument sdoc : sdocs) {
      final Document doc = new Document();
      doc.add(new Field(DEFAULT_TEST_FIELD, new MockSirenReader(sdoc), this.newFieldType()));
      docs.add(doc);
    }
    writer.addDocuments(docs);
    writer.commit();
  }

  protected void deleteAll(final RandomIndexWriter writer) throws IOException {
    writer.deleteAll();
    writer.commit();
  }

  protected void forceMerge(final RandomIndexWriter writer) throws IOException {
    logger.debug("Force merge");
    writer.forceMerge(1);
  }

}

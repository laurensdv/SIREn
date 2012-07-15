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
 * @author Renaud Delbru [ 6 Jul 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.benchmark.wrapper;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.codecs.Codec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LogByteSizeMergePolicy;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.sindice.siren.benchmark.generator.document.BenchmarkDocument;

public abstract class AbstractIndexWrapper implements IndexWrapper {

  protected final File path;

  protected Directory dir;
  protected IndexWriter writer;
  protected SearcherManager mgr;

  public static final String DEFAULT_CONTENT_FIELD = "content";
  public static final String DEFAULT_URL_FIELD = "url";

  public AbstractIndexWrapper(final File path) throws IOException {
    this.path = path;
    dir = this.initializeDirectory();
  }

  private Directory initializeDirectory() throws IOException {
    return FSDirectory.open(this.path);
  }

  protected abstract Analyzer initializeAnalyzer();

  protected abstract Codec initializeCodec();

  private IndexWriter initializeIndexWriter() throws IOException {
    final IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40,
      this.initializeAnalyzer());

    // set codec
    config.setCodec(this.initializeCodec());

    // disable auto commit - add enough ram to be sure that flush will be not
    // triggered
    config.setRAMBufferSizeMB(256);
    config.setMaxBufferedDocs(IndexWriterConfig.DISABLE_AUTO_FLUSH);
    config.setMaxBufferedDeleteTerms(IndexWriterConfig.DISABLE_AUTO_FLUSH);

    final LogByteSizeMergePolicy mergePolicy = new LogByteSizeMergePolicy();
    // Disable compound file
    mergePolicy.setUseCompoundFile(false);
    // Increase merge factor to 20 - more adapted to batch creation
    mergePolicy.setMergeFactor(20);

    return new IndexWriter(dir, config);
  }

  protected IndexWriter getWriter() throws IOException {
    if (writer == null) {
      writer = this.initializeIndexWriter();
    }
    return writer;
  }

  @Override
  public void addDocument(final BenchmarkDocument document)
  throws IOException {
    final Document doc = new Document();

    final FieldType contentFieldType = new FieldType();
    contentFieldType.setIndexed(true);
    contentFieldType.setTokenized(true);
    contentFieldType.setOmitNorms(true);
    contentFieldType.setStored(false);
    contentFieldType.setStoreTermVectors(false);

    for (final String value : document.getContent()) {
      doc.add(new Field(DEFAULT_CONTENT_FIELD, value, contentFieldType));
    }

    this.getWriter().addDocument(doc);
  }

  public void close() throws IOException {
    if (writer != null) { writer.close(); }
    dir.close();
  }

  @Override
  public void commit() throws IOException {
    this.getWriter().commit();
  }

  @Override
  public void forceMerge() throws IOException {
    this.getWriter().forceMerge(1, true);
  }

  @Override
  public void flushCache() throws IOException {
    if (mgr != null) { mgr.close(); }
    if (writer != null) writer.close();
    dir.close();
    dir = this.initializeDirectory();
    writer = this.initializeIndexWriter();
    mgr = new SearcherManager(dir, null);
  }

  public ScoreDoc[] search(final Query q, final int n) throws IOException {
    if (mgr == null) {
      mgr = new SearcherManager(dir, null);
    }
    IndexSearcher searcher = mgr.acquire();

    try {
      return searcher.search(q, null, n).scoreDocs;
    }
    finally {
      mgr.release(searcher);
      searcher = null;
    }
  }

}

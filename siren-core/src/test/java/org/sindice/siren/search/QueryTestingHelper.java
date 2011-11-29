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
 * @author Renaud Delbru [ 10 Dec 2009 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

public class QueryTestingHelper {

  public static final Version TEST_VERSION = Version.LUCENE_40;
  
  public RAMDirectory  _dir;

  public IndexWriter   _writer;
  
  private final FieldType _storedWithNorm = new FieldType();
  private final FieldType _storedNoNorm = new FieldType();
  
  public Analyzer      _analyzer;

  public static final String DEFAULT_FIELD = "content";

  public QueryTestingHelper(final Analyzer analyzer) throws CorruptIndexException, IOException {
    _analyzer = analyzer;
    this.initiate();
  }

  public void initiate()
  throws CorruptIndexException, IOException {
    _dir = new RAMDirectory();
    final IndexWriterConfig config = new IndexWriterConfig(TEST_VERSION, _analyzer);
    // TODO: check where the max field length can be set
    _writer = new IndexWriter(_dir, config);
    
    _storedWithNorm.setStored(true);
    _storedWithNorm.setOmitNorms(false);
    _storedWithNorm.setIndexed(true);
    _storedWithNorm.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
    _storedWithNorm.setTokenized(true);
    
    _storedNoNorm.setStored(true);
    _storedNoNorm.setOmitNorms(true);
    _storedNoNorm.setIndexed(true);
    _storedNoNorm.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
    _storedNoNorm.setTokenized(true);
  }

  public void reset()
  throws CorruptIndexException, IOException {
    this.close();
    this.initiate();
  }

  public IndexWriter getIndexWriter() {
    return _writer;
  }

  public IndexReader getIndexReader()
  throws CorruptIndexException, IOException {
    return IndexReader.open(_dir);
  }

  /**
   * Return a fresh searcher. This is necessary because the searcher cannot
   * found document added after its initialisation.
   */
  public IndexSearcher getSearcher()
  throws IOException {
    // Instantiate a new searcher
    return new IndexSearcher(this.getIndexReader());
  }

  public void close()
  throws CorruptIndexException, IOException {
    _writer.close();
    _dir.close();
  }

  public void addDocument(final String data)
  throws CorruptIndexException, IOException {
    final Document doc = new Document();
    doc.add(new Field(DEFAULT_FIELD, data, _storedWithNorm));
    _writer.addDocument(doc);
    _writer.commit();
  }

  public void addDocumentNoNorms(final String data)
  throws CorruptIndexException, IOException {
    final Document doc = new Document();
    doc.add(new Field(DEFAULT_FIELD, data, _storedNoNorm));
    _writer.addDocument(doc);
    _writer.commit();
  }

  public void addDocuments(final Collection<String> data)
  throws CorruptIndexException, IOException {
    for (final String doc : data)
      this.addDocument(doc);
  }

  public void addDocuments(final String[] data)
  throws CorruptIndexException, IOException {
    for (final String doc : data)
      this.addDocument(doc);
  }
  
  /**
   * Lucene 4.0 index documents by blocks, therefore the order with which they were
   * added may not be the same as the order they were written to the index.
   * For tests which depends on this order, this method keeps the original order
   * because all documents are added to the same block.
   * 
   * <pre>
   * the index does not currently record which documents were added as a block.
   * Today this is fine, because merging will preserve the block (as long as none
   * them were deleted). But it's possible in the future that Lucene may more
   * aggressively re-order documents (for example, perhaps to obtain better index
   * compression), in which case you may need to fully re-index your documents at
   * that time. ({@link IndexWriter#addDocuments(Iterable)}) 
   * @param data
   * @throws CorruptIndexException
   * @throws IOException
   */
  public void addDocumentsWithIterator(final Collection<String> data)
  throws CorruptIndexException, IOException {
    final ArrayList<Document> docs = new ArrayList<Document>();
    
    for (String d : data) {
      final Document doc = new Document();
      doc.add(new Field(DEFAULT_FIELD, d, _storedWithNorm));
      docs.add(doc);
    }
    _writer.addDocuments(docs);
    _writer.commit();
  }

  public void addDocumentsWithIterator(final String[] data)
  throws CorruptIndexException, IOException {
    final ArrayList<Document> docs = new ArrayList<Document>();
    
    for (String d : data) {
      final Document doc = new Document();
      doc.add(new Field(DEFAULT_FIELD, d, _storedWithNorm));
      docs.add(doc);
    }
    _writer.addDocuments(docs);
    _writer.commit();
  }

  public ScoreDoc[] search(final Query q) throws IOException {
    return this.getSearcher().search(q, null, 1000).scoreDocs;
  }

}

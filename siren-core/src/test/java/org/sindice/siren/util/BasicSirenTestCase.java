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
import java.util.Collection;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.RandomIndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.junit.After;
import org.junit.Before;
import org.sindice.siren.analysis.MockSirenDocument;

public abstract class BasicSirenTestCase extends SirenTestCase {

  protected Directory directory;
  protected RandomIndexWriter writer;
  protected IndexReader reader;
  protected IndexSearcher searcher;
  protected Analyzer analyzer;

  protected abstract Analyzer initAnalyzer();

  @Override
  @Before
  public void setUp()
  throws Exception {
    super.setUp();

    analyzer = this.initAnalyzer();

    directory = newDirectory();

    writer = this.newRandomIndexWriter(directory, analyzer);
    super.deleteAll(writer);
    reader = this.newIndexReader(writer);
    searcher = newSearcher(reader);
  }

  @Override
  @After
  public void tearDown()
  throws Exception {
    reader.close();
    writer.close();
    directory.close();
    super.tearDown();
  }

  protected void refreshReaderAndSearcher() throws IOException {
    reader.close();
    reader = this.newIndexReader(writer);
    searcher = newSearcher(reader);
  }

  protected void addDocument(final String data)
  throws IOException {
    super.addDocument(writer, data);
    this.refreshReaderAndSearcher();
  }

  protected void addDocumentNoNorms(final String data)
  throws IOException {
    super.addDocumentNoNorms(writer, data);
    this.refreshReaderAndSearcher();
  }

  protected void addDocumentsWithIterator(final Collection<String> data)
  throws IOException {
    super.addDocumentsWithIterator(writer, data);
    this.refreshReaderAndSearcher();
  }

  protected void addDocumentsWithIterator(final String ... docs)
  throws IOException {
    super.addDocumentsWithIterator(writer, docs);
    this.refreshReaderAndSearcher();
  }

  public void addDocuments(final MockSirenDocument ... sdocs)
  throws IOException {
    super.addDocuments(writer, sdocs);
    this.refreshReaderAndSearcher();
  }

  protected void deleteAll() throws IOException {
    super.deleteAll(writer);
    this.refreshReaderAndSearcher();
  }

}

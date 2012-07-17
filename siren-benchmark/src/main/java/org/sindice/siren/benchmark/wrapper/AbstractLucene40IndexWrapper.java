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
 * @author Renaud Delbru [ 8 Jul 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.benchmark.wrapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.lucene.codecs.Codec;
import org.apache.lucene.codecs.lucene40.Lucene40Codec;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;
import org.sindice.siren.benchmark.generator.lexicon.TermFreq;
import org.sindice.siren.benchmark.generator.lexicon.TermFreqIterator;
import org.sindice.siren.benchmark.query.provider.Query;
import org.sindice.siren.benchmark.query.task.QueryTask;

public abstract class AbstractLucene40IndexWrapper extends AbstractIndexWrapper {

  public AbstractLucene40IndexWrapper(final File path) throws IOException {
    super(path);
  }

  @Override
  protected Codec initializeCodec() {
    return new Lucene40Codec();
  }

  @Override
  public QueryTask issueQuery(final Query query) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public QueryTask issueQueries(final List<Query> queries)
  throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public TermFreqIterator getTermFreqIterator(final int minFreq) throws IOException {
    return new LuceneTermFreqIterator(DirectoryReader.open(dir), minFreq);
  }

  private static class LuceneTermFreqIterator extends TermFreqIterator {

    private final IndexReader reader;
    private final TermsEnum it;
    private final int minFreq;

    public LuceneTermFreqIterator(final IndexReader reader, final int minFreq) throws IOException {
      this.reader = reader;
      this.minFreq = minFreq;
      it = MultiFields.getFields(reader).terms(DEFAULT_CONTENT_FIELD).iterator(null);
    }

    @Override
    public boolean hasNext() {
      try {
        while (it.next() != null) {
          if (it.docFreq() >= minFreq) {
            return true;
          }
        }
        return false;
      }
      catch (final IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public TermFreq next() {
      BytesRef ref;
      try {
        ref = it.term();
        return new TermFreq(ref.utf8ToString(), it.docFreq());
      }
      catch (final IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
      reader.close();
    }

  }

}

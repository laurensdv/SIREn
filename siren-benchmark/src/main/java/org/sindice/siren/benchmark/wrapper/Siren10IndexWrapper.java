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

import org.apache.lucene.codecs.Codec;
import org.apache.lucene.codecs.PostingsFormat;
import org.apache.lucene.codecs.lucene40.Lucene40Codec;
import org.apache.lucene.codecs.lucene40.Lucene40PostingsFormat;
import org.sindice.siren.benchmark.generator.document.BenchmarkDocument;
import org.sindice.siren.index.codecs.siren10.Siren10PostingsFormat;

public class Siren10IndexWrapper extends AbstractSirenIndexWrapper {

  private final int blockSize;

  private static int DEFAULT_BLOCK_SIZE = 256;

  public Siren10IndexWrapper(final File path) throws IOException {
    this(path, DEFAULT_BLOCK_SIZE);
  }

  public Siren10IndexWrapper(final File path, final int blockSize) throws IOException {
    super(path);
    this.blockSize = blockSize;
  }

  @Override
  protected Codec initializeCodec() {
    return new Siren10Codec(blockSize);
  }

  private class Siren10Codec extends Lucene40Codec {

    final PostingsFormat lucene40 = new Lucene40PostingsFormat();
    PostingsFormat defaultTestFormat;

    public Siren10Codec(final int blockSize) {
      this.defaultTestFormat = new Siren10PostingsFormat(blockSize);
      Codec.setDefault(this);
    }

    @Override
    public PostingsFormat getPostingsFormatForField(final String field) {
      if (field.equals(BenchmarkDocument.DEFAULT_CONTENT_FIELD)) {
        return defaultTestFormat;
      }
      else {
        return lucene40;
      }
    }

    @Override
    public String toString() {
      return "Siren10Codec[" + defaultTestFormat.toString() + "]";
    }

  }

}

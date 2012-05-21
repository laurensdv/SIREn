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
 * @author Renaud Delbru [ 1 Apr 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.index.codecs.siren10;

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.codecs.BlockTreeTermsReader;
import org.apache.lucene.codecs.BlockTreeTermsWriter;
import org.apache.lucene.codecs.FieldsConsumer;
import org.apache.lucene.codecs.FieldsProducer;
import org.apache.lucene.codecs.PostingsFormat;
import org.apache.lucene.codecs.PostingsReaderBase;
import org.apache.lucene.codecs.PostingsWriterBase;
import org.apache.lucene.index.SegmentInfo;
import org.apache.lucene.index.SegmentReadState;
import org.apache.lucene.index.SegmentWriteState;
import org.sindice.siren.index.codecs.block.VIntBlockCompressor;
import org.sindice.siren.index.codecs.block.VIntBlockDecompressor;

public class Siren10PostingsFormat extends PostingsFormat {

  private static final int DEFAULT_POSTINGS_BLOCK_SIZE = 512;

  private final int blockSize;

  public Siren10PostingsFormat() {
    this(DEFAULT_POSTINGS_BLOCK_SIZE);
  }

  public Siren10PostingsFormat(final int blockSize) {
    super("siren10");
    this.blockSize = blockSize;
  }

  private Siren10BlockStreamFactory getFactory() {
    final Siren10BlockStreamFactory factory = new Siren10BlockStreamFactory(blockSize);
    factory.setDocsBlockCompressor(new VIntBlockCompressor());
    factory.setFreqBlockCompressor(new VIntBlockCompressor());
    factory.setNodBlockCompressor(new VIntBlockCompressor());
    factory.setPosBlockCompressor(new VIntBlockCompressor());
    factory.setDocsBlockDecompressor(new VIntBlockDecompressor());
    factory.setFreqBlockDecompressor(new VIntBlockDecompressor());
    factory.setNodBlockDecompressor(new VIntBlockDecompressor());
    factory.setPosBlockDecompressor(new VIntBlockDecompressor());
    return factory;
  }

  @Override
  public FieldsConsumer fieldsConsumer(final SegmentWriteState state)
  throws IOException {
    final PostingsWriterBase docs = new Siren10PostingsWriter(state,
      this.getFactory());

    boolean success = false;
    try {
      final FieldsConsumer ret = new BlockTreeTermsWriter(state, docs,
        BlockTreeTermsWriter.DEFAULT_MIN_BLOCK_SIZE,
        BlockTreeTermsWriter.DEFAULT_MAX_BLOCK_SIZE);
      success = true;
      return ret;
    }
    finally {
      if (!success) {
        docs.close();
      }
    }
  }

  @Override
  public FieldsProducer fieldsProducer(final SegmentReadState state)
  throws IOException {
    final PostingsReaderBase postings = new Siren10PostingsReader(state.dir,
      state.segmentInfo, state.context, state.segmentSuffix,
      this.getFactory());

    boolean success = false;
    try {
      final FieldsProducer ret = new BlockTreeTermsReader(state.dir,
                                                    state.fieldInfos,
                                                    state.segmentInfo.name,
                                                    postings,
                                                    state.context,
                                                    state.segmentSuffix,
                                                    state.termsIndexDivisor);
      success = true;
      return ret;
    }
    finally {
      if (!success) {
        postings.close();
      }
    }
  }

  @Override
  public void files(final SegmentInfo segmentInfo, final String segmentSuffix,
                    final Set<String> files)
  throws IOException {
    Siren10PostingsReader.files(segmentInfo, segmentSuffix, files);
    BlockTreeTermsReader.files(segmentInfo, segmentSuffix, files);
  }

}

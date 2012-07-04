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
 * @author Renaud Delbru [ 25 May 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.index.codecs.siren02;

import java.io.IOException;

import org.apache.lucene.codecs.BlockTermState;
import org.apache.lucene.codecs.lucene40.Lucene40PostingsReader;
import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.FieldInfos;
import org.apache.lucene.index.SegmentInfo;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.sindice.siren.index.SirenDocsEnum;

public class Siren02PostingsReader extends Lucene40PostingsReader {

  public Siren02PostingsReader(final Directory dir, FieldInfos fieldInfos, final SegmentInfo segmentInfo,
                               final IOContext ioContext, final String segmentSuffix)
  throws IOException {
    super(dir, fieldInfos, segmentInfo, ioContext, segmentSuffix);
  }

  @Override
  public DocsAndPositionsEnum docsAndPositions(final FieldInfo fieldInfo,
                                               final BlockTermState termState,
                                               final Bits liveDocs,
                                               final DocsAndPositionsEnum reuse,
                                               final boolean needsOffsets)
  throws IOException {
    Siren02DocsEnum docsEnum;
    DocsAndPositionsEnum newDocsEnum;

    if (reuse == null) {
      newDocsEnum = super.docsAndPositions(fieldInfo, termState, liveDocs, null, needsOffsets);
      docsEnum = new Siren02DocsEnum(newDocsEnum);
    }
    else {
      docsEnum = (Siren02DocsEnum) reuse;
      newDocsEnum = super.docsAndPositions(fieldInfo, termState, liveDocs, docsEnum.getDocsEnum(), needsOffsets);
      docsEnum.setDocsEnum(newDocsEnum);
    }

    return docsEnum;
  }

  class Siren02DocsEnum extends SirenDocsEnum {

    DocsAndPositionsEnum docsEnum;

    private Siren02DocsEnum(final DocsAndPositionsEnum docsEnum) {
      this.docsEnum = docsEnum;
    }

    public void setDocsEnum(final DocsAndPositionsEnum docsEnum) {
      this.docsEnum = docsEnum;
    }

    public DocsAndPositionsEnum getDocsEnum() {
      return docsEnum;
    }

    @Override
    public Siren02DocsNodesAndPositionsEnum getDocsNodesAndPositionsEnum() {
      return new Siren02DocsNodesAndPositionsEnum(docsEnum);
    }

    @Override
    public int nextDoc() throws IOException {
      return docsEnum.nextDoc();
    }

    @Override
    public int freq() throws IOException {
      return docsEnum.freq();
    }

    @Override
    public int docID() {
      return docsEnum.docID();
    }

    @Override
    public int advance(final int target) throws IOException {
      return docsEnum.advance(target);
    }

    @Override
    public int nextPosition() throws IOException {
      return docsEnum.nextPosition();
    }

    @Override
    public int startOffset() throws IOException {
      return docsEnum.startOffset();
    }

    @Override
    public int endOffset() throws IOException {
      return docsEnum.endOffset();
    }

    @Override
    public BytesRef getPayload() throws IOException {
      return docsEnum.getPayload();
    }

    @Override
    public boolean hasPayload() {
      return docsEnum.hasPayload();
    }

  }

}

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
 * @project siren-core
 * @author Campinas Stephane [ 25 Nov 2011 ]
 * @link stephane.campinas@deri.org
 */
package org.sindice.siren.index.codecs.siren020;

import org.apache.lucene.index.codecs.Codec;
import org.apache.lucene.index.codecs.DocValuesFormat;
import org.apache.lucene.index.codecs.FieldInfosFormat;
import org.apache.lucene.index.codecs.PostingsFormat;
import org.apache.lucene.index.codecs.SegmentInfosFormat;
import org.apache.lucene.index.codecs.StoredFieldsFormat;
import org.apache.lucene.index.codecs.TermVectorsFormat;
import org.apache.lucene.index.codecs.lucene40.Lucene40Codec;

/**
 * 
 */
public class Siren020Codec extends Codec {

  private final Lucene40Codec lucene40 = new Lucene40Codec();
    
  public Siren020Codec(String name) {
    super("Siren020");
  }

  @Override
  public StoredFieldsFormat storedFieldsFormat() {
    return lucene40.storedFieldsFormat();
  }
  
  @Override
  public TermVectorsFormat termVectorsFormat() {
    return lucene40.termVectorsFormat();
  }

  @Override
  public DocValuesFormat docValuesFormat() {
    return lucene40.docValuesFormat();
  }

  @Override
  public PostingsFormat postingsFormat() {
    return lucene40.postingsFormat();
  }
  
  @Override
  public FieldInfosFormat fieldInfosFormat() {
    return lucene40.fieldInfosFormat();
  }

  @Override
  public SegmentInfosFormat segmentInfosFormat() {
    return lucene40.segmentInfosFormat();
  }

}

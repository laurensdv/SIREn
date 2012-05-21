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
 * @author Renaud Delbru [ 23 Apr 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.index.codecs;

import org.apache.lucene.codecs.PostingsFormat;
import org.apache.lucene.codecs.lucene40.Lucene40Codec;
import org.apache.lucene.codecs.lucene40.Lucene40PostingsFormat;
import org.sindice.siren.index.codecs.siren10.Siren10PostingsFormat;
import org.sindice.siren.util.SirenTestCase;

public class MockSirenCodec extends Lucene40Codec {

  final PostingsFormat lucene40 = new Lucene40PostingsFormat();
  PostingsFormat siren10;

  public MockSirenCodec() {
    siren10 = new Siren10PostingsFormat();
  }

  public MockSirenCodec(final int blockSize) {
    siren10 = new Siren10PostingsFormat(blockSize);
  }

  @Override
  public PostingsFormat getPostingsFormatForField(final String field) {
    if (field.equals(SirenTestCase.DEFAULT_FIELD)) {
      return siren10;
    }
    else {
      return lucene40;
    }
  }
}

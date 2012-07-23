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

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

import org.apache.lucene.codecs.Codec;
import org.apache.lucene.codecs.PostingsFormat;
import org.apache.lucene.codecs.lucene40.Lucene40Codec;
import org.apache.lucene.codecs.lucene40.Lucene40PostingsFormat;
import org.sindice.siren.index.codecs.siren10.Siren10PostingsFormat;
import org.sindice.siren.util.SirenTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomSirenCodec extends Lucene40Codec {

  final Random random;
  private final HashSet<String> sirenFields = new HashSet<String>();
  final PostingsFormat lucene40 = new Lucene40PostingsFormat();
  PostingsFormat defaultTestFormat;

  private static final int[] BLOCK_SIZES = new int[] {1, 2, 16, 32, 64, 128, 256, 512, 1024};

  public enum PostingsFormatType {
    RANDOM, SIREN_10
  }

  protected static final Logger logger = LoggerFactory.getLogger(RandomSirenCodec.class);

  public RandomSirenCodec(final Random random) {
    this(random, PostingsFormatType.RANDOM);
  }

  public RandomSirenCodec(final Random random, final PostingsFormatType formatType) {
    this.addSirenFields(SirenTestCase.DEFAULT_TEST_FIELD);
    this.random = random;
    this.defaultTestFormat = this.getPostingsFormat(formatType);
    Codec.setDefault(this);
  }

  public RandomSirenCodec(final Random random, final PostingsFormat format) {
    this.addSirenFields(SirenTestCase.DEFAULT_TEST_FIELD);
    this.random = random;
    this.defaultTestFormat = format;
    Codec.setDefault(this);
  }

  public void addSirenFields(String... fields) {
    sirenFields.addAll(Arrays.asList(fields));
  }

  @Override
  public PostingsFormat getPostingsFormatForField(final String field) {
    if (sirenFields.contains(field)) {
      return defaultTestFormat;
    }
    else {
      return lucene40;
    }
  }

  @Override
  public String toString() {
    return "RandomSirenCodec[" + defaultTestFormat.toString() + "]";
  }

  private PostingsFormat getPostingsFormat(final PostingsFormatType formatType) {
    switch (formatType) {
      case RANDOM:
        return this.newRandomPostingsFormat();

      case SIREN_10:
        return this.newSiren10PostingsFormat();

      default:
        throw new InvalidParameterException();
    }
  }

  private PostingsFormat newSiren10PostingsFormat() {
    final int blockSize = this.newRandomBlockSize();
    return new Siren10PostingsFormat(blockSize);
  }

  private PostingsFormat newRandomPostingsFormat() {
    final int i = random.nextInt(1);
    switch (i) {

      case 0:
        return this.newSiren10PostingsFormat();

      default:
        throw new InvalidParameterException();
    }
  }

  private int newRandomBlockSize() {
    return BLOCK_SIZES[random.nextInt(BLOCK_SIZES.length)];
  }

}

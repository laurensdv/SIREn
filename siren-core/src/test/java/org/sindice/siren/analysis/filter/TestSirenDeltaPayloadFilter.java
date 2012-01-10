/**
 * Copyright (c) 2009-2011 National University of Ireland, Galway. All Rights Reserved.
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
 * @author Renaud Delbru [ 25 Dec 2011 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.analysis.filter;

import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.index.Payload;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.LuceneTestCase;
import org.junit.Assert;
import org.junit.Test;
import org.sindice.siren.analysis.AnyURIAnalyzer;
import org.sindice.siren.analysis.SirenTokenizerMockup;
import org.sindice.siren.analysis.TupleTokenizer;
import org.sindice.siren.index.codecs.siren020.VIntPayloadCodec;

public class TestSirenDeltaPayloadFilter extends LuceneTestCase {

  @Test
  public void testSimpleTuple() throws Exception {
    final TokenStream stream = this.getTupleTokenStream("<aaa aaa> <aaa> <aaa> <ccc> .");

    this.assertNodePathEquals("aaa", new int[]{0,0}, stream);
    this.assertNodePathEquals("aaa", new int[]{0,0}, stream);
    this.assertNodePathEquals("aaa", new int[]{0,1}, stream);
    this.assertNodePathEquals("aaa", new int[]{0,1}, stream);
    this.assertNodePathEquals("ccc", new int[]{0,3}, stream);

  }

  @Test
  public void testComplexTuple() throws Exception {
    final TokenStream stream = this.getTupleTokenStream("<aaa> <aaa> . <aaa aaa> . <bbb> . <aaa> <aaa> .");

    this.assertNodePathEquals("aaa", new int[]{0,0}, stream);
    this.assertNodePathEquals("aaa", new int[]{0,1}, stream);
    this.assertNodePathEquals("aaa", new int[]{1,0}, stream);
    this.assertNodePathEquals("aaa", new int[]{0,0}, stream);
    this.assertNodePathEquals("bbb", new int[]{2,0}, stream);
    this.assertNodePathEquals("aaa", new int[]{2,0}, stream);
    this.assertNodePathEquals("aaa", new int[]{0,1}, stream);
  }

  @Test
  public void testSameNode() throws Exception {
    final TokenStream stream = this.getSirenTokenStream(
      "aaa",
      new int[][] {
        new int[]{1,3},
        new int[]{1,3}
      }
    );

    this.assertNodePathEquals("aaa", new int[]{1,3}, stream);
    this.assertNodePathEquals("aaa", new int[]{0,0}, stream);
  }

  @Test
  public void testParentChild() throws Exception {
    final TokenStream stream = this.getSirenTokenStream(
      "aaa",
      new int[][] {
        new int[]{1},
        new int[]{1,3},
        new int[]{1,3,5}
      }
    );

    this.assertNodePathEquals("aaa", new int[]{1}, stream);
    this.assertNodePathEquals("aaa", new int[]{0,3}, stream);
    this.assertNodePathEquals("aaa", new int[]{0,0,5}, stream);
  }

  @Test
  public void testAncestorDescendant() throws Exception {
    final TokenStream stream = this.getSirenTokenStream(
      "aaa",
      new int[][] {
        new int[]{2},
        new int[]{2,2,1},
        new int[]{5,2,1},
        new int[]{5,2,1,3,4},
        new int[]{7,3,1}
      }
    );

    this.assertNodePathEquals("aaa", new int[]{2}, stream);
    this.assertNodePathEquals("aaa", new int[]{0,2,1}, stream);
    this.assertNodePathEquals("aaa", new int[]{3,2,1}, stream);
    this.assertNodePathEquals("aaa", new int[]{0,0,0,3,4}, stream);
    this.assertNodePathEquals("aaa", new int[]{2,3,1}, stream);
  }

  private void assertNodePathEquals(final String termExpected,
                                    final int[] pathExpected,
                                    final TokenStream stream)
  throws Exception {
    final CharTermAttribute termAtt = stream.getAttribute(CharTermAttribute.class);
    final PayloadAttribute payloadAtt = stream.getAttribute(PayloadAttribute.class);

    Assert.assertTrue(stream.incrementToken());
    Assert.assertEquals(termExpected, termAtt.toString());

    final VIntPayloadCodec codec = new VIntPayloadCodec();
    final Payload payload = payloadAtt.getPayload();
    final BytesRef bytes = new BytesRef(payload.getData(), payload.getOffset(), payload.length());
    final IntsRef ints = codec.decode(bytes);

    Assert.assertArrayEquals(pathExpected, Arrays.copyOfRange(ints.ints, ints.offset, ints.length));
  }

  private TokenStream getTupleTokenStream(final String input) {
    final Reader reader = new StringReader(input);
    final TupleTokenizer tokenizer = new TupleTokenizer(reader, Integer.MAX_VALUE);
    TokenStream stream = new DatatypeAnalyzerFilter(TEST_VERSION_CURRENT, tokenizer,
      new WhitespaceAnalyzer(TEST_VERSION_CURRENT),
      new AnyURIAnalyzer(TEST_VERSION_CURRENT));
    stream = new TokenTypeFilter(stream, new int[] {TupleTokenizer.DOT});
    stream = new SirenDeltaPayloadFilter(stream);
    return stream;
  }

  private TokenStream getSirenTokenStream(final String[] termInput, final int[][] pathInput) {
    final SirenTokenizerMockup tokenizer = new SirenTokenizerMockup();
    final TokenStream stream = new SirenDeltaPayloadFilter(tokenizer);
    for (int i = 0; i < termInput.length; i++) {
      tokenizer.add(termInput[i].toCharArray(), pathInput[i]);
    }
    return stream;
  }

  private TokenStream getSirenTokenStream(final String termInput, final int[][] pathInput) {
    final SirenTokenizerMockup tokenizer = new SirenTokenizerMockup();
    final TokenStream stream = new SirenDeltaPayloadFilter(tokenizer);
    for (final int[] element : pathInput) {
      tokenizer.add(termInput.toCharArray(), element);
    }
    return stream;
  }

}

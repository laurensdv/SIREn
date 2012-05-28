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
 * @author Renaud Delbru [ 27 Dec 2011 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.analysis;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.sindice.siren.analysis.attributes.DatatypeAttribute;
import org.sindice.siren.analysis.attributes.NodeAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockSirenTokenizer extends Tokenizer {

  MockSirenDocument doc;

  // the TupleTokenizer generates 6 attributes:
  // term, offset, positionIncrement, type, datatype, node
  private final CharTermAttribute termAtt;
  private final OffsetAttribute offsetAtt;
  private final PositionIncrementAttribute posIncrAtt;
  private final TypeAttribute typeAtt;
  private final DatatypeAttribute dtypeAtt;
  private final NodeAttribute nodeAtt;

  Iterator<ArrayList<MockSirenToken>> nodeIt = null;
  Iterator<MockSirenToken> tokenIt = null;

  protected static final Logger logger = LoggerFactory.getLogger(MockSirenTokenizer.class);

  public MockSirenTokenizer(final MockSirenReader reader) {
    super(reader);

    this.doc = reader.getDocument();
    nodeIt = doc.iterator();

    termAtt = this.addAttribute(CharTermAttribute.class);
    offsetAtt = this.addAttribute(OffsetAttribute.class);
    posIncrAtt = this.addAttribute(PositionIncrementAttribute.class);
    typeAtt = this.addAttribute(TypeAttribute.class);
    dtypeAtt = this.addAttribute(DatatypeAttribute.class);
    nodeAtt = this.addAttribute(NodeAttribute.class);
  }

  @Override
  public final boolean incrementToken() throws IOException {
    this.clearAttributes();

    final MockSirenToken token;
    while (nodeIt.hasNext() || (tokenIt != null && tokenIt.hasNext())) {
      if (tokenIt == null || !tokenIt.hasNext()) { // new node
        tokenIt = nodeIt.next().iterator(); // move to next node
      }

      token = tokenIt.next();
      termAtt.copyBuffer(token.term, 0, token.term.length);
      offsetAtt.setOffset(token.startOffset, token.endOffset);
      typeAtt.setType(TupleTokenizer.getTokenTypes()[token.tokenType]);
      posIncrAtt.setPositionIncrement(token.posInc);
      dtypeAtt.setDatatypeURI(token.datatype);
      for (int i = 0; i < token.nodePath.length; i++) {
        nodeAtt.append(token.nodePath.ints[i]);
      }
      return true;
    }

    return false;
  }

  @Override
  public void reset() {
    nodeIt = doc.iterator();
    this.clearAttributes();
  }

  @Override
  public void reset(final Reader input) throws IOException {
    assert input != null: "input must not be null";
    final MockSirenReader reader = (MockSirenReader) input;
    this.input = input;
    this.doc = reader.getDocument();
    nodeIt = doc.iterator();
  }

}

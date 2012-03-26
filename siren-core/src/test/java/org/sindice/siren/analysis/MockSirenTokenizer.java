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
import java.io.StringReader;
import java.util.Iterator;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.sindice.siren.analysis.attributes.DatatypeAttribute;
import org.sindice.siren.analysis.attributes.NodeAttribute;

public class MockSirenTokenizer extends Tokenizer {

  MockSirenDocument doc;

  public MockSirenTokenizer(final MockSirenDocument doc) {
    super(new StringReader(""));

    this.doc = doc;
    it = doc.iterator();

    termAtt = this.addAttribute(CharTermAttribute.class);
    offsetAtt = this.addAttribute(OffsetAttribute.class);
    posIncrAtt = this.addAttribute(PositionIncrementAttribute.class);
    typeAtt = this.addAttribute(TypeAttribute.class);
    dtypeAtt = this.addAttribute(DatatypeAttribute.class);
    nodeAtt = this.addAttribute(NodeAttribute.class);
  }

  // the TupleTokenizer generates 6 attributes:
  // term, offset, positionIncrement, type, datatype, node
  private final CharTermAttribute termAtt;
  private final OffsetAttribute offsetAtt;
  private final PositionIncrementAttribute posIncrAtt;
  private final TypeAttribute typeAtt;
  private final DatatypeAttribute dtypeAtt;
  private final NodeAttribute nodeAtt;

  Iterator<MockSirenToken> it = null;

  @Override
  public boolean incrementToken() throws IOException {
    this.clearAttributes();

    MockSirenToken tk;
    while (it.hasNext()) {
      tk = it.next();
      termAtt.copyBuffer(tk.term, 0, tk.term.length);
      posIncrAtt.setPositionIncrement(tk.posInc);
      offsetAtt.setOffset(tk.startOffset, tk.endOffset);
      typeAtt.setType(TupleTokenizer.getTokenTypes()[tk.tokenType]);
      dtypeAtt.setDatatypeURI(tk.datatype);
      for (int i = 0; i < tk.nodePath.length; i++) {
        nodeAtt.append(tk.nodePath.ints[i]);
      }
      return true;
    }

    return false;
  }

  @Override
  public void reset() {
    it = doc.iterator();
    this.clearAttributes();
  }

  public void reset(final MockSirenDocument doc) {
    this.doc = doc;
    this.reset();
  }

}

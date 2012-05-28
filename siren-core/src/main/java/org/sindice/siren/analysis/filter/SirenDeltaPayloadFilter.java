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
 * @project siren
 * @author Renaud Delbru [ 8 Dec 2009 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.analysis.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.index.Payload;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IntsRef;
import org.sindice.siren.analysis.attributes.NodeAttribute;
import org.sindice.siren.analysis.attributes.PositionAttribute;

/**
 * Filter that encode the structural information of each token into its payload.
 */
public class SirenDeltaPayloadFilter extends TokenFilter {

  private final NodeAttribute nodeAtt;
  private final PositionAttribute posAtt;
  private final PayloadAttribute payloadAtt;
  private final CharTermAttribute termAtt;

  Map<Integer, IntsRef> previousPaths = new HashMap<Integer, IntsRef>();

  VIntPayloadCodec codec = new VIntPayloadCodec();
  Payload payload = new Payload();

  public SirenDeltaPayloadFilter(final TokenStream input) {
    super(input);
    termAtt = this.addAttribute(CharTermAttribute.class);
    payloadAtt = this.addAttribute(PayloadAttribute.class);
    nodeAtt = this.addAttribute(NodeAttribute.class);
    posAtt = this.addAttribute(PositionAttribute.class);
  }

  @Override
  public void close() throws IOException {
    super.close();
    previousPaths.clear();
  }

  @Override
  public void reset() throws IOException {
    super.reset();
    previousPaths.clear();
  }

  @Override
  public final boolean incrementToken() throws IOException {
    BytesRef bytes;
    IntsRef previous;
    IntsRef current;

    if (input.incrementToken()) {

      final int hash = termAtt.hashCode();

      // Copy the current node path
      // complexity: one execution per word
      if (!previousPaths.containsKey(hash)) {
        // object creation and copy
        previousPaths.put(hash, IntsRef.deepCopyOf(nodeAtt.node()));
        // encode node path and position
        bytes = codec.encode(nodeAtt.node(), posAtt.position());
        payload.setData(bytes.bytes, bytes.offset, bytes.length);
        payloadAtt.setPayload(payload);
      }
      // Perform the difference between the previous and current node paths
      // complexity: one execution per word occurrence
      else {
        current = nodeAtt.node();
        // retrieve previous node path
        previous = previousPaths.get(hash);
        // ensure previous is large enough to store new path
        previous.grow(current.length);

        // substraction
        // update previous path during substraction to avoid an additional
        // creation and copy of array
        int i, j, tmp;
        for (i = current.offset, j = previous.offset;
             (i < current.offset + current.length) &&
             (j < previous.offset + previous.length);
             i++, j++) {
          tmp = current.ints[i]; // store node id before substraction
          if (current.ints[i] != previous.ints[j]) {
            current.ints[i] = current.ints[i] - previous.ints[j];
            previous.ints[j] = tmp; // update previous path
            i++; j++; // increment before break
            break;
          }
          current.ints[i] = 0;
          previous.ints[j] = tmp; // update previous path
        }

        // finalise update of previous path
        previous.offset = current.offset;
        previous.length = current.length;

        for (;
            (i < current.offset + current.length) &&
            (j < previous.offset + previous.length);
             i++, j++) {
          previous.ints[j] = current.ints[i];
        }

        // encode node path and position
        bytes = codec.encode(current, posAtt.position());
        payload.setData(bytes.bytes, bytes.offset, bytes.length);
        payloadAtt.setPayload(payload);
      }
      return true;
    }
    return false;
  }

}

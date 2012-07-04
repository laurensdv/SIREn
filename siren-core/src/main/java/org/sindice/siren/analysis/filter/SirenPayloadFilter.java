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
 * @author Renaud Delbru [ 30 Apr 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.analysis.filter;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.util.BytesRef;
import org.sindice.siren.analysis.attributes.NodeAttribute;
import org.sindice.siren.analysis.attributes.PositionAttribute;

public class SirenPayloadFilter extends TokenFilter  {

  private final NodeAttribute nodeAtt;
  private final PositionAttribute posAtt;
  private final PayloadAttribute payloadAtt;

  VIntPayloadCodec codec = new VIntPayloadCodec();

  public SirenPayloadFilter(final TokenStream input) {
    super(input);
    payloadAtt = this.addAttribute(PayloadAttribute.class);
    nodeAtt = this.addAttribute(NodeAttribute.class);
    posAtt = this.addAttribute(PositionAttribute.class);
  }

  @Override
  public final boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      // encode node path
      final BytesRef bytes = codec.encode(nodeAtt.node(), posAtt.position());
      payloadAtt.setPayload(bytes);
      return true;
    }
    else {
      return false;
    }
  }

}

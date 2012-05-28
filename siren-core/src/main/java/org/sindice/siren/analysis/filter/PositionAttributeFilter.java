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
 * @author Renaud Delbru [ 27 May 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.analysis.filter;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.sindice.siren.analysis.attributes.NodeAttribute;
import org.sindice.siren.analysis.attributes.PositionAttribute;

/**
 * Filter that encode the position relative to the node of each token into
 * the {@link PositionAttribute}.
 */
public class PositionAttributeFilter extends TokenFilter {

  private final NodeAttribute nodeAtt;
  private final PositionAttribute posAtt;
  private final PositionIncrementAttribute posIncrAtt;

  private long lastNodeHash = Long.MAX_VALUE;
  private int lastPosition = 0;

  public PositionAttributeFilter(final TokenStream input) {
    super(input);
    nodeAtt = this.addAttribute(NodeAttribute.class);
    posAtt = this.addAttribute(PositionAttribute.class);
    posIncrAtt = this.addAttribute(PositionIncrementAttribute.class);
  }

  @Override
  public void reset() throws IOException {
    super.reset();
    lastPosition = 0;
    lastNodeHash = Long.MAX_VALUE;

  }

  @Override
  public final boolean incrementToken() throws IOException {
    if (!input.incrementToken()) {
      return false;
    }

    final int nodeHash = nodeAtt.node().hashCode();
    if (lastNodeHash != nodeHash) { // new node
      lastPosition = 0;
      lastNodeHash = nodeHash;
    }

    lastPosition += posIncrAtt.getPositionIncrement();
    if (lastPosition > 0) {
      posAtt.setPosition(lastPosition - 1);
    }
    else {
      posAtt.setPosition(lastPosition);
    }
    return true;
  }

}

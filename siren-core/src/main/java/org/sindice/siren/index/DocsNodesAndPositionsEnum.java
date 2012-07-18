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
 * @author Campinas Stephane [ 28 Nov 2011 ]
 * @link stephane.campinas@deri.org
 */
package org.sindice.siren.index;

import java.io.IOException;

import org.apache.lucene.util.AttributeSource;

/**
 * Iterates through documents, nodes and positions.
 * <br>
 * A node is defined by a list of node identifiers which represents the path
 * in the tree to reach the node.
 */
public abstract class DocsNodesAndPositionsEnum implements DocsAndNodesIterator, PositionsIterator {

  private AttributeSource atts = null;

  /** Returns the related attributes. */
  public AttributeSource attributes() {
    if (atts == null) atts = new AttributeSource();
    return atts;
  }

  /**
   * Returns term frequency in the current node.  Do
   * not call this before {@link #nextNode} is first called,
   * nor after {@link #nextNode} returns false or
   * {@link #node()} returns NO_MORE_NOD.
   * @throws IOException
   **/
  public abstract int termFreqInNode() throws IOException;

  /**
   * Returns node frequency in the current document.  Do
   * not call this before {@link #nextDoc} is first called,
   * nor after {@link #nextDoc} returns false or
   * {@link #doc()} returns NO_MORE_DOC.
   **/
  public abstract int nodeFreqInDoc() throws IOException;

}

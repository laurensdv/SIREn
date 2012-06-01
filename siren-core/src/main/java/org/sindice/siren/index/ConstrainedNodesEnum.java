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
 * @author Renaud Delbru [ 24 Jan 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.index;

import java.io.IOException;

import org.apache.lucene.util.IntsRef;
import org.sindice.siren.util.NodeUtils;

/**
 * This {@link DocsNodesAndPositionsEnum} wraps another
 * {@link DocsNodesAndPositionsEnum} and applies the node constraints. It
 * filters all the nodes that do not satisfy the constraints.
 *
 * @see NodeUtils#isConstraintSatisfied(int[], int[], int[], boolean)
 */
public abstract class ConstrainedNodesEnum extends DocsNodesAndPositionsEnum {

  protected final DocsNodesAndPositionsEnum docsEnum;

  public ConstrainedNodesEnum(final DocsNodesAndPositionsEnum docsEnum) {
    this.docsEnum = docsEnum;
  }

  @Override
  public boolean nextDocument() throws IOException {
    return docsEnum.nextDocument();
  }

  @Override
  public abstract boolean nextNode() throws IOException;

  @Override
  public boolean skipTo(final int target) throws IOException {
    return docsEnum.skipTo(target);
  }

  @Override
  public int doc() {
    return docsEnum.doc();
  }

  @Override
  public IntsRef node() {
    return docsEnum.node();
  }

  @Override
  public boolean nextPosition() throws IOException {
    return docsEnum.nextPosition();
  }

  @Override
  public int pos() {
    return docsEnum.pos();
  }

  @Override
  public int termFreqInDoc() throws IOException {
    return docsEnum.termFreqInDoc();
  }

  @Override
  public int termFreqInNode() throws IOException {
    return docsEnum.termFreqInNode();
  }

  @Override
  public int nodeFreqInDoc() throws IOException {
    return docsEnum.nodeFreqInDoc();
  }

}

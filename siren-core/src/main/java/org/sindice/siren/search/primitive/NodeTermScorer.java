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
 * @author Renaud Delbru [ 9 Dec 2009 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.search.primitive;

import java.io.IOException;

import org.apache.lucene.search.Weight;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.IntsRef;
import org.sindice.siren.index.DocsNodesAndPositionsEnum;
import org.sindice.siren.search.base.NodePositionScorer;
import org.sindice.siren.search.base.NodeScorer;

/**
 * Expert: A {@link NodeScorer} for documents matching a
 * <code>Term</code>.
 */
public class NodeTermScorer extends NodePositionScorer {

  private final DocsNodesAndPositionsEnum docsEnum;

  private final Similarity.ExactSimScorer docScorer;

  /**
   * Construct a <code>NodeTermScorer</code>.
   *
   * @param weight
   *          The weight of the <code>Term</code> in the query.
   * @param docsEnum
   *          An iterator over the documents and the positions matching the
   *          <code>Term</code>.
   * @param similarity
   *          The </code>Similarity</code> implementation to be used for score
   *          computations.
   * @param norms
   *          The field norms of the document fields for the <code>Term</code>.
   * @throws IOException
   */
  protected NodeTermScorer(final Weight weight,
                           final DocsNodesAndPositionsEnum docsEnum,
                           final Similarity.ExactSimScorer docScorer)
  throws IOException {
    super(weight);
    this.docScorer = docScorer;
    this.docsEnum = docsEnum;
  }

  @Override
  public int doc() {
    return docsEnum.doc();
  }

  @Override
  public float freq() throws IOException {
    return docsEnum.termFreqInDoc();
  }

  @Override
  public int pos() {
    return docsEnum.pos();
  }

  @Override
  public IntsRef node() {
    return docsEnum.node();
  }

  @Override
  public boolean nextCandidateDocument() throws IOException {
    return docsEnum.nextDocument();
  }

  @Override
  public boolean nextNode() throws IOException {
    return docsEnum.nextNode();
  }

  @Override
  public boolean nextPosition() throws IOException {
    return docsEnum.nextPosition();
  }

  @Override
  public float score() throws IOException {
    assert this.doc() != NO_MORE_DOCS;
    return docScorer.score(docsEnum.doc(), docsEnum.termFreqInDoc());
  }

  @Override
  public boolean skipToCandidate(final int target) throws IOException {
    return docsEnum.skipTo(target);
  }

  @Override
  public String toString() {
    return "NodeTermScorer(" + weight + "," + this.doc() + "," + this.node() + ")";
  }

}

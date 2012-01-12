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
package org.sindice.siren.search;

import java.io.IOException;

import org.apache.lucene.search.Weight;
import org.apache.lucene.search.similarities.Similarity;
import org.sindice.siren.index.NodAndPosEnum;

/**
 * Expert: A {@link SirenPrimitiveScorer} for documents matching a
 * <code>Term</code>.
 */
class SirenTermScorer
extends SirenPrimitiveScorer {

  private final NodAndPosEnum docsEnum;

  private final Similarity.ExactDocScorer docScorer;

  /**
   * Construct a <code>SirenTermScorer</code>.
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
  protected SirenTermScorer(final Weight weight, final NodAndPosEnum td, final Similarity.ExactDocScorer docScorer)
  throws IOException {
    super(weight);
    this.docScorer = docScorer;
    this.docsEnum = td;
  }

  @Override
  public int docID() {
    return docsEnum.docID();
  }

  @Override
  public float freq() {
    return docsEnum.freq();
  }

  @Override
  public int pos() {
    return docsEnum.pos();
  }

  @Override
  public int[] node() {
    return docsEnum.node();
  }

  /**
   * Advances to the next document matching the query. <br>
   *
   * @return the document matching the query or NO_MORE_DOCS if there are no
   * more documents.
   */
  @Override
  public int nextDoc() throws IOException {
    return docsEnum.nextDoc();
  }

  /**
   * Advances to the next node path matching the query. <br>
   *
   * @return false iff there are no more node paths.
   */
  @Override
  public boolean nextNode() throws IOException {
    return docsEnum.nextNode();
  }

  /**
   * Advances to the next node path and position matching the query. <br>
   *
   * @return false iff there is no more node path and position for the current
   * entity.
   */
  @Override
  public int nextPosition() throws IOException {
    return docsEnum.nextPosition();
  }

  @Override
  public float score() throws IOException {
    assert this.docID() != NO_MORE_DOCS;
    return docScorer.score(docsEnum.docID(), docsEnum.freq());
  }

  @Override
  public int advance(final int entityID)
  throws IOException {
    return docsEnum.advance(entityID);
  }

  @Override
  public int advance(final int docID, final int[] nodes)
  throws IOException {
    return docsEnum.advance(docID, nodes);
  }

  @Override
  public String toString() {
    return "SirenTermScorer(" + weight + ")";
  }

}

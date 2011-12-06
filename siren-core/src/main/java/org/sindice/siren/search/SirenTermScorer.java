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

import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.sindice.siren.index.NodAndPosEnum;
import org.sindice.siren.index.NodesConfig;
import org.sindice.siren.index.codecs.siren020.Siren020NodAndPosEnum;

class SirenTermScorer
extends SirenPrimitiveScorer {

  private final NodAndPosEnum napEnum;

  private final TFIDFSimilarity sim;

  private final byte[]        norms;

  private final float         weightValue;

  private static final int    SCORE_CACHE_SIZE = 32;

  private final float[]       scoreCache       = new float[SCORE_CACHE_SIZE];

  /** Current structural and positional information */
//  private int                 dataset = -1;
//  private int                 docID = -1;
//  private int                 tuple = -1;
//  private int                 cell = -1;
//  private int                 pos = -1;
//  private final int[] node;
  
  /**
   * Construct a <code>SirenTermScorer</code>.
   *
   * @param weight
   *          The weight of the <code>Term</code> in the query.
   * @param napEnum
   *          An iterator over the documents and the positions matching the
   *          <code>Term</code>.
   * @param similarity
   *          The </code>Similarity</code> implementation to be used for score
   *          computations.
   * @param norms
   *          The field norms of the document fields for the <code>Term</code>.
   * @throws IOException 
   */
  protected SirenTermScorer(final Weight weight, final DocsAndPositionsEnum napEnum,
                            final Similarity similarity, final byte[] norms)
  throws IOException {
    super(weight);
    if (similarity instanceof TFIDFSimilarity)
      sim = (TFIDFSimilarity) similarity;
    else
      throw new RuntimeException("This scorer uses a TF-IDF scoring function");
    
    // TODO: don't instantiate the enum here! this should be done by the specific codec.
    this.napEnum = new Siren020NodAndPosEnum(new NodesConfig(2), napEnum);
    this.norms = norms;
    // TODO: check if this API change provides the same value as before
//    this.weightValue = weight.getValue();
    this.weightValue = weight.getValueForNormalization();

    for (int i = 0; i < SCORE_CACHE_SIZE; i++)
      scoreCache[i] = sim.tf(i) * weightValue;
  }

  @Override
  public void score(final Collector c) throws IOException {
    this.score(c, Integer.MAX_VALUE, this.nextDoc());
  }

  // firstDocID is ignored since nextDoc() sets 'doc'
  @Override
  public boolean score(final Collector c, final int end, final int firstDocID) throws IOException {
    c.setScorer(this);
    while (napEnum.docID() < end) {                           // for docs in window
      c.collect(napEnum.docID());                             // collect score
      if (this.nextDoc() == NO_MORE_DOCS) {
        return false;
      }
    }
    return true;
  }

  @Override
  public float score() throws IOException {
    final int f = napEnum.freq();
    final float raw =                                   // compute tf(f)*weight
      f < SCORE_CACHE_SIZE                              // check cache
      ? scoreCache[f]                                   // cache hit
      : sim.tf(f) * weightValue;                        // cache miss

    return norms == null ? raw : raw * sim.decodeNormValue(norms[this.docID()]); // normalize for field
  }

  /** Move to the next entity matching the query.
   * @return next entity id matching the query.
   */
  @Override
  public int nextDoc() throws IOException {
    if (napEnum.nextDoc() == NO_MORE_DOCS) {
      return NO_MORE_DOCS;
    }
//    docID = napEnum.docID();
    this.nextPosition(); // advance to the first cell [SRN-24]
    return napEnum.docID();
  }

  /**
   * Move to the next tuple, cell and position in the current entity.
   *
   * <p> This is invalid until {@link #next()} is called for the first time.
   *
   * @return false iff there is no more tuple, cell and position for the current
   * entity.
   * @throws IOException
   */
  @Override
  public int nextPosition() throws IOException {
    if (napEnum.nextPosition() == NO_MORE_POS) {
      // TODO: why not dataset ?
      napEnum.setLayersToSentinel();
      return NO_MORE_POS;
    }

//    tuple = napEnum.tuple();
//    cell = napEnum.cell();
//    pos = napEnum.pos();
    return napEnum.pos();
  }

  @Override
  public int advance(final int entityID)
  throws IOException {
    if (napEnum.advance(entityID) == NO_MORE_DOCS) {
      return NO_MORE_DOCS;
    }
//    docID = napEnum.entity();
    this.nextPosition(); // advance to the first cell [SRN-24]
    return napEnum.docID();
  }

  @Override
  public int advance(int docID, int[] nodes)
  throws IOException {
    if (napEnum.advance(docID, nodes) == NO_MORE_DOCS) {
      return NO_MORE_DOCS;
    }
    return docID();
  }
  
  /** Returns the current document number matching the query.
   * <p> Initially invalid, until {@link #next()} is called the first time.
   */
  @Override
  public int docID() { return napEnum.docID(); }

  /** Returns the current position identifier matching the query.
   * <p> Initially invalid, until {@link #nextPosition()} is
   * called the first time.
   */
  public int pos() { return napEnum.pos(); }

  @Override
  public String toString() {
    return "TermScorer(" + docID() + napEnum.toString() + ")";
  }

  @Override
  public int[] node() {
    return napEnum.node();
  }

}

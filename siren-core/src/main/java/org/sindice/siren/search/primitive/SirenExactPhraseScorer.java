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
 * @author Renaud Delbru [ 10 Dec 2009 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.search.primitive;

import java.io.IOException;

import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.similarities.Similarity;

class SirenExactPhraseScorer
extends SirenPhraseScorer {

  // TODO: what was this used for ?
//  protected final List<Integer> tuples = new ArrayList<Integer>();
//  protected final List<Integer> cells = new ArrayList<Integer>();

  SirenExactPhraseScorer(final Weight weight, final DocsAndPositionsEnum[] docsEnums, final int[] offsets,
                         final Similarity similarity, final byte[] norms) throws IOException {
    super(weight, docsEnums, offsets, similarity, norms);
  }

  private boolean isBefore(int[] nodes) {
    boolean res;
    
    for (int i = 0; i < nodes.length; i++) {
      int index = i;
      res = node()[index] < nodes[index];
      
      while (--index >= 0) {
        res = node()[index] == nodes[index] && res;
      }
      if (res) return true;
    }
    res = node()[0] == nodes[0];
    for (int i = 1; i < nodes.length; i++) {
      res = node()[i] == nodes[i] && res;
    }
    return res && first.pos() < last.pos();
  }
  
  @Override
  public int doNextPosition() throws IOException {
    while (isBefore(last.node())) {
      do {
        if (first.nextPosition() == NO_MORE_POS)
          return NO_MORE_POS;
      } while (isBefore(last.node()));
      this.firstToLast();
    }
    // all equal: a match
    pos = first.pos();
    occurrences++; // increase occurrences
    return pos;
  }

  @Override
  public String toString() {
    return "PhraseScorer(" + docID + "," + first + ")";
  }

  @Override
  public int pos() {
    return pos;
  }
  
  @Override
  public int[] node() {
    return first.node();
  }

}

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
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.sindice.siren.search.base.NodePositionScorer;

/**
 * <p> An entity is considered matching if it contains the phrase-query terms at
 * "valid" positions. What "valid positions" are depends on the type of the
 * phrase query: for an exact phrase query terms are required to appear in
 * adjacent locations, while for a sloppy phrase query some distance between the
 * terms is allowed. The method {@link #phraseFreq()}, when invoked, compute all the
 * occurrences of the phrase within the entity. A non
 * zero frequency means a match.
 * <p> Code taken from {@link PhraseScorer} and adapted for the Siren use case.
 */
abstract class SirenPhraseScorer extends NodePositionScorer {

  private final TFIDFSimilarity   sim;

  protected byte[]           norms;

  protected float            value;

  private boolean            firstTime = true;

  private boolean            more      = true;

  protected SirenPhraseQueue pq;

  protected SirenPhrasePositions first, last;

  /**
   * phrase occurrences in current entity.
   */
  protected int              occurrences = 0;

  /** Current structural and positional information */
//  protected final int           dataset = -1;
//  protected int                 tuple = -1;
//  protected int                 cell = -1;
  protected int                 pos = -1;
  protected int                 docID = -1;
//  protected int[]               curNode;

  SirenPhraseScorer(final Weight weight, final DocsAndPositionsEnum[] tps,
                    final int[] offsets, final Similarity similarity,
                    final byte[] norms) throws IOException {
    super(weight);
    if (similarity instanceof TFIDFSimilarity)
      sim = (TFIDFSimilarity) similarity;
    else
      throw new RuntimeException("This scorer uses a TF-IDF scoring function");
    this.norms = norms;
    // TODO: Check if this is the same thing
//    this.value = weight.getValue();
    this.value = weight.getValueForNormalization();

    // convert tps to a list of phrase positions.
    // note: phrase-position differs from term-position in that its position
    // reflects the phrase offset: pp.pos = tp.pos - offset.
    // this allows to easily identify a matching (exact) phrase
    // when all SirenPhrasePositions have exactly the same position, tuple and
    // cell.
    for (int i = 0; i < tps.length; i++) {
      final SirenPhrasePositions pp = new SirenPhrasePositions(tps[i], offsets[i]);
      if (last != null) { // add next to end of list
        last.next = pp;
      }
      else
        first = pp;
      last = pp;
    }
    pq = new SirenPhraseQueue(tps.length); // construct empty pq
  }

  @Override
  public int doc() {
    return docID;
  }

  public int phraseFreq() throws IOException {
    // compute all remaining occurrences
    while (this.nextPosition() != NO_MORE_POS) {
      ;
    }
    return occurrences;
  }

  /**
   * Move to the next entity identifier. Return true if there is such an entity
   * identifier.
   */
  @Override
  public int nextDocument()
  throws IOException {
    if (firstTime) {
      this.init();
      firstTime = false;
    }
    else if (more) {
      more = (last.nextDocument() != NO_MORE_DOCS); // trigger further scanning
    }
    return this.doNext();
  }

  /**
   * Perform a next without initial increment
   */
  private int doNext()
  throws IOException {
    while (more) {
      while (more && first.doc() < last.doc()) { // find entity w/ all the terms
        more = (first.skipTo(last.doc()) != NO_MORE_DOCS); // skip first upto last
        this.firstToLast(); // and move it to the end
      }

      if (more) { // found an entity with all of the terms
        this.initQueue();
        if (this.doNextPosition() != NO_MORE_POS) { // check for phrase
          docID = first.doc();
          return docID;
        }
        else {
          more = (last.nextDocument() != NO_MORE_DOCS); // trigger further scanning
        }
      }
    }
    return NO_MORE_DOCS; // no more matches
  }

  @Override
  public int nextPosition() throws IOException {
    if (last.nextPosition() == NO_MORE_POS) { // scan forward in last
      return NO_MORE_POS;
    }

    return this.doNextPosition();
  }

  public abstract int doNextPosition() throws IOException;

  @Override
  public float score()
  throws IOException {
    if (firstTime)
      throw new InvalidCallException("next or skipTo should be called first");

    final float raw = sim.tf(this.phraseFreq()) * value; // raw score
    return norms == null ? raw : raw * sim.decodeNormValue(norms[first.doc()]); // normalize
  }

  @Override
  public int skipTo(final int entityID) throws IOException {
    if (docID == entityID) { // optimised case: do nothing
      return docID;
    }

    firstTime = false;
    for (SirenPhrasePositions pp = first; more && pp != null; pp = pp.next) {
      more = (pp.skipTo(entityID) != NO_MORE_DOCS);
    }
    if (more) {
      this.sort(); // re-sort
    }
    return this.doNext();
  }

  /**
   * Return true if the current node is after of equals to the one passed in argument
   * @param nodes
   * @return
   */
  private boolean isAfterOrEquals(final int[] nodes) {
    for (int i = 0; i < nodes.length; i++) {
      int index = i;
      boolean res;
      if (index == 0) {
        res = this.node()[index] > nodes[index];
      } else {
        res = this.node()[index] >= nodes[index];
      }
      while (--index >= 0) {
        res = this.node()[index] == nodes[index] && res;
      }
      if (res) return true;
    }
    return false;
  }

  /**
   * Return true if the current node is before the one passed in argument
   * @param nodes
   * @return
   */
  private boolean isBefore(final int[] nodes) {
    for (int i = 0; i < nodes.length; i++) {
      int index = i;
      boolean res = this.node()[index] < nodes[index];

      while (--index >= 0) {
        res = this.node()[index] == nodes[index] && res;
      }
      if (res) return true;
    }
    return false;
  }

  @Override
  public int skipTo(final int docID, final int[] nodes)
  throws IOException {
    if (this.docID == docID) { // optimised case: find tuple in occurrences
      while (this.isBefore(nodes) && this.nextPosition() != NO_MORE_POS) {
        ;
      }
      // If tuple found, return true, if not, skip to next entity
      // If tuple and cell found, return true, if not, skip to next entity
      return (this.isAfterOrEquals(nodes)) ? docID : this.nextDocument();
    }

    firstTime = false;
    for (SirenPhrasePositions pp = first; more && pp != null; pp = pp.next) {
      more = (pp.skipTo(docID, nodes) != NO_MORE_DOCS);
    }
    if (more) {
      this.sort(); // re-sort
    }
    return this.doNext(); // find next matching entity
  }

  private void init()
  throws IOException {
    for (SirenPhrasePositions pp = first; more && pp != null; pp = pp.next) {
      more = (pp.nextDocument() != NO_MORE_DOCS);
    }
    if (more) {
      this.sort();
    }
  }

  private void initQueue() throws IOException {
    // sort list with pq
    pq.clear();
    for (SirenPhrasePositions pp = first; pp != null; pp = pp.next) {
      pp.firstPosition();
      pq.add(pp); // build pq from list
    }
    this.pqToList(); // rebuild list from pq
  }

  private void sort() {
    pq.clear();
    for (SirenPhrasePositions pp = first; pp != null; pp = pp.next) {
      pq.add(pp);
    }
    this.pqToList();
  }

  protected final void pqToList() {
    last = first = null;
    while (pq.top() != null) {
      final SirenPhrasePositions pp = pq.pop();
      if (last != null) { // add next to end of list
        last.next = pp;
      }
      else {
        first = pp;
      }
      last = pp;
      pp.next = null;
    }
  }

  protected final void firstToLast() {
    last.next = first; // move first to end of list
    last = first;
    first = first.next;
    last.next = null;
  }

  @Override
  public abstract String toString();

}

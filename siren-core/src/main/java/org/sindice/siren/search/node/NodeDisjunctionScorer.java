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
package org.sindice.siren.search.node;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Weight;
import org.apache.lucene.util.IntsRef;

/**
 * A Scorer for OR like queries within a node, counterpart of
 * {@link NodeConjunctionScorer}.
 * <p>
 * Code taken from {@link DisjunctionSumScorer} and adapted for the Siren
 * use case.
 */
public class NodeDisjunctionScorer extends NodeScorer {

  /** The number of subscorers. */
  private final int                          nrScorers;

  /** The scorers. */
  protected final Collection<NodeScorer> scorers;

  /**
   * The scorerNodeQueue contains all subscorers ordered by their current docID(),
   * with the minimum at the top. <br>
   * The scorerNodeQueue is initialized the first time nextDoc() or advance() is
   * called. <br>
   * An exhausted scorer is immediately removed from the scorerDocQueue. <br>
   * If less than the minimumNrMatchers scorers remain in the scorerDocQueue
   * nextDoc() and advance() return false.
   * <p>
   * After each to call to nextDoc() or advance() <code>currentScore</code> is
   * the total score of the current matching doc, <code>nrMatchers</code> is the
   * number of matching scorers, and all scorers are after the matching doc, or
   * are exhausted.
   */
  private NodeDisjunctionScorerQueue nodeScorerQueue = null;

  /** The document number of the current match. */
  private int currentDoc = -1;

  private IntsRef currentNode = new IntsRef(new int[] { -1 }, 0, 1);

  /** The number of subscorers that provide the current match. */
  protected int nrMatchers     = -1;

  private final float currentScore   = Float.NaN;

  /**
   * Construct a {@link NodeDisjunctionScorer}.
   *
   * @param subScorers
   *          A collection of at least two primitives scorers.
   * @throws IOException
   */
  public NodeDisjunctionScorer(final Weight weight,
                                final List<NodeScorer> scorers)
  throws IOException {
    super(weight);
    nrScorers = scorers.size();
    if (nrScorers <= 1) {
      throw new IllegalArgumentException("There must be at least 2 subScorers");
    }
    this.scorers = scorers;
    nodeScorerQueue  = this.initNodeScorerQueue();
  }

  /**
   * Initialize the {@link NodeDisjunctionScorerQueue}.
   */
  private NodeDisjunctionScorerQueue initNodeScorerQueue() throws IOException {
    final NodeDisjunctionScorerQueue nodeQueue = new NodeDisjunctionScorerQueue(nrScorers);
    for (final NodeScorer s : scorers) {
      nodeQueue.put(s);
    }
    return nodeQueue;
  }

  /**
   * Scores and collects all matching documents.
   *
   * @param collector
   *          The collector to which all matching documents are passed through.
   */
  @Override
  public void score(final Collector collector) throws IOException {
    throw new UnsupportedOperationException();

// TODO
//    collector.setScorer(this);
//    while (this.nextDocument()) {
//      collector.collect(currentDoc);
//    }
  }

  /**
   * Expert: Collects matching documents in a range. Hook for optimization. Note
   * that {@link #nextDocument()} must be called once before this method is
   * called for the first time.
   *
   * @param collector
   *          The collector to which all matching documents are passed through.
   * @param max
   *          Do not score documents past this.
   * @return true if more matching documents may remain.
   */
  @Override
  public boolean score(final Collector collector, final int max, final int firstDocID)
  throws IOException {
    throw new UnsupportedOperationException();

// TODO
//    // firstDocID is ignored since nextDocument() sets 'currentDoc'
//    collector.setScorer(this);
//    while (currentDoc < max) {
//      collector.collect(currentDoc);
//      if (!this.nextDocument()) {
//        return false;
//      }
//    }
//    return true;
  }

  @Override
  public boolean nextCandidateDocument() throws IOException {
    boolean more = true;

    if (currentDoc != -1) { // if not called for the first time
      more = nodeScorerQueue.nextCandidateDocumentAndAdjustElsePop();
    }

    currentDoc = nodeScorerQueue.doc();
    currentNode = nodeScorerQueue.node();
    return more;
  }

  @Override
  public boolean nextNode() throws IOException {
    final boolean more = nodeScorerQueue.nextNodeAndAdjust();
    currentNode = nodeScorerQueue.node();
    return more;
  }

  /**
   * Returns the score of the current document matching the query. Initially
   * invalid, until {@link #nextDocument()} is called the first time.
   */
  @Override
  public float score() {
    // TODO
    throw new UnsupportedOperationException();
  }

  /**
   * Returns the number of subscorers matching the current document. Initially
   * invalid, until {@link #nextDocument()} is called the first time.
   */
  public int nrMatchers() {
    // TODO
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean skipToCandidate(final int target) throws IOException {
    final boolean more = nodeScorerQueue.skipToCandidateAndAdjustElsePop(target);
    currentDoc = nodeScorerQueue.doc();
    currentNode = nodeScorerQueue.node();
    return more;
  }

  @Override
  public int doc() {
    return currentDoc;
  }

  @Override
  public IntsRef node() {
    return currentNode;
  }

  @Override
  public String toString() {
    return "NodeDisjunctionScorer(" + weight + "," + this.doc() + "," +
      this.node() + ")";
  }

}

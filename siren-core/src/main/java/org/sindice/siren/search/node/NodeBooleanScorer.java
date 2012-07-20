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
 * @author Renaud Delbru [ 6 May 2009 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.search.node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.IntsRef;
import org.sindice.siren.search.node.NodeBooleanClause.Occur;
import org.sindice.siren.search.node.NodeBooleanQuery.AbstractNodeBooleanWeight;

/**
 * A scorer that matches a boolean combination of node scorers.
 * <p>
 * Uses {@link NodeConjunctionScorer}, {@link NodeDisjunctionScorer},
 * {@link NodeReqExclScorer} and {@link NodeReqOptScorer}.
 * <p>
 * Code taken from {@link BooleanScorer2} and adapted for the Siren use case.
 */
public class NodeBooleanScorer extends NodeScorer {

  protected final List<NodeScorer> requiredScorers;
  protected final List<NodeScorer> optionalScorers;
  protected final List<NodeScorer> prohibitedScorers;

  private final Coordinator coordinator;

  /**
   * The scorer to which all scoring will be delegated, except for computing and
   * using the coordination factor.
   */
  protected NodeScorer countingSumScorer = null;

  /**
   * Creates a {@link NodeBooleanScorer} with the given lists of
   * required, prohibited and optional scorers. In no required scorers are added,
   * at least one of the optional scorers will have to match during the search.
   *
   * @param weight
   *          The BooleanWeight to be used.
   * @param disableCoord
   *          If this parameter is true, coordination level matching
   *          ({@link Similarity#coord(int, int)}) is not used.
   * @param required
   *          the list of required scorers.
   * @param prohibited
   *          the list of prohibited scorers.
   * @param optional
   *          the list of optional scorers.
   */
  public NodeBooleanScorer(final AbstractNodeBooleanWeight weight,
                           final boolean disableCoord,
                           final List<NodeScorer> required,
                           final List<NodeScorer> prohibited,
                           final List<NodeScorer> optional,
                           final int maxCoord) throws IOException {
    super(weight);
    coordinator = new Coordinator();
    coordinator.maxCoord = maxCoord;

    optionalScorers = optional;
    requiredScorers = required;
    prohibitedScorers = prohibited;

    coordinator.init(disableCoord);
    countingSumScorer = this.makeCountingSumScorer(disableCoord);
  }

  private NodeScorer countingDisjunctionSumScorer(final List<NodeScorer> scorers)
  throws IOException {
    return new NodeDisjunctionScorer(this.getWeight(), scorers) {

      private int     lastScoredDoc  = -1;

      // Save the score of lastScoredDoc, so that we don't compute it more than
      // once in score() per document.
      private float   lastNodeScore  = Float.NaN;

      @Override
      public float scoreInNode()
      throws IOException {
        final int doc = this.doc();

        if (doc >= lastScoredDoc) {
          lastNodeScore = super.scoreInNode();
          lastScoredDoc = doc;
          coordinator.nrMatchers += super.nrMatchers();
        }
        return lastNodeScore;
      }

    };
  }

  private NodeScorer countingConjunctionSumScorer(final boolean disableCoord,
                                                   final List<NodeScorer> requiredScorers)
  throws IOException {
    // each scorer from the list counted as a single matcher
    final int requiredNrMatchers = requiredScorers.size();

    return new NodeConjunctionScorer(weight,
      disableCoord ? 1.0f : ((AbstractNodeBooleanWeight) weight).coord(requiredNrMatchers, requiredNrMatchers),
                   requiredScorers) {

      private int     lastScoredDoc  = -1;

      // Save the score of lastScoredDoc, so that we don't compute it more than
      // once in score().
      private float   lastNodeScore  = Float.NaN;

      @Override
      public float scoreInNode() throws IOException {
        final int doc = this.doc();

        if (doc >= lastScoredDoc) {
          lastNodeScore = super.scoreInNode();
          lastScoredDoc = doc;
          coordinator.nrMatchers += requiredNrMatchers;
        }
        // All scorers match, so defaultSimilarity super.score() always has 1 as
        // the coordination factor.
        // Therefore the sum of the scores of the requiredScorers
        // is used as score.
        return lastNodeScore;
      }
    };
  }

  /**
   * Returns the scorer to be used for match counting and score summing. Uses
   * requiredScorers, optionalScorers and prohibitedScorers.
   */
  private NodeScorer makeCountingSumScorer(final boolean disableCoord)
  throws IOException { // each scorer counted as a single matcher
    return (requiredScorers.size() == 0) ? this.makeCountingSumScorerNoReq(disableCoord)
                                         : this.makeCountingSumScorerSomeReq(disableCoord);
  }

  private NodeScorer makeCountingSumScorerNoReq(final boolean disableCoord)
  throws IOException { // No required scorers
    NodeScorer requiredCountingSumScorer;
    if (optionalScorers.size() > 1)
      requiredCountingSumScorer = this.countingDisjunctionSumScorer(optionalScorers);
    else if (optionalScorers.size() == 1)
      requiredCountingSumScorer = new SingleMatchScorer(optionalScorers.get(0));
    else {
      requiredCountingSumScorer = this.countingConjunctionSumScorer(disableCoord, optionalScorers);
    }
    return this.addProhibitedScorers(requiredCountingSumScorer);
  }

  private NodeScorer makeCountingSumScorerSomeReq(final boolean disableCoord)
  throws IOException { // At least one required scorer.
    final NodeScorer requiredCountingSumScorer =
      (requiredScorers.size() == 1) ? new SingleMatchScorer(requiredScorers.get(0))
                                    : this.countingConjunctionSumScorer(disableCoord, requiredScorers);

    if (optionalScorers.isEmpty()) {
      return this.addProhibitedScorers(requiredCountingSumScorer);
    }
    else {
      return new NodeReqOptScorer(
        this.addProhibitedScorers(requiredCountingSumScorer),
        optionalScorers.size() == 1
          ? new SingleMatchScorer(optionalScorers.get(0))
          // require 1 in combined, optional scorer.
          : this.countingDisjunctionSumScorer(optionalScorers));
    }
  }

  /**
   * Returns the scorer to be used for match counting and score summing. Uses
   * the given required scorer and the prohibitedScorers.
   *
   * @param requiredCountingSumScorer
   *          A required scorer already built.
   */
  private NodeScorer addProhibitedScorers(final NodeScorer requiredCountingSumScorer)
  throws IOException {
    return (prohibitedScorers.size() == 0)
      ? requiredCountingSumScorer // no prohibited
      : new NodeReqExclScorer(requiredCountingSumScorer,
                               ((prohibitedScorers.size() == 1)
                               ? prohibitedScorers.get(0)
                               : new NodeDisjunctionScorer(weight, prohibitedScorers)));
  }

  @Override
  public int doc() {
    return countingSumScorer.doc();
  }

  @Override
  public float termFreqInNode()
  throws IOException {
    return coordinator.nrMatchers;
  }

  @Override
  public IntsRef node() {
    return countingSumScorer.node();
  }

  @Override
  public boolean nextCandidateDocument() throws IOException {
    return countingSumScorer.nextCandidateDocument();
  }

  @Override
  public boolean nextNode() throws IOException {
    return countingSumScorer.nextNode();
  }

  @Override
  public float scoreInNode()
  throws IOException {
    coordinator.nrMatchers = 0;
    final float sum = countingSumScorer.scoreInNode();
    /*
     * TODO: the score is weighted by the number of matched scorer.
     * Is this the right place to do it ? Shouldn't it be done inside
     * the similarity implementation ?
     */
    return sum * coordinator.coordFactors[coordinator.nrMatchers];
  }

  @Override
  public boolean skipToCandidate(final int target) throws IOException {
    return countingSumScorer.skipToCandidate(target);
  }

  @Override
  public Collection<ChildScorer> getChildren() {
    final ArrayList<ChildScorer> children = new ArrayList<ChildScorer>();
    for (final Scorer s : optionalScorers) {
      children.add(new ChildScorer(s, Occur.SHOULD.toString()));
    }
    for (final Scorer s : prohibitedScorers) {
      children.add(new ChildScorer(s, Occur.MUST_NOT.toString()));
    }
    for (final Scorer s : requiredScorers) {
      children.add(new ChildScorer(s, Occur.MUST.toString()));
    }
    return children;
  }

  @Override
  public String toString() {
    return "NodeBooleanScorer(" + this.weight + "," + this.doc() + "," +
      this.node() + ")";
  }

  private class Coordinator {

    float[] coordFactors = null;
    int maxCoord = 0; // to be increased for each non prohibited scorer
    int nrMatchers; // to be increased by score() of match counting scorers.

    void init(final boolean disableCoord) { // use after all scorers have been added.
      coordFactors = new float[optionalScorers.size() + requiredScorers.size() + 1];
      for (int i = 0; i < coordFactors.length; i++) {
        coordFactors[i] = disableCoord ? 1.0f : ((AbstractNodeBooleanWeight) weight).coord(i, maxCoord);
      }
    }
  }

  /** Count a scorer as a single match. */
  private class SingleMatchScorer extends NodeScorer {

    private final NodeScorer scorer;

    private int     lastScoredDoc  = -1;

    // Save the score of lastScoredNode, so that we don't compute it more than
    // once in score().
    private float   lastNodeScore  = Float.NaN;

    SingleMatchScorer(final NodeScorer scorer) {
      super(scorer.getWeight());
      this.scorer = scorer;
    }

    @Override
    public float scoreInNode()
    throws IOException {
      final int doc = this.doc();

      if (doc >= lastScoredDoc) {
        lastNodeScore = scorer.scoreInNode();
        lastScoredDoc = doc;
        coordinator.nrMatchers++;
      }
      return lastNodeScore;
    }

    @Override
    public int doc() {
      return scorer.doc();
    }

    @Override
    public IntsRef node() {
      return scorer.node();
    }

    @Override
    public boolean nextCandidateDocument() throws IOException {
      return scorer.nextCandidateDocument();
    }

    @Override
    public boolean nextNode() throws IOException {
      return scorer.nextNode();
    }

    @Override
    public boolean skipToCandidate(final int target) throws IOException {
      return scorer.skipToCandidate(target);
    }

    @Override
    public String toString() {
      return "SingleMatchScorer(" + weight + "," + this.doc() + "," +
        this.node() + ")";
    }

  }

}

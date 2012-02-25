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
package org.sindice.siren.search.twig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.similarities.DefaultSimilarityProvider;
import org.apache.lucene.search.similarities.SimilarityProvider;
import org.sindice.siren.search.base.NodeScorer;
import org.sindice.siren.search.node.NodeBooleanClause.Occur;
import org.sindice.siren.search.node.NodeConjunctionScorer;
import org.sindice.siren.search.node.NodeDisjunctionScorer;
import org.sindice.siren.search.node.NodeReqExclScorer;
import org.sindice.siren.search.node.NodeReqOptScorer;
import org.sindice.siren.search.primitive.NodeTermQuery;
import org.sindice.siren.search.twig.TwigQuery.TwigWeight;

/**
 * A Query that matches cells matching boolean combinations of other primitive
 * queries, e.g. {@link NodeTermQuery}s, {@link NodePhraseQuery}s, etc.
 * Implements skipTo(), and has no limitations on the numbers of added scorers.
 * <p>
 * Uses {@link NodeConjunctionScorer}, {@link NodeDisjunctionScorer},
 * {@link NodeReqExclScorer} and {@link NodeReqOptScorer}.
 * <p>
 * We consider a {@link TwigScorer} as a primitive scorer in order to
 * support nested (group) boolean query within a cell.
 * <p>
 * Code taken from {@link BooleanScorer2} and adapted for the Siren use case.
 */
public class TwigScorer extends NodeScorer {

  private final NodeScorer rootScorer;

  private final List<NodeScorer> requiredScorers;
  private final List<NodeScorer> optionalScorers;
  private final List<NodeScorer> prohibitedScorers;

  private final Coordinator coordinator;

  /**
   * The scorer to which all scoring will be delegated, except for computing and
   * using the coordination factor.
   */
  private NodeScorer countingSumScorer = null;

  private static SimilarityProvider defaultSimProvider = new DefaultSimilarityProvider();

  /**
   * Creates a {@link TwigScorer} with the given similarity and lists of
   * required, prohibited and optional scorers. In no required scorers are added,
   * at least one of the optional scorers will have to match during the search.
   *
   * @param weight
   *          The BooleanWeight to be used.
   * @param required
   *          the list of required scorers.
   * @param prohibited
   *          the list of prohibited scorers.
   * @param optional
   *          the list of optional scorers.
   */
  public TwigScorer(final TwigWeight weight,
                    final NodeScorer root,
                    final List<NodeScorer> required,
                    final List<NodeScorer> prohibited,
                    final List<NodeScorer> optional,
                    final int maxCoord) throws IOException {
    super(weight);
    coordinator = new Coordinator();
    coordinator.maxCoord = maxCoord;

    rootScorer = root;
    optionalScorers = optional;
    requiredScorers = required;
    prohibitedScorers = prohibited;

    coordinator.init();
    countingSumScorer = this.makeCountingSumScorer();
  }

  private NodeScorer countingDisjunctionSumScorer(final List<NodeScorer> scorers)
  throws IOException {

    final NodeScorer disjunctionScorer = new NodeDisjunctionScorer(this.getWeight(), scorers) {

      private final int lastScoredDoc = -1;

      // Save the score of lastScoredDoc, so that we don't compute it more than
      // once in score().
      private final float lastDocScore = Float.NaN;

      @Override
      public float score() {
        // TODO
        throw new UnsupportedOperationException();
//        final int doc = this.doc();
//        if (doc >= lastScoredDoc) {
//          if (doc > lastScoredDoc) {
//            lastDocScore = super.score();
//            lastScoredDoc = doc;
//          }
//          coordinator.nrMatchers += super.nrMatchers;
//        }
//        return lastDocScore;
      }
    };

    return new SingleDescendantScorer(weight, rootScorer, disjunctionScorer);
  }

  private NodeScorer countingConjunctionSumScorer(final List<NodeScorer> requiredScorers)
  throws IOException {
    // each scorer from the list counted as a single matcher
    final int requiredNrMatchers = requiredScorers.size();

    return new TwigConjunctionScorer(weight,
      ((TwigWeight) weight).coord(requiredNrMatchers, requiredNrMatchers),
      rootScorer, requiredScorers) {

      private final int lastScoredDoc = -1;

      // Save the score of lastScoredDoc, so that we don't compute it more than
      // once in score().
      private final float lastDocScore = Float.NaN;

      @Override
      public float score() throws IOException {
        // TODO
        throw new UnsupportedOperationException();
//        final int doc = this.doc();
//        if (doc >= lastScoredDoc) {
//          if (doc > lastScoredDoc) {
//            lastDocScore = super.score();
//            lastScoredDoc = doc;
//          }
//          coordinator.nrMatchers += requiredNrMatchers;
//        }
//        // All scorers match, so defaultSimilarity super.score() always has 1 as
//        // the coordination factor.
//        // Therefore the sum of the scores of the requiredScorers
//        // is used as score.
//        return lastDocScore;
      }
    };
  }

  /**
   * Returns the scorer to be used for match counting and score summing. Uses
   * requiredScorers, optionalScorers and prohibitedScorers.
   */
  private NodeScorer makeCountingSumScorer()
  throws IOException { // each scorer counted as a single matcher
    return (requiredScorers.size() == 0) ? this.makeCountingSumScorerNoReq()
                                         : this.makeCountingSumScorerSomeReq();
  }

  private NodeScorer makeCountingSumScorerNoReq()
  throws IOException { // No required scorers
    NodeScorer requiredCountingSumScorer;
    if (optionalScorers.size() > 1)
      requiredCountingSumScorer = this.countingDisjunctionSumScorer(optionalScorers);
    else if (optionalScorers.size() == 1)
      requiredCountingSumScorer = new SingleDescendantScorer(weight, rootScorer, optionalScorers.get(0));
    else {
      requiredCountingSumScorer = this.countingConjunctionSumScorer(optionalScorers);
    }
    return this.addProhibitedScorers(requiredCountingSumScorer);
  }

  private NodeScorer makeCountingSumScorerSomeReq()
  throws IOException { // At least one required scorer.
    final NodeScorer requiredCountingSumScorer =
      (requiredScorers.size() == 1) ? new SingleDescendantScorer(weight, rootScorer, requiredScorers.get(0))
                                    : this.countingConjunctionSumScorer(requiredScorers);

    if (optionalScorers.isEmpty()) {
      return this.addProhibitedScorers(requiredCountingSumScorer);
    }
    else {
      return new NodeReqOptScorer(
        this.addProhibitedScorers(requiredCountingSumScorer),
        optionalScorers.size() == 1
          ? new SingleDescendantScorer(weight, rootScorer, optionalScorers.get(0))
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
                               ? new SingleDescendantScorer(weight, rootScorer, prohibitedScorers.get(0))
                               : new SingleDescendantScorer(weight, rootScorer, new NodeDisjunctionScorer(weight, prohibitedScorers))));
  }

  /**
   * Scores and collects all matching documents.
   *
   * @param hc
   *          The collector to which all matching documents are passed through
   *          {@link HitCollector#collect(int, float)}. <br>
   *          When this method is used the {@link #explain(int)} method should
   *          not be used.
   */
  @Override
  public void score(final Collector collector) throws IOException {
    // TODO
    throw new UnsupportedOperationException();
//    collector.setScorer(this);
//    while (this.nextDocument()) {
//      collector.collect(this.doc());
//    }
  }

  /**
   * Expert: Collects matching documents in a range. <br>
   * Note that {@link #nextDocument()} must be called once before this method is called
   * for the first time.
   *
   * @param hc
   *          The collector to which all matching documents are passed through
   *          {@link HitCollector#collect(int, float)}.
   * @param max
   *          Do not score documents past this.
   * @return true if more matching documents may remain.
   */
  @Override
  public boolean score(final Collector collector, final int max, final int firstDocID)
  throws IOException {
    // TODO
    throw new UnsupportedOperationException();

//    int doc = firstDocID;
//    collector.setScorer(this);
//    while (doc < max) {
//      collector.collect(doc);
//      countingSumScorer.nextDocument();
//      doc = countingSumScorer.doc();
//    }
//    return doc != NO_MORE_DOCS;
  }

  @Override
  public int doc() {
    return countingSumScorer.doc();
  }

  @Override
  public float freq() {
    return coordinator.nrMatchers;
  }

  @Override
  public int[] node() {
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
  public float score()
  throws IOException {
    coordinator.nrMatchers = 0;
    final float sum = countingSumScorer.score();
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
      Arrays.toString(this.node()) + ")";
  }

  private class Coordinator {

    float[] coordFactors = null;
    int maxCoord = 0; // to be increased for each non prohibited scorer
    int nrMatchers; // to be increased by score() of match counting scorers.

    void init() { // use after all scorers have been added.
      coordFactors = new float[optionalScorers.size() + requiredScorers.size() + 1];
      for (int i = 0; i < coordFactors.length; i++) {
        coordFactors[i] = ((TwigWeight) weight).coord(i, maxCoord);
      }
    }
  }

  /**
   * Single descendant scorer which performs a conjunction between the root and
   * the descendant node.
   */
  private class SingleDescendantScorer extends TwigConjunctionScorer {

    private final NodeScorer root;

    private final int   lastScoredDoc = -1;

    // Save the score of lastScoredDoc, so that we don't compute it more than
    // once in score().
    private final float lastDocScore = Float.NaN;

    SingleDescendantScorer(final Weight weight, final NodeScorer root, final NodeScorer descendant)
    throws IOException {
      super(weight, ((TwigWeight) weight).coord(2, 2), root, descendant);
      this.root = root;
    }

    @Override
    public float score() throws IOException {
      // TODO
      throw new UnsupportedOperationException();
//      final int doc = this.doc();
//      if (doc >= lastScoredDoc) {
//        if (doc > lastScoredDoc) {
//          lastDocScore = scorer.score();
//          lastScoredDoc = doc;
//        }
//        coordinator.nrMatchers++;
//      }
//      return lastDocScore;
    }

    @Override
    public int doc() {
      return root.doc();
    }

    @Override
    public int[] node() {
      return root.node();
    }

    @Override
    public String toString() {
      return "SingleDescendantScorer(" + weight + "," + this.doc() + "," +
        Arrays.toString(this.node()) + ")";
    }

  }

}

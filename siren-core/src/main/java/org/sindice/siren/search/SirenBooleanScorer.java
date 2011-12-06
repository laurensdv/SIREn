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
package org.sindice.siren.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.similarities.DefaultSimilarityProvider;
import org.apache.lucene.search.similarities.SimilarityProvider;

/**
 * A Query that matches cells matching boolean combinations of other primitive
 * queries, e.g. {@link SirenTermQuery}s, {@link SirenPhraseQuery}s, etc.
 * Implements skipTo(), and has no limitations on the numbers of added scorers. <br>
 * Uses ConjunctionScorer, DisjunctionScorer, ReqOptScorer and ReqExclScorer.
 * <p> Code taken from {@link BooleanScorer2} and adapted for the Siren use case.
 * <br>
 * We consider a SirenBooleanScorer as a primitive scorer in order to support
 * nested (group) boolean query within a cell.
 */
class SirenBooleanScorer extends SirenPrimitiveScorer {

  private final List<SirenPrimitiveScorer> requiredScorers   = new ArrayList<SirenPrimitiveScorer>();

  private final List<SirenPrimitiveScorer> optionalScorers   = new ArrayList<SirenPrimitiveScorer>();

  private final List<SirenPrimitiveScorer> prohibitedScorers = new ArrayList<SirenPrimitiveScorer>();

  private final Coordinator coordinator;

  /**
   * The scorer to which all scoring will be delegated, except for computing and
   * using the coordination factor.
   */
  private SirenScorer            countingSumScorer = null;

  private static SimilarityProvider defaultSimProvider = new DefaultSimilarityProvider();

//  private final int dataset = -1;
  private int entity = -1;
//  private int tuple = -1;
//  private int cell = -1;
  private int[] nodes;

  /**
   * Create a SirenBooleanScorer, that matches a boolean combination of
   * primitive siren scorers. In no required scorers are added, at least one of
   * the optional scorers will have to match during the search.
   *
   * @param weight
   *          The similarity to be used.
   */
  public SirenBooleanScorer(final Weight weight) {
    super(weight);
    coordinator = new Coordinator();
  }

  public void add(final SirenPrimitiveScorer scorer, final boolean required,
                  final boolean prohibited) {
    if (!prohibited) {
      coordinator.maxCoord++;
    }

    if (required) {
      if (prohibited) {
        throw new IllegalArgumentException(
          "scorer cannot be required and prohibited");
      }
      requiredScorers.add(scorer);
    }
    else if (prohibited) {
      prohibitedScorers.add(scorer);
    }
    else {
      optionalScorers.add(scorer);
    }
  }

  /**
   * Initialize the match counting scorer that sums all the scores.
   * <p>
   * When "counting" is used in a name it means counting the number of matching
   * scorers.<br>
   * When "sum" is used in a name it means score value summing over the matching
   * scorers
   */
  private void initCountingSumScorer()
  throws IOException {
    coordinator.init();
    countingSumScorer = this.makeCountingSumScorer();
  }

  private SirenScorer countingDisjunctionSumScorer(final List<SirenPrimitiveScorer> scorers)
  // each scorer from the list counted as a single matcher
  {
    return new SirenDisjunctionScorer(this.getWeight(), scorers) {

      private int lastScoredEntity = -1;

      @Override
      public float score()
      throws IOException {
        if (this.docID() >= lastScoredEntity) {
          lastScoredEntity = this.docID();
          coordinator.nrMatchers += super.nrMatchers;
        }
        return super.score();
      }
    };
  }

  private SirenScorer countingConjunctionSumScorer(final List<SirenPrimitiveScorer> requiredScorers)
  throws IOException {
    // each scorer from the list counted as a single matcher
    final int requiredNrMatchers = requiredScorers.size();

    return new SirenConjunctionScorer(getWeight(), requiredScorers, defaultSimProvider.coord(requiredNrMatchers, requiredNrMatchers)) {

      private int lastScoredEntity = -1;

      @Override
      public float score()
      throws IOException {
        if (this.docID() >= lastScoredEntity) {
          lastScoredEntity = this.docID();
          coordinator.nrMatchers += requiredNrMatchers;
        }
        // All scorers match, so defaultSimilarity super.score() always has 1 as
        // the coordination factor.
        // Therefore the sum of the scores of the requiredScorers
        // is used as score.
        return super.score();
      }
    };
  }

  /**
   * Returns the scorer to be used for match counting and score summing. Uses
   * requiredScorers, optionalScorers and prohibitedScorers.
   */
  private SirenScorer makeCountingSumScorer()
  throws IOException { // each scorer counted as a single matcher
    return (requiredScorers.size() == 0) ? this.makeCountingSumScorerNoReq()
                                         : this.makeCountingSumScorerSomeReq();
  }

  private SirenScorer makeCountingSumScorerNoReq()
  throws IOException { // No required scorers
    if (optionalScorers.size() == 0) {
      return new NonMatchingScorer(); // no clauses or only prohibited clauses
    }
    else { // No required scorers. At least one optional scorer.
      final SirenScorer requiredCountingSumScorer =
        (optionalScorers.size() == 1) ? new SingleMatchScorer(optionalScorers.get(0))
                                      : this.countingDisjunctionSumScorer(optionalScorers);
      return this.addProhibitedScorers(requiredCountingSumScorer);
    }
  }

  private SirenScorer makeCountingSumScorerSomeReq()
  throws IOException { // At least one required scorer.
    final SirenScorer requiredCountingSumScorer =
      (requiredScorers.size() == 1) ? new SingleMatchScorer(requiredScorers.get(0))
                                    : this.countingConjunctionSumScorer(requiredScorers);

    if (optionalScorers.size() == 0) {
      return new SirenReqOptScorer(
        this.addProhibitedScorers(requiredCountingSumScorer), new NonMatchingScorer());
    }
    else if (optionalScorers.size() == 1) {
      return new SirenReqOptScorer(
        this.addProhibitedScorers(requiredCountingSumScorer),
        new SingleMatchScorer(optionalScorers.get(0)));
    }
    else { // optionalScorers.size() > 1
      return new SirenReqOptScorer(
        this.addProhibitedScorers(requiredCountingSumScorer),
        this.countingDisjunctionSumScorer(optionalScorers));
    }
  }

  /**
   * Returns the scorer to be used for match counting and score summing. Uses
   * the given required scorer and the prohibitedScorers.
   *
   * @param requiredCountingSumScorer
   *          A required scorer already built.
   */
  private SirenScorer addProhibitedScorers(final SirenScorer requiredCountingSumScorer) {
    if (prohibitedScorers.size() == 0) {
      return requiredCountingSumScorer;
    }
    else if (prohibitedScorers.size() == 1) {
      return new SirenReqExclScorer(requiredCountingSumScorer,
        prohibitedScorers.get(0));
    }
    return new SirenReqExclScorer(requiredCountingSumScorer,
      new SirenDisjunctionScorer(getWeight(), prohibitedScorers));
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
    int doc;
    collector.setScorer(this);
    while ((doc = this.nextDoc()) != NO_MORE_DOCS) {
      collector.collect(doc);
    }
  }

  /**
   * Expert: Collects matching documents in a range. <br>
   * Note that {@link #nextDoc()} must be called once before this method is called
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
    int doc = firstDocID;
    collector.setScorer(this);
    while (doc < max) {
      collector.collect(doc);
      doc = this.nextDoc();
    }
    return doc != NO_MORE_DOCS;
  }

  @Override
  public int docID() {
    return entity;
  }

  /**
   * Position is invalid in high-level scorers. It will always return
   * {@link Integer.MAX_VALUE}.
   */
  @Override
  public int pos() {
    return Integer.MAX_VALUE;
  }

  @Override
  public int nextDoc() throws IOException {
    if (countingSumScorer == null) {
      this.initCountingSumScorer();
    }

    if (countingSumScorer.nextDoc() != NO_MORE_DOCS) {
      entity = countingSumScorer.docID();
      nodes = countingSumScorer.node().clone();
    }
    else {
      entity = NO_MORE_DOCS;
    }
    return entity;
  }

  @Override
  public int nextPosition() throws IOException {
    final boolean more = (countingSumScorer.nextPosition() != NO_MORE_POS);
    if (more) {
      nodes = countingSumScorer.node().clone();
      return 0; // position is invalid in this scorer, return 0
    }
    else {
      nodes = countingSumScorer.node().clone();
      return NO_MORE_POS;
    }
  }

  @Override
  public float score()
  throws IOException {
    coordinator.initDoc();
    final float sum = countingSumScorer.score();
    return sum * coordinator.coordFactor();
  }

  @Override
  public int advance(final int entity) throws IOException {
    if (countingSumScorer == null) {
      this.initCountingSumScorer();
    }

    if (countingSumScorer.advance(entity) != NO_MORE_DOCS) {
      this.entity = countingSumScorer.docID();
      nodes = countingSumScorer.node().clone();
    }
    else {
      this.entity = NO_MORE_DOCS;
    }
    return this.entity;
  }

  @Override
  public int advance(int docID, int[] nodes)
  throws IOException {
    if (countingSumScorer == null) {
      this.initCountingSumScorer();
    }

    if (countingSumScorer.advance(docID, nodes) != NO_MORE_DOCS) {
      this.entity = countingSumScorer.docID();
      this.nodes = countingSumScorer.node().clone();
    }
    else {
      this.entity = NO_MORE_DOCS;
    }
    return this.entity;
  }

  @Override
  public int[] node() {
    return nodes;
  }

  @Override
  public String toString() {
    return "SirenBooleanScorer(" + this.docID() + "," + Arrays.toString(nodes) + ")";
  }

  private class Coordinator {

    int             maxCoord     = 0;   // to be increased for each non
                                        // prohibited scorer

    private float[] coordFactors = null;

    void init() { // use after all scorers have been added.
      coordFactors = new float[maxCoord + 1];
      for (int i = 0; i <= maxCoord; i++) {
        coordFactors[i] = SirenBooleanScorer.this.defaultSimProvider.coord(i, maxCoord);
      }
    }

    int nrMatchers; // to be increased by score() of match counting scorers.

    void initDoc() {
      nrMatchers = 0;
    }

    float coordFactor() {
      return coordFactors[nrMatchers];
    }
  }

  /** Count a scorer as a single match. */
  private class SingleMatchScorer
  extends SirenPrimitiveScorer {

    private final SirenPrimitiveScorer scorer;

    private int          lastScoredEntity = -1;

    SingleMatchScorer(final SirenPrimitiveScorer scorer) {
      super(scorer.getWeight());
      this.scorer = scorer;
    }

    @Override
    public float score()
    throws IOException {
      if (this.docID() >= lastScoredEntity) {
        lastScoredEntity = this.docID();
        coordinator.nrMatchers++;
      }
      return scorer.score();
    }

    @Override
    public int docID() {
      return scorer.docID();
    }

    @Override
    public int pos() {
      return scorer.pos();
    }

    @Override
    public int nextDoc() throws IOException {
      if (scorer.nextDoc() != NO_MORE_DOCS)
        return scorer.docID();
      return NO_MORE_DOCS;
    }

    @Override
    public int nextPosition() throws IOException {
      final boolean more = (scorer.nextPosition() != NO_MORE_POS);
      if (more) {
        return 0; // position is invalid in this scorer, return 0.
      }
      else {
        return NO_MORE_POS;
      }
    }

    @Override
    public int advance(final int entityID) throws IOException {
      if (scorer.advance(entityID) != NO_MORE_DOCS)
        return scorer.docID();
      return NO_MORE_DOCS;
    }

    @Override
    public int advance(int docID, int[] nodes)
    throws IOException {
      if (scorer.advance(docID, nodes) != NO_MORE_DOCS)
        return scorer.docID();
      return NO_MORE_DOCS;
    }

    @Override
    public String toString() {
      return "SingleMatchScorer(" + this.docID() + "," + Arrays.toString(node()) + ")";
    }

    @Override
    public int[] node() {
      return scorer.node();
    }

  }

}

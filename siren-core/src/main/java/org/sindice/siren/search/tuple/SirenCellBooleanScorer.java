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
package org.sindice.siren.search.tuple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.similarities.DefaultSimilarityProvider;
import org.sindice.siren.search.base.NonMatchingScorer;
import org.sindice.siren.search.base.NodeScorer;
import org.sindice.siren.search.node.NodeReqOptScorer;

/**
 * A Query that matches tuples matching boolean combinations of cell
 * queries, e.g. {@link SirenCellQuery}s.
 * Implements advance(), and has no limitations on the numbers of added scorers. <br>
 * Uses SirenCellConjunctionScorer, SirenCellDisjunctionScorer, ReqOptScorer and SirenCellReqExclScorer.
 * <p> Code taken from {@link BooleanScorer2} and adapted for the Siren use case.
 */
class SirenCellBooleanScorer
extends NodeScorer {

  private final List<SirenCellScorer> requiredScorers   = new ArrayList<SirenCellScorer>();

  private final List<SirenCellScorer> optionalScorers   = new ArrayList<SirenCellScorer>();

  private final List<SirenCellScorer> prohibitedScorers = new ArrayList<SirenCellScorer>();

  private final Coordinator coordinator;

  /**
   * The scorer to which all scoring will be delegated, except for computing and
   * using the coordination factor.
   */
  private NodeScorer            countingSumScorer = null;

  private static DefaultSimilarityProvider defaultSimProvider = new DefaultSimilarityProvider();

//  private final int dataset = -1;
  private int entity = -1;
  private final int[] tuple = new int[2];

  /**
   * The tuple index constraints
   */
  private final int tupleConstraintStart;
  private final int tupleConstraintEnd;

  /**
   * Create a SirenCellBooleanScorer, that matches a boolean combination of
   * siren boolean scorers. In no required scorers are added, at least one of
   * the optional scorers will have to match during the search.
   * </br>
   * This constructor accepts a tuple index constraint, to force the scorers
   * to match only a certain tuple.
   *
   * @param weight
   *          The similarity to be used.
   * @param tupleConstraintStart
   *          The minimum cell index that should match (inclusive)
   * @param tupleConstraintEnd
   *          The maximum cell index that should match (inclusive)
   */
  public SirenCellBooleanScorer(final Weight weight,
                                final int tupleConstraintStart,
                                final int tupleConstraintEnd) {
    super(weight);
    coordinator = new Coordinator();
    this.tupleConstraintStart = tupleConstraintStart;
    this.tupleConstraintEnd = tupleConstraintEnd;
  }

  /**
   * Create a SirenCellBooleanScorer, that matches a boolean combination of
   * siren boolean scorers. In no required scorers are added, at least one of
   * the optional scorers will have to match during the search.
   *
   * @param weight
   *          The similarity to be used.
   */
  public SirenCellBooleanScorer(final Weight weight) {
    this(weight, 0, Integer.MAX_VALUE);
  }

  public void add(final SirenCellScorer scorer, final boolean required,
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

  private NodeScorer countingDisjunctionSumScorer(final List<SirenCellScorer> scorers)
  // each scorer from the list counted as a single matcher
  {
    return new SirenCellDisjunctionScorer(this.getWeight(), scorers) {

      private int lastScoredEntity = -1;

      @Override
      public float score() throws IOException {
        if (this.doc() >= lastScoredEntity) {
          lastScoredEntity = this.doc();
          coordinator.nrMatchers += super.nrMatchers;
        }
        return super.score();
      }
    };
  }

  private NodeScorer countingConjunctionSumScorer(final List<SirenCellScorer> requiredScorers)
  throws IOException {
    // each scorer from the list counted as a single matcher
    final int requiredNrMatchers = requiredScorers.size();

    return new SirenCellConjunctionScorer(defaultSimProvider, this.getWeight(), requiredScorers) {

      private int lastScoredEntity = -1;

      @Override
      public float score() throws IOException {
        if (this.doc() >= lastScoredEntity) {
          lastScoredEntity = this.doc();
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
  private NodeScorer makeCountingSumScorer()
  throws IOException { // each scorer counted as a single matcher
    return (requiredScorers.size() == 0) ? this.makeCountingSumScorerNoReq()
                                         : this.makeCountingSumScorerSomeReq();
  }

  private NodeScorer makeCountingSumScorerNoReq()
  throws IOException { // No required scorers
    if (optionalScorers.size() == 0) {
      return new NonMatchingScorer(); // no clauses or only prohibited clauses
    }
    else { // No required scorers. At least one optional scorer.
      final NodeScorer requiredCountingSumScorer =
        (optionalScorers.size() == 1) ? new SingleMatchScorer(optionalScorers.get(0))
                                      : this.countingDisjunctionSumScorer(optionalScorers);
      return this.addProhibitedScorers(requiredCountingSumScorer);
    }
  }

  private NodeScorer makeCountingSumScorerSomeReq()
  throws IOException { // At least one required scorer.
    final NodeScorer requiredCountingSumScorer =
      (requiredScorers.size() == 1) ? new SingleMatchScorer(requiredScorers.get(0))
                                    : this.countingConjunctionSumScorer(requiredScorers);

    if (optionalScorers.size() == 0) {
      return new NodeReqOptScorer(
        this.addProhibitedScorers(requiredCountingSumScorer), new NonMatchingScorer());
    }
    else if (optionalScorers.size() == 1) {
      return new NodeReqOptScorer(
        this.addProhibitedScorers(requiredCountingSumScorer),
        new SingleMatchScorer(optionalScorers.get(0)));
    }
    else { // optionalScorers.size() > 1
      return new NodeReqOptScorer(
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
  private NodeScorer addProhibitedScorers(final NodeScorer requiredCountingSumScorer) {
    if (prohibitedScorers.size() == 0) {
      return requiredCountingSumScorer;
    }
    else if (prohibitedScorers.size() == 1) {
      return new SirenCellReqExclScorer(requiredCountingSumScorer,
        prohibitedScorers.get(0));
    }
    return new SirenCellReqExclScorer(requiredCountingSumScorer,
      new SirenCellDisjunctionScorer(getWeight(), prohibitedScorers));
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
  public void score(final Collector collector)
  throws IOException {
    int doc;
    collector.setScorer(this);
    while ((doc = this.nextDocument()) != NO_MORE_DOCS) {
      collector.collect(doc);
    }
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
    int doc = firstDocID;
    collector.setScorer(this);
    while (doc < max) {
      collector.collect(doc);
      doc = this.nextDocument();
    }
    return doc != NO_MORE_DOCS;
  }

  @Override
  public int doc() {
    return entity;
  }

  @Override
  public int[] node() {
    /**
     * Cell is invalid in high-level scorers. It will always return
     * {@link Integer.MAX_VALUE}.
     */
    tuple[1] = Integer.MAX_VALUE;
    return tuple;
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
  public int nextDocument() throws IOException {
    if (countingSumScorer == null) {
      this.initCountingSumScorer();
    }

    if (countingSumScorer.nextDocument() != NO_MORE_DOCS) {
      entity = this.doNext();
    }
    else {
      entity = NO_MORE_DOCS;
    }
    return entity;
  }

  /**
   * Perform a next without initial increment.
   * <p> The next is valid when the tupleID matches the constraints.
   */
  private int doNext() throws IOException {
    boolean more = true;
    node()[0] = countingSumScorer.node()[0];

    while (more && (node()[0] < tupleConstraintStart ||
                    node()[0] > tupleConstraintEnd)) {
      if (countingSumScorer.nextPosition() == NO_MORE_POS) {
        more = (countingSumScorer.nextDocument() != NO_MORE_DOCS);
      }
      node()[0] = countingSumScorer.node()[0];
    }

    if (more) {
      entity = countingSumScorer.doc();
    }
    else {
      entity = NO_MORE_DOCS;
    }
    return entity;
  }

  @Override
  public int nextPosition() throws IOException {
    boolean more = false;
    do {
      more = (countingSumScorer.nextPosition() != NO_MORE_POS);
      node()[0] = countingSumScorer.node()[0];
    } while (more && (node()[0] < tupleConstraintStart ||
                      node()[0] > tupleConstraintEnd));
    if (more) {
      return 0; // position is invalid in this scorer, return 0
    }
    else {
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
  public int skipTo(final int entity) throws IOException {
    if (countingSumScorer == null) {
      this.initCountingSumScorer();
    }

    if (countingSumScorer.skipTo(entity) != NO_MORE_DOCS) {
      this.entity = this.doNext();
      node()[0] = countingSumScorer.node()[0];
    }
    else {
      this.entity = NO_MORE_DOCS;
    }
    return this.entity;
  }

  @Override
  public int skipTo(int docID, int[] nodes)
  throws IOException {
    if (nodes.length != 1) {
      throw new UnsupportedOperationException();
    }
    
    if (countingSumScorer == null) {
      this.initCountingSumScorer();
    }

    if (countingSumScorer.skipTo(entity, nodes) != NO_MORE_DOCS) {
      this.entity = this.doNext();
      this.tuple[0] = countingSumScorer.node()[0];
    }
    else {
      this.entity = NO_MORE_DOCS;
    }
    return this.entity;
  }

  private class Coordinator {

    int             maxCoord     = 0;   // to be increased for each non
                                        // prohibited scorer

    private float[] coordFactors = null;

    void init() { // use after all scorers have been added.
      coordFactors = new float[maxCoord + 1];
      for (int i = 0; i <= maxCoord; i++) {
        coordFactors[i] = SirenCellBooleanScorer.this.defaultSimProvider.coord(i, maxCoord);
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
  private class SingleMatchScorer extends NodeScorer {

    private final SirenCellScorer scorer;

    private int          lastScoredEntity = -1;

    SingleMatchScorer(final SirenCellScorer scorer) {
      super(scorer.getWeight());
      this.scorer = scorer;
    }

    @Override
    public float score()
    throws IOException {
      if (this.doc() >= lastScoredEntity) {
        lastScoredEntity = this.doc();
        coordinator.nrMatchers++;
      }
      return scorer.score();
    }

    @Override
    public int doc() {
      return scorer.doc();
    }

    @Override
    public int[] node() {
      /**
       * Cell is invalid in high-level scorers. It will always return
       * {@link Integer.MAX_VALUE}.
       */
      // TODO: REMOVE this new!
      return new int[] { scorer.node()[0], Integer.MAX_VALUE };
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
    public int nextDocument() throws IOException {
      if (scorer.nextDocument() != NO_MORE_DOCS)
        return this.doNext();
      return NO_MORE_DOCS;
    }

    /**
     * Perform a next without initial increment.
     * <p> The next is valid when the tupleID matches the constraints.
     */
    private int doNext() throws IOException {
      boolean more = true;
      int tupleID = scorer.node()[0];

      while (more && (tupleID < tupleConstraintStart ||
                      tupleID > tupleConstraintEnd)) {
        if (scorer.nextPosition() == NO_MORE_POS) {
          more = (scorer.nextDocument() != NO_MORE_DOCS);
        }
        tupleID = scorer.node()[0];
      }

      if (more) {
        return scorer.doc();
      }
      else {
        return NO_MORE_DOCS;
      }
    }

    @Override
    public int nextPosition() throws IOException {
      boolean more = false;
      do {
        more = (scorer.nextPosition() != NO_MORE_POS);
      } while (more && (scorer.node()[0] < tupleConstraintStart ||
                        scorer.node()[0] > tupleConstraintEnd));
      if (more) {
        return 0; // position is invalid in this scorer, return 0.
      }
      else {
        return NO_MORE_POS;
      }
    }

    @Override
    public int skipTo(final int entityID) throws IOException {
      if (scorer.skipTo(entityID) != NO_MORE_DOCS)
        return this.doNext();
      return NO_MORE_DOCS;
    }

    @Override
    public int skipTo(int docID, int[] nodes)
    throws IOException {
      if (scorer.skipTo(docID, nodes) != NO_MORE_DOCS)
        return this.doNext();
      return NO_MORE_DOCS;
    }

  }

}

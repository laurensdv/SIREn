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
import java.util.List;

import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.IntsRef;
import org.sindice.siren.index.DocsAndNodesIterator;
import org.sindice.siren.search.base.NodeScorer;
import org.sindice.siren.search.node.NodeBooleanScorer;
import org.sindice.siren.search.twig.TwigQuery.TwigWeight;

/**
 * A scorer that matches a root (ancestor) scorer with a boolean combination of
 * other (descendant) scorers.
 * <p>
 * The {@link TwigScorer} subclasses the {@link NodeBooleanScorer}. A Twig query
 * is rewritten into a pure boolean query. To achieve this, we
 * must perform the following:
 * <ul>
 * <li> The descendant scorers are filtered so that they return potential common
 * ancestors. Such a filtering is performed by {@link AncestorFilterNodeScorer}.
 * <li> The root scorer is added as a required clause into the boolean query.
 * </ul>
 */
public class TwigScorer extends NodeBooleanScorer {

  private final NodeScorer rootScorer;

  /**
   * Creates a {@link TwigScorer} with the given root scorer and lists of
   * required, prohibited and optional scorers.
   *
   * @param weight
   *          The BooleanWeight to be used.
   * @param disableCoord
   *          If this parameter is true, coordination level matching
   *          ({@link Similarity#coord(int, int)}) is not used.
   * @param root
   *          The scorer of the twig root.
   * @param rootLevel
   *          The level of the twig root.
   * @param required
   *          the list of required scorers.
   * @param prohibited
   *          the list of prohibited scorers.
   * @param optional
   *          the list of optional scorers.
   */
  public TwigScorer(final TwigWeight weight,
                    final boolean disableCoord,
                    final NodeScorer root, final int rootLevel,
                    final List<NodeScorer> required,
                    final List<NodeScorer> prohibited,
                    final List<NodeScorer> optional,
                    final int maxCoord) throws IOException {
    super(weight, disableCoord,
      append(addAncestorFilter(required, rootLevel), root),
      addAncestorFilter(prohibited, rootLevel),
      addAncestorFilter(optional, rootLevel), maxCoord);
    rootScorer = root;
  }

  private static final List<NodeScorer> addAncestorFilter(final List<NodeScorer> scorers,
                                                          final int ancestorLevel) {
    final ArrayList<NodeScorer> filteredScorers = new ArrayList<NodeScorer>();
    for (final NodeScorer scorer : scorers) {
      filteredScorers.add(new AncestorFilterNodeScorer(scorer, ancestorLevel));
    }
    return filteredScorers;
  }

  private static final List<NodeScorer> append(final List<NodeScorer> scorers, final NodeScorer scorer) {
    scorers.add(scorer);
    return scorers;
  }

  @Override
  public String toString() {
    return "TwigScorer(" + this.weight + "," + this.doc() + "," +
      this.node() + ")";
  }

  /**
   * Scorer that filters node path and output the ancestor node path.
   * <p>
   * The level of the ancestor must be provided and used to modify the
   * {@link IntsRef#length} of the node path returned by the inner scorer.
   */
  protected static class AncestorFilterNodeScorer extends NodeScorer {

    private final NodeScorer scorer;
    private final int ancestorLevel;

    private final int   lastScoredDoc = -1;

    // Save the score of lastScoredDoc, so that we don't compute it more than
    // once in score().
    private final float lastDocScore = Float.NaN;

    AncestorFilterNodeScorer(final NodeScorer scorer, final int ancestorLevel) {
      super(scorer.getWeight());
      this.scorer = scorer;
      this.ancestorLevel = ancestorLevel;
    }

    protected NodeScorer getScorer() {
      return scorer;
    }

    @Override
    public float score() throws IOException {
      return scorer.score();
    }

    @Override
    public String toString() {
      return "AncestorFilterScorer(" + weight + "," + this.doc() + "," +
        this.node() + ")";
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
    public int doc() {
      return scorer.doc();
    }

    @Override
    public IntsRef node() {
      final IntsRef node = scorer.node();
      // resize node array only if node is not a sentinel value
      if (node.length > ancestorLevel &&
          node.ints[0] != -1 &&
          node != DocsAndNodesIterator.NO_MORE_NOD) {
        node.length = ancestorLevel;
      }
      return node;
    }

  }

}

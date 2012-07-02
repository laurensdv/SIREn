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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ComplexExplanation;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.ToStringUtils;
import org.sindice.siren.search.primitive.NodePrimitiveQuery;

/**
 * A Query that matches a boolean combination of Ancestor-Descendant and
 * Parent-Child node queries.
 * <p>
 * Code taken from {@link BooleanQuery} and adapted for the Siren use case.
 */
public class TwigQuery extends NodeBooleanQuery {

  private NodeQuery root;

  /** Constructs an empty twig query. */
  public TwigQuery(final int rootLevel, final NodeQuery root) {
    this.root = root;
    // copy the root node constraint, if any
    final int[] constraint = root.getNodeConstraint();
    this.setNodeConstraint(constraint[0], constraint[1]);
    // set level constraint
    this.setLevelConstraint(rootLevel);
    root.setLevelConstraint(rootLevel);
  }

  /**
   * Constructs an empty twig query.
   *
   * {@link SimilarityProvider#coord(int,int)} may be disabled in scoring, as
   * appropriate. For example, this score factor does not make sense for most
   * automatically generated queries, like {@link WildcardQuery} and {@link
   * FuzzyQuery}.
   *
   * @param disableCoord disables {@link SimilarityProvider#coord(int,int)} in scoring.
   */
  public TwigQuery(final int rootLevel, final NodeQuery root, final boolean disableCoord) {
    super(disableCoord);
    this.root = root;
    // copy the root node constraint, if any
    final int[] constraint = root.getNodeConstraint();
    this.setNodeConstraint(constraint[0], constraint[1]);
    // set level constraint
    this.setLevelConstraint(rootLevel);
    root.setLevelConstraint(rootLevel);
  }

  /**
   * Adds a child clause to the twig query.
   *
   * @throws TooManyClauses
   *           if the new number of clauses exceeds the maximum clause number
   * @see #getMaxClauseCount()
   */
  public void addChild(final NodeQuery query, final NodeBooleanClause.Occur occur) {
    // set the level constraint on the query
    query.setLevelConstraint(levelConstraint + 1);
    // set the ancestor pointer
    query.setAncestorPointer(root);
    // add the query to the clauses
    this.add(new NodeBooleanClause(query, occur));
  }

  /**
   * Adds a descendant clause to the twig query.
   *
   * @throws TooManyClauses
   *           if the new number of clauses exceeds the maximum clause number
   * @see #getMaxClauseCount()
   */
  public void addDescendant(final int nodeLevel, final NodePrimitiveQuery query, final NodeBooleanClause.Occur occur) {
    // set the level constraint on the query
    query.setLevelConstraint(nodeLevel);
    // set the ancestor pointer
    query.setAncestorPointer(root);
    // add the query to the clauses
    this.add(new NodeBooleanClause(query, occur));
  }

  /**
   * Adds a descendant clause to the twig query.
   *
   * @throws TooManyClauses
   *           if the new number of clauses exceeds the maximum clause number
   * @see #getMaxClauseCount()
   */
  public void addDescendant(final int nodeLevel, final NodeBooleanQuery query, final NodeBooleanClause.Occur occur) {
    // set the level constraint on the query
    query.setLevelConstraint(nodeLevel);
    // set the ancestor pointer
    query.setAncestorPointer(root);
    // add the query to the clauses
    this.add(new NodeBooleanClause(query, occur));
  }

  /**
   * Adds a descendant clause to the twig query.
   *
   * @throws TooManyClauses
   *           if the new number of clauses exceeds the maximum clause number
   * @see #getMaxClauseCount()
   */
  public void addDescendant(final TwigQuery query, final NodeBooleanClause.Occur occur) {
    // set the ancestor pointer
    query.setAncestorPointer(root);
    // add the query to the clauses
    this.add(new NodeBooleanClause(query, occur));
  }

  @Override
  public void setAncestorPointer(final NodeQuery ancestor) {
    super.setAncestorPointer(ancestor);
    // keep root query synchronised with twig query
    root.setAncestorPointer(ancestor);
  }

  @Override
  public void setNodeConstraint(final int lowerBound, final int upperBound) {
    super.setNodeConstraint(lowerBound, upperBound);
    // keep root query synchronised with twig query
    root.setNodeConstraint(lowerBound, upperBound);
  }

  @Override
  public void setLevelConstraint(final int levelConstraint) {
    super.setLevelConstraint(levelConstraint);
    // keep root query synchronised with twig query
    root.setLevelConstraint(levelConstraint);
  }

  /**
   * Expert: the Weight for {@link TwigQuery}, used to
   * normalize, score and explain these queries.
   */
  protected class TwigWeight extends NodeBooleanWeight {

    protected Weight rootWeight;

    public TwigWeight(final IndexSearcher searcher, final boolean disableCoord)
    throws IOException {
      super(searcher, disableCoord);
      rootWeight = root.createWeight(searcher);
    }

    @Override
    protected void initWeights(final IndexSearcher searcher) throws IOException {
      weights = new ArrayList<Weight>(clauses.size());
      for (int i = 0; i < clauses.size(); i++) {
        final NodeBooleanClause c = clauses.get(i);
        final NodeQuery q = c.getQuery();
        weights.add(q.createWeight(searcher));
        if (!c.isProhibited()) maxCoord++;
      }
    }

    @Override
    public float getValueForNormalization() throws IOException {
      float sum = super.getValueForNormalization();
      // incorporate root weight
      sum += rootWeight.getValueForNormalization() * TwigQuery.this.getBoost() * TwigQuery.this.getBoost();
      return sum;
    }

    @Override
    public void normalize(final float norm, float topLevelBoost) {
      super.normalize(norm, topLevelBoost);
      // Normalise root weight
      topLevelBoost *= TwigQuery.this.getBoost();
      rootWeight.normalize(norm, topLevelBoost);
    }

    // TODO: Add root node in the explanation
    @Override
    public Explanation explain(final AtomicReaderContext context, final int doc)
    throws IOException {
      final ComplexExplanation sumExpl = new ComplexExplanation();
      sumExpl.setDescription("sum of:");
      int coord = 0;
      float sum = 0.0f;
      boolean fail = false;
      final Iterator<NodeBooleanClause> cIter = clauses.iterator();
      for (final Weight w : weights) {
        final NodeBooleanClause c = cIter.next();
        if (w.scorer(context, true, true, context.reader().getLiveDocs()) == null) {
          if (c.isRequired()) {
            fail = true;
            final Explanation r = new Explanation(0.0f, "no match on required " +
            		"clause (" + c.getQuery().toString() + ")");
            sumExpl.addDetail(r);
          }
          continue;
        }
        final Explanation e = w.explain(context, doc);
        if (e.isMatch()) {
          if (!c.isProhibited()) {
            sumExpl.addDetail(e);
            sum += e.getValue();
            coord++;
          }
          else {
            final Explanation r =
              new Explanation(0.0f, "match on prohibited clause (" +
                c.getQuery().toString() + ")");
            r.addDetail(e);
            sumExpl.addDetail(r);
            fail = true;
          }
        }
        else if (c.isRequired()) {
          final Explanation r = new Explanation(0.0f, "no match on required " +
          		"clause (" + c.getQuery().toString() + ")");
          r.addDetail(e);
          sumExpl.addDetail(r);
          fail = true;
        }
      }
      if (fail) {
        sumExpl.setMatch(Boolean.FALSE);
        sumExpl.setValue(0.0f);
        sumExpl.setDescription
          ("Failure to meet condition(s) of required/prohibited clause(s)");
        return sumExpl;
      }

      sumExpl.setMatch(0 < coord ? Boolean.TRUE : Boolean.FALSE);
      sumExpl.setValue(sum);

      final float coordFactor = this.coord(coord, maxCoord);
      if (coordFactor == 1.0f) {
        return sumExpl;                             // eliminate wrapper
      }
      else {
        final ComplexExplanation result = new ComplexExplanation(sumExpl.isMatch(),
                                                           sum*coordFactor,
                                                           "product of:");
        result.addDetail(sumExpl);
        result.addDetail(new Explanation(coordFactor,
                                         "coord("+coord+"/"+maxCoord+")"));
        return result;
      }
    }

    @Override
    public Scorer scorer(final AtomicReaderContext context,
                         final boolean scoreDocsInOrder,
                         final boolean topScorer, final Bits acceptDocs)
    throws IOException {
      final NodeScorer rootScorer = (NodeScorer) rootWeight.scorer(context, true, false, acceptDocs);
      final List<NodeScorer> required = new ArrayList<NodeScorer>();
      final List<NodeScorer> prohibited = new ArrayList<NodeScorer>();
      final List<NodeScorer> optional = new ArrayList<NodeScorer>();
      final Iterator<NodeBooleanClause> cIter = clauses.iterator();
      for (final Weight w  : weights) {
        final NodeBooleanClause c =  cIter.next();
        final NodeScorer subScorer = (NodeScorer) w.scorer(context, true, false, acceptDocs);
        if (subScorer == null) {
          if (c.isRequired()) {
            return null;
          }
        } else if (c.isRequired()) {
          required.add(subScorer);
        } else if (c.isProhibited()) {
          prohibited.add(subScorer);
        } else {
          optional.add(subScorer);
        }
      }

      return new TwigScorer(this, disableCoord, rootScorer, levelConstraint,
        required, prohibited, optional, maxCoord);
    }

  }

  @Override
  public Weight createWeight(final IndexSearcher searcher) throws IOException {
    return new TwigWeight(searcher, disableCoord);
  }

  @Override
  public Query rewrite(final IndexReader reader) throws IOException {
    // optimize 0-clause queries
    if (clauses.size() == 0) {
      // rewrite root first
      NodeQuery query = (NodeQuery) root.rewrite(reader);

      if (this.getBoost() != 1.0f) {                 // incorporate boost
        if (query == root) {                         // if rewrite was no-op
          query = (NodeQuery) query.clone();         // then clone before boost
        }
        query.setBoost(this.getBoost() * query.getBoost());
      }

      // copy constraint
      query.setNodeConstraint(lowerBound, upperBound);
      query.setLevelConstraint(levelConstraint);

      // copy ancestor
      query.setAncestorPointer(ancestor);

      return query;
    }

    TwigQuery clone = null;

    // rewrite root
    clone = this.rewriteRoot(clone, reader);

    // recursively rewrite ancestors and childs
    clone = this.rewriteClauses(clone, reader);

    // some clauses rewrote
    if (clone != null) {
      // copy ancestor
      clone.setAncestorPointer(ancestor);
      return clone;
    }
    else { // no clauses rewrote
      return this;
    }
  }

  private TwigQuery rewriteRoot(TwigQuery clone, final IndexReader reader)
  throws IOException {
    final NodeQuery query = (NodeQuery) root.rewrite(reader);
    if (query != root) {
      if (clone == null) {
        clone = (TwigQuery) this.clone();
      }
      // copy ancestor
      query.setAncestorPointer(ancestor);
      clone.root = query;
    }
    return clone;
  }

  private TwigQuery rewriteClauses(TwigQuery clone, final IndexReader reader)
  throws IOException {
    for (int i = 0 ; i < clauses.size(); i++) {
      final NodeBooleanClause c = clauses.get(i);
      final NodeQuery query = (NodeQuery) c.getQuery().rewrite(reader);
      if (query != c.getQuery()) {                     // clause rewrote: must clone
        if (clone == null) {
          clone = (TwigQuery) this.clone();
        }
        // set root as ancestor
        query.setAncestorPointer(clone.root);
        clone.clauses.set(i, new NodeBooleanClause(query, c.getOccur()));
      }
    }
    return clone;
  }

  @Override @SuppressWarnings("unchecked")
  public Query clone() {
    final TwigQuery clone = (TwigQuery) super.clone();
    clone.clauses = (ArrayList<NodeBooleanClause>) this.clauses.clone();
    clone.root = (NodeQuery) this.root.clone();
    return clone;
  }

  @Override
  public String toString(final String field) {
    final StringBuffer buffer = new StringBuffer();
    final boolean needParens = (this.getBoost() != 1.0);
    if (needParens) {
      buffer.append("(");
    }

    buffer.append("{");
    buffer.append(root.toString(field));
    buffer.append("}");
    buffer.append(" // ");

    buffer.append("{");
    for (int i = 0; i < clauses.size(); i++) {
      final NodeBooleanClause c = clauses.get(i);
      if (c.isProhibited())
        buffer.append("-");
      else if (c.isRequired()) buffer.append("+");

      final Query subQuery = c.getQuery();
      if (subQuery != null) {
        if (subQuery instanceof TwigQuery ||
            subQuery instanceof NodeBooleanQuery) { // wrap sub-bools in parens
          buffer.append("(");
          buffer.append(subQuery.toString(field));
          buffer.append(")");
        }
        else {
          buffer.append(subQuery.toString(field));
        }
      }
      else {
        buffer.append("null");
      }

      if (i != clauses.size() - 1) buffer.append(", ");
    }
    buffer.append("}");

    if (needParens) {
      buffer.append(")");
    }

    if (this.getBoost() != 1.0f) {
      buffer.append(ToStringUtils.boost(this.getBoost()));
    }

    return buffer.toString();
  }

  /** Returns true iff <code>o</code> is equal to this. */
  @Override
  public boolean equals(final Object o) {
    if (!(o instanceof TwigQuery)) return false;
    final TwigQuery other = (TwigQuery) o;
    return (this.getBoost() == other.getBoost()) &&
           this.clauses.equals(other.clauses) &&
           this.disableCoord == other.disableCoord &&
           this.root.equals(other.root) &&
           this.levelConstraint == other.levelConstraint &&
           this.lowerBound == other.lowerBound &&
           this.upperBound == other.upperBound;
  }

}

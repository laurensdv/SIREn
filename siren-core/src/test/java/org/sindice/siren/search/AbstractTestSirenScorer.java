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
 * @author Renaud Delbru [ 21 Apr 2009 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.search;

import static org.sindice.siren.search.AbstractTestSirenScorer.NodeTermQueryBuilder.ntq;

import java.io.IOException;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.sindice.siren.index.DocsAndNodesIterator;
import org.sindice.siren.search.doc.DocumentQuery;
import org.sindice.siren.search.node.MultiNodeTermQuery.RewriteMethod;
import org.sindice.siren.search.node.NodeBooleanClause;
import org.sindice.siren.search.node.NodeBooleanClause.Occur;
import org.sindice.siren.search.node.NodeBooleanQuery;
import org.sindice.siren.search.node.NodeNumericRangeQuery;
import org.sindice.siren.search.node.NodePhraseQuery;
import org.sindice.siren.search.node.NodeQuery;
import org.sindice.siren.search.node.NodeScorer;
import org.sindice.siren.search.node.NodeTermQuery;
import org.sindice.siren.search.node.TupleQuery;
import org.sindice.siren.search.node.TwigQuery;
import org.sindice.siren.util.BasicSirenTestCase;

public abstract class AbstractTestSirenScorer extends BasicSirenTestCase {

  public static DocumentQuery dq(final NodeQuery nq) {
    return new DocumentQuery(nq);
  }

  protected NodeScorer getScorer(final NodeQueryBuilder builder) throws IOException {
    return this.getScorer(builder.getNodeQuery());
  }

  protected NodeScorer getScorer(final Query query) throws IOException {
    this.refreshReaderAndSearcher();
    final Weight weight = searcher.createNormalizedWeight(query);
    assertTrue(searcher.getTopReaderContext() instanceof AtomicReaderContext);
    final AtomicReaderContext context = (AtomicReaderContext) searcher.getTopReaderContext();
    final Scorer s = weight.scorer(context, true, true, context.reader().getLiveDocs());
    return (NodeScorer) s;
  }

  public static abstract class NodeQueryBuilder {

    public NodeQueryBuilder bound(final int lowerBound, final int upperBound) {
      this.getNodeQuery().setNodeConstraint(lowerBound, upperBound);
      return this;
    }

    public NodeQueryBuilder level(final int level) {
      this.getNodeQuery().setLevelConstraint(level);
      return this;
    }

    public abstract NodeQuery getNodeQuery();

    public abstract Query getDocumentQuery();

  }

  public static class NodeNumericRangeQueryBuilder extends NodeQueryBuilder {

    protected final NodeNumericRangeQuery<? extends Number> nmq;

    public NodeNumericRangeQueryBuilder setRewriteMethod(final RewriteMethod method) {
      nmq.setRewriteMethod(method);
      return this;
    }

    private NodeNumericRangeQueryBuilder(String field,
                                         int precisionStep,
                                         Integer min,
                                         Integer max,
                                         boolean minInclusive,
                                         boolean maxInclusive) {
      nmq = NodeNumericRangeQuery
      .newIntRange(field, precisionStep, min, max, minInclusive, maxInclusive);
    }

    private NodeNumericRangeQueryBuilder(String field,
                                         int precisionStep,
                                         Float min,
                                         Float max,
                                         boolean minInclusive,
                                         boolean maxInclusive) {
      nmq = NodeNumericRangeQuery
      .newFloatRange(field, precisionStep, min, max, minInclusive, maxInclusive);
    }

    private NodeNumericRangeQueryBuilder(String field,
                                         int precisionStep,
                                         Double min,
                                         Double max,
                                         boolean minInclusive,
                                         boolean maxInclusive) {
      nmq = NodeNumericRangeQuery
      .newDoubleRange(field, precisionStep, min, max, minInclusive, maxInclusive);
    }

    private NodeNumericRangeQueryBuilder(String field,
                                         int precisionStep,
                                         Long min,
                                         Long max,
                                         boolean minInclusive,
                                         boolean maxInclusive) {
      nmq = NodeNumericRangeQuery
      .newLongRange(field, precisionStep, min, max, minInclusive, maxInclusive);
    }

    public static NodeNumericRangeQueryBuilder nmqInt(String field,
                                                      int precisionStep,
                                                      Integer min,
                                                      Integer max,
                                                      boolean minInclusive,
                                                      boolean maxInclusive) {
      return new NodeNumericRangeQueryBuilder(field, precisionStep, min, max, minInclusive, maxInclusive);
    }

    public static NodeNumericRangeQueryBuilder nmqFloat(String field,
                                                        int precisionStep,
                                                        Float min,
                                                        Float max,
                                                        boolean minInclusive,
                                                        boolean maxInclusive) {
      return new NodeNumericRangeQueryBuilder(field, precisionStep, min, max, minInclusive, maxInclusive);
    }

    public static NodeNumericRangeQueryBuilder nmqDouble(String field,
                                                         int precisionStep,
                                                         Double min,
                                                         Double max,
                                                         boolean minInclusive,
                                                         boolean maxInclusive) {
      return new NodeNumericRangeQueryBuilder(field, precisionStep, min, max, minInclusive, maxInclusive);
    }

    public static NodeNumericRangeQueryBuilder nmqLong(String field,
                                                       int precisionStep,
                                                       Long min,
                                                       Long max,
                                                       boolean minInclusive,
                                                       boolean maxInclusive) {
      return new NodeNumericRangeQueryBuilder(field, precisionStep, min, max, minInclusive, maxInclusive);
    }

    @Override
    public NodeQuery getNodeQuery() {
      return nmq;
    }

    @Override
    public Query getDocumentQuery() {
      return new DocumentQuery(nmq);
    }

  }

  public static class NodeTermQueryBuilder extends NodeQueryBuilder {

    protected final NodeTermQuery ntq;

    private NodeTermQueryBuilder(final String fieldName, final String term) {
      final Term t = new Term(fieldName, term);
      ntq = new NodeTermQuery(t);
    }

    public static NodeTermQueryBuilder ntq(final String term) {
      return new NodeTermQueryBuilder(DEFAULT_TEST_FIELD, term);
    }

    @Override
    public NodeQuery getNodeQuery() {
      return ntq;
    }

    @Override
    public Query getDocumentQuery() {
      return new DocumentQuery(ntq);
    }

  }

  public static class NodePhraseQueryBuilder extends NodeQueryBuilder {

    protected final NodePhraseQuery npq;

    private NodePhraseQueryBuilder(final String fieldName, final String[] terms) {
      npq = new NodePhraseQuery();
      for (int i = 0; i < terms.length; i++) {
        if (terms[i].isEmpty()) { // if empty string, skip it
          continue;
        }
        final Term t = new Term(fieldName, terms[i]);
        npq.add(t, i);
      }
    }

    /**
     * If term is equal to an empty string, this is considered as a position
     * gap.
     */
    public static NodePhraseQueryBuilder npq(final String ... terms) {
      return npq(DEFAULT_TEST_FIELD, terms);
    }

    /**
     * If term is equal to an empty string, this is considered as a position
     * gap.
     * The field value is passed as an argument
     */
    public static NodePhraseQueryBuilder npq(final String field, final String[] terms) {
      return new NodePhraseQueryBuilder(field, terms);
    }

    @Override
    public NodeQuery getNodeQuery() {
      return npq;
    }

    @Override
    public Query getDocumentQuery() {
      return new DocumentQuery(npq);
    }

  }

  public static class NodeBooleanClauseBuilder {

    public static NodeBooleanClause must(final NodeQueryBuilder builder) {
      return new NodeBooleanClause(builder.getNodeQuery(), Occur.MUST);
    }

    public static NodeBooleanClause must(final String term) {
      return new NodeBooleanClause(ntq(term).ntq, Occur.MUST);
    }

    public static NodeBooleanClause[] must(final String ... terms) {
      final NodeBooleanClause[] clauses = new NodeBooleanClause[terms.length];
      for (int i = 0; i < terms.length; i++) {
        clauses[i] = new NodeBooleanClause(ntq(terms[i]).ntq, Occur.MUST);
      }
      return clauses;
    }

    public static NodeBooleanClause should(final NodeQueryBuilder builder) {
      return new NodeBooleanClause(builder.getNodeQuery(), Occur.SHOULD);
    }

    public static NodeBooleanClause should(final String term) {
      return new NodeBooleanClause(ntq(term).ntq, Occur.SHOULD);
    }

    public static NodeBooleanClause[] should(final String ... terms) {
      final NodeBooleanClause[] clauses = new NodeBooleanClause[terms.length];
      for (int i = 0; i < terms.length; i++) {
        clauses[i] = new NodeBooleanClause(ntq(terms[i]).ntq, Occur.SHOULD);
      }
      return clauses;
    }

    public static NodeBooleanClause not(final NodeQueryBuilder builder) {
      return new NodeBooleanClause(builder.getNodeQuery(), Occur.MUST_NOT);
    }

    public static NodeBooleanClause not(final String term) {
      return new NodeBooleanClause(ntq(term).ntq, Occur.MUST_NOT);
    }

    public static NodeBooleanClause[] not(final String ... terms) {
      final NodeBooleanClause[] clauses = new NodeBooleanClause[terms.length];
      for (int i = 0; i < terms.length; i++) {
        clauses[i] = new NodeBooleanClause(ntq(terms[i]).ntq, Occur.MUST_NOT);
      }
      return clauses;
    }

  }

  public static class NodeBooleanQueryBuilder extends NodeQueryBuilder {

    protected NodeBooleanQuery nbq;

    private NodeBooleanQueryBuilder(final NodeBooleanClause[] clauses) {
      nbq = new NodeBooleanQuery();
      for (final NodeBooleanClause clause : clauses) {
        nbq.add(clause);
      }
    }

    public static NodeBooleanQueryBuilder nbq(final NodeBooleanClause ... clauses) {
      return new NodeBooleanQueryBuilder(clauses);
    }

    @Override
    public NodeQuery getNodeQuery() {
      return nbq;
    }

    @Override
    public NodeBooleanQueryBuilder bound(final int lowerBound, final int upperBound) {
      return (NodeBooleanQueryBuilder) super.bound(lowerBound, upperBound);
    }

    @Override
    public Query getDocumentQuery() {
      return new DocumentQuery(nbq);
    }

  }

  public static class TwigQueryBuilder extends NodeQueryBuilder {

    protected TwigQuery twq;

    private TwigQueryBuilder(final int rootLevel, final NodeQueryBuilder builder) {
      twq = new TwigQuery(rootLevel, builder.getNodeQuery());
    }

    private TwigQueryBuilder(final int rootLevel) {
      twq = new TwigQuery(rootLevel);
    }

    public static TwigQueryBuilder twq(final int rootLevel, final NodeBooleanClause ... clauses) {
      return new TwigQueryBuilder(rootLevel, NodeBooleanQueryBuilder.nbq(clauses));
    }

    public static TwigQueryBuilder twq(final int rootLevel) {
      return new TwigQueryBuilder(rootLevel);
    }

    public TwigQueryBuilder with(final TwigChildBuilder child) {
      twq.addChild(child.nbq, Occur.MUST);
      return this;
    }

    public TwigQueryBuilder without(final TwigChildBuilder child) {
      twq.addChild(child.nbq, Occur.MUST_NOT);
      return this;
    }

    public TwigQueryBuilder optional(final TwigChildBuilder child) {
      twq.addChild(child.nbq, Occur.SHOULD);
      return this;
    }

    public TwigQueryBuilder with(final TwigDescendantBuilder desc) {
      twq.addDescendant(desc.level, desc.nbq, Occur.MUST);
      return this;
    }

    public TwigQueryBuilder without(final TwigDescendantBuilder desc) {
      twq.addDescendant(desc.level, desc.nbq, Occur.MUST_NOT);
      return this;
    }

    public TwigQueryBuilder optional(final TwigDescendantBuilder desc) {
      twq.addDescendant(desc.level, desc.nbq, Occur.SHOULD);
      return this;
    }

    @Override
    public NodeQuery getNodeQuery() {
      return twq;
    }

    @Override
    public Query getDocumentQuery() {
      return new DocumentQuery(twq);
    }

  }

  public static class TwigChildBuilder {

    NodeBooleanQuery nbq;

    private TwigChildBuilder(final NodeBooleanClause[] clauses) {
      nbq = NodeBooleanQueryBuilder.nbq(clauses).nbq;
    }

    public static TwigChildBuilder child(final NodeBooleanClause ... clauses) {
      return new TwigChildBuilder(clauses);
    }

  }

  public static class TwigDescendantBuilder {

    int level;
    NodeBooleanQuery nbq;

    private TwigDescendantBuilder(final int level, final NodeBooleanClause[] clauses) {
      this.level = level;
      nbq = NodeBooleanQueryBuilder.nbq(clauses).nbq;
    }

    public static TwigDescendantBuilder desc(final int level, final NodeBooleanClause ... clauses) {
      return new TwigDescendantBuilder(level, clauses);
    }

  }

  public static class TupleQueryBuilder extends NodeQueryBuilder {

    protected TupleQuery tq;

    private TupleQueryBuilder() {
      tq = new TupleQuery(true);
    }

    private TupleQueryBuilder(final int rootLevel) {
      tq = new TupleQuery(rootLevel, true);
    }

    public static TupleQueryBuilder tuple() {
      return new TupleQueryBuilder();
    }

    public static TupleQueryBuilder tuple(final int rootLevel) {
      return new TupleQueryBuilder(rootLevel);
    }

    public TupleQueryBuilder with(final NodeBooleanQueryBuilder ... clauses) {
      for (final NodeBooleanQueryBuilder clause : clauses) {
        tq.add(clause.nbq, Occur.MUST);
      }
      return this;
    }

    public TupleQueryBuilder without(final NodeBooleanQueryBuilder ... clauses) {
      for (final NodeBooleanQueryBuilder clause : clauses) {
        tq.add(clause.nbq, Occur.MUST_NOT);
      }
      return this;
    }

    public TupleQueryBuilder optional(final NodeBooleanQueryBuilder ... clauses) {
      for (final NodeBooleanQueryBuilder clause : clauses) {
        tq.add(clause.nbq, Occur.SHOULD);
      }
      return this;
    }

    @Override
    public NodeQuery getNodeQuery() {
      return tq;
    }

    @Override
    public Query getDocumentQuery() {
      return new DocumentQuery(tq);
    }

  }

  /**
   * Assert if a scorer reaches end of stream, and check if sentinel values are
   * set.
   */
  public static void assertEndOfStream(final NodeScorer scorer) throws IOException {
    assertFalse(scorer.nextCandidateDocument());
    assertEquals(DocsAndNodesIterator.NO_MORE_DOC, scorer.doc());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());
  }

}

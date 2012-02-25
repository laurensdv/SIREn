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
 * @author Renaud Delbru [ 9 Feb 2009 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.search.primitive;

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReader.AtomicReaderContext;
import org.apache.lucene.index.IndexReader.ReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermState;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ComplexExplanation;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.Similarity.ExactDocScorer;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.ReaderUtil;
import org.apache.lucene.util.TermContext;
import org.apache.lucene.util.ToStringUtils;
import org.sindice.siren.index.ConstrainedNodesEnum;
import org.sindice.siren.index.DocsNodesAndPositionsEnum;
import org.sindice.siren.index.codecs.siren020.Siren020DocNodAndPosEnum;
import org.sindice.siren.search.tuple.SirenCellQuery;
import org.sindice.siren.search.tuple.SirenTupleQuery;

/**
 * A Query that matches entities containing a term. Provides an interface to
 * iterate over the entities, tuples and cells containing the term. This may be
 * combined with other terms with a {@link SirenCellQuery}, a
 * {@link SirenTupleQuery} or a {@link BooleanQuery}.
 */
public class NodeTermQuery extends NodePrimitiveQuery {

  private final Term term;
  private final int docFreq;
  private final TermContext perReaderTermState;

  protected class NodeTermWeight extends Weight {

    private final Similarity similarity;
    private final Similarity.Stats stats;
    private final TermContext termStates;

    public NodeTermWeight(final IndexSearcher searcher, final TermContext termStates)
    throws IOException {
      assert termStates != null : "TermContext must not be null";
      this.termStates = termStates;
      similarity = searcher.getSimilarityProvider().get(term.field());
      stats = similarity.computeStats(searcher.collectionStatistics(term.field()),
                                      NodeTermQuery.this.getBoost(),
                                      searcher.termStatistics(term, termStates));
    }

    @Override
    public String toString() {
      return "weight(" + NodeTermQuery.this + ")";
    }

    @Override
    public Query getQuery() {
      return NodeTermQuery.this;
    }

    @Override
    public float getValueForNormalization()
    throws IOException {
      return stats.getValueForNormalization();
    }

    @Override
    public void normalize(final float queryNorm, final float topLevelBoost) {
      stats.normalize(queryNorm, topLevelBoost);
    }

    @Override
    public Scorer scorer(final AtomicReaderContext context,
                         final boolean scoreDocsInOrder,
                         final boolean topScorer, final Bits acceptDocs)
    throws IOException {
      assert termStates.topReaderContext == ReaderUtil.getTopLevelContext(context) : "The top-reader used to create Weight (" + termStates.topReaderContext + ") is not the same as the current reader's top-reader (" + ReaderUtil.getTopLevelContext(context);
      final TermsEnum termsEnum = this.getTermsEnum(context);
      if (termsEnum == null) {
        return null;
      }

      final DocsAndPositionsEnum docs = termsEnum.docsAndPositions(acceptDocs, null);
      final DocsNodesAndPositionsEnum dnpe = new Siren020DocNodAndPosEnum(docs);

      if (nodeLowerBoundConstraint == null) { // if no constraints
        return new NodeTermScorer(this, dnpe, this.createDocScorer(context));
      }
      else { // if constraints, wraps the enum
        return new NodeTermScorer(this,
          new ConstrainedNodesEnum(dnpe, nodeLowerBoundConstraint, nodeUpperBoundConstraint, isNodeLevelConstrained),
          this.createDocScorer(context));
      }
    }

    /**
     * Creates an {@link ExactDocScorer} for this {@link TermWeight}*/
    ExactDocScorer createDocScorer(final AtomicReaderContext context)
        throws IOException {
      return similarity.exactDocScorer(stats, term.field(), context);
    }

    /**
     * Returns a {@link TermsEnum} positioned at this weights Term or null if
     * the term does not exist in the given context
     */
    TermsEnum getTermsEnum(final AtomicReaderContext context) throws IOException {
      final TermState state = termStates.get(context.ord);
      if (state == null) { // term is not present in that reader
        assert this.termNotInReader(context.reader, term.field(), term.bytes()) : "no termstate found but term exists in reader term=" + term;
        return null;
      }
      //System.out.println("LD=" + reader.getLiveDocs() + " set?=" + (reader.getLiveDocs() != null ? reader.getLiveDocs().get(0) : "null"));
      final TermsEnum termsEnum = context.reader.terms(term.field()).iterator(null);
      termsEnum.seekExact(term.bytes(), state);
      return termsEnum;
    }

    private boolean termNotInReader(final IndexReader reader, final String field, final BytesRef bytes) throws IOException {
      // only called from assert
      //System.out.println("TQ.termNotInReader reader=" + reader + " term=" + field + ":" + bytes.utf8ToString());
      return reader.docFreq(field, bytes) == 0;
    }

    @Override
    public Explanation explain(final AtomicReaderContext context, final int doc) throws IOException {
      final Scorer scorer = this.scorer(context, true, false, context.reader.getLiveDocs());
      if (scorer != null) {
        final int newDoc = scorer.advance(doc);
        if (newDoc == doc) {
          final float freq = scorer.freq();
          final ExactDocScorer docScorer = similarity.exactDocScorer(stats, term.field(), context);
          final ComplexExplanation result = new ComplexExplanation();
          result.setDescription("weight("+this.getQuery()+" in "+doc+") [" + similarity.getClass().getSimpleName() + "], result of:");
          final Explanation scoreExplanation = docScorer.explain(doc, new Explanation(freq, "termFreq=" + freq));
          result.addDetail(scoreExplanation);
          result.setValue(scoreExplanation.getValue());
          result.setMatch(true);
          return result;
        }
      }
      return new ComplexExplanation(false, 0.0f, "no matching term");
    }

  }

  /** Constructs a query for the term <code>t</code>. */
  public NodeTermQuery(final Term t) {
    this(t, -1);
  }

  /** Expert: constructs a TermQuery that will use the
   *  provided docFreq instead of looking up the docFreq
   *  against the searcher. */
  public NodeTermQuery(final Term t, final int docFreq) {
    term = t;
    this.docFreq = docFreq;
    perReaderTermState = null;
  }

  /** Expert: constructs a TermQuery that will use the
   *  provided docFreq instead of looking up the docFreq
   *  against the searcher. */
  public NodeTermQuery(final Term t, final TermContext states) {
    assert states != null;
    term = t;
    docFreq = states.docFreq();
    perReaderTermState = states;
  }

  /** Returns the term of this query. */
  public Term getTerm() {
    return term;
  }

  @Override
  public Weight createWeight(final IndexSearcher searcher) throws IOException {
    final ReaderContext context = searcher.getTopReaderContext();
    final TermContext termState;
    if (perReaderTermState == null || perReaderTermState.topReaderContext != context) {
      // make TermQuery single-pass if we don't have a PRTS or if the context differs!
      termState = TermContext.build(context, term, true); // cache term lookups!
    } else {
     // PRTS was pre-build for this IS
     termState = this.perReaderTermState;
    }

    // we must not ignore the given docFreq - if set use the given value (lie)
    if (docFreq != -1)
      termState.setDocFreq(docFreq);

    return new NodeTermWeight(searcher, termState);
  }

  @Override
  public void extractTerms(final Set<Term> terms) {
    terms.add(this.getTerm());
  }

  /** Prints a user-readable version of this query. */
  @Override
  public String toString(final String field) {
    final StringBuffer buffer = new StringBuffer();
    if (!term.field().equals(field)) {
      buffer.append(term.field());
      buffer.append(":");
    }
    buffer.append(term.text());
    buffer.append(ToStringUtils.boost(this.getBoost()));
    return buffer.toString();
  }

  /** Returns true iff <code>o</code> is equal to this. */
  @Override
  public boolean equals(final Object o) {
    if (!(o instanceof NodeTermQuery)) return false;
    final NodeTermQuery other = (NodeTermQuery) o;
    return (this.getBoost() == other.getBoost()) &&
           this.term.equals(other.term);
  }

  /** Returns a hash code value for this object. */
  @Override
  public int hashCode() {
    return Float.floatToIntBits(this.getBoost()) ^ term.hashCode();
  }

}

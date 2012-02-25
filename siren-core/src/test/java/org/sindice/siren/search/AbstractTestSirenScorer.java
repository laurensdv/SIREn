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

import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReader.AtomicReaderContext;
import org.apache.lucene.index.RandomIndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.store.Directory;
import org.junit.After;
import org.junit.Before;
import org.sindice.siren.analysis.AnyURIAnalyzer;
import org.sindice.siren.analysis.AnyURIAnalyzer.URINormalisation;
import org.sindice.siren.analysis.TupleAnalyzer;
import org.sindice.siren.index.DocsAndNodesIterator;
import org.sindice.siren.index.PositionsIterator;
import org.sindice.siren.search.base.NodePositionScorer;
import org.sindice.siren.search.base.NodeScorer;
import org.sindice.siren.search.node.NodeBooleanQuery;
import org.sindice.siren.search.node.NodeBooleanScorer;
import org.sindice.siren.search.node.NodeBooleanClause.Occur;
import org.sindice.siren.search.primitive.NodeTermQuery;
import org.sindice.siren.search.primitive.NodeTermScorer;
import org.sindice.siren.util.SirenTestCase;

public abstract class AbstractTestSirenScorer extends SirenTestCase {

  protected Directory directory;
  protected RandomIndexWriter writer;
  protected IndexReader reader;
  protected IndexSearcher searcher;

  @Override
  @Before
  public void setUp()
  throws Exception {
    super.setUp();

    final AnyURIAnalyzer uriAnalyzer = new AnyURIAnalyzer(TEST_VERSION_CURRENT);
    uriAnalyzer.setUriNormalisation(URINormalisation.FULL);
    final TupleAnalyzer analyzer = new TupleAnalyzer(TEST_VERSION_CURRENT,
      new StandardAnalyzer(TEST_VERSION_CURRENT), uriAnalyzer);

    directory = newDirectory();

    writer = this.newRandomIndexWriter(directory, analyzer);
    reader = this.newIndexReader(writer);
    searcher = newSearcher(reader);
  }

  @Override
  @After
  public void tearDown()
  throws Exception {
    reader.close();
    writer.close();
    directory.close();
    super.tearDown();
  }

  private void refreshReaderAndSearcher() throws IOException {
    reader.close();
    reader = this.newIndexReader(writer);
    searcher = newSearcher(reader);
  }

  protected void assertTo(final AssertFunctor functor, final String[] input,
                        final String[] terms, final int[][] deweyPath)
  throws Exception {
    throw new UnsupportedOperationException();
  }

  public abstract class AssertFunctor {
    public abstract void run(final NodeScorer scorer, int[][] deweyPaths)
    throws Exception;
  }

  public class AssertNodeScorerFunctor
  extends AssertFunctor {

    @Override
    public void run(final NodeScorer scorer, final int[][] deweyPaths)
    throws Exception {
      int index = 0;

      // Iterate over candidate documents
      while (scorer.nextCandidateDocument()) {

        // Iterate over matching nodes
        while (scorer.nextNode()) {
          // check document only in matching nodes
          assertEquals(deweyPaths[index][0], scorer.doc());

          // check node path
          for (int i = 1; i < deweyPaths[index].length - 1; i++) {
            assertEquals(deweyPaths[index][i], scorer.node()[i - 1]);
          }

          if (scorer instanceof NodePositionScorer) {
            final NodePositionScorer pscorer = (NodePositionScorer) scorer;
            while (pscorer.nextPosition()) {
              // check position
              assertEquals(deweyPaths[index][deweyPaths[index].length - 1], pscorer.pos());
              index++;
            }
            assertEquals(PositionsIterator.NO_MORE_POS, pscorer.pos());
          }
        }

        assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());

      }

      assertEquals(DocsAndNodesIterator.NO_MORE_DOC, scorer.doc());

    }
  }

//  protected SirenExactPhraseScorer getExactScorer(final String field,
//                                                  final String[] phraseTerms)
//  throws IOException {
//    final DocsAndPositionsEnum[] tps = new DocsAndPositionsEnum[phraseTerms.length];
//    final int[] positions = new int[phraseTerms.length];
//    for (int i = 0; i < phraseTerms.length; i++) {
//      final DocsAndPositionsEnum p = MultiFields.getTermPositionsEnum(reader,
//        MultiFields.getLiveDocs(reader), DEFAULT_FIELD,
//        new BytesRef(phraseTerms[i]));
//      if (p == null) return null;
//      tps[i] = p;
//      positions[i] = i;
//    }
//
//    return new SirenExactPhraseScorer(new ConstantWeight(), tps, positions,
//      new DefaultSimilarity(), MultiNorms.norms(reader, field));
//  }
//
//  protected SirenExactPhraseScorer getExactScorer(final String field,
//                                                  final int[] positions,
//                                                  final String[] phraseTerms)
//  throws IOException {
//    final DocsAndPositionsEnum[] tps = new DocsAndPositionsEnum[phraseTerms.length];
//    for (int i = 0; i < phraseTerms.length; i++) {
//      final DocsAndPositionsEnum p = MultiFields.getTermPositionsEnum(reader,
//        MultiFields.getLiveDocs(reader), DEFAULT_FIELD,
//        new BytesRef(phraseTerms[i]));
//      if (p == null) return null;
//      tps[i] = p;
//    }
//
//    return new SirenExactPhraseScorer(new ConstantWeight(), tps, positions,
//    new DefaultSimilarity(), MultiNorms.norms(reader, field));
//  }

  protected NodeTermScorer getTermScorer(final String field,
                                         final String term)
  throws IOException {
    final Term t = new Term(field, term);
    final NodeTermQuery termQuery = new NodeTermQuery(t);
    return this.getTermScorer(termQuery);
  }

  protected NodeTermScorer getTermScorer(final String field,
                                         final String term,
                                         final int[] lowerBound,
                                         final int[] upperBound,
                                         final boolean levelConstraint)
  throws IOException {
    final Term t = new Term(field, term);
    final NodeTermQuery termQuery = new NodeTermQuery(t);
    termQuery.setNodeConstraint(lowerBound, upperBound, levelConstraint);
    return this.getTermScorer(termQuery);
  }

  private NodeTermScorer getTermScorer(final NodeTermQuery termQuery)
  throws IOException {
    this.refreshReaderAndSearcher();

    final Weight weight = searcher.createNormalizedWeight(termQuery);
    assertTrue(searcher.getTopReaderContext().isAtomic);
    final AtomicReaderContext context = (AtomicReaderContext) searcher.getTopReaderContext();
    final Scorer ts = weight.scorer(context, true, true, context.reader.getLiveDocs());
    return (NodeTermScorer) ts;
  }

  protected NodeBooleanScorer getConjunctionScorer(final String[] terms)
  throws IOException {
    final NodeBooleanQuery bq = new NodeBooleanQuery();
    for (final String term : terms) {
      final Term t = new Term(DEFAULT_FIELD, term);
      final NodeTermQuery termQuery = new NodeTermQuery(t);
      bq.add(termQuery, Occur.MUST);
    }

    return this.getBooleanScorer(bq);
  }

//  protected SirenConjunctionScorer getConjunctionScorer(final String[][] phraseTerms)
//  throws IOException {
//    final SirenPhraseScorer[] scorers = new SirenPhraseScorer[phraseTerms.length];
//    for (int i = 0; i < phraseTerms.length; i++) {
//      scorers[i] = this.getExactScorer(DEFAULT_FIELD, phraseTerms[i]);
//    }
//    return new SirenConjunctionScorer(scorers[0].getWeight(), scorers, new DefaultSimilarityProvider().coord(scorers.length, scorers.length));
//  }

  protected NodeBooleanScorer getDisjunctionScorer(final String[] terms)
  throws IOException {
    final NodeBooleanQuery bq = new NodeBooleanQuery();
    for (final String term : terms) {
      final Term t = new Term(DEFAULT_FIELD, term);
      final NodeTermQuery termQuery = new NodeTermQuery(t);
      bq.add(termQuery, Occur.SHOULD);
    }

    return this.getBooleanScorer(bq);
  }

  protected NodeBooleanScorer getReqExclScorer(final String reqTerm, final String exclTerm)
  throws IOException {
    final NodeBooleanQuery bq = new NodeBooleanQuery();
    Term t = new Term(DEFAULT_FIELD, reqTerm);
    NodeTermQuery termQuery = new NodeTermQuery(t);
    bq.add(termQuery, Occur.MUST);

    t = new Term(DEFAULT_FIELD, exclTerm);
    termQuery = new NodeTermQuery(t);
    bq.add(termQuery, Occur.MUST_NOT);

    return this.getBooleanScorer(bq);
  }

//  protected SirenReqExclScorer getReqExclScorer(final String[] reqPhrase, final String[] exclPhrase)
//  throws IOException {
//    final SirenExactPhraseScorer reqScorer = this.getExactScorer(DEFAULT_FIELD, reqPhrase);
//    final SirenExactPhraseScorer exclScorer = this.getExactScorer(DEFAULT_FIELD, exclPhrase);
//    return new SirenReqExclScorer(reqScorer, exclScorer);
//  }

  protected NodeBooleanScorer getReqOptScorer(final String reqTerm, final String optTerm)
  throws IOException {
    final NodeBooleanQuery bq = new NodeBooleanQuery();
    Term t = new Term(DEFAULT_FIELD, reqTerm);
    NodeTermQuery termQuery = new NodeTermQuery(t);
    bq.add(termQuery, Occur.MUST);

    t = new Term(DEFAULT_FIELD, optTerm);
    termQuery = new NodeTermQuery(t);
    bq.add(termQuery, Occur.SHOULD);

    return this.getBooleanScorer(bq);
  }

  protected NodeBooleanScorer getBooleanScorer(final String[] reqTerms,
                                               final String[] optTerms,
                                               final String[] exclTerms)
  throws IOException {
    final NodeBooleanQuery bq = this.getBooleanQuery(reqTerms, optTerms, exclTerms);
    return this.getBooleanScorer(bq);
  }

  protected NodeBooleanScorer getBooleanScorer(final String[] reqTerms,
                                               final String[] optTerms,
                                               final String[] exclTerms,
                                               final int[] lowerBound,
                                               final int[] upperBound,
                                               final boolean levelConstraint)
  throws IOException {
    final NodeBooleanQuery bq = this.getBooleanQuery(reqTerms, optTerms, exclTerms);
    bq.setNodeConstraint(lowerBound, upperBound, levelConstraint);
    return this.getBooleanScorer(bq);
  }

  private NodeBooleanScorer getBooleanScorer(final NodeBooleanQuery bq) throws IOException {
    this.refreshReaderAndSearcher();

    final Weight weight = searcher.createNormalizedWeight(bq);
    assertTrue(searcher.getTopReaderContext().isAtomic);
    final AtomicReaderContext context = (AtomicReaderContext) searcher.getTopReaderContext();
    final Scorer s = weight.scorer(context, true, true, context.reader.getLiveDocs());
    return (NodeBooleanScorer) s;
  }

  private NodeBooleanQuery getBooleanQuery(final String[] reqTerms,
                                           final String[] optTerms,
                                           final String[] exclTerms)
  throws IOException {
    final NodeBooleanQuery bq = new NodeBooleanQuery();

    if (reqTerms != null) {
      for (final String term : reqTerms) {
        final Term t = new Term(DEFAULT_FIELD, term);
        final NodeTermQuery termQuery = new NodeTermQuery(t);
        bq.add(termQuery, Occur.MUST);
      }
    }
    if (optTerms != null) {
      for (final String term : optTerms) {
        final Term t = new Term(DEFAULT_FIELD, term);
        final NodeTermQuery termQuery = new NodeTermQuery(t);
        bq.add(termQuery, Occur.SHOULD);
      }
    }
    if (exclTerms != null) {
      for (final String term : exclTerms) {
        final Term t = new Term(DEFAULT_FIELD, term);
        final NodeTermQuery termQuery = new NodeTermQuery(t);
        bq.add(termQuery, Occur.MUST_NOT);
      }
    }
    return bq;
  }

//  protected SirenCellScorer getCellScorer(final int startCell, final int endCell,
//                                          final String[] reqTerms, final String[] optTerms,
//                                          final String[] exclTerms)
//  throws IOException {
//    final SirenCellScorer cscorer = new SirenCellScorer(new ConstantWeight(), startCell, endCell);
//    final NodeScorer bscorer = this.getBooleanScorer(reqTerms, optTerms, exclTerms);
//    cscorer.setScorer(bscorer);
//    return cscorer;
//  }

}

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
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.similarities.DefaultSimilarityProvider;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Bits;
import org.junit.After;
import org.junit.Before;
import org.sindice.siren.analysis.AnyURIAnalyzer;
import org.sindice.siren.analysis.AnyURIAnalyzer.URINormalisation;
import org.sindice.siren.analysis.TupleAnalyzer;
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

  protected abstract void assertTo(final AssertFunctor functor, final String[] input,
                        final String[] terms, final int[][] deweyPath)
  throws Exception;

  protected abstract class AssertFunctor {
    protected abstract void run(final SirenScorer scorer, int[][] deweyPaths)
    throws Exception;
  }

  protected class AssertNextEntityFunctor
  extends AssertFunctor {

    @Override
    protected void run(final SirenScorer scorer, final int[][] deweyPaths)
    throws Exception {
      int index = 0;

      while (scorer.nextDocument()) {
        // check document
        assertEquals(deweyPaths[index][0], scorer.doc());

        while (scorer.nextNode()) {
          // check node path
          for (int i = 1; i < deweyPaths[index].length - 1; i++) {
            assertEquals(deweyPaths[index][i], scorer.node()[i - 1]);
          }

          while (scorer.nextPosition()) {
            // check position
            assertEquals(deweyPaths[index][deweyPaths[index].length - 1], scorer.pos());
            index++;
          }
        }
      }
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

  protected SirenTermScorer getTermScorer(final String field,
                                          final String term)
  throws IOException {
    final Term t = new Term(field, term);
    final SirenTermQuery termQuery = new SirenTermQuery(t);

    this.refreshReaderAndSearcher();

    final Weight weight = searcher.createNormalizedWeight(termQuery);
    assertTrue(searcher.getTopReaderContext().isAtomic);
    final AtomicReaderContext context = (AtomicReaderContext) searcher.getTopReaderContext();
    final Scorer ts = weight.scorer(context, true, true, context.reader.getLiveDocs());
    return (SirenTermScorer) ts;
  }

  /**
   * Return a term scorer which is positioned to the first element, i.e.
   * {@link SirenScorer#next()} has been called one time.
   */
  protected SirenTermScorer getPositionedTermScorer(final String field,
                                                    final String term)
  throws IOException {
    final SirenTermScorer ts = this.getTermScorer(field, term);
    assertNotSame(DocIdSetIterator.NO_MORE_DOCS, ts.nextDocument());
    return ts;
  }

  protected SirenConjunctionScorer getConjunctionScorer(final String[] terms)
  throws IOException {
    final SirenTermScorer[] scorers = new SirenTermScorer[terms.length];
    for (int i = 0; i < terms.length; i++) {
      scorers[i] = this.getTermScorer(DEFAULT_FIELD, terms[i]);
    }
    return new SirenConjunctionScorer(scorers[0].getWeight(), scorers, new DefaultSimilarityProvider().coord(scorers.length, scorers.length));
  }

//  protected SirenConjunctionScorer getConjunctionScorer(final String[][] phraseTerms)
//  throws IOException {
//    final SirenPhraseScorer[] scorers = new SirenPhraseScorer[phraseTerms.length];
//    for (int i = 0; i < phraseTerms.length; i++) {
//      scorers[i] = this.getExactScorer(DEFAULT_FIELD, phraseTerms[i]);
//    }
//    return new SirenConjunctionScorer(scorers[0].getWeight(), scorers, new DefaultSimilarityProvider().coord(scorers.length, scorers.length));
//  }

  protected SirenDisjunctionScorer getDisjunctionScorer(final String[] terms)
  throws IOException {
    final SirenTermScorer[] scorers = new SirenTermScorer[terms.length];
    for (int i = 0; i < terms.length; i++) {
      scorers[i] = this.getTermScorer(DEFAULT_FIELD, terms[i]);
    }
    return new SirenDisjunctionScorer(scorers[0].getWeight(), scorers);
  }

  protected SirenReqExclScorer getReqExclScorer(final String reqTerm, final String exclTerm)
  throws IOException {
    final SirenTermScorer reqScorer = this.getTermScorer(DEFAULT_FIELD, reqTerm);
    final SirenTermScorer exclScorer = this.getTermScorer(DEFAULT_FIELD, exclTerm);
    return new SirenReqExclScorer(reqScorer, exclScorer);
  }

//  protected SirenReqExclScorer getReqExclScorer(final String[] reqPhrase, final String[] exclPhrase)
//  throws IOException {
//    final SirenExactPhraseScorer reqScorer = this.getExactScorer(DEFAULT_FIELD, reqPhrase);
//    final SirenExactPhraseScorer exclScorer = this.getExactScorer(DEFAULT_FIELD, exclPhrase);
//    return new SirenReqExclScorer(reqScorer, exclScorer);
//  }

  protected SirenReqOptScorer getReqOptScorer(final String reqTerm, final String optTerm)
  throws IOException {
    final SirenTermScorer reqScorer = this.getTermScorer(DEFAULT_FIELD, reqTerm);
    final SirenTermScorer optScorer = this.getTermScorer(DEFAULT_FIELD, optTerm);
    return new SirenReqOptScorer(reqScorer, optScorer);
  }

  protected SirenBooleanScorer getBooleanScorer(final String[] reqTerms,
                                                final String[] optTerms,
                                                final String[] exclTerms)
  throws IOException {
    final SirenBooleanScorer scorer = new SirenBooleanScorer(new ConstantWeight());
    if (reqTerms != null) {
      for (final String term : reqTerms)
        scorer.add(this.getTermScorer(DEFAULT_FIELD, term), true, false);
    }
    if (optTerms != null) {
      for (final String term : optTerms)
        scorer.add(this.getTermScorer(DEFAULT_FIELD, term), false, false);
    }
    if (exclTerms != null) {
      for (final String term : exclTerms)
        scorer.add(this.getTermScorer(DEFAULT_FIELD, term), false, true);
    }
    return scorer;
  }

  protected SirenCellScorer getCellScorer(final int startCell, final int endCell,
                                          final String[] reqTerms, final String[] optTerms,
                                          final String[] exclTerms)
  throws IOException {
    final SirenCellScorer cscorer = new SirenCellScorer(new ConstantWeight(), startCell, endCell);
    final SirenBooleanScorer bscorer = this.getBooleanScorer(reqTerms, optTerms, exclTerms);
    cscorer.setScorer(bscorer);
    return cscorer;
  }

  protected class ConstantWeight extends Weight {

    @Override
    public Query getQuery() { return null; }

    @Override
    public Explanation explain(final AtomicReaderContext context, final int doc)
    throws IOException {
      return null;
    }

    @Override
    public float getValueForNormalization()
    throws IOException {
      return 1;
    }

    @Override
    public void normalize(final float norm, final float topLevelBoost) {
    }

    @Override
    public Scorer scorer(final AtomicReaderContext context, final boolean scoreDocsInOrder,
                         final boolean topScorer, final Bits acceptDocs)
    throws IOException {
      return null;
    }
  }

}

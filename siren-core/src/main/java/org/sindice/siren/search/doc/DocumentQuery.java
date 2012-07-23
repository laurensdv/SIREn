/**
 * Copyright (c) 2009-2012 National University of Ireland, Galway. All Rights Reserved.
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
 * @project siren-core
 * @author Campinas Stephane [ 17 Jul 2012 ]
 */
package org.sindice.siren.search.doc;

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.util.Bits;
import org.sindice.siren.search.node.NodeQuery;
import org.sindice.siren.search.node.NodeScorer;

/**
 * Abstract class for SIREn's document queries
 */
public class DocumentQuery extends Query {

  private final NodeQuery nodeQuery;

  protected class DocumentWeight extends Weight {

    private final Weight weight;

    public DocumentWeight(final Weight weight) {
      this.weight = weight;
    }

    @Override
    public Explanation explain(final AtomicReaderContext context, final int doc)
    throws IOException {
      final DocumentScorer dScorer = (DocumentScorer) this.scorer(context, true, false, context.reader().getLiveDocs());
      return dScorer.getWeight().explain(context, doc);
    }

    @Override
    public Query getQuery() {
      return nodeQuery;
    }

    @Override
    public float getValueForNormalization()
    throws IOException {
      return weight.getValueForNormalization();
    }

    @Override
    public void normalize(final float norm, final float topLevelBoost) {
      weight.normalize(norm, topLevelBoost);
    }

    @Override
    public Scorer scorer(final AtomicReaderContext context,
                         final boolean scoreDocsInOrder,
                         final boolean topScorer,
                         final Bits acceptDocs)
    throws IOException {
      final NodeScorer nodeScorer = (NodeScorer) weight.scorer(context,
        scoreDocsInOrder, topScorer, acceptDocs);
      return nodeScorer == null ? null // no match
                                : new DocumentScorer(nodeScorer);
    }

  }

  public DocumentQuery(final NodeQuery nq) {
    this.nodeQuery = nq;
  }

  @Override
  public Weight createWeight(final IndexSearcher searcher)
  throws IOException {
    return new DocumentWeight(nodeQuery.createWeight(searcher));
  }

  @Override
  public Query rewrite(final IndexReader reader)
  throws IOException {
    final Query rewroteQuery = nodeQuery.rewrite(reader);

    return nodeQuery == rewroteQuery ? this
                                 : new DocumentQuery((NodeQuery) rewroteQuery);
  }

  @Override
  public void extractTerms(final Set<Term> terms) {
    nodeQuery.extractTerms(terms);
  }

  @Override
  public String toString(final String field) {
    return "documentQuery(" + nodeQuery.toString(field) + ")";
  }

  @Override
  public int hashCode() {
    return Float.floatToIntBits(this.getBoost()) ^ nodeQuery.hashCode();
  }

}

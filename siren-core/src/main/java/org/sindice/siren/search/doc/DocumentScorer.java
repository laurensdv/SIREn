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
 * @project siren-core
 * @author Stephane Campinas [ 17 Jul 2012 ]
 */
package org.sindice.siren.search.doc;

import java.io.IOException;

import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;

/**
 * The abstract {@link Scorer} class that defines the interface for iterating
 * over an ordered list of documents matching a query.
 * <p>
 * This class provides a bridge between the SIREn and the Lucene APIs.
 */
public abstract class DocumentScorer extends Scorer {

  protected DocumentScorer(final Weight weight) {
    super(weight);
  }

  /**
   * Scores and collects all matching documents.
   *
   * @param collector
   *          The collector to which all matching documents are passed through.
   */
  @Override
  public void score(final Collector collector) throws IOException {
    collector.setScorer(this);
    while (this.nextDoc() != NO_MORE_DOCS) {
      collector.collect(this.docID());
    }
  }

  /**
   * Expert: Collects matching documents in a range. Hook for optimization. Note
   * that {@link #nextDoc()} must be called once before this method is
   * called for the first time.
   *
   * @param collector
   *          The collector to which all matching documents are passed through.
   * @param max
   *          Do not score documents past this.
   * @return true if more matching documents may remain.
   */
  @Override
  public boolean score(Collector collector, int max, int firstDocID)
  throws IOException {
    // firstDocID is ignored since nextDocument() sets 'currentDoc'
    collector.setScorer(this);
    while (this.docID() < max) {
        collector.collect(this.docID());
      if (this.nextDoc() == NO_MORE_DOCS) {
        return false;
      }
    }
    return true;
  }

}

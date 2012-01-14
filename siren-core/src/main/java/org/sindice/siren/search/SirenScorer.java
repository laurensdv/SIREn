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

import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.sindice.siren.index.DocsNodesAndPositionsIterator;

/**
 * The Siren abstract {@link Scorer} class that implements the interface
 * {@link DocsNodesAndPositionsIterator}.
 * <br>
 * Implement {@link #docID()}, {@link #nextDoc()} and {@link #advance(int)} for
 * compatibility with {@link Scorer}.
 */
public abstract class SirenScorer extends Scorer implements DocsNodesAndPositionsIterator {

  protected SirenScorer(final Weight weight) {
    super(weight);
  }

  @Override
  public int docID() {
    return this.doc();
  }

  @Override
  public int nextDoc() throws IOException {
    if (this.nextDocument()) {
      return this.doc();
    }
    return NO_MORE_DOCS;
  }

  @Override
  public int advance(final int target) throws IOException {
    if (this.skipTo(target)) {
      return this.doc();
    }
    return NO_MORE_DOCS;
  }

}

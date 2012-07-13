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
 * @project siren-benchmark
 * @author Renaud Delbru [ 6 Jul 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.benchmark.query.provider;

import java.util.Iterator;

public class StarShapedQuery implements Query, Iterator<FieldQuery> {

  private final FieldQuery[]          starQuery;
  private int                         offset;
  private final int                   nbPairs;
  private StringBuilder               sBuilder      = null;

  public StarShapedQuery(final int nbPairs) {
    starQuery = new FieldQuery[nbPairs];
    this.nbPairs = nbPairs;
  }

  public void addFieldQuery(final FieldQuery fq) {
    if (offset >= nbPairs)
      throw new IllegalArgumentException("You provided too much attribute-value pairs for this star shaped query");
    starQuery[offset++] = fq;
  }

  public String getDescription() {
    return nbPairs + "pairs";
  }

  @Override
  public String toString() {
    if (sBuilder == null)
      sBuilder = new StringBuilder();
    else
      sBuilder.setLength(0);
    for (int i = 0; i < nbPairs; i++) {
      sBuilder.append(starQuery[i].toString() + "\n");
    }
    return sBuilder.toString();
  }

  @Override
  public boolean hasNext() {
    return offset > 0;
  }

  @Override
  public FieldQuery next() {
    return starQuery[--offset];
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

}

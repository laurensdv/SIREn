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

import java.io.IOException;

import org.sindice.siren.benchmark.query.provider.PrimitiveQueryProvider.PrimitiveQuery;

public class AttributeQueryProvider extends QueryProvider {

  private PrimitiveQueryProvider attributeProvider;
  private PrimitiveQueryProvider valueProvider;

  private int counter = 0;

  @Override
  public void setSeed(final int seed) {
    attributeProvider.setSeed(seed);
    valueProvider.setSeed(seed);
  }

  @Override
  public void setNbQueries(final int nbQueries) {
    super.setNbQueries(nbQueries);
    attributeProvider.setNbQueries(nbQueries);
    valueProvider.setNbQueries(nbQueries);
  }

  protected void addAttributeProvider(final PrimitiveQueryProvider attributeProvider) {
    this.attributeProvider = attributeProvider;
  }

  protected void addValueProvider(final PrimitiveQueryProvider valueProvider) {
    this.valueProvider = valueProvider;
  }

  @Override
  public boolean hasNext() {
    return (counter < nbQueries) ? true : false;
  }

  @Override
  public AttributeQuery next() {
    counter++;
    return new AttributeQuery((PrimitiveQuery) attributeProvider.next(),
      (PrimitiveQuery) valueProvider.next());
  }

  @Override
  public void close() throws IOException {
    attributeProvider.close();
    valueProvider.close();
  }

  public static class AttributeQuery implements Query {

    private final PrimitiveQuery attribute;
    private final PrimitiveQuery value;

    public AttributeQuery(final PrimitiveQuery attribute, final PrimitiveQuery value) {
      this.attribute = attribute;
      this.value = value;
    }

    public PrimitiveQuery getAttributeQuery() {
      return this.attribute;
    }

    public PrimitiveQuery getValueQuery() {
      return this.value;
    }

    @Override
    public String toString() {
      final StringBuilder builder = new StringBuilder();
      builder.append("{ attribute: ");
      builder.append(attribute);
      builder.append(", value: ");
      builder.append(value);
      builder.append(" }");
      return builder.toString();
    }

  }

}

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

import org.sindice.siren.benchmark.generator.lexicon.TermLexiconWriter.TermGroups;
import org.sindice.siren.benchmark.query.provider.KeywordQuery.Occur;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class FieldQueryProvider extends QueryProvider {

  private KeywordQueryProvider predicateProvider;
  private TermLexiconQueryProvider valueProvider;

  public FieldQueryProvider(final Occur[] occurs,
                            final TermGroups[] groups)
  throws IOException {
    super(occurs, groups);
  }

  @Override
  public final void setNbQueries(final int nbQueries) {
    this.nbQueries = nbQueries;
    if (predicateProvider != null) this.predicateProvider.setNbQueries(nbQueries);
    if (valueProvider != null) this.valueProvider.setNbQueries(nbQueries);
  }

  public void setPredicateProvider(final KeywordQueryProvider predicateProvider)
  throws IOException {
    this.predicateProvider = predicateProvider;
    this.predicateProvider.setNbQueries(nbQueries);
  }

  public void setValueProvider(final TermLexiconQueryProvider valueProvider) {
    this.valueProvider = valueProvider;
    this.valueProvider.setNbQueries(nbQueries);
  }

  @Override
  public boolean hasNext() {
    return (queryPos < nbQueries) ? true : false;
  }

  @Override
  public Query next() {
    final FieldQuery fq = new FieldQuery();

    queryPos++;
    fq.setFieldName((KeywordQuery) predicateProvider.next());
    fq.setValueQuery(valueProvider.next());

    return fq;
  }

  @Override
  public void remove() {
    throw new NotImplementedException();
  }

  @Override
  public void close()
  throws IOException {
    predicateProvider.close();
    valueProvider.close();
  }

}

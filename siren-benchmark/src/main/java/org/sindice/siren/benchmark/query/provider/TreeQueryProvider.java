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
import java.util.ArrayList;
import java.util.List;

import org.sindice.siren.benchmark.query.provider.AttributeQueryProvider.AttributeQuery;

public class TreeQueryProvider extends QueryProvider {

  private final List<AttributeQueryProvider> rootAttributeProviders = new ArrayList<AttributeQueryProvider>();
  private final List<TreeQueryProvider> ancestorProviders = new ArrayList<TreeQueryProvider>();

  private int counter = 0;

  @Override
  public void setNbQueries(final int nbQueries) {
    super.setNbQueries(nbQueries);
    for (final AttributeQueryProvider attrProvider : rootAttributeProviders) {
      attrProvider.setNbQueries(nbQueries);
    }
    for (final TreeQueryProvider treeProvider : ancestorProviders) {
      treeProvider.setNbQueries(nbQueries);
    }
  }

  protected void addRootAttributeProvider(final AttributeQueryProvider attrProvider) {
    rootAttributeProviders.add(attrProvider);
  }

  protected void addAncestorProvider(final TreeQueryProvider treeProvider) {
    ancestorProviders.add(treeProvider);
  }

  @Override
  public boolean hasNext() {
    return counter < nbQueries;
  }

  @Override
  public TreeQuery next() {
    final TreeQuery treeQuery = new TreeQuery();

    counter++;

    for (final AttributeQueryProvider attrProvider : rootAttributeProviders) {
      treeQuery.addRootAttributeQuery(attrProvider.next());
    }

    for (final TreeQueryProvider treeProvider : ancestorProviders) {
      treeQuery.addAncestorQuery(treeProvider.next());
    }

    return treeQuery;
  }

  @Override
  public void close() throws IOException {
    for (final AttributeQueryProvider attrProvider : rootAttributeProviders) {
      attrProvider.close();
    }
    for (final TreeQueryProvider treeProvider : ancestorProviders) {
      treeProvider.close();
    }
  }

  public static class TreeQuery implements Query {

    private final List<AttributeQuery> root = new ArrayList<AttributeQuery>();
    private final List<TreeQuery> ancestors = new ArrayList<TreeQuery>();

    public void addRootAttributeQuery(final AttributeQuery attributeQuery) {
      this.root.add(attributeQuery);
    }

    public void addAncestorQuery(final TreeQuery ancestorQuery) {
      this.ancestors.add(ancestorQuery);
    }

    public List<AttributeQuery> getRootAttributeQueries() {
      return root;
    }

    public List<TreeQuery> getAncestorQueries() {
      return ancestors;
    }

    @Override
    public String toString() {
      final StringBuilder builder = new StringBuilder();
      builder.append("{ root: ");
      builder.append(root);
      builder.append(", ancestors: ");
      builder.append(ancestors);
      builder.append(" }");
      return builder.toString();
    }

  }

}

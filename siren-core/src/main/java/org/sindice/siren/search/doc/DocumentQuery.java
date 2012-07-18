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

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Weight;
import org.sindice.siren.search.node.NodeQuery;

/**
 * Abstract class for SIREn's document queries
 */
public class DocumentQuery extends Query {

  private final NodeQuery nodeQuery;

  public DocumentQuery(NodeQuery nq) {
    this.nodeQuery = nq;
  }

  @Override
  public Weight createWeight(IndexSearcher searcher)
  throws IOException {
    return nodeQuery.createWeight(searcher);
  }

  @Override
  public Query rewrite(IndexReader reader)
  throws IOException {
    return nodeQuery.rewrite(reader);
  }

  @Override
  public void extractTerms(Set<Term> terms) {
    nodeQuery.extractTerms(terms);
  }

  @Override
  public String toString(String field) {
    return nodeQuery.toString(field);
  }

}

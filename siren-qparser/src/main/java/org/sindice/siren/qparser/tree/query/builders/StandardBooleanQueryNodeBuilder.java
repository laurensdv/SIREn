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
package org.sindice.siren.qparser.tree.query.builders;

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.similarities.Similarity;
import org.sindice.siren.search.node.NodeQuery;

/**
 * This builder does the same as the {@link NodeBooleanQueryNodeBuilder}, but this
 * considers if the built {@link BooleanQuery} should have its coord disabled or
 * not. <br/>
 *
 * @see NodeBooleanQueryNodeBuilder
 * @see BooleanQuery
 * @see Similarity#coord(int, int)
 */
public class StandardBooleanQueryNodeBuilder implements NodeQueryBuilder {

  public StandardBooleanQueryNodeBuilder() {
    // empty constructor
  }

  public NodeQuery build(final QueryNode queryNode)
  throws QueryNodeException {
    final NodeBooleanQueryNodeBuilder bqNodeBuilder = new NodeBooleanQueryNodeBuilder();

    return bqNodeBuilder.build(queryNode);
  }

}

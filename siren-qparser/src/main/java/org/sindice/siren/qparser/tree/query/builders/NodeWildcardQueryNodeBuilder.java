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

import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.standard.builders.WildcardQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.nodes.WildcardQueryNode;
import org.sindice.siren.qparser.tree.query.processors.MultiNodeTermRewriteMethodProcessor;
import org.sindice.siren.search.node.MultiNodeTermQuery;
import org.sindice.siren.search.node.NodeWildcardQuery;

/**
 * Builds a {@link NodeWildcardQuery} object from a {@link WildcardQueryNode}
 * object.
 * <p>
 * Code taken from {@link WildcardQueryNodeBuilder} and adapted to SIREn
 */
public class NodeWildcardQueryNodeBuilder implements NodeQueryBuilder {

  public NodeWildcardQueryNodeBuilder() {
    // empty constructor
  }

  public NodeWildcardQuery build(QueryNode queryNode) throws QueryNodeException {
    WildcardQueryNode wildcardNode = (WildcardQueryNode) queryNode;

    NodeWildcardQuery q = new NodeWildcardQuery(new Term(wildcardNode.getFieldAsString(),
                                                         wildcardNode.getTextAsString()));

    MultiNodeTermQuery.RewriteMethod method = (MultiNodeTermQuery.RewriteMethod)queryNode.getTag(MultiNodeTermRewriteMethodProcessor.TAG_ID);
    if (method != null) {
      q.setRewriteMethod(method);
    }
    return q;
  }

}

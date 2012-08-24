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

import java.util.List;

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.builders.QueryTreeBuilder;
import org.apache.lucene.queryparser.flexible.core.nodes.BooleanQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.ModifierQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.search.Query;
import org.sindice.siren.search.node.NodeBooleanClause;
import org.sindice.siren.search.node.NodeBooleanQuery;
import org.sindice.siren.search.node.NodeQuery;

/**
 * Builds a {@link NodeBooleanQuery} object from a {@link BooleanQueryNode} object.
 * Every children in the {@link BooleanQueryNode} object must be already tagged
 * using {@link QueryTreeBuilder#QUERY_TREE_BUILDER_TAGID} with a {@link Query}
 * object. <br/>
 * <br/>
 * It takes in consideration if the children is a {@link ModifierQueryNode} to
 * define the {@link NodeBooleanClause}.
 */
public class NodeBooleanQueryNodeBuilder implements NodeQueryBuilder {

  public NodeBooleanQueryNodeBuilder() {
    // empty constructor
  }

  public NodeQuery build(final QueryNode queryNode)
  throws QueryNodeException {

    final BooleanQueryNode booleanNode = (BooleanQueryNode) queryNode;
    final List<QueryNode> children = booleanNode.getChildren();
    final NodeBooleanQuery bq = new NodeBooleanQuery();

    if (children == null) {
      return bq; // return empty boolean query
    }

    // If more than one child, wrap them into a NodeBooleanQuery
    if (children.size() > 1) {

      for (final QueryNode child : children) {
        final Object obj = child.getTag(QueryTreeBuilder.QUERY_TREE_BUILDER_TAGID);
        if (obj != null) {
          if (obj instanceof NodeQuery) {
            bq.add((NodeQuery) obj,
              NodeBooleanQueryNodeBuilder.getModifierValue(child));
          }
          else {
            throw new QueryNodeException(new Error("Expected NodeQuery: got '" +
            	obj.getClass().getCanonicalName() + "'"));
          }
        }
      }
      return bq;
    }
    // If only one child, return it directly
    else {
      final Object obj = children.get(0).getTag(QueryTreeBuilder.QUERY_TREE_BUILDER_TAGID);
      if (obj != null) {
        if (obj instanceof NodeQuery) {
          return (NodeQuery) obj;
        }
        else {
          throw new QueryNodeException(new Error("Non NodeQuery query '" +
            obj.getClass().getCanonicalName() + "' received"));
        }
      }
      return bq; // return empty boolean query
    }
  }

  private static NodeBooleanClause.Occur getModifierValue(final QueryNode node)
      throws QueryNodeException {

    if (node instanceof ModifierQueryNode) {
      final ModifierQueryNode mNode = ((ModifierQueryNode) node);
      switch (mNode.getModifier()) {

      case MOD_REQ:
        return NodeBooleanClause.Occur.MUST;

      case MOD_NOT:
        return NodeBooleanClause.Occur.MUST_NOT;

      case MOD_NONE:
        return NodeBooleanClause.Occur.SHOULD;

      }

    }

    return NodeBooleanClause.Occur.SHOULD;

  }

}

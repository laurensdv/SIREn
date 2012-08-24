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
import org.apache.lucene.queryparser.flexible.core.nodes.FieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.TokenizedPhraseQueryNode;
import org.apache.lucene.queryparser.flexible.standard.builders.PhraseQueryNodeBuilder;
import org.sindice.siren.search.node.NodePhraseQuery;
import org.sindice.siren.search.node.NodeTermQuery;

/**
 * Builds a {@link NodePhraseQuery} object from a {@link TokenizedPhraseQueryNode}
 * object
 * <p>
 * Code taken from {@link PhraseQueryNodeBuilder} and adapted for SIREn
 */
public class NodePhraseQueryNodeBuilder implements NodeQueryBuilder {

  public NodePhraseQueryNodeBuilder() {
    // empty constructor
  }

  public NodePhraseQuery build(QueryNode queryNode) throws QueryNodeException {
    final TokenizedPhraseQueryNode phraseNode = (TokenizedPhraseQueryNode) queryNode;

    final NodePhraseQuery phraseQuery = new NodePhraseQuery();
    final List<QueryNode> children = phraseNode.getChildren();

    if (children != null) {
      
      for (QueryNode child : children) {
        NodeTermQuery termQuery = (NodeTermQuery) child
            .getTag(QueryTreeBuilder.QUERY_TREE_BUILDER_TAGID);
        FieldQueryNode termNode = (FieldQueryNode) child;
        
        phraseQuery.add(termQuery.getTerm(), termNode.getPositionIncrement());

      }

    }
    return phraseQuery;

  }

}

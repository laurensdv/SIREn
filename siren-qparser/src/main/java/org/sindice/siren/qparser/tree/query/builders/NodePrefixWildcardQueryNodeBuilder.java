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
import org.apache.lucene.queryparser.flexible.standard.builders.PrefixWildcardQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.nodes.PrefixWildcardQueryNode;
import org.sindice.siren.qparser.tree.query.processors.MultiNodeTermRewriteMethodProcessor;
import org.sindice.siren.search.node.MultiNodeTermQuery;
import org.sindice.siren.search.node.NodePrefixQuery;

/**
 * Builds a {@link NodePrefixQuery} object from a {@link PrefixWildcardQueryNode}
 * object.
 * <p>
 * Code taken from {@link PrefixWildcardQueryNodeBuilder} and adapted to SIREn
 */
public class NodePrefixWildcardQueryNodeBuilder implements NodeQueryBuilder {

  public NodePrefixWildcardQueryNodeBuilder() {
    // empty constructor
  }

  public NodePrefixQuery build(QueryNode queryNode) throws QueryNodeException {
    PrefixWildcardQueryNode wildcardNode = (PrefixWildcardQueryNode) queryNode;
    
    final String text = wildcardNode.getText().subSequence(0, wildcardNode.getText().length() - 1).toString();
    NodePrefixQuery q = new NodePrefixQuery(new Term(wildcardNode.getFieldAsString(), text));
    
    MultiNodeTermQuery.RewriteMethod method = (MultiNodeTermQuery.RewriteMethod)queryNode.getTag(MultiNodeTermRewriteMethodProcessor.TAG_ID);
    if (method != null) {
      q.setRewriteMethod(method);
    }
        
    return q;
  }

}

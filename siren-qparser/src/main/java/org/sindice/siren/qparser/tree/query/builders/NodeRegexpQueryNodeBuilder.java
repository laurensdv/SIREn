package org.sindice.siren.qparser.tree.query.builders;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.standard.builders.RegexpQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.builders.StandardQueryBuilder;
import org.apache.lucene.queryparser.flexible.standard.nodes.RegexpQueryNode;
import org.sindice.siren.qparser.tree.query.processors.MultiNodeTermRewriteMethodProcessor;
import org.sindice.siren.search.node.MultiNodeTermQuery;
import org.sindice.siren.search.node.NodeQuery;
import org.sindice.siren.search.node.NodeRegexpQuery;

/**
 * Builds a {@link NodeRegexpQuery} object from a {@link RegexpQueryNode} object.
 *
 * <p> Code taken from {@link RegexpQueryNodeBuilder} and adapted for SIREn.
 */
public class NodeRegexpQueryNodeBuilder implements StandardQueryBuilder {

  public NodeRegexpQueryNodeBuilder() {
    // empty constructor
  }

  public NodeQuery build(QueryNode queryNode) throws QueryNodeException {
    RegexpQueryNode regexpNode = (RegexpQueryNode) queryNode;

    NodeRegexpQuery q = new NodeRegexpQuery(new Term(regexpNode.getFieldAsString(),
        regexpNode.textToBytesRef()));

    MultiNodeTermQuery.RewriteMethod method = (MultiNodeTermQuery.RewriteMethod) queryNode
        .getTag(MultiNodeTermRewriteMethodProcessor.TAG_ID);
    if (method != null) {
      q.setRewriteMethod(method);
    }

    return q;
  }

}

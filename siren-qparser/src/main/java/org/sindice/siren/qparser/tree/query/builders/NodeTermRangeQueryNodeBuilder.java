package org.sindice.siren.qparser.tree.query.builders;

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.nodes.FieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.core.util.StringUtils;
import org.apache.lucene.queryparser.flexible.standard.nodes.TermRangeQueryNode;
import org.sindice.siren.qparser.tree.query.processors.MultiNodeTermRewriteMethodProcessor;
import org.sindice.siren.search.node.MultiNodeTermQuery;
import org.sindice.siren.search.node.NodeTermRangeQuery;

public class NodeTermRangeQueryNodeBuilder
implements NodeQueryBuilder {

  public NodeTermRangeQueryNodeBuilder() {
    // empty constructor
  }

  public NodeTermRangeQuery build(QueryNode queryNode) throws QueryNodeException {
    TermRangeQueryNode rangeNode = (TermRangeQueryNode) queryNode;
    FieldQueryNode upper = rangeNode.getUpperBound();
    FieldQueryNode lower = rangeNode.getLowerBound();

    String field = StringUtils.toString(rangeNode.getField());
    String lowerText = lower.getTextAsString();
    String upperText = upper.getTextAsString();
    
    if (lowerText.length() == 0) {
      lowerText = null;
    }
    
    if (upperText.length() == 0) {
      upperText = null;
    }
    
    NodeTermRangeQuery rangeQuery = NodeTermRangeQuery.newStringRange(field, lowerText, upperText, rangeNode
        .isLowerInclusive(), rangeNode.isUpperInclusive());
    
    MultiNodeTermQuery.RewriteMethod method = (MultiNodeTermQuery.RewriteMethod) queryNode
        .getTag(MultiNodeTermRewriteMethodProcessor.TAG_ID);
    if (method != null) {
      rangeQuery.setRewriteMethod(method);
    }
    return rangeQuery;
  }

}

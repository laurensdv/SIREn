package org.sindice.siren.qparser.tree.query.processors;

import java.util.List;

import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.core.processors.QueryNodeProcessorImpl;
import org.apache.lucene.queryparser.flexible.standard.nodes.AbstractRangeQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.RegexpQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.WildcardQueryNode;
import org.apache.lucene.queryparser.flexible.standard.processors.MultiTermRewriteMethodProcessor;
import org.sindice.siren.qparser.tree.TreeQueryParser.TreeConfigurationKeys;
import org.sindice.siren.search.node.MultiNodeTermQuery;

/**
 * Copied From {@link MultiTermRewriteMethodProcessor} for the SIREn use case.
 * <p>
 * This processor instates the default
 * {@link org.sindice.siren.search.node.MultiNodeTermQuery.RewriteMethod},
 * {@link MultiNodeTermQuery#CONSTANT_SCORE_AUTO_REWRITE_DEFAULT}, for multi-term
 * query nodes.
 */
public class MultiNodeTermRewriteMethodProcessor extends QueryNodeProcessorImpl {

  public static final String TAG_ID = "MultiNodeTermRewriteMethodConfiguration";

  @Override
  protected QueryNode postProcessNode(QueryNode node) {

    // set setMultiTermRewriteMethod for WildcardQueryNode and
    // PrefixWildcardQueryNode
    if (node instanceof WildcardQueryNode ||
        node instanceof AbstractRangeQueryNode ||
        node instanceof RegexpQueryNode) {

      MultiNodeTermQuery.RewriteMethod rewriteMethod = getQueryConfigHandler().get(TreeConfigurationKeys.MULTI_NODE_TERM_REWRITE_METHOD);

      if (rewriteMethod == null) {
        // This should not happen, this configuration is set in the
        // StandardQueryConfigHandler
        throw new IllegalArgumentException(
            "TreeQueryParser.TreeConfigurationKeys.MULTI_NODE_TERM_REWRITE_METHOD should be set on the QueryConfigHandler");
      }

      // use a TAG to take the value to the Builder
      node.setTag(MultiNodeTermRewriteMethodProcessor.TAG_ID, rewriteMethod);

    }

    return node;
  }

  @Override
  protected QueryNode preProcessNode(QueryNode node) {
    return node;
  }

  @Override
  protected List<QueryNode> setChildrenOrder(List<QueryNode> children) {
    return children;
  }
}

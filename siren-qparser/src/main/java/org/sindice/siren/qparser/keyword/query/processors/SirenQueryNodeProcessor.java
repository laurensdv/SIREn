package org.sindice.siren.qparser.keyword.query.processors;

import java.util.List;

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.nodes.FieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.FuzzyQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.SlopQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.TokenizedPhraseQueryNode;
import org.apache.lucene.queryparser.flexible.core.processors.QueryNodeProcessorImpl;
import org.apache.lucene.queryparser.flexible.standard.nodes.MultiPhraseQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.PrefixWildcardQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.RegexpQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.TermRangeQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.WildcardQueryNode;
import org.sindice.siren.qparser.keyword.nodes.SirenQueryNode;
import org.sindice.siren.qparser.tree.query.nodes.NodeNumericRangeQueryNode;

public class SirenQueryNodeProcessor
extends QueryNodeProcessorImpl {

  @Override
  protected QueryNode preProcessNode(QueryNode node)
  throws QueryNodeException {
    return node;
  }

  @Override
  protected QueryNode postProcessNode(QueryNode node)
  throws QueryNodeException {
    if (node instanceof FuzzyQueryNode ||
        node instanceof WildcardQueryNode ||
        node instanceof TokenizedPhraseQueryNode ||
        node instanceof PrefixWildcardQueryNode ||
        node instanceof SlopQueryNode ||
        node instanceof MultiPhraseQueryNode ||
        node instanceof FieldQueryNode ||
        node instanceof NodeNumericRangeQueryNode ||
        node instanceof TermRangeQueryNode ||
        node instanceof RegexpQueryNode) {
      return new SirenQueryNode(node);
    }
    return node;
  }

  @Override
  protected List<QueryNode> setChildrenOrder(List<QueryNode> children)
  throws QueryNodeException {
    return children;
  }

}

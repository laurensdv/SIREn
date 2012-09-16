package org.sindice.siren.qparser.keyword.query.builders;

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.builders.QueryTreeBuilder;
import org.apache.lucene.queryparser.flexible.core.messages.QueryParserMessages;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.messages.MessageImpl;
import org.apache.lucene.queryparser.flexible.standard.builders.StandardQueryBuilder;
import org.apache.lucene.search.Query;
import org.sindice.siren.qparser.keyword.nodes.SirenQueryNode;
import org.sindice.siren.search.doc.DocumentQuery;
import org.sindice.siren.search.node.NodeQuery;

public class SirenQueryNodeBuilder
implements StandardQueryBuilder {

  public SirenQueryNodeBuilder() {
  }

  @Override
  public Query build(QueryNode queryNode)
  throws QueryNodeException {
    SirenQueryNode sqn = (SirenQueryNode) queryNode;
    final Query q = (Query) sqn.getQueryNode().getTag(QueryTreeBuilder.QUERY_TREE_BUILDER_TAGID);

    if (q == null) {
      return null;
    }
    if (q instanceof NodeQuery) {
      return new DocumentQuery((NodeQuery) q);
    }
    throw new QueryNodeException(new MessageImpl(QueryParserMessages.EMPTY_MESSAGE));
  }

}

package org.sindice.siren.qparser.keyword.nodes;

import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNodeImpl;
import org.apache.lucene.queryparser.flexible.core.parser.EscapeQuerySyntax;
import org.sindice.siren.search.doc.DocumentQuery;

public class SirenQueryNode
extends QueryNodeImpl {

  private final QueryNode qn;

  /**
   * @param qn The query node to be wrapped in a {@link DocumentQuery}
   */
  public SirenQueryNode(final QueryNode qn) {
    this.qn = qn;

    setLeaf(false);
    allocate();
    add(qn);
  }

  public QueryNode getQueryNode() {
    return qn;
  }

  @Override
  public CharSequence toQueryString(EscapeQuerySyntax escapeSyntaxParser) {
    return qn.toQueryString(escapeSyntaxParser);
  }

  @Override
  public String toString() {
    return "<siren>\n" + qn.toString() + "\n</siren>";
  }

}

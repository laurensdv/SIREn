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
/**
 * @project siren-qparser_rdelbru
 * @author Campinas Stephane [ 14 Oct 2011 ]
 * @link stephane.campinas@deri.org
 */
package org.sindice.siren.qparser.tree.query.processors;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeParseException;
import org.apache.lucene.queryparser.flexible.core.config.QueryConfigHandler;
import org.apache.lucene.queryparser.flexible.core.messages.QueryParserMessages;
import org.apache.lucene.queryparser.flexible.core.nodes.FieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.RangeQueryNode;
import org.apache.lucene.queryparser.flexible.core.processors.QueryNodeProcessorImpl;
import org.apache.lucene.queryparser.flexible.messages.MessageImpl;
import org.apache.lucene.queryparser.flexible.standard.nodes.NumericQueryNode;
import org.apache.lucene.queryparser.flexible.standard.processors.NumericQueryNodeProcessor;
import org.sindice.siren.analysis.NumericAnalyzer;
import org.sindice.siren.qparser.tree.TreeQueryParser.TreeConfigurationKeys;
import org.sindice.siren.qparser.tree.query.nodes.NodeNumericRangeQueryNode;
import org.sindice.siren.util.XSDPrimitiveTypeParser;

/**
 * Copied from {@link NumericQueryNodeProcessor} for the Siren use case. Since
 * the datatype of the value is set with ^^, there is no need for a field query.
 */
public class NodeNumericQueryNodeProcessor
extends QueryNodeProcessorImpl {

  /**
   * Constructs an empty {@link NodeNumericQueryNodeProcessor} object.
   */
  public NodeNumericQueryNodeProcessor() {
    // empty constructor
  }

  @Override
  protected QueryNode postProcessNode(QueryNode node)
  throws QueryNodeException {

    if (node instanceof FieldQueryNode &&
        !(node.getParent() instanceof RangeQueryNode)) {
      QueryConfigHandler config = getQueryConfigHandler();

      if (config != null) {
        FieldQueryNode fieldNode = (FieldQueryNode) node;

        NumericAnalyzer na = config
        .get(TreeConfigurationKeys.NUMERIC_ANALYZERS);

        if (na != null) {
          final String text = fieldNode.getTextAsString();
          Number number = null;
          try {
            final StringReader textReader = new StringReader(fieldNode.getTextAsString());
            switch (na.getNumericType()) {
              case LONG:
                number = XSDPrimitiveTypeParser.parseLong(textReader);
                break;
              case INT:
                number = XSDPrimitiveTypeParser.parseInt(textReader);
                break;
              case DOUBLE:
                number = XSDPrimitiveTypeParser.parseDouble(textReader);
                break;
              case FLOAT:
                number = XSDPrimitiveTypeParser.parseFloat(textReader);
            }
          } catch (NumberFormatException e) {
            throw new QueryNodeParseException(new MessageImpl(QueryParserMessages.COULD_NOT_PARSE_NUMBER, text), e);
          } catch (IOException e) {
            throw new QueryNodeParseException(new MessageImpl(QueryParserMessages.COULD_NOT_PARSE_NUMBER, text), e);
          }

          NumericQueryNode lowerNode = new NumericQueryNode(fieldNode.getField(), number, null);
          NumericQueryNode upperNode = new NumericQueryNode(fieldNode.getField(), number, null);

          return new NodeNumericRangeQueryNode(lowerNode, upperNode, true, true, na);
        }
      }
    }
    return node;

  }

  @Override
  protected QueryNode preProcessNode(QueryNode node)
  throws QueryNodeException {
    return node;
  }

  @Override
  protected List<QueryNode> setChildrenOrder(List<QueryNode> children)
  throws QueryNodeException {
    return children;
  }

}

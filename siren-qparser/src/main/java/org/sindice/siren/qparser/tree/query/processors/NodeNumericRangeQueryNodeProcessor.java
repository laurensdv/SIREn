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
 * @author Campinas Stephane [ 13 Oct 2011 ]
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
import org.apache.lucene.queryparser.flexible.core.processors.QueryNodeProcessorImpl;
import org.apache.lucene.queryparser.flexible.messages.MessageImpl;
import org.apache.lucene.queryparser.flexible.standard.nodes.NumericQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.TermRangeQueryNode;
import org.apache.lucene.queryparser.flexible.standard.processors.NumericRangeQueryNodeProcessor;
import org.sindice.siren.analysis.NumericAnalyzer;
import org.sindice.siren.qparser.tree.TreeQueryParser.TreeConfigurationKeys;
import org.sindice.siren.qparser.tree.query.nodes.NodeNumericRangeQueryNode;
import org.sindice.siren.util.XSDPrimitiveTypeParser;

/**
 * Class copied from {@link NumericRangeQueryNodeProcessor} for the SIREn use
 * case.
 */
public class NodeNumericRangeQueryNodeProcessor
extends QueryNodeProcessorImpl {

  /**
   * Constructs an empty {@link NodeNumericRangeQueryNodeProcessor} object.
   */
  public NodeNumericRangeQueryNodeProcessor() {
    // empty constructor
  }

  @Override
  protected QueryNode postProcessNode(QueryNode node)
  throws QueryNodeException {

    if (node instanceof TermRangeQueryNode) {
      QueryConfigHandler config = getQueryConfigHandler();

      if (config != null) {
        TermRangeQueryNode termRangeNode = (TermRangeQueryNode) node;
        NumericAnalyzer na = config
        .get(TreeConfigurationKeys.NUMERIC_ANALYZERS);

        if (na != null) {

          FieldQueryNode lower = termRangeNode.getLowerBound();
          FieldQueryNode upper = termRangeNode.getUpperBound();

          String lowerText = lower.getTextAsString();
          String upperText = upper.getTextAsString();

          Number lowerNumber = null, upperNumber = null;
          try {
            final StringReader lowerReader = new StringReader(lowerText);
            final StringReader upperReader = new StringReader(upperText);

            switch (na.getNumericType()) {
              case LONG:
                lowerNumber = (lowerText.length() == 0) ? Long.MIN_VALUE : XSDPrimitiveTypeParser
                .parseLong(lowerReader);
                upperNumber = (upperText.length() == 0) ? Long.MAX_VALUE : XSDPrimitiveTypeParser
                .parseLong(upperReader);
                break;
              case INT:
                lowerNumber = (lowerText.length() == 0) ? Integer.MIN_VALUE : XSDPrimitiveTypeParser
                .parseInt(lowerReader);
                upperNumber = (upperText.length() == 0) ? Integer.MAX_VALUE : XSDPrimitiveTypeParser
                .parseInt(upperReader);
                break;
              case DOUBLE:
                lowerNumber = (lowerText.length() == 0) ? Double.MIN_VALUE : XSDPrimitiveTypeParser
                .parseDouble(lowerReader);
                upperNumber = (upperText.length() == 0) ? Double.MAX_VALUE : XSDPrimitiveTypeParser
                .parseDouble(upperReader);
                break;
              case FLOAT:
                lowerNumber = (lowerText.length() == 0) ? Float.MIN_VALUE : XSDPrimitiveTypeParser
                .parseFloat(lowerReader);
                upperNumber = (upperText.length() == 0) ? Float.MAX_VALUE : XSDPrimitiveTypeParser
                .parseFloat(upperReader);
                break;
              default:
                throw new QueryNodeParseException(new MessageImpl(QueryParserMessages.UNSUPPORTED_NUMERIC_DATA_TYPE, na
                .getNumericType()));
            }
          } catch (NumberFormatException e) {
            throw new QueryNodeParseException(new MessageImpl(QueryParserMessages.COULD_NOT_PARSE_NUMBER, lower
            .getTextAsString(), upper.getTextAsString()), e);
          } catch (IOException e) {
            throw new QueryNodeParseException(new MessageImpl(QueryParserMessages.COULD_NOT_PARSE_NUMBER, lower
            .getTextAsString(), upper.getTextAsString()), e);
          }

          NumericQueryNode lowerNode = new NumericQueryNode(termRangeNode.getField(), lowerNumber, null);
          NumericQueryNode upperNode = new NumericQueryNode(termRangeNode.getField(), upperNumber, null);

          boolean lowerInclusive = termRangeNode.isLowerInclusive();
          boolean upperInclusive = termRangeNode.isUpperInclusive();

          return new NodeNumericRangeQueryNode(lowerNode, upperNode, lowerInclusive, upperInclusive, na);

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

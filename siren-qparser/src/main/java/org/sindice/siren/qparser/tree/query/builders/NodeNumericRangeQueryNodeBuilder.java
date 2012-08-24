/**
 * Copyright (c) 2009-2011 Sindice Limited. All Rights Reserved. Project and
 * contact information: http://www.siren.sindice.com/ This file is part of the
 * SIREn project. SIREn is a free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version. SIREn is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details. You should have received a
 * copy of the GNU Affero General Public License along with SIREn. If not, see
 * <http://www.gnu.org/licenses/>.
 */
/**
 * @project siren-qparser_rdelbru
 * @author Campinas Stephane [ 10 Oct 2011 ]
 * @link stephane.campinas@deri.org
 */
package org.sindice.siren.qparser.tree.query.builders;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.FieldType.NumericType;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.messages.QueryParserMessages;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.messages.MessageImpl;
import org.apache.lucene.queryparser.flexible.standard.builders.NumericRangeQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.config.NumericConfig;
import org.apache.lucene.queryparser.flexible.standard.nodes.NumericQueryNode;
import org.sindice.siren.analysis.NumericAnalyzer;
import org.sindice.siren.qparser.tree.query.nodes.NodeNumericRangeQueryNode;
import org.sindice.siren.search.node.NodeNumericRangeQuery;

/**
 * Class copied from {@link NumericRangeQueryNodeBuilder} for the Siren use case:
 * in Siren, we use an {@link Analyzer} instead of a {@link NumericConfig}
 * configuration object.
 */
public class NodeNumericRangeQueryNodeBuilder implements NodeQueryBuilder {

  /**
   * Constructs a {@link NodeNumericRangeQueryNodeBuilder} object.
   */
  public NodeNumericRangeQueryNodeBuilder() {
    // empty constructor
  }

  public NodeNumericRangeQuery<? extends Number> build(QueryNode queryNode)
  throws QueryNodeException {
    NodeNumericRangeQueryNode numericRangeNode = (NodeNumericRangeQueryNode) queryNode;

    NumericQueryNode lowerNumericNode = numericRangeNode.getLowerBound();
    NumericQueryNode upperNumericNode = numericRangeNode.getUpperBound();

    final Number lowerNumber, upperNumber;

    if (lowerNumericNode != null) {
      lowerNumber = lowerNumericNode.getValue();
    }
    else {
      lowerNumber = null;
    }

    if (upperNumericNode != null) {
      upperNumber = upperNumericNode.getValue();
    }
    else {
      upperNumber = null;
    }

    NumericAnalyzer numericAnalyzer = numericRangeNode.getNumericAnalyzer();
    NumericType numberType = numericAnalyzer.getNumericType();
    String field = numericRangeNode.getField().toString();
    boolean minInclusive = numericRangeNode.isLowerInclusive();
    boolean maxInclusive = numericRangeNode.isUpperInclusive();
    int precisionStep = numericAnalyzer.getPrecisionStep();

    switch (numberType) {

      case LONG:
        return NodeNumericRangeQuery.newLongRange(field, precisionStep,
          (Long) lowerNumber, (Long) upperNumber, minInclusive, maxInclusive);

      case INT:
        return NodeNumericRangeQuery.newIntRange(field, precisionStep,
          (Integer) lowerNumber, (Integer) upperNumber, minInclusive,
          maxInclusive);

      case FLOAT:
        return NodeNumericRangeQuery.newFloatRange(field, precisionStep,
          (Float) lowerNumber, (Float) upperNumber, minInclusive, maxInclusive);

      case DOUBLE:
        return NodeNumericRangeQuery.newDoubleRange(field, precisionStep,
          (Double) lowerNumber, (Double) upperNumber, minInclusive,
          maxInclusive);

      default:
        throw new QueryNodeException(new MessageImpl(QueryParserMessages.UNSUPPORTED_NUMERIC_DATA_TYPE, numberType));

    }
  }

}

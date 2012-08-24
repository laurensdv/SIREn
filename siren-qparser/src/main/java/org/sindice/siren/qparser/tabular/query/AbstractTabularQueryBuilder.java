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
 * @project siren
 * @author Renaud Delbru [ 29 Oct 2010 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2010 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.qparser.tabular.query;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.Operator;
import org.apache.lucene.util.Version;
import org.sindice.siren.analysis.NumericAnalyzer;
import org.sindice.siren.qparser.tabular.query.model.LiteralPattern;
import org.sindice.siren.qparser.tabular.query.model.VisitorAdaptor;
import org.sindice.siren.qparser.tree.QueryBuilderException;
import org.sindice.siren.qparser.tree.TreeQueryParser;

/**
 * The visitor for translating the AST into a Siren Tabular Query.
 */
public abstract class AbstractTabularQueryBuilder extends VisitorAdaptor {

  /**
   * Analyzer used on a {@link LiteralPattern}, in the case of a numeric query.
   */
  private final WhitespaceAnalyzer wsAnalyzer;

  /**
   * The default operator to use in the inner parsers
   */
  StandardQueryConfigHandler.Operator defaultOp = StandardQueryConfigHandler.Operator.AND;

  /**
   * Exception handling during building a query
   */
  private QueryBuilderException queryException = null;

  public AbstractTabularQueryBuilder(final Version matchVersion) {
    this.wsAnalyzer = new WhitespaceAnalyzer(matchVersion);
  }

  public boolean hasError() {
    return queryException != null &&
      queryException.getError() != QueryBuilderException.Error.NO_ERROR;
  }

  public String getErrorDescription() {
    return queryException.toString();
  }

  public void setDefaultOperator(final Operator op) {
    defaultOp = op;
  }

  /**
   * Convert the given exception into a {@link QueryBuilderException} and inform
   * the query builder.
   */
  protected void createQueryException(final Exception e) {
    if (queryException == null) {
      String message = null;
      if (e.getCause() != null) {
        message = e.getCause().getMessage();
      }
      else {
        message = e.getMessage();
      }
      queryException = new QueryBuilderException(QueryBuilderException.Error.PARSE_ERROR,
        message, e.getStackTrace());
    }
  }

  /**
   * Instantiate a {@link TreeQueryParser} depending on the object type.
   * Then, set the default operator.
   */
  protected TreeQueryParser getResourceQueryParser(final Analyzer analyzer) {
    final TreeQueryParser qph;
    
    if (analyzer instanceof NumericAnalyzer)
      qph = new TreeQueryParser(wsAnalyzer, (NumericAnalyzer) analyzer);
    else
      qph = new TreeQueryParser(analyzer);
    qph.setDefaultOperator(defaultOp);
    return qph;
  }

}

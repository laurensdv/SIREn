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
 * @author Renaud Delbru [ 23 Jan 2011 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2010 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.qparser.tree;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.flexible.core.config.ConfigurationKey;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.sindice.siren.analysis.NumericAnalyzer;
import org.sindice.siren.qparser.tree.query.builders.NodeQueryTreeBuilder;
import org.sindice.siren.qparser.tree.query.config.TreeQueryConfigHandler;
import org.sindice.siren.qparser.tree.query.processors.TreeQueryNodeProcessorPipeline;
import org.sindice.siren.search.node.MultiNodeTermQuery;
import org.sindice.siren.search.node.MultiNodeTermQuery.RewriteMethod;

public class TreeQueryParser extends StandardQueryParser {

  final public static class TreeConfigurationKeys {

    /**
     * Key used to set a token to its numeric datatype.
     */
    final public static ConfigurationKey<NumericAnalyzer> NUMERIC_ANALYZERS = ConfigurationKey.newInstance();

    /**
     * Key used to set the {@link RewriteMethod} used when creating queries
     * 
     * @see TreeQueryParser#setMultiTermRewriteMethod(MultiNodeTermQuery.RewriteMethod)
     * @see TreeQueryParser#getMultiTermRewriteMethod()
     */
    final public static ConfigurationKey<MultiNodeTermQuery.RewriteMethod> MULTI_NODE_TERM_REWRITE_METHOD = ConfigurationKey.newInstance();

  }

  /**
   * Constructs a {@link TreeQueryParser} object. The same as:
   * <ul>
   * StandardQueryParser qph = new StandardQueryParser();
   * qph.setQueryBuilder(new ResourceQueryTreeBuilder());
   * qph.setQueryNodeProcessor(new ResourceQueryNodeProcessorPipeline(qph.getQueryConfigHandler()));
   * <ul>
   */
  public TreeQueryParser() {
    super();
    setQueryConfigHandler(new TreeQueryConfigHandler());
    this.setQueryNodeProcessor(new TreeQueryNodeProcessorPipeline(getQueryConfigHandler()));
    this.setQueryBuilder(new NodeQueryTreeBuilder());
  }

  /**
   * Constructs a {@link TreeQueryParser} object and sets an
   * {@link Analyzer} to it. The same as:
   *
   * <ul>
   * ResourceQueryParser qp = new ResourceQueryParser();
   * qp.getQueryConfigHandler().setAnalyzer(analyzer);
   * </ul>
   *
   * @param analyzer
   *          the analyzer to be used by this query parser helper
   */
  public TreeQueryParser(final Analyzer analyzer) {
    this();
    this.setAnalyzer(analyzer);
  }
  
  /**
   * Constructs a {@link TreeQueryParser} object and sets the node analyzer to a
   * {@link TreeConfigurationKeys#*_ANALYZER} key, if it is either an int, float,
   * long or double value analyzer.
   */
  public TreeQueryParser(final Analyzer analyzer, final NumericAnalyzer na) {
    this(analyzer);
    this.getQueryConfigHandler().set(TreeConfigurationKeys.NUMERIC_ANALYZERS, na);
  }

}

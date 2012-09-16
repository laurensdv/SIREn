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
package org.sindice.siren.qparser.keyword.query.builders;

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.builders.QueryTreeBuilder;
import org.apache.lucene.queryparser.flexible.core.nodes.BooleanQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.BoostQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.FieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.FuzzyQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.GroupQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.MatchAllDocsQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.MatchNoDocsQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.ModifierQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.SlopQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.TokenizedPhraseQueryNode;
import org.apache.lucene.queryparser.flexible.standard.builders.BooleanQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.builders.BoostQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.builders.DummyQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.builders.GroupQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.builders.MatchAllDocsQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.builders.MatchNoDocsQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.builders.ModifierQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.nodes.MultiPhraseQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.NumericQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.PrefixWildcardQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.RegexpQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.TermRangeQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.WildcardQueryNode;
import org.apache.lucene.queryparser.flexible.standard.processors.StandardQueryNodeProcessorPipeline;
import org.apache.lucene.search.Query;
import org.sindice.siren.qparser.keyword.nodes.SirenQueryNode;
import org.sindice.siren.qparser.tree.query.builders.FieldQueryNodeBuilder;
import org.sindice.siren.qparser.tree.query.builders.MultiPhraseQueryNodeBuilder;
import org.sindice.siren.qparser.tree.query.builders.NodeFuzzyQueryNodeBuilder;
import org.sindice.siren.qparser.tree.query.builders.NodeNumericRangeQueryNodeBuilder;
import org.sindice.siren.qparser.tree.query.builders.NodePhraseQueryNodeBuilder;
import org.sindice.siren.qparser.tree.query.builders.NodePrefixWildcardQueryNodeBuilder;
import org.sindice.siren.qparser.tree.query.builders.NodeQueryBuilder;
import org.sindice.siren.qparser.tree.query.builders.NodeRegexpQueryNodeBuilder;
import org.sindice.siren.qparser.tree.query.builders.NodeTermRangeQueryNodeBuilder;
import org.sindice.siren.qparser.tree.query.builders.NodeWildcardQueryNodeBuilder;
import org.sindice.siren.qparser.tree.query.builders.SlopQueryNodeBuilder;
import org.sindice.siren.qparser.tree.query.nodes.NodeNumericRangeQueryNode;

/**
 * This query tree builder only defines the necessary map to build a
 * {@link Query} tree object. It should be used to generate a {@link Query} tree
 * object from a query node tree processed by a
 * {@link StandardQueryNodeProcessorPipeline}. <br/>
 * 
 * @see QueryTreeBuilder
 * @see StandardQueryNodeProcessorPipeline
 */
public class KeywordQueryTreeBuilder extends QueryTreeBuilder implements NodeQueryBuilder {

  public KeywordQueryTreeBuilder() {
    // Create Siren primitive queries
    setBuilder(FuzzyQueryNode.class, new NodeFuzzyQueryNodeBuilder());
    setBuilder(WildcardQueryNode.class, new NodeWildcardQueryNodeBuilder());
    setBuilder(TokenizedPhraseQueryNode.class, new NodePhraseQueryNodeBuilder());
    setBuilder(PrefixWildcardQueryNode.class, new NodePrefixWildcardQueryNodeBuilder());
    setBuilder(SlopQueryNode.class, new SlopQueryNodeBuilder());
    setBuilder(MultiPhraseQueryNode.class, new MultiPhraseQueryNodeBuilder());
    setBuilder(FieldQueryNode.class, new FieldQueryNodeBuilder());
    setBuilder(NodeNumericRangeQueryNode.class, new NodeNumericRangeQueryNodeBuilder());
    setBuilder(TermRangeQueryNode.class, new NodeTermRangeQueryNodeBuilder());
    setBuilder(RegexpQueryNode.class, new NodeRegexpQueryNodeBuilder());

    setBuilder(SirenQueryNode.class, new SirenQueryNodeBuilder());
    // Create Lucene queries
    setBuilder(GroupQueryNode.class, new GroupQueryNodeBuilder());
    setBuilder(ModifierQueryNode.class, new ModifierQueryNodeBuilder());
    setBuilder(MatchAllDocsQueryNode.class, new MatchAllDocsQueryNodeBuilder());
    setBuilder(NumericQueryNode.class, new DummyQueryNodeBuilder());
    setBuilder(BooleanQueryNode.class, new BooleanQueryNodeBuilder());
    setBuilder(MatchNoDocsQueryNode.class, new MatchNoDocsQueryNodeBuilder());
    setBuilder(BoostQueryNode.class, new BoostQueryNodeBuilder());
  }

  @Override
  public Query build(QueryNode queryNode) throws QueryNodeException {
    try {
      return (Query) super.build(queryNode);
    }
    catch (ClassCastException e) {
      throw new Error("Unsupported query construct", e);
    }
  }

}

/**
 * Copyright (c) 2009-2012 National University of Ireland, Galway. All Rights Reserved.
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
 * @project siren-benchmark
 * @author Renaud Delbru [ 6 Jul 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.benchmark.query.provider;

import java.util.List;
import java.util.Map.Entry;

import org.apache.lucene.index.Term;
import org.sindice.siren.benchmark.generator.document.BenchmarkDocument;
import org.sindice.siren.benchmark.query.provider.AttributeQueryProvider.AttributeQuery;
import org.sindice.siren.benchmark.query.provider.BooleanQueryProvider.BooleanQuery;
import org.sindice.siren.benchmark.query.provider.PhraseQueryProvider.PhraseQuery;
import org.sindice.siren.benchmark.query.provider.PrimitiveQueryProvider.PrimitiveQuery;
import org.sindice.siren.benchmark.query.provider.Query.Occur;
import org.sindice.siren.benchmark.query.provider.TreeQueryProvider.TreeQuery;
import org.sindice.siren.search.node.NodeBooleanClause;
import org.sindice.siren.search.node.NodeBooleanQuery;
import org.sindice.siren.search.node.NodePhraseQuery;
import org.sindice.siren.search.node.NodeQuery;
import org.sindice.siren.search.node.NodeTermQuery;
import org.sindice.siren.search.node.TwigQuery;

public class SirenQueryConverter {

  public TwigQuery convert(final Query treeQuery) {
    return this.visitTreeQuery((TreeQuery) treeQuery);
  }

  private TwigQuery visitTreeQuery(final TreeQuery treeQuery) {
    final TwigQuery root = this.visitRootAttributes(treeQuery.getRootAttributeQueries());
    final TwigQuery twig = new TwigQuery(root);

    for (final TreeQuery ancestor : treeQuery.getAncestorQueries()) {
      twig.addChild(this.visitTreeQuery(ancestor), NodeBooleanClause.Occur.MUST);
    }

    return twig;
  }

  private TwigQuery visitRootAttributes(final List<AttributeQuery> rootAttrQueries) {
    final TwigQuery twig = new TwigQuery();

    for (final AttributeQuery attrQuery : rootAttrQueries) {
      twig.addChild(this.visitAttributeQuery(attrQuery), NodeBooleanClause.Occur.MUST);
    }

    return twig;
  }

  private TwigQuery visitAttributeQuery(final AttributeQuery attrQuery) {
    final NodeQuery attr = this.visitPrimitiveQuery(attrQuery.getAttributeQuery());
    final NodeQuery value = this.visitPrimitiveQuery(attrQuery.getValueQuery());

    // check if returned node query is null
    final TwigQuery twig = attr == null ? new TwigQuery() : new TwigQuery(attr);
    if (value != null) {
      twig.addChild(value, NodeBooleanClause.Occur.MUST);
    }
    return twig;
  }

  private NodeQuery visitPrimitiveQuery(final PrimitiveQuery query) {
    if (query instanceof BooleanQuery) {
      return this.visitBooleanQuery((BooleanQuery) query);
    }
    else if (query instanceof PhraseQuery) {
      return this.visitPhraseQuery((PhraseQuery) query);
    }
    else { // return null
      return null;
    }
  }

  private NodeBooleanQuery visitBooleanQuery(final BooleanQuery booleanQuery) {
    final NodeBooleanQuery nbq = new NodeBooleanQuery();
    for (final Entry<String,Occur> clause : booleanQuery.getClauses()) {
      nbq.add(new NodeTermQuery(
        new Term(BenchmarkDocument.DEFAULT_CONTENT_FIELD, clause.getKey())),
        this.convertToNodeOccur(clause.getValue()));
    }
    return nbq;
  }

  private NodePhraseQuery visitPhraseQuery(final PhraseQuery phraseQuery) {
    final NodePhraseQuery npq = new NodePhraseQuery();
    for (final String term : phraseQuery.getPhrases()) {
      npq.add(new Term(BenchmarkDocument.DEFAULT_CONTENT_FIELD, term));
    }
    return npq;
  }

  private NodeBooleanClause.Occur convertToNodeOccur(final Occur occur) {
    switch (occur) {
      case MUST:
        return NodeBooleanClause.Occur.MUST;

      case MUST_NOT:
        return NodeBooleanClause.Occur.MUST_NOT;

      case SHOULD:
        return NodeBooleanClause.Occur.SHOULD;
    }
    return null;
  }

}

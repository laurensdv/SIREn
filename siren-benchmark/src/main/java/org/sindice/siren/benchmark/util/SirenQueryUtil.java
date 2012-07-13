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
package org.sindice.siren.benchmark.util;

import java.util.Map.Entry;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.sindice.siren.benchmark.query.provider.FieldQuery;
import org.sindice.siren.benchmark.query.provider.KeywordQuery;
import org.sindice.siren.benchmark.query.provider.KeywordQuery.Occur;
import org.sindice.siren.benchmark.query.provider.PhraseQuery;
import org.sindice.siren.benchmark.query.provider.StarShapedQuery;
import org.sindice.siren.benchmark.wrapper.AbstractIndexWrapper;
import org.sindice.siren.search.node.NodeBooleanClause;
import org.sindice.siren.search.node.NodeBooleanQuery;
import org.sindice.siren.search.node.TwigQuery;
import org.sindice.siren.search.primitive.NodePhraseQuery;
import org.sindice.siren.search.primitive.NodeTermQuery;

public class SirenQueryUtil {

  public static Query convertQuery(final org.sindice.siren.benchmark.query.provider.Query query) {
    if (query instanceof KeywordQuery) {
      return convertKeywordQuery((KeywordQuery) query);
    }
    else if (query instanceof PhraseQuery) {
      return convertPhraseQuery((PhraseQuery) query);
    }
    else if (query instanceof FieldQuery) {
      return convertFieldQuery((FieldQuery) query);
    }
    else if (query instanceof StarShapedQuery) {
      return convertStarQuery((StarShapedQuery) query);
    }
    throw new RuntimeException("Unknown query class: " + query.getClass().toString());
  }

  protected static NodePhraseQuery convertPhraseQuery(final PhraseQuery query) {
    final NodePhraseQuery pq = new NodePhraseQuery();
    for (final String word : query.getPhrases()) {
      pq.add(new Term(AbstractIndexWrapper.DEFAULT_CONTENT_FIELD, word));
    }
    return pq;
  }

  protected static NodeBooleanQuery convertKeywordQuery(final KeywordQuery query) {
    final NodeBooleanQuery bq = new NodeBooleanQuery();
    NodeTermQuery tq;
    NodeBooleanClause clause;

    for (final Entry<String, Occur> entry : query.getKeywords()) {
      tq = new NodeTermQuery(new Term(AbstractIndexWrapper.DEFAULT_CONTENT_FIELD, entry.getKey()));
      clause = new NodeBooleanClause(tq, convertToNodeOccur(entry.getValue()));
      bq.add(clause);
    }
    return bq;
  }

  protected static NodeBooleanQuery convertToBooleanQuery(final String query) {
    final NodeBooleanQuery bq = new NodeBooleanQuery();
    NodeTermQuery tq;
    NodeBooleanClause clause;

    tq = new NodeTermQuery(new Term(AbstractIndexWrapper.DEFAULT_CONTENT_FIELD, query));
    clause = new NodeBooleanClause(tq, NodeBooleanClause.Occur.MUST);
    bq.add(clause);
    return bq;
  }

  protected static TwigQuery convertFieldQuery(final FieldQuery query) {
    // Convert field name into node boolean query
    final NodeBooleanQuery field = convertKeywordQuery(query.getFieldName());

    final TwigQuery tq = new TwigQuery(1, field);

    // convert value query
    final org.sindice.siren.benchmark.query.provider.Query valueQuery = query.getValueQuery();
    if (valueQuery instanceof KeywordQuery) {
      tq.addChild(convertKeywordQuery((KeywordQuery) valueQuery), NodeBooleanClause.Occur.MUST);
    }
    else if (valueQuery instanceof PhraseQuery) {
      tq.addChild(convertPhraseQuery((PhraseQuery) valueQuery), NodeBooleanClause.Occur.MUST);
    }

    return tq;
  }

  protected static BooleanQuery convertStarQuery(final StarShapedQuery ssQ) {
    final BooleanQuery bq = new BooleanQuery();

    while (ssQ.hasNext()) {
      bq.add(convertFieldQuery(ssQ.next()), BooleanClause.Occur.MUST);
    }

    return bq;
  }

  private static NodeBooleanClause.Occur convertToNodeOccur(final Occur occur) {
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

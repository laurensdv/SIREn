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
 * @author Renaud Delbru [ 21 Jan 2011 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2010 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.qparser.keyword.query.processors;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.config.QueryConfigHandler;
import org.apache.lucene.queryparser.flexible.core.nodes.FieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.FuzzyQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.GroupQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.NoTokenFoundQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.OrQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QuotedFieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.RangeQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.TextableQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.TokenizedPhraseQueryNode;
import org.apache.lucene.queryparser.flexible.core.processors.QueryNodeProcessorImpl;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.ConfigurationKeys;
import org.apache.lucene.queryparser.flexible.standard.nodes.MultiPhraseQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.RegexpQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.WildcardQueryNode;

/**
 * This processor verifies if the attribute {@link AnalyzerQueryNodeProcessor}
 * is defined in the {@link QueryConfigHandler}. If it is and the analyzer is
 * not <code>null</code>, it looks for every {@link FieldQueryNode} that is not
 * {@link WildcardQueryNode}, {@link FuzzyQueryNode} or
 * {@link ParametricQueryNode} contained in the query node tree, then it applies
 * the analyzer to that {@link FieldQueryNode} object. <br/>
 * <br/>
 * If the analyzer return only one term, the returned term is set to the
 * {@link FieldQueryNode} and it's returned. <br/>
 * <br/>
 * If the analyzer return more than one term
 * {@link TokenizedPhraseQueryNode} or {@link MultiPhraseQueryNode} is created,
 * whether there is one or more
 * terms at the same position, and it's returned. <br/>
 * <br/>
 * A {@link OrQueryNode} can be returned if query expansion is detected, i.e.,
 * more than one term at the same position.
 * <br/>
 * If no term is returned by the analyzer a {@link NoTokenFoundQueryNode} object
 * is returned. <br/>
 * <br/>
 * Extended version of the original
 * {@link org.apache.lucene.queryParser.standard.processors.AnalyzerQueryNodeProcessor}.
 *
 * @see Analyzer
 * @see TokenStream
 */
public class AnalyzerQueryNodeProcessor extends QueryNodeProcessorImpl {

  private Analyzer analyzer;

  private boolean positionIncrementsEnabled;

  public AnalyzerQueryNodeProcessor() {
    // empty constructor
  }

  @Override
  public QueryNode process(final QueryNode queryTree) throws QueryNodeException {

    if (this.getQueryConfigHandler().has(ConfigurationKeys.ANALYZER)) {

      this.analyzer = this.getQueryConfigHandler().get(ConfigurationKeys.ANALYZER);

      this.positionIncrementsEnabled = false;

      if (this.getQueryConfigHandler().has(ConfigurationKeys.ENABLE_POSITION_INCREMENTS)) {

        if (this.getQueryConfigHandler().get(ConfigurationKeys.ENABLE_POSITION_INCREMENTS)) {

          this.positionIncrementsEnabled = true;

        }

      }

      if (this.analyzer != null) {
        return super.process(queryTree);
      }

    }

    return queryTree;

  }

  @Override
  protected QueryNode postProcessNode(final QueryNode node) throws QueryNodeException {

    if (node instanceof TextableQueryNode
    && !(node instanceof WildcardQueryNode)
    && !(node instanceof FuzzyQueryNode)
    && !(node instanceof RegexpQueryNode)
    && !(node.getParent() instanceof RangeQueryNode)) {

      final FieldQueryNode fieldNode = ((FieldQueryNode) node);
      final String text = fieldNode.getTextAsString();
      final String field = fieldNode.getFieldAsString();

      PositionIncrementAttribute posIncrAtt = null;
      int numTokens = 0;
      int positionCount = 0;
      boolean severalTokensAtSamePosition = false;

      final TokenStream source;
      try {
        source = this.analyzer.tokenStream(field, new StringReader(text));
        // The source is reset in the original AnalyzerQueryNodeProcessor class
        source.reset();
      } catch (IOException e1) {
        // return the query node untouched
        return node;
      }
      // TODO: Shouldn't this stream be closed ?
      final CachingTokenFilter buffer = new CachingTokenFilter(source);

      try {
        if (buffer.hasAttribute(PositionIncrementAttribute.class)) {
          posIncrAtt = buffer.getAttribute(PositionIncrementAttribute.class);
        }

        while (buffer.incrementToken()) {
          numTokens++;
          final int positionIncrement = (posIncrAtt != null) ? posIncrAtt
              .getPositionIncrement() : 1;
          if (positionIncrement != 0) {
            positionCount += positionIncrement;

          } else {
            severalTokensAtSamePosition = true;
          }

        }
        // rewind the buffer stream
        buffer.reset();

        // close original stream - all tokens buffered
        source.close();
      } catch (final IOException e) {
        // ignore
      }

      if (!buffer.hasAttribute(CharTermAttribute.class)) {
        return new NoTokenFoundQueryNode();
      }

      final CharTermAttribute termAtt = buffer.getAttribute(CharTermAttribute.class);

      if (numTokens == 0) {
        return new NoTokenFoundQueryNode();
      }
      else if (numTokens == 1) {
        String term = null;
        try {
          boolean hasNext;
          hasNext = buffer.incrementToken();
          assert hasNext == true;
          term = termAtt.toString();

        } catch (final IOException e) {
          // safe to ignore, because we know the number of tokens
        }

        fieldNode.setText(term);

        return fieldNode;

      }
      else if (severalTokensAtSamePosition || !(node instanceof QuotedFieldQueryNode)) {
        if (positionCount == 1 || !(node instanceof QuotedFieldQueryNode)) {
          // no phrase query:
          final LinkedList<QueryNode> children = new LinkedList<QueryNode>();

          int position = -1;

          for (int i = 0; i < numTokens; i++) {
            String term = null;
            final int positionIncrement = 1;

            try {
              final boolean hasNext = buffer.incrementToken();
              assert hasNext == true;
              term = termAtt.toString();

            } catch (final IOException e) {
              // safe to ignore, because we know the number of tokens
            }

            final FieldQueryNode newFieldNode = new FieldQueryNode(field, term, -1, -1);

            if (this.positionIncrementsEnabled) {
              position += positionIncrement;
              newFieldNode.setPositionIncrement(position);
            } else {
              newFieldNode.setPositionIncrement(i);
            }

            children.add(new FieldQueryNode(field, term, -1, -1));
          }

          // If multiple terms at one single position, this must be a query
          // expansion. Perform a OR between the terms.
          if (severalTokensAtSamePosition && positionCount == 1) {
            return new GroupQueryNode(new OrQueryNode(children));
          }
          // if several tokens at same position && position count > 1, then
          // results can be unexpected
          else {
            final TokenizedPhraseQueryNode pq = new TokenizedPhraseQueryNode();
            for (int i = 0; i < children.size(); i++) {
              pq.add(children.get(i));
            }
            return pq;
          }

        }
        else {
          // phrase query:
          final MultiPhraseQueryNode mpq = new MultiPhraseQueryNode();

          final List<FieldQueryNode> multiTerms = new ArrayList<FieldQueryNode>();
          int position = -1;
          int i = 0;
          int termGroupCount = 0;

          for (; i < numTokens; i++) {
            String term = null;
            int positionIncrement = 1;
            try {
              final boolean hasNext = buffer.incrementToken();
              assert hasNext == true;
              term = termAtt.toString();
              if (posIncrAtt != null) {
                positionIncrement = posIncrAtt.getPositionIncrement();
              }

            }
            catch (final IOException e) {
              // safe to ignore, because we know the number of tokens
            }

            if (positionIncrement > 0 && multiTerms.size() > 0) {

              for (final FieldQueryNode termNode : multiTerms) {

                if (this.positionIncrementsEnabled) {
                  termNode.setPositionIncrement(position);
                }
                else {
                  termNode.setPositionIncrement(termGroupCount);
                }

                mpq.add(termNode);
              }

              // Only increment once for each "group" of
              // terms that were in the same position:
              termGroupCount++;

              multiTerms.clear();

            }

            position += positionIncrement;
            multiTerms.add(new FieldQueryNode(field, term, -1, -1));

          }

          for (final FieldQueryNode termNode : multiTerms) {

            if (this.positionIncrementsEnabled) {
              termNode.setPositionIncrement(position);

            } else {
              termNode.setPositionIncrement(termGroupCount);
            }

            mpq.add(termNode);

          }

          return mpq;

        }

      } else {

        final TokenizedPhraseQueryNode pq = new TokenizedPhraseQueryNode();

        int position = -1;

        for (int i = 0; i < numTokens; i++) {
          String term = null;
          int positionIncrement = 1;

          try {
            final boolean hasNext = buffer.incrementToken();
            assert hasNext == true;
            term = termAtt.toString();

            if (posIncrAtt != null) {
              positionIncrement = posIncrAtt.getPositionIncrement();
            }

          } catch (final IOException e) {
            // safe to ignore, because we know the number of tokens
          }

          final FieldQueryNode newFieldNode = new FieldQueryNode(field, term, -1, -1);

          if (this.positionIncrementsEnabled) {
            position += positionIncrement;
            newFieldNode.setPositionIncrement(position);

          } else {
            newFieldNode.setPositionIncrement(i);
          }

          pq.add(newFieldNode);

        }

        return pq;

      }

    }

    return node;

  }

  @Override
  protected QueryNode preProcessNode(final QueryNode node) throws QueryNodeException {

    return node;

  }

  @Override
  protected List<QueryNode> setChildrenOrder(final List<QueryNode> children)
      throws QueryNodeException {

    return children;

  }

}

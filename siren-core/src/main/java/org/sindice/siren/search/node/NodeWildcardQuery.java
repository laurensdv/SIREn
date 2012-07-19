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
 * @project siren-core
 * @author Renaud Delbru [ 28 Sep 2011 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.search.node;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.AutomatonQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.util.ToStringUtils;
import org.apache.lucene.util.automaton.Automaton;
import org.apache.lucene.util.automaton.BasicAutomata;
import org.apache.lucene.util.automaton.BasicOperations;

/**
 * Implements the wildcard search query. Supported wildcards are <code>*</code>, which
 * matches any character sequence (including the empty one), and <code>?</code>,
 * which matches any single character. Note this query can be slow, as it
 * needs to iterate over many terms. In order to prevent extremely slow WildcardQueries,
 * a Wildcard term should not start with the wildcards <code>*</code>.
 *
 * <p>This query uses the {@link
 * MultiNodeTermQuery#CONSTANT_SCORE_AUTO_REWRITE_DEFAULT}
 * rewrite method.
 *
 * <p> Code taken from {@link WildcardQuery} and adapted for SIREn.
 *
 * @see AutomatonQuery
 **/
public class NodeWildcardQuery extends NodeAutomatonQuery {

  /** String equality with support for wildcards */
  public static final char WILDCARD_STRING = '*';

  /** Char equality with support for wildcards */
  public static final char WILDCARD_CHAR = '?';

  /** Escape character */
  public static final char WILDCARD_ESCAPE = '\\';

  public NodeWildcardQuery(final Term term) {
    super(term, toAutomaton(term));
  }

  /**
   * Convert wildcard syntax into an automaton.
   * @lucene.internal
   */
  @SuppressWarnings("fallthrough")
  public static Automaton toAutomaton(final Term wildcardquery) {
    final List<Automaton> automata = new ArrayList<Automaton>();

    final String wildcardText = wildcardquery.text();

    for (int i = 0; i < wildcardText.length();) {
      final int c = wildcardText.codePointAt(i);
      int length = Character.charCount(c);
      switch(c) {
        case WILDCARD_STRING:
          automata.add(BasicAutomata.makeAnyString());
          break;
        case WILDCARD_CHAR:
          automata.add(BasicAutomata.makeAnyChar());
          break;
        case WILDCARD_ESCAPE:
          // add the next codepoint instead, if it exists
          if (i + length < wildcardText.length()) {
            final int nextChar = wildcardText.codePointAt(i + length);
            length += Character.charCount(nextChar);
            automata.add(BasicAutomata.makeChar(nextChar));
            break;
          } // else fallthru, lenient parsing with a trailing \
        default:
          automata.add(BasicAutomata.makeChar(c));
      }
      i += length;
    }

    return BasicOperations.concatenate(automata);
  }

  /**
   * Returns the pattern term.
   */
  public Term getTerm() {
    return term;
  }

  /** Prints a user-readable version of this query. */
  @Override
  public String toString(final String field) {
    final StringBuilder buffer = new StringBuilder();
    if (!this.getField().equals(field)) {
      buffer.append(this.getField());
      buffer.append(":");
    }
    buffer.append(term.text());
    buffer.append(ToStringUtils.boost(this.getBoost()));
    return buffer.toString();
  }

}


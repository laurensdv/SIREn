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
 * @project siren-core
 * @author Renaud Delbru [ 19 Jul 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.search.node;

import java.io.IOException;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.AutomatonQuery;
import org.apache.lucene.search.RegexpQuery;
import org.apache.lucene.util.ToStringUtils;
import org.apache.lucene.util.automaton.Automaton;
import org.apache.lucene.util.automaton.AutomatonProvider;
import org.apache.lucene.util.automaton.RegExp;

/**
 * A fast regular expression query based on the
 * {@link org.apache.lucene.util.automaton} package.
 * <ul>
 * <li>Comparisons are <a
 * href="http://tusker.org/regex/regex_benchmark.html">fast</a>
 * <li>The term dictionary is enumerated in an intelligent way, to avoid
 * comparisons. See {@link AutomatonQuery} for more details.
 * </ul>
 * <p>
 * The supported syntax is documented in the {@link RegExp} class.
 * Note this might be different than other regular expression implementations.
 * For some alternatives with different syntax, look under the sandbox.
 * </p>
 * <p>
 * Note this query can be slow, as it needs to iterate over many terms. In order
 * to prevent extremely slow RegexpQueries, a Regexp term should not start with
 * the expression <code>.*</code>
 *
 * <p> Code taken from {@link RegexpQuery} and adapted for SIREn.
 */
public class NodeRegexpQuery extends NodeAutomatonQuery {
  /**
   * A provider that provides no named automata
   */
  private static AutomatonProvider defaultProvider = new AutomatonProvider() {
    public Automaton getAutomaton(final String name) throws IOException {
      return null;
    }
  };

  /**
   * Constructs a query for terms matching <code>term</code>.
   * <p>
   * By default, all regular expression features are enabled.
   * </p>
   *
   * @param term regular expression.
   */
  public NodeRegexpQuery(final Term term) {
    this(term, RegExp.ALL);
  }

  /**
   * Constructs a query for terms matching <code>term</code>.
   *
   * @param term regular expression.
   * @param flags optional RegExp features from {@link RegExp}
   */
  public NodeRegexpQuery(final Term term, final int flags) {
    this(term, flags, defaultProvider);
  }

  /**
   * Constructs a query for terms matching <code>term</code>.
   *
   * @param term regular expression.
   * @param flags optional RegExp features from {@link RegExp}
   * @param provider custom AutomatonProvider for named automata
   */
  public NodeRegexpQuery(final Term term, final int flags, final AutomatonProvider provider) {
    super(term, new RegExp(term.text(), flags).toAutomaton(provider));
  }

  /** Prints a user-readable version of this query. */
  @Override
  public String toString(final String field) {
    final StringBuilder buffer = new StringBuilder();
    if (!term.field().equals(field)) {
      buffer.append(term.field());
      buffer.append(":");
    }
    buffer.append(term.text());
    buffer.append(ToStringUtils.boost(this.getBoost()));
    return buffer.toString();
  }

}


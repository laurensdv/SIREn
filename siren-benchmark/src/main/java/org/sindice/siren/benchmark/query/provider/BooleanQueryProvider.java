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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.sindice.siren.benchmark.generator.lexicon.TermLexiconGenerator;
import org.sindice.siren.benchmark.generator.lexicon.TermLexiconReader;
import org.sindice.siren.benchmark.generator.lexicon.TermLexiconWriter.TermGroup;
import org.sindice.siren.benchmark.query.provider.Query.Occur;

public class BooleanQueryProvider extends PrimitiveQueryProvider {

  private final Map<TermGroup, Occur> clauses;
  private int counter = 0;

  public BooleanQueryProvider(final Map<TermGroup, Occur> clauses) {
    this.clauses = clauses;
  }

  @Override
  public void setTermLexicon(final File lexiconDir) throws IOException {
    reader = new TermLexiconReader(new File(lexiconDir, TermLexiconGenerator.TERM_SUBDIR));
  }

  @Override
  public boolean hasNext() {
    return (counter < nbQueries) ? true : false;
  }

  @Override
  public BooleanQuery next() {
    final BooleanQuery kq = new BooleanQuery();

    counter++;
    for (final Entry<TermGroup, Occur> clause : clauses.entrySet()) {
      try {
        kq.addClause(reader.getRandomTerm(clause.getKey()), clause.getValue());
      }
      catch (final IOException e) {
        e.printStackTrace();
      }
    }

    return kq;
  }

  public static class BooleanQuery extends PrimitiveQuery {

    private final Map<String, Occur> clauses;

    public BooleanQuery() {
      clauses = new HashMap<String, Occur>(4);
    }

    public void addClause(final String term, final Occur occur) {
      this.clauses.put(term, occur);
    }

    public Set<Entry<String, Occur>> getClauses() {
      return this.clauses.entrySet();
    }

    @Override
    public String toString() {
      final StringBuilder builder = new StringBuilder();
      for (final Entry<String, Occur> clause : clauses.entrySet()) {
        builder.append(clause.getKey());
        builder.append(":");
        builder.append(clause.getValue());
        builder.append(",");
      }
      builder.setLength(builder.length() - 1);
      return builder.toString();
    }

  }

}

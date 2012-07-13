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

import java.util.ArrayList;
import java.util.List;

/*
 * By default, Occur equals MUST
 */
public class PhraseQuery
implements Query {

  private final List<String> phrases = new ArrayList<String>();

  public void addPhrase(final String phrase) {
    final String[] words = phrase.split(" ");
    for (final String word : words) {
      phrases.add(word);
    }
  }

  public List<String> getPhrases() {
    return this.phrases;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("<");
    for (final String word : phrases) {
      builder.append(word + ",");
    }
    builder.setLength(builder.length() - 1);
    builder.append(">");
    return builder.toString();
  }
}

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
package org.sindice.siren.benchmark.generator.lexicon;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.sindice.siren.benchmark.generator.lexicon.TermLexiconWriter.TermGroups;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TermLexiconReader {

  private final Map<TermGroups, TermFileReader> readers;

  final Random rand    = new Random();
  final Logger logger  = LoggerFactory.getLogger(TermLexiconReader.class);

  public TermLexiconReader(final File dir) throws IOException {
    readers = TermLexiconReader.getReaders(dir);
  }

  public void setSeed(final long seed) {
    rand.setSeed(seed);
  }

  public void close() throws IOException {
    for (final TermFileReader reader : readers.values()) {
      reader.close();
    }
  }

  private static Map<TermGroups, TermFileReader> getReaders(final File dir)
  throws IOException {
    final Map<TermGroups, TermFileReader> readers = new HashMap<TermGroups, TermFileReader>(3);
    readers.put(TermGroups.HIGH, new TermFileReader(new File(dir, TermGroups.HIGH.toString())));
    readers.put(TermGroups.MEDIUM, new TermFileReader(new File(dir, TermGroups.MEDIUM.toString())));
    readers.put(TermGroups.LOW, new TermFileReader(new File(dir, TermGroups.LOW.toString())));
    return readers;
  }

  /**
   * Return a random term from the group QueryGroups
   * @param group
   * @return
   * @throws IOException
   */
  public final String getRandomTerm(final TermGroups group)
  throws IOException {
    final TermFileReader reader = readers.get(group);
    return reader.getTerm(rand.nextInt((int) reader.getNumberTerms()));
  }

}

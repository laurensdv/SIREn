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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sindice.siren.benchmark.MockTermFreqIterator;
import org.sindice.siren.benchmark.generator.lexicon.TermLexiconWriter.TermGroups;

public class TermLexiconTest {

  private File tmpDir;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp()
  throws Exception {
    tmpDir = new File("./target/term-lexicon-test/");
    tmpDir.mkdir();
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown()
  throws Exception {
    FileUtils.deleteDirectory(tmpDir);
  }

  @Test
  public void testCreation() throws IOException {
    final TermLexiconWriter writer = new TermLexiconWriter(tmpDir, "80-10-10", true);
    writer.create(new MockTermFreqIterator());
    writer.close();

    final TermLexiconReader reader = new TermLexiconReader(tmpDir);

    final Set<String> high = new HashSet<String>();
    high.add("aaa"); high.add("zzz"); high.add("ccc"); high.add("jjj");
    for (int i = 0; i < 10; i++) {
      final String term = reader.getRandomTerm(TermGroups.HIGH);
      assertTrue("Should not contain " + term, high.contains(term));
    }

    final Set<String> medium = new HashSet<String>();
    medium.add("eee"); medium.add("ddd");
    for (int i = 0; i < 10; i++) {
      final String term = reader.getRandomTerm(TermGroups.MEDIUM);
      assertTrue("Should not contain " + term, medium.contains(term));
    }

    final Set<String> low = new HashSet<String>();
    low.add("bbb"); low.add("hhh"); low.add("ooo"); low.add("ppp");
    for (int i = 0; i < 10; i++) {
      final String term = reader.getRandomTerm(TermGroups.LOW);
      assertTrue("Should not contain " + term, low.contains(term));
    }

    reader.close();
  }

}

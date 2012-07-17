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
package org.sindice.siren.benchmark.query;

import static junit.framework.Assert.assertEquals;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sindice.siren.benchmark.Measurement;
import org.sindice.siren.benchmark.MockIndexWrapper;
import org.sindice.siren.benchmark.MockTermFreqIterator;
import org.sindice.siren.benchmark.generator.lexicon.TermLexiconReader;
import org.sindice.siren.benchmark.generator.lexicon.TermLexiconWriter;
import org.sindice.siren.benchmark.generator.lexicon.TermLexiconWriter.TermGroup;
import org.sindice.siren.benchmark.query.provider.BooleanQuery.Occur;
import org.sindice.siren.benchmark.query.provider.BooleanQueryProvider;

public class ConcurrentQueryExecutorTest {

  private File tmpDir;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    tmpDir = new File("./target/term-lexicon-test/");
    tmpDir.mkdir();

    final TermLexiconWriter writer = new TermLexiconWriter(tmpDir, "80-10-10", true);
    writer.create(new MockTermFreqIterator());
    writer.close();

    AbstractQueryExecutor.WARMUP_TIME = 0;
    AbstractQueryExecutor.NUMBER_MEASUREMENTS = 2;
    AbstractQueryExecutor.EXECUTION_TIME_GOAL = (500 * 1000 * 1000);
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    FileUtils.deleteDirectory(tmpDir);
  }

  @Test
  public void testExecute() throws Exception {
    final Occur[] occurs = new Occur[] {Occur.MUST, Occur.MUST};
    final TermGroup[] groups = new TermGroup[] {TermGroup.MEDIUM, TermGroup.LOW};
    final BooleanQueryProvider provider = new BooleanQueryProvider(occurs, groups);
    final TermLexiconReader reader = new TermLexiconReader(tmpDir);
    reader.setSeed(42);
    provider.setTermLexicon(reader);
    provider.setNbQueries(4);
    final ConcurrentQueryExecutor executor = new ConcurrentQueryExecutor(new MockIndexWrapper(), provider, 2);
    executor.execute();
    assertEquals(AbstractQueryExecutor.NUMBER_MEASUREMENTS, executor.getMeasurements().length);
    for (final Measurement measurement : executor.getMeasurements()) {
      assertEquals(100, measurement.getHitsPerExecution());
    }
    executor.close();
  }

}

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
package org.sindice.siren.benchmark.generator.viz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sindice.siren.benchmark.generator.viz.index.IndexBenchmarkResults;
import org.sindice.siren.benchmark.generator.viz.index.IndexResultsIterator;
import org.sindice.siren.benchmark.generator.viz.query.QueryBenchmarkResults;
import org.sindice.siren.benchmark.generator.viz.query.QueryResultsIterator;

public class TestResultsIterator {

  private final File complete   = new File("./src/test/resources/benchmark/complete");
  private final File uncomplete = new File("./src/test/resources/benchmark/uncomplete");

  @Before
  public void setUp()
  throws Exception {}

  @After
  public void tearDown()
  throws Exception {}

  @Test
  public void testIndexResultsIterator() {
    final IndexResultsIterator it = new IndexResultsIterator();
    final File[] directories = new File[] {
      new File(complete, "sindice-afor-20"),
      new File(complete, "sindice-vint-20")
    };

    for (File f: directories) {
      it.init(f);
      assertTrue(it.hasNext());
      assertTrue(it.next() instanceof IndexBenchmarkResults);
      assertFalse(it.hasNext());
    }
  }

  @Test
  public void testQueryResultsIterator() {
    final QueryResultsIterator it = new QueryResultsIterator();

    // sindice-afor-20
    it.init(new File(complete, "sindice-afor-20"));
    checkDirectoryForQueryResults(it, 5, 4, 1);
    // sindice-vint-20
    it.init(new File(complete, "sindice-vint-20"));
    checkDirectoryForQueryResults(it, 5, 3, 2);
  }

  private void checkDirectoryForQueryResults(ResultsIterator it,
                                             int expectedNbIts,
                                             int expectedWarm,
                                             int expectedCold) {
    int nWarm = 0;
    int nCold = 0;

    for (int i = 0; i < expectedNbIts; i++) {
      assertTrue(it.hasNext());
      QueryBenchmarkResults qbr = (QueryBenchmarkResults) it.next();
      nWarm += qbr.isWarm() ? 1 : 0;
      nCold += qbr.isWarm() ? 0 : 1;
    }
    assertFalse(it.hasNext());
    assertEquals(expectedWarm, nWarm);
    assertEquals(expectedCold, nCold);
  }

  @Test
  public void testUncomplete() {
    final QueryResultsIterator it = new QueryResultsIterator();

    it.init(uncomplete);
    checkDirectoryForQueryResults(it, 4, 3, 1);
  }

}

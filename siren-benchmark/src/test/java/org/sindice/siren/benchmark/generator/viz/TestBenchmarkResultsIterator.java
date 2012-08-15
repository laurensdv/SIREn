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
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sindice.siren.benchmark.generator.viz.BenchmarkResults.ResultsType;
import org.sindice.siren.benchmark.wrapper.IndexWrapperFactory.IndexWrapperType;

public class TestBenchmarkResultsIterator {

  private final File             index      = new File("./src/test/resources/benchmark/indexes/one");
  private final File             benchmark  = new File("./src/test/resources/benchmark/query/complete");
  private final IndexWrapperType siren10    = IndexWrapperType.Siren10;

  @Before
  public void setUp()
  throws Exception {}

  @After
  public void tearDown()
  throws Exception {}

  @Test
  public void testOneIndex() {
    final BenchmarkResultsIterator it = new BenchmarkResultsIterator(siren10, index, benchmark);
    int itNb = 0;
    int qNb = 0;

    // 5 query results + 1 for the index times
    assertTrue(it.hasNext());
    if (it.next().getResutlsType() == ResultsType.INDEX) {
      itNb++;
    } else {
      qNb++;
    }
    assertTrue(it.hasNext());
    if (it.next().getResutlsType() == ResultsType.INDEX) {
      itNb++;
    } else {
      qNb++;
    }
    assertTrue(it.hasNext());
    if (it.next().getResutlsType() == ResultsType.INDEX) {
      itNb++;
    } else {
      qNb++;
    }
    assertTrue(it.hasNext());
    if (it.next().getResutlsType() == ResultsType.INDEX) {
      itNb++;
    } else {
      qNb++;
    }
    assertTrue(it.hasNext());
    if (it.next().getResutlsType() == ResultsType.INDEX) {
      itNb++;
    } else {
      qNb++;
    }
    assertTrue(it.hasNext());
    if (it.next().getResutlsType() == ResultsType.INDEX) {
      itNb++;
    } else {
      qNb++;
    }
    assertFalse(it.hasNext());
    assertEquals(5, qNb);
    assertEquals(1, itNb);
  }

  private void updateCounters(final BenchmarkResults br,
                              final HashMap<IndexWrapperType, Integer> indexes,
                              final HashMap<ResultsType, Integer> results) {
    if (!indexes.containsKey(br.getIndex())) {
      indexes.put(br.getIndex(), 0);
    }
    indexes.put(br.getIndex(), indexes.get(br.getIndex()) + 1);

    if (!results.containsKey(br.getResutlsType())) {
      results.put(br.getResutlsType(), 0);
    }
    results.put(br.getResutlsType(), results.get(br.getResutlsType()) + 1);
  }

  @Test
  public void testSeveralIndexes() {
    final BenchmarkResultsIterator it = new BenchmarkResultsIterator(new File("src/test/resources/benchmark/indexes/several"),
                                                                     new File("./src/test/resources/benchmark/query/several-indexes"));
    final HashMap<IndexWrapperType, Integer> indexes = new HashMap<IndexWrapperType, Integer>();
    final HashMap<ResultsType, Integer> results = new HashMap<ResultsType, Integer>();

    // Mock index
    assertTrue(it.hasNext());
    updateCounters(it.next(), indexes, results);
    assertTrue(it.hasNext());
    updateCounters(it.next(), indexes, results);
    assertTrue(it.hasNext());
    updateCounters(it.next(), indexes, results);
    assertTrue(it.hasNext());
    updateCounters(it.next(), indexes, results);
    assertTrue(it.hasNext());
    updateCounters(it.next(), indexes, results);
    // Siren10 index
    assertTrue(it.hasNext());
    updateCounters(it.next(), indexes, results);
    assertTrue(it.hasNext());
    updateCounters(it.next(), indexes, results);
    assertTrue(it.hasNext());
    updateCounters(it.next(), indexes, results);
    assertTrue(it.hasNext());
    updateCounters(it.next(), indexes, results);
    assertTrue(it.hasNext());
    updateCounters(it.next(), indexes, results);
    // Index Times
    assertTrue(it.hasNext());
    updateCounters(it.next(), indexes, results);
    assertTrue(it.hasNext());
    updateCounters(it.next(), indexes, results);
    assertFalse(it.hasNext());

    assertEquals(2, results.get(ResultsType.INDEX), 0);
    assertEquals(10, results.get(ResultsType.QUERY), 0);
    assertEquals(6, indexes.get(IndexWrapperType.Siren10), 0);
    assertEquals(6, indexes.get(IndexWrapperType.Mock), 0);
  }

  @Test
  public void testNotThere()
  throws Exception {
    final BenchmarkResultsIterator it = new BenchmarkResultsIterator(siren10, index, new File("./src/test/resources"));
    int itNb = 0;
    int qNb = 0;

    assertTrue(it.hasNext());
    if (it.next().getResutlsType() == ResultsType.INDEX) {
      itNb++;
    } else {
      qNb++;
    }
    assertFalse(it.hasNext());
    assertEquals(0, qNb);
    assertEquals(1, itNb);
  }

  @Test
  public void testRegex() {
    final BenchmarkResultsIterator it = new BenchmarkResultsIterator(siren10,
                                                                     index,
                                                                     benchmark, ".*-conjunction");
    int itNb = 0;
    int qNb = 0;

    assertTrue(it.hasNext());
    final BenchmarkResults b1 = it.next();
    if (b1.getResutlsType() == ResultsType.QUERY) {
      assertTrue(b1.getQuerySpec().endsWith("-conjunction"));
      qNb++;
    } else {
      itNb++;
    }
    assertTrue(it.hasNext());
    final BenchmarkResults b2 = it.next();
    if (b2.getResutlsType() == ResultsType.QUERY) {
      assertTrue(b2.getQuerySpec().endsWith("-conjunction"));
      qNb++;
    } else {
      itNb++;
    }
    assertTrue(it.hasNext());
    final BenchmarkResults b3 = it.next();
    if (b3.getResutlsType() == ResultsType.QUERY) {
      assertTrue(b3.getQuerySpec().endsWith("-conjunction"));
      qNb++;
    } else {
      itNb++;
    }
    assertFalse(it.hasNext());
    assertEquals(2, qNb);
    assertEquals(1, itNb);
  }

  @Test(expected=IllegalStateException.class)
  public void testUncomplete() {
    final BenchmarkResultsIterator it = new BenchmarkResultsIterator(siren10,
      index, new File("./src/test/resources/benchmark/query/uncomplete"));

    assertTrue(it.hasNext());
    final BenchmarkResults b1 = it.next();
    assertEquals(ResultsType.INDEX, b1.getResutlsType());

    it.hasNext();
  }

  @Test
  public void testColdOnly() {
    final BenchmarkResultsIterator it = new BenchmarkResultsIterator(siren10, index, benchmark);
    int itNb = 0;
    int qNb = 0;

    it.setWithWarm(false);
    assertTrue(it.hasNext());
    final BenchmarkResults b1 = it.next();
    if (b1.getResutlsType() == ResultsType.QUERY) {
      assertFalse(b1.isWarm());
      qNb++;
    } else {
      itNb++;
    }
    assertTrue(it.hasNext());
    final BenchmarkResults b2 = it.next();
    if (b2.getResutlsType() == ResultsType.QUERY) {
      assertFalse(b2.isWarm());
      qNb++;
    } else {
      itNb++;
    }
    assertFalse(it.hasNext());
    assertEquals(1, qNb);
    assertEquals(1, itNb);
  }

  @Test
  public void testWarmOnly() {
    final BenchmarkResultsIterator it = new BenchmarkResultsIterator(siren10, index, benchmark);
    int itNb = 0;
    int qNb = 0;

    it.setWithCold(false);
    assertTrue(it.hasNext());
    final BenchmarkResults b1 = it.next();
    if (b1.getResutlsType() == ResultsType.QUERY) {
      assertTrue(b1.isWarm());
      qNb++;
    } else {
      itNb++;
    }
    assertTrue(it.hasNext());
    final BenchmarkResults b2 = it.next();
    if (b2.getResutlsType() == ResultsType.QUERY) {
      assertTrue(b2.isWarm());
      qNb++;
    } else {
      itNb++;
    }
    assertTrue(it.hasNext());
    final BenchmarkResults b3 = it.next();
    if (b3.getResutlsType() == ResultsType.QUERY) {
      assertTrue(b3.isWarm());
      qNb++;
    } else {
      itNb++;
    }
    assertTrue(it.hasNext());
    final BenchmarkResults b4 = it.next();
    if (b4.getResutlsType() == ResultsType.QUERY) {
      assertTrue(b4.isWarm());
      qNb++;
    } else {
      itNb++;
    }
    assertTrue(it.hasNext());
    final BenchmarkResults b5 = it.next();
    if (b5.getResutlsType() == ResultsType.QUERY) {
      assertTrue(b5.isWarm());
      qNb++;
    } else {
      itNb++;
    }
    assertFalse(it.hasNext());
    assertEquals(4, qNb);
    assertEquals(1, itNb);
  }

}

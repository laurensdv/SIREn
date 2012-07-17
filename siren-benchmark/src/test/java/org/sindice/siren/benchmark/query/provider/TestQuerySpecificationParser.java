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
 * @author Renaud Delbru [ 14 Jul 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.benchmark.query.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;

import org.junit.Test;
import org.sindice.siren.benchmark.query.provider.BooleanQueryProvider.BooleanQuery;
import org.sindice.siren.benchmark.query.provider.EmptyQueryProvider.EmptyQuery;
import org.sindice.siren.benchmark.query.provider.PhraseQueryProvider.PhraseQuery;
import org.sindice.siren.benchmark.query.provider.Query.Occur;
import org.sindice.siren.benchmark.query.provider.QuerySpecificationParser.TreeQuerySpecification;
import org.sindice.siren.benchmark.query.provider.TreeQueryProvider.TreeQuery;

public class TestQuerySpecificationParser {

  private final File lexiconDir = new File("./src/test/resources/lexicon/");
  private final File querySpecDir = new File("./src/test/resources/query/spec/");

  @Test
  public void testParsingBoolean() throws IOException {
    final QuerySpecificationParser parser = new QuerySpecificationParser(lexiconDir);
    final TreeQuerySpecification spec = parser.parse(new File(querySpecDir, "boolean.txt"));
    final QueryProvider provider = spec.getQueryProvider();

    int counter = 0;
    while (provider.hasNext()) {
      counter++;
      final TreeQuery q = (TreeQuery) provider.next();
      assertNotNull(q);
      assertTrue(q.getAncestorQueries().isEmpty());
      assertEquals(1, q.getRootAttributeQueries().size());
      assertTrue(q.getRootAttributeQueries().get(0).getAttributeQuery() instanceof EmptyQuery);
      assertTrue(q.getRootAttributeQueries().get(0).getValueQuery() instanceof BooleanQuery);
      final BooleanQuery bq = (BooleanQuery) q.getRootAttributeQueries().get(0).getValueQuery();
      assertEquals(3, bq.getClauses().size());
      for (final Entry<String,Occur> clause : bq.getClauses()) {
        assertNotNull(clause.getKey());
        assertFalse(clause.getKey().isEmpty());
        assertNotNull(clause.getValue());
      }
    }

    provider.close();
    assertEquals(50, counter);
  }

  @Test
  public void testParsingAttribute() throws IOException {
    final QuerySpecificationParser parser = new QuerySpecificationParser(lexiconDir);
    final TreeQuerySpecification spec = parser.parse(new File(querySpecDir, "attribute.txt"));
    final QueryProvider provider = spec.getQueryProvider();

    int counter = 0;
    while (provider.hasNext()) {
      counter++;
      final TreeQuery q = (TreeQuery) provider.next();
      assertNotNull(q);
      assertTrue(q.getAncestorQueries().isEmpty());
      assertEquals(1, q.getRootAttributeQueries().size());
      assertTrue(q.getRootAttributeQueries().get(0).getAttributeQuery() instanceof PhraseQuery);
      assertTrue(q.getRootAttributeQueries().get(0).getValueQuery() instanceof BooleanQuery);
    }
    provider.close();
    assertEquals(50, counter);
  }

  @Test
  public void testParsingTree() throws IOException {
    final QuerySpecificationParser parser = new QuerySpecificationParser(lexiconDir);
    final TreeQuerySpecification spec = parser.parse(new File(querySpecDir, "tree.txt"));
    final QueryProvider provider = spec.getQueryProvider();

    int counter = 0;
    while (provider.hasNext()) {
      counter++;
      final TreeQuery q = (TreeQuery) provider.next();
      assertNotNull(q);
      assertEquals(2, q.getRootAttributeQueries().size());
      assertEquals(1, q.getAncestorQueries().size());
      assertTrue(q.getAncestorQueries().get(0) instanceof TreeQuery);
      assertEquals(1, q.getAncestorQueries().get(0).getRootAttributeQueries().size());
      assertTrue(q.getAncestorQueries().get(0).getAncestorQueries().isEmpty());
    }
    provider.close();
    assertEquals(50, counter);
  }

}

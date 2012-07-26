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
 * @author Renaud Delbru [ 15 Jul 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.benchmark.query.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.sindice.siren.benchmark.query.provider.QuerySpecificationParser.QuerySpecification;
import org.sindice.siren.benchmark.query.provider.TreeQueryProvider.TreeQuery;
import org.sindice.siren.search.node.NodeBooleanQuery;
import org.sindice.siren.search.node.NodePhraseQuery;
import org.sindice.siren.search.node.TwigQuery;
import org.sindice.siren.search.node.TwigQuery.EmptyRootQuery;

public class TestSirenQueryConverter {

  private final File lexiconDir = new File("./src/test/resources/lexicon/");
  private final File querySpecDir = new File("./src/test/resources/query/spec/");

  @Test
  public void testBoolean() throws IOException {
    final QuerySpecificationParser parser = new QuerySpecificationParser(lexiconDir);
    final QuerySpecification spec = parser.parse(new File(querySpecDir, "boolean.txt"));
    final QueryProvider provider = spec.getQueryProvider();

    final TreeQuery q = (TreeQuery) provider.next();
    final SirenQueryConverter converter = new SirenQueryConverter();
    final TwigQuery tq = converter.convert(q);

    System.out.println(tq);

    assertNotNull(tq.getRoot());
    assertEquals(0, tq.getClauses().length);
    assertTrue(tq.getRoot() instanceof TwigQuery);

    final TwigQuery root = (TwigQuery) tq.getRoot();
    assertNotNull(root.getRoot());
    assertTrue(root.getRoot() instanceof EmptyRootQuery);
    assertEquals(1, root.getClauses().length);
    assertTrue(root.getClauses()[0].getQuery() instanceof TwigQuery);

    final TwigQuery attr = (TwigQuery) root.getClauses()[0].getQuery();
    assertNotNull(attr.getRoot());
    assertTrue(attr.getRoot() instanceof EmptyRootQuery);
    assertEquals(1, attr.getClauses().length);
    assertTrue(attr.getClauses()[0].getQuery() instanceof NodeBooleanQuery);
    final NodeBooleanQuery nbq = (NodeBooleanQuery) attr.getClauses()[0].getQuery();
    assertEquals(3, nbq.getClauses().length);

    provider.close();
  }

  @Test
  public void testAttribute() throws IOException {
    final QuerySpecificationParser parser = new QuerySpecificationParser(lexiconDir);
    final QuerySpecification spec = parser.parse(new File(querySpecDir, "attribute.txt"));
    final QueryProvider provider = spec.getQueryProvider();

    final TreeQuery q = (TreeQuery) provider.next();
    final SirenQueryConverter converter = new SirenQueryConverter();
    final TwigQuery tq = converter.convert(q);

    final TwigQuery root = (TwigQuery) tq.getRoot();
    assertNotNull(root.getRoot());
    assertTrue(root.getRoot() instanceof EmptyRootQuery);
    assertEquals(1, root.getClauses().length);
    assertTrue(root.getClauses()[0].getQuery() instanceof TwigQuery);

    final TwigQuery attr = (TwigQuery) root.getClauses()[0].getQuery();
    assertNotNull(attr.getRoot());
    assertTrue(attr.getRoot() instanceof NodePhraseQuery);
    final NodePhraseQuery npq = (NodePhraseQuery) attr.getRoot();
    assertEquals(2, npq.getTerms().length);

    provider.close();
  }

  @Test
  public void testTree() throws IOException {
    final QuerySpecificationParser parser = new QuerySpecificationParser(lexiconDir);
    final QuerySpecification spec = parser.parse(new File(querySpecDir, "tree.txt"));
    final QueryProvider provider = spec.getQueryProvider();

    final TreeQuery q = (TreeQuery) provider.next();
    final SirenQueryConverter converter = new SirenQueryConverter();
    final TwigQuery tq = converter.convert(q);

    // should contain one ancestor
    assertEquals(1, tq.getClauses().length);

    final TwigQuery root = (TwigQuery) tq.getRoot();
    assertNotNull(root.getRoot());
    assertTrue(root.getRoot() instanceof EmptyRootQuery);
    assertEquals(2, root.getClauses().length);
    assertTrue(root.getClauses()[0].getQuery() instanceof TwigQuery);
    assertTrue(root.getClauses()[1].getQuery() instanceof TwigQuery);

    provider.close();
  }

}

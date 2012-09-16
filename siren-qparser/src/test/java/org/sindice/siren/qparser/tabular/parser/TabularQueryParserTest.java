/**
 * Copyright (c) 2009-2011 Sindice Limited. All Rights Reserved.
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
 * @project siren
 * @author Renaud Delbru [ 25 Apr 2008 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2010 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.qparser.tabular.parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.LockObtainFailedException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sindice.siren.analysis.IntNumericAnalyzer;
import org.sindice.siren.qparser.analysis.TupleTestHelper;
import org.sindice.siren.qparser.tree.TupleQueryParserTestHelper;
import org.sindice.siren.qparser.tree.TupleQueryParserTestHelper.QueryParserEnum;

public class TabularQueryParserTest {

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    TupleQueryParserTestHelper.setTestQuery(QueryParserEnum.TABULAR);
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  @Test
  /**
   * Test a query with tabular syntax
   */
  public void testTabularQuerySimpleTriple()
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    final String ntriple = "<http://s.org> <http://p.org> <http://o.org> .";
    final String query = " [0]<http://s.org> [1]<http://p.org> [2]<http://o.org>";

    assertTrue(TupleQueryParserTestHelper.match(ntriple, query));
  }

  @Test
  /**
   * Test a query with tabular syntax, different cell offset
   */
  public void testTabularQuerySimpleTriple2()
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    final String ntriple = "<http://s.org> <http://p.org> <http://o.org> .";
    
    String query = " [0]<http://s.org> [1]<http://p.org> [3]<http://o.org>";
    assertFalse(TupleQueryParserTestHelper.match(ntriple, query));
    
    query = " [0]<http://s.org> [1]<http://p.org> [2]<http://o.org>";
    assertTrue(TupleQueryParserTestHelper.match(ntriple, query));
  }
  
  @Test
  public void testTabularQuery()
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    final String ntuple = "<http://s.org> <http://p.org> \"literal\" <http://o.org> \"literal2\" .";
    
    String query = " [0]<http://s.org> [1]<http://p.org> [2]<http://o.org>";
    assertFalse(TupleQueryParserTestHelper.match(ntuple, query));
    
    query = " [0]<http://s.org> [1]<http://p.org> [2]\"literal\"";
    assertTrue(TupleQueryParserTestHelper.match(ntuple, query));
    
    query = " [0]<http://s.org> [1]<http://p.org> [3]\"literal\"";
    assertFalse(TupleQueryParserTestHelper.match(ntuple, query));
    
    query = "[1]<http://p.org> [3]<http://o.org>";
    assertTrue(TupleQueryParserTestHelper.match(ntuple, query));
    
    query = "[1]<http://p.org> [4]\"literal2\"";
    assertTrue(TupleQueryParserTestHelper.match(ntuple, query));
    
    query = " [3]<http://o.org> [4]\"literal2\"";
    assertTrue(TupleQueryParserTestHelper.match(ntuple, query));
  }
  
  @Test
  public void testTabularQuery2()
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    final String ntuple = "\"literal\" \"some long literal\" <http://o1.org> <http://o2.org> \"some long literal\" <http://o3.org> .";
    
    String query = "[0]<http://o1.org>";
    assertFalse(TupleQueryParserTestHelper.match(ntuple, query));
    
    query = "[2]<http://o1.org>";
    assertTrue(TupleQueryParserTestHelper.match(ntuple, query));
    
    query = "[5]<http://o3.org> [1]'some AND literal' [2]<http://o1.org>";
    assertTrue(TupleQueryParserTestHelper.match(ntuple, query));

    query = "[5]<http://o3.org> [1]\"some literal\" [2]<http://o1.org>";
    assertFalse(TupleQueryParserTestHelper.match(ntuple, query));
    
    query = "[4]\"some literal\"";
    assertFalse(TupleQueryParserTestHelper.match(ntuple, query));
  }
  
  @Test
  public void testEmptyTabular()
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    final String ntuple = "\"literal\" \"\" <http://o1.org> \"some long literal\" .";
    
    String query = "[2]<http://o1.org>";
    assertTrue(TupleQueryParserTestHelper.match(ntuple, query));
  }
  
  @Test(expected=ParseException.class)
  public void testBadTabularQuery()
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    final String ntuple = "\"literal\" <http://o1.org> <http://o2.org> .";

    final String query = "* [1]<http://o1.org> *";
    TupleQueryParserTestHelper.match(ntuple, query);
  }
  
  @Test
  public void testTabularQueryDatatype()
  throws Exception {
    TupleQueryParserTestHelper.registerTokenConfig(TupleTestHelper._defaultField,
      "int4", new IntNumericAnalyzer(4));
    
    final String ntuple = "<http://stephane> <http://price> <ie> \"500\"^^<int4> <pl> \"25\"^^<int4> .\n";

    String query = " [3]'[100 TO 2000]'^^<int4>";
    assertTrue(TupleQueryParserTestHelper.match(ntuple, query));
    
    query = "[5]'[100 TO 2000]'^^<int4>";
    assertFalse(TupleQueryParserTestHelper.match(ntuple, query));
    
    query = "[5]'[3 TO 30]'^^<int4>";
    assertTrue(TupleQueryParserTestHelper.match(ntuple, query));
  }
  
  @Test
  public void testScatteredTabularMultiFieldQuery()
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    final Map<String, Float> boosts = new HashMap<String, Float>();
    boosts.put(TupleTestHelper._defaultField, 1.0f);
    boosts.put(TupleTestHelper._implicitField, 1.0f);
    Map<String, String> ntuples = new HashMap<String, String>();
    ntuples.put(TupleTestHelper._defaultField, "<http://s.org> <http://p1.org> \"literal\" \"literal2\" \"literal3\" .\n");
    ntuples.put(TupleTestHelper._implicitField, "<http://s.org> <http://p2.org> <http://o.org> <http://o1.org> .\n");
    final String query = "[0]<http://s.org> [4]'literal3' AND\r\n [1]<http://p2.org> \n\r \n [2]<http://o.org>";

    // Should match, the two field content are matching either one of the two triple patterns
    assertTrue(TupleQueryParserTestHelper.match(ntuples, boosts, query, true));

    ntuples = new HashMap<String, String>();
    ntuples.put(TupleTestHelper._implicitField, "<http://s.org> <http://p2.org> <http://o2.org> <http://o1.org> .\n");

    // Should not match, only the first field content is matching one triple pattern
    assertFalse(TupleQueryParserTestHelper.match(ntuples, boosts, query, true));
  }
  
}

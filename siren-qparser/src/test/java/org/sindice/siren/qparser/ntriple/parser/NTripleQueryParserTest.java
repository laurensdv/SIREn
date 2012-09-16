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
package org.sindice.siren.qparser.ntriple.parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.LockObtainFailedException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sindice.siren.analysis.FloatNumericAnalyzer;
import org.sindice.siren.analysis.IntNumericAnalyzer;
import org.sindice.siren.analysis.LongNumericAnalyzer;
import org.sindice.siren.qparser.analysis.TupleTestHelper;
import org.sindice.siren.qparser.tree.TupleQueryParserTestHelper;
import org.sindice.siren.qparser.tree.TupleQueryParserTestHelper.QueryParserEnum;
import org.sindice.siren.qparser.tree.query.processors.NodeNumericQueryNodeProcessor;
import org.sindice.siren.qparser.tree.query.processors.NodeNumericRangeQueryNodeProcessor;

public class NTripleQueryParserTest {

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    TupleQueryParserTestHelper.setTestQuery(QueryParserEnum.NTRIPLE);
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  /**
   * Test method for {@link org.sindice.solr.plugins.analysis.NTripleStandardAnalyzer#RDFNtripleStandardAnalyzer()}.
   */
  @Test
  public void testNTripleQuerySimpleTriple()
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    final String ntriple = "<http://s.org> <http://p.org> <http://o.org> .";
    final String query = " <http://s.org> <http://p.org> <http://o.org>";

    assertTrue(TupleQueryParserTestHelper.match(ntriple, query));
  }

  /**
   * Test for special Lucene characters within URIs.
   * @throws Exception
   */
  @Test
  public void testLuceneSpecialCharacter()
  throws Exception {
    /*
     * Test special tilde character 
     */
    String ntriple = "<http://sw.deri.org/~aidanh/> <http://p.org> <http://o.org> .";
    // The URITrailingSlashFilter is called
    String query = " <http://sw.deri.org/~aidanh> <http://p.org> <http://o.org>";
    assertTrue(TupleQueryParserTestHelper.match(ntriple, query));
    
    /*
     * ? Wildcard
     */
    ntriple = "<http://example.com/?foo=bar> <http://p.org> <http://o.org> .";
    query = " <http://example.com/?foo=bar> <http://p.org> <http://o.org>";
    assertTrue(TupleQueryParserTestHelper.match(ntriple, query));
    
    // wildcard ? is escaped in the URI
    ntriple = "<http://example.com/afoo=bar> <http://p.org> <http://o.org> .";
    query = " <http://example.com/?foo=bar> <http://p.org> <http://o.org>";
    assertFalse(TupleQueryParserTestHelper.match(ntriple, query));
  }

  @Test
  public void testNTripleQuerySimpleImplicitTriple()
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    final String ntriple = "<http://s.org> <http://p.org> <http://o.org> .";
    final String query = " <http://s.org> <http://p.org> <http://o.org>";

    assertTrue(TupleQueryParserTestHelper.matchImplicit(ntriple, query));
  }

  /**
   * Test method for {@link org.sindice.solr.plugins.analysis.NTripleStandardAnalyzer#RDFNtripleStandardAnalyzer()}.
   */
  @Test
  public void testNTripleQueryWildcard()
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    final String ntriple = "<http://s.org> <http://p.org> <http://o.org> .";
    final String query1 = " <http://s.org> <http://p.org> *";
    final String query2 = " <http://s.org> * <http://o.org>";
    final String query3 = " * <http://p.org> <http://o.org>";
    final String query4 = "<http://s.org> * * ";
    final String query5 = "* <http://p.org> * ";
    final String query6 = "* * <http://o.org> ";

    assertTrue(TupleQueryParserTestHelper.match(ntriple, query1));
    assertTrue(TupleQueryParserTestHelper.match(ntriple, query2));
    assertTrue(TupleQueryParserTestHelper.match(ntriple, query3));
    assertTrue(TupleQueryParserTestHelper.match(ntriple, query4));
    assertTrue(TupleQueryParserTestHelper.match(ntriple, query5));
    assertTrue(TupleQueryParserTestHelper.match(ntriple, query6));
  }

  /**
   * Test method for {@link org.sindice.solr.plugins.analysis.NTripleStandardAnalyzer#RDFNtripleStandardAnalyzer()}.
   */
  @Test
  public void testNTripleQueryWildcardFalse()
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    final String ntriple = "<http://s.org> <http://p.org> <http://o.org> ";
    final String query = " * * *";

    assertFalse(TupleQueryParserTestHelper.match(ntriple, query));
  }

  /**
   * Test method for {@link org.sindice.solr.plugins.analysis.NTripleStandardAnalyzer#RDFNtripleStandardAnalyzer()}.
   */
  @Test
  public void testNTripleQueryPropertyLiteral()
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    final String ntriple = "<http://s.org> <http://p.org> \"a simple literal\" .";
    final String query = "* <http://p.org> \"a simple literal\"";

    assertTrue(TupleQueryParserTestHelper.match(ntriple, query));
  }

  /**
   * Test method for {@link org.sindice.solr.plugins.analysis.NTripleStandardAnalyzer#RDFNtripleStandardAnalyzer()}.
   * Test if an URIPattern is correctly executed
   */
  @Test
  public void testNTripleQueryURIPattern1()
  throws Exception {
    final String ntriple = "<http://s.org> <http://p.org> \"literal\" .";
    final String query1 = "<http://s.org OR news://s.org> <http://p.org> 'literal'";
    final String query2 = "<http://s.org || news://s.org> <http://p.org> 'literal'";
    final String query3 = "<http://s.org && news://s.org> <http://p.org> 'literal'";
    final String query4 = "<NOT http://s.org || news://s.org> <http://p.org> 'literal'";
    final String query5 = "* <http://p1.org OR http://p2.org OR http://p3.org OR http://p.org> *";

    assertTrue(TupleQueryParserTestHelper.match(ntriple, query1));
    assertTrue(TupleQueryParserTestHelper.match(ntriple, query2));
    assertFalse(TupleQueryParserTestHelper.match(ntriple, query3));
    assertFalse(TupleQueryParserTestHelper.match(ntriple, query4));
    assertTrue(TupleQueryParserTestHelper.match(ntriple, query5));
  }

  /**
   * Test method for {@link org.sindice.solr.plugins.analysis.NTripleStandardAnalyzer#RDFNtripleStandardAnalyzer()}.
   * Test that a parsing in the Query Builder happened
   * @throws Exception
   */
  @Test(expected=ParseException.class)
  public void testNTripleQueryBuilderError()
  throws Exception {
    final String ntriple = "<http://s.org> <http://p.org> \"test simple literal\" .";
    final String query1 = "<http://s OR news://s OR> <http://p.org> 'literal'";
    final String query2 = "<http://s OR news://s> <http://p.org> 'test & literal'";

    TupleQueryParserTestHelper.match(ntriple, query1);
    TupleQueryParserTestHelper.match(ntriple, query2);
  }

  /**
   * Test method for {@link org.sindice.solr.plugins.analysis.NTripleStandardAnalyzer#RDFNtripleStandardAnalyzer()}.
   */
  @Test
  public void testNTripleQueryPropertyLiteralPattern1()
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    final String ntriple = "<http://s.org> <http://p.org> \"Test for literal pattern\" .";
    final String query = "* <http://p.org> 'literal'";

    assertTrue(TupleQueryParserTestHelper.match(ntriple, query));
  }

  /**
   * Test method for {@link org.sindice.solr.plugins.analysis.NTripleStandardAnalyzer#RDFNtripleStandardAnalyzer()}.
   */
  @Test
  public void testNTripleQueryPropertyLiteralPattern2()
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    final String ntriple = "<http://s.org> <http://p.org> \"Test for literal pattern\" .";
    final String query = "* <http://p.org> 'Test && literal'";

    assertTrue(TupleQueryParserTestHelper.match(ntriple, query));
  }

  /**
   * SRN-99
   */
  @Test
  public void testNTripleQueryPropertyLiteralPattern3()
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    final String ntriple1 = "<http://s.org> <http://p.org> \"Test for literal pattern\" .";
    final String ntriple2 = "<http://s.org> <http://p.org> \"Test for pattern\" .";
    final String query1 = "* <http://p.org> 'Test AND ((literal OR uri) AND pattern)'";
    final String query2 = "* <http://p.org> 'Test AND ((literal OR uri OR resource) AND (pattern OR patterns OR query))'";

    assertTrue(TupleQueryParserTestHelper.match(ntriple1, query1));
    assertFalse(TupleQueryParserTestHelper.match(ntriple2, query1));
    assertTrue(TupleQueryParserTestHelper.match(ntriple1, query1));
    assertFalse(TupleQueryParserTestHelper.match(ntriple2, query2));
  }

  /**
   * Test method for {@link org.sindice.solr.plugins.analysis.NTripleStandardAnalyzer#RDFNtripleStandardAnalyzer()}.
   */
  @Test
  public void testNTripleSubjectLiteral()
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    final String ntriple = "<http://s.org> <http://p.org> \"A simple literal\" .";
    final String query = "<http://s.org> * \"A simple literal\"";

    assertTrue(TupleQueryParserTestHelper.match(ntriple, query));
  }

  /**
   * Test method for {@link org.sindice.solr.plugins.analysis.NTripleStandardAnalyzer#RDFNtripleStandardAnalyzer()}.
   */
  @Test
  public void testNTripleSubjectLiteralPattern1()
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    final String ntriple = "<http://s.org> <http://p.org> \"Test for literal pattern\" .";
    final String query = "<http://s.org> * 'literal'";

    assertTrue(TupleQueryParserTestHelper.match(ntriple, query));
  }

  /**
   * Test method for {@link org.sindice.solr.plugins.analysis.NTripleStandardAnalyzer#RDFNtripleStandardAnalyzer()}.
   */
  @Test
  public void testNTripleSubjectLiteralPattern2()
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    final String ntriple = "<http://s.org> <http://p.org> \"Test for literal pattern\" .";
    final String query = "<http://s.org> * 'Test && literal'";

    assertTrue(TupleQueryParserTestHelper.match(ntriple, query));
  }

  /**
   * Test method for {@link org.sindice.solr.plugins.analysis.NTripleStandardAnalyzer#RDFNtripleStandardAnalyzer()}.
   */
  @Test
  public void testNTripleQueryLiteralPattern()
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    final String ntriple = "<http://s.org> <http://p.org> \"Blue Socks\" .";
    final String query1 = "* <http://p.org> 'Blue'";
    final String query2 = "* <http://p.org> 'Socks'";
    final String query3 = "* <http://p.org> '\"Blue Socks\"'";

    assertTrue(TupleQueryParserTestHelper.match(ntriple, query1));
    assertTrue(TupleQueryParserTestHelper.match(ntriple, query2));
    assertTrue(TupleQueryParserTestHelper.match(ntriple, query3));
  }

  /**
   * Test method for {@link org.sindice.solr.plugins.analysis.NTripleStandardAnalyzer#RDFNtripleStandardAnalyzer()}.
   */
  @Test
  public void testNTripleQueryTriplePatternDisjunction()
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    final String ntriple1 = "<http://s.org> <http://p1.org> \"literal\" .\n" +
    		"<http://s.org> <http://p2.org> <http://o2.org> .";
    final String ntriple2 = "<http://s.org> <http://p.org> \"literals\" .\n" +
    "<http://s.org> <http://p2.org> <http://o.org> .";
    final String query = "<http://s.org> * \"literal\" OR * <http://p2.org> <http://o.org>";

    assertTrue(TupleQueryParserTestHelper.match(ntriple1, query));
    assertTrue(TupleQueryParserTestHelper.match(ntriple2, query));
  }

  /**
   * Test method for {@link org.sindice.solr.plugins.analysis.NTripleStandardAnalyzer#RDFNtripleStandardAnalyzer()}.
   */
  @Test
  public void testNTripleQueryTriplePatternConjunction()
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    final String ntriples = "<http://s.org> <http://p1.org> \"literal\" .\n" +
    		"<http://s.org> <http://p2.org> <http://o.org> .\n";
    final String query = "<http://s.org> * 'literal' AND * <http://p2.org> <http://o.org>";

    assertTrue(TupleQueryParserTestHelper.match(ntriples, query));
  }

  /**
   * Test method for {@link org.sindice.solr.plugins.analysis.NTripleStandardAnalyzer#RDFNtripleStandardAnalyzer()}.
   */
  @Test
  public void testNTripleQueryTriplePatternComplement1()
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    final String ntriples1 = "<http://s.org> <http://p1.org> \"literal\" .\n" +
      "<http://s.org> <http://p2.org> <http://o.org> .\n";
    final String ntriples2 = "<http://s.org> <http://p1.org> \"literal\" .\n" +
      "<http://s.org> <http://p2.org> <http://o2> .\n";
    final String query = "<http://s.org> * 'literal' - * <http://p2.org> <http://o.org>";

    assertFalse(TupleQueryParserTestHelper.match(ntriples1, query));
    assertTrue(TupleQueryParserTestHelper.match(ntriples2, query));
  }

  /**
   * Test method for {@link org.sindice.solr.plugins.analysis.NTripleStandardAnalyzer#RDFNtripleStandardAnalyzer()}.
   * SRN-91
   */
  @Test
  public void testNTripleQueryTriplePatternComplement2()
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    final String ntriples1 = "<http://s.org> <http://p1.org> \"literal\" .\n" +
      "<http://s.org> <http://p2.org> <http://o.org> .\n";
    final String ntriples2 = "<http://s.org> <http://p1.org> \"literal\" .\n" +
      "<http://s.org> <http://p2.org> <http://o2> .\n";
    final String query = "<http://s.org> * 'literal' NOT * <http://p2.org> <http://o.org>";

    assertFalse(TupleQueryParserTestHelper.match(ntriples1, query));
    assertTrue(TupleQueryParserTestHelper.match(ntriples2, query));
  }

  /**
   * Test method for {@link org.sindice.solr.plugins.analysis.NTripleStandardAnalyzer#RDFNtripleStandardAnalyzer()}.
   */
  @Test
  public void testNTripleQueryLineFeed()
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    final String ntriple = "<http://s.org> <http://p1.org> \"literal\" .\n" +
    		"<http://s.org> <http://p2.org> <http://o.org> .";
    final String query = "<http://s.org> * 'literal' AND\r\n * <http://p2.org> \n\r \n <http://o.org>";

    assertTrue(TupleQueryParserTestHelper.match(ntriple, query));
  }

  @Test
  public void testNTripleMultiFieldQuery()
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    final Map<String, Float> boosts = new HashMap<String, Float>();
    boosts.put(TupleTestHelper._defaultField, 1.0f);
    boosts.put(TupleTestHelper._implicitField, 1.0f);

    Map<String, String> ntriples = new HashMap<String, String>();
    ntriples.put(TupleTestHelper._defaultField, "<http://s.org> <http://p1.org> \"literal\" .\n");
    ntriples.put(TupleTestHelper._implicitField, "<http://s.org> <http://p2.org> <http://o.org> .\n");
    final String query = "<http://s.org> * 'literal' AND\r\n * <http://p2.org> \n\r \n <http://o.org>";

    // Should not match, no field content is matching the two triple patterns
    assertFalse(TupleQueryParserTestHelper.match(ntriples, boosts, query, false));

    ntriples = new HashMap<String, String>();
    ntriples.put(TupleTestHelper._defaultField, "<http://s.org> <http://p1.org> \"literal\" .\n" +
    		"<http://s.org> <http://p2.org> <http://o.org> .\n");
    ntriples.put(TupleTestHelper._implicitField, "<http://s.org> <http://p1.org> \"literal\" .\n" +
    		"<http://s.org> <http://p2.org> <http://o.org> .\n");

    // Should match, the two field content are matching the two triple patterns
    assertTrue(TupleQueryParserTestHelper.match(ntriples, boosts, query, false));

    ntriples = new HashMap<String, String>();
    ntriples.put(TupleTestHelper._defaultField, "<http://s.org> <http://p1.org> \"literal\" .\n" +
        "<http://s.org> <http://p2.org> <http://o.org> .\n");
    ntriples.put(TupleTestHelper._implicitField, "<http://s.org> <http://p2.org> <http://o.org> .\n");

    // Should match, one of the field content is matching the two triple patterns
    assertTrue(TupleQueryParserTestHelper.match(ntriples, boosts, query, false));
  }

  @Test
  public void testScatteredNTripleMultiFieldQuery()
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    final Map<String, Float> boosts = new HashMap<String, Float>();
    boosts.put(TupleTestHelper._defaultField, 1.0f);
    boosts.put(TupleTestHelper._implicitField, 1.0f);
    Map<String, String> ntriples = new HashMap<String, String>();
    ntriples.put(TupleTestHelper._defaultField, "<http://s.org> <http://p1.org> \"literal\" .\n");
    ntriples.put(TupleTestHelper._implicitField, "<http://s.org> <http://p2.org> <http://o.org> .\n");
    final String query = "<http://s.org> * 'literal' AND\r\n * <http://p2.org> \n\r \n <http://o.org>";

    // Should match, the two field content are matching either one of the two triple patterns
    assertTrue(TupleQueryParserTestHelper.match(ntriples, boosts, query, true));

    ntriples = new HashMap<String, String>();
    ntriples.put(TupleTestHelper._defaultField, "<http://s.org> <http://p1.org> \"literal\" .\n");
    ntriples.put(TupleTestHelper._implicitField, "<http://s.org> <http://p2.org> <http://o2> .\n");

    // Should not match, only the first field content is matching one triple pattern
    assertFalse(TupleQueryParserTestHelper.match(ntriples, boosts, query, true));
  }
  
  /**
   * Test different datatype per field
   * 
   * @throws CorruptIndexException
   * @throws LockObtainFailedException
   * @throws IOException
   * @throws ParseException
   */
  @Test
  public void testScatteredNTripleMultiFieldQuery2()
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    TupleQueryParserTestHelper.registerTokenConfig(TupleTestHelper._defaultField,
      "number", new IntNumericAnalyzer(4));
    TupleQueryParserTestHelper.registerTokenConfig(TupleTestHelper._implicitField,
      "number", new IntNumericAnalyzer(1));
    TupleQueryParserTestHelper.registerTokenConfig(TupleTestHelper._defaultField,
      "mynumber", new FloatNumericAnalyzer(4));
    
    TupleQueryParserTestHelper.registerTokenConfig(TupleTestHelper._defaultField,
      "numberIL", new IntNumericAnalyzer(4));
    TupleQueryParserTestHelper.registerTokenConfig(TupleTestHelper._implicitField,
      "numberIL", new LongNumericAnalyzer(4));
    
    final Map<String, Float> boosts = new HashMap<String, Float>();
    boosts.put(TupleTestHelper._defaultField, 1.0f);
    boosts.put(TupleTestHelper._implicitField, 1.0f);
    Map<String, String> ntriples = new HashMap<String, String>();
    ntriples.put(TupleTestHelper._defaultField, "<http://s.org> <http://p1.org> \"42\"^^<number> .\n");
    ntriples.put(TupleTestHelper._implicitField, "<http://s.org> <http://p2.org> \"14\"^^<number> .\n");
    String query = "<http://s.org> * '[1 TO 100]'^^<number> AND\r\n * <http://p2.org> \n\r \n '[13 TO 20]'^^<number>";

    assertTrue(TupleQueryParserTestHelper.match(ntriples, boosts, query, true));

    ntriples.put(TupleTestHelper._defaultField, "<http://s.org> <http://p1.org> \"42\"^^<numberIL> .\n");
    ntriples.put(TupleTestHelper._implicitField, "<http://s.org> <http://p2.org> \"14\"^^<numberIL> .\n");
    query = "<http://s.org> * '[1 TO 100]'^^<numberIL> AND\r\n * <http://p2.org> \n\r \n '[13 TO 20]'^^<numberIL>";

    assertTrue(TupleQueryParserTestHelper.match(ntriples, boosts, query, true));
    
    boolean fail = false;
    
    // mynumber datatype is not registered in the implicit field
    query = "<http://s.org> * '[1 TO 100]'^^<int> AND\r\n <http://s.org> * \n\r \n '[13.5 TO 15.5]'^^<mynumber>";
    try {
      TupleQueryParserTestHelper.match(ntriples, boosts, query, true);      
    } catch (ParseException e) {
      fail = true;
    }
    assertTrue(fail);

    // mynumber datatype is not registered in the default field
    fail = false;
    TupleQueryParserTestHelper.unRegisterTokenConfig(TupleTestHelper._defaultField, "mynumber");
    TupleQueryParserTestHelper.registerTokenConfig(TupleTestHelper._implicitField,
      "mynumber", new IntNumericAnalyzer(4));
    query = "<http://s.org> * '[1 TO 100]'^^<mynumber> AND\r\n <http://s.org> * \n\r \n '[13 TO 20]'^^<number>";
    try {
      TupleQueryParserTestHelper.match(ntriples, boosts, query, true);      
    } catch (ParseException e) {
      fail = true;
    }
    assertTrue(fail);
  }

  @Test
  public void testScatteredNTripleMultiFieldQueryScore()
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    Map<String, Float> boosts = new HashMap<String, Float>();
    boosts.put(TupleTestHelper._defaultField, 1.0f);
    boosts.put(TupleTestHelper._implicitField, 1.0f);
    final Map<String, String> ntriples = new HashMap<String, String>();
    ntriples.put(TupleTestHelper._defaultField, "<http://s.org> <http://p1.org> \"literal\" .\n");
    ntriples.put(TupleTestHelper._implicitField, "<http://s.org> <http://p2.org> <http://o.org> .\n");
    final String query = "<http://s.org> * 'literal' AND\r\n * <http://p2.org> \n\r \n <http://o.org>";

    final float score1 = TupleQueryParserTestHelper.getScore(ntriples, boosts, query, true);

    boosts = new HashMap<String, Float>();
    boosts.put(TupleTestHelper._defaultField, 1.0f);
    boosts.put(TupleTestHelper._implicitField, 0.6f);
    final float score2 = TupleQueryParserTestHelper.getScore(ntriples, boosts, query, true);

    assertTrue(score1 + " > " + score2, score1 > score2);
  }

  @Test
  public void testFuzzyQuery()
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    String query = "<http://stephane.org> * 'literale~'";

    /*
     * match because the distance between literale and literaleme is 2, which is
     * within the maxEdits == 2.
     */
    String ntriple = "<http://stephane.org> <http://p1.org> \"literaleme\" .\n";
    assertTrue(TupleQueryParserTestHelper.match(ntriple, query));

    /*
     * do no match because the distance between literal and literalement is 4
     */
    ntriple = "<http://stephane.org> <http://p1.org> \"literalemen\" .\n";
    assertFalse(TupleQueryParserTestHelper.match(ntriple, query));

    // it doesn't matches with a default similarity of 0.8, because the maxEdits
    // is then equals to 1 (1.8, converted to int)
    query = "<http://stephane.org> * 'literalem~0.8'";
    assertFalse(TupleQueryParserTestHelper.match(ntriple, query));

    /*
     * Matching within an URI
     */
    ntriple = "<http://sw.deri.org/aidanh> <http://p.org> <http://o.org> .";
    query = " <http://sw.deri.org/aidan~> <http://p.org> <http://o.org>";
    assertTrue(TupleQueryParserTestHelper.match(ntriple, query));

    // too many edits, do not match
    query = " <http://sw.deri~> <http://p.org> <http://o.org>";
    assertFalse(TupleQueryParserTestHelper.match(ntriple, query));
    
    // first tilde is escaped, not the second one
    ntriple = "<http://sw.deri.org/~aidanh/> <http://p.org> <http://o.org> .";
    query = "<http://sw.deri.org/~aida~> <http://p.org> <http://o.org>";
    assertTrue(TupleQueryParserTestHelper.match(ntriple, query));
  }

  @Test
  public void testPrefixQuery()
  throws Exception {
    final String ntriple = "<http://stephane.org> <http://p1.org> \"literaleme\" .\n";
    String query = "<http://stephane.org> * 'lit*'";

    assertTrue(TupleQueryParserTestHelper.match(ntriple, query));

    query = "<http://steph*> * \"literaleme\"";
    assertTrue(TupleQueryParserTestHelper.match(ntriple, query));

    query = "<http://stephane.org> * \"lita*\"";
    assertFalse(TupleQueryParserTestHelper.match(ntriple, query));
  }

  @Test
  public void testTermRangeQuery()
  throws Exception {
    final String ntriple = "<http://stephane.org> <http://p1.org> \"literal laretil\" .\n";

    String query = "<http://stephane.org> * '[bla TO mla]'";
    assertTrue(TupleQueryParserTestHelper.match(ntriple, query));

    query = "<http://stephane.org> * '[bla TO k]'";
    assertFalse(TupleQueryParserTestHelper.match(ntriple, query));
  }

  @Test
  public void testWildcardQuery()
  throws Exception {
    final String ntriple = "<http://stephane.campinas> <http://p1.org> \"literal laretil\" .\n";

    String query = "<http://stephane.campinas> * 'li*al'";
    assertTrue(TupleQueryParserTestHelper.match(ntriple, query));

    query = "<http://stephane.campinas> * 'liter?l'";
    assertTrue(TupleQueryParserTestHelper.match(ntriple, query));

    query = "<http://st*e.ca*as> * 'literal'";
    assertTrue(TupleQueryParserTestHelper.match(ntriple, query));
    query = "<http://stephane.ca*os> * 'literal'";
    assertFalse(TupleQueryParserTestHelper.match(ntriple, query));
  }

  /**
   * Numeric ranges get processed with {@link NodeNumericRangeQueryNodeProcessor}.
   * Single numeric values are processed with {@link NodeNumericQueryNodeProcessor}.
   * @throws Exception
   */
  @Test
  public void testNumericQuery()
  throws Exception {
    TupleQueryParserTestHelper.registerTokenConfig(TupleTestHelper._defaultField,
      "int4", new IntNumericAnalyzer(4));
    TupleQueryParserTestHelper.registerTokenConfig(TupleTestHelper._defaultField,
      "float4", new FloatNumericAnalyzer(4));

    // test for incorrect numeric values
    String ntriple = "<http://stephane.org> <http://p1.org> \"numeric\"^^<int4> .\n";
    String query = "<http://stephane.org> * '[10 TO 2000]'^^<int4>";
    try {
      TupleQueryParserTestHelper.match(ntriple, query);
      fail();
    } catch (NumberFormatException e) { // fail when indexing
    }
    ntriple = "<http://stephane.org> <http://p1.org> \"500\"^^<int4> .\n";
    query = "<http://stephane.org> * '[10 TO bla]'^^<int4>";
    try {
      TupleQueryParserTestHelper.match(ntriple, query);
      fail();
    } catch (ParseException e) { // fail when processing the query
    }

    /*
     * Test for integer
     */
    ntriple = "<http://stephane.org> <http://p1.org> \"500\"^^<int4> .\n";
    query = "<http://stephane.org> * '[10 TO 2000]'^^<int4>";
    assertTrue(TupleQueryParserTestHelper.match(ntriple, query));

    // test for wildcard bounds
    query = "<http://stephane.org> * '[* TO 2000]'^^<int4>";
    assertTrue(TupleQueryParserTestHelper.match(ntriple, query));
    query = "<http://stephane.org> * '[100 TO *]'^^<int4>";
    assertTrue(TupleQueryParserTestHelper.match(ntriple, query));
    query = "<http://stephane.org> * '[550 TO *]'^^<int4>";
    assertFalse(TupleQueryParserTestHelper.match(ntriple, query));
    query = "<http://stephane.org> * '[* TO 400]'^^<int4>";
    assertFalse(TupleQueryParserTestHelper.match(ntriple, query));
    
    // boolean of ranges
    ntriple = "<http://stephane.org> <http://p1.org> \"500\"^^<int4> .\n";
    query = "<http://stephane.org> * '[900 TO 2000] OR [5000 TO 20000]'^^<int4>";
    assertFalse(TupleQueryParserTestHelper.match(ntriple, query));

    ntriple = "<http://stephane.org> <http://p1.org> \"500\"^^<int4> \"6420\"^^<int4> .\n";
    assertTrue(TupleQueryParserTestHelper.match(ntriple, query));

    /*
     * Test for float
     */
    ntriple = "<http://stephane.org> <http://p1.org> \"3.42\"^^<float4> .\n";
    query = "<http://stephane.org> * '[3.3 TO 3.5]'^^<float4>";
    assertTrue(TupleQueryParserTestHelper.match(ntriple, query));

    query = "<http://stephane.org> * '[3.45 TO 3.5]'^^<float4>";
    assertFalse(TupleQueryParserTestHelper.match(ntriple, query));

    query = "<http://stephane.org> * '3.42'^^<float4>";
    assertTrue(TupleQueryParserTestHelper.match(ntriple, query));

    query = "<http://stephane.org> * '42.42 OR [1 TO 5]'^^<float4>";
    assertTrue(TupleQueryParserTestHelper.match(ntriple, query));

    query = "<http://stephane.org> * '42.42 OR [10 TO 50]'^^<float4>";
    assertFalse(TupleQueryParserTestHelper.match(ntriple, query));
    
    // cannot match, the value was indexed using a float
    ntriple = "<http://stephane.org> <http://p1.org> \"5\"^^<float4> .\n";
    query = "<http://stephane.org> * '[2 TO 20]'^^<int4>";
    assertFalse(TupleQueryParserTestHelper.match(ntriple, query));

    /*
     * Test on a value without datatype
     */
    query = "<http://stephane.org> * '3.42'";
    assertFalse(TupleQueryParserTestHelper.match(ntriple, query));
  }

}

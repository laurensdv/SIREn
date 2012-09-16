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
package org.sindice.siren.qparser.keyword.parser;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.Operator;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.LuceneTestCase;
import org.junit.Before;
import org.junit.Test;
import org.sindice.siren.qparser.keyword.KeywordQParserImpl;

public class KeywordQParserTest {

  private Map<String, Float> boosts;
  private KeywordQParserImpl parser;

  @Before
  public void setUp() throws Exception {
    final Analyzer analyzer = new WhitespaceAnalyzer(LuceneTestCase.TEST_VERSION_CURRENT);
    final PerFieldAnalyzerWrapper analyzerWrapper = new PerFieldAnalyzerWrapper(analyzer);
    boosts = new HashMap<String, Float>();
    boosts.put("explicit-content", 1.0f);
    boosts.put("label", 2.5f);
    parser = new KeywordQParserImpl(analyzerWrapper, boosts, false);
  }

  @Test
  public void testSingleWord() throws ParseException {
    final Query q = parser.parse("hello");
    assertEquals("documentQuery(explicit-content:hello) documentQuery(label:hello^2.5)", q.toString());
  }

  @Test
  public void testDistinctAnalyzer() throws ParseException {
    final Analyzer analyzer = new WhitespaceAnalyzer(LuceneTestCase.TEST_VERSION_CURRENT);
    final HashMap<String, Analyzer> fields = new HashMap<String, Analyzer>();
    fields.put("label", new StandardAnalyzer(LuceneTestCase.TEST_VERSION_CURRENT));
    final PerFieldAnalyzerWrapper analyzerWrapper = new PerFieldAnalyzerWrapper(analyzer, fields);
    final KeywordQParserImpl parser = new KeywordQParserImpl(analyzerWrapper, boosts, false);
    final Query q = parser.parse("hELlo");
    assertEquals("documentQuery(explicit-content:hELlo) documentQuery(label:hello^2.5)", q.toString());
  }

  @Test
  public void testMultipleWords() throws ParseException {
    final Query q = parser.parse("hello world");
    assertEquals("(documentQuery(explicit-content:hello) documentQuery(label:hello^2.5)) " +
                 "(documentQuery(explicit-content:world) documentQuery(label:world^2.5))", q.toString());
  }

  @Test
  public void testURIsWithDefaultOR() throws ParseException {
    parser.setDefaultOperator(Operator.OR);
    final Query q = parser.parse("http://www.google.com http://hello.world#me");
    assertEquals("(documentQuery(explicit-content:http://www.google.com) documentQuery(label:http://www.google.com^2.5)) " +
                 "(documentQuery(explicit-content:http://hello.world#me) documentQuery(label:http://hello.world#me^2.5))",
                 q.toString());
  }

  @Test
  public void testURIsWithDefaultAND() throws ParseException {
    parser.setDefaultOperator(Operator.AND);
    final Query q = parser.parse("http://www.google.com http://hello.world#me");
    assertEquals("+(documentQuery(explicit-content:http://www.google.com) documentQuery(label:http://www.google.com^2.5)) " +
                 "+(documentQuery(explicit-content:http://hello.world#me) documentQuery(label:http://hello.world#me^2.5))",
                 q.toString());
  }

  @Test
  public void testCompoundQuery() throws ParseException {
    final Query q = parser.parse("http://www.google.com +hello -world");
    assertEquals("(documentQuery(explicit-content:http://www.google.com) documentQuery(label:http://www.google.com^2.5)) " +
        "+(documentQuery(explicit-content:hello) documentQuery(label:hello^2.5)) " +
        "-(documentQuery(explicit-content:world) documentQuery(label:world^2.5))", q.toString());
  }

  @Test
  public void testCustomFieldQuery() throws ParseException {
    final Query q = parser.parse("domain:dbpedia data-source:DUMP");
    assertEquals("documentQuery(domain:dbpedia) documentQuery(data-source:DUMP)", q.toString());
  }

  @Test
  public void testFormatQuery() throws Exception {
    final Query q = parser.parse("format:MICROFORMAT");
    assertEquals("documentQuery(format:MICROFORMAT)", q.toString());
  }

  @Test(expected=ParseException.class)
  public void testFuzzyQuery() throws Exception {
    parser.parse("michele~0.9");
  }

  @Test(expected=ParseException.class)
  public void testWildcardQuery() throws Exception {
    parser.parse("miche*");
  }

  @Test
  public void testWildcardInURI() throws Exception {
    Query q = parser.parse("http://example.com/~foo=bar");
    assertEquals("documentQuery(explicit-content:http://example.com/~foo=bar) " +
    		"documentQuery(label:http://example.com/~foo=bar^2.5)", q.toString());
    
    q = parser.parse("http://example.com/?foo=bar");
    assertEquals("documentQuery(explicit-content:http://example.com/?foo=bar) " +
      "documentQuery(label:http://example.com/?foo=bar^2.5)", q.toString());
  }

  @Test
  public void testEncoding() throws Exception {
    final Query q = parser.parse("möller");
    assertEquals("documentQuery(explicit-content:möller) documentQuery(label:möller^2.5)", q.toString());
  }

  @Test
  public void testDashedURI() throws Exception {
    final String url = "http://semantic-conference.com/session/569/";
    assertEquals("documentQuery(explicit-content:http://semantic-conference.com/session/569/) " +
                   "documentQuery(label:http://semantic-conference.com/session/569/^2.5)", parser.parse(url).toString());
  }

  @Test
  public void testDisabledFieldQuery() throws ParseException {
    final Map<String, Float> boosts = new HashMap<String, Float>();
    boosts.put("explicit-content", 1.0f);
    final Analyzer analyzer = new WhitespaceAnalyzer(LuceneTestCase.TEST_VERSION_CURRENT);
    final KeywordQParserImpl parser = new KeywordQParserImpl(analyzer, boosts, true);
    
    final Query q1 = parser.parse("+foaf:name -foaf\\:person domain:dbpedia.org http://test.org/ http://test2.org/");
    final Query q2 = parser.parse("+foaf:name http://test.org/ -foaf\\:person domain:dbpedia.org http://test2.org/");
    final Query q3 = parser.parse("+foaf:name http://test.org/ -foaf\\:person domain:dbpedia.org");
    final Query q4 = parser.parse("http://www.w3.org/1999/xhtml/vocab#alternate +foaf:name -foaf\\:person domain:dbpedia.org nothingToEscape");
    assertEquals("+documentQuery(explicit-content:foaf:name) " +
                 "-documentQuery(explicit-content:foaf\\:person) " +
                 "documentQuery(explicit-content:domain:dbpedia.org) " +
                 "documentQuery(explicit-content:http://test.org/) " +
                 "documentQuery(explicit-content:http://test2.org/)", q1.toString());
    assertEquals("+documentQuery(explicit-content:foaf:name) " +
                 "documentQuery(explicit-content:http://test.org/) " +
                 "-documentQuery(explicit-content:foaf\\:person) " +
                 "documentQuery(explicit-content:domain:dbpedia.org) " +
                 "documentQuery(explicit-content:http://test2.org/)", q2.toString());
    assertEquals("+documentQuery(explicit-content:foaf:name) " +
                 "documentQuery(explicit-content:http://test.org/) " +
                 "-documentQuery(explicit-content:foaf\\:person) " +
                 "documentQuery(explicit-content:domain:dbpedia.org)", q3.toString());
    assertEquals("documentQuery(explicit-content:http://www.w3.org/1999/xhtml/vocab#alternate) " +
                 "+documentQuery(explicit-content:foaf:name) " +
                 "-documentQuery(explicit-content:foaf\\:person) " +
                 "documentQuery(explicit-content:domain:dbpedia.org) " +
                 "documentQuery(explicit-content:nothingToEscape)", q4.toString());
  }

  @Test
  public void testMailtoURI()
  throws Exception {
    final Map<String, Float> boosts = new HashMap<String, Float>();
    boosts.put("explicit-content", 1.0f);
    final Analyzer analyzer = new WhitespaceAnalyzer(LuceneTestCase.TEST_VERSION_CURRENT);
    KeywordQParserImpl parser = new KeywordQParserImpl(analyzer, boosts, false);
    
    Query q = parser.parse("mailto:stephane.campinas@deri.org");
    assertEquals("documentQuery(explicit-content:mailto:stephane.campinas@deri.org)", q.toString());
    
    parser = new KeywordQParserImpl(analyzer, boosts, true);
    q = parser.parse("mailto:stephane.campinas@deri.org domain:dbpedia.org");
    assertEquals("documentQuery(explicit-content:mailto:stephane.campinas@deri.org) " +
                 "documentQuery(explicit-content:domain:dbpedia.org)", q.toString());
  }

  @Test
  public void testDisabledFieldQueryExpanded() throws ParseException {
    final Map<String, Float> boosts = new HashMap<String, Float>();
    boosts.put("explicit-content", 1.0f);
    boosts.put("label", 1.0f);
    final Analyzer analyzer = new WhitespaceAnalyzer(LuceneTestCase.TEST_VERSION_CURRENT);
    final KeywordQParserImpl parser = new KeywordQParserImpl(analyzer, boosts, true);
    final Query q = parser.parse("+foaf:name http://test.org/");
    assertEquals("+(documentQuery(explicit-content:foaf:name) documentQuery(label:foaf:name)) " +
        "(documentQuery(explicit-content:http://test.org/) documentQuery(label:http://test.org/))", q.toString());
  }

  // SRN-106: Query expansion does not respect unary operator
  @Test
  public void testUnaryOperator() throws ParseException {
    final Map<String, Float> boosts = new HashMap<String, Float>();
    boosts.put("explicit-content", 1.0f);
    final Analyzer analyzer = new WhitespaceAnalyzer(LuceneTestCase.TEST_VERSION_CURRENT);
    final KeywordQParserImpl parser = new KeywordQParserImpl(analyzer, boosts, true);

    parser.setDefaultOperator(Operator.AND);
    final Query q = parser.parse("+mike +conlon vivo");
    assertEquals("+documentQuery(explicit-content:mike) +documentQuery(explicit-content:conlon) documentQuery(explicit-content:vivo)",
                 q.toString());
  }

  // SRN-106: Query expansion does not respect unary operator
  @Test
  public void testUnaryOperatorMultiField() throws ParseException {
    parser.setDefaultOperator(Operator.AND);
    final Query q = parser.parse("+mike +conlon vivo");
    assertEquals("+(documentQuery(explicit-content:mike) documentQuery(label:mike^2.5)) " +
                 "+(documentQuery(explicit-content:conlon) documentQuery(label:conlon^2.5)) " +
                 "(documentQuery(explicit-content:vivo) documentQuery(label:vivo^2.5))",
                 q.toString());
  }

  @Test
  public void testNestedGroups() throws ParseException {
    final Map<String, Float> boosts = new HashMap<String, Float>();
    boosts.put("explicit-content", 1.0f);
    final Analyzer analyzer = new WhitespaceAnalyzer(LuceneTestCase.TEST_VERSION_CURRENT);
    final KeywordQParserImpl parser = new KeywordQParserImpl(analyzer, boosts, true);
    parser.setDefaultOperator(Operator.AND);

    final Query q = parser.parse("Test AND ((literal OR uri OR resource) AND (pattern OR patterns OR query))");
    assertEquals("+documentQuery(explicit-content:Test) " +
                 "+(+(documentQuery(explicit-content:literal) documentQuery(explicit-content:uri) documentQuery(explicit-content:resource)) " +
                 "+(documentQuery(explicit-content:pattern) documentQuery(explicit-content:patterns) documentQuery(explicit-content:query)))",
                 q.toString());
  }

}

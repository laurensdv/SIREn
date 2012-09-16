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
package org.sindice.siren.qparser.ntriple.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;
import org.sindice.siren.qparser.ntriple.query.SimpleNTripleQueryBuilder;
import org.sindice.siren.qparser.ntriple.query.model.BinaryClause;
import org.sindice.siren.qparser.ntriple.query.model.EmptyQuery;
import org.sindice.siren.qparser.ntriple.query.model.Literal;
import org.sindice.siren.qparser.ntriple.query.model.LiteralPattern;
import org.sindice.siren.qparser.ntriple.query.model.NestedClause;
import org.sindice.siren.qparser.ntriple.query.model.Operator;
import org.sindice.siren.qparser.ntriple.query.model.SimpleExpression;
import org.sindice.siren.qparser.ntriple.query.model.TriplePattern;
import org.sindice.siren.qparser.ntriple.query.model.URIPattern;
import org.sindice.siren.qparser.ntriple.query.model.Wildcard;
import org.sindice.siren.qparser.tree.NodeValue;
import org.sindice.siren.search.doc.DocumentQuery;
import org.sindice.siren.search.node.NodeBooleanClause;
import org.sindice.siren.search.node.NodeBooleanQuery;
import org.sindice.siren.search.node.NodePhraseQuery;
import org.sindice.siren.search.node.NodeTermQuery;
import org.sindice.siren.search.node.TupleQuery;
import org.sindice.siren.util.XSDDatatype;


public class NTripleQueryBuilderTest {

  private final String  _field = "triple";

  private final static char[] XSD_ANY_URI = XSDDatatype.XSD_ANY_URI.toCharArray();
  private final static char[] XSD_STRING = XSDDatatype.XSD_STRING.toCharArray();
  
  private final static Version matchVersion = LuceneTestCase.TEST_VERSION_CURRENT;
  private final static Map<String, Analyzer> tokenConfigMap = new HashMap<String, Analyzer>();
  static {
    tokenConfigMap.put(XSDDatatype.XSD_ANY_URI,  new WhitespaceAnalyzer(matchVersion));
    tokenConfigMap.put(XSDDatatype.XSD_STRING, new WhitespaceAnalyzer(matchVersion));
  }
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {}

  /**
   * Test method for {@link org.sindice.siren.qparser.ntriple.query.SimpleNTripleQueryBuilder#visit(org.sindice.siren.qparser.ntriple.query.model.TriplePattern)}.
   * Test a TriplePattern composed of 3 URIPattern: s, p, o.
   */
  @Test
  public void testVisitTriplePattern1() {
    final TriplePattern pattern = new TriplePattern(new URIPattern(new NodeValue(XSD_ANY_URI, "s")),
      new URIPattern(new NodeValue(XSD_ANY_URI, "p")),
      new URIPattern(new NodeValue(XSD_ANY_URI, "o")));

    final SimpleNTripleQueryBuilder translator = new SimpleNTripleQueryBuilder(matchVersion, _field, tokenConfigMap);
    pattern.traverseBottomUp(translator);
    final Query dq = pattern.getQuery();

    assertTrue(dq instanceof DocumentQuery);
    assertTrue(((DocumentQuery) dq).getNodeQuery() instanceof TupleQuery);
    final TupleQuery query = (TupleQuery) ((DocumentQuery) dq).getNodeQuery();
    assertEquals(3, ((TupleQuery) query).clauses().size());

    assertTrue(((TupleQuery) query).clauses().get(0) instanceof NodeBooleanClause);
    NodeBooleanClause tupleClause = ((TupleQuery) query).clauses().get(0);
    NodeBooleanQuery cellQuery = (NodeBooleanQuery) tupleClause.getQuery();
    assertTrue(cellQuery.getClauses()[0].getQuery() instanceof NodeTermQuery);
    NodeTermQuery termQuery = (NodeTermQuery) cellQuery.getClauses()[0].getQuery();
    assertEquals(_field, termQuery.getTerm().field());
    assertEquals("s", termQuery.getTerm().text());

    assertTrue(((TupleQuery) query).clauses().get(1) instanceof NodeBooleanClause);
    tupleClause = ((TupleQuery) query).clauses().get(1);
    cellQuery = (NodeBooleanQuery) tupleClause.getQuery();
    assertTrue(cellQuery.getClauses()[0].getQuery() instanceof NodeTermQuery);
    termQuery = (NodeTermQuery) cellQuery.getClauses()[0].getQuery();
    assertEquals(_field, termQuery.getTerm().field());
    assertEquals("p", termQuery.getTerm().text());

    assertTrue(((TupleQuery) query).clauses().get(2) instanceof NodeBooleanClause);
    tupleClause = ((TupleQuery) query).clauses().get(2);
    cellQuery = (NodeBooleanQuery) tupleClause.getQuery();
    assertTrue(cellQuery.getClauses()[0].getQuery() instanceof NodeTermQuery);
    termQuery = (NodeTermQuery) cellQuery.getClauses()[0].getQuery();
    assertEquals(_field, termQuery.getTerm().field());
    assertEquals("o", termQuery.getTerm().text());
  }

  /**
   * Test method for {@link org.sindice.siren.qparser.ntriple.query.SimpleNTripleQueryBuilder#visit(org.sindice.siren.qparser.ntriple.query.model.TriplePattern)}.
   * Test a TriplePattern composed of 1 URIPattern and 1 literal: p, " literal ".
   */
  @Test
  public void testVisitTriplePattern2() {
    final String literal = " literal ";
    final NodeValue dtLit = new NodeValue(XSD_STRING, literal);
    final TriplePattern pattern = new TriplePattern(new Wildcard("*"),
      new URIPattern(new NodeValue(XSD_ANY_URI, "p")), new Literal(dtLit));

    final SimpleNTripleQueryBuilder translator = new SimpleNTripleQueryBuilder(matchVersion, _field, tokenConfigMap);
    pattern.traverseBottomUp(translator);
    final Query dq = pattern.getQuery();

    assertTrue(dq instanceof DocumentQuery);
    assertTrue(((DocumentQuery) dq).getNodeQuery() instanceof TupleQuery);
    final TupleQuery query = (TupleQuery) ((DocumentQuery) dq).getNodeQuery();
    assertEquals(2, ((TupleQuery) query).clauses().size());

    NodeBooleanQuery cellQuery = (NodeBooleanQuery) ((TupleQuery) query).clauses().get(0).getQuery();
    assertTrue(cellQuery.getClauses()[0].getQuery() instanceof NodeTermQuery);
    NodeTermQuery termQuery = (NodeTermQuery) cellQuery.getClauses()[0].getQuery();
    assertEquals(_field, termQuery.getTerm().field());
    assertEquals("p", termQuery.getTerm().text());

    cellQuery = (NodeBooleanQuery) ((TupleQuery) query).clauses().get(1).getQuery();
    assertTrue(cellQuery.getClauses()[0].getQuery() instanceof NodeTermQuery);
    termQuery = (NodeTermQuery) cellQuery.getClauses()[0].getQuery();
    assertEquals(_field, termQuery.getTerm().field());
    assertEquals("literal", termQuery.getTerm().text());
  }

  /**
   * Test method for {@link org.sindice.siren.qparser.ntriple.query.SimpleNTripleQueryBuilder#visit(org.sindice.siren.qparser.ntriple.query.model.TriplePattern)}.
   * Test a TriplePattern composed of 1 URIPattern and 1 literal: p, " some literal ".
   */
  @Test
  public void testVisitTriplePattern3() {
    final String literal = " some literal ";
    final NodeValue dtLit = new NodeValue(XSD_STRING, literal);
    final TriplePattern pattern = new TriplePattern(new Wildcard("*"),
      new URIPattern(new NodeValue(XSD_ANY_URI, "p")), new Literal(dtLit));

    final SimpleNTripleQueryBuilder translator = new SimpleNTripleQueryBuilder(matchVersion, _field, tokenConfigMap);
    pattern.traverseBottomUp(translator);
    final Query dq = pattern.getQuery();

    assertTrue(dq instanceof DocumentQuery);
    assertTrue(((DocumentQuery) dq).getNodeQuery() instanceof TupleQuery);
    final TupleQuery query = (TupleQuery) ((DocumentQuery) dq).getNodeQuery();
    assertEquals(2, ((TupleQuery) query).clauses().size());

    NodeBooleanQuery cellQuery = (NodeBooleanQuery) ((TupleQuery) query).clauses().get(0).getQuery();
    assertTrue(cellQuery.getClauses()[0].getQuery() instanceof NodeTermQuery);
    final NodeTermQuery termQuery = (NodeTermQuery) cellQuery.getClauses()[0].getQuery();
    assertEquals(_field, termQuery.getTerm().field());
    assertEquals("p", termQuery.getTerm().text());

    cellQuery = (NodeBooleanQuery) ((TupleQuery) query).clauses().get(1).getQuery();
    assertTrue(cellQuery.getClauses()[0].getQuery() instanceof NodePhraseQuery);
    final NodePhraseQuery phraseQuery = (NodePhraseQuery) cellQuery.getClauses()[0].getQuery();
    assertTrue(phraseQuery.getTerms().length == 2);
    assertEquals(_field, phraseQuery.getTerms()[0].field());
    assertEquals("some", phraseQuery.getTerms()[0].text());
    assertEquals("literal", phraseQuery.getTerms()[1].text());
  }

  /**
   * Test method for {@link org.sindice.siren.qparser.ntriple.query.SimpleNTripleQueryBuilder#visit(org.sindice.siren.qparser.ntriple.query.model.TriplePattern)}.
   * Test a TriplePattern composed of 1 URIPattern and 1 boolean literal pattern: p, "Some (literal OR text) ".
   */
  @Test
  public void testVisitTriplePattern4() {
    final String literal = " (literal OR text) ";
    final NodeValue dtLit = new NodeValue(XSD_STRING, literal);
    final TriplePattern pattern = new TriplePattern(new Wildcard("*"),
      new URIPattern(new NodeValue(XSD_ANY_URI, "p")), new LiteralPattern(dtLit));

    final SimpleNTripleQueryBuilder translator = new SimpleNTripleQueryBuilder(matchVersion, _field, tokenConfigMap);
    pattern.traverseBottomUp(translator);
    final Query dq = pattern.getQuery();

    assertTrue(dq instanceof DocumentQuery);
    assertTrue(((DocumentQuery) dq).getNodeQuery() instanceof TupleQuery);
    final TupleQuery query = (TupleQuery) ((DocumentQuery) dq).getNodeQuery();
    assertEquals(2, ((TupleQuery) query).clauses().size());

    assertTrue(((TupleQuery) query).clauses().get(0) instanceof NodeBooleanClause);
    NodeBooleanQuery cellQuery = (NodeBooleanQuery) ((TupleQuery) query).clauses().get(0).getQuery();
    assertTrue(cellQuery.getClauses()[0].getQuery() instanceof NodeTermQuery);
    NodeTermQuery termQuery = (NodeTermQuery) cellQuery.getClauses()[0].getQuery();
    assertEquals(_field, termQuery.getTerm().field());
    assertEquals("p", termQuery.getTerm().text());

    assertTrue(((TupleQuery) query).clauses().get(1) instanceof NodeBooleanClause);
    assertTrue(((TupleQuery) query).clauses().get(1).getQuery() instanceof NodeBooleanQuery);
    cellQuery = (NodeBooleanQuery) ((TupleQuery) query).clauses().get(1).getQuery();
    assertTrue(cellQuery.getClauses()[0].getQuery() instanceof NodeBooleanQuery);
    final NodeBooleanQuery bq = (NodeBooleanQuery) cellQuery.getClauses()[0].getQuery();
    assertTrue(bq.getClauses().length == 2);
    assertTrue(bq.getClauses()[0].getQuery() instanceof NodeTermQuery);
    assertTrue(bq.getClauses()[1].getQuery() instanceof NodeTermQuery);
    termQuery = (NodeTermQuery) bq.getClauses()[0].getQuery();
    assertEquals(_field, termQuery.getTerm().field());
    assertEquals("literal", termQuery.getTerm().text());
    termQuery = (NodeTermQuery) bq.getClauses()[1].getQuery();
    assertEquals(_field, termQuery.getTerm().field());
    assertEquals("text", termQuery.getTerm().text());
  }

  /**
   * Test method for {@link org.sindice.solr.plugins.qparser.ntriple.query.StandardQueryTranslator#visit(org.sindice.siren.qparser.ntriple.query.model.EmptyQuery)}.
   * Test an EmptyQuery.
   */
  @Test
  public void testVisitEmptyQuery() {
    final EmptyQuery empty = new EmptyQuery();

    final SimpleNTripleQueryBuilder translator = new SimpleNTripleQueryBuilder(matchVersion, _field, tokenConfigMap);
    empty.traverseBottomUp(translator);
    final Query query = empty.getQuery();
    assertTrue(query instanceof BooleanQuery);
    assertEquals(0, ((BooleanQuery) query).getClauses().length);
  }

  /**
   * Test method for {@link org.sindice.siren.qparser.ntriple.query.SimpleNTripleQueryBuilder#visit(org.sindice.siren.qparser.ntriple.query.model.BinaryClause)}.
   * Test disjunctive binary clause
   */
  @Test
  public void testVisitBinaryClause1() {
    final TriplePattern pattern1 = new TriplePattern(new Wildcard("*"),
      new URIPattern(new NodeValue(XSD_ANY_URI, "p")),
      new URIPattern(new NodeValue(XSD_ANY_URI, "o")));
    final SimpleExpression lhe = new SimpleExpression(pattern1);

    final String literal = "some literal";
    final NodeValue dtLit = new NodeValue(XSD_STRING, literal);
    final TriplePattern pattern2 = new TriplePattern(new Wildcard("s"),
      new URIPattern(new NodeValue(XSD_ANY_URI, "p")), new Literal(dtLit));
    final SimpleExpression rhe = new SimpleExpression(pattern2);

    final BinaryClause clause = new BinaryClause(lhe, Operator.OR, rhe);

    final SimpleNTripleQueryBuilder translator = new SimpleNTripleQueryBuilder(matchVersion, _field, tokenConfigMap);
    clause.traverseBottomUp(translator);
    final Query query = clause.getQuery();

    assertTrue(query instanceof BooleanQuery);
    assertEquals(2, ((BooleanQuery) query).getClauses().length);

    // First part of OR
    assertEquals(Occur.SHOULD, ((BooleanQuery) query).getClauses()[0].getOccur());
    assertTrue(((BooleanQuery) query).getClauses()[0].getQuery() instanceof DocumentQuery);
    assertTrue(((DocumentQuery) ((BooleanQuery) query).getClauses()[0].getQuery()).getNodeQuery() instanceof TupleQuery);
    TupleQuery q = (TupleQuery) ((DocumentQuery) ((BooleanQuery) query).getClauses()[0].getQuery()).getNodeQuery();
    assertTrue(q.clauses().size() == 2);

    NodeBooleanQuery nbq = (NodeBooleanQuery) q.clauses().get(0).getQuery();
    assertTrue(nbq.getClauses()[0].getQuery() instanceof NodeTermQuery);
    NodeTermQuery termQuery = (NodeTermQuery) nbq.getClauses()[0].getQuery();
    assertEquals(_field, termQuery.getTerm().field());
    assertEquals("p", termQuery.getTerm().text());

    nbq = (NodeBooleanQuery) q.clauses().get(1).getQuery();
    assertTrue(nbq.getClauses()[0].getQuery() instanceof NodeTermQuery);
    termQuery = (NodeTermQuery) nbq.getClauses()[0].getQuery();
    assertEquals(_field, termQuery.getTerm().field());
    assertEquals("o", termQuery.getTerm().text());

    // Second part of OR
    assertEquals(Occur.SHOULD, ((BooleanQuery) query).getClauses()[1].getOccur());
    assertTrue(((BooleanQuery) query).getClauses()[1].getQuery() instanceof DocumentQuery);
    assertTrue(((DocumentQuery) ((BooleanQuery) query).getClauses()[1].getQuery()).getNodeQuery() instanceof TupleQuery);
    q = (TupleQuery) ((DocumentQuery) ((BooleanQuery) query).getClauses()[1].getQuery()).getNodeQuery();
    assertTrue(q.clauses().size() == 2);

    nbq = (NodeBooleanQuery) q.clauses().get(0).getQuery();
    assertTrue(nbq.getClauses()[0].getQuery() instanceof NodeTermQuery);
    termQuery = (NodeTermQuery) nbq.getClauses()[0].getQuery();
    assertEquals(_field, termQuery.getTerm().field());
    assertEquals("p", termQuery.getTerm().text());

    nbq = (NodeBooleanQuery) q.clauses().get(1).getQuery();
    assertTrue(nbq.getClauses()[0].getQuery() instanceof NodePhraseQuery);
    final NodePhraseQuery phraseQuery = (NodePhraseQuery) nbq.getClauses()[0].getQuery();
    assertTrue(phraseQuery.getTerms().length == 2);
    assertEquals(_field, phraseQuery.getTerms()[0].field());
    assertEquals("some", phraseQuery.getTerms()[0].text());
    assertEquals(_field, phraseQuery.getTerms()[1].field());
    assertEquals("literal", phraseQuery.getTerms()[1].text());
  }

  /**
   * Test method for {@link org.sindice.siren.qparser.ntriple.query.SimpleNTripleQueryBuilder#visit(org.sindice.siren.qparser.ntriple.query.model.BinaryClause)}.
   * Test conjunctive binary clause
   */
  @Test
  public void testVisitBinaryClause2() {
    final TriplePattern pattern1 = new TriplePattern(new Wildcard("*"),
      new URIPattern(new NodeValue(XSD_ANY_URI, "p")),
      new URIPattern(new NodeValue(XSD_ANY_URI, "o")));
    final SimpleExpression lhe = new SimpleExpression(pattern1);

    final String literal = "some literal";
    final NodeValue dtLit = new NodeValue(XSD_STRING, literal);
    final TriplePattern pattern2 = new TriplePattern(new Wildcard("s"),
      new URIPattern(new NodeValue(XSD_ANY_URI, "p")), new Literal(dtLit));
    final SimpleExpression rhe = new SimpleExpression(pattern2);

    final BinaryClause clause = new BinaryClause(lhe, Operator.AND, rhe);

    final SimpleNTripleQueryBuilder translator = new SimpleNTripleQueryBuilder(matchVersion, _field, tokenConfigMap);
    clause.traverseBottomUp(translator);
    final Query query = clause.getQuery();

    assertTrue(query instanceof BooleanQuery);
    assertEquals(2, ((BooleanQuery) query).getClauses().length);

    // First part of AND
    assertEquals(Occur.MUST, ((BooleanQuery) query).getClauses()[0].getOccur());
    assertTrue(((BooleanQuery) query).getClauses()[0].getQuery() instanceof DocumentQuery);
    assertTrue(((DocumentQuery) ((BooleanQuery) query).getClauses()[0].getQuery()).getNodeQuery() instanceof TupleQuery);
    TupleQuery q = (TupleQuery) ((DocumentQuery) ((BooleanQuery) query).getClauses()[0].getQuery()).getNodeQuery();
    assertTrue(q.clauses().size() == 2);

    NodeBooleanQuery cellQuery = (NodeBooleanQuery) q.clauses().get(0).getQuery();
    assertTrue(cellQuery.getClauses()[0].getQuery() instanceof NodeTermQuery);
    NodeTermQuery termQuery = (NodeTermQuery) cellQuery.getClauses()[0].getQuery();
    assertEquals(_field, termQuery.getTerm().field());
    assertEquals("p", termQuery.getTerm().text());

    cellQuery = (NodeBooleanQuery) q.clauses().get(1).getQuery();
    assertTrue(cellQuery.getClauses()[0].getQuery() instanceof NodeTermQuery);
    termQuery = (NodeTermQuery) cellQuery.getClauses()[0].getQuery();
    assertEquals(_field, termQuery.getTerm().field());
    assertEquals("o", termQuery.getTerm().text());

    // Second part of AND
    assertEquals(Occur.MUST, ((BooleanQuery) query).getClauses()[1].getOccur());
    assertTrue(((BooleanQuery) query).getClauses()[1].getQuery() instanceof DocumentQuery);
    assertTrue(((DocumentQuery) ((BooleanQuery) query).getClauses()[1].getQuery()).getNodeQuery() instanceof TupleQuery);
    q = (TupleQuery) ((DocumentQuery) ((BooleanQuery) query).getClauses()[1].getQuery()).getNodeQuery();
    assertTrue(q.clauses().size() == 2);

    cellQuery = (NodeBooleanQuery) q.clauses().get(0).getQuery();
    assertTrue(cellQuery.getClauses()[0].getQuery() instanceof NodeTermQuery);
    termQuery = (NodeTermQuery) cellQuery.getClauses()[0].getQuery();
    assertEquals(_field, termQuery.getTerm().field());
    assertEquals("p", termQuery.getTerm().text());

    cellQuery = (NodeBooleanQuery) q.clauses().get(1).getQuery();
    assertTrue(cellQuery.getClauses()[0].getQuery() instanceof NodePhraseQuery);
    final NodePhraseQuery phraseQuery = (NodePhraseQuery) cellQuery.getClauses()[0].getQuery();
    assertTrue(phraseQuery.getTerms().length == 2);
    assertEquals(_field, phraseQuery.getTerms()[0].field());
    assertEquals("some", phraseQuery.getTerms()[0].text());
    assertEquals(_field, phraseQuery.getTerms()[1].field());
    assertEquals("literal", phraseQuery.getTerms()[1].text());
  }

  /**
   * Test method for {@link org.sindice.siren.qparser.ntriple.query.SimpleNTripleQueryBuilder#visit(org.sindice.siren.qparser.ntriple.query.model.BinaryClause)}.
   * Test substractive binary clause
   */
  @Test
  public void testVisitBinaryClause3() {
    final TriplePattern pattern1 = new TriplePattern(new Wildcard("*"),
      new URIPattern(new NodeValue(XSD_ANY_URI, "p")),
      new URIPattern(new NodeValue(XSD_ANY_URI, "o")));
    final SimpleExpression lhe = new SimpleExpression(pattern1);

    final String literal = "some literal";
    final NodeValue dtLit = new NodeValue(XSD_STRING, literal);
    final TriplePattern pattern2 = new TriplePattern(new Wildcard("s"),
      new URIPattern(new NodeValue(XSD_ANY_URI, "p")), new Literal(dtLit));
    final SimpleExpression rhe = new SimpleExpression(pattern2);

    final BinaryClause clause = new BinaryClause(lhe, Operator.MINUS, rhe);

    final SimpleNTripleQueryBuilder translator = new SimpleNTripleQueryBuilder(matchVersion, _field, tokenConfigMap);
    clause.traverseBottomUp(translator);
    final Query query = clause.getQuery();

    assertTrue(query instanceof BooleanQuery);
    assertEquals(2, ((BooleanQuery) query).getClauses().length);

    // First part
    assertEquals(Occur.MUST, ((BooleanQuery) query).getClauses()[0].getOccur());
    assertTrue(((BooleanQuery) query).getClauses()[0].getQuery() instanceof DocumentQuery);
    assertTrue(((DocumentQuery) ((BooleanQuery) query).getClauses()[0].getQuery()).getNodeQuery() instanceof TupleQuery);
    TupleQuery q = (TupleQuery) ((DocumentQuery) ((BooleanQuery) query).getClauses()[0].getQuery()).getNodeQuery();
    assertTrue(q.clauses().size() == 2);

    NodeBooleanQuery cellQuery = (NodeBooleanQuery) q.clauses().get(0).getQuery();
    assertTrue(cellQuery.getClauses()[0].getQuery() instanceof NodeTermQuery);
    NodeTermQuery termQuery = (NodeTermQuery) cellQuery.getClauses()[0].getQuery();
    assertEquals(_field, termQuery.getTerm().field());
    assertEquals("p", termQuery.getTerm().text());

    cellQuery = (NodeBooleanQuery) q.clauses().get(1).getQuery();
    assertTrue(cellQuery.getClauses()[0].getQuery() instanceof NodeTermQuery);
    termQuery = (NodeTermQuery) cellQuery.getClauses()[0].getQuery();
    assertEquals(_field, termQuery.getTerm().field());
    assertEquals("o", termQuery.getTerm().text());

    // Second part of MINUS
    assertEquals(Occur.MUST_NOT, ((BooleanQuery) query).getClauses()[1].getOccur());
    assertTrue(((BooleanQuery) query).getClauses()[1].getQuery() instanceof DocumentQuery);
    assertTrue(((DocumentQuery) ((BooleanQuery) query).getClauses()[1].getQuery()).getNodeQuery() instanceof TupleQuery);
    q = (TupleQuery) ((DocumentQuery) ((BooleanQuery) query).getClauses()[1].getQuery()).getNodeQuery();
    assertTrue(q.clauses().size() == 2);

    cellQuery = (NodeBooleanQuery) q.clauses().get(0).getQuery();
    assertTrue(cellQuery.getClauses()[0].getQuery() instanceof NodeTermQuery);
    termQuery = (NodeTermQuery) cellQuery.getClauses()[0].getQuery();
    assertEquals(_field, termQuery.getTerm().field());
    assertEquals("p", termQuery.getTerm().text());

    cellQuery = (NodeBooleanQuery) q.clauses().get(1).getQuery();
    assertTrue(cellQuery.getClauses()[0].getQuery() instanceof NodePhraseQuery);
    final NodePhraseQuery phraseQuery = (NodePhraseQuery) cellQuery.getClauses()[0].getQuery();
    assertTrue(phraseQuery.getTerms().length == 2);
    assertEquals(_field, phraseQuery.getTerms()[0].field());
    assertEquals("some", phraseQuery.getTerms()[0].text());
    assertEquals(_field, phraseQuery.getTerms()[1].field());
    assertEquals("literal", phraseQuery.getTerms()[1].text());
  }

  /**
   * Test method for {@link org.sindice.siren.qparser.ntriple.query.SimpleNTripleQueryBuilder#visit(org.sindice.siren.qparser.ntriple.query.model.BinaryClause)}.
   * Test nested binary clause: <p> <o> OR (<s> <p> "some literal" AND <s> <p> <o2>)
   */
  @Test
  public void testVisitBinaryClause4() {
    final TriplePattern pattern1 = new TriplePattern(new Wildcard("*"),
      new URIPattern(new NodeValue(XSD_ANY_URI, "p")),
      new URIPattern(new NodeValue(XSD_ANY_URI, "o")));
    final SimpleExpression lhe = new SimpleExpression(pattern1);

    final String literal = "some literal";
    final NodeValue dtLit = new NodeValue(XSD_STRING, literal);
    final TriplePattern pattern2 = new TriplePattern(new Wildcard("s"),
      new URIPattern(new NodeValue(XSD_ANY_URI, "p")), new Literal(dtLit));
    final SimpleExpression rhe1 = new SimpleExpression(pattern2);

    final TriplePattern pattern3 = new TriplePattern(new Wildcard("s"),
      new URIPattern(new NodeValue(XSD_ANY_URI, "p")),
      new URIPattern(new NodeValue(XSD_ANY_URI, "o2")));
    final SimpleExpression rhe2 = new SimpleExpression(pattern3);

    final BinaryClause bclause = new BinaryClause(lhe, Operator.AND, rhe1);

    final NestedClause qclause = new NestedClause(bclause, Operator.OR, rhe2);

    final SimpleNTripleQueryBuilder translator = new SimpleNTripleQueryBuilder(matchVersion, _field, tokenConfigMap);
    qclause.traverseBottomUp(translator);
    final Query query = qclause.getQuery();

    assertTrue(query instanceof BooleanQuery);
    assertEquals(2, ((BooleanQuery) query).getClauses().length);

    // First part
    assertEquals(Occur.SHOULD, ((BooleanQuery) query).getClauses()[0].getOccur());
    assertTrue(((BooleanQuery) query).getClauses()[0].getQuery() instanceof BooleanQuery);

    final BooleanQuery bQueryAnd = (BooleanQuery) ((BooleanQuery) query).getClauses()[0].getQuery();
    assertEquals(2, bQueryAnd.getClauses().length);

    // First Part : Nested AND
    assertEquals(Occur.MUST, bQueryAnd.getClauses()[0].getOccur());
    assertTrue(((BooleanQuery) bQueryAnd).getClauses()[0].getQuery() instanceof DocumentQuery);
    assertTrue(((DocumentQuery) ((BooleanQuery) bQueryAnd).getClauses()[0].getQuery()).getNodeQuery() instanceof TupleQuery);
    TupleQuery q = (TupleQuery) ((DocumentQuery) ((BooleanQuery) bQueryAnd).getClauses()[0].getQuery()).getNodeQuery();
    assertTrue(q.clauses().size() == 2);

    NodeBooleanQuery cellQuery = (NodeBooleanQuery) q.clauses().get(0).getQuery();
    assertTrue(cellQuery.getClauses()[0].getQuery() instanceof NodeTermQuery);
    NodeTermQuery termQuery = (NodeTermQuery) cellQuery.getClauses()[0].getQuery();
    assertEquals(_field, termQuery.getTerm().field());
    assertEquals("p", termQuery.getTerm().text());

    cellQuery = (NodeBooleanQuery) q.clauses().get(1).getQuery();
    assertTrue(cellQuery.getClauses()[0].getQuery() instanceof NodeTermQuery);
    termQuery = (NodeTermQuery) cellQuery.getClauses()[0].getQuery();
    assertEquals(_field, termQuery.getTerm().field());
    assertEquals("o", termQuery.getTerm().text());

    // Second Part : Nested AND
    assertEquals(Occur.MUST, bQueryAnd.getClauses()[1].getOccur());
    assertTrue(((BooleanQuery) bQueryAnd).getClauses()[1].getQuery() instanceof DocumentQuery);
    assertTrue(((DocumentQuery) ((BooleanQuery) bQueryAnd).getClauses()[1].getQuery()).getNodeQuery() instanceof TupleQuery);
    q = (TupleQuery) ((DocumentQuery) ((BooleanQuery) bQueryAnd).getClauses()[1].getQuery()).getNodeQuery();
    assertTrue(q.clauses().size() == 2);

    cellQuery = (NodeBooleanQuery) q.clauses().get(0).getQuery();
    assertTrue(cellQuery.getClauses()[0].getQuery() instanceof NodeTermQuery);
    termQuery = (NodeTermQuery) cellQuery.getClauses()[0].getQuery();
    assertEquals(_field, termQuery.getTerm().field());
    assertEquals("p", termQuery.getTerm().text());

    cellQuery = (NodeBooleanQuery) q.clauses().get(1).getQuery();
    assertTrue(cellQuery.getClauses()[0].getQuery() instanceof NodePhraseQuery);
    final NodePhraseQuery phraseQuery = (NodePhraseQuery) cellQuery.getClauses()[0].getQuery();
    assertTrue(phraseQuery.getTerms().length == 2);
    assertEquals(_field, phraseQuery.getTerms()[0].field());
    assertEquals("some", phraseQuery.getTerms()[0].text());
    assertEquals(_field, phraseQuery.getTerms()[1].field());
    assertEquals("literal", phraseQuery.getTerms()[1].text());

    // Second part
    assertEquals(Occur.SHOULD, ((BooleanQuery) query).getClauses()[1].getOccur());
    assertTrue(((BooleanQuery) query).getClauses()[1].getQuery() instanceof DocumentQuery);
    assertTrue(((DocumentQuery) ((BooleanQuery) query).getClauses()[1].getQuery()).getNodeQuery() instanceof TupleQuery);
    q = (TupleQuery) ((DocumentQuery) ((BooleanQuery) query).getClauses()[1].getQuery()).getNodeQuery();
    assertTrue(q.clauses().size() == 2);

    cellQuery = (NodeBooleanQuery) q.clauses().get(0).getQuery();
    assertTrue(cellQuery.getClauses()[0].getQuery() instanceof NodeTermQuery);
    termQuery = (NodeTermQuery) cellQuery.getClauses()[0].getQuery();
    assertEquals(_field, termQuery.getTerm().field());
    assertEquals("p", termQuery.getTerm().text());

    cellQuery = (NodeBooleanQuery) q.clauses().get(1).getQuery();
    assertTrue(cellQuery.getClauses()[0].getQuery() instanceof NodeTermQuery);
    termQuery = (NodeTermQuery) cellQuery.getClauses()[0].getQuery();
    assertEquals(_field, termQuery.getTerm().field());
    assertEquals("o2", termQuery.getTerm().text());
  }

  /**
   * Test method for {@link org.sindice.siren.qparser.ntriple.query.SimpleNTripleQueryBuilder#visit(org.sindice.siren.qparser.ntriple.query.model.Literal)}.
   */
  @Test
  public void testVisitLiteral() {
    final String text = "Some Literal ...";
    final NodeValue dtLit = new NodeValue(XSD_STRING, text);
    final Literal literal = new Literal(dtLit);

    final SimpleNTripleQueryBuilder translator = new SimpleNTripleQueryBuilder(matchVersion, _field, tokenConfigMap);
    literal.traverseBottomUp(translator);
    final Query query = literal.getQuery();

    assertTrue(query instanceof NodePhraseQuery);
    assertEquals(3, ((NodePhraseQuery) query).getTerms().length);
    final Term[] terms = ((NodePhraseQuery) query).getTerms();

    assertEquals(_field, terms[0].field());
    assertEquals("Some", terms[0].text());

    assertEquals(_field, terms[1].field());
    assertEquals("Literal", terms[1].text());

    assertEquals(_field, terms[2].field());
    assertEquals("...", terms[2].text());
  }

  /**
   * Test method for {@link org.sindice.siren.qparser.ntriple.query.SimpleNTripleQueryBuilder#visit(org.sindice.siren.qparser.ntriple.query.model.Literal)}.
   */
  @Test
  public void testVisitLiteralPattern() {
    final String text = "\"Some Literal ...\"";
    final NodeValue dtLit = new NodeValue(XSD_STRING, text);
    final LiteralPattern literal = new LiteralPattern(dtLit);

    final SimpleNTripleQueryBuilder translator = new SimpleNTripleQueryBuilder(matchVersion, _field, tokenConfigMap);
    literal.traverseBottomUp(translator);
    final Query query = literal.getQuery();

    assertTrue(query instanceof NodePhraseQuery);
    assertEquals(3, ((NodePhraseQuery) query).getTerms().length);
    final Term[] terms = ((NodePhraseQuery) query).getTerms();

    assertEquals(_field, terms[0].field());
    assertEquals("Some", terms[0].text());

    assertEquals(_field, terms[1].field());
    assertEquals("Literal", terms[1].text());

    assertEquals(_field, terms[2].field());
    assertEquals("...", terms[2].text());
  }

  /**
   * Test method for {@link org.sindice.siren.qparser.ntriple.query.SimpleNTripleQueryBuilder#visit(org.sindice.siren.qparser.ntriple.query.model.Literal)}.
   */
  @Test
  public void testVisitLiteralPattern2() {
    final String text = "Some AND Literal";
    final NodeValue dtLit = new NodeValue(XSD_STRING, text);
    final LiteralPattern literal = new LiteralPattern(dtLit);

    final SimpleNTripleQueryBuilder translator = new SimpleNTripleQueryBuilder(matchVersion, _field, tokenConfigMap);
    literal.traverseBottomUp(translator);
    final Query query = literal.getQuery();

    assertTrue(query instanceof NodeBooleanQuery);
    final NodeBooleanQuery bq = (NodeBooleanQuery) query;
    assertEquals(2, bq.getClauses().length);

    NodeBooleanClause cellQuery = bq.getClauses()[0];
    assertEquals(cellQuery.getOccur(), NodeBooleanClause.Occur.MUST);
    assertTrue(cellQuery.getQuery() instanceof NodeTermQuery);
    NodeTermQuery q = (NodeTermQuery) cellQuery.getQuery();
    assertEquals(_field, q.getTerm().field());
    assertEquals("Some", q.getTerm().text());

    cellQuery = bq.getClauses()[1];
    assertEquals(cellQuery.getOccur(), NodeBooleanClause.Occur.MUST);
    assertTrue(cellQuery.getQuery() instanceof NodeTermQuery);
    q = (NodeTermQuery) cellQuery.getQuery();
    assertEquals(_field, q.getTerm().field());
    assertEquals("Literal", q.getTerm().text());
  }

  /**
   * Test method for {@link org.sindice.siren.qparser.ntriple.query.SimpleNTripleQueryBuilder#visit(org.sindice.siren.qparser.ntriple.query.model.Literal)}.
   */
  @Test
  public void testVisitLiteralPattern3() {
    final String text = "Some OR Literal";
    final NodeValue dtLit = new NodeValue(XSD_STRING, text);
    final LiteralPattern literal = new LiteralPattern(dtLit);

    final SimpleNTripleQueryBuilder translator = new SimpleNTripleQueryBuilder(matchVersion, _field, tokenConfigMap);
    literal.traverseBottomUp(translator);
    final Query query = literal.getQuery();

    assertTrue(query instanceof NodeBooleanQuery);
    final NodeBooleanQuery bq = (NodeBooleanQuery) query;
    assertEquals(2, bq.getClauses().length);

    NodeBooleanClause cellQuery = bq.getClauses()[0];
    assertEquals(cellQuery.getOccur(), NodeBooleanClause.Occur.SHOULD);
    assertTrue(cellQuery.getQuery() instanceof NodeTermQuery);
    NodeTermQuery q = (NodeTermQuery) cellQuery.getQuery();
    assertEquals(_field, q.getTerm().field());
    assertEquals("Some", q.getTerm().text());

    cellQuery = bq.getClauses()[1];
    assertEquals(cellQuery.getOccur(), NodeBooleanClause.Occur.SHOULD);
    assertTrue(cellQuery.getQuery() instanceof NodeTermQuery);
    q = (NodeTermQuery) cellQuery.getQuery();
    assertEquals(_field, q.getTerm().field());
    assertEquals("Literal", q.getTerm().text());
  }

  /**
   * Create a NodeTermQuery from the URI.
   * The uriAnalyzer would interpret the URI scheme (e.g. aaa) as a field
   * name if the key word ":" was not escaped.
   */
  @Test
  public void testVisitURIPattern1() {
    /*
     * TODO: aaa://s is not a valid URI, therefore it is not completely escaped
     * by (@link EscapeLuceneCharacters#escape}. This raises the question if
     * characters other than ':' have also to be escaped, e.g., '=' or '/'.
     */
    final String text = "http://s.com";
    final URIPattern uri = new URIPattern(new NodeValue(XSD_ANY_URI, text));

    final SimpleNTripleQueryBuilder translator = new SimpleNTripleQueryBuilder(matchVersion, _field, tokenConfigMap);
    uri.traverseBottomUp(translator);
    final Query query = uri.getQuery();

    assertTrue(query instanceof NodeTermQuery);
    final NodeTermQuery tq = (NodeTermQuery) query;
    assertEquals(_field, tq.getTerm().field());
    assertEquals("http://s.com", tq.getTerm().text());
  }

  @Test
  public void testVisitURIPattern2() {
    /*
     * TODO: same as above
     */
    final String text = "http://s.com || http://test.ie";
    final URIPattern uri = new URIPattern(new NodeValue(XSD_ANY_URI, text));

    final SimpleNTripleQueryBuilder translator = new SimpleNTripleQueryBuilder(matchVersion, _field, tokenConfigMap);
    uri.traverseBottomUp(translator);
    final Query query = uri.getQuery();

    assertTrue(query instanceof NodeBooleanQuery);
    final NodeBooleanQuery bq = (NodeBooleanQuery) query;
    assertEquals(2, bq.getClauses().length);

    NodeBooleanClause cellQuery = bq.getClauses()[0];
    assertEquals(cellQuery.getOccur(), NodeBooleanClause.Occur.SHOULD);
    assertTrue(cellQuery.getQuery() instanceof NodeTermQuery);
    NodeTermQuery q = (NodeTermQuery) cellQuery.getQuery();
    assertEquals(_field, q.getTerm().field());
    assertEquals("http://s.com", q.getTerm().text());

    cellQuery = bq.getClauses()[1];
    assertEquals(cellQuery.getOccur(), NodeBooleanClause.Occur.SHOULD);
    assertTrue(cellQuery.getQuery() instanceof NodeTermQuery);
    q = (NodeTermQuery) cellQuery.getQuery();
    assertEquals(_field, q.getTerm().field());
    assertEquals("http://test.ie", q.getTerm().text());
  }

}

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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.sindice.siren.qparser.analysis.TupleTestHelper._defaultField;
import static org.sindice.siren.qparser.analysis.TupleTestHelper._implicitField;
import static org.sindice.siren.qparser.tree.TupleQueryParserTestHelper.match;
import static org.sindice.siren.qparser.tree.TupleQueryParserTestHelper.setTestQuery;

import java.util.HashMap;

import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.sindice.siren.qparser.tree.TupleQueryParserTestHelper.QueryParserEnum;

public class KeywordQueryParserTest {

  private final HashMap<String, String> ntriples = new HashMap<String, String>();

  @Before
  public void setUp() throws Exception {
    ntriples.clear();
    setTestQuery(QueryParserEnum.KEYWORD);
  }

  @Test
  public void testSingleWord()
  throws Exception {
    ntriples.put(_defaultField, "_:b1 <http://p.fr> \"hello\" .");
    ntriples.put(_implicitField, "_:b1 <http://p.fr> \"nohello\" .");
    assertTrue(match(ntriples, null, "hello", false));
    ntriples.clear();
    ntriples.put(_defaultField, "_:b1 <http://p.fr> \"nohello\" .");
    ntriples.put(_implicitField, "_:b1 <http://p.fr> \"hello\" .");
    assertTrue(match(ntriples, null, "hello", false));
    ntriples.clear();
    ntriples.put(_defaultField, "_:b1 <http://p.fr> \"nohello1\" .");
    ntriples.put(_implicitField, "_:b1 <http://p.fr> \"nohello2\" .");
    assertFalse(match(ntriples, null, "hello", false));
  }

  @Test
  public void testMultipleWords()
  throws Exception {
    ntriples.put(_defaultField, "_:b1 <http://p.fr> \"hello world\" .");
    ntriples.put(_implicitField, "_:b1 <http://p.fr> \"nohello world\" .");
    assertTrue(match(ntriples, null, "hello world", false));
    ntriples.clear();
    ntriples.put(_defaultField, "_:b1 <http://p.fr> \"nohello world\" .");
    ntriples.put(_implicitField, "_:b1 <http://p.fr> \"hello world\" .");
    assertTrue(match(ntriples, null, "hello", false));
    ntriples.clear();
    ntriples.put(_defaultField, "_:b1 <http://p.fr> \"nohello1 world\" .");
    ntriples.put(_implicitField, "_:b1 <http://p.fr> \"nohello2 world\" .");
    assertFalse(match(ntriples, null, "hello", false));
  }

  @Test
  public void testCompoundQuery() throws Exception {
    final String query = "http://www.google.com +hello -world";

    ntriples.put(_defaultField, "<http://www.google.com> <p> \"hello2\" .");
    ntriples.put(_implicitField, "<http://www.google.com> <p> \"hello world2\" .");
    assertTrue(match(ntriples, null, query, false));
    ntriples.clear();
    ntriples.put(_defaultField, "<http://www.google.com> <p> \"hello world2\" .");
    ntriples.put(_implicitField, "<http://www.google.com> <p> \"hello2\" .");
    assertTrue(match(ntriples, null, query, false));
    ntriples.clear();
    ntriples.put(_defaultField, "<http://www.google.com> <p> \"hello\" .");
    ntriples.put(_implicitField, "<http://www.google.com> <p> \"hello world\" .");
    assertFalse(match(ntriples, null, query, false));
    ntriples.clear();
    ntriples.put(_defaultField, "<http://www.sindice.com> <p> \"hello world\" .");
    ntriples.put(_implicitField, "<http://www.sindice.com> <p> \"hello\" .");
    assertFalse(match(ntriples, null, query, false));
    ntriples.clear();
    ntriples.put(_defaultField, "<http://www.sindice.com> <p> \"hey\" .");
    ntriples.put(_implicitField, "<http://www.sindice.com> <p> \"hello\" .");
    assertTrue(match(ntriples, null, query, false));
    ntriples.clear();
    ntriples.put(_defaultField, "<http://www.google.com> <p> \"hey\" .");
    ntriples.put(_implicitField, "<http://www.google.com> <p> \"hey ho\" .");
    assertFalse(match(ntriples, null, query, false));
    ntriples.clear();
    ntriples.put(_defaultField, "<http://www.sindice.com> <hello> \"hey\" .");
    ntriples.put(_implicitField, "<s> <p> \"http://www.google.com\" .");
    assertTrue(match(ntriples, null, query, false));
  }

  @Test(expected=ParseException.class)
  public void testFuzzyQuery()
  throws Exception {
    ntriples.put(_defaultField, "<http://www.google.com> <p> \"michelle\" .");
    match(ntriples, null, "michel~", false);
  }

  @Test(expected=ParseException.class)
  public void testWildcardQuery() throws Exception {
    ntriples.put(_defaultField, "<http://www.google.com> <p> \"michelle\" .");
    match(ntriples, null, "miche*", false);
  }

  @Test
  public void testWildcardInURI() throws Exception {
    ntriples.put(_defaultField, "<http://example.com/~foo=bar> <p> \"o\" .");
    ntriples.put(_implicitField, "<s> <p> \"o\" .");
    assertTrue(match(ntriples, null, "http://example.com/~foo=bar", false));

    ntriples.clear();
    ntriples.put(_defaultField, "<s> <p> \"o\" .");
    ntriples.put(_implicitField, "<http://example.com/~foo=bar> <p> \"o\" .");
    assertTrue(match(ntriples, null, "http://example.com/~foo=bar", false));

    ntriples.clear();
    ntriples.put(_defaultField, "<http://example.com/?foo=bar> <p> \"o\" .");
    ntriples.put(_implicitField, "<s> <p> \"o\" .");
    assertTrue(match(ntriples, null, "http://example.com/?foo=bar", false));

    ntriples.clear();
    ntriples.put(_defaultField, "<s> <p> \"o\" .");
    ntriples.put(_implicitField, "<http://example.com/?foo=bar> <p> \"o\" .");
    assertTrue(match(ntriples, null, "http://example.com/?foo=bar", false));
  }

  @Test
  public void testDashedURI() throws Exception {
    ntriples.put(_implicitField, "<http://semantic-conference.com/session/569> <p> \"o\" .");
    assertTrue(match(ntriples, null, "http://semantic-conference.com/session/569", false));
  }

}

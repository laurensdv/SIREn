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
package org.sindice.siren.qparser.ntriple.query.builder;


import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.QueryParserHelper;
import org.apache.lucene.queryparser.flexible.core.builders.QueryTreeBuilder;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.Operator;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.util.Version;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sindice.siren.analysis.filter.ASCIIFoldingExpansionFilter;
import org.sindice.siren.analysis.filter.SirenPayloadFilter;
import org.sindice.siren.qparser.tree.Siren10Codec;
import org.sindice.siren.qparser.tree.TreeQueryParser;
import org.sindice.siren.qparser.tree.TreeQueryParser.TreeConfigurationKeys;
import org.sindice.siren.qparser.tree.query.builders.NodeQueryTreeBuilder;
import org.sindice.siren.search.doc.DocumentQuery;
import org.sindice.siren.search.node.MultiNodeTermQuery;
import org.sindice.siren.search.node.NodeBooleanClause;
import org.sindice.siren.search.node.NodeBooleanClause.Occur;
import org.sindice.siren.search.node.NodeBooleanQuery;
import org.sindice.siren.search.node.NodePhraseQuery;
import org.sindice.siren.search.node.NodeRegexpQuery;
import org.sindice.siren.search.node.NodeTermQuery;

/**
 * This test case is a copy of the core Solr query parser helper test, it was adapted
 * to test the use of the QueryTreeBuilder for SIREn.
 */
public class StandardQueryTreeBuilderTest extends LuceneTestCase {

  private final String DEFAULT_CONTENT_FIELD = "content";
  private final Version matchVersion = TEST_VERSION_CURRENT;
  
  private RAMDirectory        dir = null;
  private IndexWriter         writer = null;
  private Analyzer            analyser = null;
  private StandardQueryParser qph = null;
  private QueryTreeBuilder    qBuilder = null;
  private final Analyzer      simpleAnalyser = new SimpleAnalyzer(matchVersion);

  @Before
  @Override
  public void setUp()
  throws Exception {
    super.setUp();
    qBuilder = new NodeQueryTreeBuilder();
    final IndexWriterConfig config = new IndexWriterConfig(matchVersion,
      new StandardAnalyzer(matchVersion));
    final Siren10Codec codec = new Siren10Codec();
    codec.addSirenField(DEFAULT_CONTENT_FIELD);
    config.setCodec(codec);
    dir = new RAMDirectory();
    writer = new IndexWriter(dir, config);
    analyser = new Analyzer() {
      @Override
      protected TokenStreamComponents createComponents(String fieldName,
                                                       Reader reader) {
        final Tokenizer t = new StandardTokenizer(matchVersion, reader);
        // Emulates that the content of the field comes from the first node of a tuple
        t.addAttributeImpl(new MockTupleNodeAttributeImpl());
        TokenStream ts = new SirenPayloadFilter(t);
        return new TokenStreamComponents(t, ts);
      }
    };

    qph = new TreeQueryParser(simpleAnalyser);
    qph.setDefaultOperator(Operator.OR);
  }

  @After
  @Override
  public void tearDown()
  throws Exception {
    analyser.close();
    writer.close();
    super.tearDown();
  }

  private ScoreDoc[] search(final Query q, final int n) throws IOException {
    final IndexReader reader = DirectoryReader.open(writer.getDirectory());
    final IndexSearcher searcher = new IndexSearcher(reader);
    try {
      return searcher.search(q, null, n).scoreDocs;
    }
    finally {
      reader.close();
    }
  }

  public void assertQueryEquals(final String query, final Analyzer analyser, final String expected) throws QueryNodeException {
    if (analyser == null)
      qph.setAnalyzer(simpleAnalyser);
    else
      qph.setAnalyzer(analyser);
    assertEquals(expected, qph.parse(query, "field").toString("field"));
  }

  @Test
  public void testQuerySyntax() throws QueryNodeException {

    this.assertQueryEquals("term term term", null, "term term term");
    this.assertQueryEquals("t�rm term term", new WhitespaceAnalyzer(LuceneTestCase.TEST_VERSION_CURRENT), "t�rm term term");
    this.assertQueryEquals("�mlaut", new WhitespaceAnalyzer(LuceneTestCase.TEST_VERSION_CURRENT), "�mlaut");

    this.assertQueryEquals("\"\"", new KeywordAnalyzer(), "");
    this.assertQueryEquals("foo:\"\"", new KeywordAnalyzer(), "foo:");

    this.assertQueryEquals("a AND b", null, "+a +b");
    this.assertQueryEquals("(a AND b)", null, "+a +b");

    this.assertQueryEquals("a AND NOT b", null, "+a -b");

    this.assertQueryEquals("a AND -b", null, "+a -b");

    this.assertQueryEquals("a AND !b", null, "+a -b");

    this.assertQueryEquals("a && b", null, "+a +b");

    this.assertQueryEquals("a && ! b", null, "+a -b");

    this.assertQueryEquals("a OR b", null, "a b");
    this.assertQueryEquals("a || b", null, "a b");

    this.assertQueryEquals("a OR !b", null, "a -b");

    this.assertQueryEquals("a OR ! b", null, "a -b");

    this.assertQueryEquals("a OR -b", null, "a -b");

    this.assertQueryEquals("+term -term term", null, "+term -term term");
    this.assertQueryEquals("foo:term AND field:anotherTerm", null, "+foo:term +anotherterm");
    this.assertQueryEquals("term AND \"phrase phrase\"", null, "+term +\"phrase phrase\"");
    this.assertQueryEquals("\"hello there\"", null, "\"hello there\"");
  }

  @Test
  public void testNumber() throws Exception {
    // The numbers go away because SimpleAnalyzer ignores them
    this.assertQueryEquals("3", null, ""); // test empty query
    this.assertQueryEquals("term 1.0 1 2", null, "term");
    this.assertQueryEquals("term term1 term2", null, "term term term");
  }

  @Test
  public void testBoost() throws Exception {
    this.assertQueryEquals("term^1.0", null, "term");
  }

  @Test
  public void testEscaped() throws QueryNodeException {
    final Analyzer a = new WhitespaceAnalyzer(matchVersion);

    this.assertQueryEquals("\\*", a, "*");

    this.assertQueryEquals("\\a", a, "a");

    this.assertQueryEquals("a\\-b:c", a, "a-b:c");
    this.assertQueryEquals("a\\+b:c", a, "a+b:c");
    this.assertQueryEquals("a\\:b:c", a, "a:b:c");
    this.assertQueryEquals("a\\\\b:c", a, "a\\b:c");

    this.assertQueryEquals("a:b\\-c", a, "a:b-c");
    this.assertQueryEquals("a:b\\+c", a, "a:b+c");
    this.assertQueryEquals("a:b\\:c", a, "a:b:c");
    this.assertQueryEquals("a:b\\\\c", a, "a:b\\c");

    this.assertQueryEquals("a\\\\\\+b", a, "a\\+b");

    this.assertQueryEquals("a \\\"b c\\\" d", a, "a \"b c\" d");
    this.assertQueryEquals("\"a \\\"b c\\\" d\"", a, "\"a \"b c\" d\"");
    this.assertQueryEquals("\"a \\+b c d\"", a, "\"a +b c d\"");
  }

  @Test
  public void testQueryType() throws QueryNodeException {
    Query query = qph.parse("a:aaa AND a:bbb", "a");
    assertTrue(query instanceof NodeBooleanQuery);
    query = qph.parse("a:hello", "a");
    assertTrue(query instanceof NodeTermQuery);
    query = qph.parse("a:\"hello Future\"", "a");
    assertTrue(query instanceof NodePhraseQuery);
  }

  @Test
  public void testRegexps() throws Exception {
    // unset the Simple analyzer
    qph.setAnalyzer(null);

    final String df = "field" ;
    NodeRegexpQuery q = new NodeRegexpQuery(new Term("field", "[a-z][123]"));
    assertEquals(q, qph.parse("/[a-z][123]/", df));
    qph.setLowercaseExpandedTerms(true);
    assertEquals(q, qph.parse("/[A-Z][123]/", df));
    q.setBoost(0.5f);
    assertEquals(q, qph.parse("/[A-Z][123]/^0.5", df));
    q.setRewriteMethod(MultiNodeTermQuery.SCORING_BOOLEAN_QUERY_REWRITE);
    qph.getQueryConfigHandler().set(TreeConfigurationKeys.MULTI_NODE_TERM_REWRITE_METHOD, MultiNodeTermQuery.SCORING_BOOLEAN_QUERY_REWRITE);
    assertTrue(qph.parse("/[A-Z][123]/^0.5", df) instanceof NodeRegexpQuery);
    assertEquals(q, qph.parse("/[A-Z][123]/^0.5", df));
    assertEquals(MultiNodeTermQuery.SCORING_BOOLEAN_QUERY_REWRITE, ((NodeRegexpQuery)qph.parse("/[A-Z][123]/^0.5", df)).getRewriteMethod());
    qph.getQueryConfigHandler().set(TreeConfigurationKeys.MULTI_NODE_TERM_REWRITE_METHOD, MultiNodeTermQuery.CONSTANT_SCORE_AUTO_REWRITE_DEFAULT);
    
    Query escaped = new NodeRegexpQuery(new Term("field", "[a-z]\\/[123]"));
    assertEquals(escaped, qph.parse("/[a-z]\\/[123]/", df));
    Query escaped2 = new NodeRegexpQuery(new Term("field", "[a-z]\\*[123]"));
    assertEquals(escaped2, qph.parse("/[a-z]\\*[123]/", df));
    
    NodeBooleanQuery complex = new NodeBooleanQuery();
    complex.add(new NodeRegexpQuery(new Term("field", "[a-z]\\/[123]")), NodeBooleanClause.Occur.MUST);
    complex.add(new NodeTermQuery(new Term("path", "/etc/init.d/")), Occur.MUST);
    complex.add(new NodeTermQuery(new Term("field", "/etc/init[.]d/lucene/")), Occur.SHOULD);
    assertEquals(complex, qph.parse("/[a-z]\\/[123]/ AND path:\"/etc/init.d/\" OR \"/etc\\/init\\[.\\]d/lucene/\" ", df));
    
    Query re = new NodeRegexpQuery(new Term("field", "http.*"));
    assertEquals(re, qph.parse("field:/http.*/", df));
    assertEquals(re, qph.parse("/http.*/", df));
    
    re = new NodeRegexpQuery(new Term("field", "http~0.5"));
    assertEquals(re, qph.parse("field:/http~0.5/", df));
    assertEquals(re, qph.parse("/http~0.5/", df));
    
    re = new NodeRegexpQuery(new Term("field", "boo"));
    assertEquals(re, qph.parse("field:/boo/", df));
    assertEquals(re, qph.parse("/boo/", df));
    
    assertEquals(new NodeTermQuery(new Term("field", "/boo/")), qph.parse("\"/boo/\"", df));
    assertEquals(new NodeTermQuery(new Term("field", "/boo/")), qph.parse("\\/boo\\/", df));
    
    NodeBooleanQuery two = new NodeBooleanQuery();
    two.add(new NodeRegexpQuery(new Term("field", "foo")), Occur.SHOULD);
    two.add(new NodeRegexpQuery(new Term("field", "bar")), Occur.SHOULD);
    assertEquals(two, qph.parse("field:/foo/ field:/bar/", df));
    assertEquals(two, qph.parse("/foo/ /bar/", df));
  }

  @Test
  public void testRegexQueryParsing() throws Exception {
    final String[] fields = {"b", "t"};

    qph.setMultiFields(fields);
    qph.setDefaultOperator(StandardQueryConfigHandler.Operator.AND);

    NodeBooleanQuery exp = new NodeBooleanQuery();
    exp.add(new NodeBooleanClause(new NodeRegexpQuery(new Term("b", "ab.+")), NodeBooleanClause.Occur.SHOULD));//TODO spezification? was "MUST"
    exp.add(new NodeBooleanClause(new NodeRegexpQuery(new Term("t", "ab.+")), NodeBooleanClause.Occur.SHOULD));//TODO spezification? was "MUST"

    assertEquals(exp, qph.parse("/ab.+/", null));

    NodeRegexpQuery regexpQueryexp = new NodeRegexpQuery(new Term("test", "[abc]?[0-9]"));

    assertEquals(regexpQueryexp, qph.parse("test:/[abc]?[0-9]/", null));
  }

  @Test
  public void testNodeBooleanQuery()
  throws QueryNodeException, CorruptIndexException, IOException {
    final Document doc = new Document();
    final Document doc2 = new Document();
    final FieldType ft = new FieldType();
    ft.setStored(false);
    ft.setIndexed(true);
    ft.setTokenized(true);
    ft.setOmitNorms(true);
    doc.add(new Field(DEFAULT_CONTENT_FIELD, "aaa bbb ccc", ft));
    writer.addDocument(doc, analyser);

    doc2.add(new Field(DEFAULT_CONTENT_FIELD, "bbb ccc ddd", ft));
    writer.addDocument(doc2, analyser);
    writer.commit();

    final QueryParserHelper qph = new QueryParserHelper(new StandardQueryConfigHandler(),
                                                        new StandardSyntaxParser(),
                                                        null,
                                                        qBuilder);

    NodeBooleanQuery query = (NodeBooleanQuery) qph.parse(DEFAULT_CONTENT_FIELD + ":aaa OR " + DEFAULT_CONTENT_FIELD + ":bbb", DEFAULT_CONTENT_FIELD);

    ScoreDoc[] results = this.search(new DocumentQuery(query), 10);
    assertEquals(2, results.length);
    assertEquals(0, results[0].doc);

    query = (NodeBooleanQuery) qph.parse(DEFAULT_CONTENT_FIELD + ":aaa AND NOT " + DEFAULT_CONTENT_FIELD + ":ddd", DEFAULT_CONTENT_FIELD);

    results = this.search(new DocumentQuery(query), 10);
    assertEquals(1, results.length);
    assertEquals(0, results[0].doc);
  }

  @Test
  public void testNodeTermQuery()
  throws QueryNodeException, CorruptIndexException, IOException {
    final Document doc = new Document();
    final FieldType ft = new FieldType();
    ft.setStored(false);
    ft.setIndexed(true);
    ft.setTokenized(true);
    ft.setOmitNorms(true);

    doc.add(new Field(DEFAULT_CONTENT_FIELD, "aaa bbb ccc", ft));
    writer.addDocument(doc, analyser);
    writer.commit();

    NodeTermQuery query = (NodeTermQuery) qph.parse(DEFAULT_CONTENT_FIELD + ":bbb", DEFAULT_CONTENT_FIELD);

    ScoreDoc[] results = this.search(new DocumentQuery(query), 10);
    assertEquals(1, results.length);
    assertEquals(0, results[0].doc);

    query = (NodeTermQuery) qph.parse(DEFAULT_CONTENT_FIELD + ":ddd", DEFAULT_CONTENT_FIELD);

    results = this.search(query, 10);
    assertEquals(0, results.length);
  }

  @Test
  public void testQueryTermAtSamePosition() throws QueryNodeException {
    final Analyzer analyser = new Analyzer() {
      @Override
      protected TokenStreamComponents createComponents(String fieldName,
                                                       Reader reader) {
        WhitespaceTokenizer t = new WhitespaceTokenizer(matchVersion, reader);
        TokenStream ts = new ASCIIFoldingExpansionFilter(t);
        return new TokenStreamComponents(t, ts);
      }
    };

    this.assertQueryEquals("latte +café the", analyser, "latte +(cafe café) the");
    this.assertQueryEquals("+café", analyser, "cafe café");
    this.assertQueryEquals("+café +maté", analyser, "+(cafe café) +(mate maté)");
    this.assertQueryEquals("+café -maté", analyser, "+(cafe café) -(mate maté)");
    this.assertQueryEquals("café maté", analyser, "(cafe café) (mate maté)");
  }

}


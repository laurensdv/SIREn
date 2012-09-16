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
 * @project solr-plugins
 *
 * @author Renaud Delbru [ 25 Apr 2008 ]
 * @link http://renaud.delbru.fr/
 * All rights reserved.
 */
package org.sindice.siren.qparser.tree;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.Operator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
import org.sindice.siren.qparser.analysis.NTripleQueryAnalyzer;
import org.sindice.siren.qparser.analysis.TabularQueryAnalyzer;
import org.sindice.siren.qparser.analysis.TupleTestHelper;
import org.sindice.siren.qparser.keyword.KeywordQParserImpl;
import org.sindice.siren.qparser.ntriple.NTripleQueryParser;
import org.sindice.siren.qparser.tabular.TabularQueryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TupleQueryParserTestHelper extends TupleTestHelper {

  protected static final Logger logger = LoggerFactory.getLogger(TupleQueryParserTestHelper.class);

  public static float getScore(final Map<String, String> ntriples,
                               final Map<String, Float> boosts,
                               final String query,
                               final boolean scattered)
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    RAMDirectory ramDir = null;

    try {
      ramDir = new RAMDirectory();
      TupleQueryParserTestHelper.index(ramDir, ntriples);
      return TupleQueryParserTestHelper.getScore(ramDir, query, boosts, scattered);
    }
    finally {
      if (ramDir != null) ramDir.close();
    }
  }

  public static boolean match(final Map<String, String> ntriples,
                              final Map<String, Float> boosts,
                              final String query,
                              final boolean scattered)
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    RAMDirectory ramDir = null;

    try {
      ramDir = new RAMDirectory();
      TupleQueryParserTestHelper.index(ramDir, ntriples);
      return TupleQueryParserTestHelper.match(ramDir, query, boosts, scattered);
    }
    finally {
      if (ramDir != null) ramDir.close();
    }
  }

  public static boolean match(final String ntriple, final String query)
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    RAMDirectory ramDir = null;

    try {
      ramDir = new RAMDirectory();
      TupleQueryParserTestHelper.index(ramDir, ntriple);
      return TupleQueryParserTestHelper.match(ramDir, query, _defaultField);
    }
    finally {
      if (ramDir != null) ramDir.close();
    }
  }

  public static boolean matchImplicit(final String ntriple, final String query)
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    RAMDirectory ramDir = null;

    try {
      ramDir = new RAMDirectory();
      TupleQueryParserTestHelper.indexImplicit(ramDir, ntriple);
      return TupleQueryParserTestHelper.match(ramDir, query, _implicitField);
    }
    finally {
      if (ramDir != null) ramDir.close();
    }
  }

  private static void index(final RAMDirectory ramDir, final String ntriple)
  throws CorruptIndexException, LockObtainFailedException, IOException {
    IndexWriter ramWriter = null;

    try {
      ramWriter = TupleTestHelper.createRamIndexWriter(ramDir);

      final Document doc = new Document();

      FieldType ft = new FieldType();
      ft.setStored(false);
      ft.setIndexed(true);
      ft.setTokenized(false);
      ft.setOmitNorms(true);
      doc.add(new Field(_ID_FIELD, "doc1", ft));

      ft = new FieldType();
      ft.setStored(false);
      ft.setOmitNorms(true);
      ft.setIndexed(true);
      ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
      ft.setTokenized(true);
      doc.add(new Field(_defaultField, ntriple, ft));
      ramWriter.addDocument(doc);
      ramWriter.commit();
    }
    finally {
      if (ramWriter != null) ramWriter.close();
    }
  }

  private static void indexImplicit(final RAMDirectory ramDir, final String ntriple)
  throws CorruptIndexException, LockObtainFailedException, IOException {
    IndexWriter ramWriter = null;

    try {
      ramWriter = TupleTestHelper.createRamIndexWriter(ramDir);

      final Document doc = new Document();

      FieldType ft = new FieldType();
      ft.setStored(false);
      ft.setIndexed(true);
      ft.setTokenized(true);
      ft.setOmitNorms(true);
      doc.add(new Field(_ID_FIELD, "doc1", ft));

      ft = new FieldType();
      ft.setStored(false);
      ft.setOmitNorms(true);
      ft.setIndexed(true);
      ft.setStoreTermVectorPositions(true);
      ft.setStoreTermVectorOffsets(true);
      ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
      ft.setTokenized(true);
      doc.add(new Field(_implicitField, ntriple, ft));
      ramWriter.addDocument(doc);
      ramWriter.commit();
    }
    finally {
      if (ramWriter != null) ramWriter.close();
    }
  }

  private static void index(final RAMDirectory ramDir,
                            final Map<String, String> ntriples)
  throws CorruptIndexException, LockObtainFailedException, IOException {
    IndexWriter ramWriter = null;

    try {
      ramWriter = TupleTestHelper.createRamIndexWriter(ramDir);

      final Document doc = new Document();

      FieldType ft = new FieldType();
      ft.setStored(false);
      ft.setIndexed(true);
      ft.setTokenized(false);
      ft.setOmitNorms(true);
      doc.add(new Field(_ID_FIELD, "doc1", ft));

      ft = new FieldType();
      ft.setStored(false);
      ft.setIndexed(true);
      ft.setTokenized(true);
      ft.setOmitNorms(true);
      for (final Entry<String, String> entry : ntriples.entrySet()) {
        doc.add(new Field(entry.getKey(), entry.getValue(), ft));
      }
      ramWriter.addDocument(doc);
      ramWriter.commit();
    }
    finally {
      if (ramWriter != null) ramWriter.close();
    }
  }

  protected static boolean match(final RAMDirectory ramDir, final String query,
                               final String field)
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    IndexSearcher ramSearcher = null;
    try {
      ramSearcher = TupleTestHelper.createRamIndexSearcher(ramDir);
      final Query q = testQuery.getQuery(field, query);
      logger.debug("{} = {}", query, q.toString());
      final int hits = ramSearcher.search(q, null, 100).totalHits;
      return (hits >= 1);
    } finally {
      /*
       * IndexSearcher.close() is removed
       * see https://issues.apache.org/jira/browse/LUCENE-3640?attachmentSortBy=dateTime
       */
      if (ramSearcher != null) ramSearcher.getIndexReader().close();
    }
  }

  private static boolean match(final RAMDirectory ramDir, final String query,
                               final Map<String, Float> boosts,
                               final boolean scattered)
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    IndexSearcher ramSearcher = null;
    try {
      ramSearcher = TupleTestHelper.createRamIndexSearcher(ramDir);
      final Query q = testQuery.getScatteredQuery(query, boosts, scattered);
      logger.debug("{} = {}", query, q.toString());
      final int hits = ramSearcher.search(q, null, 100).totalHits;
      return (hits >= 1);
    } finally {
      /*
       * IndexSearcher.close() is removed
       * see https://issues.apache.org/jira/browse/LUCENE-3640?attachmentSortBy=dateTime
       */
      if (ramSearcher != null) ramSearcher.getIndexReader().close();
    }
  }

  private static float getScore(final RAMDirectory ramDir,
                                final String query,
                                  final Map<String, Float> boosts,
                                  final boolean scattered)
  throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
    IndexSearcher ramSearcher = null;
    try {
      ramSearcher = TupleTestHelper.createRamIndexSearcher(ramDir);
      final Query q = testQuery.getScatteredQuery(query, boosts, scattered);
      logger.debug("{} = {}", query, q.toString());
      final ScoreDoc[] result = ramSearcher.search(q, null, 100).scoreDocs;
      assertEquals(1, result.length);
      return result[0].score;
    } finally {
      /*
       * IndexSearcher.close() is removed
       * see https://issues.apache.org/jira/browse/LUCENE-3640?attachmentSortBy=dateTime
       */
      if (ramSearcher != null) ramSearcher.getIndexReader().close();
    }
  }

  public enum QueryParserEnum {
    NTRIPLE, TABULAR, KEYWORD
  }

  /**
   * Create a {@link Query} from the tuple String query
   * @author Stephane Campinas [23 Aug 2012]
   * @email stephane.campinas@deri.org
   *
   */
  private interface TestQuery {

    public Query getQuery(String field, String query)
    throws ParseException;

    public Query getScatteredQuery(String query, Map<String, Float> boosts, boolean scattered)
    throws ParseException;

  }

  private static TestQuery testQuery;

  /**
   * Set what Tuple query parser implementation to use.
   */
  public static void setTestQuery(QueryParserEnum qpe) {
    switch (qpe) {
      case TABULAR:
        testQuery = tabularQuery;
        break;
      case NTRIPLE:
        testQuery = tupleQuery;
        break;
      case KEYWORD:
        testQuery = kwQuery;
        break;
      default:
        throw new EnumConstantNotPresentException(QueryParserEnum.class, qpe.toString());
    }
  }

  // For testing Keyword queries
  private static final TestQuery kwQuery = new TestQuery() {
    @Override
    public Query getScatteredQuery(String query,
                                   Map<String, Float> boosts,
                                   boolean scattered)
    throws ParseException {
      return this.getQuery(null, query);
    }

    @Override
    public Query getQuery(String field, String query)
    throws ParseException {
      final Analyzer analyzer = new WhitespaceAnalyzer(LuceneTestCase.TEST_VERSION_CURRENT);
      final PerFieldAnalyzerWrapper analyzerWrapper = new PerFieldAnalyzerWrapper(analyzer);
      final Map<String, Float> boosts = new HashMap<String, Float>();
      boosts.put(_defaultField, 1.0f);
      boosts.put(_implicitField, 2.5f);
      return new KeywordQParserImpl(analyzerWrapper, boosts, false).parse(query);
    }
  };

  // For testing Tuple queries
  private static final TestQuery tupleQuery = new TestQuery() {
    @Override
    public Query getScatteredQuery(String query,
                                   Map<String, Float> boosts,
                                   boolean scattered)
    throws ParseException {
      return NTripleQueryParser.parse(query, matchVersion, boosts,
        new NTripleQueryAnalyzer(), datatypeConfigs, Operator.AND, scattered);
    }

    @Override
    public Query getQuery(String field, String query)
    throws ParseException {
      return NTripleQueryParser.parse(query, matchVersion, field,
        new NTripleQueryAnalyzer(), datatypeConfigs.get(field), Operator.AND);
    }
  };

  // For testing Tabular queries
  private static final TestQuery tabularQuery = new TestQuery() {
    @Override
    public Query getScatteredQuery(String query,
                                   Map<String, Float> boosts,
                                   boolean scattered)
    throws ParseException {
      return TabularQueryParser.parse(query, matchVersion, boosts,
        new TabularQueryAnalyzer(), datatypeConfigs, Operator.AND, scattered);
    }
    
    @Override
    public Query getQuery(String field, String query)
    throws ParseException {
      return TabularQueryParser.parse(query, matchVersion, field,
        new TabularQueryAnalyzer(), datatypeConfigs.get(field), Operator.AND);
    }
  };

}

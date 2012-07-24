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
 * @project siren-core
 * @author Renaud Delbru [ 6 Oct 2011 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.search.node;

import static org.sindice.siren.search.AbstractTestSirenScorer.NodeBooleanClauseBuilder.must;
import static org.sindice.siren.search.AbstractTestSirenScorer.NodeNumericRangeQueryBuilder.nmqDouble;
import static org.sindice.siren.search.AbstractTestSirenScorer.NodeNumericRangeQueryBuilder.nmqLong;
import static org.sindice.siren.search.AbstractTestSirenScorer.TwigChildBuilder.child;
import static org.sindice.siren.search.AbstractTestSirenScorer.TwigQueryBuilder.twq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryUtils;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;
import org.junit.Test;
import org.sindice.siren.analysis.DoubleNumericAnalyzer;
import org.sindice.siren.analysis.LongNumericAnalyzer;
import org.sindice.siren.analysis.TupleAnalyzer;
import org.sindice.siren.index.codecs.RandomSirenCodec.PostingsFormatType;
import org.sindice.siren.util.BasicSirenTestCase;
import org.sindice.siren.util.XSDDatatype;

public class TestNodeNumericRangeQuery64 extends BasicSirenTestCase {

  private final double[] DOUBLE_NANs =  {
    Double.NaN,
    Double.longBitsToDouble(0x7ff0000000000001L),
    Double.longBitsToDouble(0x7fffffffffffffffL),
    Double.longBitsToDouble(0xfff0000000000001L),
    Double.longBitsToDouble(0xffffffffffffffffL)
  };

  private final Random random = new Random(random().nextLong());
  // distance of entries
  private static long distance;
  // shift the starting of the values to the left, to also have negative values:
  private static final long startOffset = - 1L << 31;
  // number of docs to generate for testing
  private static int noDocs;

  @Override
  protected void configure() throws IOException {
    this.setAnalyzer(AnalyzerType.TUPLE);
    this.setPostingsFormat(PostingsFormatType.RANDOM);
    codec.addSirenFields("field8", "field6", "field4", "field2", "field" + Integer.MAX_VALUE,
                         "ascfield8", "ascfield6", "ascfield4", "ascfield2",
                         "double8", "double6", "double4", "double2");
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    final TupleAnalyzer tupleAnalyzer = (TupleAnalyzer) analyzer;

    noDocs = atLeast(4096);
    distance = (1L << 60) / noDocs;

    tupleAnalyzer.registerLiteralAnalyzer((XSDDatatype.XSD_LONG+"8").toCharArray(), new LongNumericAnalyzer(8));
    tupleAnalyzer.registerLiteralAnalyzer((XSDDatatype.XSD_LONG+"6").toCharArray(), new LongNumericAnalyzer(6));
    tupleAnalyzer.registerLiteralAnalyzer((XSDDatatype.XSD_LONG+"4").toCharArray(), new LongNumericAnalyzer(4));
    tupleAnalyzer.registerLiteralAnalyzer((XSDDatatype.XSD_LONG+"2").toCharArray(), new LongNumericAnalyzer(2));
    tupleAnalyzer.registerLiteralAnalyzer((XSDDatatype.XSD_DOUBLE+"8").toCharArray(), new DoubleNumericAnalyzer(8));
    tupleAnalyzer.registerLiteralAnalyzer((XSDDatatype.XSD_DOUBLE+"6").toCharArray(), new DoubleNumericAnalyzer(6));
    tupleAnalyzer.registerLiteralAnalyzer((XSDDatatype.XSD_DOUBLE+"4").toCharArray(), new DoubleNumericAnalyzer(4));
    tupleAnalyzer.registerLiteralAnalyzer((XSDDatatype.XSD_DOUBLE+"2").toCharArray(), new DoubleNumericAnalyzer(2));
    tupleAnalyzer.registerLiteralAnalyzer((XSDDatatype.XSD_LONG+Integer.MAX_VALUE).toCharArray(), new LongNumericAnalyzer(Integer.MAX_VALUE));

    // Add a series of noDocs docs with increasing long values, by updating the fields
    final ArrayList<Document> docs = new ArrayList<Document>();
    for (int l=0; l<noDocs; l++) {
      final Document doc = new Document();

      long val=distance*l+startOffset;

      // add fields, that have a distance to test general functionality
      doc.add(new Field("field8", getTriple(val, XSDDatatype.XSD_LONG+"8"), this.newStoredFieldType()));
      doc.add(new Field("field6", getTriple(val, XSDDatatype.XSD_LONG+"6"), this.newStoredFieldType()));
      doc.add(new Field("field4", getTriple(val, XSDDatatype.XSD_LONG+"4"), this.newStoredFieldType()));
      doc.add(new Field("field2", getTriple(val, XSDDatatype.XSD_LONG+"2"), this.newStoredFieldType()));
      doc.add(new Field("field"+Integer.MAX_VALUE, getTriple(val, XSDDatatype.XSD_LONG+Integer.MAX_VALUE), this.newStoredFieldType()));

      // add ascending fields with a distance of 1, beginning at -noDocs/2 to
      // test the correct splitting of range and inclusive/exclusive
      val=l-(noDocs/2);

      doc.add(new Field("ascfield8", getTriple(val, XSDDatatype.XSD_LONG+"8"), this.newStoredFieldType()));
      doc.add(new Field("ascfield6", getTriple(val, XSDDatatype.XSD_LONG+"6"), this.newStoredFieldType()));
      doc.add(new Field("ascfield4", getTriple(val, XSDDatatype.XSD_LONG+"4"), this.newStoredFieldType()));
      doc.add(new Field("ascfield2", getTriple(val, XSDDatatype.XSD_LONG+"2"), this.newStoredFieldType()));

      doc.add(new Field("double8", getTriple(val, XSDDatatype.XSD_DOUBLE+"8"), this.newStoredFieldType()));
      doc.add(new Field("double6", getTriple(val, XSDDatatype.XSD_DOUBLE+"6"), this.newStoredFieldType()));
      doc.add(new Field("double4", getTriple(val, XSDDatatype.XSD_DOUBLE+"4"), this.newStoredFieldType()));
      doc.add(new Field("double2", getTriple(val, XSDDatatype.XSD_DOUBLE+"2"), this.newStoredFieldType()));

      docs.add(doc);
    }
    this.addDocument(docs);

    // Remove maximum clause limit for the tests
    NodeBooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
  }

  private static String getTriple(final Number val, final String datatypeURI) {
    return "<http://fake.subject> <http://fake.predicate/"+val+"> \"" + val + "\"^^<"+datatypeURI+"> .\n";
  }

  private static String getLiteralValue(final String triple) {
    final int firstColon = triple.indexOf('"');
    final int secondColon = triple.indexOf('"', firstColon + 1);
    return triple.substring(firstColon + 1, secondColon);
  }

  /** test for constant score + boolean query + filter, the other tests only use the constant score mode */
  private void testRange(final int precisionStep) throws Exception {
    final String field="field"+precisionStep;
    final int count=3000;
    final long lower=(distance*3/2)+startOffset, upper=lower + count*distance + (distance/3);

    Query dq = twq(1)
    .with(child(must(nmqLong(field, precisionStep, lower, upper, true, true)
    .setRewriteMethod(MultiNodeTermQuery.CONSTANT_SCORE_BOOLEAN_QUERY_REWRITE)
    .bound(2, 2)))).getDocumentQuery();

    final String type = " (constant score boolean rewrite)";

    TopDocs topDocs = searcher.search(dq, null, noDocs, Sort.INDEXORDER);

    final ScoreDoc[] sd = topDocs.scoreDocs;
    assertNotNull(sd);
    assertEquals("Score doc count"+type, count, sd.length );
    Document doc=searcher.doc(sd[0].doc);
    assertEquals("First doc"+type, 2*distance+startOffset, Long.parseLong(getLiteralValue(doc.get(field))));
    doc=searcher.doc(sd[sd.length-1].doc);
    assertEquals("Last doc"+type, (1+count)*distance+startOffset, Long.parseLong(getLiteralValue(doc.get(field))));

  }

  @Test
  public void testRange_8bit() throws Exception {
    this.testRange(8);
  }

  @Test
  public void testRange_6bit() throws Exception {
    this.testRange(6);
  }

  @Test
  public void testRange_4bit() throws Exception {
    this.testRange(4);
  }

  @Test
  public void testRange_2bit() throws Exception {
    this.testRange(2);
  }

  @Test
  public void testOneMatchQuery() throws Exception {
    Query dq = twq(1)
    .with(child(must(nmqLong("ascfield8", 8, 1000L, 1000L, true, true)
    .setRewriteMethod(MultiNodeTermQuery.CONSTANT_SCORE_BOOLEAN_QUERY_REWRITE)
    .bound(2, 2)))).getDocumentQuery();

    final TopDocs topDocs = searcher.search(dq, noDocs);
    final ScoreDoc[] sd = topDocs.scoreDocs;
    assertNotNull(sd);
    assertEquals("Score doc count", 1, sd.length );
  }

  private void testLeftOpenRange(final int precisionStep) throws Exception {
    final String field="field"+precisionStep;
    final int count=3000;
    final long upper=(count-1)*distance + (distance/3) + startOffset;

    Query dq = twq(1)
    .with(child(must(nmqLong(field, precisionStep, null, upper, true, true)
      .setRewriteMethod(MultiNodeTermQuery.CONSTANT_SCORE_BOOLEAN_QUERY_REWRITE)
      .bound(2, 2)))).getDocumentQuery();

    TopDocs topDocs = searcher.search(dq, null, noDocs, Sort.INDEXORDER);
    ScoreDoc[] sd = topDocs.scoreDocs;
    assertNotNull(sd);
    assertEquals("Score doc count", count, sd.length );
    Document doc=searcher.doc(sd[0].doc);
    assertEquals("First doc", startOffset, Long.parseLong(getLiteralValue(doc.get(field))));
    doc=searcher.doc(sd[sd.length-1].doc);
    assertEquals("Last doc", (count-1)*distance+startOffset, Long.parseLong(getLiteralValue(doc.get(field))));

    dq = twq(1)
    .with(child(must(nmqLong(field, precisionStep, null, upper, false, true)
      .setRewriteMethod(MultiNodeTermQuery.CONSTANT_SCORE_BOOLEAN_QUERY_REWRITE)
      .bound(2, 2)))).getDocumentQuery();
    topDocs = searcher.search(dq, null, noDocs, Sort.INDEXORDER);
    sd = topDocs.scoreDocs;
    assertNotNull(sd);
    assertEquals("Score doc count", count, sd.length );
    doc=searcher.doc(sd[0].doc);
    assertEquals("First doc", startOffset, Long.parseLong(getLiteralValue(doc.get(field))));
    doc=searcher.doc(sd[sd.length-1].doc);
    assertEquals("Last doc", (count-1)*distance+startOffset, Long.parseLong(getLiteralValue(doc.get(field))));
  }

  @Test
  public void testLeftOpenRange_8bit() throws Exception {
    this.testLeftOpenRange(8);
  }

  @Test
  public void testLeftOpenRange_6bit() throws Exception {
    this.testLeftOpenRange(6);
  }

  @Test
  public void testLeftOpenRange_4bit() throws Exception {
    this.testLeftOpenRange(4);
  }

  @Test
  public void testLeftOpenRange_2bit() throws Exception {
    this.testLeftOpenRange(2);
  }

  private void testRightOpenRange(final int precisionStep) throws Exception {
    final String field="field"+precisionStep;
    final int count=3000;
    final long lower=(count-1)*distance + (distance/3) +startOffset;

    Query dq = twq(1)
    .with(child(must(nmqLong(field, precisionStep, lower, null, true, true)
      .setRewriteMethod(MultiNodeTermQuery.CONSTANT_SCORE_BOOLEAN_QUERY_REWRITE)
      .bound(2, 2)))).getDocumentQuery();

    TopDocs topDocs = searcher.search(dq, null, noDocs, Sort.INDEXORDER);
    ScoreDoc[] sd = topDocs.scoreDocs;
    assertNotNull(sd);
    assertEquals("Score doc count", noDocs-count, sd.length );
    Document doc=searcher.doc(sd[0].doc);
    assertEquals("First doc", count*distance+startOffset, Long.parseLong(getLiteralValue(doc.get(field))));
    doc=searcher.doc(sd[sd.length-1].doc);
    assertEquals("Last doc", (noDocs-1)*distance+startOffset, Long.parseLong(getLiteralValue(doc.get(field))));

    dq = twq(1)
    .with(child(must(nmqLong(field, precisionStep, lower, null, true, false)
      .setRewriteMethod(MultiNodeTermQuery.CONSTANT_SCORE_BOOLEAN_QUERY_REWRITE)
      .bound(2, 2)))).getDocumentQuery();

    topDocs = searcher.search(dq, null, noDocs, Sort.INDEXORDER);
    sd = topDocs.scoreDocs;
    assertNotNull(sd);
    assertEquals("Score doc count", noDocs-count, sd.length );
    doc=searcher.doc(sd[0].doc);
    assertEquals("First doc", count*distance+startOffset, Long.parseLong(getLiteralValue(doc.get(field))));
    doc=searcher.doc(sd[sd.length-1].doc);
    assertEquals("Last doc", (noDocs-1)*distance+startOffset, Long.parseLong(getLiteralValue(doc.get(field))));
  }

  @Test
  public void testRightOpenRange_8bit() throws Exception {
    this.testRightOpenRange(8);
  }

  @Test
  public void testRightOpenRange_6bit() throws Exception {
    this.testRightOpenRange(6);
  }

  @Test
  public void testRightOpenRange_4bit() throws Exception {
    this.testRightOpenRange(4);
  }

  @Test
  public void testRightOpenRange_2bit() throws Exception {
    this.testRightOpenRange(2);
  }


  @Test
  public void testInfiniteValues() throws Exception {
    final ArrayList<Document> docs = new ArrayList<Document>();

    Document doc = new Document();
    doc.add(new Field("double4", getTriple(Double.NEGATIVE_INFINITY, XSDDatatype.XSD_DOUBLE+"4"), this.newStoredFieldType()));
    doc.add(new Field("field4", getTriple(Long.MIN_VALUE, XSDDatatype.XSD_LONG+"4"), this.newStoredFieldType()));
    docs.add(doc);

    doc = new Document();
    doc.add(new Field("double4", getTriple(Double.POSITIVE_INFINITY, XSDDatatype.XSD_DOUBLE+"4"), this.newStoredFieldType()));
    doc.add(new Field("field4", getTriple(Long.MAX_VALUE, XSDDatatype.XSD_LONG+"4"), this.newStoredFieldType()));
    docs.add(doc);

    doc = new Document();
    doc.add(new Field("double4", getTriple(0.0d, XSDDatatype.XSD_DOUBLE+"4"), this.newStoredFieldType()));
    doc.add(new Field("field4", getTriple(0L, XSDDatatype.XSD_LONG+"4"), this.newStoredFieldType()));
    docs.add(doc);

    for (double f : DOUBLE_NANs) {
      doc = new Document();
      doc.add(new Field("double4", getTriple(f, XSDDatatype.XSD_DOUBLE+"4"), this.newStoredFieldType()));
      docs.add(doc);
    }
    this.deleteAll();
    this.addDocument(docs);

    Query q = twq(1)
    .with(child(must(nmqLong("field4", NumericUtils.PRECISION_STEP_DEFAULT, null, null, true, true)
      .bound(2, 2)))).getDocumentQuery();
    TopDocs topDocs = searcher.search(q, 10);
    assertEquals("Score doc count", 3,  topDocs.scoreDocs.length );

    q = twq(1)
    .with(child(must(nmqLong("field4", NumericUtils.PRECISION_STEP_DEFAULT, null, null, false, false)
      .bound(2, 2)))).getDocumentQuery();
    topDocs = searcher.search(q, 10);
    assertEquals("Score doc count", 3,  topDocs.scoreDocs.length );

    q = twq(1)
    .with(child(must(nmqLong("field4", NumericUtils.PRECISION_STEP_DEFAULT, Long.MIN_VALUE, Long.MAX_VALUE, true, true)
      .bound(2, 2)))).getDocumentQuery();
    topDocs = searcher.search(q, 10);
    assertEquals("Score doc count", 3,  topDocs.scoreDocs.length );

    q = twq(1)
    .with(child(must(nmqLong("field4", NumericUtils.PRECISION_STEP_DEFAULT, Long.MIN_VALUE, Long.MAX_VALUE, false, false)
      .bound(2, 2)))).getDocumentQuery();
    topDocs = searcher.search(q, 10);
    assertEquals("Score doc count", 1,  topDocs.scoreDocs.length );

    q = twq(1)
    .with(child(must(nmqLong("field4", NumericUtils.PRECISION_STEP_DEFAULT, null, null, true, true)
      .bound(2, 2)))).getDocumentQuery();
    topDocs = searcher.search(q, 10);
    assertEquals("Score doc count", 3,  topDocs.scoreDocs.length );

    q = twq(1)
    .with(child(must(nmqDouble("double4", NumericUtils.PRECISION_STEP_DEFAULT, null, null, false, false)
      .bound(2, 2)))).getDocumentQuery();
    topDocs = searcher.search(q, 10);
    assertEquals("Score doc count", 3,  topDocs.scoreDocs.length );

    q = twq(1)
    .with(child(must(nmqDouble("double4", NumericUtils.PRECISION_STEP_DEFAULT, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true, true)
      .bound(2, 2)))).getDocumentQuery();
    topDocs = searcher.search(q, 10);
    assertEquals("Score doc count", 3,  topDocs.scoreDocs.length );

    q = twq(1)
    .with(child(must(nmqDouble("double4", NumericUtils.PRECISION_STEP_DEFAULT, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false, false)
      .bound(2, 2)))).getDocumentQuery();
    topDocs = searcher.search(q, 10);
    assertEquals("Score doc count", 1,  topDocs.scoreDocs.length );

    q = twq(1)
    .with(child(must(nmqDouble("double4", NumericUtils.PRECISION_STEP_DEFAULT, Double.NaN, Double.NaN, true, true)
      .bound(2, 2)))).getDocumentQuery();
    topDocs = searcher.search(q, 10);
    assertEquals("Score doc count", DOUBLE_NANs.length,  topDocs.scoreDocs.length );
  }

  // TODO: to uncomment when NodeTermRangeQueries are working
//  private void testRandomTrieAndClassicRangeQuery(final int precisionStep) throws Exception {
//    final String field="field"+precisionStep;
//    int termCountT=0,termCountC=0;
//    final int num = 10 * RANDOM_MULTIPLIER;
//    for (int i = 0; i < num; i++) {
//      long lower=(long)(random.nextDouble()*noDocs*distance)+startOffset;
//      long upper=(long)(random.nextDouble()*noDocs*distance)+startOffset;
//      if (lower>upper) {
//        final long a=lower; lower=upper; upper=a;
//      }
//      final String prefix = DataType.LONG.name() + precisionStep;
//      // test inclusive range
//      NodeNumericRangeQuery<Long> tq=NodeNumericRangeQuery.newLongRange(field, precisionStep, lower, upper, true, true);
//      NodeTermRangeQuery cq=new NodeTermRangeQuery(field, prefix + NumericUtils.longToPrefixCoded(lower), prefix + NumericUtils.longToPrefixCoded(upper), true, true);
//      TopDocs tTopDocs = searcher.search(tq, 1);
//      TopDocs cTopDocs = searcher.search(cq, 1);
//      assertEquals("Returned count for SirenNumericRangeQuery and SirenTermRangeQuery must be equal", cTopDocs.totalHits, tTopDocs.totalHits );
//      termCountT += tq.getTotalNumberOfTerms();
//      termCountC += cq.getTotalNumberOfTerms();
//      // test exclusive range
//      tq=NodeNumericRangeQuery.newLongRange(field, precisionStep, lower, upper, false, false);
//      cq=new NodeTermRangeQuery(field, prefix + NumericUtils.longToPrefixCoded(lower), prefix + NumericUtils.longToPrefixCoded(upper), false, false);
//      tTopDocs = searcher.search(tq, 1);
//      cTopDocs = searcher.search(cq, 1);
//      assertEquals("Returned count for SirenNumericRangeQuery and SirenTermRangeQuery must be equal", cTopDocs.totalHits, tTopDocs.totalHits );
//      termCountT += tq.getTotalNumberOfTerms();
//      termCountC += cq.getTotalNumberOfTerms();
//      // test left exclusive range
//      tq=NodeNumericRangeQuery.newLongRange(field, precisionStep, lower, upper, false, true);
//      cq=new NodeTermRangeQuery(field, prefix + NumericUtils.longToPrefixCoded(lower), prefix + NumericUtils.longToPrefixCoded(upper), false, true);
//      tTopDocs = searcher.search(tq, 1);
//      cTopDocs = searcher.search(cq, 1);
//      assertEquals("Returned count for SirenNumericRangeQuery and SirenTermRangeQuery must be equal", cTopDocs.totalHits, tTopDocs.totalHits );
//      termCountT += tq.getTotalNumberOfTerms();
//      termCountC += cq.getTotalNumberOfTerms();
//      // test right exclusive range
//      tq=NodeNumericRangeQuery.newLongRange(field, precisionStep, lower, upper, true, false);
//      cq=new NodeTermRangeQuery(field, prefix + NumericUtils.longToPrefixCoded(lower), prefix + NumericUtils.longToPrefixCoded(upper), true, false);
//      tTopDocs = searcher.search(tq, 1);
//      cTopDocs = searcher.search(cq, 1);
//      assertEquals("Returned count for SirenNumericRangeQuery and SirenTermRangeQuery must be equal", cTopDocs.totalHits, tTopDocs.totalHits );
//      termCountT += tq.getTotalNumberOfTerms();
//      termCountC += cq.getTotalNumberOfTerms();
//    }
//    if (precisionStep == Integer.MAX_VALUE && searcher.getIndexReader().getSequentialSubReaders().length == 1) {
//      assertEquals("Total number of terms should be equal for unlimited precStep", termCountT, termCountC);
//    } else if (VERBOSE) {
//      System.out.println("Average number of terms during random search on '" + field + "':");
//      System.out.println(" Trie query: " + (((double)termCountT)/(num * 4)));
//      System.out.println(" Classical query: " + (((double)termCountC)/(num * 4)));
//    }
//  }
//
//  @Test
//  public void testRandomTrieAndClassicRangeQuery_8bit() throws Exception {
//    this.testRandomTrieAndClassicRangeQuery(8);
//  }
//
//  @Test
//  public void testRandomTrieAndClassicRangeQuery_6bit() throws Exception {
//    this.testRandomTrieAndClassicRangeQuery(6);
//  }
//
//  @Test
//  public void testRandomTrieAndClassicRangeQuery_4bit() throws Exception {
//    this.testRandomTrieAndClassicRangeQuery(4);
//  }
//
//  @Test
//  public void testRandomTrieAndClassicRangeQuery_2bit() throws Exception {
//    this.testRandomTrieAndClassicRangeQuery(2);
//  }
//
//  @Test
//  public void testRandomTrieAndClassicRangeQuery_NoTrie() throws Exception {
//    this.testRandomTrieAndClassicRangeQuery(Integer.MAX_VALUE);
//  }

  private void testRangeSplit(final int precisionStep) throws Exception {
    final String field="ascfield"+precisionStep;
    // 10 random tests
    final int num = 10 * RANDOM_MULTIPLIER;
    for (int i = 0; i < num; i++) {
      long lower=(long)(random.nextDouble()*noDocs - noDocs/2);
      long upper=(long)(random.nextDouble()*noDocs - noDocs/2);
      if (lower>upper) {
        final long a=lower; lower=upper; upper=a;
      }
      // test inclusive range
      Query tq = twq(1)
      .with(child(must(nmqLong(field, precisionStep, lower, upper, true, true)
        .setRewriteMethod(MultiNodeTermQuery.CONSTANT_SCORE_BOOLEAN_QUERY_REWRITE)
        .bound(2, 2)))).getDocumentQuery();
      TopDocs tTopDocs = searcher.search(tq, 1);
      assertEquals("Returned count of range query must be equal to inclusive range length", upper-lower+1, tTopDocs.totalHits );
      // test exclusive range
      tq=twq(1)
      .with(child(must(nmqLong(field, precisionStep, lower, upper, false, false)
        .setRewriteMethod(MultiNodeTermQuery.CONSTANT_SCORE_BOOLEAN_QUERY_REWRITE)
        .bound(2, 2)))).getDocumentQuery();
      tTopDocs = searcher.search(tq, 1);
      assertEquals("Returned count of range query must be equal to exclusive range length", Math.max(upper-lower-1, 0), tTopDocs.totalHits );
      // test left exclusive range
      tq=twq(1)
      .with(child(must(nmqLong(field, precisionStep, lower, upper, false, true)
        .setRewriteMethod(MultiNodeTermQuery.CONSTANT_SCORE_BOOLEAN_QUERY_REWRITE)
        .bound(2, 2)))).getDocumentQuery();
      tTopDocs = searcher.search(tq, 1);
      assertEquals("Returned count of range query must be equal to half exclusive range length", upper-lower, tTopDocs.totalHits );
      // test right exclusive range
      tq=twq(1)
      .with(child(must(nmqLong(field, precisionStep, lower, upper, true, false)
        .setRewriteMethod(MultiNodeTermQuery.CONSTANT_SCORE_BOOLEAN_QUERY_REWRITE)
        .bound(2, 2)))).getDocumentQuery();
      tTopDocs = searcher.search(tq, 1);
      assertEquals("Returned count of range query must be equal to half exclusive range length", upper-lower, tTopDocs.totalHits );
    }
  }

  @Test
  public void testRangeSplit_8bit() throws Exception {
    this.testRangeSplit(8);
  }

  @Test
  public void testRangeSplit_6bit() throws Exception {
    this.testRangeSplit(6);
  }

  @Test
  public void testRangeSplit_4bit() throws Exception {
    this.testRangeSplit(4);
  }

  @Test
  public void testRangeSplit_2bit() throws Exception {
    this.testRangeSplit(2);
  }

  /** we fake a double test using long2double conversion of NumericUtils */
  private void testDoubleRange(final int precisionStep) throws Exception {
    final String field="double"+precisionStep;
    final double lower=-1000L, upper=+2000L;

//    final Query tq=SirenNumericRangeQuery.newDoubleRange(field, precisionStep,
//      NumericUtils.sortableLongToDouble(lower), NumericUtils.sortableLongToDouble(upper), true, true);

    /*
     * Original Lucene test was faking a double using the NumericUtils.sortableLongToDouble method.
     * Since in Siren we index also the datatype, we cannot do that: using a double query to search
     * for a value indexed with XSD_LONG datatype. 
     */

    Query dq = twq(1)
    .with(child(must(nmqDouble(field, precisionStep, lower, upper, true, true)
      .setRewriteMethod(MultiNodeTermQuery.CONSTANT_SCORE_BOOLEAN_QUERY_REWRITE)
      .bound(2, 2)))).getDocumentQuery();
    final TopDocs tTopDocs = searcher.search(dq, 1);
    assertEquals("Returned count of range query must be equal to inclusive range length", upper-lower+1, tTopDocs.totalHits, 0);
  }

  @Test
  public void testDoubleRange_8bit() throws Exception {
    this.testDoubleRange(8);
  }

  @Test
  public void testDoubleRange_6bit() throws Exception {
    this.testDoubleRange(6);
  }

  @Test
  public void testDoubleRange_4bit() throws Exception {
    this.testDoubleRange(4);
  }

  @Test
  public void testDoubleRange_2bit() throws Exception {
    this.testDoubleRange(2);
  }

  @Test
  public void testEqualsAndHash() throws Exception {
    QueryUtils.checkHashEquals(NodeNumericRangeQuery.newLongRange("test1", 4, 10L, 20L, true, true));
    QueryUtils.checkHashEquals(NodeNumericRangeQuery.newLongRange("test2", 4, 10L, 20L, false, true));
    QueryUtils.checkHashEquals(NodeNumericRangeQuery.newLongRange("test3", 4, 10L, 20L, true, false));
    QueryUtils.checkHashEquals(NodeNumericRangeQuery.newLongRange("test4", 4, 10L, 20L, false, false));
    QueryUtils.checkHashEquals(NodeNumericRangeQuery.newLongRange("test5", 4, 10L, null, true, true));
    QueryUtils.checkHashEquals(NodeNumericRangeQuery.newLongRange("test6", 4, null, 20L, true, true));
    QueryUtils.checkHashEquals(NodeNumericRangeQuery.newLongRange("test7", 4, null, null, true, true));
    QueryUtils.checkEqual(
      NodeNumericRangeQuery.newLongRange("test8", 4, 10L, 20L, true, true),
      NodeNumericRangeQuery.newLongRange("test8", 4, 10L, 20L, true, true)
    );
    QueryUtils.checkUnequal(
      NodeNumericRangeQuery.newLongRange("test9", 4, 10L, 20L, true, true),
      NodeNumericRangeQuery.newLongRange("test9", 8, 10L, 20L, true, true)
    );
    QueryUtils.checkUnequal(
      NodeNumericRangeQuery.newLongRange("test10a", 4, 10L, 20L, true, true),
      NodeNumericRangeQuery.newLongRange("test10b", 4, 10L, 20L, true, true)
    );
    QueryUtils.checkUnequal(
      NodeNumericRangeQuery.newLongRange("test11", 4, 10L, 20L, true, true),
      NodeNumericRangeQuery.newLongRange("test11", 4, 20L, 10L, true, true)
    );
    QueryUtils.checkUnequal(
      NodeNumericRangeQuery.newLongRange("test12", 4, 10L, 20L, true, true),
      NodeNumericRangeQuery.newLongRange("test12", 4, 10L, 20L, false, true)
    );
    QueryUtils.checkUnequal(
      NodeNumericRangeQuery.newLongRange("test13", 4, 10L, 20L, true, true),
      NodeNumericRangeQuery.newFloatRange("test13", 4, 10f, 20f, true, true)
    );
     // difference to int range is tested in TestSirenNumericRangeQuery32
  }

  @Test
  public void testEmptyEnums() throws Exception {
    int count=3000;
    long lower=(distance*3/2)+startOffset, upper=lower + count*distance + (distance/3);
    // test empty enum
    assert lower < upper;
    assertTrue(0 < countTerms((MultiNodeTermQuery) nmqLong("field4", 4, lower, upper, true, true).getNodeQuery()));
    assertEquals(0, countTerms((MultiNodeTermQuery) nmqLong("field4", 4, upper, lower, true, true).getNodeQuery()));
    // test empty enum outside of bounds
    lower = distance*noDocs+startOffset;
    upper = 2L * lower;
    assert lower < upper;
    assertEquals(0, countTerms((MultiNodeTermQuery) nmqLong("field4", 4, lower, upper, true, true).getNodeQuery()));
  }

  private int countTerms(MultiNodeTermQuery q) throws Exception {
    final Terms terms = MultiFields.getTerms(reader, q.getField());
    if (terms == null)
      return 0;
    final TermsEnum termEnum = q.getTermsEnum(terms);
    assertNotNull(termEnum);
    int count = 0;
    BytesRef cur, last = null;
    while ((cur = termEnum.next()) != null) {
      count++;
      if (last != null) {
        assertTrue(last.compareTo(cur) < 0);
      }
      last = BytesRef.deepCopyOf(cur);
    } 
    // LUCENE-3314: the results after next() already returned null are undefined,
    // assertNull(termEnum.next());
    return count;
  }

}

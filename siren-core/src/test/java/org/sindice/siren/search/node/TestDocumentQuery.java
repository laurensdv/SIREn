package org.sindice.siren.search.node;

import java.io.IOException;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.junit.Test;
import org.sindice.siren.index.codecs.RandomSirenCodec.PostingsFormatType;
import org.sindice.siren.search.AbstractTestSirenScorer;
import org.sindice.siren.search.doc.DocumentQuery;

public class TestDocumentQuery
extends AbstractTestSirenScorer {

  @Override
  protected void configure()
  throws IOException {
    this.setAnalyzer(AnalyzerType.TUPLE);
    this.setPostingsFormat(PostingsFormatType.RANDOM);
  }

  @Test
  public void testBoost()
  throws Exception {
    final float boost = 2.5f;

    this.addDocument("\"aaa ccc\" \"one five\" . \"aaa bbb\" \"ccc eee\" .");

    BooleanQuery bq1 = new BooleanQuery();
    NodeTermQuery tq = new NodeTermQuery(new Term (DEFAULT_TEST_FIELD, "one"));
    tq.setBoost(boost);
    bq1.add(new DocumentQuery(tq), Occur.MUST);
    bq1.add(new DocumentQuery(new NodeTermQuery(new Term (DEFAULT_TEST_FIELD, "five"))), Occur.MUST);

    BooleanQuery bq2 = new BooleanQuery();
    tq = new NodeTermQuery(new Term (DEFAULT_TEST_FIELD, "one"));
    DocumentQuery dq = new DocumentQuery(tq);
    dq.setBoost(boost);
    bq2.add(dq, Occur.MUST);
    bq2.add(new DocumentQuery(new NodeTermQuery(new Term (DEFAULT_TEST_FIELD, "five"))), Occur.MUST);

    assertScoreEquals(bq1, bq2);
  }

  /**
   * Tests whether the scores of the two queries are the same.
   */
  public void assertScoreEquals(Query q1, Query q2)
  throws Exception {
    ScoreDoc[] hits1 = searcher.search (q1, null, 1000).scoreDocs;
    ScoreDoc[] hits2 = searcher.search (q2, null, 1000).scoreDocs;

    assertEquals(hits1.length, hits2.length);

    for (int i = 0; i < hits1.length; i++) {
      assertEquals(hits1[i].score, hits2[i].score, 0.0000001f);
    }
  }

}

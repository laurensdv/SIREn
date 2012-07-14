package org.sindice.siren.analysis;

import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.LuceneTestCase;
import org.junit.After;
import org.junit.Before;
import org.sindice.siren.analysis.attributes.NodeAttribute;
import org.sindice.siren.analysis.attributes.PositionAttribute;

public abstract class NodeAnalyzerHelper<A extends Analyzer>
extends LuceneTestCase {

  protected A _a;

  protected abstract A getNodeAnalyzer();

  @Override
  @Before
  public void setUp()
  throws Exception {
    super.setUp();
    _a = getNodeAnalyzer();
  }

  @Override
  @After
  public void tearDown()
  throws Exception {
    super.tearDown();
    _a.close();
  }

  public void assertAnalyzesTo(final Analyzer a, final String input,
                                final String[] expected)
  throws Exception {
    this.assertAnalyzesTo(a, input, expected, null);
  }

  public void assertAnalyzesTo(final Analyzer a, final String input,
                                final String[] expectedImages,
                                final String[] expectedTypes)
  throws Exception {
    this.assertAnalyzesTo(a, input, expectedImages, expectedTypes, null);
  }

  public void assertAnalyzesTo(final Analyzer a, final String input,
                                final String[] expectedImages,
                                final String[] expectedTypes,
                                final int[] expectedPosIncrs)
  throws Exception {
    this.assertAnalyzesTo(a, input, expectedImages, expectedTypes, expectedPosIncrs, null,
      null);
  }

  public void assertAnalyzesTo(final Analyzer a, final String input,
                                final String[] expectedImages,
                                final String[] expectedTypes,
                                final int[] expectedPosIncrs,
                                final IntsRef[] expectedNode,
                                final int[] expectedPos)
  throws Exception {
    final TokenStream t = a.tokenStream("", new StringReader(input));

    assertTrue("has TermAttribute", t.hasAttribute(CharTermAttribute.class));
    final CharTermAttribute termAtt = t.getAttribute(CharTermAttribute.class);

    TypeAttribute typeAtt = null;
    if (expectedTypes != null) {
      assertTrue("has TypeAttribute", t.hasAttribute(TypeAttribute.class));
      typeAtt = t.getAttribute(TypeAttribute.class);
    }

    PositionIncrementAttribute posIncrAtt = null;
    if (expectedPosIncrs != null) {
      assertTrue("has PositionIncrementAttribute", t.hasAttribute(PositionIncrementAttribute.class));
      posIncrAtt = t.getAttribute(PositionIncrementAttribute.class);
    }

    NodeAttribute nodeAtt = null;
    if (expectedNode != null) {
      assertTrue("has NodeAttribute", t.hasAttribute(NodeAttribute.class));
      nodeAtt = t.getAttribute(NodeAttribute.class);
    }

    PositionAttribute posAtt = null;
    if (expectedPos != null) {
      assertTrue("has PositionAttribute", t.hasAttribute(PositionAttribute.class));
      posAtt = t.getAttribute(PositionAttribute.class);
    }

    for (int i = 0; i < expectedImages.length; i++) {

      assertTrue("token "+i+" exists", t.incrementToken());

      assertEquals(expectedImages[i], termAtt.toString());

      if (expectedTypes != null) {
        assertEquals(expectedTypes[i], typeAtt.type());
      }

      if (expectedPosIncrs != null) {
        assertEquals(expectedPosIncrs[i], posIncrAtt.getPositionIncrement());
      }

      if (expectedNode != null) {
        assertEquals(expectedNode[i], nodeAtt.node());
      }

      if (expectedPos != null) {
        assertEquals(expectedPos[i], posAtt.position());
      }
    }

    assertFalse("end of stream, received token " + termAtt.toString(), t.incrementToken());
    t.end();
    t.close();
  }

}

package org.sindice.siren.analysis;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.junit.Test;
import org.sindice.siren.analysis.AnyURIAnalyzer.URINormalisation;
import org.sindice.siren.util.XSDDatatype;

public class TestJsonAnalyzer
extends NodeAnalyzerHelper<JsonAnalyzer> {

  @Override
  protected JsonAnalyzer getNodeAnalyzer() {
    // TODO: URI and other RDF specific data is not handled by the current JSON scanner
    final AnyURIAnalyzer uriAnalyzer = new AnyURIAnalyzer(TEST_VERSION_CURRENT);
    uriAnalyzer.setUriNormalisation(URINormalisation.FULL);
    return new JsonAnalyzer(TEST_VERSION_CURRENT, new StandardAnalyzer(TEST_VERSION_CURRENT), uriAnalyzer);
  }

  @Test
  public void testLiteral()
  throws Exception {
    this.assertAnalyzesTo(_a, "{\"foo BAR\":[null,\"FOO bar\"]}", // null is typed as XSD_STRING
      new String[] { "foo", "bar", "null", "foo", "bar" },
      new String[] { "<ALPHANUM>", "<ALPHANUM>", "<ALPHANUM>", "<ALPHANUM>", "<ALPHANUM>" });
    this.assertAnalyzesTo(_a, "{\"ABC\\u0061\\u0062\\u0063\\u00E9\\u00e9ABC\":\"empty\"}",
      new String[] { "abcabcééabc", "empty" }, new String[] { "<ALPHANUM>", "<ALPHANUM>" });
  }

  @Test
  public void testNumber()
  throws Exception {
    _a.registerLiteralAnalyzer(XSDDatatype.XSD_DOUBLE.toCharArray(), new StandardAnalyzer(TEST_VERSION_CURRENT));
    this.assertAnalyzesTo(_a, "{\"foo\":12}",
      new String[] { "foo", "12" },
      new String[] { "<ALPHANUM>", "<NUM>" });
  }

  @Test
  public void testBoolean()
  throws Exception {
    _a.registerLiteralAnalyzer(XSDDatatype.XSD_BOOLEAN.toCharArray(), new WhitespaceAnalyzer(TEST_VERSION_CURRENT));
    this.assertAnalyzesTo(_a, "{\"foo\":[true,false]}",
      new String[] { "foo", "true", "false" },
      new String[] { "<ALPHANUM>", "word", "word" });
  }

}

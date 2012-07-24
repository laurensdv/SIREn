package org.sindice.siren.analysis;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.document.FieldType.NumericType;
import org.sindice.siren.util.XSDPrimitiveTypeParser;

public class FloatNumericTokenizer
extends NumericTokenizer {

  protected FloatNumericTokenizer(Reader input, int precisionStep) {
    super(input, precisionStep);
  }

  @Override
  protected void setNumericValue(Reader reader)
  throws IOException {
    setFloatValue(XSDPrimitiveTypeParser.parseFloat(reader));
  }

  @Override
  public NumericType getNumericType() {
    return NumericType.FLOAT;
  }

}

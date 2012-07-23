package org.sindice.siren.analysis.attributes;

import org.apache.lucene.analysis.NumericTokenStream.NumericTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute;
import org.apache.lucene.document.FieldType.NumericType;
import org.apache.lucene.util.Attribute;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.BytesRef;

public interface NodeNumericTermAttribute
extends Attribute {

  /** Returns current shift value, undefined before first token */
  int getShift();
  /** Returns current token's raw value as {@code long} with all {@link #getShift} applied, undefined before first token */
  long getRawValue();
  /** Returns value size in bits (32 for {@code float}, {@code int}; 64 for {@code double}, {@code long}) */
  int getValueSize();

  /** <em>Don't call this method!</em>
    * @lucene.internal */
  void init(long value, int valSize, int precisionStep, int shift);

  /** <em>Don't call this method!</em>
    * @lucene.internal */
  void setShift(int shift);

  /**
   * The original Lucene {@link NumericTermAttribute} implements {@link TermToBytesRefAttribute}.
   * This is a problem because we are using the {@link CharTermAttribute} in the SIREn field,
   * which also implements {@link TermToBytesRefAttribute}.
   * This is a problem because the {@link AttributeSource} is not able to choose between the two
   * when requested an attribute implementing {@link TermToBytesRefAttribute}, e.g., in TermsHashPerField.
   * <p>
   * The current solutionn is to fill the {@link BytesRef} attribute of the {@link CharTermAttribute}
   * with the encoded numeric value.
   * @param termAtt
   * @param numericType
   * @return
   */
  int incShift(CharTermAttribute termAtt, NumericType numericType);

}

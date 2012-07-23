package org.sindice.siren.analysis.attributes;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.FieldType.NumericType;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;

public class NodeNumericTermAttributeImpl
extends AttributeImpl
implements NodeNumericTermAttribute {

  private long     value    = 0L;
  private int      valueSize = 0, shift = 0, precisionStep = 0;
  private BytesRef bytesRef  = new BytesRef();
  private char[]   precisionStepCA;

  /**
   * Create a char array from the precision step
   */
  private char[] init() {
    int pstep = precisionStep;
    int size = 1;
    
    while (pstep / 10 > 0) {
      size++;
      pstep /= 10;
    }
    pstep = precisionStep;
    final char[] c = new char[size];
    
    for (int i = size - 1; i >= 0; i--) {
      c[i] = (char) ('0' + pstep % 10);
      pstep /= 10;
    }
    return c;
  }

  private void bytesRefToChar(CharTermAttribute termAtt, NumericType numericType) {
    final String dt = numericType.toString();
    final char[] buffer;
    final int prefixSize = dt.length() + precisionStepCA.length;

    switch (valueSize) {
      case 64:
        NumericUtils.longToPrefixCoded(value, shift, bytesRef);
        buffer = termAtt.resizeBuffer(NumericUtils.BUF_SIZE_LONG + prefixSize);
        break;

      case 32:
        NumericUtils.intToPrefixCoded((int) value, shift, bytesRef);
        buffer = termAtt.resizeBuffer(NumericUtils.BUF_SIZE_INT + prefixSize);
        break;

      default:
        // should not happen
        throw new IllegalArgumentException("valueSize must be 32 or 64");
    }

    /*
     * Prepend the numericType for more precise search
     */
    // write the prefix numericType
    switch (numericType) {
      case INT:
        buffer[0] = 'I';
        buffer[1] = 'N';
        buffer[2] = 'T';
        System.arraycopy(precisionStepCA, 0, buffer, 3, precisionStepCA.length);
        break;
      case FLOAT:
        buffer[0] = 'F';
        buffer[1] = 'L';
        buffer[2] = 'O';
        buffer[3] = 'A';
        buffer[4] = 'T';
        System.arraycopy(precisionStepCA, 0, buffer, 5, precisionStepCA.length);
        break;
      case LONG:
        buffer[0] = 'L';
        buffer[1] = 'O';
        buffer[2] = 'N';
        buffer[3] = 'G';
        System.arraycopy(precisionStepCA, 0, buffer, 4, precisionStepCA.length);
        break;
      case DOUBLE:
        buffer[0] = 'D';
        buffer[1] = 'O';
        buffer[2] = 'U';
        buffer[3] = 'B';
        buffer[4] = 'L';
        buffer[5] = 'E';
        System.arraycopy(precisionStepCA, 0, buffer, 6, precisionStepCA.length);
        break;
      default:
        break;
    }
    // append the numeric encoded value
    for (int i = 0; i < bytesRef.length; i++) {
      buffer[i + prefixSize] = (char) bytesRef.bytes[i];
    }
    termAtt.setLength(bytesRef.length + prefixSize);
  }

  public int getShift() { return shift; }

  public void setShift(int shift) { this.shift = shift; }

  public int incShift(CharTermAttribute termAtt, NumericType numericType) {
    shift += precisionStep;

    try {
      bytesRefToChar(termAtt, numericType);
    } catch (IllegalArgumentException iae) {
      /*
       * TODO: Check what are the implication of this comment on
       * the Siren use case
       */
      // return empty token before first or after last
      termAtt.setEmpty();
      shift = valueSize; // ends the numeric tokenstream
    }
    return shift;
  }

  public long getRawValue() { return value  & ~((1L << shift) - 1L); }

  public int getValueSize() { return valueSize; }

  public void init(long value, int valueSize, int precisionStep, int shift) {
    this.value = value;
    this.valueSize = valueSize;
    this.precisionStep = precisionStep;
    this.shift = shift;
    precisionStepCA = init();
  }

  @Override
  public void clear() {
    // this attribute has no contents to clear!
    // we keep it untouched as it's fully controlled by outer class.
  }

  @Override
  public void copyTo(AttributeImpl target) {
    final NodeNumericTermAttribute a = (NodeNumericTermAttribute) target;
    a.init(value, valueSize, precisionStep, shift);
  }

}

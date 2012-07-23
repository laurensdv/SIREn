package org.sindice.siren.analysis;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.NumericTokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.document.FieldType.NumericType;
import org.apache.lucene.util.NumericUtils;
import org.sindice.siren.analysis.attributes.NodeNumericTermAttribute;

public abstract class NumericTokenizer
extends Tokenizer {

  private final NodeNumericTermAttribute numericAtt = addAttribute(NodeNumericTermAttribute.class);
  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
  private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
  private boolean isReset = false;

  private int valSize = 0; // valSize==0 means not initialized
  private final int precisionStep;

  /**
   * Creates a token stream for numeric values using the default <code>precisionStep</code>
   * {@link NumericUtils#PRECISION_STEP_DEFAULT} (4). The stream is not yet initialized,
   * before using set a value using the various set<em>???</em>Value() methods.
   */
  public NumericTokenizer(Reader input) {
    this(input, AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY, NumericUtils.PRECISION_STEP_DEFAULT);
  }
  
  /**
   * Creates a token stream for numeric values with the specified
   * <code>precisionStep</code>. The stream is not yet initialized,
   * before using set a value using the various set<em>???</em>Value() methods.
   */
  public NumericTokenizer(Reader input, final int precisionStep) {
    this(input, AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY, precisionStep);
  }

  /**
   * Expert: Creates a token stream for numeric values with the specified
   * <code>precisionStep</code> using the given
   * {@link org.apache.lucene.util.AttributeSource.AttributeFactory}.
   * The stream is not yet initialized,
   * before using set a value using the various set<em>???</em>Value() methods.
   */
  public NumericTokenizer(Reader input, AttributeFactory factory, final int precisionStep) {
    super(factory, input);
    if (precisionStep < 1)
      throw new IllegalArgumentException("precisionStep must be >=1");
    this.precisionStep = precisionStep;
    numericAtt.setShift(-precisionStep);
  }

  /**
   * Initializes the token stream with the supplied <code>long</code> value.
   * @param value the value, for which this TokenStream should enumerate tokens.
   * @return this instance, because of this you can use it the following way:
   * <code>new Field(name, new NumericTokenStream(precisionStep).setLongValue(value))</code>
   */
  public NumericTokenizer setLongValue(final long value) {
    numericAtt.init(value, valSize = 64, precisionStep, -precisionStep);
    return this;
  }
  
  /**
   * Initializes the token stream with the supplied <code>int</code> value.
   * @param value the value, for which this TokenStream should enumerate tokens.
   * @return this instance, because of this you can use it the following way:
   * <code>new Field(name, new NumericTokenStream(precisionStep).setIntValue(value))</code>
   */
  public NumericTokenizer setIntValue(final int value) {
    numericAtt.init(value, valSize = 32, precisionStep, -precisionStep);
    return this;
  }
  
  /**
   * Initializes the token stream with the supplied <code>double</code> value.
   * @param value the value, for which this TokenStream should enumerate tokens.
   * @return this instance, because of this you can use it the following way:
   * <code>new Field(name, new NumericTokenStream(precisionStep).setDoubleValue(value))</code>
   */
  public NumericTokenizer setDoubleValue(final double value) {
    numericAtt.init(NumericUtils.doubleToSortableLong(value), valSize = 64, precisionStep, -precisionStep);
    return this;
  }

  /**
   * Initializes the token stream with the supplied <code>float</code> value.
   * @param value the value, for which this TokenStream should enumerate tokens.
   * @return this instance, because of this you can use it the following way:
   * <code>new Field(name, new NumericTokenStream(precisionStep).setFloatValue(value))</code>
   */
  public NumericTokenizer setFloatValue(final float value) {
    numericAtt.init(NumericUtils.floatToSortableInt(value), valSize = 32, precisionStep, -precisionStep);
    return this;
  }

  @Override
  public void reset()
  throws IOException {
    setNumericValue(this.input);
    if (valSize == 0)
      throw new IllegalStateException("call set???Value() before usage");
    numericAtt.setShift(-precisionStep);
    isReset = true;
  }

  @Override
  public void reset(Reader input)
  throws IOException {
    super.reset(input);
    this.reset();
  }

  @Override
  public final boolean incrementToken()
  throws IOException {
    if (!isReset) {
      isReset = true;
      this.reset();
    }
    if (valSize == 0)
      throw new IllegalStateException("call set???Value() before usage");
    
    // this will only clear all other attributes in this TokenStream
    clearAttributes();

    final int shift = numericAtt.incShift(termAtt, getNumericType());
    typeAtt.setType((shift == 0) ? NumericTokenStream.TOKEN_TYPE_FULL_PREC : NumericTokenStream.TOKEN_TYPE_LOWER_PREC);
    posIncrAtt.setPositionIncrement((shift == 0) ? 1 : 0);
    return (shift < valSize);
  }

  /** Returns the precision step. */
  public int getPrecisionStep() {
    return precisionStep;
  }

  /**
   * Set the numeric value of the token stream
   */
  protected abstract void setNumericValue(final Reader reader)
  throws IOException;

  public abstract NumericType getNumericType();

}

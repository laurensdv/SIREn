package org.sindice.siren.analysis;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.sindice.siren.analysis.attributes.DatatypeAttribute;
import org.sindice.siren.analysis.attributes.NodeAttribute;

public class JsonTokenizer
extends Tokenizer {

  private final JsonTokenizerImpl scanner;

  /** Maximum length limitation */
  private int                     maxLength = 0;

  private int                     length    = 0;

  /** Token Definition */
  public static final int         COMMA     = 0;
  public static final int         COLON     = 1;
  public static final int         NULL      = 2;
  public static final int         TRUE      = 3;
  public static final int         FALSE     = 4;
  public static final int         NUMBER    = 5;
  public static final int         LITERAL   = 6;

  public JsonTokenizer(Reader input, int maxLength) {
    super(input);
    scanner = new JsonTokenizerImpl(input);
    this.maxLength = maxLength;
    this.initAttributes();
  }

  protected static String[]        TOKEN_TYPES;

  public static String[] getTokenTypes() {
    if (TOKEN_TYPES == null) {
      TOKEN_TYPES = new String[7];
      TOKEN_TYPES[COMMA] = "<COMMA>";
      TOKEN_TYPES[COLON] = "<COLON>";
      TOKEN_TYPES[NULL] = "<NULL>";
      TOKEN_TYPES[TRUE] = "<TRUE>";
      TOKEN_TYPES[FALSE] = "<FALSE>";
      TOKEN_TYPES[NUMBER] = "<INTEGER>";
      TOKEN_TYPES[LITERAL] = "<LITERAL>";
    }
    return TOKEN_TYPES;
  }

  // the TupleTokenizer generates 6 attributes:
  // term, offset, positionIncrement, type, datatype, node
  private CharTermAttribute          termAtt;
  private OffsetAttribute            offsetAtt;
  private PositionIncrementAttribute posIncrAtt;
  private TypeAttribute              typeAtt;
  private DatatypeAttribute          dtypeAtt;
  private NodeAttribute              nodeAtt;

  private void initAttributes() {
    termAtt = this.addAttribute(CharTermAttribute.class);
    offsetAtt = this.addAttribute(OffsetAttribute.class);
    posIncrAtt = this.addAttribute(PositionIncrementAttribute.class);
    typeAtt = this.addAttribute(TypeAttribute.class);
    dtypeAtt = this.addAttribute(DatatypeAttribute.class);
    nodeAtt = this.addAttribute(NodeAttribute.class);
  }

  @Override
  public final boolean incrementToken()
  throws IOException {
    this.clearAttributes();

    while (length < maxLength) {
      posIncrAtt.setPositionIncrement(1);
      return this.nextTupleToken();
    }
    return false;
  }

  private boolean nextTupleToken()
  throws IOException {
    final int tokenType = scanner.getNextToken();

    System.out.println("\t*** object: " + scanner.getNodeObjectPath());
    System.out.println("\t*** value: " + scanner.getNodeValuePath());

    switch (tokenType) {
      case FALSE:
      case TRUE:
      case NULL:
      case NUMBER:
      case LITERAL:
      case COMMA:
      case COLON:
        break;

      case JsonTokenizerImpl.YYEOF:
        return false;

      default:
        return false;
    }
    return true;
  }

  @Override
  public void reset() throws IOException {
    super.reset();
    if (input.markSupported()) {
      input.reset();
    }
    scanner.yyreset(input);
    length = 0;
  }

  @Override
  public void reset(final Reader reader) throws IOException {
    input = reader;
    this.reset();
  }

  @Override
  public void close()
  throws IOException {
    scanner.yyclose();
  }

}

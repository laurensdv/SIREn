package org.sindice.siren.analysis;

import java.io.IOException;
import java.io.Reader;

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
  public static final int         NULL      = 0;
  public static final int         TRUE      = 1;
  public static final int         FALSE     = 2;
  public static final int         NUMBER    = 3;
  public static final int         LITERAL   = 4;

  public JsonTokenizer(Reader input, int maxLength) {
    super(input);
    scanner = new JsonTokenizerImpl(input);
    this.maxLength = maxLength;
    this.initAttributes();
  }

  protected static String[]        TOKEN_TYPES;

  public static String[] getTokenTypes() {
    if (TOKEN_TYPES == null) {
      TOKEN_TYPES = new String[5];
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

    switch (tokenType) {
      case FALSE:
        termAtt.append("false");
        this.updateToken(tokenType, null, scanner.yychar());
        length++;
        break;

      case TRUE:
        termAtt.append("true");
        this.updateToken(tokenType, null, scanner.yychar());
        length++;
        break;

      case NULL:
        termAtt.append("null");
        this.updateToken(tokenType, null, scanner.yychar());
        length++;
        break;

      case NUMBER:
        termAtt.append(scanner.getNumber());
        this.updateToken(tokenType, null, scanner.yychar());
        length++;
        break;

      case LITERAL:
        scanner.getLiteralText(termAtt);
        this.updateToken(tokenType, null, scanner.yychar() + 1);
        length++;
        break;

      case JsonTokenizerImpl.YYEOF:
        return false;

      default:
        return false;
    }
    return true;
  }

  /**
   * Update type, datatype, offset, tuple id and cell id of the token
   *
   * @param tokenType The type of the generated token
   * @param datatypeURI The datatype of the generated token
   * @param startOffset The starting offset of the token
   */
  private void updateToken(final int tokenType, final char[] datatypeURI, final int startOffset) {
    // Update offset
    offsetAtt.setOffset(this.correctOffset(startOffset),
      this.correctOffset(startOffset + termAtt.length()));
    // update token type
    typeAtt.setType(TOKEN_TYPES[tokenType]);
    // update datatype
//    dtypeAtt.setDatatypeURI(datatypeURI);
    // Update structural information
    nodeAtt.copyNode(scanner.getNodePath());
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

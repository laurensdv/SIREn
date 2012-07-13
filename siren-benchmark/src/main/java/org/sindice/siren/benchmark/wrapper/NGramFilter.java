/**
 * Copyright (c) 2009-2012 National University of Ireland, Galway. All Rights Reserved.
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
 * @project siren-benchmark
 * @author Renaud Delbru [ 12 Jul 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.benchmark.wrapper;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

public final class NGramFilter extends TokenFilter {

  private final CharTermAttribute cTermAtt;
  private final PositionIncrementAttribute posIncAtt;

  /**
   * The number of terms per gram
   */
  private final int nGram;

  /**
   * The array to store each of the gram's temrs
   */
  private final StringBuilder[] gram;

  /**
   * The number of terms currently in gram
   */
  private int nTerms = 0;

  /**
   * The string to use when joining adjacent tokens to form a shingle
   */
  public static String TOKEN_SEPARATOR = " ";

  public NGramFilter(final TokenStream input, final int nGram) {
    super(input);
    if (nGram < 2)
      throw new IllegalArgumentException("nGram must be >= 2");
    this.nGram = nGram;
    posIncAtt = this.addAttribute(PositionIncrementAttribute.class);
    cTermAtt = this.addAttribute(CharTermAttribute.class);
    gram = new StringBuilder[nGram];
    for (int i = 0; i < nGram; i++)
      gram[i] = new StringBuilder();
  }

  /**
   * Clear the array used tp store the gram's terms
   */
  private final void clearNGram() {
    for (int i = nTerms; i < nGram; i++)
      gram[i].setLength(0);
  }

  /**
   * Add a new term to the current gram
   * @return true if there is still terms left to be added for the current gram
   */
  private final boolean addTerm() {
    gram[nTerms++].append(cTermAtt.buffer(), 0, cTermAtt.length());
    return (nTerms < nGram) ? true : false;
  }

  /**
   * Write the NGram to the CharTermAttribute Buffer
   */
  private final void writeNGram() {
    cTermAtt.setLength(0);
    for (int i = 0; i < this.nGram; i++) {
      cTermAtt.append(gram[i]);
      if (i + 1 != this.nGram) {
        cTermAtt.append(TOKEN_SEPARATOR);
      }
    }
    nTerms--;
    for (int i = 0; i < this.nGram - 1; i++) {
      gram[i].setLength(0);
      gram[i].append(gram[i+1]);
    }
  }

  @Override
  public boolean incrementToken()
  throws IOException {
    while (nTerms < nGram && input.incrementToken()) {
      // check that the words are consecutives
      if (posIncAtt.getPositionIncrement() != 1) {
        nTerms = 0;
        this.clearNGram();
        // Init the number of words in the N-Gram
        this.addTerm();
        continue;
      }
      if (this.addTerm() == false) { // Constructs the NGram
        this.writeNGram();
        this.clearNGram();
        return true;
      }
    }
    // reached EOS
    return false;
  }

  @Override
  public void reset() throws IOException {
    super.reset();
    // necessary to avoid creating ngrams over multiple values
    nTerms = 0;
    this.clearNGram();
  }

}


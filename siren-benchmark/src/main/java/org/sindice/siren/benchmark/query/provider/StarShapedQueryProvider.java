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
 * @author Renaud Delbru [ 6 Jul 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.benchmark.query.provider;

import java.io.IOException;
import java.util.Random;

import org.sindice.siren.benchmark.generator.lexicon.TermLexiconReader;
import org.sindice.siren.benchmark.generator.lexicon.TermLexiconWriter.TermGroups;
import org.sindice.siren.benchmark.query.provider.KeywordQuery.Occur;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class StarShapedQueryProvider extends QueryProvider {

  private final int             nbPairs;
  private TermLexiconReader     readerAtt;
  private TermLexiconReader     readerVal;

  private final Random          random        = new Random(42);
  private final boolean         withLOW;

  public StarShapedQueryProvider(final Occur[] occurs, final TermGroups[] groups, final int nbPairs, final boolean withLOW)
  throws IOException {
    super(occurs, groups);
    this.nbPairs = nbPairs;
    this.withLOW = withLOW;
  }

  @Override
  public String toString() {
    return nbPairs + "pairs" + "-" + withLOW;
  }

  public void setLexiconReaders(final TermLexiconReader readerAtt, final TermLexiconReader readerVal) {
    this.readerAtt = readerAtt;
    this.readerVal = readerVal;
  }

  @Override
  public Query next() {
    final StarShapedQuery starQ = new StarShapedQuery(nbPairs);

    queryPos++;
    for (int i = 0; i < nbPairs; i++) {
      final FieldQuery fq = new FieldQuery();
      try {
        fq.setFieldName(this.getAttribute());
        fq.setValueQuery(this.getValue());
      } catch (final IOException e) {
        e.printStackTrace();
      }
      starQ.addFieldQuery(fq);
    }
    return starQ;
  }

  private KeywordQuery getValue()
  throws IOException {
    final KeywordQuery kq = new KeywordQuery();

    switch (random.nextInt(4)) {
      case 0: // term1 AND term2 AND term3 AND term4
        kq.addKeyword(readerVal.getRandomTerm(this.getRandomGroup()), Occur.MUST);
        kq.addKeyword(readerVal.getRandomTerm(this.getRandomGroup()), Occur.MUST);
      case 1: // term1 AND term2
        kq.addKeyword(readerVal.getRandomTerm(this.getRandomGroup()), Occur.MUST);
        kq.addKeyword(readerVal.getRandomTerm(this.getRandomGroup()), Occur.MUST);
        break;
      case 2: // term1 OR term2 OR term3 OR term4
        kq.addKeyword(readerVal.getRandomTerm(this.getRandomGroup()), Occur.SHOULD);
        kq.addKeyword(readerVal.getRandomTerm(this.getRandomGroup()), Occur.SHOULD);
      case 3: // term1 OR term2
        kq.addKeyword(readerVal.getRandomTerm(this.getRandomGroup()), Occur.SHOULD);
        kq.addKeyword(readerVal.getRandomTerm(this.getRandomGroup()), Occur.SHOULD);
        break;
    }
    return kq;
  }

  private KeywordQuery getAttribute()
  throws IOException {
    final KeywordQuery kq = new KeywordQuery();
    kq.addKeyword(readerAtt.getRandomTerm(this.getRandomGroup()), Occur.MUST);

    return kq;
  }

  private TermGroups getRandomGroup() {
    if (withLOW) {
      final int offset = random.nextInt(TermGroups.values().length);
      return TermGroups.values()[offset];
    } else {
      if (Math.random() >= 0.5) {
        return TermGroups.MEDIUM;
      }
      return TermGroups.HIGH;
    }
  }

  @Override
  public boolean hasNext() {
    return queryPos < nbQueries;
  }

  @Override
  public void remove() {
    throw new NotImplementedException();
  }

  @Override
  public void close()
  throws IOException {
    readerAtt.close();
    readerVal.close();
  }

}

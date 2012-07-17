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
package org.sindice.siren.benchmark.generator.lexicon;

import java.io.IOException;
import java.util.LinkedList;

public class MockTermFreqIterator extends TermFreqIterator {

  LinkedList<TermFreq> data = new LinkedList<TermFreq>();

  public MockTermFreqIterator() {
    data.add(new TermFreq("aaa", 45));
    data.add(new TermFreq("zzz", 11));
    data.add(new TermFreq("eee", 6));
    data.add(new TermFreq("bbb", 2));
    data.add(new TermFreq("hhh", 2));
    data.add(new TermFreq("ccc", 20));
    data.add(new TermFreq("ddd", 5));
    data.add(new TermFreq("ooo", 1));
    data.add(new TermFreq("jjj", 9));
    data.add(new TermFreq("ppp", 1));
  }

  @Override
  public boolean hasNext() {
    return !data.isEmpty();
  }

  @Override
  public TermFreq next() {
    return data.poll();
  }

  @Override
  public void remove() {}

  @Override
  public void close()
  throws IOException {}

}

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
 * @project siren-core
 * @author Renaud Delbru [ 19 Jan 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.index.codecs.siren10;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.apache.lucene.util.IntsRef;

import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;

public class NodeEqualityBenchmark extends SimpleBenchmark {

  public static void main(final String[] args) throws Exception {
    Runner.main(NodeEqualityBenchmark.class, args);
  }

  private final ArrayList<IntsRef> arrays1 = new ArrayList<IntsRef>();

  private final Random r = new Random(0);

  @Override
  public void setUp() {
    for (int i = 0; i < 100; i++) {
      final int len = r.nextInt(10);
      final IntsRef a = new IntsRef(len);
      for (int j = 0; j < len; j++) {
        a.ints[j] = r.nextInt(100);
      }
      a.offset = 0;
      a.length = len;
      arrays1.add(a);
    }
  }

  IntsRef a = new IntsRef(100);

  public int timeArrayCopyEquals(final int reps) throws IOException {
    int equal = 0;

    for (int i = 0; i < reps; i++) {
      for (int j = 0; j < arrays1.size(); j++) {
        final IntsRef a1 = arrays1.get(j);

        if (a1.intsEquals(a)) {
          equal++;
        }

        a.copyInts(a1);
      }
    }

    return equal;
  }

  long hash = Long.MAX_VALUE;

  public int timeArrayHashcodeEquals(final int reps) throws IOException {
    int equal = 0;

    for (int i = 0; i < reps; i++) {
      for (int j = 0; j < arrays1.size(); j++) {
        final IntsRef a1 = arrays1.get(j);

        final int hash1 = a1.hashCode();
        if (hash == hash1) {
          equal++;
        }

        hash = hash1;
      }
    }

    return equal;
  }

}

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
package org.sindice.siren.search.node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;

public class MicroBenchmark extends SimpleBenchmark {

  public static void main(final String[] args) throws Exception {
    Runner.main(MicroBenchmark.class, args);
  }

  private final ArrayList<int[]> arrays1 = new ArrayList<int[]>();
  private final ArrayList<int[]> arrays2 = new ArrayList<int[]>();

  private final Random r = new Random(0);

  @Override
  public void setUp() {
    for (int i = 0; i < 100; i++) {
      final int len = r.nextInt(10);
      final int[] a = new int[len];
      for (int j = 0; j < len; j++) {
        a[j] = r.nextInt(100);
      }
      arrays1.add(a);
    }

    for (int i = 0; i < 100; i++) {
      final int len = r.nextInt(10);
      final int[] a = new int[len];
      for (int j = 0; j < len; j++) {
        a[j] = r.nextInt(100);
      }
      arrays2.add(a);
    }
  }

  public int timeArrayCloneEquals(final int reps) throws IOException {
    int equal = 0;

    for (int i = 0; i < reps; i++) {
      for (int j = 0; j < arrays1.size(); j++) {
        final int[] a1 = arrays1.get(j).clone();
        final int[] a2 = arrays2.get(j);

        if (Arrays.equals(a1, a2)) {
          equal++;
        }
      }
    }

    return equal;
  }

  int[] a = new int[100];

  public int timeArrayCopyEquals(final int reps) throws IOException {
    int equal = 0;

    for (int i = 0; i < reps; i++) {
      for (int j = 0; j < arrays1.size(); j++) {
        final int[] a1 = arrays1.get(j);
        final int[] a2 = arrays2.get(j);

        System.arraycopy(a1, 0, a, 0, a1.length);

        if (Arrays.equals(a, a2)) {
          equal++;
        }
      }
    }

    return equal;
  }

  public int timeArrayEquals(final int reps) throws IOException {
    int equal = 0;

    for (int i = 0; i < reps; i++) {
      for (int j = 0; j < arrays1.size(); j++) {
        final int[] a1 = arrays1.get(j);
        final int[] a2 = arrays2.get(j);

        if (Arrays.equals(a1, a2)) {
          equal++;
        }
      }
    }

    return equal;
  }

  public int timeArrayHashcodeEquals(final int reps) throws IOException {
    int equal = 0;

    for (int i = 0; i < reps; i++) {
      for (int j = 0; j < arrays1.size(); j++) {
        final int[] a1 = arrays1.get(j);
        final int[] a2 = arrays2.get(j);

        final int hash1 = Arrays.hashCode(a1);
        final int hash2 = Arrays.hashCode(a2);
        if (hash1 == hash2) {
          equal++;
        }
      }
    }

    return equal;
  }

}

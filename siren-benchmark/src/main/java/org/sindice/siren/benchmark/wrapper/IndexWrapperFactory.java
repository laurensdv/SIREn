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
package org.sindice.siren.benchmark.wrapper;

import java.io.File;
import java.io.IOException;

public class IndexWrapperFactory {

  public enum IndexWrapperType {
    Siren10,
    // For tests only
    Mock
  }

  public static final IndexWrapper createIndexWrapper(final IndexWrapperType type, final File indexPath)
  throws IOException {
    switch (type) {
      case Siren10:
        return new Siren10IndexWrapper(indexPath);

      default:
        throw new IllegalArgumentException("IndexWrapperFactory: Error: There is no " + type + " IndexWrapper to implement.");
    }
  }
}

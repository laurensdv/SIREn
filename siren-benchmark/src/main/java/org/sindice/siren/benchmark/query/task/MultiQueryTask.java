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
package org.sindice.siren.benchmark.query.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sindice.siren.benchmark.Measurement;
import org.sindice.siren.benchmark.query.provider.Query;

public abstract class MultiQueryTask extends QueryTask {

  protected final List<Query> queries;

  public MultiQueryTask(final List<Query> queries) {
    this.queries = queries;
  }

  public MultiQueryTask(final Query[] queries) {
    this.queries = new ArrayList<Query>(Arrays.asList(queries));
  }

  @Override
  public String toString() {
    return "Multi Query Task: " + queries.size() + " - " + queries.get(0).toString();
  }

  @Override
  public abstract Measurement call() throws Exception;

}

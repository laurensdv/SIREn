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
package org.sindice.siren.benchmark;

import java.io.File;

import org.apache.commons.configuration.ConfigurationException;
import org.sindice.siren.benchmark.query.QueryExecutorCLI;
import org.sindice.siren.benchmark.query.StarShapedQueryExecutorCLI;

public class BenchmarkStarShapedQueryExecutor extends BenchmarkQueryExecutor {

  /**
   * @param config
   * @throws ConfigurationException
   */
  public BenchmarkStarShapedQueryExecutor(final File config) throws ConfigurationException {
    super(config);
  }

  @Override
  public void loadParameters() {
    super.loadParameters();
    // Terms Spec may be multi valued
    params.put(StarShapedQueryExecutorCLI.ATTRIBUTE_LEXICON_DIR, config.getListValues(StarShapedQueryExecutorCLI.ATTRIBUTE_LEXICON_DIR));
    params.put(StarShapedQueryExecutorCLI.VALUE_LEXICON_DIR, config.getListValues(StarShapedQueryExecutorCLI.VALUE_LEXICON_DIR));
    params.put(StarShapedQueryExecutorCLI.NB_PAIRS, config.getListValues(StarShapedQueryExecutorCLI.NB_PAIRS));
    params.put(StarShapedQueryExecutorCLI.WITH_LOW, config.getListValues(StarShapedQueryExecutorCLI.WITH_LOW));
  }

  @Override
  public QueryExecutorCLI getCLI() {
    return new StarShapedQueryExecutorCLI();
  }

  public static void main(final String[] args)
  throws Exception {
    if (args.length != 1) {
      throw new RuntimeException("Only one argument: the config file");
    }
    final BenchmarkStarShapedQueryExecutor star = new BenchmarkStarShapedQueryExecutor(new File(args[0]));
    star.loadParameters();
    star.execute();
  }

}

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
import org.sindice.siren.benchmark.query.AbstractQueryExecutorCLI;
import org.sindice.siren.benchmark.query.FieldQueryExecutorCLI;

public class BenchmarkFieldQueryExecutor extends BenchmarkQueryExecutor {

  public BenchmarkFieldQueryExecutor(final File config)
  throws ConfigurationException {
    super(config);
  }

  @Override
  public void loadParameters() {
    super.loadParameters();
    // Terms Spec may be multi valued
    params.put(FieldQueryExecutorCLI.PREDICATE_LEXICON_DIR, config.getListValues(FieldQueryExecutorCLI.PREDICATE_LEXICON_DIR));
    params.put(FieldQueryExecutorCLI.PREDICATE_SPEC, config.getListValues(FieldQueryExecutorCLI.PREDICATE_SPEC));
    params.put(FieldQueryExecutorCLI.VALUE_SPEC, config.getListValues(FieldQueryExecutorCLI.VALUE_SPEC));
    params.put(FieldQueryExecutorCLI.VALUE_LEXICON_DIR, config.getListValues(FieldQueryExecutorCLI.VALUE_LEXICON_DIR));
    params.put(FieldQueryExecutorCLI.VALUE_QUERY_PROVIDER, config.getListValues(FieldQueryExecutorCLI.VALUE_QUERY_PROVIDER));
  }

  @Override
  public AbstractQueryExecutorCLI getCLI() {
    return new FieldQueryExecutorCLI();
  }

  public static void main(final String[] args)
  throws Exception {
    if (args.length != 1) {
      throw new RuntimeException("Only one argument: the config file");
    }
    final BenchmarkFieldQueryExecutor basic = new BenchmarkFieldQueryExecutor(new File(args[0]));
    basic.loadParameters();
    basic.execute();
  }

}

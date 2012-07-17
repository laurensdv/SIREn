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
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.sindice.siren.benchmark.query.QueryExecutorCLI;
import org.sindice.siren.benchmark.util.BenchmarkConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BenchmarkQueryExecutor {

  private final Logger logger = LoggerFactory.getLogger(BenchmarkQueryExecutor.class);

  protected final StringBuilder sBuilder = new StringBuilder();
  protected final BenchmarkConfiguration config;
  protected final HashMap<String, List<Object>> params = new HashMap<String, List<Object>>();

  public BenchmarkQueryExecutor(final File config) throws ConfigurationException {
    this.config = new BenchmarkConfiguration(config);
  }

  public abstract QueryExecutorCLI getCLI();

  public void loadParameters() {
    // Wrapper is the only params with multiple values
    params.put(QueryExecutorCLI.INDEX_WRAPPER, config.getListValues(QueryExecutorCLI.INDEX_WRAPPER));
    params.put(QueryExecutorCLI.NB_QUERIES, config.getListValues(QueryExecutorCLI.NB_QUERIES));
    params.put(QueryExecutorCLI.MULTI_QUERY, config.getListValues(QueryExecutorCLI.MULTI_QUERY));
    params.put(QueryExecutorCLI.COLD_CACHE, config.getListValues(QueryExecutorCLI.COLD_CACHE));
    params.put(QueryExecutorCLI.RESULTS_DIR, config.getListValues(QueryExecutorCLI.RESULTS_DIR));
    params.put(QueryExecutorCLI.SEED, config.getListValues(QueryExecutorCLI.SEED));
    params.put(QueryExecutorCLI.THREADS, config.getListValues(QueryExecutorCLI.THREADS));
  }

  public void execute() throws Exception {
    final String timeStamp = this.putTimeStamp();
    final ArrayList<ArrayList<String>> cmds = new ArrayList<ArrayList<String>>();
    final ArrayList<String> args = new ArrayList<String>();
    final Set<Entry<String, List<Object>>> entries = params.entrySet();
    String param = null;
    Iterator<Entry<String, List<Object>>> it = entries.iterator();
    Entry<String, List<Object>> entry = null;
    ListIterator<Object> listIt = null;

    while (it.hasNext()) { // single valued params
      entry = it.next();
      if (entry.getValue().size() == 1) {
        param = "--" + entry.getKey() + " " + entry.getValue().get(0);
        if (entry.getKey().equals(QueryExecutorCLI.RESULTS_DIR))
          args.add(param + "/" + timeStamp);
        else if (entry.getKey().equals(QueryExecutorCLI.INDEX_WRAPPER))
          args.add(param + " --" + QueryExecutorCLI.INDEX_DIRECTORY + " " + config.getValue((String) entry.getValue().get(0)));
        else
          args.add(param);
      }
    }
    it = entries.iterator();
    cmds.add(args);
    while (it.hasNext()) { // multi valued params: generate cmds in factoriel fashion
      entry = it.next();
      if (entry.getValue().size() != 1) {
        final ArrayList<ArrayList<String>> cmds_clone = (ArrayList<ArrayList<String>>) cmds.clone();
        cmds.clear();
        for (final ArrayList<String> cmd : cmds_clone) {
          listIt = entry.getValue().listIterator();
          while (listIt.hasNext()) {
            final ArrayList<String> clone = (ArrayList<String>) cmd.clone();
            final String value = (String) listIt.next();
            param = "--" + entry.getKey() + " " + value;
            if (entry.getKey().equals(QueryExecutorCLI.INDEX_WRAPPER))
              clone.add(param + " --" + QueryExecutorCLI.INDEX_DIRECTORY + " " + config.getValue(value));
            else
              clone.add(param);
            cmds.add(clone);
          }
        }
      }
    }
    for (final ArrayList<String> cmd : cmds) {
      logger.info("Executing: " + this.toString(cmd));
      this.getCLI().parseAndExecute(this.toArray(cmd));
    }
  }


  private final String putTimeStamp() {
    final SimpleDateFormat date = new SimpleDateFormat("y-M-d-H-m");

    return date.format(new Date(System.currentTimeMillis()));
  }

  private final String[] toArray(final ArrayList<String> cmd) {
    final ArrayList<String> array = new ArrayList<String>();

    for (int i = 0; i < cmd.size(); i++) {
      for (final String word : cmd.get(i).split(" ")) {
        array.add(word);
      }
    }
    return array.toArray(new String[array.size()]);
  }

  private final String toString(final ArrayList<String> cmd) {
    sBuilder.setLength(0);
    for (final String s : cmd) {
      sBuilder.append(s + " ");
    }
    return sBuilder.toString();
  }

}

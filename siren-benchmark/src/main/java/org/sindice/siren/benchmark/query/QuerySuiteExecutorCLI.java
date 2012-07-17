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
package org.sindice.siren.benchmark.query;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.apache.commons.io.FileUtils;
import org.sindice.siren.benchmark.wrapper.IndexWrapperFactory.IndexWrapperType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuerySuiteExecutorCLI {

  protected final OptionParser parser;
  protected OptionSet          opts;

  public static final String       HELP                = "help";

  public static final String       INDEX_WRAPPER       = "index";
  public static final String       INDEX_DIRECTORY     = "input";

  public static final String       LEXICON_DIR         = "lexicon";

  public static final String       NB_QUERIES          = "nb-queries";
  public static final String       COLD_CACHE          = "cold-cache";
  public static final String       SEED                = "seed";

  public static final String       QUERY_SPEC_DIR      = "./src/main/resources/query/spec/";

  final Logger logger = LoggerFactory.getLogger(QueryExecutor.class);

  public QuerySuiteExecutorCLI () {
    parser = new OptionParser();
    parser.accepts(HELP, "print this help");
    parser.accepts(INDEX_WRAPPER, "The type of index to use: [Siren10]")
        .withRequiredArg().ofType(IndexWrapperType.class);
    parser.accepts(INDEX_DIRECTORY, "Specify the indexing directory.")
        .withRequiredArg().ofType(File.class);
    parser.accepts(NB_QUERIES, "the number of queries that will be generated " +
    		"by the QueryProvider.")
        .withRequiredArg().ofType(Integer.class).defaultsTo(50);
    parser.accepts(COLD_CACHE, "The cache will be flushed before executing the query.")
        .withRequiredArg().ofType(Boolean.class).defaultsTo(false);
    parser.accepts(SEED, "The seed for generating random keywords")
        .withRequiredArg().ofType(Integer.class).defaultsTo(42);
    parser.accepts(LEXICON_DIR, "The directory where the groups terms " +
    		"files for the Indexed Data are stored.")
        .withRequiredArg().ofType(File.class);
  }

  public final void parseAndExecute(final String[] args) throws Exception {
    opts = parser.parse(args);
    if (opts.has(HELP)) {
      parser.printHelpOn(System.out);
      return;
    }

    // Index directory
    if (!opts.has(INDEX_DIRECTORY)) {
      parser.printHelpOn(System.err);
      throw new IllegalArgumentException("You must specify an index " +
          "directory (--input)");
    }

    // Index wrapper
    if (!opts.has(INDEX_WRAPPER)) {
      parser.printHelpOn(System.err);
      throw new IllegalArgumentException("You must specify a type of index" +
          " (--index)");
    }

    // Term lexicon directory
    if (!opts.has(LEXICON_DIR)) {
      parser.printHelpOn(System.err);
      throw new IllegalArgumentException("You must specify the term lexicon" +
          " directory (--lexicon)");
    }

    final List<String> commands = this.preprareCommands(args);
    for (final File querySpec : this.getQuerySpecSuite()) {
      logger.info("Execute benchmark for query specification: {}", querySpec.getName());
      this.executeQueryExecutorProcess(commands, querySpec);
    }
  }

  private List<String> preprareCommands(final String[] args) {
    final List<String> commands = new ArrayList<String>();
    // add java command
    commands.add(System.getProperty("java.home") + "/bin/java");

    final RuntimeMXBean mx = ManagementFactory.getRuntimeMXBean();
    // add jvm args
    commands.addAll(mx.getInputArguments());
    // add classpath
    commands.add("-cp");
    commands.add(mx.getClassPath());
    // add main class
    commands.add(QueryExecutorCLI.class.getName());
    // add main args
    for (final String arg : args) {
      commands.add(arg);
    }

    return commands;
  }

  @SuppressWarnings("unchecked")
  private Collection<File> getQuerySpecSuite() {
    return FileUtils.listFiles(new File(QUERY_SPEC_DIR), new String[] { "txt" }, false);
  }

  private void executeQueryExecutorProcess(final List<String> commands, final File querySpec)
  throws IOException {
    // append query spec argument
    final List<String> fullCommands = new ArrayList<String>(commands);
    fullCommands.add("--" + QueryExecutorCLI.QUERY_SPEC);
    fullCommands.add(querySpec.getAbsolutePath());

    final ProcessBuilder builder = new ProcessBuilder(fullCommands);
    builder.redirectErrorStream(true);
    final Process process = builder.start();

    // Read out dir output
    final InputStream is = process.getInputStream();
    final InputStreamReader isr = new InputStreamReader(is);
    final BufferedReader br = new BufferedReader(isr);

    String line;
    while ((line = br.readLine()) != null) {
      System.out.println(line);
    }

    //Wait to get exit value
    try {
      final int exitValue = process.waitFor();
      if (exitValue != 0) {
        logger.error("QueryExecutorCLI subprocess exit with value {}", exitValue);
      }
    }
    catch (final InterruptedException e) {
      logger.error("QueryExecutorCLI subprocess interrupted", e);
    }
  }

  public static void main(final String[] args) throws Exception {
    final QuerySuiteExecutorCLI cmd = new QuerySuiteExecutorCLI();
    cmd.parseAndExecute(args);
  }

}

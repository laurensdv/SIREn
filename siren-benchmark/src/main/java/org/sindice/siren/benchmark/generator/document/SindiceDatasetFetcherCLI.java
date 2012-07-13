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
 * @author Renaud Delbru [ 11 Jul 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.benchmark.generator.document;

import java.io.File;
import java.io.IOException;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SindiceDatasetFetcherCLI {

  private final OptionParser parser;
  private OptionSet          opts;
  final Logger               logger              = LoggerFactory.getLogger(SindiceDatasetFetcherCLI.class);

  private final String       HELP                = "help";
  private final String       OUTPUT_DIR          = "output-dir";
  private final String       SAMPLE_SIZE         = "sample-size";

  private File               outputDir;
  private int                sampleSize;

  public SindiceDatasetFetcherCLI () {
    parser = new OptionParser();
    parser.accepts(HELP, "print this help");
    parser.accepts(OUTPUT_DIR, "The directory where the dataset archives will be stored")
          .withRequiredArg().ofType(File.class);
    parser.accepts(SAMPLE_SIZE, "The size (in number of archives) of the dataset sample")
          .withRequiredArg().ofType(Integer.class);
  }

  public void parseAndExecute (final String[] cmds) throws IOException {
    opts = parser.parse(cmds);
    if (opts.has(HELP)) {
      parser.printHelpOn(System.out);
      return;
    }

    // OutPut Directory
    if (opts.has(OUTPUT_DIR)) {
      outputDir = (File) opts.valueOf(OUTPUT_DIR);
    }
    else {
      parser.printHelpOn(System.err);
      throw new IllegalArgumentException("You must specify an output directory (--output-dir)");
    }

    // Sample size
    if (opts.has(SAMPLE_SIZE)) {
      sampleSize = (Integer) opts.valueOf(SAMPLE_SIZE);
    }
    else {
      parser.printHelpOn(System.err);
      throw new IllegalArgumentException("You must specify a sample size (--sample-size)");
    }

    // Fetch dataset sample
    final SindiceDatasetFetcher fetcher = new SindiceDatasetFetcher();
    fetcher.fetch(outputDir, sampleSize);
  }

  public static void main(final String[] args) throws IOException {
    final SindiceDatasetFetcherCLI cmd = new SindiceDatasetFetcherCLI();
    cmd.parseAndExecute(args);
  }

}

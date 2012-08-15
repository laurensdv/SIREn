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
package org.sindice.siren.benchmark.generator.viz;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExporterCLI {

  private static final Logger  logger         = LoggerFactory
                                              .getLogger(ExporterCLI.class);

  protected final OptionParser parser;
  protected OptionSet          opts;

  public static final String   HELP           = "help";

  public static final String   DIRECTORIES    = "directories";
  public static final String   DIFF           = "diff";
  public static final String   FORMATTER_TYPE = "formatter-type";

  public ExporterCLI() {
    parser = new OptionParser();
    parser.accepts(HELP, "print this help");
    parser.accepts(DIRECTORIES, "The list of index directories")
        .withRequiredArg().ofType(File.class).withValuesSeparatedBy(',');
    parser.accepts(DIFF, "Compute the diff of the selected directories");
    parser.accepts(FORMATTER_TYPE, "The formatter to export results with: " +
          Arrays.toString(FormatterType.values()))
          .withRequiredArg().ofType(FormatterType.class).defaultsTo(FormatterType.HTML);
  }

  public final void parseAndExecute(final String[] cmds) throws Exception {
    opts = parser.parse(cmds);
    if (opts.has(HELP)) {
      parser.printHelpOn(System.out);
      return;
    }

    // DIRECTORIES
    List<File> directories = null;
    if (opts.has(DIRECTORIES)) {
      directories = (List<File>) opts.valuesOf(DIRECTORIES);
    } else {
      parser.printHelpOn(System.out);
      throw new RuntimeException("Missing option: " + DIRECTORIES);
    }
    // FORMATTER_TYPE
    final FormatterType ft = (FormatterType) opts.valueOf(FORMATTER_TYPE);

    logger.info("Exporting results from {} using formatter={}", directories, ft);
    final Exporter ex = new Exporter();
    if (opts.has(DIFF)) {
      ex.diff(ft, directories, null);
    } else {
      ex.export(ft, directories, null);
    }
  }

  public static void main(String[] args)
  throws Exception {
    ExporterCLI cli = new ExporterCLI();
    cli.parseAndExecute(args);
  }

}

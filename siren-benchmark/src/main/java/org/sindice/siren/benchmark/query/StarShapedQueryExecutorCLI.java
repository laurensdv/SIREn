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

import java.io.File;
import java.io.IOException;

import org.sindice.siren.benchmark.generator.lexicon.TermLexiconReader;
import org.sindice.siren.benchmark.query.provider.QueryProvider;
import org.sindice.siren.benchmark.query.provider.TreeQueryProvider;

/**
 *
 */
public class StarShapedQueryExecutorCLI extends AbstractQueryExecutorCLI {

  public static final String              ATTRIBUTE_LEXICON_DIR       = "attribute-lexicon-dir";
  public static final String              VALUE_LEXICON_DIR           = "value-lexicon-dir";
  public static final String              NB_PAIRS                    = "nb-pairs";
  public static final String              WITH_LOW                    = "low-group";

  public StarShapedQueryExecutorCLI() {
    super();
    parser.accepts(ATTRIBUTE_LEXICON_DIR, "The directory of the predicate lexicon.")
          .withRequiredArg().ofType(File.class);
    parser.accepts(VALUE_LEXICON_DIR, "The directory of the value lexicon.")
          .withRequiredArg().ofType(File.class);
    parser.accepts(NB_PAIRS, "The number of attribute-value pairs for these star-shaped queries.")
          .withRequiredArg().ofType(Integer.class);
    parser.accepts(WITH_LOW, "use or not LOW frequency group")
          .withRequiredArg().ofType(Boolean.class);
  }

  @Override
  protected QueryProvider getQueryProvider()
  throws IOException {
    if (!opts.has(NB_PAIRS)) {
      parser.printHelpOn(System.out);
      throw new RuntimeException("nb-pairs option missing");
    }
    if (!opts.has(ATTRIBUTE_LEXICON_DIR) || !opts.has(VALUE_LEXICON_DIR)) {
      parser.printHelpOn(System.out);
      throw new RuntimeException("lexicon paths missing");
    }
    if (!opts.has(WITH_LOW)) {
      parser.printHelpOn(System.out);
      throw new RuntimeException("low-group option missing");
    }
    final TreeQueryProvider provider = new TreeQueryProvider(null, null, (Integer) opts.valueOf(NB_PAIRS), (Boolean) opts.valueOf(WITH_LOW));
    final TermLexiconReader readerAtt = new TermLexiconReader((File) opts.valueOf(ATTRIBUTE_LEXICON_DIR));
    final TermLexiconReader readerVal = new TermLexiconReader((File) opts.valueOf(VALUE_LEXICON_DIR));

    provider.setLexiconReaders(readerAtt, readerVal);
    return provider;
  }

  public static void main (final String[] args)
  throws Exception {
    final StarShapedQueryExecutorCLI cli = new StarShapedQueryExecutorCLI();
    cli.parseAndExecute(args);
  }

}

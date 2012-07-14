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
import java.lang.reflect.Constructor;
import java.util.List;

import org.sindice.siren.benchmark.generator.lexicon.TermLexiconReader;
import org.sindice.siren.benchmark.generator.lexicon.TermLexiconWriter.TermGroup;
import org.sindice.siren.benchmark.query.provider.AttributeQueryProvider;
import org.sindice.siren.benchmark.query.provider.BooleanQuery.Occur;
import org.sindice.siren.benchmark.query.provider.BooleanQueryProvider;
import org.sindice.siren.benchmark.query.provider.QueryProvider;
import org.sindice.siren.benchmark.query.provider.PrimitiveQueryProvider;

public class FieldQueryExecutorCLI extends AbstractQueryExecutorCLI {

  public static final String       VALUE_QUERY_PROVIDER  = "value-provider";
  public static final String       PREDICATE_LEXICON_DIR = "predicate-lexicon-dir";
  public static final String       VALUE_LEXICON_DIR     = "value-lexicon-dir";
  public static final String       PREDICATE_SPEC        = "predicate-spec";
  public static final String       VALUE_SPEC            = "value-spec";

  private Occur[]            occurs              = null;
  private TermGroup[]       groups              = null;

  public FieldQueryExecutorCLI () {
    super();
    parser.accepts(VALUE_QUERY_PROVIDER, "the class of the QueryProvider that will be used.")
          .withRequiredArg().ofType(String.class);
    parser.accepts(PREDICATE_LEXICON_DIR, "The directory of the predicate lexicon.")
          .withRequiredArg().ofType(File.class);
    parser.accepts(VALUE_LEXICON_DIR, "The directory of the value lexicon.")
          .withRequiredArg().ofType(File.class);
    parser.accepts(PREDICATE_SPEC, "The specification for the predicate, in the format <TermGroups:Occur>+, values seperated by ','.")
          .withRequiredArg().withValuesSeparatedBy(',');
    parser.accepts(VALUE_SPEC, "The specification for the values, in the format <TermGroups:Occur>+, values seperated by ','.")
          .withRequiredArg().withValuesSeparatedBy(',');
  }

  @Override
  protected QueryProvider getQueryProvider() throws IOException {
    final AttributeQueryProvider provider = new AttributeQueryProvider(null, null);
    provider.setPredicateProvider(this.getPredicateProvider());
    provider.setValueProvider(this.getValueProvider());
// rendel - 25-09-10 - commented because compilation error
//    provider.setSpec(listToString(opts.valuesOf(PREDICATE_SPEC)) + "--:--" +
//                     listToString(opts.valuesOf(VALUE_SPEC)) + "_" +
//                     ((File) opts.valueOf(VALUE_LEXICON_DIR)).getName());
    return provider;
  }

  private final String listToString(final List<?> spec) {
    final StringBuilder builder = new StringBuilder();
    for (final Object s : spec) {
      builder.append(s.toString());
    }
    return builder.toString();
  }

  private BooleanQueryProvider getPredicateProvider() throws IOException {
    // Terms Specification
    if (opts.has(PREDICATE_SPEC)) {
      this.setTermsSpec(opts.valuesOf(PREDICATE_SPEC));
    }
    else {
      logger.error("You have to specify at least one query specification");
      parser.printHelpOn(System.err);
      System.exit(1);
    }

    // QueryProvider
    final BooleanQueryProvider queryProvider = new BooleanQueryProvider(occurs, groups);

    // Term lexicon directory
    File termLexiconDir = null;
    if (opts.has(PREDICATE_LEXICON_DIR)) {
      termLexiconDir = (File) opts.valueOf(PREDICATE_LEXICON_DIR);
    }
    else {
      logger.error("You have to specify the directory of the term lexicon");
      parser.printHelpOn(System.err);
      System.exit(1);
    }

    // Instantiate the query provider
    final TermLexiconReader reader = new TermLexiconReader(termLexiconDir);
    reader.setSeed(seed);
    queryProvider.setTermLexicon(reader);

    return queryProvider;
  }

  private PrimitiveQueryProvider getValueProvider() throws IOException {
    // Terms Specification
    if (opts.has(VALUE_SPEC)) {
      this.setTermsSpec(opts.valuesOf(VALUE_SPEC));
    }
    else {
      logger.error("You have to specify at least one query specification");
      parser.printHelpOn(System.err);
      System.exit(1);
    }

    // QueryProvider
    PrimitiveQueryProvider queryProvider = null;
    if (opts.has(VALUE_QUERY_PROVIDER)) {
      try {
        final Class clazz = Class.forName((String) opts.valueOf(VALUE_QUERY_PROVIDER));
        final Constructor<PrimitiveQueryProvider> ct = clazz.getConstructors()[0];
        queryProvider = ct.newInstance(occurs, groups);
      } catch (final Exception e) {
        logger.error("The QueryProvider class could not be instanciated.", e);
        System.exit(1);
      }
    }
    logger.info("Value query provider: " + (String) opts.valueOf(VALUE_QUERY_PROVIDER));

    // Term lexicon directory
    File termLexiconDir = null;
    if (opts.has(VALUE_LEXICON_DIR)) {
      termLexiconDir = (File) opts.valueOf(VALUE_LEXICON_DIR);
    }
    else {
      logger.error("You have to specify the directory of the term lexicon");
      parser.printHelpOn(System.err);
      System.exit(1);
    }

    // Instantiate the query provider
    final TermLexiconReader reader = new TermLexiconReader(termLexiconDir);
    reader.setSeed(seed);
    queryProvider.setTermLexicon(reader);

    return queryProvider;
  }

  private final void setTermsSpec(final List<?> spec) {
    String[] specValues = null;
    groups = new TermGroup[spec.size()];
    occurs = new Occur[spec.size()];
    for (int i = 0; i < spec.size(); i++) {
      specValues = ((String) spec.get(i)).trim().split(":");
      groups[i] = TermGroup.valueOf(specValues[0]);
      occurs[i] = Occur.valueOf(specValues[1]);
    }
  }

  public static void main (final String[] args)
  throws Exception {
    final FieldQueryExecutorCLI cli = new FieldQueryExecutorCLI();
    cli.parseAndExecute(args);
  }
}

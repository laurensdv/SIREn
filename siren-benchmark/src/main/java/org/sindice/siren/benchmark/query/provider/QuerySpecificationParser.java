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
 * @author Renaud Delbru [ 13 Jul 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.benchmark.query.provider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.sindice.siren.benchmark.generator.lexicon.TermLexiconGenerator;
import org.sindice.siren.benchmark.generator.lexicon.TermLexiconWriter.TermGroup;
import org.sindice.siren.benchmark.query.provider.Query.Occur;

/**
 * Parse query specification in JSON syntax
 * <p>
 * Grammar: <br>
 * <ul>
 * <li> GROUP     : HIGH | MEDIUM | LOW
 * <li> OCCUR     : MUST | SHOULD | MUST_NOT
 * <li> EMPTY     : NULL
 * <li> PHRASE    : { phrase: GROUP }
 * <li> BOOLEAN   : { boolean: GROUP:OCCUR+ }
 * <li> PRIMITIVE : EMPTY | PHRASE | BOOLEAN
 * <li> ATTRIBUTE : { attribute: PRIMITIVE , value: PRIMITIVE }
 * <li> TREE      : { root: ATTRIBUTE+ , ancestors: TREE* }
 * </ul>
 */
public class QuerySpecificationParser {

  private final File lexiconDir;

  public QuerySpecificationParser(final File lexiconDir) {
    this.lexiconDir = lexiconDir;
  }

  public TreeQuerySpecification parse(final File jsonQuerySpec)
  throws IOException {
    final ObjectMapper mapper = new ObjectMapper();
    final JsonNode node = mapper.readTree(jsonQuerySpec);

    return this.visitTree(node);
  }

  private TreeQuerySpecification visitTree(final JsonNode node) {
    final TreeQuerySpecification spec = new TreeQuerySpecification();

    final Iterator<JsonNode> rootAttributes = node.path(TreeQuerySpecification.ROOT_ATTRIBUTE).getElements();
    while (rootAttributes.hasNext()) {
      final JsonNode attribute = rootAttributes.next();
      spec.addRootAttribute(this.visitAttribute(attribute));
    }

    final Iterator<JsonNode> ancestors = node.path(TreeQuerySpecification.ANCESTOR_ATTRIBUTE).getElements();
    while (ancestors.hasNext()) {
      final JsonNode ancestor = ancestors.next();
      spec.addAncestor(this.visitTree(ancestor));
    }

    return spec;
  }

  private AttributeQuerySpecification visitAttribute(final JsonNode node) {
    final AttributeQuerySpecification spec = new AttributeQuerySpecification(lexiconDir);

    final JsonNode attrNode = node.path(AttributeQuerySpecification.ATTRIBUTE_ATTRIBUTE);
    spec.addAttribute(this.visitPrimitive(attrNode));
    final JsonNode valueNode = node.path(AttributeQuerySpecification.VALUE_ATTRIBUTE);
    spec.addValue(this.visitPrimitive(valueNode));

    return spec;
  }

  private PrimitiveQuerySpecification visitPrimitive(final JsonNode node) {
    // if boolean
    if (node.has(BooleanQuerySpecification.BOOLEAN_ATTRIBUTE)) {
      return this.visitBoolean(node);
    }
    // if phrase
    else if (node.has(PhraseQuerySpecification.PHRASE_ATTRIBUTE)) {
      return this.visitPhrase(node);
    }
    // else
    else {
      return new EmptyQuerySpecification();
    }
  }

  private PhraseQuerySpecification visitPhrase(final JsonNode node) {
    final PhraseQuerySpecification spec = new PhraseQuerySpecification();

    spec.addTerm(node.path(PhraseQuerySpecification.PHRASE_ATTRIBUTE).asText());

    return spec;
  }

  private BooleanQuerySpecification visitBoolean(final JsonNode node) {
    final BooleanQuerySpecification spec = new BooleanQuerySpecification();

    final Iterator<JsonNode> clauses = node.path(BooleanQuerySpecification.BOOLEAN_ATTRIBUTE).getElements();
    while (clauses.hasNext()) {
      spec.addClause(clauses.next().asText());
    }

    return spec;
  }

  public static class TreeQuerySpecification extends QuerySpecification {

    private static String ROOT_ATTRIBUTE = "root";
    private static String ANCESTOR_ATTRIBUTE = "ancestors";

    private final List<AttributeQuerySpecification> root = new ArrayList<AttributeQuerySpecification>();
    private final List<TreeQuerySpecification> ancestors = new ArrayList<TreeQuerySpecification>();

    public void addRootAttribute(final AttributeQuerySpecification attribute) {
      this.root.add(attribute);
    }

    public void addAncestor(final TreeQuerySpecification tree) {
      this.ancestors.add(tree);
    }

    @Override
    public TreeQueryProvider getQueryProvider() throws IOException {
      final TreeQueryProvider provider = new TreeQueryProvider();
      for (final AttributeQuerySpecification attrSpec : root) {
        provider.addRootAttributeProvider(attrSpec.getQueryProvider());
      }
      for (final TreeQuerySpecification treeSpec : ancestors) {
        provider.addAncestorProvider(treeSpec.getQueryProvider());
      }
      return provider;
    }

  }

  public static class AttributeQuerySpecification extends QuerySpecification {

    private PrimitiveQuerySpecification attribute, value;
    private final File lexiconDir;

    private static String ATTRIBUTE_ATTRIBUTE = "attribute";
    private static String VALUE_ATTRIBUTE = "value";

    public AttributeQuerySpecification(final File lexiconDir) {
      this.lexiconDir = lexiconDir;
    }

    public void addAttribute(final PrimitiveQuerySpecification attribute) {
      this.attribute = attribute;
    }

    public void addValue(final PrimitiveQuerySpecification value) {
      this.value = value;
    }

    @Override
    public AttributeQueryProvider getQueryProvider() throws IOException {
      final AttributeQueryProvider provider = new AttributeQueryProvider();

      // Prepare primitive query provider for attribute
      final PrimitiveQueryProvider attrProvider = attribute.getQueryProvider();
      attrProvider.setTermLexicon(new File(lexiconDir, TermLexiconGenerator.PREDICATE_SUBDIR));
      provider.addAttributeProvider(attrProvider);

      // Prepare primitive query provider for value
      final PrimitiveQueryProvider valueProvider = value.getQueryProvider();
      valueProvider.setTermLexicon(new File(lexiconDir, TermLexiconGenerator.OBJECT_SUBDIR));
      provider.addValueProvider(valueProvider);
      return provider;
    }

  }

  public static class PhraseQuerySpecification extends PrimitiveQuerySpecification {

    private static String PHRASE_ATTRIBUTE = "phrase";

    private TermGroup termGroup;

    public void addTerm(final String term) {
      this.termGroup = TermGroup.valueOf(term);
    }

    @Override
    public PhraseQueryProvider getQueryProvider() {
      return new PhraseQueryProvider(termGroup);
    }

  }

  public static class BooleanQuerySpecification extends PrimitiveQuerySpecification {

    private static String BOOLEAN_ATTRIBUTE = "boolean";

    private final Map<TermGroup, Occur> clauses = new HashMap<TermGroup, Occur>();

    public void addClause(final String clause) {
      final String[] elements = clause.trim().split(":");
      final TermGroup group = TermGroup.valueOf(elements[0]);
      final Occur occur = Occur.valueOf(elements[1]);
      clauses.put(group, occur);
    }

    @Override
    public BooleanQueryProvider getQueryProvider() {
      return new BooleanQueryProvider(clauses);
    }

  }

  public static class EmptyQuerySpecification extends PrimitiveQuerySpecification {

    @Override
    public EmptyQueryProvider getQueryProvider() {
      return new EmptyQueryProvider();
    }

  }

  public static abstract class QuerySpecification {

    public abstract QueryProvider getQueryProvider() throws IOException;

  }

  public static abstract class PrimitiveQuerySpecification extends QuerySpecification {

    @Override
    public abstract PrimitiveQueryProvider getQueryProvider();

  }

}

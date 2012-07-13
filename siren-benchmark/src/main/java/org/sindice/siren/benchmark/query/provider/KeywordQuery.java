/**
 * @project index-generator
 * @author Renaud Delbru [ 27 Mar 2010 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.benchmark.query.provider;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class KeywordQuery implements Query {

  /**
   * Specifies how keywords are to occur.
   */
  public enum Occur {
    MUST,
    MUST_NOT,
    SHOULD
  };

  private final Map<String, Occur> keywords;

  public KeywordQuery() {
    keywords = new HashMap<String, Occur>(4);
  }

  public void addKeyword(final String term, final Occur occur) {
    this.keywords.put(term, occur);
  }

  public Set<Entry<String, Occur>> getKeywords() {
    return this.keywords.entrySet();
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    for (final Entry<String, Occur> keyword : keywords.entrySet()) {
      builder.append("<");
      builder.append(keyword.getKey());
      builder.append(",");
      builder.append(keyword.getValue());
      builder.append(">,");
    }
    builder.setLength(builder.length() - 1);
    return builder.toString();
  }

}

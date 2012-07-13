/**
 * @project index-generator
 * @author Renaud Delbru [ 27 Mar 2010 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.benchmark.query.provider;


public class FieldQuery implements Query {

  private KeywordQuery fieldName;
  private Query valueQuery;

  public FieldQuery() {}

  public void setFieldName(final KeywordQuery fieldName) {
    this.fieldName = fieldName;
  }

  public void setValueQuery(final Query valueQuery) {
    this.valueQuery = valueQuery;
  }

  public KeywordQuery getFieldName() {
    return this.fieldName;
  }

  public Query getValueQuery() {
    return this.valueQuery;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append(fieldName);
    builder.append(":");
    builder.append(valueQuery.toString());
    return builder.toString();
  }

}

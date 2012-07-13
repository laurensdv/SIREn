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
package org.sindice.siren.benchmark.generator.document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class BenchmarkDocument {

  private final Map<String, String[]> fields = new TreeMap<String, String[]>();

  public static final String DEFAULT_URL_FIELD = "url";
  public static final String DEFAULT_CONTENT_FIELD = "content";

  public BenchmarkDocument() {}

  /**
   * Clear the content of the document
   */
  public void clear() {
    fields.clear();
  }

  public List<String> get(final String field) {
    final String[] val = fields.get(field);
    if (val == null || val.length == 0) {
      return new ArrayList<String>(0);
    }
    return Arrays.asList(val);
  }

  public void put(final String field, final String[] values) {
    fields.put(field, values);
  }

  public void putUrl(final String value) {
    fields.put(DEFAULT_URL_FIELD, new String[]{value});
  }

  public void putContent(final String[] value) {
    fields.put(DEFAULT_CONTENT_FIELD, value);
  }

  public void putContent(final String value) {
    String[] content = fields.get(DEFAULT_CONTENT_FIELD);
    int size = 0;

    if (content == null)
      this.putContent(new String[] { value });
    else {
      size = content.length;
      content = Arrays.copyOf(content, size + 1);
      content[size] = value;
      fields.remove(DEFAULT_CONTENT_FIELD);
      this.putContent(content);
    }
  }

  public String getFirst(final String field) {
    final List<String> values = this.get(field);
    if (values.isEmpty()) {
      return null;
    }
    else {
      return values.get(0);
    }
  }

  public String getUrl() {
    return this.getFirst(DEFAULT_URL_FIELD);
  }

  public List<String> getContent() {
    return this.get(DEFAULT_CONTENT_FIELD);
  }

  public Iterable<String> getFieldNames() {
    return fields.keySet();
  }

}

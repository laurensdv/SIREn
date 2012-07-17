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
 * @author Renaud Delbru [ 9 Jul 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.benchmark.util;

import java.io.IOException;
import java.io.StringReader;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.ntriples.NTriplesParser;

public class RdfJsonUtil {

  public static JsonNode convertNTriplesEntity(final String subject, final String ntriples)
  throws RDFParseException, RDFHandlerException, IOException {
    final ObjectMapper mapper = new ObjectMapper();
    final ObjectNode rootNode = mapper.createObjectNode();

    // parse ntriples
    final NTriplesParser parser = new NTriplesParser();
    final JSONCollector collector = new JSONCollector(mapper);
    parser.setPreserveBNodeIDs(true);
    parser.setRDFHandler(collector);
    parser.setStopAtFirstError(false);
    parser.parse(new StringReader(ntriples), "");

    // create entity pair
    rootNode.put(subject, collector.getJsonNode());

    return rootNode;
  }

  private static class JSONCollector extends RDFHandlerBase {

    final ObjectMapper mapper;
    ObjectNode rootNode;

    public JSONCollector(final ObjectMapper mapper) {
      this.mapper = mapper;
      this.rootNode = mapper.createObjectNode();
    }

    public JsonNode getJsonNode() {
      return rootNode;
    }

    @Override
    public void handleStatement(final Statement st) {
      final String predicate = st.getPredicate().stringValue();

      // add array node
      if (!rootNode.has(predicate)) {
        rootNode.put(predicate, mapper.createArrayNode());
      }

      final ArrayNode value = (ArrayNode) rootNode.get(predicate);
      value.add(st.getObject().stringValue());
    }

  }

}

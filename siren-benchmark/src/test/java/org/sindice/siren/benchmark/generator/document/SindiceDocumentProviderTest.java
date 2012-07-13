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
 * @author Renaud Delbru [ 10 Jul 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.benchmark.generator.document;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

public class SindiceDocumentProviderTest {

  @Test
  public void testReadAndParseDirectory() throws IOException {
    final File input = new File("./src/test/resources/dataset/sindice/");
    final SindiceDocumentProvider reader = new SindiceDocumentProvider(input);
    final ObjectMapper mapper = new ObjectMapper();

    // first doc contains one entity

    assertTrue(reader.hasNext());
    BenchmarkDocument doc = reader.next();
    assertNotNull(doc);
    assertNotNull(doc.getContent());
    assertEquals(1, doc.getContent().size());

    JsonNode node = mapper.readTree(doc.getContent().get(0));
    assertNotNull(node.get("http://eprints.rkbexplorer.com/id/caltech/eprints-7519"));
    assertNotNull(node.get("http://eprints.rkbexplorer.com/id/caltech/eprints-7519").get("http://www.aktors.org/ontology/portal#has-author"));

    // second doc contains two entities

    assertTrue(reader.hasNext());
    doc = reader.next();
    assertNotNull(doc);
    assertNotNull(doc.getContent());
    assertEquals(1, doc.getContent().size());

    node = mapper.readTree(doc.getContent().get(0));
    assertNotNull(node.get("http://eprints.rkbexplorer.com/id/caltech/eprints-7519"));
    assertNotNull(node.get("http://eprints.rkbexplorer.com/id/caltech/eprints-7519").get("http://www.aktors.org/ontology/portal#has-author"));

    assertNotNull(node.get("http://eprints.rkbexplorer.com/id/caltech/eprints-752"));
    assertNotNull(node.get("http://eprints.rkbexplorer.com/id/caltech/eprints-752").get("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"));

    assertFalse(reader.hasNext());

    reader.close();
  }

}

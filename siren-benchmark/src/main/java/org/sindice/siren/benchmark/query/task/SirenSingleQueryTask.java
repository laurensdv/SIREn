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
package org.sindice.siren.benchmark.query.task;

import java.io.IOException;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TotalHitCountCollector;
import org.sindice.siren.benchmark.Measurement;
import org.sindice.siren.benchmark.query.provider.Query;
import org.sindice.siren.benchmark.query.provider.SirenQueryConverter;
import org.sindice.siren.search.doc.DocumentQuery;
import org.sindice.siren.search.node.NodeQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SirenSingleQueryTask extends QueryTask {

  private final DocumentQuery query;
  private final SearcherManager mgr;

  protected final Logger logger = LoggerFactory.getLogger(SirenSingleQueryTask.class);

  public SirenSingleQueryTask(final Query query, final SearcherManager mgr)
  throws IOException {
    this.mgr = mgr;
    final SirenQueryConverter converter = new SirenQueryConverter();
    logger.debug("Received query: {}", query.toString());
    final NodeQuery nq = converter.convert(query);
    logger.debug("Converted query into: {}", nq.toString());
    this.query = new DocumentQuery(nq);
  }

  @Override
  public Measurement call() throws IOException {
    IndexSearcher searcher = this.mgr.acquire();

    try {
      final TotalHitCountCollector collector = new TotalHitCountCollector();
      searcher.search(query, collector);
      return new Measurement(collector.getTotalHits());
    }
    finally {
      this.mgr.release(searcher);
      searcher = null;
    }
  }

  @Override
  public void close() throws IOException {}

}

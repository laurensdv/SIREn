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
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TotalHitCountCollector;
import org.sindice.siren.benchmark.Measurement;
import org.sindice.siren.benchmark.query.provider.Query;
import org.sindice.siren.benchmark.query.provider.SirenQueryConverter;
import org.sindice.siren.search.doc.DocumentQuery;
import org.sindice.siren.search.node.TwigQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SirenMultiQueryTask extends QueryTask {

  private final List<DocumentQuery> queries;
  private final SearcherManager mgr;

  protected final Logger logger = LoggerFactory.getLogger(SirenMultiQueryTask.class);

  public SirenMultiQueryTask(final List<Query> queries, final SearcherManager mgr)
  throws IOException {
    this.mgr = mgr;
    this.queries = new ArrayList<DocumentQuery>(queries.size());
    final SirenQueryConverter converter = new SirenQueryConverter();
    for (final Query query : queries) {
      // logger.debug("Received query: {}", query.toString());
      final TwigQuery q = converter.convert(query);
      // logger.debug("Converted query into: {}", q.toString());
      this.queries.add(new DocumentQuery(q));
    }
  }

  @Override
  public Measurement call() throws Exception {
    IndexSearcher searcher = this.mgr.acquire();
    final TotalHitCountCollector collector = new TotalHitCountCollector();

    try {
      for (final org.apache.lucene.search.Query query : queries) {
        searcher.search(query, collector);
      }

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

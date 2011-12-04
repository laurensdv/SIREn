/**
 * Copyright (c) 2009-2011 Sindice Limited. All Rights Reserved.
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
 * @project siren
 * @author Renaud Delbru [ 21 Apr 2009 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sindice.siren.analysis.AnyURIAnalyzer;
import org.sindice.siren.analysis.TupleAnalyzer;
import org.sindice.siren.index.codecs.siren020.Siren020NodAndPosEnum;
import org.sindice.siren.search.QueryTestingHelper;

public class TestSiren020NodAndposEnum {

  protected QueryTestingHelper _helper = null;
  private final NodesConfig siren020Conf = new NodesConfig(2);
  
  @Before
  public void setUp()
  throws Exception {
    _helper = new QueryTestingHelper(new TupleAnalyzer(Version.LUCENE_40, new StandardAnalyzer(Version.LUCENE_40), new AnyURIAnalyzer(Version.LUCENE_40)));
  }

  @After
  public void tearDown()
  throws Exception {
    _helper.close();
  }

  @Test
  public void testNextSimpleOccurence1()
  throws Exception {
    _helper.addDocument("\"word1\" . ");
    final BytesRef term = new BytesRef("word1");
    final IndexReader reader = _helper.getIndexReader();
    final DocsAndPositionsEnum e = MultiFields.getTermPositionsEnum(reader, MultiFields.getLiveDocs(reader), QueryTestingHelper.DEFAULT_FIELD, term);
    final Siren020NodAndPosEnum termEnum = new Siren020NodAndPosEnum(siren020Conf, e);

    assertTrue(termEnum.nextDoc() != Siren020NodAndPosEnum.NO_MORE_DOCS);
    assertEquals(0, termEnum.docID());
    
    assertEquals(-1, termEnum.node()[0]);
    assertEquals(-1, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());

    assertEquals(1, termEnum.freq());
    assertEquals(0, termEnum.nextPosition());
    assertEquals(0, termEnum.node()[0]);
    assertEquals(0, termEnum.node()[1]);
    assertEquals(0, termEnum.pos());

    // end of the list, should return NO_MORE_POS
    assertEquals(NodAndPosEnum.NO_MORE_POS, termEnum.nextPosition());
  }

  @Test
  public void testNextSimpleOccurence2()
  throws Exception {
    _helper.addDocument("\"word1\" \"word2 word3 word4\" . ");
    final BytesRef term = new BytesRef("word3");
    final IndexReader reader = _helper.getIndexReader();
    final DocsAndPositionsEnum e = MultiFields.getTermPositionsEnum(reader, MultiFields.getLiveDocs(reader), QueryTestingHelper.DEFAULT_FIELD, term);
    final Siren020NodAndPosEnum termEnum = new Siren020NodAndPosEnum(siren020Conf, e);

    assertTrue(termEnum.nextDoc() != Siren020NodAndPosEnum.NO_MORE_DOCS);
    assertEquals(0, termEnum.docID());
    
    assertEquals(-1, termEnum.node()[0]);
    assertEquals(-1, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());

    assertEquals(1, termEnum.freq());
    // here it is assumed that the position of the term in the global position
    // in the flow of tokens (and not within a cell).
    assertEquals(2, termEnum.nextPosition());
    assertEquals(0, termEnum.node()[0]);
    assertEquals(1, termEnum.node()[1]);
    assertEquals(2, termEnum.pos());

    // end of the list, should return NO_MORE_POS
    assertEquals(NodAndPosEnum.NO_MORE_POS, termEnum.nextPosition());
  }

  @Test
  public void testNextMultipleOccurences1()
  throws Exception {
    _helper.addDocument("\"word1 word1 word1\" . ");
    final BytesRef term = new BytesRef("word1");
    final IndexReader reader = _helper.getIndexReader();
    final DocsAndPositionsEnum e = MultiFields.getTermPositionsEnum(reader, MultiFields.getLiveDocs(reader), QueryTestingHelper.DEFAULT_FIELD, term);
    final Siren020NodAndPosEnum termEnum = new Siren020NodAndPosEnum(siren020Conf, e);

    assertTrue(termEnum.nextDoc() != Siren020NodAndPosEnum.NO_MORE_DOCS);
    assertEquals(0, termEnum.docID());
    
    assertEquals(-1, termEnum.node()[0]);
    assertEquals(-1, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());

    assertEquals(3, termEnum.freq());
    assertEquals(0, termEnum.nextPosition());
    assertEquals(0, termEnum.node()[0]);
    assertEquals(0, termEnum.node()[1]);
    assertEquals(0, termEnum.pos());
    assertEquals(1, termEnum.nextPosition());
    assertEquals(0, termEnum.node()[0]);
    assertEquals(0, termEnum.node()[1]);
    assertEquals(1, termEnum.pos());
    assertEquals(2, termEnum.nextPosition());
    assertEquals(0, termEnum.node()[0]);
    assertEquals(0, termEnum.node()[1]);
    assertEquals(2, termEnum.pos());

    // end of the list, should return NO_MORE_POS
    assertEquals(NodAndPosEnum.NO_MORE_POS, termEnum.nextPosition());
  }

  @Test
  public void testNextMultipleOccurences2()
  throws Exception {
    _helper.addDocument("\"word1 word2\" \"word1\" . \"word1 word2\" . \"word1\" . ");
    final BytesRef term = new BytesRef("word1");
    final IndexReader reader = _helper.getIndexReader();
    final DocsAndPositionsEnum e = MultiFields.getTermPositionsEnum(reader, MultiFields.getLiveDocs(reader), QueryTestingHelper.DEFAULT_FIELD, term);
    final Siren020NodAndPosEnum termEnum = new Siren020NodAndPosEnum(siren020Conf, e);

    assertTrue(termEnum.nextDoc() != Siren020NodAndPosEnum.NO_MORE_DOCS);
    assertEquals(0, termEnum.docID());
    
    assertEquals(-1, termEnum.node()[0]);
    assertEquals(-1, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());

    assertEquals(4, termEnum.freq());
    assertEquals(0, termEnum.nextPosition());
    assertEquals(0, termEnum.node()[0]);
    assertEquals(0, termEnum.node()[1]);
    assertEquals(0, termEnum.pos());
    assertEquals(2, termEnum.nextPosition());
    assertEquals(0, termEnum.node()[0]);
    assertEquals(1, termEnum.node()[1]);
    assertEquals(2, termEnum.pos());
    assertEquals(3, termEnum.nextPosition());
    assertEquals(1, termEnum.node()[0]);
    assertEquals(0, termEnum.node()[1]);
    assertEquals(3, termEnum.pos());
    assertEquals(5, termEnum.nextPosition());
    assertEquals(2, termEnum.node()[0]);
    assertEquals(0, termEnum.node()[1]);
    assertEquals(5, termEnum.pos());

    // end of the list, should return NO_MORE_POS
    assertEquals(NodAndPosEnum.NO_MORE_POS, termEnum.nextPosition());
  }

  @Test
  public void testSkipTo()
  throws Exception {
    final ArrayList<String> data = new ArrayList<String>();
    for (int i = 0; i < 64; i++) {
      data.add("\"aaa aaa\" . \"aaa\" \"aaa\" .");
      data.add("\"aaa bbb\" . \"aaa ccc\" .");
    }
    _helper.addDocumentsWithIterator(data);

    final BytesRef term = new BytesRef("aaa");
    final IndexReader reader = _helper.getIndexReader();
    final DocsAndPositionsEnum e = MultiFields.getTermPositionsEnum(reader, MultiFields.getLiveDocs(reader), QueryTestingHelper.DEFAULT_FIELD, term);
    final Siren020NodAndPosEnum termEnum = new Siren020NodAndPosEnum(siren020Conf, e);
    
    assertTrue(termEnum.nextDoc() != Siren020NodAndPosEnum.NO_MORE_DOCS);
    
    termEnum.advance(16);
    assertEquals(16, termEnum.docID());
    
    assertEquals(-1, termEnum.node()[0]);
    assertEquals(-1, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());
    assertEquals(4, termEnum.freq());

    termEnum.advance(33, new int[] { 1, -1 });
    assertEquals(33, termEnum.docID());
    
    assertEquals(1, termEnum.node()[0]);
    assertEquals(0, termEnum.node()[1]);
    assertEquals(2, termEnum.pos());
    assertEquals(2, termEnum.freq());

    termEnum.advance(96, new int[] { 1, 1 });
    assertEquals(96, termEnum.docID());
    
    assertEquals(1, termEnum.node()[0]);
    assertEquals(1, termEnum.node()[1]);
    assertEquals(3, termEnum.pos());
    assertEquals(4, termEnum.freq());
  }

  /**
   * If the entity, tuple and cell are not found, it should return the first
   * match that is greater than the target. (SRN-17)
   */
  @Test
  public void testSkipToNotFound()
  throws Exception {
    final ArrayList<String> data = new ArrayList<String>();
    for (int i = 0; i < 32; i++) {
      data.add("\"aaa aaa\" . \"aaa\" \"aaa\" .");
      data.add("\"aaa bbb\" . \"aaa ccc\" . \"aaa bbb\" . \"aaa ccc\" \"aaa bbb\" . ");
    }
    _helper.addDocumentsWithIterator(data);
    
    final BytesRef term = new BytesRef("bbb");
    final IndexReader reader = _helper.getIndexReader();
    final DocsAndPositionsEnum e = MultiFields.getTermPositionsEnum(reader, MultiFields.getLiveDocs(reader), QueryTestingHelper.DEFAULT_FIELD, term);
    final Siren020NodAndPosEnum termEnum = new Siren020NodAndPosEnum(siren020Conf, e);

    // Should move to the next entity, without updating tuple and cell
    // information
    assertTrue(termEnum.advance(16) != DocsAndPositionsEnum.NO_MORE_DOCS);
    assertEquals(17, termEnum.docID());
    
    assertEquals(-1, termEnum.node()[0]);
    assertEquals(-1, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());
    assertEquals(3, termEnum.freq());

    // Should jump to the third tuples
    assertTrue(termEnum.advance(17, new int[] { 1, -1 }) != DocsAndPositionsEnum.NO_MORE_DOCS);
    assertEquals(17, termEnum.docID());
    
    assertEquals(2, termEnum.node()[0]);
    assertEquals(0, termEnum.node()[1]);
    assertEquals(5, termEnum.pos());
    assertEquals(3, termEnum.freq());

    // Should jump to the second cell
    assertTrue(termEnum.advance(17, new int[] { 3, 0 }) != DocsAndPositionsEnum.NO_MORE_DOCS);
    assertEquals(17, termEnum.docID());
    
    assertEquals(3, termEnum.node()[0]);
    assertEquals(1, termEnum.node()[1]);
    assertEquals(9, termEnum.pos());
    assertEquals(3, termEnum.freq());
  }

  @Test
  public void testSkipToAtSameEntity()
  throws Exception {
    final ArrayList<String> data = new ArrayList<String>();
    for (int i = 0; i < 64; i++) {
      data.add("\"aaa aaa\" . \"aaa\" \"aaa\" .");
      data.add("\"aaa bbb\" . \"aaa ccc\" .");
    }
    _helper.addDocumentsWithIterator(data);
    
    final BytesRef term = new BytesRef("aaa");
    final IndexReader reader = _helper.getIndexReader();
    final DocsAndPositionsEnum e = MultiFields.getTermPositionsEnum(reader, MultiFields.getLiveDocs(reader), QueryTestingHelper.DEFAULT_FIELD, term);
    final Siren020NodAndPosEnum termEnum = new Siren020NodAndPosEnum(siren020Conf, e);

    termEnum.advance(16);
    assertEquals(16, termEnum.docID());
    
    assertEquals(-1, termEnum.node()[0]);
    assertEquals(-1, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());
    assertEquals(4, termEnum.freq());

    termEnum.advance(16, new int[] { 1, -1 });
    assertEquals(16, termEnum.docID());
    
    assertEquals(1, termEnum.node()[0]);
    assertEquals(0, termEnum.node()[1]);
    assertEquals(2, termEnum.pos());
    assertEquals(4, termEnum.freq());

    termEnum.advance(16, new int[] { 1, 1 });
    assertEquals(16, termEnum.docID());
    
    assertEquals(1, termEnum.node()[0]);
    assertEquals(1, termEnum.node()[1]);
    assertEquals(3, termEnum.pos());
    assertEquals(4, termEnum.freq());
  }

  @Test
  public void testSkipToEntityNextPosition()
  throws Exception {
    final ArrayList<String> data = new ArrayList<String>();
    for (int i = 0; i < 32; i++) {
      data.add("\"aaa aaa\" . \"aaa\" \"aaa\" .");
      data.add("\"aaa bbb\" . \"aaa ccc\" .");
    }
    _helper.addDocumentsWithIterator(data);

    final BytesRef term = new BytesRef("aaa");
    final IndexReader reader = _helper.getIndexReader();
    final DocsAndPositionsEnum e = MultiFields.getTermPositionsEnum(reader, MultiFields.getLiveDocs(reader), QueryTestingHelper.DEFAULT_FIELD, term);
    final Siren020NodAndPosEnum termEnum = new Siren020NodAndPosEnum(siren020Conf, e);

    termEnum.advance(16);
    assertEquals(16, termEnum.docID());
    
    assertEquals(-1, termEnum.node()[0]);
    assertEquals(-1, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());
    assertEquals(4, termEnum.freq());

    for (int i = 0; i < termEnum.freq(); i++) {
      assertEquals(i, termEnum.nextPosition());
    }
    assertEquals(NodAndPosEnum.NO_MORE_POS, termEnum.nextPosition());
  }

  @Test
  public void testSkipToCellNextPosition()
  throws Exception {
    final ArrayList<String> data = new ArrayList<String>();
    for (int i = 0; i < 32; i++) {
      data.add("\"aaa aaa\" . \"aaa\" \"aaa\" .");
      data.add("\"aaa bbb\" . \"aaa ccc\" .");
    }
    _helper.addDocumentsWithIterator(data);
    
    final BytesRef term = new BytesRef("aaa");
    final IndexReader reader = _helper.getIndexReader();
    final DocsAndPositionsEnum e = MultiFields.getTermPositionsEnum(reader, MultiFields.getLiveDocs(reader), QueryTestingHelper.DEFAULT_FIELD, term);
    final Siren020NodAndPosEnum termEnum = new Siren020NodAndPosEnum(siren020Conf, e);

    termEnum.advance(16, new int[] { 1, 0 });
    assertEquals(16, termEnum.docID());
    
    assertEquals(1, termEnum.node()[0]);
    assertEquals(0, termEnum.node()[1]);
    assertEquals(2, termEnum.pos());
    assertEquals(4, termEnum.freq());

    assertEquals(3, termEnum.nextPosition());
    assertEquals(1, termEnum.node()[0]);
    assertEquals(1, termEnum.node()[1]);
    assertEquals(3, termEnum.pos());

    // end of the list, should return NO_MORE_POS
    assertEquals(NodAndPosEnum.NO_MORE_POS, termEnum.nextPosition());
  }

  @Test
  public void testSkipToNext()
  throws Exception {
    final ArrayList<String> data = new ArrayList<String>();
    for (int i = 0; i < 32; i++) {
      data.add("\"aaa aaa\" . \"aaa\" \"aaa\" .");
      data.add("\"aaa bbb\" . \"aaa ccc\" .");
    }
    _helper.addDocumentsWithIterator(data);
    
    final BytesRef term = new BytesRef("aaa");
    final IndexReader reader = _helper.getIndexReader();
    final DocsAndPositionsEnum e = MultiFields.getTermPositionsEnum(reader, MultiFields.getLiveDocs(reader), QueryTestingHelper.DEFAULT_FIELD, term);
    final Siren020NodAndPosEnum termEnum = new Siren020NodAndPosEnum(siren020Conf, e);

    termEnum.nextDoc();
    assertEquals(0, termEnum.docID());
    
    assertEquals(-1, termEnum.node()[0]);
    assertEquals(-1, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());
    assertEquals(4, termEnum.freq());

    termEnum.advance(16);
    assertEquals(16, termEnum.docID());
    
    assertEquals(-1, termEnum.node()[0]);
    assertEquals(-1, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());
    assertEquals(4, termEnum.freq());

    termEnum.nextDoc();
    assertEquals(17, termEnum.docID());
    
    assertEquals(-1, termEnum.node()[0]);
    assertEquals(-1, termEnum.node()[1]);
    assertEquals(-1, termEnum.pos());
    assertEquals(2, termEnum.freq());
  }

  @Test
  public void testSkipToNonExistingEntityTupleCell()
  throws Exception {
    final ArrayList<String> data = new ArrayList<String>();
    for (int i = 0; i < 16; i++) {
      data.add("\"aaa aaa\" . \"aaa\" \"aaa\" .");
      data.add("\"aaa bbb\" . \"aaa ccc\" .");
    }
    _helper.addDocumentsWithIterator(data);
    
    final BytesRef term = new BytesRef("aaa");
    final IndexReader reader = _helper.getIndexReader();
    final DocsAndPositionsEnum e = MultiFields.getTermPositionsEnum(reader, MultiFields.getLiveDocs(reader), QueryTestingHelper.DEFAULT_FIELD, term);
    final Siren020NodAndPosEnum termEnum = new Siren020NodAndPosEnum(siren020Conf, e);

    // does not exist, should skip to entity 17 and to the first cell
    assertTrue(termEnum.advance(16, new int[] { 3, 2 }) != DocsAndPositionsEnum.NO_MORE_DOCS);
    assertEquals(17, termEnum.docID());
    
    assertEquals(0, termEnum.node()[0]);
    assertEquals(0, termEnum.node()[1]);
//    assertEquals(-1, termEnum.dataset());
    assertEquals(0, termEnum.pos());

    // does not exist, should skip to entity 19 and to the first cell
    assertTrue(termEnum.advance(18, new int[] { 2, 2 }) != DocsAndPositionsEnum.NO_MORE_DOCS);
    assertEquals(19, termEnum.docID());
    
    assertEquals(0, termEnum.node()[0]);
    assertEquals(0, termEnum.node()[1]);
//    assertEquals(-1, termEnum.dataset());
    assertEquals(0, termEnum.pos());

    assertFalse(termEnum.advance(31, new int[] { 2, 0 }) != DocsAndPositionsEnum.NO_MORE_DOCS); // does not exist, reach end of list: should return false
  }

}

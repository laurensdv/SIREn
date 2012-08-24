package org.sindice.siren.qparser.tree.ntriple.query.builder;

import org.apache.lucene.util.IntsRef;
import org.sindice.siren.analysis.attributes.TupleNodeAttributeImpl;

/**
 * Emulates the first node of a tuple
 * @author Stephane Campinas [22 Aug 2012]
 * @email stephane.campinas@deri.org
 *
 */
public class MockTupleNodeAttributeImpl
extends TupleNodeAttributeImpl {

  private static final long serialVersionUID = 1L;


  @Override
  public IntsRef node() {
    clear();
    append(0);
    append(0);
    return super.node();
  }

}

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
 * @project index-beta2
 * @author Renaud Delbru [ 8 Dec 2009 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.analysis.filter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.util.FilteringTokenFilter;
import org.sindice.siren.analysis.TupleTokenizer;

/**
 * Filter out tokens with a given type.
 */
public class TokenTypeFilter extends FilteringTokenFilter {

  protected Set<String> stopTokenTypes;

  private final TypeAttribute typeAtt = this.addAttribute(TypeAttribute.class);

  public TokenTypeFilter(final TokenStream input, final int[] stopTokenTypes) {
    super(true, input);
    this.stopTokenTypes = new HashSet<String>(stopTokenTypes.length);
    for (final int type : stopTokenTypes) {
      this.stopTokenTypes.add(TupleTokenizer.getTokenTypes()[type]);
    }
  }

  @Override
  protected boolean accept() throws IOException {
    if (stopTokenTypes.contains(typeAtt.type())) {
      return false;
    }
    return true;
  }



}

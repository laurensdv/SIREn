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
 * @author Renaud Delbru [ 21 May 2011 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2010, 2011, by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.qparser.keyword.config;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.queryparser.flexible.core.config.ConfigurationKey;
import org.apache.lucene.queryparser.flexible.core.config.QueryConfigHandler;
import org.apache.lucene.queryparser.flexible.standard.config.FieldBoostMapFCListener;
import org.apache.lucene.queryparser.flexible.standard.config.FieldDateResolutionFCListener;
import org.apache.lucene.queryparser.flexible.standard.config.FuzzyConfig;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.ConfigurationKeys;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.Operator;
import org.sindice.siren.qparser.tree.TreeQueryParser.TreeConfigurationKeys;
import org.sindice.siren.search.node.MultiNodeTermQuery;

public class KeywordQueryConfigHandler
extends QueryConfigHandler {

  /**
   * Key used to set fuzzy and wildcard queries are allowed.
   */
  final public static ConfigurationKey<Boolean> ALLOW_FUZZY_AND_WILDCARD = ConfigurationKey.newInstance();

  public KeywordQueryConfigHandler() {
    // Add listener that will build the FieldConfig attributes.
    this.addFieldConfigListener(new FieldBoostMapFCListener(this));
    this.addFieldConfigListener(new FieldDateResolutionFCListener(this));

    // Default Values
    set(ConfigurationKeys.ALLOW_LEADING_WILDCARD, false); // default in 2.9
    set(ConfigurationKeys.ANALYZER, null); //default value 2.4
    set(ConfigurationKeys.PHRASE_SLOP, 0); //default value 2.4
    set(ConfigurationKeys.LOWERCASE_EXPANDED_TERMS, true); //default value 2.4
    set(ConfigurationKeys.FIELD_BOOST_MAP, new LinkedHashMap<String, Float>());
    set(ConfigurationKeys.FUZZY_CONFIG, new FuzzyConfig());
    set(ConfigurationKeys.LOCALE, Locale.getDefault());
    set(ConfigurationKeys.FIELD_DATE_RESOLUTION_MAP, new HashMap<CharSequence, DateTools.Resolution>());

    // SIREn Default Values
    this.set(ConfigurationKeys.ENABLE_POSITION_INCREMENTS, true);
    this.set(ALLOW_FUZZY_AND_WILDCARD, false);
    this.set(ConfigurationKeys.DEFAULT_OPERATOR, Operator.OR);
    this.set(TreeConfigurationKeys.MULTI_NODE_TERM_REWRITE_METHOD, MultiNodeTermQuery.CONSTANT_SCORE_AUTO_REWRITE_DEFAULT);
  }

}

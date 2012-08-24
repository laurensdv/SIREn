package org.sindice.siren.qparser.tree.query.config;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.queryparser.flexible.core.config.QueryConfigHandler;
import org.apache.lucene.queryparser.flexible.standard.config.FieldBoostMapFCListener;
import org.apache.lucene.queryparser.flexible.standard.config.FieldDateResolutionFCListener;
import org.apache.lucene.queryparser.flexible.standard.config.FuzzyConfig;
import org.apache.lucene.queryparser.flexible.standard.config.NumericFieldConfigListener;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.ConfigurationKeys;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.Operator;
import org.sindice.siren.qparser.tree.TreeQueryParser.TreeConfigurationKeys;
import org.sindice.siren.search.node.MultiNodeTermQuery;

/**
 * Copied from {@link StandardQueryConfigHandler} and adapted to SIREn
 * @author Stephane Campinas [24 Aug 2012]
 * @email stephane.campinas@deri.org
 *
 */
public class TreeQueryConfigHandler extends QueryConfigHandler {

  public TreeQueryConfigHandler() {
    // Add listener that will build the FieldConfig.
    addFieldConfigListener(new FieldBoostMapFCListener(this));
    addFieldConfigListener(new FieldDateResolutionFCListener(this));
    addFieldConfigListener(new NumericFieldConfigListener(this));
    
    // Default Values
    set(ConfigurationKeys.ALLOW_LEADING_WILDCARD, false); // default in 2.9
    set(ConfigurationKeys.ANALYZER, null); //default value 2.4
    set(ConfigurationKeys.DEFAULT_OPERATOR, Operator.OR);
    set(ConfigurationKeys.PHRASE_SLOP, 0); //default value 2.4
    set(ConfigurationKeys.LOWERCASE_EXPANDED_TERMS, true); //default value 2.4
    set(ConfigurationKeys.ENABLE_POSITION_INCREMENTS, false); //default value 2.4
    set(ConfigurationKeys.FIELD_BOOST_MAP, new LinkedHashMap<String, Float>());
    set(ConfigurationKeys.FUZZY_CONFIG, new FuzzyConfig());
    set(ConfigurationKeys.LOCALE, Locale.getDefault());
    set(ConfigurationKeys.FIELD_DATE_RESOLUTION_MAP, new HashMap<CharSequence, DateTools.Resolution>());

    // SIREn Values
    set(TreeConfigurationKeys.MULTI_NODE_TERM_REWRITE_METHOD, MultiNodeTermQuery.CONSTANT_SCORE_AUTO_REWRITE_DEFAULT);
  }

}

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.multielastic;

import java.util.Collection;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;

/**
 * What the MultiElasticSearch worker classes need to know from the parent
 * engine.
 */
public interface MultiElasticSearchContext {
    String getBaseUrl();
    
    String getIndexUrl(String index);
    
    String getDefaultIndexUrl();

    String getDefaultIndex();

    Collection<String> getSecondaryIndexes();
    
    Collection<String> getAllIndexes();
    
    SearchInputDocument createInputDocument();
}

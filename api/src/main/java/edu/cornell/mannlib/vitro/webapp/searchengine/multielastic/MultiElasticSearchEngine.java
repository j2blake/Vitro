/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.multielastic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResponse;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchQuery;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Validation;

/**
 * A first draft of a multi-index Elasticsearch implementation.
 */
public class MultiElasticSearchEngine
        implements SearchEngine, MultiElasticSearchContext {
    private static final Log log = LogFactory
            .getLog(MultiElasticSearchEngine.class);

    // ----------------------------------------------------------------------
    // Configuration
    // ----------------------------------------------------------------------

    private String baseUrl;
    private String defaultIndex;
    private List<String> secondaryIndexes = new ArrayList<>();

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasBaseUrl", minOccurs = 1, maxOccurs = 1)
    public void setBaseUrl(String url) {
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        baseUrl = url;
    }

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasDefaultIndex", minOccurs = 1, maxOccurs = 1)
    public void setDefaultIndex(String name) {
        defaultIndex = name;
    }

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasSecondaryIndex")
    public void addSecondaryIndex(String name) {
        secondaryIndexes.add(name);
    }

    @Validation
    public void validate() throws Exception {
        if (secondaryIndexes.contains(defaultIndex)) {
            throw new IllegalStateException("'" + defaultIndex + "' is defined "
                    + "as both the default index and a secondary index.");
        }
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public String getIndexUrl(String index) {
        return baseUrl + "/" + index;
    }

    @Override
    public String getDefaultIndexUrl() {
        return getIndexUrl(defaultIndex);
    }

    @Override
    public String getDefaultIndex() {
        return defaultIndex;
    }

    @Override
    public Collection<String> getSecondaryIndexes() {
        return new ArrayList<String>(secondaryIndexes);
    }

    @Override
    public Collection<String> getAllIndexes() {
        Set<String> indexes = new HashSet<>();
        indexes.add(defaultIndex);
        indexes.addAll(secondaryIndexes);
        return indexes;
    }

    // ----------------------------------------------------------------------
    // The instance
    // ----------------------------------------------------------------------

    @Override
    public void startup(Application application, ComponentStartupStatus ss) {
        /*
         * TODO
         * 
         * What should this do? I'm thinking that it should check to see whether
         * all of the indexes exist. If any does not exist, this method should
         * load a schema for that index. Where do the schemas reside? Do we
         * configure a schema directory, and use the name of the index as a
         * filename? Or do we create an ElasticIndexConfig class that gets both
         * index name and schema filename as properties?
         * 
         * It would be nice if this routine also did a smoke test.
         */
        log.warn("MultiElasticSearchEngine.startup() not implemented."); // TODO
    }

    @Override
    public void shutdown(Application application) {
        // TODO Flush the buffers?
        log.warn("MultiElasticSearchEngine.shutdown not implemented.");
    }

    @Override
    public void ping() throws SearchEngineException {
        /*
         * TODO
         * 
         * What's the simplest we can do? Ask the number of documents? Is it
         * enough to ask one index, or do we need to confirm that all of them
         * respond?
         */
        log.warn("MultiElasticSearchEngine.ping() not implemented."); // TODO
    }

    @Override
    public SearchInputDocument createInputDocument() {
        return new BaseSearchInputDocument();
    }

    @Override
    public void add(SearchInputDocument... docs) throws SearchEngineException {
        add(Arrays.asList(docs));
    }

    @Override
    public void add(Collection<SearchInputDocument> docs)
            throws SearchEngineException {
        new MESAdder(this).add(docs);
    }

    @Override
    public void commit() throws SearchEngineException {
        new MESFlusher(this).flush();
    }

    @Override
    public void commit(boolean wait) throws SearchEngineException {
        new MESFlusher(this).flush(wait);
    }

    @Override
    public void deleteById(String... ids) throws SearchEngineException {
        deleteById(Arrays.asList(ids));
    }

    @Override
    public void deleteById(Collection<String> ids)
            throws SearchEngineException {
        new MESDeleter(this).deleteByIds(ids);
    }

    @Override
    public void deleteByQuery(String query) throws SearchEngineException {
        new MESDeleter(this).deleteByQuery(query);
    }

    @Override
    public SearchQuery createQuery() {
        return new BaseSearchQuery();
    }

    @Override
    public SearchQuery createQuery(String queryText) {
        BaseSearchQuery query = new BaseSearchQuery();
        query.setQuery(queryText);
        return query;
    }

    @Override
    public SearchResponse query(SearchQuery query)
            throws SearchEngineException {
        return new MESQuery(this).query(query);
    }

    @Override
    public int documentCount() throws SearchEngineException {
        return new MESCounter(this).count();
    }
}

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.multielastic;

import java.net.URLEncoder;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;

/**
 * The nuts and bolts of adding a document to the Elasticsearch indexes.
 * 
 * Break the input document into separate docs, based on the field prefixes. Add
 * each document to the corresponding index.
 */
public class MESAdder {
    private static final Log log = LogFactory.getLog(MESAdder.class);

    private final MultiElasticSearchContext parent;

    public MESAdder(MultiElasticSearchContext parent) {
        this.parent = parent;
    }

    public void add(Collection<SearchInputDocument> docs)
            throws SearchEngineException {
        for (SearchInputDocument doc : docs) {
            addDocument(doc);
        }
    }

    private void addDocument(SearchInputDocument doc)
            throws SearchEngineException {
        Map<String, SearchInputDocument> docs = new DocumentSplitter(parent)
                .split(doc);
        for (Entry<String, SearchInputDocument> entry : docs.entrySet()) {
            addDocumentToIndex(entry.getKey(), entry.getValue());
        }
    }

    private void addDocumentToIndex(String index, SearchInputDocument doc)
            throws SearchEngineException {
        try {
            String docId = (String) doc.getField("DocId").getFirstValue();
            String json = new DocumentMarshaller(doc).asJson();

            log.debug("Adding document to '" + index + "' for '" + docId + "'");
            putToElastic(index, json, docId);
        } catch (Exception e) {
            throw new SearchEngineException(
                    "Failed to add document to '" + index + "'", e);
        }
    }

    private void putToElastic(String index, String json, String docId)
            throws SearchEngineException {
        try {
            log.debug("putting to ElasticSearch: index='" + index + "', json='"
                    + json + "'");
            String url = parent.getIndexUrl(index) + "/_doc/"
                    + URLEncoder.encode(docId, "UTF8");
            Response response = Request.Put(url)
                    .bodyString(json, ContentType.APPLICATION_JSON).execute();
            log.debug("Response from Elasticsearch: "
                    + response.returnContent().asString());
        } catch (Exception e) {
            throw new SearchEngineException("Failed to put to Elasticsearch",
                    e);
        }
    }
}

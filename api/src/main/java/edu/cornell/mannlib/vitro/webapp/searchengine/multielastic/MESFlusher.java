/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.multielastic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;

/**
 * Just does a "commit" or "flush" to the index.
 */
public class MESFlusher {
    private static final Log log = LogFactory.getLog(MESFlusher.class);

    private final MultiElasticSearchContext parent;

    public MESFlusher(MultiElasticSearchContext parent) {
        this.parent = parent;
    }

    public void flush() throws SearchEngineException {
        flush(false);
    }

    public void flush(boolean wait) throws SearchEngineException {
        for (String index : parent.getAllIndexes()) {
            flush(index, wait);
        }
    }

    private void flush(String index, boolean wait)
            throws SearchEngineException {
        try {
            String url = parent.getIndexUrl(index) + "/_flush"
                    + (wait ? "?wait_for_ongoing" : "");
            Response response = Request.Get(url).execute();
            String json = response.returnContent().asString();
            log.debug("flush response: " + json);
        } catch (Exception e) {
            throw new SearchEngineException("Failed to put to Elasticsearch",
                    e);
        }
    }

}

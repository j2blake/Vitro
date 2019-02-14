/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.transience;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResponse;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResultDocument;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchQuery;
import edu.cornell.mannlib.vitro.webapp.searchengine.transience.index.TransientIndex;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

/**
 * A memory-based implementation that forgets everything on shutdown.
 */
public class TransientSearchEngine implements SearchEngine {
	private static final Log log = LogFactory
			.getLog(TransientSearchEngine.class);

	private List<String> textFields = Collections.emptyList();

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasTextFields", minOccurs = 0, maxOccurs = 1)
	public void setTextFields(String names) {
		textFields = Arrays.asList(names.split("[\\s,]+"));
	}

	private TransientIndex index = new TransientIndex();

	@Override
	public void startup(Application application, ComponentStartupStatus ss) {
		index = new TransientIndex(textFields);
	}

	@Override
	public void shutdown(Application application) {
		// nothing to shut down.
	}

	@Override
	public void ping() throws SearchEngineException {
		// never throws an exception.
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
		for (SearchInputDocument doc : docs) {
			index.add((BaseSearchInputDocument) doc);
		}
	}

	@Override
	public void commit() throws SearchEngineException {
		// nothing to commit.
	}

	@Override
	public void commit(boolean wait) throws SearchEngineException {
		// nothing to commit.
	}

	@Override
	public void deleteById(String... ids) throws SearchEngineException {
		deleteById(Arrays.asList(ids));
	}

	@Override
	public void deleteById(Collection<String> ids)
			throws SearchEngineException {
		for (String id : ids) {
			index.deleteById(id);
		}
	}

	@Override
	public void deleteByQuery(String queryText) throws SearchEngineException {
		try {
			SearchResponse resp = query(createQuery(queryText));
			long howMany = resp.getResults().size();

			int sizeBefore = index.size();
			for (SearchResultDocument doc : resp.getResults()) {
				index.deleteById(doc.getUniqueId());
			}
			int sizeAfter = index.size();
			log.debug(String.format(
					"DeleteByQuery: query='%s', before=%d, deleted=%d, after=%d",
					queryText, sizeBefore, howMany, sizeAfter));
		} catch (Exception e) {
			// If this occurs, the indexing thread will not report it.
			// Report it here.
			log.error("EXCEPTION", e);
			throw e;
		}
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
		return index.processQuery(query);
	}

	@Override
	public int documentCount() throws SearchEngineException {
		return index.size();
	}

}

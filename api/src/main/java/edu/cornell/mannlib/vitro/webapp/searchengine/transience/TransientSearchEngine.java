/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.transience;

import java.util.Arrays;
import java.util.Collection;

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

/**
 * A memory-based implementation that forgets everything on shutdown.
 */
public class TransientSearchEngine implements SearchEngine {
	private final TransientIndex index = new TransientIndex();

	@Override
	public void startup(Application application, ComponentStartupStatus ss) {
		// nothing to start up.
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
		SearchQuery q = createQuery(queryText);
		SearchResponse resp = query(q);
		for (SearchResultDocument doc : resp.getResults()) {
			index.deleteById(doc.getUniqueId());
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

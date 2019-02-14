package edu.cornell.mannlib.vitro.webapp.searchengine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;

import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResponse;
import edu.cornell.mannlib.vitro.webapp.searchengine.transience.utils.LuceneUtils;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class SearchQueryParsingLogger implements SearchEngine {
	private static final Log log = LogFactory
			.getLog(SearchQueryParsingLogger.class);

	// ----------------------------------------------------------------------
	// Configuration
	// ----------------------------------------------------------------------

	private SearchEngine innerEngine;
	private List<String> textFields = Collections.emptyList();

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#wraps", minOccurs = 1, maxOccurs = 1)
	public void setInnerEngine(SearchEngine inner) {
		innerEngine = inner;
	}

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasTextFields", minOccurs = 0, maxOccurs = 1)
	public void setTextFields(String names) {
		textFields = Arrays.asList(names.split("[\\s,]+"));
	}

	// ----------------------------------------------------------------------
	// Running
	// ----------------------------------------------------------------------

	@Override
	public void startup(Application application, ComponentStartupStatus ss) {
		innerEngine.startup(application, ss);
	}

	@Override
	public void shutdown(Application application) {
		innerEngine.shutdown(application);
	}

	@Override
	public void ping() throws SearchEngineException {
		innerEngine.ping();
	}

	@Override
	public SearchInputDocument createInputDocument() {
		return innerEngine.createInputDocument();
	}

	@Override
	public void add(SearchInputDocument... docs) throws SearchEngineException {
		innerEngine.add(docs);
	}

	@Override
	public void add(Collection<SearchInputDocument> docs)
			throws SearchEngineException {
		innerEngine.add(docs);
	}

	@Override
	public void commit() throws SearchEngineException {
		innerEngine.commit();
	}

	@Override
	public void commit(boolean wait) throws SearchEngineException {
		innerEngine.commit(wait);
	}

	@Override
	public void deleteById(String... ids) throws SearchEngineException {
		innerEngine.deleteById(ids);
	}

	@Override
	public void deleteById(Collection<String> ids)
			throws SearchEngineException {
		innerEngine.deleteById(ids);
	}

	@Override
	public void deleteByQuery(String query) throws SearchEngineException {
		log.info("deleteByQuery: " + query);
		innerEngine.deleteByQuery(query);
	}

	@Override
	public SearchQuery createQuery() {
		return innerEngine.createQuery();
	}

	@Override
	public SearchQuery createQuery(String queryText) {
		return innerEngine.createQuery(queryText);
	}

	@Override
	public SearchResponse query(SearchQuery query)
			throws SearchEngineException {
		parseAndLogQuery(query);
		return innerEngine.query(query);
	}

	@Override
	public int documentCount() throws SearchEngineException {
		return innerEngine.documentCount();
	}

	/**
	 * <pre>
	 * Note: VIVO submits queries like this one: "classgroup:http://vivoweb.org/someUri",
	 * which is not syntactically correct by basic Lucene rules. Solr is kind enough to translate this
	 * into "classgroup://vivoweb.org/someUri http://vivoweb.org/someUri", which is not what was 
	 * intended but appears to be working. In order to mimic this, we need to use a 
	 * org.apache.lucene.queryparser.surround.parser.QueryParser to do the initial parsing, 
	 * and then translate to a standard Query to process it.
	 * 
	 * Reference:
	 * https://stackoverflow.com/questions/32700560/lucene-query-object-and-search
	 * </pre>
	 */
	private void parseAndLogQuery(SearchQuery query) {
		log.info("Parsing: " + formatSearchQuery(query));
		try {
			String queryString = query.getQuery();
			queryString = queryString.replace("http://", "http\\://");
			queryString = queryString.replace("/", "\\/");

			QueryParser parser = new MultiFieldQueryParser(
					textFields.toArray(new String[0]),
					new WhitespaceAnalyzer());
			Query luceneQuery = parser.parse(queryString);
			log.info("Parsed: " + LuceneUtils.formatLuceneQuery(luceneQuery));
		} catch (Exception e) {
			log.info("Failed to parse Query", e);
		}
	}

	private String formatSearchQuery(SearchQuery q) {
		List<String> terms = new ArrayList<>();
		if (q.getQuery().trim().length() > 0) {
			terms.add("queryText=" + q.getQuery());
		}
		if (q.getStart() != 0) {
			terms.add("start=" + q.getStart());
		}
		if (q.getRows() != -1) {
			terms.add("rows=" + q.getRows());
		}
		if (q.getFieldsToReturn().size() > 0) {
			terms.add("fieldsToReturn=" + q.getFieldsToReturn());
		}
		if (q.getSortFields().size() > 0) {
			terms.add("sortFields=" + q.getSortFields());
		}
		if (q.getFilters().size() > 0) {
			terms.add("filters=" + q.getFilters());
		}
		if (q.getFacetFields().size() > 0) {
			terms.add("facetFields=" + q.getFacetFields());
		}
		if (q.getFacetLimit() != 100) {
			terms.add("facetLimit=" + q.getFacetLimit());
		}
		if (q.getFacetMinCount() != 0) {
			terms.add("facetMinCount=" + q.getFacetMinCount());
		}
		return "BaseSearchQuery" + terms;
	}

}

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.transience.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchFacetField;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResponse;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResultDocumentList;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchResponse;
import edu.cornell.mannlib.vitro.webapp.searchengine.transience.query.TransientQuery;
import edu.cornell.mannlib.vitro.webapp.searchengine.transience.response.TransientSearchResultDocumentList;

/**
 * The memory-based map of search documents, with the ability to add, delete,
 * and process queries.
 */
public class TransientIndex {
	private static final Log log = LogFactory.getLog(TransientIndex.class);

	private List<String> textFields;
	private final SortedMap<String, TransientIndexDocument> map = new TreeMap<>();

	public TransientIndex() {
		this(Collections.emptyList());
	}
	
	public TransientIndex(List<String> textFields) {
		this.textFields = textFields;
	}

	public void add(BaseSearchInputDocument doc) {
		TransientIndexDocument indexDoc = new TransientIndexDocument(doc, textFields);
		map.put(indexDoc.getId(), indexDoc);
	}

	public int size() {
		return map.size();
	}

	public void deleteById(String id) {
		map.remove(id);
	}

	public SearchResponse processQuery(SearchQuery query) {
		TransientQuery tQuery = TransientQuery.parse(query, textFields);
		
		List<TransientIndexDocument> reducedList = tQuery
				.process(mapToDocList());

		return new BaseSearchResponse( //
				figureHighlights(reducedList), //
				figureFacets(reducedList, query), //
				figureFinalResults(reducedList, query));
	}

	private List<TransientIndexDocument> mapToDocList() {
		return new ArrayList<TransientIndexDocument>(map.values());
	}

	private Map<String, Map<String, List<String>>> figureHighlights(
			List<TransientIndexDocument> docs) {
		return new TransientIndexHighlighter(docs).distill();
	}

	private Map<String, SearchFacetField> figureFacets(
			List<TransientIndexDocument> docs, SearchQuery query) {
		return new TransientIndexFaceter(docs, query).distill();
	}

	private SearchResultDocumentList figureFinalResults(
			List<TransientIndexDocument> docs, SearchQuery query) {
		return new TransientSearchResultDocumentList(docs, query);
	}

}

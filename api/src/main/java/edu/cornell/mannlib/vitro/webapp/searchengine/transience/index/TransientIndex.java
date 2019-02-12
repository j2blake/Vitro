/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.transience.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchFacetField;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchFacetField.Count;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResponse;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResultDocumentList;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchFacetField;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchResponse;
import edu.cornell.mannlib.vitro.webapp.searchengine.transience.query.TransientQuery;
import edu.cornell.mannlib.vitro.webapp.searchengine.transience.response.TransientSearchResultDocumentList;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchFacetField.BaseCount;

/**
 * The memory-based map of search documents, with the ability to add, delete,
 * and process queries.
 */
public class TransientIndex {
	private static final Log log = LogFactory.getLog(TransientIndex.class);

	private final SortedMap<String, TransientIndexDocument> map = new TreeMap<>();

	public void add(BaseSearchInputDocument doc) {
		TransientIndexDocument indexDoc = new TransientIndexDocument(doc);
		map.put(indexDoc.getId(), indexDoc);
	}

	public int size() {
		return map.size();
	}

	public void deleteById(String id) {
		map.remove(id);
	}

	public SearchResponse processQuery(SearchQuery query) {
		TransientQuery tQuery = TransientQuery.parse(query);
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

	/**
	 * Need to (optionally) create the highlight array from the final list of
	 * documents? Highlights must be determined by the individual query
	 * processors, since the match area is dependent on the type of query.
	 * 
	 * Find out what the highlight array looks like. Query on "nations", and
	 * display the search results.
	 * 
	 * An example of the highlights:
	 * 
	 * <pre>
	 * Map<String, Map<String, List<String>>>
	 * 
	 * {
	 *   "vitroIndividual:http://aims.fao.org/aos/geopolitical.owl#Serbia"=
	 *     {
	 *       "ALLTEXT"=["former <strong>Hungary</strong> FAO_2006"]
	 *     }
	 * }
	 * </pre>
	 * 
	 * So, docID (all response docs) => fieldName (which fields?) => list of
	 * snippets, with embedded "strong" tags.
	 * 
	 * Don't know whether Solr trims the length. VIVO takes the first snippet
	 * that is associated with ALLTEXT surrounds it with ellipses, and uses it.
	 */
	private Map<String, Map<String, List<String>>> figureHighlights(
			List<TransientIndexDocument> docs) {
		// TODO Auto-generated method stub
		log.warn("TransientIndex.figureHighlights() not implemented.");
		return new HashMap<>();
	}

	private Map<String, SearchFacetField> figureFacets(
			List<TransientIndexDocument> docs, SearchQuery query) {
		Map<String, SearchFacetField> facets = new HashMap<>();
		for (String facetField : query.getFacetFields()) {
			facets.put(facetField, figureFacetField(facetField, docs));
		}
		return facets;
	}

	private SearchFacetField figureFacetField(String fieldName,
			List<TransientIndexDocument> docs) {
		CountingMap counts = new CountingMap();
		for (TransientIndexDocument doc : docs) {
			for (Object value : doc.getValues(fieldName)) {
				counts.add(String.valueOf(value));
			}
		}
		return new BaseSearchFacetField(fieldName, counts.toList());
	}

	private SearchResultDocumentList figureFinalResults(
			List<TransientIndexDocument> docs, SearchQuery query) {
		return new TransientSearchResultDocumentList(docs, query);
	}

	private static class CountingMap {
		private final Map<String, Integer> map = new HashMap<>();

		public void add(String value) {
			if (map.containsKey(value)) {
				map.put(value, map.get(value) + 1);
			} else {
				map.put(value, 1);
			}
		}

		public List<Count> toList() {
			List<Count> list = new ArrayList<>();
			for (Entry<String, Integer> entry : map.entrySet()) {
				list.add(new BaseCount(entry.getKey(), entry.getValue()));
			}
			return list;
		}
	}

}

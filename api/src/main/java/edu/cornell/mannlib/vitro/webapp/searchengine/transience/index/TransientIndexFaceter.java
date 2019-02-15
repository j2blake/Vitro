/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.transience.index;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchFacetField;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchFacetField.Count;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchFacetField;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchFacetField.BaseCount;

/**
 * For each facet field, look through the result documents and find out how many
 * docs have each value. Produce a list of value counts for each field.
 * 
 * For each such list, remove any whose counts fall below the minimum specified
 * by the query.
 * 
 * Then, truncate each list to the length specified in the query.
 */
public class TransientIndexFaceter {

	private final List<TransientIndexDocument> docs;
	private final SearchQuery query;

	public TransientIndexFaceter(List<TransientIndexDocument> docs,
			SearchQuery query) {
		this.docs = docs;
		this.query = query;
	}

	public Map<String, SearchFacetField> distill() {
		Map<String, SearchFacetField> facets = new HashMap<>();
		for (String facetField : query.getFacetFields()) {
			facets.put(facetField, distillField(facetField));
		}
		return facets;
	}

	private SearchFacetField distillField(String fieldName) {
		CountingMap counts = new CountingMap();
		for (TransientIndexDocument doc : docs) {
			for (Object value : doc.getValues(fieldName)) {
				counts.add(String.valueOf(value));
			}
		}
		List<Count> countsList = limitTheLength(
				removeInsignificantCounts(counts.toList()));
		return new BaseSearchFacetField(fieldName, countsList);
	}

	private List<Count> removeInsignificantCounts(List<Count> list) {
		int minimumCount = query.getFacetMinCount();
		List<Count> pruned = new ArrayList<>();
		for (Count count : list) {
			if (count.getCount() >= minimumCount) {
				pruned.add(count);
			}
		}
		return pruned;
	}

	private List<Count> limitTheLength(List<Count> list) {
		int limit = query.getFacetLimit();
		List<Count> limited = new ArrayList<>(list);
		if (limit < 0) {
			return limited;
		} else if (limited.size() <= limit) {
			return limited;
		} else {
			return limited.subList(0, limit);
		}
	}

	/**
	 * Keep tallies of the facet field values, and create a sorted list on
	 * request.
	 */
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
			list.sort(new BaseCountComparator());
			return list;
		}

		/** Sort by count descending, then by name ascending. */
		private static class BaseCountComparator implements Comparator<Count> {
			@Override
			public int compare(Count c1, Count c2) {
				if (c1.getCount() < c2.getCount()) {
					return 1;
				} else if (c1.getCount() > c2.getCount()) {
					return -1;
				} else {
					return c1.getName().compareTo(c2.getName());
				}
			}

		}
	}

}

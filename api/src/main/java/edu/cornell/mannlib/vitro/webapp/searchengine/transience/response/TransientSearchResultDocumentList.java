/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.transience.response;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery.Order;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResultDocument;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResultDocumentList;
import edu.cornell.mannlib.vitro.webapp.searchengine.transience.index.TransientIndexDocument;

/**
 * The documents that are returned from our query -- surprisingly, there is no
 * base class for this.
 */
public class TransientSearchResultDocumentList
		implements SearchResultDocumentList {
	private static final Log log = LogFactory
			.getLog(TransientSearchResultDocumentList.class);

	private final long numFound;
	private final List<SearchResultDocument> docList;

	/**
	 * Maybe we pass around a list of tree entries, taking subsets,
	 * intersections, and unions while preserving ordering.
	 * 
	 * Sort, start, rows, and return fields are all applied to the final result.
	 * 
	 * The resulting documents must be in a repeatable order, so when we ask for
	 * docs 10 through 50, we know what to expect.
	 */
	public TransientSearchResultDocumentList(List<TransientIndexDocument> docs,
			SearchQuery query) {
		// log.warn("RESULT LIST: " + query + ", " + docs);
		this.numFound = docs.size();

		docs = new ArrayList<>(docs);
		docs = sortDocs(docs, query);
		docs = trimToStart(docs, query);
		docs = limitRows(docs, query);
		this.docList = transformDocs(docs, query);
	}

	private List<TransientIndexDocument> sortDocs(
			List<TransientIndexDocument> docs, SearchQuery query) {
		Map<String, Order> sortFields = query.getSortFields();
		for (Entry<String, Order> sortEntry : sortFields.entrySet()) {
			docs.sort(new TransientIndexDocumentComparator(sortEntry.getKey(),
					sortEntry.getValue()));
		}
		return docs;
	}

	private List<TransientIndexDocument> trimToStart(
			List<TransientIndexDocument> docs, SearchQuery query) {
		int start = query.getStart();
		if (start >= docs.size()) {
			return new ArrayList<>();
		} else {
			return docs.subList(start, docs.size());
		}
	}

	private List<TransientIndexDocument> limitRows(
			List<TransientIndexDocument> docs, SearchQuery query) {
		int limit = query.getRows();
		if (limit < 0) {
			return docs;
		}
		if (limit >= docs.size()) {
			return docs;
		}
		return docs.subList(0, limit);
	}

	private List<SearchResultDocument> transformDocs(
			List<TransientIndexDocument> docs, SearchQuery query) {
		List<SearchResultDocument> results = new ArrayList<>();
		for (TransientIndexDocument doc : docs) {
			results.add(doc.toSearchResultDocument(query.getFieldsToReturn()));
		}
		return results;
	}

	@Override
	public Iterator<SearchResultDocument> iterator() {
		return this.docList.iterator();
	}

	@Override
	public long getNumFound() {
		return this.numFound;
	}

	@Override
	public int size() {
		return this.docList.size();
	}

	@Override
	public SearchResultDocument get(int i) {
		return this.docList.get(i);
	}

	private static class TransientIndexDocumentComparator
			implements Comparator<TransientIndexDocument> {
		private final String fieldName;
		private final Order order;

		public TransientIndexDocumentComparator(String fieldName, Order order) {
			this.fieldName = fieldName;
			this.order = order;
		}

		@Override
		public int compare(TransientIndexDocument doc1,
				TransientIndexDocument doc2) {
			String value1 = doc1.getSortValue(fieldName);
			String value2 = doc2.getSortValue(fieldName);
			if (order == Order.ASC) {
				return value1.compareTo(value2);
			} else {
				return value2.compareTo(value1);
			}
		}
	}
}

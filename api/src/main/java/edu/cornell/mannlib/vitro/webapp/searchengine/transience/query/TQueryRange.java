/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.transience.query;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.util.BytesRef;

import edu.cornell.mannlib.vitro.webapp.searchengine.transience.index.TransientIndexDocument;

/**
 * Query: get the documents with a field value that falls between two bounds.
 */
public class TQueryRange extends TransientQuery {
	private final String fieldName;
	private final String lowerTerm;
	private final String upperTerm;
	private final boolean includesLower;
	private final boolean includesUpper;

	public TQueryRange(TermRangeQuery trQuery) {
		this.fieldName = trQuery.getField();
		this.lowerTerm = convertToString(trQuery.getLowerTerm());
		this.upperTerm = convertToString(trQuery.getUpperTerm());
		this.includesLower = trQuery.includesLower();
		this.includesUpper = trQuery.includesUpper();
	}

	private String convertToString(BytesRef term) {
		return (term == null) ? "" : term.utf8ToString();
	}

	@Override
	public List<TransientIndexDocument> process(
			List<TransientIndexDocument> list) {
		List<TransientIndexDocument> results = new ArrayList<>();
		for (TransientIndexDocument doc : list) {
			for (Object value : doc.getValues(fieldName)) {
				if (meetsTheRange(String.valueOf(value))) {
					results.add(doc);
					break;
				}
			}
		}
		return results;
	}

	private boolean meetsTheRange(String value) {
		return meetsTheLower(value) && meetsTheUpper(value);
	}

	private boolean meetsTheLower(String value) {
		int compare = value.compareTo(lowerTerm);
		return compare > 0 || (includesLower && compare >= 0);
	}

	private boolean meetsTheUpper(String value) {
		int compare = value.compareTo(upperTerm);
		return compare < 0 || (includesUpper && compare <= 0);
	}

	@Override
	public String toString() {
		return String.format(
				"TQueryRange[fieldName=%s, lowerTerm=%s, upperTerm=%s, includesLower=%s, includesUpper=%s]",
				fieldName, lowerTerm, upperTerm, includesLower, includesUpper);
	}

}

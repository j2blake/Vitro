/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.transience.query;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;

import edu.cornell.mannlib.vitro.webapp.searchengine.transience.index.TransientIndexDocument;

/**
 * Query: does a term in a field equal the specified value?
 */
public class TTermQuery extends TransientQuery {
	private static final Log log = LogFactory.getLog(TTermQuery.class);

	private final String field;
	private final String text;

	public TTermQuery(TermQuery luceneQuery) {
		Term term = luceneQuery.getTerm();
		this.field = term.field();
		this.text = term.text().trim().toLowerCase();
	}

	@Override
	public List<TransientIndexDocument> process(
			List<TransientIndexDocument> list) {
		List<TransientIndexDocument> results = new ArrayList<>();
		for (TransientIndexDocument doc : list) {
			for (String term : doc.getLowerCaseTerms(field)) {
				if (term.equals(text)) {
					results.add(doc);
					break;
				}
			}
		}
		return results;
	}

	@Override
	public String toString() {
		return String.format("TTermQuery[field=%s, text=%s]", field, text);
	}

}

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.transience.query;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.Query;

import edu.cornell.mannlib.vitro.webapp.searchengine.transience.index.TransientIndexDocument;

/**
 * A placeholder that says the user submitted a type of query that we weren't
 * prepared for,
 * 
 * Also used in case of a parsing exception,
 */
public class TQueryNotImplemented extends TransientQuery {
	private static final Log log = LogFactory
			.getLog(TQueryNotImplemented.class);

	public TQueryNotImplemented(String qString, Query luceneQuery) {
		log.warn("QUERY NOT IMPLEMENTED for '" + qString + "', parsed to: "
				+ formatLuceneQuery(luceneQuery));
	}

	private String formatLuceneQuery(Query luceneQuery) {
		return String.format("%s: %s", luceneQuery.getClass(), luceneQuery);
	}

	@Override
	public List<TransientIndexDocument> process(
			List<TransientIndexDocument> list) {
		return new ArrayList<>();
	}

	@Override
	public String toString() {
		return String.format("TQueryNotImplemented[]");
	}

}

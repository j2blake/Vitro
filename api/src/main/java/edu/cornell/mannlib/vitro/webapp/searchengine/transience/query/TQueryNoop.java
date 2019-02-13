/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.transience.query;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.queryparser.classic.ParseException;

import edu.cornell.mannlib.vitro.webapp.searchengine.transience.index.TransientIndexDocument;

/**
 * If we failed to parse a query, don't throw a horrible exception; use this
 * query instead. It logs a warning and returns no documents.
 */
public class TQueryNoop extends TransientQuery {
	private static final Log log = LogFactory.getLog(TQueryNoop.class);

	public TQueryNoop(String qString, ParseException e) {
		log.warn("Failed to parse the query string: '" + qString
				+ "', failure is " + e.toString());
	}

	@Override
	public List<TransientIndexDocument> process(
			List<TransientIndexDocument> list) {
		return new ArrayList<>();
	}
}

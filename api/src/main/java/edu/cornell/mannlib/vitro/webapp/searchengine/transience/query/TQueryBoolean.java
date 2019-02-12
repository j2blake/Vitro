/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.transience.query;

import java.util.List;

import org.apache.lucene.search.BooleanClause.Occur;

import edu.cornell.mannlib.vitro.webapp.searchengine.transience.index.TransientIndexDocument;

/**
 * TODO
 */
public class TQueryBoolean extends TransientQuery {

	/**
	 * @param result
	 * @param must
	 * @param parse
	 */
	public TQueryBoolean(TransientQuery result, Occur must,
			TransientQuery parse) {
		// TODO Auto-generated constructor stub
		throw new RuntimeException("TQueryBoolean Constructor not implemented.");
	}

	/* (non-Javadoc)
	 * @see edu.cornell.mannlib.vitro.webapp.searchengine.transience.query.TransientQuery#process(java.util.List)
	 */
	@Override
	public List<TransientIndexDocument> process(
			List<TransientIndexDocument> list) {
		// TODO Auto-generated method stub
		throw new RuntimeException("QueryBoolean.process() not implemented.");

	}

	@Override
	public String toString() {
		return String.format("TQueryBoolean[]");
	}

}

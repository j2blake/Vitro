/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.transience.query;

import static edu.cornell.mannlib.vitro.webapp.searchengine.transience.query.TQueryBoolean.Occur.FILTER;
import static edu.cornell.mannlib.vitro.webapp.searchengine.transience.query.TQueryBoolean.Occur.SHOULD;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.searchengine.transience.index.TransientIndexDocument;
import edu.cornell.mannlib.vitro.webapp.searchengine.transience.query.TQueryBoolean.Clause;
import edu.cornell.mannlib.vitro.webapp.searchengine.transience.utils.LuceneUtils;

/**
 * A base class for the query objects that will simulate the Lucene queries
 * against our simulated index.
 */
public abstract class TransientQuery {
	private static final Log log = LogFactory.getLog(TransientQuery.class);

	/**
	 * Build a tree of TransientQuery objects for the query and any filters.
	 * 
	 * Pass the list of textFields to the QueryParser, so it can tokenize
	 * properly.
	 */
	public static TransientQuery parse(SearchQuery query,
			List<String> textFields) {
		QueryParser parser = createParser(textFields);

		TransientQuery tq = parse(cleanQueryString(query.getQuery()), parser);

		if (query.getFilters().size() > 0) {
			tq = combineWithFilters(tq, query, parser);
		}

		log.warn("Query: " + tq);
		return tq;
	}

	private static QueryParser createParser(List<String> textFields) {
		return new MultiFieldQueryParser(textFields.toArray(new String[0]),
				new WhitespaceAnalyzer());
	}

	private static TransientQuery combineWithFilters(TransientQuery tq,
			SearchQuery query, QueryParser parser) {
		List<Clause> clauses = new ArrayList<>();
		clauses.add(new Clause(tq, SHOULD));

		for (String fString : query.getFilters()) {
			TransientQuery tQuery = parse(cleanQueryString(fString), parser);
			clauses.add(new Clause(tQuery, FILTER));
		}

		return new TQueryBoolean(clauses);
	}

	/**
	 * Run the query string through Lucene's parser, and translate the resulting
	 * Lucene Query objects into their equivalent TransientQuery objects.
	 */
	private static TransientQuery parse(String qString, QueryParser parser) {
		try {
			Query luceneQuery = parser.parse(qString);
			return LuceneUtils.convertToTransientQuery(luceneQuery, qString);
		} catch (ParseException e) {
			return new TQueryNoop(qString, e);
		}
	}

	/**
	 * Lucene considers slashes as delimiters to regular expressions, unless
	 * they are escaped.
	 * 
	 * Lucene considers "fieldName:http://this/that" to be equivalent to the
	 * boolean query "fieldName://this/that && http://this/that", unless the
	 * second colon is escaped.
	 * 
	 * This does not cover all possible transgressions and might be over-zealous
	 * in some cases. The proper solution would be to issue correct queries,
	 * with any necessary escaping done by the client code.
	 */
	private static String cleanQueryString(String dirty) {
		String q = dirty.replace("http://", "http\\://");
		return q.replace("/", "\\/");
	}

	public abstract List<TransientIndexDocument> process(
			List<TransientIndexDocument> list);

	@Override
	public String toString() {
		return String.format("TransientQuery[]");
	}

}

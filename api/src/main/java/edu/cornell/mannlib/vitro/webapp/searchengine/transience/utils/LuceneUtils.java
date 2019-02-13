/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.transience.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.util.BytesRef;

import edu.cornell.mannlib.vitro.webapp.searchengine.transience.query.TQueryBoolean;
import edu.cornell.mannlib.vitro.webapp.searchengine.transience.query.TQueryNotImplemented;
import edu.cornell.mannlib.vitro.webapp.searchengine.transience.query.TQueryRange;
import edu.cornell.mannlib.vitro.webapp.searchengine.transience.query.TQueryTerm;
import edu.cornell.mannlib.vitro.webapp.searchengine.transience.query.TransientQuery;
import edu.cornell.mannlib.vitro.webapp.searchengine.transience.query.TQueryBoolean.Clause;
import edu.cornell.mannlib.vitro.webapp.searchengine.transience.query.TQueryBoolean.Occur;

/**
 * Some utility methods for dealing with Lucene.
 */
public final class LuceneUtils {
	private static final Log log = LogFactory.getLog(LuceneUtils.class);

	private LuceneUtils() {
		// No need to instantiate this. All methods are static.
	}

	/**
	 * Display the salient features of this luceneQuery, going recursive if
	 * necessary.
	 */
	public static String formatLuceneQuery(Query luceneQuery) {
		return new LuceneQueryFormatter(luceneQuery).toString();
	}

	private static class LuceneQueryFormatter {
		private final String formatted;

		public LuceneQueryFormatter(Query q) {
			this.formatted = formatLuceneQuery(q);
		}

		private String formatLuceneQuery(Query q) {
			if (q instanceof BooleanQuery) {
				return formatBooleanQuery((BooleanQuery) q);
			} else if (q instanceof TermQuery) {
				return formatTermQuery((TermQuery) q);
			} else if (q instanceof PhraseQuery) {
				return formatPhraseQuery((PhraseQuery) q);
			} else if (q instanceof WildcardQuery) {
				return formatWildcardQuery((WildcardQuery) q);
			} else if (q instanceof PrefixQuery) {
				return formatPrefixQuery((PrefixQuery) q);
			} else if (q instanceof TermRangeQuery) {
				return formatTermRangeQuery((TermRangeQuery) q);
			} else
				return q.getClass().getName() + ": " + String.valueOf(q);
		}

		private String formatBooleanQuery(BooleanQuery q) {
			List<String> clauses = new ArrayList<>();
			for (BooleanClause c : q.clauses()) {
				clauses.add(c.getOccur().name() + " "
						+ formatLuceneQuery(c.getQuery()));
			}
			return "BooleanQuery" + clauses;
		}

		private String formatTermQuery(TermQuery q) {
			Term t = q.getTerm();
			return "TermQuery[field=" + t.field() + ", text=" + t.text() + "]";
		}

		private String formatPhraseQuery(PhraseQuery q) {
			List<String> terms = new ArrayList<>();
			for (Term t : q.getTerms()) {
				terms.add("[field=" + t.field() + ", text=" + t.text() + "]");
			}
			return "PhraseQuery[terms=" + terms + ", positions="
					+ Arrays.toString(q.getPositions()) + ", slop="
					+ q.getSlop() + "]";
		}

		private String formatWildcardQuery(WildcardQuery q) {
			Term t = q.getTerm();
			return "WildcardQuery[field=" + t.field() + ", text=" + t.text()
					+ "]";
		}

		private String formatPrefixQuery(PrefixQuery q) {
			Term t = q.getPrefix();
			return "PrefixQuery[field=" + t.field() + ", text=" + t.text()
					+ "]";
		}

		private String formatTermRangeQuery(TermRangeQuery q) {
			return String.format(
					"TermRangeQuery[field=%s, lowerTerm='%s', includesLower=%s, upperTerm='%s', includesUpper=%s]",
					q.getField(), TermConverter.convert(q.getLowerTerm()),
					q.includesLower(), TermConverter.convert(q.getUpperTerm()),
					q.includesUpper());

		}

		@Override
		public String toString() {
			return formatted;
		}

		private static class TermConverter {
			static String convert(BytesRef term) {
				return term == null ? "" : term.utf8ToString();
			}
		}
	}

	/**
	 * Convert this lucene query to a TransientQuery, recursing as needed.
	 * 
	 * If a lucene query cannot be converted, create a TQueryNotImplemented with
	 * the qString as an informative message in the log.
	 */
	public static TransientQuery convertToTransientQuery(Query luceneQuery,
			String qString) {
		TransientQuery tQuery = new QueryConverter(qString)
				.convert(luceneQuery);
		log.info(String.format("Converted '%s' to %s",
				formatLuceneQuery(luceneQuery), tQuery));
		return tQuery;
	}

	private static class QueryConverter {
		private final String qString;

		public QueryConverter(String qString) {
			this.qString = qString;
		}

		public TransientQuery convert(Query lQuery) {
			if (lQuery instanceof TermQuery) {
				return new TQueryTerm((TermQuery) lQuery);
			} else if (lQuery instanceof TermRangeQuery) {
				return new TQueryRange((TermRangeQuery) lQuery);
			} else if (lQuery instanceof BooleanQuery) {
				return new TQueryBoolean(convertClauses((BooleanQuery) lQuery));
			} else {
				return new TQueryNotImplemented(qString, lQuery);
			}
		}

		private List<Clause> convertClauses(BooleanQuery lQuery) {
			List<Clause> clauses = new ArrayList<>();
			for (BooleanClause c : lQuery.clauses()) {
				clauses.add(new TQueryBoolean.Clause(convert(c.getQuery()),
						toOccur(c.getOccur())));
			}
			return clauses;
		}

		private Occur toOccur(BooleanClause.Occur o) {
			switch (o) {
			case FILTER:
				return Occur.FILTER;
			case MUST:
				return Occur.MUST;
			case MUST_NOT:
				return Occur.MUST_NOT;
			default:
				return Occur.SHOULD;
			}
		}
	}

}

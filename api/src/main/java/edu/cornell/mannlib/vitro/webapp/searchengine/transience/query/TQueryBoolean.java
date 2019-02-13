/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.transience.query;

import static edu.cornell.mannlib.vitro.webapp.searchengine.transience.query.TQueryBoolean.Occur.FILTER;
import static edu.cornell.mannlib.vitro.webapp.searchengine.transience.query.TQueryBoolean.Occur.MUST;
import static edu.cornell.mannlib.vitro.webapp.searchengine.transience.query.TQueryBoolean.Occur.MUST_NOT;
import static edu.cornell.mannlib.vitro.webapp.searchengine.transience.query.TQueryBoolean.Occur.SHOULD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.searchengine.transience.index.TransientIndexDocument;

/**
 * Query: apply intersections and unions to a set of queries.
 * 
 * Each document in the results must: pass all MUST and FILTER clauses, fail all
 * MUST_NOT clauses, and pass at least one SHOULD clause.
 * 
 * In Lucene, MUST and FILTER are the same except that MUST clauses participate
 * in the scoring. We aren't scoring, so no difference.
 */
public class TQueryBoolean extends TransientQuery {
	private final List<Clause> clauses;

	public TQueryBoolean(Clause... clauses) {
		this(Arrays.asList(clauses));
	}

	public TQueryBoolean(Collection<Clause> clauses) {
		this.clauses = new ArrayList<>(clauses);
	}

	@Override
	public List<TransientIndexDocument> process(
			List<TransientIndexDocument> list) {
		return processShouldClauses(
				processMustNotClauses(processMustAndFilterClauses(list)));
	}

	/** Include only those that pass all MUST and FILTER clauses. */
	private List<TransientIndexDocument> processMustAndFilterClauses(
			List<TransientIndexDocument> list) {
		List<TransientIndexDocument> reduced = new ArrayList<>(list);
		for (Clause clause : clauses) {
			if (clause.getOccur() == MUST || clause.getOccur() == FILTER) {
				reduced = clause.getTQuery().process(reduced);
			}
		}
		return reduced;
	}

	/** Remove all those that pass any MUST_NOT clause. */
	private List<TransientIndexDocument> processMustNotClauses(
			List<TransientIndexDocument> list) {
		List<TransientIndexDocument> reduced = new ArrayList<>(list);
		for (Clause clause : clauses) {
			if (clause.getOccur() == MUST_NOT) {
				reduced.removeAll(clause.getTQuery().process(reduced));
			}
		}
		return reduced;
	}

	/** Include all those that pass any SHOULD clause. */
	private List<TransientIndexDocument> processShouldClauses(
			List<TransientIndexDocument> list) {
		List<TransientIndexDocument> results = new ArrayList<>();
		for (Clause clause : clauses) {
			if (clause.getOccur() == SHOULD) {
				results.addAll(clause.getTQuery().process(list));
			}
		}
		return results;
	}

	@Override
	public String toString() {
		return String.format("TQueryBoolean[clauses=%s]", clauses);
	}

	public static class Clause {
		private final TransientQuery tQuery;
		private final Occur occur;

		public Clause(TransientQuery tQuery, Occur occur) {
			this.tQuery = tQuery;
			this.occur = occur;
		}

		public TransientQuery getTQuery() {
			return tQuery;
		}

		public Occur getOccur() {
			return occur;
		}

		@Override
		public String toString() {
			return String.format("Clause[tQuery=%s, occur=%s]", tQuery, occur);
		}

	}

	public static enum Occur {
		MUST, FILTER, SHOULD, MUST_NOT
	}
}

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.transience.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputField;

/**
 * Keep in mind: In a SearchInputField, each "value" may actually be an array of
 * values, or a collection of values. Store them here as separate objects.
 */
public class TransientIndexField {

	private List<Object> values;
	private List<String> lowerCaseTerms;
	private String sortValue;

	public TransientIndexField(SearchInputField field) {
		this.values = figureValues(field.getValues());
		this.lowerCaseTerms = figureLowerCaseTerms();
		this.sortValue = figureSortValue();
	}

	private List<Object> figureValues(Collection<Object> vals) {
		List<Object> list = new ArrayList<>();
		for (Object v : vals) {
			if (v.getClass().isArray()) {
				list.addAll(Arrays.asList((Object[]) v));
			} else if (v instanceof Collection) {
				list.addAll((Collection<?>) v);
			} else {
				list.add(v);
			}
		}
		return list;
	}

	private List<String> figureLowerCaseTerms() {
		List<String> lcTerms = new ArrayList<>();
		for (Object value : values) {
			String cleaned = String.valueOf(value).trim().toLowerCase();
			String[] split = cleaned.split("\\s+");
			for (String term : split) {
				if (term.length() > 0) {
					lcTerms.add(term);
				}
			}
		}
		return lcTerms;
	}

	private String figureSortValue() {
		StringBuilder builder = new StringBuilder();
		for (Object value : values) {
			builder.append(String.valueOf(value)).append(" ");
		}
		return builder.toString();
	}

	public String getSortValue() {
		return sortValue;
	}

	public Collection<Object> getValues() {
		return new ArrayList<>(values);
	}

	public List<String> getLowerCaseTerms() {
		return new ArrayList<>(lowerCaseTerms);
	}

	@Override
	public String toString() {
		return String.format(
				"TransientIndexField[values=%s, lowerCaseTerms=%s, sortValue=%s]",
				values, lowerCaseTerms, sortValue);
	}

}

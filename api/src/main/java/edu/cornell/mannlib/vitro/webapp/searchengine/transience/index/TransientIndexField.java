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
 * 
 * Do we parse the values into smaller terms or not? Let the sub-classes decide.
 */
public abstract class TransientIndexField {

	private List<Object> values;
	private List<String> lowerCaseTerms;
	private String sortValue;

	public TransientIndexField(SearchInputField field) {
		this.values = figureValues(field.getValues());
		this.lowerCaseTerms = figureLowerCaseTerms(this.values);
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

	protected abstract List<String> figureLowerCaseTerms(List<Object> values);

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

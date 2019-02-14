/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.transience.index;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputField;

/**
 * In a text field, values are broken into tokens which contain only
 * word-characters. They are also converted to lower-case.
 */
public class TransientIndexTextField extends TransientIndexField {

	public TransientIndexTextField(SearchInputField field) {
		super(field);
	}

	@Override
	protected List<String> figureLowerCaseTerms(List<Object> values) {
		List<String> lcTerms = new ArrayList<>();
		for (Object value : values) {
			String cleaned = String.valueOf(value).trim().toLowerCase();
			String[] split = cleaned.split("[\\s\\W]+");
			for (String term : split) {
				if (term.length() > 0) {
					lcTerms.add(term);
				}
			}
		}
		return lcTerms;
	}
}

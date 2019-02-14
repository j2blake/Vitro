/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.transience.index;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputField;

/**
 * In a token field, values are not broken up into smaller terms, although they
 * are converted to lower-case.
 */
public class TransientIndexTokenField extends TransientIndexField {

	public TransientIndexTokenField(SearchInputField field) {
		super(field);
	}

	@Override
	protected List<String> figureLowerCaseTerms(List<Object> values) {
		List<String> lcTerms = new ArrayList<>();
		for (Object value : values) {
			lcTerms.add(String.valueOf(value).toLowerCase());
		}
		return lcTerms;
	}
}

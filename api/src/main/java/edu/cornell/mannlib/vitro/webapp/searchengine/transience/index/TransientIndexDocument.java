/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.transience.index;

import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.DOCID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResultDocument;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchResultDocument;

/**
 * Holds all of the info required for our mock index to do a search.
 */
public class TransientIndexDocument {
	private final String id;
	private final Map<String, TransientIndexField> fieldMap;

	public TransientIndexDocument(BaseSearchInputDocument doc) {
		this.id = uniqueId(doc);
		this.fieldMap = new HashMap<>();
		for (String name : doc.getFieldMap().keySet()) {
			fieldMap.put(name, new TransientIndexField(doc.getField(name)));
		}
	}

	private String uniqueId(SearchInputDocument doc) {
		return String.valueOf(doc.getField(DOCID).getFirstValue());
	}

	public String getId() {
		return id;
	}

	public Collection<Object> getValues(String fieldName) {
		TransientIndexField field = fieldMap.get(fieldName);
		if (field == null) {
			return new ArrayList<>();
		} else {
			return field.getValues();
		}
	}

	/**
	 * The concatenated lowercase values for this field. May return an empty
	 * string, but never returns null.
	 */
	public String getSortValue(String fieldName) {
		TransientIndexField field = fieldMap.get(fieldName);
		if (field == null) {
			return "";
		} else {
			return field.getSortValue();
		}
	}

	public List<String> getLowerCaseTerms(String fieldName) {
		TransientIndexField field = fieldMap.get(fieldName);
		if (field == null) {
			return new ArrayList<>();
		} else {
			return field.getLowerCaseTerms();
		}
	}

	public SearchResultDocument toSearchResultDocument(
			Set<String> fieldsToReturn) {
		if (fieldsToReturn.isEmpty()) {
			fieldsToReturn = fieldMap.keySet();
		}

		Map<String, Collection<Object>> fields = new HashMap<>();
		for (String name : fieldsToReturn) {
			if (fieldMap.containsKey(name)) {
				fields.put(name, fieldMap.get(name).getValues());
			}
		}
		return new BaseSearchResultDocument(id, fields);
	}

	@Override
	public String toString() {
		return String.format("TransientIndexDocument[id=%s, fieldMap=%s]", id,
				fieldMap);
	}

}

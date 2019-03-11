/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.multielastic;

import java.util.HashMap;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputField;

/**
 * Take a single SearchInputDocument which may have fields destined for
 * different indexes, and return a map of SearchInputDocuments, each for a named
 * index.
 * 
 * So, if the input document contains fields names "a1", "index2:b1", and
 * "index2:b2", and if the parent context specifies a default index of "index1",
 * then the result would be a map of two documents. with "index1" mapping to a
 * document with one field, named "a1" and "index2" mapping to a document with
 * fields names "b1" and "b2".
 * 
 * A special case: each input document must have a "DocID" field, and that field
 * will be included in all of the documents produced by this split.
 * 
 * Perhaps this code is more fastidious than necessary, but it ensures that each
 * new SearchInputDocument is created by the parent context, and that each new
 * SearchInputField is created by its parent document, as specified in the
 * interfaces.
 */
public class DocumentSplitter {
    private final MultiElasticSearchContext parent;

    public DocumentSplitter(MultiElasticSearchContext parent) {
        this.parent = parent;
    }

    public Map<String, SearchInputDocument> split(SearchInputDocument inDoc)
            throws SearchEngineException {
        DocumentMap docMap = new DocumentMap(parent, inDoc.getField("DocId"));
        for (SearchInputField field : inDoc.getFieldMap().values()) {
            docMap.addField(field);
        }
        return docMap.toMap();
    }

    private class DocumentMap {
        private final Map<String, SearchInputDocument> map;
        private final MultiElasticSearchContext parent;
        private final SearchInputField idField;

        public DocumentMap(MultiElasticSearchContext parent,
                SearchInputField idField) {
            this.map = new HashMap<>();
            this.parent = parent;
            this.idField = idField;
        }

        public void addField(SearchInputField inField)
                throws SearchEngineException {
            SearchInputDocument doc = findDoc(indexName(inField));
            doc.addField(unqualifyField(doc, inField));
        }

        private SearchInputDocument findDoc(String indexName) {
            if (!map.containsKey(indexName)) {
                SearchInputDocument doc = parent.createInputDocument();
                doc.addField(unqualifyField(doc, idField));
                map.put(indexName, doc);
            }
            return map.get(indexName);
        }

        private SearchInputField unqualifyField(SearchInputDocument doc,
                SearchInputField inField) {
            SearchInputField outField = doc.createField(fieldName(inField));
            outField.addValues(inField.getValues());
            return outField;
        }

        private String indexName(SearchInputField field)
                throws SearchEngineException {
            String indexName;
            String fullName = field.getName();
            String[] parts = fullName.split(":");

            if (parts.length == 1) {
                indexName = parent.getDefaultIndex();
            } else if (parts.length == 2) {
                indexName = parts[0];
            } else {
                throw new SearchEngineException("The field name '" + fullName
                        + "' is invalid. Too many colons.");
            }

            if (parent.getAllIndexes().contains(indexName)) {
                return indexName;
            } else {
                throw new SearchEngineException("The index '" + indexName
                        + "' is not configured: field name is '" + fullName
                        + "'");
            }
        }

        private String fieldName(SearchInputField field) {
            String[] parts = field.getName().split(":");
            if (parts.length == 2) {
                return parts[1];
            } else {
                return parts[0];
            }
        }

        public Map<String, SearchInputDocument> toMap() {
            return map;
        }
    }
}

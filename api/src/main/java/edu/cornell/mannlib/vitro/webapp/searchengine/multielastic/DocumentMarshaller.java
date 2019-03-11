/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.multielastic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputField;

/**
 * Creates a JSON string from a SearchInputDocument
 */
public class DocumentMarshaller {
    private static final Log log = LogFactory.getLog(DocumentMarshaller.class);

    private final SearchInputDocument doc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DocumentMarshaller(SearchInputDocument doc) {
        this.doc = doc;
    }

    public DocumentMarshaller setPrettyPrint() {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return this;
    }

    public String asJson() throws SearchEngineException {
        try {
            String json = objectMapper.writeValueAsString(docToMap());

            if (log.isDebugEnabled()) {
                log.debug("converted document: '" + doc + "' to json '" + json
                        + "'");
            }

            return json;
        } catch (JsonProcessingException e) {
            throw new SearchEngineException(
                    "Failed to convert '" + doc + "' to JSON", e);
        }
    }

    private Map<String, List<Object>> docToMap() {
        NestedMap nestedMap = new NestedMap();
        for (SearchInputField field : doc.getFieldMap().values()) {
            nestedMap.addField(field.getName(),
                    valuesToList(field.getValues()));
        }
        return nestedMap.asMap();
    }

    /**
     * Some field values are collections. Add the members of the collection
     * instead.
     */
    private ArrayList<Object> valuesToList(Collection<Object> values) {
        ArrayList<Object> list = new ArrayList<>();
        for (Object value : values) {
            if (value instanceof Collection) {
                Collection<?> cValue = (Collection<?>) value;
                list.addAll(cValue);
            } else {
                list.add(value);
            }
        }
        return list;
    }

    /**
     * Translates the names and values into a map of lists.
     * 
     * Where the names have multiple parts (delimited by '.'), creates nested
     * maps within the lists.
     */
    private static class NestedMap {
        private final Map<String, List<Object>> map = new HashMap<>();

        public void addField(String name, List<Object> values) {
            List<String> parts = Arrays.asList(name.split("\\."));
            storeValues(map, parts, values);
        }

        private void storeValues(Map<String, List<Object>> target,
                List<String> parts, List<Object> values) {
            String part = parts.get(0);
            List<String> remaining = dropOne(parts);

            if (remaining.isEmpty()) {
                target.put(part, values);
            } else {
                storeValues(locateMap(target, part), remaining, values);
            }
        }

        private List<String> dropOne(List<String> list) {
            return list.subList(1, list.size());
        }

        /**
         * Find the map located at this key. If there is nothing there, create a
         * map with an empty list.
         */
        @SuppressWarnings("unchecked")
        private Map<String, List<Object>> locateMap(
                Map<String, List<Object>> parent, String key) {
            if (!parent.containsKey(key)) {
                ArrayList<Object> newList = new ArrayList<Object>();
                newList.add(new HashMap<String, List<Object>>());
                parent.put(key, newList);
            }
            return (Map<String, List<Object>>) parent.get(key).get(0);
        }

        public Map<String, List<Object>> asMap() {
            return map;
        }
    }
}

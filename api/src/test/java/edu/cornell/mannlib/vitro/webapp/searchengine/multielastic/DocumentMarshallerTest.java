/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.multielastic;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchInputField;

/**
 * TODO
 */
public class DocumentMarshallerTest {
    private static final SearchInputDocument FLAT_DOC = createFlatDoc();
    private static final String FLAT_JSON = "{\n" //
            + "  \"DocId\" : [ \"http://flat.doc\" ],\n"
            + "  \"label\" : [ \"The flat document\" ],\n"
            + "  \"text\" : [ \"a\", \"bunch\", \"of\", \"text\" ]\n" //
            + "}";

    private static SearchInputDocument createFlatDoc() {
        BaseSearchInputField idField = new BaseSearchInputField("DocId");
        idField.addValues("http://flat.doc");

        BaseSearchInputField labelField = new BaseSearchInputField("label");
        labelField.addValues("The flat document");

        BaseSearchInputField textField = new BaseSearchInputField("text");
        textField.addValues( // add some collections
                Arrays.asList(new String[] { "a", "bunch" }),
                Arrays.asList(new String[] { "of", "text" }));

        BaseSearchInputDocument doc = new BaseSearchInputDocument();
        doc.addField(idField);
        doc.addField(labelField);
        doc.addField(textField);
        return doc;
    }

    private static final SearchInputDocument NESTED_DOC = createNestedDoc();
    private static final String NESTED_JSON = "{\n" //
            + "  \"DocId\" : [ \"http://nested.doc\" ],\n" //
            + "  \"label\" : [ \"The nested document\" ],\n" //
            + "  \"text\" : [ \"a\", \"bunch\", \"of\", \"text\" ],\n" //
            + "  \"nested\" : [ {\n" //
            + "    \"one\" : [ \"nested first\" ],\n" //
            + "    \"three\" : [ {\n" //
            + "      \"deep\" : [ \"nested third\" ]\n" //
            + "    } ],\n" //
            + "    \"two\" : [ \"nested second\" ]\n" //
            + "  } ]\n" //
            + "}";

    private static SearchInputDocument createNestedDoc() {
        BaseSearchInputField idField = new BaseSearchInputField("DocId");
        idField.addValues("http://nested.doc");

        BaseSearchInputField labelField = new BaseSearchInputField("label");
        labelField.addValues("The nested document");

        BaseSearchInputField textField = new BaseSearchInputField("text");
        textField.addValues( // add some collections
                Arrays.asList(new String[] { "a", "bunch" }),
                Arrays.asList(new String[] { "of", "text" }));

        BaseSearchInputField n1Field = new BaseSearchInputField("nested.one");
        n1Field.addValues("nested first");

        BaseSearchInputField n2Field = new BaseSearchInputField("nested.two");
        n2Field.addValues("nested second");

        BaseSearchInputField n3Field = new BaseSearchInputField(
                "nested.three.deep");
        n3Field.addValues("nested third");

        BaseSearchInputDocument doc = new BaseSearchInputDocument();
        doc.addField(idField);
        doc.addField(labelField);
        doc.addField(textField);
        doc.addField(n1Field);
        doc.addField(n2Field);
        doc.addField(n3Field);
        return doc;
    }

    @Test
    public void marshallFlatDocument() throws SearchEngineException {
        assertEquals(FLAT_JSON,
                new DocumentMarshaller(FLAT_DOC).setPrettyPrint().asJson());
    }

    @Test
    public void marshallNestedDocument() throws SearchEngineException {
        assertEquals(NESTED_JSON,
                new DocumentMarshaller(NESTED_DOC).setPrettyPrint().asJson());
    }
}

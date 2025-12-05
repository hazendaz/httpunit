/*
 * MIT License
 *
 * Copyright 2011-2025 Russell Gold
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package com.meterware.httpunit.dom;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.w3c.dom.html.HTMLBodyElement;
import org.w3c.dom.html.HTMLFormElement;
import org.w3c.dom.html.HTMLInputElement;
import org.w3c.dom.html.HTMLOptionElement;
import org.w3c.dom.html.HTMLSelectElement;

class HTMLFormSubmitTest extends AbstractHTMLElementTest {

    private HTMLFormElement _form;

    @BeforeEach
    void setUp() throws Exception {
        TestWindowProxy windowProxy = new TestWindowProxy(_htmlDocument);
        windowProxy.setUrl(new URL("http://localhost/aux.html"));

        _htmlDocument.getWindow().setProxy(windowProxy);
        HTMLBodyElement body = (HTMLBodyElement) _htmlDocument.createElement("body");
        _htmlDocument.appendChild(body);

        _form = (HTMLFormElement) createElement("form", new String[][] { { "action", "go_here" } });
        body.appendChild(_form);
        _form.setMethod("GET");
        _form.setAction("tryMe");
    }

    /**
     * Verifies that submitting a simple form works.
     */
    @Test
    void submitFromForm() throws Exception {
        addInput("text", "name").setValue("master");
        addInput("checkbox", "second").setChecked(true);
        _form.submit();
        assertEquals("submitRequest( GET, http://localhost/tryMe?name=master&second=on, null, null )",
                TestWindowProxy.popProxyCall(), "Expected response");
    }

    /**
     * Verifies that submitting a simple form from a button selects that button only.
     */
    @Test
    void submitFromButton() throws Exception {
        addInput("text", "name", "master");
        addInput("checkbox", "second").setChecked(true);
        addInput("submit", "save", "none");
        HTMLInputElementImpl button = (HTMLInputElementImpl) addInput("submit", "save", "all");
        button.doClickAction();
        assertEquals("submitRequest( GET, http://localhost/tryMe?name=master&second=on&save=all, null, null )",
                TestWindowProxy.popProxyCall(), "Expected response");
    }

    /**
     * Verifies that characters in parameter names will be appropriately encoded.
     */
    @Test
    void embeddedEquals() throws Exception {
        addInput("text", "age=x", "12");
        _form.submit();
        assertEquals("submitRequest( GET, http://localhost/tryMe?age%3Dx=12, null, null )",
                TestWindowProxy.popProxyCall(), "Expected response");
    }

    /**
     * Verifies that an empty "select" element does not transmit any parameter values.
     */
    @Test
    void emptyChoiceSubmit() throws Exception {
        addInput("text", "age", "12");
        addSelect("empty");
        _form.submit();
        assertEquals("submitRequest( GET, http://localhost/tryMe?age=12, null, null )", TestWindowProxy.popProxyCall(),
                "Expected response");
    }

    /**
     * Verifies that a select will send a value taken from the "value" attribute.
     */
    @Test
    void submitUsingSelectOptionAttributes() throws Exception {
        addInput("text", "age", "12");
        HTMLSelectElement select = addSelect("color");
        addOption(select, "red", null);
        addOption(select, "blue", "azure").setAttribute("selected", "selected");
        addOption(select, "green", null);
        _form.submit();
        assertEquals("submitRequest( GET, http://localhost/tryMe?age=12&color=blue, null, null )",
                TestWindowProxy.popProxyCall(), "Expected response");
    }

    /**
     * Verifies that a select will send a value taken from the text nodes following the option tags.
     */
    @Test
    void submitUsingSelectOptionLabels() throws Exception {
        addInput("text", "age", "12");
        HTMLSelectElement select = addSelect("color");
        select.setMultiple(true);
        select.setSize(2);
        addOption(select, null, "red");
        addOption(select, null, "blue").setAttribute("selected", "selected");
        addOption(select, null, "green").setAttribute("selected", "selected");
        _form.submit();
        assertEquals("submitRequest( GET, http://localhost/tryMe?age=12&color=blue&color=green, null, null )",
                TestWindowProxy.popProxyCall(), "Expected response");
    }

    /**
     * Verifies that a radio button will send its value on submit.
     */
    @Test
    void submitRadioButtons() throws Exception {
        addInput("radio", "color", "red").setChecked(true);
        addInput("radio", "color", "blue").setChecked(true);
        addInput("radio", "color", "green");
        _form.submit();
        assertEquals("submitRequest( GET, http://localhost/tryMe?color=blue, null, null )",
                TestWindowProxy.popProxyCall(), "Expected response");
    }

    /**
     * Verifies that checkboxes will send their values on submit.
     */
    @Test
    void submitCheckboxes() throws Exception {
        addInput("checkbox", "color", "red").setChecked(true);
        addInput("checkbox", "color", "blue").setChecked(true);
        addInput("checkbox", "color", "green");
        _form.submit();
        assertEquals("submitRequest( GET, http://localhost/tryMe?color=red&color=blue, null, null )",
                TestWindowProxy.popProxyCall(), "Expected response");
    }

    /**
     * Verifies that forms with the POST method send their data in the message body.
     */
    @Test
    @Disabled
    void submitUsingPost() throws Exception {
        _form.setMethod("POST");
        addInput("checkbox", "color", "red").setChecked(true);
        addInput("checkbox", "color", "blue").setChecked(true);
        addInput("checkbox", "color", "green");
        _form.submit();
        assertEquals("submitRequest( POST, http://localhost/tryMe, null, color=red&color=blue )",
                TestWindowProxy.popProxyCall(), "Expected response");
    }

    private HTMLSelectElement addSelect(String name) {
        HTMLSelectElement select = (HTMLSelectElement) _htmlDocument.createElement("select");
        _form.appendChild(select);
        select.setName(name);
        return select;
    }

    private HTMLOptionElement addOption(HTMLSelectElement select, String value, String label) {
        HTMLOptionElement option = (HTMLOptionElement) _htmlDocument.createElement("option");
        select.appendChild(option);
        if (value != null) {
            option.setValue(value);
        }
        if (label != null) {
            select.appendChild(_htmlDocument.createTextNode(label));
        }
        return option;
    }

    private HTMLInputElement addInput(String type, String name) {
        HTMLInputElement element = (HTMLInputElement) _htmlDocument.createElement("input");
        element.setAttribute("type", type);
        element.setAttribute("name", name);
        _form.appendChild(element);
        return element;
    }

    private HTMLInputElement addInput(String type, String name, String value) {
        HTMLInputElement element = addInput(type, name);
        element.setValue(value);
        return element;
    }

}

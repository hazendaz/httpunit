/*
 * MIT License
 *
 * Copyright 2011-2023 Russell Gold
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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mozilla.javascript.ScriptableObject;
import org.w3c.dom.Element;
import org.w3c.dom.html.*;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 */
class HTMLFormTest extends AbstractHTMLElementTest {

    private HTMLFormElement _form;
    private HTMLInputElement _textField, _passwordField, _defaultField, _hiddenField;
    private HTMLInputElement _radio1[], _radio2[], _checkbox[];
    private HTMLInputElement _submitInput, _resetInput, _buttonInput;
    private HTMLTextAreaElement _textArea;
    private Element _body;

    @BeforeEach
    void setUp() throws Exception {
        _form = (HTMLFormElement) createElement("form", new String[][] { { "action", "go_here" } });
        _body = _htmlDocument.createElement("body");
        _htmlDocument.appendChild(_body);
        _body.appendChild(_form);

        _textField = (HTMLInputElement) createElement("input",
                new String[][] { { "name", "text" }, { "type", "text" }, { "value", "initial" } });
        _passwordField = (HTMLInputElement) createElement("input",
                new String[][] { { "id", "password" }, { "type", "password" } });
        _hiddenField = (HTMLInputElement) createElement("input",
                new String[][] { { "id", "hidden" }, { "type", "hidden" }, { "value", "saved" } });
        _defaultField = (HTMLInputElement) createElement("input",
                new String[][] { { "name", "default" }, { "value", "zero" } });
        _submitInput = (HTMLInputElement) createElement("input",
                new String[][] { { "name", "submit" }, { "type", "submit" }, { "value", "go" } });
        _buttonInput = (HTMLInputElement) createElement("input",
                new String[][] { { "name", "button" }, { "type", "button" }, { "value", "go" } });
        _resetInput = (HTMLInputElement) createElement("input",
                new String[][] { { "name", "reset" }, { "type", "reset" }, { "value", "clear" } });
        _textArea = (HTMLTextAreaElement) createElement("textarea", new String[][] { { "name", "area" } });

        _radio1 = new HTMLInputElement[3];
        for (int i = 0; i < _radio1.length; i++) {
            _radio1[i] = (HTMLInputElement) createElement("input",
                    new String[][] { { "name", "radio" }, { "type", "radio" }, { "value", "channel" + (i + 1) } });
        }
        _radio1[1].setAttribute("checked", "true");

        _radio2 = new HTMLInputElement[4];
        for (int i = 0; i < _radio2.length; i++) {
            _radio2[i] = (HTMLInputElement) createElement("input",
                    new String[][] { { "name", "radio3" }, { "type", "radio" }, { "value", "color" + (i + 1) } });
        }
        _radio2[3].setAttribute("checked", "true");

        _checkbox = new HTMLInputElement[2];
        for (int i = 0; i < _checkbox.length; i++) {
            _checkbox[i] = (HTMLInputElement) createElement("input",
                    new String[][] { { "name", "checkbox" }, { "type", "checkbox" }, { "value", "on" + (i + 1) } });
        }
        _checkbox[1].setAttribute("checked", "true");
    }

    @Test
    void testTextValues() throws Exception {
        _form.appendChild(_textField);
        _form.appendChild(_passwordField);
        _form.appendChild(_hiddenField);
        _form.appendChild(_defaultField);

        verifyTextField(_textField, "initial");
        verifyTextField(_passwordField, null);
        verifyTextField(_defaultField, "zero");
        verifyTextField(_hiddenField, "saved");
    }

    @Test
    void testTextArea() throws Exception {
        _form.appendChild(_textArea);
        _textArea.appendChild(_htmlDocument.createTextNode("something here to see"));
        assertEquals("something here to see", _textArea.getValue(), "Initial value");
        assertEquals("something here to see", _textArea.getDefaultValue(), "Initial default value");

        _textArea.setValue("what it is now");
        assertEquals("what it is now", _textArea.getValue(), "value after change");
        assertEquals("something here to see", _textArea.getDefaultValue(), "default value after change");

        ((HTMLControl) _textArea).reset();
        assertEquals("something here to see", _textArea.getValue(), "Reset value");
        assertEquals("something here to see", _textArea.getDefaultValue(), "Reset default value");
    }

    private void verifyTextField(HTMLInputElement textField, String initialValue) {
        assertSame(_form, textField.getForm(), "Form for control");
        assertEquals(initialValue, textField.getValue(), "Initial value");
        assertEquals(initialValue, textField.getDefaultValue(), "Initial default value");

        textField.setValue("changed");
        assertEquals("changed", textField.getValue(), "value after change");
        assertEquals(initialValue, textField.getDefaultValue(), "default value after change");

        ((HTMLControl) textField).reset();
        assertEquals(initialValue, textField.getValue(), "Reset value");
        assertEquals(initialValue, textField.getDefaultValue(), "Reset default value");
    }

    @Test
    void testDefaults() throws Exception {
        _textField.setDefaultValue("green");
        assertEquals("green", _textField.getDefaultValue(), "default text value");
        _checkbox[0].setDefaultChecked(true);
        assertEquals(true, _checkbox[0].getDefaultChecked(), "default checked value");
    }

    @Test
    void testCheckboxes() throws Exception {
        String[] values = { "on1", "on2" };
        boolean[] initialChecked = { false, true };

        for (int i = 0; i < _checkbox.length; i++) {
            _form.appendChild(_checkbox[i]);
        }

        for (int i = 0; i < _checkbox.length; i++) {
            assertSame(_form, _checkbox[i].getForm(), "Form for control");
            assertEquals(values[i], _checkbox[i].getValue(), "Initial value " + i);
            assertEquals(initialChecked[i], _checkbox[i].getChecked(), "Initial checked " + i);
            assertEquals(initialChecked[i], _checkbox[i].getDefaultChecked(), "Initial default checked " + i);
        }

        for (int j = 0; j < _checkbox.length; j++) {
            _checkbox[j].setChecked(!initialChecked[j]);
            for (int i = 0; i < _checkbox.length; i++) {
                assertEquals(values[i], _checkbox[i].getValue(), "value " + i + " after change " + j);
                assertEquals(!initialChecked[j], _checkbox[i].getChecked(), "checked " + i + " after change " + j);
                assertEquals(initialChecked[i], _checkbox[i].getDefaultChecked(),
                        "default checked " + i + " after change " + j);
            }
            ((HTMLControl) _checkbox[j]).reset();
            for (int i = 0; i < _checkbox.length; i++) {
                assertEquals(initialChecked[i], _checkbox[i].getChecked(), "Initial checked " + i);
                assertEquals(initialChecked[i], _checkbox[i].getDefaultChecked(), "Initial default checked " + i);
            }
        }

        _checkbox[0].click();
        assertEquals(true, _checkbox[0].getChecked(), "checkbox 0 after 1st click");

        _checkbox[0].click();
        assertEquals(false, _checkbox[0].getChecked(), "checkbox 0 after 2nd click");
    }

    @Test
    void testRadioButtons() throws Exception {
        for (int i = 0; i < _radio1.length; i++)
            _form.appendChild(_radio1[i]);
        for (int i = 0; i < _radio2.length; i++)
            _form.appendChild(_radio2[i]);

        verifyRadioButtons("radio 1 initial", _radio1, new boolean[] { false, true, false });
        verifyRadioButtons("radio 2 initial", _radio2, new boolean[] { false, false, false, true });

        _radio2[2].setChecked(true);
        verifyRadioButtons("radio 1 after set", _radio1, new boolean[] { false, true, false });
        verifyRadioButtons("radio 2 after set", _radio2, new boolean[] { false, false, true, false });

        _form.reset();
        verifyRadioButtons("radio 1 after reset", _radio1, new boolean[] { false, true, false });
        verifyRadioButtons("radio 2 after reset", _radio2, new boolean[] { false, false, false, true });

        _radio1[0].click();
        verifyRadioButtons("radio 1 after click", _radio1, new boolean[] { true, false, false });
    }

    private void verifyRadioButtons(String comment, HTMLInputElement[] radioButtons, boolean[] expected) {
        for (int i = 0; i < radioButtons.length; i++) {
            assertEquals(expected[i], radioButtons[i].getChecked(), comment + " checked " + i);
        }
    }

    @Test
    void testFormElements() throws Exception {
        HTMLElement[] elements = { _textField, _passwordField, _hiddenField, _textArea, _checkbox[0], _checkbox[1],
                _resetInput, _submitInput };
        _form.appendChild(_htmlDocument.createElement("i")).appendChild(_htmlDocument.createTextNode("Some controls"));
        for (int i = 0; i < elements.length; i++) {
            HTMLElement element = elements[i];
            _form.appendChild(element);
        }
        HTMLCollection collection = _form.getElements();
        assertNotNull(collection, "No collection returned");
        assertEquals(elements.length, collection.getLength(), "Number of elements");
        for (int i = 0; i < elements.length; i++) {
            assertSame(elements[i], collection.item(i), "Form element " + i);
        }
    }

    /**
     * Verifies that controls in the document body after the form but before the next form are included in the form
     * collection of elements.
     *
     * @throws Exception
     */
    @Test
    void testImproperFormElements() throws Exception {
        HTMLElement[] elements = { _textField, _passwordField, _hiddenField, _textArea };
        HTMLElement[] improperElements = { _checkbox[0], _checkbox[1], _resetInput, _submitInput };
        _form.appendChild(_htmlDocument.createElement("i")).appendChild(_htmlDocument.createTextNode("Some controls"));
        for (int i = 0; i < elements.length; i++) {
            HTMLElement element = elements[i];
            _form.appendChild(element);
        }
        for (int i = 0; i < improperElements.length; i++) {
            HTMLElement element = improperElements[i];
            _body.appendChild(element);
        }
        Element form = _htmlDocument.createElement("form");
        _body.appendChild(form);
        form.appendChild(_htmlDocument.createElement("button"));

        HTMLCollection collection = _form.getElements();
        assertNotNull(collection, "No collection returned");
        assertEquals(elements.length + improperElements.length, collection.getLength(), "Number of elements");
        for (int i = 0; i < elements.length; i++) {
            assertSame(elements[i], collection.item(i), "Form element " + i);
            assertSame(_form, ((HTMLControl) elements[i]).getForm(), "Form for element " + i);
        }
        for (int i = 0; i < improperElements.length; i++) {
            int j = elements.length + i;
            assertSame(improperElements[i], collection.item(j), "Form element " + j);
            assertSame(_form, ((HTMLControl) improperElements[i]).getForm(), "Form for element " + j);
        }
    }

    /**
     * Verifies that we can recognize buttons without forms.
     *
     * @throws Exception
     */
    @Test
    void testFormDetection() throws Exception {
        _body.insertBefore(_buttonInput, _form);
        _body.appendChild(_textField);
        _body.appendChild(_htmlDocument.createElement("form"));

        assertNull(_buttonInput.getForm(), "button should not be part of any form");
        assertSame(_form, _textField.getForm(), "Form for text field");
    }

    @Test
    void testResetInput() throws Exception {
        _form.appendChild(_textArea);
        _textArea.setDefaultValue("Original");
        _form.appendChild(_resetInput);

        assertEquals("Original", _textArea.getValue(), "generated default");
        _textArea.setValue("Changed this");
        _resetInput.click();
        assertEquals("Original", _textArea.getValue(), "value after reset");
    }

    @Test
    void testGetControlByName() throws Exception {
        _form.appendChild(_textField);
        _form.appendChild(_passwordField);
        _form.appendChild(_hiddenField);
        _form.appendChild(_defaultField);

        verifyNamedControlAndValue("text", _textField, "initial");
        verifyNamedControlAndValue("hidden", _hiddenField, "saved");
    }

    private void verifyNamedControlAndValue(String name, HTMLInputElement textField, String expectedValue) {
        Object o = ((HTMLFormElementImpl) _form).get(name, null);
        assertTrue(o instanceof ScriptableObject, "Result should be scriptable, is " + o);
        ScriptableObject control = (ScriptableObject) o;
        assertSame(textField, control, "control");
        assertEquals(expectedValue, control.get("value", null), "field value");
    }

}

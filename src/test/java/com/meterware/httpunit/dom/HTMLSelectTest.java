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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.html.HTMLFormElement;
import org.w3c.dom.html.HTMLOptionElement;
import org.w3c.dom.html.HTMLSelectElement;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 */
class HTMLSelectTest extends AbstractHTMLElementTest {

    private HTMLFormElement _form;
    private HTMLSelectElement _select;
    private HTMLOptionElement[] _options;

    @BeforeEach
    void setUp() throws Exception {
        _form = (HTMLFormElement) createElement("form", new String[][] { { "action", "go_here" } });
        _select = (HTMLSelectElement) createElement("select");
        _htmlDocument.appendChild(_form);
        _form.appendChild(_select);

        _options = new HTMLOptionElement[] { createOption("red", "Vermillion", false),
                createOption("blue", "Azure", true), createOption("green", "Chartreuse", false) };
        for (HTMLOptionElement option : _options) {
            _select.appendChild(option);
        }
    }

    @Test
    void testSingleSelect() throws Exception {
        assertSame(_form, _select.getForm(), "Form for select");
        assertEquals(HTMLSelectElementImpl.TYPE_SELECT_ONE, _select.getType(), "type with no size");
        assertEquals(1, _select.getSelectedIndex(), "select index");
        assertEquals("blue", _select.getValue(), "initial value");

        _select.setSelectedIndex(0);
        assertEquals(0, _select.getSelectedIndex(), "modified select index");
        assertProperties("changed default selected", "defaultSelected", _options,
                new Boolean[] { Boolean.FALSE, Boolean.TRUE, Boolean.FALSE });
        assertProperties("changed selected", "selected", _options,
                new Boolean[] { Boolean.TRUE, Boolean.FALSE, Boolean.FALSE });

        ((HTMLOptionElementImpl) _options[2]).setSelected(true);
        assertEquals(2, _select.getSelectedIndex(), "remodified select index");
        assertProperties("rechanged selected", "selected", _options,
                new Boolean[] { Boolean.FALSE, Boolean.FALSE, Boolean.TRUE });

        ((HTMLControl) _select).reset();
        assertEquals("blue", _select.getValue(), "reset value");
        assertEquals(1, _select.getSelectedIndex(), "reset index");
        assertProperties("reset selected", "selected", _options,
                new Boolean[] { Boolean.FALSE, Boolean.TRUE, Boolean.FALSE });
    }

    @Test
    void testMultiSelect() throws Exception {
        _select.setMultiple(true);
        _select.setSize(3);

        assertEquals(HTMLSelectElementImpl.TYPE_SELECT_MULTIPLE, _select.getType(), "type with size");
        assertEquals(1, _select.getSelectedIndex(), "select index");
        assertEquals("blue", _select.getValue(), "initial value");

        ((HTMLOptionElementImpl) _options[0]).setSelected(true);
        assertEquals(0, _select.getSelectedIndex(), "modified select index");
        assertProperties("changed default selected", "defaultSelected", _options,
                new Boolean[] { Boolean.FALSE, Boolean.TRUE, Boolean.FALSE });
        assertProperties("changed selected", "selected", _options,
                new Boolean[] { Boolean.TRUE, Boolean.TRUE, Boolean.FALSE });

        ((HTMLControl) _select).reset();
        assertEquals("blue", _select.getValue(), "reset value");
        assertProperties("reset selected", "selected", _options,
                new Boolean[] { Boolean.FALSE, Boolean.TRUE, Boolean.FALSE });
    }

    @Test
    void testSingleLineSelect() throws Exception {
        _select.setMultiple(true);
        _select.setSize(1);
        assertEquals(HTMLSelectElementImpl.TYPE_SELECT_ONE, _select.getType(), "type with size 1");

        assertEquals(1, _select.getSelectedIndex(), "select index");
        assertEquals("blue", _select.getValue(), "initial value");

        ((HTMLOptionElementImpl) _options[0]).setSelected(true);
        assertEquals(0, _select.getSelectedIndex(), "modified select index");
        assertProperties("changed default selected", "defaultSelected", _options,
                new Boolean[] { Boolean.FALSE, Boolean.TRUE, Boolean.FALSE });
        assertProperties("changed selected", "selected", _options,
                new Boolean[] { Boolean.TRUE, Boolean.FALSE, Boolean.FALSE });
    }

    @Test
    void testElements() throws Exception {
        assertEquals(_options.length, _select.getOptions().getLength(), "number of options");
        assertSame(_options[0], _select.getOptions().item(0), "first option");
        assertProperties("default selected", "defaultSelected", _options,
                new Boolean[] { Boolean.FALSE, Boolean.TRUE, Boolean.FALSE });
        assertProperties("initial selected", "selected", _options,
                new Boolean[] { Boolean.FALSE, Boolean.TRUE, Boolean.FALSE });
        assertProperties("index", "index", _options,
                new Integer[] { Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2) });
        assertProperties("text", "text", _options, new String[] { "Vermillion", "Azure", "Chartreuse" });
        assertProperties("value", "value", _options, new String[] { "red", "blue", "green" });
        assertEquals(_options.length, _select.getLength(), "select length");
    }

    @Test
    void testSingleWithNothingSelected() throws Exception {
        ((HTMLOptionElementImpl) _options[1]).setSelected(false);
        assertEquals(0, _select.getSelectedIndex(), "select index");
        assertEquals("red", _select.getValue(), "initial value");

        assertProperties("initial selected", "selected", _options,
                new Boolean[] { Boolean.FALSE, Boolean.FALSE, Boolean.FALSE });
    }

    @Test
    void testMultipleWithNothingSelected() throws Exception {
        _select.setMultiple(true);
        _select.setSize(3);
        ((HTMLOptionElementImpl) _options[1]).setSelected(false);
        assertEquals(-1, _select.getSelectedIndex(), "select index");
        assertNull(_select.getValue(), "initial value");

        assertProperties("initial selected", "selected", _options,
                new Boolean[] { Boolean.FALSE, Boolean.FALSE, Boolean.FALSE });
    }

    // XXX value (write), length (write)
    // XXX add, remove, blur, focus

}

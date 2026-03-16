/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
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
 * The Class HTMLSelectTest.
 */
class HTMLSelectTest extends AbstractHTMLElementTest {

    /** The form. */
    private HTMLFormElement _form;

    /** The select. */
    private HTMLSelectElement _select;

    /** The options. */
    private HTMLOptionElement[] _options;

    /**
     * Sets the up.
     *
     * @throws Exception
     *             the exception
     */
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

    /**
     * Single select.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void singleSelect() throws Exception {
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

    /**
     * Multi select.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void multiSelect() throws Exception {
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

    /**
     * Single line select.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void singleLineSelect() throws Exception {
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

    /**
     * Elements.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void elements() throws Exception {
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

    /**
     * Single with nothing selected.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void singleWithNothingSelected() throws Exception {
        ((HTMLOptionElementImpl) _options[1]).setSelected(false);
        assertEquals(0, _select.getSelectedIndex(), "select index");
        assertEquals("red", _select.getValue(), "initial value");

        assertProperties("initial selected", "selected", _options,
                new Boolean[] { Boolean.FALSE, Boolean.FALSE, Boolean.FALSE });
    }

    /**
     * Multiple with nothing selected.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void multipleWithNothingSelected() throws Exception {
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

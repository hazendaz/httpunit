/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.dom;

import com.meterware.httpunit.protocol.ParameterProcessor;

import java.io.IOException;

import org.w3c.dom.DOMException;
import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLOptionElement;
import org.w3c.dom.html.HTMLSelectElement;

/**
 * The Class HTMLSelectElementImpl.
 */
public class HTMLSelectElementImpl extends HTMLControl implements HTMLSelectElement {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Constant TYPE_SELECT_ONE. */
    public static final String TYPE_SELECT_ONE = "select-one";

    /** The Constant TYPE_SELECT_MULTIPLE. */
    public static final String TYPE_SELECT_MULTIPLE = "select-multiple";

    @Override
    ElementImpl create() {
        return new HTMLSelectElementImpl();
    }

    /**
     * Adds the.
     *
     * @param element
     *            the element
     * @param before
     *            the before
     *
     * @throws DOMException
     *             the DOM exception
     */
    @Override
    public void add(HTMLElement element, HTMLElement before) throws DOMException {
    }

    /**
     * simulate blur.
     */
    @Override
    public void blur() {
        handleEvent("onblur");
    }

    /**
     * simulate focus;.
     */
    @Override
    public void focus() {
        handleEvent("onfocus");
    }

    @Override
    public String getType() {
        return isMultiSelect() ? TYPE_SELECT_MULTIPLE : TYPE_SELECT_ONE;
    }

    /**
     * Checks if is multi select.
     *
     * @return true, if is multi select
     */
    private boolean isMultiSelect() {
        return getMultiple() && getSize() > 1;
    }

    /**
     * Gets the length.
     *
     * @return the length
     */
    @Override
    public int getLength() {
        return getOptions().getLength();
    }

    /**
     * Gets the multiple.
     *
     * @return the multiple
     */
    @Override
    public boolean getMultiple() {
        return getBooleanAttribute("multiple");
    }

    /**
     * Gets the options.
     *
     * @return the options
     */
    @Override
    public HTMLCollection getOptions() {
        return HTMLCollectionImpl
                .createHTMLCollectionImpl(getElementsByTagName(getHtmlDocument().toNodeCase("option")));
    }

    /**
     * Gets the selected index.
     *
     * @return the selected index
     */
    @Override
    public int getSelectedIndex() {
        HTMLCollection options = getOptions();
        for (int i = 0; i < options.getLength(); i++) {
            if (((HTMLOptionElement) options.item(i)).getSelected()) {
                return i;
            }
        }
        return isMultiSelect() ? -1 : 0;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    @Override
    public String getValue() {
        HTMLCollection options = getOptions();
        for (int i = 0; i < options.getLength(); i++) {
            HTMLOptionElement optionElement = (HTMLOptionElement) options.item(i);
            if (optionElement.getSelected()) {
                return optionElement.getValue();
            }
        }
        return isMultiSelect() || options.getLength() == 0 ? null : ((HTMLOptionElement) options.item(0)).getValue();
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    @Override
    public int getSize() {
        return getIntegerAttribute("size");
    }

    /**
     * Removes the.
     *
     * @param index
     *            the index
     */
    @Override
    public void remove(int index) {
    }

    /**
     * Sets the multiple.
     *
     * @param multiple
     *            the new multiple
     */
    @Override
    public void setMultiple(boolean multiple) {
        setAttribute("multiple", multiple);
    }

    /**
     * Sets the selected index.
     *
     * @param selectedIndex
     *            the new selected index
     */
    @Override
    public void setSelectedIndex(int selectedIndex) {
        HTMLCollection options = getOptions();
        for (int i = 0; i < options.getLength(); i++) {
            HTMLOptionElementImpl optionElement = (HTMLOptionElementImpl) options.item(i);
            optionElement.setSelected(i == selectedIndex);
        }
    }

    /**
     * Sets the size.
     *
     * @param size
     *            the new size
     */
    @Override
    public void setSize(int size) {
        setAttribute("size", size);
    }

    /**
     * Gets the index of.
     *
     * @param option
     *            the option
     *
     * @return the index of
     */
    int getIndexOf(HTMLOptionElementImpl option) {
        HTMLCollection options = getOptions();
        for (int i = 0; i < options.getLength(); i++) {
            if (options.item(i) == option) {
                return i;
            }
        }
        throw new IllegalStateException("option is not part of this select");
    }

    /**
     * Clear selected.
     */
    void clearSelected() {
        setSelectedIndex(-1);
    }

    @Override
    void addValues(ParameterProcessor processor, String characterSet) throws IOException {
        HTMLCollection options = getOptions();
        String name = getName();
        for (int i = 0; i < options.getLength(); i++) {
            ((HTMLOptionElementImpl) options.item(i)).addValueIfSelected(processor, name, characterSet);
        }
    }

    /**
     * Sets the value.
     *
     * @param value
     *            the new value
     */
    @Override
    public void setValue(String value) {
        setAttribute("value", value);
    }

    @Override
    public void reset() {
        HTMLCollection options = getOptions();
        for (int i = 0; i < options.getLength(); i++) {
            HTMLControl optionElement = (HTMLControl) options.item(i);
            optionElement.reset();
        }
    }
}

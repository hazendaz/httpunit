/*
 * MIT License
 *
 * Copyright 2011-2026 Russell Gold
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

import com.meterware.httpunit.protocol.ParameterProcessor;

import java.io.IOException;

import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLOptionElement;

/**
 * The Class HTMLOptionElementImpl.
 */
public class HTMLOptionElementImpl extends HTMLControl implements HTMLOptionElement {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The selected. */
    private Boolean _selected;

    @Override
    ElementImpl create() {
        return new HTMLOptionElementImpl();
    }

    /**
     * Gets the default selected.
     *
     * @return the default selected
     */
    @Override
    public boolean getDefaultSelected() {
        return getBooleanAttribute("selected");
    }

    /**
     * Gets the index.
     *
     * @return the index
     */
    @Override
    public int getIndex() {
        return getSelect().getIndexOf(this);
    }

    /**
     * Sets the index.
     *
     * @param i
     *            the new index
     */
    public void setIndex(int i) {
    } // obsolete - required for compatibility with JDK 1.3

    /**
     * Gets the label.
     *
     * @return the label
     */
    @Override
    public String getLabel() {
        return getAttributeWithNoDefault("label");
    }

    /**
     * Gets the selected.
     *
     * @return the selected
     */
    @Override
    public boolean getSelected() {
        return _selected != null ? _selected.booleanValue() : getDefaultSelected();
    }

    /**
     * Gets the text.
     *
     * @return the text
     */
    @Override
    public String getText() {
        return asText();
    }

    /**
     * Sets the default selected.
     *
     * @param defaultSelected
     *            the new default selected
     */
    @Override
    public void setDefaultSelected(boolean defaultSelected) {
    }

    /**
     * Sets the label.
     *
     * @param label
     *            the new label
     */
    @Override
    public void setLabel(String label) {
        setAttribute("label", label);
    }

    /**
     * Sets the selected.
     *
     * @param selected
     *            the new selected
     */
    public void setSelected(boolean selected) {
        if (selected && getSelect().getType().equals(HTMLSelectElementImpl.TYPE_SELECT_ONE)) {
            getSelect().clearSelected();
        }
        _selected = selected ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Gets the select.
     *
     * @return the select
     */
    private HTMLSelectElementImpl getSelect() {
        Node parent = getParentNode();
        while (parent != null && !"select".equalsIgnoreCase(parent.getNodeName())) {
            parent = parent.getParentNode();
        }
        return (HTMLSelectElementImpl) parent;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    @Override
    public String getValue() {
        return getAttributeWithNoDefault("value");
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
        _selected = null;
    }

    /**
     * Adds the value if selected.
     *
     * @param processor
     *            the processor
     * @param name
     *            the name
     * @param characterSet
     *            the character set
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    void addValueIfSelected(ParameterProcessor processor, String name, String characterSet) throws IOException {
        if (getSelected()) {
            String value = getValue();
            if (value == null) {
                value = readDisplayedValue();
            }
            processor.addParameter(name, value, characterSet);
        }
    }

    /**
     * Read displayed value.
     *
     * @return the string
     */
    private String readDisplayedValue() {
        Node nextSibling = getNextSibling();
        while (nextSibling != null && nextSibling.getNodeType() != Node.TEXT_NODE
                && nextSibling.getNodeType() != Node.ELEMENT_NODE) {
            nextSibling = nextSibling.getNextSibling();
        }
        if (nextSibling == null || nextSibling.getNodeType() != Node.TEXT_NODE) {
            return "";
        }
        return nextSibling.getNodeValue();
    }

}

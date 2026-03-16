/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.dom;

import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.html.HTMLTextAreaElement;

/**
 * The Class HTMLTextAreaElementImpl.
 */
public class HTMLTextAreaElementImpl extends HTMLControl implements HTMLTextAreaElement {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The value. */
    private String _value;

    @Override
    ElementImpl create() {
        return new HTMLTextAreaElementImpl();
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

    /**
     * Gets the access key.
     *
     * @return the access key
     */
    @Override
    public String getAccessKey() {
        return getAttributeWithNoDefault("accesskey");
    }

    /**
     * Gets the cols.
     *
     * @return the cols
     */
    @Override
    public int getCols() {
        return getIntegerAttribute("cols");
    }

    /**
     * Gets the default value.
     *
     * @return the default value
     */
    @Override
    public String getDefaultValue() {
        Node node = getFirstChild();

        if (node == null) {
            return "";
        }

        if (node.getNodeType() != Node.TEXT_NODE) {
            return null;
        }

        return node.getNodeValue();
    }

    /**
     * Gets the rows.
     *
     * @return the rows
     */
    @Override
    public int getRows() {
        return getIntegerAttribute("rows");
    }

    /**
     * Select.
     */
    @Override
    public void select() {
    }

    /**
     * Sets the access key.
     *
     * @param accessKey
     *            the new access key
     */
    @Override
    public void setAccessKey(String accessKey) {
        setAttribute("accesskey", accessKey);
    }

    /**
     * Sets the cols.
     *
     * @param cols
     *            the new cols
     */
    @Override
    public void setCols(int cols) {
        setAttribute("cols", cols);
    }

    /**
     * Sets the default value.
     *
     * @param defaultValue
     *            the new default value
     */
    @Override
    public void setDefaultValue(String defaultValue) {
        Text textNode = getOwnerDocument().createTextNode(defaultValue);
        Node child = getFirstChild();
        if (child == null) {
            appendChild(textNode);
        } else {
            replaceChild(textNode, child);
        }
    }

    /**
     * Sets the rows.
     *
     * @param rows
     *            the new rows
     */
    @Override
    public void setRows(int rows) {
        setAttribute("rows", rows);
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    @Override
    public String getValue() {
        return _value != null ? _value : getDefaultValue();
    }

    /**
     * Sets the value.
     *
     * @param value
     *            the new value
     */
    @Override
    public void setValue(String value) {
        _value = value;
    }

    @Override
    public void reset() {
        _value = null;
    }
}

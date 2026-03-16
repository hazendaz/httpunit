/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.dom;

import org.w3c.dom.Attr;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLElement;

/**
 * The Class HTMLElementImpl.
 */
public class HTMLElementImpl extends ElementImpl implements HTMLElement {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Constant UNSPECIFIED_ATTRIBUTE. */
    public static final String UNSPECIFIED_ATTRIBUTE = null;

    /**
     * Creates the.
     *
     * @return the element impl
     */
    ElementImpl create() {
        return new HTMLElementImpl();
    }

    /**
     * Click.
     */
    public void click() {
        doClickAction();
    }

    /**
     * Do click action.
     */
    public void doClickAction() {
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    @Override
    public String getId() {
        return getAttributeWithNoDefault("id");
    }

    /**
     * Sets the id.
     *
     * @param id
     *            the new id
     */
    @Override
    public void setId(String id) {
        setAttribute("id", id);
    }

    /**
     * Gets the title.
     *
     * @return the title
     */
    @Override
    public String getTitle() {
        return getAttributeWithNoDefault("title");
    }

    /**
     * Sets the title.
     *
     * @param title
     *            the new title
     */
    @Override
    public void setTitle(String title) {
        setAttribute("title", title);
    }

    /**
     * Gets the lang.
     *
     * @return the lang
     */
    @Override
    public String getLang() {
        return getAttributeWithNoDefault("lang");
    }

    /**
     * Sets the lang.
     *
     * @param lang
     *            the new lang
     */
    @Override
    public void setLang(String lang) {
        setAttribute("lang", lang);
    }

    /**
     * Gets the dir.
     *
     * @return the dir
     */
    @Override
    public String getDir() {
        return getAttributeWithNoDefault("dir");
    }

    /**
     * Sets the dir.
     *
     * @param dir
     *            the new dir
     */
    @Override
    public void setDir(String dir) {
        setAttribute("dir", dir);
    }

    @Override
    public String getClassName() {
        return getAttributeWithNoDefault("class");
    }

    /**
     * Sets the class name.
     *
     * @param className
     *            the new class name
     */
    @Override
    public void setClassName(String className) {
        setAttribute("class", className);
    }

    @Override
    public NodeList getElementsByTagName(String name) {
        return super.getElementsByTagName(((HTMLDocumentImpl) getOwnerDocument()).toNodeCase(name));
    }

    // ---------------------------------------------- protected methods
    // -----------------------------------------------------

    /**
     * Gets the attribute with default.
     *
     * @param attributeName
     *            the attribute name
     * @param defaultValue
     *            the default value
     *
     * @return the attribute with default
     */
    protected final String getAttributeWithDefault(String attributeName, String defaultValue) {
        if (hasAttribute(attributeName)) {
            return getAttribute(attributeName);
        }
        return defaultValue;
    }

    /**
     * Gets the attribute with no default.
     *
     * @param attributeName
     *            the attribute name
     *
     * @return the attribute with no default
     */
    protected final String getAttributeWithNoDefault(String attributeName) {
        if (hasAttribute(attributeName)) {
            return getAttribute(attributeName);
        }
        return UNSPECIFIED_ATTRIBUTE;
    }

    /**
     * Gets the boolean attribute.
     *
     * @param name
     *            the name
     *
     * @return the boolean attribute
     */
    protected boolean getBooleanAttribute(String name) {
        Attr attr = getAttributeNode(name);
        return attr != null && !attr.getValue().equalsIgnoreCase("false");
    }

    /**
     * Gets the integer attribute.
     *
     * @param name
     *            the name
     *
     * @return the integer attribute
     */
    protected int getIntegerAttribute(String name) {
        String value = getAttribute(name);
        return value.isEmpty() ? 0 : Integer.parseInt(value);
    }

    /**
     * Gets the integer attribute.
     *
     * @param name
     *            the name
     * @param defaultValue
     *            the default value
     *
     * @return the integer attribute
     */
    protected int getIntegerAttribute(String name, int defaultValue) {
        String value = getAttribute(name);
        return value.isEmpty() ? defaultValue : Integer.parseInt(value);
    }

    /**
     * Sets the attribute.
     *
     * @param name
     *            the name
     * @param disabled
     *            the disabled
     */
    protected void setAttribute(String name, boolean disabled) {
        setAttribute(name, disabled ? "true" : "false");
    }

    /**
     * Sets the attribute.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     */
    protected void setAttribute(String name, int value) {
        setAttribute(name, Integer.toString(value));
    }

    /**
     * Gets the html document.
     *
     * @return the html document
     */
    HTMLDocumentImpl getHtmlDocument() {
        return (HTMLDocumentImpl) getOwnerDocument();
    }

}

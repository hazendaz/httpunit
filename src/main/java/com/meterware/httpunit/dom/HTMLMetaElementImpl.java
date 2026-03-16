/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.dom;

import org.w3c.dom.html.HTMLMetaElement;

/**
 * The Class HTMLMetaElementImpl.
 */
public class HTMLMetaElementImpl extends HTMLElementImpl implements HTMLMetaElement {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    @Override
    ElementImpl create() {
        return new HTMLMetaElementImpl();
    }

    /**
     * Gets the content.
     *
     * @return the content
     */
    @Override
    public String getContent() {
        return getAttributeWithNoDefault("content");
    }

    /**
     * Gets the http equiv.
     *
     * @return the http equiv
     */
    @Override
    public String getHttpEquiv() {
        return getAttributeWithNoDefault("http-equiv");
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return getAttributeWithNoDefault("name");
    }

    /**
     * Gets the scheme.
     *
     * @return the scheme
     */
    @Override
    public String getScheme() {
        return getAttributeWithNoDefault("scheme");
    }

    /**
     * Sets the content.
     *
     * @param content
     *            the new content
     */
    @Override
    public void setContent(String content) {
        setAttribute("content", content);
    }

    /**
     * Sets the http equiv.
     *
     * @param httpEquiv
     *            the new http equiv
     */
    @Override
    public void setHttpEquiv(String httpEquiv) {
        setAttribute("http-equiv", httpEquiv);
    }

    /**
     * Sets the name.
     *
     * @param name
     *            the new name
     */
    @Override
    public void setName(String name) {
        setAttribute("name", name);
    }

    /**
     * Sets the scheme.
     *
     * @param scheme
     *            the new scheme
     */
    @Override
    public void setScheme(String scheme) {
        setAttribute("scheme", scheme);
    }
}

/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.dom;

import org.w3c.dom.html.HTMLHtmlElement;

/**
 * The Class HTMLHtmlElementImpl.
 */
public class HTMLHtmlElementImpl extends HTMLElementImpl implements HTMLHtmlElement {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    @Override
    ElementImpl create() {
        return new HTMLHtmlElementImpl();
    }

    /**
     * Gets the version.
     *
     * @return the version
     */
    @Override
    public String getVersion() {
        return getAttributeWithNoDefault("version");
    }

    /**
     * Sets the version.
     *
     * @param version
     *            the new version
     */
    @Override
    public void setVersion(String version) {
        setAttribute("version", version);
    }

}

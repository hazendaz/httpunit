/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.dom;

import org.w3c.dom.html.HTMLBaseElement;

/**
 * The Class HTMLBaseElementImpl.
 */
public class HTMLBaseElementImpl extends HTMLElementImpl implements HTMLBaseElement {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    @Override
    ElementImpl create() {
        return new HTMLBaseElementImpl();
    }

    /**
     * Gets the href.
     *
     * @return the href
     */
    @Override
    public String getHref() {
        return getAttributeWithNoDefault("href");
    }

    /**
     * Gets the target.
     *
     * @return the target
     */
    @Override
    public String getTarget() {
        return getAttributeWithNoDefault("target");
    }

    /**
     * Sets the href.
     *
     * @param href
     *            the new href
     */
    @Override
    public void setHref(String href) {
        setAttribute("href", href);
    }

    /**
     * Sets the target.
     *
     * @param target
     *            the new target
     */
    @Override
    public void setTarget(String target) {
        setAttribute("target", target);
    }
}

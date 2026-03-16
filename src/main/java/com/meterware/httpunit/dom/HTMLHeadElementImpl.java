/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.dom;

import org.w3c.dom.html.HTMLHeadElement;

/**
 * The Class HTMLHeadElementImpl.
 */
public class HTMLHeadElementImpl extends HTMLElementImpl implements HTMLHeadElement {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    @Override
    ElementImpl create() {
        return new HTMLHeadElementImpl();
    }

    /**
     * Gets the profile.
     *
     * @return the profile
     */
    @Override
    public String getProfile() {
        return getAttributeWithNoDefault("profile");
    }

    /**
     * Sets the profile.
     *
     * @param profile
     *            the new profile
     */
    @Override
    public void setProfile(String profile) {
        setAttribute("profile", profile);
    }

}

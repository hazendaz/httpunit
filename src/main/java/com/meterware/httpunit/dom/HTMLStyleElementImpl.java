/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.dom;

import org.w3c.dom.html.HTMLStyleElement;

/**
 * The Class HTMLStyleElementImpl.
 */
public class HTMLStyleElementImpl extends HTMLElementImpl implements HTMLStyleElement {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    @Override
    ElementImpl create() {
        return new HTMLStyleElementImpl();
    }

    /**
     * Gets the disabled.
     *
     * @return the disabled
     */
    @Override
    public boolean getDisabled() {
        return getBooleanAttribute("disabled");
    }

    /**
     * Gets the media.
     *
     * @return the media
     */
    @Override
    public String getMedia() {
        return getAttributeWithDefault("media", "screen");
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    @Override
    public String getType() {
        return getAttributeWithNoDefault("type");
    }

    /**
     * Sets the disabled.
     *
     * @param disabled
     *            the new disabled
     */
    @Override
    public void setDisabled(boolean disabled) {
        setAttribute("disabled", disabled);
    }

    /**
     * Sets the media.
     *
     * @param media
     *            the new media
     */
    @Override
    public void setMedia(String media) {
        setAttribute("media", media);
    }

    /**
     * Sets the type.
     *
     * @param type
     *            the new type
     */
    @Override
    public void setType(String type) {
        setAttribute("type", type);
    }
}

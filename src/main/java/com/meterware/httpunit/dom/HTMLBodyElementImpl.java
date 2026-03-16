/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.dom;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.w3c.dom.html.HTMLBodyElement;

/**
 * The Class HTMLBodyElementImpl.
 */
public class HTMLBodyElementImpl extends HTMLElementImpl implements HTMLBodyElement {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The on load. */
    private HTMLEventHandler _onLoad = new HTMLEventHandler(this, "onload");

    @Override
    ElementImpl create() {
        return new HTMLBodyElementImpl();
    }

    /**
     * Gets the onload event.
     *
     * @return the onload event
     */
    public Function getOnloadEvent() {
        if (getParentScope() == null && getOwnerDocument() instanceof Scriptable) {
            setParentScope((Scriptable) getOwnerDocument());
        }
        return _onLoad.getHandler();
    }

    // ----------------------------------------- HTMLBodyElement methods
    // ----------------------------------------------------

    /**
     * Gets the a link.
     *
     * @return the a link
     */
    @Override
    public String getALink() {
        return getAttributeWithNoDefault("aLink");
    }

    /**
     * Gets the background.
     *
     * @return the background
     */
    @Override
    public String getBackground() {
        return getAttributeWithNoDefault("background");
    }

    /**
     * Gets the bg color.
     *
     * @return the bg color
     */
    @Override
    public String getBgColor() {
        return getAttributeWithNoDefault("bgColor");
    }

    /**
     * Gets the link.
     *
     * @return the link
     */
    @Override
    public String getLink() {
        return getAttributeWithNoDefault("link");
    }

    /**
     * Gets the text.
     *
     * @return the text
     */
    @Override
    public String getText() {
        return getAttributeWithNoDefault("text");
    }

    /**
     * Gets the v link.
     *
     * @return the v link
     */
    @Override
    public String getVLink() {
        return getAttributeWithNoDefault("vLink");
    }

    /**
     * Sets the a link.
     *
     * @param aLink
     *            the new a link
     */
    @Override
    public void setALink(String aLink) {
        setAttribute("aLink", aLink);
    }

    /**
     * Sets the background.
     *
     * @param background
     *            the new background
     */
    @Override
    public void setBackground(String background) {
        setAttribute("background", background);
    }

    /**
     * Sets the bg color.
     *
     * @param bgColor
     *            the new bg color
     */
    @Override
    public void setBgColor(String bgColor) {
        setAttribute("bgColor", bgColor);
    }

    /**
     * Sets the link.
     *
     * @param link
     *            the new link
     */
    @Override
    public void setLink(String link) {
        setAttribute("link", link);
    }

    /**
     * Sets the text.
     *
     * @param text
     *            the new text
     */
    @Override
    public void setText(String text) {
        setAttribute("text", text);
    }

    /**
     * Sets the v link.
     *
     * @param vLink
     *            the new v link
     */
    @Override
    public void setVLink(String vLink) {
        setAttribute("vLink", vLink);
    }
}

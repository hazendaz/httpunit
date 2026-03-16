/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.dom;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.html.HTMLAreaElement;

/**
 * The Class HTMLAreaElementImpl.
 */
public class HTMLAreaElementImpl extends HTMLElementImpl implements HTMLAreaElement {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    @Override
    ElementImpl create() {
        return new HTMLAreaElementImpl();
    }

    /**
     * Gets the href.
     *
     * @return the href
     */
    @Override
    public String getHref() {
        try {
            return new URL(((HTMLDocumentImpl) getOwnerDocument()).getWindow().getUrl(),
                    getAttributeWithNoDefault("href")).toExternalForm();
        } catch (MalformedURLException e) {
            return e.toString();
        }
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
     * Gets the coords.
     *
     * @return the coords
     */
    @Override
    public String getCoords() {
        return getAttributeWithNoDefault("coords");
    }

    /**
     * Gets the shape.
     *
     * @return the shape
     */
    @Override
    public String getShape() {
        return getAttributeWithNoDefault("shape");
    }

    /**
     * Gets the tab index.
     *
     * @return the tab index
     */
    @Override
    public int getTabIndex() {
        return getIntegerAttribute("tabindex");
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
     * Sets the coords.
     *
     * @param coords
     *            the new coords
     */
    @Override
    public void setCoords(String coords) {
        setAttribute("coords", coords);
    }

    /**
     * Sets the shape.
     *
     * @param shape
     *            the new shape
     */
    @Override
    public void setShape(String shape) {
        setAttribute("shape", shape);
    }

    /**
     * Sets the tab index.
     *
     * @param tabIndex
     *            the new tab index
     */
    @Override
    public void setTabIndex(int tabIndex) {
        setAttribute("tabindex", tabIndex);
    }

    /**
     * Gets the alt.
     *
     * @return the alt
     */
    @Override
    public String getAlt() {
        return getAttributeWithNoDefault("alt");
    }

    /**
     * Gets the no href.
     *
     * @return the no href
     */
    @Override
    public boolean getNoHref() {
        return getBooleanAttribute("nohref");
    }

    /**
     * Sets the alt.
     *
     * @param alt
     *            the new alt
     */
    @Override
    public void setAlt(String alt) {
        setAttribute("alt", alt);
    }

    /**
     * Sets the no href.
     *
     * @param noHref
     *            the new no href
     */
    @Override
    public void setNoHref(boolean noHref) {
        setAttribute("nohref", noHref);
    }
}

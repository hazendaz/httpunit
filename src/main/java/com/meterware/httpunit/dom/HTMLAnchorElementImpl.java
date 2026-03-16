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

import org.w3c.dom.html.HTMLAnchorElement;

/**
 * The Class HTMLAnchorElementImpl.
 */
public class HTMLAnchorElementImpl extends HTMLElementImpl implements HTMLAnchorElement {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    @Override
    ElementImpl create() {
        return new HTMLAnchorElementImpl();
    }

    /**
     * Gets the charset.
     *
     * @return the charset
     */
    @Override
    public String getCharset() {
        return getAttributeWithNoDefault("charset");
    }

    /**
     * Gets the href.
     *
     * @return the href
     */
    @Override
    public String getHref() {
        String relativeLocation = getAttributeWithNoDefault("href");
        if (relativeLocation.indexOf(':') > 0 || relativeLocation.equals("#")) {
            return relativeLocation;
        }
        try {
            return new URL(((HTMLDocumentImpl) getOwnerDocument()).getBaseUrl(), relativeLocation).toExternalForm();
        } catch (MalformedURLException e) {
            return e.toString();
        }
    }

    /**
     * Gets the hreflang.
     *
     * @return the hreflang
     */
    @Override
    public String getHreflang() {
        return getAttributeWithNoDefault("hreflang");
    }

    /**
     * Gets the rel.
     *
     * @return the rel
     */
    @Override
    public String getRel() {
        return getAttributeWithNoDefault("rel");
    }

    /**
     * Gets the rev.
     *
     * @return the rev
     */
    @Override
    public String getRev() {
        return getAttributeWithNoDefault("rev");
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
     * Gets the type.
     *
     * @return the type
     */
    @Override
    public String getType() {
        return getAttributeWithNoDefault("type");
    }

    /**
     * Sets the charset.
     *
     * @param charset
     *            the new charset
     */
    @Override
    public void setCharset(String charset) {
        setAttribute("charset", charset);
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
     * Sets the hreflang.
     *
     * @param hreflang
     *            the new hreflang
     */
    @Override
    public void setHreflang(String hreflang) {
        setAttribute("hreflang", hreflang);
    }

    /**
     * Sets the rel.
     *
     * @param rel
     *            the new rel
     */
    @Override
    public void setRel(String rel) {
        setAttribute("rel", rel);
    }

    /**
     * Sets the rev.
     *
     * @param rev
     *            the new rev
     */
    @Override
    public void setRev(String rev) {
        setAttribute("rev", rev);
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
     * Sets the type.
     *
     * @param type
     *            the new type
     */
    @Override
    public void setType(String type) {
        setAttribute("type", type);
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
     * Gets the coords.
     *
     * @return the coords
     */
    @Override
    public String getCoords() {
        return getAttributeWithNoDefault("coords");
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

    @Override
    public void doClickAction() {
        if (null == getHref() || getHref().startsWith("#")) {
            return;
        }
        try {
            ((HTMLDocumentImpl) getOwnerDocument()).getWindow().submitRequest(this, "GET", getHref(), getTarget(),
                    new byte[0]);
        } catch (Exception e) {
            throw new RuntimeException("Error clicking link: " + e);
        }
    }
}

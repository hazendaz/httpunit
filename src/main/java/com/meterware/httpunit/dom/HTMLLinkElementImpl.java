/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.dom;

import org.w3c.dom.html.HTMLLinkElement;

/**
 * The Class HTMLLinkElementImpl.
 */
public class HTMLLinkElementImpl extends HTMLElementImpl implements HTMLLinkElement {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    @Override
    ElementImpl create() {
        return new HTMLLinkElementImpl();
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
     * Gets the disabled.
     *
     * @return the disabled
     */
    @Override
    public boolean getDisabled() {
        return getBooleanAttribute("disabled");
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
     * Gets the hreflang.
     *
     * @return the hreflang
     */
    @Override
    public String getHreflang() {
        return getAttributeWithNoDefault("hreflang");
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

}

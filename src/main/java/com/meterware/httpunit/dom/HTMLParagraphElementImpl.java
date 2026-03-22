/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.dom;

import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.html.HTMLParagraphElement;

/**
 * The Class HTMLParagraphElementImpl.
 */
public class HTMLParagraphElementImpl extends HTMLElementImpl implements HTMLParagraphElement, HTMLContainerElement {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    @Override
    ElementImpl create() {
        return new HTMLParagraphElementImpl();
    }

    // ------------------------------------------ HTMLContainerElement methods
    // ----------------------------------------------

    @Override
    public HTMLCollection getLinks() {
        return getHtmlDocument().getContainerDelegate().getLinks(this);
    }

    @Override
    public HTMLCollection getImages() {
        return getHtmlDocument().getContainerDelegate().getImages(this);
    }

    @Override
    public HTMLCollection getApplets() {
        return getHtmlDocument().getContainerDelegate().getApplets(this);
    }

    @Override
    public HTMLCollection getForms() {
        return getHtmlDocument().getContainerDelegate().getForms(this);
    }

    @Override
    public HTMLCollection getAnchors() {
        return getHtmlDocument().getContainerDelegate().getAnchors(this);
    }

    // ----------------------------------------- HTMLParagraphElement methods
    // -----------------------------------------------

    /**
     * Gets the align.
     *
     * @return the align
     */
    @Override
    public String getAlign() {
        return getAttributeWithNoDefault("align");
    }

    /**
     * Sets the align.
     *
     * @param align
     *            the new align
     */
    @Override
    public void setAlign(String align) {
        setAttribute("align", align);
    }

}

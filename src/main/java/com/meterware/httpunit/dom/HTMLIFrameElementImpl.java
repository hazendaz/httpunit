/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.dom;

import org.w3c.dom.Document;
import org.w3c.dom.html.HTMLIFrameElement;

/**
 * The Class HTMLIFrameElementImpl.
 */
public class HTMLIFrameElementImpl extends HTMLElementImpl implements HTMLIFrameElement {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    @Override
    ElementImpl create() {
        return new HTMLIFrameElementImpl();
    }

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

    /**
     * Gets the frame border.
     *
     * @return the frame border
     */
    @Override
    public String getFrameBorder() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Sets the frame border.
     *
     * @param frameBorder
     *            the new frame border
     */
    @Override
    public void setFrameBorder(String frameBorder) {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Gets the height.
     *
     * @return the height
     */
    @Override
    public String getHeight() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Sets the height.
     *
     * @param height
     *            the new height
     */
    @Override
    public void setHeight(String height) {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Gets the long desc.
     *
     * @return the long desc
     */
    @Override
    public String getLongDesc() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Sets the long desc.
     *
     * @param longDesc
     *            the new long desc
     */
    @Override
    public void setLongDesc(String longDesc) {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Gets the margin height.
     *
     * @return the margin height
     */
    @Override
    public String getMarginHeight() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Sets the margin height.
     *
     * @param marginHeight
     *            the new margin height
     */
    @Override
    public void setMarginHeight(String marginHeight) {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Gets the margin width.
     *
     * @return the margin width
     */
    @Override
    public String getMarginWidth() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Sets the margin width.
     *
     * @param marginWidth
     *            the new margin width
     */
    @Override
    public void setMarginWidth(String marginWidth) {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Sets the name.
     *
     * @param name
     *            the new name
     */
    @Override
    public void setName(String name) {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Gets the scrolling.
     *
     * @return the scrolling
     */
    @Override
    public String getScrolling() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Sets the scrolling.
     *
     * @param scrolling
     *            the new scrolling
     */
    @Override
    public void setScrolling(String scrolling) {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Gets the src.
     *
     * @return the src
     */
    @Override
    public String getSrc() {
        return getAttributeWithNoDefault("src");
    }

    /**
     * Sets the src.
     *
     * @param src
     *            the new src
     */
    @Override
    public void setSrc(String src) {
        setAttribute("src", src);
    }

    /**
     * Gets the width.
     *
     * @return the width
     */
    @Override
    public String getWidth() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Sets the width.
     *
     * @param width
     *            the new width
     */
    @Override
    public void setWidth(String width) {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Gets the content document.
     *
     * @return the content document
     */
    @Override
    public Document getContentDocument() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

}

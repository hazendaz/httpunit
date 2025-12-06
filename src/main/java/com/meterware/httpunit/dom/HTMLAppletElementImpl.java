/*
 * MIT License
 *
 * Copyright 2011-2025 Russell Gold
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package com.meterware.httpunit.dom;

import org.w3c.dom.html.HTMLAppletElement;

/**
 * The Class HTMLAppletElementImpl.
 */
public class HTMLAppletElementImpl extends HTMLElementImpl implements HTMLAppletElement {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    @Override
    ElementImpl create() {
        return new HTMLAppletElementImpl();
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
     * Gets the alt.
     *
     * @return the alt
     */
    @Override
    public String getAlt() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Sets the alt.
     *
     * @param alt
     *            the new alt
     */
    @Override
    public void setAlt(String alt) {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Gets the archive.
     *
     * @return the archive
     */
    @Override
    public String getArchive() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Sets the archive.
     *
     * @param archive
     *            the new archive
     */
    @Override
    public void setArchive(String archive) {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Gets the code.
     *
     * @return the code
     */
    @Override
    public String getCode() {
        return getAttributeWithNoDefault("code");
    }

    /**
     * Sets the code.
     *
     * @param code
     *            the new code
     */
    @Override
    public void setCode(String code) {
        setAttribute("code", code);
    }

    /**
     * get the codebase of this applet modified for bug report [ 1895501 ] Handling no codebase attribute in APPLET tag.
     *
     * @return the code base
     */
    @Override
    public String getCodeBase() {
        return getAttributeWithDefault("codebase", ".");
    }

    /**
     * Sets the code base.
     *
     * @param codeBase
     *            the new code base
     */
    @Override
    public void setCodeBase(String codeBase) {
        setAttribute("codebase", codeBase);
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
     * Gets the hspace.
     *
     * @return the hspace
     */
    @Override
    public String getHspace() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Sets the hspace.
     *
     * @param hspace
     *            the new hspace
     */
    @Override
    public void setHspace(String hspace) {
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
     * Gets the object.
     *
     * @return the object
     */
    @Override
    public String getObject() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Sets the object.
     *
     * @param object
     *            the new object
     */
    @Override
    public void setObject(String object) {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Gets the vspace.
     *
     * @return the vspace
     */
    @Override
    public String getVspace() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Sets the vspace.
     *
     * @param vspace
     *            the new vspace
     */
    @Override
    public void setVspace(String vspace) {
        // To change body of implemented methods use File | Settings | File Templates.
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
}

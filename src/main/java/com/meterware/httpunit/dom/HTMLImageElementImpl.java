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

import org.w3c.dom.html.HTMLImageElement;

/**
 * The Class HTMLImageElementImpl.
 */
public class HTMLImageElementImpl extends HTMLElementImpl implements HTMLImageElement {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    @Override
    ElementImpl create() {
        return new HTMLImageElementImpl();
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
     * Gets the alt.
     *
     * @return the alt
     */
    @Override
    public String getAlt() {
        return getAttributeWithNoDefault("alt");
    }

    /**
     * Gets the border.
     *
     * @return the border
     */
    @Override
    public String getBorder() {
        return getAttributeWithNoDefault("border");
    }

    /**
     * Gets the height.
     *
     * @return the height
     */
    @Override
    public String getHeight() {
        return getAttributeWithNoDefault("height");
    }

    /**
     * Gets the hspace.
     *
     * @return the hspace
     */
    @Override
    public String getHspace() {
        return getAttributeWithNoDefault("hspace");
    }

    /**
     * Gets the checks if is map.
     *
     * @return the checks if is map
     */
    @Override
    public boolean getIsMap() {
        return getBooleanAttribute("ismap");
    }

    /**
     * Gets the long desc.
     *
     * @return the long desc
     */
    @Override
    public String getLongDesc() {
        return getAttributeWithNoDefault("longdesc");
    }

    /**
     * Gets the low src.
     *
     * @return the low src
     */
    @Override
    public String getLowSrc() {
        return null;
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
     * Gets the src.
     *
     * @return the src
     */
    @Override
    public String getSrc() {
        return getAttributeWithNoDefault("src");
    }

    /**
     * Gets the use map.
     *
     * @return the use map
     */
    @Override
    public String getUseMap() {
        return getAttributeWithNoDefault("usemap");
    }

    /**
     * Gets the vspace.
     *
     * @return the vspace
     */
    @Override
    public String getVspace() {
        return getAttributeWithNoDefault("vspace");
    }

    /**
     * Gets the width.
     *
     * @return the width
     */
    @Override
    public String getWidth() {
        return getAttributeWithNoDefault("width");
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
     * Sets the border.
     *
     * @param border
     *            the new border
     */
    @Override
    public void setBorder(String border) {
        setAttribute("border", border);
    }

    /**
     * Sets the height.
     *
     * @param height
     *            the new height
     */
    @Override
    public void setHeight(String height) {
        setAttribute("height", height);
    }

    /**
     * Sets the hspace.
     *
     * @param hspace
     *            the new hspace
     */
    @Override
    public void setHspace(String hspace) {
        setAttribute("hspace", hspace);
    }

    /**
     * Sets the checks if is map.
     *
     * @param isMap
     *            the new checks if is map
     */
    @Override
    public void setIsMap(boolean isMap) {
        setAttribute("ismap", isMap);
    }

    /**
     * Sets the long desc.
     *
     * @param longDesc
     *            the new long desc
     */
    @Override
    public void setLongDesc(String longDesc) {
        setAttribute("longdesc", longDesc);
    }

    /**
     * Sets the low src.
     *
     * @param lowSrc
     *            the new low src
     */
    @Override
    public void setLowSrc(String lowSrc) {
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
     * Sets the use map.
     *
     * @param useMap
     *            the new use map
     */
    @Override
    public void setUseMap(String useMap) {
        setAttribute("usemap", useMap);
    }

    /**
     * Sets the vspace.
     *
     * @param vspace
     *            the new vspace
     */
    @Override
    public void setVspace(String vspace) {
        setAttribute("vspace", vspace);
    }

    /**
     * Sets the width.
     *
     * @param width
     *            the new width
     */
    @Override
    public void setWidth(String width) {
        setAttribute("width", width);
    }
}

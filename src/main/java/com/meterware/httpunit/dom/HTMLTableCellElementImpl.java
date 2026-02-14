/*
 * MIT License
 *
 * Copyright 2011-2026 Russell Gold
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

import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.html.HTMLTableCellElement;

/**
 * The Class HTMLTableCellElementImpl.
 */
public class HTMLTableCellElementImpl extends HTMLElementImpl
        implements HTMLTableCellElement, HTMLContainerElement, AttributeNameAdjusted {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    @Override
    ElementImpl create() {
        return new HTMLTableCellElementImpl();
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

    // -------------------------------------------- HTMLTableCellElement methods
    // --------------------------------------------

    /**
     * Gets the abbr.
     *
     * @return the abbr
     */
    @Override
    public String getAbbr() {
        return getAttributeWithNoDefault("abbr");
    }

    /**
     * Sets the abbr.
     *
     * @param abbr
     *            the new abbr
     */
    @Override
    public void setAbbr(String abbr) {
        setAttribute("abbr", abbr);
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
     * Gets the axis.
     *
     * @return the axis
     */
    @Override
    public String getAxis() {
        return getAttributeWithNoDefault("axis");
    }

    /**
     * Sets the axis.
     *
     * @param axis
     *            the new axis
     */
    @Override
    public void setAxis(String axis) {
        setAttribute("axis", axis);
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
     * Gets the cell index.
     *
     * @return the cell index
     */
    @Override
    public int getCellIndex() {
        return 0; // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Gets the ch.
     *
     * @return the ch
     */
    @Override
    public String getCh() {
        return getAttributeWithDefault("char", ".");
    }

    /**
     * Sets the ch.
     *
     * @param ch
     *            the new ch
     */
    @Override
    public void setCh(String ch) {
        setAttribute("char", ch);
    }

    /**
     * Gets the ch off.
     *
     * @return the ch off
     */
    @Override
    public String getChOff() {
        return getAttributeWithNoDefault("charoff");
    }

    /**
     * Sets the ch off.
     *
     * @param chOff
     *            the new ch off
     */
    @Override
    public void setChOff(String chOff) {
        setAttribute("charoff", chOff);
    }

    /**
     * Gets the col span.
     *
     * @return the col span
     */
    @Override
    public int getColSpan() {
        return getIntegerAttribute("colspan", 1);
    }

    /**
     * Sets the col span.
     *
     * @param colSpan
     *            the new col span
     */
    @Override
    public void setColSpan(int colSpan) {
        setAttribute("colspan", colSpan);
    }

    /**
     * Gets the headers.
     *
     * @return the headers
     */
    @Override
    public String getHeaders() {
        return getAttributeWithNoDefault("headers");
    }

    /**
     * Sets the headers.
     *
     * @param headers
     *            the new headers
     */
    @Override
    public void setHeaders(String headers) {
        setAttribute("headers", headers);
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
     * Gets the no wrap.
     *
     * @return the no wrap
     */
    @Override
    public boolean getNoWrap() {
        return getBooleanAttribute("nowrap");
    }

    /**
     * Sets the no wrap.
     *
     * @param noWrap
     *            the new no wrap
     */
    @Override
    public void setNoWrap(boolean noWrap) {
        setAttribute("nowrap", noWrap);
    }

    /**
     * Gets the row span.
     *
     * @return the row span
     */
    @Override
    public int getRowSpan() {
        return getIntegerAttribute("rowspan", 1);
    }

    /**
     * Sets the row span.
     *
     * @param rowSpan
     *            the new row span
     */
    @Override
    public void setRowSpan(int rowSpan) {
        setAttribute("rowspan", rowSpan);
    }

    /**
     * Gets the scope.
     *
     * @return the scope
     */
    @Override
    public String getScope() {
        return getAttributeWithNoDefault("scope");
    }

    /**
     * Sets the scope.
     *
     * @param scope
     *            the new scope
     */
    @Override
    public void setScope(String scope) {
        setAttribute("scope", scope);
    }

    /**
     * Gets the v align.
     *
     * @return the v align
     */
    @Override
    public String getVAlign() {
        return getAttributeWithDefault("valign", "middle");
    }

    /**
     * Sets the v align.
     *
     * @param vAlign
     *            the new v align
     */
    @Override
    public void setVAlign(String vAlign) {
        setAttribute("valign", vAlign);
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
     * Sets the width.
     *
     * @param width
     *            the new width
     */
    @Override
    public void setWidth(String width) {
        setAttribute("width", width);
    }

    @Override
    public String getJavaAttributeName(String attributeName) {
        if (attributeName.equals("char")) {
            return "ch";
        }
        if (attributeName.equals("charoff")) {
            return "choff";
        }
        return attributeName;
    }

    /**
     * Sets the cell index.
     *
     * @param cellIndex
     *            the new cell index
     */
    public void setCellIndex(int cellIndex) {
        // TODO Auto-generated method stub

    }

}

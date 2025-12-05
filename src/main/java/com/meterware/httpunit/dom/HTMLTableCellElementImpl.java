/*
 * MIT License
 *
 * Copyright 2011-2024 Russell Gold
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

public class HTMLTableCellElementImpl extends HTMLElementImpl
        implements HTMLTableCellElement, HTMLContainerElement, AttributeNameAdjusted {

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

    @Override
    public String getAbbr() {
        return getAttributeWithNoDefault("abbr");
    }

    @Override
    public void setAbbr(String abbr) {
        setAttribute("abbr", abbr);
    }

    @Override
    public String getAlign() {
        return getAttributeWithNoDefault("align");
    }

    @Override
    public void setAlign(String align) {
        setAttribute("align", align);
    }

    @Override
    public String getAxis() {
        return getAttributeWithNoDefault("axis");
    }

    @Override
    public void setAxis(String axis) {
        setAttribute("axis", axis);
    }

    @Override
    public String getBgColor() {
        return getAttributeWithNoDefault("bgColor");
    }

    @Override
    public void setBgColor(String bgColor) {
        setAttribute("bgColor", bgColor);
    }

    @Override
    public int getCellIndex() {
        return 0; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getCh() {
        return getAttributeWithDefault("char", ".");
    }

    @Override
    public void setCh(String ch) {
        setAttribute("char", ch);
    }

    @Override
    public String getChOff() {
        return getAttributeWithNoDefault("charoff");
    }

    @Override
    public void setChOff(String chOff) {
        setAttribute("charoff", chOff);
    }

    @Override
    public int getColSpan() {
        return getIntegerAttribute("colspan", 1);
    }

    @Override
    public void setColSpan(int colSpan) {
        setAttribute("colspan", colSpan);
    }

    @Override
    public String getHeaders() {
        return getAttributeWithNoDefault("headers");
    }

    @Override
    public void setHeaders(String headers) {
        setAttribute("headers", headers);
    }

    @Override
    public String getHeight() {
        return getAttributeWithNoDefault("height");
    }

    @Override
    public void setHeight(String height) {
        setAttribute("height", height);
    }

    @Override
    public boolean getNoWrap() {
        return getBooleanAttribute("nowrap");
    }

    @Override
    public void setNoWrap(boolean noWrap) {
        setAttribute("nowrap", noWrap);
    }

    @Override
    public int getRowSpan() {
        return getIntegerAttribute("rowspan", 1);
    }

    @Override
    public void setRowSpan(int rowSpan) {
        setAttribute("rowspan", rowSpan);
    }

    @Override
    public String getScope() {
        return getAttributeWithNoDefault("scope");
    }

    @Override
    public void setScope(String scope) {
        setAttribute("scope", scope);
    }

    @Override
    public String getVAlign() {
        return getAttributeWithDefault("valign", "middle");
    }

    @Override
    public void setVAlign(String vAlign) {
        setAttribute("valign", vAlign);
    }

    @Override
    public String getWidth() {
        return getAttributeWithNoDefault("width");
    }

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

    public void setCellIndex(int cellIndex) {
        // TODO Auto-generated method stub

    }

}

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

import org.w3c.dom.DOMException;
import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLTableCaptionElement;
import org.w3c.dom.html.HTMLTableElement;
import org.w3c.dom.html.HTMLTableSectionElement;

public class HTMLTableElementImpl extends HTMLElementImpl implements HTMLTableElement {

    private static final long serialVersionUID = 1L;

    @Override
    ElementImpl create() {
        return new HTMLTableElementImpl();
    }

    @Override
    public String getAlign() {
        return getAttributeWithDefault("align", "center");
    }

    @Override
    public void setAlign(String align) {
        setAttribute("align", align);
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
    public String getBorder() {
        return getAttributeWithNoDefault("border");
    }

    @Override
    public void setBorder(String border) {
        setAttribute("border", border);
    }

    @Override
    public String getCellPadding() {
        return getAttributeWithNoDefault("cellpadding");
    }

    @Override
    public void setCellPadding(String cellPadding) {
        setAttribute("cellpadding", cellPadding);
    }

    @Override
    public String getCellSpacing() {
        return getAttributeWithNoDefault("cellspacing");
    }

    @Override
    public void setCellSpacing(String cellSpacing) {
        setAttribute("cellspacing", cellSpacing);
    }

    @Override
    public String getFrame() {
        return getAttributeWithDefault("frame", "void");
    }

    @Override
    public void setFrame(String frame) {
        setAttribute("frame", frame);
    }

    @Override
    public String getRules() {
        return getAttributeWithDefault("rules", "none");
    }

    @Override
    public void setRules(String rules) {
        setAttribute("rules", rules);
    }

    @Override
    public String getSummary() {
        return getAttributeWithNoDefault("summary");
    }

    @Override
    public void setSummary(String summary) {
        setAttribute("summary", summary);
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
    public HTMLElement createCaption() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public HTMLElement createTFoot() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public HTMLElement createTHead() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteCaption() {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteRow(int index) throws DOMException {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteTFoot() {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteTHead() {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public HTMLTableCaptionElement getCaption() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public HTMLCollection getRows() {
        return HTMLCollectionImpl.createHTMLCollectionImpl(getElementsByTagName("tr"));
    }

    @Override
    public HTMLCollection getTBodies() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public HTMLTableSectionElement getTFoot() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public HTMLTableSectionElement getTHead() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public HTMLElement insertRow(int index) throws DOMException {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setCaption(HTMLTableCaptionElement caption) {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setTFoot(HTMLTableSectionElement tFoot) {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setTHead(HTMLTableSectionElement tHead) {
        // To change body of implemented methods use File | Settings | File Templates.
    }
}

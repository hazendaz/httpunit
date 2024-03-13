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
import org.w3c.dom.html.HTMLTableRowElement;

/**
 * @author <a href="mailto:russgold@gmail.com">Russell Gold</a>
 */
public class HTMLTableRowElementImpl extends HTMLElementImpl implements HTMLTableRowElement, AttributeNameAdjusted {

    private static final long serialVersionUID = 1L;

    @Override
    ElementImpl create() {
        return new HTMLTableRowElementImpl();
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
    public String getBgColor() {
        return getAttributeWithNoDefault("bgColor");
    }

    @Override
    public void setBgColor(String bgColor) {
        setAttribute("bgColor", bgColor);
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
    public String getVAlign() {
        return getAttributeWithDefault("valign", "middle");
    }

    @Override
    public void setVAlign(String vAlign) {
        setAttribute("valign", vAlign);
    }

    @Override
    public void deleteCell(int index) throws DOMException {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public HTMLCollection getCells() {
        return HTMLCollectionImpl.createHTMLCollectionImpl(getElementsByTagNames(new String[] { "td", "th " }));
    }

    @Override
    public int getRowIndex() {
        return 0; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getSectionRowIndex() {
        return 0; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public HTMLElement insertCell(int index) throws DOMException {
        return null; // To change body of implemented methods use File | Settings | File Templates.
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

    public void setRowIndex(int rowIndex) {
        // TODO Auto-generated method stub

    }

    public void setSectionRowIndex(int sectionRowIndex) {
        // TODO Auto-generated method stub

    }

    public void setCells(HTMLCollection cells) {
        // TODO Auto-generated method stub

    }
}

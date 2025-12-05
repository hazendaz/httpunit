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

import org.w3c.dom.DOMException;
import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLTableRowElement;

/**
 * The Class HTMLTableRowElementImpl.
 */
public class HTMLTableRowElementImpl extends HTMLElementImpl implements HTMLTableRowElement, AttributeNameAdjusted {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    @Override
    ElementImpl create() {
        return new HTMLTableRowElementImpl();
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
     * Delete cell.
     *
     * @param index
     *            the index
     *
     * @throws DOMException
     *             the DOM exception
     */
    @Override
    public void deleteCell(int index) throws DOMException {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Gets the cells.
     *
     * @return the cells
     */
    @Override
    public HTMLCollection getCells() {
        return HTMLCollectionImpl.createHTMLCollectionImpl(getElementsByTagNames(new String[] { "td", "th " }));
    }

    /**
     * Gets the row index.
     *
     * @return the row index
     */
    @Override
    public int getRowIndex() {
        return 0; // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Gets the section row index.
     *
     * @return the section row index
     */
    @Override
    public int getSectionRowIndex() {
        return 0; // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Insert cell.
     *
     * @param index
     *            the index
     *
     * @return the HTML element
     *
     * @throws DOMException
     *             the DOM exception
     */
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

    /**
     * Sets the row index.
     *
     * @param rowIndex
     *            the new row index
     */
    public void setRowIndex(int rowIndex) {
        // TODO Auto-generated method stub

    }

    /**
     * Sets the section row index.
     *
     * @param sectionRowIndex
     *            the new section row index
     */
    public void setSectionRowIndex(int sectionRowIndex) {
        // TODO Auto-generated method stub

    }

    /**
     * Sets the cells.
     *
     * @param cells
     *            the new cells
     */
    public void setCells(HTMLCollection cells) {
        // TODO Auto-generated method stub

    }
}

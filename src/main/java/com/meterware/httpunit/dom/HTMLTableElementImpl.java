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

import org.w3c.dom.DOMException;
import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLTableCaptionElement;
import org.w3c.dom.html.HTMLTableElement;
import org.w3c.dom.html.HTMLTableSectionElement;

/**
 * The Class HTMLTableElementImpl.
 */
public class HTMLTableElementImpl extends HTMLElementImpl implements HTMLTableElement {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    @Override
    ElementImpl create() {
        return new HTMLTableElementImpl();
    }

    /**
     * Gets the align.
     *
     * @return the align
     */
    @Override
    public String getAlign() {
        return getAttributeWithDefault("align", "center");
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
     * Gets the border.
     *
     * @return the border
     */
    @Override
    public String getBorder() {
        return getAttributeWithNoDefault("border");
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
     * Gets the cell padding.
     *
     * @return the cell padding
     */
    @Override
    public String getCellPadding() {
        return getAttributeWithNoDefault("cellpadding");
    }

    /**
     * Sets the cell padding.
     *
     * @param cellPadding
     *            the new cell padding
     */
    @Override
    public void setCellPadding(String cellPadding) {
        setAttribute("cellpadding", cellPadding);
    }

    /**
     * Gets the cell spacing.
     *
     * @return the cell spacing
     */
    @Override
    public String getCellSpacing() {
        return getAttributeWithNoDefault("cellspacing");
    }

    /**
     * Sets the cell spacing.
     *
     * @param cellSpacing
     *            the new cell spacing
     */
    @Override
    public void setCellSpacing(String cellSpacing) {
        setAttribute("cellspacing", cellSpacing);
    }

    /**
     * Gets the frame.
     *
     * @return the frame
     */
    @Override
    public String getFrame() {
        return getAttributeWithDefault("frame", "void");
    }

    /**
     * Sets the frame.
     *
     * @param frame
     *            the new frame
     */
    @Override
    public void setFrame(String frame) {
        setAttribute("frame", frame);
    }

    /**
     * Gets the rules.
     *
     * @return the rules
     */
    @Override
    public String getRules() {
        return getAttributeWithDefault("rules", "none");
    }

    /**
     * Sets the rules.
     *
     * @param rules
     *            the new rules
     */
    @Override
    public void setRules(String rules) {
        setAttribute("rules", rules);
    }

    /**
     * Gets the summary.
     *
     * @return the summary
     */
    @Override
    public String getSummary() {
        return getAttributeWithNoDefault("summary");
    }

    /**
     * Sets the summary.
     *
     * @param summary
     *            the new summary
     */
    @Override
    public void setSummary(String summary) {
        setAttribute("summary", summary);
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

    /**
     * Creates the caption.
     *
     * @return the HTML element
     */
    @Override
    public HTMLElement createCaption() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Creates the T foot.
     *
     * @return the HTML element
     */
    @Override
    public HTMLElement createTFoot() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Creates the T head.
     *
     * @return the HTML element
     */
    @Override
    public HTMLElement createTHead() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Delete caption.
     */
    @Override
    public void deleteCaption() {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Delete row.
     *
     * @param index
     *            the index
     *
     * @throws DOMException
     *             the DOM exception
     */
    @Override
    public void deleteRow(int index) throws DOMException {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Delete T foot.
     */
    @Override
    public void deleteTFoot() {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Delete T head.
     */
    @Override
    public void deleteTHead() {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Gets the caption.
     *
     * @return the caption
     */
    @Override
    public HTMLTableCaptionElement getCaption() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Gets the rows.
     *
     * @return the rows
     */
    @Override
    public HTMLCollection getRows() {
        return HTMLCollectionImpl.createHTMLCollectionImpl(getElementsByTagName("tr"));
    }

    /**
     * Gets the t bodies.
     *
     * @return the t bodies
     */
    @Override
    public HTMLCollection getTBodies() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Gets the t foot.
     *
     * @return the t foot
     */
    @Override
    public HTMLTableSectionElement getTFoot() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Gets the t head.
     *
     * @return the t head
     */
    @Override
    public HTMLTableSectionElement getTHead() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Insert row.
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
    public HTMLElement insertRow(int index) throws DOMException {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Sets the caption.
     *
     * @param caption
     *            the new caption
     */
    @Override
    public void setCaption(HTMLTableCaptionElement caption) {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Sets the t foot.
     *
     * @param tFoot
     *            the new t foot
     */
    @Override
    public void setTFoot(HTMLTableSectionElement tFoot) {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Sets the t head.
     *
     * @param tHead
     *            the new t head
     */
    @Override
    public void setTHead(HTMLTableSectionElement tHead) {
        // To change body of implemented methods use File | Settings | File Templates.
    }
}

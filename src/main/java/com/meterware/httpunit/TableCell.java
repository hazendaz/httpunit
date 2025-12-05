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
package com.meterware.httpunit;

import java.net.URL;

import org.w3c.dom.html.HTMLTableCellElement;

/**
 * A single cell in an HTML table.
 **/
public class TableCell extends BlockElement {

    /** The element. */
    private HTMLTableCellElement _element;

    /**
     * Returns the number of columns spanned by this cell.
     *
     * @return the col span
     */
    public int getColSpan() {
        return _element.getColSpan();
    }

    /**
     * Returns the number of rows spanned by this cell.
     *
     * @return the row span
     */
    public int getRowSpan() {
        return _element.getRowSpan();
    }

    /**
     * Returns the text value of this cell.
     *
     * @return the string
     *
     * @deprecated as of 1.6, use #getText()
     */
    @Deprecated
    public String asText() {
        return getText();
    }

    // ---------------------------------------- package methods -----------------------------------------

    /**
     * Instantiates a new table cell.
     *
     * @param response
     *            the response
     * @param frame
     *            the frame
     * @param element
     *            the element
     * @param url
     *            the url
     * @param parentTarget
     *            the parent target
     * @param characterSet
     *            the character set
     */
    TableCell(WebResponse response, FrameSelector frame, HTMLTableCellElement element, URL url, String parentTarget,
            String characterSet) {
        super(response, frame, url, parentTarget, element, characterSet);
        _element = element;
    }

}

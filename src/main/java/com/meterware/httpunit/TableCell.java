/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
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

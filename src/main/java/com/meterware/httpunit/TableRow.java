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
package com.meterware.httpunit;

import com.meterware.httpunit.scripting.ScriptableDelegate;

import java.util.ArrayList;

import org.w3c.dom.html.HTMLTableCellElement;
import org.w3c.dom.html.HTMLTableRowElement;

/**
 * The Class TableRow.
 */
public class TableRow extends HTMLElementBase {

    /** The cells. */
    private ArrayList _cells = new ArrayList<>();

    /** The web table. */
    private WebTable _webTable;

    /**
     * Instantiates a new table row.
     *
     * @param webTable
     *            the web table
     * @param element
     *            the element
     */
    TableRow(WebTable webTable, HTMLTableRowElement element) {
        super(element);
        _webTable = webTable;
    }

    /**
     * Gets the cells.
     *
     * @return the cells
     */
    TableCell[] getCells() {

        return (TableCell[]) _cells.toArray(new TableCell[_cells.size()]);
    }

    /**
     * New table cell.
     *
     * @param element
     *            the element
     *
     * @return the table cell
     */
    TableCell newTableCell(HTMLTableCellElement element) {
        return _webTable.newTableCell(element);
    }

    /**
     * Adds the table cell.
     *
     * @param cell
     *            the cell
     */
    void addTableCell(TableCell cell) {
        _cells.add(cell);
    }

    @Override
    public ScriptableDelegate newScriptable() {
        return new HTMLElementScriptable(this);
    }

    @Override
    public ScriptableDelegate getParentDelegate() {
        return _webTable.getParentDelegate();
    }
}

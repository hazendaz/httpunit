/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
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

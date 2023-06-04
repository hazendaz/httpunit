/*
 * MIT License
 *
 * Copyright 2011-2023 Russell Gold
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.html.HTMLTableCellElement;
import org.w3c.dom.html.HTMLTableElement;
import org.w3c.dom.html.HTMLTableRowElement;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 */
class HTMLTableTest extends AbstractHTMLElementTest {

    private Element _body;
    private HTMLTableElement _mainTable;
    private HTMLTableRowElement[] _htmlMainTableRows = new HTMLTableRowElement[3];

    @BeforeEach
    void setUp() throws Exception {
        _body = _htmlDocument.createElement("body");
        _htmlDocument.appendChild(_body);

        _mainTable = (HTMLTableElement) _htmlDocument.createElement("table");
        _body.appendChild(_mainTable);

        for (int i = 0; i < _htmlMainTableRows.length; i++) {
            _htmlMainTableRows[i] = (HTMLTableRowElement) _htmlDocument.createElement("tr");
            _mainTable.appendChild(_htmlMainTableRows[i]);
            for (int j = 0; j < 2; j++) {
                _htmlMainTableRows[i].appendChild(_htmlDocument.createElement("td"));
            }
        }
    }

    /**
     * Verify the construction of table nodes with their attributes.
     */
    @Test
    void testTableNodeCreation() throws Exception {
        doElementTest("td", HTMLTableCellElement.class,
                new Object[][] { { "abbr", "lots" }, { "align", "center" }, { "axis", "age" }, { "bgColor", "red" },
            { "char", ",", "." /* ch */ }, { "charoff", "20" /* charoff */ },
            { "colspan", Integer.valueOf(3), Integer.valueOf(1) }, { "headers", "time,age" },
            { "height", "20" }, { "nowrap", Boolean.TRUE, Boolean.FALSE },
            { "rowspan", Integer.valueOf(15), Integer.valueOf(1) }, { "scope", "row" },
            { "valign", "top", "middle" }, { "width", "10" } });
        doElementTest("th", HTMLTableCellElement.class, new Object[][] { { "abbr", "lots" } });
        doElementTest("tr", HTMLTableRowElement.class, new Object[][] { { "align", "center" }, { "bgColor", "red" },
            { "char", ",", "." /* ch */ }, { "charoff", "20" /* charoff */ }, { "valign", "top", "middle" } });
        doElementTest("table", HTMLTableElement.class,
                new Object[][] { { "align", "right", "center" }, { "bgColor", "red" }, { "border", "2" },
            { "cellpadding", "20" }, { "cellspacing", "20" }, { "frame", "above", "void" },
            { "rules", "groups", "none" }, { "summary", "blah blah" }, { "width", "5" } });
    }

    @Test
    void testReadTable() throws Exception {
        HTMLCollection rows = _mainTable.getRows();
        assertEquals(3, rows.getLength(), "Number of rows in table");
        for (int i = 0; i < 3; i++) {
            Node node = rows.item(i);
            assertTrue(node instanceof HTMLTableRowElement, "Row " + (i + 1) + " is not a table row");
            HTMLCollection cells = ((HTMLTableRowElement) node).getCells();
            assertEquals(2, cells.getLength(), "Number of cells in row");
            for (int j = 0; j < 2; j++) {
                assertTrue(cells.item(j) instanceof HTMLTableCellElement,
                        "Cell (" + (i + 1) + "," + (j + 1) + ") is not a table cell");
            }
        }
    }
}

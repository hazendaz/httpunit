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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.migrationsupport.rules.ExternalResourceSupport;

/**
 * A unit test of the table handling code.
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 * @author <a href="mailto:bx@bigfoot.com">Benoit Xhenseval</a>
 */
@ExtendWith(ExternalResourceSupport.class)
public class HtmlTablesTest extends HttpUnitTest {

    @BeforeEach
    void setUp() throws Exception {
        _wc = new WebConversation();

        defineWebPage("OneTable",
                "<h2>Interesting data</h2>" + "<table summary=\"tough luck\">"
                        + "<tr><th>One</th><td>&nbsp;</td><td>1</td></tr>"
                        + "<tr><td colspan=3><IMG SRC=\"/images/spacer.gif\" ALT=\"\" WIDTH=1 HEIGHT=1></td></tr>"
                        + "<tr><th>Two</th><td>&nbsp;</td><td>2</td></tr>"
                        + "<tr><td colspan=3><IMG SRC=\"/images/spacer.gif\" ALT=\"\" WIDTH=1 HEIGHT=1></td></tr>"
                        + "<tr><th>Three</th><td>&nbsp;</td><td>3</td></tr>" + "</table>");
        defineWebPage("SpanTable",
                "<h2>Interesting data</h2>" + "<table summary=\"tough luck\">"
                        + "<tr><th colspan=2>Colors</th><th>Names</th></tr>"
                        + "<tr><td>Red</td><td rowspan=\"2\"><b>gules</b></td><td>rot</td></tr>"
                        + "<tr><td>Green</td><td><a href=\"nowhere\">vert</a></td></tr>" + "</table>");
    }

    @Test
    void findNoTables() throws Exception {
        defineWebPage("Default", "This has no tables but it does" + "have <a href=\"/other.html\">an active link</A>"
                + " and <a name=here>an anchor</a>");

        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebTable[] tables = page.getTables();
        assertNotNull(tables);
        assertEquals(0, tables.length);
    }

    @Test
    void findOneTable() throws Exception {
        WebTable[] tables = _wc.getResponse(getHostPath() + "/OneTable.html").getTables();
        assertEquals(1, tables.length);
    }

    /**
     * test for patch [ 1117822 ] Patch for purgeEmptyCells() problem by Glen Stampoultzis
     *
     * @throws Exception
     */
    @Test
    void purgeEmptyCells() throws Exception {
        defineWebPage("StrangeSpan", "<h2>Interesting data</h2>"
                + "<table class=\"headerTable\" width=\"97%\" cellspacing=\"2\" cellpadding=\"0\" border=\"0\" id=\"personalTable\">\n"
                + "        <tr>\n"
                + "          <th colspan=\"6\"><img src=\"images/curve-left.gif\" align=\"top\" border=\"0\">Notifications:</th>\n"
                + "        </tr>\n" + "\n" + "<tr> <td width=\"10\">&nbsp;</td>\n"
                + "          <td colspan=\"5\">None</td>\n" + "\n" + "</tr> <tr>\n"
                + "          <th colspan=\"6\"><img src=\"images/curve-left.gif\" align=\"top\" border=\"0\">Watches:</th>\n"
                + "        </tr>\n" + "\n" + "<tr> <td>&nbsp;</td>\n" + "          <td colspan=\"5\">None</td>\n"
                + "</tr> <tr>\n"
                + "          <th colspan=\"6\"><img src=\"images/curve-left.gif\" align=\"top\" border=\"0\">Messages:</th>\n"
                + "\n" + "        </tr>\n" + "\n" + "<tr> <td>&nbsp;</td>\n" + "          <td colspan=\"5\">None</td>\n"
                + "</tr> <tr>\n"
                + "          <th colspan=\"6\"><img src=\"images/curve-left.gif\" align=\"top\" border=\"0\">Favourite Documents:</th>\n"
                + "        </tr>\n" + "\n" + "<tr> <td>&nbsp;</td>\n" + "\n" + "          <td colspan=\"5\">None</td>\n"
                + "</tr>\t</table>");
        WebTable table = _wc.getResponse(getHostPath() + "/StrangeSpan.html").getTables()[0];
        assertNotNull(table);

        assertEquals(6, table.getColumnCount());
        assertEquals(8, table.getRowCount());
        table.purgeEmptyCells();
        assertEquals(2, table.getColumnCount(), "after purging Cells there should be 2 columns left");
        assertEquals(8, table.getRowCount());
        String[][] text = table.asText();
        int row = 0;
        assertEquals("Notifications:", text[row][0]);
        assertEquals("Notifications:", text[row][1]);
        row++;
        assertEquals("", text[row][0]);
        assertEquals("None", text[row][1]);
        row++;
        assertEquals("Watches:", text[row][0]);
        assertEquals("Watches:", text[row][1]);
        row++;
        assertEquals("", text[row][0]);
        assertEquals("None", text[row][1]);
        row++;
        assertEquals("Messages:", text[row][0]);
        assertEquals("Messages:", text[row][1]);
        row++;
        assertEquals("", text[row][0]);
        assertEquals("None", text[row][1]);
        row++;
        assertEquals("Favourite Documents:", text[row][0]);
        assertEquals("Favourite Documents:", text[row][1]);
        row++;
        assertEquals("", text[row][0]);
        assertEquals("None", text[row][1]);
        row++;
    }

    /**
     * test for bug report [ 1295782 ] Method purgeEmptyCells Truncates Table by ahansen 2005-09-19 22:47
     *
     * @throws Exception
     */
    @Test
    void purgeEmptyCells2() throws Exception {
        defineWebPage("BrokenSpan", "<h2>Broken Span</h2>"
                + "<table id=\"testTable\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">" + "   <tr>"
                + "       <td><img src=\"test.jpg\"/></td>" + "       <td colspan=\"2\">h3</td>" + "   </tr>"
                + "   <tr>" + "       <td colspan=\"2\">a</td>" + "       <td>1</td>" + "   </tr>" + "</table>");
        WebResponse page = _wc.getResponse(getHostPath() + "/BrokenSpan.html");
        WebTable table = page.getTables()[0];
        // String expected="WebTable:\n[0]: [0]= [1]=h3 [2]=h3\n[1]: [0]=a [1]=a [2]=1";
        String expected = table.toString();
        table.purgeEmptyCells();
        assertEquals(table.toString(), expected, "1st");
        table.purgeEmptyCells();
        assertEquals(table.toString(), expected, "2nd");
    }

    /**
     * test finding the Table Size
     *
     * @throws Exception
     */
    @Test
    void findTableSize() throws Exception {
        WebTable table = _wc.getResponse(getHostPath() + "/OneTable.html").getTables()[0];
        assertEquals(5, table.getRowCount());
        assertEquals(3, table.getColumnCount());
        try {
            table.getCellAsText(5, 0);
            fail("Should throw out of range exception");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            table.getCellAsText(0, 3);
            fail("Should throw out of range exception");
        } catch (RuntimeException e) {
        }
    }

    @Test
    void findTableCell() throws Exception {
        WebTable table = _wc.getResponse(getHostPath() + "/OneTable.html").getTables()[0];
        assertEquals("Two", table.getCellAsText(2, 0));
        assertEquals("3", table.getCellAsText(4, 2));
    }

    @Test
    void tableAsText() throws Exception {
        WebTable table = _wc.getResponse(getHostPath() + "/OneTable.html").getTables()[0];
        table.purgeEmptyCells();
        String[][] text = table.asText();
        assertEquals(3, text.length, "rows with text");
        assertEquals("Two", text[1][0]);
        assertEquals("3", text[2][1]);
        assertEquals(2, text[0].length, "columns with text");
    }

    @Test
    void nestedTable() throws Exception {
        defineWebPage("Default",
                "<h2>Interesting data</h2>" + "<table summary=\"outer one\">" + "<tr><td>" + "Inner Table<br>"
                        + "<table summary=\"inner one\">" + "        <tr><td>Red</td><td>1</td></tr>"
                        + "        <tr><td>Blue</td><td>2</td></tr>" + "</table></td></tr>" + "</table>");

        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebTable[] tables = page.getTables();
        assertEquals(1, tables.length, "top level tables count");
        assertEquals(1, tables[0].getRowCount(), "rows");
        assertEquals(1, tables[0].getColumnCount(), "columns");
        WebTable[] nested = tables[0].getTableCell(0, 0).getTables();
        assertEquals(1, nested.length, "nested tables count");
        assertEquals(2, nested[0].getRowCount(), "nested rows");
        assertEquals(2, nested[0].getColumnCount(), "nested columns");

        String nestedString = tables[0].getCellAsText(0, 0);
        assertTrue(nestedString.indexOf("Red") >= 0, "Cannot find 'Red' in string");
        assertTrue(nestedString.indexOf("Blue") >= 0, "Cannot find 'Blue' in string");
    }

    @Test
    void columnSpan() throws Exception {
        WebResponse page = _wc.getResponse(getHostPath() + "/SpanTable.html");
        WebTable table = page.getTables()[0];
        assertEquals("Colors", table.getCellAsText(0, 0));
        assertEquals("Colors", table.getCellAsText(0, 1));
        assertEquals("Names", table.getCellAsText(0, 2));
        assertSame(table.getTableCell(0, 0), table.getTableCell(0, 1));
    }

    public static String htmlForBug1043368 = "<HTML>\n" + "<head>\n" + "<title>FormTable Servlet GET</title>\n"
            + "</head>\n<body>\n" + "<FORM METHOD=\"POST\" ACTION=\"/some/action\">\n" + "<TABLE>\n"
            + "   <TR><TD colspan=\"4\">Test Form:</TD></TR>\n\n" + "   <TR>\n" + "       <TD>*Contact Name:</TD>\n"
            + "       <TD><input type=\"text\" size=\"21\" name=\"CONTACT_NAME\" value=\"TIMOTHY O'LEARY\"></TD>\n"
            + "       <TD>Building Number:</TD>\n"
            + "       <TD><input type=\"text\" size=\"7\" name=\"BUILDING_NUMBER\" value=\"355\"></TD>\n" + "   </TR>\n"
            + "</TABLE>\n" + "</FORM>";

    /**
     * test for bug report [ 1043368 ] WebTable has wrong number of columns by AutoTest
     */
    @Test
    void columnNumberInTable() throws Exception {
        defineWebPage("Default", htmlForBug1043368);
        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebTable table = page.getTableStartingWithPrefix("Test Form");
        assertNotNull(table, "didn't find table");
        // System.out.println( table.toString() );
        assertNotNull(-1, "wrong table");
        assertEquals(4, table.getColumnCount(), "wrong column count");
    }

    @Test
    void rowSpan() throws Exception {
        WebResponse page = _wc.getResponse(getHostPath() + "/SpanTable.html");
        WebTable table = page.getTables()[0];
        assertEquals(3, table.getRowCount());
        assertEquals(3, table.getColumnCount());
        assertEquals("gules", table.getCellAsText(1, 1));
        assertEquals("gules", table.getCellAsText(2, 1));
        assertEquals("vert", table.getCellAsText(2, 2));
        assertSame(table.getTableCell(1, 1), table.getTableCell(2, 1));
    }

    @Test
    void missingColumns() throws Exception {
        defineWebPage("Default",
                "<h2>Interesting data</h2>" + "<table summary=\"tough luck\">"
                        + "<tr><th colspan=2>Colors</th><th>Names</th></tr>"
                        + "<tr><td>Red</td><td rowspan=\"2\"><b>gules</b></td></tr>"
                        + "<tr><td>Green</td><td><a href=\"nowhere\">vert</a></td></tr>" + "</table>");

        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebTable table = page.getTables()[0];
        table.purgeEmptyCells();
        assertEquals(3, table.getRowCount());
        assertEquals(3, table.getColumnCount());
    }

    @Test
    void innerTableSeek() throws Exception {
        defineWebPage("Default",
                "<h2>Interesting data</h2>" + "<table id=you summary=\"outer one\">" + "<tr><td>Here we are</td><td>"
                        + "Inner Table 1<br>" + "<table id=you summary='inner zero'>"
                        + "        <tr><td colspan=2>&nbsp;</td></tr>" + "        <tr><td>\nRed\n</td><td>1</td></tr>"
                        + "        <tr><td>Blue</td><td>2</td></tr>" + "</table></td><td>" + "Inner Table 2<br>"
                        + "<table id=me summary=\"inner one\">" + "        <tr><td colspan=2>&nbsp;</td></tr>"
                        + "        <tr><td>Black</td><td>1</td></tr>" + "        <tr><td>White</td><td>2</td></tr>"
                        + "</table></td></tr>" + "</table>");

        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebTable wt = page.getTableStartingWith("Red");
        assertNotNull(wt, "Did not find table starting with 'Red'");
        wt.purgeEmptyCells();
        String[][] cells = wt.asText();
        assertEquals(2, cells.length, "Non-blank rows");
        assertEquals(2, cells[0].length, "Non-blank columns");
        assertEquals("Blue", cells[1][0], "cell at 1,0");

        wt = page.getTableStartingWithPrefix("Re");
        assertNotNull(wt, "Did not find table starting with prefix 'Re'");
        cells = wt.asText();
        assertEquals(2, cells.length, "Non-blank rows");
        assertEquals(2, cells[0].length, "Non-blank columns");
        assertEquals("Blue", cells[1][0], "cell at 1,0");

        wt = page.getTableWithSummary("Inner One");
        assertNotNull(wt, "Did not find table with summary 'Inner One'");
        cells = wt.asText();
        assertEquals(3, cells.length, "Total rows");
        assertEquals(2, cells[0].length, "Total columns");
        assertEquals("White", cells[2][0], "cell at 2,0");

        wt = page.getTableWithID("me");
        assertNotNull(wt, "Did not find table with id 'me'");
        cells = wt.asText();
        assertEquals(3, cells.length, "Total rows");
        assertEquals(2, cells[0].length, "Total columns");
        assertEquals("White", cells[2][0], "cell at 2,0");
    }

    @Test
    void spanOverEmptyColumns() throws Exception {
        defineWebPage("Default",
                "<h2>Interesting data</h2>" + "<table summary=little>"
                        + "<tr><td colspan=2>Title</td><td>Data</td></tr>"
                        + "<tr><td>Name</td><td>&nbsp;</td><td>Value</td></tr>"
                        + "<tr><td>Name</td><td>&nbsp;</td><td>Value</td></tr>" + "</table>");

        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebTable table = page.getTableStartingWith("Title");
        table.purgeEmptyCells();
        String[][] cells = table.asText();
        assertEquals(3, cells.length, "Non-blank rows");
        assertEquals(2, cells[0].length, "Non-blank columns");
        assertEquals("Value", cells[1][1], "cell at 1,1");
    }

    @Test
    void spanOverAllEmptyColumns() throws Exception {
        defineWebPage("Default",
                "<h2>Interesting data</h2>" + "<table summary=little>"
                        + "<tr><td colspan=2>Title</td><td>Data</td></tr>"
                        + "<tr><td>&nbsp;</td><td>&nbsp;</td><td>Value</td></tr>"
                        + "<tr><td>&nbsp;</td><td>&nbsp;</td><td>Value</td></tr>" + "</table>");

        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebTable table = page.getTableStartingWith("Title");
        table.purgeEmptyCells();
        String[][] cells = table.asText();
        assertEquals(3, cells.length, "Non-blank rows");
        assertEquals(2, cells[0].length, "Non-blank columns");
        assertEquals("Value", cells[1][1], "cell at 1,1");
    }

    @Test
    void tableInParagraph() throws Exception {
        defineWebPage("Default", "<p>" + "<table summary=little>" + "<tr><td>a</td><td>b</td><td>Value</td></tr>"
                + "<tr><td>c</td><td>d</td><td>Value</td></tr>" + "</table></p>");

        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        assertEquals(1, page.getTextBlocks()[0].getTables().length, "Number of tables in paragraph");
        assertEquals(1, page.getTables().length, "Number of tables in page");
    }

    /**
     * Get a specific cell with a given id in a WebTable
     */
    @Test
    void cellsWithID() throws Exception {
        defineWebPage("Default",
                "<h2>Interesting data</h2>" + "<table id=\"table\" summary=little>"
                        + "<tr><td>Title</td><td>Data</td></tr>"
                        + "<tr><td id=\"id1\">value1</td><td id=\"id2\">value2</td><td>Value</td></tr>"
                        + "<tr><td>&nbsp;</td><td>&nbsp;</td><td>Value</td></tr>" + "</table>");

        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebTable table = page.getTableWithID("table");
        assertNotNull(table, "there is a table");
        TableCell cell = table.getTableCellWithID("id1");
        assertNotNull(cell, "cell id1");
        assertEquals("value1", cell.getText(), "Value of cell id1");
        cell = table.getTableCellWithID("id2");
        assertNotNull(cell, "cell id2");
        assertEquals("value2", cell.getText(), "Value of cell id2");

        // test non existent cell id
        cell = table.getTableCellWithID("nonExistingID");
        assertNull(cell, "cell id2");

        cell = (TableCell) page.getElementWithID("id1");
        assertEquals("value1", cell.getText(), "value of cell found from page");
    }

    /**
     * Test that the tag name can be extracted for a cell.
     */
    @Test
    void cellTagName() throws Exception {
        WebTable table = _wc.getResponse(getHostPath() + "/OneTable.html").getTables()[0];
        assertEquals("TH", table.getTableCell(0, 0).getTagName().toUpperCase(), "Tag name of header cell");
        assertEquals("TD", table.getTableCell(0, 1).getTagName().toUpperCase(), "Tag name of non-header cell");
    }

    private WebConversation _wc;
}

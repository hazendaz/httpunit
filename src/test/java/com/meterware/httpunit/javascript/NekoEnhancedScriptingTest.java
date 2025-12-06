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
package com.meterware.httpunit.javascript;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.HttpUnitTest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebResponse;

import org.junit.jupiter.api.Test;

/**
 * Tests that work under NekoHTML but not JTidy due to the ability to do script processing during parsing.
 */
class NekoEnhancedScriptingTest extends HttpUnitTest {

    /**
     * Embedded document write.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void embeddedDocumentWrite() throws Exception {
        defineResource("OnCommandWrite.html",
                "<html><head><title>something</title></head>" + "<body>" + "<script language='JavaScript'>"
                        + "document.write( '<a id=here href=about:blank>' );" + "document.writeln( document.title );"
                        + "document.write( '</a>' );" + "</script>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommandWrite.html");
        WebLink link = response.getLinkWithID("here");
        assertNotNull(link, "The link was not found");
        assertEquals("something", link.getText(), "Link contents");
    }

    /**
     * Embedded document write with close.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void embeddedDocumentWriteWithClose() throws Exception {
        defineResource("OnCommand.html",
                "<html><head><title>something</title></head>" + "<body>" + "<script language='JavaScript'>"
                        + "document.write( '<a id=here href=about:blank>' );" + "document.writeln( document.title );"
                        + "document.write( '</a>' );" + "document.close();" + "</script>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        WebLink link = response.getLinkWithID("here");
        assertNotNull(link, "The link was not found");
        assertEquals("something", link.getText(), "Link contents");
    }

    /**
     * Unknown script.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void unknownScript() throws Exception {
        defineWebPage("FunkyScript", "<SCRIPT>" + "var stuff='<A href=\"#\">Default JavaScript Working</A><BR>';"
                + "document.writeln(stuff);" + "</SCRIPT>" + "<SCRIPT Language='JavaScript'>"
                + "var stuff='<A href=\"#\">JavaScript Working</A><BR>';" + "document.writeln(stuff);" + "</SCRIPT>"
                + "<SCRIPT Language='JavaScript1.2'>" + "var stuff='<A href=\"#\">JavaScript 1.2 Working</A><BR>';"
                + "document.writeln(stuff);" + "</SCRIPT>" + "<SCRIPT Language='VBScript'>" + "Dim stuff"
                + "stuff = '<A href=\"#\">VBScript</A><BR>'" + "document.writeln(stuff)" + "</SCRIPT>");
        WebConversation wc = new WebConversation();
        WebResponse wr = wc.getResponse(getHostPath() + "/FunkyScript.html");
        assertNotNull(wr.getLinkWith("Default JavaScript Working"), "No default script link found");
        assertNotNull(wr.getLinkWith("JavaScript Working"), "No default script link found");
        assertNotNull(wr.getLinkWith("JavaScript 1.2 Working"), "No default script link found");
        assertNull(wr.getLinkWith("VBScript"), "VBScript link found");
    }

    /**
     * test no script sections.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void noScriptSections() throws Exception {
        defineResource("OnCommand.html",
                "<html><head><title>something</title></head>" + "<body>" + "<script language='JavaScript'>"
                        + "document.write( '<a id=here href=about:blank>' );" + "document.writeln( document.title );"
                        + "document.write( '</a>' );" + "</script>" + "<noscript>"
                        + "<a href='#' id='there'>anything</a>" + "</noscript>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        WebLink link = response.getLinkWithID("here");
        assertNotNull(link, "The link was not found");
        assertEquals("something", link.getText(), "Link contents");
        assertNull(response.getLinkWithID("there"), "Should not have found link in noscript");

        HttpUnitOptions.setScriptingEnabled(false);
        response = wc.getResponse(getHostPath() + "/OnCommand.html");
        link = response.getLinkWithID("there");
        assertNotNull(link, "The link was not found");
        assertEquals("anything", link.getText(), "Link contents");
        assertNull(response.getLinkWithID("here"), "Should not have found scripted link");
    }

    /**
     * Verifies that nodes defined before a script section are available to that script section, even if a preceding
     * script section has caused them to be cached. Currently does not work with JTidy since there is no way to parse
     * only to a specific position in the document. It may be possible to fix this with some logic changes...
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void formsCaching() throws Exception {
        defineWebPage("OnCommand",
                "<form>" + "  <input type='text' name='color' value='blue' >" + "</form>" + "<script type='JavaScript'>"
                        + "  alert( document.forms[0].color.value );" + "</script>" + "<form>"
                        + "  <input type='text' name='size' value='3' >" + "</form>" + "<script type='JavaScript'>"
                        + "  alert( document.forms[1].size.value );" + "</script>");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals("blue", wc.popNextAlert(), "Message 1");
        assertEquals("3", wc.popNextAlert(), "Message 2");
    }

    /**
     * Verifies that a script can write part of the frameset.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void scriptedFrames() throws Exception {
        assertDoesNotThrow(() -> {
            defineWebPage("OneForm", "<form name='form'><input name=text value='nothing special'></form>");
            defineResource("Frames.html", "<html><script>" + "  document.write( '<frameset>' )" + "</script>"
                    + "    <frame src='OneForm.html' name='green'>" + "    <frame name=blue>" + "</frameset></htmlL>");

            WebConversation wc = new WebConversation();
            wc.getResponse(getHostPath() + "/Frames.html");
            assertMatchingSet("Loaded frames", new String[] { "_top", "green", "blue" }, wc.getFrameNames());
        });
    }

}

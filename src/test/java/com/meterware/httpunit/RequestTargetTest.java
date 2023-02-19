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
package com.meterware.httpunit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.migrationsupport.rules.ExternalResourceSupport;

/**
 * Tests to ensure the proper handling of the target attribute.
 */
@ExtendWith(ExternalResourceSupport.class)
class RequestTargetTest extends HttpUnitTest {

    @BeforeEach
    void setUp() throws Exception {
        _wc = new WebConversation();
    }


    @Test
    void testDefaultLinkTarget() throws Exception {
        defineWebPage("Initial", "Here is a <a href=\"SimpleLink.html\">simple link</a>.");

        WebRequest request = new GetMethodWebRequest(getHostPath() + "/Initial.html");
        assertEquals(WebRequest.TOP_FRAME, request.getTarget(), "new link target");

        WebResponse response = _wc.getResponse(request);
        assertEquals(WebRequest.TOP_FRAME, response.getFrameName(), "default response target");
        WebLink link = response.getLinks()[0];
        assertEquals(WebRequest.TOP_FRAME, link.getTarget(), "default link target");
    }


    @Test
    void testExplicitLinkTarget() throws Exception {
        defineWebPage("Initial", "Here is a <a href=\"SimpleLink.html\" target=\"subframe\">simple link</a>.");

        WebLink link = _wc.getResponse(getHostPath() + "/Initial.html").getLinks()[0];
        assertEquals("subframe", link.getTarget(), "explicit link target");
        assertEquals("subframe", link.getRequest().getTarget(), "request target");
    }


    @Test
    void testInheritedLinkTarget() throws Exception {
        defineResource("Start.html",
                "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" +
                        "<FRAMESET cols=\"50%,50%\">" +
                        "    <FRAME src=\"Initial.html\" name=\"red\">" +
                        "    <FRAME name=\"blue\">" +
                        "</FRAMESET></HTML>");
        defineWebPage("Initial", "Here is a <a href=\"SimpleLink.html\" target=\"blue\">simple link</a>.");
        defineWebPage("SimpleLink", "Here is <a href=\"Initial.html\">another simple link</a>.");

        _wc.getResponse(getHostPath() + "/Start.html");
        WebLink link = _wc.getFrameContents("red").getLinks()[0];
        assertEquals("blue", link.getTarget(), "explicit link target");
        assertEquals("blue", link.getRequest().getTarget(), "request target");

        WebResponse response = _wc.getResponse(link.getRequest());
        assertEquals("blue", response.getFrameName(), "response target");
        link = response.getLinks()[0];
        assertEquals("blue", link.getTarget(), "inherited link target");
    }


    @Test
    void testInheritedLinkTargetInTable() throws Exception {
        defineResource("Start.html",
                "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" +
                        "<FRAMESET cols=\"50%,50%\">" +
                        "    <FRAME src=\"Initial.html\" name=\"red\">" +
                        "    <FRAME name=\"subframe\">" +
                        "</FRAMESET></HTML>");
        defineWebPage("Initial", "Here is a <a href=\"SimpleLink.html\" target=\"subframe\">simple link</a>.");
        defineWebPage("SimpleLink", "Here is <table><tr><td><a href=\"Initial.html\">another simple link</a>.</td></tr></table>");

        WebLink link = _wc.getResponse(getHostPath() + "/Start.html").getSubframeContents("red").getLinks()[0];
        assertEquals("subframe", link.getTarget(), "explicit link target");
        assertEquals("subframe", link.getRequest().getTarget(), "request target");

        WebResponse response = _wc.getResponse(link.getRequest());
        assertEquals("subframe", response.getFrameName(), "response target");
        WebTable table = response.getTables()[0];
        TableCell cell = table.getTableCell(0, 0);
        link = cell.getLinks()[0];
        assertEquals("subframe", link.getTarget(), "inherited link target");
    }


    @Test
    void testDefaultFormTarget() throws Exception {
        defineWebPage("Initial", "Here is a simple form: " +
                "<form method=POST action = \"/servlet/Login\"><B>" +
                "<input type=\"checkbox\" name=first>Disabled" +
                "<br><Input type=submit value = \"Log in\">" +
                "</form>");

        WebResponse response = _wc.getResponse(getHostPath() + "/Initial.html");
        assertEquals(1, response.getForms().length, "Num forms in page");
        WebForm form = response.getForms()[0];
        assertEquals(WebRequest.TOP_FRAME, form.getTarget(), "default form target");
    }


    @Test
    void testExplicitPostFormTarget() throws Exception {
        defineWebPage("Initial", "Here is a simple form: " +
                "<form method=POST action = \"/servlet/Login\" target=\"subframe\"><B>" +
                "<input type=\"checkbox\" name=first>Disabled" +
                "<br><Input type=submit value = \"Log in\">" +
                "</form>");

        WebForm form = _wc.getResponse(getHostPath() + "/Initial.html").getForms()[0];
        assertEquals("subframe", form.getTarget(), "explicit form target");
        assertEquals("subframe", form.getRequest().getTarget(), "request target");
    }


    @Test
    void testExplicitGetFormTarget() throws Exception {
        defineWebPage("Initial", "Here is a simple form: " +
                "<form method=GET action = \"/servlet/Login\" target=\"subframe\"><B>" +
                "<input type=\"checkbox\" name=first>Disabled" +
                "<br><Input type=submit value = \"Log in\">" +
                "</form>");

        WebForm form = _wc.getResponse(getHostPath() + "/Initial.html").getForms()[0];
        assertEquals("subframe", form.getTarget(), "explicit form target");
        assertEquals("subframe", form.getRequest().getTarget(), "request target");
    }


    @Test
    void testInheritedFormTarget() throws Exception {
        defineResource("Start.html",
                "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" +
                        "<FRAMESET cols=\"50%,50%\">" +
                        "    <FRAME src=\"Initial.html\" name=\"red\">" +
                        "    <FRAME name=\"subframe\">" +
                        "</FRAMESET></HTML>");
        defineWebPage("Initial", "Here is a <a href=\"SimpleLink.html\" target=\"subframe\">simple link</a>.");
        defineWebPage("SimpleLink", "Here is a simple form: " +
                "<form method=GET action = \"/servlet/Login\" target=\"subframe\"><B>" +
                "<input type=\"checkbox\" name=first>Disabled" +
                "<br><Input type=submit value = \"Log in\">" +
                "</form>");

        WebLink link = _wc.getResponse(getHostPath() + "/Start.html").getSubframeContents("red").getLinks()[0];
        assertEquals("subframe", link.getTarget(), "explicit link target");
        assertEquals("subframe", link.getRequest().getTarget(), "request target");

        WebResponse response = _wc.getResponse(link.getRequest());
        assertEquals("subframe", response.getFrameName(), "response target");
        WebForm form = response.getForms()[0];
        assertEquals("subframe", form.getTarget(), "inherited form target");
    }


    private WebConversation _wc;
}

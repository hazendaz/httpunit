/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests to ensure the proper handling of the target attribute.
 */
class RequestTargetTest extends HttpUnitTest {

    /**
     * Sets the up.
     *
     * @throws Exception
     *             the exception
     */
    @BeforeEach
    void setUp() throws Exception {
        _wc = new WebConversation();
    }

    /**
     * Default link target.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void defaultLinkTarget() throws Exception {
        defineWebPage("Initial", "Here is a <a href=\"SimpleLink.html\">simple link</a>.");

        WebRequest request = new GetMethodWebRequest(getHostPath() + "/Initial.html");
        assertEquals(WebRequest.TOP_FRAME, request.getTarget(), "new link target");

        WebResponse response = _wc.getResponse(request);
        assertEquals(WebRequest.TOP_FRAME, response.getFrameName(), "default response target");
        WebLink link = response.getLinks()[0];
        assertEquals(WebRequest.TOP_FRAME, link.getTarget(), "default link target");
    }

    /**
     * Explicit link target.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void explicitLinkTarget() throws Exception {
        defineWebPage("Initial", "Here is a <a href=\"SimpleLink.html\" target=\"subframe\">simple link</a>.");

        WebLink link = _wc.getResponse(getHostPath() + "/Initial.html").getLinks()[0];
        assertEquals("subframe", link.getTarget(), "explicit link target");
        assertEquals("subframe", link.getRequest().getTarget(), "request target");
    }

    /**
     * Inherited link target.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void inheritedLinkTarget() throws Exception {
        defineResource("Start.html", "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" + "<FRAMESET cols=\"50%,50%\">"
                + "    <FRAME src=\"Initial.html\" name=\"red\">" + "    <FRAME name=\"blue\">" + "</FRAMESET></HTML>");
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

    /**
     * Inherited link target in table.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void inheritedLinkTargetInTable() throws Exception {
        defineResource("Start.html",
                "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" + "<FRAMESET cols=\"50%,50%\">"
                        + "    <FRAME src=\"Initial.html\" name=\"red\">" + "    <FRAME name=\"subframe\">"
                        + "</FRAMESET></HTML>");
        defineWebPage("Initial", "Here is a <a href=\"SimpleLink.html\" target=\"subframe\">simple link</a>.");
        defineWebPage("SimpleLink",
                "Here is <table><tr><td><a href=\"Initial.html\">another simple link</a>.</td></tr></table>");

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

    /**
     * Default form target.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void defaultFormTarget() throws Exception {
        defineWebPage("Initial",
                "Here is a simple form: " + "<form method=POST action = \"/servlet/Login\"><B>"
                        + "<input type=\"checkbox\" name=first>Disabled" + "<br><Input type=submit value = \"Log in\">"
                        + "</form>");

        WebResponse response = _wc.getResponse(getHostPath() + "/Initial.html");
        assertEquals(1, response.getForms().length, "Num forms in page");
        WebForm form = response.getForms()[0];
        assertEquals(WebRequest.TOP_FRAME, form.getTarget(), "default form target");
    }

    /**
     * Explicit post form target.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void explicitPostFormTarget() throws Exception {
        defineWebPage("Initial",
                "Here is a simple form: " + "<form method=POST action = \"/servlet/Login\" target=\"subframe\"><B>"
                        + "<input type=\"checkbox\" name=first>Disabled" + "<br><Input type=submit value = \"Log in\">"
                        + "</form>");

        WebForm form = _wc.getResponse(getHostPath() + "/Initial.html").getForms()[0];
        assertEquals("subframe", form.getTarget(), "explicit form target");
        assertEquals("subframe", form.getRequest().getTarget(), "request target");
    }

    /**
     * Explicit get form target.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void explicitGetFormTarget() throws Exception {
        defineWebPage("Initial",
                "Here is a simple form: " + "<form method=GET action = \"/servlet/Login\" target=\"subframe\"><B>"
                        + "<input type=\"checkbox\" name=first>Disabled" + "<br><Input type=submit value = \"Log in\">"
                        + "</form>");

        WebForm form = _wc.getResponse(getHostPath() + "/Initial.html").getForms()[0];
        assertEquals("subframe", form.getTarget(), "explicit form target");
        assertEquals("subframe", form.getRequest().getTarget(), "request target");
    }

    /**
     * Inherited form target.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void inheritedFormTarget() throws Exception {
        defineResource("Start.html",
                "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" + "<FRAMESET cols=\"50%,50%\">"
                        + "    <FRAME src=\"Initial.html\" name=\"red\">" + "    <FRAME name=\"subframe\">"
                        + "</FRAMESET></HTML>");
        defineWebPage("Initial", "Here is a <a href=\"SimpleLink.html\" target=\"subframe\">simple link</a>.");
        defineWebPage("SimpleLink",
                "Here is a simple form: " + "<form method=GET action = \"/servlet/Login\" target=\"subframe\"><B>"
                        + "<input type=\"checkbox\" name=first>Disabled" + "<br><Input type=submit value = \"Log in\">"
                        + "</form>");

        WebLink link = _wc.getResponse(getHostPath() + "/Start.html").getSubframeContents("red").getLinks()[0];
        assertEquals("subframe", link.getTarget(), "explicit link target");
        assertEquals("subframe", link.getRequest().getTarget(), "request target");

        WebResponse response = _wc.getResponse(link.getRequest());
        assertEquals("subframe", response.getFrameName(), "response target");
        WebForm form = response.getForms()[0];
        assertEquals("subframe", form.getTarget(), "inherited form target");
    }

    /** The wc. */
    private WebConversation _wc;
}

/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.javascript;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.meterware.httpunit.HttpUnitTest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

/**
 * The Class EventHandlingTest.
 */
class EventHandlingTest extends HttpUnitTest {

    /** The wc. */
    private WebConversation _wc;

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
     * add a resource with the given name and title defining the given javaScript and content.
     *
     * @param name
     *            the name
     * @param title
     *            the title
     * @param onLoad
     *            the on load
     * @param javaScript
     *            the java script
     * @param content
     *            the content
     */
    private void addResource(String name, String title, String onLoad, String javaScript, String content) {
        if (onLoad == null) {
            onLoad = "";
        }
        if (!onLoad.equals("")) {
            onLoad = " onload='" + onLoad + "'";
        }
        defineResource(name + ".html",
                "<html>\n\t<head>\n\t\t<title>" + title + "</title>\n" + "\t\t<script type='text/javascript'>\n"
                        + javaScript + "\t\t</script>\n\t</head>\n" + "\t<body" + onLoad + ">\n\t\t" + content
                        + "\n\t</body>\n</html>");
    }

    /**
     * get the response for the resource with the given name.
     *
     * @param name
     *            the name
     *
     * @return the response
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws SAXException
     *             the SAX exception
     */
    private WebResponse getResponse(String name) throws IOException, SAXException {
        return _wc.getResponse(getHostPath() + "/" + name + ".html");
    }

    /**
     * test for [ 1163753 ] partial patch for bug 771335 (DOM2 Events support) by Rafael Krzewski.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws SAXException
     *             the SAX exception
     */
    @Test
    void simpleEventHandler() throws IOException, SAXException {
        String javaScript = "			function testEventHandler() {\n"
                + "				if (document.addEventListener) {\n"
                + "					alert('found addEventListener');\n" + "				}\n" + "			}\n";
        String onLoad = "testEventHandler()";
        String content = "";
        String name = "simple1";
        addResource(name, "only check addEventListener function available", onLoad, javaScript, content);
        getResponse(name);
        // System.out.println(response.getText());
        String alert = _wc.popNextAlert();
        assertEquals("found addEventListener", alert);
    }

    // FIXME need more tests for event handling

}

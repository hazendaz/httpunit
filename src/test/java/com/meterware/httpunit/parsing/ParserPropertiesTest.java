/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpUnitTest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This test checks certain customizable behaviors of the HTML parsers. Not every parser implements every behavior.
 */
public class ParserPropertiesTest extends HttpUnitTest {

    /**
     * Tear down.
     *
     * @throws Exception
     *             the exception
     */
    @AfterEach
    void tearDown() throws Exception {
        HTMLParserFactory.reset();
    }

    /**
     * verify the upper/lower case handling.
     *
     * @param wc
     *            the wc
     * @param request
     *            the request
     * @param boldNodeContents
     *            the bold node contents
     * @param tagName
     *            - the tagName to look for
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws SAXException
     *             the SAX exception
     */
    private void verifyMatchingBoldNodes(WebConversation wc, WebRequest request, String[] boldNodeContents,
            String tagName) throws IOException, SAXException {
        WebResponse simplePage = wc.getResponse(request);
        Document doc = simplePage.getDOM();
        NodeList nlist = doc.getElementsByTagName(tagName);
        assertEquals(boldNodeContents.length, nlist.getLength(), "Number of nodes with tag '" + tagName + "':");
        for (int i = 0; i < nlist.getLength(); i++) {
            assertEquals(boldNodeContents[i], nlist.item(i).getFirstChild().getNodeValue(), "Element " + i);
        }
    }

    /**
     * verify the upper/lower case handling.
     *
     * @param wc
     *            the wc
     * @param request
     *            the request
     * @param boldNodeContents
     *            the bold node contents
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws SAXException
     *             the SAX exception
     */
    private void verifyMatchingBoldNodes(WebConversation wc, WebRequest request, String[] boldNodeContents)
            throws IOException, SAXException {
        verifyMatchingBoldNodes(wc, request, boldNodeContents, "b");
    }

    /** shared by all tests. */
    private WebConversation wc = null;

    /** The request. */
    private WebRequest request = null;

    /**
     * same page for all tests.
     *
     * @throws Exception
     *             the exception
     */
    public void prepareTestCase() throws Exception {
        defineResource("SimplePage.html",
                "<HTML><head><title>A Sample Page</title></head>\n" + "<body>This has no forms but it does\n"
                        + "have <a href=\"/other.html\">an <b>active</b> link</A>\n"
                        + " and <a name=here>an <B>anchor</B></a>\n" + "</body></HTML>\n");
        wc = new WebConversation();
        request = new GetMethodWebRequest(getHostPath() + "/SimplePage.html");
    }

    /**
     * test the preserveTagCase configuration feature ofh the HTMLParserFactory.
     *
     * @param preserveTagCase
     *            the preserve tag case
     * @param expected1
     *            the expected 1
     * @param expected2
     *            the expected 2
     *
     * @throws Exception
     *             the exception
     */
    public void doTestKeepCase(boolean preserveTagCase, String[] expected1, String[] expected2) throws Exception {
        prepareTestCase();
        verifyMatchingBoldNodes(wc, request, expected1);
        HTMLParserFactory.setPreserveTagCase(preserveTagCase);
        verifyMatchingBoldNodes(wc, request, expected2);
    }

    /**
     * test the keepcase setting.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    @Disabled
    void keepCase() throws Exception {
        doTestKeepCase(true, new String[] { "active", "anchor" }, new String[] { "active" });
    }

    /**
     * test for patch [ 1211154 ] NekoDOMParser default to lowercase by Dan Allen.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void lowerCase() throws Exception {
        doTestKeepCase(false, new String[] { "active", "anchor" }, new String[] { "active", "anchor" });
    }

    /**
     * test for patch [ 1176688 ] Allow configuration of neko parser properties by james abley.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    @Disabled
    void forceUpperCase() throws Exception {
        prepareTestCase();
        assertFalse(HTMLParserFactory.getForceUpperCase());
        verifyMatchingBoldNodes(wc, request, new String[] { "active", "anchor" }, "B");
        HTMLParserFactory.setForceUpperCase(true);
        verifyMatchingBoldNodes(wc, request, new String[] { "active", "anchor" }, "B");
        verifyMatchingBoldNodes(wc, request, new String[0], "b");
    }

    /**
     * test for patch [ 1176688 ] Allow configuration of neko parser properties by james abley.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    @Disabled
    void forceLowerCase() throws Exception {
        prepareTestCase();
        assertFalse(HTMLParserFactory.getForceLowerCase());
        verifyMatchingBoldNodes(wc, request, new String[] { "active", "anchor" }, "b");
        HTMLParserFactory.setForceLowerCase(true);
        verifyMatchingBoldNodes(wc, request, new String[] { "active", "anchor" }, "b");
        verifyMatchingBoldNodes(wc, request, new String[0], "B");
    }

}

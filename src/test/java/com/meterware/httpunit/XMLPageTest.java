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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * A test for the XML handling functionality.
 */
class XMLPageTest extends HttpUnitTest {

    /**
     * Xml.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void xml() throws Exception {
        assertDoesNotThrow(() -> {
            defineResource("SimplePage.xml", "<?xml version=\"1.0\" ?><main><title>See me now</title></main>",
                    "text/xml");

            WebConversation wc = new WebConversation();
            WebRequest request = new GetMethodWebRequest(getHostPath() + "/SimplePage.xml");
            WebResponse simplePage = wc.getResponse(request);
            simplePage.getDOM();
        });
    }

    /**
     * test case for BR [2373755] by Frank Waldheim deactivated since it is the opposite of 1281655.
     *
     * @throws Exception
     *             the exception
     */
    @Disabled
    public void testXMLisHTML() throws Exception {
        String originalXml = "<?xml version=\"1.0\" ?><main><title>See me now</title></main>";
        defineResource("SimplePage.xml", originalXml, "text/xml");
        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest(getHostPath() + "/SimplePage.xml");
        WebResponse simplePage = wc.getResponse(request);
        // we do not have an html result
        assertFalse(simplePage.isHTML(), "xml result is not HTML");
        // get the main element as root
        assertNotNull(simplePage.getDOM().getDocumentElement(), "we do have an root-element");
        assertEquals("main", simplePage.getDOM().getDocumentElement().getTagName(),
                "the actual root must be the root of our test-xml");
    }

    /**
     * test for BR 2946821.
     *
     * @return the document element
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws SAXException
     *             the SAX exception
     */
    @Test
    void getDocumentElement() throws IOException, SAXException {
        String html = "<html><body></body></html>";
        defineResource("BR2946821.html", html, "text/html");
        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest(getHostPath() + "/BR2946821.html");
        WebResponse page = wc.getResponse(request);
        assertTrue(page.isHTML());
        Document doc = page.getDOM();
        Element docElement = doc.getDocumentElement();
        assertNotNull(docElement, "There should be a root element");
    }

    /**
     * Traversal.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void traversal() throws Exception {
        defineResource("SimplePage.xml",
                "<?xml version='1.0' ?><zero><main><first><second/></first><main><normal/><simple/></main><after/></main><end/></zero>",
                "text/xml");
        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest(getHostPath() + "/SimplePage.xml");
        WebResponse simplePage = wc.getResponse(request);
        NodeUtils.PreOrderTraversal pot = new NodeUtils.PreOrderTraversal(simplePage.getDOM());
        final StringBuilder sb = new StringBuilder();
        pot.perform(new NodeUtils.NodeAction() {
            @Override
            public boolean processElement(NodeUtils.PreOrderTraversal traversal, Element element) {
                if (element.getNodeName().equalsIgnoreCase("main")) {
                    traversal.pushContext("x");
                } else {
                    for (Iterator i = traversal.getContexts(); i.hasNext();) {
                        sb.append(i.next());
                    }
                    sb.append(element.getNodeName()).append("|");
                }
                return true;
            }

            @Override
            public void processTextNode(NodeUtils.PreOrderTraversal traversal, Node textNode) {
            }
        });
        // pre [ 1281655 ] [patch] result
        String expected = "zero|xfirst|xsecond|xxnormal|xxsimple|xafter|end|";
        // new result
        // expected="HTML|HEAD|ZERO|xFIRST|xSECOND|xxNORMAL|xxSIMPLE|xAFTER|END|";
        String got = sb.toString().toLowerCase(Locale.ENGLISH);
        assertTrue(got.endsWith(expected), "Traversal result");
    }
}

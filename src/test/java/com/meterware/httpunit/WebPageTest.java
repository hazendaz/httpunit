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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.migrationsupport.rules.ExternalResourceSupport;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLDocument;
import org.xml.sax.SAXException;

/**
 * Unit tests for page structure, style, and headers.
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 * @author <a href="mailto:bx@bigfoot.com">Benoit Xhenseval</a>
 */
@ExtendWith(ExternalResourceSupport.class)
class WebPageTest extends HttpUnitTest {

    @Test
    void noResponse() throws Exception {
        WebConversation wc = new WebConversation();
        try {
            WebRequest request = new GetMethodWebRequest(getHostPath() + "/NonExistentSimplePage.html");
            wc.getResponse(request);
            fail("Did not complain about missing page");
        } catch (HttpNotFoundException e) {
        }
    }

    @Test
    void proxyServerAccess() throws Exception {
        defineResource("http://someserver.com/sample", "Get this", "text/plain");
        WebConversation wc = new WebConversation();
        try {
            wc.setProxyServer("localhost", getHostPort());
            WebResponse wr = wc.getResponse("http://someserver.com/sample");
            String result = wr.getText();
            assertEquals("Get this", result.trim(), "Expected text");
        } finally {
            wc.clearProxyServer();
        }
    }

    /**
     * check the valid contentTypes modified for bug report [ 1281655 ] [patch] allow text/xml to be parsed as html by
     * fabrizio giustina
     *
     * @throws Exception
     */

    @Test
    void htmlRequirement() throws Exception {
        defineResource("TextPage.txt", "Just text", "text/plain");
        defineResource("SimplePage.html",
                "<html><head><title>A Sample Page</title></head><body>Something here</body></html>", "text/html");
        defineResource("StructuredPage.html",
                "<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML Basic 1.0//EN' 'http://www.w3.org/TR/xhtml-basic/xhtml-basic10.dtd'>"
                        + "<html><head><title>A Structured Page</title></head><body>Something here</body></html>",
                "text/xhtml");
        defineResource("XHTMLPage.html",
                "<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML Basic 1.0//EN' 'http://www.w3.org/TR/xhtml-basic/xhtml-basic10.dtd'>"
                        + "<html><head><title>An XHTML Page</title></head><body>Something here</body></html>",
                "application/xhtml+xml");
        // see http://www.xmlrpc.com/spec
        defineResource("XMLRPC.html",
                "<?xml version=\"1.0\"?><methodCall><methodName>stock.getQuote</methodName><params><param><value>JAVA</value></param></params></methodCall>",
                "text/xml");
        WebConversation wc = new WebConversation();
        HttpUnitOptions.setCheckHtmlContentType(true);
        try {
            wc.getResponse(getHostPath() + "/TextPage.txt").getReceivedPage().getTitle();
            fail("Should have rejected attempt to get a title from a text page");
        } catch (NotHTMLException e) {
        }

        WebResponse simplePage = wc.getResponse(getHostPath() + "/SimplePage.html");
        assertEquals("A Sample Page", simplePage.getReceivedPage().getTitle(), "HTML Title");

        WebResponse structuredPage = wc.getResponse(getHostPath() + "/StructuredPage.html");
        assertEquals("A Structured Page", structuredPage.getReceivedPage().getTitle(), "XHTML Title");
        Document root = structuredPage.getDOM();
        assertTrue(root != null, "document root of structured  Page should be available");

        WebResponse xhtmlPage = wc.getResponse(getHostPath() + "/XHTMLPage.html");
        assertEquals("An XHTML Page", xhtmlPage.getReceivedPage().getTitle(), "XHTML Title");

        WebResponse xmlrpcPage = wc.getResponse(getHostPath() + "/XMLRPC.html");
        root = xmlrpcPage.getDOM();
        assertTrue(root != null, "document root of xml RPC page should be available");
        NodeList elements = root.getElementsByTagName("methodCall");
        assertEquals(1, elements.getLength(), "there should be one methodCall node");

    }

    /**
     * Verify that even if a page does not claim to be HTML, that we can treat it as whatever we like.
     *
     * @throws Exception
     *             if an unexpected exception occurs during the test.
     */
    @Test
    void forceAsHtml() throws Exception {
        HttpUnitOptions.setCheckHtmlContentType(true);
        defineResource("SimplePage.html",
                "<html><head><title>A Sample Page</title></head><body>Something here</body></html>", "text");
        WebConversation wc = new WebConversation();
        try {
            wc.getResponse(getHostPath() + "/SimplePage.html").getReceivedPage().getTitle();
            fail("should have complained that the page is not HTML");
        } catch (NotHTMLException e) {
        }

        wc.getClientProperties().setOverrideContextType("text/html");
        WebResponse simplePage = wc.getResponse(getHostPath() + "/SimplePage.html");
        assertEquals("A Sample Page", simplePage.getReceivedPage().getTitle(), "HTML Title");
    }

    @Test
    void htmlDocument() throws Exception {
        defineWebPage("SimplePage", "This has no forms but it does\n"
                + "have <a href=\"/other.html\">an <b>active</b> link</A>\n" + " and <a name=here>an anchor</a>\n"
                + "<a href=\"basic.html\"><IMG SRC=\"/images/arrow.gif\" ALT=\"Next -->\" WIDTH=1 HEIGHT=4></a>\n");
        WebConversation wc = new WebConversation();
        WebResponse simplePage = wc.getResponse(getHostPath() + "/SimplePage.html");
        Document dom = simplePage.getDOM();
        assertNotNull(dom, "No DOM created for document");
        assertTrue(dom instanceof HTMLDocument,
                "returned dom does not implement HTMLDocument, but is " + dom.getClass().getName());
    }

    /**
     * add test for HeadMethodWebRequest
     *
     * @throws Exception
     *
     * @author Dan Lipofsky 2009-08-19
     */
    @Test
    void headMethodWebRequest() throws Exception {
        defineResource("SimplePage.html",
                "<html><head><title>A Sample Page</title></head>\n" + "<body>Hello</body></html>\n");
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(true);
        WebConversation wc = new WebConversation();
        // create a HeadMethodWebRequest
        // see http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html for definition
        WebRequest request = new HeadMethodWebRequest(getHostPath() + "/SimplePage.html");
        WebResponse simplePage = wc.getResponse(request);
        String text = simplePage.getText();
        // no body should be returned
        assertEquals("", text);
    }

    // @Ignore
    @Test
    void title() throws Exception {
        defineResource("/SimpleTitlePage.html", "<html><head><title>A Sample Page</title></head>\n"
                + "<body>This has no forms but it does\n" + "have <a href=\"/other.html\">an <b>active</b> link</A>\n"
                + " and <a name=here>an anchor</a>\n"
                + "<a href=\"basic.html\"><IMG SRC=\"/images/arrow.gif\" ALT=\"Next -->\" WIDTH=1 HEIGHT=4></a>\n"
                + "</body></html>\n");
        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest(getHostPath() + "/SimpleTitlePage.html");
        WebResponse simplePage = wc.getResponse(request);
        assertEquals("A Sample Page", simplePage.getTitle(), "Title");
        assertEquals("iso-8859-1", simplePage.getCharacterSet(), "Character set");
        assertNull(simplePage.getRefreshRequest(), "No refresh request should have been found");
    }

    @Test
    void localFile() throws Exception {
        File file = new File("temp.html");
        FileWriter fw = new FileWriter(file);
        PrintWriter pw = new PrintWriter(fw);
        pw.println("<html><head><title>A Sample Page</title></head>");
        pw.println("<body>This is a very simple page<p>With not much text</body></html>");
        pw.close();

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest("file:" + file.getAbsolutePath());
        WebResponse simplePage = wc.getResponse(request);
        assertEquals("A Sample Page", simplePage.getTitle(), "Title");
        assertEquals(Charset.defaultCharset().displayName(), simplePage.getCharacterSet(), "Character set");

        file.delete();
    }

    @Test
    void noLocalFile() throws Exception {
        File file = new File("temp.html");
        file.delete();

        try {
            WebConversation wc = new WebConversation();
            WebRequest request = new GetMethodWebRequest("file:" + file.getAbsolutePath());
            wc.getResponse(request);
            fail("Should have complained about missing file");
        } catch (java.io.FileNotFoundException e) {
        }

    }

    @Test
    void refreshHeader() throws Exception {
        String refreshURL = getHostPath() + "/NextPage.html";
        String page = "<html><head><title>Sample</title></head>\n" + "<body>This has no data\n" + "</body></html>\n";
        defineResource("SimplePage.html", page);
        addResourceHeader("SimplePage.html", "Refresh: 2;URL=NextPage.html");

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest(getHostPath() + "/SimplePage.html");
        WebResponse simplePage = wc.getResponse(request);

        assertNotNull(simplePage.getRefreshRequest(), "No Refresh header found");
        assertEquals(refreshURL, simplePage.getRefreshRequest().getURL().toExternalForm(), "Refresh URL");
        assertEquals(2, simplePage.getRefreshDelay(), "Refresh delay");
    }

    @Test
    void metaRefreshRequest() throws Exception {
        String refreshURL = getHostPath() + "/NextPage.html";
        String page = "<html><head><title>Sample</title>" + "<meta Http_equiv=refresh content='2;\"" + refreshURL
                + "\"'></head>\n" + "<body>This has no data\n" + "</body></html>\n";
        defineResource("SimplePage.html", page);

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest(getHostPath() + "/SimplePage.html");
        WebResponse simplePage = wc.getResponse(request);

        assertEquals(refreshURL, simplePage.getRefreshRequest().getURL().toExternalForm(), "Refresh URL");
        assertEquals(2, simplePage.getRefreshDelay(), "Refresh delay");
    }

    @Test
    void metaRefreshURLRequest() throws Exception {
        String refreshURL = getHostPath() + "/NextPage.html";
        String page = "<html><head><title>Sample</title>"
                + "<meta Http-equiv=refresh content='2;URL=\"NextPage.html\"'></head>\n" + "<body>This has no data\n"
                + "</body></html>\n";
        defineResource("SimplePage.html", page);

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest(getHostPath() + "/SimplePage.html");
        WebResponse simplePage = wc.getResponse(request);

        assertEquals(refreshURL, simplePage.getRefreshRequest().getURL().toExternalForm(), "Refresh URL");
        assertEquals(2, simplePage.getRefreshDelay(), "Refresh delay");
    }

    @Test
    void metaRefreshAbsoluteURLRequestWithAmpersandEncoding() throws Exception {
        String refreshURL = "http://localhost:8080/someapp/secure/?username=abc&somevalue=abc";
        String page = "<html><head><title>Sample</title>"
                + "<meta Http-equiv=refresh content='2;URL=\"http://localhost:8080/someapp/secure/?username=abc&amp;somevalue=abc\"'></head>\n"
                + "<body>This has no data\n" + "</body></html>\n";
        defineResource("SimplePage.html", page);

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest(getHostPath() + "/SimplePage.html");
        WebResponse simplePage = wc.getResponse(request);

        assertEquals(refreshURL, simplePage.getRefreshRequest().getURL().toExternalForm(), "Refresh URL");
        assertEquals(2, simplePage.getRefreshDelay(), "Refresh delay");
    }

    @Test
    void metaRefreshURLRequestNoDelay() throws Exception {
        String refreshURL = getHostPath() + "/NextPage.html";
        String page = "<html><head><title>Sample</title>"
                + "<meta Http-equiv=refresh content='URL=\"NextPage.html\"'></head>\n" + "<body>This has no data\n"
                + "</body></html>\n";
        defineResource("SimplePage.html", page);

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest(getHostPath() + "/SimplePage.html");
        WebResponse simplePage = wc.getResponse(request);

        assertEquals(refreshURL, simplePage.getRefreshRequest().getURL().toExternalForm(), "Refresh URL");
        assertEquals(0, simplePage.getRefreshDelay(), "Refresh delay");
    }

    @Test
    void metaRefreshURLRequestDelayOnly() throws Exception {
        String refreshURL = getHostPath() + "/SimplePage.html";
        String page = "<html><head><title>Sample</title>" + "<meta Http-equiv=refresh content='5'></head>\n"
                + "<body>This has no data\n" + "</body></html>\n";
        defineResource("SimplePage.html", page);

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest(getHostPath() + "/SimplePage.html");
        WebResponse simplePage = wc.getResponse(request);

        assertEquals(refreshURL, simplePage.getRefreshRequest().getURL().toExternalForm(), "Refresh URL");
        assertEquals(5, simplePage.getRefreshDelay(), "Refresh delay");
    }

    @Test
    void autoRefresh() throws Exception {
        String refreshURL = getHostPath() + "/NextPage.html";
        String page = "<html><head><title>Sample</title>" + "<meta Http_equiv=refresh content='2;" + refreshURL
                + "'></head>\n" + "<body>This has no data\n" + "</body></html>\n";
        defineResource("SimplePage.html", page);
        defineWebPage("NextPage", "Not much here");

        WebConversation wc = new WebConversation();
        wc.getClientProperties().setAutoRefresh(true);
        WebRequest request = new GetMethodWebRequest(getHostPath() + "/SimplePage.html");
        WebResponse simplePage = wc.getResponse(request);

        assertNull(simplePage.getRefreshRequest(), "No refresh request should have been found");
    }

    /**
     * Test the meta tag content retrieval
     */
    @Test
    void metaTag() throws Exception {
        assertDoesNotThrow(() -> {
            String page = "<html><head><title>Sample</title>" + "<meta Http-equiv=\"Expires\" content=\"now\"/>\n"
                    + "<meta name=\"robots\" content=\"index,follow\"/>" + "<meta name=\"keywords\" content=\"test\"/>"
                    + "<meta name=\"keywords\" content=\"demo\"/>" + "</head>\n" + "<body>This has no data\n"
                    + "</body></html>\n";
            defineResource("SimplePage.html", page);

            WebConversation wc = new WebConversation();
            WebRequest request = new GetMethodWebRequest(getHostPath() + "/SimplePage.html");
            WebResponse simplePage = wc.getResponse(request);

            assertMatchingSet("robots meta tag", new String[] { "index,follow" },
                    simplePage.getMetaTagContent("name", "robots"));
            assertMatchingSet("keywords meta tag", new String[] { "test", "demo" },
                    simplePage.getMetaTagContent("name", "keywords"));
            assertMatchingSet("Expires meta tag", new String[] { "now" },
                    simplePage.getMetaTagContent("http-equiv", "Expires"));
        });
    }

    /**
     * test the stylesheet retrieval
     */
    @Test
    void getExternalStylesheet() throws Exception {
        String page = "<html><head><title>Sample</title>" + "<link rev=\"made\" href=\"/Me@mycompany.com\"/>"
                + "<link type=\"text/css\" rel=\"stylesheet\" href=\"/style.css\"/>" + "</head>\n"
                + "<body>This has no data\n" + "</body></html>\n";
        defineResource("SimplePage.html", page);

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest(getHostPath() + "/SimplePage.html");
        WebResponse simplePage = wc.getResponse(request);

        assertEquals("/style.css", simplePage.getExternalStyleSheet(), "Stylesheet");
    }

    /**
     * This test verifies that an IO exception is thrown when only a partial response is received.
     */
    @Test
    void truncatedPage() throws Exception {
        HttpUnitOptions.setCheckContentLength(true);
        String page = "abcdefghijklmnop";
        defineResource("alphabet.html", page, "text/plain");
        addResourceHeader("alphabet.html", "Connection: close");
        addResourceHeader("alphabet.html", "Content-length: 26");

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest(getHostPath() + "/alphabet.html");
        try {
            WebResponse simplePage = wc.getResponse(request);
            String alphabet = simplePage.getText();
            assertEquals("abcdefghijklmnopqrstuvwxyz", alphabet, "Full string");
        } catch (IOException e) {
        }
    }

    @Test
    void getElementByID() throws Exception {
        assertDoesNotThrow(() -> {
            defineResource("SimplePage.html", "<html><head><title>A Sample Page</title></head>\n"
                    + "<body><form id='aForm'><input name=color></form>"
                    + "have <a id='link1' href='/other.html'>an <b>active</b> link</A>\n"
                    + "<img id='23' src='/images/arrow.gif' ALT='Next -->' WIDTH=1 HEIGHT=4>\n" + "</body></html>\n");
            WebConversation wc = new WebConversation();
            WebRequest request = new GetMethodWebRequest(getHostPath() + "/SimplePage.html");
            WebResponse simplePage = wc.getResponse(request);
            assertImplements("element with id 'aForm'", simplePage.getElementWithID("aForm"), WebForm.class);
            assertImplements("element with id 'link1'", simplePage.getElementWithID("link1"), WebLink.class);
            assertImplements("element with id '23'", simplePage.getElementWithID("23"), WebImage.class);
        });
    }

    @Test
    void getElementsByName() throws Exception {
        assertDoesNotThrow(() -> {
            defineResource("SimplePage.html", "<html><head><title>A Sample Page</title></head>\n"
                    + "<body><form name='aForm'><input name=color></form>"
                    + "have <a id='link1' href='/other.html'>an <b>active</b> link</A>\n"
                    + "<img id='23' src='/images/arrow.gif' ALT='Next -->' WIDTH=1 HEIGHT=4>\n" + "</body></html>\n");
            WebConversation wc = new WebConversation();
            WebRequest request = new GetMethodWebRequest(getHostPath() + "/SimplePage.html");
            WebResponse simplePage = wc.getResponse(request);
            assertImplement("element with name 'aForm'", simplePage.getElementsWithName("aForm"), WebForm.class);
            assertImplement("element with name 'color'", simplePage.getElementsWithName("color"), FormControl.class);
        });
    }

    @Test
    void getElementsByAttribute() throws Exception {
        assertDoesNotThrow(() -> {
            defineResource("SimplePage.html", "<html><head><title>A Sample Page</title></head>\n"
                    + "<body><form class='first' name='aForm'><input name=color></form>"
                    + "have <a id='link1' href='/other.html'>an <b>active</b> link</A>\n"
                    + "<img id='23' src='/images/arrow.gif' ALT='Next -->' WIDTH=1 HEIGHT=4>\n" + "</body></html>\n");
            WebConversation wc = new WebConversation();
            WebRequest request = new GetMethodWebRequest(getHostPath() + "/SimplePage.html");
            WebResponse simplePage = wc.getResponse(request);
            assertImplement("elements with class 'first'", simplePage.getElementsWithAttribute("class", "first"),
                    WebForm.class);
            assertImplement("elements with name 'color'", simplePage.getElementsWithAttribute("name", "color"),
                    FormControl.class);
            assertImplement("elements with id 'link1'", simplePage.getElementsWithAttribute("id", "link1"),
                    WebLink.class);
            assertImplement("elements with src '/images/arrow.gif'",
                    simplePage.getElementsWithAttribute("src", "/images/arrow.gif"), WebImage.class);
        });
    }

    /**
     * test for getElementsWithClassName supplied by Rick Huff
     *
     * @throws Exception
     */
    @Test
    void getElementsWithClassName() throws Exception {
        assertDoesNotThrow(() -> {
            defineResource("SimplePage.html", "<html><head><title>A Sample Page</title></head>\n"
                    + "<body><form class='first colorsample' name='aForm'><input name=color></form>"
                    + "have <a id='link1' href='/other.html'>an <b>active</b> link</A>\n"
                    + "<img id='23' src='/images/arrow.gif' ALT='Next -->' WIDTH=1 HEIGHT=4>\n" + "</body></html>\n");
            WebConversation wc = new WebConversation();
            WebRequest request = new GetMethodWebRequest(getHostPath() + "/SimplePage.html");
            WebResponse simplePage = wc.getResponse(request);
            assertImplement("elements with class attribute 'first colorsample'",
                    simplePage.getElementsWithAttribute("class", "first colorsample"), WebForm.class);
            assertImplement("elements with class 'first'", simplePage.getElementsWithClassName("first"), WebForm.class);
            assertImplement("elements with class 'colorsample'", simplePage.getElementsWithClassName("colorsample"),
                    WebForm.class);
        });
    }

    /**
     * Test the {@link WebResponse.ByteTagParser} to ensure that embedded JavaScript is skipped.
     */
    @Test
    void byteTagParser() throws Exception {
        final URL mainBaseURL = new URL(getHostPath() + "/Main/Base");
        final URL targetBaseURL = new URL(getHostPath() + "/Target/Base");
        final String targetWindow = "target";
        final String document = "<html><head><title>main</title>\n"
                + scriptToWriteAnotherDocument(simpleDocument(targetBaseURL), targetWindow) + "<base href=\""
                + mainBaseURL.toExternalForm() + "\">\n"
                + "</head>\n<body>\nThis is a <a href=\"Link\">relative link</a>.\n" + "</body>\n</html>\n";
        WebResponse.ByteTagParser parser = new WebResponse.ByteTagParser(document.getBytes(StandardCharsets.UTF_8));

        String[] expectedTags = { "html", "head", "title", "/title", "script", "/script", "base", "/head", "body", "a",
                "/a", "/body", "/html" };
        for (int i = 0; i < expectedTags.length; i++) {
            final String tagName = parser.getNextTag().getName();
            final String expectedTag = expectedTags[i];
            assertEquals(expectedTag, tagName, "Tag number " + i);
        }
        final WebResponse.ByteTag nextTag = parser.getNextTag();
        assertNull(nextTag, "More tags than expected: " + nextTag + "...?");
    }

    /**
     * Test whether a base tag embedded within JavaScript in the header of a page confuses the parser.
     */
    @Test
    void baseTagWithinJavaScriptInHeader() throws Exception {
        final URL mainBaseURL = new URL(getHostPath() + "/Main/Base");
        final URL targetBaseURL = new URL(getHostPath() + "/Target/Base");
        final String targetWindow = "target";
        defineResource("main.html",
                "<html><head><title>main</title>\n"
                        + scriptToWriteAnotherDocument(simpleDocument(targetBaseURL), targetWindow) + "<base href=\""
                        + mainBaseURL.toExternalForm() + "\">\n"
                        + "</head>\n<body>\nThis is a <a href=\"Link\">relative link</a>.\n" + "</body>\n</html>\n");

        WebConversation wc = new WebConversation();
        final WebResponse response = wc.getResponse(getHostPath() + "/main.html");
        assertEquals(mainBaseURL, response.getLinkWith("relative link").getBaseURL(),
                "Base URL of link in main document");

        final WebResponse targetResponse = wc.getOpenWindow(targetWindow).getCurrentPage();
        assertEquals(targetBaseURL, targetResponse.getLinkWith("relative link").getBaseURL(),
                "Base URL of link in target document");
    }

    /**
     * Test whether a base tag embedded within JavaScript in the body of a page confuses the parser.
     */
    @Test
    void baseTagWithinJavaScriptInBody() throws Exception {
        final URL mainBaseURL = new URL(getHostPath() + "/Main/Base");
        final URL targetBaseURL = new URL(getHostPath() + "/Target/Base");
        final String targetWindow = "target";
        defineResource("main.html", "<html><head><title>main</title>\n" + "<base href=\"" + mainBaseURL.toExternalForm()
                + "\">\n" + "</head>\n<body>\nThis is a <a href=\"Link\">relative link</a>.\n"
                + scriptToWriteAnotherDocument(simpleDocument(targetBaseURL), targetWindow) + "</body>\n</html>\n");

        WebConversation wc = new WebConversation();
        final WebResponse response = wc.getResponse(getHostPath() + "/main.html");
        assertEquals(mainBaseURL, response.getLinkWith("relative link").getBaseURL(),
                "Base URL of link in main document");

        final WebResponse targetResponse = wc.getOpenWindow(targetWindow).getCurrentPage();
        assertEquals(targetBaseURL, targetResponse.getLinkWith("relative link").getBaseURL(),
                "Base URL of link in target document");
    }

    /**
     * test case for BR [ 2100376 ] Unable to implement an XPath Predicate (which used to work) by Stephane Mikaty
     *
     * @throws Exception
     */
    @Test
    void getFirstMatchingForm() throws Exception {
        defineResource("SimplePage.html", "<html><title>Hello</title>"
                + " <body><form action='blah' method='GET'><button type='Submit' value='Blah'>Blah</button></form>"
                + " <form action='new' method='GET'>"
                + " <p>Some Junk</p><button type='Submit' value='Save'>Save</button></form>" + "</html>");
        defineResource("new", "<html><body><p>Success.</p></body></html>");
        defineResource("blah", "<html><body><p>Failure.</p></body></html>");

        WebConversation wc = new WebConversation();
        WebResponse resp = wc.getResponse(getHostPath() + "/SimplePage.html");

        // find our desired Save form and submit it
        WebForm form = resp.getFirstMatchingForm(new XPathPredicate("//BUTTON[@value='Save']/ancestor::FORM"), null);
        assertNotNull(form, "The form found should not be null");
        resp = wc.sendRequest(form.getRequest());

        // check the response
        assertTrue(resp.getText().indexOf("Success") >= 0);
    }

    /**
     * test case for BR 2883515
     *
     * @throws SAXException
     * @throws IOException
     */
    @Test
    void invalidNoScriptHandling() throws IOException, SAXException {
        assertDoesNotThrow(() -> {
            defineResource("/InvalidNoScriptPage.html", "<html><body></body></html><noscript>t</noscript>");
            WebConversation wc = new WebConversation();
            WebResponse resp = wc.getResponse(getHostPath() + "/InvalidNoScriptPage.html");
            // indirectly invoke readTags
            resp.replaceText("dummy", "dummy");
        });
    }

    /**
     * Create a fragment of HTML defining JavaScript that writes a document into a different window.
     *
     * @param document
     *            the document to be written
     * @param targetWindow
     *            the name of the target window to open
     *
     * @return a fragment of HTML text
     */
    private String scriptToWriteAnotherDocument(String document, String targetWindow) {
        StringBuilder buff = new StringBuilder();
        buff.append("<script language=\"JavaScript\">\n");
        buff.append("target = window.open('', '").append(targetWindow).append("');\n");
        buff.append("target.document.write('").append(document).append("');\n");
        buff.append("target.document.close();\n");
        buff.append("</script>\n");
        return buff.toString();
    }

    /**
     * Create a simple document with or without a 'base' tag and containing a relative link.
     *
     * @param baseUrl
     *            the base URL to insert, or null for no base tag
     *
     * @return the text of a very simple document
     */
    private String simpleDocument(final URL baseUrl) {
        return "<html><head><title>Simple Page</title>"
                + (baseUrl == null ? "" : "<base href=\"" + baseUrl.toExternalForm() + "\"></base>")
                + "</head><body>This is a simple page with a <a href=\"Link\">relative link</a>.</body></html>";
    }

}

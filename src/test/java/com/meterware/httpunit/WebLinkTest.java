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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

/**
 * Tests for the WebLink class.
 */
class WebLinkTest extends HttpUnitTest {

    /**
     * Sets the up.
     *
     * @throws Exception
     *             the exception
     */
    @BeforeEach
    void setUp() throws Exception {
        defineResource("SimplePage.html", "<html><head><title>A Sample Page</title></head>\n"
                + "<body>This has no forms but it does\n"
                + "have <a href='/other.html#middle' id='activeID'>an <b>active</b> link</A>\n"
                + " and <a name=here>an anchor</a>\n"
                + "<table><tr><td name='acell'><a href='basic.html' name='acelllink'>a link in a cell</a></td></tr></table>"
                + "<a href='basic.html' name=\"nextLink\"><IMG SRC=\"/images/arrow.gif\" ALT=\"Next -->\" WIDTH=1 HEIGHT=4></a>\n"
                + "<a href='another.html' name='myLink'>some text</a>\n" + "</body></html>\n");

        WebConversation wc = new WebConversation();
        _simplePage = wc.getResponse(getHostPath() + "/SimplePage.html");
    }

    /**
     * Find no links.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void findNoLinks() throws Exception {
        defineResource("NoLinks.html", "<html><head><title>NoLinks</title></head><body>No links at all</body></html>");
        WebConversation wc = new WebConversation();

        WebLink[] links = wc.getResponse(getHostPath() + "/NoLinks.html").getLinks();
        assertNotNull(links);
        assertEquals(0, links.length);
    }

    /**
     * test for Bug report 1908117 by firebird74 http://www.w3.org/Addressing/URL/url-spec.html says Spaces and control
     * characters in URLs must be escaped for transmission in HTTP so %20 ist used for space/blank trying out <html>
     * <body> <a href="http://www.bit plan.com">BITlan blank link</a> </body> </html> in different browers gives
     * different results. Firefox 2.0.0.13 will not show a link at all Internet Explorer 6.0 will show a link that has
     * http://www.bit%20plan.com as it's target
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void linkToURLWithBlanks() throws Exception {
        defineWebPage("urlwithblank", "<a href='http://bla.fasel.com/a/b/lorem ipsum.pdf'>link with blank</a>"
                + "<a href='http://bla.fasel.com/a/b/lorem%20ipsum.pdf'>link with blank</a>");
        WebConversation wc = new WebConversation();
        WebResponse resp = wc.getResponse(getHostPath() + "/urlwithblank.html");
        WebLink[] webLinks = resp.getLinks();
        assertEquals(2, webLinks.length, "There should be two link but there are " + webLinks.length);
        // TODO this is what we expect in fact
        // String blankLink1="http://bla.fasel.com/a/b/lorem%20ipsum.pdf";
        String blankLink1 = "http://bla.fasel.com/a/b/loremipsum.pdf";
        String blankLink2 = "http://bla.fasel.com/a/b/lorem%20ipsum.pdf";
        WebLink link1 = webLinks[0];
        assertEquals(link1.getURLString(), blankLink1,
                "the blank in the link1 should be converted but we got '" + link1.getURLString() + "'");
        WebLink link2 = webLinks[1];
        assertEquals(link2.getURLString(), blankLink2,
                "the blank %20 in the link2 should not be converted but we got '" + link2.getURLString() + "'");
    }

    /**
     * check the number of links in the sample page.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void links() throws Exception {
        WebLink[] links = _simplePage.getLinks();
        assertNotNull(links, "Found no links");
        assertEquals(4, links.length, "number of links in page");
    }

    /**
     * Embedded font tags.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void embeddedFontTags() throws Exception {
        defineResource("FontPage.html", "<html><head><title>A Sample Page</title></head>\n"
                + "<table><tr><td><a href='/other.html' id='activeID'><font face='Arial'>an <b>active</b> link</font></A></td>\n"
                + "<td><a href='basic.html' name=\"nextLink\"><IMG SRC=\"/images/arrow.gif\" ALT=\"Next -->\" WIDTH=1 HEIGHT=4></a></td>\n"
                + "<td><a href='another.html' name='myLink'>some text</a></td>\n" + "</tr></table></body></html>\n");
        WebConversation wc = new WebConversation();
        WebResponse wr = wc.getResponse(getHostPath() + "/FontPage.html");
        assertEquals(3, wr.getLinks().length, "Number of links found");
    }

    /**
     * Link request.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void linkRequest() throws Exception {
        WebLink link = _simplePage.getLinks()[0];
        WebRequest request = link.getRequest();
        assertTrue(request instanceof GetMethodWebRequest, "Should be a get request");
        assertEquals(getHostPath() + "/other.html", request.getURL().toExternalForm());
    }

    /**
     * Link reference.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void linkReference() throws Exception {
        WebLink link = _simplePage.getLinks()[0];
        assertEquals("/other.html", link.getURLString(), "URLString");
    }

    /**
     * test for BR 2534057 getLinks() for a Cell return all page links.
     *
     * @return the links for cell
     *
     * @throws SAXException
     *             the SAX exception
     */
    @Test
    void getLinksForCell() throws SAXException {
        HTMLElement[] elements = _simplePage.getElementsWithName("acell");
        assertEquals(1, elements.length);
        assertTrue(elements[0] instanceof TableCell);
        TableCell aCell = (TableCell) elements[0];
        WebLink[] cellLinks = aCell.getLinks();
        /*
         * for (int i=0;i<cellLinks.length;i++) { WebLink link=cellLinks[i];
         * System.out.println("link "+i+"="+link.getName()); }
         */
        assertEquals(1, cellLinks.length);
        assertEquals("acelllink", cellLinks[0].getName());
    }

    /**
     * Gets the link by text.
     *
     * @return the link by text
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void getLinkByText() throws Exception {
        WebLink link = _simplePage.getLinkWith("no link");
        assertNull(link, "Non-existent link should not have been found");
        link = _simplePage.getLinkWith("an active link");
        assertNotNull(link, "an active link was not found");
        assertEquals(getHostPath() + "/other.html", link.getRequest().getURL().toExternalForm(), "active link URL");

        link = _simplePage.getLinkWithImageText("Next -->");
        assertNotNull(link, "the image link was not found");
        assertEquals(getHostPath() + "/basic.html", link.getRequest().getURL().toExternalForm(), "image link URL");

        HttpUnitOptions.setImagesTreatedAsAltText(true);
        link = _simplePage.getLinkWith("Next -->");
        assertNotNull(link, "the image link was not found");
        assertEquals(getHostPath() + "/basic.html", link.getRequest().getURL().toExternalForm(), "image link URL");

        HttpUnitOptions.setImagesTreatedAsAltText(false);
        link = _simplePage.getLinkWith("Next -->");
        assertNull(link, "the image link was found based on its hidden alt attribute");
    }

    /**
     * Custom matching.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void customMatching() throws Exception {
        WebLink link = _simplePage.getFirstMatchingLink(WebLink.MATCH_URL_STRING, "nothing");
        assertNull(link, "Non-existent link should not have been found");

        link = _simplePage.getFirstMatchingLink(WebLink.MATCH_URL_STRING, "/other.html");
        assertNotNull(link, "an active link was not found");
        assertEquals("an active link", link.getText(), "active link text");

        link = _simplePage.getFirstMatchingLink(WebLink.MATCH_URL_STRING, "basic");
        assertNotNull(link, "the image link was not found");
        assertEquals(getHostPath() + "/basic.html", link.getRequest().getURL().toExternalForm(), "image link URL");

        WebLink[] links = _simplePage.getMatchingLinks(WebLink.MATCH_URL_STRING, "other.ht");
        assertNotNull(links, "No link array returned");
        assertEquals(2, links.length, "Number of links with URL containing 'other.ht'");
    }

    /**
     * Gets the link by ID and name.
     *
     * @return the link by ID and name
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void getLinkByIDAndName() throws Exception {
        WebLink link = _simplePage.getLinkWithID("noSuchID");
        assertNull(link, "Non-existent link should not have been found");

        link = _simplePage.getLinkWithID("activeID");
        assertNotNull(link, "an active link was not found");
        assertEquals(getHostPath() + "/other.html", link.getRequest().getURL().toExternalForm(), "active link URL");

        link = _simplePage.getLinkWithName("nextLink");
        assertNotNull(link, "the image link was not found");
        assertEquals(getHostPath() + "/basic.html", link.getRequest().getURL().toExternalForm(), "image link URL");
    }

    /**
     * Fragment identifier.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void fragmentIdentifier() throws Exception {
        WebLink link = (WebLink) _simplePage.getElementWithID("activeID");
        assertNotNull(link, "the active link was not found");
        assertEquals("middle", link.getFragmentIdentifier(), "fragment identifier #1");

        assertEquals("", _simplePage.getLinks()[1].getFragmentIdentifier(), "fragment identifier #2");
    }

    /**
     * Link text.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void linkText() throws Exception {
        WebLink link = _simplePage.getLinks()[0];
        assertEquals("an active link", link.getText(), "Link text");
    }

    /**
     * Link image as text.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void linkImageAsText() throws Exception {
        WebConversation wc = new WebConversation();
        defineWebPage("HasImage", "<a href='somwhere.html' >\r\n<img src='blah.gif' alt='Blah Blah' >\r\n</a>");

        WebResponse initialPage = wc.getResponse(getHostPath() + "/HasImage.html");
        WebLink link = initialPage.getLinks()[0];
        assertEquals("", link.getText().trim(), "Link text");
        initialPage.getLinkWithImageText("Blah Blah");
    }

    /**
     * Link following.
     *
     * @throws Exception
     *             the exception
     *
     * @see [ 1156972 ] isWebLink doesn't recognize all anchor tags for different opinion on weblink count.
     */
    @Test
    void linkFollowing() throws Exception {
        WebConversation wc = new WebConversation();
        defineWebPage("Initial", "Go to <a href=\"Next.html\">the next page.</a> <a name=\"bottom\">Bottom</a>");
        defineWebPage("Next", "And go back to <a href=\"Initial.html#Bottom\">the first page.</a>");

        WebResponse initialPage = wc.getResponse(getHostPath() + "/Initial.html");
        assertEquals(1, initialPage.getLinks().length, "Num links in initial page");
        WebLink link = initialPage.getLinks()[0];

        WebResponse nextPage = wc.getResponse(link.getRequest());
        assertEquals("Next", nextPage.getTitle(), "Title of next page");
        assertEquals(1, nextPage.getLinks().length, "Num links in next page");
        link = nextPage.getLinks()[0];

        link.click();
        assertEquals("Initial", wc.getFrameContents(link.getTarget()).getTitle(), "Title of next page");
    }

    /**
     * test for bug report [ 1232591 ] getTarget() gives "_top" even if target is not present by Rifi.
     *
     * @return the target top
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void getTargetTop() throws Exception {
        WebConversation wc = new WebConversation();
        defineWebPage("target", "<a href=\"a.html\">");
        WebResponse targetPage = wc.getResponse(getHostPath() + "/target.html");
        assertEquals(1, targetPage.getLinks().length, "Num links in initial page");
        WebLink link = targetPage.getLinks()[0];
        String target = link.getTarget();
        // the bug report _top is NOT what we expect
        // but for the time being this is how httpunit behaves ...
        String expected = "_top";
        // System.err.println(target);
        assertEquals(target, expected);
    }

    /**
     * Links with fragments and parameters.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void linksWithFragmentsAndParameters() throws Exception {
        WebConversation wc = new WebConversation();
        defineResource("Initial.html?age=3", "<html><head><title>Initial</title></head><body>"
                + "Go to <a href=\"Next.html\">the next page.</a> <a name=\"bottom\">Bottom</a>" + "</body></html>");
        defineWebPage("Next", "And go back to <a href=\"Initial.html?age=3#Bottom\">the first page.</a>");

        WebResponse initialPage = wc.getResponse(getHostPath() + "/Initial.html?age=3");
        assertEquals(1, initialPage.getLinks().length, "Num links in initial page");
        WebLink link = initialPage.getLinks()[0];

        WebResponse nextPage = wc.getResponse(link.getRequest());
        assertEquals("Next", nextPage.getTitle(), "Title of next page");
        assertEquals(1, nextPage.getLinks().length, "Num links in next page");
        link = nextPage.getLinks()[0];

        WebResponse thirdPage = wc.getResponse(link.getRequest());
        assertEquals("Initial", thirdPage.getTitle(), "Title of next page");
    }

    /**
     * Links with slashes in query.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void linksWithSlashesInQuery() throws Exception {
        WebConversation wc = new WebConversation();
        defineResource("sample/Initial.html?age=3/5", "<html><head><title>Initial</title></head><body>"
                + "Go to <a href=\"Next.html\">the next page.</a>" + "</body></html>");
        defineWebPage("sample/Next", "And go back to <a href=\"Initial.html?age=3/5\">the first page.</a>");

        WebResponse initialPage = wc.getResponse(getHostPath() + "/sample/Initial.html?age=3/5");
        assertEquals(1, initialPage.getLinks().length, "Num links in initial page");
        WebLink link = initialPage.getLinks()[0];

        WebResponse nextPage = wc.getResponse(link.getRequest());
        assertEquals("sample/Next", nextPage.getTitle(), "Title of next page");
        assertEquals(1, nextPage.getLinks().length, "Num links in next page");
    }

    /**
     * Document base.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void documentBase() throws Exception {
        WebConversation wc = new WebConversation();
        defineWebPage("alternate/Target", "Found me!");
        defineResource("Initial.html",
                "<html><head><title>Test for Base</title>" + "            <base href='/alternate/'></head>"
                        + "      <body><a href=\"Target.html\">Go</a></body></html>");

        WebResponse initialPage = wc.getResponse(getHostPath() + "/Initial.html");
        assertEquals(1, initialPage.getLinks().length, "Num links in initial page");
        WebLink link = initialPage.getLinks()[0];

        WebRequest request = link.getRequest();
        assertEquals(getHostPath() + "/alternate/Target.html", request.getURL().toExternalForm(),
                "Destination for link");
        WebResponse nextPage = wc.getResponse(request);
        assertTrue(nextPage.getText().indexOf("Found") >= 0, "Did not find the target");
    }

    /**
     * Target base.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void targetBase() throws Exception {
        WebConversation wc = new WebConversation();
        defineWebPage("alternate/Target", "Found me!");
        defineResource("Initial.html", "<html><head><title>Test for Base</title>"
                + "            <base target=blue></head>" + "      <body><a href=\"Target.html\">Go</a></body></html>");

        WebResponse initialPage = wc.getResponse(getHostPath() + "/Initial.html");
        assertEquals(1, initialPage.getLinks().length, "Num links in initial page");
        WebLink link = initialPage.getLinks()[0];

        assertEquals("blue", link.getTarget(), "Target for link");
    }

    /**
     * Parameters on links.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void parametersOnLinks() throws Exception {
        defineResource("ParameterLinks.html",
                "<html><head><title>Param on Link Page</title></head>\n" + "<body>"
                        + "<a href=\"/other.html\">no parameter link</A>\n"
                        + "<a href=\"/other.html?param1=value1\">one parameter link</A>\n"
                        + "<a href=\"/other.html?param1=value1&param2=value2\">two parameters link</A>\n"
                        + "<a href=\"/other.html?param1=value1&param1=value3\">two values link</A>\n"
                        + "<a href=\"/other.html?param1=value1&param2=value2&param1=value3\">two values link</A>\n"
                        + "</body></html>\n");
        WebConversation wc = new WebConversation();

        WebLink[] links = wc.getResponse(getHostPath() + "/ParameterLinks.html").getLinks();
        assertNotNull(links);
        assertEquals(5, links.length, "number of links");
        WebRequest request;

        // first link should not have any param
        request = links[0].getRequest();
        assertNotNull(request);
        String[] names = request.getRequestParameterNames();
        assertNotNull(names);
        assertEquals(0, names.length, "Num parameters found");
        assertEquals("", request.getParameter("nonexistent"), "Non Existent parameter should be empty");

        // second link should have one parameter
        checkLinkParameters(links[1], new String[] { "param1" }, new String[][] { { "value1" } });

        // third link should have 2 parameters. !! Order of parameters cannot be guaranted.
        checkLinkParameters(links[2], new String[] { "param1", "param2" },
                new String[][] { { "value1" }, { "value2" } });

        // fourth link should have 1 parameter with 2 values.
        checkLinkParameters(links[3], new String[] { "param1" }, new String[][] { { "value1", "value3" } });

        // fifth link should have 2 parameters with one with 2 values.
        checkLinkParameters(links[4], new String[] { "param1", "param2" },
                new String[][] { { "value1", "value3" }, { "value2" } });
    }

    /**
     * Check link parameters.
     *
     * @param link
     *            the link
     * @param expectedNames
     *            the expected names
     * @param expectedValues
     *            the expected values
     */
    private void checkLinkParameters(WebLink link, String[] expectedNames, String[][] expectedValues) {
        WebRequest request = link.getRequest();
        assertNotNull(request);
        assertMatchingSet("Parameter names", expectedNames, request.getRequestParameterNames());
        for (int i = 0; i < expectedValues.length; i++) {
            assertMatchingSet(expectedNames[i] + " values", expectedValues[i],
                    request.getParameterValues(expectedNames[i]));
        }
    }

    /**
     * Encoded link parameters.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void encodedLinkParameters() throws Exception {
        WebConversation wc = new WebConversation();
        defineWebPage("encodedLinks", "<html><head><title>Encode Test</title></head>" + "<body>"
                + "<a href=\"/request?%24dollar=%25percent&%23hash=%26ampersand\">request</a>" + "</body></html>");
        WebResponse mapPage = wc.getResponse(getHostPath() + "/encodedLinks.html");
        WebLink link = mapPage.getLinks()[0];
        WebRequest wr = link.getRequest();
        assertMatchingSet("Request parameter names", new String[] { "$dollar", "#hash" },
                wr.getRequestParameterNames());
        assertEquals("%percent", wr.getParameter("$dollar"), "Value of $dollar");
        assertEquals("&ampersand", wr.getParameter("#hash"), "Value of #hash");
    }

    /**
     * Valueless link parameters.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void valuelessLinkParameters() throws Exception {
        WebConversation wc = new WebConversation();
        defineWebPage("encodedLinks", "<html><head><title>Encode Test</title></head>" + "<body>"
                + "<a href=\"/request?arg1&valueless=\">request</a>" + "</body></html>");
        WebResponse mapPage = wc.getResponse(getHostPath() + "/encodedLinks.html");
        WebLink link = mapPage.getLinks()[0];
        WebRequest wr = link.getRequest();
        assertMatchingSet("Request parameter names", new String[] { "arg1", "valueless" },
                wr.getRequestParameterNames());
        assertNull(wr.getParameter("arg1"), "Value of arg1");
    }

    /**
     * Link parameter order.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void linkParameterOrder() throws Exception {
        WebConversation wc = new WebConversation();
        defineWebPage("encodedLinks", "<html><head><title>Encode Test</title></head>" + "<body>"
                + "<a href='/request?arg0=0\n&arg1&arg0=2&valueless='>request</a>" + "</body></html>");
        WebResponse mapPage = wc.getResponse(getHostPath() + "/encodedLinks.html");
        WebLink link = mapPage.getLinks()[0];
        WebRequest wr = link.getRequest();
        assertMatchingSet("Request parameter names", new String[] { "arg0", "arg1", "valueless" },
                wr.getRequestParameterNames());
        assertMatchingSet("Value of arg0", new String[] { "0", "2" }, wr.getParameterValues("arg0"));
        assertEquals("arg0=0&arg1&arg0=2&valueless=", wr.getQueryString(), "Actual query");
    }

    /**
     * Link parameter validation.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void linkParameterValidation() throws Exception {
        WebConversation wc = new WebConversation();
        defineWebPage("encodedLinks", "<html><head><title>Encode Test</title></head>" + "<body>"
                + "<a href='/request?arg0=0&arg1&arg0=2&valueless='>request</a>" + "</body></html>");
        WebResponse mapPage = wc.getResponse(getHostPath() + "/encodedLinks.html");
        WebLink link = mapPage.getLinks()[0];
        WebRequest wr = link.getRequest();
        wr.setParameter("arg0", new String[] { "0", "2" });
        try {
            wr.setParameter("arg0", "3");
            fail("Did not prevent change to link parameters");
        } catch (IllegalRequestParameterException e) {
        }
    }

    /**
     * Image map links.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void imageMapLinks() throws Exception {
        WebConversation wc = new WebConversation();
        defineWebPage("pageWithMap", "Here is a page with <a href=\"somewhere\">a link</a>"
                + " and a map: <IMG src=\"navbar1.gif\" usemap=\"#map1\" alt=\"navigation bar\">"
                + "<map name=\"map1\">"
                + "  <area href=\"guide.html\" alt=\"Guide\" shape=\"rect\" coords=\"0,0,118,28\">"
                + "  <area href=\"search.html\" alt=\"Search\" shape=\"circle\" coords=\"184,200,60\">" + "</map>");
        WebResponse mapPage = wc.getResponse(getHostPath() + "/pageWithMap.html");
        WebLink[] links = mapPage.getLinks();
        assertEquals(3, links.length, "number of links found");

        WebLink guide = mapPage.getLinkWith("Guide");
        assertNotNull(guide, "Did not find the guide area");
        assertEquals("guide.html", guide.getURLString(), "Relative URL");
    }

    /**
     * test for bug report [ 1035949 ] NullPointerException on Weblink.click by Ute Platzer
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void linkBug() throws Exception {
        WebConversation wc = new WebConversation();
        String html = "\"<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html40/strict.dtd\">\n"
                + "<html><body>\n"
                + "	<a target=\"_parent\" class=\"core_button_normal\"  href=\"/test2.html\">test link</a>"
                + "</body></html>";
        defineWebPage("test3", html);
        defineWebPage("test2", "test page2");
        HttpUnitOptions.setLoggingHttpHeaders(false);
        HttpUnitOptions.setScriptingEnabled(false);
        wc.getResponse(getHostPath() + "/test3.html");
        WebLink link = wc.getCurrentPage().getLinkWith("test link");
        link.click();

        String html2 = wc.getCurrentPage().getText();
        assertTrue(html2.indexOf("test page2") > 0, "click should lead to page 2");
    }

    /** The simple page. */
    private WebResponse _simplePage;
}

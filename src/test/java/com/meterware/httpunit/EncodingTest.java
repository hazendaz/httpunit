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

import com.meterware.pseudoserver.PseudoServlet;
import com.meterware.pseudoserver.WebResource;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * Tests handling of non-Latin scripts.
 */
class EncodingTest extends HttpUnitTest {

    /**
     * Decode with character set as arg.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void decodeWithCharacterSetAsArg() throws Exception {
        String expected = "newpage\u30b5\u30f3\u30d7\u30eb"; // "\u30b5\u30f3\u30d7\u30eb" means "SAMPLE" in Japanese
        // EUC-JP characterSet

        String encodedString = "newpage%A5%B5%A5%F3%A5%D7%A5%EB";
        String actual = HttpUnitUtils.decode(encodedString, "EUC-JP");
        assertEquals(expected, actual, "decoded string");
    }

    /**
     * test parseContentHeader.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void parseContentHeader() throws Exception {
        String[] headers = { "", "text/plain", "text/html; charset=Cp1252", "text/html; charset=ISO-8859-8",
                "text/html; charset=EUC-JP", "text/html charset=windows-1251", "text/html; charset=utf-8",
                "text/html; charset = utf-8", "text/html; charset=\"ISO-8859-8\"" };
        String[][] expected = { { "text/plain", null }, { "text/plain", null }, { "text/html", "Cp1252" },
                { "text/html", "ISO-8859-8" }, { "text/html", "EUC-JP" }, { "text/html", "windows-1251" },
                { "text/html", "utf-8" }, { "text/html", "utf-8" }, { "text/html", "ISO-8859-8" } };
        for (int i = 0; i < headers.length; i++) {
            String[] result = HttpUnitUtils.parseContentTypeHeader(headers[i]);
            assertEquals(2, result.length);
            assertEquals(expected[i][0], result[0], "header " + i);
            assertEquals(expected[i][1], result[1], "header " + i);
        } // for
    }

    /**
     * Specified encoding.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void specifiedEncoding() throws Exception {
        String hebrewTitle = "\u05d0\u05d1\u05d2\u05d3";
        String page = "<html><head><title>" + hebrewTitle + "</title></head>\n" + "<body>This has no data\n"
                + "</body></html>\n";
        defineResource("SimplePage.html", page);
        setResourceCharSet("SimplePage.html", "ISO-8859-8", true);

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest(getHostPath() + "/SimplePage.html");
        WebResponse simplePage = wc.getResponse(request);

        assertEquals(hebrewTitle, simplePage.getTitle(), "Title");
        assertEquals("ISO-8859-8", simplePage.getCharacterSet(), "Character set");
    }

    /**
     * Quoted encoding.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void quotedEncoding() throws Exception {
        String hebrewTitle = "\u05d0\u05d1\u05d2\u05d3";
        String page = "<html><head><title>" + hebrewTitle + "</title></head>\n" + "<body>This has no data\n"
                + "</body></html>\n";
        defineResource("SimplePage.html", page);
        setResourceCharSet("SimplePage.html", "\"ISO-8859-8\"", true);

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest(getHostPath() + "/SimplePage.html");
        WebResponse simplePage = wc.getResponse(request);

        assertEquals(hebrewTitle, simplePage.getTitle(), "Title");
        assertEquals("ISO-8859-8", simplePage.getCharacterSet(), "Character set");
    }

    /**
     * Unspecified encoding.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void unspecifiedEncoding() throws Exception {
        String hebrewTitle = "\u05d0\u05d1\u05d2\u05d3";
        String page = "<html><head><title>" + hebrewTitle + "</title></head>\n" + "<body>This has no data\n"
                + "</body></html>\n";
        defineResource("SimplePage.html", page);
        setResourceCharSet("SimplePage.html", "ISO-8859-8", false);

        HttpUnitOptions.setDefaultCharacterSet("ISO-8859-8");
        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest(getHostPath() + "/SimplePage.html");
        WebResponse simplePage = wc.getResponse(request);

        assertEquals("ISO-8859-8", simplePage.getCharacterSet(), "Character set");
        assertEquals(hebrewTitle, simplePage.getTitle(), "Title");
    }

    /**
     * Meta encoding.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void metaEncoding() throws Exception {
        String hebrewTitle = "\u05d0\u05d1\u05d2\u05d3";
        String page = "<html><head><title>" + hebrewTitle + "</title>"
                + "<meta Http_equiv=content-type content=\"text/html; charset=ISO-8859-8\"></head>\n"
                + "<body>This has no data\n" + "</body></html>\n";
        defineResource("SimplePage.html", page);
        setResourceCharSet("SimplePage.html", "ISO-8859-8", false);

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest(getHostPath() + "/SimplePage.html");
        WebResponse simplePage = wc.getResponse(request);

        assertEquals("ISO-8859-8", simplePage.getCharacterSet(), "Character set");
        assertEquals(hebrewTitle, simplePage.getTitle(), "Title");
    }

    /**
     * Hebrew form.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void hebrewForm() throws Exception {
        String hebrewName = "\u05d0\u05d1\u05d2\u05d3";
        defineResource("HebrewForm.html", "<html><head></head>" + "<form method=POST action=\"SayHello\">"
                + "<input type=text name=name><input type=submit></form></body></html>");
        setResourceCharSet("HebrewForm.html", "ISO-8859-8", true);
        defineResource("SayHello", new PseudoServlet() {
            @Override
            public WebResource getPostResponse() throws UnsupportedEncodingException {
                String name = getParameter("name")[0];
                WebResource result = new WebResource("<html><body><table><tr><td>Hello, "
                        + new String(name.getBytes(StandardCharsets.ISO_8859_1), "ISO-8859-8")
                        + "</td></tr></table></body></html>");
                result.setCharacterSet("ISO-8859-8");
                result.setSendCharacterSet(true);
                return result;
            }
        });

        WebConversation wc = new WebConversation();
        WebResponse formPage = wc.getResponse(getHostPath() + "/HebrewForm.html");
        WebForm form = formPage.getForms()[0];
        WebRequest request = form.getRequest();
        request.setParameter("name", hebrewName);

        WebResponse answer = wc.getResponse(request);
        String[][] cells = answer.getTables()[0].asText();

        assertEquals("Hello, " + hebrewName, cells[0][0], "Message");
        assertEquals("ISO-8859-8", answer.getCharacterSet(), "Character set");
    }

    /**
     * Encoded request without form.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void encodedRequestWithoutForm() throws Exception {
        String hebrewName = "\u05d0\u05d1\u05d2\u05d3";
        defineResource("SayHello", new PseudoServlet() {
            @Override
            public WebResource getPostResponse() throws UnsupportedEncodingException {
                String name = getParameter("name")[0];
                WebResource result = new WebResource("<html><body><table><tr><td>Hello, "
                        + new String(name.getBytes(StandardCharsets.ISO_8859_1), "ISO-8859-8")
                        + "</td></tr></table></body></html>");
                result.setCharacterSet("ISO-8859-8");
                result.setSendCharacterSet(true);
                return result;
            }
        });

        WebConversation wc = new WebConversation();
        HttpUnitOptions.setDefaultCharacterSet("ISO-8859-8");
        WebRequest request = new PostMethodWebRequest(getHostPath() + "/SayHello");
        request.setParameter("name", hebrewName);

        WebResponse answer = wc.getResponse(request);
        String[][] cells = answer.getTables()[0].asText();

        assertEquals("Hello, " + hebrewName, cells[0][0], "Message");
        assertEquals("ISO-8859-8", answer.getCharacterSet(), "Character set");
    }

    /**
     * Unsupported encoding.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void unsupportedEncoding() throws Exception {
        defineResource("SimplePage.html", "not much here");
        addResourceHeader("SimplePage.html", "Content-type: text/plain; charset=BOGUS");

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest(getHostPath() + "/SimplePage.html");
        WebResponse simplePage = wc.getResponse(request);

        assertEquals("not much here", simplePage.getText(), "Text");
        assertEquals(WebResponse.getDefaultEncoding(), simplePage.getCharacterSet(), "Character set");
    }

    /**
     * Japanese link param name with value.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void japaneseLinkParamNameWithValue() throws Exception {
        String japaneseUrl = "request?%A5%D8%A5%EB%A5%D7=2";
        defineWebPage("Linker", "<a id='link' href='" + japaneseUrl + "'>goThere</a>");
        setResourceCharSet("Linker.html", "EUC-JP", true);
        defineResource(japaneseUrl, "You made it!");

        WebConversation wc = new WebConversation();
        WebResponse formPage = wc.getResponse(getHostPath() + "/Linker.html");
        WebResponse target = formPage.getLinkWithID("link").click();
        assertEquals("You made it!", target.getText(), "Resultant page");
    }

    /**
     * Japanese link param name without value.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void japaneseLinkParamNameWithoutValue() throws Exception {
        String japaneseUrl = "request?%A5%D8%A5%EB%A5%D7";
        defineWebPage("Linker", "<a id='link' href='" + japaneseUrl + "'>goThere</a>");
        setResourceCharSet("Linker.html", "EUC-JP", true);
        defineResource(japaneseUrl, "You made it!");

        WebConversation wc = new WebConversation();
        WebResponse formPage = wc.getResponse(getHostPath() + "/Linker.html");
        WebResponse target = formPage.getLinkWithID("link").click();
        assertEquals("You made it!", target.getText(), "Resultant page");
    }

    /**
     * Simple entity replacement.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void simpleEntityReplacement() throws Exception {
        String rawString = "Cox&amp;&amp;Forkum";
        assertEquals("Cox&&Forkum", HttpUnitUtils.replaceEntities(rawString), "After substitution");
    }

    /**
     * Skip entity replacement on bad string.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void skipEntityReplacementOnBadString() throws Exception {
        String rawString = "Cox&Forkum";
        assertEquals("Cox&Forkum", HttpUnitUtils.replaceEntities(rawString), "After substitution");
    }

    /**
     * Skip entity replacement on unhandled entity.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void skipEntityReplacementOnUnhandledEntity() throws Exception {
        String rawString = "&lt;something&gt;";
        assertEquals("&lt;something&gt;", HttpUnitUtils.replaceEntities(rawString), "After substitution");
    }

}

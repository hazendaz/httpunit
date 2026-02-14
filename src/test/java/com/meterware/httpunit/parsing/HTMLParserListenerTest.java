/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.parsing;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HeadMethodWebRequest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.HttpUnitTest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

import java.io.PrintWriter;
import java.net.URL;

import org.junit.jupiter.api.Test;

/**
 * The Class HTMLParserListenerTest.
 */
class HTMLParserListenerTest extends HttpUnitTest {

    /**
     * Bad HTML page.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void badHTMLPage() throws Exception {
        defineResource("BadPage.html",
                "<html>" + "<head><title>A Sample Page</head>\n" + "<body><p><b>Wrong embedded tags</p></b>\n"
                        + "have <a blef=\"other.html?a=1&b=2\">an invalid link</A>\n"
                        + "<IMG SRC=\"/images/arrow.gif\" WIDTH=1 HEIGHT=4>\n" + "<unknownTag>bla</unknownTag>"
                        + "</body></html>\n");

        final ErrorHandler errorHandler = new ErrorHandler(/* expectProblems */true);
        try {
            WebConversation wc = new WebConversation();
            HTMLParserFactory.addHTMLParserListener(errorHandler);
            WebRequest request = new GetMethodWebRequest(getHostPath() + "/BadPage.html");
            wc.getResponse(request);
            assertTrue(errorHandler.foundProblems(), "Should have found problems");
            assertEquals(request.getURL(), errorHandler.getBadURL(), "Expected URL");
        } finally {
            HTMLParserFactory.removeHTMLParserListener(errorHandler);
        }
    }

    /**
     * Good HTML page.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void goodHTMLPage() throws Exception {
        assertDoesNotThrow(() -> {
            final ErrorHandler errorHandler = new ErrorHandler(/* expectProblems */false);
            try {
                defineResource("SimplePage.html",
                        "<html>\n" + "<head><title>A Sample Page</title></head>\n"
                                + "<body><p><b>OK embedded tags</b></p>\n"
                                + "have <a href=\"other.html?a=1&amp;b=2\">an OK link</A>\n"
                                + "<IMG SRC=\"/images/arrow.gif\" alt=\"\" WIDTH=1 HEIGHT=4>\n" + "</body></html>\n");

                WebConversation wc = new WebConversation();
                HTMLParserFactory.addHTMLParserListener(errorHandler);
                WebRequest request = new GetMethodWebRequest(getHostPath() + "/SimplePage.html");
                wc.getResponse(request);
            } finally {
                HTMLParserFactory.removeHTMLParserListener(errorHandler);
            }
        });
    }

    /**
     * J tidy print writer parsing.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void jTidyPrintWriterParsing() throws Exception {
        assertDoesNotThrow(() -> {
            URL url = new URL("http://localhost/blank.html");
            PrintWriter p = new JTidyPrintWriter(url);
            p.print("line 1234 column 1234");
            p.print("line 1,234 column 1,234");
            p.print("line 1,234,567 column 1,234,567");
            p.print("line 1,2,34 column 12,34");
            p.print("line 123,,4 column 12,,34");
        });
    }

    /**
     * test by Dan Lipofsky.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void headMethodWebRequest2() throws Exception {
        defineResource("SimplePage.html",
                "<html><head><title>A Sample Page</title></head>\n" + "<body>Hello</body></html>\n");
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(true);
        try {
            HTMLParserFactory.setParserWarningsEnabled(true);
            HTMLParserFactory.setHTMLParser(new NekoHTMLParser() {
                @Override
                public void parse(URL pageURL, String pageText, DocumentAdapter adapter) {
                    System.err.println("Parsing URL=" + pageURL + "\n" + pageText);
                    fail("Should not be parsing a HEAD request");
                }
            });
            HTMLParserFactory.addHTMLParserListener(new HTMLParserListener() {
                @Override
                public void error(URL url, String msg, int line, int column) {
                    System.err.println("ERROR @url=" + url + ": (" + line + ", " + column + "):" + msg);
                }

                @Override
                public void warning(URL url, String msg, int line, int column) {
                    System.err.println("WARN @url=" + url + ": (" + line + ", " + column + "):" + msg);
                }

            });
            WebConversation wc = new WebConversation();
            // create a HeadMethodWebRequest
            // see http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html for
            // definition
            WebRequest request = new HeadMethodWebRequest(getHostPath() + "/SimplePage.html");
            WebResponse simplePage = wc.getResponse(request);
            String text = simplePage.getText();
            // no body should be returned
            assertEquals("", text);
        } finally {
            HTMLParserFactory.reset();
        }
    }

    /**
     * The Class ErrorHandler.
     */
    static private class ErrorHandler implements HTMLParserListener {

        /** The expect problems. */
        private boolean _expectProblems;

        /** The found problems. */
        private boolean _foundProblems;

        /** The bad URL. */
        private URL _badURL;

        /**
         * Instantiates a new error handler.
         *
         * @param expectProblems
         *            the expect problems
         */
        public ErrorHandler(boolean expectProblems) {
            _expectProblems = expectProblems;
        }

        @Override
        public void warning(URL url, String msg, int line, int column) {
            _foundProblems = true;
            _badURL = url;
        }

        @Override
        public void error(URL url, String msg, int line, int column) {
            assertTrue(_expectProblems, msg + " at line " + line + ", column " + column);
            _foundProblems = true;
            _badURL = url;
        }

        /**
         * Gets the bad URL.
         *
         * @return the bad URL
         */
        public URL getBadURL() {
            return _badURL;
        }

        /**
         * Found problems.
         *
         * @return true, if successful
         */
        public boolean foundProblems() {
            return _foundProblems;
        }
    }

}

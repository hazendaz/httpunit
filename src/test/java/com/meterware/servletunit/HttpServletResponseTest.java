/*
 * MIT License
 *
 * Copyright 2011-2024 Russell Gold
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
package com.meterware.servletunit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.meterware.httpunit.FrameSelector;
import com.meterware.httpunit.WebResponse;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.junit.jupiter.api.Test;

/**
 * Tests the ServletUnitHttpResponse class.
 */
class HttpServletResponseTest extends ServletUnitTest {

    @Test
    void testDefaultResponse() throws Exception {
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        WebResponse response = new ServletUnitWebResponse(null, FrameSelector.TOP_FRAME, null, servletResponse);
        assertEquals("", response.getText(), "Contents");
    }

    @Test
    void testSimpleResponse() throws Exception {
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        servletResponse.setContentType("text/html");
        servletResponse.setContentLength(65);
        PrintWriter pw = servletResponse.getWriter();
        pw.println("<html><head><title>Sample Page</title></head><body></body></html>");

        WebResponse response = new ServletUnitWebResponse(null, FrameSelector.TOP_FRAME, null, servletResponse);
        assertEquals(HttpServletResponse.SC_OK, response.getResponseCode(), "Status code");
        assertEquals("text/html", response.getContentType(), "Content type");
        assertEquals("Sample Page", response.getTitle(), "Title");
        assertEquals(65, response.getContentLength(), "Content length");
        assertEquals("iso-8859-1", response.getCharacterSet(), "Content encoding");
        assertEquals("text/html; charset=iso-8859-1", response.getHeaderField("Content-type"), "Content header");
    }

    @Test
    void testEncoding() throws Exception {
        String hebrewTitle = "\u05d0\u05d1\u05d2\u05d3";
        String page = "<html><head><title>" + hebrewTitle + "</title></head>\n" + "<body>This has no data\n"
                + "</body></html>\n";
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        servletResponse.setContentType("text/html; charset=iso-8859-8");
        PrintWriter pw = servletResponse.getWriter();
        pw.print(page);
        pw.close();

        WebResponse response = new ServletUnitWebResponse(null, FrameSelector.TOP_FRAME, null, servletResponse);
        assertEquals("iso-8859-8", response.getCharacterSet(), "Character set");
        assertEquals(hebrewTitle, response.getTitle(), "Title");
    }

    @Test
    void testLocale() throws Exception {
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        assertEquals(Locale.getDefault(), servletResponse.getLocale(), "Default locale");

        servletResponse.setContentType("text/html");
        servletResponse.setLocale(new Locale("he", "IL"));
        assertEquals(new Locale("he", "IL"), servletResponse.getLocale(), "Specified locale");
        assertEquals("text/html; charset=iso-8859-8", servletResponse.getHeaderField("Content-type"), "Content type");

    }

    @Test
    void testStreamResponse() throws Exception {
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        servletResponse.setContentType("text/html");
        ServletOutputStream sos = servletResponse.getOutputStream();
        sos.println("<html><head><title>Sample Page</title></head><body></body></html>");

        WebResponse response = new ServletUnitWebResponse(null, FrameSelector.TOP_FRAME, null, servletResponse);
        assertEquals(HttpServletResponse.SC_OK, response.getResponseCode(), "Status code");
        assertEquals("text/html", response.getContentType(), "Content type");
        assertEquals("Sample Page", response.getTitle(), "Title");
    }

    @Test
    void testStreamWriterAfterOutputStream() throws Exception {
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        servletResponse.setContentType("text/html");
        servletResponse.getOutputStream();
        try {
            servletResponse.getWriter();
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
        }
    }

    @Test
    void testStreamOutputStreamAfterWriter() throws Exception {
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        servletResponse.getWriter();
        try {
            servletResponse.getOutputStream();
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
        }
    }

    @Test
    void testSetBufferSizeAfterWrite() throws Exception {
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        servletResponse.setBufferSize(120);
        servletResponse.getWriter();
        servletResponse.setBufferSize(100);
        servletResponse.getWriter().print("something");
        try {
            servletResponse.setBufferSize(80);
            fail("Should not have permitted setBufferSize after write");
        } catch (IllegalStateException e) {
        }
    }

    @Test
    void testSetBufferSizeAfterStreamOutput() throws Exception {
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        servletResponse.setBufferSize(120);
        servletResponse.getOutputStream();
        servletResponse.setBufferSize(100);
        servletResponse.getOutputStream().print("something");
        try {
            servletResponse.setBufferSize(80);
            fail("Should not have permitted setBufferSize after output");
        } catch (IllegalStateException e) {
        }
    }

    @Test
    void testResetBuffer() throws Exception {
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        servletResponse.getOutputStream().print("something");
        assertEquals(9, servletResponse.getContents().length, "buffer size");
        servletResponse.resetBuffer();
        assertEquals(0, servletResponse.getContents().length, "buffer size");

        servletResponse.flushBuffer();
        try {
            servletResponse.resetBuffer();
            fail("Should not have permitted resetBuffer after flush");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * test isComitted flag after flushing buffer
     *
     * @throws Exception
     */
    @Test
    void testUpdateAfterFlushBuffer() throws Exception {
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        servletResponse.getWriter();
        assertFalse(servletResponse.isCommitted(), "Should not be committed yet");
        servletResponse.flushBuffer();
        assertTrue(servletResponse.isCommitted(), "Should be committed now");
    }

    @Test
    void testSingleHeaders() throws Exception {
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        servletResponse.setContentType("text/html");

        assertFalse(servletResponse.containsHeader("foo"), "header foo wrongly detected");
        servletResponse.setHeader("foo", "bar");
        String headerValue = servletResponse.getHeaderField("foo");
        assertEquals("bar", headerValue, "header is wrong");
        assertTrue(servletResponse.containsHeader("foo"), "header foo not detected");

        servletResponse.setHeader("foo", "baz");
        headerValue = servletResponse.getHeaderField("foo");
        assertEquals("baz", headerValue, "header is wrong");

        servletResponse.setIntHeader("three", 3);
        headerValue = servletResponse.getHeaderField("three");
        assertEquals("3", headerValue, "int header is wrong");

        // use RFC1123_DATE_SPEC formatter
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z");
        Date d = df.parse("12/9/1969 12:00:00 GMT");
        servletResponse.setDateHeader("date", d.getTime());
        headerValue = servletResponse.getHeaderField("date");
        assertEquals("Tue, 09 Dec 1969 12:00:00 GMT", headerValue, "date header is wrong");
    }

    @Test
    void testMultipleHeaders() throws Exception {
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        servletResponse.setContentType("text/html");

        // RFC1123_DATE_SPEC format
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z");
        Date date = df.parse("12/9/1969 12:00:00 GMT");

        servletResponse.addHeader("list", "over-rideme");
        servletResponse.setHeader("list", "foo");
        servletResponse.addIntHeader("list", 3);
        servletResponse.addDateHeader("list", date.getTime());
        String[] headerList = servletResponse.getHeaderFields("list");
        assertEquals("foo", headerList[0], "header is wrong");
        assertEquals("3", headerList[1], "header is wrong");
        assertEquals("Tue, 09 Dec 1969 12:00:00 GMT", headerList[2], "header is wrong");

        servletResponse.setHeader("list", "monkeyboy");
        headerList = servletResponse.getHeaderFields("list");
        assertEquals(1, headerList.length, "setHeader did not replace the list header");
        assertEquals("monkeyboy", headerList[0], "header is wrong");
    }

    @Test
    void testSendRedirect() throws Exception {
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        final String location = "http://localhost/newLocation";
        servletResponse.sendRedirect(location);
        assertEquals(location, servletResponse.getHeaderField("Location"), "Redirected Location");
        assertEquals(302, servletResponse.getStatus(), "Status");
    }

}

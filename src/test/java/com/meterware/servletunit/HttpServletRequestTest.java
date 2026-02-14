/*
 * MIT License
 *
 * Copyright 2011-2026 Russell Gold
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.meterware.httpunit.FrameSelector;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebRequest;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the ServletUnitHttpRequest class.
 */
class HttpServletRequestTest extends ServletUnitTest {

    /** The context. */
    private ServletUnitContext _context;

    /**
     * Sets the up.
     *
     * @throws Exception
     *             the exception
     */
    @BeforeEach
    void setUp() throws Exception {
        _context = new ServletUnitContext(null, null, new SessionListenerDispatcher() {
            @Override
            public void sendSessionCreated(HttpSession session) {
            }

            @Override
            public void sendSessionDestroyed(HttpSession session) {
            }

            @Override
            public void sendAttributeAdded(HttpSession session, String name, Object value) {
            }

            @Override
            public void sendAttributeReplaced(HttpSession session, String name, Object oldValue) {
            }

            @Override
            public void sendAttributeRemoved(HttpSession session, String name, Object oldValue) {
            }
        });

    }

    /**
     * Header access.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void headerAccess() throws Exception {
        WebRequest wr = new GetMethodWebRequest("http://localhost/simple");
        wr.setHeaderField("sample", "value");
        HttpServletRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context, new Hashtable<>(),
                NO_MESSAGE_BODY);
        assertEquals("value", request.getHeader("sample"), "sample header value");

        assertContains("Header names", "sample", request.getHeaderNames());
        Enumeration e = request.getHeaders("Sample");
        assertNotNull(e, "No header enumeration returned");
        assertTrue(e.hasMoreElements(), "Enumeration is empty");
        assertEquals("value", e.nextElement(), "first header");
        assertFalse(e.hasMoreElements(), "Enumeration has spurious header value");
    }

    /**
     * test getting a date header;.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void dateHeaderAccess() throws Exception {
        WebRequest wr = new GetMethodWebRequest("http://localhost/simple");
        String dateStr = "Mon, 26 Jul 1997 05:00:00 GMT";
        Date testDate = new Date(dateStr);
        wr.setHeaderField("Expires", dateStr);
        HttpServletRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context, new Hashtable<>(),
                NO_MESSAGE_BODY);
        String dateStrRequest = request.getHeader("Expires");
        assertEquals(dateStrRequest, dateStr, "Expires header field");
        // invalid date headers return -1
        long requestDate = request.getDateHeader("invalid");
        assertEquals(-1, requestDate, "invalid date header field");
        // valid date header field return the millisecs
        requestDate = request.getDateHeader("Expires");
        assertEquals(requestDate, testDate.getTime(), "Expires date header field");
    }

    /**
     * Assert contains.
     *
     * @param comment
     *            the comment
     * @param string
     *            the string
     * @param headerNames
     *            the header names
     */
    private void assertContains(String comment, String string, Enumeration headerNames) {
        while (headerNames != null && headerNames.hasMoreElements()) {
            String name = (String) headerNames.nextElement();
            if (name.equalsIgnoreCase(string)) {
                return;
            }
        }
        fail(comment + " does not contain " + string);
    }

    /**
     * Gets the default properties.
     *
     * @return the default properties
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void getDefaultProperties() throws Exception {
        WebRequest wr = new GetMethodWebRequest("http://localhost/simple");
        HttpServletRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context, new Hashtable<>(),
                NO_MESSAGE_BODY);
        assertNull(request.getAuthType(), "Authorization incorrectly specified");
        assertNull(request.getCharacterEncoding(), "Character encoding incorrectly specified");
        assertEquals("", request.getQueryString(), "Parameters unexpectedly specified");
        assertNotNull(request.getInputStream(), "No input stream available");
    }

    /**
     * Sets the single valued parameter.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void setSingleValuedParameter() throws Exception {
        WebRequest wr = new GetMethodWebRequest("http://localhost/simple");
        wr.setParameter("age", "12");
        wr.setParameter("color", new String[] { "red", "blue" });
        HttpServletRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context, new Hashtable<>(),
                NO_MESSAGE_BODY);

        assertEquals("12", request.getParameter("age"), "age parameter");
        assertNull(request.getParameter("unset"), "unset parameter should be null");
    }

    /**
     * Sets the multi valued parameter.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void setMultiValuedParameter() throws Exception {
        WebRequest wr = new GetMethodWebRequest("http://localhost/simple");
        wr.setParameter("age", "12");
        wr.setParameter("color", new String[] { "red", "blue" });
        HttpServletRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context, new Hashtable<>(),
                NO_MESSAGE_BODY);

        assertMatchingSet("age parameter", new String[] { "12" }, request.getParameterValues("age"));
        assertMatchingSet("color parameter", new String[] { "red", "blue" }, request.getParameterValues("color"));
        assertNull(request.getParameterValues("unset"), "unset parameter should be null");
    }

    /**
     * test for bug report [ 1143757 ] encoding of Special charcters broken with 1.6 by Klaus Halfmann test for bug
     * report [ 1159810 ] URL encoding problem with ServletUnit by Sven Helmberger
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void parameterWithSpacesAndAt() throws Exception {
        WebRequest wr = new GetMethodWebRequest("http://localhost/simple");
        String pValueWithSpaces = "This Input is to long";
        String pValueWithAt = "@user@example.org";
        wr.setParameter("age", pValueWithSpaces);
        String result = wr.getParameter("age");
        // according to Klaus Halfman as of 2005-02-18 / version 1.6 results in "This+Input+is+to+long"
        // System.err.println(result);
        assertEquals(pValueWithSpaces, result, "spaces should survive");
        wr.setParameter("age", pValueWithAt);
        result = wr.getParameter("age");
        assertEquals(pValueWithAt, result, "@ should survive");
    }

    /**
     * Parameter with at.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void parameterWithAt() throws Exception {
        WebRequest wr = new GetMethodWebRequest("http://localhost/simple");
        String pValueWithSpaces = "This Input is to long";
        wr.setParameter("age", pValueWithSpaces);
        String result = wr.getParameter("age");
        // according to Klaus Halfman as of 2005-02-18 / version 1.6 results in "This+Input+is+to+long"
        // System.err.println(result);
        assertEquals(pValueWithSpaces, result, "spaces should survive");
    }

    /**
     * Parameter map.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void parameterMap() throws Exception {
        WebRequest wr = new GetMethodWebRequest("http://localhost/simple");
        wr.setParameter("age", "12");
        wr.setParameter("color", new String[] { "red", "blue" });
        HttpServletRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context, new Hashtable<>(),
                NO_MESSAGE_BODY);

        Map map = request.getParameterMap();
        assertMatchingSet("age parameter", new String[] { "12" }, (Object[]) map.get("age"));
        assertMatchingSet("color parameter", new String[] { "red", "blue" }, (Object[]) map.get("color"));
        assertNull(map.get("unset"), "unset parameter should be null");
    }

    /**
     * Sets the query string.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void setQueryString() throws Exception {
        WebRequest wr = new GetMethodWebRequest("http://localhost/simple");
        wr.setParameter("age", "12");
        wr.setParameter("color", new String[] { "red", "blue" });
        HttpServletRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context, new Hashtable<>(),
                NO_MESSAGE_BODY);

        assertEquals("color=red&color=blue&age=12", request.getQueryString(), "query string");
    }

    /**
     * test Bug report 1212204 by Brian Bonner.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void bug1212204() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://localhost/pathinfo?queryString");
        // assertEquals("queryString", request.getQueryString());
        request = new GetMethodWebRequest("http://localhost/pathinfo?queryString");
        request.setParameter("queryString", "");
        assertEquals("queryString=", request.getQueryString());
        request = new GetMethodWebRequest("http://localhost/pathinfo?queryString");
        request.setParameter("queryString", (String) null);
        assertEquals("queryString", request.getQueryString());
        WebRequest wr = new GetMethodWebRequest("http://localhost?wsdl");
        wr.setParameter("abc", "def");
        wr.setParameter("def", "");
        wr.setParameter("test", (String) null);
        wr.setParameter("wsdl", (String) null);

        assertEquals("wsdl&abc=def&def=&test", wr.getQueryString());
    }

    /**
     * Inline single valued parameter.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void inlineSingleValuedParameter() throws Exception {
        WebRequest wr = new GetMethodWebRequest("http://localhost/simple?color=red&color=blue&age=12");
        HttpServletRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context, new Hashtable<>(),
                NO_MESSAGE_BODY);

        assertEquals("12", request.getParameter("age"), "age parameter");
        assertNull(request.getParameter("unset"), "unset parameter should be null");
    }

    /**
     * Inline parameter with embedded space.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void inlineParameterWithEmbeddedSpace() throws Exception {
        WebRequest wr = new GetMethodWebRequest("http://localhost/simple?color=dark+red&age=12");
        HttpServletRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context, new Hashtable<>(),
                NO_MESSAGE_BODY);

        assertEquals("12", request.getParameter("age"), "age parameter");
        assertEquals("dark red", request.getParameter("color"), "color parameter");
    }

    /**
     * Inline multi valued parameter.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void inlineMultiValuedParameter() throws Exception {
        WebRequest wr = new GetMethodWebRequest("http://localhost/simple?color=red&color=blue&age=12");
        HttpServletRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context, new Hashtable<>(),
                NO_MESSAGE_BODY);

        assertMatchingSet("age parameter", new String[] { "12" }, request.getParameterValues("age"));
        assertMatchingSet("color parameter", new String[] { "red", "blue" }, request.getParameterValues("color"));
        assertNull(request.getParameterValues("unset"), "unset parameter should be null");
    }

    /**
     * test patch for [ 1705925 ] Bug in URL-decoding of GET-Request-Parameters
     * http://sourceforge.net/tracker/index.php?func=detail&amp;aid=1705925&amp;group_id=6550&amp;atid=106550
     *
     * @throws Exception
     *             the exception
     */

    public void testGetMethodRequestParametersEncodedWithDefaultCharacterSet_Hebrew() throws Exception {
        String hebrewValue = "\u05d0\u05d1\u05d2\u05d3";
        // encoded
        HttpUnitOptions.setDefaultCharacterSet("ISO-8859-8");
        WebRequest wr = new GetMethodWebRequest("http://localhost/simple");
        wr.setParameter("param1", "red");
        wr.setParameter("param2", hebrewValue);
        ServletUnitHttpRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context,
                new Hashtable<>(), new byte[0]);
        assertEquals("red", request.getParameter("param1"), "param1 value");
        assertEquals(hebrewValue, request.getParameter("param2"), "param2 value");
    }

    /**
     * test patch for [ 1705925 ] Bug in URL-decoding of GET-Request-Parameters
     * http://sourceforge.net/tracker/index.php?func=detail&amp;aid=1705925&amp;group_id=6550&amp;atid=106550
     *
     * @return the method request parameters encoded with default character set UTF 8
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void getMethodRequestParametersEncodedWithDefaultCharacterSetUTF8() throws Exception {
        String hebrewValue = "\u05d0\u05d1\u05d2\u05d3";
        // encoded
        HttpUnitOptions.setDefaultCharacterSet("UTF-8");
        WebRequest wr = new GetMethodWebRequest("http://localhost/simple");
        wr.setParameter("param1", "red");
        wr.setParameter("param2", hebrewValue);
        ServletUnitHttpRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context,
                new Hashtable<>(), new byte[0]);
        assertEquals("red", request.getParameter("param1"), "param1 value");
        assertEquals(hebrewValue, request.getParameter("param2"), "param2 value");
    }

    /**
     * Notest inline query string.
     *
     * @throws Exception
     *             the exception
     */
    public void notestInlineQueryString() throws Exception { // TODO make this work
        WebRequest wr = new GetMethodWebRequest("http://localhost/simple?color=red&color=blue&age=12");
        HttpServletRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context, new Hashtable<>(),
                NO_MESSAGE_BODY);

        assertEquals("color=red&color=blue&age=12", request.getQueryString(), "query string");
    }

    /**
     * Request message body.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void requestMessageBody() throws Exception {
        String body = "12345678901234567890";
        InputStream stream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
        WebRequest wr = new PutMethodWebRequest("http://localhost/simple", stream, "text/plain");
        HttpServletRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context, new Hashtable<>(),
                body.getBytes(StandardCharsets.UTF_8));

        assertEquals(body.length(), request.getContentLength(), "Request content length");
        BufferedInputStream bis = new BufferedInputStream(request.getInputStream());
        byte[] buffer = new byte[request.getContentLength()];
        bis.read(buffer);
        assertEquals(body, new String(buffer), "Request content");
    }

    /**
     * Default attributes.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void defaultAttributes() throws Exception {
        WebRequest wr = new GetMethodWebRequest("http://localhost/simple");
        HttpServletRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context, new Hashtable<>(),
                NO_MESSAGE_BODY);

        assertNull(request.getAttribute("unset"), "attribute should not be defined yet");
        assertFalse(request.getAttributeNames().hasMoreElements(), "attribute enumeration should be empty");
    }

    /**
     * Non default attributes.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void nonDefaultAttributes() throws Exception {
        WebRequest wr = new GetMethodWebRequest("http://localhost/simple");
        HttpServletRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context, new Hashtable<>(),
                NO_MESSAGE_BODY);
        Object value = Integer.valueOf(1);

        request.setAttribute("one", value);

        assertEquals(value, request.getAttribute("one"), "attribute one");

        Enumeration names = request.getAttributeNames();
        assertTrue(names.hasMoreElements(), "attribute enumeration should not be empty");
        assertEquals("one", names.nextElement(), "contents in enumeration");
        assertFalse(names.hasMoreElements(), "attribute enumeration should now be empty");
    }

    /**
     * Duplicate attributes.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void duplicateAttributes() throws Exception {
        WebRequest wr = new GetMethodWebRequest("http://localhost/simple");
        HttpServletRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context, new Hashtable<>(),
                NO_MESSAGE_BODY);

        request.setAttribute("one", Integer.valueOf(1));
        request.setAttribute("one", "One");
        assertEquals("One", request.getAttribute("one"), "Revised attribute value");
    }

    /**
     * Null attribute value.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void nullAttributeValue() throws Exception {
        WebRequest wr = new GetMethodWebRequest("http://localhost/simple");
        HttpServletRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context, new Hashtable<>(),
                NO_MESSAGE_BODY);

        request.setAttribute("one", "One");
        assertEquals("One", request.getAttribute("one"), "Initial attribute value");
        request.setAttribute("one", null);
        assertNull(request.getAttribute("one"), "Attribute 'one' should have been removed");
    }

    /**
     * Default cookies.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void defaultCookies() throws Exception {
        WebRequest wr = new GetMethodWebRequest("http://localhost/simple");
        HttpServletRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context, new Hashtable<>(),
                NO_MESSAGE_BODY);
        Cookie[] cookies = request.getCookies();
        assertNull(cookies, "Unexpected cookies found");
    }

    /**
     * Sets the cookie via request header.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void setCookieViaRequestHeader() throws Exception {
        WebRequest wr = new GetMethodWebRequest("http://localhost/simple");
        wr.setHeaderField("Cookie", "flavor=vanilla,variety=sandwich");
        ServletUnitHttpRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context,
                new Hashtable<>(), NO_MESSAGE_BODY);

        Cookie[] cookies = request.getCookies();
        assertNotNull(cookies, "No cookies found");
        assertEquals(2, cookies.length, "Num cookies found");
        assertEquals("flavor", cookies[0].getName(), "Cookie 1 name");
        assertEquals("vanilla", cookies[0].getValue(), "Cookie 1 value");
        assertEquals("variety", cookies[1].getName(), "Cookie 2 name");
        assertEquals("sandwich", cookies[1].getValue(), "Cookie 2 value");
    }

    /**
     * Gets the session for first time.
     *
     * @return the session for first time
     *
     * @throws MalformedURLException
     *             the malformed URL exception
     */
    @Test
    void getSessionForFirstTime() throws MalformedURLException {
        WebRequest wr = new GetMethodWebRequest("http://localhost/simple");
        ServletUnitContext context = _context;
        assertEquals(0, context.getSessionIDs().size(), "Initial number of sessions in context");

        ServletUnitHttpRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, context,
                new Hashtable<>(), NO_MESSAGE_BODY);
        assertNull(request.getRequestedSessionId(), "New request should not have a request session ID");
        assertNull(request.getSession( /* create */ false), "New request should not have a session");
        assertEquals(0, context.getSessionIDs().size(),
                "Number of sessions in the context after request.getSession(false)");

        HttpSession session = request.getSession();
        assertNotNull(session, "No session created");
        assertTrue(session.isNew(), "Session not marked as new");
        assertEquals(1, context.getSessionIDs().size(), "Number of sessions in context after request.getSession()");
        assertSame(session, context.getSession(session.getId()), "Session with ID");
        assertNull(request.getRequestedSessionId(), "New request should still not have a request session ID");
    }

    /**
     * Test recognition of cookies defined on the client. is test case for [ 1151277 ] httpunit 1.6 breaks Cookie
     * handling for ServletUnitClient by Michael Corum
     *
     * @return the user cookies
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void getUserCookies() throws Exception {
        String FIRST_COOKIE = "RANDOM_COOKIE";
        String SECOND_COOKIE = "ANOTHER_COOKIE";
        String FIRST_COOKIE_VALUE = "cookie1";
        String SECOND_COOKIE_VALUE = "cookie2";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet("testServlet", "ServletName");
        ServletUnitClient m_sc = sr.newClient();
        m_sc.putCookie(FIRST_COOKIE, FIRST_COOKIE_VALUE);
        m_sc.putCookie(SECOND_COOKIE, SECOND_COOKIE_VALUE);

        InvocationContext invocation = m_sc.newInvocation("http://localhost/testServlet");
        HttpServletRequest requ = invocation.getRequest();

        Cookie[] cookies = requ.getCookies();
        assertEquals(2, cookies.length);
        Cookie firstActualCookie = cookies[0];
        Cookie secondActualCookie = cookies[1];

        assertEquals(FIRST_COOKIE, firstActualCookie.getName());
        assertEquals(SECOND_COOKIE, secondActualCookie.getName());

        assertEquals(FIRST_COOKIE_VALUE, firstActualCookie.getValue());
        assertEquals(SECOND_COOKIE_VALUE, secondActualCookie.getValue());
    }

    /**
     * Verifies that even when session creation is not explicitly requested, the inclusion of a session cookie will
     * cause a session to be made available.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void retrieveSession() throws Exception {
        ServletUnitContext context = _context;
        final ServletUnitHttpSession session = context.newSession();
        final String sessionID = session.getId();

        WebRequest wr = new GetMethodWebRequest("http://localhost/simple");
        wr.setHeaderField("Cookie", ServletUnitHttpSession.SESSION_COOKIE_NAME + '=' + sessionID);

        ServletUnitHttpRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, context,
                new Hashtable<>(), NO_MESSAGE_BODY);
        assertEquals(sessionID, request.getRequestedSessionId(), "Requested session ID defined in request");

        assertSame(session, request.getSession( /* create */ false), "Session returned when creation not requested");
        assertSame(session, request.getSession(true), "Session returned when creation requested");
    }

    /**
     * Access forbidden to invalid session.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void accessForbiddenToInvalidSession() throws Exception {
        ServletUnitContext context = _context;

        HttpSession session = context.newSession();
        session.setAttribute("Initial", Integer.valueOf(1));
        Enumeration attributeNames = session.getAttributeNames();
        assertTrue(attributeNames.hasMoreElements());
        assertEquals("Initial", attributeNames.nextElement());

        session.invalidate();

        try {
            session.getAttributeNames().hasMoreElements();
            fail("Should not be able to access an invalid session's attributes");
        } catch (IllegalStateException ex) {
        }

        try {
            session.getAttribute("Initial");
            fail("Should not be able to access an invalid session's attributes");
        } catch (IllegalStateException ex) {
        }
    }

    /**
     * Verifies that a request for a session when the current one is invalid will result in a new session.
     * <p/>
     * Obtains a new session, invalidates it, and verifies that
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void sessionInvalidation() throws Exception {
        ServletUnitContext context = _context;
        HttpSession originalSession = context.newSession();
        String originalID = originalSession.getId();

        WebRequest wr = new GetMethodWebRequest("http://localhost/simple");
        wr.setHeaderField("Cookie", ServletUnitHttpSession.SESSION_COOKIE_NAME + '=' + originalID);

        ServletUnitHttpRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, context,
                new Hashtable<>(), NO_MESSAGE_BODY);
        originalSession.setAttribute("Initial", Integer.valueOf(1));
        Enumeration attributeNames = originalSession.getAttributeNames();
        assertTrue(attributeNames.hasMoreElements());
        assertEquals("Initial", attributeNames.nextElement());

        originalSession.invalidate();

        assertNull(request.getSession(false), "Invalidated session returned");

        HttpSession newSession = request.getSession(true);
        assertNotNull(newSession, "getSession(true) did not return a session");
        assertNotSame(originalSession, newSession, "getSession(true) returned the original invalidated session");
        assertSame(newSession, request.getSession(false), "session returned by getSession(false)");
        assertSame(newSession, context.getSession(newSession.getId()), "Session in context with new ID");
    }

    /**
     * Verifies that a request with a bad session ID causes a new session to be generated only when explicitly
     * requested.
     *
     * @return the session with bad cookie
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void getSessionWithBadCookie() throws Exception {
        ServletUnitContext context = _context;
        HttpSession originalSession = context.newSession();
        String originalID = originalSession.getId();

        WebRequest wr = new GetMethodWebRequest("http://localhost/simple");
        wr.setHeaderField("Cookie", ServletUnitHttpSession.SESSION_COOKIE_NAME + '=' + originalID);

        ServletUnitHttpRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, context,
                new Hashtable<>(), NO_MESSAGE_BODY);
        request.getSession();

        wr.setHeaderField("Cookie", ServletUnitHttpSession.SESSION_COOKIE_NAME + '=' + originalID + "BAD");
        request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, context, new Hashtable<>(), NO_MESSAGE_BODY);

        assertNull(request.getSession(false), "Unexpected session returned for bad cookie");
        assertNotNull(request.getSession(true), "Should have returned session when asked");
        assertNotSame(originalSession, request.getSession(true), "Created session");
    }

    /**
     * test getting the uri.
     *
     * @param uri
     *            the uri
     * @param path
     *            the path
     *
     * @throws Exception
     *             the exception
     */
    public void testGetRequestURI(String uri, String path) throws Exception {
        /*
         * http://de.wikipedia.org/wiki/Uniform_Resource_Identifier foo://example.com:8042/over/there?name=ferret#nose
         * \_/ \______________/\_________/ \_________/ \__/ | | | | | scheme authority path query fragment
         */
        ServletUnitContext context = _context;
        WebRequest wr = new GetMethodWebRequest(uri);

        ServletUnitHttpRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, context,
                new Hashtable<>(), NO_MESSAGE_BODY);
        assertEquals(path, request.getRequestURI());
        assertEquals(uri, request.getRequestURL().toString());

        wr = new GetMethodWebRequest(uri + "?foo=bar");
        request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, context, new Hashtable<>(), NO_MESSAGE_BODY);
        assertEquals(path, request.getRequestURI());
        assertEquals(uri, request.getRequestURL().toString());
    }

    /**
     * Gets the request URI.
     *
     * @return the request URI
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void getRequestURI() throws Exception {
        testGetRequestURI("http://localhost/simple", "/simple");
    }

    /**
     * test for BR 3076917 Missing port number in URL from ServletUnitHttpRequest.
     *
     * @return the request URI with port
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void getRequestURIWithPort() throws Exception {
        testGetRequestURI("http://localhost:8042/simple8042", "/simple8042");
    }

    /**
     * Default locale.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void defaultLocale() throws Exception {
        WebRequest wr = new GetMethodWebRequest("http://localhost/simple");

        ServletUnitHttpRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context,
                new Hashtable<>(), NO_MESSAGE_BODY);
        Locale[] expectedLocales = { Locale.getDefault() };
        verifyLocales(request, expectedLocales);

    }

    /**
     * verify the secure property and scheme http/https extended test for bug report [ 1165454 ]
     * ServletUnitHttpRequest.getScheme() returns "http" for secure by Jeff Mills
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void secureProperty() throws Exception {
        WebRequest wr = new GetMethodWebRequest("http://localhost/simple");
        ServletUnitHttpRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context,
                new Hashtable<>(), NO_MESSAGE_BODY);
        assertFalse(request.isSecure(), "Incorrectly noted request as secure");
        assertEquals("http", request.getScheme(), "http");

        WebRequest secureReq = new GetMethodWebRequest("https://localhost/simple");
        request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, secureReq, _context, new Hashtable<>(),
                NO_MESSAGE_BODY);
        assertTrue(request.isSecure(), "Request not marked as secure");
        assertEquals("https", request.getScheme(), "https");

        wr = new GetMethodWebRequest("ftp://localhost/simple");
        request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context, new Hashtable<>(), NO_MESSAGE_BODY);
        assertFalse(request.isSecure(), "Incorrectly noted request as secure");
        assertEquals("ftp", request.getScheme(), "ftp");

        secureReq = new GetMethodWebRequest("ftps://localhost/simple");
        try {
            request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, secureReq, _context, new Hashtable<>(),
                    NO_MESSAGE_BODY);
            assertTrue(request.isSecure(), "Request not marked as secure");
            assertEquals("ftps", request.getScheme(), "ftps");
        } catch (java.net.MalformedURLException mue) {
            // as of 2008-03 this happends - I'm not sure whether that should be expected WF
            String msg = mue.getMessage();
            // System.err.println(msg);
            assertTrue(msg.indexOf("unknown protocol: ftps") >= 0, "ftps is not a known protocol");
        }
    }

    /**
     * Verify locales.
     *
     * @param request
     *            the request
     * @param expectedLocales
     *            the expected locales
     */
    private void verifyLocales(ServletUnitHttpRequest request, Locale[] expectedLocales) {
        assertNotNull(request.getLocale(), "No default locale found");
        assertEquals(expectedLocales[0], request.getLocale(), "default locale");

        final Enumeration locales = request.getLocales();
        assertNotNull(locales, "local enumeration not returned");
        for (int i = 0; i < expectedLocales.length; i++) {
            assertTrue(locales.hasMoreElements(), "Expected " + expectedLocales.length + " locales, only found " + i);
            assertEquals(expectedLocales[i], locales.nextElement(), "Locale #" + (i + 1));
        }
        assertFalse(locales.hasMoreElements(), "Too many locales returned");
    }

    /**
     * Specified locales.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void specifiedLocales() throws Exception {
        WebRequest wr = new GetMethodWebRequest("http://localhost/simple");
        wr.setHeaderField("Accept-language", "fr, en;q=0.6, en-us;q=0.7");

        ServletUnitHttpRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context,
                new Hashtable<>(), NO_MESSAGE_BODY);
        verifyLocales(request, new Locale[] { Locale.FRENCH, Locale.US, Locale.ENGLISH });
    }

    /**
     * Gets the input stream same object.
     *
     * @return the input stream same object
     *
     * @throws Exception
     *             the exception
     */
    /*
     * Test for patch [ 1246438 ] For issue 1221537; ServletUnitHttpRequest.getReader not impl by Tim
     */
    @Test
    void getInputStreamSameObject() throws Exception {
        byte[] bytes = {};
        InputStream stream = new ByteArrayInputStream(bytes);
        WebRequest wr = new PostMethodWebRequest("http://localhost/simple", stream, "text/plain");
        HttpServletRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context, new Hashtable<>(),
                bytes);

        InputStream first = request.getInputStream();
        InputStream second = request.getInputStream();
        assertSame(first, second, "Different InputStreams");
    }

    /**
     * Gets the input stream after get reader.
     *
     * @return the input stream after get reader
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void getInputStreamAfterGetReader() throws Exception {
        byte[] bytes = {};
        InputStream stream = new ByteArrayInputStream(bytes);
        WebRequest wr = new PostMethodWebRequest("http://localhost/simple", stream, "text/plain");
        HttpServletRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context, new Hashtable<>(),
                bytes);

        request.getReader();
        try {
            request.getInputStream();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    /**
     * Gets the input stream.
     *
     * @return the input stream
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void getInputStream() throws Exception {
        String body = "12345678901234567890";
        InputStream stream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
        WebRequest wr = new PostMethodWebRequest("http://localhost/simple", stream, "text/plain");
        HttpServletRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context, new Hashtable<>(),
                body.getBytes(StandardCharsets.UTF_8));

        BufferedInputStream bis = new BufferedInputStream(request.getInputStream());
        byte[] buffer = new byte[request.getContentLength()];
        bis.read(buffer);
        assertEquals(body, new String(buffer, StandardCharsets.UTF_8), "Request content");
    }

    /**
     * Gets the reader same object.
     *
     * @return the reader same object
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void getReaderSameObject() throws Exception {
        byte[] bytes = {};
        InputStream stream = new ByteArrayInputStream(bytes);
        WebRequest wr = new PostMethodWebRequest("http://localhost/simple", stream, "text/plain");
        HttpServletRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context, new Hashtable<>(),
                bytes);

        BufferedReader first = request.getReader();
        BufferedReader second = request.getReader();
        assertSame(first, second, "Different Readers");
    }

    /**
     * Gets the reader after get input stream.
     *
     * @return the reader after get input stream
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void getReaderAfterGetInputStream() throws Exception {
        byte[] bytes = {};
        InputStream stream = new ByteArrayInputStream(bytes);
        WebRequest wr = new PostMethodWebRequest("http://localhost/simple", stream, "text/plain");
        HttpServletRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context, new Hashtable<>(),
                bytes);

        request.getInputStream();
        try {
            request.getReader();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    /**
     * Gets the reader default charset.
     *
     * @return the reader default charset
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void getReaderDefaultCharset() throws Exception {
        String body = "12345678901234567890";
        InputStream stream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
        WebRequest wr = new PostMethodWebRequest("http://localhost/simple", stream, "text/plain");
        HttpServletRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context, new Hashtable<>(),
                body.getBytes(StandardCharsets.UTF_8));

        char[] buffer = new char[body.length()];
        request.getReader().read(buffer);
        assertEquals(body, new String(buffer), "Request content");
    }

    /**
     * test the reader with a Specific Character set (here UTF-8).
     *
     * @throws Exception
     *             FIXME make work an switch back on
     */
    public void xtestGetReaderSpecificCharset() throws Exception {
        String body = "\u05d0\u05d1\u05d2\u05d3";
        InputStream stream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
        WebRequest wr = new PostMethodWebRequest("http://localhost/simple", stream, "text/plain; charset=UTF-8");
        HttpServletRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context, new Hashtable<>(),
                body.getBytes(StandardCharsets.UTF_8));

        char[] buffer = new char[body.length()];
        request.getReader().read(buffer);
        assertEquals(body, new String(buffer), "Request content");
    }

    /**
     * test the specific character encoding (here hebrew).
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void specifiedCharEncoding() throws Exception {
        String hebrewValue = "\u05d0\u05d1\u05d2\u05d3";
        String paramString = "param1=red&param2=%E0%E1%E2%E3"; // use ISO-8859-8 to encode the data
        WebRequest wr = new PostMethodWebRequest("http://localhost/simple");
        wr.setHeaderField("Content-Type", "application/x-www-form-urlencoded; charset=ISO-8859-8");
        ServletUnitHttpRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context,
                new Hashtable<>(), paramString.getBytes(StandardCharsets.ISO_8859_1));
        assertEquals("red", request.getParameter("param1"), "param1 value");
        assertEquals(hebrewValue, request.getParameter("param2"), "param2 value");
    }

    /** The dummyfactory. */
    private InvocationContextFactory _dummyfactory = new InvocationContextFactory() {
        @Override
        public InvocationContext newInvocation(ServletUnitClient client, FrameSelector targetFrame, WebRequest request,
                Dictionary clientHeaders, byte[] messageBody) throws IOException, MalformedURLException {
            return new InvocationContextImpl(client, null, targetFrame, request, clientHeaders, messageBody);
        }

        @Override
        public HttpSession getSession(String sessionId, boolean create) {
            return _context.getValidSession(sessionId, null, create);
        }
    };

    /**
     * Specified char encoding 2.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void specifiedCharEncoding2() throws Exception {
        String hebrewValue = "\u05d0\u05d1\u05d2\u05d3";
        HttpUnitOptions.setDefaultCharacterSet("ISO-8859-8");
        WebRequest wr = new PostMethodWebRequest("http://localhost/simple");
        wr.setParameter("param1", "red");
        wr.setParameter("param2", hebrewValue);
        wr.setHeaderField("Content-Type", "application/x-www-form-urlencoded; charset=ISO-8859-8");
        ServletUnitClient client = ServletUnitClient.newClient(_dummyfactory);
        ByteArrayOutputStream messageBody = client.getMessageBody(wr);
        ServletUnitHttpRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context,
                new Hashtable<>(), messageBody.toByteArray());
        String parameter = request.getParameter("param2");
        assertEquals(hebrewValue, parameter, "param2 value");
        assertEquals("red", request.getParameter("param1"), "param1 value");
    }

    /**
     * Supplied char encoding.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void suppliedCharEncoding() throws Exception { // xxx turn this back on
        String hebrewValue = "\u05d0\u05d1\u05d2\u05d3";
        String paramString = "param1=red&param2=%E0%E1%E2%E3"; // use ISO-8859-8 to encode the data, then string is URL
        // encoded
        WebRequest wr = new PostMethodWebRequest("http://localhost/simple");
        ServletUnitHttpRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context,
                new Hashtable<>(), paramString.getBytes(StandardCharsets.ISO_8859_1));
        request.setCharacterEncoding("ISO-8859-8");
        assertEquals("red", request.getParameter("param1"), "param1 value");
        assertEquals(hebrewValue, request.getParameter("param2"), "param2 value");
    }

    /**
     * test for getServerPort and getServerName by Antoine Vernois.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void defaultHttpServerPort() throws Exception {
        WebRequest wr = new GetMethodWebRequest("http://localhost/simple");
        HttpServletRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context, new Hashtable<>(),
                NO_MESSAGE_BODY);
        int serverPort = request.getServerPort();
        assertEquals(80, serverPort, "default http server port");
    }

    /**
     * test for getServerPort and getServerName by Antoine Vernois.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void suppliedHttpServerPort() throws Exception {
        WebRequest wr = new GetMethodWebRequest("http://localhost:8080/simple");
        HttpServletRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context, new Hashtable<>(),
                NO_MESSAGE_BODY);
        int serverPort = request.getServerPort();
        assertEquals(8080, serverPort, "supplied http server port");
    }

    /**
     * test for getServerPort and getServerName by Antoine Vernois.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void serverName() throws Exception {
        WebRequest wr = new GetMethodWebRequest("http://myhost:8080/simple");
        HttpServletRequest request = new ServletUnitHttpRequest(NULL_SERVLET_REQUEST, wr, _context, new Hashtable<>(),
                NO_MESSAGE_BODY);
        String serverName = request.getServerName();
        assertEquals("myhost", serverName, "server name");
    }

    /** The Constant NO_MESSAGE_BODY. */
    private static final byte[] NO_MESSAGE_BODY = {};

    /** The Constant NULL_SERVLET_REQUEST. */
    private static final ServletMetaData NULL_SERVLET_REQUEST = new ServletMetaData() {

        @Override
        public Servlet getServlet() throws ServletException {
            return null;
        }

        @Override
        public String getServletPath() {
            return null;
        }

        @Override
        public String getPathInfo() {
            return null;
        }

        @Override
        public FilterMetaData[] getFilters() {
            return null;
        }
    };
}

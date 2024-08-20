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

import com.meterware.httpunit.HttpUnitTest;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConnection;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * @author <a href="russgold@httpunit.org">Russell Gold</a>
 */

class RequestContextTest extends HttpUnitTest {

    /**
     * Verify parsing of a query string.
     */
    @Test
    void testQueryStringParsing() throws Exception {
        RequestContext rc = new RequestContext(new URL("http://localhost/basic?param=red&param1=old&param=blue"));
        assertMatchingSet("parameter names", new String[] { "param", "param1" }, rc.getParameterNames());
        assertMatchingSet("param values", new String[] { "red", "blue" }, rc.getParameterValues("param"));
        assertEquals("old", ((String[]) rc.getParameterMap().get("param1"))[0], "param1 value");
    }

    /**
     * Verify override of parent request parameters.
     */
    @Test
    void testParameterOverride() throws Exception {
        HttpServletRequest request = new DummyHttpServletRequest(
                new URL("http://localhost/basic?param=red&param1=old&param=blue"));
        RequestContext context = new RequestContext(new URL("http://localhost/second?param=yellow&param2=fast"));
        context.setParentRequest(request);
        assertMatchingSet("parameter names", new String[] { "param", "param1", "param2" }, context.getParameterNames());
        assertMatchingSet("param values", new String[] { "yellow" }, context.getParameterValues("param"));
        assertEquals("old", ((String[]) context.getParameterMap().get("param1"))[0], "param1 value");
    }

    /**
     * Verify parsing of message body parameters.
     */
    @Test
    void testPostParameterParsing() throws Exception {
        RequestContext rc = new RequestContext(new URL("http://localhost/basic"));
        rc.setMessageBody("param=red&param1=old&param=blue".getBytes(StandardCharsets.UTF_8));
        assertMatchingSet("parameter names", new String[] { "param", "param1" }, rc.getParameterNames());
        assertMatchingSet("param values", new String[] { "red", "blue" }, rc.getParameterValues("param"));
        assertEquals("old", ((String[]) rc.getParameterMap().get("param1"))[0], "param1 value");
    }

    /**
     * Verify parsing of message body parameters using a specified character encoding.
     */
    @Test
    void testEncodedParameterParsing() throws Exception {
        RequestContext rc = new RequestContext(new URL("http://localhost/basic"));
        String hebrewValue = "\u05d0\u05d1\u05d2\u05d3";
        String paramString = "param=red&param1=%E0%E1%E2%E3&param=blue";
        rc.setMessageBody(paramString.getBytes(StandardCharsets.ISO_8859_1));
        rc.setMessageEncoding("iso-8859-8");
        assertMatchingSet("parameter names", new String[] { "param", "param1" }, rc.getParameterNames());
        assertMatchingSet("param values", new String[] { "red", "blue" }, rc.getParameterValues("param"));
        assertEquals(hebrewValue, ((String[]) rc.getParameterMap().get("param1"))[0], "param1 value");
    }

    class DummyHttpServletRequest implements HttpServletRequest {

        private RequestContext _requestContext;

        public DummyHttpServletRequest(URL requestURL) {
            _requestContext = new RequestContext(requestURL);
        }

        @Override
        public String getAuthType() {
            return null;
        }

        @Override
        public Cookie[] getCookies() {
            return new Cookie[0];
        }

        @Override
        public long getDateHeader(String s) {
            return 0;
        }

        @Override
        public String getHeader(String s) {
            return null;
        }

        @Override
        public Enumeration getHeaders(String s) {
            return null;
        }

        @Override
        public Enumeration getHeaderNames() {
            return null;
        }

        @Override
        public int getIntHeader(String s) {
            return 0;
        }

        @Override
        public String getMethod() {
            return null;
        }

        @Override
        public String getPathInfo() {
            return null;
        }

        @Override
        public String getPathTranslated() {
            return null;
        }

        @Override
        public String getContextPath() {
            return null;
        }

        @Override
        public String getQueryString() {
            return null;
        }

        @Override
        public String getRemoteUser() {
            return null;
        }

        @Override
        public boolean isUserInRole(String s) {
            return false;
        }

        @Override
        public Principal getUserPrincipal() {
            return null;
        }

        @Override
        public String getRequestedSessionId() {
            return null;
        }

        @Override
        public String getRequestURI() {
            return null;
        }

        @Override
        public StringBuffer getRequestURL() {
            return null;
        }

        @Override
        public String getServletPath() {
            return null;
        }

        @Override
        public HttpSession getSession(boolean b) {
            return null;
        }

        @Override
        public HttpSession getSession() {
            return null;
        }

        @Override
        public boolean isRequestedSessionIdValid() {
            return false;
        }

        @Override
        public boolean isRequestedSessionIdFromCookie() {
            return false;
        }

        @Override
        public boolean isRequestedSessionIdFromURL() {
            return false;
        }

        @Override
        public Object getAttribute(String s) {
            return null;
        }

        @Override
        public Enumeration getAttributeNames() {
            return null;
        }

        @Override
        public String getCharacterEncoding() {
            return null;
        }

        @Override
        public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
        }

        @Override
        public int getContentLength() {
            return 0;
        }

        @Override
        public String getContentType() {
            return null;
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            return null;
        }

        @Override
        public String getParameter(String s) {
            return _requestContext.getParameter(s);
        }

        @Override
        public Enumeration getParameterNames() {
            return _requestContext.getParameterNames();
        }

        @Override
        public String[] getParameterValues(String s) {
            return _requestContext.getParameterValues(s);
        }

        @Override
        public Map getParameterMap() {
            return _requestContext.getParameterMap();
        }

        @Override
        public String getProtocol() {
            return null;
        }

        @Override
        public String getScheme() {
            return null;
        }

        @Override
        public String getServerName() {
            return null;
        }

        @Override
        public int getServerPort() {
            return 0;
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return null;
        }

        @Override
        public String getRemoteAddr() {
            return null;
        }

        @Override
        public String getRemoteHost() {
            return null;
        }

        @Override
        public void setAttribute(String s, Object o) {
        }

        @Override
        public void removeAttribute(String s) {
        }

        @Override
        public Locale getLocale() {
            return null;
        }

        @Override
        public Enumeration getLocales() {
            return null;
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public RequestDispatcher getRequestDispatcher(String s) {
            return null;
        }

        @Override
        public int getRemotePort() {
            return 0; // To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getLocalName() {
            return null; // To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getLocalAddr() {
            return null; // To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public int getLocalPort() {
            return 0; // To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public ServletContext getServletContext() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public AsyncContext startAsync() throws IllegalStateException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
                throws IllegalStateException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isAsyncStarted() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isAsyncSupported() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public AsyncContext getAsyncContext() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public DispatcherType getDispatcherType() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void login(String username, String password) throws ServletException {
            // TODO Auto-generated method stub

        }

        @Override
        public void logout() throws ServletException {
            // TODO Auto-generated method stub

        }

        @Override
        public Collection<Part> getParts() throws IOException, ServletException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Part getPart(String name) throws IOException, ServletException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getContentLengthLong() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public String changeSessionId() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getRequestId() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getProtocolRequestId() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ServletConnection getServletConnection() {
            // TODO Auto-generated method stub
            return null;
        }
    }

}

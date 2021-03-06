package com.meterware.servletunit;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2003, Russell Gold
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
 *
 *******************************************************************************************************************/

import com.meterware.httpunit.HttpUnitTest;
import org.junit.Test;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertEquals;


/**
 * @author <a href="russgold@httpunit.org">Russell Gold</a>
 */

public class RequestContextTest extends HttpUnitTest {

    /**
     * Verify parsing of a query string.
     */
    @Test
    public void testQueryStringParsing() throws Exception {
        RequestContext rc = new RequestContext(new URL("http://localhost/basic?param=red&param1=old&param=blue"));
        assertMatchingSet("parameter names", new String[]{"param", "param1"}, rc.getParameterNames());
        assertMatchingSet("param values", new String[]{"red", "blue"}, rc.getParameterValues("param"));
        assertEquals("param1 value", "old", ((String[]) rc.getParameterMap().get("param1"))[0]);
    }


    /**
     * Verify override of parent request parameters.
     */
    @Test
    public void testParameterOverride() throws Exception {
        HttpServletRequest request = new DummyHttpServletRequest(new URL("http://localhost/basic?param=red&param1=old&param=blue"));
        RequestContext context = new RequestContext(new URL("http://localhost/second?param=yellow&param2=fast"));
        context.setParentRequest(request);
        assertMatchingSet("parameter names", new String[]{"param", "param1", "param2"}, context.getParameterNames());
        assertMatchingSet("param values", new String[]{"yellow"}, context.getParameterValues("param"));
        assertEquals("param1 value", "old", ((String[]) context.getParameterMap().get("param1"))[0]);
    }


    /**
     * Verify parsing of message body parameters.
     */
    @Test
    public void testPostParameterParsing() throws Exception {
        RequestContext rc = new RequestContext(new URL("http://localhost/basic"));
        rc.setMessageBody("param=red&param1=old&param=blue".getBytes());
        assertMatchingSet("parameter names", new String[]{"param", "param1"}, rc.getParameterNames());
        assertMatchingSet("param values", new String[]{"red", "blue"}, rc.getParameterValues("param"));
        assertEquals("param1 value", "old", ((String[]) rc.getParameterMap().get("param1"))[0]);
    }


    /**
     * Verify parsing of message body parameters using a specified character encoding.
     */
    @Test
    public void testEncodedParameterParsing() throws Exception {
        RequestContext rc = new RequestContext(new URL("http://localhost/basic"));
        String hebrewValue = "\u05d0\u05d1\u05d2\u05d3";
        String paramString = "param=red&param1=%E0%E1%E2%E3&param=blue";
        rc.setMessageBody(paramString.getBytes("iso-8859-1"));
        rc.setMessageEncoding("iso-8859-8");
        assertMatchingSet("parameter names", new String[]{"param", "param1"}, rc.getParameterNames());
        assertMatchingSet("param values", new String[]{"red", "blue"}, rc.getParameterValues("param"));
        assertEquals("param1 value", hebrewValue, ((String[]) rc.getParameterMap().get("param1"))[0]);
    }


    class DummyHttpServletRequest implements HttpServletRequest {

        private RequestContext _requestContext;


        public DummyHttpServletRequest(URL requestURL) {
            _requestContext = new RequestContext(requestURL);
        }


        public String getAuthType() {
            return null;
        }


        public Cookie[] getCookies() {
            return new Cookie[0];
        }


        public long getDateHeader(String s) {
            return 0;
        }


        public String getHeader(String s) {
            return null;
        }


        public Enumeration getHeaders(String s) {
            return null;
        }


        public Enumeration getHeaderNames() {
            return null;
        }


        public int getIntHeader(String s) {
            return 0;
        }


        public String getMethod() {
            return null;
        }


        public String getPathInfo() {
            return null;
        }


        public String getPathTranslated() {
            return null;
        }


        public String getContextPath() {
            return null;
        }


        public String getQueryString() {
            return null;
        }


        public String getRemoteUser() {
            return null;
        }


        public boolean isUserInRole(String s) {
            return false;
        }


        public Principal getUserPrincipal() {
            return null;
        }


        public String getRequestedSessionId() {
            return null;
        }


        public String getRequestURI() {
            return null;
        }


        public StringBuffer getRequestURL() {
            return null;
        }


        public String getServletPath() {
            return null;
        }


        public HttpSession getSession(boolean b) {
            return null;
        }


        public HttpSession getSession() {
            return null;
        }


        public boolean isRequestedSessionIdValid() {
            return false;
        }


        public boolean isRequestedSessionIdFromCookie() {
            return false;
        }


        public boolean isRequestedSessionIdFromURL() {
            return false;
        }


        public boolean isRequestedSessionIdFromUrl() {
            return false;
        }


        public Object getAttribute(String s) {
            return null;
        }


        public Enumeration getAttributeNames() {
            return null;
        }


        public String getCharacterEncoding() {
            return null;
        }


        public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
        }


        public int getContentLength() {
            return 0;
        }


        public String getContentType() {
            return null;
        }


        public ServletInputStream getInputStream() throws IOException {
            return null;
        }


        public String getParameter(String s) {
            return _requestContext.getParameter(s);
        }


        public Enumeration getParameterNames() {
            return _requestContext.getParameterNames();
        }


        public String[] getParameterValues(String s) {
            return _requestContext.getParameterValues(s);
        }


        public Map getParameterMap() {
            return _requestContext.getParameterMap();
        }


        public String getProtocol() {
            return null;
        }


        public String getScheme() {
            return null;
        }


        public String getServerName() {
            return null;
        }


        public int getServerPort() {
            return 0;
        }


        public BufferedReader getReader() throws IOException {
            return null;
        }


        public String getRemoteAddr() {
            return null;
        }


        public String getRemoteHost() {
            return null;
        }


        public void setAttribute(String s, Object o) {
        }


        public void removeAttribute(String s) {
        }


        public Locale getLocale() {
            return null;
        }


        public Enumeration getLocales() {
            return null;
        }


        public boolean isSecure() {
            return false;
        }


        public RequestDispatcher getRequestDispatcher(String s) {
            return null;
        }


        public String getRealPath(String s) {
            return null;
        }

        public int getRemotePort() {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public String getLocalName() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public String getLocalAddr() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public int getLocalPort() {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }


        public ServletContext getServletContext() {
          // TODO Auto-generated method stub
          return null;
        }


        public AsyncContext startAsync() throws IllegalStateException {
          // TODO Auto-generated method stub
          return null;
        }


        public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
            throws IllegalStateException {
          // TODO Auto-generated method stub
          return null;
        }


        public boolean isAsyncStarted() {
          // TODO Auto-generated method stub
          return false;
        }


        public boolean isAsyncSupported() {
          // TODO Auto-generated method stub
          return false;
        }


        public AsyncContext getAsyncContext() {
          // TODO Auto-generated method stub
          return null;
        }


        public DispatcherType getDispatcherType() {
          // TODO Auto-generated method stub
          return null;
        }


        public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
          // TODO Auto-generated method stub
          return false;
        }


        public void login(String username, String password) throws ServletException {
          // TODO Auto-generated method stub
          
        }


        public void logout() throws ServletException {
          // TODO Auto-generated method stub
          
        }


        public Collection<Part> getParts() throws IOException, ServletException {
          // TODO Auto-generated method stub
          return null;
        }


        public Part getPart(String name) throws IOException, ServletException {
          // TODO Auto-generated method stub
          return null;
        }
    }


}

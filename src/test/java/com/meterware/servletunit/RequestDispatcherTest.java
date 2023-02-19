/*
 * MIT License
 *
 * Copyright 2011-2023 Russell Gold
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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * test the Request Dispatcher
 */
class RequestDispatcherTest {

    final String outerServletName = "something/interesting";
    final String innerServletName = "something/more";
    final String decodeExampleName = "repository/Default%20repository";
    final String errorPageServletName = "errorPage";

    final static String REQUEST_URI = "javax.servlet.include.request_uri";
    final static String CONTEXT_PATH = "javax.servlet.include.context_path";
    final static String SERVLET_PATH = "javax.servlet.include.servlet_path";
    final static String PATH_INFO = "javax.servlet.include.path_info";
    final static String QUERY_STRING = "javax.servlet.include.query_string";

    private ServletRunner _runner;
    private WebXMLString _wxs;

    /**
     * prepare the test
     */
    @BeforeEach
    void setUp() throws Exception {
        _wxs = new WebXMLString();
        _wxs.addServlet(outerServletName, RequestDispatcherServlet.class);
        _wxs.addServlet(decodeExampleName, RequestDispatcherServlet.class);
        _wxs.addServlet(innerServletName, IncludedServlet.class);
        _wxs.addServlet(errorPageServletName, ErrorPageServlet.class);
        _runner = new ServletRunner(_wxs.asInputStream(), "/sample");
    }

    @Test
    void testRequestDispatcherParameters() throws Exception {
        InvocationContext ic = _runner.newClient()
                .newInvocation("http://localhost/sample/" + outerServletName + "?param=original&param1=first");

        final HttpServletRequest request = ic.getRequest();
        final HttpServletResponse response = ic.getResponse();
        RequestDispatcherServlet servlet = (RequestDispatcherServlet) ic.getServlet();
        RequestDispatcher rd = servlet.getServletContext()
                .getRequestDispatcher("/" + innerServletName + "?param=revised&param2=new");

        assertEquals("original", request.getParameter("param"), "param");
        assertEquals("first", request.getParameter("param1"), "param1");
        assertNull(request.getParameter("param2"), "param2 should not be defined");

        ic.pushIncludeRequest(rd, request, response);

        final HttpServletRequest innerRequest = ic.getRequest();
        assertEquals("revised", innerRequest.getParameter("param"), "param in included servlet");
        assertEquals("first", innerRequest.getParameter("param1"), "param1 in included servlet");
        assertEquals("new", innerRequest.getParameter("param2"), "param2 in included servlet");

        assertEquals(IncludedServlet.class, ic.getServlet().getClass(), "Included servlet class");

        ic.popRequest();

        final HttpServletRequest restoredRequest = ic.getRequest();
        assertEquals("original", restoredRequest.getParameter("param"), "reverted param");
        assertEquals("first", restoredRequest.getParameter("param1"), "reverted param1");
        assertNull(restoredRequest.getParameter("param2"), "reverted param2 should not be defined");
        assertEquals(RequestDispatcherServlet.class, ic.getServlet().getClass(), "Included servlet class");
    }

    @Test
    void testRequestDispatcherIncludePaths() throws Exception {
        InvocationContext ic = _runner.newClient()
                .newInvocation("http://localhost/sample/" + outerServletName + "?param=original&param1=first");

        final HttpServletRequest request = ic.getRequest();
        RequestDispatcherServlet servlet = (RequestDispatcherServlet) ic.getServlet();
        RequestDispatcher rd = servlet.getServletContext()
                .getRequestDispatcher("/" + innerServletName + "?param=revised&param2=new");

        assertEquals("/sample/" + outerServletName, request.getRequestURI(), "request URI");
        assertEquals("/sample", request.getContextPath(), "context path attribute");
        assertEquals("/" + outerServletName, request.getServletPath(), "servlet path attribute");
        assertNull(request.getPathInfo(), "path info not null attribute");
        // assertEquals( "query string attribute", "param=original&param1=first", request.getQueryString() ); TODO make
        // this work

        final HttpServletResponse response = ic.getResponse();
        ic.pushIncludeRequest(rd, request, response);

        final HttpServletRequest innerRequest = ic.getRequest();
        assertEquals("/sample/" + outerServletName, innerRequest.getRequestURI(), "request URI");
        assertEquals("/sample", innerRequest.getContextPath(), "context path attribute");
        assertEquals("/" + outerServletName, innerRequest.getServletPath(), "servlet path attribute");
        assertNull(innerRequest.getPathInfo(), "path info not null attribute");
        // assertEquals( "query string attribute", "param=original&param1=first", innerRequest.getQueryString() );

        assertEquals("/sample/" + innerServletName, innerRequest.getAttribute(REQUEST_URI), "request URI attribute");
        assertEquals("/sample", innerRequest.getAttribute(CONTEXT_PATH), "context path attribute");
        assertEquals("/" + innerServletName, innerRequest.getAttribute(SERVLET_PATH), "servlet path attribute");
        assertNull(innerRequest.getAttribute(PATH_INFO), "path info attribute not null");
        // assertEquals( "query string attribute", "param=revised&param2=new", innerRequest.getAttribute( QUERY_STRING )
        // );

        ic.popRequest();
        final HttpServletRequest restoredRequest = ic.getRequest();

        assertNull(restoredRequest.getAttribute(REQUEST_URI), "reverted URI attribute not null");
        assertNull(restoredRequest.getAttribute(CONTEXT_PATH), "context path attribute not null");
        assertNull(restoredRequest.getAttribute(SERVLET_PATH), "servlet path attribute not null");
        assertNull(restoredRequest.getAttribute(PATH_INFO), "path info attribute not null");
        // assertNull( "query string attribute not null", "param=revised&param2=new", restoredRequest.getAttribute(
        // QUERY_STRING ) );
    }

    /**
     * test for fix of bug [ 1323031 ] getPathInfo does not decode request URL by Hugh Winkler -
     */
    @Test
    void testDecodeRequestURL() throws Exception {
        InvocationContext ic = _runner.newClient().newInvocation("http://localhost/sample/" + decodeExampleName);

        final HttpServletRequest request = ic.getRequest();
        RequestDispatcherServlet servlet = (RequestDispatcherServlet) ic.getServlet();
        RequestDispatcher rd = servlet.getServletContext()
                .getRequestDispatcher("/" + innerServletName + "?param=revised&param2=new");
        String path = request.getPathInfo();
        String servletPath = request.getServletPath();
        // System.err.println("servletPath='"+servletPath+"'\npath='"+path+"'");
        assertEquals("/repository/Default repository", servletPath);
    }

    /**
     * test for implementation of getNamedDispatcher as patched up by Izzy Alanis
     *
     * @throws Exception
     */
    @Test
    void testGetNamedDispatcher() throws Exception {
        InvocationContext ic = _runner.newClient()
                .newInvocation("http://localhost/sample/" + this.errorPageServletName);
        final HttpServletRequest request = ic.getRequest();
        ErrorPageServlet servlet = (ErrorPageServlet) ic.getServlet();
        RequestDispatcher rd = servlet.getServletContext().getNamedDispatcher("errorPage");
        // !!! fixme assertTrue("the dispatcher returned by getNamedDispatcher should not be null",rd!=null);
    }

    @Test
    void testRequestDispatcherForwardPaths() throws Exception {
        InvocationContext ic = _runner.newClient()
                .newInvocation("http://localhost/sample/" + outerServletName + "?param=original&param1=first");

        final HttpServletRequest request = ic.getRequest();
        RequestDispatcherServlet servlet = (RequestDispatcherServlet) ic.getServlet();
        RequestDispatcher rd = servlet.getServletContext()
                .getRequestDispatcher("/" + innerServletName + "?param=revised&param2=new");

        assertEquals("/sample/" + outerServletName, request.getRequestURI(), "request URI");
        assertEquals("/sample", request.getContextPath(), "context path attribute");
        assertEquals("/" + outerServletName, request.getServletPath(), "servlet path attribute");
        assertNull(request.getPathInfo(), "path info not null attribute");
        // assertEquals( "query string attribute", "param=original&param1=first", request.getQueryString() ); TODO make
        // this work

        final HttpServletResponse response = ic.getResponse();
        ic.pushForwardRequest(rd, request, response);

        final HttpServletRequest innerRequest = ic.getRequest();
        assertEquals("/sample/" + innerServletName, innerRequest.getRequestURI(), "request URI");
        assertEquals("/sample", innerRequest.getContextPath(), "context path attribute");
        assertEquals("/" + innerServletName, innerRequest.getServletPath(), "servlet path attribute");
        assertNull(innerRequest.getPathInfo(), "path info not null attribute");
        // assertEquals( "query string attribute", "param=original&param1=first", innerRequest.getQueryString() );

        ic.popRequest();
        final HttpServletRequest restoredRequest = ic.getRequest();

        assertNull(restoredRequest.getAttribute(REQUEST_URI), "reverted URI attribute not null");
        assertNull(restoredRequest.getAttribute(CONTEXT_PATH), "context path attribute not null");
        assertNull(restoredRequest.getAttribute(SERVLET_PATH), "servlet path attribute not null");
        assertNull(restoredRequest.getAttribute(PATH_INFO), "path info attribute not null");
        // assertNull( "query string attribute not null", "param=revised&param2=new", restoredRequest.getAttribute(
        // QUERY_STRING ) );
    }

    static class RequestDispatcherServlet extends HttpServlet {

        public void doGet(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
            RequestDispatcher dispatcher = getServletContext()
                    .getRequestDispatcher("/subdir/pagename.jsp?param=value&param2=value");
            dispatcher.forward(request, response);
        }
    }

    static class ErrorPageServlet extends HttpServlet {

    }

    static class IncludedServlet extends HttpServlet {

        static String DESIRED_REQUEST_URI = "localhost/subdir/pagename.jsp";
        static String DESIRED_SERVLET_PATH = "/subdir/pagename.jsp";
        static String DESIRED_QUERY_STRING = "param=value&param2=value";
        static String DESIRED_OUTPUT = DESIRED_REQUEST_URI + DESIRED_QUERY_STRING + DESIRED_SERVLET_PATH;

        public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
            response.setContentType("text/plain");
            String requestUri = (String) request.getAttribute(REQUEST_URI);
            String queryString = (String) request.getAttribute(QUERY_STRING);
            String servletPath = (String) request.getAttribute(SERVLET_PATH);
            PrintWriter pw = response.getWriter();
            pw.write(blankIfNull(requestUri));
            pw.write(blankIfNull(queryString));
            pw.write(blankIfNull(servletPath));
            pw.close();
        }

        private String blankIfNull(String s) {
            return s == null ? "" : s;
        }
    }
}

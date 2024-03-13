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
import static org.junit.jupiter.api.Assertions.fail;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

/* uncomment this and the other jetty dependend parts and also activate the pom.xml and if you use eclipse the classpath settings
import org.mortbay.jetty.Server;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
 */

/**
 * Tests for Bug Report [ 2264431 ] form.submit() sends multiple HTTP POSTS
 */
public class PostTest {

    /*
     * Server jetty; protected void setUp() { jetty = new Server(8989); } protected void tearDown() throws Exception {
     * jetty.stop(); }
     */
    static final String resourceName = "test";

    /**
     * check the webresponse
     *
     * @param response
     *
     * @throws SAXException
     * @throws IOException
     */
    public void check(WebResponse response) throws SAXException, IOException {
        TestServlet.postCount = 0;
        WebForm form = response.getFormWithID("bug");
        form.setParameter("handle", "steve");
        form.setParameter("brainz", "has none");
        form.submit();
        int expected = 1;
        assertEquals(TestServlet.postCount, expected,
                "The postcount should be " + expected + " but is " + TestServlet.postCount);
    }

    /*
     * @Test public void testThatFormSubmitIssuesASinglePost() throws Exception {
     * TestServlet.location="http://localhost:8989/"; TestServlet servlet = new TestServlet(); ServletHandler handler =
     * new ServletHandler(); handler.addServletWithMapping(new ServletHolder(servlet), "/"+resourceName);
     * jetty.setHandler(handler); jetty.start(); WebConversation wc = new WebConversation(); WebResponse response =
     * wc.getResponse(TestServlet.location+resourceName); check(response); }
     */

    @Test
    void testMultiplePosts() throws Exception {
        TestServlet.location = "http://localhost/";

        try {
            ServletRunner sr = new ServletRunner();
            sr.registerServlet(resourceName, TestServlet.class.getName());

            WebRequest request = new GetMethodWebRequest(TestServlet.location + resourceName);
            WebResponse response = sr.getResponse(request);
            check(response);
        } catch (Throwable th) {
            th.printStackTrace();
            fail("There should be no exception but we got " + th.getMessage());
        }
    }

    @Test
    void testMultiPartPost() throws Exception {
        TestServlet.location = "http://localhost/";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet(resourceName, TestServlet.class.getName());

        WebRequest request = new GetMethodWebRequest(TestServlet.location + resourceName);
        WebResponse response = sr.getResponse(request);

        WebForm form = response.getFormWithID("multipart-bug");
        response = form.submit();

        assertEquals(true, response.getText().contains("name=\"empty\""));
        assertEquals(true, response.getText().contains("name=\"empty_textarea\""));
        // check(response);
    }

    /**
     * a Servlet that counts the posts being done
     */
    static class TestServlet extends HttpServlet {
        private static final long serialVersionUID = 1L;
        public static int postCount = 0;
        public static String location = null;

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                throws IOException, ServletException {
            response.setContentType("text/html");
            response.getWriter()
            .println("<html>" + "<body>" + "<form action='" + location + resourceName
                    + "' method='post' id='bug'>" + "<input name='handle'/>" + "<input name='brainz'/>"
                    + "</form>" + "<form id='multipart-bug' method='post' action='" + location + resourceName
                    + "' enctype='multipart/form-data'>" + "<input name='empty' value=''>"
                    + "<input name='notempty' value='1'>" + "<textarea name='empty_textarea'></textarea>"
                    + "</form>" + "</body>" + "</html>");
            /*
             * if (request instanceof Request) ((Request) request).setHandled(true);
             */
        }

        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response)
                throws IOException, ServletException {
            postCount++;
            InputStream is = request.getInputStream();
            OutputStream os = response.getOutputStream();
            byte[] buffer = new byte[4096];
            for (int length = is.read(buffer); length > 0; length = is.read(buffer)) {
                os.write(buffer, 0, length);
            }
        }
    }
}

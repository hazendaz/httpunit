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
package com.meterware.servletunit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpNotFoundException;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebClient;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.pseudoserver.HttpUserAgentTest;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * Tests support for stateless HttpServlets.
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 */
class StatelessTest {

    @Test
    void notFound() throws Exception {
        ServletRunner sr = new ServletRunner();

        WebRequest request = new GetMethodWebRequest("http://localhost/nothing");
        try {
            sr.getResponse(request);
            fail("Should have rejected the request");
        } catch (HttpNotFoundException e) {
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, e.getResponseCode(), "Response code");
        }
    }

    @Test
    void servletCaching() throws Exception {
        AccessCountServlet._numInstances = 0;
        final String resourceName = "something/interesting";

        assertEquals(0, AccessCountServlet.getNumInstances(), "Initial instances of servlet class");
        ServletRunner sr = new ServletRunner();
        sr.registerServlet(resourceName, AccessCountServlet.class.getName());

        WebRequest request = new GetMethodWebRequest("http://localhost/" + resourceName);
        assertEquals("1", sr.getResponse(request).getText().trim(), "First reply");
        assertEquals(1, AccessCountServlet.getNumInstances(), "Instances of servlet class after first call");
        assertEquals("2", sr.getResponse(request).getText().trim(), "Second reply");
        assertEquals(1, AccessCountServlet.getNumInstances(), "Instances of servlet class after first call");
        sr.shutDown();
        assertEquals(0, AccessCountServlet.getNumInstances(), "Instances of servlet class after shutdown");
    }

    @Test
    void servletAccessByClassName() throws Exception {
        ServletRunner sr = new ServletRunner();

        WebRequest request = new GetMethodWebRequest("http://localhost/servlet/" + SimpleGetServlet.class.getName());
        WebResponse response = sr.getResponse(request);
        assertNotNull(response, "No response received");
        assertEquals("text/html", response.getContentType(), "content type");
        assertEquals(SimpleGetServlet.RESPONSE_TEXT, response.getText(), "requested resource");
    }

    @Test
    void simpleGet() throws Exception {
        final String resourceName = "something/interesting";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet(resourceName, SimpleGetServlet.class.getName());

        WebRequest request = new GetMethodWebRequest("http://localhost/" + resourceName);
        WebResponse response = sr.getResponse(request);
        assertNotNull(response, "No response received");
        assertEquals("text/html", response.getContentType(), "content type");
        assertEquals(SimpleGetServlet.RESPONSE_TEXT, response.getText(), "requested resource");
    }

    @Test
    void getWithSetParams() throws Exception {
        final String resourceName = "something/interesting";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet(resourceName, ParameterServlet.class.getName());

        WebRequest request = new GetMethodWebRequest("http://localhost/" + resourceName);
        request.setParameter("color", "red");
        WebResponse response = sr.getResponse(request);
        assertNotNull(response, "No response received");
        assertEquals("text/plain", response.getContentType(), "content type");
        assertEquals("You selected red", response.getText(), "requested resource");
        String[] headers = response.getHeaderFields("MyHeader");
        assertEquals(2, headers.length, "Number of MyHeaders returned");
        assertEquals("value1", headers[0], "MyHeader #1");
        assertEquals("value2", headers[1], "MyHeader #2");
    }

    @Test
    void getWithInlineParams() throws Exception {
        final String resourceName = "something/interesting";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet(resourceName, ParameterServlet.class.getName());

        WebRequest request = new GetMethodWebRequest("http://localhost/" + resourceName + "?color=dark+red");
        WebResponse response = sr.getResponse(request);
        assertNotNull(response, "No response received");
        assertEquals("text/plain", response.getContentType(), "content type");
        assertEquals("You selected dark red", response.getText(), "requested resource");
    }

    @Test
    void headerRetrieval() throws Exception {
        ServletRunner sr = new ServletRunner();
        sr.registerServlet("/Parameters", ParameterServlet.class.getName());

        ServletUnitClient client = sr.newClient();
        client.setHeaderField("Sample", "Value");
        client.setHeaderField("Request", "Client");
        WebRequest request = new GetMethodWebRequest("http://localhost/Parameters?color=dark+red");
        request.setHeaderField("request", "Caller");
        InvocationContext ic = client.newInvocation(request);
        assertEquals("Value", ic.getRequest().getHeader("sample"), "Sample header");
        assertEquals("Caller", ic.getRequest().getHeader("Request"), "Request header");
    }

    @Test
    void parameterHandling() throws Exception {
        ServletRunner sr = new ServletRunner();
        sr.registerServlet("/testForm", FormSubmissionServlet.class.getName());

        ServletUnitClient client = sr.newClient();
        WebResponse wr = client.getResponse("http://localhost/testForm");
        WebForm form = wr.getForms()[0];
        form.setParameter("login", "me");
        form.setParameter("password", "haha");
        form.submit();
        assertEquals("You posted me,haha", client.getCurrentPage().getText(), "Resultant response");
    }

    @Test
    void simplePost() throws Exception {
        final String resourceName = "something/interesting";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet(resourceName, ParameterServlet.class.getName());

        WebRequest request = new PostMethodWebRequest("http://localhost/" + resourceName);
        request.setParameter("color", "red");
        WebResponse response = sr.getResponse(request);
        assertNotNull(response, "No response received");
        assertEquals("text/plain", response.getContentType(), "content type");
        assertEquals("You posted red", response.getText(), "requested resource");
    }

    @Test
    void streamBasedPost() throws Exception {
        ServletRunner sr = new ServletRunner();
        sr.registerServlet("ReportData", BodyEcho.class.getName());

        String sourceData = "This is an interesting test\nWith two lines";
        InputStream source = new ByteArrayInputStream(sourceData.getBytes(StandardCharsets.UTF_8));

        WebClient wc = sr.newClient();
        WebRequest wr = new PostMethodWebRequest("http://localhost/ReportData", source, "text/sample");
        WebResponse response = wc.getResponse(wr);
        assertEquals(sourceData.length() + "\n" + sourceData, response.getText(), "Body response");
        assertEquals("text/sample", response.getContentType(), "Content-type");
    }

    @Test
    void requestInputStream() throws Exception {
        ServletRunner sr = new ServletRunner();
        WebRequest request = new PostMethodWebRequest("http://localhost/servlet/" + ParameterServlet.class.getName());
        request.setParameter("color", "green");
        final String expectedBody = "color=green";
        InvocationContext ic = sr.newClient().newInvocation(request);
        assertEquals("application/x-www-form-urlencoded", ic.getRequest().getContentType(), "Message body type");
        InputStream is = ic.getRequest().getInputStream();
        byte[] buffer = new byte[expectedBody.length()];
        assertEquals(buffer.length, is.read(buffer), "Input stream length");
        assertEquals(expectedBody, new String(buffer), "Message body");
    }

    @Test
    void frameAccess() throws Exception {
        ServletRunner sr = new ServletRunner();
        sr.registerServlet("Frames", FrameTopServlet.class.getName());
        sr.registerServlet("RedFrame", SimpleGetServlet.class.getName());
        sr.registerServlet("BlueFrame", AccessCountServlet.class.getName());

        WebClient client = sr.newClient();
        WebRequest request = new GetMethodWebRequest("http://host/Frames");
        WebResponse page = client.getResponse(request);
        HttpUserAgentTest.assertMatchingSet("Frames defined for the conversation",
                new String[] { "_top", "red", "blue" }, client.getFrameNames());
        WebResponse response = client.getFrameContents("red");
        assertEquals(SimpleGetServlet.RESPONSE_TEXT, response.getText(), "Frame contents");

        page.getSubframeContents(page.getFrameNames()[0]);
    }

    static class SimpleGetServlet extends HttpServlet {

        private static final long serialVersionUID = 1L;
        static String RESPONSE_TEXT = "the desired content\r\n";

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("text/html");
            PrintWriter pw = resp.getWriter();
            pw.print(RESPONSE_TEXT);
            pw.close();
        }
    }

    static class AccessCountServlet extends HttpServlet {

        private static final long serialVersionUID = 1L;

        private int _numAccesses;

        private static int _numInstances = 0;

        @Override
        public void init() throws ServletException {
            super.init();
            _numInstances++;
        }

        @Override
        public void destroy() {
            super.destroy();
            _numInstances--;
        }

        public static int getNumInstances() {
            return _numInstances;
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("text/plain");
            PrintWriter pw = resp.getWriter();
            pw.print(String.valueOf(++_numAccesses));
            pw.close();
        }
    }

    static class ParameterServlet extends HttpServlet {

        private static final long serialVersionUID = 1L;
        static String RESPONSE_TEXT = "the desired content\r\n";

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("text/plain");
            resp.addHeader("MyHeader", "value1");
            resp.addHeader("MyHeader", "value2");

            PrintWriter pw = resp.getWriter();
            pw.print("You selected " + req.getParameter("color"));
            pw.close();
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("text/plain");
            PrintWriter pw = resp.getWriter();
            pw.print("You posted " + req.getParameter("color"));
            pw.close();
        }

    }

    static class BodyEcho extends HttpServlet {
        private static final long serialVersionUID = 1L;

        /**
         * Returns a resource object as a result of a get request.
         */
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            int length = req.getIntHeader("Content-length");
            String contentType = req.getHeader("Content-type");
            resp.setContentType(contentType);

            InputStreamReader isr = new InputStreamReader(req.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            resp.getWriter().print(length);

            String line = br.readLine();
            while (line != null) {
                resp.getWriter().print("\n");
                resp.getWriter().print(line);
                line = br.readLine();
            }
            resp.getWriter().flush();
            resp.getWriter().close();
        }
    }

    static class FormSubmissionServlet extends HttpServlet {

        private static final long serialVersionUID = 1L;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("text/html");

            PrintWriter pw = resp.getWriter();
            pw.println("<html><head></head><body>");
            pw.println("<FORM ACTION='/testForm?submission=act' METHOD='POST'>");
            pw.println("<INPUT NAME='login' TYPE='TEXT'>");
            pw.println("<INPUT NAME='password' TYPE='PASSWORD'>");
            pw.println("<INPUT TYPE='SUBMIT'>");
            pw.println("</FORM></body></html>");
            pw.close();
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("text/plain");
            PrintWriter pw = resp.getWriter();
            pw.print("You posted " + req.getParameter("login") + "," + req.getParameter("password"));
            pw.close();
        }

    }

    static class FrameTopServlet extends HttpServlet {

        private static final long serialVersionUID = 1L;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("text/html");

            PrintWriter pw = resp.getWriter();
            pw.println("<html><head></head><frameset cols='20%,80&'>");
            pw.println("<frame src='RedFrame' name='red'>");
            pw.println("<frame src='BlueFrame' name='blue'>");
            pw.println("</frameset></html>");
            pw.close();
        }

    }
}

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebClient;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import org.junit.jupiter.api.Test;

/**
 * Tests support for state-management behavior.
 */
class StatefulTest {

    /**
     * No initial state.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void noInitialState() throws Exception {
        final String resourceName = "something/interesting";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet(resourceName, StatefulServlet.class.getName());

        WebRequest request = new GetMethodWebRequest("http://localhost/" + resourceName);
        WebResponse response = sr.getResponse(request);
        assertNotNull(response, "No response received");
        assertEquals("text/plain", response.getContentType(), "content type");
        assertEquals("No session found", response.getText(), "requested resource");
        assertEquals(0, response.getNewCookieNames().length, "Returned cookie count");
    }

    /**
     * State cookies.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void stateCookies() throws Exception {
        final String resourceName = "something/interesting";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet(resourceName, StatefulServlet.class.getName());

        WebRequest request = new PostMethodWebRequest("http://localhost/" + resourceName);
        request.setParameter("color", "red");
        WebResponse response = sr.getResponse(request);
        assertNotNull(response, "No response received");
        assertEquals(1, response.getNewCookieNames().length, "Returned cookie count");
    }

    /**
     * State preservation.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void statePreservation() throws Exception {
        final String resourceName1 = "something/interesting/start";
        final String resourceName2 = "something/continue";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet(resourceName1, StatefulServlet.class.getName());
        sr.registerServlet(resourceName2, StatefulServlet.class.getName());
        WebClient wc = sr.newClient();

        WebRequest request = new PostMethodWebRequest("http://localhost/" + resourceName1);
        request.setParameter("color", "red");
        WebResponse response = wc.getResponse(request);
        assertNotNull(response, "No response received");
        assertEquals("text/plain", response.getContentType(), "content type");
        assertEquals("You selected red", response.getText(), "requested resource");

        request = new GetMethodWebRequest("http://localhost/" + resourceName2);
        response = wc.getResponse(request);
        assertNotNull(response, "No response received");
        assertEquals("text/plain", response.getContentType(), "content type");
        assertEquals("You posted red", response.getText(), "requested resource");
        assertEquals(0, response.getNewCookieNames().length, "Returned cookie count");
    }

    /**
     * Session preloading.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void sessionPreloading() throws Exception {
        final String resourceName1 = "something/interesting/start";
        final String resourceName2 = "something/continue";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet(resourceName1, StatefulServlet.class.getName());
        sr.registerServlet(resourceName2, StatefulServlet.class.getName());
        ServletUnitClient wc = sr.newClient();

        wc.getSession(true).setAttribute("color", "green");
        WebRequest request = new GetMethodWebRequest("http://localhost/" + resourceName2);
        WebResponse response = wc.getResponse(request);
        assertNotNull(response, "No response received");
        assertEquals("text/plain", response.getContentType(), "content type");
        assertEquals("You posted green", response.getText(), "requested resource");
        assertEquals(0, response.getNewCookieNames().length, "Returned cookie count");
    }

    /**
     * Session access.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void sessionAccess() throws Exception {
        final String resourceName1 = "something/interesting/start";
        final String resourceName2 = "something/continue";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet(resourceName1, StatefulServlet.class.getName());
        sr.registerServlet(resourceName2, StatefulServlet.class.getName());

        WebRequest request = new PostMethodWebRequest("http://localhost/" + resourceName1);
        request.setParameter("color", "yellow");
        sr.getResponse(request);

        assertNotNull(sr.getSession(false), "No session was created");
        assertEquals("yellow", sr.getSession(false).getAttribute("color"), "Color attribute in session");
    }

    /**
     * Invocation context.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void invocationContext() throws Exception {
        final String resourceName = "something/interesting";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet(resourceName, StatefulServlet.class.getName());
        ServletUnitClient suc = sr.newClient();

        WebRequest request = new PostMethodWebRequest("http://localhost/" + resourceName);
        request.setParameter("color", "red");

        InvocationContext ic = suc.newInvocation(request);
        StatefulServlet ss = (StatefulServlet) ic.getServlet();
        assertNull(ss.getColor(ic.getRequest()), "A session already exists");

        ss.setColor(ic.getRequest(), "blue");
        assertEquals("blue", ss.getColor(ic.getRequest()), "Color in session");

        Enumeration e = ic.getRequest().getSession().getAttributeNames();
        assertNotNull(e, "No attribute list returned");
        assertTrue(e.hasMoreElements(), "No attribute names in list");
        assertEquals("color", e.nextElement(), "First attribute name");
        assertFalse(e.hasMoreElements(), "List did not end after one name");

        Enumeration<String> names = ic.getRequest().getSession().getAttributeNames();
        assertEquals("color", names.nextElement(), "first name");
    }

    /**
     * Invocation completion.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void invocationCompletion() throws Exception {
        final String resourceName = "something/interesting";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet(resourceName, StatefulServlet.class.getName());
        ServletUnitClient suc = sr.newClient();

        WebRequest request = new PostMethodWebRequest("http://localhost/" + resourceName);
        request.setParameter("color", "red");

        InvocationContext ic = suc.newInvocation(request);
        StatefulServlet ss = (StatefulServlet) ic.getServlet();
        ss.setColor(ic.getRequest(), "blue");
        ss.writeSelectMessage("blue", ic.getResponse().getWriter());

        WebResponse response = ic.getServletResponse();
        assertEquals("You selected blue", response.getText(), "requested resource");
        assertEquals(1, response.getNewCookieNames().length, "Returned cookie count");
    }

    /**
     * Invocation context update.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void invocationContextUpdate() throws Exception {
        final String resourceName = "something/interesting";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet(resourceName, StatefulServlet.class.getName());
        ServletUnitClient suc = sr.newClient();

        WebRequest request = new PostMethodWebRequest("http://localhost/" + resourceName);
        request.setParameter("color", "red");

        InvocationContext ic = suc.newInvocation(request);
        StatefulServlet ss = (StatefulServlet) ic.getServlet();
        ss.setColor(ic.getRequest(), "blue");
        suc.getResponse(ic);

        WebResponse response = suc.getResponse("http://localhost/" + resourceName);
        assertNotNull(response, "No response received");
        assertEquals("text/plain", response.getContentType(), "content type");
        assertEquals("You posted blue", response.getText(), "requested resource");
        assertEquals(0, response.getNewCookieNames().length, "Returned cookie count");
    }

    /**
     * The Class StatefulServlet.
     */
    static class StatefulServlet extends HttpServlet {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The response text. */
        static String RESPONSE_TEXT = "the desired content\r\n";

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("text/plain");
            writeSelectMessage(req.getParameter("color"), resp.getWriter());
            setColor(req, req.getParameter("color"));
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("text/plain");
            PrintWriter pw = resp.getWriter();
            String color = getColor(req);
            if (color == null) {
                pw.print("No session found");
            } else {
                pw.print("You posted " + color);
            }
            pw.close();
        }

        /**
         * Write select message.
         *
         * @param color
         *            the color
         * @param pw
         *            the pw
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        protected void writeSelectMessage(String color, PrintWriter pw) throws IOException {
            pw.print("You selected " + color);
            pw.close();
        }

        /**
         * Sets the color.
         *
         * @param req
         *            the req
         * @param color
         *            the color
         *
         * @throws ServletException
         *             the servlet exception
         */
        protected void setColor(HttpServletRequest req, String color) throws ServletException {
            req.getSession().setAttribute("color", color);
        }

        /**
         * Gets the color.
         *
         * @param req
         *            the req
         *
         * @return the color
         *
         * @throws ServletException
         *             the servlet exception
         */
        protected String getColor(HttpServletRequest req) throws ServletException {
            HttpSession session = req.getSession( /* create */ false);
            if (session == null) {
                return null;
            }

            return (String) session.getAttribute("color");
        }

    }
}

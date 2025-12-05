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

import com.meterware.httpunit.WebClient;
import com.meterware.httpunit.WebResponse;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

import org.junit.jupiter.api.Test;

/**
 * Tests support for navigating among servlets.
 */
class NavigationTest {

    /**
     * Redirect.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void redirect() throws Exception {
        ServletRunner sr = new ServletRunner();
        sr.registerServlet("target", TargetServlet.class.getName());
        sr.registerServlet("origin", OriginServlet.class.getName());

        WebClient wc = sr.newClient();
        WebResponse response = wc.getResponse("http://localhost/origin?color=green");
        assertNotNull(response, "No response received");
        assertEquals("color=null: path=/target", response.getText(), "Expected response");
        assertEquals(0, response.getNewCookieNames().length, "Returned cookie count");
    }

    /**
     * Forward.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void forward() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet("/target", TargetServlet.class);
        wxs.addServlet("/origin", FowarderServlet.class);

        ServletRunner sr = new ServletRunner(wxs.asInputStream(), "/context");

        WebClient wc = sr.newClient();
        WebResponse response = wc.getResponse("http://localhost/context/origin?color=green");
        assertNotNull(response, "No response received");
        assertEquals("color=green: path=/context/target", response.getText(), "Expected response");
        assertEquals(0, response.getNewCookieNames().length, "Returned cookie count");
    }

    /**
     * Include.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void include() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet("/target", TargetServlet.class);
        wxs.addServlet("/origin", IncluderServlet.class);

        ServletRunner sr = new ServletRunner(wxs.asInputStream(), "/context");

        WebClient wc = sr.newClient();
        WebResponse response = wc.getResponse("http://localhost/context/origin?color=green");
        assertNotNull(response, "No response received");
        assertEquals("expecting: color=blue: path=/context/origin", response.getText(), "Expected response");
        assertEquals(0, response.getNewCookieNames().length, "Returned cookie count");
    }

    /**
     * Forward via http servlet request.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void forwardViaHttpServletRequest() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet("/target", TargetServlet.class);
        wxs.addServlet("/origin", FowarderServlet2.class);

        ServletRunner sr = new ServletRunner(wxs.asInputStream(), "/context");

        WebClient wc = sr.newClient();
        WebResponse response = wc.getResponse("http://localhost/context/origin?color=green");
        assertNotNull(response, "No response received");
        assertEquals("color=green: path=/context/target", response.getText(), "Expected response");
        assertEquals(0, response.getNewCookieNames().length, "Returned cookie count");
    }

    /**
     * Forward via relative path.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void forwardViaRelativePath() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet("/some/target", TargetServlet.class);
        wxs.addServlet("/some/origin", FowarderServlet3.class);

        ServletRunner sr = new ServletRunner(wxs.asInputStream(), "/context");

        WebClient wc = sr.newClient();
        WebResponse response = wc.getResponse("http://localhost/context/some/origin?color=green");
        assertNotNull(response, "No response received");
        assertEquals("color=green: path=/context/some/target", response.getText(), "Expected response");
        assertEquals(0, response.getNewCookieNames().length, "Returned cookie count");
    }

    /**
     * The Class OriginServlet.
     */
    static class OriginServlet extends HttpServlet {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("text/plain");
            resp.sendRedirect("http://localhost/target");
        }

    }

    /**
     * The Class FowarderServlet.
     */
    static class FowarderServlet extends HttpServlet {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            getServletContext().getRequestDispatcher("/target").forward(req, resp);
        }

    }

    /**
     * The Class FowarderServlet2.
     */
    static class FowarderServlet2 extends HttpServlet {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            req.getRequestDispatcher("/target").forward(req, resp);
        }

    }

    /**
     * The Class FowarderServlet3.
     */
    static class FowarderServlet3 extends HttpServlet {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            req.getRequestDispatcher("target").forward(req, resp);
        }

    }

    /**
     * The Class IncluderServlet.
     */
    static class IncluderServlet extends HttpServlet {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The Constant PREFIX. */
        static final String PREFIX = "expecting: ";

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.getWriter().print(PREFIX);
            getServletContext().getRequestDispatcher("/target?color=blue").include(req, resp);
        }
    }

    /**
     * The Class TargetServlet.
     */
    static class TargetServlet extends HttpServlet {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("text/plain");
            PrintWriter pw = resp.getWriter();
            pw.print("color=" + req.getParameter("color"));
            pw.print(": path=" + req.getRequestURI());
            pw.close();
        }

    }
}

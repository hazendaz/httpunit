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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.meterware.httpunit.WebClient;
import com.meterware.httpunit.WebResponse;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;

/**
 * Tests support for navigating among servlets.
 */
class NavigationTest {

    @Test
    void testRedirect() throws Exception {
        ServletRunner sr = new ServletRunner();
        sr.registerServlet("target", TargetServlet.class.getName());
        sr.registerServlet("origin", OriginServlet.class.getName());

        WebClient wc = sr.newClient();
        WebResponse response = wc.getResponse("http://localhost/origin?color=green");
        assertNotNull(response, "No response received");
        assertEquals("color=null: path=/target", response.getText(), "Expected response");
        assertEquals(0, response.getNewCookieNames().length, "Returned cookie count");
    }

    @Test
    void testForward() throws Exception {
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

    @Test
    void testInclude() throws Exception {
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

    @Test
    void testForwardViaHttpServletRequest() throws Exception {
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

    @Test
    void testForwardViaRelativePath() throws Exception {
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

    static class OriginServlet extends HttpServlet {

        private static final long serialVersionUID = 1L;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("text/plain");
            resp.sendRedirect("http://localhost/target");
        }

    }

    static class FowarderServlet extends HttpServlet {

        private static final long serialVersionUID = 1L;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            getServletContext().getRequestDispatcher("/target").forward(req, resp);
        }

    }

    static class FowarderServlet2 extends HttpServlet {

        private static final long serialVersionUID = 1L;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            req.getRequestDispatcher("/target").forward(req, resp);
        }

    }

    static class FowarderServlet3 extends HttpServlet {

        private static final long serialVersionUID = 1L;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            req.getRequestDispatcher("target").forward(req, resp);
        }

    }

    static class IncluderServlet extends HttpServlet {
        private static final long serialVersionUID = 1L;
        static final String PREFIX = "expecting: ";

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.getWriter().print(PREFIX);
            getServletContext().getRequestDispatcher("/target?color=blue").include(req, resp);
        }
    }

    static class TargetServlet extends HttpServlet {
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

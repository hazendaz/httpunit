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

import static org.junit.jupiter.api.Assertions.*;

import com.meterware.httpunit.HttpNotFoundException;
import com.meterware.httpunit.WebClient;
import com.meterware.httpunit.WebResponse;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;

/**
 * Tests support for the servlet configuration.
 */
class ConfigTest {

    @Test
    void testConfigObject() throws Exception {
        final String resourceName = "something/interesting";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet(resourceName, ConfigServlet.class.getName());
        WebClient wc = sr.newClient();
        WebResponse response = wc.getResponse("http://localhost/" + resourceName);
        assertNotNull(response, "No response received");
        assertEquals("text/plain", response.getContentType(), "content type");
        assertEquals("servlet name is " + ConfigServlet.class.getName(), response.getText());
    }

    /**
     * Test added by WF 2012-11-12 to answer question on developers mailing list
     * @throws Exception
     */
    @Test
    void testInvalidConfig() throws Exception {
        final String resourceName = "something/interesting";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet(resourceName, ConfigServlet.class.getName());
        WebClient wc = sr.newClient();
        try {
            WebResponse response = wc.getResponse("http://localhost/" + "ISB/" + resourceName);
            fail("No Exception thrown");
        } catch (Throwable th) {
            // com.meterware.httpunit.HttpNotFoundException:
            // Error on HTTP request: 404 No servlet mapping defined [http://localhost/ISB/something/interesting]
            String expected = "Error on HTTP request: 404 No servlet mapping defined [http://localhost/ISB/something/interesting]";
            assertTrue(th instanceof HttpNotFoundException, "HttpNotFoundException expected");
            assertEquals(expected, th.getMessage(), "wrong exception message");
        }
    }


    @Test
    void testContextAttributes() throws Exception {
        final String servlet1Name = "something/interesting";
        final String servlet2Name = "something/else";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet(servlet1Name, ConfigServlet.class.getName());
        sr.registerServlet(servlet2Name, ConfigServlet.class.getName());
        ServletUnitClient wc = sr.newClient();
        InvocationContext ic1 = wc.newInvocation("http://localhost/" + servlet1Name);
        ServletContext sc1 = ic1.getServlet().getServletConfig().getServletContext();
        sc1.setAttribute("sample", "found me");

        InvocationContext ic2 = wc.newInvocation("http://localhost/" + servlet2Name);
        ServletContext sc2 = ic2.getServlet().getServletConfig().getServletContext();
        assertEquals("found me", sc2.getAttribute("sample"), "attribute 'sample'");
    }


    @Test
    void testFileMimeType() throws Exception {
        final String servlet1Name = "something/interesting";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet(servlet1Name, ConfigServlet.class.getName());
        ServletUnitClient wc = sr.newClient();
        InvocationContext ic1 = wc.newInvocation("http://localhost/" + servlet1Name);
        ServletContext context = ic1.getServlet().getServletConfig().getServletContext();
        checkMimeType(context, "sample.txt", "text/plain");
        checkMimeType(context, "sample.html", "text/html");
        checkMimeType(context, "sample.gif", "image/gif");
    }


    @Test
    void testServletContextAccess() throws Exception {
        ServletRunner sr = new ServletRunner();
        sr.registerServlet("SimpleServlet", ConfigServlet.class.getName());
        ServletUnitClient client = sr.newClient();
        InvocationContext ic = client.newInvocation("http://localhost/SimpleServlet");
        ServletContext context = ic.getServlet().getServletConfig().getServletContext();
        assertSame(context, ic.getRequest().getSession().getServletContext(), "Context from session");
    }


    private void checkMimeType(ServletContext context, String fileName, String expectedMimeType) {
        assertEquals(expectedMimeType, context.getMimeType(fileName), "mime type for " + fileName);
    }


    static class ConfigServlet extends HttpServlet {
        static String RESPONSE_TEXT = "the desired content\r\n";

        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("text/plain");
            PrintWriter pw = resp.getWriter();
            ServletConfig config = getServletConfig();

            if (config == null) {
                pw.print("config object is null");
            } else {
                pw.print("servlet name is " + config.getServletName());
            }
            pw.close();
        }

    }
}



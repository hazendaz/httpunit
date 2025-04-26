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

import com.meterware.httpunit.WebResponse;

import javax.servlet.http.HttpServlet;

import org.junit.Test;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 */
public class ServletAccessTestClass extends ServletTestCase {

    /**
     * construct a ServletAccessTest
     *
     * @param name
     */
    public ServletAccessTestClass(String name) {
        super(name);
    }

    // TODO JWL 4/20/2025 Keep 'test' on this method as well as public until such time its not using junit 3.
    @Test
    public void testServletParameters() throws Exception {
        ServletUnitClient client = newClient();
        InvocationContext ic = client.newInvocation("http://localhost/SimpleServlet");
        assertNull("init parameter 'gender' should be null",
                ic.getServlet().getServletConfig().getInitParameter("gender"));
        assertEquals("init parameter via config", "red", ic.getServlet().getServletConfig().getInitParameter("color"));
        assertEquals("init parameter directly", "12", ((HttpServlet) ic.getServlet()).getInitParameter("age"));
        ic.getServlet().service(ic.getRequest(), ic.getResponse());

        WebResponse wr = client.getResponse(ic);
        assertEquals("Servlet response", "the desired content", wr.getText());
    }

}

/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.servletunit;

import com.meterware.httpunit.WebResponse;

import jakarta.servlet.http.HttpServlet;

import org.junit.Test;

/**
 * The Class ServletAccessTestClass.
 */
public class ServletAccessTestClass extends ServletTestCase {

    /**
     * construct a ServletAccessTest.
     *
     * @param name
     *            the name
     */
    public ServletAccessTestClass(String name) {
        super(name);
    }

    /**
     * Test servlet parameters.
     *
     * @throws Exception
     *             the exception
     */
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

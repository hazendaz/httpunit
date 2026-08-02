/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2026 hazendaz
 */
package com.meterware.servletunit;

import com.meterware.httpunit.WebResponse;

import jakarta.servlet.http.HttpServlet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * The Class ServletAccessTestClass.
 */
public class ServletAccessTestClass extends ServletTestCase {

    /**
     * Test servlet parameters.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void testServletParameters() throws Exception {
        ServletUnitClient client = newClient();
        InvocationContext ic = client.newInvocation("http://localhost/SimpleServlet");
        Assertions.assertNull(ic.getServlet().getServletConfig().getInitParameter("gender"),
                "init parameter 'gender' should be null");
        Assertions.assertEquals("red", ic.getServlet().getServletConfig().getInitParameter("color"),
                "init parameter via config");
        Assertions.assertEquals("12", ((HttpServlet) ic.getServlet()).getInitParameter("age"),
                "init parameter directly");
        ic.getServlet().service(ic.getRequest(), ic.getResponse());

        WebResponse wr = client.getResponse(ic);
        Assertions.assertEquals("the desired content", wr.getText(), "Servlet response");
    }

}

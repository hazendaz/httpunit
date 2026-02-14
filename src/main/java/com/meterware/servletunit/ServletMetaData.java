/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.servletunit;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;

/**
 * The Interface ServletMetaData.
 */
interface ServletMetaData {

    /**
     * Returns the servlet instance to use.
     *
     * @return the servlet
     *
     * @throws ServletException
     *             the servlet exception
     */
    Servlet getServlet() throws ServletException;

    /**
     * Returns the path used to identify the servlet.
     *
     * @return the servlet path
     */
    String getServletPath();

    /**
     * Returns the path info beyond the servlet path.
     *
     * @return the path info
     */
    String getPathInfo();

    /**
     * Returns an ordered list of the filters associated with this servlet.
     *
     * @return the filters
     */
    FilterMetaData[] getFilters();

}

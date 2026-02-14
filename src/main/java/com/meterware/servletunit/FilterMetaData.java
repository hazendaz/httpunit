/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.servletunit;

import jakarta.servlet.Filter;
import jakarta.servlet.ServletException;

/**
 * The Interface FilterMetaData.
 */
interface FilterMetaData {

    /**
     * Returns the filter instance to use.
     *
     * @return the filter
     *
     * @throws ServletException
     *             the servlet exception
     */
    Filter getFilter() throws ServletException;
}

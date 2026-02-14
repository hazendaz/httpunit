/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.servletunit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import jakarta.servlet.ServletContext;

import java.io.InputStream;
import java.net.URL;

import org.junit.jupiter.api.Test;

/**
 * The Class ServletUnitServletContextTest.
 */
class ServletUnitServletContextTest {

    /** The Constant EXISTENT_RESOURCE_PATH. */
    private static final String EXISTENT_RESOURCE_PATH = "src/test/resources/existent.xml";

    /** The Constant NONEXISTENT_RESOURCE_PATH. */
    private static final String NONEXISTENT_RESOURCE_PATH = "src/test/resources/nonexistent.xml";

    /**
     * Gets the resource.
     *
     * @return the resource
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void getResource() throws Exception {
        WebApplication webapp = new WebApplication();
        ServletContext sc = new ServletUnitServletContext(webapp);

        // for existent resources
        InputStream is = sc.getResourceAsStream(EXISTENT_RESOURCE_PATH);
        assertNotNull(is, "must not return a null");
        is.close();

        URL r = sc.getResource(EXISTENT_RESOURCE_PATH);
        assertNotNull(r, "must not return a null");

        // for non-existent resources
        is = sc.getResourceAsStream(NONEXISTENT_RESOURCE_PATH);
        assertNull(is, "must return a null");

        r = sc.getResource(NONEXISTENT_RESOURCE_PATH);
        assertNull(r, "must return a null");

    }

}

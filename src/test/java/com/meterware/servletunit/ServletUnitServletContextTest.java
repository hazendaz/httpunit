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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.InputStream;
import java.net.URL;

import javax.servlet.ServletContext;

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

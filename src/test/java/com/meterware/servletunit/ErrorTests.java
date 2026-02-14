/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.servletunit;

import org.junit.Ignore;

import junit.framework.TestCase;

/**
 * This test is run by the junit servlet only.
 */
// XXX This test is managed via JUnitServletTest and thus ignore here ensures newer surefire plugin doesn't use directly
// nor any IDE
@Ignore
public class ErrorTests extends TestCase {

    /**
     * Instantiates a new error tests.
     *
     * @param s
     *            the s
     */
    public ErrorTests(String s) {
        super(s);
    }

    /**
     * Test addition.
     */
    public void testAddition() {
        throw new RuntimeException("Got a problem?");
    }

    /**
     * Test multiplication.
     */
    public void testMultiplication() {
        assertEquals(4, 2 * 2);
    }
}

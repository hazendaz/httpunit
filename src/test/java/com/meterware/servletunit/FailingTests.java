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

// XXX This test is managed via JUnitServletTest and thus ignore here ensures newer surefire plugin doesn't use directly
/**
 * The Class FailingTests.
 */
// nor any IDE
@Ignore
public class FailingTests extends TestCase {

    /**
     * Instantiates a new failing tests.
     *
     * @param s
     *            the s
     */
    public FailingTests(String s) {
        super(s);
    }

    /**
     * Test addition.
     */
    public void testAddition() {
        assertEquals(3, 1 + 1);
    }

    /**
     * Test subtraction.
     */
    public void testSubtraction() {
        assertEquals(3, 5 - 4);
    }

    /**
     * Test multiplication.
     */
    public void testMultiplication() {
        assertEquals(4, 2 * 2);
    }
}

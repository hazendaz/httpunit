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
 * The Class PassingTests.
 */
// nor any IDE
@Ignore
public class PassingTests extends TestCase {

    /**
     * Instantiates a new passing tests.
     *
     * @param s
     *            the s
     */
    public PassingTests(String s) {
        super(s);
    }

    /**
     * Test addition.
     */
    public void testAddition() {
        assertEquals(2, 1 + 1);
    }
}

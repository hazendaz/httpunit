/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2026 hazendaz
 */
package com.meterware.servletunit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * The Class FailingTests.
 */
public class FailingTests {

    /**
     * Test addition.
     */
    @Test
    public void testAddition() {
        Assertions.assertEquals(3, 1 + 1);
    }

    /**
     * Test subtraction.
     */
    @Test
    public void testSubtraction() {
        Assertions.assertEquals(3, 5 - 4);
    }

    /**
     * Test multiplication.
     */
    @Test
    public void testMultiplication() {
        Assertions.assertEquals(4, 2 * 2);
    }
}

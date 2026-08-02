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
 * This test is run by the junit servlet only.
 */
public class ErrorTests {

    /**
     * Test addition.
     */
    @Test
    public void testAddition() {
        throw new RuntimeException("Got a problem?");
    }

    /**
     * Test multiplication.
     */
    @Test
    public void testMultiplication() {
        Assertions.assertEquals(4, 2 * 2);
    }
}

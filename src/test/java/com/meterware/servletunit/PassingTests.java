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
 * The Class PassingTests.
 */
public class PassingTests {

    /**
     * Test addition.
     */
    @Test
    public void testAddition() {
        Assertions.assertEquals(2, 1 + 1);
    }
}

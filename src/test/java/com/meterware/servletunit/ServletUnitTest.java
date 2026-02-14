/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.servletunit;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;

/**
 * A base class for servlet unit tests.
 */
public abstract class ServletUnitTest {

    // ------------------------------------ protected members ------------------------------------------

    /**
     * Assert matching set.
     *
     * @param comment
     *            the comment
     * @param expected
     *            the expected
     * @param found
     *            the found
     */
    protected void assertMatchingSet(String comment, Object[] expected, Object[] found) {
        List expectedItems = new ArrayList<>();
        List foundItems = new ArrayList<>();

        for (Object element : expected) {
            expectedItems.add(element);
        }
        for (Object element : found) {
            foundItems.add(element);
        }

        for (Object element : expected) {
            if (!foundItems.contains(element)) {
                fail(comment + ": expected " + asText(expected) + " but found " + asText(found));
            } else {
                foundItems.remove(element);
            }
        }

        for (Object element : found) {
            if (!expectedItems.contains(element)) {
                fail(comment + ": expected " + asText(expected) + " but found " + asText(found));
            } else {
                expectedItems.remove(element);
            }
        }

        if (!foundItems.isEmpty()) {
            fail(comment + ": expected " + asText(expected) + " but found " + asText(found));
        }
    }

    /**
     * As text.
     *
     * @param args
     *            the args
     *
     * @return the string
     */
    protected String asText(Object[] args) {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < args.length; i++) {
            if (i != 0) {
                sb.append(",");
            }
            sb.append('"').append(args[i]).append('"');
        }
        sb.append("}");
        return sb.toString();
    }

}

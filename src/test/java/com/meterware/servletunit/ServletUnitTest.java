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

import static org.junit.jupiter.api.Assertions.fail;

import java.util.Vector;

/**
 * A base class for servlet unit tests.
 */
public abstract class ServletUnitTest {

    // ------------------------------------ protected members ------------------------------------------

    protected void assertMatchingSet(String comment, Object[] expected, Object[] found) {
        Vector expectedItems = new Vector<>();
        Vector foundItems = new Vector<>();

        for (Object element : expected) {
            expectedItems.addElement(element);
        }
        for (Object element : found) {
            foundItems.addElement(element);
        }

        for (Object element : expected) {
            if (!foundItems.contains(element)) {
                fail(comment + ": expected " + asText(expected) + " but found " + asText(found));
            } else {
                foundItems.removeElement(element);
            }
        }

        for (Object element : found) {
            if (!expectedItems.contains(element)) {
                fail(comment + ": expected " + asText(expected) + " but found " + asText(found));
            } else {
                expectedItems.removeElement(element);
            }
        }

        if (!foundItems.isEmpty()) {
            fail(comment + ": expected " + asText(expected) + " but found " + asText(found));
        }
    }

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

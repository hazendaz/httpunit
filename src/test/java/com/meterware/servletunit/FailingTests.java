/*
 * MIT License
 *
 * Copyright 2011-2023 Russell Gold
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

import org.junit.Ignore;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 */
// XXX This test is managed via JUnitServletTest and thus ignore here ensures newer surefire plugin doesn't use directly
// nor any IDE
@Ignore
public class FailingTests extends TestCase {

    public FailingTests(String s) {
        super(s);
    }

    public void testAddition() {
        assertEquals(3, 1 + 1);
    }

    public void testSubtraction() {
        assertEquals(3, 5 - 4);
    }

    public void testMultiplication() {
        assertEquals(4, 2 * 2);
    }
}

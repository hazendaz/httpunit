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

import junit.framework.TestCase;

/**
 * A base class for test cases to be run via {@link JUnitServlet JUnitServlet}.
 **/
public abstract class ServletTestCase extends TestCase {

    /** The invocation context factory. */
    private static InvocationContextFactory _invocationContextFactory;

    /**
     * construct a ServletTestCase with the given name.
     *
     * @param name
     *            the name
     */
    protected ServletTestCase(String name) {
        super(name);
    }

    /**
     * Returns a client object which can access the servlet context in which this test is running.
     *
     * @return the servlet unit client
     */
    protected final ServletUnitClient newClient() {
        if (_invocationContextFactory == null) {
            throw new RuntimeException(
                    "ServletTestCase.newClient called before setInvocationContextFactory was called");
        }
        return ServletUnitClient.newClient(_invocationContextFactory);
    }

    /**
     * set the invocation context factory to be used.
     *
     * @param invocationContextFactory
     *            the new invocation context factory
     */
    static void setInvocationContextFactory(InvocationContextFactory invocationContextFactory) {
        if (invocationContextFactory == null) {
            throw new RuntimeException(
                    "setInvocationContextFactory called with null invocationContextFactory parameter");
        }
        _invocationContextFactory = invocationContextFactory;
    }

}

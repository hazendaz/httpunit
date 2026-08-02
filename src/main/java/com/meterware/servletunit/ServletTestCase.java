/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2026 hazendaz
 */
package com.meterware.servletunit;

/**
 * A base class for test cases to be run via {@link JUnitServlet JUnitServlet}.
 **/
public abstract class ServletTestCase {

    /** The invocation context factory. */
    private static InvocationContextFactory _invocationContextFactory;

    /**
     * Returns a client object which can access the servlet context in which this test is running.
     *
     * @return the servlet unit client
     */
    protected final ServletUnitClient newClient() {
        if (_invocationContextFactory == null) {
            throw new IllegalStateException(
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
            throw new IllegalStateException(
                    "setInvocationContextFactory called with null invocationContextFactory parameter");
        }
        _invocationContextFactory = invocationContextFactory;
    }

}

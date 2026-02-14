/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.servletunit;

import com.meterware.httpunit.FrameSelector;
import com.meterware.httpunit.WebRequest;

import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Dictionary;

/**
 * An interface for an object which acts as a factory of InvocationContexts.
 */
public interface InvocationContextFactory {

    /**
     * Creates and returns a new invocation context to test calling of servlet methods.
     *
     * @param client
     *            the client
     * @param targetFrame
     *            the target frame
     * @param request
     *            the request
     * @param clientHeaders
     *            the client headers
     * @param messageBody
     *            the message body
     *
     * @return the invocation context
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws MalformedURLException
     *             the malformed URL exception
     */
    InvocationContext newInvocation(ServletUnitClient client, FrameSelector targetFrame, WebRequest request,
            Dictionary clientHeaders, byte[] messageBody) throws IOException, MalformedURLException;

    /**
     * Returns the session with the specified ID; if none exists or the session is invalid, will create a new session if
     * the create flag is true.
     *
     * @param sessionId
     *            the session id
     * @param create
     *            the create
     *
     * @return the session
     */
    HttpSession getSession(String sessionId, boolean create);
}

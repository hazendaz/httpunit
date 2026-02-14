/*
 * MIT License
 *
 * Copyright 2011-2026 Russell Gold
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

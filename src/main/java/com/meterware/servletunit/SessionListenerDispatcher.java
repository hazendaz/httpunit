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

import jakarta.servlet.http.HttpSession;

/**
 * The Interface SessionListenerDispatcher.
 */
interface SessionListenerDispatcher {

    /**
     * Send session created.
     *
     * @param session
     *            the session
     */
    void sendSessionCreated(HttpSession session);

    /**
     * Send session destroyed.
     *
     * @param session
     *            the session
     */
    void sendSessionDestroyed(HttpSession session);

    /**
     * Send attribute added.
     *
     * @param session
     *            the session
     * @param name
     *            the name
     * @param value
     *            the value
     */
    void sendAttributeAdded(HttpSession session, String name, Object value);

    /**
     * Send attribute replaced.
     *
     * @param session
     *            the session
     * @param name
     *            the name
     * @param oldValue
     *            the old value
     */
    void sendAttributeReplaced(HttpSession session, String name, Object oldValue);

    /**
     * Send attribute removed.
     *
     * @param session
     *            the session
     * @param name
     *            the name
     * @param oldValue
     *            the old value
     */
    void sendAttributeRemoved(HttpSession session, String name, Object oldValue);

}

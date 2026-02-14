/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
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

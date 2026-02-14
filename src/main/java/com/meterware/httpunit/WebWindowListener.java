/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

/**
 * A listener for web window openings and closings.
 */
public interface WebWindowListener {

    /**
     * Invoked when the web client opens a new window.
     *
     * @param client
     *            the client
     * @param window
     *            the window
     */
    void windowOpened(WebClient client, WebWindow window);

    /**
     * Invoked when the web client closes a window.
     *
     * @param client
     *            the client
     * @param window
     *            the window
     */
    void windowClosed(WebClient client, WebWindow window);
}

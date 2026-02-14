/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

/**
 * A listener for messages sent and received by a web client.
 */
public interface WebClientListener {

    /**
     * Invoked when the web client sends a request.
     *
     * @param src
     *            the src
     * @param req
     *            the req
     */
    void requestSent(WebClient src, WebRequest req);

    /**
     * Invoked when the web client receives a response.
     *
     * @param src
     *            the src
     * @param resp
     *            the resp
     */
    void responseReceived(WebClient src, WebResponse resp);
}

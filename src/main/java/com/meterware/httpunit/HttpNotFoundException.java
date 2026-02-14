/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This exception is thrown when the desired URL is not found.
 **/
public class HttpNotFoundException extends HttpException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * construct a HttpNotFoundException (404 Error).
     *
     * @param responseMessage
     *            the response message
     * @param baseURL
     *            the base URL
     */
    public HttpNotFoundException(String responseMessage, URL baseURL) {
        super(HttpURLConnection.HTTP_NOT_FOUND, responseMessage, baseURL);
    }

    /**
     * construct a HttpNotFoundException (404 Error).
     *
     * @param url
     *            the url
     * @param t
     *            the t
     */
    public HttpNotFoundException(URL url, Throwable t) {
        this(t.toString(), url);
    }

}

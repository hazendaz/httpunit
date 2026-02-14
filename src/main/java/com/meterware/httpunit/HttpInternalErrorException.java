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
 * This exception is thrown when an internal error is found on the server.
 **/
public class HttpInternalErrorException extends HttpException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * construct an internal http error form an url.
     *
     * @param url
     *            the url
     */
    public HttpInternalErrorException(URL url) {
        super(HttpURLConnection.HTTP_INTERNAL_ERROR, "Internal Error", url);
    }

    /**
     * construct an internal HTTP Error from a URL and a throwable.
     *
     * @param url
     *            the url
     * @param t
     *            the t
     */
    public HttpInternalErrorException(URL url, Throwable t) {
        super(HttpURLConnection.HTTP_INTERNAL_ERROR, t.toString(), url, t);
    }

}

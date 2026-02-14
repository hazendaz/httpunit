/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

import java.net.URL;

/**
 * Class used to indicate when a request to a resource resulted in an HTTP redirect response that lead to a recursive
 * loop of redirections.
 */
public class RecursiveRedirectionException extends RuntimeException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The url. */
    private URL url;

    /**
     * Create a new <code>RecursiveRedirectionException</code> with the specified URL and cause.
     *
     * @param url
     *            the {@link URL}that caused the recursive loop to be detected
     * @param cause
     *            the cause (which is saved for later retrieval by the {@link #getCause()}method). (A null value is
     *            permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public RecursiveRedirectionException(URL url, Throwable cause) {
        super(cause);
        this.url = url;
    }

    /**
     * Create a new <code>RecursiveRedirectionException</code> with the specified URL and detail message.
     *
     * @param url
     *            the <code>URL</code> that caused the recursive loop to be detected. The URL is saved for later
     *            retrieval by {@link #getURL()}
     * @param message
     *            the detail message. The detail message is saved for later retrieval by {@link #getMessage()}
     */
    public RecursiveRedirectionException(URL url, String message) {
        super(message);
        this.url = url;
    }

    /**
     * Create a new <code>RecursiveRedirectionException</code> with the specified URL, detail message and cause.
     *
     * @param url
     *            the <code>URL</code> that caused the recursive loop to be detected. The URL is saved for later
     *            retrieval by {@link #getURL()}
     * @param message
     *            the detail message. The detail message is saved for later retrieval by {@link #getMessage()}
     * @param cause
     *            the cause (which is saved for later retrieval by the {@link #getCause()}method). (A null value is
     *            permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public RecursiveRedirectionException(URL url, String message, Throwable cause) {
        super(message, cause);
        this.url = url;
    }

    /**
     * Returns the URL that caused this exception to be thrown.
     *
     * @return the <code>URL</code> that gave rise to this Exception
     */
    public URL getURL() {
        return url;
    }
}

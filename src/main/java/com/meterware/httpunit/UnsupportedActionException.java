/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

/**
 * An exception thrown when an action URL is not supported.
 **/
public class UnsupportedActionException extends RuntimeException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new unsupported action exception.
     *
     * @param message
     *            the message
     */
    public UnsupportedActionException(String message) {
        super(message);
    }

}

/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.pseudoserver;

import java.io.IOException;

/**
 * The Class UnknownMethodException.
 */
class UnknownMethodException extends IOException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new unknown method exception.
     *
     * @param method
     *            the method
     */
    UnknownMethodException(String method) {
        _method = method;
    }

    /**
     * Gets the method.
     *
     * @return the method
     */
    String getMethod() {
        return _method;
    }

    /** The method. */
    private String _method;
}

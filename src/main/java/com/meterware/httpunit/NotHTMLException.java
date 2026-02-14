/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

/**
 * The Class NotHTMLException.
 */
class NotHTMLException extends RuntimeException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new not HTML exception.
     *
     * @param contentType
     *            the content type
     */
    NotHTMLException(String contentType) {
        _contentType = contentType;
    }

    @Override
    public String getMessage() {
        return "The content type of the response is '" + _contentType
                + "': it must be 'text/html' in order to be recognized as HTML";
    }

    /** The content type. */
    private String _contentType;
}

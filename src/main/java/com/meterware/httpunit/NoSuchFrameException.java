/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

/**
 * The Class NoSuchFrameException.
 */
class NoSuchFrameException extends RuntimeException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new no such frame exception.
     *
     * @param frameName
     *            the frame name
     */
    NoSuchFrameException(String frameName) {
        _frameName = frameName;
    }

    @Override
    public String getMessage() {
        return "No frame named " + _frameName + " is currently active";
    }

    /** The frame name. */
    private String _frameName;
}

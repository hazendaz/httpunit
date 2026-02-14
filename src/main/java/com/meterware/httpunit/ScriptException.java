/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

/**
 * An exception thrown when there is a problem running a script.
 **/
public class ScriptException extends RuntimeException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new script exception.
     *
     * @param s
     *            the s
     */
    public ScriptException(String s) {
        super(s);
    }
}

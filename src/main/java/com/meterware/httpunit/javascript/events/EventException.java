/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.javascript.events;

/**
 * Event operations may throw an EventException as specified in their method descriptions.
 */
public class EventException extends Exception {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;
    /**
     * An integer indicating the type of error generated.
     */
    private final short code;

    /**
     * Creates new EventException instance.
     *
     * @param codeArg
     *            An integer indicating the type of error generated.
     */
    public EventException(short codeArg) {
        this.code = codeArg;
    }

    /**
     * An integer indicating the type of error generated.
     *
     * @return the code
     */
    public short getCode() {
        return code;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage() {
        return Short.toString(code);
    }
}

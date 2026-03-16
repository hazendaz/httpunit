/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.scripting;

/**
 * Represents an array of Options.
 */
public interface SelectionOptions {

    /**
     * Returns the length of this array.
     *
     * @return the length
     */
    int getLength();

    /**
     * Sets a new length to this array.
     *
     * @param length
     *            the new length
     */
    void setLength(int length);

    /**
     * Specify the specified option.
     *
     * @param i
     *            the i
     * @param option
     *            the option
     */
    void put(int i, SelectionOption option);

}

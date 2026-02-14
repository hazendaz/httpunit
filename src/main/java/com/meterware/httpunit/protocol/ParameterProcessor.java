/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.protocol;

import java.io.IOException;

/**
 * The Interface ParameterProcessor.
 */
public interface ParameterProcessor {

    /**
     * Adds the parameter.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     * @param characterSet
     *            the character set
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    void addParameter(String name, String value, String characterSet) throws IOException;

    /**
     * Adds the file.
     *
     * @param parameterName
     *            the parameter name
     * @param fileSpec
     *            the file spec
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    void addFile(String parameterName, UploadFileSpec fileSpec) throws IOException;
}

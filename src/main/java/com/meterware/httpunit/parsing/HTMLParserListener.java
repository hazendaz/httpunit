/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.parsing;

import java.net.URL;

/**
 * A listener for messages from the HTMLParser. This provides a mechanism to watch for errors and warnings generated
 * during parsing.
 */
public interface HTMLParserListener {

    /**
     * Invoked when the parser wishes to report a warning.
     *
     * @param url
     *            the location of the document to which the warning applies.
     * @param msg
     *            the warning message
     * @param line
     *            the line in the document on which the problematic HTML was found
     * @param column
     *            the column in the document on which the problematic HTML was found
     */
    void warning(URL url, String msg, int line, int column);

    /**
     * Invoked when the parser wishes to report an error.
     *
     * @param url
     *            the location of the document to which the error applies.
     * @param msg
     *            the warning message
     * @param line
     *            the line in the document on which the problematic HTML was found
     * @param column
     *            the column in the document on which the problematic HTML was found
     */
    void error(URL url, String msg, int line, int column);
}

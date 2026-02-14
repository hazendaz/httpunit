/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

import com.meterware.httpunit.parsing.HTMLParserListener;

/**
 * This interface represents a listener which can receive notification of errors and warnings during the parsing of an
 * HTML page.
 *
 * @deprecated as of 1.5.2, use HTMLParserListener
 **/
@Deprecated
public interface HtmlErrorListener extends HTMLParserListener {

}

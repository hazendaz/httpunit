/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.scripting;

/**
 * The Interface ScriptingHandler.
 */
public interface ScriptingHandler extends ScriptingEventHandler {

    /**
     * Supports script language.
     *
     * @param language
     *            the language
     *
     * @return true, if successful
     */
    boolean supportsScriptLanguage(String language);

    /**
     * Run script.
     *
     * @param language
     *            the language
     * @param script
     *            the script
     *
     * @return the string
     */
    String runScript(String language, String script);

    /**
     * Evaluate expression.
     *
     * @param urlString
     *            the url string
     *
     * @return the object
     */
    Object evaluateExpression(String urlString);

    /**
     * Clear caches.
     */
    void clearCaches();
}

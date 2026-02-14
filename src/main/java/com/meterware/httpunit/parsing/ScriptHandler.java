/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.parsing;

/**
 * The Interface ScriptHandler.
 */
public interface ScriptHandler {

    /**
     * Gets the included script.
     *
     * @param srcAttribute
     *            the src attribute
     *
     * @return the included script
     */
    String getIncludedScript(String srcAttribute);

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
     * @param scriptText
     *            the script text
     *
     * @return the string
     */
    String runScript(String language, String scriptText);

}

/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.scripting;

import com.meterware.httpunit.HTMLElement;
import com.meterware.httpunit.WebResponse;

/**
 * A factory for creating ScriptingEngine objects.
 */
public interface ScriptingEngineFactory {

    /**
     * Returns true if this engine is enabled.
     *
     * @return true, if is enabled
     */
    boolean isEnabled();

    /**
     * Associates a scripting engine with the specified HTML web response.
     *
     * @param response
     *            the response
     */
    void associate(WebResponse response);

    /**
     * Runs the 'onload' event (if any) for the specified HTML web response. Will associate a scripting engine with the
     * response if that has not already been done.
     *
     * @param response
     *            the response
     */
    void load(WebResponse response);

    /**
     * Determines whether script errors result in exceptions or warning messages.
     *
     * @param throwExceptions
     *            the new throw exceptions on error
     */
    void setThrowExceptionsOnError(boolean throwExceptions);

    /**
     * Returns true if script errors cause exceptions to be thrown.
     *
     * @return true, if is throw exceptions on error
     */
    boolean isThrowExceptionsOnError();

    /**
     * Returns the accumulated script error messages encountered. Error messages are accumulated only if
     * 'throwExceptionsOnError' is disabled.
     *
     * @return the error messages
     */
    String[] getErrorMessages();

    /**
     * Clears the accumulated script error messages.
     */
    void clearErrorMessages();

    /**
     * handle Exceptions.
     *
     * @param e
     *            - the exception to handle
     * @param badScript
     *            - the script that caused the problem
     */
    void handleScriptException(Exception e, String badScript);

    /**
     * Creates a new ScriptingEngine object.
     *
     * @param elementBase
     *            the element base
     *
     * @return the scripting handler
     */
    ScriptingHandler createHandler(HTMLElement elementBase);

    /**
     * Creates a new ScriptingEngine object.
     *
     * @param response
     *            the response
     *
     * @return the scripting handler
     */
    ScriptingHandler createHandler(WebResponse response);
}

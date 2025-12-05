/*
 * MIT License
 *
 * Copyright 2011-2025 Russell Gold
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
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

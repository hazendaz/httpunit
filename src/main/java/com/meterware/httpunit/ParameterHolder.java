/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

import com.meterware.httpunit.protocol.ParameterCollection;
import com.meterware.httpunit.protocol.ParameterProcessor;
import com.meterware.httpunit.protocol.UploadFileSpec;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * This abstract class is extended by classes which hold parameters for web requests. Note that it is an abstract class
 * rather than an interface in order to keep its methods package-local.
 **/
abstract class ParameterHolder implements ParameterCollection {

    /**
     * Specifies the position at which an image button (if any) was clicked. This default implementation does nothing.
     *
     * @param imageButton
     *            the image button
     * @param x
     *            the x
     * @param y
     *            the y
     */
    void selectImageButtonPosition(SubmitButton imageButton, int x, int y) {
    }

    /**
     * Iterates through the fixed, predefined parameters in this holder, recording them in the supplied parameter
     * processor.\ These parameters always go on the URL, no matter what encoding method is used.
     *
     * @param processor
     *            the processor
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    abstract void recordPredefinedParameters(ParameterProcessor processor) throws IOException;

    /**
     * Returns an array of all parameter names in this collection.
     *
     * @return the parameter names
     */
    abstract String[] getParameterNames();

    /**
     * Returns the multiple default values of the named parameter.
     *
     * @param name
     *            the name
     *
     * @return the parameter values
     */
    abstract String[] getParameterValues(String name);

    /**
     * Removes a parameter name from this collection.
     *
     * @param name
     *            the name
     */
    abstract void removeParameter(String name);

    /**
     * Sets the value of a parameter in a web request.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     */
    abstract void setParameter(String name, String value);

    /**
     * Sets the multiple values of a parameter in a web request.
     *
     * @param name
     *            the name
     * @param values
     *            the values
     */
    abstract void setParameter(String name, String[] values);

    /**
     * Sets the multiple values of a file upload parameter in a web request.
     *
     * @param name
     *            the name
     * @param files
     *            the files
     */
    abstract void setParameter(String name, UploadFileSpec[] files);

    /**
     * Returns true if the specified name is that of a file parameter. The default implementation returns false.
     *
     * @param name
     *            the name
     *
     * @return true, if is file parameter
     */
    boolean isFileParameter(String name) {
        return false;
    }

    /**
     * Returns the character set encoding for the request.
     *
     * @return the character set
     */
    String getCharacterSet() {
        return StandardCharsets.ISO_8859_1.name();
    }

    /**
     * Checks if is submit as mime.
     *
     * @return true, if is submit as mime
     */
    abstract boolean isSubmitAsMime();
}

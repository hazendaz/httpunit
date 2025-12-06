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

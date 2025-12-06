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

import java.net.URLEncoder;
import java.nio.charset.Charset;

/**
 * The Class URLEncodedString.
 */
public class URLEncodedString implements ParameterProcessor {

    /** The Constant DEFAULT_BUFFER_SIZE. */
    public static final int DEFAULT_BUFFER_SIZE = 128;

    /** The buffer. */
    private StringBuilder _buffer = new StringBuilder(DEFAULT_BUFFER_SIZE);

    /** The have parameters. */
    private boolean _haveParameters = false;

    /**
     * Gets the string.
     *
     * @return the string
     */
    public String getString() {
        return _buffer.toString();
    }

    @Override
    public void addParameter(String name, String value, String characterSet) {
        if (_haveParameters) {
            _buffer.append('&');
        }
        _buffer.append(encode(name, characterSet));
        if (value != null) {
            _buffer.append('=').append(encode(value, characterSet));
        }
        _haveParameters = true;
    }

    @Override
    public void addFile(String parameterName, UploadFileSpec fileSpec) {
        throw new RuntimeException("May not URL-encode a file upload request");
    }

    /**
     * Returns a URL-encoded version of the string.
     *
     * @param source
     *            the source
     * @param characterSet
     *            the character set
     *
     * @return the string
     */
    private String encode(String source, String characterSet) {
        return URLEncoder.encode(source, Charset.forName(characterSet));
    }

}

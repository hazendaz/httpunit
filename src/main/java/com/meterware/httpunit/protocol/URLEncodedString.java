/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
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

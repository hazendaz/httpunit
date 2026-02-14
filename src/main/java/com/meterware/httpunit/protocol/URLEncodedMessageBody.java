/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.protocol;

import com.meterware.httpunit.HttpUnitOptions;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * A POST method request message body which uses the default URL encoding.
 **/
class URLEncodedMessageBody extends MessageBody {

    /**
     * Instantiates a new URL encoded message body.
     *
     * @param characterSet
     *            the character set
     */
    URLEncodedMessageBody(String characterSet) {
        super(characterSet);
    }

    /**
     * Returns the content type of this message body.
     **/
    @Override
    public String getContentType() {
        return "application/x-www-form-urlencoded"
                + (!HttpUnitOptions.isPostIncludesCharset() ? "" : "; charset=" + getCharacterSet());
    }

    /**
     * Transmits the body of this request as a sequence of bytes.
     **/
    @Override
    public void writeTo(OutputStream outputStream, ParameterCollection parameters) throws IOException {
        outputStream.write(getParameterString(parameters).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Gets the parameter string.
     *
     * @param parameters
     *            the parameters
     *
     * @return the parameter string
     */
    private String getParameterString(ParameterCollection parameters) {
        try {
            URLEncodedString encoder = new URLEncodedString();
            parameters.recordParameters(encoder);
            return encoder.getString();
        } catch (IOException e) {
            throw new RuntimeException("Programming error: " + e); // should never happen
        }
    }
}

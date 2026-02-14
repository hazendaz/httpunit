/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.protocol;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An abstract class representing the body of a web request.
 **/
public abstract class MessageBody {

    /** The character set. */
    private String _characterSet;

    /**
     * Creates a message body for a POST request, selecting an appropriate encoding.
     *
     * @param mimeEncoded
     *            if true, indicates that the request is using mime encoding.
     * @param characterSet
     *            the character set of the request.
     *
     * @return an appropriate message body.
     */
    public static MessageBody createPostMethodMessageBody(boolean mimeEncoded, String characterSet) {
        return mimeEncoded ? (MessageBody) new MimeEncodedMessageBody(characterSet)
                : (MessageBody) new URLEncodedMessageBody(characterSet);
    }

    /**
     * Instantiates a new message body.
     *
     * @param characterSet
     *            the character set
     */
    public MessageBody(String characterSet) {
        _characterSet = characterSet;
    }

    /**
     * Returns the character set associated with this message body.
     *
     * @return the character set
     */
    public String getCharacterSet() {
        return _characterSet;
    }

    /**
     * Returns the content type of this message body. For text messages, this should include the character set.
     *
     * @return the content type
     */
    public abstract String getContentType();

    /**
     * Transmits the body of this request as a sequence of bytes.
     *
     * @param outputStream
     *            the output stream
     * @param parameters
     *            the parameters
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract void writeTo(OutputStream outputStream, ParameterCollection parameters) throws IOException;
}

/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.servletunit;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

/**
 * An implementation of the standard servlet input stream.
 */
class ServletInputStreamImpl extends ServletInputStream {

    /** The base stream. */
    private ByteArrayInputStream _baseStream;

    /**
     * Instantiates a new servlet input stream impl.
     *
     * @param messageBody
     *            the message body
     */
    public ServletInputStreamImpl(byte[] messageBody) {
        _baseStream = new ByteArrayInputStream(messageBody);
    }

    @Override
    public int read() throws IOException {
        return _baseStream.read();
    }

    @Override
    public boolean isFinished() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isReady() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        // TODO Auto-generated method stub

    }

}

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
package com.meterware.pseudoserver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * The Class ReceivedHttpMessage.
 */
abstract class ReceivedHttpMessage {

    /** The Constant CR. */
    private static final int CR = 13;

    /** The Constant LF. */
    private static final int LF = 10;

    /** The reader. */
    private Reader _reader;

    /** The headers. */
    private Hashtable _headers = new Hashtable<>();

    /** The request body. */
    private byte[] _requestBody;

    /**
     * Instantiates a new received http message.
     *
     * @param inputStream
     *            the input stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    ReceivedHttpMessage(InputStream inputStream) throws IOException {
        interpretMessageHeader(readHeaderLine(inputStream));
        readHeaders(inputStream);
        readMessageBody(inputStream);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClassName()).append("[ ");
        appendMessageHeader(sb);
        sb.append("\n");
        for (Enumeration e = _headers.keys(); e.hasMoreElements();) {
            Object key = e.nextElement();
            sb.append("      ").append(key).append(": ").append(_headers.get(key)).append("\n");
        }
        sb.append("   body contains ").append(getBody().length).append(" byte(s)]");
        return sb.toString();
    }

    /**
     * Read header line.
     *
     * @param inputStream
     *            the input stream
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private String readHeaderLine(InputStream inputStream) throws IOException {
        return new String(readDelimitedChunk(inputStream));
    }

    /**
     * Read delimited chunk.
     *
     * @param inputStream
     *            the input stream
     *
     * @return the byte[]
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private byte[] readDelimitedChunk(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b = inputStream.read();
        while (b != CR) {
            baos.write(b);
            b = inputStream.read();
        }

        b = inputStream.read();
        if (b != LF) {
            throw new IOException("Bad header line termination: " + b);
        }
        return baos.toByteArray();
    }

    /**
     * Append contents.
     *
     * @param sb
     *            the sb
     */
    void appendContents(StringBuilder sb) {
        for (Enumeration e = _headers.keys(); e.hasMoreElements();) {
            Object key = e.nextElement();
            sb.append("      ").append(key).append(": ").append(_headers.get(key)).append("\n");
        }
        sb.append("   body contains ").append(getBody().length).append(" byte(s)");
    }

    /**
     * Gets the reader.
     *
     * @return the reader
     */
    Reader getReader() {
        return _reader;
    }

    /**
     * Gets the header.
     *
     * @param name
     *            the name
     *
     * @return the header
     */
    String getHeader(String name) {
        return (String) _headers.get(name.toUpperCase());
    }

    /**
     * Gets the body.
     *
     * @return the body
     */
    byte[] getBody() {
        return _requestBody;
    }

    /**
     * Read message body.
     *
     * @param inputStream
     *            the input stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void readMessageBody(InputStream inputStream) throws IOException {
        if ("chunked".equalsIgnoreCase(getHeader("Transfer-Encoding"))) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while (getNextChunkLength(inputStream) > 0) {
                baos.write(readDelimitedChunk(inputStream));
            }
            flushChunkTrailer(inputStream);
            _requestBody = baos.toByteArray();
        } else {
            int totalExpected = getContentLength();
            ByteArrayOutputStream baos = new ByteArrayOutputStream(totalExpected);
            byte[] buffer = new byte[1024];
            int total = 0;
            int count = -1;
            while (total < totalExpected && (count = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, count);
                total += count;
            }
            baos.flush();
            _requestBody = baos.toByteArray();
        }
        _reader = new InputStreamReader(new ByteArrayInputStream(_requestBody));
    }

    /**
     * Flush chunk trailer.
     *
     * @param inputStream
     *            the input stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void flushChunkTrailer(InputStream inputStream) throws IOException {
        byte[] line;
        do {
            line = readDelimitedChunk(inputStream);
        } while (line.length > 0);
    }

    /**
     * Gets the next chunk length.
     *
     * @param inputStream
     *            the input stream
     *
     * @return the next chunk length
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private int getNextChunkLength(InputStream inputStream) throws IOException {
        try {
            return Integer.parseInt(readHeaderLine(inputStream), 16);
        } catch (NumberFormatException e) {
            throw new IOException("Unabled to read chunk length: " + e);
        }
    }

    /**
     * Gets the content length.
     *
     * @return the content length
     */
    private int getContentLength() {
        try {
            return Integer.parseInt(getHeader("Content-Length"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Read headers.
     *
     * @param inputStream
     *            the input stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void readHeaders(InputStream inputStream) throws IOException {
        String lastHeader = null;

        String header = readHeaderLine(inputStream);
        while (header.length() > 0) {
            if (header.charAt(0) <= ' ') {
                if (lastHeader == null) {
                    continue;
                }
                _headers.put(lastHeader, _headers.get(lastHeader) + header.trim());
            } else {
                lastHeader = header.substring(0, header.indexOf(':')).toUpperCase();
                _headers.put(lastHeader, header.substring(header.indexOf(':') + 1).trim());
            }
            header = readHeaderLine(inputStream);
        }
    }

    /**
     * Gets the class name.
     *
     * @return the class name
     */
    private String getClassName() {
        return getClass().getName().substring(getClass().getName().lastIndexOf('.') + 1);
    }

    /**
     * Append message header.
     *
     * @param sb
     *            the sb
     */
    abstract void appendMessageHeader(StringBuilder sb);

    /**
     * Interpret message header.
     *
     * @param messageHeader
     *            the message header
     */
    abstract void interpretMessageHeader(String messageHeader);

}

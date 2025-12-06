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

import com.meterware.httpunit.HttpUnitUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Vector;

/**
 * A resource to be returned from the simulated server.
 **/
public class WebResource {

    /** The Constant DEFAULT_CONTENT_TYPE. */
    static final String DEFAULT_CONTENT_TYPE = "text/html";

    /** The closes connection. */
    private boolean _closesConnection;

    /** The contents. */
    private byte[] _contents;

    /** The string. */
    private String _string;

    /** The stream. */
    private InputStream _stream;

    /** The response code. */
    private int _responseCode;

    /** The send character set. */
    private boolean _sendCharacterSet;

    /** The content type. */
    private String _contentType = DEFAULT_CONTENT_TYPE;

    /** The character set. */
    private String _characterSet = StandardCharsets.ISO_8859_1.name();

    /** The has explicit content type header. */
    private boolean _hasExplicitContentTypeHeader;

    /** The has explicit content length header. */
    private boolean _hasExplicitContentLengthHeader;

    /** The headers. */
    private Vector _headers = new Vector<>();

    /** The is chunked. */
    private boolean _isChunked;

    /**
     * Instantiates a new web resource.
     *
     * @param contents
     *            the contents
     */
    public WebResource(String contents) {
        this(contents, DEFAULT_CONTENT_TYPE);
    }

    /**
     * Instantiates a new web resource.
     *
     * @param contents
     *            the contents
     * @param contentType
     *            the content type
     */
    public WebResource(String contents, String contentType) {
        this(contents, contentType, HttpURLConnection.HTTP_OK);
    }

    /**
     * Instantiates a new web resource.
     *
     * @param contents
     *            the contents
     * @param contentType
     *            the content type
     */
    public WebResource(byte[] contents, String contentType) {
        this(contents, contentType, HttpURLConnection.HTTP_OK);
    }

    /**
     * Adds the header.
     *
     * @param header
     *            the header
     */
    public void addHeader(String header) {
        _headers.addElement(header);
        if (header.toLowerCase(Locale.ENGLISH).startsWith("content-type")) {
            _hasExplicitContentTypeHeader = true;
        }
        if (header.toLowerCase(Locale.ENGLISH).startsWith("content-length")) {
            _hasExplicitContentLengthHeader = true;
        }
        if (header.trim().toLowerCase(Locale.ENGLISH).startsWith("connection")
                && header.trim().toLowerCase(Locale.ENGLISH).endsWith("close")) {
            _closesConnection = true;
        }
        if (header.trim().toLowerCase(Locale.ENGLISH).startsWith("transfer-encoding")
                && header.trim().toLowerCase(Locale.ENGLISH).endsWith("chunked")) {
            _isChunked = true;
        }
    }

    /**
     * Sets the character set.
     *
     * @param characterSet
     *            the new character set
     */
    public void setCharacterSet(String characterSet) {
        _characterSet = characterSet;
    }

    /**
     * Sets the send character set.
     *
     * @param enabled
     *            the new send character set
     */
    public void setSendCharacterSet(boolean enabled) {
        _sendCharacterSet = enabled;
    }

    /**
     * Suppress automatic length header.
     */
    public void suppressAutomaticLengthHeader() {
        _hasExplicitContentLengthHeader = true;
    }

    /**
     * Suppress automatic content type header.
     */
    public void suppressAutomaticContentTypeHeader() {
        _hasExplicitContentTypeHeader = true;
    }

    /**
     * Instantiates a new web resource.
     *
     * @param contents
     *            the contents
     * @param responseCode
     *            the response code
     */
    public WebResource(String contents, int responseCode) {
        this(contents, DEFAULT_CONTENT_TYPE, responseCode);
    }

    /**
     * Instantiates a new web resource.
     *
     * @param contents
     *            the contents
     * @param contentType
     *            the content type
     * @param responseCode
     *            the response code
     */
    public WebResource(String contents, String contentType, int responseCode) {
        _string = contents;
        _contentType = contentType;
        _responseCode = responseCode;
    }

    /**
     * Instantiates a new web resource.
     *
     * @param contents
     *            the contents
     * @param contentType
     *            the content type
     * @param responseCode
     *            the response code
     */
    public WebResource(byte[] contents, String contentType, int responseCode) {
        _contents = contents;
        _contentType = contentType;
        _responseCode = responseCode;
    }

    /**
     * Instantiates a new web resource.
     *
     * @param stream
     *            the stream
     * @param contentType
     *            the content type
     * @param responseCode
     *            the response code
     */
    public WebResource(InputStream stream, String contentType, int responseCode) {
        _stream = stream;
        _contentType = contentType;
        _responseCode = responseCode;
        addHeader("Connection: close");
    }

    /**
     * Gets the headers.
     *
     * @return the headers
     */
    String[] getHeaders() {
        final Vector effectiveHeaders = (Vector) _headers.clone();
        if (!_hasExplicitContentTypeHeader) {
            effectiveHeaders.add(getContentTypeHeader());
        }
        if (_stream == null && !_hasExplicitContentLengthHeader && !isChunked()) {
            effectiveHeaders.add(getContentLengthHeader());
        }
        String[] headers = new String[effectiveHeaders.size()];
        effectiveHeaders.copyInto(headers);
        return headers;
    }

    /**
     * Checks if is chunked.
     *
     * @return true, if is chunked
     */
    private boolean isChunked() {
        return _isChunked;
    }

    /**
     * Closes connection.
     *
     * @return true, if successful
     */
    boolean closesConnection() {
        return _closesConnection;
    }

    /**
     * Write to.
     *
     * @param outputStream
     *            the output stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    void writeTo(OutputStream outputStream) throws IOException {
        if (_stream == null) {
            outputStream.write(getContentsAsBytes());
        } else {
            byte[] buffer = new byte[8 * 1024];
            int count = 0;
            do {
                outputStream.write(buffer, 0, count);
                count = _stream.read(buffer, 0, buffer.length);
            } while (count != -1);
        }
    }

    /**
     * To string.
     *
     * @param contentsAsBytes
     *            the contents as bytes
     *
     * @return the string
     */
    static String toString(byte[] contentsAsBytes) {
        StringBuilder sb = new StringBuilder();
        for (byte contentsAsByte : contentsAsBytes) {
            sb.append(Integer.toHexString(contentsAsByte)).append(' ');
        }
        return sb.toString();
    }

    /**
     * Gets the contents as bytes.
     *
     * @return the contents as bytes
     */
    private byte[] getContentsAsBytes() {
        if (_contents != null) {
            return _contents;
        }
        if (_string != null) {
            return _string.getBytes(getCharacterSet());
        }
        throw new IllegalStateException("Cannot get bytes from stream");
    }

    /**
     * Gets the content type header.
     *
     * @return the content type header
     */
    private String getContentTypeHeader() {
        return "Content-Type: " + _contentType + getCharacterSetParameter();
    }

    /**
     * Gets the content length header.
     *
     * @return the content length header
     */
    private String getContentLengthHeader() {
        return "Content-Length: " + getContentsAsBytes().length;
    }

    /**
     * Gets the character set.
     *
     * @return the character set
     */
    Charset getCharacterSet() {
        return Charset.forName(HttpUnitUtils.stripQuotes(_characterSet));
    }

    /**
     * Gets the character set parameter.
     *
     * @return the character set parameter
     */
    String getCharacterSetParameter() {
        if (!_sendCharacterSet) {
            return "";
        }
        return "; charset=" + _characterSet;
    }

    /**
     * Gets the response code.
     *
     * @return the response code
     */
    int getResponseCode() {
        return _responseCode;
    }

    @Override
    public String toString() {
        return "WebResource [code=" + _responseCode + "; type = " + _contentType + "; charset = " + _characterSet
                + "]\n" + getContentsAsString();
    }

    /**
     * Gets the contents as string.
     *
     * @return the contents as string
     */
    private String getContentsAsString() {
        if (_string != null) {
            return _string;
        }
        return "<< hex bytes >>";
    }

}

/*
 * MIT License
 *
 * Copyright 2011-2026 Russell Gold
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
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * Represents a single HTTP request, extracted from the input stream.
 */
public class HttpRequest extends ReceivedHttpMessage {

    /** The protocol. */
    private String _protocol;

    /** The command. */
    private String _command;

    /** The uri. */
    private String _uri;

    /** The parameters. */
    private Hashtable _parameters;

    /**
     * Instantiates a new http request.
     *
     * @param inputStream
     *            the input stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    HttpRequest(InputStream inputStream) throws IOException {
        super(inputStream);
    }

    @Override
    void interpretMessageHeader(String messageHeader) {
        StringTokenizer st = new StringTokenizer(messageHeader);
        _command = st.nextToken();
        _uri = st.nextToken();
        _protocol = st.nextToken();
    }

    @Override
    void appendMessageHeader(StringBuilder sb) {
        sb.append(_command).append(' ').append(_uri).append(' ').append(_protocol);
    }

    /**
     * Returns the command associated with this request.
     *
     * @return the command
     */
    public String getCommand() {
        return _command;
    }

    /**
     * Returns the URI specified in the message header for this request.
     *
     * @return the uri
     */
    public String getURI() {
        return _uri;
    }

    /**
     * Returns the protocol string specified in the message header for this request.
     *
     * @return the protocol
     */
    public String getProtocol() {
        return _protocol;
    }

    /**
     * Returns the parameter with the specified name. If no such parameter exists, will return null.
     *
     * @param name
     *            the name
     *
     * @return the parameter
     */
    public String[] getParameter(String name) {
        if (_parameters == null) {
            if (_command.equalsIgnoreCase("GET") || _command.equalsIgnoreCase("HEAD")) {
                _parameters = readParameters(getParameterString(_uri));
            } else {
                _parameters = readParameters(new String(getBody()));
            }
        }
        return (String[]) _parameters.get(name);
    }

    /**
     * Gets the parameter string.
     *
     * @param uri
     *            the uri
     *
     * @return the parameter string
     */
    private String getParameterString(String uri) {
        return uri.indexOf('?') < 0 ? "" : uri.substring(uri.indexOf('?') + 1);
    }

    /**
     * Wants keep alive.
     *
     * @return true, if successful
     */
    boolean wantsKeepAlive() {
        if ("Keep-alive".equalsIgnoreCase(getConnectionHeader())) {
            return true;
        }
        if (_protocol.equals("HTTP/1.1")) {
            return !"Close".equalsIgnoreCase(getConnectionHeader());
        }
        return false;
    }

    /**
     * Read parameters.
     *
     * @param content
     *            the content
     *
     * @return the hashtable
     */
    private Hashtable readParameters(String content) {
        Hashtable parameters = new Hashtable<>();
        if (content == null || content.trim().isEmpty()) {
            return parameters;
        }

        for (String spec : content.split("&")) {
            String[] split = spec.split("=");
            addParameter(parameters, HttpUnitUtils.decode(split[0]),
                    split.length < 2 ? null : HttpUnitUtils.decode(split[1]));
        }
        return parameters;
    }

    /**
     * Adds the parameter.
     *
     * @param parameters
     *            the parameters
     * @param name
     *            the name
     * @param value
     *            the value
     */
    private void addParameter(Hashtable parameters, String name, String value) {
        String[] oldValues = (String[]) parameters.get(name);
        if (oldValues == null) {
            parameters.put(name, new String[] { value });
        } else {
            String[] values = new String[oldValues.length + 1];
            System.arraycopy(oldValues, 0, values, 0, oldValues.length);
            values[oldValues.length] = value;
            parameters.put(name, values);
        }
    }

    /**
     * Gets the connection header.
     *
     * @return the connection header
     */
    private String getConnectionHeader() {
        return getHeader("Connection");
    }

}

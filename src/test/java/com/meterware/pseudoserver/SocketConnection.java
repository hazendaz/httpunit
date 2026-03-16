/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.pseudoserver;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

/**
 * The Class SocketConnection.
 */
class SocketConnection {

    /** The socket. */
    private Socket _socket;

    /** The os. */
    private OutputStream _os;

    /** The is. */
    private InputStream _is;

    /** The host. */
    private String _host;

    /** The is chunking. */
    private boolean _isChunking;

    /**
     * Instantiates a new socket connection.
     *
     * @param host
     *            the host
     * @param port
     *            the port
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws UnknownHostException
     *             the unknown host exception
     */
    public SocketConnection(String host, int port) throws IOException, UnknownHostException {
        _host = host;
        _socket = new Socket(host, port);
        _os = _socket.getOutputStream();
        _is = new BufferedInputStream(_socket.getInputStream());
    }

    /**
     * Close.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    void close() throws IOException {
        _socket.close();
    }

    /**
     * Gets the response.
     *
     * @param method
     *            the method
     * @param path
     *            the path
     *
     * @return the response
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    SocketResponse getResponse(String method, String path) throws IOException {
        if (_isChunking) {
            throw new IllegalStateException("May not initiate a new request while chunking.");
        }
        sendHTTPLine(method + ' ' + path + " HTTP/1.1");
        sendHTTPLine("Host: " + _host);
        sendHTTPLine("Connection: Keep-Alive");
        sendHTTPLine("");
        return new SocketResponse(_is);
    }

    /**
     * Gets the response.
     *
     * @param method
     *            the method
     * @param path
     *            the path
     * @param body
     *            the body
     *
     * @return the response
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    SocketResponse getResponse(String method, String path, String body) throws IOException {
        if (_isChunking) {
            throw new IllegalStateException("May not initiate a new request while chunking.");
        }
        sendHTTPLine(method + ' ' + path + " HTTP/1.1");
        sendHTTPLine("Host: " + _host);
        sendHTTPLine("Connection: Keep-Alive");
        sendHTTPLine("Content-Length: " + body.length());
        sendHTTPLine("");
        _os.write(body.getBytes(StandardCharsets.UTF_8));
        return new SocketResponse(_is);
    }

    /**
     * Start chunked response.
     *
     * @param method
     *            the method
     * @param path
     *            the path
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    void startChunkedResponse(String method, String path) throws IOException {
        if (_isChunking) {
            throw new IllegalStateException("May not initiate a new request while chunking.");
        }
        sendHTTPLine(method + ' ' + path + " HTTP/1.1");
        sendHTTPLine("Host: " + _host);
        sendHTTPLine("Connection: Keep-Alive");
        sendHTTPLine("Transfer-Encoding: chunked");
        sendHTTPLine("");
        _isChunking = true;
    }

    /**
     * Send chunk.
     *
     * @param chunk
     *            the chunk
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void sendChunk(String chunk) throws IOException {
        if (!_isChunking) {
            throw new IllegalStateException("May not send a chunk when not in mid-request.");
        }
        sendHTTPLine(Integer.toHexString(chunk.length()));
        sendHTTPLine(chunk);
    }

    /**
     * Gets the response.
     *
     * @return the response
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    SocketResponse getResponse() throws IOException {
        if (!_isChunking) {
            throw new IllegalStateException("Not chunking a request.");
        }
        _isChunking = false;
        sendHTTPLine("0");
        sendHTTPLine("");
        return new SocketResponse(_is);
    }

    /**
     * Send HTTP line.
     *
     * @param line
     *            the line
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void sendHTTPLine(final String line) throws IOException {
        _os.write(line.getBytes(StandardCharsets.UTF_8));
        _os.write(13);
        _os.write(10);
    }

    /**
     * The Class SocketResponse.
     */
    class SocketResponse extends ReceivedHttpMessage {

        /** The protocol. */
        private String _protocol;

        /** The response code. */
        private int _responseCode;

        /** The message. */
        private String _message;

        /**
         * Instantiates a new socket response.
         *
         * @param inputStream
         *            the input stream
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        public SocketResponse(InputStream inputStream) throws IOException {
            super(inputStream);
        }

        /**
         * Append message header.
         *
         * @param sb
         *            the sb
         */
        @Override
        void appendMessageHeader(StringBuilder sb) {
            sb.append(_protocol).append(' ').append(_responseCode).append(' ').append(_message);
        }

        /**
         * Interpret message header.
         *
         * @param messageHeader
         *            the message header
         */
        @Override
        void interpretMessageHeader(String messageHeader) {
            int s1 = messageHeader.indexOf(' ');
            int s2 = messageHeader.indexOf(' ', s1 + 1);

            _protocol = messageHeader.substring(0, s1);
            _message = messageHeader.substring(s2 + 1);

            try {
                _responseCode = Integer.parseInt(messageHeader.substring(s1 + 1, s2));
            } catch (NumberFormatException e) {
                _responseCode = -1;
            }
        }

        /**
         * Gets the response code.
         *
         * @return the response code
         */
        public int getResponseCode() {
            return _responseCode;
        }
    }

}

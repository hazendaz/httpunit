/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

import java.net.URL;

/**
 * This exception is thrown when an Http error (response code 4xx or 5xx) is detected.
 **/
public class HttpException extends RuntimeException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * throw a http Exception with the given responseCode.
     *
     * @param responseCode
     *            the response code
     */
    protected HttpException(int responseCode) {
        _responseCode = responseCode;
        System.err.println(responseCode);
    }

    /**
     * throw a http Exception with the given responseCode and cause.
     *
     * @param responseCode
     *            the response code
     * @param cause
     *            the cause
     */
    protected HttpException(int responseCode, Throwable cause) {
        _responseCode = responseCode;
        _cause = cause;
    }

    /**
     * throw a http Exception with the given responseCode and Message and base url.
     *
     * @param responseCode
     *            the response code
     * @param responseMessage
     *            the response message
     * @param baseURL
     *            the base URL
     */
    protected HttpException(int responseCode, String responseMessage, URL baseURL) {
        _responseMessage = responseMessage;
        _responseCode = responseCode;
        _url = baseURL;
    }

    /**
     * throw a http Exception with the given responseCode and Message, base url and cause.
     *
     * @param responseCode
     *            the response code
     * @param responseMessage
     *            the response message
     * @param baseURL
     *            the base URL
     * @param cause
     *            the cause
     */
    protected HttpException(int responseCode, String responseMessage, URL baseURL, Throwable cause) {
        _responseMessage = responseMessage;
        _responseCode = responseCode;
        _url = baseURL;
        _cause = cause;
    }

    /**
     * get the Message for the http Exception
     *
     * @return - the message of the Exception
     */
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(HttpUnitUtils.DEFAULT_TEXT_BUFFER_SIZE).append("Error on HTTP request: ");
        sb.append(_responseCode);
        if (_responseMessage != null) {
            sb.append(" ");
            sb.append(_responseMessage);
            sb.append("");
        }
        if (_url != null) {
            sb.append(" [");
            sb.append(_url.toExternalForm());
            sb.append("]");
        }
        return sb.toString();
    }

    /**
     * get the response Code of this http Exception.
     *
     * @return - the response Code code 4xx or 5xx
     */
    public int getResponseCode() {
        return _responseCode;
    }

    /**
     * get the response Message of this http Exception.
     *
     * @return the response message
     */
    public String getResponseMessage() {
        return _responseMessage;
    }

    /** The response code. */
    // private local copies of variables
    private int _responseCode;

    /** The url. */
    private URL _url;

    /** The response message. */
    private String _responseMessage;

    /**
     * get the cause (if any)
     */
    @Override
    public Throwable getCause() {
        return _cause;
    }

    /** The cause. */
    private Throwable _cause;

    /** The response. */
    // see feature request [ 914314 ] Add HttpException.getResponse for better reporting
    private WebResponse response;

    /**
     * return the WebResponse associated with this Exception (if any).
     *
     * @return the response
     */
    public WebResponse getResponse() {
        return response;
    }

    /**
     * add the given response to this exception.
     *
     * @param response
     *            the new response
     */
    public void setResponse(WebResponse response) {
        this.response = response;
    }

}

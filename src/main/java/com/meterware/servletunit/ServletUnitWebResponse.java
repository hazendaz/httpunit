/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.servletunit;

import com.meterware.httpunit.FrameSelector;
import com.meterware.httpunit.WebResponse;

import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A response from to a request from the simulated servlet environment.
 **/
class ServletUnitWebResponse extends WebResponse {

    /**
     * Constructs a response object from a servlet response.
     *
     * @param client
     *            the client
     * @param frame
     *            the target frame on which the response will be displayed
     * @param url
     *            the url from which the response was received
     * @param response
     *            the response populated by the servlet
     * @param throwExceptionOnError
     *            the throw exception on error
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    ServletUnitWebResponse(ServletUnitClient client, FrameSelector frame, URL url, HttpServletResponse response,
            boolean throwExceptionOnError) throws IOException {
        super(client, frame, url);
        _response = (ServletUnitHttpResponse) response;
        /** make sure that any IO exception for HTML received page happens here, not later. **/
        if (getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST || !throwExceptionOnError) {
            defineRawInputStream(new ByteArrayInputStream(_response.getContents()));
            if (getContentType().startsWith("text")) {
                loadResponseText();
            }
        }
    }

    /**
     * Constructs a response object from a servlet response.
     *
     * @param client
     *            the client
     * @param frame
     *            the target frame on which the response will be displayed
     * @param url
     *            the url from which the response was received
     * @param response
     *            the response populated by the servlet
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    ServletUnitWebResponse(ServletUnitClient client, FrameSelector frame, URL url, HttpServletResponse response)
            throws IOException {
        this(client, frame, url, response, true);
    }

    /**
     * Returns the response code associated with this response.
     **/
    @Override
    public int getResponseCode() {
        return _response.getStatus();
    }

    /**
     * Returns the response message associated with this response.
     **/
    @Override
    public String getResponseMessage() {
        return _response.getMessage();
    }

    @Override
    public String[] getHeaderFieldNames() {
        return _response.getHeaderFieldNames();
    }

    /**
     * Returns the value for the specified header field. If no such field is defined, will return null.
     **/
    @Override
    public String getHeaderField(String fieldName) {
        return _response.getHeaderField(fieldName);
    }

    @Override
    public String[] getHeaderFields(String fieldName) {
        return _response.getHeaderFields(fieldName);
    }

    @Override
    public String toString() {
        return "[ _response = " + _response + "]";
    }

    // -------------------------------------------- private members ------------------------------------------------

    /** The response. */
    private ServletUnitHttpResponse _response;

}

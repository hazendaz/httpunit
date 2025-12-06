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

import java.io.IOException;
import java.io.Reader;

/**
 * A basic simulated servlet for testing the HttpUnit library.
 **/
public abstract class PseudoServlet {

    /** The Constant CONTENTS. */
    public static final String CONTENTS = "contents";

    /**
     * Returns a resource object as a result of a get request.
     *
     * @param methodType
     *            the method type
     *
     * @return the response
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public WebResource getResponse(String methodType) throws IOException {
        if (methodType.equalsIgnoreCase("GET")) {
            return getGetResponse();
        }
        if (methodType.equalsIgnoreCase("PUT")) {
            return getPutResponse();
        }
        if (methodType.equalsIgnoreCase("POST")) {
            return getPostResponse();
        }
        if (methodType.equalsIgnoreCase("DELETE")) {
            return getDeleteResponse();
        }
        throw new UnknownMethodException(methodType);
    }

    /**
     * Returns a resource object as a result of a get request.
     *
     * @return the gets the response
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public WebResource getGetResponse() throws IOException {
        throw new UnknownMethodException("GET");
    }

    /**
     * Gets the post response.
     *
     * @return the post response
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    /*
     * Returns a resource object as a result of a post request.
     **/
    public WebResource getPostResponse() throws IOException {
        throw new UnknownMethodException("POST");
    }

    /**
     * Gets the put response.
     *
     * @return the put response
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    /*
     * Returns a resource object as a result of a put request.
     **/
    public WebResource getPutResponse() throws IOException {
        throw new UnknownMethodException("PUT");
    }

    /**
     * Gets the delete response.
     *
     * @return the delete response
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    /*
     * Returns a resource object as a result of a delete request.
     **/
    public WebResource getDeleteResponse() throws IOException {
        throw new UnknownMethodException("DELETE");
    }

    /**
     * Inits the.
     *
     * @param requestStream
     *            the request stream
     */
    void init(HttpRequest requestStream) {
        _request = requestStream;
    }

    /**
     * Returns the header with the specified name. If no such header exists, will return null.
     *
     * @param name
     *            the name
     *
     * @return the header
     */
    protected String getHeader(String name) {
        return _request.getHeader(name);
    }

    /**
     * Returns the values for the parameter with the specified name. If no values exist will return null.
     *
     * @param name
     *            the name
     *
     * @return the parameter
     */
    protected String[] getParameter(String name) {
        return _request.getParameter(name);
    }

    /**
     * Returns a reader for the body of the request.
     *
     * @return the reader
     */
    protected Reader getReader() {
        return _request.getReader();
    }

    /**
     * Gets the body.
     *
     * @return the body
     */
    protected byte[] getBody() {
        return _request.getBody();
    }

    /**
     * Gets the request.
     *
     * @return the request
     */
    protected HttpRequest getRequest() {
        return _request;
    }

    /** The request. */
    private HttpRequest _request;

}

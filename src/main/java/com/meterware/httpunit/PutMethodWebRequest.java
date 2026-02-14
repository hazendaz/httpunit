/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

import java.io.InputStream;

/**
 * A web request using the PUT protocol. The objectives of this class are to suport an HTTP PUT petition so we can test
 * this HTTP requests. <B>Documentation</B> See the HTTP 1.1 [<a href="http://www.w3.org/Protocols/HTTP/">spec</a>]
 **/
public class PutMethodWebRequest extends MessageBodyWebRequest {

    /**
     * Constructs a web request using a specific absolute url string and input stream.
     *
     * @param url
     *            the URL to which the request should be issued
     * @param source
     *            an input stream which will provide the body of this request
     * @param contentType
     *            the MIME content type of the body, including any character set
     **/
    public PutMethodWebRequest(String url, InputStream source, String contentType) {
        super(url, new InputStreamMessageBody(source, contentType));
    }

    /**
     * Returns 'PUT' to indicate the method.
     **/
    @Override
    public String getMethod() {
        return "PUT";
    }
}

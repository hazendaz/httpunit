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
 * A web request using the HEAD method. This request is used to obtain header information for a resource without
 * necessarily waiting for the data to be computed or transmitted. RFC 2616
 * http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html defines: 9.4 HEAD The HEAD method is identical to GET except
 * that the server MUST NOT return a message-body in the response. The metainformation contained in the HTTP headers in
 * response to a HEAD request SHOULD be identical to the information sent in response to a GET request. This method can
 * be used for obtaining metainformation about the entity implied by the request without transferring the entity-body
 * itself. This method is often used for testing hypertext links for validity, accessibility, and recent modification.
 * The response to a HEAD request MAY be cacheable in the sense that the information contained in the response MAY be
 * used to update a previously cached entity from that resource. If the new field values indicate that the cached entity
 * differs from the current entity (as would be indicated by a change in Content-Length, Content-SHA-256, ETag or
 * Last-Modified), then the cache MUST treat the cache entry as stale.
 **/
public class HeadMethodWebRequest extends HeaderOnlyWebRequest {

    /**
     * initialize me - set method to HEAD.
     */
    private void init() {
        super.setMethod("HEAD");
    }

    /**
     * Creates a new head request from a complete URL string.
     *
     * @param urlString
     *            the URL desired, including the protocol.
     */
    public HeadMethodWebRequest(String urlString) {
        super(urlString);
        init();
    }

    /**
     * Creates a new head request using a relative URL and base.
     *
     * @param urlBase
     *            the base URL.
     * @param urlString
     *            the relative URL
     */
    public HeadMethodWebRequest(URL urlBase, String urlString) {
        super(urlBase, urlString);
        init();
    }

}

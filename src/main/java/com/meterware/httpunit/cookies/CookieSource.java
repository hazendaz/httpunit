/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.cookies;

import java.net.URL;

/**
 * This interface represents a source from which to parse out cookies.
 **/
public interface CookieSource {

    /**
     * Returns the URL which invoked this response.
     *
     * @return the url
     */
    URL getURL();

    /**
     * Returns the values for the specified header field. If no such field is defined, will return an empty array.
     *
     * @param fieldName
     *            the field name
     *
     * @return the header fields
     */
    String[] getHeaderFields(String fieldName);

}

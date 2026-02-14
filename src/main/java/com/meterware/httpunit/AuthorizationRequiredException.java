/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

import java.util.Map;
import java.util.Properties;

/**
 * This exception is thrown when an unauthorized request is made for a page that requires authentication.
 **/
public class AuthorizationRequiredException extends RuntimeException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates the basic authentication required exception.
     *
     * @param realm
     *            the realm
     *
     * @return the authorization required exception
     */
    public static AuthorizationRequiredException createBasicAuthenticationRequiredException(String realm) {
        Properties props = new Properties();
        props.put("realm", realm);
        return new AuthorizationRequiredException("Basic", props);
    }

    /**
     * Creates the exception.
     *
     * @param scheme
     *            the scheme
     * @param properties
     *            the properties
     *
     * @return the authorization required exception
     */
    static AuthorizationRequiredException createException(String scheme, Map properties) {
        return new AuthorizationRequiredException(scheme, properties);
    }

    /**
     * Instantiates a new authorization required exception.
     *
     * @param scheme
     *            the scheme
     * @param properties
     *            the properties
     */
    private AuthorizationRequiredException(String scheme, Map properties) {
        _scheme = scheme;
        _properties = properties;
    }

    @Override
    public String getMessage() {
        return _scheme + " authentication required: " + _properties;
    }

    /**
     * Returns the name of the <a href="http://www.freesoft.org/CIE/RFC/Orig/rfc2617.txt">authentication scheme</a>.
     *
     * @return the scheme
     **/
    public String getAuthenticationScheme() {
        return _scheme;
    }

    /**
     * Returns the named authentication parameter. For Basic authentication, the only parameter is "realm".
     *
     * @param parameterName
     *            the name of the parameter to fetch
     *
     * @return the parameter, without quotes
     **/
    public String getAuthenticationParameter(String parameterName) {
        return unQuote((String) _properties.get(parameterName));
    }

    /**
     * Un quote.
     *
     * @param value
     *            the value
     *
     * @return the string
     */
    private String unQuote(String value) {
        if (value == null || value.length() <= 1 || !value.startsWith("\"") || !value.endsWith("\"")) {
            return value;
        }

        return value.substring(1, value.length() - 1);
    }

    // ------------------------------------- private members ------------------------------------------

    /** The scheme. */
    private String _scheme;

    /** The properties. */
    private Map _properties;
}

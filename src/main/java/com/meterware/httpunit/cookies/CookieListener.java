/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.cookies;

/**
 * An interface for classes which can listen for cookies being rejected and the reason.
 */
public interface CookieListener {

    /** Indicates that the cookie was accepted. **/
    int ACCEPTED = 0;

    /** Indicates that the domain attribute has only one dot. **/
    int DOMAIN_ONE_DOT = 2;

    /** Indicates that the domain attribute is not a suffix of the source domain issuing the cookie. **/
    int DOMAIN_NOT_SOURCE_SUFFIX = 3;

    /** Indicates that the source domain has an extra dot beyond those defined in the domain attribute. **/
    int DOMAIN_TOO_MANY_LEVELS = 4;

    /** Indicates that the source path does not begin with the path attribute. **/
    int PATH_NOT_PREFIX = 5;

    /**
     * Invoked when a cookie is rejected by HttpUnit.
     *
     * @param cookieName
     *            the cookie name
     * @param reason
     *            the reason
     * @param attribute
     *            the attribute
     */
    void cookieRejected(String cookieName, int reason, String attribute);

}

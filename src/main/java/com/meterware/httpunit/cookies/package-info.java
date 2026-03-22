/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
/**
 * Classes to support cookie handling. Supports the HTTP state mechanism.
 * <p>
 * The central class of this package is the {@link com.meterware.httpunit.cookies.CookieJar}, which acts as a repository
 * of {@link com.meterware.httpunit.cookies.Cookie} objects. There are two main ways to get cookies into the CookieJar.
 * The first is to construct the CookieJar, passing a {@link com.meterware.httpunit.cookies.CookieSource} to its
 * constructor. This will cause the CookieJar to parse the Set-Cookie headers from the source object. The second is to
 * copy them from another CookieJar through use of the {@link com.meterware.httpunit.cookies.CookieJar#updateCookies
 * updateCookies(CookieJar)} method.
 * </p>
 * <p>
 * The CookieJar can also produce a Cookie header to be sent as part of a request. The
 * {@link com.meterware.httpunit.cookies.CookieJar#getCookieHeaderField} method will select any cookies that it has
 * which can be sent to the specified URL and assemble them into an appropriate header.
 * </p>
 */
package com.meterware.httpunit.cookies;

/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.cookies;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Controls behavior for cookies.
 */
public class CookieProperties {

    /** If true, domain matching follows the spec. If false, permits any domain which is a prefix of the host. **/
    private static boolean _domainMatchingStrict = true;

    /** If true, path matching follows the spec. If false, permits any path. **/
    private static boolean _pathMatchingStrict = true;

    /** A collection of listeners for cookie events. **/
    private static ArrayList<CookieListener> _listeners;

    /**
     * Reset.
     */
    public static void reset() {
        _domainMatchingStrict = true;
        _pathMatchingStrict = true;
        _listeners = null;
    }

    /**
     * Returns true (the default) if cookies should be rejected if they specify a domain which is not a suffix of the
     * host domain or does not contain all of the dots in that host domain name (see
     * <a href="http://www.faqs.org/rfcs/rfc2965.html">RFC2965</a>).
     *
     * @return true, if is domain matching strict
     */
    public static boolean isDomainMatchingStrict() {
        return _domainMatchingStrict;
    }

    /**
     * Specifies whether strict domain name matching must be followed.
     *
     * @param domainMatchingStrict
     *            the new domain matching strict
     */
    public static void setDomainMatchingStrict(boolean domainMatchingStrict) {
        _domainMatchingStrict = domainMatchingStrict;
    }

    /**
     * Returns true (the default) if cookies should be rejected if they specify a path which is not a prefix of the
     * request path (see <a href="http://www.faqs.org/rfcs/rfc2965.html">RFC2965</a>).
     *
     * @return true, if is path matching strict
     */
    public static boolean isPathMatchingStrict() {
        return _pathMatchingStrict;
    }

    /**
     * Specifies whether strict path name matching must be followed.
     *
     * @param pathMatchingStrict
     *            the new path matching strict
     */
    public static void setPathMatchingStrict(boolean pathMatchingStrict) {
        _pathMatchingStrict = pathMatchingStrict;
    }

    /**
     * Adds a listener for cookie events.
     *
     * @param listener
     *            the listener
     */
    public static void addCookieListener(CookieListener listener) {
        if (_listeners == null) {
            _listeners = new ArrayList<>();
        }
        synchronized (_listeners) {
            _listeners.add(listener);
        }
    }

    /**
     * Report cookie rejected.
     *
     * @param reason
     *            the reason
     * @param attribute
     *            the attribute
     * @param source
     *            the source
     */
    public static void reportCookieRejected(int reason, String attribute, String source) {
        if (_listeners == null) {
            return;
        }

        List<CookieListener> listeners;
        synchronized (_listeners) {
            listeners = (List<CookieListener>) _listeners.clone();
        }

        for (Iterator<CookieListener> i = listeners.iterator(); i.hasNext();) {
            (i.next()).cookieRejected(source, reason, attribute);
        }
    }
}

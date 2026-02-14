/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

import java.util.ArrayList;
import java.util.Iterator;

import org.xml.sax.SAXException;

/**
 * The context for a request which could have subrequests.
 **/
class RequestContext {

    /** The new responses. */
    private ArrayList _newResponses = new ArrayList<>();

    /**
     * Adds the new response.
     *
     * @param response
     *            the response
     */
    void addNewResponse(WebResponse response) {
        _newResponses.add(response);
    }

    /**
     * Run scripts.
     *
     * @throws SAXException
     *             the SAX exception
     */
    void runScripts() throws SAXException {
        for (Iterator iterator = _newResponses.iterator(); iterator.hasNext();) {
            WebResponse response = (WebResponse) iterator.next();
            HttpUnitOptions.getScriptingEngine().load(response);
        }
    }
}

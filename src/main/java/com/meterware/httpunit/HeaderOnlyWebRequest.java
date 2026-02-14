/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

import com.meterware.httpunit.protocol.URLEncodedString;

import java.io.IOException;
import java.net.URL;

import org.w3c.dom.Element;

/**
 * A web request which has no information in its message body.
 **/
public class HeaderOnlyWebRequest extends WebRequest {

    /**
     * Returns the query string defined for this request.
     **/
    @Override
    public String getQueryString() {
        try {
            URLEncodedString encoder = new URLEncodedString();
            getParameterHolder().recordPredefinedParameters(encoder);
            getParameterHolder().recordParameters(encoder);
            return encoder.getString();
        } catch (IOException e) {
            throw new RuntimeException("Programming error: " + e); // should never happen
        }
    }

    /**
     * Sets the method.
     *
     * @param method
     *            the method to set
     */
    public void setMethod(String method) {
        this.method = method;
    }

    // -------------------------------- protected members ---------------------------

    /**
     * Instantiates a new header only web request.
     *
     * @param urlBase
     *            the url base
     * @param urlString
     *            the url string
     * @param frame
     *            the frame
     * @param target
     *            the target
     */
    protected HeaderOnlyWebRequest(URL urlBase, String urlString, FrameSelector frame, String target) {
        super(urlBase, urlString, frame, target);
    }

    /**
     * Instantiates a new header only web request.
     *
     * @param urlBase
     *            the url base
     * @param urlString
     *            the url string
     * @param target
     *            the target
     */
    protected HeaderOnlyWebRequest(URL urlBase, String urlString, String target) {
        super(urlBase, urlString, target);
    }

    /**
     * Instantiates a new header only web request.
     *
     * @param referer
     *            the referer
     * @param sourceElement
     *            the source element
     * @param urlBase
     *            the url base
     * @param urlString
     *            the url string
     * @param target
     *            the target
     */
    protected HeaderOnlyWebRequest(WebResponse referer, Element sourceElement, URL urlBase, String urlString,
            String target) {
        super(referer, sourceElement, urlBase, urlString, target);
    }

    /**
     * Instantiates a new header only web request.
     *
     * @param urlBase
     *            the url base
     * @param urlString
     *            the url string
     */
    protected HeaderOnlyWebRequest(URL urlBase, String urlString) {
        super(urlBase, urlString);
    }

    /**
     * Instantiates a new header only web request.
     *
     * @param urlString
     *            the url string
     */
    protected HeaderOnlyWebRequest(String urlString) {
        super(urlString);
    }

    // ------------------------------------ package members --------------------------

    /**
     * Instantiates a new header only web request.
     *
     * @param requestSource
     *            the request source
     */
    HeaderOnlyWebRequest(WebRequestSource requestSource) {
        super(requestSource, WebRequest.newParameterHolder(requestSource));
        setHeaderField(REFERER_HEADER_NAME, requestSource.getBaseURL().toExternalForm());
    }

    /**
     * Instantiates a new header only web request.
     *
     * @param sourceForm
     *            the source form
     * @param parameterHolder
     *            the parameter holder
     * @param button
     *            the button
     * @param x
     *            the x
     * @param y
     *            the y
     */
    HeaderOnlyWebRequest(WebForm sourceForm, ParameterHolder parameterHolder, SubmitButton button, int x, int y) {
        super(sourceForm, parameterHolder, button, x, y);
        setHeaderField(REFERER_HEADER_NAME, sourceForm.getBaseURL().toExternalForm());
    }

    /**
     * Instantiates a new header only web request.
     *
     * @param urlBase
     *            the url base
     * @param urlString
     *            the url string
     * @param frame
     *            the frame
     */
    HeaderOnlyWebRequest(URL urlBase, String urlString, FrameSelector frame) {
        super(urlBase, urlString, frame, frame.getName());
    }

}

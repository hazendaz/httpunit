/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

import com.meterware.httpunit.scripting.ScriptableDelegate;

import java.net.URL;

import org.w3c.dom.Node;

/**
 * A frame in a web page.
 **/
class WebFrame extends HTMLElementBase {

    /** The selector. */
    private FrameSelector _selector;

    /** The response. */
    private WebResponse _response;

    /** The element. */
    private Node _element;

    /** The base URL. */
    private URL _baseURL;

    @Override
    public ScriptableDelegate getParentDelegate() {
        return _response.getDocumentScriptable();
    }

    // ---------------------------------------- package methods -----------------------------------------

    /**
     * Instantiates a new web frame.
     *
     * @param response
     *            the response
     * @param baseURL
     *            the base URL
     * @param frameNode
     *            the frame node
     * @param parentFrame
     *            the parent frame
     */
    WebFrame(WebResponse response, URL baseURL, Node frameNode, FrameSelector parentFrame) {
        super(frameNode);
        _response = response;
        _element = frameNode;
        _baseURL = baseURL;
        _selector = getFrameSelector(parentFrame);
    }

    /**
     * Gets the frame name.
     *
     * @return the frame name
     */
    String getFrameName() {
        return _selector.getName();
    }

    /**
     * Gets the selector.
     *
     * @return the selector
     */
    FrameSelector getSelector() {
        return _selector;
    }

    /**
     * Gets the frame selector.
     *
     * @param parentFrame
     *            the parent frame
     *
     * @return the frame selector
     */
    private FrameSelector getFrameSelector(FrameSelector parentFrame) {
        return FrameHolder.newNestedFrame(parentFrame, super.getName());
    }

    /**
     * Gets the initial request.
     *
     * @return the initial request
     */
    WebRequest getInitialRequest() {
        return new GetMethodWebRequest(_baseURL,
                HttpUnitUtils.trimFragment(NodeUtils.getNodeAttribute(_element, "src")), _selector);
    }

    /**
     * Checks for initial request.
     *
     * @return true, if successful
     */
    boolean hasInitialRequest() {
        return NodeUtils.getNodeAttribute(_element, "src").length() > 0;
    }

}

/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.dom;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;

/**
 * The handler for HTML events.
 */
class HTMLEventHandler {

    /** The base element. */
    private HTMLElementImpl _baseElement;

    /** The handler name. */
    private String _handlerName;

    /** The handler. */
    private Function _handler;

    /**
     * create a handler for the given HTML Event.
     *
     * @param baseElement
     *            the base element
     * @param handlerName
     *            the handler name
     */
    public HTMLEventHandler(HTMLElementImpl baseElement, String handlerName) {
        _baseElement = baseElement;
        _handlerName = handlerName;
    }

    /**
     * set the handler Function for this event Handler.
     *
     * @param handler
     *            the new handler
     */
    void setHandler(Function handler) {
        _handler = handler;
    }

    /**
     * get the (cached) handler Function for this event Handler on first access compile the function.
     *
     * @return the handler
     */
    Function getHandler() {
        if (_handler == null) {
            String attribute = _baseElement.getAttributeWithNoDefault(_handlerName);
            if (attribute != null && Context.getCurrentContext() != null) {
                _handler = Context.getCurrentContext().compileFunction(_baseElement,
                        "function " + AbstractDomComponent.createAnonymousFunctionName() + "() { " + attribute + "}",
                        "httpunit", 0, null);
            }
        }
        return _handler;
    }
}

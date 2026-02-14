/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

import com.meterware.httpunit.scripting.DocumentElement;
import com.meterware.httpunit.scripting.ScriptableDelegate;

/**
 * The Class HTMLElementScriptable.
 */
class HTMLElementScriptable extends ScriptableDelegate implements DocumentElement {

    /** the element that I am scripting for. */
    private HTMLElement _element;

    /**
     * Gets the element.
     *
     * @return the _element
     */
    protected HTMLElement get_element() {
        return _element;
    }

    /**
     * get the property with the given name
     *
     * @param propertyName
     *            - the name of the property to get
     */
    @Override
    public Object get(String propertyName) {
        if (propertyName.equals("nodeName") || propertyName.equals("tagName")) {
            return _element.getTagName();
        }
        if (propertyName.equalsIgnoreCase("title")) {
            return _element.getTitle();
        }
        if (_element.isSupportedAttribute(propertyName)) {
            return _element.getAttribute(propertyName);
        }
        return super.get(propertyName);
    }

    /**
     * get the content of the given attribute.
     *
     * @param attributeName
     *            the attribute name
     *
     * @return the attribute as a string
     */
    public String getAttribute(String attributeName) {
        return _element.getAttribute(attributeName);
    }

    /**
     * set the attribute with the given attribute name to the given value.
     *
     * @param attributeName
     *            the attribute name
     * @param value
     *            the value
     */
    public void setAttribute(String attributeName, Object value) {
        _element.setAttribute(attributeName, value);
    }

    /**
     * remove the given attribute.
     *
     * @param attributeName
     *            the attribute name
     */
    public void removeAttribute(String attributeName) {
        _element.removeAttribute(attributeName);
    }

    @Override
    public boolean handleEvent(String eventName) {
        // check whether onclick is activated
        if (eventName.equalsIgnoreCase("onclick")) {
            handleEvent("onmousedown");
        }
        String eventScript = getAttribute(eventName);
        boolean result = doEventScript(eventScript);
        if (eventName.equalsIgnoreCase("onclick")) {
            handleEvent("onmouseup");
        }
        return result;
    }

    /**
     * construct me from a given element.
     *
     * @param element
     *            the element
     */
    public HTMLElementScriptable(HTMLElement element) {
        _element = element;
    }
}

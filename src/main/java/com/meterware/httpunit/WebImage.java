/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

import com.meterware.httpunit.scripting.NamedDelegate;
import com.meterware.httpunit.scripting.ScriptableDelegate;

import java.net.URL;

import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLImageElement;

/**
 * Represents an image in an HTML document.
 **/
public class WebImage extends FixedURLWebRequestSource {

    /** The element. */
    private HTMLImageElement _element;

    /** The parsed HTML. */
    private ParsedHTML _parsedHTML;

    /**
     * Instantiates a new web image.
     *
     * @param response
     *            the response
     * @param parsedHTML
     *            the parsed HTML
     * @param baseURL
     *            the base URL
     * @param element
     *            the element
     * @param sourceFrame
     *            the source frame
     * @param defaultTarget
     *            the default target
     * @param characterSet
     *            the character set
     */
    WebImage(WebResponse response, ParsedHTML parsedHTML, URL baseURL, HTMLImageElement element,
            FrameSelector sourceFrame, String defaultTarget, String characterSet) {
        super(response, element, baseURL, "src", sourceFrame, defaultTarget, characterSet);
        _element = element;
        _parsedHTML = parsedHTML;
    }

    @Override
    public String getName() {
        return _element.getName();
    }

    /**
     * Gets the source.
     *
     * @return the source
     */
    public String getSource() {
        return _element.getSrc();
    }

    /**
     * Gets the alt text.
     *
     * @return the alt text
     */
    public String getAltText() {
        return _element.getAlt();
    }

    /**
     * Gets the link.
     *
     * @return the link
     */
    public WebLink getLink() {
        return _parsedHTML.getFirstMatchingLink((link, parentNode) -> {
            for (Node parent = (Node) parentNode; parent != null; parent = parent.getParentNode()) {
                if (parent.equals(((WebLink) link).getElement())) {
                    return true;
                }
            }
            return false;
        }, _element.getParentNode());
    }

    /**
     * The Class Scriptable.
     */
    public class Scriptable extends HTMLElementScriptable implements NamedDelegate {

        /**
         * Instantiates a new scriptable.
         */
        public Scriptable() {
            super(WebImage.this);
        }

        @Override
        public String getName() {
            return WebImage.this.getID().length() != 0 ? WebImage.this.getID() : WebImage.this.getName();
        }

        @Override
        public Object get(String propertyName) {
            if (propertyName.equalsIgnoreCase("src")) {
                return getSource();
            }
            if (propertyName.equalsIgnoreCase("name")) {
                return getName();
            }
            return super.get(propertyName);
        }

        @Override
        public void set(String propertyName, Object value) {
            if (propertyName.equalsIgnoreCase("src")) {
                if (value != null) {
                    _element.setSrc(value.toString());
                }
            } else {
                super.set(propertyName, value);
            }
        }
    }

    // ---------------------------------- WebRequestSource methods ------------------------------------------

    @Override
    public ScriptableDelegate newScriptable() {
        return new Scriptable();
    }

}

/*
 * MIT License
 *
 * Copyright 2011-2025 Russell Gold
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
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

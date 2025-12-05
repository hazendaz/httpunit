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
package com.meterware.httpunit.dom;

import org.w3c.dom.html.HTMLMetaElement;

/**
 * The Class HTMLMetaElementImpl.
 */
public class HTMLMetaElementImpl extends HTMLElementImpl implements HTMLMetaElement {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    @Override
    ElementImpl create() {
        return new HTMLMetaElementImpl();
    }

    /**
     * Gets the content.
     *
     * @return the content
     */
    @Override
    public String getContent() {
        return getAttributeWithNoDefault("content");
    }

    /**
     * Gets the http equiv.
     *
     * @return the http equiv
     */
    @Override
    public String getHttpEquiv() {
        return getAttributeWithNoDefault("http-equiv");
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return getAttributeWithNoDefault("name");
    }

    /**
     * Gets the scheme.
     *
     * @return the scheme
     */
    @Override
    public String getScheme() {
        return getAttributeWithNoDefault("scheme");
    }

    /**
     * Sets the content.
     *
     * @param content
     *            the new content
     */
    @Override
    public void setContent(String content) {
        setAttribute("content", content);
    }

    /**
     * Sets the http equiv.
     *
     * @param httpEquiv
     *            the new http equiv
     */
    @Override
    public void setHttpEquiv(String httpEquiv) {
        setAttribute("http-equiv", httpEquiv);
    }

    /**
     * Sets the name.
     *
     * @param name
     *            the new name
     */
    @Override
    public void setName(String name) {
        setAttribute("name", name);
    }

    /**
     * Sets the scheme.
     *
     * @param scheme
     *            the new scheme
     */
    @Override
    public void setScheme(String scheme) {
        setAttribute("scheme", scheme);
    }
}

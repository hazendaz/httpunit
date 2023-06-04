/*
 * MIT License
 *
 * Copyright 2011-2023 Russell Gold
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

import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.html.HTMLParagraphElement;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 */
public class HTMLParagraphElementImpl extends HTMLElementImpl implements HTMLParagraphElement, HTMLContainerElement {

    private static final long serialVersionUID = 1L;

    @Override
    ElementImpl create() {
        return new HTMLParagraphElementImpl();
    }

    // ------------------------------------------ HTMLContainerElement methods
    // ----------------------------------------------

    @Override
    public HTMLCollection getLinks() {
        return getHtmlDocument().getContainerDelegate().getLinks(this);
    }

    @Override
    public HTMLCollection getImages() {
        return getHtmlDocument().getContainerDelegate().getImages(this);
    }

    @Override
    public HTMLCollection getApplets() {
        return getHtmlDocument().getContainerDelegate().getApplets(this);
    }

    @Override
    public HTMLCollection getForms() {
        return getHtmlDocument().getContainerDelegate().getForms(this);
    }

    @Override
    public HTMLCollection getAnchors() {
        return getHtmlDocument().getContainerDelegate().getAnchors(this);
    }

    // ----------------------------------------- HTMLParagraphElement methods
    // -----------------------------------------------

    @Override
    public String getAlign() {
        return getAttributeWithNoDefault("align");
    }

    @Override
    public void setAlign(String align) {
        setAttribute("align", align);
    }

}

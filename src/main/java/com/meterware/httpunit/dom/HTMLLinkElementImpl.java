/*
 * MIT License
 *
 * Copyright 2011-2024 Russell Gold
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

import org.w3c.dom.html.HTMLLinkElement;

public class HTMLLinkElementImpl extends HTMLElementImpl implements HTMLLinkElement {

    private static final long serialVersionUID = 1L;

    @Override
    ElementImpl create() {
        return new HTMLLinkElementImpl();
    }

    @Override
    public String getCharset() {
        return getAttributeWithNoDefault("charset");
    }

    @Override
    public boolean getDisabled() {
        return getBooleanAttribute("disabled");
    }

    @Override
    public String getHref() {
        return getAttributeWithNoDefault("href");
    }

    @Override
    public String getHreflang() {
        return getAttributeWithNoDefault("hreflang");
    }

    @Override
    public String getMedia() {
        return getAttributeWithDefault("media", "screen");
    }

    @Override
    public String getRel() {
        return getAttributeWithNoDefault("rel");
    }

    @Override
    public String getRev() {
        return getAttributeWithNoDefault("rev");
    }

    @Override
    public String getTarget() {
        return getAttributeWithNoDefault("target");
    }

    @Override
    public String getType() {
        return getAttributeWithNoDefault("type");
    }

    @Override
    public void setCharset(String charset) {
        setAttribute("charset", charset);
    }

    @Override
    public void setDisabled(boolean disabled) {
        setAttribute("disabled", disabled);
    }

    @Override
    public void setHref(String href) {
        setAttribute("href", href);
    }

    @Override
    public void setHreflang(String hreflang) {
        setAttribute("hreflang", hreflang);
    }

    @Override
    public void setMedia(String media) {
        setAttribute("media", media);
    }

    @Override
    public void setRel(String rel) {
        setAttribute("rel", rel);
    }

    @Override
    public void setRev(String rev) {
        setAttribute("rev", rev);
    }

    @Override
    public void setTarget(String target) {
        setAttribute("target", target);
    }

    @Override
    public void setType(String type) {
        setAttribute("type", type);
    }

}

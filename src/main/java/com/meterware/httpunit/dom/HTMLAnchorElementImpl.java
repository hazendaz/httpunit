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

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.html.HTMLAnchorElement;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class HTMLAnchorElementImpl extends HTMLElementImpl implements HTMLAnchorElement {

    private static final long serialVersionUID = 1L;

    @Override
    ElementImpl create() {
        return new HTMLAnchorElementImpl();
    }

    @Override
    public String getCharset() {
        return getAttributeWithNoDefault("charset");
    }

    @Override
    public String getHref() {
        String relativeLocation = getAttributeWithNoDefault("href");
        if (relativeLocation.indexOf(':') > 0 || relativeLocation.equals("#")) {
            return relativeLocation;
        }
        try {
            return new URL(((HTMLDocumentImpl) getOwnerDocument()).getBaseUrl(), relativeLocation).toExternalForm();
        } catch (MalformedURLException e) {
            return e.toString();
        }
    }

    @Override
    public String getHreflang() {
        return getAttributeWithNoDefault("hreflang");
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
    public void setHref(String href) {
        setAttribute("href", href);
    }

    @Override
    public void setHreflang(String hreflang) {
        setAttribute("hreflang", hreflang);
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

    /**
     * simulate blur
     */
    @Override
    public void blur() {
        handleEvent("onblur");
    }

    /**
     * simulate focus;
     */
    @Override
    public void focus() {
        handleEvent("onfocus");
    }

    @Override
    public String getAccessKey() {
        return getAttributeWithNoDefault("accesskey");
    }

    @Override
    public String getCoords() {
        return getAttributeWithNoDefault("coords");
    }

    @Override
    public String getName() {
        return getAttributeWithNoDefault("name");
    }

    @Override
    public String getShape() {
        return getAttributeWithNoDefault("shape");
    }

    @Override
    public int getTabIndex() {
        return getIntegerAttribute("tabindex");
    }

    @Override
    public void setAccessKey(String accessKey) {
        setAttribute("accesskey", accessKey);
    }

    @Override
    public void setCoords(String coords) {
        setAttribute("coords", coords);
    }

    @Override
    public void setName(String name) {
        setAttribute("name", name);
    }

    @Override
    public void setShape(String shape) {
        setAttribute("shape", shape);
    }

    @Override
    public void setTabIndex(int tabIndex) {
        setAttribute("tabindex", tabIndex);
    }

    @Override
    public void doClickAction() {
        if (null == getHref() || getHref().startsWith("#")) {
            return;
        }
        try {
            ((HTMLDocumentImpl) getOwnerDocument()).getWindow().submitRequest(this, "GET", getHref(), getTarget(),
                    new byte[0]);
        } catch (Exception e) {
            throw new RuntimeException("Error clicking link: " + e);
        }
    }
}

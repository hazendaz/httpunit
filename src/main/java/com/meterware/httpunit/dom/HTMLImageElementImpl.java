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

import org.w3c.dom.html.HTMLImageElement;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class HTMLImageElementImpl extends HTMLElementImpl implements HTMLImageElement {

    private static final long serialVersionUID = 1L;

    @Override
    ElementImpl create() {
        return new HTMLImageElementImpl();
    }

    @Override
    public String getAlign() {
        return getAttributeWithNoDefault("align");
    }

    @Override
    public String getAlt() {
        return getAttributeWithNoDefault("alt");
    }

    @Override
    public String getBorder() {
        return getAttributeWithNoDefault("border");
    }

    @Override
    public String getHeight() {
        return getAttributeWithNoDefault("height");
    }

    @Override
    public String getHspace() {
        return getAttributeWithNoDefault("hspace");
    }

    @Override
    public boolean getIsMap() {
        return getBooleanAttribute("ismap");
    }

    @Override
    public String getLongDesc() {
        return getAttributeWithNoDefault("longdesc");
    }

    @Override
    public String getLowSrc() {
        return null;
    }

    @Override
    public String getName() {
        return getAttributeWithNoDefault("name");
    }

    @Override
    public String getSrc() {
        return getAttributeWithNoDefault("src");
    }

    @Override
    public String getUseMap() {
        return getAttributeWithNoDefault("usemap");
    }

    @Override
    public String getVspace() {
        return getAttributeWithNoDefault("vspace");
    }

    @Override
    public String getWidth() {
        return getAttributeWithNoDefault("width");
    }

    @Override
    public void setAlign(String align) {
        setAttribute("align", align);
    }

    @Override
    public void setAlt(String alt) {
        setAttribute("alt", alt);
    }

    @Override
    public void setBorder(String border) {
        setAttribute("border", border);
    }

    @Override
    public void setHeight(String height) {
        setAttribute("height", height);
    }

    @Override
    public void setHspace(String hspace) {
        setAttribute("hspace", hspace);
    }

    @Override
    public void setIsMap(boolean isMap) {
        setAttribute("ismap", isMap);
    }

    @Override
    public void setLongDesc(String longDesc) {
        setAttribute("longdesc", longDesc);
    }

    @Override
    public void setLowSrc(String lowSrc) {
    }

    @Override
    public void setName(String name) {
        setAttribute("name", name);
    }

    @Override
    public void setSrc(String src) {
        setAttribute("src", src);
    }

    @Override
    public void setUseMap(String useMap) {
        setAttribute("usemap", useMap);
    }

    @Override
    public void setVspace(String vspace) {
        setAttribute("vspace", vspace);
    }

    @Override
    public void setWidth(String width) {
        setAttribute("width", width);
    }
}

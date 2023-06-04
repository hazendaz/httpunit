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

import org.w3c.dom.Document;
import org.w3c.dom.html.HTMLIFrameElement;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 */
public class HTMLIFrameElementImpl extends HTMLElementImpl implements HTMLIFrameElement {

    private static final long serialVersionUID = 1L;

    @Override
    ElementImpl create() {
        return new HTMLIFrameElementImpl();
    }

    @Override
    public String getAlign() {
        return getAttributeWithNoDefault("align");
    }

    @Override
    public void setAlign(String align) {
        setAttribute("align", align);
    }

    @Override
    public String getFrameBorder() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setFrameBorder(String frameBorder) {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getHeight() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setHeight(String height) {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getLongDesc() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setLongDesc(String longDesc) {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getMarginHeight() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setMarginHeight(String marginHeight) {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getMarginWidth() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setMarginWidth(String marginWidth) {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getName() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setName(String name) {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getScrolling() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setScrolling(String scrolling) {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getSrc() {
        return getAttributeWithNoDefault("src");
    }

    @Override
    public void setSrc(String src) {
        setAttribute("src", src);
    }

    @Override
    public String getWidth() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setWidth(String width) {
        // To change body of implemented methods use File | Settings | File Templates.
    }

}

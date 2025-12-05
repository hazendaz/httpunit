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

import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.html.HTMLTextAreaElement;

public class HTMLTextAreaElementImpl extends HTMLControl implements HTMLTextAreaElement {

    private static final long serialVersionUID = 1L;
    private String _value;

    @Override
    ElementImpl create() {
        return new HTMLTextAreaElementImpl();
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
    public int getCols() {
        return getIntegerAttribute("cols");
    }

    @Override
    public String getDefaultValue() {
        Node node = getFirstChild();

        if (node == null) {
            return "";
        }

        if (node.getNodeType() != Node.TEXT_NODE) {
            return null;
        }

        return node.getNodeValue();
    }

    @Override
    public int getRows() {
        return getIntegerAttribute("rows");
    }

    @Override
    public void select() {
    }

    @Override
    public void setAccessKey(String accessKey) {
        setAttribute("accesskey", accessKey);
    }

    @Override
    public void setCols(int cols) {
        setAttribute("cols", cols);
    }

    @Override
    public void setDefaultValue(String defaultValue) {
        Text textNode = getOwnerDocument().createTextNode(defaultValue);
        Node child = getFirstChild();
        if (child == null) {
            appendChild(textNode);
        } else {
            replaceChild(textNode, child);
        }
    }

    @Override
    public void setRows(int rows) {
        setAttribute("rows", rows);
    }

    @Override
    public String getValue() {
        return _value != null ? _value : getDefaultValue();
    }

    @Override
    public void setValue(String value) {
        _value = value;
    }

    @Override
    public void reset() {
        _value = null;
    }
}

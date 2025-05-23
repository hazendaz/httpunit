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

import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.html.HTMLTitleElement;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class HTMLTitleElementImpl extends HTMLElementImpl implements HTMLTitleElement {

    private static final long serialVersionUID = 1L;

    @Override
    ElementImpl create() {
        return new HTMLTitleElementImpl();
    }

    @Override
    public String getText() {
        Text contentNode = getContentNode();
        return contentNode == null ? "" : contentNode.getData();
    }

    private Text getContentNode() {
        NodeList childNodes = getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == TEXT_NODE) {
                return (Text) childNodes.item(i);
            }
        }
        return null;
    }

    @Override
    public void setText(String text) {
        Text newChild = getOwnerDocument().createTextNode(text);
        Text oldChild = getContentNode();
        if (oldChild == null) {
            appendChild(newChild);
        } else {
            replaceChild(newChild, oldChild);
        }
    }
}

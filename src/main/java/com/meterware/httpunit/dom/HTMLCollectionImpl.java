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

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.html.HTMLElement;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class HTMLCollectionImpl extends ScriptableObject implements HTMLCollection {

    private static final long serialVersionUID = 1L;
    private NodeList _list;

    public static HTMLCollectionImpl createHTMLCollectionImpl(NodeList list) {
        HTMLCollectionImpl htmlCollection = new HTMLCollectionImpl();
        htmlCollection.initialize(list);
        return htmlCollection;
    }

    private void initialize(NodeList list) {
        _list = list;
    }

    // ------------------------------------------ HTMLCollection methods
    // --------------------------------------------------

    @Override
    public int getLength() {
        return _list.getLength();
    }

    @Override
    public Node item(int index) {
        return _list.item(index);
    }

    @Override
    public Node namedItem(String name) {
        if (name == null) {
            return null;
        }

        Node nodeByName = null;
        for (int i = 0; null == nodeByName && i < getLength(); i++) {
            Node node = item(i);
            if (!(node instanceof HTMLElementImpl)) {
                continue;
            }
            if (name.equalsIgnoreCase(((HTMLElement) node).getId())) {
                return node;
            }
            if (name.equalsIgnoreCase(((HTMLElementImpl) node).getAttributeWithNoDefault("name"))) {
                nodeByName = node;
            }
        }
        return nodeByName;
    }

    // ------------------------------------------ ScriptableObject methods
    // --------------------------------------------------

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public Object get(String propertyName, Scriptable scriptable) {
        Object result = super.get(propertyName, scriptable);
        if (result != NOT_FOUND) {
            return result;
        }

        Object namedProperty = ScriptingSupport.getNamedProperty(this, propertyName, scriptable);
        if (namedProperty != NOT_FOUND) {
            return namedProperty;
        }

        Node namedItem = namedItem(propertyName);
        return namedItem == null ? NOT_FOUND : namedItem;
    }

    @Override
    public Object get(int index, Scriptable start) {
        if (index < 0 || index >= _list.getLength()) {
            return NOT_FOUND;
        }
        return item(index);
    }
}

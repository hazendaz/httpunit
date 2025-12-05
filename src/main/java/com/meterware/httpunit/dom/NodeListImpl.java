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

import java.util.List;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NodeListImpl extends ScriptableObject implements NodeList {

    private static final long serialVersionUID = 1L;
    private List _list;

    public NodeListImpl(List list) {
        _list = list;
    }

    @Override
    public Node item(int index) {
        return (Node) _list.get(index);
    }

    @Override
    public int getLength() {
        return _list.size();
    }

    @Override
    public String getClassName() {
        return NodeListImpl.class.getName();
    }

    @Override
    public Object get(String name, Scriptable start) {
        if ("length".equals(name)) {
            return Integer.valueOf(getLength());
        }
        return NOT_FOUND;
    }

    @Override
    public Object get(int index, Scriptable start) {
        if (index < 0 || index >= getLength()) {
            return NOT_FOUND;
        }
        return item(index);
    }
}

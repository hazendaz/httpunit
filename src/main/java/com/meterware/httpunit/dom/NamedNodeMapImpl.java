/*
 * MIT License
 *
 * Copyright 2011-2026 Russell Gold
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

import java.util.Hashtable;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * The Class NamedNodeMapImpl.
 */
public class NamedNodeMapImpl implements NamedNodeMap {

    /** The items. */
    private Hashtable _items;

    /** The item array. */
    private Node[] _itemArray;

    /**
     * Instantiates a new named node map impl.
     *
     * @param items
     *            the items
     */
    NamedNodeMapImpl(Hashtable items) {
        _items = (Hashtable) items.clone();
        _itemArray = (Node[]) _items.values().toArray(new Node[_items.size()]);
    }

    @Override
    public Node getNamedItem(String name) {
        return (Node) _items.get(name);
    }

    @Override
    public Node setNamedItem(Node arg) throws DOMException {
        return null;
    }

    @Override
    public Node removeNamedItem(String name) throws DOMException {
        return null;
    }

    @Override
    public Node item(int index) {
        return _itemArray[index];
    }

    @Override
    public int getLength() {
        return _items.size();
    }

    @Override
    public Node getNamedItemNS(String namespaceURI, String localName) {
        return null;
    }

    @Override
    public Node setNamedItemNS(Node arg) throws DOMException {
        return null;
    }

    @Override
    public Node removeNamedItemNS(String namespaceURI, String localName) throws DOMException {
        return null;
    }
}

/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
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

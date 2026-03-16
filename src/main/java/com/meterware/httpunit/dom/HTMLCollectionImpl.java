/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.dom;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.html.HTMLElement;

/**
 * The Class HTMLCollectionImpl.
 */
public class HTMLCollectionImpl extends ScriptableObject implements HTMLCollection {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The list. */
    private NodeList _list;

    /**
     * Creates the HTML collection impl.
     *
     * @param list
     *            the list
     *
     * @return the HTML collection impl
     */
    public static HTMLCollectionImpl createHTMLCollectionImpl(NodeList list) {
        HTMLCollectionImpl htmlCollection = new HTMLCollectionImpl();
        htmlCollection.initialize(list);
        return htmlCollection;
    }

    /**
     * Initialize.
     *
     * @param list
     *            the list
     */
    private void initialize(NodeList list) {
        _list = list;
    }

    // ------------------------------------------ HTMLCollection methods
    // --------------------------------------------------

    /**
     * Gets the length.
     *
     * @return the length
     */
    @Override
    public int getLength() {
        return _list.getLength();
    }

    /**
     * Item.
     *
     * @param index
     *            the index
     *
     * @return the node
     */
    @Override
    public Node item(int index) {
        return _list.item(index);
    }

    /**
     * Named item.
     *
     * @param name
     *            the name
     *
     * @return the node
     */
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

/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Node;

/**
 * The Class ElementRegistry.
 */
class ElementRegistry {

    /** The map. */
    private Map _map = new HashMap<>();

    /**
     * Registers an HttpUnit element for a node.
     *
     * @param node
     *            the node
     * @param htmlElement
     *            the html element
     *
     * @return the registered element
     */
    Object registerElement(Node node, HTMLElement htmlElement) {
        _map.put(node, htmlElement);
        return htmlElement;
    }

    /**
     * Returns the HttpUnit element associated with the specified DOM element, if any.
     *
     * @param node
     *            the node
     *
     * @return the registered element
     */
    Object getRegisteredElement(Node node) {
        return _map.get(node);
    }

    /**
     * Iterator.
     *
     * @return the iterator
     */
    Iterator iterator() {
        return _map.values().iterator();
    }

    /**
     * Checks for node.
     *
     * @param node
     *            the node
     *
     * @return true, if successful
     */
    boolean hasNode(Node node) {
        return _map.containsKey(node);
    }
}

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

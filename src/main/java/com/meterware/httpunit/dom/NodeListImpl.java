/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.dom;

import java.util.List;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The Class NodeListImpl.
 */
public class NodeListImpl extends ScriptableObject implements NodeList {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The list. */
    private List _list;

    /**
     * Instantiates a new node list impl.
     *
     * @param list
     *            the list
     */
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

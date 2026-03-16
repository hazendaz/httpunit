/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * The Class TextImpl.
 */
public class TextImpl extends CharacterDataImpl implements Text {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates the text.
     *
     * @param ownerDocument
     *            the owner document
     * @param data
     *            the data
     *
     * @return the text impl
     */
    static TextImpl createText(DocumentImpl ownerDocument, String data) {
        TextImpl text = new TextImpl();
        text.initialize(ownerDocument, data);
        return text;
    }

    @Override
    public String getNodeName() {
        return "#text";
    }

    @Override
    public String getNodeValue() throws DOMException {
        return getData();
    }

    @Override
    public void setNodeValue(String nodeValue) throws DOMException {
        setData(nodeValue);
    }

    @Override
    public short getNodeType() {
        return TEXT_NODE;
    }

    @Override
    protected NodeImpl getChildIfPermitted(Node proposedChild) {
        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Text nodes may not have children");
    }

    @Override
    public Text splitText(int offset) throws DOMException {
        return null;
    }

    /**
     * Import node.
     *
     * @param document
     *            the document
     * @param text
     *            the text
     *
     * @return the node
     */
    public static Node importNode(DocumentImpl document, Text text) {
        return document.createTextNode(text.getData());
    }

    @Override
    void appendContents(StringBuilder sb) {
        sb.append(getData());
    }

    // ------------------------------------- DOM level 3 methods
    // ------------------------------------------------------------

    @Override
    public boolean isElementContentWhitespace() {
        return false; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getWholeText() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Text replaceWholeText(String content) throws DOMException {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }
}

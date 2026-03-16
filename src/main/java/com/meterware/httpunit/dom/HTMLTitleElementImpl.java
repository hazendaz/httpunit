/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.dom;

import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.html.HTMLTitleElement;

/**
 * The Class HTMLTitleElementImpl.
 */
public class HTMLTitleElementImpl extends HTMLElementImpl implements HTMLTitleElement {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    @Override
    ElementImpl create() {
        return new HTMLTitleElementImpl();
    }

    /**
     * Gets the text.
     *
     * @return the text
     */
    @Override
    public String getText() {
        Text contentNode = getContentNode();
        return contentNode == null ? "" : contentNode.getData();
    }

    /**
     * Gets the content node.
     *
     * @return the content node
     */
    private Text getContentNode() {
        NodeList childNodes = getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == TEXT_NODE) {
                return (Text) childNodes.item(i);
            }
        }
        return null;
    }

    /**
     * Sets the text.
     *
     * @param text
     *            the new text
     */
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

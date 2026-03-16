/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.dom;

import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

/**
 * The Class CommentImpl.
 */
public class CommentImpl extends CharacterDataImpl implements Comment {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates the comment.
     *
     * @param ownerDocument
     *            the owner document
     * @param data
     *            the data
     *
     * @return the comment impl
     */
    static CommentImpl createComment(DocumentImpl ownerDocument, String data) {
        CommentImpl comment = new CommentImpl();
        comment.initialize(ownerDocument, data);
        return comment;
    }

    /**
     * Import node.
     *
     * @param document
     *            the document
     * @param comment
     *            the comment
     *
     * @return the node
     */
    public static Node importNode(DocumentImpl document, Comment comment) {
        return document.createComment(comment.getData());
    }

    @Override
    public String getNodeName() {
        return "#comment";
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
        return COMMENT_NODE;
    }

    @Override
    protected NodeImpl getChildIfPermitted(Node proposedChild) {
        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Comment nodes may not have children");
    }

    @Override
    void appendContents(StringBuilder sb) {
        sb.append(getData());
    }

}

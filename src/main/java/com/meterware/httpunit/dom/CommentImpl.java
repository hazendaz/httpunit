/*
 * MIT License
 *
 * Copyright 2011-2024 Russell Gold
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

import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

public class CommentImpl extends CharacterDataImpl implements Comment {

    private static final long serialVersionUID = 1L;

    static CommentImpl createComment(DocumentImpl ownerDocument, String data) {
        CommentImpl comment = new CommentImpl();
        comment.initialize(ownerDocument, data);
        return comment;
    }

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

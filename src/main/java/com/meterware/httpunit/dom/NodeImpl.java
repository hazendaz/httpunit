/*
 * MIT License
 *
 * Copyright 2011-2025 Russell Gold
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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;
import org.w3c.dom.html.HTMLIFrameElement;

public abstract class NodeImpl extends AbstractDomComponent implements Node {

    private static final long serialVersionUID = 1L;
    private DocumentImpl _ownerDocument;
    private NodeImpl _parentNode;
    private NodeImpl _firstChild;
    private NodeImpl _nextSibling;
    private NodeImpl _previousSibling;
    private Hashtable _userData = new Hashtable<>();

    static IteratorMask SKIP_IFRAMES = subtreeRoot -> subtreeRoot instanceof HTMLIFrameElement;

    protected void initialize(DocumentImpl ownerDocument) {
        if (_ownerDocument != null) {
            throw new IllegalStateException("NodeImpl already initialized");
        }
        if (ownerDocument == null) {
            throw new IllegalArgumentException("No owner document specified");
        }
        _ownerDocument = ownerDocument;
    }

    // ------------------------------------------ ScriptableObject methods
    // --------------------------------------------------

    // ------------------------------------------ ScriptingEngine methods
    // --------------------------------------------------

    // ----------------------------------------------- Node methods
    // ---------------------------------------------------------

    @Override
    public Node getParentNode() {
        return _parentNode;
    }

    @Override
    public NodeList getChildNodes() {
        ArrayList list = new ArrayList<>();
        for (NodeImpl child = _firstChild; child != null; child = child._nextSibling) {
            list.add(child);
        }
        return new NodeListImpl(list);
    }

    @Override
    public Node getFirstChild() {
        return _firstChild;
    }

    @Override
    public Node getLastChild() {
        if (_firstChild == null) {
            return null;
        }

        Node child = _firstChild;
        while (child.getNextSibling() != null) {
            child = child.getNextSibling();
        }
        return child;
    }

    @Override
    public Node getPreviousSibling() {
        return _previousSibling;
    }

    @Override
    public Node getNextSibling() {
        return _nextSibling;
    }

    @Override
    public NamedNodeMap getAttributes() {
        return null;
    }

    @Override
    public Document getOwnerDocument() {
        return _ownerDocument;
    }

    @Override
    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        NodeImpl refChildNode = (NodeImpl) refChild;
        if (refChildNode.getParentNode() != this) {
            throw new DOMException(DOMException.NOT_FOUND_ERR, "Must specify an existing child as the reference");
        }
        NodeImpl newChildNode = getChildIfPermitted(newChild);
        removeFromTree(newChildNode);
        newChildNode._parentNode = this;
        if (refChildNode._previousSibling == null) {
            _firstChild = newChildNode;
        } else {
            refChildNode._previousSibling.setNextSibling(newChildNode);
        }
        newChildNode.setNextSibling(refChildNode);
        return newChildNode;
    }

    private void removeFromTree(NodeImpl childNode) {
        if (childNode._parentNode != null) {
            if (childNode._previousSibling != null) {
                childNode._previousSibling.setNextSibling(childNode._nextSibling);
            } else {
                childNode._parentNode._firstChild = childNode._nextSibling;
                childNode._nextSibling._previousSibling = null;
            }
            childNode._parentNode = null;
        }
    }

    @Override
    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        insertBefore(newChild, oldChild);
        return removeChild(oldChild);
    }

    @Override
    public Node removeChild(Node oldChild) throws DOMException {
        if (oldChild.getParentNode() != this) {
            throw new DOMException(DOMException.NOT_FOUND_ERR, "May only remove a node from its own parent");
        }
        removeFromTree((NodeImpl) oldChild);
        return oldChild;
    }

    @Override
    public Node appendChild(Node newChild) throws DOMException {
        if (newChild == null) {
            throw new IllegalArgumentException("child to append may not be null");
        }

        NodeImpl childNode = getChildIfPermitted(newChild);
        removeFromTree(childNode);
        childNode._parentNode = this;
        if (_firstChild == null) {
            _firstChild = childNode;
        } else {
            ((NodeImpl) getLastChild()).setNextSibling(childNode);
        }
        return newChild;
    }

    protected NodeImpl getChildIfPermitted(Node proposedChild) {
        if (!(proposedChild instanceof NodeImpl)) {
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,
                    "Specified node is from a different DOM implementation");
        }
        NodeImpl childNode = (NodeImpl) proposedChild;
        if (getOwnerDocument() != childNode._ownerDocument) {
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, "Specified node is from a different document");
        }
        for (Node parent = this; parent != null; parent = parent.getParentNode()) {
            if (proposedChild == parent) {
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "May not add node as its own descendant");
            }
        }

        return childNode;
    }

    private void setNextSibling(NodeImpl sibling) {
        _nextSibling = sibling;
        if (sibling != null) {
            sibling._previousSibling = this;
        }
    }

    @Override
    public boolean hasChildNodes() {
        return _firstChild != null;
    }

    @Override
    public Node cloneNode(boolean deep) {
        return getOwnerDocument().importNode(this, deep);
    }

    @Override
    public void normalize() {
    }

    @Override
    public boolean isSupported(String feature, String version) {
        return false;
    }

    @Override
    public String getNamespaceURI() {
        return null;
    }

    @Override
    public String getPrefix() {
        return null;
    }

    @Override
    public void setPrefix(String prefix) throws DOMException {
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public boolean hasAttributes() {
        return false;
    }

    // ------------------------------------ DOM level 3 methods
    // -------------------------------------------------------------

    @Override
    public Object setUserData(String key, Object data, UserDataHandler handler) {
        return _userData.put(key, data);
    }

    @Override
    public Object getUserData(String key) {
        return _userData.get(key);
    }

    @Override
    public Object getFeature(String feature, String version) {
        return null;
    }

    @Override
    public boolean isEqualNode(Node arg) {
        return false; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String lookupNamespaceURI(String prefix) {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getBaseURI() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public short compareDocumentPosition(Node other) throws DOMException {
        return 0; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getTextContent() throws DOMException {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setTextContent(String textContent) throws DOMException {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isSameNode(Node other) {
        return this == other;
    }

    @Override
    public String lookupPrefix(String namespaceURI) {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isDefaultNamespace(String namespaceURI) {
        return false; // To change body of implemented methods use File | Settings | File Templates.
    }

    // ----------------------------------------- implementation internals
    // ---------------------------------------------------

    public NodeList getElementsByTagName(String name) {
        ArrayList matchingElements = new ArrayList<>();
        appendElementsWithTag(name, matchingElements);
        return new NodeListImpl(matchingElements);
    }

    private void appendElementsWithTag(String name, ArrayList matchingElements) {
        for (Node child = getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() != ELEMENT_NODE) {
                continue;
            }
            if (name.equals("*") || ((Element) child).getTagName().equalsIgnoreCase(name)) {
                matchingElements.add(child);
            }
            ((NodeImpl) child).appendElementsWithTag(name, matchingElements);
        }
    }

    protected NodeList getElementsByTagNames(String[] names) {
        ArrayList matchingElements = new ArrayList<>();
        appendElementsWithTags(names, matchingElements);
        return new NodeListImpl(matchingElements);
    }

    void appendElementsWithTags(String[] names, ArrayList matchingElements) {
        for (Node child = getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() != ELEMENT_NODE) {
                continue;
            }
            String tagName = ((Element) child).getTagName();
            for (String name : names) {
                if (tagName.equalsIgnoreCase(name)) {
                    matchingElements.add(child);
                }
            }
            ((NodeImpl) child).appendElementsWithTags(names, matchingElements);
        }
    }

    String asText() {
        StringBuilder sb = new StringBuilder();
        appendContents(sb);
        return sb.toString();
    }

    void appendContents(StringBuilder sb) {
        NodeList nl = getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            ((NodeImpl) nl.item(i)).appendContents(sb);
        }
    }

    public Iterator preOrderIterator() {
        return new PreOrderIterator(this);
    }

    public Iterator preOrderIterator(IteratorMask mask) {
        return new PreOrderIterator(this, mask);
    }

    public Iterator preOrderIteratorAfterNode() {
        return new PreOrderIterator(PreOrderIterator.nextNode(this));
    }

    /**
     * @return
     */
    public Iterator preOrderIteratorWithinNode() {
        PreOrderIterator result = new PreOrderIterator(PreOrderIterator.nextNode(this));
        result.setDoNotLeaveNode(this);
        return result;
    }

    public Iterator preOrderIteratorWithinNode(IteratorMask mask) {
        PreOrderIterator result = new PreOrderIterator(PreOrderIterator.nextNode(this), mask);
        result.setDoNotLeaveNode(this);
        return result;
    }

    public Iterator preOrderIteratorAfterNode(IteratorMask mask) {
        return new PreOrderIterator(PreOrderIterator.nextNode(this), mask);
    }

    @Override
    protected String getJavaPropertyName(String propertyName) {
        if (propertyName.equals("document")) {
            return "ownerDocument";
        }
        return propertyName;
    }

    /**
     * allow masking of the iteration
     */
    interface IteratorMask {
        // skip a given subtree
        boolean skipSubtree(Node subtreeRoot);
    }

    /**
     * iterator for Nodetrees that can be influenced with an Iterator mask to skip specific parts
     */
    static class PreOrderIterator implements Iterator {
        private NodeImpl _nextNode;
        private IteratorMask _mask;
        private NodeImpl _doNotLeaveNode = null;

        /**
         * get the limit node
         *
         * @return
         */
        public NodeImpl getDoNotLeaveNode() {
            return _doNotLeaveNode;
        }

        /**
         * limit the PreOrderIterator not to leave the given node
         *
         * @param doNotLeaveNode
         */
        public void setDoNotLeaveNode(NodeImpl doNotLeaveNode) {
            _doNotLeaveNode = doNotLeaveNode;
        }

        /**
         * check whether the node is a child of the doNotLeaveNode (if one is set)
         *
         * @param node
         *
         * @return
         */
        private boolean isChild(Node node) {
            if (node == null) {
                return false;
            }
            if (_doNotLeaveNode == null) {
                return true;
            }
            Node parent = node.getParentNode();
            if (parent == null) {
                return false;
            }
            if (parent.isSameNode(_doNotLeaveNode)) {
                return true;
            }
            return isChild(parent);
        }

        /**
         * create a PreOrderIterator starting at a given currentNode
         *
         * @param currentNode
         */
        PreOrderIterator(NodeImpl currentNode) {
            _nextNode = currentNode;
        }

        /**
         * create a PreOrderIterator starting at a given currentNode and setting the iterator mask to the given mask
         *
         * @param currentNode
         * @param mask
         */
        PreOrderIterator(NodeImpl currentNode, IteratorMask mask) {
            this(currentNode);
            _mask = mask;
        }

        /**
         * is there still a next node?
         */
        @Override
        public boolean hasNext() {
            return null != _nextNode;
        }

        /**
         * move one step in the tree
         */
        @Override
        public Object next() {
            NodeImpl currentNode = _nextNode;
            _nextNode = nextNode(_nextNode);
            while (_mask != null && _nextNode != null && _mask.skipSubtree(_nextNode)) {
                _nextNode = nextSubtree(_nextNode);
            }
            // check that we fit the doNotLeaveNode condition in case there is one
            if (!isChild(_nextNode)) {
                _nextNode = null;
            }
            return currentNode;
        }

        @Override
        public void remove() {
            throw new java.lang.UnsupportedOperationException();
        }

        static NodeImpl nextNode(NodeImpl node) {
            if (node._firstChild != null) {
                return node._firstChild;
            }
            return nextSubtree(node);
        }

        private static NodeImpl nextSubtree(NodeImpl node) {
            if (node._nextSibling != null) {
                return node._nextSibling;
            }
            while (node._parentNode != null) {
                node = node._parentNode;
                if (node._nextSibling != null) {
                    return node._nextSibling;
                }
            }
            return null;
        }
    }

}

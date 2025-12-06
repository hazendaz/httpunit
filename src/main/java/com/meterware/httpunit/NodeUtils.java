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
package com.meterware.httpunit;

import com.meterware.httpunit.parsing.HTMLParserFactory;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.Stack;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Some common utilities for manipulating DOM nodes.
 **/
public class NodeUtils {

    /**
     * get the attribute with the given name from the given node as an int value.
     *
     * @param node
     *            - the node to look in
     * @param attributeName
     *            - the attribute's name to look for
     * @param defaultValue
     *            the default value
     *
     * @return - the value - defaultValue as default
     */
    public static int getAttributeValue(Node node, String attributeName, int defaultValue) {
        NamedNodeMap nnm = node.getAttributes();
        Node attribute = nnm.getNamedItem(attributeName);
        if (attribute == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(attribute.getNodeValue());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * get the attribute with the given name from the given node.
     *
     * @param node
     *            - the node to look in
     * @param attributeName
     *            - the attribute's name to look for
     *
     * @return - the value - "" as default
     */
    public static String getNodeAttribute(Node node, String attributeName) {
        return getNodeAttribute(node, attributeName, "");
    }

    /**
     * get the attribute with the given name from the given node.
     *
     * @param node
     *            - the node to look in
     * @param attributeName
     *            - the attribute's name to look for
     * @param defaultValue
     *            the default value
     *
     * @return - the value - defaultValue as default
     */
    public static String getNodeAttribute(Node node, String attributeName, String defaultValue) {
        NamedNodeMap attributes = node.getAttributes();
        if (attributes == null) {
            return defaultValue;
        }

        Node attribute = attributes.getNamedItem(attributeName);
        return attribute == null ? defaultValue : attribute.getNodeValue();
    }

    /**
     * set the attribute with the given attribute to the given value in the given node.
     *
     * @param node
     *            the node
     * @param attributeName
     *            - the attribute's name to look for
     * @param value
     *            - the value to set
     */
    static void setNodeAttribute(Node node, String attributeName, String value) {
        ((Element) node).setAttributeNS(null, attributeName, value);
    }

    /**
     * remove the given attribute from the given node based on the attribute's name.
     *
     * @param node
     *            the node
     * @param attributeName
     *            the attribute name
     */
    static void removeNodeAttribute(Node node, String attributeName) {
        ((Element) node).removeAttribute(attributeName);
    }

    /**
     * check whether the given Attribute in the Node is Present.
     *
     * @param node
     *            - the node to check
     * @param attributeName
     *            - the attribute name to check
     *
     * @return true if the attribute is present
     */
    public static boolean isNodeAttributePresent(Node node, final String attributeName) {
        return node.getAttributes().getNamedItem(attributeName) != null;
    }

    /**
     * common Node action methods.
     */
    interface NodeAction {

        /**
         * Does appropriate processing on specified element. Will return false if the subtree below the element should
         * be skipped.
         *
         * @param traversal
         *            the traversal
         * @param element
         *            the element
         *
         * @return true, if successful
         */
        boolean processElement(PreOrderTraversal traversal, Element element);

        /**
         * Processes a text node.
         *
         * @param traversal
         *            the traversal
         * @param textNode
         *            the text node
         */
        void processTextNode(PreOrderTraversal traversal, Node textNode);
    }

    /**
     * Converts the DOM trees rooted at the specified nodes to text, ignoring any HTML tags.
     *
     * @param rootNodes
     *            the root nodes
     *
     * @return the string
     */
    public static String asText(NodeList rootNodes) {
        final StringBuilder sb = new StringBuilder(HttpUnitUtils.DEFAULT_TEXT_BUFFER_SIZE);
        NodeAction action = new NodeAction() {
            @Override
            public boolean processElement(PreOrderTraversal traversal, Element node) {
                String nodeName = node.getNodeName();
                if (nodeName.equalsIgnoreCase("p") || nodeName.equalsIgnoreCase("br")
                        || nodeName.equalsIgnoreCase("tr")) {
                    sb.append("\n");
                } else if (nodeName.equalsIgnoreCase("td") || nodeName.equalsIgnoreCase("th")) {
                    sb.append(" | ");
                } else if (nodeName.equalsIgnoreCase("img") && HttpUnitOptions.getImagesTreatedAsAltText()) {
                    sb.append(getNodeAttribute(node, "alt"));
                }
                return true;
            }

            @Override
            public void processTextNode(PreOrderTraversal traversal, Node textNode) {
                sb.append(HTMLParserFactory.getHTMLParser().getCleanedText(textNode.getNodeValue()));
            }
        };
        new PreOrderTraversal(rootNodes).perform(action);
        return sb.toString();
    }

    /**
     * The Class PreOrderTraversal.
     */
    static class PreOrderTraversal {

        /** The pending nodes. */
        private Stack _pendingNodes = new Stack();

        /** The traversal context. */
        private Stack _traversalContext = new Stack();

        /** The Constant POP_CONTEXT. */
        private static final Object POP_CONTEXT = new Object();

        /**
         * Instantiates a new pre order traversal.
         *
         * @param rootNodes
         *            the root nodes
         */
        public PreOrderTraversal(NodeList rootNodes) {
            pushNodeList(rootNodes);
        }

        /**
         * Instantiates a new pre order traversal.
         *
         * @param rootNode
         *            the root node
         */
        public PreOrderTraversal(Node rootNode) {
            pushNodeList(rootNode.getLastChild());
        }

        /**
         * Push base context.
         *
         * @param context
         *            the context
         */
        public void pushBaseContext(Object context) {
            _traversalContext.push(context);
        }

        /**
         * Push context.
         *
         * @param context
         *            the context
         */
        public void pushContext(Object context) {
            _traversalContext.push(context);
            _pendingNodes.push(POP_CONTEXT);
        }

        /**
         * Gets the contexts.
         *
         * @return the contexts
         */
        public Iterator getContexts() {
            Stack stack = _traversalContext;
            return getTopDownIterator(stack);
        }

        /**
         * Gets the root context.
         *
         * @return the root context
         */
        public Object getRootContext() {
            return _traversalContext.firstElement();
        }

        /**
         * Gets the top down iterator.
         *
         * @param stack
         *            the stack
         *
         * @return the top down iterator
         */
        private Iterator getTopDownIterator(final Stack stack) {
            return new Iterator() {
                private ListIterator _forwardIterator = stack.listIterator(stack.size());

                @Override
                public boolean hasNext() {
                    return _forwardIterator.hasPrevious();
                }

                @Override
                public Object next() {
                    return _forwardIterator.previous();
                }

                @Override
                public void remove() {
                    _forwardIterator.remove();
                }
            };
        }

        /**
         * Returns the most recently pushed context which implements the specified class. Will return null if no
         * matching context is found.
         *
         * @param matchingClass
         *            the matching class
         *
         * @return the closest context
         */
        public Object getClosestContext(Class matchingClass) {
            for (int i = _traversalContext.size() - 1; i >= 0; i--) {
                Object o = _traversalContext.elementAt(i);
                if (matchingClass.isInstance(o)) {
                    return o;
                }
            }
            return null;
        }

        /**
         * Perform.
         *
         * @param action
         *            the action
         */
        public void perform(NodeAction action) {
            while (!_pendingNodes.empty()) {
                final Object object = _pendingNodes.pop();
                if (object == POP_CONTEXT) {
                    _traversalContext.pop();
                } else {
                    Node node = (Node) object;
                    if (node.getNodeType() == Node.TEXT_NODE) {
                        action.processTextNode(this, node);
                    } else if (node.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    } else {
                        action.processElement(this, (Element) node);
                    }
                    pushNodeList(node.getLastChild());
                }
            }
        }

        /**
         * Push node list.
         *
         * @param nl
         *            the nl
         */
        private void pushNodeList(NodeList nl) {
            if (nl != null) {
                for (int i = nl.getLength() - 1; i >= 0; i--) {
                    _pendingNodes.push(nl.item(i));
                }
            }
        }

        /**
         * Push node list.
         *
         * @param lastChild
         *            the last child
         */
        private void pushNodeList(Node lastChild) {
            for (Node node = lastChild; node != null; node = node.getPreviousSibling()) {
                _pendingNodes.push(node);
            }
        }
    }

}

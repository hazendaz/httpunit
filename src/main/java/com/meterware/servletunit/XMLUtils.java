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
package com.meterware.servletunit;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The Class XMLUtils.
 */
abstract class XMLUtils {

    /**
     * Gets the child node value.
     *
     * @param root
     *            the root
     * @param childNodeName
     *            the child node name
     *
     * @return the child node value
     *
     * @throws SAXException
     *             the SAX exception
     */
    static String getChildNodeValue(Element root, String childNodeName) throws SAXException {
        return getChildNodeValue(root, childNodeName, null);
    }

    /**
     * Gets the child node value.
     *
     * @param root
     *            the root
     * @param childNodeName
     *            the child node name
     * @param defaultValue
     *            the default value
     *
     * @return the child node value
     *
     * @throws SAXException
     *             the SAX exception
     */
    static String getChildNodeValue(Element root, String childNodeName, String defaultValue) throws SAXException {
        NodeList nl = root.getElementsByTagName(childNodeName);
        if (nl.getLength() == 1) {
            return getTextValue(nl.item(0)).trim();
        }
        if (defaultValue == null) {
            throw new SAXException("Node <" + root.getNodeName() + "> has no child named <" + childNodeName + ">");
        }
        return defaultValue;
    }

    /**
     * Gets the text value.
     *
     * @param node
     *            the node
     *
     * @return the text value
     *
     * @throws SAXException
     *             the SAX exception
     */
    static String getTextValue(Node node) throws SAXException {
        Node textNode = node.getFirstChild();
        if (textNode == null) {
            return "";
        }
        if (textNode.getNodeType() != Node.TEXT_NODE) {
            throw new SAXException("No text value found for <" + node.getNodeName() + "> node");
        }
        return textNode.getNodeValue();
    }

    /**
     * Checks for child node.
     *
     * @param root
     *            the root
     * @param childNodeName
     *            the child node name
     *
     * @return true, if successful
     */
    static boolean hasChildNode(Element root, String childNodeName) {
        NodeList nl = root.getElementsByTagName(childNodeName);
        return nl.getLength() > 0;
    }

}

/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
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

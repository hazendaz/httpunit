/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.dom;

import com.meterware.httpunit.ParsedHTML;

import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLCollection;

/**
 * The Class HTMLContainerDelegate.
 */
class HTMLContainerDelegate {

    /** The iterator mask. */
    private NodeImpl.IteratorMask _iteratorMask = NodeImpl.SKIP_IFRAMES;

    /**
     * Instantiates a new HTML container delegate.
     *
     * @param iteratorMask
     *            the iterator mask
     */
    HTMLContainerDelegate(NodeImpl.IteratorMask iteratorMask) {
        _iteratorMask = iteratorMask;
    }

    /**
     * get Links for a given Node.
     *
     * @param rootNode
     *            - an array of forms
     *
     * @return the links
     */
    HTMLCollection getLinks(NodeImpl rootNode) {
        ArrayList elements = new ArrayList<>();
        for (Iterator each = rootNode.preOrderIteratorWithinNode(_iteratorMask); each.hasNext();) {
            Node node = (Node) each.next();
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            if (ParsedHTML.isWebLink(node)) {
                elements.add(node);
            }
        }
        return HTMLCollectionImpl.createHTMLCollectionImpl(new NodeListImpl(elements));
    }

    /**
     * get forms for a given Node.
     *
     * @param rootNode
     *            - the node to start from
     *
     * @return - an array of forms
     */
    HTMLCollection getForms(NodeImpl rootNode) {
        ArrayList elements = new ArrayList<>();
        for (Iterator each = rootNode.preOrderIteratorWithinNode(_iteratorMask); each.hasNext();) {
            Node node = (Node) each.next();
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            if ("form".equalsIgnoreCase(((Element) node).getTagName())) {
                elements.add(node);
            }
        }
        return HTMLCollectionImpl.createHTMLCollectionImpl(new NodeListImpl(elements));
    }

    /**
     * Gets the anchors.
     *
     * @param rootNode
     *            the root node
     *
     * @return the anchors
     */
    HTMLCollection getAnchors(NodeImpl rootNode) {
        NodeList nodeList = rootNode.getElementsByTagName("A");
        ArrayList elements = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getAttributes().getNamedItem("name") != null) {
                elements.add(node);
            }
        }
        return HTMLCollectionImpl.createHTMLCollectionImpl(new NodeListImpl(elements));
    }

    /**
     * Gets the images.
     *
     * @param rootNode
     *            the root node
     *
     * @return the images
     */
    HTMLCollection getImages(NodeImpl rootNode) {
        ArrayList elements = new ArrayList<>();
        rootNode.appendElementsWithTags(new String[] { "img" }, elements);
        return HTMLCollectionImpl.createHTMLCollectionImpl(new NodeListImpl(elements));
    }

    /**
     * Gets the applets.
     *
     * @param rootNode
     *            the root node
     *
     * @return the applets
     */
    HTMLCollection getApplets(NodeImpl rootNode) {
        ArrayList elements = new ArrayList<>();
        rootNode.appendElementsWithTags(new String[] { "applet" }, elements);
        return HTMLCollectionImpl.createHTMLCollectionImpl(new NodeListImpl(elements));
    }
}

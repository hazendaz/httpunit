/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Provides an HTMLElement Predicate that is capable of matching based on an XPath node specification. This allows for
 * very advanced matching techniques. THREAD: Instances are not thread safe, each thread should create its own instance
 * with a specific xpath. (The same instance can be used for multiple documents, each change in document will result in
 * its internal caches being flushed).
 */
public class XPathPredicate implements HTMLElementPredicate {

    /** XPath which dictates matching nodes, from root. */
    private XPathExpression xpath;

    /** The path. */
    private String path;

    /** The Constant DEBUG. */
    // set to true for debugging
    public static final boolean DEBUG = false;

    /**
     * Constructs an HTMLElementPredicate that matches only those elements which match the provided XPath.
     *
     * @param path
     *            [in] XPath specification of valid/matching nodes
     *
     * @throws XPathExpressionException
     *             if the xpath is invalid
     */
    public XPathPredicate(String path) throws XPathExpressionException {
        this.path = path;
        this.xpath = XPathFactory.newInstance().newXPath().compile(path);
    }

    /**
     * debug Output for node structure.
     *
     * @param node
     *            the node
     * @param indent
     *            the indent
     */
    private void debugOut(Node node, String indent) {
        System.out.print(indent + node.getNodeName() + ":");
        System.out.println(indent + node.getNodeValue());
        NodeList nl = node.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            debugOut(nl.item(i), indent + "\t");
        }
    }

    /**
     * check whether the given criteria are matched for the given element
     *
     * @param someElement
     *            - the element to check
     * @param criteria
     *            - the criteria to check
     */
    @Override
    public boolean matchesCriteria(final Object someElement, final Object criteria) {

        // this condition should normally be false
        if (!(someElement instanceof HTMLElement)) {
            return false;
        }

        HTMLElement htmlElement = (HTMLElement) someElement;

        Node htmlNode = htmlElement.getNode();
        Document doc = htmlNode.getOwnerDocument();
        if (DEBUG) {
            debugOut(doc, "");
        }

        NodeList nodes;
        try {
            nodes = (NodeList) xpath.evaluate(doc, XPathConstants.NODESET);
            final int nodeCount = nodes.getLength();
            for (int i = 0; i < nodeCount; i++) {
                if (nodes.item(i).equals(htmlNode)) {
                    return true;
                }
            }
        } catch (XPathExpressionException e) {
            throw new RuntimeException("unable to evaluate xpath '" + path + "'", e);
        }
        return false;
    }

}

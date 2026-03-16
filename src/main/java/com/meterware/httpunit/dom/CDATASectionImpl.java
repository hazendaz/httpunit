/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.dom;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Node;

/**
 * The Class CDATASectionImpl.
 */
public class CDATASectionImpl extends TextImpl implements CDATASection {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates the CDATA section.
     *
     * @param ownerDocument
     *            the owner document
     * @param data
     *            the data
     *
     * @return the CDATA section
     */
    public static CDATASection createCDATASection(DocumentImpl ownerDocument, String data) {
        CDATASectionImpl cdataSection = new CDATASectionImpl();
        cdataSection.initialize(ownerDocument, data);
        return cdataSection;
    }

    /**
     * Import node.
     *
     * @param document
     *            the document
     * @param cdataSection
     *            the cdata section
     *
     * @return the node
     */
    public static Node importNode(DocumentImpl document, CDATASection cdataSection) {
        return document.createCDATASection(cdataSection.getData());
    }

    @Override
    public String getNodeName() {
        return "#cdata-section";
    }

    @Override
    public short getNodeType() {
        return CDATA_SECTION_NODE;
    }
}

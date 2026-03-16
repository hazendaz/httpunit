/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;

/**
 * The Class DocumentTypeImpl.
 */
public class DocumentTypeImpl extends NodeImpl implements DocumentType {

    // ---------------------------------------------- DocumentType methods
    // --------------------------------------------------

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    @Override
    public NamedNodeMap getEntities() {
        return null;
    }

    @Override
    public String getInternalSubset() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public NamedNodeMap getNotations() {
        return null;
    }

    @Override
    public String getPublicId() {
        return null;
    }

    @Override
    public String getSystemId() {
        return null;
    }

    // ------------------------------------------------ NodeImpl methods
    // ----------------------------------------------------

    @Override
    public String getNodeName() {
        return null;
    }

    @Override
    public short getNodeType() {
        return 0;
    }

    @Override
    public String getNodeValue() throws DOMException {
        return null;
    }

    @Override
    public void setNodeValue(String nodeValue) throws DOMException {
    }
}

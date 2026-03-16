/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.dom;

/**
 * The Class NamespaceAwareNodeImpl.
 */
public abstract class NamespaceAwareNodeImpl extends NodeImpl {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The tag name. */
    private String _tagName;

    /** The local name. */
    private String _localName;

    /** The namespace uri. */
    private String _namespaceUri;

    /**
     * Initialize.
     *
     * @param owner
     *            the owner
     * @param tagName
     *            the tag name
     */
    protected void initialize(DocumentImpl owner, String tagName) {
        initialize(owner);
        _localName = _tagName = tagName;
    }

    /**
     * initialize the name space.
     *
     * @param owner
     *            the owner
     * @param namespaceURI
     *            the namespace URI
     * @param qualifiedName
     *            the qualified name
     */
    protected void initialize(DocumentImpl owner, String namespaceURI, String qualifiedName) {
        initialize(owner);
        _tagName = qualifiedName;
        _namespaceUri = namespaceURI;
        if (qualifiedName.indexOf(':') < 0) {
            _localName = qualifiedName;
        } else {
            _localName = qualifiedName.substring(qualifiedName.indexOf(':') + 1);
        }
        setParentScope(owner);
    }

    @Override
    public String getNodeName() {
        return getTagName();
    }

    /**
     * Gets the tag name.
     *
     * @return the tag name
     */
    public String getTagName() {
        return _tagName;
    }

    @Override
    public String getNamespaceURI() {
        return _namespaceUri;
    }

    @Override
    public String getLocalName() {
        return _localName;
    }
}

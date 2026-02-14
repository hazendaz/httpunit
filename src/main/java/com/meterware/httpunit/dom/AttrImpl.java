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
package com.meterware.httpunit.dom;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.TypeInfo;

/**
 * The Class AttrImpl.
 */
public class AttrImpl extends NodeImpl implements Attr {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The name. */
    private String _name;

    /** The value. */
    private String _value = "";

    /** The specified. */
    private boolean _specified = false;

    /** The owner element. */
    private Element _ownerElement;

    /**
     * Creates the attribute.
     *
     * @param owner
     *            the owner
     * @param name
     *            the name
     *
     * @return the attr impl
     */
    static AttrImpl createAttribute(DocumentImpl owner, String name) {
        AttrImpl attribute = new AttrImpl();
        attribute.initialize(owner, name);
        return attribute;
    }

    /**
     * Creates the attribute.
     *
     * @param owner
     *            the owner
     * @param namespaceURI
     *            the namespace URI
     * @param qualifiedName
     *            the qualified name
     *
     * @return the attr
     */
    public static Attr createAttribute(DocumentImpl owner, String namespaceURI, String qualifiedName) {
        AttrImpl attribute = new AttrImpl();
        attribute.initialize(owner, qualifiedName);
        return attribute;
    }

    /**
     * Initialize.
     *
     * @param owner
     *            the owner
     * @param name
     *            the name
     */
    protected void initialize(DocumentImpl owner, String name) {
        super.initialize(owner);
        _name = name;
    }

    @Override
    public String getNodeName() {
        return getName();
    }

    @Override
    public String getNodeValue() throws DOMException {
        return getValue();
    }

    @Override
    public void setNodeValue(String nodeValue) throws DOMException {
        setValue(nodeValue);
    }

    @Override
    public short getNodeType() {
        return ATTRIBUTE_NODE;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public boolean getSpecified() {
        return _specified;
    }

    @Override
    public String getValue() {
        return _value;
    }

    @Override
    public void setValue(String value) throws DOMException {
        _value = value;
        _specified = true;
    }

    @Override
    public Element getOwnerElement() {
        return _ownerElement;
    }

    /**
     * Sets the owner element.
     *
     * @param ownerElement
     *            the new owner element
     */
    void setOwnerElement(Element ownerElement) {
        _ownerElement = ownerElement;
    }

    /**
     * Import node.
     *
     * @param document
     *            the document
     * @param attr
     *            the attr
     *
     * @return the node
     */
    public static Node importNode(Document document, Attr attr) {
        Attr attribute = document.createAttributeNS(attr.getNamespaceURI(), attr.getName());
        attribute.setValue(attr.getValue());
        return attribute;
    }

    // ------------------------------------- DOM level 3 methods
    // ------------------------------------------------------------

    @Override
    public TypeInfo getSchemaTypeInfo() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isId() {
        return false; // To change body of implemented methods use File | Settings | File Templates.
    }
}

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

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.TypeInfo;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class AttrImpl extends NodeImpl implements Attr {

    private static final long serialVersionUID = 1L;
    private String _name;
    private String _value = "";
    private boolean _specified = false;
    private Element _ownerElement;

    static AttrImpl createAttribute(DocumentImpl owner, String name) {
        AttrImpl attribute = new AttrImpl();
        attribute.initialize(owner, name);
        return attribute;
    }

    public static Attr createAttribute(DocumentImpl owner, String namespaceURI, String qualifiedName) {
        AttrImpl attribute = new AttrImpl();
        attribute.initialize(owner, qualifiedName);
        return attribute;
    }

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

    void setOwnerElement(Element ownerElement) {
        _ownerElement = ownerElement;
    }

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

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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;

/**
 * The Class ElementImpl.
 */
public class ElementImpl extends NamespaceAwareNodeImpl implements Element {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The attributes. */
    private Hashtable _attributes = new Hashtable<>();

    /** The listeners. */
    private ArrayList _listeners = new ArrayList<>();

    /**
     * Creates the element.
     *
     * @param owner
     *            the owner
     * @param tagName
     *            the tag name
     *
     * @return the element impl
     */
    static ElementImpl createElement(DocumentImpl owner, String tagName) {
        ElementImpl element = new ElementImpl();
        element.initialize(owner, tagName);
        return element;
    }

    /**
     * Creates the element.
     *
     * @param owner
     *            the owner
     * @param namespaceURI
     *            the namespace URI
     * @param qualifiedName
     *            the qualified name
     *
     * @return the element
     */
    public static Element createElement(DocumentImpl owner, String namespaceURI, String qualifiedName) {
        ElementImpl element = new ElementImpl();
        element.initialize(owner, namespaceURI, qualifiedName);
        return element;
    }

    /**
     * Adds the dom listener.
     *
     * @param listener
     *            the listener
     */
    public void addDomListener(DomListener listener) {
        synchronized (_listeners) {
            _listeners.add(listener);
        }
    }

    /**
     * Report property changed.
     *
     * @param propertyName
     *            the property name
     */
    protected void reportPropertyChanged(String propertyName) {
        ArrayList listeners;
        synchronized (_listeners) {
            listeners = (ArrayList) _listeners.clone();
        }

        for (Iterator each = listeners.iterator(); each.hasNext();) {
            ((DomListener) each.next()).propertyChanged(this, propertyName);
        }
    }

    // ---------------------------------------- Element methods
    // -------------------------------------------------------------

    @Override
    public short getNodeType() {
        return ELEMENT_NODE;
    }

    @Override
    public String getNodeValue() throws DOMException {
        return null;
    }

    @Override
    public void setNodeValue(String nodeValue) throws DOMException {
    }

    @Override
    public boolean hasAttributes() {
        return !_attributes.isEmpty();
    }

    @Override
    public NamedNodeMap getAttributes() {
        return new NamedNodeMapImpl(_attributes);
    }

    /**
     * get the attribute with the given name
     *
     * @param name
     *            - the name of the attribute to get
     */
    @Override
    public String getAttribute(String name) {
        Attr attr = getAttributeNode(name);
        return attr == null ? "" : attr.getValue();
    }

    @Override
    public void setAttribute(String name, String value) throws DOMException {
        if (value.equals(getAttribute(name))) {
            return;
        }

        Attr attribute = getOwnerDocument().createAttribute(name);
        attribute.setValue(value);
        setAttributeNode(attribute);
        reportPropertyChanged(name);
    }

    /**
     * get the event Handler script for the event e.g. onchange, onmousedown, onclick, onmouseup execute the script if
     * it's assigned by calling doEvent for the script
     *
     * @param eventName
     *            the event name
     *
     * @return true, if successful
     */
    @Override
    public boolean handleEvent(String eventName) {
        // check whether onclick is activated
        if (eventName.equalsIgnoreCase("onclick")) {
            handleEvent("onmousedown");
        }
        String eventScript = getAttribute(eventName);
        boolean result = doEventScript(eventScript);
        if (eventName.equalsIgnoreCase("onclick")) {
            handleEvent("onmouseup");
        }
        return result;
    }

    @Override
    public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException {
        Attr attribute = getOwnerDocument().createAttributeNS(namespaceURI, qualifiedName);
        attribute.setValue(value);
        setAttributeNodeNS(attribute);
    }

    @Override
    public void removeAttribute(String name) throws DOMException {
        _attributes.remove(name);
    }

    @Override
    public Attr getAttributeNode(String name) {
        return (Attr) _attributes.get(name);
    }

    @Override
    public Attr setAttributeNode(Attr newAttr) throws DOMException {
        if (newAttr.getOwnerDocument() != getOwnerDocument()) {
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,
                    "attribute must be from the same document as the element");
        }

        ((AttrImpl) newAttr).setOwnerElement(this);
        AttrImpl oldAttr = (AttrImpl) _attributes.put(newAttr.getName(), newAttr);
        if (oldAttr != null) {
            oldAttr.setOwnerElement(null);
        }
        return oldAttr;
    }

    @Override
    public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
        if (newAttr.getOwnerDocument() != getOwnerDocument()) {
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,
                    "attribute must be from the same document as the element");
        }

        ((AttrImpl) newAttr).setOwnerElement(this);
        AttrImpl oldAttr = (AttrImpl) _attributes.put(newAttr.getName(), newAttr);
        if (oldAttr != null) {
            oldAttr.setOwnerElement(null);
        }
        return oldAttr;
    }

    @Override
    public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
        if (!_attributes.containsValue(oldAttr)) {
            throw new DOMException(DOMException.NOT_FOUND_ERR, "Specified attribute is not defined for this element");
        }

        AttrImpl removedAttr = (AttrImpl) _attributes.remove(oldAttr.getName());
        if (removedAttr != null) {
            removedAttr.setOwnerElement(null);
        }
        return removedAttr;
    }

    @Override
    public boolean hasAttribute(String name) {
        return _attributes.containsKey(name);
    }

    // ----------------------- namespaces are not supported at present --------------------------------

    @Override
    public String getAttributeNS(String namespaceURI, String localName) {
        return null;
    }

    @Override
    public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
    }

    @Override
    public Attr getAttributeNodeNS(String namespaceURI, String localName) {
        return null;
    }

    @Override
    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        return null;
    }

    @Override
    public boolean hasAttributeNS(String namespaceURI, String localName) {
        return false;
    }

    /**
     * Import node.
     *
     * @param document
     *            the document
     * @param original
     *            the original
     * @param deep
     *            the deep
     *
     * @return the element
     */
    public static Element importNode(DocumentImpl document, Element original, boolean deep) {
        Element copy = document.createElementNS(original.getNamespaceURI(), original.getTagName());
        NamedNodeMap attributes = original.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            copy.setAttributeNode((Attr) document.importNode(attributes.item(i), false));
        }
        if (deep) {
            document.importChildren(original, copy);
        }
        return copy;
    }

    // ------------------------------------- DOM level 3 methods
    // ------------------------------------------------------------

    @Override
    public TypeInfo getSchemaTypeInfo() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setIdAttribute(String name, boolean isId) throws DOMException {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException {
        // To change body of implemented methods use File | Settings | File Templates.
    }
}

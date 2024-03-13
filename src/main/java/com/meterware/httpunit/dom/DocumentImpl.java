/*
 * MIT License
 *
 * Copyright 2011-2024 Russell Gold
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

import java.util.Iterator;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.html.HTMLElement;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class DocumentImpl extends NodeImpl implements Document {

    private static final long serialVersionUID = 1L;
    protected Element _documentElement;

    static DocumentImpl createDocument() {
        DocumentImpl document = new DocumentImpl();
        document.initialize();
        return document;
    }

    protected void initialize() {
    }

    @Override
    public String getNodeName() {
        return "#document";
    }

    @Override
    public String getNodeValue() throws DOMException {
        return null;
    }

    @Override
    public void setNodeValue(String nodeValue) throws DOMException {
    }

    @Override
    public short getNodeType() {
        return DOCUMENT_NODE;
    }

    @Override
    public Document getOwnerDocument() {
        return this;
    }

    @Override
    public DocumentType getDoctype() {
        return null;
    }

    @Override
    public DOMImplementation getImplementation() {
        return null;
    }

    @Override
    public Element getDocumentElement() {
        return _documentElement;
    }

    void setDocumentElement(Element documentElement) {
        if (_documentElement != null) {
            throw new IllegalStateException("A document may have only one root");
        }
        _documentElement = documentElement;
        appendChild(documentElement);
    }

    @Override
    public Element createElement(String tagName) throws DOMException {
        return ElementImpl.createElement(this, tagName);
    }

    @Override
    public DocumentFragment createDocumentFragment() {
        throw new UnsupportedOperationException("DocumentFragment creation not supported ");
    }

    @Override
    public Text createTextNode(String data) {
        return TextImpl.createText(this, data);
    }

    @Override
    public Comment createComment(String data) {
        return CommentImpl.createComment(this, data);
    }

    @Override
    public CDATASection createCDATASection(String data) throws DOMException {
        return CDATASectionImpl.createCDATASection(this, data);
    }

    @Override
    public ProcessingInstruction createProcessingInstruction(String target, String data) throws DOMException {
        return ProcessingInstructionImpl.createProcessingImpl(this, target, data);
    }

    @Override
    public Attr createAttribute(String name) throws DOMException {
        return AttrImpl.createAttribute(this, name);
    }

    @Override
    public EntityReference createEntityReference(String name) throws DOMException {
        throw new UnsupportedOperationException("EntityReference creation not supported ");
    }

    @Override
    public Node importNode(Node importedNode, boolean deep) throws DOMException {
        switch (importedNode.getNodeType()) {
        case Node.ATTRIBUTE_NODE:
            return AttrImpl.importNode(this, (Attr) importedNode);
        case Node.CDATA_SECTION_NODE:
            return CDATASectionImpl.importNode(this, (CDATASection) importedNode);
        case Node.COMMENT_NODE:
            return CommentImpl.importNode(this, (Comment) importedNode);
        case Node.ELEMENT_NODE:
            return ElementImpl.importNode(this, (Element) importedNode, deep);
        case Node.PROCESSING_INSTRUCTION_NODE:
            return ProcessingInstructionImpl.importNode(this, (ProcessingInstruction) importedNode);
        case Node.TEXT_NODE:
            return TextImpl.importNode(this, (Text) importedNode);
        default:
            throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                    "Cannot import node type " + importedNode.getNodeType());
        }
    }

    @Override
    public Element getElementById(String elementId) {
        for (Iterator each = preOrderIterator(); each.hasNext();) {
            Node node = (Node) each.next();
            if (!(node instanceof HTMLElement)) {
                continue;
            }
            HTMLElement element = (HTMLElement) node;
            if (elementId.equals(element.getId())) {
                return element;
            }
        }
        return null;
    }

    @Override
    public Element createElementNS(String namespaceURI, String qualifiedName) throws DOMException {
        return ElementImpl.createElement(this, namespaceURI, qualifiedName);
    }

    @Override
    public Attr createAttributeNS(String namespaceURI, String qualifiedName) throws DOMException {
        return AttrImpl.createAttribute(this, namespaceURI, qualifiedName);
    }

    @Override
    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        if (namespaceURI != null) {
            throw new UnsupportedOperationException("Namespaces are not supported");
        }
        return getElementsByTagName(localName);
    }

    /**
     * import the children
     *
     * @param original
     * @param copy
     */
    void importChildren(Node original, Node copy) {
        NodeList children = original.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node childCopy = importNode(children.item(i), /* deep */ true);
            copy.appendChild(childCopy);
        }
    }

    // ------------------------------------- DOM level 3 methods
    // ------------------------------------------------------------

    @Override
    public String getInputEncoding() {
        return null;
    }

    @Override
    public String getXmlEncoding() {
        return null;
    }

    @Override
    public Node renameNode(Node n, String namespaceURI, String qualifiedName) throws DOMException {
        return null;
    }

    @Override
    public boolean getXmlStandalone() {
        return false;
    }

    @Override
    public void setXmlStandalone(boolean xmlStandalone) throws DOMException {
    }

    @Override
    public String getXmlVersion() {
        return null;
    }

    @Override
    public void setXmlVersion(String xmlVersion) throws DOMException {
    }

    @Override
    public boolean getStrictErrorChecking() {
        return false;
    }

    @Override
    public void setStrictErrorChecking(boolean strictErrorChecking) {
    }

    @Override
    public String getDocumentURI() {
        return null;
    }

    @Override
    public void setDocumentURI(String documentURI) {
    }

    @Override
    public Node adoptNode(Node source) throws DOMException {
        return null;
    }

    @Override
    public DOMConfiguration getDomConfig() {
        return null;
    }

    @Override
    public void normalizeDocument() {
    }

}

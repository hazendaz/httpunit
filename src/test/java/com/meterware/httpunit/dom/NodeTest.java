/*
 * MIT License
 *
 * Copyright 2011-2023 Russell Gold
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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.w3c.dom.*;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 */
class NodeTest {

    private DocumentImpl _document;
    private Element _element;
    private Element _foo1;
    private Element _foo2;
    private Element _bar1;
    private Element _bar2;
    private Text _text;


    @BeforeEach
    void setUp() throws Exception {
        _document = DocumentImpl.createDocument();
        _element = _document.createElement("zork");
        _foo1 = _document.createElement("foo");
        _foo2 = _document.createElement("foo");
        _bar1 = _document.createElement("bar");
        _bar2 = _document.createElement("bar");
        _text = _document.createTextNode("Something to say");
        _document.setDocumentElement(_element);
        _element.appendChild(_foo1);
        _element.appendChild(_bar2);
        _foo1.appendChild(_bar1);
        _foo1.appendChild(_text);
        _foo1.appendChild(_foo2);
    }


    /**
     * Verifies that we can create a document and verify its type.
     */
    @Test
    void testDocumentCreation() throws Exception {
        assertEquals("#document", _document.getNodeName(), "Node name");
        assertEquals(Node.DOCUMENT_NODE, _document.getNodeType(), "Node type");
        assertNull(_document.getAttributes(), "Documents should not have attributes");
        assertNull(_document.getNodeValue(), "Documents should not have values");
        _document.setNodeValue("an example");
        assertNull(_document.getNodeValue(), "Setting the element value should have no effect");
        assertSame(_document, _document.getOwnerDocument(), "Owner document");
    }


    /**
     * Verifies that we can create an element with a given name and verify its type.
     */
    @Test
    void testElementCreation() throws Exception {
        assertNotNull(_element, "Failed to create an element");
        assertSame(_document, _element.getOwnerDocument(), "Owner document");
        assertEquals("zork", _element.getTagName(), "Tag name");
        assertEquals("zork", _element.getNodeName(), "Node name");
        assertEquals(Node.ELEMENT_NODE, _element.getNodeType(), "Node type");
        assertNull(_element.getNodeValue(), "Elements should not have values");
        _element.setNodeValue("an example");
        assertNull(_element.getNodeValue(), "Setting the element value should have no effect");
    }


    /**
     * Verifies that we can create a text node and verify its type.
     */
    @Test
    void testTextCreation() throws Exception {
        assertNotNull(_text, "Failed to create a text node");
        assertSame(_document, _text.getOwnerDocument(), "Owner document");
        assertEquals("#text", _text.getNodeName(), "Node name");
        assertEquals(Node.TEXT_NODE, _text.getNodeType(), "Node type");
        assertNull(_text.getAttributes(), "Text nodes should not have attributes");
        assertEquals("Something to say", _text.getNodeValue(), "Text node value");
        assertEquals("Something to say".length(), _text.getLength(), "Text length");
        _text.setNodeValue("an example");
        assertEquals("an example", _text.getNodeValue(), "Revised node value");
    }


    /**
     * Verifies that we can create a document type node and verify its type.
     */
     @Test
    @Disabled
    void testDocumentTypeCreation() throws Exception {
//        DocumentType documentType = com.meterware.httpunit.dom.DocumentTypeImpl.createDocumentType( _document );
        assertNotNull(_text, "Failed to create a text node");
        assertSame(_document, _text.getOwnerDocument(), "Owner document");
        assertEquals("#text", _text.getNodeName(), "Node name");
        assertEquals(Node.TEXT_NODE, _text.getNodeType(), "Node type");
        assertNull(_text.getAttributes(), "Text nodes should not have attributes");
        assertEquals("Something to say", _text.getNodeValue(), "Text node value");
        assertEquals("Something to say".length(), _text.getLength(), "Text length");
        _text.setNodeValue("an example");
        assertEquals("an example", _text.getNodeValue(), "Revised node value");
    }


    /**
     * Verifies that node accessors work for empty documents.
     */
    @Test
    void testEmptyDocument() throws Exception {
        Document document = DocumentImpl.createDocument();
        assertNull(document.getFirstChild(), "Found a bogus first child");
        assertNull(document.getLastChild(), "Found a bogus last child");
        assertFalse(document.hasChildNodes(), "Reported bogus children");
        assertNull(document.getNextSibling(), "Found a bogus next sibling");
        assertNull(document.getPreviousSibling(), "Found a bogus previous sibling");
        assertNull(document.getParentNode(), "Found a bogus parent");
        verifyNodeList("empty document children", document.getChildNodes(), new Node[0]);
    }


    /**
     * Verifies that we can add children to an element (or document) and find them.
     */
    @Test
    void testAddNodeChildren() throws Exception {
        assertSame(_foo1, _element.getFirstChild(), "First child of element");
        assertSame(_bar2, _element.getLastChild(), "Last child of element");
        assertSame(_bar2, _foo1.getNextSibling(), "Next sibling of foo1");
        assertSame(_foo1, _bar2.getPreviousSibling(), "Previous sibling of bar2");
        verifyNodeList("foo1 child", _foo1.getChildNodes(), new Node[]{_bar1, _text, _foo2});
        assertTrue(_foo1.hasChildNodes(), "Did not find children for foo1");
        assertFalse(_bar1.hasChildNodes(), "Found ghost children for bar1");
        assertSame(_foo1, _bar1.getParentNode(), "Parent of bar1");
        assertSame(_document, _element.getParentNode(), "Parent of element");
    }


    /**
     * Verifies that we can add children to an element or document and find them.
     */
    @Test
    void testElementChildrenByTagName() throws Exception {
        verifyNodeList("baz", _element.getElementsByTagName("baz"), new Node[0]);
        verifyNodeList("foo", _element.getElementsByTagName("foo"), new Element[]{_foo1, _foo2});
        verifyNodeList("bar", _element.getElementsByTagName("bar"), new Element[]{_bar1, _bar2});
        verifyNodeList("*", _element.getElementsByTagName("*"), new Element[]{_foo1, _bar1, _foo2, _bar2});

        verifyNodeList("baz", _document.getElementsByTagName("baz"), new Node[0]);
        verifyNodeList("foo", _document.getElementsByTagName("foo"), new Element[]{_foo1, _foo2});
        verifyNodeList("bar", _document.getElementsByTagName("bar"), new Element[]{_bar1, _bar2});
        verifyNodeList("*", _document.getElementsByTagName("*"), new Element[]{_element, _foo1, _bar1, _foo2, _bar2});
    }


    /**
     * Verifies that only children of a particular document may be added to its children.
     */
    @Test
    void testNodeCreatedByOtherDocument() throws Exception {
        Document foreignDocument = DocumentImpl.createDocument();
        Element foreignElement = foreignDocument.createElement("stranger");
        try {
            _element.appendChild(foreignElement);
            fail("Permitted addition of element from different document");
        } catch (DOMException e) {
            assertEquals(DOMException.WRONG_DOCUMENT_ERR, e.code, "Reason for exception");
        }

    }


    /**
     * Verifies that a document can have only one 'document element'
     */
    @Test
    void testUniqueDocumentElement() throws Exception {
        Element bogusRoot = _document.createElement("root");
        try {
            _document.setDocumentElement(bogusRoot);
            fail("Permitted addition of a second document element");
        } catch (IllegalStateException e) {
        }
    }


    /**
     * Verifies that text nodes cannot have children
     */
    @Test
    void testNoChildrenForTextNodes() throws Exception {
        Element orphan = _document.createElement("baz");
        try {
            _text.appendChild(orphan);
            fail("Should not have permitted addition of a child to a text node");
        } catch (DOMException e) {
            assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code, "Reason for exception");
        }
    }


    /**
     * Verifies that a node or one of its ancestors may not be added as its child
     */
    @Test
    void testRejectAddSelfOrAncestorAsChild() throws Exception {
        try {
            _element.appendChild(_element);
            fail("Permitted addition of element as its own child");
        } catch (DOMException e) {
            assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code, "Reason for exception");
        }
        try {
            _bar1.appendChild(_element);
            fail("Permitted addition of element as its descendant's child");
        } catch (DOMException e) {
            assertEquals(DOMException.HIERARCHY_REQUEST_ERR, e.code, "Reason for exception");
        }
    }


    /**
     * Verifies that we can insert a child node at a specific position.
     */
    @Test
    void testInsertChild() throws Exception {
        Text newText = _document.createTextNode("Something new");
        _element.insertBefore(newText, _bar2);
        verifyNodeList("element child", _element.getChildNodes(), new Node[]{_foo1, newText, _bar2});
        _element.insertBefore(newText, _foo1);
        verifyNodeList("element child", _element.getChildNodes(), new Node[]{newText, _foo1, _bar2});
    }


    /**
     * Verifies that we cannot insert a child at a target by specifying a node which is not already a child of that target.
     */
    @Test
    void testInsertChildWithBadPredecessor() throws Exception {
        Text newText = _document.createTextNode("Something new");
        _element.insertBefore(newText, _bar2);
        try {
            _foo1.insertBefore(newText, _bar2);
            fail("Permitted insertion before a node that was not a child of the target");
        } catch (DOMException e) {
            assertEquals(DOMException.NOT_FOUND_ERR, e.code, "Reason for exception");
        }
        verifyNodeList("foo1 child", _foo1.getChildNodes(), new Node[]{_bar1, _text, _foo2});
        verifyNodeList("element child", _element.getChildNodes(), new Node[]{_foo1, newText, _bar2});
    }


    /**
     * Verifies that we can remove a child node from the document.
     */
    @Test
    void testRemoveChildFromEnd() throws Exception {
        _foo1.removeChild(_foo2);
        verifyNodeList("foo1 child", _foo1.getChildNodes(), new Node[]{_bar1, _text});
    }


    @Test
    void testRemoveChildFromBeginning() throws Exception {
        _foo1.removeChild(_bar1);
        verifyNodeList("foo1 child", _foo1.getChildNodes(), new Node[]{_text, _foo2});
    }


    @Test
    void testRemoveChildFromMiddle() throws Exception {
        _foo1.removeChild(_text);
        verifyNodeList("foo1 child", _foo1.getChildNodes(), new Node[]{_bar1, _foo2});
    }


    /**
     * Verifies that an exception is thrown if we try to remove a node which is not a child.
     */
    @Test
    void testRemoveChildFromWrongParent() throws Exception {
        try {
            _foo1.removeChild(_bar2);
            fail("Permitted node removal from wrong child");
        } catch (DOMException e) {
            assertEquals(DOMException.NOT_FOUND_ERR, e.code, "reason for exception");
        }
    }


    /**
     * Verifies that we can replace children (including those already elsewhere in the tree)
     */
    @Test
    void testReplaceChild() throws Exception {
        Element baz = _document.createElement("baz");
        Node old = _foo1.replaceChild(baz, _text);
        assertSame(_text, old, "Removed node");
        verifyNodeList("foo1 child", _foo1.getChildNodes(), new Node[]{_bar1, baz, _foo2});
    }


    /**
     * Verifies that we can clone nodes
     */
    @Test
    void testCloneNode() throws Exception {
        _element.setAttribute("msg", "hi there");
        Element shallowClone = (Element) _element.cloneNode( /* deep */ false);
        assertEquals("hi there", shallowClone.getAttribute("msg"), "Cloned attribute");
        assertFalse(shallowClone.hasChildNodes(), "Shallow clone should not have children");

        Element deepClone = (Element) _element.cloneNode( /* deep */ true);
        assertEquals("hi there", deepClone.getAttribute("msg"), "Cloned attribute");
        assertTrue(deepClone.hasChildNodes(), "Deep clone should have children");
        NodeList childNodes = deepClone.getChildNodes();
        assertEquals(2, childNodes.getLength(), "Number of deepClone's children");
        assertTrue(childNodes.item(0) instanceof Element, "First child is not an element");
        assertEquals(3, childNodes.item(0).getChildNodes().getLength(), "First cloned child's children");
    }


    /**
     * Verifies that we can iterate through nodes in order
     */
    @Test
    void testPreOrderIterator() throws Exception {
        Iterator each = ((NodeImpl) _element).preOrderIterator();
        Node[] expectedNodes = {_element, _foo1, _bar1, _text, _foo2, _bar2};
        for (int i = 0;i < expectedNodes.length;i++) {
            assertTrue(each.hasNext(), "Iterator prematurely terminated after " + i + " nodes");
            assertSame(expectedNodes[i], each.next(), "Node " + (1 + i) + ":");
        }
        assertFalse(each.hasNext(), "Iterator should have terminated after " + expectedNodes.length + " nodes");
    }


    /**
     * Verifies that we can iterate through nodes in order, starting from a specific node.
     */
    @Test
    void testPreOrderIteratorFromANode() throws Exception {
        Iterator each = ((NodeImpl) _text).preOrderIterator();
        Node[] expectedNodes = {_text, _foo2, _bar2};
        for (int i = 0;i < expectedNodes.length;i++) {
            assertTrue(each.hasNext(), "Iterator prematurely terminated after " + i + " nodes");
            assertSame(expectedNodes[i], each.next(), "Node " + (1 + i) + ":");
        }
        assertFalse(each.hasNext(), "Iterator should have terminated after " + expectedNodes.length + " nodes");
    }


    /**
     * Verifies that we can iterate through nodes in order, starting after a specific node.
     */
    @Test
    void testPreOrderIteratorAfterANode() throws Exception {
        Iterator each = ((NodeImpl) _foo1).preOrderIteratorAfterNode();
        Node[] expectedNodes = {_bar1, _text, _foo2, _bar2};
        for (int i = 0;i < expectedNodes.length;i++) {
            assertTrue(each.hasNext(), "Iterator prematurely terminated after " + i + " nodes");
            assertSame(expectedNodes[i], each.next(), "Node " + (1 + i) + ":");
        }
        assertFalse(each.hasNext(), "Iterator should have terminated after " + expectedNodes.length + " nodes");
    }

    /**
     * Verifies that we can iterate through nodes in order, starting after a specific node.
     */
    @Test
    void testPreOrderIteratorWithinNode() throws Exception {
        Iterator each = ((NodeImpl) _foo1).preOrderIteratorWithinNode();
        Node[] expectedNodes = {_bar1, _text, _foo2};
        for (int i = 0;i < expectedNodes.length;i++) {
            assertTrue(each.hasNext(), "Iterator prematurely terminated after " + i + " nodes");
            Object node = each.next();
            assertSame(expectedNodes[i], node, "Node " + (1 + i) + ":");
        }
        assertFalse(each.hasNext(), "Iterator should have terminated after " + expectedNodes.length + " nodes");
    }

    /**
     * Verifies that we can iterate through nodes in order skipping a specified subtree.
     */
    @Test
    void testPreOrderIteratorWithMask() throws Exception {
        Iterator each = ((NodeImpl) _element).preOrderIterator(new NodeImpl.IteratorMask() {
            public boolean skipSubtree(Node subtreeRoot) {
                return subtreeRoot == _foo1;
            }
        });
        Node[] expectedNodes = {_element, _bar2};
        for (int i = 0;i < expectedNodes.length;i++) {
            assertTrue(each.hasNext(), "Iterator prematurely terminated after " + i + " nodes");
            assertSame(expectedNodes[i], each.next(), "Node " + (1 + i) + ":");
        }
        assertFalse(each.hasNext(), "Iterator should have terminated after " + expectedNodes.length + " nodes");
    }


    private void verifyNodeList(String comment, NodeList nl, Node[] expectedNodes) {
        assertNotNull(nl, "No " + comment + " node list returned");
        assertEquals(expectedNodes.length, nl.getLength(), "Number of " + comment + " nodes found");
        for (int i = 0; i < expectedNodes.length; i++) {
            Node expectedNode = expectedNodes[i];
            assertSame(expectedNode, nl.item(i), comment + " node " + (i + 1));
        }
    }
}

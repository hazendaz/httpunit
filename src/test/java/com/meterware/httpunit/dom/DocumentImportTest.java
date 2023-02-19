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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.*;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 */
class DocumentImportTest {

    private DocumentImpl _document;

    @BeforeEach
    void setUp() throws Exception {
        _document = DocumentImpl.createDocument();
    }


    /**
     * Verifies the importing of an attribute node with no children.
     *
     * @throws Exception thrown if an error occurs during the test.
     */
    @Test
    void testImportAttribute() throws Exception {
        Element element = _document.createElement("rainbow");
        Attr original = _document.createAttribute("color");
        element.setAttributeNode(original);
        original.setValue("red");

        Attr copy = (Attr) _document.importNode(original, false);
        assertEquals(Node.ATTRIBUTE_NODE, copy.getNodeType(), "Node type");
        assertEquals("color", copy.getNodeName(), "Node name");
        assertNull(copy.getOwnerElement(), "Should have removed the original element");
        assertEquals("red", copy.getNodeValue(), "Node value");
        assertTrue(copy.getSpecified(), "Node value should be specified");
    }


    /**
     * Verifies the importing of a text node.
     *
     * @throws Exception thrown if an error occurs during the test.
     */
    @Test
    void testImportText() throws Exception {
        String textValue = "something to say";
        Text original = _document.createTextNode(textValue);

        Text copy = (Text) _document.importNode(original, false);
        assertEquals(Node.TEXT_NODE, copy.getNodeType(), "Node type");
        assertEquals("#text", copy.getNodeName(), "Node name");
        assertEquals(textValue.length(), copy.getLength(), "length");
    }


    /**
     * Verifies the importing of a comment node.
     *
     * @throws Exception thrown if an error occurs during the test.
     */
    @Test
    void testImportComment() throws Exception {
        String commentText = "something to say";
        Comment original = _document.createComment(commentText);

        Comment copy = (Comment) _document.importNode(original, false);
        assertEquals(Node.COMMENT_NODE, copy.getNodeType(), "Node type");
        assertEquals("#comment", copy.getNodeName(), "Node name");
        assertEquals(commentText.length(), copy.getLength(), "length");
    }


    /**
     * Verifies the importing of a CData section.
     *
     * @throws Exception thrown if an error occurs during the test.
     */
    @Test
    void testImportCData() throws Exception {
        String cDataText = "something <to> say";
        CDATASection original = _document.createCDATASection(cDataText);

        CDATASection copy = (CDATASection) _document.importNode(original, false);
        assertEquals(Node.CDATA_SECTION_NODE, copy.getNodeType(), "Node type");
        assertEquals("#cdata-section", copy.getNodeName(), "Node name");
        assertEquals(cDataText.length(), copy.getLength(), "length");
        assertEquals(cDataText, copy.getNodeValue(), "value");
    }


    /**
     * Verifies the importing of a processing instruction.
     *
     * @throws Exception thrown if an error occurs during the test.
     */
    @Test
    void testImportProcessingInstruction() throws Exception {
        String target = "mememe";
        String data = "you you you";
        ProcessingInstruction original = _document.createProcessingInstruction(target, data);
        assertEquals(Node.PROCESSING_INSTRUCTION_NODE, original.getNodeType(), "Original node type");

        ProcessingInstruction copy = (ProcessingInstruction) _document.importNode(original, false);
        assertEquals(Node.PROCESSING_INSTRUCTION_NODE, copy.getNodeType(), "Node type");
        assertEquals(target, copy.getNodeName(), "Node name");
        assertEquals(data, copy.getNodeValue(), "value");
        assertEquals(target, copy.getTarget(), "target");
        assertEquals(data, copy.getData(), "data");
    }


    /**
     * Verifies the importing of a simple element with attributes.
     *
     * @throws Exception thrown if an error occurs during the test.
     */
    @Test
    void testImportElementWithAttributes() throws Exception {
        Element original = _document.createElement("zork");
        Attr size = _document.createAttribute("interactive");
        original.setAttribute("version", "2.0");
        original.setAttributeNode(size);

        Element copy = (Element) _document.importNode(original, /* deep */ false);
        assertEquals(Node.ELEMENT_NODE, copy.getNodeType(), "Node type");
        assertEquals("zork", copy.getNodeName(), "Node name");
        assertEquals("2.0", copy.getAttribute("version"), "version attribute");
        assertTrue(copy.hasAttribute("interactive"), "copy does not have interactive attribute");
    }


    /**
     * Verifies the importing of a simple element with attributes, both supporting namespaces.
     *
     * @throws Exception thrown if an error occurs during the test.
     */
    @Test
    void testImportNSElementWithNSAttributes() throws Exception {
        Element original = _document.createElementNS("http://funnyspace/", "fs:zork");
        original.setAttributeNS("http://funnyspace/", "fs:version", "2.0");
        Attr size = _document.createAttributeNS("http://funnyspace/", "fs:interactive");
        original.setAttributeNode(size);
        verifyNSElementWithNSAttributes("original", original);

        Element copy = (Element) _document.importNode(original, /* deep */ false);
        verifyNSElementWithNSAttributes("copy", copy);
    }


    private void verifyNSElementWithNSAttributes(String comment, Element element) {
        assertEquals(Node.ELEMENT_NODE, element.getNodeType(), comment + " node type");
        assertEquals("fs:zork", element.getNodeName(), comment + " node name");
        assertEquals("zork", element.getLocalName(), comment + " local name");
        assertEquals("http://funnyspace/", element.getNamespaceURI(), comment + " namespace URI");
        assertEquals("2.0", element.getAttribute("fs:version"), comment + " version attribute");
        assertTrue(element.hasAttribute("fs:interactive"), comment + " does not have interactive attribute");
    }


    /**
     * Verifies the shallow importing of an element with children.
     *
     * @throws Exception thrown if an error occurs during the test.
     */
    @Test
    void testShallowImportElementWithChildren() throws Exception {
        Element original = _document.createElement("zork");
        original.appendChild(_document.createElement("foo"));
        original.appendChild(_document.createElement("bar"));

        Element copy = (Element) _document.importNode(original, /* deep */ false);
        assertEquals(Node.ELEMENT_NODE, copy.getNodeType(), "Node type");
        assertEquals("zork", copy.getNodeName(), "Node name");
        assertFalse(copy.hasChildNodes(), "copy should have no children");
    }


    /**
     * Verifies the deep importing of an element with children.
     *
     * @throws Exception thrown if an error occurs during the test.
     */
    @Test
    void testDeepImportElementWithChildren() throws Exception {
        Element original = _document.createElement("zork");
        original.appendChild(_document.createElement("foo"));
        original.appendChild(_document.createTextNode("in the middle"));
        original.appendChild(_document.createElement("bar"));

        Element copy = (Element) _document.importNode(original, /* deep */ true);
        assertEquals(Node.ELEMENT_NODE, copy.getNodeType(), "Node type");
        assertEquals("zork", copy.getNodeName(), "Node name");
        assertTrue(copy.hasChildNodes(), "copy should have children");

        NodeList children = copy.getChildNodes();
        assertEquals(3, children.getLength(), "Number of child nodes");

        Node child = copy.getFirstChild();
        verifyNode("1st", child, Node.ELEMENT_NODE, "foo", null);
        child = child.getNextSibling();
        verifyNode("2nd", child, Node.TEXT_NODE, "#text", "in the middle");
        child = child.getNextSibling();
        verifyNode("3rd", child, Node.ELEMENT_NODE, "bar", null);
    }


    private void verifyNode(String comment, Node node, short type, String name, String value) {
        assertEquals(type, node.getNodeType(), comment + " node type");
        assertEquals(name, node.getNodeName(), comment + " node name");
        assertEquals(value, node.getNodeValue(), comment + " node value");
    }

}

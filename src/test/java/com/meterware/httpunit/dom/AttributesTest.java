/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.dom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * The Class AttributesTest.
 */
class AttributesTest {

    /** The document. */
    private DocumentImpl _document;

    /** The element. */
    private Element _element;

    /** The height attribute. */
    private Attr _heightAttribute;

    /** The weight attribute. */
    private Attr _weightAttribute;

    /**
     * Sets the up.
     *
     * @throws Exception
     *             the exception
     */
    @BeforeEach
    void setUp() throws Exception {
        _document = DocumentImpl.createDocument();
        _element = _document.createElement("zork");
        _heightAttribute = _document.createAttribute("height");
        _weightAttribute = _document.createAttribute("weight");
    }

    /**
     * Verifies that we can create an attributes node and verify it.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void attributeCreation() throws Exception {
        assertSame(_document, _heightAttribute.getOwnerDocument(), "Owner document");
        assertEquals("height", _heightAttribute.getName(), "Name");
        assertEquals("height", _heightAttribute.getNodeName(), "Node name");
        assertEquals(Node.ATTRIBUTE_NODE, _heightAttribute.getNodeType(), "Node type");
        assertNull(_heightAttribute.getAttributes(), "Attributes should not have attributes");
        assertEquals("", _heightAttribute.getNodeValue(), "Initial attribute value");
        assertEquals("", _heightAttribute.getValue(), "Initial Value");
        assertFalse(_heightAttribute.getSpecified(), "Should not be marked as specified");

        _heightAttribute.setNodeValue("an example");
        assertEquals("an example", _heightAttribute.getNodeValue(), "Node Value after nodevalue update");
        assertEquals("an example", _heightAttribute.getValue(), "Value after nodevalue update");
        _heightAttribute.setValue("another one");
        assertEquals("another one", _heightAttribute.getNodeValue(), "Node Value after value update");
        assertEquals("another one", _heightAttribute.getValue(), "Value after value update");
        assertTrue(_heightAttribute.getSpecified(), "Should now be marked as specified");
    }

    /**
     * Verifies that we can set unique attribute nodes on an element and retrieve and remove them.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void simpleAttrNodeAssignment() throws Exception {
        assertFalse(_element.hasAttributes(), "Element should report no attributes");
        assertNull(_heightAttribute.getOwnerElement(), "Onwer element should not be set before assignment");
        _element.setAttributeNode(_heightAttribute);
        _element.setAttributeNode(_weightAttribute);
        assertSame(_element, _heightAttribute.getOwnerElement(), "owner element");
        assertTrue(_element.hasAttributes(), "Element should acknowledge having attributes");

        NamedNodeMap attributes = _element.getAttributes();
        assertNotNull(attributes, "No attributes returned");
        assertAttributesInMap(attributes, new Attr[] { _heightAttribute, _weightAttribute });
        assertSame(_heightAttribute, _element.getAttributeNode("height"), "height attribute");
        assertSame(_weightAttribute, _element.getAttributeNode("weight"), "weight attribute");

        _element.removeAttributeNode(_heightAttribute);
        assertAttributesInMap(_element.getAttributes(), new Attr[] { _weightAttribute });
        assertNull(_element.getAttributeNode("height"), "height attribute should be gone");
        assertSame(_weightAttribute, _element.getAttributeNode("weight"), "weight attribute");

        assertNull(_heightAttribute.getOwnerElement(), "Onwer element should not be set after removal");
    }

    /**
     * Verifies that we cannot remove attribute nodes that are not defined.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void illegalAttributeNodeRemoval() throws Exception {
        _element.setAttributeNode(_heightAttribute);
        try {
            _element.removeAttributeNode(_weightAttribute);
            fail("Should have rejected attempt to remove unknown attribute node");
        } catch (DOMException e) {
            assertEquals(DOMException.NOT_FOUND_ERR, e.code, "Reason for failure");
        }
    }

    /**
     * Verifies that setting an attribute node removes any older matching attribute node.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void setReplacementAttributeNode() throws Exception {
        _element.setAttributeNode(_heightAttribute);
        Attr newHeight = _document.createAttribute("height");
        _element.setAttributeNode(newHeight);
        assertSame(_element, newHeight.getOwnerElement(), "owner element");
        assertNull(_heightAttribute.getOwnerElement(), "Onwer element should not be set after removal");

        assertAttributesInMap(_element.getAttributes(), new Attr[] { newHeight });
        assertSame(newHeight, _element.getAttributeNode("height"), "height attribute");
    }

    /**
     * Verifies that an undefined attribute is returned as an empty string.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void emptyAttribute() throws Exception {
        assertEquals("", _element.getAttribute("abcdef"), "Value for undefined attribute");
    }

    /**
     * Verifies that we can set and get attributes by value.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void simpleAttributes() throws Exception {
        _element.setAttribute("height", "3");
        _element.setAttribute("width", "really wide");
        assertAttributesInMap(_element.getAttributes(),
                new NVPair[] { new NVPair("height", "3"), new NVPair("width", "really wide") });
        assertTrue(_element.hasAttribute("height"), "Did not recognize height attribute");
        assertFalse(_element.hasAttribute("color"), "Should not have claimed attribute 'color' was present");
        assertEquals("really wide", _element.getAttribute("width"), "width attribute");

        _element.removeAttribute("height");
        assertFalse(_element.hasAttribute("height"), "Height attribute should be gone now");
    }

    /**
     * The Class NVPair.
     */
    static class NVPair {

        /** The name. */
        private String _name;

        /** The value. */
        private String _value;

        /**
         * Instantiates a new NV pair.
         *
         * @param name
         *            the name
         * @param value
         *            the value
         */
        public NVPair(String name, String value) {
            _name = name;
            _value = value;
        }

        /**
         * Gets the name.
         *
         * @return the name
         */
        public String getName() {
            return _name;
        }

        /**
         * Instantiates a new NV pair.
         *
         * @param attrNode
         *            the attr node
         */
        public NVPair(Attr attrNode) {
            this(attrNode.getName(), attrNode.getValue());
        }

        @Override
        public String toString() {
            return "NV Pair: {" + _name + "," + _value + "}";
        }

        @Override
        public boolean equals(Object obj) {
            return getClass().equals(obj.getClass()) && equals((NVPair) obj);
        }

        /**
         * Equals.
         *
         * @param obj
         *            the obj
         *
         * @return true, if successful
         */
        private boolean equals(NVPair obj) {
            return _name.equals(obj._name) && _value.equals(obj._value);
        }
    }

    /**
     * Assert attributes in map.
     *
     * @param attributes
     *            the attributes
     * @param expectedAttributes
     *            the expected attributes
     */
    private void assertAttributesInMap(NamedNodeMap attributes, NVPair[] expectedAttributes) {
        assertEquals(expectedAttributes.length, attributes.getLength(), "Number of known attribute nodes");

        List attributesMissing = new ArrayList<>();
        for (int i = 0; i < attributes.getLength(); i++) {
            attributesMissing.add(new NVPair((Attr) attributes.item(i)));
        }

        for (NVPair expectedAttribute : expectedAttributes) {
            assertTrue(attributesMissing.contains(expectedAttribute),
                    "Did not find attribute " + expectedAttribute + " in item sequence");
            assertEquals(expectedAttribute, new NVPair((Attr) attributes.getNamedItem(expectedAttribute.getName())),
                    "attribute named '" + expectedAttribute.getName() + "' in map");
            attributesMissing.remove(expectedAttribute);
        }
    }

    /**
     * Confirms that the map contains the expected attributes.
     *
     * @param attributes
     *            the attributes
     * @param expectedAttributes
     *            the expected attributes
     */
    private void assertAttributesInMap(NamedNodeMap attributes, Attr[] expectedAttributes) {
        assertEquals(expectedAttributes.length, attributes.getLength(), "Number of known attribute nodes");

        List attributesMissing = new ArrayList<>();
        for (int i = 0; i < attributes.getLength(); i++) {
            attributesMissing.add(attributes.item(i));
        }

        for (Attr expectedAttribute : expectedAttributes) {
            assertTrue(attributesMissing.contains(expectedAttribute),
                    "Did not find attribute " + expectedAttribute + " in item sequence");
            assertSame(expectedAttribute, attributes.getNamedItem(expectedAttribute.getName()),
                    "attribute named '" + expectedAttribute.getName() + "' in map");
            attributesMissing.remove(expectedAttribute);
        }
    }

}

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.w3c.dom.Element;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLOptionElement;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public abstract class AbstractHTMLElementTest implements DomListener {

    protected HTMLDocumentImpl _htmlDocument;
    private List _eventsReceived = new ArrayList<>();

    @BeforeEach
    public void setUpAbstractHTMLElementTest() throws Exception {
        _htmlDocument = new HTMLDocumentImpl();
    }

    protected void assertProperties(String comment, String name, HTMLElement[] elements, Object[] expectedValues)
            throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        for (int i = 0; i < elements.length; i++) {
            HTMLElement element = elements[i];
            assertEquals(expectedValues[i], AbstractHTMLElementTest.getProperty(element, name), comment + " " + i);
        }
    }

    protected HTMLOptionElement createOption(String value, String text, boolean selected) {
        HTMLOptionElement optionElement = (HTMLOptionElement) createElement("option",
                selected ? new String[][] { { "value", value }, { "selected", "true" } }
                        : new String[][] { { "value", value } });
        optionElement.appendChild(_htmlDocument.createTextNode(text));
        return optionElement;
    }

    /**
     * Performs a number of common tests for DOM elements. In the first test, the element is created with a set of
     * attribute values and those values are then verified. In the second test, the element is created without any
     * specified attributes and each attribute is compared with the default value. A write accessor is then sought for
     * each attribute, which should be present unless it is marked as read-only. Finally, each attribute is set to a
     * non-default value, which is then verified. A propertyChanged event should be thrown whenever the attribute is
     * changed. The attributes array has 2-4 values per entry:
     * <ol>
     * <li>the attribute name</li>
     * <li>a non-default value for the attribute</li>
     * <li>the default value for the attribute (defaults to null)</li>
     * <li>"ro" if the attribute is read-only (defaults to writeable)</li>
     * </ol>
     */
    protected void doElementTest(String tagName, Class interfaceName, Object[][] attributes)
            throws IntrospectionException, IllegalAccessException, InvocationTargetException {
        Element qualifiedElement = createElement(tagName, attributes);
        assertTrue(interfaceName.isAssignableFrom(qualifiedElement.getClass()),
                "node should be a " + interfaceName.getName() + " but is " + qualifiedElement.getClass().getName());
        assertEquals(tagName.toUpperCase(), qualifiedElement.getNodeName(), "Tag name");

        for (Object[] attribute : attributes) {
            String propertyName = (String) attribute[0];
            Object propertyValue = attribute[1];
            assertEquals(propertyValue, getProperty(qualifiedElement, propertyName), propertyName);
        }

        Element element = createElement(tagName);
        ((ElementImpl) element).addDomListener(this);
        for (Object[] attribute : attributes) {
            final String propertyName = (String) attribute[0];
            final Object propertyValue = attribute[1];
            Object defaultValue = attribute.length == 2 ? null : attribute[2];
            if (defaultValue == null) {
                assertNull(getProperty(element, propertyName), propertyName + " should not be specified by default");
            } else {
                assertEquals(defaultValue, getProperty(element, propertyName), "default " + propertyName);
            }

            Method writeMethod = AbstractHTMLElementTest.getWriteMethod(element, propertyName);
            if (attribute.length > 3 && attribute[3].equals("ro")) {
                assertNull(writeMethod, propertyName + " is not read-only");
            } else {
                assertNotNull("No modifier defined for " + propertyName);
                clearReceivedEvents();
                writeMethod.invoke(element, propertyValue);
                assertEquals(propertyValue, getProperty(element, propertyName), "modified " + propertyName);
                expectPropertyChange(element, propertyName);
            }
        }
    }

    protected Element createElement(String tagName) {
        return createElement(tagName, new String[0][]);
    }

    protected Element createElement(String tagName, Object[][] attributes) {
        Element element = _htmlDocument.createElement(tagName);
        for (Object[] attribute : attributes) {
            element.setAttribute((String) attribute[0], toAttributeValue(attribute[1]));
        }
        return element;
    }

    private static String toAttributeValue(Object value) {
        return value == null ? null : value.toString();
    }

    private static Object getProperty(Object element, final String propertyName)
            throws IntrospectionException, IllegalAccessException, InvocationTargetException {
        PropertyDescriptor descriptor = getPropertyDescriptor(element, propertyName);
        if (descriptor == null || descriptor.getReadMethod() == null) {
            return null;
        }
        Method readMethod = descriptor.getReadMethod();
        Object[] args = {};
        return readMethod.invoke(element, args);

    }

    private static Method getWriteMethod(Object element, final String propertyName) throws IntrospectionException {
        PropertyDescriptor descriptor = getPropertyDescriptor(element, propertyName);
        return descriptor == null ? null : descriptor.getWriteMethod();
    }

    private static PropertyDescriptor getPropertyDescriptor(Object element, String propertyName)
            throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(element.getClass(), Object.class);

        int index;
        while ((index = propertyName.indexOf('-')) >= 0) {
            propertyName = propertyName.substring(0, index) + Character.toUpperCase(propertyName.charAt(index + 1))
                    + propertyName.substring(index + 2);
        }
        if (element instanceof AttributeNameAdjusted) {
            propertyName = ((AttributeNameAdjusted) element).getJavaAttributeName(propertyName);
        }

        PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor property : properties) {
            if (property.getName().equalsIgnoreCase(propertyName)) {
                return property;
            }
        }
        return null;
    }

    protected void clearReceivedEvents() {
        _eventsReceived.clear();
    }

    protected void expectPropertyChange(Element element, String property) {
        assertFalse(_eventsReceived.isEmpty(),
                "Did not receive a property change event for " + element.getTagName() + "." + property);
    }

    // -------------------------------------- DomListener methods --------------------------------------------

    @Override
    public void propertyChanged(Element changedElement, String propertyName) {
        _eventsReceived.add(new Object[] { changedElement, propertyName });
    }

}

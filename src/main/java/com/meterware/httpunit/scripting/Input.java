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
package com.meterware.httpunit.scripting;

import java.io.IOException;

import org.xml.sax.SAXException;

/**
 * An interface for scriptable delegates which represent form controls.
 **/
public interface Input extends IdentifiedDelegate, NamedDelegate {

    /**
     * Gets the.
     *
     * @param propertyName
     *            the property name
     *
     * @return the object
     */
    Object get(String propertyName);

    /**
     * set the given property to the given value.
     *
     * @param propertyName
     *            the property name
     * @param value
     *            the value
     */
    void set(String propertyName, Object value);

    /**
     * set the given attribute to the given value.
     *
     * @param attributeName
     *            the attribute name
     * @param value
     *            the value
     */
    void setAttribute(String attributeName, Object value);

    /**
     * remove the given attribute.
     *
     * @param attributeName
     *            the attribute name
     */
    void removeAttribute(String attributeName);

    /**
     * simulate a click.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws SAXException
     *             the SAX exception
     */
    void click() throws IOException, SAXException;

    /**
     * fire a on change event.
     */
    void sendOnChangeEvent();

}

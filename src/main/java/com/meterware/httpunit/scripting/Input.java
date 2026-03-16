/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
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

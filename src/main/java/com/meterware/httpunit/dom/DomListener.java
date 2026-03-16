/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.dom;

import org.w3c.dom.Element;

/**
 * The listener interface for receiving dom events. The class that is interested in processing a dom event implements
 * this interface, and the object created with that class is registered with a component using the component's
 * <code>addDomListener</code> method. When the dom event occurs, that object's appropriate method is invoked.
 */
public interface DomListener {

    /**
     * Property changed.
     *
     * @param changedElement
     *            the changed element
     * @param propertyName
     *            the property name
     */
    void propertyChanged(Element changedElement, String propertyName);
}

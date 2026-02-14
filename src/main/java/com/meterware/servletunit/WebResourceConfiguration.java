/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.servletunit;

import java.util.Hashtable;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The Class WebResourceConfiguration.
 */
abstract class WebResourceConfiguration {

    /** The class name. */
    private String _className;

    /** The init params. */
    private Hashtable _initParams = new Hashtable<>();

    /**
     * Instantiates a new web resource configuration.
     *
     * @param className
     *            the class name
     */
    WebResourceConfiguration(String className) {
        _className = className;
    }

    /**
     * Instantiates a new web resource configuration.
     *
     * @param className
     *            the class name
     * @param initParams
     *            the init params
     */
    WebResourceConfiguration(String className, Hashtable initParams) {
        _className = className;
        if (initParams != null) {
            _initParams = initParams;
        }
    }

    /**
     * Instantiates a new web resource configuration.
     *
     * @param resourceElement
     *            the resource element
     * @param resourceNodeName
     *            the resource node name
     *
     * @throws SAXException
     *             the SAX exception
     */
    WebResourceConfiguration(Element resourceElement, String resourceNodeName) throws SAXException {
        this(resourceElement, resourceNodeName, XMLUtils.getChildNodeValue(resourceElement, resourceNodeName));
    }

    /**
     * construct a WebResourceConfiguration from the given parameters.
     *
     * @param resourceElement
     *            the resource element
     * @param resourceNodeName
     *            the resource node name
     * @param className
     *            the class name
     *
     * @throws SAXException
     *             the SAX exception
     */
    protected WebResourceConfiguration(Element resourceElement, String resourceNodeName, String className)
            throws SAXException {
        this(className);
        final NodeList initParams = resourceElement.getElementsByTagName("init-param");
        for (int i = initParams.getLength() - 1; i >= 0; i--) {
            _initParams.put(XMLUtils.getChildNodeValue((Element) initParams.item(i), "param-name"),
                    XMLUtils.getChildNodeValue((Element) initParams.item(i), "param-value"));
        }
    }

    /**
     * Destroy resource.
     */
    abstract void destroyResource();

    /**
     * Gets the class name.
     *
     * @return the class name
     */
    String getClassName() {
        return _className;
    }

    /**
     * Gets the inits the params.
     *
     * @return the inits the params
     */
    Hashtable getInitParams() {
        return _initParams;
    }

    /**
     * Checks if is load on startup.
     *
     * @return true, if is load on startup
     */
    abstract boolean isLoadOnStartup();

}

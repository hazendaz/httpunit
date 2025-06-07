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
package com.meterware.servletunit;

import java.util.Properties;

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
    private Properties _initParams = new Properties();

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
    WebResourceConfiguration(String className, Properties initParams) {
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
    Properties getInitParams() {
        return _initParams;
    }

    /**
     * Checks if is load on startup.
     *
     * @return true, if is load on startup
     */
    abstract boolean isLoadOnStartup();

}

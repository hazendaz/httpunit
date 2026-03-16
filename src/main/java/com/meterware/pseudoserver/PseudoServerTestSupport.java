/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.pseudoserver;

import java.io.IOException;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * helper class for JUnit Tests of httpunit.
 */
public class PseudoServerTestSupport implements BeforeEachCallback, AfterEachCallback {

    /** The host path. */
    private String _hostPath;

    /** The server. */
    private PseudoServer _server;

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        setUpServer();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        tearDownServer();
    }

    /**
     * Sets the up server.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void setUpServer() throws IOException {
        _server = new PseudoServer();
        _hostPath = "http://localhost:" + _server.getConnectedPort();
    }

    /**
     * Tear down server.
     */
    public void tearDownServer() {
        if (_server != null) {
            _server.shutDown();
        }
    }

    /**
     * Map to classpath.
     *
     * @param directory
     *            the directory
     */
    public void mapToClasspath(String directory) {
        _server.mapToClasspath(directory);
    }

    /**
     * Define resource.
     *
     * @param resourceName
     *            the resource name
     * @param servlet
     *            the servlet
     */
    public void defineResource(String resourceName, PseudoServlet servlet) {
        _server.setResource(resourceName, servlet);
    }

    /**
     * Define resource.
     *
     * @param resourceName
     *            the resource name
     * @param value
     *            the value
     */
    public void defineResource(String resourceName, String value) {
        _server.setResource(resourceName, value);
    }

    /**
     * Define resource.
     *
     * @param resourceName
     *            the resource name
     * @param value
     *            the value
     * @param contentType
     *            the content type
     */
    public void defineResource(String resourceName, byte[] value, String contentType) {
        _server.setResource(resourceName, value, contentType);
    }

    /**
     * Define resource.
     *
     * @param resourceName
     *            the resource name
     * @param value
     *            the value
     * @param statusCode
     *            the status code
     */
    public void defineResource(String resourceName, String value, int statusCode) {
        _server.setErrorResource(resourceName, statusCode, value);
    }

    /**
     * Define resource.
     *
     * @param resourceName
     *            the resource name
     * @param value
     *            the value
     * @param contentType
     *            the content type
     */
    public void defineResource(String resourceName, String value, String contentType) {
        _server.setResource(resourceName, value, contentType);
    }

    /**
     * Adds the resource header.
     *
     * @param resourceName
     *            the resource name
     * @param header
     *            the header
     */
    public void addResourceHeader(String resourceName, String header) {
        _server.addResourceHeader(resourceName, header);
    }

    /**
     * Sets the resource char set.
     *
     * @param resourceName
     *            the resource name
     * @param setName
     *            the set name
     * @param reportCharSet
     *            the report char set
     */
    public void setResourceCharSet(String resourceName, String setName, boolean reportCharSet) {
        _server.setCharacterSet(resourceName, setName);
        _server.setSendCharacterSet(resourceName, reportCharSet);
    }

    /**
     * define a Web Page with the given page name and boy adding the html and body tags with pageName as the title of
     * the page use the given xml names space if it is not null.
     *
     * @param xmlns
     *            the xmlns
     * @param pageName
     *            the page name
     * @param body
     *            the body
     */
    public void defineWebPage(String xmlns, String pageName, String body) {
        String preamble = "";
        if (xmlns == null) {
            xmlns = "";
        } else {
            preamble = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
            preamble += "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n";
            xmlns = " xmlns=\"" + xmlns + "\"";
        }
        defineResource(pageName + ".html", preamble + "<html" + xmlns + ">\n<head><title>" + pageName
                + "</title></head>\n" + "<body>\n" + body + "\n</body>\n</html>");
    }

    /**
     * define a Web Page with the given page name and boy adding the html and body tags with pageName as the title of
     * the page.
     *
     * @param pageName
     *            the page name
     * @param body
     *            the body
     */
    public void defineWebPage(String pageName, String body) {
        defineWebPage(null, pageName, body);
    }

    /**
     * Gets the server.
     *
     * @return the server
     */
    public PseudoServer getServer() {
        return _server;
    }

    /**
     * Sets the server debug.
     *
     * @param enabled
     *            the new server debug
     */
    public void setServerDebug(boolean enabled) {
        _server.setDebug(enabled);
    }

    /**
     * Gets the host path.
     *
     * @return the host path
     */
    public String getHostPath() {
        return _hostPath;
    }

    /**
     * Gets the host port.
     *
     * @return the host port
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public int getHostPort() throws IOException {
        return _server.getConnectedPort();
    }
}

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

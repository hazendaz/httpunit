/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.dom;

import com.meterware.httpunit.scripting.ScriptingHandler;

import java.io.IOException;
import java.net.URL;

import org.mozilla.javascript.Scriptable;
import org.w3c.dom.html.HTMLDocument;
import org.xml.sax.SAXException;

/**
 * The Class DomWindow.
 */
public class DomWindow extends AbstractDomComponent implements Scriptable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The proxy. */
    private DomWindowProxy _proxy;

    /** The document. */
    private HTMLDocumentImpl _document;

    /**
     * construct me from a document.
     *
     * @param document
     *            the document
     */
    public DomWindow(HTMLDocumentImpl document) {
        _document = document;
    }

    /**
     * Instantiates a new dom window.
     *
     * @param implementation
     *            the implementation
     */
    public DomWindow(DomWindowProxy implementation) {
        _proxy = implementation;
    }

    /**
     * Sets the proxy.
     *
     * @param proxy
     *            the new proxy
     */
    public void setProxy(DomWindowProxy proxy) {
        _proxy = proxy;
    }

    /**
     * Gets the window.
     *
     * @return the window
     */
    public DomWindow getWindow() {
        return this;
    }

    /**
     * Gets the self.
     *
     * @return the self
     */
    public DomWindow getSelf() {
        return this;
    }

    /**
     * Gets the document.
     *
     * @return the document
     */
    public HTMLDocument getDocument() {
        return _document;
    }

    /**
     * Returns the document associated with this window. Uses the same name as that used by elements in the DOM.
     *
     * @return the owner document
     */
    public HTMLDocument getOwnerDocument() {
        return _document;
    }

    /**
     * Opens a named window.
     *
     * @param urlString
     *            the location (relative to the current page) from which to populate the window.
     * @param name
     *            the name of the window.
     * @param features
     *            special features for the window.
     * @param replace
     *            if true, replaces the contents of an existing window.
     *
     * @return a new populated window object
     */
    public DomWindow open(String urlString, String name, String features, boolean replace) {
        try {
            if (_proxy == null) {
                throw new RuntimeException("DomWindow.open failed for '" + name + "' _proxy is null");
            }

            DomWindowProxy newWindow = _proxy.openNewWindow(name, urlString);
            if (newWindow == null) {
                // throw new RuntimeException("DomWindow.open failed for '"+name+"','"+urlString+"' openNewWindow
                // returned null");
                return null;
            }
            ScriptingHandler result = newWindow.getScriptingHandler();
            return (DomWindow) result;
        } catch (IOException | SAXException e) {
            return null;
        }
    }

    /**
     * Closes the current window. Has no effect if this "window" is actually a nested frame.
     */
    public void close() {
        _proxy.close();
    }

    /**
     * Displays an alert box with the specified message.
     *
     * @param message
     *            the message to display
     */
    public void alert(String message) {
        _proxy.alert(message);
    }

    /**
     * Displays a prompt, asking for a yes or no answer and returns the answer.
     *
     * @param prompt
     *            the prompt text to display
     *
     * @return true if the user answered 'yes'
     */
    public boolean confirm(String prompt) {
        return _proxy.confirm(prompt);
    }

    /**
     * Displays a promptand returns the user's textual reply, which could be the default reply.
     *
     * @param message
     *            the prompt text to display
     * @param defaultResponse
     *            the response to return if the user doesn't enter anything
     *
     * @return the reply selected by the user.
     */
    public String prompt(String message, String defaultResponse) {
        return _proxy.prompt(message, defaultResponse);
    }

    /**
     * Sets the timeout.
     *
     * @param timeout
     *            the new timeout
     */
    public void setTimeout(int timeout) {
    }

    /**
     * Focus.
     */
    public void focus() {
    }

    /**
     * Move to.
     *
     * @param x
     *            the x
     * @param y
     *            the y
     */
    public void moveTo(int x, int y) {
    }

    /**
     * Scroll to.
     *
     * @param x
     *            the x
     * @param y
     *            the y
     */
    public void scrollTo(int x, int y) {
    }

    @Override
    protected String getDocumentWriteBuffer() {
        return _document.getWriteBuffer().toString();
    }

    @Override
    protected void discardDocumentWriteBuffer() {
        _document.clearWriteBuffer();
    }

    /**
     * Replace text.
     *
     * @param string
     *            the string
     * @param mimeType
     *            the mime type
     *
     * @return true, if successful
     */
    boolean replaceText(String string, String mimeType) {
        return _proxy.replaceText(string, mimeType);
    }

    /**
     * Gets the url.
     *
     * @return the url
     */
    URL getUrl() {
        return _proxy.getURL();
    }

    /**
     * Submit request.
     *
     * @param sourceElement
     *            the source element
     * @param method
     *            the method
     * @param location
     *            the location
     * @param target
     *            the target
     * @param requestBody
     *            the request body
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws SAXException
     *             the SAX exception
     */
    void submitRequest(HTMLElementImpl sourceElement, String method, String location, String target, byte[] requestBody)
            throws IOException, SAXException {
        _proxy.submitRequest(sourceElement, method, location, target, null);
    }
}

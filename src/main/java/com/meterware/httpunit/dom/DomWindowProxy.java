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

import com.meterware.httpunit.protocol.MessageBody;
import com.meterware.httpunit.scripting.ScriptingHandler;

import java.io.IOException;
import java.net.URL;

import org.xml.sax.SAXException;

/**
 * The Interface DomWindowProxy.
 */
public interface DomWindowProxy {

    /**
     * Open new window.
     *
     * @param name
     *            the name
     * @param relativeUrl
     *            the relative url
     *
     * @return the dom window proxy
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws SAXException
     *             the SAX exception
     */
    DomWindowProxy openNewWindow(String name, String relativeUrl) throws IOException, SAXException;

    /**
     * Gets the scripting handler.
     *
     * @return the scripting handler
     */
    ScriptingHandler getScriptingHandler();

    /**
     * Close.
     */
    void close();

    /**
     * Alert.
     *
     * @param message
     *            the message
     */
    void alert(String message);

    /**
     * Confirm.
     *
     * @param message
     *            the message
     *
     * @return true, if successful
     */
    boolean confirm(String message);

    /**
     * Prompt.
     *
     * @param prompt
     *            the prompt
     * @param defaultResponse
     *            the default response
     *
     * @return the string
     */
    String prompt(String prompt, String defaultResponse);

    /**
     * Returns the URL associated with the window.
     *
     * @return the URL associated with the window.
     */
    URL getURL();

    /**
     * Replaces the text in the window with the specified text and content type. Returns false if unable to do the
     * replacement.
     *
     * @param text
     *            the text
     * @param contentType
     *            the content type
     *
     * @return true, if successful
     */
    boolean replaceText(String text, String contentType);

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
     * @return the dom window proxy
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws SAXException
     *             the SAX exception
     */
    DomWindowProxy submitRequest(HTMLElementImpl sourceElement, String method, String location, String target,
            MessageBody requestBody) throws IOException, SAXException;
}

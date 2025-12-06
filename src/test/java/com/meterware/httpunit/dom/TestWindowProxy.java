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

import com.meterware.httpunit.protocol.MessageBody;
import com.meterware.httpunit.scripting.ScriptingHandler;

import java.io.IOException;
import java.net.URL;
import java.util.Stack;

import org.junit.jupiter.api.Disabled;
import org.xml.sax.SAXException;

/**
 * The Class TestWindowProxy.
 */
@Disabled
class TestWindowProxy implements DomWindowProxy {

    /** The proxy calls. */
    private static Stack _proxyCalls = new Stack();

    /** The document. */
    private HTMLDocumentImpl _document;

    /** The url. */
    private URL _url;

    /** The replacement text. */
    private String _replacementText = null;

    /** The answer. */
    private String _answer;

    /**
     * Instantiates a new test window proxy.
     *
     * @param htmlDocument
     *            the html document
     */
    public TestWindowProxy(HTMLDocumentImpl htmlDocument) {
        _document = htmlDocument;
        _document.getWindow().setProxy(this);
    }

    /**
     * Clear proxy calls.
     */
    static void clearProxyCalls() {
        _proxyCalls.clear();
    }

    /**
     * Pop proxy call.
     *
     * @return the string
     */
    static String popProxyCall() {
        if (_proxyCalls.isEmpty()) {
            return "";
        }
        return (String) _proxyCalls.pop();
    }

    /**
     * Push proxy call.
     *
     * @param call
     *            the call
     */
    static void pushProxyCall(String call) {
        _proxyCalls.push(call);
    }

    /**
     * Assert last proxy method.
     *
     * @param method
     *            the method
     */
    static void assertLastProxyMethod(String method) {
        assertEquals(method, popProxyCall(), "Last proxy method called");
    }

    /**
     * Sets the answer.
     *
     * @param answer
     *            the new answer
     */
    void setAnswer(String answer) {
        _answer = answer;
    }

    /**
     * Gets the replacement text.
     *
     * @return the replacement text
     */
    String getReplacementText() {
        return _replacementText;
    }

    @Override
    public ScriptingHandler getScriptingHandler() {
        return _document.getWindow();
    }

    @Override
    public DomWindowProxy openNewWindow(String name, String relativeUrl) throws IOException, SAXException {
        HTMLDocumentImpl document = new HTMLDocumentImpl();
        document.setTitle(name + " (" + relativeUrl + ')');
        return new TestWindowProxy(document);
    }

    @Override
    public void close() {
        pushProxyCall("close");
    }

    @Override
    public void alert(String message) {
        pushProxyCall("alert( " + message + " )");
    }

    @Override
    public boolean confirm(String message) {
        pushProxyCall("confirm( " + message + " )");
        return _answer.equals("yes");
    }

    @Override
    public String prompt(String prompt, String defaultResponse) {
        pushProxyCall("prompt( " + prompt + " )");
        return _answer == null ? defaultResponse : _answer;
    }

    @Override
    public boolean replaceText(String text, String contentType) {
        _replacementText = text;
        return true;
    }

    /**
     * Sets the url.
     *
     * @param url
     *            the new url
     */
    void setUrl(URL url) {
        _url = url;
    }

    @Override
    public URL getURL() {
        return _url;
    }

    @Override
    public DomWindowProxy submitRequest(HTMLElementImpl sourceElement, String method, String location, String target,
            MessageBody requestBody) throws IOException, SAXException {
        pushProxyCall("submitRequest( " + method + ", " + location + ", " + target + ", "
                + stringifyMessageBody(requestBody) + " )");
        return null;
    }

    /**
     * Stringify message body.
     *
     * @param requestBody
     *            the request body
     *
     * @return the string
     */
    private String stringifyMessageBody(MessageBody requestBody) {
        if (requestBody == null) {
            return "null";
        }
        return "something";
    }
}

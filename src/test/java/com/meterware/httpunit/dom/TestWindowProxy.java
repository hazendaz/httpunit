/*
 * MIT License
 *
 * Copyright 2011-2023 Russell Gold
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
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 */
@Disabled
class TestWindowProxy implements DomWindowProxy {

    private static Stack _proxyCalls = new Stack();
    private HTMLDocumentImpl _document;
    private URL _url;
    private String _replacementText = null;
    private String _answer;

    public TestWindowProxy(HTMLDocumentImpl htmlDocument) {
        _document = htmlDocument;
        _document.getWindow().setProxy(this);
    }

    static void clearProxyCalls() {
        _proxyCalls.clear();
    }

    static String popProxyCall() {
        if (_proxyCalls.isEmpty()) {
            return "";
        }
        return (String) _proxyCalls.pop();
    }

    static void pushProxyCall(String call) {
        _proxyCalls.push(call);
    }

    static void assertLastProxyMethod(String method) {
        assertEquals(method, popProxyCall(), "Last proxy method called");
    }

    void setAnswer(String answer) {
        _answer = answer;
    }

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

    private String stringifyMessageBody(MessageBody requestBody) {
        if (requestBody == null) {
            return "null";
        }
        return "something";
    }
}

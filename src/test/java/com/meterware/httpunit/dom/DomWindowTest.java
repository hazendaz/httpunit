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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;

import org.junit.jupiter.api.Test;
import org.w3c.dom.html.HTMLDocument;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 */
class DomWindowTest extends AbstractHTMLElementTest {

    private TestWindowProxy _proxy;

    /**
     * Verifies that we can obtain a window for a document and then retrieve document for that window.
     */
    @Test
    void documentWindowAccess() throws Exception {
        DomWindow window = _htmlDocument.getWindow();
        assertSame(_htmlDocument, window.getDocument(), "The original document");
        assertSame(window, _htmlDocument.getWindow(), "The window upon subsequence request");
        assertSame(window, window.getWindow(), "The window accessed from itself");
        assertSame(window, window.getSelf(), "The window's 'self' object");
    }

    /**
     * Verifies that the open method returns a window with an appropriate document.
     */
    @Test
    void windowOpen() throws Exception {
        DomWindow window1 = createMainWindow();
        DomWindow window2 = window1.open("next.html", "broken", "", false);
        HTMLDocument document = window2.getDocument();
        assertNotNull(document, "Window has no associated document");
        assertEquals("broken (next.html)", document.getTitle(), "Title of document in new window");

        window2.close();
        TestWindowProxy.assertLastProxyMethod("close");
    }

    /**
     * Writes to a document should appear in the window's document write buffer.
     */
    @Test
    void documentWrite() throws Exception {
        DomWindow window1 = createMainWindow();
        window1.getDocument().write("A simple string");
        assertEquals("A simple string", window1.getDocumentWriteBuffer(), "Contents of write buffer");
        window1.discardDocumentWriteBuffer();
        assertEquals("", window1.getDocumentWriteBuffer(), "Contents of cleared write buffer");

    }

    private DomWindow createMainWindow() {
        DomWindow window = _htmlDocument.getWindow();
        _proxy = new TestWindowProxy(_htmlDocument);
        window.setProxy(_proxy);
        TestWindowProxy.clearProxyCalls();
        return window;
    }

    /**
     * Verifies that an alert request is sent to the proxy appropriately.
     */
    @Test
    void alert() throws Exception {
        assertDoesNotThrow(() -> {
            DomWindow window = createMainWindow();
            window.alert("A little message");
            TestWindowProxy.assertLastProxyMethod("alert( A little message )");
        });
    }

    /**
     * Verifies that a confirmation request is sent to the proxy and the appropriate answer is returned.
     */
    @Test
    void confirm() throws Exception {
        DomWindow window = createMainWindow();
        _proxy.setAnswer("no");
        assertFalse(window.confirm("Time to quit?"), "Should have said no");
        TestWindowProxy.assertLastProxyMethod("confirm( Time to quit? )");
        _proxy.setAnswer("yes");
        assertTrue(window.confirm("Want to stay?"), "Should have said yes");
        TestWindowProxy.assertLastProxyMethod("confirm( Want to stay? )");
    }

    /**
     * Verifies that a prompt is sent to the proxy and the appropriate answer is returned.
     */
    @Test
    void prompt() throws Exception {
        DomWindow window = createMainWindow();
        _proxy.setAnswer(null);
        assertEquals("0", window.prompt("How many choices?", "0"), "User default choice");
        TestWindowProxy.assertLastProxyMethod("prompt( How many choices? )");
        _proxy.setAnswer("blue");
        assertEquals("blue", window.prompt("What is your favorite color?", "yellow"), "Explicit user choice");
        TestWindowProxy.assertLastProxyMethod("prompt( What is your favorite color? )");
    }

    /**
     * Verifies that writing to and closing a document triggers a replaceText request.
     */
    @Test
    void textReplacement() throws Exception {
        DomWindow window = createMainWindow();
        window.getDocument().write("A bit of text");
        window.getDocument().close();
        assertNotNull(_proxy.getReplacementText(), "No text replacement occurred");
        assertEquals("A bit of text", _proxy.getReplacementText(), "Replacement text");
    }

    /**
     * Verifies that the window can report its URL, which it obtains via its prozy.
     */
    @Test
    void windowUrl() throws Exception {
        DomWindow window = createMainWindow();
        _proxy.setUrl(new URL("http://localhost"));
        assertEquals(new URL("http://localhost"), window.getUrl(), "Window url");
    }

    // todo test getNavigator
    // todo test getScreen
    // todo test getLocation, setLocation
    // todo test getFrames
    // todo test clearCaches, getDocumentWriteBuffer, clearDocumentWriteBuffer

    /**
     * Verifies simply the existence of some methods not currently implemented. Todo make them do something useful.
     */
    @Test
    void methodExistences() throws Exception {
        assertDoesNotThrow(() -> {
            DomWindow window = _htmlDocument.getWindow();
            window.setTimeout(40);
            window.focus();
            window.moveTo(10, 20);
            window.scrollTo(10, 20);
        });
    }

}

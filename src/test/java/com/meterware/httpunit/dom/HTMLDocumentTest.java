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

import static org.junit.jupiter.api.Assertions.*;

import com.meterware.pseudoserver.HttpUserAgentTest;

import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.html.*;

/**
 * Test for HTMLDocumentImpl.
 */
class HTMLDocumentTest extends AbstractHTMLElementTest {

    private Element _headElement;
    private Element _htmlElement;

    @BeforeEach
    void setUp() throws Exception {
        _htmlDocument.appendChild(_htmlElement = createElement("html"));
        _htmlElement.appendChild(_headElement = createElement("head"));
    }

    /**
     * Verifies that we can detect the lack of a document title.
     */
    @Test
    void testEmptyDocumentTitle() throws Exception {
        assertEquals("", _htmlDocument.getTitle(), "title seen by document");
    }

    /**
     * Verifies that we can find the document title.
     */
    @Test
    void testReadDocumentTitle() throws Exception {
        Element title = createElement("title");
        Text text = _htmlDocument.createTextNode("something here");
        title.appendChild(text);

        _headElement.appendChild(title);

        assertEquals("something here", _htmlDocument.getTitle(), "title seen by document");
    }

    /**
     * Verifies that we can modify an existing document title.
     */
    @Test
    void testModifyDocumentTitle() throws Exception {
        Element title = createElement("title");
        Text text = _htmlDocument.createTextNode("something here");
        title.appendChild(text);

        _headElement.appendChild(title);

        _htmlDocument.setTitle("new value");
        assertEquals("new value", _htmlDocument.getTitle(), "title seen by document");
    }

    /**
     * Verifies that we can set the document title if none exists.
     */
    @Test
    void testCreateDocumentTitle() throws Exception {
        _htmlDocument.setTitle("initial value");
        assertEquals("initial value", _htmlDocument.getTitle(), "title seen by document");
    }

    /**
     * Verifies retrieval of the body element.
     */
    @Test
    void testGetBody() throws Exception {
        Element body = createElement("body");
        _htmlElement.appendChild(body);

        assertSame(body, _htmlDocument.getBody(), "Body element");
    }

    /**
     * Verifies setting the body element.
     */
    @Test
    void testSetBody() throws Exception {
        HTMLElement body = (HTMLElement) createElement("body");
        _htmlDocument.setBody(body);

        assertSame(body, _htmlDocument.getBody(), "Body element");
    }

    /**
     * Verifies retrieving elements by their ID attribute.
     */
    @Test
    void testGetElementsById() throws Exception {
        HTMLElement body = (HTMLElement) createElement("body");
        _htmlDocument.setBody(body);
        body.setId("abc");

        HTMLAnchorElement anchor1 = (HTMLAnchorElement) createElement("a");
        anchor1.setHref("first");
        anchor1.setId("sea");
        body.appendChild(anchor1);

        HTMLImageElement image1 = (HTMLImageElement) createElement("img");
        image1.setId("see");
        body.appendChild(image1);

        assertSame(body, _htmlDocument.getElementById("abc"), "Body element");
        assertSame(anchor1, _htmlDocument.getElementById("sea"), "Anchor element");
        assertSame(image1, _htmlDocument.getElementById("see"), "Image element");
    }

    /**
     * Verifies retrieving elements by their name attribute.
     */
    @Test
    void testGetElementsByName() throws Exception {
        HTMLElement body = (HTMLElement) createElement("body");
        _htmlDocument.setBody(body);

        HTMLAnchorElement anchor1 = (HTMLAnchorElement) createElement("a");
        anchor1.setHref("first");
        anchor1.setName("see");
        body.appendChild(anchor1);

        HTMLImageElement image1 = (HTMLImageElement) createElement("img");
        image1.setName("see");
        body.appendChild(image1);

        assertElementsByName(_htmlDocument, "see", new HTMLElement[] { anchor1, image1 });
        assertElementsByName(_htmlDocument, "abc", new HTMLElement[0]);
    }

    private void assertElementsByName(HTMLDocument document, String name, HTMLElement[] expectedElements) {
        NodeList actualElements = document.getElementsByName(name);
        HttpUserAgentTest.assertMatchingSet("Elements with name '" + name + "'", expectedElements,
                toArray(actualElements));
    }

    private Object[] toArray(NodeList list) {
        Object[] result = new Object[list.getLength()];
        for (int i = 0; i < result.length; i++) {
            result[i] = list.item(i);
        }
        return result;
    }

    /**
     * Verifies retrieval of the collection of links ('img' tags).
     */
    @Test
    void testGetImages() throws Exception {
        HTMLElement body = (HTMLElement) createElement("body");
        _htmlDocument.setBody(body);

        HTMLAnchorElement anchor1 = (HTMLAnchorElement) createElement("a");
        anchor1.setHref("first");
        body.appendChild(anchor1);

        HTMLImageElement image1 = (HTMLImageElement) createElement("img");
        body.appendChild(image1);

        HTMLImageElement image2 = (HTMLImageElement) createElement("img");
        image2.setName("ship");
        body.appendChild(image2);

        HTMLAreaElement area1 = (HTMLAreaElement) createElement("area");
        body.appendChild(area1);

        HTMLCollection images = _htmlDocument.getImages();
        assertNotNull(images, "Did not get the image collection");
        assertEquals(2, images.getLength(), "Number of images");
        assertSame(image1, images.item(0), "image 1");
        assertSame(image2, images.item(1), "image 2");
    }

    /**
     * Verifies retrieval of the collection of links ('area' tags and 'a' tags with 'href' attributes).
     */
    @Test
    void testGetLinks() throws Exception {
        HTMLElement body = (HTMLElement) createElement("body");
        _htmlDocument.setBody(body);

        HTMLAnchorElement anchor1 = (HTMLAnchorElement) createElement("a");
        anchor1.setHref("first");
        body.appendChild(anchor1);

        HTMLAreaElement area1 = (HTMLAreaElement) createElement("area");
        area1.setHref("area");
        body.appendChild(area1);

        HTMLIFrameElement iframe = (HTMLIFrameElement) createElement("iframe");
        body.appendChild(iframe);
        HTMLAnchorElement hiddenAnchor = (HTMLAnchorElement) createElement("a");
        hiddenAnchor.setHref("hidden");
        iframe.appendChild(hiddenAnchor);

        HTMLAnchorElement anchor2 = (HTMLAnchorElement) createElement("a");
        anchor2.setHref("tent");
        body.appendChild(anchor2);

        HTMLAnchorElement anchor3 = (HTMLAnchorElement) createElement("a");
        anchor3.setName("ship");
        body.appendChild(anchor3);

        HTMLCollection links = _htmlDocument.getLinks();
        assertNotNull(links, "Did not get the links collection");
        assertEquals(3, links.getLength(), "Number of links");
        assertSame(anchor1, links.item(0), "link 1");
        assertSame(area1, links.item(1), "link 2");
        assertSame(anchor2, links.item(2), "link 3");
    }

    /**
     * Verifies retrieval of the collection of forms.
     */
    @Test
    void testGetForms() throws Exception {
        HTMLElement body = (HTMLElement) createElement("body");
        _htmlDocument.setBody(body);

        HTMLFormElement form1 = (HTMLFormElement) createElement("form");
        form1.setId("left");
        body.appendChild(form1);

        HTMLIFrameElement iframe = (HTMLIFrameElement) createElement("iframe");
        body.appendChild(iframe);
        HTMLFormElement hiddenForm = (HTMLFormElement) createElement("form");
        hiddenForm.setId("hidden");
        iframe.appendChild(hiddenForm);

        HTMLFormElement form2 = (HTMLFormElement) createElement("form");
        form2.setName("right");
        body.appendChild(form2);

        HTMLCollection forms = _htmlDocument.getForms();
        assertNotNull(forms, "Did not get the forms collection");
        assertEquals(2, forms.getLength(), "Number of forms");
        assertSame(form1, forms.item(0), "form 1");
        assertSame(form2, forms.item(1), "form 2");

        assertSame(form1, forms.namedItem("left"), "form 1 by id");
        assertSame(form2, forms.namedItem("right"), "form 2 by name");

        _htmlDocument.setIFramesEnabled(false);
        assertEquals(3, _htmlDocument.getForms().getLength(), "Forms found with iframes disabled");
    }

    /**
     * Verifies retrieval of the collection of anchors.
     */
    @Test
    void testGetAnchors() throws Exception {
        HTMLElement body = (HTMLElement) createElement("body");
        _htmlDocument.setBody(body);

        HTMLAnchorElement anchor1 = (HTMLAnchorElement) createElement("a");
        anchor1.setName("boat");
        body.appendChild(anchor1);

        HTMLAnchorElement anchor2 = (HTMLAnchorElement) createElement("a");
        anchor2.setId("tent");
        body.appendChild(anchor2);

        HTMLAnchorElement anchor3 = (HTMLAnchorElement) createElement("a");
        anchor3.setName("ship");
        anchor3.setHref("..");
        body.appendChild(anchor3);

        HTMLCollection anchors = _htmlDocument.getAnchors();
        assertNotNull(anchors, "Did not get the anchor collection");
        assertEquals(2, anchors.getLength(), "Number of anchors");
        assertSame(anchor1, anchors.item(0), "anchor 1");
        assertSame(anchor3, anchors.item(1), "anchor 3");
    }

    /**
     * Verifies that the document has an empty write buffer by default.
     */
    @Test
    void testInitialWriteBuffer() throws Exception {
        assertNotNull(_htmlDocument.getWriteBuffer(), "No write buffer was defined for the document");
        assertEquals(0, _htmlDocument.getWriteBuffer().length(), "Default buffer size");
    }

    /**
     * Verifies that writing to the document updates the write buffer.
     */
    @Test
    void testWriteBufferUpdate() throws Exception {
        _htmlDocument.write("This is a test");
        assertNotNull(_htmlDocument.getWriteBuffer(), "No write buffer was defined for the document");
        assertEquals("This is a test", _htmlDocument.getWriteBuffer().toString(), "Result of write buffer");
    }

    /**
     * Verifies that writing to the document updates the write buffer.
     */
    @Test
    void testWritelnBufferUpdate() throws Exception {
        _htmlDocument.writeln("This is a test");
        _htmlDocument.writeln("And another.");
        assertNotNull(_htmlDocument.getWriteBuffer(), "No write buffer was defined for the document");
        assertEquals("This is a test\r\nAnd another.\r\n", _htmlDocument.getWriteBuffer().toString(),
                "Result of write buffer");
    }

    /**
     * Verifies that clearing the write buffer leaves it ready for new writes.
     */
    @Test
    void testBufferClear() throws Exception {
        _htmlDocument.write("This is a test");
        _htmlDocument.clearWriteBuffer();
        assertEquals(0, _htmlDocument.getWriteBuffer().length(), "Cleared buffer length");
        _htmlDocument.write("And another.");
        assertNotNull(_htmlDocument.getWriteBuffer(), "No write buffer was defined for the document");
        assertEquals("And another.", _htmlDocument.getWriteBuffer().toString(), "Result of write buffer");
    }

    /**
     * Verifies that the href of a link will be based on the URL of the enclosing window.
     */
    @Test
    void testLinkHref() throws Exception {
        TestWindowProxy proxy = new TestWindowProxy(_htmlDocument);
        _htmlDocument.getWindow().setProxy(proxy);
        _htmlDocument.setBody((HTMLElement) _htmlDocument.createElement("body"));
        HTMLAnchorElementImpl link = (HTMLAnchorElementImpl) _htmlDocument.createElement("a");
        link.setAttribute("href", "main.html");
        proxy.setUrl(new URL("http://localhost/aux.html"));
        assertEquals("http://localhost/main.html", link.getHref(), "referenced URL");
    }

    /**
     * Verifies that the href of a link will be based on the base URL of the enclosing window, if there is one.
     */
    @Test
    void testLinkHrefUsingBase() throws Exception {
        TestWindowProxy proxy = new TestWindowProxy(_htmlDocument);
        _htmlDocument.getWindow().setProxy(proxy);
        HTMLBaseElement baseElement = (HTMLBaseElement) _htmlDocument.createElement("base");
        _headElement.appendChild(baseElement);
        baseElement.setHref("http://meterware.com/httpunit/");
        _htmlDocument.setBody((HTMLElement) _htmlDocument.createElement("body"));
        HTMLAnchorElementImpl link = (HTMLAnchorElementImpl) _htmlDocument.createElement("a");
        link.setAttribute("href", "main.html");
        proxy.setUrl(new URL("http://localhost/aux.html"));
        assertEquals("http://meterware.com/httpunit/main.html", link.getHref(), "referenced URL");
    }

    /**
     * Verifies that the href of a javascript link does not use the enclosing window URL.
     */
    @Test
    void testJavascriptLinkHref() throws Exception {
        TestWindowProxy proxy = new TestWindowProxy(_htmlDocument);
        _htmlDocument.getWindow().setProxy(proxy);
        _htmlDocument.setBody((HTMLElement) _htmlDocument.createElement("body"));
        HTMLAnchorElementImpl link = (HTMLAnchorElementImpl) _htmlDocument.createElement("a");
        link.setAttribute("href", "javascript:doSomething(123)");
        proxy.setUrl(new URL("http://localhost/aux.html"));
        assertEquals("javascript:doSomething(123)", link.getHref(), "referenced URL");
    }

    /**
     * Verifies that a click on an href link will send a request for the referenced page.
     */
    @Test
    void testClickOnLink() throws Exception {
        TestWindowProxy proxy = new TestWindowProxy(_htmlDocument);
        _htmlDocument.getWindow().setProxy(proxy);
        _htmlDocument.setBody((HTMLElement) _htmlDocument.createElement("body"));
        HTMLAnchorElementImpl link = (HTMLAnchorElementImpl) _htmlDocument.createElement("a");
        link.setAttribute("href", "main.html");
        link.setAttribute("target", "there");
        proxy.setUrl(new URL("http://localhost/aux.html"));
        link.click();
        assertEquals("submitRequest( GET, http://localhost/main.html, there, null )", TestWindowProxy.popProxyCall(),
                "method invocation");
    }
}

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

import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.html.*;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 */
class HTMLElementTest extends AbstractHTMLElementTest {

    @Test
    void testCaseInsensitiveTagSearch() throws Exception {
        Element element = createElement("body");
        Node form = element.appendChild(createElement("form"));
        NodeList nl = element.getElementsByTagName("form");
        assertEquals(1, nl.getLength(), "# form nodes to find");
        assertSame(form, nl.item(0), "Found form node");
    }

    @Test
    void testBaseElementDefaults() throws Exception {
        Element element = createElement("b", new String[0][]);
        assertTrue(element instanceof HTMLElement, "node should be an HTMLElement but is " + element.getClass());
        assertEquals("B", element.getNodeName(), "Tag name");

        HTMLElement htmlElement = (HTMLElement) element;
        assertNull(htmlElement.getClassName(), "class name should not be specified by default");
        assertNull(htmlElement.getDir(), "direction should not be specified by default");
        assertNull(htmlElement.getId(), "id should not be specified by default");
        assertNull(htmlElement.getLang(), "lang should not be specified by default");
        assertNull(htmlElement.getTitle(), "title should not be specified by default");
    }

    /**
     * test base element attributes
     *
     * @throws Exception
     */
    @Test
    void testBaseElementAttributes() throws Exception {
        Element element = createElement("code", new String[][] { { "class", "special" }, { "dir", "rtl" },
                { "id", "sample" }, { "lang", "hb" }, { "title", "psalm 83" } });
        assertTrue(element instanceof HTMLElement, "node should be an HTMLElement but is " + element.getClass());
        assertEquals("CODE", element.getNodeName(), "Tag name");

        HTMLElement htmlElement = (HTMLElement) element;
        assertEquals("special", htmlElement.getClassName(), "class name");
        assertEquals("rtl", htmlElement.getDir(), "direction");
        assertEquals("sample", htmlElement.getId(), "id");
        assertEquals("hb", htmlElement.getLang(), "lang");
        assertEquals("psalm 83", htmlElement.getTitle(), "title");
    }

    @Test
    void testWriteableElementAttributes() throws Exception {
        Element element = createElement("cite", new String[0][]);
        assertTrue(element instanceof HTMLElement, "node should be an HTMLElement but is " + element.getClass());
        assertEquals("CITE", element.getNodeName(), "Tag name");

        HTMLElement htmlElement = (HTMLElement) element;
        htmlElement.setClassName("special");
        htmlElement.setDir("rtl");
        htmlElement.setId("sample");
        htmlElement.setLang("hb");
        htmlElement.setTitle("psalm 83");

        assertEquals("special", htmlElement.getClassName(), "class name");
        assertEquals("rtl", htmlElement.getDir(), "direction");
        assertEquals("sample", htmlElement.getId(), "id");
        assertEquals("hb", htmlElement.getLang(), "lang");
        assertEquals("psalm 83", htmlElement.getTitle(), "title");
    }

    @Test
    void testEmptyFormDefaults() throws Exception {
        Element element = createElement("form", new String[][] { { "action", "go_here" } });
        assertTrue(element instanceof HTMLFormElement,
                "node should be an HTMLFormElement but is " + element.getClass());
        assertEquals("FORM", element.getNodeName(), "Tag name");

        HTMLFormElement form = (HTMLFormElement) element;
        assertEquals("UNKNOWN", form.getAcceptCharset(), "default character set");
        assertEquals("go_here", form.getAction(), "specified action");
        assertEquals(0, form.getElements().getLength(), "number of controls in collection");
        assertEquals("application/x-www-form-urlencoded", form.getEnctype(), "default form encoding");
        assertEquals(0, form.getLength(), "number of controls in form");
        assertEquals("GET", form.getMethod().toUpperCase(), "default method");
        assertNull(form.getName(), "form name should not be specified by default");
        assertNull(form.getTarget(), "default target is not null");
    }

    @Test
    void testFormAttributes() throws Exception {
        Element element = createElement("form",
                new String[][] { { "accept-charset", "latin-1" }, { "enctype", "multipart/form-data" },
                        { "method", "post" }, { "name", "aform" }, { "target", "green" } });
        HTMLFormElement form = (HTMLFormElement) element;
        assertEquals("latin-1", form.getAcceptCharset(), "character set");
        assertEquals("multipart/form-data", form.getEnctype(), "form encoding");
        assertEquals("post", form.getMethod(), "method");
        assertEquals("aform", form.getName(), "form name");
        assertEquals("green", form.getTarget(), "target");
    }

    @Test
    void testWriteableFormAttributes() throws Exception {
        Element element = createElement("form", new String[][] { { "action", "go_here" } });
        HTMLFormElement form = (HTMLFormElement) element;

        form.setAction("go_there");
        form.setAcceptCharset("latin-1");
        form.setEnctype("multipart/form-data");
        form.setMethod("post");
        form.setName("aform");
        form.setTarget("green");

        assertEquals("go_there", form.getAction(), "specified action");
        assertEquals("latin-1", form.getAcceptCharset(), "character set");
        assertEquals("multipart/form-data", form.getEnctype(), "form encoding");
        assertEquals("post", form.getMethod(), "method");
        assertEquals("aform", form.getName(), "form name");
        assertEquals("green", form.getTarget(), "target");
    }

    @Test
    void testTitleElement() throws Exception {
        Element element = createElement("title");
        Text text = _htmlDocument.createTextNode("something here");
        element.appendChild(text);

        assertTrue(element instanceof HTMLTitleElement,
                "node should be an HTMLTitleElement but is " + element.getClass());
        assertEquals("TITLE", element.getNodeName(), "Tag name");

        HTMLTitleElement title = (HTMLTitleElement) element;
        assertEquals("something here", title.getText(), "initial title");

        title.setText("what it says now");
        NodeList childNodes = element.getChildNodes();
        assertEquals(1, childNodes.getLength(), "Number of child nodes");
        assertTrue(childNodes.item(0) instanceof Text, "Sole child node is not text");
        assertEquals("what it says now", ((Text) childNodes.item(0)).getData(), "Revised title text");
        assertEquals("what it says now", title.getText(), "revised title");
    }

    @Test
    void testEmptyTitleElement() throws Exception {
        Element element = createElement("title");

        assertTrue(element instanceof HTMLTitleElement,
                "node should be an HTMLTitleElement but is " + element.getClass());
        assertEquals("TITLE", element.getNodeName(), "Tag name");

        HTMLTitleElement title = (HTMLTitleElement) element;
        assertEquals("", title.getText(), "initial title");
        title.setText("what it says now");
        NodeList childNodes = element.getChildNodes();
        assertEquals(1, childNodes.getLength(), "Number of child nodes");
        assertTrue(childNodes.item(0) instanceof Text, "Sole child node is not text");
        assertEquals("what it says now", ((Text) childNodes.item(0)).getData(), "Revised title text");
        assertEquals("what it says now", title.getText(), "revised title");
    }

    @Test
    void testHtmlElement() throws Exception {
        doElementTest("html", HTMLHtmlElement.class, new String[][] { { "version", "4.0" } });
    }

    @Test
    void testHeadElement() throws Exception {
        doElementTest("head", HTMLHeadElement.class,
                new String[][] { { "profile", "http://www.acme.com/profiles/core" } });
    }

    @Test
    void testLinkElement() throws Exception {
        doElementTest("link", HTMLLinkElement.class,
                new Object[][] { { "charset", "utf-8" }, { "href", "site.css" }, { "hreflang", "en" },
                        { "disabled", Boolean.TRUE, Boolean.FALSE }, { "rel", "ccc.html" }, { "rev", "aaa.html" },
                        { "target", "green" }, { "type", "text/html" }, { "media", "paper", "screen" } });
    }

    @Test
    void testMetaElement() throws Exception {
        doElementTest("meta", HTMLMetaElement.class, new Object[][] { { "content", "Something" },
                { "http-equiv", "Refresh" }, { "name", "author" }, { "scheme", "ISBN" } });
    }

    @Test
    void testBaseElement() throws Exception {
        doElementTest("base", HTMLBaseElement.class,
                new Object[][] { { "href", "somewhere.html" }, { "target", "blue" } });
    }

    @Test
    void testStyleElment() throws Exception {
        doElementTest("style", HTMLStyleElement.class, new Object[][] { { "disabled", Boolean.TRUE, Boolean.FALSE },
                { "media", "paper", "screen" }, { "type", "text/css" } });
    }

    @Test
    void testBodyElement() throws Exception {
        doElementTest("body", HTMLBodyElement.class, new Object[][] { { "aLink", "red" }, { "background", "blue" },
                { "link", "azure" }, { "bgColor", "white" }, { "text", "maroon" }, { "vLink", "crimson" } });
    }

    @Test
    void testOptionElementAttributes() throws Exception {
        doElementTest("option", HTMLOptionElement.class, new Object[][] { { "disabled", Boolean.TRUE, Boolean.FALSE },
                { "label", "Vert" }, { "value", "green" } });
    }

    @Test
    void testSelectElement() throws Exception {
        doElementTest("select", HTMLSelectElement.class,
                new Object[][] { { "multiple", Boolean.TRUE, Boolean.FALSE }, { "name", "here" }, { "tabindex", 1, 0 },
                        { "size", 12, 0 }, { "disabled", Boolean.TRUE, Boolean.FALSE } });
    }

    @Test
    void testInputElement() throws Exception {
        doElementTest("input", HTMLInputElement.class,
                new Object[][] { { "accept", "text/html" }, { "accessKey", "C" }, { "align", "middle", "bottom" },
                        { "alt", "check" }, { "disabled", Boolean.TRUE, Boolean.FALSE }, { "maxlength", 5, 0 },
                        { "name", "here" }, { "readonly", Boolean.TRUE, Boolean.FALSE }, { "size", "12" },
                        { "src", "arrow.jpg" }, { "tabindex", 1, 0 }, { "type", "radio", "text", "ro" },
                        { "useMap", "myMap" }, { "value", "230" } });
        // XXX blur, focus, select, click
    }

    @Test
    void testButtonElement() throws Exception {
        doElementTest("button", HTMLButtonElement.class,
                new Object[][] { { "accesskey", "C" }, { "disabled", Boolean.TRUE, Boolean.FALSE }, { "name", "here" },
                        { "tabindex", 1, 0 }, { "type", "button", "submit", "ro" }, { "value", "230" } });
        // XXX blur, focus, select, click
    }

    @Test
    void testTextAreaElement() throws Exception {
        doElementTest("textarea", HTMLTextAreaElement.class,
                new Object[][] { { "accesskey", "C" }, { "cols", 1, 0 }, { "disabled", Boolean.TRUE, Boolean.FALSE },
                        { "name", "here" }, { "readonly", Boolean.TRUE, Boolean.FALSE }, { "rows", 8, 0 },
                        { "tabindex", 1, 0 }, { "type", "radio", "text", "ro" } });
        // XXX blur, focus, select
    }

    @Test
    void testAnchorElement() throws Exception {
        doElementTest("a", HTMLAnchorElement.class,
                new Object[][] { { "accesskey", "U" }, { "charset", "utf-8" }, { "hreflang", "en" }, { "name", "here" },
                        { "rel", "link" }, { "rev", "index" }, { "target", "green" }, { "type", "text/html" } });
    }

    @Test
    void testAreaElement() throws Exception {
        doElementTest("area", HTMLAreaElement.class,
                new Object[][] { { "accesskey", "U" }, { "alt", "[draw]" }, { "coords", "30,40,20" },
                        { "nohref", Boolean.TRUE, Boolean.FALSE }, { "shape", "circle" }, { "tabindex", 4, 0 },
                        { "target", "green" } });
    }

    @Test
    void testImageElement() throws Exception {
        doElementTest("img", HTMLImageElement.class,
                new Object[][] { { "name", "here" }, { "align", "top" }, { "alt", "big show" }, { "border", "3" },
                        { "height", "7" }, { "hspace", "1" }, { "ismap", Boolean.TRUE, Boolean.FALSE },
                        { "longdesc", "not too very" }, { "src", "circle.jpg" }, { "usemap", "mapname" },
                        { "vspace", "4" }, { "width", "15" } });
    }

    @Test
    void testParagraphElement() throws Exception {
        doElementTest("p", HTMLParagraphElement.class,
                new Object[][] { { "title", "here" }, { "id", "aaa" }, { "align", "top" } });
    }

    @Test
    void testIFrameElement() throws Exception {
        doElementTest("iframe", HTMLIFrameElement.class, new Object[][] { { "align", "center" }, { "src", "aaa" } });
    }

    /**
     * test the Applet Element changed default codebase from "/" to "." according to bug report [ 1895501 ] Handling no
     * codebase attribute in APPLET tag
     *
     * @throws Exception
     */
    @Test
    void testAppletElement() throws Exception {
        doElementTest("applet", HTMLAppletElement.class, new Object[][] { { "align", "center" }, /*
                                                                                                  * { "alt", "an applet"
                                                                                                  * }, { "archive",
                                                                                                  * "my.jar" },
                                                                                                  */
                { "code", "here.There" },
                { "codebase", "there", "." }, /*
                                               * { "height", "17" }, { "hspace", "2" }, { "name", "applet" }, {
                                               * "object", "a file here" }, { "vspace", "3" }, { "width", "80"}
                                               */ });
    }

    // XXX form.getLength, form.submit
    // XXX input.blur, input.focus, input.select, input.click
    // XXX a.blur, a.focus
    // XXX iframe.longDescm iframe.name, iframe.width, iframe.height, iframe.scrolling
    // XXX iframe.marginheight, iframe.marginwidth, iframe.frameborder

}

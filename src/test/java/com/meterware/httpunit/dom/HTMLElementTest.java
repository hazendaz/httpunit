/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.dom;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.html.HTMLAnchorElement;
import org.w3c.dom.html.HTMLAppletElement;
import org.w3c.dom.html.HTMLAreaElement;
import org.w3c.dom.html.HTMLBaseElement;
import org.w3c.dom.html.HTMLBodyElement;
import org.w3c.dom.html.HTMLButtonElement;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLFormElement;
import org.w3c.dom.html.HTMLHeadElement;
import org.w3c.dom.html.HTMLHtmlElement;
import org.w3c.dom.html.HTMLIFrameElement;
import org.w3c.dom.html.HTMLImageElement;
import org.w3c.dom.html.HTMLInputElement;
import org.w3c.dom.html.HTMLLinkElement;
import org.w3c.dom.html.HTMLMetaElement;
import org.w3c.dom.html.HTMLOptionElement;
import org.w3c.dom.html.HTMLParagraphElement;
import org.w3c.dom.html.HTMLSelectElement;
import org.w3c.dom.html.HTMLStyleElement;
import org.w3c.dom.html.HTMLTextAreaElement;
import org.w3c.dom.html.HTMLTitleElement;

/**
 * The Class HTMLElementTest.
 */
class HTMLElementTest extends AbstractHTMLElementTest {

    /**
     * Case insensitive tag search.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void caseInsensitiveTagSearch() throws Exception {
        Element element = createElement("body");
        Node form = element.appendChild(createElement("form"));
        NodeList nl = element.getElementsByTagName("form");
        assertEquals(1, nl.getLength(), "# form nodes to find");
        assertSame(form, nl.item(0), "Found form node");
    }

    /**
     * Base element defaults.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void baseElementDefaults() throws Exception {
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
     * test base element attributes.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void baseElementAttributes() throws Exception {
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

    /**
     * Writeable element attributes.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void writeableElementAttributes() throws Exception {
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

    /**
     * Empty form defaults.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void emptyFormDefaults() throws Exception {
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

    /**
     * Form attributes.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void formAttributes() throws Exception {
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

    /**
     * Writeable form attributes.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void writeableFormAttributes() throws Exception {
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

    /**
     * Title element.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void titleElement() throws Exception {
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

    /**
     * Empty title element.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void emptyTitleElement() throws Exception {
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

    /**
     * Html element.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void htmlElement() throws Exception {
        assertDoesNotThrow(() -> {
            doElementTest("html", HTMLHtmlElement.class, new String[][] { { "version", "4.0" } });
        });
    }

    /**
     * Head element.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void headElement() throws Exception {
        assertDoesNotThrow(() -> {
            doElementTest("head", HTMLHeadElement.class,
                    new String[][] { { "profile", "http://www.acme.com/profiles/core" } });
        });
    }

    /**
     * Link element.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void linkElement() throws Exception {
        assertDoesNotThrow(() -> {
            doElementTest("link", HTMLLinkElement.class,
                    new Object[][] { { "charset", "utf-8" }, { "href", "site.css" }, { "hreflang", "en" },
                            { "disabled", Boolean.TRUE, Boolean.FALSE }, { "rel", "ccc.html" }, { "rev", "aaa.html" },
                            { "target", "green" }, { "type", "text/html" }, { "media", "paper", "screen" } });
        });
    }

    /**
     * Meta element.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void metaElement() throws Exception {
        assertDoesNotThrow(() -> {
            doElementTest("meta", HTMLMetaElement.class, new Object[][] { { "content", "Something" },
                    { "http-equiv", "Refresh" }, { "name", "author" }, { "scheme", "ISBN" } });
        });
    }

    /**
     * Base element.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void baseElement() throws Exception {
        assertDoesNotThrow(() -> {
            doElementTest("base", HTMLBaseElement.class,
                    new Object[][] { { "href", "somewhere.html" }, { "target", "blue" } });
        });
    }

    /**
     * Style elment.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void styleElment() throws Exception {
        assertDoesNotThrow(() -> {
            doElementTest("style", HTMLStyleElement.class, new Object[][] { { "disabled", Boolean.TRUE, Boolean.FALSE },
                    { "media", "paper", "screen" }, { "type", "text/css" } });
        });
    }

    /**
     * Body element.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void bodyElement() throws Exception {
        assertDoesNotThrow(() -> {
            doElementTest("body", HTMLBodyElement.class, new Object[][] { { "aLink", "red" }, { "background", "blue" },
                    { "link", "azure" }, { "bgColor", "white" }, { "text", "maroon" }, { "vLink", "crimson" } });
        });
    }

    /**
     * Option element attributes.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void optionElementAttributes() throws Exception {
        assertDoesNotThrow(() -> {
            doElementTest("option", HTMLOptionElement.class, new Object[][] {
                    { "disabled", Boolean.TRUE, Boolean.FALSE }, { "label", "Vert" }, { "value", "green" } });
        });
    }

    /**
     * Select element.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void selectElement() throws Exception {
        assertDoesNotThrow(() -> {
            doElementTest("select", HTMLSelectElement.class,
                    new Object[][] { { "multiple", Boolean.TRUE, Boolean.FALSE }, { "name", "here" },
                            { "tabindex", 1, 0 }, { "size", 12, 0 }, { "disabled", Boolean.TRUE, Boolean.FALSE } });
        });
    }

    /**
     * Input element.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void inputElement() throws Exception {
        assertDoesNotThrow(() -> {
            doElementTest("input", HTMLInputElement.class,
                    new Object[][] { { "accept", "text/html" }, { "accessKey", "C" }, { "align", "middle", "bottom" },
                            { "alt", "check" }, { "disabled", Boolean.TRUE, Boolean.FALSE }, { "maxlength", 5, 0 },
                            { "name", "here" }, { "readonly", Boolean.TRUE, Boolean.FALSE }, { "size", "12" },
                            { "src", "arrow.jpg" }, { "tabindex", 1, 0 }, { "type", "radio", "text", "ro" },
                            { "useMap", "myMap" }, { "value", "230" } });
            // XXX blur, focus, select, click
        });
        // XXX blur, focus, select, click
    }

    /**
     * Button element.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void buttonElement() throws Exception {
        assertDoesNotThrow(() -> {
            doElementTest("button", HTMLButtonElement.class,
                    new Object[][] { { "accesskey", "C" }, { "disabled", Boolean.TRUE, Boolean.FALSE },
                            { "name", "here" }, { "tabindex", 1, 0 }, { "type", "button", "submit", "ro" },
                            { "value", "230" } });
            // XXX blur, focus, select, click
        });
        // XXX blur, focus, select, click
    }

    /**
     * Text area element.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void textAreaElement() throws Exception {
        assertDoesNotThrow(() -> {
            doElementTest("textarea", HTMLTextAreaElement.class,
                    new Object[][] { { "accesskey", "C" }, { "cols", 1, 0 },
                            { "disabled", Boolean.TRUE, Boolean.FALSE }, { "name", "here" },
                            { "readonly", Boolean.TRUE, Boolean.FALSE }, { "rows", 8, 0 }, { "tabindex", 1, 0 },
                            { "type", "radio", "text", "ro" } });
            // XXX blur, focus, select
        });
        // XXX blur, focus, select
    }

    /**
     * Anchor element.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void anchorElement() throws Exception {
        assertDoesNotThrow(() -> {
            doElementTest("a", HTMLAnchorElement.class,
                    new Object[][] { { "accesskey", "U" }, { "charset", "utf-8" }, { "hreflang", "en" },
                            { "name", "here" }, { "rel", "link" }, { "rev", "index" }, { "target", "green" },
                            { "type", "text/html" } });
        });
    }

    /**
     * Area element.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void areaElement() throws Exception {
        assertDoesNotThrow(() -> {
            doElementTest("area", HTMLAreaElement.class,
                    new Object[][] { { "accesskey", "U" }, { "alt", "[draw]" }, { "coords", "30,40,20" },
                            { "nohref", Boolean.TRUE, Boolean.FALSE }, { "shape", "circle" }, { "tabindex", 4, 0 },
                            { "target", "green" } });
        });
    }

    /**
     * Image element.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void imageElement() throws Exception {
        assertDoesNotThrow(() -> {
            doElementTest("img", HTMLImageElement.class,
                    new Object[][] { { "name", "here" }, { "align", "top" }, { "alt", "big show" }, { "border", "3" },
                            { "height", "7" }, { "hspace", "1" }, { "ismap", Boolean.TRUE, Boolean.FALSE },
                            { "longdesc", "not too very" }, { "src", "circle.jpg" }, { "usemap", "mapname" },
                            { "vspace", "4" }, { "width", "15" } });
        });
    }

    /**
     * Paragraph element.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void paragraphElement() throws Exception {
        assertDoesNotThrow(() -> {
            doElementTest("p", HTMLParagraphElement.class,
                    new Object[][] { { "title", "here" }, { "id", "aaa" }, { "align", "top" } });
        });
    }

    /**
     * I frame element.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void iFrameElement() throws Exception {
        assertDoesNotThrow(() -> {
            doElementTest("iframe", HTMLIFrameElement.class,
                    new Object[][] { { "align", "center" }, { "src", "aaa" } });
        });
    }

    /**
     * test the Applet Element changed default codebase from "/" to "." according to bug report [ 1895501 ] Handling no
     * codebase attribute in APPLET tag
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void appletElement() throws Exception {
        assertDoesNotThrow(() -> {
            doElementTest("applet", HTMLAppletElement.class, new Object[][] { { "align", "center" },
                    /*
                     * { "alt", "an applet" }, { "archive", "my.jar" },
                     */
                    { "code", "here.There" }, { "codebase", "there", "." },
                    /*
                     * { "height", "17" }, { "hspace", "2" }, { "name", "applet" }, { "object", "a file here" }, {
                     * "vspace", "3" }, { "width", "80"}
                     */
            });
        });
    }

    // XXX form.getLength, form.submit
    // XXX input.blur, input.focus, input.select, input.click
    // XXX a.blur, a.focus
    // XXX iframe.longDescm iframe.name, iframe.width, iframe.height, iframe.scrolling
    // XXX iframe.marginheight, iframe.marginwidth, iframe.frameborder

}

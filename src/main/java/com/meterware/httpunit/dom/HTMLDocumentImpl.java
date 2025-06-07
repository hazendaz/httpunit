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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;

import org.mozilla.javascript.Scriptable;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLBaseElement;
import org.w3c.dom.html.HTMLBodyElement;
import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.html.HTMLDocument;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLHeadElement;
import org.w3c.dom.html.HTMLHtmlElement;
import org.w3c.dom.html.HTMLTitleElement;

/**
 * The Class HTMLDocumentImpl.
 */
public class HTMLDocumentImpl extends DocumentImpl implements HTMLDocument, HTMLContainerElement {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The exemplars. */
    private static Properties<String, HTMLElementImpl> _exemplars = new Properties();

    /** The window. */
    private DomWindow _window;

    /** The write buffer. */
    private StringBuilder _writeBuffer;

    /** The container delegate. */
    private HTMLContainerDelegate _containerDelegate = new HTMLContainerDelegate(SKIP_IFRAMES);

    /**
     * Sets the i frames enabled.
     *
     * @param enabled
     *            the new i frames enabled
     */
    public void setIFramesEnabled(boolean enabled) {
        _containerDelegate = new HTMLContainerDelegate(enabled ? SKIP_IFRAMES : null);
    }

    @Override
    public Object get(String propertyName, Scriptable scriptable) {
        if (propertyName.equals("document")) {
            return this;
        }

        Object result = super.get(propertyName, scriptable);
        if (result != NOT_FOUND) {
            return result;
        }

        Element element = getElementById(propertyName);
        if (element != null) {
            return element;
        }

        NodeList elements = getElementsByName(propertyName);
        if (elements.getLength() >= 1) {
            return elements.item(0);
        }

        return ScriptingSupport.getNamedProperty(this, getJavaPropertyName(propertyName), scriptable);
    }

    @Override
    public void put(String propertyName, Scriptable initialObject, Object value) {
        ScriptingSupport.setNamedProperty(this, getJavaPropertyName(propertyName), value);
    }

    // ------------------------------------------ HTMLContainerElement methods
    // ----------------------------------------------

    @Override
    public HTMLCollection getLinks() {
        return _containerDelegate.getLinks(this);
    }

    @Override
    public HTMLCollection getImages() {
        return _containerDelegate.getImages(this);
    }

    @Override
    public HTMLCollection getApplets() {
        return _containerDelegate.getApplets(this);
    }

    @Override
    public HTMLCollection getForms() {
        return _containerDelegate.getForms(this);
    }

    @Override
    public HTMLCollection getAnchors() {
        return _containerDelegate.getAnchors(this);
    }

    // -------------------------------------------- HTMLDocument methods
    // ----------------------------------------------------

    /**
     * Gets the title.
     *
     * @return the title
     */
    @Override
    public String getTitle() {
        HTMLTitleElement result = getTitleElement();
        return result == null ? "" : result.getText();
    }

    /**
     * Gets the title element.
     *
     * @return the title element
     */
    private HTMLTitleElement getTitleElement() {
        HTMLTitleElement result = null;
        NodeList titleNodes = getElementsByTagName("title");
        for (int i = 0; i < titleNodes.getLength(); i++) {
            Node node = titleNodes.item(i);
            if (node instanceof HTMLTitleElement) {
                result = (HTMLTitleElement) node;
            }
        }
        return result;
    }

    /**
     * Gets the head element.
     *
     * @return the head element
     */
    private HTMLHeadElement getHeadElement() {
        NodeList headNodes = getElementsByTagName("head");
        for (int i = 0; i < headNodes.getLength(); i++) {
            Node node = headNodes.item(i);
            if (node instanceof HTMLHeadElement) {
                return (HTMLHeadElement) node;
            }
        }

        HTMLHeadElement head = (HTMLHeadElement) createElement("head");
        getHtmlElement().appendChild(head);
        return head;
    }

    /**
     * Gets the html element.
     *
     * @return the html element
     */
    private HTMLHtmlElement getHtmlElement() {
        NodeList htmlNodes = getElementsByTagName("html");
        for (int i = 0; i < htmlNodes.getLength(); i++) {
            Node node = htmlNodes.item(i);
            if (node instanceof HTMLHtmlElement) {
                return (HTMLHtmlElement) node;
            }
        }

        HTMLHtmlElement html = (HTMLHtmlElement) createElement("html");
        appendChild(html);
        return html;
    }

    /**
     * Sets the title.
     *
     * @param title
     *            the new title
     */
    @Override
    public void setTitle(String title) {
        HTMLTitleElement titleElement = getTitleElement();
        if (titleElement != null) {
            titleElement.setText(title);
        } else {
            titleElement = (HTMLTitleElement) createElement("title");
            titleElement.setText(title);
            getHeadElement().appendChild(titleElement);
        }
    }

    /**
     * Gets the referrer.
     *
     * @return the referrer
     */
    @Override
    public String getReferrer() {
        return null;
    }

    /**
     * Gets the domain.
     *
     * @return the domain
     */
    @Override
    public String getDomain() {
        return null;
    }

    /**
     * Gets the url.
     *
     * @return the url
     */
    @Override
    public String getURL() {
        return null;
    }

    /**
     * Gets the body.
     *
     * @return the body
     */
    @Override
    public HTMLElement getBody() {
        NodeList bodyNodes = getElementsByTagName("body");
        for (int i = 0; i < bodyNodes.getLength(); i++) {
            Node node = bodyNodes.item(i);
            if (node instanceof HTMLBodyElement) {
                return (HTMLBodyElement) node;
            }
        }
        return null;
    }

    /**
     * Sets the body.
     *
     * @param body
     *            the new body
     */
    @Override
    public void setBody(HTMLElement body) {
        getHtmlElement().appendChild(body);
    }

    /**
     * Gets the cookie.
     *
     * @return the cookie
     */
    @Override
    public String getCookie() {
        return null;
    }

    /**
     * Sets the cookie.
     *
     * @param cookie
     *            the new cookie
     */
    @Override
    public void setCookie(String cookie) {
    }

    /**
     * Open.
     */
    @Override
    public void open() {
    }

    /**
     * Close.
     */
    @Override
    public void close() {
        if (getWindow().replaceText(getWriteBuffer().toString(), getMimeType())) {
            clearWriteBuffer();
        }
    }

    /**
     * Gets the mime type.
     *
     * @return the mime type
     */
    private String getMimeType() {
        return "text/html";
    }

    /**
     * Write.
     *
     * @param text
     *            the text
     */
    @Override
    public void write(String text) {
        getWriteBuffer().append(text);
    }

    /**
     * Writeln.
     *
     * @param text
     *            the text
     */
    @Override
    public void writeln(String text) {
        getWriteBuffer().append(text).append((char) 0x0d).append((char) 0x0a);
    }

    /**
     * Gets the elements by name.
     *
     * @param elementName
     *            the element name
     *
     * @return the elements by name
     */
    @Override
    public NodeList getElementsByName(String elementName) {
        ArrayList<HTMLElementImpl> elements = new ArrayList<HTMLElementImpl>();
        for (Iterator<?> each = preOrderIterator(); each.hasNext();) {
            Node node = (Node) each.next();
            if (!(node instanceof HTMLElementImpl)) {
                continue;
            }
            HTMLElementImpl element = (HTMLElementImpl) node;
            if (elementName.equals(element.getAttributeWithNoDefault("name"))) {
                elements.add(element);
            }
        }
        return new NodeListImpl(elements);
    }

    @Override
    public Element createElement(String tagName) throws DOMException {
        ElementImpl element = getExemplar(tagName).create();
        element.initialize(this, toNodeCase(tagName));
        return element;
    }

    @Override
    public Element createElementNS(String namespaceURI, String qualifiedName) throws DOMException {
        ElementImpl element = getExemplar(qualifiedName).create();
        element.initialize(this, namespaceURI, toNodeCase(qualifiedName));
        return element;
    }

    @Override
    public NodeList getElementsByTagName(String name) {
        return super.getElementsByTagName(toNodeCase(name));
    }

    @Override
    public Node cloneNode(boolean deep) {
        HTMLDocumentImpl copy = new HTMLDocumentImpl();

        if (deep) {
            copy.importChildren(this, copy);
            copy._documentElement = copy.getHtmlElement();
        }
        return copy;
    }

    /**
     * Gets the exemplar.
     *
     * @param tagName
     *            the tag name
     *
     * @return the exemplar
     */
    private static HTMLElementImpl getExemplar(String tagName) {
        HTMLElementImpl impl = (HTMLElementImpl) _exemplars.get(tagName.toLowerCase(Locale.ENGLISH));
        if (impl == null) {
            impl = new HTMLElementImpl();
        }
        return impl;
    }

    /**
     * To node case.
     *
     * @param nodeName
     *            the node name
     *
     * @return the string
     */
    String toNodeCase(String nodeName) {
        return nodeName.toUpperCase();
    }

    /**
     * Gets the container delegate.
     *
     * @return the container delegate
     */
    HTMLContainerDelegate getContainerDelegate() {
        return _containerDelegate;
    }

    static {
        _exemplars.put("html", new HTMLHtmlElementImpl());
        _exemplars.put("head", new HTMLHeadElementImpl());
        _exemplars.put("link", new HTMLLinkElementImpl());
        _exemplars.put("title", new HTMLTitleElementImpl());
        _exemplars.put("meta", new HTMLMetaElementImpl());
        _exemplars.put("base", new HTMLBaseElementImpl());
        _exemplars.put("style", new HTMLStyleElementImpl());
        _exemplars.put("body", new HTMLBodyElementImpl());
        _exemplars.put("form", new HTMLFormElementImpl());
        _exemplars.put("select", new HTMLSelectElementImpl());
        _exemplars.put("option", new HTMLOptionElementImpl());
        _exemplars.put("input", new HTMLInputElementImpl());
        _exemplars.put("button", new HTMLButtonElementImpl());
        _exemplars.put("textarea", new HTMLTextAreaElementImpl());
        _exemplars.put("a", new HTMLAnchorElementImpl());
        _exemplars.put("area", new HTMLAreaElementImpl());
        _exemplars.put("img", new HTMLImageElementImpl());
        _exemplars.put("td", new HTMLTableCellElementImpl());
        _exemplars.put("th", new HTMLTableCellElementImpl());
        _exemplars.put("tr", new HTMLTableRowElementImpl());
        _exemplars.put("table", new HTMLTableElementImpl());
        _exemplars.put("p", new HTMLParagraphElementImpl());
        _exemplars.put("iframe", new HTMLIFrameElementImpl());
        _exemplars.put("applet", new HTMLAppletElementImpl());
    }

    /**
     * get the Window.
     *
     * @return the window
     */
    public DomWindow getWindow() {
        // if there is now window yet
        if (_window == null) {
            // create a window for this document
            _window = new DomWindow(this);
            setParentScope(_window);
        }
        return _window;
    }

    /**
     * Gets the write buffer.
     *
     * @return the write buffer
     */
    StringBuilder getWriteBuffer() {
        if (_writeBuffer == null) {
            _writeBuffer = new StringBuilder();
        }
        return _writeBuffer;
    }

    /**
     * Clear write buffer.
     */
    public void clearWriteBuffer() {
        _writeBuffer = null;
    }

    /**
     * Gets the base url.
     *
     * @return the base url
     */
    URL getBaseUrl() {
        NodeList list = getElementsByTagName("base");
        if (list.getLength() == 0) {
            return getWindow().getUrl();
        }

        HTMLBaseElement base = (HTMLBaseElement) list.item(0);
        try {
            return new URL(getWindow().getUrl(), base.getHref());
        } catch (MalformedURLException e) {
            return getWindow().getUrl();
        }
    }
}

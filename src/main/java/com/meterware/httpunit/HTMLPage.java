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
package com.meterware.httpunit;

import com.meterware.httpunit.parsing.DocumentAdapter;
import com.meterware.httpunit.parsing.HTMLParserFactory;
import com.meterware.httpunit.scripting.NamedDelegate;
import com.meterware.httpunit.scripting.ScriptableDelegate;
import com.meterware.httpunit.scripting.ScriptingHandler;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLDocument;
import org.xml.sax.SAXException;

/**
 * This class represents an HTML page returned from a request.
 **/
public class HTMLPage extends ParsedHTML {

    /** The scriptable. */
    private Scriptable _scriptable;

    /**
     * Instantiates a new HTML page.
     *
     * @param response
     *            the response
     * @param frame
     *            the frame
     * @param baseURL
     *            the base URL
     * @param baseTarget
     *            the base target
     * @param characterSet
     *            the character set
     */
    HTMLPage(WebResponse response, FrameSelector frame, URL baseURL, String baseTarget, String characterSet) {
        super(response, frame, baseURL, baseTarget, null, characterSet);
    }

    /**
     * Returns the title of the page.
     *
     * @return the title
     *
     * @throws SAXException
     *             the SAX exception
     */
    public String getTitle() throws SAXException {
        NodeList nl = ((Document) getOriginalDOM()).getElementsByTagName("title");
        if (nl.getLength() == 0 || !nl.item(0).hasChildNodes()) {
            return "";
        }
        return nl.item(0).getFirstChild().getNodeValue();
    }

    /**
     * Returns the onLoad event script.
     *
     * @return the on load event
     *
     * @throws SAXException
     *             the SAX exception
     */
    public String getOnLoadEvent() throws SAXException {
        Element mainElement = getMainElement((Document) getOriginalDOM());
        return mainElement == null ? "" : mainElement.getAttribute("onload");
    }

    /**
     * Gets the main element.
     *
     * @param document
     *            the document
     *
     * @return the main element
     */
    private Element getMainElement(Document document) {
        NodeList nl = document.getElementsByTagName("frameset");
        if (nl.getLength() == 0) {
            nl = document.getElementsByTagName("body");
        }
        return nl.getLength() == 0 ? null : (Element) nl.item(0);
    }

    /**
     * Returns the location of the linked stylesheet in the head &lt;code&gt; &lt;link type="text/css" rel="stylesheet"
     * href="/mystyle.css" /&gt; &lt;/code&gt;
     *
     * @return the external style sheet
     *
     * @throws SAXException
     *             the SAX exception
     */
    public String getExternalStyleSheet() throws SAXException {
        NodeList nl = ((Document) getOriginalDOM()).getElementsByTagName("link");
        int length = nl.getLength();
        if (length == 0) {
            return "";
        }

        for (int i = 0; i < length; i++) {
            if ("stylesheet".equalsIgnoreCase(NodeUtils.getNodeAttribute(nl.item(i), "rel"))) {
                return NodeUtils.getNodeAttribute(nl.item(i), "href");
            }
        }
        return "";
    }

    /**
     * Retrieves the "content" of the meta tags for a key pair attribute-attributeValue. &lt;code&gt; &lt;meta
     * name="robots" content="index" /&gt; &lt;meta name="robots" content="follow" /&gt; &lt;meta http-equiv="Expires"
     * content="now" /&gt; &lt;/code&gt; this can be used like this &lt;code&gt; getMetaTagContent("name","robots") will
     * return { "index","follow" } getMetaTagContent("http-equiv","Expires") will return { "now" } &lt;/code&gt;
     *
     * @param attribute
     *            the attribute
     * @param attributeValue
     *            the attribute value
     *
     * @return the meta tag content
     */
    public String[] getMetaTagContent(String attribute, String attributeValue) {
        List<String> matches = new ArrayList<>();
        NodeList nl = ((Document) getOriginalDOM()).getElementsByTagName("meta");
        int length = nl.getLength();

        for (int i = 0; i < length; i++) {
            if (attributeValue.equalsIgnoreCase(NodeUtils.getNodeAttribute(nl.item(i), attribute))) {
                matches.add(NodeUtils.getNodeAttribute(nl.item(i), "content"));
            }
        }
        return matches.toArray(new String[0]);
    }

    /**
     * scriptable for HTML Page.
     */

    public class Scriptable extends ScriptableDelegate {

        /**
         * get the Object with the given propertyName
         *
         * @param propertyName
         *            - the name of the property
         */
        @Override
        public Object get(String propertyName) {
            NamedDelegate delegate = getNamedItem(getForms(), propertyName);
            if (delegate != null) {
                return delegate;
            }

            delegate = getNamedItem(getLinks(), propertyName);
            if (delegate != null) {
                return delegate;
            }

            return getNamedItem(getImages(), propertyName);
        }

        /**
         * Gets the named item.
         *
         * @param items
         *            the items
         * @param name
         *            the name
         *
         * @return the named item
         */
        private NamedDelegate getNamedItem(ScriptingHandler[] items, String name) {
            if (name == null) {
                return null;
            }
            for (ScriptingHandler item : items) {
                if (item instanceof NamedDelegate && name.equals(((NamedDelegate) item).getName())) {
                    return (NamedDelegate) item;
                }
            }
            return null;
        }

        /**
         * Sets the value of the named property. Will throw a runtime exception if the property does not exist or cannot
         * accept the specified value.
         **/
        @Override
        public void set(String propertyName, Object value) {
            if (propertyName.equalsIgnoreCase("location")) {
                getResponse().getScriptableObject().set("location", value);
            } else {
                super.set(propertyName, value);
            }
        }

        /**
         * Gets the parent.
         *
         * @return the parent
         */
        public WebResponse.Scriptable getParent() {
            return getResponse().getScriptableObject();
        }

        /**
         * Gets the title.
         *
         * @return the title
         *
         * @throws SAXException
         *             the SAX exception
         */
        public String getTitle() throws SAXException {
            return HTMLPage.this.getTitle();
        }

        /**
         * Gets the links.
         *
         * @return the links
         */
        public ScriptingHandler[] getLinks() {
            WebLink[] links = HTMLPage.this.getLinks();
            ScriptingHandler[] result = new WebLink.Scriptable[links.length];
            for (int i = 0; i < links.length; i++) {
                result[i] = links[i].getScriptingHandler();
            }
            return result;
        }

        /**
         * Gets the forms.
         *
         * @return the forms
         */
        public ScriptingHandler[] getForms() {
            WebForm[] forms = HTMLPage.this.getForms();
            ScriptingHandler[] result = new ScriptingHandler[forms.length];
            for (int i = 0; i < forms.length; i++) {
                result[i] = forms[i].getScriptingHandler();
            }
            return result;
        }

        /**
         * Gets the images.
         *
         * @return the images
         */
        public ScriptingHandler[] getImages() {
            WebImage[] images = HTMLPage.this.getImages();
            ScriptingHandler[] result = new WebImage.Scriptable[images.length];
            for (int i = 0; i < images.length; i++) {
                result[i] = images[i].getScriptingHandler();
            }
            return result;
        }

        /**
         * Instantiates a new scriptable.
         */
        Scriptable() {
        }

        /**
         * Replace text.
         *
         * @param text
         *            the text
         * @param contentType
         *            the content type
         *
         * @return true, if successful
         */
        public boolean replaceText(String text, String contentType) {
            return getResponse().replaceText(text, contentType);
        }

        /**
         * Sets the cookie.
         *
         * @param name
         *            the name
         * @param value
         *            the value
         */
        public void setCookie(String name, String value) {
            getResponse().setCookie(name, value);
        }

        /**
         * Gets the cookie.
         *
         * @return the cookie
         */
        public String getCookie() {
            return emptyIfNull(getResponse().getCookieHeader());
        }

        /**
         * Empty if null.
         *
         * @param string
         *            the string
         *
         * @return the string
         */
        private String emptyIfNull(String string) {
            return string == null ? "" : string;
        }

        /**
         * Gets the element with ID.
         *
         * @param id
         *            the id
         *
         * @return the element with ID
         */
        public ScriptableDelegate getElementWithID(String id) {
            final HTMLElement elementWithID = HTMLPage.this.getElementWithID(id);
            return elementWithID == null ? null : (ScriptableDelegate) elementWithID.getScriptingHandler();
        }

        /**
         * Gets the elements by name.
         *
         * @param name
         *            the name
         *
         * @return the elements by name
         */
        public ScriptableDelegate[] getElementsByName(String name) {
            return getDelegates(HTMLPage.this.getElementsWithName(name));
        }

        /**
         * Gets the elements by tag name.
         *
         * @param name
         *            the name
         *
         * @return the elements by tag name
         */
        public ScriptableDelegate[] getElementsByTagName(String name) {
            return getDelegates(HTMLPage.this.getElementsByTagName(HTMLPage.this.getRootNode(), name));
        }
    }

    /**
     * Gets the scriptable object.
     *
     * @return the scriptable object
     */
    Scriptable getScriptableObject() {
        if (_scriptable == null) {
            _scriptable = new Scriptable();
            _scriptable.setScriptEngine(getResponse().getScriptableObject().getScriptEngine(_scriptable));
        }
        return _scriptable;
    }

    /**
     * parse the given test with the given URL.
     *
     * @param text
     *            the text
     * @param pageURL
     *            the page URL
     *
     * @throws SAXException
     *             the SAX exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void parse(String text, URL pageURL) throws SAXException, IOException {
        HTMLParserFactory.getHTMLParser().parse(pageURL, text, new DocumentAdapter() {
            @Override
            public void setDocument(HTMLDocument document) {
                HTMLPage.this.setRootNode(document);
            }

            @Override
            public String getIncludedScript(String srcAttribute) throws IOException {
                return HTMLPage.this.getIncludedScript(srcAttribute);
            }

            @Override
            public ScriptingHandler getScriptingHandler() {
                return getResponse().getScriptingHandler();
            }
        });
    }

}

/*
 * MIT License
 *
 * Copyright 2011-2026 Russell Gold
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

import com.meterware.httpunit.dom.HTMLElementImpl;
import com.meterware.httpunit.scripting.NamedDelegate;
import com.meterware.httpunit.scripting.ScriptableDelegate;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * This class represents a link in an HTML page. Users of this class may examine the structure of the link (as a DOM),
 * or create a {@link WebRequest} to simulate clicking on the link.
 **/
public class WebLink extends FixedURLWebRequestSource {

    /** Predicate to match part or all of a link's URL string. **/
    public static final HTMLElementPredicate MATCH_URL_STRING;

    /** Predicate to match a link's text exactly. **/
    public static final HTMLElementPredicate MATCH_TEXT;

    /** Predicate to match part or all of a link's contained text. **/
    public static final HTMLElementPredicate MATCH_CONTAINED_TEXT;

    /** Predicate to match a link's ID. **/
    public static final HTMLElementPredicate MATCH_ID;

    /** Predicate to match a link's name. **/
    public static final HTMLElementPredicate MATCH_NAME;

    /**
     * Returns the URL referenced by this link. This may be a relative URL. It will not include any fragment identifier.
     *
     * @return the URL string
     */
    public String getURLString() {
        return getRelativeURL();
    }

    /**
     * Returns the text value of this link.
     **/
    @Override
    public String getText() {
        if (getElement().getNodeName().equalsIgnoreCase("area")) {
            return getAttribute("alt");
        }
        return super.getText();
    }

    /**
     * Returns the text value of this link.
     *
     * @return the string
     *
     * @deprecated as of 1.6, use #getText instead
     */
    @Deprecated
    public String asText() {
        return getText();
    }

    /**
     * Submits a request as though the user had clicked on this link. Will also fire the 'onClick', 'onMouseDown' and
     * 'onMouseUp' event if defined. Returns the updated contents of the frame containing the link. Note that if the
     * click updates a different frame, that frame will not be returned by this method.
     *
     * @return the web response
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws SAXException
     *             the SAX exception
     */
    public WebResponse click() throws IOException, SAXException {
        if (handleEvent("onclick")) {
            ((HTMLElementImpl) getNode()).doClickAction();
        }
        return getCurrentFrameContents();
    }

    /**
     * Simulates moving the mouse over the link. Will fire the 'onMouseOver' event if defined.
     **/
    public void mouseOver() {
        handleEvent("onmouseover");
    }

    /**
     * The Class Scriptable.
     */
    public class Scriptable extends HTMLElementScriptable implements NamedDelegate {

        /**
         * Instantiates a new scriptable.
         */
        public Scriptable() {
            super(WebLink.this);
        }

        @Override
        public String getName() {
            return WebLink.this.getID().length() != 0 ? WebLink.this.getID() : WebLink.this.getName();
        }

        @Override
        public Object get(String propertyName) {
            if (propertyName.equalsIgnoreCase("href")) {
                return getReference().toExternalForm();
            }
            return super.get(propertyName);
        }

        @Override
        public void set(String propertyName, Object value) {
            if (propertyName.equals("href")) {
                setDestination((String) value);
            } else {
                super.set(propertyName, value);
            }
        }

        /**
         * Gets the reference.
         *
         * @return the reference
         */
        private URL getReference() {
            try {
                return getRequest().getURL();
            } catch (MalformedURLException e) {
                return WebLink.this.getBaseURL();
            }
        }
    }

    // ----------------------------------------- WebRequestSource methods
    // ---------------------------------------------------

    @Override
    public ScriptableDelegate newScriptable() {
        return new Scriptable();
    }

    // --------------------------------------------------- package members
    // --------------------------------------------------

    /**
     * Contructs a web link given the URL of its source page and the DOM extracted from that page.
     *
     * @param response
     *            the response
     * @param baseURL
     *            the base URL
     * @param node
     *            the node
     * @param sourceFrame
     *            the source frame
     * @param defaultTarget
     *            the default target
     * @param characterSet
     *            the character set
     */
    WebLink(WebResponse response, URL baseURL, Node node, FrameSelector sourceFrame, String defaultTarget,
            String characterSet) {
        super(response, node, baseURL, "href", sourceFrame, defaultTarget, characterSet);
    }

    static {
        MATCH_URL_STRING = (htmlElement, criteria) -> HttpUnitUtils.contains(((WebLink) htmlElement).getURLString(),
                (String) criteria);

        MATCH_TEXT = (htmlElement, criteria) -> HttpUnitUtils.matches(((WebLink) htmlElement).getText(),
                (String) criteria);

        MATCH_CONTAINED_TEXT = (htmlElement, criteria) -> HttpUnitUtils.contains(((WebLink) htmlElement).getText(),
                (String) criteria);

        MATCH_ID = (htmlElement, criteria) -> HttpUnitUtils.matches(((WebLink) htmlElement).getID(), (String) criteria);

        MATCH_NAME = (htmlElement, criteria) -> HttpUnitUtils.matches(((WebLink) htmlElement).getName(),
                (String) criteria);

    }

}

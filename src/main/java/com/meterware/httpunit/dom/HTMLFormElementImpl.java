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
package com.meterware.httpunit.dom;

import com.meterware.httpunit.protocol.URLEncodedString;
import com.meterware.httpunit.scripting.FormScriptable;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.html.HTMLFormElement;

/**
 * The Class HTMLFormElementImpl.
 */
public class HTMLFormElementImpl extends HTMLElementImpl implements HTMLFormElement, FormScriptable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    @Override
    ElementImpl create() {
        return new HTMLFormElementImpl();
    }

    // ------------------------------- ScriptableObject methods
    // ----------------------------------------------------------

    @Override
    public Object get(String propertyName, Scriptable scriptable) {
        HTMLCollection elements = getElements();
        for (int i = 0; i < elements.getLength(); i++) {
            Node node = elements.item(i);
            NamedNodeMap attributes = node.getAttributes();
            AttrImpl nameAttribute = (AttrImpl) attributes.getNamedItem("name");
            if (nameAttribute != null && propertyName.equals(nameAttribute.getValue())) {
                return node;
            }
            AttrImpl idAttribute = (AttrImpl) attributes.getNamedItem("id");
            if (idAttribute != null && propertyName.equals(idAttribute.getValue())) {
                return node;
            }
        }
        return super.get(propertyName, scriptable);
    }

    // ------------------------------- HTMLFormElement methods
    // ----------------------------------------------------------

    /**
     * Gets the accept charset.
     *
     * @return the accept charset
     */
    @Override
    public String getAcceptCharset() {
        return getAttributeWithDefault("accept-charset", "UNKNOWN");
    }

    /**
     * Sets the accept charset.
     *
     * @param acceptCharset
     *            the new accept charset
     */
    @Override
    public void setAcceptCharset(String acceptCharset) {
        setAttribute("accept-charset", acceptCharset);
    }

    /**
     * Gets the action.
     *
     * @return the action
     */
    @Override
    public String getAction() {
        return getAttribute("action");
    }

    @Override
    public void setAction(String action) {
        setAttribute("action", action);
    }

    @Override
    public void setParameterValue(String name, String value) {
        Object control = get(name, null);
        if (control instanceof ScriptableObject) {
            ((ScriptableObject) control).put("value", this, value);
        }
    }

    /**
     * Gets the enctype.
     *
     * @return the enctype
     */
    @Override
    public String getEnctype() {
        return getAttributeWithDefault("enctype", "application/x-www-form-urlencoded");
    }

    /**
     * Sets the enctype.
     *
     * @param enctype
     *            the new enctype
     */
    @Override
    public void setEnctype(String enctype) {
        setAttribute("enctype", enctype);
    }

    /**
     * Gets the method.
     *
     * @return the method
     */
    @Override
    public String getMethod() {
        return getAttributeWithDefault("method", "get");
    }

    /**
     * Sets the method.
     *
     * @param method
     *            the new method
     */
    @Override
    public void setMethod(String method) {
        setAttribute("method", method);
    }

    /**
     * getter for the name.
     *
     * @return the name
     *
     * @see org.w3c.dom.html.HTMLFormElement#getName()
     */
    @Override
    public String getName() {
        String result = getAttributeWithNoDefault("name");
        if (result == null) {
            result = this.getId();
        }
        return result;
    }

    /**
     * Sets the name.
     *
     * @param name
     *            the new name
     */
    @Override
    public void setName(String name) {
        setAttribute("name", name);
    }

    /**
     * Gets the target.
     *
     * @return the target
     */
    @Override
    public String getTarget() {
        return getAttributeWithNoDefault("target");
    }

    /**
     * Sets the target.
     *
     * @param target
     *            the new target
     */
    @Override
    public void setTarget(String target) {
        setAttribute("target", target);
    }

    /**
     * Gets the elements.
     *
     * @return the elements
     */
    @Override
    public HTMLCollection getElements() {
        ArrayList elements = new ArrayList<>();
        String[] names = { "INPUT", "TEXTAREA", "BUTTON", "SELECT" };
        for (Iterator each = preOrderIteratorAfterNode(); each.hasNext();) {
            Node node = (Node) each.next();
            if (node instanceof HTMLFormElement) {
                break;
            }

            if (node.getNodeType() != ELEMENT_NODE) {
                continue;
            }
            String tagName = ((Element) node).getTagName();
            for (String name : names) {
                if (tagName.equalsIgnoreCase(name)) {
                    elements.add(node);
                }
            }
        }
        return HTMLCollectionImpl.createHTMLCollectionImpl(new NodeListImpl(elements));
    }

    /**
     * Gets the length.
     *
     * @return the length
     */
    @Override
    public int getLength() {
        return 0;
    }

    /**
     * Reset.
     */
    @Override
    public void reset() {
        HTMLCollection elements = getElements();
        for (int i = 0; i < elements.getLength(); i++) {
            Node node = elements.item(i);
            if (node instanceof HTMLControl) {
                ((HTMLControl) node).reset();
            }
        }
    }

    /**
     * Submit.
     */
    @Override
    public void submit() {
        doSubmitAction();
    }

    /**
     * Handles the actual form submission - does not handle the "submit" event.
     */
    void doSubmitAction() {
        try {
            if ("get".equalsIgnoreCase(getMethod())) {
                getDomWindow().submitRequest(this, getMethod(), getEffectiveUrl(), getTarget(), new byte[0]);
            } else if ("post".equalsIgnoreCase(getMethod())) {
                getDomWindow().submitRequest(this, getMethod(), getAction(), getTarget(), new byte[0]);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error submitting form: " + e);
        } finally {
            silenceSubmitButtons();
        }
    }

    /**
     * Silence submit buttons.
     */
    private void silenceSubmitButtons() {
        HTMLCollection controls = getElements();
        for (int i = 0; i < controls.getLength(); i++) {
            ((HTMLControl) controls.item(i)).silenceSubmitButton();
        }
    }

    /**
     * Gets the effective url.
     *
     * @return the effective url
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private String getEffectiveUrl() throws IOException {
        StringBuilder spec = new StringBuilder(getAction());
        if ("get".equalsIgnoreCase(getMethod())) {
            URLEncodedString parameters = new URLEncodedString();
            HTMLCollection controls = getElements();
            for (int i = 0; i < controls.getLength(); i++) {
                ((HTMLControl) controls.item(i)).addValues(parameters, "us-ascii");
            }
            if (spec.indexOf("?") >= 0 && !spec.toString().endsWith("?")) {
                spec.append('&');
            } else {
                spec.append('?');
            }
            spec.append(parameters.getString());
        }
        return new URL(getDomWindow().getUrl(), spec.toString()).toExternalForm();
    }

    /**
     * Gets the dom window.
     *
     * @return the dom window
     */
    private DomWindow getDomWindow() {
        return ((HTMLDocumentImpl) getOwnerDocument()).getWindow();
    }

}

/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.dom;

import com.meterware.httpunit.protocol.ParameterProcessor;

import java.io.IOException;
import java.util.Iterator;

import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.html.HTMLFormElement;

/**
 * The Class HTMLControl.
 */
public class HTMLControl extends HTMLElementImpl {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Gets the disabled.
     *
     * @return the disabled
     */
    public boolean getDisabled() {
        return getBooleanAttribute("disabled");
    }

    /**
     * Gets the form.
     *
     * @return the form
     */
    public HTMLFormElement getForm() {
        Node parent = getParentNode();
        while (parent != null && !"form".equalsIgnoreCase(parent.getNodeName())) {
            parent = parent.getParentNode();
        }
        if (parent != null) {
            return (HTMLFormElement) parent;
        }

        for (Iterator here = preOrderIterator(); here.hasNext();) {
            Object o = here.next();
            if (o instanceof HTMLFormElement) {
                return getPreviousForm((HTMLFormElement) o);
            }
        }
        return getLastFormInDocument();
    }

    /**
     * Gets the previous form.
     *
     * @param nextForm
     *            the next form
     *
     * @return the previous form
     */
    private HTMLFormElement getPreviousForm(HTMLFormElement nextForm) {
        HTMLCollection forms = getHtmlDocument().getForms();
        for (int i = 0; i < forms.getLength(); i++) {
            if (nextForm == forms.item(i)) {
                return i == 0 ? null : (HTMLFormElement) forms.item(i - 1);
            }
        }
        return null;
    }

    /**
     * Gets the last form in document.
     *
     * @return the last form in document
     */
    private HTMLFormElement getLastFormInDocument() {
        HTMLCollection forms = getHtmlDocument().getForms();
        return forms.getLength() == 0 ? null : (HTMLFormElement) forms.item(forms.getLength() - 1);
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return getAttributeWithNoDefault("name");
    }

    /**
     * Gets the read only.
     *
     * @return the read only
     */
    public boolean getReadOnly() {
        return getBooleanAttribute("readonly");
    }

    /**
     * Gets the tab index.
     *
     * @return the tab index
     */
    public int getTabIndex() {
        return getIntegerAttribute("tabindex");
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        return getAttributeWithDefault("type", "text");
    }

    /**
     * Sets the disabled.
     *
     * @param disabled
     *            the new disabled
     */
    public void setDisabled(boolean disabled) {
        setAttribute("disabled", disabled);
    }

    /**
     * Sets the name.
     *
     * @param name
     *            the new name
     */
    public void setName(String name) {
        setAttribute("name", name);
    }

    /**
     * Sets the read only.
     *
     * @param readOnly
     *            the new read only
     */
    public void setReadOnly(boolean readOnly) {
        setAttribute("readonly", readOnly);
    }

    /**
     * Sets the tab index.
     *
     * @param tabIndex
     *            the new tab index
     */
    public void setTabIndex(int tabIndex) {
        setAttribute("tabindex", tabIndex);
    }

    /**
     * Reset.
     */
    public void reset() {
    }

    /**
     * Adds the values.
     *
     * @param processor
     *            the processor
     * @param characterSet
     *            the character set
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    void addValues(ParameterProcessor processor, String characterSet) throws IOException {
    }

    /**
     * Silence submit button.
     */
    public void silenceSubmitButton() {
    }
}

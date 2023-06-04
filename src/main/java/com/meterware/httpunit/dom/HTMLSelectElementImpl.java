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

import com.meterware.httpunit.protocol.ParameterProcessor;

import java.io.IOException;

import org.w3c.dom.DOMException;
import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLOptionElement;
import org.w3c.dom.html.HTMLSelectElement;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class HTMLSelectElementImpl extends HTMLControl implements HTMLSelectElement {

    private static final long serialVersionUID = 1L;
    public static final String TYPE_SELECT_ONE = "select-one";
    public static final String TYPE_SELECT_MULTIPLE = "select-multiple";

    @Override
    ElementImpl create() {
        return new HTMLSelectElementImpl();
    }

    @Override
    public void add(HTMLElement element, HTMLElement before) throws DOMException {
    }

    /**
     * simulate blur
     */
    @Override
    public void blur() {
        handleEvent("onblur");
    }

    /**
     * simulate focus;
     */
    @Override
    public void focus() {
        handleEvent("onfocus");
    }

    @Override
    public String getType() {
        return isMultiSelect() ? TYPE_SELECT_MULTIPLE : TYPE_SELECT_ONE;
    }

    private boolean isMultiSelect() {
        return getMultiple() && getSize() > 1;
    }

    @Override
    public int getLength() {
        return getOptions().getLength();
    }

    @Override
    public boolean getMultiple() {
        return getBooleanAttribute("multiple");
    }

    @Override
    public HTMLCollection getOptions() {
        return HTMLCollectionImpl
                .createHTMLCollectionImpl(getElementsByTagName(getHtmlDocument().toNodeCase("option")));
    }

    @Override
    public int getSelectedIndex() {
        HTMLCollection options = getOptions();
        for (int i = 0; i < options.getLength(); i++) {
            if (((HTMLOptionElement) options.item(i)).getSelected()) {
                return i;
            }
        }
        return isMultiSelect() ? -1 : 0;
    }

    @Override
    public String getValue() {
        HTMLCollection options = getOptions();
        for (int i = 0; i < options.getLength(); i++) {
            HTMLOptionElement optionElement = (HTMLOptionElement) options.item(i);
            if (optionElement.getSelected()) {
                return optionElement.getValue();
            }
        }
        return isMultiSelect() || options.getLength() == 0 ? null : ((HTMLOptionElement) options.item(0)).getValue();
    }

    @Override
    public int getSize() {
        return getIntegerAttribute("size");
    }

    @Override
    public void remove(int index) {
    }

    @Override
    public void setMultiple(boolean multiple) {
        setAttribute("multiple", multiple);
    }

    @Override
    public void setSelectedIndex(int selectedIndex) {
        HTMLCollection options = getOptions();
        for (int i = 0; i < options.getLength(); i++) {
            HTMLOptionElementImpl optionElement = (HTMLOptionElementImpl) options.item(i);
            optionElement.setSelected(i == selectedIndex);
        }
    }

    @Override
    public void setSize(int size) {
        setAttribute("size", size);
    }

    int getIndexOf(HTMLOptionElementImpl option) {
        HTMLCollection options = getOptions();
        for (int i = 0; i < options.getLength(); i++) {
            if (options.item(i) == option) {
                return i;
            }
        }
        throw new IllegalStateException("option is not part of this select");
    }

    void clearSelected() {
        setSelectedIndex(-1);
    }

    @Override
    void addValues(ParameterProcessor processor, String characterSet) throws IOException {
        HTMLCollection options = getOptions();
        String name = getName();
        for (int i = 0; i < options.getLength(); i++) {
            ((HTMLOptionElementImpl) options.item(i)).addValueIfSelected(processor, name, characterSet);
        }
    }

    @Override
    public void setValue(String value) {
        setAttribute("value", value);
    }

    @Override
    public void reset() {
        HTMLCollection options = getOptions();
        for (int i = 0; i < options.getLength(); i++) {
            HTMLControl optionElement = (HTMLControl) options.item(i);
            optionElement.reset();
        }
    }
}

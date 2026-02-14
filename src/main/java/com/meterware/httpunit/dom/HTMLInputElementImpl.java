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

import com.meterware.httpunit.protocol.ParameterProcessor;

import java.io.IOException;
import java.util.Locale;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.html.HTMLInputElement;

/**
 * The Class HTMLInputElementImpl.
 */
public class HTMLInputElementImpl extends HTMLControl implements HTMLInputElement {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The value. */
    private String _value;

    /** The checked. */
    private Boolean _checked;

    /** The behavior. */
    private TypeSpecificBehavior _behavior;

    @Override
    ElementImpl create() {
        return new HTMLInputElementImpl();
    }

    /**
     * simulate blur.
     */
    @Override
    public void blur() {
        handleEvent("onblur");
    }

    /**
     * simulate focus;.
     */
    @Override
    public void focus() {
        handleEvent("onfocus");
    }

    @Override
    public void doClickAction() {
        getBehavior().click();
    }

    /**
     * Select.
     */
    @Override
    public void select() {
    }

    /**
     * Gets the accept.
     *
     * @return the accept
     */
    @Override
    public String getAccept() {
        return getAttributeWithNoDefault("accept");
    }

    /**
     * Gets the access key.
     *
     * @return the access key
     */
    @Override
    public String getAccessKey() {
        return getAttributeWithNoDefault("accessKey");
    }

    /**
     * Gets the align.
     *
     * @return the align
     */
    @Override
    public String getAlign() {
        return getAttributeWithDefault("align", "bottom");
    }

    /**
     * Gets the alt.
     *
     * @return the alt
     */
    @Override
    public String getAlt() {
        return getAttributeWithNoDefault("alt");
    }

    /**
     * Gets the checked.
     *
     * @return the checked
     */
    @Override
    public boolean getChecked() {
        return getBehavior().getChecked();
    }

    /**
     * Gets the default checked.
     *
     * @return the default checked
     */
    @Override
    public boolean getDefaultChecked() {
        return getBooleanAttribute("checked");
    }

    /**
     * Gets the default value.
     *
     * @return the default value
     */
    @Override
    public String getDefaultValue() {
        return getAttributeWithNoDefault("value");
    }

    /**
     * Gets the max length.
     *
     * @return the max length
     */
    @Override
    public int getMaxLength() {
        return getIntegerAttribute("maxlength");
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    @Override
    public String getSize() {
        return getAttributeWithNoDefault("size");
    }

    /**
     * Gets the src.
     *
     * @return the src
     */
    @Override
    public String getSrc() {
        return getAttributeWithNoDefault("src");
    }

    /**
     * Gets the use map.
     *
     * @return the use map
     */
    @Override
    public String getUseMap() {
        return getAttributeWithNoDefault("useMap");
    }

    /**
     * Sets the accept.
     *
     * @param accept
     *            the new accept
     */
    @Override
    public void setAccept(String accept) {
        setAttribute("accept", accept);
    }

    /**
     * Sets the access key.
     *
     * @param accessKey
     *            the new access key
     */
    @Override
    public void setAccessKey(String accessKey) {
        setAttribute("accessKey", accessKey);
    }

    /**
     * Sets the align.
     *
     * @param align
     *            the new align
     */
    @Override
    public void setAlign(String align) {
        setAttribute("align", align);
    }

    /**
     * Sets the alt.
     *
     * @param alt
     *            the new alt
     */
    @Override
    public void setAlt(String alt) {
        setAttribute("alt", alt);
    }

    /**
     * Sets the checked.
     *
     * @param checked
     *            the new checked
     */
    @Override
    public void setChecked(boolean checked) {
        getBehavior().setChecked(checked);
    }

    /**
     * Sets the default checked.
     *
     * @param defaultChecked
     *            the new default checked
     */
    @Override
    public void setDefaultChecked(boolean defaultChecked) {
        setAttribute("checked", defaultChecked);
    }

    /**
     * Sets the default value.
     *
     * @param defaultValue
     *            the new default value
     */
    @Override
    public void setDefaultValue(String defaultValue) {
        setAttribute("value", defaultValue);
    }

    /**
     * Sets the max length.
     *
     * @param maxLength
     *            the new max length
     */
    @Override
    public void setMaxLength(int maxLength) {
        setAttribute("maxlength", maxLength);
    }

    /**
     * Sets the size.
     *
     * @param size
     *            the new size
     */
    @Override
    public void setSize(String size) {
        setAttribute("size", size);
    }

    /**
     * Sets the src.
     *
     * @param src
     *            the new src
     */
    @Override
    public void setSrc(String src) {
        setAttribute("src", src);
    }

    /**
     * Sets the use map.
     *
     * @param useMap
     *            the new use map
     */
    @Override
    public void setUseMap(String useMap) {
        setAttribute("useMap", useMap);
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    @Override
    public String getValue() {
        return getBehavior().getValue();
    }

    /**
     * Sets the value.
     *
     * @param value
     *            the new value
     */
    @Override
    public void setValue(String value) {
        getBehavior().setValue(value);
    }

    @Override
    public void reset() {
        getBehavior().reset();
    }

    @Override
    public void setAttribute(String name, String value) throws DOMException {
        super.setAttribute(name, value);
        if (name.equalsIgnoreCase("type")) {
            selectBehavior(getType().toLowerCase(Locale.ENGLISH));
        }
    }

    @Override
    void addValues(ParameterProcessor processor, String characterSet) throws IOException {
        getBehavior().addValues(getName(), processor, characterSet);
    }

    @Override
    public void silenceSubmitButton() {
        getBehavior().silenceSubmitButton();
    }

    /**
     * Sets the state.
     *
     * @param checked
     *            the new state
     */
    void setState(boolean checked) {
        _checked = checked ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Equals.
     *
     * @param s1
     *            the s 1
     * @param s2
     *            the s 2
     *
     * @return true, if successful
     */
    static boolean equals(String s1, String s2) {
        return s1 == null ? s2 == null : s1.equals(s2);
    }

    /**
     * Select behavior.
     *
     * @param type
     *            the type
     */
    private void selectBehavior(String type) {
        if (type == null || type.equals("text") || type.equals("password") || type.equals("hidden")) {
            _behavior = new EditableTextBehavior(this);
        } else if (type.equals("checkbox")) {
            _behavior = new CheckboxBehavior(this);
        } else if (type.equals("radio")) {
            _behavior = new RadioButtonBehavior(this);
        } else if (type.equals("reset")) {
            _behavior = new ResetButtonBehavior(this);
        } else if (type.equals("submit")) {
            _behavior = new SubmitButtonBehavior(this);
        } else {
            _behavior = new DefaultBehavior(this);
        }
    }

    /**
     * Gets the behavior.
     *
     * @return the behavior
     */
    private TypeSpecificBehavior getBehavior() {
        if (_behavior == null) {
            selectBehavior(getType().toLowerCase(Locale.ENGLISH));
        }
        return _behavior;
    }

    /**
     * The Interface TypeSpecificBehavior.
     */
    interface TypeSpecificBehavior {

        /**
         * Sets the value.
         *
         * @param value
         *            the new value
         */
        void setValue(String value);

        /**
         * Gets the value.
         *
         * @return the value
         */
        String getValue();

        /**
         * Reset.
         */
        void reset();

        /**
         * Click.
         */
        void click();

        /**
         * Gets the checked.
         *
         * @return the checked
         */
        boolean getChecked();

        /**
         * Sets the checked.
         *
         * @param checked
         *            the new checked
         */
        void setChecked(boolean checked);

        /**
         * Adds the values.
         *
         * @param name
         *            the name
         * @param processor
         *            the processor
         * @param characterSet
         *            the character set
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        void addValues(String name, ParameterProcessor processor, String characterSet) throws IOException;

        /**
         * Silence submit button.
         */
        void silenceSubmitButton();
    }

    /**
     * The Class DefaultBehavior.
     */
    class DefaultBehavior implements TypeSpecificBehavior {

        /** The element. */
        private HTMLElementImpl _element;

        /**
         * Instantiates a new default behavior.
         *
         * @param element
         *            the element
         */
        public DefaultBehavior(HTMLElementImpl element) {
            _element = element;
        }

        @Override
        public String getValue() {
            return _value != null ? _value : getDefaultValue();
        }

        @Override
        public void setValue(String value) {
            if (HTMLInputElementImpl.equals(value, _value)) {
                return;
            }

            _value = value;
            reportPropertyChanged("value");
        }

        @Override
        public boolean getChecked() {
            return getDefaultChecked();
        }

        @Override
        public void setChecked(boolean checked) {
        }

        @Override
        public void reset() {
        }

        @Override
        public void click() {
        }

        /**
         * Report property changed.
         *
         * @param propertyName
         *            the property name
         */
        protected void reportPropertyChanged(String propertyName) {
            _element.reportPropertyChanged(propertyName);
        }

        @Override
        public void addValues(String name, ParameterProcessor processor, String characterSet) throws IOException {
            processor.addParameter(name, getValue(), characterSet);
        }

        @Override
        public void silenceSubmitButton() {
        }
    }

    /**
     * The Class EditableTextBehavior.
     */
    class EditableTextBehavior extends DefaultBehavior {

        /**
         * Instantiates a new editable text behavior.
         *
         * @param element
         *            the element
         */
        public EditableTextBehavior(HTMLElementImpl element) {
            super(element);
        }

        @Override
        public void reset() {
            _value = null;
        }

    }

    /**
     * The Class SubmitButtonBehavior.
     */
    class SubmitButtonBehavior extends DefaultBehavior {

        /** The send with submit. */
        private boolean _sendWithSubmit;

        /**
         * Instantiates a new submit button behavior.
         *
         * @param element
         *            the element
         */
        public SubmitButtonBehavior(HTMLElementImpl element) {
            super(element);
        }

        @Override
        public void click() {
            _sendWithSubmit = true;
            ((HTMLFormElementImpl) getForm()).doSubmitAction();
        }

        @Override
        public void addValues(String name, ParameterProcessor processor, String characterSet) throws IOException {
            if (!_sendWithSubmit) {
                return;
            }
            super.addValues(name, processor, characterSet);
        }

        @Override
        public void silenceSubmitButton() {
            _sendWithSubmit = false;
        }

    }

    /**
     * The Class CheckboxBehavior.
     */
    class CheckboxBehavior extends DefaultBehavior {

        /**
         * Instantiates a new checkbox behavior.
         *
         * @param element
         *            the element
         */
        public CheckboxBehavior(HTMLElementImpl element) {
            super(element);
        }

        @Override
        public boolean getChecked() {
            return _checked != null ? _checked.booleanValue() : getDefaultChecked();
        }

        @Override
        public void setChecked(boolean checked) {
            setState(checked);
        }

        @Override
        public void reset() {
            _checked = null;
        }

        @Override
        public void click() {
            setChecked(!getChecked());
        }

        @Override
        public void addValues(String name, ParameterProcessor processor, String characterSet) throws IOException {
            if (!getDisabled() && getChecked()) {
                processor.addParameter(name, getFormValue(), characterSet);
            }
        }

        /**
         * Gets the form value.
         *
         * @return the form value
         */
        private String getFormValue() {
            return _value == null ? "on" : _value;
        }
    }

    /**
     * The Class RadioButtonBehavior.
     */
    class RadioButtonBehavior extends CheckboxBehavior {

        /**
         * Instantiates a new radio button behavior.
         *
         * @param element
         *            the element
         */
        public RadioButtonBehavior(HTMLElementImpl element) {
            super(element);
        }

        @Override
        public void setChecked(boolean checked) {
            if (checked) {
                HTMLCollection elements = getForm().getElements();
                for (int i = 0; i < elements.getLength(); i++) {
                    Node node = elements.item(i);
                    if (!(node instanceof HTMLInputElementImpl)) {
                        continue;
                    }
                    HTMLInputElementImpl input = (HTMLInputElementImpl) node;
                    if (getName().equals(input.getName()) && input.getType().equalsIgnoreCase("radio")) {
                        input.setState(false);
                    }
                }
            }
            setState(checked);
        }

        @Override
        public void click() {
            setChecked(true);
        }
    }

    /**
     * The Class ResetButtonBehavior.
     */
    class ResetButtonBehavior extends DefaultBehavior {

        /**
         * Instantiates a new reset button behavior.
         *
         * @param element
         *            the element
         */
        public ResetButtonBehavior(HTMLElementImpl element) {
            super(element);
        }

        @Override
        public void click() {
            getForm().reset();
        }

    }

}

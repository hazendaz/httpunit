/*
 * MIT License
 *
 * Copyright 2011-2024 Russell Gold
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
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.html.HTMLInputElement;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class HTMLInputElementImpl extends HTMLControl implements HTMLInputElement {

    private static final long serialVersionUID = 1L;
    private String _value;
    private Boolean _checked;
    private TypeSpecificBehavior _behavior;

    @Override
    ElementImpl create() {
        return new HTMLInputElementImpl();
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
    public void doClickAction() {
        getBehavior().click();
    }

    @Override
    public void select() {
    }

    @Override
    public String getAccept() {
        return getAttributeWithNoDefault("accept");
    }

    @Override
    public String getAccessKey() {
        return getAttributeWithNoDefault("accessKey");
    }

    @Override
    public String getAlign() {
        return getAttributeWithDefault("align", "bottom");
    }

    @Override
    public String getAlt() {
        return getAttributeWithNoDefault("alt");
    }

    @Override
    public boolean getChecked() {
        return getBehavior().getChecked();
    }

    @Override
    public boolean getDefaultChecked() {
        return getBooleanAttribute("checked");
    }

    @Override
    public String getDefaultValue() {
        return getAttributeWithNoDefault("value");
    }

    @Override
    public int getMaxLength() {
        return getIntegerAttribute("maxlength");
    }

    @Override
    public String getSize() {
        return getAttributeWithNoDefault("size");
    }

    @Override
    public String getSrc() {
        return getAttributeWithNoDefault("src");
    }

    @Override
    public String getUseMap() {
        return getAttributeWithNoDefault("useMap");
    }

    @Override
    public void setAccept(String accept) {
        setAttribute("accept", accept);
    }

    @Override
    public void setAccessKey(String accessKey) {
        setAttribute("accessKey", accessKey);
    }

    @Override
    public void setAlign(String align) {
        setAttribute("align", align);
    }

    @Override
    public void setAlt(String alt) {
        setAttribute("alt", alt);
    }

    @Override
    public void setChecked(boolean checked) {
        getBehavior().setChecked(checked);
    }

    @Override
    public void setDefaultChecked(boolean defaultChecked) {
        setAttribute("checked", defaultChecked);
    }

    @Override
    public void setDefaultValue(String defaultValue) {
        setAttribute("value", defaultValue);
    }

    @Override
    public void setMaxLength(int maxLength) {
        setAttribute("maxlength", maxLength);
    }

    @Override
    public void setSize(String size) {
        setAttribute("size", size);
    }

    @Override
    public void setSrc(String src) {
        setAttribute("src", src);
    }

    @Override
    public void setUseMap(String useMap) {
        setAttribute("useMap", useMap);
    }

    @Override
    public String getValue() {
        return getBehavior().getValue();
    }

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
            selectBehavior(getType().toLowerCase());
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

    void setState(boolean checked) {
        _checked = checked ? Boolean.TRUE : Boolean.FALSE;
    }

    static boolean equals(String s1, String s2) {
        return s1 == null ? s2 == null : s1.equals(s2);
    }

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

    private TypeSpecificBehavior getBehavior() {
        if (_behavior == null) {
            selectBehavior(getType().toLowerCase());
        }
        return _behavior;
    }

    interface TypeSpecificBehavior {
        void setValue(String value);

        String getValue();

        void reset();

        void click();

        boolean getChecked();

        void setChecked(boolean checked);

        void addValues(String name, ParameterProcessor processor, String characterSet) throws IOException;

        void silenceSubmitButton();
    }

    class DefaultBehavior implements TypeSpecificBehavior {

        private HTMLElementImpl _element;

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

    class EditableTextBehavior extends DefaultBehavior {

        public EditableTextBehavior(HTMLElementImpl element) {
            super(element);
        }

        @Override
        public void reset() {
            _value = null;
        }

    }

    class SubmitButtonBehavior extends DefaultBehavior {

        private boolean _sendWithSubmit;

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

    class CheckboxBehavior extends DefaultBehavior {

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

        private String getFormValue() {
            return _value == null ? "on" : _value;
        }
    }

    class RadioButtonBehavior extends CheckboxBehavior {

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

    class ResetButtonBehavior extends DefaultBehavior {

        public ResetButtonBehavior(HTMLElementImpl element) {
            super(element);
        }

        @Override
        public void click() {
            getForm().reset();
        }

    }

}

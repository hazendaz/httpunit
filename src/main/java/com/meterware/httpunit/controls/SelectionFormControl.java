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
package com.meterware.httpunit.controls;

import com.meterware.httpunit.FormControl;
import com.meterware.httpunit.NodeUtils;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.dom.HTMLSelectElementImpl;
import com.meterware.httpunit.protocol.ParameterProcessor;
import com.meterware.httpunit.scripting.ScriptableDelegate;
import com.meterware.httpunit.scripting.SelectionOption;
import com.meterware.httpunit.scripting.SelectionOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * FormControl for "Select" moved here by wf for testability and visibility see bugreport [ 1124057 ] Out of Bounds
 * Exception should be avoided.
 */
public class SelectionFormControl extends FormControl {

    /** The multi select. */
    private final boolean _multiSelect;

    /** The list box. */
    private final boolean _listBox;

    /** The selection options. */
    private Options _selectionOptions;

    @Override
    public String getType() {
        return isMultiValued() ? MULTIPLE_TYPE : SINGLE_TYPE;
    }

    /**
     * Instantiates a new selection form control.
     *
     * @param form
     *            the form
     * @param element
     *            the element
     */
    public SelectionFormControl(WebForm form, HTMLSelectElementImpl element) {
        super(form, element);
        if (!element.getNodeName().equalsIgnoreCase("select")) {
            throw new RuntimeException("Not a select element");
        }

        int size = NodeUtils.getAttributeValue(element, "size", 0);
        _multiSelect = NodeUtils.isNodeAttributePresent(element, "multiple");
        _listBox = size > 1 || _multiSelect && size != 1;

        _selectionOptions = _listBox ? (Options) new MultiSelectOptions(element)
                : (Options) new SingleSelectOptions(element);
    }

    @Override
    public String[] getValues() {
        return _selectionOptions.getSelectedValues();
    }

    @Override
    public String[] getOptionValues() {
        return _selectionOptions.getValues();
    }

    @Override
    public String[] getDisplayedOptions() {
        return _selectionOptions.getDisplayedText();
    }

    /**
     * Returns true if a single control can have multiple values.
     **/
    @Override
    public boolean isMultiValued() {
        return _multiSelect;
    }

    /**
     * The Class Scriptable.
     */
    class Scriptable extends FormControl.Scriptable {

        /**
         * get the Object with the given property name
         *
         * @param propertyName
         *            - the name of the property to get
         *
         * @return the Object for the property
         */
        @Override
        public Object get(String propertyName) {
            if (propertyName.equalsIgnoreCase("options")) {
                return _selectionOptions;
            }
            if (propertyName.equalsIgnoreCase("length")) {
                return Integer.valueOf(getOptionValues().length);
            }
            if (propertyName.equalsIgnoreCase("value")) {
                return getSelectedValue();
            }
            if (propertyName.equalsIgnoreCase("selectedIndex")) {
                return Integer.valueOf(_selectionOptions.getFirstSelectedIndex());
            }
            return super.get(propertyName);
        }

        /**
         * get the Object at the given index
         *
         * @param index
         *            - the index of the object to get
         *
         * @return the object at the given index
         */
        @Override
        public Object get(int index) {
            return _selectionOptions.get(index);
        }

        /**
         * Gets the selected value.
         *
         * @return the selected value
         */
        private String getSelectedValue() {
            String[] values = getValues();
            return values.length == 0 ? "" : values[0];
        }

        /**
         * set the property with the given name to the given value
         *
         * @param propertyName
         *            - the name of the property to set
         * @param value
         *            - the value to assign to the property
         */
        @Override
        public void set(String propertyName, Object value) {
            if (propertyName.equalsIgnoreCase("value")) {
                ArrayList values = new ArrayList<>();
                values.add(value);
                _selectionOptions.claimUniqueValues(values);
            } else if (propertyName.equalsIgnoreCase("selectedIndex")) {
                if (!(value instanceof Number)) {
                    throw new RuntimeException("selectedIndex must be set to an integer");
                }
                _selectionOptions.setSelectedIndex(((Number) value).intValue());
            } else if (propertyName.equalsIgnoreCase("length")) {
                _selectionOptions.setLength(((Number) value).intValue());
            } else {
                super.set(propertyName, value);
            }
        }
    }

    @Override
    public ScriptableDelegate newScriptable() {
        return new Scriptable();
    }

    /**
     * Update required parameters.
     *
     * @param required
     *            the required
     */
    void updateRequiredParameters(Properties required) {
        if (isReadOnly()) {
            required.put(getName(), getValues());
        }
    }

    @Override
    protected void addValues(ParameterProcessor processor, String characterSet) throws IOException {
        if (isDisabled()) {
            return;
        }
        for (int i = 0; i < getValues().length; i++) {
            processor.addParameter(getName(), getValues()[i], characterSet);
        }
    }

    @Override
    protected void claimUniqueValue(List values) {
        boolean changed = _selectionOptions.claimUniqueValues(values);
        if (changed) {
            sendOnChangeEvent();
        }
    }

    @Override
    protected void reset() {
        _selectionOptions.reset();
    }

    /**
     * The Class Option.
     */
    public static class Option extends ScriptableDelegate implements SelectionOption {

        /** The text. */
        private String _text = "";

        /** The value. */
        private String _value;

        /** The default selected. */
        private boolean _defaultSelected;

        /** The selected. */
        private boolean _selected;

        /** The index. */
        private int _index;

        /** The container. */
        private Options _container;

        /**
         * Instantiates a new option.
         */
        public Option() {
        }

        /**
         * Instantiates a new option.
         *
         * @param text
         *            the text
         * @param value
         *            the value
         * @param selected
         *            the selected
         */
        Option(String text, String value, boolean selected) {
            _text = text;
            _value = value;
            _defaultSelected = _selected = selected;
        }

        /**
         * Reset.
         */
        void reset() {
            _selected = _defaultSelected;
        }

        /**
         * Adds the value if selected.
         *
         * @param list
         *            the list
         */
        void addValueIfSelected(List list) {
            if (_selected) {
                list.add(_value);
            }
        }

        /**
         * Sets the index.
         *
         * @param container
         *            the container
         * @param index
         *            the index
         */
        void setIndex(Options container, int index) {
            _container = container;
            _index = index;
        }

        // ------------------------- SelectionOption methods ------------------------------

        @Override
        public void initialize(String text, String value, boolean defaultSelected, boolean selected) {
            _text = text;
            _value = value;
            _defaultSelected = defaultSelected;
            _selected = selected;
        }

        @Override
        public int getIndex() {
            return _index;
        }

        @Override
        public String getText() {
            return _text;
        }

        @Override
        public void setText(String text) {
            _text = text;
        }

        @Override
        public String getValue() {
            return _value;
        }

        @Override
        public void setValue(String value) {
            _value = value;
        }

        @Override
        public boolean isDefaultSelected() {
            return _defaultSelected;
        }

        @Override
        public void setSelected(boolean selected) {
            _selected = selected;
            if (selected) {
                _container.optionSet(_index);
            }
        }

        @Override
        public boolean isSelected() {
            return _selected;
        }
    }

    /**
     * The Class Options.
     */
    public abstract class Options extends ScriptableDelegate implements SelectionOptions {

        /** The options. */
        private Option[] _options;

        /**
         * Instantiates a new options.
         *
         * @param selectionNode
         *            the selection node
         */
        Options(Node selectionNode) {
            // BR [ 1843978 ] Accessing Options in a form is in lower case
            // calls for uppercase "option" here ... pending as of 2007-12-30
            NodeList nl = ((Element) selectionNode).getElementsByTagName("OPTION");

            _options = new Option[nl.getLength()];
            for (int i = 0; i < _options.length; i++) {
                final String displayedText = getValue(nl.item(i).getFirstChild()).trim();
                _options[i] = new Option(displayedText, getOptionValue(nl.item(i), displayedText),
                        nl.item(i).getAttributes().getNamedItem("selected") != null);
                _options[i].setIndex(this, i);
            }
        }

        /**
         * claim unique values from the given list of values.
         *
         * @param values
         *            - the list of values
         *
         * @return true, if successful
         */
        boolean claimUniqueValues(List values) {
            return claimUniqueValues(values, _options);
        }

        /**
         * Claim unique values.
         *
         * @param values
         *            the values
         * @param options
         *            the options
         *
         * @return true, if successful
         */
        protected abstract boolean claimUniqueValues(List values, Option[] options);

        /**
         * report if there are no matches be aware of [ 1100437 ] Patch for ClassCastException in FormControl TODO
         * implement patch if test get's available.
         *
         * @param values
         *            the values
         */
        protected final void reportNoMatches(List values) {
            if (!_listBox) {
                throw new IllegalParameterValueException(getName(), values, getOptionValues());
            }
        }

        /**
         * Gets the selected values.
         *
         * @return the selected values
         */
        String[] getSelectedValues() {
            ArrayList list = new ArrayList<>();
            for (Option _option : _options) {
                _option.addValueIfSelected(list);
            }
            if (!_listBox && list.isEmpty() && _options.length > 0) {
                list.add(_options[0].getValue());
            }
            return (String[]) list.toArray(new String[list.size()]);
        }

        /**
         * Reset.
         */
        void reset() {
            for (Option _option : _options) {
                _option.reset();
            }
        }

        /**
         * Gets the displayed text.
         *
         * @return the displayed text
         */
        String[] getDisplayedText() {
            String[] displayedText = new String[_options.length];
            for (int i = 0; i < displayedText.length; i++) {
                displayedText[i] = _options[i].getText();
            }
            return displayedText;
        }

        /**
         * Gets the values.
         *
         * @return the values
         */
        String[] getValues() {
            String[] values = new String[_options.length];
            for (int i = 0; i < values.length; i++) {
                values[i] = _options[i].getValue();
            }
            return values;
        }

        /**
         * Selects the matching item and deselects the others.
         *
         * @param index
         *            the new selected index
         */
        void setSelectedIndex(int index) {
            for (int i = 0; i < _options.length; i++) {
                _options[i]._selected = i == index;
            }
        }

        /**
         * Returns the index of the first item selected, or -1 if none is selected.
         *
         * @return the first selected index
         */
        int getFirstSelectedIndex() {
            for (int i = 0; i < _options.length; i++) {
                if (_options[i].isSelected()) {
                    return i;
                }
            }
            return noOptionSelectedIndex();
        }

        /**
         * No option selected index.
         *
         * @return the int
         */
        protected abstract int noOptionSelectedIndex();

        @Override
        public int getLength() {
            return _options.length;
        }

        /**
         * Modified by gklopp - 12/19/2005 [ 1396835 ] Javascript : length of a select element cannot be increased Bug
         * corrected : The length can be greater than the original length
         */
        @Override
        public void setLength(int length) {
            if (length < 0) {
                return;
            }
            Option[] newArray = new Option[length];
            System.arraycopy(_options, 0, newArray, 0, Math.min(length, _options.length));
            for (int i = _options.length; i < length; i++) {
                newArray[i] = new Option();
            }
            _options = newArray;
        }

        @Override
        public void put(int i, SelectionOption option) {
            if (i < 0) {
                return;
            }

            if (option == null) {
                if (i >= _options.length) {
                    return;
                }
                deleteOptionsEntry(i);
            } else {
                if (i >= _options.length) {
                    i = _options.length;
                    expandOptionsArray();
                }
                _options[i] = (Option) option;
                _options[i].setIndex(this, i);
                if (option.isSelected()) {
                    ensureUniqueOption(_options, i);
                }
            }
        }

        /**
         * Ensure unique option.
         *
         * @param options
         *            the options
         * @param i
         *            the i
         */
        protected abstract void ensureUniqueOption(Option[] options, int i);

        /**
         * Delete options entry.
         *
         * @param i
         *            the i
         */
        private void deleteOptionsEntry(int i) {
            Option[] newArray = new Option[_options.length - 1];
            System.arraycopy(_options, 0, newArray, 0, i);
            System.arraycopy(_options, i + 1, newArray, i, newArray.length - i);
            _options = newArray;
        }

        /**
         * Expand options array.
         */
        private void expandOptionsArray() {
            Option[] newArray = new Option[_options.length + 1];
            System.arraycopy(_options, 0, newArray, 0, _options.length);
            _options = newArray;
        }

        /**
         * get the Object at the given index check that the index is not out of bounds
         *
         * @param index
         *            - the index of the object to get
         *
         * @throws RuntimeException
         *             if index is out of bounds
         */
        @Override
        public Object get(int index) {
            // if the index is out of bounds
            if (index < 0 || index >= _options.length) {
                // create a user friendly error message
                StringBuilder msg = new StringBuilder("invalid index ").append(index).append(" for Options ");
                // by listing all possible options
                for (int i = 0; i < _options.length; i++) {
                    msg.append(_options[i]._text);
                    if (i < _options.length - 1) {
                        msg.append(",");
                    }
                } // for
                  // now throw a RunTimeException that would
                  // have happened anyways with a less friendly message
                throw new RuntimeException(msg.toString());
            } // if
            return _options[index];
        } // get

        /**
         * Invoked when an option is set true. *
         *
         * @param i
         *            the i
         */
        void optionSet(int i) {
            ensureUniqueOption(_options, i);
        }

        /**
         * Gets the option value.
         *
         * @param optionNode
         *            the option node
         * @param displayedText
         *            the displayed text
         *
         * @return the option value
         */
        private String getOptionValue(Node optionNode, String displayedText) {
            NamedNodeMap nnm = optionNode.getAttributes();
            if (nnm.getNamedItem("value") != null) {
                return getValue(nnm.getNamedItem("value"));
            }
            return displayedText;
        }

        /**
         * Gets the value.
         *
         * @param node
         *            the node
         *
         * @return the value
         */
        private String getValue(Node node) {
            return node == null ? "" : emptyIfNull(node.getNodeValue());
        }
    }

    /**
     * The Class SingleSelectOptions.
     */
    class SingleSelectOptions extends Options {

        /**
         * Instantiates a new single select options.
         *
         * @param selectionNode
         *            the selection node
         */
        public SingleSelectOptions(Node selectionNode) {
            super(selectionNode);
        }

        @Override
        protected void ensureUniqueOption(Option[] options, int i) {
            for (int j = 0; j < options.length; j++) {
                options[j]._selected = i == j;
            }
        }

        @Override
        protected int noOptionSelectedIndex() {
            return 0;
        }

        /**
         * claim the values be aware of [ 1100437 ] Patch for ClassCastException in FormControl TODO implement patch if
         * test get's available - the (String) cast might fail
         */
        @Override
        protected boolean claimUniqueValues(List values, Option[] options) {
            boolean changed = false;
            for (int i = 0; i < values.size(); i++) {
                String value = (String) values.get(i);
                for (int j = 0; j < options.length; j++) {
                    boolean selected = value.equals(options[j].getValue());
                    if (selected != options[j].isSelected()) {
                        changed = true;
                    }
                    options[j].setSelected(selected);
                    if (selected) {
                        values.remove(value);
                        for (++j; j < options.length; j++) {
                            options[j].setSelected(false);
                        }
                        return changed;
                    }
                }
            }
            reportNoMatches(values);
            return changed;
        }
    }

    /**
     * The Class MultiSelectOptions.
     */
    class MultiSelectOptions extends Options {

        /**
         * Instantiates a new multi select options.
         *
         * @param selectionNode
         *            the selection node
         */
        public MultiSelectOptions(Node selectionNode) {
            super(selectionNode);
        }

        @Override
        protected void ensureUniqueOption(Option[] options, int i) {
        }

        @Override
        protected int noOptionSelectedIndex() {
            return -1;
        }

        @Override
        protected boolean claimUniqueValues(List values, Option[] options) {
            boolean changed = false;
            for (Option option : options) {
                final boolean newValue = values.contains(option.getValue());
                if (newValue != option.isSelected()) {
                    changed = true;
                }
                option.setSelected(newValue);
                if (newValue) {
                    values.remove(option.getValue());
                }
            }
            return changed;
        }
    }

}

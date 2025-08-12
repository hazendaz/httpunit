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

import com.meterware.httpunit.protocol.UploadFileSpec;
import com.meterware.httpunit.scripting.ScriptableDelegate;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Represents the aggregate of all form controls with a particular name. This permits us to abstract setting values so
 * that changing a control type does not break a test.
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class FormParameter {

    private static final FormParameter UNKNOWN_PARAMETER = new FormParameter();

    private FormControl[] _controls;
    private ArrayList _controlList = new ArrayList<>();
    private RadioGroupFormControl _group;
    private String _name;

    /**
     * @return the uNKNOWN_PARAMETER
     */
    public static FormParameter getUNKNOWN_PARAMETER() {
        return UNKNOWN_PARAMETER;
    }

    /**
     * return whether I am the unknown parameter
     *
     * @return
     */
    public boolean isUnknown() {
        return this == UNKNOWN_PARAMETER;
    }

    /**
     * add the given form control
     *
     * @param control
     */
    void addControl(FormControl control) {
        _controls = null;
        if (_name == null) {
            _name = control.getName();
        }
        if (!_name.equalsIgnoreCase(control.getName())) {
            throw new RuntimeException("all controls should have the same name");
        }
        if (control.isExclusive()) {
            getRadioGroup(control.getForm()).addRadioButton((RadioButtonFormControl) control);
        } else {
            _controlList.add(control);
        }
    }

    /**
     * get the controls for this form Parameter
     *
     * @return the controls
     */
    public FormControl[] getControls() {
        if (_controls == null) {
            _controls = (FormControl[]) _controlList.toArray(new FormControl[_controlList.size()]);
        }
        return _controls;
    }

    /**
     * get the control for this form Parameter (assuming it has only one as for a text control
     *
     * @return the controls
     */
    public FormControl getControl() {
        FormControl[] controls = getControls();
        if (controls.length != 1) {
            throw new RuntimeException("getControl can only be called if the number of controls is 1 but it is "
                    + controls.length + " you might want to use getControls instead");
        }
        return controls[0];
    }

    Object getScriptableObject() {
        if (getControls().length == 1) {
            return getControls()[0].getDelegate();
        }
        ArrayList list = new ArrayList<>();
        for (FormControl control : _controls) {
            list.add(control.getScriptingHandler());
        }
        return list.toArray(new ScriptableDelegate[list.size()]);
    }

    String[] getValues() {
        ArrayList valueList = new ArrayList<>();
        FormControl[] controls = getControls();
        for (FormControl control : controls) {
            valueList.addAll(Arrays.asList(control.getValues()));
        }
        return (String[]) valueList.toArray(new String[valueList.size()]);
    }

    /**
     * set values to the given values
     *
     * @param values
     */
    void setValues(String[] values) {
        ArrayList list = new ArrayList(values.length);
        list.addAll(Arrays.asList(values));
        FormControl[] controls = getControls();
        for (FormControl control : controls) {
            control.claimRequiredValues(list);
        }
        for (FormControl control : controls) {
            control.claimUniqueValue(list);
        }
        for (FormControl control : controls) {
            control.claimValue(list);
        }
        if (!list.isEmpty()) {
            throw new UnusedParameterValueException(_name, (String) list.get(0));
        }
    }

    public void toggleCheckbox() {
        FormControl[] controls = getControls();
        if (controls.length != 1) {
            throw new IllegalCheckboxParameterException(_name, "toggleCheckbox");
        }
        controls[0].toggle();
    }

    public void toggleCheckbox(String value) {
        FormControl[] controls = getControls();
        for (FormControl control : controls) {
            if (value.equals(control.getValueAttribute())) {
                control.toggle();
                return;
            }
        }
        throw new IllegalCheckboxParameterException(_name + "/" + value, "toggleCheckbox");
    }

    public void setValue(boolean state) {
        FormControl[] controls = getControls();
        if (controls.length != 1) {
            throw new IllegalCheckboxParameterException(_name, "setCheckbox");
        }
        controls[0].setState(state);
    }

    public void setValue(String value, boolean state) {
        FormControl[] controls = getControls();
        for (FormControl control : controls) {
            if (value.equals(control.getValueAttribute())) {
                control.setState(state);
                return;
            }
        }
        throw new IllegalCheckboxParameterException(_name + "/" + value, "setCheckbox");
    }

    void setFiles(UploadFileSpec[] fileArray) {
        ArrayList list = new ArrayList(fileArray.length);
        list.addAll(Arrays.asList(fileArray));
        for (int i = 0; i < getControls().length; i++) {
            getControls()[i].claimUploadSpecification(list);
        }
        if (!list.isEmpty()) {
            throw new UnusedUploadFileException(_name, fileArray.length - list.size(), fileArray.length);
        }
    }

    String[] getOptions() {
        ArrayList optionList = new ArrayList<>();
        FormControl[] controls = getControls();
        for (FormControl control : controls) {
            optionList.addAll(Arrays.asList(control.getDisplayedOptions()));
        }
        return (String[]) optionList.toArray(new String[optionList.size()]);
    }

    String[] getOptionValues() {
        ArrayList valueList = new ArrayList<>();
        for (int i = 0; i < getControls().length; i++) {
            valueList.addAll(Arrays.asList(getControls()[i].getOptionValues()));
        }
        return (String[]) valueList.toArray(new String[valueList.size()]);
    }

    boolean isMultiValuedParameter() {
        FormControl[] controls = getControls();
        for (FormControl control : controls) {
            if (control.isMultiValued() || !control.isExclusive() && controls.length > 1) {
                return true;
            }
        }
        return false;
    }

    int getNumTextParameters() {
        int result = 0;
        FormControl[] controls = getControls();
        for (FormControl control : controls) {
            if (control.isTextControl()) {
                result++;
            }
        }
        return result;
    }

    boolean isTextParameter() {
        FormControl[] controls = getControls();
        for (FormControl control : controls) {
            if (control.isTextControl()) {
                return true;
            }
        }
        return false;
    }

    boolean isFileParameter() {
        FormControl[] controls = getControls();
        for (FormControl control : controls) {
            if (control.isFileParameter()) {
                return true;
            }
        }
        return false;
    }

    /**
     * is this a disabled parameter
     *
     * @return false if one of the controls is not disabled or this is the unknown parameter
     */
    boolean isDisabledParameter() {
        FormControl[] controls = getControls();
        for (FormControl control : controls) {
            if (!control.isDisabled()) {
                return false;
            }
        }
        return !this.isUnknown();
    }

    /**
     * is this a read only parameter
     *
     * @return false if one of the controls is not read only or this is the unknown parameter
     */
    boolean isReadOnlyParameter() {
        FormControl[] controls = getControls();
        for (FormControl control : controls) {
            if (!control.isReadOnly()) {
                return false;
            }
        }
        return !this.isUnknown();
    }

    /**
     * is this a hidden parameter?
     *
     * @return false if one of the controls is not hidden or this is the unknown parameter
     */
    public boolean isHiddenParameter() {
        FormControl[] controls = getControls();
        for (FormControl control : controls) {
            if (!control.isHidden()) {
                return false;
            }
        }
        return !this.isUnknown();
    }

    private RadioGroupFormControl getRadioGroup(WebForm form) {
        if (_group == null) {
            _group = new RadioGroupFormControl(form);
            _controlList.add(_group);
        }
        return _group;
    }

    // ============================= exception class UnusedParameterValueException
    // ======================================

    /**
     * This exception is thrown on an attempt to set a parameter to a value not permitted to it by the form.
     **/
    public class UnusedParameterValueException extends IllegalRequestParameterException {

        private static final long serialVersionUID = 1L;

        /**
         * construct an exception for an unused parameter with the given name and the value that is bad
         *
         * @param parameterName
         * @param badValue
         */
        UnusedParameterValueException(String parameterName, String badValue) {
            _parameterName = parameterName;
            _badValue = badValue;
        }

        /**
         * get the message for this exception
         *
         * @return the message
         */
        @Override
        public String getMessage() {
            StringBuilder sb = new StringBuilder(HttpUnitUtils.DEFAULT_TEXT_BUFFER_SIZE);
            sb.append("Attempted to assign to parameter '").append(_parameterName);
            sb.append("' the extraneous value '").append(_badValue).append("'.");
            return sb.toString();
        }

        private String _parameterName;
        private String _badValue;
    }

    // ============================= exception class UnusedUploadFileException ======================================

    /**
     * This exception is thrown on an attempt to upload more files than permitted by the form.
     **/
    class UnusedUploadFileException extends IllegalRequestParameterException {

        private static final long serialVersionUID = 1L;

        /**
         * construct a new UnusedUploadFileException exception base on the parameter Name the number of files expected
         * and supplied
         *
         * @param parameterName
         * @param numFilesExpected
         * @param numFilesSupplied
         */
        UnusedUploadFileException(String parameterName, int numFilesExpected, int numFilesSupplied) {
            _parameterName = parameterName;
            _numExpected = numFilesExpected;
            _numSupplied = numFilesSupplied;
        }

        /**
         * get the message for this exception
         */
        @Override
        public String getMessage() {
            StringBuilder sb = new StringBuilder(HttpUnitUtils.DEFAULT_TEXT_BUFFER_SIZE);
            sb.append("Attempted to upload ").append(_numSupplied).append(" files using parameter '")
                    .append(_parameterName);
            if (_numExpected == 0) {
                sb.append("' which is not a file parameter.");
            } else {
                sb.append("' which only has room for ").append(_numExpected).append('.');
            }
            return sb.toString();
        }

        private String _parameterName;
        private int _numExpected;
        private int _numSupplied;
    }

    // ============================= exception class IllegalCheckboxParameterException
    // ======================================

    /**
     * This exception is thrown on an attempt to set a parameter to a value not permitted to it by the form.
     **/
    static class IllegalCheckboxParameterException extends IllegalRequestParameterException {

        private static final long serialVersionUID = 1L;

        IllegalCheckboxParameterException(String parameterName, String methodName) {
            _parameterName = parameterName;
            _methodName = methodName;
        }

        @Override
        public String getMessage() {
            StringBuilder sb = new StringBuilder(HttpUnitUtils.DEFAULT_TEXT_BUFFER_SIZE);
            sb.append("Attempted to invoke method '").append(_methodName);
            sb.append("' for parameter '").append(_parameterName).append("', which is not a unique checkbox control.");
            return sb.toString();
        }

        private String _parameterName;
        private String _methodName;
    }

}

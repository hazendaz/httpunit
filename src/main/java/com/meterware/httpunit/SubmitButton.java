/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

import com.meterware.httpunit.dom.HTMLControl;
import com.meterware.httpunit.protocol.ParameterProcessor;

import java.io.IOException;

import org.xml.sax.SAXException;

/**
 * This class represents a submit button in an HTML form.
 **/
public class SubmitButton extends Button {

    /** The fake. */
    private boolean _fake;

    @Override
    public String getType() {
        return isImageButton() ? IMAGE_BUTTON_TYPE : SUBMIT_BUTTON_TYPE;
    }

    /**
     * Returns true if this submit button is an image map.
     *
     * @return true, if is image button
     */
    public boolean isImageButton() {
        return _isImageButton;
    }

    /**
     * Performs the action associated with clicking this button after running any 'onClick' script. For a submit button
     * this typically submits the form.
     *
     * @param x
     *            the x
     * @param y
     *            the y
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws SAXException
     *             the SAX exception
     */
    public void click(int x, int y) throws IOException, SAXException {
        if (!isImageButton()) {
            throw new IllegalStateException("May only specify positions for an image button");
        }
        doOnClickSequence(x, y);
    }

    // --------------------------------- Button methods ----------------------------------------------

    /**
     * do the button Action
     *
     * @param x
     *            - x coordinate
     * @param y
     *            - y coordinate
     */
    @Override
    protected void doButtonAction(int x, int y) throws IOException, SAXException {
        getForm().doFormSubmit(this, x, y);
    }

    // ------------------------------------ Object methods ----------------------------------------

    @Override
    public String toString() {
        return "Submit with " + getName() + "=" + getValue();
    }

    @Override
    public int hashCode() {
        return getName().hashCode() + getValue().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return getClass().equals(o.getClass()) && equals((SubmitButton) o);
    }

    // ------------------------------------------ package members ----------------------------------

    /**
     * Instantiates a new submit button.
     *
     * @param form
     *            the form
     * @param control
     *            the control
     */
    SubmitButton(WebForm form, HTMLControl control) {
        super(form, control);
        _isImageButton = control.getType().equalsIgnoreCase(IMAGE_BUTTON_TYPE);
    }

    /**
     * Instantiates a new submit button.
     *
     * @param form
     *            the form
     */
    SubmitButton(WebForm form) {
        super(form);
        _isImageButton = false;
    }

    /**
     * Creates the fake submit button.
     *
     * @param form
     *            the form
     *
     * @return the submit button
     */
    static SubmitButton createFakeSubmitButton(WebForm form) {
        return new SubmitButton(form, /* fake */ true);
    }

    /**
     * Instantiates a new submit button.
     *
     * @param form
     *            the form
     * @param fake
     *            the fake
     */
    private SubmitButton(WebForm form, boolean fake) {
        this(form);
        _fake = fake;
    }

    /**
     * getter for the fake flag Returns true for synthetic submit buttons, created by HttpUnit in forms that contain no
     * submit buttons, or used during {@link WebForm#submitNoButton()} call.
     *
     * @return - whether this button is a faked button inserted by httpunit
     */
    public boolean isFake() {
        return _fake;
    }

    /**
     * flag that the button was pressed.
     *
     * @param pressed
     *            the new pressed
     */
    void setPressed(boolean pressed) {
        _pressed = pressed;
    }

    /**
     * Sets the location.
     *
     * @param x
     *            the x
     * @param y
     *            the y
     */
    void setLocation(int x, int y) {
        _x = x;
        _y = y;
    }

    // --------------------------------- FormControl methods
    // ----------------------------------------------------------------

    /**
     * Returns the current value(s) associated with this control. These values will be transmitted to the server if the
     * control is 'successful'.
     **/
    @Override
    protected String[] getValues() {
        return isDisabled() || !_pressed ? NO_VALUE : toArray(getValue());
    }

    /** should we allow unnamed Image Buttons?. */
    private static boolean allowUnnamedImageButton = false;

    /**
     * Checks if is allow unnamed image button.
     *
     * @return the allowUnnamedImageButton
     */
    public static boolean isAllowUnnamedImageButton() {
        return allowUnnamedImageButton;
    }

    /**
     * Sets the allow unnamed image button.
     *
     * @param allowUnnamedImageButton
     *            the allowUnnamedImageButton to set
     */
    public static void setAllowUnnamedImageButton(boolean allowUnnamedImageButton) {
        SubmitButton.allowUnnamedImageButton = allowUnnamedImageButton;
    }

    /**
     * return whether this is a validImageButton.
     *
     * @return true if it is an image Button
     */
    public boolean isValidImageButton() {
        String buttonName = getName();
        boolean valid = this.isImageButton();
        if (!allowUnnamedImageButton) {
            valid = valid && buttonName != null && buttonName.length() > 0;
        }
        return valid;
    }

    /**
     * return the name of the positionParameter for this button (if this is an image Button).
     *
     * @param direction
     *            e.g. "x" or "y"
     *
     * @return the name e.g. "image.x" or just "x"
     */
    public String positionParameterName(String direction) {
        // [ 1443333 ] Allow unnamed Image input elments to submit x,y values
        String buttonName = getName();
        String buttonPrefix = "";
        if (buttonName != null && buttonName.length() > 0) {
            buttonPrefix = buttonName + ".";
        }
        return buttonPrefix + direction;
    }

    /**
     * addValues if not disabled and pressed
     *
     * @param processor
     *            - the ParameterProcessor used
     * @param characterSet
     *            - the active character set
     *
     * @throws IOException
     *             if addValues fails
     */
    @Override
    protected void addValues(ParameterProcessor processor, String characterSet) throws IOException {
        if (_pressed && !isDisabled()) {
            String buttonName = getName();
            if (buttonName != null && buttonName.length() > 0 && getValue().length() > 0) {
                processor.addParameter(getName(), getValue(), characterSet);
            }
            if (isValidImageButton()) {
                processor.addParameter(positionParameterName("x"), Integer.toString(_x), characterSet);
                processor.addParameter(positionParameterName("y"), Integer.toString(_y), characterSet);
            }
        } // if
    }

    // ------------------------------------------ private members ----------------------------------

    /** The value. */
    private String[] _value = new String[1];

    /** The is image button. */
    private final boolean _isImageButton;

    /** The pressed. */
    private boolean _pressed;

    /** The x. */
    private int _x;

    /** The y. */
    private int _y;

    /**
     * To array.
     *
     * @param value
     *            the value
     *
     * @return the string[]
     */
    private String[] toArray(String value) {
        _value[0] = value;
        return _value;
    }

    /**
     * Equals.
     *
     * @param button
     *            the button
     *
     * @return true, if successful
     */
    private boolean equals(SubmitButton button) {
        return getName().equals(button.getName()) && (getName().isEmpty() || getValue().equals(button.getValue()));
    }

    @Override
    public void throwDisabledException() {
        throw new DisabledSubmitButtonException(this);
    }

    /**
     * This exception is thrown on an attempt to define a form request with a button not defined on that form.
     **/
    class DisabledSubmitButtonException extends DisabledButtonException {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /**
         * Instantiates a new disabled submit button exception.
         *
         * @param button
         *            the button
         */
        DisabledSubmitButtonException(SubmitButton button) {
            super(button);
        }

        @Override
        public String getMessage() {
            return "The specified button (name='" + _name + "' value='" + _value
                    + "' is disabled and may not be used to submit this form.";
        }

    }

}

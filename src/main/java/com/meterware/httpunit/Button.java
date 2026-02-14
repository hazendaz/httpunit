/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

import com.meterware.httpunit.dom.HTMLButtonElementImpl;
import com.meterware.httpunit.dom.HTMLControl;
import com.meterware.httpunit.dom.HTMLInputElementImpl;
import com.meterware.httpunit.protocol.ParameterProcessor;
import com.meterware.httpunit.scripting.ScriptableDelegate;

import java.io.IOException;

import org.xml.sax.SAXException;

/**
 * A button in a form.
 **/
public class Button extends FormControl {

    /** The Constant WITH_ID. */
    public static final HTMLElementPredicate WITH_ID;

    /** The Constant WITH_LABEL. */
    public static final HTMLElementPredicate WITH_LABEL;

    /** The base response. */
    private WebResponse _baseResponse;

    /** The was enabled. */
    // remember the last verifyEnabled result
    private boolean _wasEnabled = true;

    @Override
    public String getType() {
        return BUTTON_TYPE;
    }

    /**
     * Instantiates a new button.
     *
     * @param form
     *            the form
     */
    Button(WebForm form) {
        super(form);
    }

    /**
     * construct a button from the given html control and assign the event handlers for onclick, onmousedown and
     * onmouseup.
     *
     * @param form
     *            the form
     * @param control
     *            the control
     */
    Button(WebForm form, HTMLControl control) {
        super(form, control);
    }

    /**
     * Instantiates a new button.
     *
     * @param response
     *            the response
     * @param control
     *            the control
     */
    Button(WebResponse response, HTMLControl control) {
        super(null, control);
        _baseResponse = response;
    }

    /**
     * Returns the value associated with this button.
     *
     * @return the value
     */
    public String getValue() {
        return emptyIfNull(getNode() instanceof HTMLInputElementImpl ? ((HTMLInputElementImpl) getNode()).getValue()
                : ((HTMLButtonElementImpl) getNode()).getValue());
    }

    /**
     * the onClickSequence for this button.
     *
     * @param x
     *            the x
     * @param y
     *            the y
     *
     * @return true if the even was handled
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws SAXException
     *             the SAX exception
     */
    protected boolean doOnClickSequence(int x, int y) throws IOException, SAXException {
        verifyButtonEnabled();
        boolean result = doOnClickEvent();
        if (result) {
            doButtonAction(x, y);
        }
        this.rememberEnableState();
        return result;
    }

    /**
     * Performs the action associated with clicking this button after running any 'onClick' script. For a submit button
     * this typically submits the form.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws SAXException
     *             the SAX exception
     */
    public void click() throws IOException, SAXException {
        doOnClickSequence(0, 0);
    }

    /**
     * Was enabled.
     *
     * @return true, if successful
     */
    boolean wasEnabled() {
        return _wasEnabled;
    }

    /**
     * remember wether the button was enabled.
     */
    public void rememberEnableState() {
        _wasEnabled = !isDisabled();
    }

    /**
     * verifyButtonEnabled.
     */
    protected void verifyButtonEnabled() {
        rememberEnableState();
        if (isDisabled()) {
            throwDisabledException();
        }
    }

    /**
     * throw an exception that I'm disbled.
     */
    public void throwDisabledException() {
        throw new DisabledButtonException(this);
    }

    /**
     * This exception is thrown on an attempt to click on a button disabled button.
     */
    class DisabledButtonException extends IllegalStateException {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /**
         * Instantiates a new disabled button exception.
         *
         * @param button
         *            the button
         */
        DisabledButtonException(Button button) {
            _name = button.getName();
            _value = button.getValue();
        }

        @Override
        public String getMessage() {
            return "Button" + (getName().isEmpty() ? "" : " '" + getName() + "'")
                    + " is disabled and may not be clicked.";
        }

        /** The name. */
        protected String _name;

        /** The value. */
        protected String _value;

    }

    /**
     * Returns true if this button is disabled, meaning that it cannot be clicked.
     **/
    @Override
    public boolean isDisabled() {
        return super.isDisabled();
    }

    /**
     * Perform the normal action of this button.
     *
     * @param x
     *            - the x coordinate
     * @param y
     *            - the y coordinate
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws SAXException
     *             the SAX exception
     */
    protected void doButtonAction(int x, int y) throws IOException, SAXException {
        // pseudo - abstract
    }

    /**
     * Perform the normal action of this button. delegate to the x-y version
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws SAXException
     *             the SAX exception
     */
    protected void doButtonAction() throws IOException, SAXException {
        doButtonAction(0, 0);
    }

    // -------------------------------------------------- FormControl methods
    // -----------------------------------------------

    @Override
    protected String[] getValues() {
        return new String[0];
    }

    @Override
    protected void addValues(ParameterProcessor processor, String characterSet) throws IOException {
    }

    @Override
    public ScriptableDelegate newScriptable() {
        return new Scriptable();
    }

    @Override
    public ScriptableDelegate getParentDelegate() {
        if (getForm() != null) {
            return super.getParentDelegate();
        }
        return _baseResponse.getDocumentScriptable();
    }

    /**
     * The Class Scriptable.
     */
    class Scriptable extends FormControl.Scriptable {

        @Override
        public void click() throws IOException, SAXException {
            doButtonAction();
        }
    }

    static {
        WITH_ID = (button, id) -> ((Button) button).getID().equals(id);

        WITH_LABEL = (button, label) -> ((Button) button).getValue().equals(label);

    }
}

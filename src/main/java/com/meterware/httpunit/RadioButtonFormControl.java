/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

import com.meterware.httpunit.dom.HTMLInputElementImpl;

/**
 * The Class RadioButtonFormControl.
 */
public class RadioButtonFormControl extends BooleanFormControl {

    @Override
    public String getType() {
        return RADIO_BUTTON_TYPE;
    }

    /**
     * Instantiates a new radio button form control.
     *
     * @param form
     *            the form
     * @param element
     *            the element
     */
    public RadioButtonFormControl(WebForm form, HTMLInputElementImpl element) {
        super(form, element);
    }

    /**
     * Returns true if only one control of this kind can have a value.
     **/
    @Override
    public boolean isExclusive() {
        return true;
    }

    @Override
    public String getQueryValue() {
        return getValueAttribute();
    }
}

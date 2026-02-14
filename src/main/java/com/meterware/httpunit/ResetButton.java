/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

import com.meterware.httpunit.dom.HTMLControl;

/**
 * Represents a form 'reset' button.
 **/
public class ResetButton extends Button {

    @Override
    public String getType() {
        return RESET_BUTTON_TYPE;
    }

    /**
     * Instantiates a new reset button.
     *
     * @param form
     *            the form
     * @param control
     *            the control
     */
    ResetButton(WebForm form, HTMLControl control) {
        super(form, control);
    }

    /**
     * overridden button action
     */
    @Override
    protected void doButtonAction(int x, int y) {
        getForm().reset();
    }
}

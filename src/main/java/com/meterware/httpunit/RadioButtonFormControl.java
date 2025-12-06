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

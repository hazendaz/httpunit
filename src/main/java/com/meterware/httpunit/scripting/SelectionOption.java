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
package com.meterware.httpunit.scripting;

/**
 * The Interface SelectionOption.
 */
public interface SelectionOption {

    /**
     * Gets the text.
     *
     * @return the text
     */
    String getText();

    /**
     * Sets the text.
     *
     * @param text
     *            the new text
     */
    void setText(String text);

    /**
     * Gets the value.
     *
     * @return the value
     */
    String getValue();

    /**
     * Sets the value.
     *
     * @param value
     *            the new value
     */
    void setValue(String value);

    /**
     * Checks if is default selected.
     *
     * @return true, if is default selected
     */
    boolean isDefaultSelected();

    /**
     * Checks if is selected.
     *
     * @return true, if is selected
     */
    boolean isSelected();

    /**
     * Sets the selected.
     *
     * @param selected
     *            the new selected
     */
    void setSelected(boolean selected);

    /**
     * Gets the index.
     *
     * @return the index
     */
    int getIndex();

    /**
     * Initialize.
     *
     * @param text
     *            the text
     * @param value
     *            the value
     * @param defaultSelected
     *            the default selected
     * @param selected
     *            the selected
     */
    void initialize(String text, String value, boolean defaultSelected, boolean selected);

}

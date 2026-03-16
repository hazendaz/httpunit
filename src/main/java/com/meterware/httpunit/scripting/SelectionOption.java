/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
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

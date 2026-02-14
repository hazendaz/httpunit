/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

/**
 * Interface for an object to supply user responses to dialogs.
 **/
public interface DialogResponder {

    /**
     * Invoked when the user agent needs to display a confirmation dialog. This method should return true to accept the
     * proposed action or false to reject it.
     *
     * @param confirmationPrompt
     *            the confirmation prompt
     *
     * @return the confirmation
     */
    boolean getConfirmation(String confirmationPrompt);

    /**
     * Invoked when the user agent needs to display a generic dialog and obtain a user response. This method should
     * return the user's answer.
     *
     * @param prompt
     *            the prompt
     * @param defaultResponse
     *            the default response
     *
     * @return the user response
     */
    String getUserResponse(String prompt, String defaultResponse);

}

/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

/**
 * The Class DialogAdapter.
 */
public class DialogAdapter implements DialogResponder {

    /**
     * Invoked when the user agent needs to display a confirmation dialog. This default implementation confirms all
     * dialogs.
     */
    @Override
    public boolean getConfirmation(String confirmationPrompt) {
        return true;
    }

    /**
     * Invoked when the user agent needs to display a generic dialog and obtain a user response. This default
     * implementation returns the default response.
     */
    @Override
    public String getUserResponse(String prompt, String defaultResponse) {
        return defaultResponse;
    }
}

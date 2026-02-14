/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.scripting;

/**
 * The Interface FormScriptable.
 */
public interface FormScriptable extends ScriptingEventHandler {

    /**
     * set the action.
     *
     * @param newAction
     *            the new action
     */
    void setAction(String newAction);

    /**
     * set the value of a parameter.
     *
     * @param name
     *            - the name of the parameter to set
     * @param value
     *            - the value to use for the parameter
     */
    void setParameterValue(String name, String value);
}

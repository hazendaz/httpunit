/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.scripting;

/**
 * interface for every object that may have excutable events and their scripts attached.
 */
public interface ScriptingEventHandler {

    /**
     * run the Script for the given Event.
     *
     * @param eventScript
     *            the event script
     *
     * @return true if the script is empty or the result of the script
     *
     * @deprecated since 1.7
     */
    @Deprecated
    boolean doEvent(String eventScript);

    /**
     * run the Script for the given Event.
     *
     * @param eventScript
     *            the event script
     *
     * @return true if the script is empty or the result of the script
     */
    boolean doEventScript(String eventScript);

    /**
     * handle the event with the given name by getting the attribute and then executing the eventScript for it.
     *
     * @param eventName
     *            the event name
     *
     * @return the result of doEventScript
     */
    boolean handleEvent(String eventName);

}

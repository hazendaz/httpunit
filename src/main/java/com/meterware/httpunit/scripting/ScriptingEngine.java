/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.scripting;

/**
 * The Interface ScriptingEngine.
 */
public interface ScriptingEngine extends ScriptingHandler {

    /**
     * Returns a new scripting engine for the specified delegate.
     *
     * @param child
     *            the child
     *
     * @return the scripting engine
     */
    ScriptingEngine newScriptingEngine(ScriptableDelegate child);

    /**
     * Clears any cached values, permitting them to be recomputed as needed.
     */
    @Override
    void clearCaches();

}

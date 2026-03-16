/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.scripting;

import com.meterware.httpunit.HTMLElement;

/**
 * An interface for objects which will be accessible via scripting.
 **/
public abstract class ScriptableDelegate implements ScriptingHandler {

    /** The script engine. */
    private ScriptingEngine _scriptEngine;

    /** a dummy ScriptingEngine implementation. */
    public static final ScriptingEngine NULL_SCRIPT_ENGINE = new ScriptingEngine() {
        @Override
        public boolean supportsScriptLanguage(String language) {
            return false;
        }

        @Override
        public String runScript(String language, String script) {
            return "";
        }

        @Override
        public boolean doEventScript(String eventScript) {
            return true;
        }

        @Override
        public boolean doEvent(String eventScript) {
            return true;
        }

        @Override
        public boolean handleEvent(String eventName) {
            return true;
        }

        @Override
        public Object evaluateExpression(String urlString) {
            return null;
        }

        @Override
        public ScriptingEngine newScriptingEngine(ScriptableDelegate child) {
            return this;
        }

        @Override
        public void clearCaches() {
        }
    };

    @Override
    public boolean supportsScriptLanguage(String language) {
        return getScriptEngine().supportsScriptLanguage(language);
    }

    /**
     * handle the event that has the given script attached by compiling the eventScript as a function and executing it
     *
     * @param eventScript
     *            - the script to use
     *
     * @deprecated since 1.7 - use doEventScript instead
     */
    @Deprecated
    @Override
    public boolean doEvent(String eventScript) {
        return doEventScript(eventScript);
    }

    /**
     * Executes the specified scripted event.
     *
     * @param eventScript
     *            - the eventScript to execute
     *
     * @return true if the event has been handled.
     **/
    @Override
    public boolean doEventScript(String eventScript) {
        return eventScript.isEmpty() || getScriptEngine().doEventScript(eventScript);
    }

    /**
     * Executes the event Handler script for the specified event (such as onchange, onmousedown, onclick, onmouseup) if
     * it is defined.
     *
     * @param eventName
     *            the name of the event for which a handler should be run.
     *
     * @return whether the event with the given name was handled
     */
    @Override
    public boolean handleEvent(String eventName) {
        String eventScript = (String) get(eventName);
        return doEventScript(eventScript);
    }

    /**
     * Executes the specified script, returning any intended replacement text.
     *
     * @return the replacement text, which may be empty.
     **/
    @Override
    public String runScript(String language, String script) {
        return script.isEmpty() ? "" : getScriptEngine().runScript(language, script);
    }

    /**
     * Evaluates the specified javascript expression, returning its value.
     **/
    @Override
    public Object evaluateExpression(String urlString) {
        if (urlString.isEmpty()) {
            return null;
        }
        return getScriptEngine().evaluateExpression(urlString);
    }

    @Override
    public void clearCaches() {
        getScriptEngine().clearCaches();
    }

    /**
     * Returns the value of the named property. Will return null if the property does not exist.
     *
     * @param propertyName
     *            the property name
     *
     * @return the object
     */
    public Object get(String propertyName) {
        return null;
    }

    /**
     * Returns the value of the index property. Will return null if the property does not exist.
     *
     * @param index
     *            the index
     *
     * @return the object
     */
    public Object get(int index) {
        return null;
    }

    /**
     * Sets the value of the named property. Will throw a runtime exception if the property does not exist or cannot
     * accept the specified value.
     *
     * @param propertyName
     *            the property name
     * @param value
     *            the value
     */
    public void set(String propertyName, Object value) {
        throw new RuntimeException("No such property: " + propertyName);
    }

    /**
     * Specifies the scripting engine to be used.
     *
     * @param scriptEngine
     *            the new script engine
     */
    public void setScriptEngine(ScriptingEngine scriptEngine) {
        _scriptEngine = scriptEngine;
    }

    /**
     * Gets the script engine.
     *
     * @return the script engine
     */
    public ScriptingEngine getScriptEngine() {
        return _scriptEngine != null ? _scriptEngine : NULL_SCRIPT_ENGINE;
    }

    /**
     * Gets the script engine.
     *
     * @param child
     *            the child
     *
     * @return the script engine
     */
    public ScriptingEngine getScriptEngine(ScriptableDelegate child) {
        return getScriptEngine().newScriptingEngine(child);
    }

    /**
     * Gets the delegates.
     *
     * @param elements
     *            the elements
     *
     * @return the delegates
     */
    protected ScriptableDelegate[] getDelegates(final HTMLElement[] elements) {
        ScriptableDelegate[] result = new ScriptableDelegate[elements.length];
        for (int i = 0; i < elements.length; i++) {
            result[i] = (ScriptableDelegate) elements[i].getScriptingHandler();
        }
        return result;
    }
}

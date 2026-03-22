/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.dom;

import com.meterware.httpunit.javascript.ScriptingEngineImpl;
import com.meterware.httpunit.scripting.ScriptableDelegate;
import com.meterware.httpunit.scripting.ScriptingEngine;

import org.mozilla.javascript.Scriptable;

/**
 * The Class AbstractDomComponent.
 */
public abstract class AbstractDomComponent extends ScriptingEngineImpl implements Scriptable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The anonymous function num. */
    private static int _anonymousFunctionNum;

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public ScriptingEngine newScriptingEngine(ScriptableDelegate child) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearCaches() {
    }

    @Override
    public boolean has(String name, Scriptable start) {
        return super.has(name, start) || ScriptingSupport.hasNamedProperty(this, getJavaPropertyName(name), start);
    }

    @Override
    public Object get(String propertyName, Scriptable scriptable) {
        Object result = super.get(propertyName, scriptable);
        if (result != NOT_FOUND) {
            return result;
        }

        return ScriptingSupport.getNamedProperty(this, getJavaPropertyName(propertyName), scriptable);
    }

    /**
     * Gets the java property name.
     *
     * @param propertyName
     *            the property name
     *
     * @return the java property name
     */
    protected String getJavaPropertyName(String propertyName) {
        return propertyName;
    }

    @Override
    public void put(String propertyName, Scriptable initialObject, Object value) {
        super.put(propertyName, initialObject, value);
        ScriptingSupport.setNamedProperty(this, getJavaPropertyName(propertyName), value);
    }

    /**
     * Creates the anonymous function name.
     *
     * @return the string
     */
    protected static String createAnonymousFunctionName() {
        return "anon_" + ++_anonymousFunctionNum;
    }
}

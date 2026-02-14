/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.javascript;

import com.meterware.httpunit.HTMLElement;
import com.meterware.httpunit.HttpUnitUtils;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.scripting.ScriptableDelegate;
import com.meterware.httpunit.scripting.ScriptingEngineFactory;
import com.meterware.httpunit.scripting.ScriptingHandler;

/**
 * An implementation of the scripting engine factory which selects a Rhino-based implementation of JavaScript.
 **/
public class JavaScriptEngineFactory implements ScriptingEngineFactory {

    @Override
    public boolean isEnabled() {
        try {
            Class.forName("org.mozilla.javascript.Context");
            return true;
        } catch (Exception e) {
            System.err.println("Rhino classes (js.jar) not found - Javascript disabled");
            return false;
        }
    }

    @Override
    public void associate(WebResponse response) {
        try {
            JavaScript.run(response);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            HttpUnitUtils.handleException(e);
            throw new RuntimeException(e.toString());
        }
    }

    @Override
    public void load(WebResponse response) {
        try {
            JavaScript.load(response);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.toString());
        }
    }

    @Override
    public void setThrowExceptionsOnError(boolean throwExceptions) {
        JavaScript.setThrowExceptionsOnError(throwExceptions);
    }

    @Override
    public boolean isThrowExceptionsOnError() {
        return JavaScript.isThrowExceptionsOnError();
    }

    @Override
    public String[] getErrorMessages() {
        return ScriptingEngineImpl.getErrorMessages();
    }

    /**
     * delegate the handling for Script exceptions
     */
    @Override
    public void handleScriptException(Exception e, String badScript) {
        ScriptingEngineImpl.handleScriptException(e, badScript);
    }

    @Override
    public void clearErrorMessages() {
        ScriptingEngineImpl.clearErrorMessages();
    }

    @Override
    public ScriptingHandler createHandler(HTMLElement elementBase) {
        ScriptableDelegate delegate = elementBase.newScriptable();
        delegate.setScriptEngine(elementBase.getParentDelegate().getScriptEngine(delegate));
        return delegate;
    }

    @Override
    public ScriptingHandler createHandler(WebResponse response) {
        return response.createJavascriptScriptingHandler();
    }

}

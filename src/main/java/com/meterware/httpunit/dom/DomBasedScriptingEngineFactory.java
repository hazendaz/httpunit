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
package com.meterware.httpunit.dom;

import com.meterware.httpunit.HTMLElement;
import com.meterware.httpunit.HttpUnitUtils;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.javascript.JavaScript;
import com.meterware.httpunit.javascript.ScriptingEngineImpl;
import com.meterware.httpunit.scripting.ScriptingEngineFactory;
import com.meterware.httpunit.scripting.ScriptingHandler;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.html.HTMLDocument;

/**
 * The scripting engine factory which relies directly on the DOM.
 */
public class DomBasedScriptingEngineFactory implements ScriptingEngineFactory {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(DomBasedScriptingEngineFactory.class);

    /**
     * check whether this ScriptingEngineFactory is enabled
     */
    @Override
    public boolean isEnabled() {
        try {
            Class.forName("org.mozilla.javascript.Context");
            return true;
        } catch (Exception e) {
            logger.warn("Rhino classes (js.jar) not found - Javascript disabled");
            return false;
        }
    }

    /**
     * associate me with a webresponse
     *
     * @param response
     *            - the WebResponse to use
     */
    @Override
    public void associate(WebResponse response) {
        try {
            // JavaScript.run( response ); // can't do this (yet?)
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            HttpUnitUtils.handleException(e);
            throw new RuntimeException(e.toString());
        }
    }

    /**
     * load.
     *
     * @param response
     *            the response
     */
    @Override
    public void load(WebResponse response) {
        Function onLoadEvent = null;
        try {
            Context context = Context.enter();
            context.initStandardObjects(null);

            HTMLDocument htmlDocument = ((DomWindow) response.getScriptingHandler()).getDocument();
            if (!(htmlDocument instanceof HTMLDocumentImpl)) {
                return;
            }

            HTMLBodyElementImpl body = (HTMLBodyElementImpl) htmlDocument.getBody();
            if (body == null) {
                return;
            }
            onLoadEvent = body.getOnloadEvent();
            if (onLoadEvent == null) {
                return;
            }
            onLoadEvent.call(context, body, body, new Object[0]);
        } catch (JavaScriptException | EcmaError ee) {
            // throw ee;
            ScriptingEngineImpl.handleScriptException(ee, onLoadEvent.toString());
        } finally {
            Context.exit();
        }
    }

    /**
     * setter for the throwExceptions flag
     *
     * @param throwExceptions
     *            - true if Exceptions should be thrown
     */
    @Override
    public void setThrowExceptionsOnError(boolean throwExceptions) {
        JavaScript.setThrowExceptionsOnError(throwExceptions);
    }

    /**
     * getter for the throwExceptions flag
     *
     * @return - true if Exceptions should be thrown
     */
    @Override
    public boolean isThrowExceptionsOnError() {
        return JavaScript.isThrowExceptionsOnError();
    }

    @Override
    public String[] getErrorMessages() {
        return ScriptingEngineImpl.getErrorMessages();
    }

    @Override
    public void clearErrorMessages() {
        ScriptingEngineImpl.clearErrorMessages();
    }

    @Override
    public ScriptingHandler createHandler(HTMLElement elementBase) {
        return (ScriptingHandler) elementBase.getNode();
    }

    @Override
    public ScriptingHandler createHandler(WebResponse response) {
        return response.createDomScriptingHandler();
    }

    /**
     * handle Exceptions
     *
     * @param e
     *            - the exception to handle
     * @param badScript
     *            - the script that caused the problem
     */
    @Override
    public void handleScriptException(Exception e, String badScript) {
        ScriptingEngineImpl.handleScriptException(e, badScript);
    }
}

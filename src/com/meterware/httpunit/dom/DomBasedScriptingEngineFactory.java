package com.meterware.httpunit.dom;
/********************************************************************************************************************
* $Id$
*
* Copyright (c) 2006-2007, Russell Gold
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
*
*******************************************************************************************************************/
import com.meterware.httpunit.scripting.ScriptingEngineFactory;
import com.meterware.httpunit.scripting.ScriptingHandler;
import com.meterware.httpunit.scripting.ScriptingEngine;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.HTMLElement;

import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.html.HTMLDocument;
import org.w3c.dom.html.HTMLBodyElement;
import org.xml.sax.SAXException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;

/**
 * The scripting engine factory which relies directly on the DOM.
 */
public class DomBasedScriptingEngineFactory implements ScriptingEngineFactory {

    public boolean isEnabled() {
        try {
            Class.forName( "org.mozilla.javascript.Context" );
            return true;
        } catch (Exception e) {
            Logger.getLogger( "httpunit.org" ).warning( "Rhino classes (js.jar) not found - Javascript disabled" );
            return false;
        }
    }


    public void associate( WebResponse response ) {
    }


    public void load( WebResponse response ) {
        try {
            Context context = Context.enter();
            context.initStandardObjects( null );

            HTMLDocument htmlDocument = ((DomWindow) response.getScriptingHandler()).getDocument();
            if (!(htmlDocument instanceof HTMLDocumentImpl)) return;

            HTMLBodyElementImpl body = (HTMLBodyElementImpl) htmlDocument.getBody();
            if (body == null) return;
            Function onLoadEvent = body.getOnloadEvent();
            if (onLoadEvent == null) return;
            onLoadEvent.call( context, body, body, new Object[0] );
        } catch (JavaScriptException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            Context.exit();
        }
    }


    public void setThrowExceptionsOnError( boolean throwExceptions ) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    public boolean isThrowExceptionsOnError() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }


    public String[] getErrorMessages() {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }


    public void clearErrorMessages() {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    public ScriptingHandler createHandler( HTMLElement elementBase ) {
        return (ScriptingHandler) elementBase.getNode();
    }


    public ScriptingHandler createHandler( WebResponse response ) {
        return response.createDomScriptingHandler();
    }
}

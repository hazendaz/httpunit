package com.meterware.httpunit.javascript;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2002, Russell Gold
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
import com.meterware.httpunit.HTMLPage;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebImage;
import com.meterware.httpunit.ScriptException;

import com.meterware.httpunit.scripting.ScriptingEngine;
import com.meterware.httpunit.scripting.ScriptableDelegate;
import com.meterware.httpunit.scripting.SelectionOption;
import com.meterware.httpunit.scripting.SelectionOptions;
import com.meterware.httpunit.scripting.NamedDelegate;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.ArrayList;
import java.io.IOException;

import org.mozilla.javascript.*;
import org.xml.sax.SAXException;


/**
 *
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 **/
public class JavaScript {

    private final static Object[] NO_ARGS = new Object[0];

    private static boolean _throwExceptionsOnError = true;

    private static ArrayList _errorMessages = new ArrayList();


    static boolean isThrowExceptionsOnError() {
        return _throwExceptionsOnError;
    }


    static void setThrowExceptionsOnError( boolean throwExceptionsOnError ) {
        _throwExceptionsOnError = throwExceptionsOnError;
    }


    static void clearErrorMessages() {
        _errorMessages.clear();
    }


    static String[] getErrorMessages() {
        return (String[]) _errorMessages.toArray( new String[ _errorMessages.size() ] );
    }


    /**
     * Initiates JavaScript execution for the specified web response.
     */
    static void run( WebResponse response ) throws IllegalAccessException, InstantiationException,
            InvocationTargetException, ClassDefinitionException, NotAFunctionException,
            PropertyException, SAXException, JavaScriptException {
        Context context = Context.enter();
        Scriptable scope = context.initStandardObjects( null );
        initHTMLObjects( scope );

        Window w = (Window) context.newObject( scope, "Window" );
        w.initialize( null, response.getScriptableObject() );
    }


    private static void initHTMLObjects( Scriptable scope ) throws IllegalAccessException, InstantiationException,
            InvocationTargetException, ClassDefinitionException, PropertyException {
        ScriptableObject.defineClass( scope, Window.class );
        ScriptableObject.defineClass( scope, Document.class );
        ScriptableObject.defineClass( scope, Navigator.class );
        ScriptableObject.defineClass( scope, Link.class );
        ScriptableObject.defineClass( scope, Form.class );
        ScriptableObject.defineClass( scope, Control.class );
        ScriptableObject.defineClass( scope, Link.class );
        ScriptableObject.defineClass( scope, Image.class );
        ScriptableObject.defineClass( scope, Options.class );
        ScriptableObject.defineClass( scope, Option.class );
        ScriptableObject.defineClass( scope, ElementArray.class );
    }


    abstract static class JavaScriptEngine extends ScriptableObject implements ScriptingEngine {

        protected ScriptableDelegate _scriptable;


        public void executeScript( String script ) {
            try {
                script = script.trim();
                if (script.startsWith( "<!--" )) script = withoutFirstLine( script );
                Context.getCurrentContext().evaluateString( this, script, "httpunit", 0, null );
            } catch (Exception e) {
                handleScriptException( e, "Script '" + script + "'" );
            }
        }


        private String withoutFirstLine( String script ) {
            for (int i=0; i < script.length(); i++) {
                if (isLineTerminator( script.charAt(i) )) return script.substring( i ).trim();
            }
            return "";
        }


        private boolean isLineTerminator( char c ) {
            return c == 0x0A || c == 0x0D;
        }


        public boolean performEvent( String eventScript ) {
            try {
                final Context context = Context.getCurrentContext();
                context.setOptimizationLevel( -1 );
                Function f = context.compileFunction( this, "function x() { " + eventScript + "}", "httpunit", 0, null );
                Object result = f.call( context, this, this, NO_ARGS );
                return (result instanceof Boolean) ? ((Boolean) result).booleanValue() : true;
            } catch (Exception e) {
                handleScriptException( e, "Event '" + eventScript + "'" );
                return false;
            }
        }

        /**
         * Evaluates the specified string as JavaScript. Will return null if the script has no return value.
         */
        public String getURLContents( String urlString ) {
            try {
                Object result = Context.getCurrentContext().evaluateString( this, urlString, "httpunit", 0, null );
                return (result == null || result instanceof Undefined) ? null : result.toString();
            } catch (Exception e) {
                handleScriptException( e, "URL '" + urlString + "'" );
                return null;
            }
        }


        private void handleScriptException( Exception e, String badScript ) {
            final String errorMessage = badScript + " failed: " + e;
            if (!(e instanceof EcmaError)) {
                throw new RuntimeException( errorMessage );
            } else if (isThrowExceptionsOnError()) {
                throw new ScriptException( errorMessage );
            } else {
                _errorMessages.add( errorMessage );
            }
        }


        void initialize( JavaScriptEngine parent, ScriptableDelegate scriptable )
                throws JavaScriptException, NotAFunctionException, PropertyException, SAXException {
            _scriptable = scriptable;
            _scriptable.setScriptEngine( this );
            if (parent != null) setParentScope( parent );
       }


        String getName() {
            return _scriptable instanceof NamedDelegate ? ((NamedDelegate) _scriptable).getName() : "";
        }


        public boolean has( String propertyName, Scriptable scriptable ) {
            return super.has( propertyName, scriptable ) ||
                    (_scriptable != null && _scriptable.get( propertyName ) != null);
        }


        public Object get( String propertyName, Scriptable scriptable ) {
            Object result = super.get( propertyName, scriptable );
            if (result != NOT_FOUND) return result;
            if (_scriptable == null) return NOT_FOUND;

            return convertIfNeeded( _scriptable.get( propertyName ) );

        }


        public Object get( int i, Scriptable scriptable ) {
            Object result = super.get( i, scriptable );
            if (result != NOT_FOUND) return result;
            if (_scriptable == null) return NOT_FOUND;

            return convertIfNeeded( _scriptable.get( i ) );
        }


        private Object convertIfNeeded( final Object property ) {
            if (property == null) return NOT_FOUND;

            try {
                if (property instanceof ScriptableDelegate[]) return toScriptable( (ScriptableDelegate[]) property );
                if (!(property instanceof ScriptableDelegate)) return property;
                return toScriptable( (ScriptableDelegate) property );
            } catch (PropertyException e) {
                throw new RuntimeException( e.toString() );
            } catch (NotAFunctionException e) {
                throw new RuntimeException( e.toString() );
            } catch (JavaScriptException e) {
                throw new RuntimeException( e.toString() );
            } catch (SAXException e) {
                throw new RuntimeException( e.toString() );
            }
        }


        private Object toScriptable( ScriptableDelegate[] list )
                throws PropertyException, NotAFunctionException, JavaScriptException, SAXException {
            Object[] delegates = new Object[ list.length ];
            for (int i = 0; i < delegates.length; i++) {
                delegates[i] = toScriptable( list[i] );
            }
            return Context.getCurrentContext().newArray( this, delegates );
        }


        public void put( String propertyName, Scriptable scriptable, Object value ) {
            if (_scriptable == null || _scriptable.get( propertyName ) == null) {
                super.put( propertyName, scriptable, value );
            } else {
                _scriptable.set( propertyName, value );
            }
        }


        public String toString() {
            return (_scriptable == null ? "prototype " : "") + getClassName();
        }


        /**
         * Converts a scriptable delegate obtained from a subobject into the appropriate Rhino-compatible Scriptable.
         * This default implementation throws an exception.
         **/
        Scriptable toScriptable( ScriptableDelegate delegate )
                throws PropertyException, NotAFunctionException, JavaScriptException, SAXException {
            throw new UnsupportedOperationException();
        }

    }


    static public class Window extends JavaScriptEngine {

        private Document  _document;
        private Navigator _navigator;


        public String getClassName() {
            return "Window";
        }


        public Window jsGet_window() {
            return this;
        }


        public Window jsGet_self() {
            return this;
        }


        public Document jsGet_document() {
            return _document;
        }


        public Navigator jsGet_navigator() {
            return _navigator;
        }


        void initialize( JavaScriptEngine parent, ScriptableDelegate scriptable )
                throws JavaScriptException, NotAFunctionException, PropertyException, SAXException {
            super.initialize( parent, scriptable );
            _document = (Document) Context.getCurrentContext().newObject( this, "Document" );
            _document.initialize( this, getDelegate().getDocument() );

            _navigator = (Navigator) Context.getCurrentContext().newObject( this, "Navigator" );
            _navigator.initialize( this, getDelegate() );

            getDelegate().load();
        }


        public void jsFunction_alert( String message ) {
            getDelegate().alert( message );
        }


        public boolean jsFunction_confirm( String message ) {
            return getDelegate().getConfirmationResponse( message );
        }


        public String jsFunction_prompt( String message, String defaultResponse ) {
            return getDelegate().getUserResponse( message, defaultResponse );
        }


        Scriptable toScriptable( ScriptableDelegate delegate ) {
            return null;
        }


        private WebResponse.Scriptable getDelegate() {
            return (WebResponse.Scriptable) _scriptable;
        }
    }


    static public class Document extends JavaScriptEngine {

        private ElementArray _forms;
        private ElementArray _links;
        private ElementArray _images;


        public String getClassName() {
            return "Document";
        }


        void initialize( JavaScriptEngine parent, ScriptableDelegate scriptable )
                throws JavaScriptException, NotAFunctionException, PropertyException, SAXException {
            super.initialize( parent, scriptable );
            initializeLinks();
            initializeForms();
            initializeImages();
        }


        public String jsGet_title() throws SAXException {
            return getDelegate().getTitle();
        }


        public Scriptable jsGet_images() {
            return _images;
        }


        public Scriptable jsGet_links() {
            return _links;
        }


        public Scriptable jsGet_forms() {
            return _forms;
        }


        private void initializeImages() throws PropertyException, NotAFunctionException, JavaScriptException, SAXException {
            WebImage.Scriptable scriptables[] = getDelegate().getImages();
            Image[] images = new Image[ scriptables.length ];
            for (int i = 0; i < images.length; i++) {
                images[ i ] = (Image) toScriptable( scriptables[ i ] );
            }
            _images = (ElementArray) Context.getCurrentContext().newObject( this, "ElementArray" );
            _images.initialize( images );
        }


        private void initializeLinks() throws PropertyException, NotAFunctionException, JavaScriptException, SAXException {
            WebLink.Scriptable scriptables[] = getDelegate().getLinks();
            Link[] links = new Link[ scriptables.length ];
            for (int i = 0; i < links.length; i++) {
                links[ i ] = (Link) toScriptable( scriptables[ i ] );
            }
            _links = (ElementArray) Context.getCurrentContext().newObject( this, "ElementArray" );
            _links.initialize( links );
        }


        private void initializeForms() throws PropertyException, NotAFunctionException, JavaScriptException, SAXException {
            WebForm.Scriptable scriptables[] = getDelegate().getForms();
            Form[] forms = new Form[ scriptables.length ];
            for (int i = 0; i < forms.length; i++) {
                forms[ i ] = (Form) toScriptable( scriptables[ i ] );
            }
            _forms = (ElementArray) Context.getCurrentContext().newObject( this, "ElementArray" );
            _forms.initialize( forms );
        }


        Scriptable toScriptable( com.meterware.httpunit.scripting.ScriptableDelegate delegate )
                throws PropertyException, JavaScriptException, NotAFunctionException, SAXException {
            JavaScriptEngine element = (JavaScriptEngine) Context.getCurrentContext().newObject( this, getScriptableClassName( delegate ) );
            element.initialize( this, delegate );
            return element;
        }


        private String getScriptableClassName( ScriptableDelegate delegate ) {
            if (delegate instanceof WebForm.Scriptable) {
                return "Form";
            } else if (delegate instanceof WebLink.Scriptable) {
                return "Link";
            } else if (delegate instanceof WebImage.Scriptable) {
                return "Image";
            } else {
                throw new IllegalArgumentException( "Unknown ScriptableDelegate class: " + delegate.getClass() );
            }
        }


        private HTMLPage.Scriptable getDelegate() {
            return (HTMLPage.Scriptable) _scriptable;
        }

    }


    static public class Navigator extends JavaScriptEngine {

        public String getClassName() {
            return "Navigator";
        }


        void initialize( JavaScriptEngine parent, ScriptableDelegate scriptable )
                throws JavaScriptException, NotAFunctionException, PropertyException, SAXException {
            _scriptable = scriptable;
            if (parent != null) setParentScope( parent );
        }


        public String jsGet_appName() {
            return getDelegate().getClientProperties().getApplicationName();
        }


        public String jsGet_appCodeName() {
            return getDelegate().getClientProperties().getApplicationCodeName();
        }


        public String jsGet_appVersion() {
            return getDelegate().getClientProperties().getApplicationVersion();
        }


        public String jsGet_userAgent() {
            return getDelegate().getClientProperties().getUserAgent();
        }


        public Object[] jsGet_plugins() {
            return new Object[0];
        }


        public boolean jsFunction_javaEnabled() {
            return false;   // no support is provided for applets at present
        }


        private WebResponse.Scriptable getDelegate() {
            return (WebResponse.Scriptable) _scriptable;
        }

    }


    static public class ElementArray extends ScriptableObject {

        private JavaScriptEngine _contents[] = new HTMLElement[0];


        public ElementArray() {
        }


        void initialize( JavaScriptEngine[] contents ) {
            _contents = contents;
        }


        public int jsGet_length() {
            return _contents.length;
        }


        public String getClassName() {
            return "ElementArray";
        }


        public Object get( int i, Scriptable scriptable ) {
            if (i >= 0 && i < _contents.length) {
                return _contents[i];
            } else {
                return super.get( i, scriptable );
            }
        }


        public Object get( String name, Scriptable scriptable ) {
            for (int i = 0; i < _contents.length; i++) {
                JavaScriptEngine content = _contents[ i ];
                if (name.equalsIgnoreCase( content.getName() )) return content;
            }
            return super.get( name, scriptable );
        }
    }


    abstract static public class HTMLElement extends JavaScriptEngine {

        private Document _document;


        public Document jsGet_document() {
            return _document;
        }


        void initialize( JavaScriptEngine parent, ScriptableDelegate scriptable )
                throws JavaScriptException, NotAFunctionException, PropertyException, SAXException {
            super.initialize( parent, scriptable );
            _document = (Document) parent;
        }

    }


    static public class Image extends HTMLElement {

        public String getClassName() {
            return "Image";
        }
    }


    static public class Link extends HTMLElement {

        public Document jsGet_document() {
            return super.jsGet_document();
        }


        public String getClassName() {
            return "Link";
        }
    }


    static public class Form extends HTMLElement {

        private ElementArray _controls;

        public String getClassName() {
            return "Form";
        }


        Scriptable toScriptable( ScriptableDelegate delegate )
                throws PropertyException, NotAFunctionException, JavaScriptException, SAXException {
            final Control control = (Control) Context.getCurrentContext().newObject( this, "Control" );
            control.initialize( this, delegate );
            return control;
        }


        public String jsGet_action() {
            return getDelegate().getAction();
        }


        public void jsSet_action( String action ) {
            getDelegate().setAction( action );
        }


        public Scriptable jsGet_elements() {
            return _controls;
        }


        public void jsFunction_submit() throws IOException, SAXException {
            getDelegate().submit();
        }


        public void jsFunction_reset() throws IOException, SAXException {
            getDelegate().reset();
        }


        void initialize( JavaScriptEngine parent, ScriptableDelegate scriptable )
                throws JavaScriptException, NotAFunctionException, PropertyException, SAXException {
            super.initialize( parent, scriptable );
            initializeControls();
        }


        private void initializeControls() throws PropertyException, NotAFunctionException, JavaScriptException, SAXException {
            ScriptableDelegate scriptables[] = getDelegate().getElementDelegates();
            Control[] controls = new Control[ scriptables.length ];
            for (int i = 0; i < controls.length; i++) {
                controls[ i ] = (Control) toScriptable( scriptables[ i ] );
            }
            _controls = (ElementArray) Context.getCurrentContext().newObject( this, "ElementArray" );
            _controls.initialize( controls );
        }


        private WebForm.Scriptable getDelegate() {
            return (WebForm.Scriptable) _scriptable;
        }

    }


    static public class Control extends JavaScriptEngine {

        private Form _form;

        public String getClassName() {
            return "Control";
        }

        public Form jsGet_form() {
            return _form;
        }

        public void jsFunction_focus() {}

        public void jsFunction_select() {}


        void initialize( JavaScriptEngine parent, ScriptableDelegate scriptable )
                throws JavaScriptException, NotAFunctionException, PropertyException, SAXException {
            super.initialize( parent, scriptable );
            _form = (Form) parent;
        }


        Scriptable toScriptable( ScriptableDelegate delegate )
                throws PropertyException, JavaScriptException, NotAFunctionException, SAXException {
            JavaScriptEngine element = (JavaScriptEngine) Context.getCurrentContext().newObject( this, getScriptableClassName( delegate ) );
            element.initialize( this, delegate );
            return element;
        }


        private String getScriptableClassName( ScriptableDelegate delegate ) {
            if (delegate instanceof SelectionOptions) {
                return "Options";
            } else {
                throw new IllegalArgumentException( "Unknown ScriptableDelegate class: " + delegate.getClass() );
            }
        }

    }


    static public class Options extends JavaScriptEngine {

        public String getClassName() {
            return "Options";
        }


        public int jsGet_length() {
            return getDelegate().getLength();
        }


        public void jsSet_length( int length ) {
            getDelegate().setLength( length );
        }


        public void put( int i, Scriptable scriptable, Object object ) {
            if (object == null) {
                getDelegate().put( i, null );
            } else {
                if (!(object instanceof Option)) throw new IllegalArgumentException( "May only add an Option to this array" );
                Option option = (Option) object;
                getDelegate().put( i, option.getDelegate() );
            }
        }


        private SelectionOptions getDelegate() {
            return (SelectionOptions) _scriptable;
        }


        Scriptable toScriptable( ScriptableDelegate delegate )
                throws PropertyException, JavaScriptException, NotAFunctionException, SAXException {
            JavaScriptEngine element = (JavaScriptEngine) Context.getCurrentContext().newObject( this, "Option" );
            element.initialize( this, delegate );
            return element;
        }
    }


    static public class Option extends JavaScriptEngine {

        public String getClassName() {
            return "Option";
        }


        public void jsConstructor( String text, String value, boolean defaultSelected, boolean selected ) {
            _scriptable = WebResponse.newDelegate( "Option" );
            getDelegate().initialize( text, value, defaultSelected, selected );
        }


        public int jsGet_index() {
            return getDelegate().getIndex();
        }


        public String jsGet_text() {
            return getDelegate().getText();
        }


        public void jsSet_text( String text ) {
            getDelegate().setText( text );
        }


        public String jsGet_value() {
            return getDelegate().getValue();
        }


        public void jsSet_value( String value ) {
            getDelegate().setValue( value );
        }


        public boolean jsGet_selected() {
            return getDelegate().isSelected();
        }


        public void jsSet_selected( boolean selected ) {
            getDelegate().setSelected( selected );
        }


        public boolean jsGet_defaultSelected() {
            return getDelegate().isDefaultSelected();
        }


        SelectionOption getDelegate() {
            return (SelectionOption) _scriptable;
        }
    }

}

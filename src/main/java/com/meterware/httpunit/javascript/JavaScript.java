/*
 * MIT License
 *
 * Copyright 2011-2025 Russell Gold
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
package com.meterware.httpunit.javascript;

import com.meterware.httpunit.ClientProperties;
import com.meterware.httpunit.HTMLPage;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.HttpUnitUtils;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebImage;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.javascript.events.EventException;
import com.meterware.httpunit.javascript.events.EventTarget;
import com.meterware.httpunit.scripting.DocumentElement;
import com.meterware.httpunit.scripting.FormScriptable;
import com.meterware.httpunit.scripting.IdentifiedDelegate;
import com.meterware.httpunit.scripting.Input;
import com.meterware.httpunit.scripting.NamedDelegate;
import com.meterware.httpunit.scripting.ScriptableDelegate;
import com.meterware.httpunit.scripting.ScriptingEngine;
import com.meterware.httpunit.scripting.ScriptingHandler;
import com.meterware.httpunit.scripting.SelectionOption;
import com.meterware.httpunit.scripting.SelectionOptions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.xml.sax.SAXException;

/**
 * This class is the Rhino-compatible implementation of the JavaScript DOM objects.
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class JavaScript {

    private static boolean _throwExceptionsOnError = true;

    public static boolean isThrowExceptionsOnError() {
        return _throwExceptionsOnError;
    }

    public static void setThrowExceptionsOnError(boolean throwExceptionsOnError) {
        _throwExceptionsOnError = throwExceptionsOnError;
    }

    /**
     * Initiates JavaScript execution for the specified web response.
     */
    public static void run(WebResponse response) throws IllegalAccessException, InstantiationException,
            InvocationTargetException, EvaluatorException, EvaluatorException, SAXException, JavaScriptException {
        Context context = Context.enter();
        // suggest bug fix for large java scripts see
        // bug report [ 1216567 ] Exception for large javascripts
        // by Grzegorz Lukasik
        // and

        context.setOptimizationLevel(HttpUnitOptions.getJavaScriptOptimizationLevel());
        Scriptable scope = context.initStandardObjects(null);
        initHTMLObjects(scope);

        Window w = (Window) context.newObject(scope, "Window");
        w.initialize(null, response.getScriptableObject());
    }

    /**
     * Runs the onload event for the specified web response.
     */
    public static void load(WebResponse response) throws EvaluatorException, InstantiationException,
            IllegalAccessException, InvocationTargetException, JavaScriptException, SAXException, EvaluatorException {
        if (!(response.getScriptableObject().getScriptEngine() instanceof JavaScriptEngine)) {
            run(response);
        }
        response.getScriptableObject().load();
    }

    private static void initHTMLObjects(Scriptable scope)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, EvaluatorException {
        ScriptableObject.defineClass(scope, Window.class);
        ScriptableObject.defineClass(scope, Document.class);
        ScriptableObject.defineClass(scope, Style.class);
        ScriptableObject.defineClass(scope, Location.class);
        ScriptableObject.defineClass(scope, Navigator.class);
        ScriptableObject.defineClass(scope, Screen.class);
        ScriptableObject.defineClass(scope, Link.class);
        ScriptableObject.defineClass(scope, Form.class);
        ScriptableObject.defineClass(scope, Control.class);
        ScriptableObject.defineClass(scope, Link.class);
        ScriptableObject.defineClass(scope, Image.class);
        ScriptableObject.defineClass(scope, Options.class);
        ScriptableObject.defineClass(scope, Option.class);
        ScriptableObject.defineClass(scope, ElementArray.class);
        ScriptableObject.defineClass(scope, HTMLElement.class);
    }

    /**
     * abstract Engine for JavaScript
     */
    abstract static class JavaScriptEngine extends ScriptingEngineImpl implements EventTarget {

        private static final long serialVersionUID = 1L;
        protected ScriptableDelegate _scriptable;
        protected JavaScriptEngine _parent;
        protected Map _eventListeners = new HashMap<>(); // Map<String,Set<EventListener>>
        protected Map _eventCaptureListeners = new HashMap<>(); // Map<String,Set<EventListener>>

        /**
         * initialize JavaScript for the given ScriptEngine
         *
         * @parent - the Script Engine to use
         *
         * @scriptable - the scriptable object to do the initialization for
         */
        void initialize(JavaScriptEngine parent, ScriptableDelegate scriptable)
                throws SAXException, JavaScriptException, EvaluatorException {
            _scriptable = scriptable;
            _scriptable.setScriptEngine(this);
            _parent = parent;
            if (parent != null) {
                setParentScope(parent);
            }
        }

        String getName() {
            return _scriptable instanceof NamedDelegate ? ((NamedDelegate) _scriptable).getName() : "";
        }

        String getID() {
            return _scriptable instanceof IdentifiedDelegate ? ((IdentifiedDelegate) _scriptable).getID() : "";
        }

        /**
         * get the event Handler script for the event e.g. onchange, onmousedown, onclick, onmouseup execute the script
         * if it's assigned by calling doEvent for the script
         *
         * @param eventName
         *
         * @return
         */
        @Override
        public boolean handleEvent(String eventName) {
            return _scriptable.handleEvent(eventName);
        }

        @Override
        public boolean has(String propertyName, Scriptable scriptable) {
            return super.has(propertyName, scriptable) || _scriptable != null && _scriptable.get(propertyName) != null;
        }

        @Override
        public Object get(String propertyName, Scriptable scriptable) {
            Object result = super.get(propertyName, scriptable);
            if (result != NOT_FOUND) {
                return result;
            }
            if (_scriptable == null) {
                return NOT_FOUND;
            }

            return convertIfNeeded(_scriptable.get(propertyName));

        }

        @Override
        public Object get(int i, Scriptable scriptable) {
            Object result = super.get(i, scriptable);
            if (result != NOT_FOUND) {
                return result;
            }
            if (_scriptable == null) {
                return NOT_FOUND;
            }

            return convertIfNeeded(_scriptable.get(i));
        }

        private Object convertIfNeeded(final Object property) {
            if (property == null) {
                return NOT_FOUND;
            }

            if (property instanceof ScriptableDelegate[]) {
                return toScriptable((ScriptableDelegate[]) property);
            }
            if (!(property instanceof ScriptableDelegate)) {
                return property;
            }
            return toScriptable((ScriptableDelegate) property);
        }

        private Object toScriptable(ScriptableDelegate[] list) {
            Object[] delegates = new Object[list.length];
            for (int i = 0; i < delegates.length; i++) {
                delegates[i] = toScriptable(list[i]);
            }
            return Context.getCurrentContext().newArray(this, delegates);
        }

        @Override
        public void put(String propertyName, Scriptable scriptable, Object value) {
            if (_scriptable == null || _scriptable.get(propertyName) == null) {
                super.put(propertyName, scriptable, value);
            } else {
                _scriptable.set(propertyName, value);
            }
        }

        @Override
        public String toString() {
            return (_scriptable == null ? "prototype " : "") + getClassName();
        }

        @Override
        public ScriptingEngine newScriptingEngine(ScriptableDelegate child) {
            try {
                return (ScriptingEngine) toScriptable(child);
            } catch (Exception e) {
                HttpUnitUtils.handleException(e);
                throw new RuntimeException(e.toString());
            }
        }

        @Override
        public void clearCaches() {
        }

        protected static String toStringIfNotUndefined(Object object) {
            return object == null || Undefined.instance.equals(object) ? null : object.toString();
        }

        /**
         * Converts a scriptable delegate obtained from a subobject into the appropriate Rhino-compatible Scriptable.
         **/
        final Object toScriptable(ScriptableDelegate delegate) {
            if (delegate == null) {
                return NOT_FOUND;
            }
            if (delegate.getScriptEngine() instanceof Scriptable) {
                return delegate.getScriptEngine();
            }
            try {
                JavaScriptEngine element = (JavaScriptEngine) Context.getCurrentContext().newObject(this,
                        getScriptableClassName(delegate));
                element.initialize(this, delegate);
                return element;
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RhinoException(e);
            }
        }

        /**
         * get the classname of the given ScriptableDelegate
         *
         * @param delegate
         *            - the object to get the class name for
         *
         * @return - the simple local class name for the delegate e.g. Window, Document, Form, Link, Image, Options,
         *         Option, Control, HTMLElement
         *
         * @throws an
         *             IllegalArgumentException if the delegate is not known
         */
        private String getScriptableClassName(ScriptableDelegate delegate) {
            if (delegate instanceof WebResponse.Scriptable) {
                return "Window";
            }
            if (delegate instanceof HTMLPage.Scriptable) {
                return "Document";
            }
            if (delegate instanceof FormScriptable) {
                return "Form";
            }
            if (delegate instanceof WebLink.Scriptable) {
                return "Link";
            }
            if (delegate instanceof WebImage.Scriptable) {
                return "Image";
            }
            if (delegate instanceof SelectionOptions) {
                return "Options";
            }
            if (delegate instanceof SelectionOption) {
                return "Option";
            }
            if (delegate instanceof Input) {
                return "Control";
            }
            if (delegate instanceof DocumentElement) {
                return "HTMLElement";
            }

            throw new IllegalArgumentException("Unknown ScriptableDelegate class: " + delegate.getClass());
        }

        protected ElementArray toElementArray(ScriptingHandler[] scriptables) {
            JavaScriptEngine[] elements = new JavaScriptEngine[scriptables.length];
            for (int i = 0; i < elements.length; i++) {
                elements[i] = (JavaScriptEngine) toScriptable((ScriptableDelegate) scriptables[i]);
            }
            ElementArray result = ElementArray.newElementArray(this);
            result.initialize(elements);
            return result;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void jsFunction_addEventListener(String type, Scriptable listener, boolean useCapture) {
            if (useCapture) {
                Set set = (Set) _eventCaptureListeners.get(type); // Set<Scriptable>
                if (set == null) {
                    set = new HashSet<>();
                    _eventCaptureListeners.put(type, set);
                }
                set.add(listener);
            } else {
                Set set = (Set) _eventListeners.get(type); // Set<Scriptable>
                if (set == null) {
                    set = new HashSet<>();
                    _eventListeners.put(type, set);
                }
                set.add(listener);
            }
            // System.out.println(getClassName()+".addEventListener("+type+")");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean jsFunction_dispatchEvent(Scriptable evt) throws EventException {
            // TODO implement event dispatching & listener invocation
            // System.out.println(getClassName()+".dispatchEvent("+evt.get("type",evt)+")");
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void jsFunction_removeEventListener(String type, Scriptable listener, boolean useCapture) {
            if (useCapture) {
                Set set = (Set) _eventCaptureListeners.get(type); // Set<EventListener>
                if (set != null) {
                    set.remove(listener);
                }
            } else {
                Set set = (Set) _eventListeners.get(type); // Set<EventListener>
                if (set != null) {
                    set.remove(listener);
                }
            }
            // System.out.println(getClassName()+".removeEventListener("+type+")");
        }
    }

    /**
     * Window functions
     */
    static public class Window extends JavaScriptEngine {

        private static final long serialVersionUID = 1L;
        private Document _document;
        private Navigator _navigator;
        private Location _location;
        private Screen _screen;
        private ElementArray _frames;

        @Override
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
            if (_document == null) {
                _document = (Document) toScriptable(getDelegate().getDocument());
            }
            return _document;
        }

        public Scriptable jsGet_frames() throws SAXException, JavaScriptException, EvaluatorException {
            if (_frames == null) {
                WebResponse.Scriptable[] scriptables = getDelegate().getFrames();
                Window[] frames = new Window[scriptables.length];
                for (int i = 0; i < frames.length; i++) {
                    frames[i] = (Window) toScriptable(scriptables[i]);
                }
                _frames = (ElementArray) Context.getCurrentContext().newObject(this, "ElementArray");
                _frames.initialize(frames);
            }
            return _frames;
        }

        public Navigator jsGet_navigator() {
            return _navigator;
        }

        public Screen jsGet_screen() {
            return _screen;
        }

        public Location jsGet_location() {
            return _location;
        }

        public void jsSet_location(String relativeURL) throws IOException, SAXException {
            setLocation(relativeURL);
        }

        void setLocation(String relativeURL) throws IOException, SAXException {
            getDelegate().setLocation(relativeURL);
        }

        /**
         * initialize JavaScript for the given ScriptEngine
         *
         * @parent - the Script Engine to use
         *
         * @scriptable - the scriptable object to do the initialization for
         */
        @Override
        void initialize(JavaScriptEngine parent, ScriptableDelegate scriptable)
                throws JavaScriptException, EvaluatorException, SAXException {
            super.initialize(parent, scriptable);

            _location = (Location) Context.getCurrentContext().newObject(this, "Location");
            _location.initialize(this, ((WebResponse.Scriptable) scriptable).getURL());

            _navigator = (Navigator) Context.getCurrentContext().newObject(this, "Navigator");
            _navigator.setClientProperties(getDelegate().getClientProperties());

            _screen = (Screen) Context.getCurrentContext().newObject(this, "Screen");
            _screen.setClientProperties(getDelegate().getClientProperties());
        }

        /**
         * javascript alert handling
         *
         * @param message
         *            - the alert message
         */
        public void jsFunction_alert(String message) {
            getDelegate().alertUser(message);
        }

        /**
         * javascript built in function "toLowerCase"
         *
         * @param s
         */
        public String jsFunction_toLowerCase(String s) {
            return s.toLowerCase();
        }

        public boolean jsFunction_confirm(String message) {
            return getDelegate().getConfirmationResponse(message);
        }

        public String jsFunction_prompt(String message, String defaultResponse) {
            return getDelegate().getUserResponse(message, defaultResponse);
        }

        public void jsFunction_moveTo(int x, int y) {
        }

        public void jsFunction_scrollTo(int x, int y) {
        }

        public void jsFunction_focus() {
        }

        public void jsFunction_setTimeout() {
        }

        public void jsFunction_close() {
            getDelegate().closeWindow();
        }

        public Window jsFunction_open(Object url, String name, String features, boolean replace)
                throws JavaScriptException, EvaluatorException, IOException, SAXException {
            WebResponse.Scriptable delegate = getDelegate().open(toStringIfNotUndefined(url), name, features, replace);
            return delegate == null ? null : (Window) toScriptable(delegate);
        }

        /** The global "event" object is not supported, so return null (instead of causing '"event" is not defined') */
        public Location jsGet_event() {
            return null;
        }

        @Override
        public void clearCaches() {
            if (_document != null) {
                _document.clearCaches();
            }
        }

        @Override
        protected String getDocumentWriteBuffer() {
            return jsGet_document().getWriteBuffer().toString();
        }

        @Override
        protected void discardDocumentWriteBuffer() {
            jsGet_document().clearWriteBuffer();
        }

        private WebResponse.Scriptable getDelegate() {
            return (WebResponse.Scriptable) _scriptable;
        }
    }

    /**
     * Document script handling
     */
    static public class Document extends JavaScriptEngine {

        private static final long serialVersionUID = 1L;
        private ElementArray _forms;
        private ElementArray _links;
        private ElementArray _images;
        private StringBuilder _writeBuffer;
        private String _mimeType;

        @Override
        public String getClassName() {
            return "Document";
        }

        @Override
        public void clearCaches() {
            _forms = _links = _images = null;
        }

        public String jsGet_title() throws SAXException {
            return getDelegate().getTitle();
        }

        public Scriptable jsGet_images() throws SAXException {
            if (_images == null) {
                _images = toElementArray(getDelegate().getImages());
            }
            return _images;
        }

        public Scriptable jsGet_links() throws SAXException {
            if (_links == null) {
                _links = toElementArray(getDelegate().getLinks());
            }
            return _links;
        }

        public Scriptable jsGet_forms() throws SAXException {
            if (_forms == null) {
                _forms = toElementArray(getDelegate().getForms());
            }
            return _forms;
        }

        public Object jsFunction_getElementById(String id) {
            ScriptableDelegate elementWithID = getDelegate().getElementWithID(id);
            return elementWithID == null ? null : toScriptable(elementWithID);
        }

        public Object jsFunction_getElementsByName(String name) {
            return toElementArray(getDelegate().getElementsByName(name));
        }

        public Object jsFunction_getElementsByTagName(String name) {
            return toElementArray(getDelegate().getElementsByTagName(name));
        }

        public Object jsGet_location() {
            return _parent == null ? NOT_FOUND : getWindow().jsGet_location();
        }

        public void jsSet_location(String urlString) throws IOException, SAXException {
            if (urlString.startsWith("color")) {
                return;
            }
            getWindow().setLocation(urlString);
        }

        public String jsGet_cookie() {
            return getDelegate().getCookie();
        }

        public void jsSet_cookie(String cookieSpec) {
            final int equalsIndex = cookieSpec.indexOf('=');
            if (equalsIndex < 0) {
                return;
            }
            int endIndex = cookieSpec.indexOf(";", equalsIndex);
            if (endIndex < 0) {
                endIndex = cookieSpec.length();
            }
            String name = cookieSpec.substring(0, equalsIndex);
            String value = cookieSpec.substring(equalsIndex + 1, endIndex);
            getDelegate().setCookie(name, value);
        }

        private Window getWindow() {
            return (Window) _parent;
        }

        public void jsFunction_open(Object mimeType) {
            _mimeType = toStringIfNotUndefined(mimeType);
        }

        public void jsFunction_close() {
            if (getDelegate().replaceText(getWriteBuffer().toString(), _mimeType == null ? "text/html" : _mimeType)) {
                getWriteBuffer().setLength(0);
            }
        }

        public void jsFunction_write(String string) {
            getWriteBuffer().append(string);
        }

        public void jsFunction_writeln(String string) {
            getWriteBuffer().append(string).append((char) 0x0D).append((char) 0x0A);
        }

        protected StringBuilder getWriteBuffer() {
            if (_writeBuffer == null) {
                _writeBuffer = new StringBuilder();
            }
            return _writeBuffer;
        }

        protected void clearWriteBuffer() {
            _writeBuffer = null;
        }

        private HTMLPage.Scriptable getDelegate() {
            return (HTMLPage.Scriptable) _scriptable;
        }

        @Override
        public void jsFunction_addEventListener(String type, Scriptable listener, boolean useCapture) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean jsFunction_dispatchEvent(Scriptable evt) throws EventException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void jsFunction_removeEventListener(String type, Scriptable listener, boolean useCapture) {
            // TODO Auto-generated method stub

        }

    }

    static public class Location extends JavaScriptEngine {

        private static final long serialVersionUID = 1L;
        private URL _url;
        private Window _window;

        @Override
        public String getClassName() {
            return "Location";
        }

        void initialize(Window window, URL url) {
            _window = window;
            _url = url;
        }

        public void jsFunction_replace(String urlString) throws IOException, SAXException {
            _window.setLocation(urlString);
        }

        public String jsGet_href() {
            return toString();
        }

        public void jsSet_href(String urlString) throws SAXException, IOException {
            _window.setLocation(urlString);
        }

        public String jsGet_protocol() {
            return _url.getProtocol() + ':';
        }

        public String jsGet_host() {
            return _url.getHost() + ':' + _url.getPort();
        }

        public String jsGet_hostname() {
            return _url.getHost();
        }

        public String jsGet_port() {
            return String.valueOf(_url.getPort());
        }

        public String jsGet_pathname() {
            return _url.getPath();
        }

        public void jsSet_pathname(String newPath) throws SAXException, IOException {
            if (!newPath.startsWith("/")) {
                newPath = '/' + newPath;
            }
            URL newURL = new URL(_url, newPath);
            _window.setLocation(newURL.toExternalForm());
        }

        public String jsGet_search() {
            return '?' + _url.getQuery();
        }

        public void jsSet_search(String newSearch) throws SAXException, IOException {
            if (!newSearch.startsWith("?")) {
                newSearch = '?' + newSearch;
            }
            _window.setLocation(jsGet_protocol() + "//" + jsGet_host() + jsGet_pathname() + newSearch);
        }

        /**
         * Returns the default value of this scriptable object. In this case, it returns simply the URL as a string.
         * Note that this method is necessary, since Rhino will only call the toString method directly if there are no
         * Rhino methods defined (jsGet_*, jsFunction_*, etc.)
         */
        @Override
        public Object getDefaultValue(Class typeHint) {
            return _url.toExternalForm();
        }

        @Override
        public String toString() {
            return _url.toExternalForm();
        }

    }

    static public class Style extends JavaScriptEngine {

        private static final long serialVersionUID = 1L;
        private String _display = "inline";
        private String _visibility = "visible";

        @Override
        public String getClassName() {
            return "Style";
        }

        public String jsGet_display() {
            return _display;
        }

        public void jsSet_display(String display) {
            _display = display;
        }

        public String jsGet_visibility() {
            return _visibility;
        }

        public void jsSet_visibility(String visibility) {
            _visibility = visibility;
        }
    }

    static public class Navigator extends JavaScriptEngine {

        private static final long serialVersionUID = 1L;
        private ClientProperties _clientProperties;

        @Override
        public String getClassName() {
            return "Navigator";
        }

        void setClientProperties(ClientProperties clientProperties) {
            _clientProperties = clientProperties;
        }

        public String jsGet_appName() {
            return _clientProperties.getApplicationName();
        }

        public String jsGet_appCodeName() {
            return _clientProperties.getApplicationCodeName();
        }

        public String jsGet_appVersion() {
            return _clientProperties.getApplicationVersion();
        }

        public String jsGet_userAgent() {
            return _clientProperties.getUserAgent();
        }

        public String jsGet_platform() {
            return _clientProperties.getPlatform();
        }

        public Object[] jsGet_plugins() {
            return new Object[0];
        }

        public boolean jsFunction_javaEnabled() {
            return false; // no support is provided for applets at present
        }

    }

    static public class Screen extends JavaScriptEngine {

        private static final long serialVersionUID = 1L;
        private ClientProperties _clientProperties;

        void setClientProperties(ClientProperties clientProperties) {
            _clientProperties = clientProperties;
        }

        @Override
        public String getClassName() {
            return "Screen";
        }

        public int jsGet_availWidth() {
            return _clientProperties.getAvailableScreenWidth();
        }

        public int jsGet_availHeight() {
            return _clientProperties.getAvailHeight();
        }

    }

    static public class ElementArray extends ScriptableObject {

        private static final long serialVersionUID = 1L;
        private JavaScriptEngine[] _contents = new HTMLElement[0];

        static ElementArray newElementArray(Scriptable parent) {
            try {
                return (ElementArray) Context.getCurrentContext().newObject(parent, "ElementArray");
            } catch (EvaluatorException | JavaScriptException e) {
                throw new RhinoException(e);
            }
        }

        public ElementArray() {
        }

        void initialize(JavaScriptEngine[] contents) {
            _contents = contents;
        }

        public int jsGet_length() {
            return _contents.length;
        }

        @Override
        public String getClassName() {
            return "ElementArray";
        }

        @Override
        public Object get(int i, Scriptable scriptable) {
            if (i >= 0 && i < _contents.length) {
                return _contents[i];
            }
            return super.get(i, scriptable);
        }

        @Override
        public Object get(String name, Scriptable scriptable) {
            for (JavaScriptEngine content : _contents) {
                if (name.equalsIgnoreCase(content.getID())) {
                    return content;
                }
            }
            for (JavaScriptEngine content : _contents) {
                if (name.equalsIgnoreCase(content.getName())) {
                    return content;
                }
            }
            return super.get(name, scriptable);
        }

        protected JavaScriptEngine[] getContents() {
            return _contents;
        }
    }

    /**
     * HTML Element support for JavaScript
     */
    static public class HTMLElement extends JavaScriptEngine {

        private static final long serialVersionUID = 1L;
        private Style _style;
        private Document _document;

        @Override
        public String getClassName() {
            return "HTMLElement";
        }

        public Document jsGet_document() {
            return _document;
        }

        public Style jsGet_style() {
            return _style;
        }

        /**
         * arbitrary attribute access
         *
         * @param attributeName
         *
         * @return
         */
        public Object jsFunction_getAttribute(String attributeName) {
            return _scriptable.get(attributeName);
        }

        @Override
        void initialize(JavaScriptEngine parent, ScriptableDelegate scriptable)
                throws JavaScriptException, EvaluatorException, SAXException {
            super.initialize(parent, scriptable);
            _document = (Document) parent;
            _style = (Style) Context.getCurrentContext().newObject(this, "Style");
        }

    }

    static public class Image extends HTMLElement {

        private static final long serialVersionUID = 1L;

        @Override
        public String getClassName() {
            return "Image";
        }
    }

    static public class Link extends HTMLElement {

        private static final long serialVersionUID = 1L;

        @Override
        public Document jsGet_document() {
            return super.jsGet_document();
        }

        @Override
        public String getClassName() {
            return "Link";
        }
    }

    /**
     * Form functions
     */
    static public class Form extends HTMLElement {

        private static final long serialVersionUID = 1L;
        private ElementArray _controls;

        @Override
        public String getClassName() {
            return "Form";
        }

        public String jsGet_name() {
            return getDelegate().getName();
        }

        /**
         * @since FR [ 2163079 ] make form.name property mutable by Peter De Bruycker
         *
         * @param name
         */
        public void jsSet_name(String name) {
            getDelegate().set("name", name);
        }

        public String jsGet_action() {
            return getDelegate().getAction();
        }

        public void jsSet_action(String action) {
            getDelegate().setAction(action);
        }

        public Scriptable jsGet_elements() throws EvaluatorException, JavaScriptException {
            if (_controls == null) {
                initializeControls();
            }
            return _controls;
        }

        public Object jsFunction_getElementsByTagName(String name) throws SAXException {
            return toElementArray(getDelegate().getElementsByTagName(name));
        }

        public void jsFunction_submit() throws IOException, SAXException {
            getDelegate().submit();
        }

        public void jsFunction_reset() throws IOException, SAXException {
            getDelegate().reset();
        }

        private void initializeControls() throws EvaluatorException, JavaScriptException {
            ScriptableDelegate[] scriptables = getDelegate().getElementDelegates();
            Control[] controls = new Control[scriptables.length];
            for (int i = 0; i < controls.length; i++) {
                controls[i] = (Control) toScriptable(scriptables[i]);
            }
            _controls = (ElementArray) Context.getCurrentContext().newObject(this, "ElementArray");
            _controls.initialize(controls);
        }

        private WebForm.Scriptable getDelegate() {
            return (WebForm.Scriptable) _scriptable;
        }

    }

    /**
     * Javascript support for any control
     */
    static public class Control extends JavaScriptEngine {

        private static final long serialVersionUID = 1L;
        private Form _form;

        @Override
        public String getClassName() {
            return "Control";
        }

        public Form jsGet_form() {
            return _form;
        }

        public void jsFunction_focus() {
        }

        public void jsFunction_select() {
        }

        /**
         * click via javascript
         *
         * @throws IOException
         * @throws SAXException
         */
        public void jsFunction_click() throws IOException, SAXException {
            getDelegate().click();
        }

        private Input getDelegate() {
            return (Input) _scriptable;
        }

        /** Support getting value of arbitrary attribute */
        public Object jsFunction_getAttribute(String attributeName) throws JavaScriptException {
            return getDelegate().get(attributeName);
        }

        /** Support getting value of arbitrary attribute */
        public void jsFunction_setAttribute(String attributeName, Object value) throws JavaScriptException {
            getDelegate().setAttribute(attributeName, value);
        }

        /** Support getting value of arbitrary attribute */
        public void jsFunction_removeAttribute(String attributeName) throws JavaScriptException {
            getDelegate().removeAttribute(attributeName);
        }

        /** Allow calling onchange() from within a JavaScript function */
        public void jsFunction_onchange() throws JavaScriptException {
            Input myInput = this.getDelegate();
            myInput.sendOnChangeEvent();
        }

        @Override
        void initialize(JavaScriptEngine parent, ScriptableDelegate scriptable)
                throws JavaScriptException, EvaluatorException, SAXException {
            super.initialize(parent, scriptable);
            if (parent instanceof Form) {
                _form = (Form) parent;
            }
        }

    }

    static public class Options extends JavaScriptEngine {

        private static final long serialVersionUID = 1L;

        @Override
        public String getClassName() {
            return "Options";
        }

        public int jsGet_length() {
            return getDelegate().getLength();
        }

        public void jsSet_length(int length) {
            getDelegate().setLength(length);
        }

        @Override
        public void put(int i, Scriptable scriptable, Object object) {
            if (object == null) {
                getDelegate().put(i, null);
            } else {
                if (!(object instanceof Option)) {
                    throw new IllegalArgumentException("May only add an Option to this array");
                }
                Option option = (Option) object;
                getDelegate().put(i, option.getDelegate());
            }
        }

        private SelectionOptions getDelegate() {
            return (SelectionOptions) _scriptable;
        }

    }

    static public class Option extends JavaScriptEngine {

        private static final long serialVersionUID = 1L;

        @Override
        public String getClassName() {
            return "Option";
        }

        public void jsConstructor(String text, String value, boolean defaultSelected, boolean selected) {
            _scriptable = WebResponse.newDelegate("Option");
            getDelegate().initialize(text, value, defaultSelected, selected);
        }

        public int jsGet_index() {
            return getDelegate().getIndex();
        }

        public String jsGet_text() {
            return getDelegate().getText();
        }

        public void jsSet_text(String text) {
            getDelegate().setText(text);
        }

        public String jsGet_value() {
            return getDelegate().getValue();
        }

        public void jsSet_value(String value) {
            getDelegate().setValue(value);
        }

        public boolean jsGet_selected() {
            return getDelegate().isSelected();
        }

        public void jsSet_selected(boolean selected) {
            getDelegate().setSelected(selected);
        }

        public boolean jsGet_defaultSelected() {
            return getDelegate().isDefaultSelected();
        }

        SelectionOption getDelegate() {
            return (SelectionOption) _scriptable;
        }
    }

}

/**
 * special exception for the Rhino Javscript engine
 */
class RhinoException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private Exception _cause;

    public RhinoException(Exception cause) {
        _cause = cause;
    }

    @Override
    public String getMessage() {
        return "Rhino exception: " + _cause;
    }
}

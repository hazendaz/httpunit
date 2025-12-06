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
package com.meterware.httpunit;

import com.meterware.httpunit.parsing.HTMLParserFactory;
import com.meterware.httpunit.parsing.HTMLParserListener;
import com.meterware.httpunit.scripting.ScriptableDelegate;
import com.meterware.httpunit.scripting.ScriptingEngineFactory;
import com.meterware.httpunit.scripting.ScriptingHandler;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A collection of global options to control HttpUnit's behavior.
 **/
public abstract class HttpUnitOptions {

    /** The Constant ORIGINAL_SCRIPTING_ENGINE_FACTORY. */
    public static final String ORIGINAL_SCRIPTING_ENGINE_FACTORY = "com.meterware.httpunit.javascript.JavaScriptEngineFactory";
    // comment out the scripting engine not to be used by allowing the appropriate number of asterisks in the comment on
    // the next line (1 or 2)
    /** The Constant DEFAULT_SCRIPT_ENGINE_FACTORY. */
    /**/
    public static final String DEFAULT_SCRIPT_ENGINE_FACTORY = ORIGINAL_SCRIPTING_ENGINE_FACTORY;
    /*
     * / public static final String DEFAULT_SCRIPT_ENGINE_FACTORY = NEW_SCRIPTING_ENGINE_FACTORY; /
     */

    /**
     * Resets all options to their default values.
     */
    public static void reset() {
        _exceptionsOnErrorStatus = true;
        _parameterValuesValidated = true;
        _imagesTreatedAsAltText = false;
        _loggingHttpHeaders = false;
        _matchesIgnoreCase = true;
        _checkContentLength = false;
        _redirectDelay = 0; // TODO move this to ClientProperties
        _characterSet = StandardCharsets.ISO_8859_1.name();
        _contentType = DEFAULT_CONTENT_TYPE;
        _postIncludesCharset = false;
        _exceptionsThrownOnScriptError = true;
        _customAttributes = null;
        _javaScriptOptimizationLevel = -1;
        _checkHtmlContentType = false;
        setScriptEngineClassName(DEFAULT_SCRIPT_ENGINE_FACTORY);
        setScriptingEnabled(true);
    }

    /**
     * Returns true if HttpUnit is accepting and saving cookies. The default is to accept them.
     *
     * @return true, if is accept cookies
     *
     * @deprecated as of 1.5.3, use ClientProperties#isAcceptCookies();
     */
    @Deprecated
    public static boolean isAcceptCookies() {
        return ClientProperties.getDefaultProperties().isAcceptCookies();
    }

    /**
     * Specifies whether HttpUnit should accept and send cookies.
     *
     * @param acceptCookies
     *            the new accept cookies
     *
     * @deprecated as of 1.5.3, use ClientProperties#setAcceptCookies();
     */
    @Deprecated
    public static void setAcceptCookies(boolean acceptCookies) {
        ClientProperties.getDefaultProperties().setAcceptCookies(acceptCookies);
    }

    /**
     * Returns true if any WebClient created will accept GZIP encoding of responses. The default is to accept GZIP
     * encoding.
     *
     * @return true, if is accept gzip
     *
     * @deprecated as of 1.5.3, use ClientProperties#isAcceptGzip();
     */
    @Deprecated
    public static boolean isAcceptGzip() {
        return ClientProperties.getDefaultProperties().isAcceptGzip();
    }

    /**
     * Specifies whether a WebClient will be initialized to accept GZIP encoded responses. The default is true.
     *
     * @param acceptGzip
     *            the new accept gzip
     *
     * @deprecated as of 1.5.3, use ClientProperties#setAcceptGzip();
     */
    @Deprecated
    public static void setAcceptGzip(boolean acceptGzip) {
        ClientProperties.getDefaultProperties().setAcceptGzip(acceptGzip);
    }

    /**
     * Resets the default character set to the HTTP default encoding.
     **/
    public static void resetDefaultCharacterSet() {
        _characterSet = StandardCharsets.ISO_8859_1.name();
    }

    /**
     * Resets the default content type to plain text.
     **/
    public static void resetDefaultContentType() {
        _contentType = DEFAULT_CONTENT_TYPE;
    }

    /**
     * Sets the default character set for pages which do not specify one and for requests created without HTML sources.
     * By default, HttpUnit uses the HTTP default encoding, ISO-8859-1.
     *
     * @param characterSet
     *            the new default character set
     */
    public static void setDefaultCharacterSet(String characterSet) {
        _characterSet = characterSet;
    }

    /**
     * Returns the character set to be used for pages which do not specify one.
     *
     * @return the default character set
     */
    public static String getDefaultCharacterSet() {
        return _characterSet;
    }

    /**
     * Returns true if HttpUnit will throw an exception when a message is only partially received. The default is to
     * avoid such checks.
     *
     * @return true, if is check content length
     */
    public static boolean isCheckContentLength() {
        return _checkContentLength;
    }

    /**
     * Specifies whether HttpUnit should throw an exception when the content length of a message does not match its
     * actual received length. Defaults to false.
     *
     * @param checkContentLength
     *            the new check content length
     */
    public static void setCheckContentLength(boolean checkContentLength) {
        _checkContentLength = checkContentLength;
    }

    /**
     * Determines whether a normal POST request will include the character set in the content-type header. The default
     * is to include it; however, some older servlet engines (most notably Tomcat 3.1) get confused when they see it.
     *
     * @param postIncludesCharset
     *            the new post includes charset
     */
    public static void setPostIncludesCharset(boolean postIncludesCharset) {
        _postIncludesCharset = postIncludesCharset;
    }

    /**
     * Returns true if POST requests should include the character set in the content-type header.
     *
     * @return true, if is post includes charset
     */
    public static boolean isPostIncludesCharset() {
        return _postIncludesCharset;
    }

    /**
     * Sets the default content type for pages which do not specify one.
     *
     * @param contentType
     *            the new default content type
     */
    public static void setDefaultContentType(String contentType) {
        _contentType = contentType;
    }

    /**
     * Returns the content type to be used for pages which do not specify one.
     *
     * @return the default content type
     */
    public static String getDefaultContentType() {
        return _contentType;
    }

    /**
     * Returns true if parser warnings are enabled.
     *
     * @return the parser warnings enabled
     *
     * @deprecated as of 1.5.2, use HTMLParserFactory#isParserWarningsEnabled
     */
    @Deprecated
    public static boolean getParserWarningsEnabled() {
        return HTMLParserFactory.isParserWarningsEnabled();
    }

    /**
     * If true, tells the parser to display warning messages. The default is false (warnings are not shown).
     *
     * @param enabled
     *            the new parser warnings enabled
     *
     * @deprecated as of 1.5.2, use HTMLParserFactory#setParserWarningsEnabled
     */
    @Deprecated
    public static void setParserWarningsEnabled(boolean enabled) {
        HTMLParserFactory.setParserWarningsEnabled(enabled);
    }

    /**
     * If true, WebClient.getResponse throws an exception when it receives an error status. Defaults to true.
     *
     * @param enabled
     *            the new exceptions thrown on error status
     */
    public static void setExceptionsThrownOnErrorStatus(boolean enabled) {
        _exceptionsOnErrorStatus = enabled;
    }

    /**
     * Returns true if WebClient.getResponse throws exceptions when detected an error status.
     *
     * @return the exceptions thrown on error status
     */
    public static boolean getExceptionsThrownOnErrorStatus() {
        return _exceptionsOnErrorStatus;
    }

    /**
     * Returns true if form parameter settings are checked.
     *
     * @return the parameter values validated
     *
     * @deprecated as of 1.6, use WebForm#newUnvalidatedRequest() to obtain a request without parameter validation.
     */
    @Deprecated
    public static boolean getParameterValuesValidated() {
        return _parameterValuesValidated;
    }

    /**
     * If true, tells HttpUnit to throw an exception on any attempt to set a form parameter to a value which could not
     * be set via the browser. The default is true (parameters are validated).<br>
     * <b>Note:</b> this only applies to a WebRequest created after this setting is changed. A request created with this
     * option disabled will not only not be checked for correctness, its parameter submission order will not be
     * guaranteed, and changing parameters will not trigger Javascript onChange / onClick events.
     *
     * @param validated
     *            the new parameter values validated
     *
     * @deprecated as of 1.6, use WebForm#newUnvalidatedRequest() to obtain a request without parameter validation.
     */
    @Deprecated
    public static void setParameterValuesValidated(boolean validated) {
        _parameterValuesValidated = validated;
    }

    /**
     * Returns true if images are treated as text, using their alt attributes.
     *
     * @return the images treated as alt text
     */
    public static boolean getImagesTreatedAsAltText() {
        return _imagesTreatedAsAltText;
    }

    /**
     * If true, tells HttpUnit to treat images with alt attributes as though they were the text value of that attribute
     * in all searches and displays. The default is false (image text is generally ignored).
     *
     * @param asText
     *            the new images treated as alt text
     */
    public static void setImagesTreatedAsAltText(boolean asText) {
        _imagesTreatedAsAltText = asText;
    }

    /**
     * If true, text matches in methods such as {@link HTMLSegment#getLinkWith} are case insensitive. The default is
     * true (matches ignore case).
     *
     * @return the matches ignore case
     */
    public static boolean getMatchesIgnoreCase() {
        return _matchesIgnoreCase;
    }

    /**
     * If true, text matches in methods such as {@link HTMLSegment#getLinkWith} are case insensitive. The default is
     * true (matches ignore case).
     *
     * @param ignoreCase
     *            the new matches ignore case
     */
    public static void setMatchesIgnoreCase(boolean ignoreCase) {
        _matchesIgnoreCase = ignoreCase;
    }

    /**
     * Returns true if HTTP headers are to be dumped to system output.
     *
     * @return true, if is logging http headers
     */
    public static boolean isLoggingHttpHeaders() {
        return _loggingHttpHeaders;
    }

    /**
     * If true, tells HttpUnit to log HTTP headers to system output. The default is false.
     *
     * @param enabled
     *            the new logging http headers
     */
    public static void setLoggingHttpHeaders(boolean enabled) {
        _loggingHttpHeaders = enabled;
    }

    /**
     * Returns true if HttpUnit throws an exception when attempting to parse as HTML a response whose content type is
     * not HTML. The default is false (content type is ignored).
     *
     * @return true, if is check html content type
     */
    public static boolean isCheckHtmlContentType() {
        return _checkHtmlContentType;
    }

    /**
     * If true, HttpUnit throws an exception when attempting to parse as HTML a response whose content type is not HTML.
     * The default is false (content type is ignored).
     *
     * @param checkHtmlContentType
     *            the new check html content type
     */
    public static void setCheckHtmlContentType(boolean checkHtmlContentType) {
        _checkHtmlContentType = checkHtmlContentType;
    }

    /**
     * Returns true if HttpUnit should automatically follow page redirect requests (status 3xx). By default, this is
     * true.
     *
     * @return the auto redirect
     *
     * @deprecated as of 1.5.3, use ClientProperties#isAutoRedirect();
     */
    @Deprecated
    public static boolean getAutoRedirect() {
        return ClientProperties.getDefaultProperties().isAutoRedirect();
    }

    /**
     * Determines whether HttpUnit should automatically follow page redirect requests (status 3xx). By default, this is
     * true in order to simulate normal browser operation.
     *
     * @param autoRedirect
     *            the new auto redirect
     *
     * @deprecated as of 1.5.3, use ClientProperties#setAutoRedirect();
     */
    @Deprecated
    public static void setAutoRedirect(boolean autoRedirect) {
        ClientProperties.getDefaultProperties().setAutoRedirect(autoRedirect);
    }

    /**
     * Returns the delay, in milliseconds, before a redirect request is issues.
     *
     * @return the redirect delay
     */
    public static int getRedirectDelay() {
        return _redirectDelay;
    }

    /**
     * Sets the delay, in milliseconds, before a redirect request is issued. This may be necessary if the server under
     * some cases where the server performs asynchronous processing which must be completed before the new request can
     * be handled properly, and is taking advantage of slower processing by most user agents. It almost always indicates
     * an error in the server design, and therefore the default delay is zero.
     *
     * @param delayInMilliseconds
     *            the new redirect delay
     */
    public static void setRedirectDelay(int delayInMilliseconds) {
        _redirectDelay = delayInMilliseconds;
    }

    /**
     * Returns true if HttpUnit should automatically follow page refresh requests. By default, this is false, so that
     * programs can verify the redirect page presented to users before the browser switches to the new page.
     *
     * @return the auto refresh
     *
     * @deprecated as of 1.5.3, use ClientProperties#isAutoRefresh();
     */
    @Deprecated
    public static boolean getAutoRefresh() {
        return ClientProperties.getDefaultProperties().isAutoRefresh();
    }

    /**
     * Specifies whether HttpUnit should automatically follow page refresh requests. By default, this is false, so that
     * programs can verify the redirect page presented to users before the browser switches to the new page. Setting
     * this to true can cause an infinite loop on pages that refresh themselves.
     *
     * @param autoRefresh
     *            the new auto refresh
     *
     * @deprecated as of 1.5.3, use ClientProperties#setAutoRefresh();
     */
    @Deprecated
    public static void setAutoRefresh(boolean autoRefresh) {
        ClientProperties.getDefaultProperties().setAutoRefresh(autoRefresh);
    }

    /**
     * Remove an Html error listener.
     *
     * @param el
     *            the el
     *
     * @deprecated as of 1.5.2, use HTMLParserfactory#removeHTMLParserListener
     */
    @Deprecated
    public static void removeHtmlErrorListener(HTMLParserListener el) {
        HTMLParserFactory.removeHTMLParserListener(el);
    }

    /**
     * Add an Html error listener.
     *
     * @param el
     *            the el
     *
     * @deprecated as of 1.5.2, use HTMLParserfactory#addHTMLParserListener
     */
    @Deprecated
    public static void addHtmlErrorListener(HTMLParserListener el) {
        HTMLParserFactory.addHTMLParserListener(el);
    }

    /**
     * Get the list of Html Error Listeners.
     *
     * @return the html error listeners
     *
     * @deprecated as of 1.5.2, removed with no replacement
     */
    @Deprecated
    public static List getHtmlErrorListeners() {
        return null;
    }

    /**
     * Gets the script engine class name.
     *
     * @return the script engine class name
     */
    public static String getScriptEngineClassName() {
        return _scriptEngineClassName;
    }

    /**
     * Sets the script engine class name.
     *
     * @param scriptEngineClassName
     *            the new script engine class name
     */
    public static void setScriptEngineClassName(String scriptEngineClassName) {
        if (_scriptEngineClassName == null || !_scriptEngineClassName.equals(scriptEngineClassName)) {
            _scriptingEngine = null;
        }
        _scriptEngineClassName = scriptEngineClassName;
    }

    /**
     * Gets the scripting engine.
     *
     * @return the scripting engine
     */
    public static ScriptingEngineFactory getScriptingEngine() {
        if (_scriptingEngine == null) {
            try {
                Class factoryClass = Class.forName(_scriptEngineClassName);
                final ScriptingEngineFactory factory = (ScriptingEngineFactory) factoryClass.getDeclaredConstructor()
                        .newInstance();
                _scriptingEngine = factory.isEnabled() ? factory : NULL_SCRIPTING_ENGINE_FACTORY;
                _scriptingEngine.setThrowExceptionsOnError(_exceptionsThrownOnScriptError);
            } catch (ClassNotFoundException e) {
                disableScripting(e, "Unable to find scripting engine factory class ");
            } catch (InstantiationException e) {
                disableScripting(e, "Unable to instantiate scripting engine factory class ");
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                    | NoSuchMethodException | SecurityException e) {
                disableScripting(e, "Unable to create scripting engine factory class ");
            }
        }
        return _scriptingEngine;
    }

    /**
     * change the scriptingEnabled flag.
     *
     * @param scriptingEnabled
     *            the new scripting enabled
     */
    public static void setScriptingEnabled(boolean scriptingEnabled) {
        if (scriptingEnabled != _scriptingEnabled) {
            _scriptingEngine = scriptingEnabled ? null : NULL_SCRIPTING_ENGINE_FACTORY;
        }
        _scriptingEnabled = scriptingEnabled;
    }

    /**
     * Checks if is scripting enabled.
     *
     * @return true, if is scripting enabled
     */
    public static boolean isScriptingEnabled() {
        return _scriptingEnabled;
    }

    /**
     * Determines whether script errors result in exceptions or warning messages.
     *
     * @param throwExceptions
     *            the throw exceptions
     *
     * @return the current state
     */
    public static boolean setExceptionsThrownOnScriptError(boolean throwExceptions) {
        boolean current = _exceptionsThrownOnScriptError;
        _exceptionsThrownOnScriptError = throwExceptions;
        getScriptingEngine().setThrowExceptionsOnError(throwExceptions);
        return current;
    }

    /**
     * Returns true if script errors cause exceptions to be thrown.
     *
     * @return the exceptions thrown on script error
     */
    public static boolean getExceptionsThrownOnScriptError() {
        return _exceptionsThrownOnScriptError;
    }

    /**
     * Returns the accumulated script error messages encountered. Error messages are accumulated only if
     * 'throwExceptionsOnError' is disabled.
     *
     * @return the script error messages
     */
    public static String[] getScriptErrorMessages() {
        return getScriptingEngine().getErrorMessages();
    }

    /**
     * Clears the accumulated script error messages.
     */
    public static void clearScriptErrorMessages() {
        getScriptingEngine().clearErrorMessages();
    }

    /**
     * Disable scripting.
     *
     * @param e
     *            the e
     * @param errorMessage
     *            the error message
     */
    private static void disableScripting(Exception e, String errorMessage) {
        System.err.println(errorMessage + _scriptEngineClassName);
        System.err.println("" + e);
        System.err.println("JavaScript execution disabled");
        _scriptingEngine = NULL_SCRIPTING_ENGINE_FACTORY;
    }

    // --------------------------------- private members --------------------------------------

    /** The Constant DEFAULT_CONTENT_TYPE. */
    private static final String DEFAULT_CONTENT_TYPE = "text/html";

    /** The Constant NULL_SCRIPTING_ENGINE_FACTORY. */
    private static final ScriptingEngineFactory NULL_SCRIPTING_ENGINE_FACTORY = new ScriptingEngineFactory() {
        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public void associate(WebResponse response) {
        }

        @Override
        public void load(WebResponse response) {
        }

        @Override
        public void setThrowExceptionsOnError(boolean throwExceptions) {
        }

        @Override
        public boolean isThrowExceptionsOnError() {
            return false;
        }

        @Override
        public String[] getErrorMessages() {
            return new String[0];
        }

        @Override
        public void clearErrorMessages() {
        }

        @Override
        public ScriptingHandler createHandler(HTMLElement element) {
            return ScriptableDelegate.NULL_SCRIPT_ENGINE;
        }

        @Override
        public ScriptingHandler createHandler(WebResponse response) {
            return ScriptableDelegate.NULL_SCRIPT_ENGINE;
        }

        @Override
        public void handleScriptException(Exception e, String badScript) {
            // happily ignore and exception
        }
    };

    /**
     * Add the name of a custom attribute that should be supported for form controls.
     *
     * @param attributeName
     *            the attribute name
     *
     * @deprecated for new Scripting engine
     */
    @Deprecated
    public static void addCustomAttribute(String attributeName) {
        if (_customAttributes == null) {
            _customAttributes = new HashSet<>();
        }
        _customAttributes.add(attributeName);
    }

    /**
     * Get the Set of custom attribute names to be supported by form controls.
     *
     * @return the custom attributes
     *
     * @deprecated for new scripting engine
     */
    @Deprecated
    static Set getCustomAttributes() {
        return _customAttributes;
    }

    /** The custom attributes. */
    private static Set _customAttributes = null;

    /** The exceptions on error status. */
    private static boolean _exceptionsOnErrorStatus = true;

    /** The parameter values validated. */
    private static boolean _parameterValuesValidated = true;

    /** The images treated as alt text. */
    private static boolean _imagesTreatedAsAltText;

    /** The logging http headers. */
    private static boolean _loggingHttpHeaders;

    /** The matches ignore case. */
    private static boolean _matchesIgnoreCase = true;

    /** The post includes charset. */
    private static boolean _postIncludesCharset = false;

    /** The check content length. */
    private static boolean _checkContentLength = false;

    /** The redirect delay. */
    private static int _redirectDelay;

    /** The character set. */
    private static String _characterSet = StandardCharsets.ISO_8859_1.name();

    /** The content type. */
    private static String _contentType = DEFAULT_CONTENT_TYPE;

    /** The script engine class name. */
    private static String _scriptEngineClassName;

    /** The scripting engine. */
    private static ScriptingEngineFactory _scriptingEngine;

    /** The scripting enabled. */
    private static boolean _scriptingEnabled = true;

    /** The exceptions thrown on script error. */
    private static boolean _exceptionsThrownOnScriptError = true;

    /** The java script optimization level. */
    private static int _javaScriptOptimizationLevel = -1;

    /** The check html content type. */
    private static boolean _checkHtmlContentType = false;

    static {
        reset();

    }

    /**
     * getter for Java Script optimization level.
     *
     * @return the javaScriptOptimizationLevel to be use for running scripts
     */
    public static int getJavaScriptOptimizationLevel() {
        return _javaScriptOptimizationLevel;
    }

    /**
     * setter for Java Script optimization level.
     *
     * @param scriptOptimizationLevel
     *            the _javaScriptOptimizationLevel to set see rhino documentation for valid values: -2: with
     *            continuation -1: interpret 0: compile to Java bytecode, don't optimize 1..9: compile to Java bytecode,
     *            optimize *
     */
    public static void setJavaScriptOptimizationLevel(int scriptOptimizationLevel) {
        _javaScriptOptimizationLevel = scriptOptimizationLevel;
    }
}

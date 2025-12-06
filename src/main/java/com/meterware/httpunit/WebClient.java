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

import com.meterware.httpunit.cookies.Cookie;
import com.meterware.httpunit.cookies.CookieJar;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.xml.sax.SAXException;

/**
 * The context for a series of web requests. This class manages cookies used to maintain session context, computes
 * relative URLs, and generally emulates the browser behavior needed to build an automated test of a web site.
 **/
public abstract class WebClient {

    /** The open windows. */
    private ArrayList _openWindows = new ArrayList<>();

    /** The current main window. **/
    private WebWindow _mainWindow = new WebWindow(this);

    /** An authorization string to be sent with every request, whether challenged or not. May be null. **/
    private String _fixedAuthorizationString;

    /** An authorization string to be sent with the next request only. May be null. **/
    private String _authorizationString;

    /** The proxy authorization string. */
    private String _proxyAuthorizationString;

    /** The credentials. */
    private Hashtable _credentials = new Hashtable<>();

    /**
     * Gets the main window.
     *
     * @return the main window
     */
    public WebWindow getMainWindow() {
        return _mainWindow;
    }

    /**
     * Sets the main window.
     *
     * @param mainWindow
     *            the new main window
     */
    public void setMainWindow(WebWindow mainWindow) {
        if (!_openWindows.contains(mainWindow)) {
            throw new IllegalArgumentException("May only select an open window owned by this client");
        }
        _mainWindow = mainWindow;
    }

    /**
     * Gets the open windows.
     *
     * @return the open windows
     */
    public WebWindow[] getOpenWindows() {
        return (WebWindow[]) _openWindows.toArray(new WebWindow[_openWindows.size()]);
    }

    /**
     * Gets the open window.
     *
     * @param name
     *            the name
     *
     * @return the open window
     */
    public WebWindow getOpenWindow(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        for (Iterator i = _openWindows.iterator(); i.hasNext();) {
            WebWindow window = (WebWindow) i.next();
            if (name.equals(window.getName())) {
                return window;
            }
        }
        return null;
    }

    /**
     * Submits a GET method request and returns a response.
     *
     * @param urlString
     *            the url string
     *
     * @return the response
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     *
     * @exception SAXException
     *                thrown if there is an error parsing the retrieved page
     */
    public WebResponse getResponse(String urlString) throws IOException, SAXException {
        return _mainWindow.getResponse(urlString);
    }

    /**
     * Submits a web request and returns a response. This is an alternate name for the getResponse method.
     *
     * @param request
     *            the request
     *
     * @return the web response
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws SAXException
     *             the SAX exception
     */
    public WebResponse sendRequest(WebRequest request) throws IOException, SAXException {
        return _mainWindow.sendRequest(request);
    }

    /**
     * Returns the response representing the current top page in the main window.
     *
     * @return the current page
     */
    public WebResponse getCurrentPage() {
        return _mainWindow.getCurrentPage();
    }

    /**
     * Submits a web request and returns a response, using all state developed so far as stored in cookies as requested
     * by the server.
     *
     * @param request
     *            the request
     *
     * @return the response
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     *
     * @exception SAXException
     *                thrown if there is an error parsing the retrieved page
     */
    public WebResponse getResponse(WebRequest request) throws IOException, SAXException {
        return _mainWindow.getResponse(request);
    }

    /**
     * Returns the name of the currently active frames in the main window.
     *
     * @return the frame names
     */
    public String[] getFrameNames() {
        return _mainWindow.getFrameNames();
    }

    /**
     * Returns the response associated with the specified frame name in the main window. Throws a runtime exception if
     * no matching frame is defined.
     *
     * @param frameName
     *            the frame name
     *
     * @return the frame contents
     */
    public WebResponse getFrameContents(String frameName) {
        return _mainWindow.getFrameContents(frameName);
    }

    /**
     * Returns the response associated with the specified frame name in the main window. Throws a runtime exception if
     * no matching frame is defined.
     *
     * @param targetFrame
     *            the target frame
     *
     * @return the frame contents
     */
    public WebResponse getFrameContents(FrameSelector targetFrame) {
        return _mainWindow.getFrameContents(targetFrame);
    }

    /**
     * Returns the resource specified by the request. Does not update the client or load included framesets or scripts.
     * May return null if the resource is a JavaScript URL which would normally leave the client unchanged.
     *
     * @param request
     *            the request
     *
     * @return the resource
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public WebResponse getResource(WebRequest request) throws IOException {
        return _mainWindow.getResource(request);
    }

    /**
     * Resets the state of this client, removing all cookies, frames, and per-client headers. This does not affect any
     * listeners or preferences which may have been set.
     **/
    public void clearContents() {
        _mainWindow = new WebWindow(this);
        _cookieJar.clear();
        _headers = new HeaderDictionary();
    }

    /**
     * Defines a cookie to be sent to the server on every request.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     *
     * @deprecated as of 1.6, use #putCookie instead.
     */
    @Deprecated
    public void addCookie(String name, String value) {
        _cookieJar.addCookie(name, value);
    }

    /**
     * Defines a cookie to be sent to the server on every request. This overrides any previous setting for this cookie
     * name.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     */
    public void putCookie(String name, String value) {
        _cookieJar.putCookie(name, value);
    }

    /**
     * Returns the name of all the active cookies which will be sent to the server.
     *
     * @return the cookie names
     */
    public String[] getCookieNames() {
        return _cookieJar.getCookieNames();
    }

    /**
     * Returns an object containing the details of the named cookie.
     *
     * @param name
     *            the name
     *
     * @return the cookie details
     */
    public Cookie getCookieDetails(String name) {
        return _cookieJar.getCookie(name);
    }

    /**
     * Returns the value of the specified cookie.
     *
     * @param name
     *            the name
     *
     * @return the cookie value
     */
    public String getCookieValue(String name) {
        return _cookieJar.getCookieValue(name);
    }

    /**
     * Returns the properties associated with this client.
     *
     * @return the client properties
     */
    public ClientProperties getClientProperties() {
        if (_clientProperties == null) {
            _clientProperties = ClientProperties.getDefaultProperties().cloneProperties();
        }
        return _clientProperties;
    }

    /**
     * Specifies the user agent identification. Used to trigger browser-specific server behavior.
     *
     * @param userAgent
     *            the new user agent
     *
     * @deprecated as of 1.4.6. Use ClientProperties#setUserAgent instead.
     */
    @Deprecated
    public void setUserAgent(String userAgent) {
        getClientProperties().setUserAgent(userAgent);
    }

    /**
     * Returns the current user agent setting.
     *
     * @return the user agent
     *
     * @deprecated as of 1.4.6. Use ClientProperties#getUserAgent instead.
     */
    @Deprecated
    public String getUserAgent() {
        return getClientProperties().getUserAgent();
    }

    /**
     * Sets a username and password for a basic authentication scheme. Use #setAuthentication for more accurate
     * emulation of browser behavior.
     *
     * @param userName
     *            the user name
     * @param password
     *            the password
     */
    public void setAuthorization(String userName, String password) {
        _fixedAuthorizationString = "Basic "
                + Base64.getEncoder().encodeToString((userName + ':' + password).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Specifies a username and password for on-demand authentication. Will only send the authorization header when
     * challenged for the specified realm.
     *
     * @param realm
     *            the realm for which the credentials apply.
     * @param username
     *            the user to authenticate
     * @param password
     *            the credentials for the user
     */
    public void setAuthentication(String realm, String username, String password) {
        _credentials.put(realm, new PasswordAuthentication(username, password.toCharArray()));
    }

    /**
     * get the credentials for the given realm.
     *
     * @param realm
     *            the realm
     *
     * @return the credentials for realm
     */
    PasswordAuthentication getCredentialsForRealm(String realm) {
        if (_credentials == null) {
            throw new Error("null _credentials while calling getCredentialsForRealm");
        }
        if (realm == null) {
            throw new Error("null realm while calling getCredentialsForRealm");
        }
        return (PasswordAuthentication) _credentials.get(realm);
    }

    /**
     * Specifies a proxy server to use for requests from this client.
     *
     * @param proxyHost
     *            the proxy host
     * @param proxyPort
     *            the proxy port
     */
    public abstract void setProxyServer(String proxyHost, int proxyPort);

    /**
     * Specifies a proxy server to use, along with a user and password for authentication.
     *
     * @param proxyHost
     *            the proxy host
     * @param proxyPort
     *            the proxy port
     * @param userName
     *            the user name
     * @param password
     *            the password
     */
    public void setProxyServer(String proxyHost, int proxyPort, String userName, String password) {
        setProxyServer(proxyHost, proxyPort);
        _proxyAuthorizationString = "Basic "
                + Base64.getEncoder().encodeToString((userName + ':' + password).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Clears the proxy server settings.
     */
    public void clearProxyServer() {
    }

    /**
     * Returns the name of the active proxy server.
     *
     * @return the proxy host
     */
    public String getProxyHost() {
        return System.getProperty("proxyHost");
    }

    /**
     * Returns the number of the active proxy port, or 0 is none is specified.
     *
     * @return the proxy port
     */
    public int getProxyPort() {
        try {
            return Integer.getInteger("proxyPort");
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Sets the value for a header field to be sent with all requests. If the value set is null, removes the header from
     * those to be sent.
     *
     * @param fieldName
     *            the field name
     * @param fieldValue
     *            the field value
     */
    public void setHeaderField(String fieldName, String fieldValue) {
        _headers.put(fieldName, fieldValue);
    }

    /**
     * Returns the value for the header field with the specified name. This method will ignore the case of the field
     * name.
     *
     * @param fieldName
     *            the field name
     *
     * @return the header field
     */
    public String getHeaderField(String fieldName) {
        return (String) _headers.get(fieldName);
    }

    /**
     * Specifies whether an exception will be thrown when an error status (4xx or 5xx) is detected on a response.
     * Defaults to the value returned by HttpUnitOptions.getExceptionsThrownOnErrorStatus.
     *
     * @param throwExceptions
     *            the new exceptions thrown on error status
     */
    public void setExceptionsThrownOnErrorStatus(boolean throwExceptions) {
        _exceptionsThrownOnErrorStatus = throwExceptions;
    }

    /**
     * Returns true if an exception will be thrown when an error status (4xx or 5xx) is detected on a response.
     *
     * @return the exceptions thrown on error status
     */
    public boolean getExceptionsThrownOnErrorStatus() {
        return _exceptionsThrownOnErrorStatus;
    }

    /**
     * Adds a listener to watch for requests and responses.
     *
     * @param listener
     *            the listener
     */
    public void addClientListener(WebClientListener listener) {
        synchronized (_clientListeners) {
            if (listener != null && !_clientListeners.contains(listener)) {
                _clientListeners.add(listener);
            }
        }
    }

    /**
     * Removes a listener to watch for requests and responses.
     *
     * @param listener
     *            the listener
     */
    public void removeClientListener(WebClientListener listener) {
        synchronized (_clientListeners) {
            _clientListeners.remove(listener);
        }
    }

    /**
     * Adds a listener to watch for window openings and closings.
     *
     * @param listener
     *            the listener
     */
    public void addWindowListener(WebWindowListener listener) {
        synchronized (_windowListeners) {
            if (listener != null && !_windowListeners.contains(listener)) {
                _windowListeners.add(listener);
            }
        }
    }

    /**
     * Removes a listener to watch for window openings and closings.
     *
     * @param listener
     *            the listener
     */
    public void removeWindowListener(WebWindowListener listener) {
        synchronized (_windowListeners) {
            _windowListeners.remove(listener);
        }
    }

    /**
     * Returns the next javascript alert without removing it from the queue.
     *
     * @return the next alert
     */
    public String getNextAlert() {
        return _alerts.isEmpty() ? null : (String) _alerts.getFirst();
    }

    /**
     * Returns the next javascript alert and removes it from the queue. If the queue is empty, will return an empty
     * string.
     *
     * @return the string
     */
    public String popNextAlert() {
        if (_alerts.isEmpty()) {
            return "";
        }
        return (String) _alerts.removeFirst();
    }

    /**
     * Specifies the object which will respond to all dialogs.
     *
     * @param responder
     *            the new dialog responder
     */
    public void setDialogResponder(DialogResponder responder) {
        _dialogResponder = responder;
    }

    // ------------------------------------------ protected members -----------------------------------

    /**
     * Instantiates a new web client.
     */
    protected WebClient() {
        _openWindows.add(_mainWindow);
    }

    /**
     * Creates a web response object which represents the response to the specified web request.
     *
     * @param request
     *            the request to which the response should be generated
     * @param targetFrame
     *            the frame in which the response should be stored
     *
     * @return the web response
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    abstract protected WebResponse newResponse(WebRequest request, FrameSelector targetFrame) throws IOException;

    /**
     * Writes the message body for the request.
     *
     * @param request
     *            the request
     * @param stream
     *            the stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected final void writeMessageBody(WebRequest request, OutputStream stream) throws IOException {
        request.writeMessageBody(stream);
    }

    /**
     * Returns the value of all current header fields.
     *
     * @param targetURL
     *            the target URL
     *
     * @return the header fields
     */
    protected Dictionary getHeaderFields(URL targetURL) {
        Hashtable result = (Hashtable) _headers.clone();
        result.put("User-Agent", getClientProperties().getUserAgent());
        if (getClientProperties().isAcceptGzip()) {
            result.put("Accept-Encoding", "gzip");
        }
        AddHeaderIfNotNull(result, "Cookie", _cookieJar.getCookieHeaderField(targetURL));
        if (_authorizationString == null) {
            _authorizationString = _fixedAuthorizationString;
        }
        AddHeaderIfNotNull(result, "Authorization", _authorizationString);
        AddHeaderIfNotNull(result, "Proxy-Authorization", _proxyAuthorizationString);
        _authorizationString = null;
        return result;
    }

    /**
     * Adds the header if not null.
     *
     * @param result
     *            the result
     * @param headerName
     *            the header name
     * @param headerValue
     *            the header value
     */
    private void AddHeaderIfNotNull(Hashtable result, final String headerName, final String headerValue) {
        if (headerValue != null) {
            result.put(headerName, headerValue);
        }
    }

    /**
     * Updates this web client based on a received response. This includes updating cookies and frames. This method is
     * required by ServletUnit, which cannot call the updateWindow method directly.
     *
     * @param frame
     *            the frame
     * @param response
     *            the response
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws SAXException
     *             the SAX exception
     */
    protected final void updateMainWindow(FrameSelector frame, WebResponse response) throws IOException, SAXException {
        getMainWindow().updateWindow(frame.getName(), response, new RequestContext());
    }

    // ------------------------------------------------- package members
    // ----------------------------------------------------

    /**
     * Tell listeners.
     *
     * @param request
     *            the request
     */
    void tellListeners(WebRequest request) {
        List listeners;

        synchronized (_clientListeners) {
            listeners = new ArrayList(_clientListeners);
        }

        for (Iterator i = listeners.iterator(); i.hasNext();) {
            ((WebClientListener) i.next()).requestSent(this, request);
        }
    }

    /**
     * Tell listeners.
     *
     * @param response
     *            the response
     */
    void tellListeners(WebResponse response) {
        List listeners;

        synchronized (_clientListeners) {
            listeners = new ArrayList(_clientListeners);
        }

        for (Iterator i = listeners.iterator(); i.hasNext();) {
            ((WebClientListener) i.next()).responseReceived(this, response);
        }
    }

    /**
     * Update client.
     *
     * @param response
     *            the response
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    void updateClient(WebResponse response) throws IOException {
        if (getClientProperties().isAcceptCookies()) {
            _cookieJar.updateCookies(response.getCookieJar());
        }
        validateHeaders(response);
    }

    /**
     * Support Request [ 1288796 ] getCookieJar() in WebClient.
     *
     * @return the cookie jar
     *
     * @deprecated - use with care - was not public in the past
     */
    @Deprecated
    public CookieJar getCookieJar() {
        return _cookieJar;
    }

    /**
     * Update frame contents.
     *
     * @param requestWindow
     *            the request window
     * @param requestTarget
     *            the request target
     * @param response
     *            the response
     * @param requestContext
     *            the request context
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws SAXException
     *             the SAX exception
     */
    void updateFrameContents(WebWindow requestWindow, String requestTarget, WebResponse response,
            RequestContext requestContext) throws IOException, SAXException {
        if (response.getFrame() == FrameSelector.NEW_FRAME) {
            WebWindow window = new WebWindow(this, requestWindow.getCurrentPage());
            if (!WebRequest.NEW_WINDOW.equalsIgnoreCase(requestTarget)) {
                window.setName(requestTarget);
            }
            response.setFrame(window.getTopFrame());
            window.updateFrameContents(response, requestContext);
            _openWindows.add(window);
            reportWindowOpened(window);
        } else if (response.getFrame().getWindow() != null && response.getFrame().getWindow() != requestWindow) {
            response.getFrame().getWindow().updateFrameContents(response, requestContext);
        } else {
            if (response.getFrame() == FrameSelector.TOP_FRAME) {
                response.setFrame(requestWindow.getTopFrame());
            }
            requestWindow.updateFrameContents(response, requestContext);
        }
    }

    /**
     * Close.
     *
     * @param window
     *            the window
     */
    void close(WebWindow window) {
        if (!_openWindows.contains(window)) {
            throw new IllegalStateException("Window is already closed");
        }
        _openWindows.remove(window);
        if (_openWindows.isEmpty()) {
            _openWindows.add(new WebWindow(this));
        }
        if (window.equals(_mainWindow)) {
            _mainWindow = (WebWindow) _openWindows.get(0);
        }
        reportWindowClosed(window);
    }

    /**
     * Report window opened.
     *
     * @param window
     *            the window
     */
    private void reportWindowOpened(WebWindow window) {
        List listeners;

        synchronized (_windowListeners) {
            listeners = new ArrayList(_windowListeners);
        }

        for (Iterator i = listeners.iterator(); i.hasNext();) {
            ((WebWindowListener) i.next()).windowOpened(this, window);
        }
    }

    /**
     * Report window closed.
     *
     * @param window
     *            the window
     */
    private void reportWindowClosed(WebWindow window) {
        List listeners;

        synchronized (_windowListeners) {
            listeners = new ArrayList(_windowListeners);
        }

        for (Iterator i = listeners.iterator(); i.hasNext();) {
            ((WebWindowListener) i.next()).windowClosed(this, window);
        }
    }

    // ------------------------------------------ package members ------------------------------------

    /**
     * Gets the confirmation response.
     *
     * @param message
     *            the message
     *
     * @return the confirmation response
     */
    boolean getConfirmationResponse(String message) {
        return _dialogResponder.getConfirmation(message);
    }

    /**
     * Gets the user response.
     *
     * @param message
     *            the message
     * @param defaultResponse
     *            the default response
     *
     * @return the user response
     */
    String getUserResponse(String message, String defaultResponse) {
        return _dialogResponder.getUserResponse(message, defaultResponse);
    }

    /**
     * simulate an alert by remembering the alert message on a Stack.
     *
     * @param message
     *            - the alert message to post
     */
    void postAlert(String message) {
        _alerts.addLast(message);
    }

    // ------------------------------------------ private members -------------------------------------

    /** The list of alerts generated by JavaScript. **/
    private LinkedList _alerts = new LinkedList();

    /** The currently defined cookies. **/
    private CookieJar _cookieJar = new CookieJar();

    /** A map of header names to values. **/
    private HeaderDictionary _headers = new HeaderDictionary();

    /** The exceptions thrown on error status. */
    private boolean _exceptionsThrownOnErrorStatus = HttpUnitOptions.getExceptionsThrownOnErrorStatus();

    /** The client listeners. */
    private final List _clientListeners = new ArrayList<>();

    /** The window listeners. */
    private final List _windowListeners = new ArrayList<>();

    /** The dialog responder. */
    private DialogResponder _dialogResponder = new DialogAdapter();

    /** The client properties. */
    private ClientProperties _clientProperties;

    /**
     * Examines the headers in the response and throws an exception if appropriate.
     *
     * @param response
     *            the response
     *
     * @throws HttpException
     *             the http exception
     *
     * @parm response - the response to validate
     */
    private void validateHeaders(WebResponse response) throws HttpException {
        HttpException exception = null;
        if (response.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
            exception = new HttpInternalErrorException(response.getURL());
        } else if (response.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
            exception = new HttpNotFoundException(response.getResponseMessage(), response.getURL());
        } else if (response.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
            exception = new HttpException(response.getResponseCode(), response.getResponseMessage(), response.getURL());
        }
        // is there an exception?
        if (exception != null) {
            // see feature request [ 914314 ] Add HttpException.getResponse for better reporting
            exception.setResponse(response);
            // shall we ignore errors?
            if (!getExceptionsThrownOnErrorStatus()) {
                return;
            }
            throw exception;
        }
    }

    /**
     * Find frame.
     *
     * @param target
     *            the target
     *
     * @return the frame selector
     */
    FrameSelector findFrame(String target) {
        for (Object _openWindow : _openWindows) {
            WebWindow webWindow = (WebWindow) _openWindow;
            FrameSelector frame = webWindow.getFrame(target);
            if (frame != null) {
                return frame;
            }
        }
        return null;
    }

    /**
     * Sends a request and returns a response after dealing with any authentication challenge. If challenged and able to
     * respond, resends the request after setting the authentication header (which will apply only for the that
     * request).
     *
     * @param request
     *            the original request
     * @param targetFrame
     *            the frame into which the result will be stored
     *
     * @return a response from the server
     *
     * @throws IOException
     *             if an exception (including authorization failure) occurs
     */
    WebResponse createResponse(WebRequest request, FrameSelector targetFrame) throws IOException {
        WebResponse response = newResponse(request, targetFrame);
        AuthenticationChallenge challenge = new AuthenticationChallenge(this, request,
                response.getHeaderField("WWW-Authenticate"));
        if (!challenge.needToAuthenticate()) {
            return response;
        }
        setOnetimeAuthenticationHeader(challenge.createAuthenticationHeader());
        WebResponse response2 = newResponse(request, targetFrame);
        if (response2.getHeaderField("WWW-Authenticate") != null && getExceptionsThrownOnErrorStatus()) {
            throw AuthenticationChallenge.createException(response2.getHeaderField("WWW-Authenticate"));
        }
        return response2;
    }

    /**
     * Sets the onetime authentication header.
     *
     * @param authorizationHeader
     *            the new onetime authentication header
     */
    private void setOnetimeAuthenticationHeader(String authorizationHeader) {
        _authorizationString = authorizationHeader;
    }

    // ==================================================================================================

    /**
     * The Class HeaderDictionary.
     */
    static public class HeaderDictionary extends Hashtable {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /**
         * Adds the entries.
         *
         * @param source
         *            the source
         */
        public void addEntries(Dictionary source) {
            for (Enumeration e = source.keys(); e.hasMoreElements();) {
                Object key = e.nextElement();
                put(key, source.get(key));
            }
        }

        @Override
        public boolean containsKey(Object key) {
            return super.containsKey(matchPreviousFieldName(key.toString()));
        }

        @Override
        public Object get(Object fieldName) {
            return super.get(matchPreviousFieldName(fieldName.toString()));
        }

        @Override
        public Object put(Object fieldName, Object fieldValue) {
            fieldName = matchPreviousFieldName(fieldName.toString());
            Object oldValue = super.get(fieldName);
            if (fieldValue == null) {
                remove(fieldName);
            } else {
                super.put(fieldName, fieldValue);
            }
            return oldValue;
        }

        /**
         * If a matching field name with different case is already known, returns the older name. Otherwise, returns the
         * specified name.
         *
         * @param fieldName
         *            the field name
         *
         * @return the string
         */
        private String matchPreviousFieldName(String fieldName) {
            for (Enumeration e = keys(); e.hasMoreElements();) {
                String key = (String) e.nextElement();
                if (key.equalsIgnoreCase(fieldName)) {
                    return key;
                }
            }
            return fieldName;
        }

    }

}

// ==================================================================================================

class RedirectWebRequest extends WebRequest {

    RedirectWebRequest(WebResponse response) {
        super(response.getURL(), response.getHeaderField("Location"), response.getFrame(), response.getFrameName());
        if (response.getReferer() != null) {
            setHeaderField("Referer", response.getReferer());
        }
    }

    /**
     * Returns the HTTP method defined for this request.
     **/
    @Override
    public String getMethod() {
        return "GET";
    }
}

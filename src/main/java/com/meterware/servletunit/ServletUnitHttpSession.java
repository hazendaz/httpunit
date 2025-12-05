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
package com.meterware.servletunit;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

import java.net.URL;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * The Class ServletUnitHttpSession.
 */
class ServletUnitHttpSession implements HttpSession {

    /** The Constant SESSION_COOKIE_NAME. */
    public static final String SESSION_COOKIE_NAME = "JSESSION";

    /** The servlet context. */
    private ServletContext _servletContext;

    /** The listener dispatcher. */
    private SessionListenerDispatcher _listenerDispatcher;

    /**
     * Instantiates a new servlet unit http session.
     *
     * @param servletContext
     *            the servlet context
     * @param listenerDispatcher
     *            the listener dispatcher
     */
    ServletUnitHttpSession(ServletContext servletContext, SessionListenerDispatcher listenerDispatcher) {
        _servletContext = servletContext;
        _listenerDispatcher = listenerDispatcher;
    }

    /**
     * Returns the maximum time interval, in seconds, that the servlet engine will keep this session open between client
     * requests. You can set the maximum time interval with the setMaxInactiveInterval method.
     **/
    @Override
    public int getMaxInactiveInterval() {
        if (_invalid) {
            throw new IllegalStateException();
        }
        return _maxInactiveInterval;
    }

    /**
     * Specifies the maximum length of time, in seconds, that the servlet engine keeps this session if no user requests
     * have been made of the session.
     **/
    @Override
    public void setMaxInactiveInterval(int interval) {
        if (_invalid) {
            throw new IllegalStateException();
        }
        _maxInactiveInterval = interval;
    }

    /**
     * Returns a string containing the unique identifier assigned to this session. The identifier is assigned by the
     * servlet engine and is implementation dependent.
     **/
    @Override
    public String getId() {
        if (_invalid) {
            throw new IllegalStateException();
        }
        return _id;
    }

    /**
     * Returns the time when this session was created, measured in milliseconds since midnight January 1, 1970 GMT.
     *
     * @exception IllegalStateException
     *                if you attempt to get the session's creation time after the session has been invalidated
     **/
    @Override
    public long getCreationTime() {
        if (_invalid) {
            throw new IllegalStateException();
        }
        return _creationTime;
    }

    /**
     * Returns the last time the client sent a request associated with this session, as the number of milliseconds since
     * midnight January 1, 1970 GMT.
     **/
    @Override
    public long getLastAccessedTime() {
        if (_invalid) {
            throw new IllegalStateException();
        }
        return _lastAccessedTime;
    }

    /**
     * Returns true if the Web server has created a session but the client has not yet joined. For example, if the
     * server used only cookie-based sessions, and the client had disabled the use of cookies, then a session would be
     * new.
     **/
    @Override
    public boolean isNew() {
        return _isNew;
    }

    /**
     * Invalidates this session and unbinds any objects bound to it.
     **/
    @Override
    public void invalidate() {
        _listenerDispatcher.sendSessionDestroyed(this);
        _invalid = true;
        _values.clear();
    }

    /**
     * Returns the object bound with the specified name in this session or null if no object of that name exists.
     **/
    @Override
    public Object getAttribute(String name) {
        if (_invalid) {
            throw new IllegalStateException();
        }
        return _values.get(name);
    }

    /**
     * Binds an object to this session, using the name specified. If an object of the same name is already bound to the
     * session, the object is replaced.
     **/
    @Override
    public void setAttribute(String name, Object value) {
        if (_invalid) {
            throw new IllegalStateException();
        }

        if (value == null) {
            removeAttribute(name);
        } else if (!_values.containsKey(name)) {
            _values.put(name, value);
            _listenerDispatcher.sendAttributeAdded(this, name, value);
        } else {
            Object oldValue = _values.get(name);
            _values.put(name, value);
            _listenerDispatcher.sendAttributeReplaced(this, name, oldValue);
        }
    }

    /**
     * Removes the object bound with the specified name from this session. If the session does not have an object bound
     * with the specified name, this method does nothing.
     **/
    @Override
    public void removeAttribute(String name) {
        if (_invalid) {
            throw new IllegalStateException();
        }
        if (_values.containsKey(name)) {
            Object oldValue = _values.get(name);
            _values.remove(name);
            _listenerDispatcher.sendAttributeRemoved(this, name, oldValue);
        }
    }

    /**
     * Returns an array containing the names of all the objects bound to this session. This method is useful, for
     * example, when you want to delete all the objects bound to this session.
     **/
    @Override
    public Enumeration<String> getAttributeNames() {
        if (_invalid) {
            throw new IllegalStateException();
        }
        return _values.keys();
    }

    // ---------------------------- methods added to HttpSession in JSDK 2.3 ----------------------------------------

    /**
     * Returns the ServletContext to which this session belongs.
     **/
    @Override
    public ServletContext getServletContext() {
        return _servletContext;
    }

    // -------------------------------------------- package members -------------------------------------------------

    /**
     * This method should be invoked when a servlet joins an existing session. It will update the last access time and
     * mark the session as no longer new.
     **/
    void access() {
        _lastAccessedTime = new Date().getTime();
        _isNew = false;
    }

    /**
     * Gets the original URL.
     *
     * @return the original URL
     */
    URL getOriginalURL() {
        return _originalURL;
    }

    /**
     * Sets the original URL.
     *
     * @param originalURL
     *            the new original URL
     */
    void setOriginalURL(URL originalURL) {
        _originalURL = originalURL;
    }

    /**
     * Sets the authenticated user information for a session.
     *
     * @param userName
     *            the name the user supplied when logging in
     * @param roles
     *            an array of role names assigned to the user
     **/
    void setUserInformation(String userName, String[] roles) {
        _userName = userName;
        _roles = roles;
    }

    /**
     * Gets the user name.
     *
     * @return the user name
     */
    String getUserName() {
        return _userName;
    }

    /**
     * Gets the roles.
     *
     * @return the roles
     */
    String[] getRoles() {
        return _roles;
    }

    /**
     * Checks if is invalid.
     *
     * @return true, if is invalid
     */
    boolean isInvalid() {
        return _invalid;
    }

    // ------------------------------------- private members ---------------------------------------

    /** The Next ID. */
    private static int _NextID = 1;

    /** The creation time. */
    private final long _creationTime = new Date().getTime();

    /** The id. */
    private final String _id = Integer.toString(_NextID++);

    /** The max inactive interval. */
    private int _maxInactiveInterval;

    /** The last accessed time. */
    private long _lastAccessedTime = new Date().getTime();

    /** The invalid. */
    private boolean _invalid;

    /** The values. */
    private Hashtable _values = new Hashtable<>();

    /** The is new. */
    private boolean _isNew = true;

    /** The user name. */
    private String _userName;

    /** The roles. */
    private String[] _roles;

    /** The original URL. */
    private URL _originalURL;

}

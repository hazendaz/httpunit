/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.servletunit;

import jakarta.servlet.ServletContext;

import java.util.Hashtable;
import java.util.Set;

/**
 * The Class ServletUnitContext.
 */
class ServletUnitContext {

    /** The listener dispatcher. */
    private SessionListenerDispatcher _listenerDispatcher;

    /** The servlet context. */
    private ServletContext _servletContext;

    /**
     * Instantiates a new servlet unit context.
     *
     * @param contextPath
     *            the context path
     * @param servletContext
     *            the servlet context
     * @param dispatcher
     *            the dispatcher
     */
    ServletUnitContext(String contextPath, ServletContext servletContext, SessionListenerDispatcher dispatcher) {
        _servletContext = servletContext;
        _contextPath = contextPath != null ? contextPath : "";
        _listenerDispatcher = dispatcher;
    }

    /**
     * Gets the session I ds.
     *
     * @return the session I ds
     */
    Set getSessionIDs() {
        return _sessions.keySet();
    }

    /**
     * Returns an appropriate session for a request. If no cached session is
     *
     * @param sessionId
     *            the session id
     * @param session
     *            the session cached by previous requests. May be null.
     * @param create
     *            the create
     *
     * @return the valid session
     */
    ServletUnitHttpSession getValidSession(String sessionId, ServletUnitHttpSession session, boolean create) {
        if (session == null && sessionId != null) {
            session = getSession(sessionId);
        }

        if (session != null && session.isInvalid()) {
            session = null;
        }

        if (session == null && create) {
            session = newSession();
        }
        return session;
    }

    /**
     * Returns the session with the specified ID, if any.
     *
     * @param id
     *            the id
     *
     * @return the session
     */
    ServletUnitHttpSession getSession(String id) {
        return (ServletUnitHttpSession) _sessions.get(id);
    }

    /**
     * Creates a new session with a unique ID.
     *
     * @return the servlet unit http session
     */
    ServletUnitHttpSession newSession() {
        ServletUnitHttpSession result = new ServletUnitHttpSession(_servletContext, _listenerDispatcher);
        _sessions.put(result.getId(), result);
        _listenerDispatcher.sendSessionCreated(result);
        return result;
    }

    /**
     * Returns the contextPath.
     *
     * @return the context path
     */
    String getContextPath() {
        return _contextPath;
    }

    // ------------------------------- private members ---------------------------

    /** The sessions. */
    private Hashtable _sessions = new Hashtable<>();

    /** The context path. */
    private String _contextPath = null;

}

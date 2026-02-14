/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.servletunit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the HttpSession implementation.
 */
class SessionTest extends ServletUnitTest {

    /** The context. */
    private ServletUnitContext _context;

    /** The servlet context. */
    private ServletContext _servletContext = new ServletUnitServletContext(null);

    /**
     * Sets the up.
     *
     * @throws Exception
     *             the exception
     */
    @BeforeEach
    void setUp() throws Exception {
        _context = new ServletUnitContext(null, _servletContext, new SessionListenerDispatcher() {
            @Override
            public void sendSessionCreated(HttpSession session) {
            }

            @Override
            public void sendSessionDestroyed(HttpSession session) {
            }

            @Override
            public void sendAttributeAdded(HttpSession session, String name, Object value) {
            }

            @Override
            public void sendAttributeReplaced(HttpSession session, String name, Object oldValue) {
            }

            @Override
            public void sendAttributeRemoved(HttpSession session, String name, Object oldValue) {
            }
        });

    }

    /**
     * No initial state.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void noInitialState() throws Exception {
        assertNull(_context.getSession("12345"), "Session with incorrect ID");
    }

    /**
     * Creates the session.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void createSession() throws Exception {
        ServletUnitHttpSession session = _context.newSession();
        assertNotNull(session, "Session is null");
        assertTrue(session.isNew(), "Session is not marked as new");
        ServletUnitHttpSession session2 = _context.newSession();
        assertNotEquals(session.getId(), session2.getId(), "New session has the same ID");
        assertEquals(session, _context.getSession(session.getId()), "Different session returned");
    }

    /**
     * Session state.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void sessionState() throws Exception {
        ServletUnitHttpSession session = _context.newSession();
        long accessedAt = session.getLastAccessedTime();
        assertTrue(session.isNew(), "Session is not marked as new");
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
        assertEquals(accessedAt, _context.getSession(session.getId()).getLastAccessedTime(), "Initial access time");
        session.access();
        assertTrue(accessedAt != _context.getSession(session.getId()).getLastAccessedTime(),
                "Last access time not changed");
        assertFalse(_context.getSession(session.getId()).isNew(), "Session is still marked as new");

    }

    /**
     * Session attributes.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void sessionAttributes() throws Exception {
        assertDoesNotThrow(() -> {
            ServletUnitHttpSession session = _context.newSession();
            session.setAttribute("first", Integer.valueOf(1));
            session.setAttribute("second", "two");
            session.setAttribute("third", "III");

            assertMatchingSet("Attribute names", new String[] { "first", "second", "third" },
                    Collections.list(session.getAttributeNames()).toArray());

            session.removeAttribute("third");
            session.setAttribute("first", null);
            assertMatchingSet("Attribute names", new String[] { "second" },
                    Collections.list(session.getAttributeNames()).toArray());
        });
    }

    /**
     * Session context.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void sessionContext() throws Exception {
        ServletUnitHttpSession session = _context.newSession();
        assertNotNull(session.getServletContext(), "No context returned");
        assertSame(_servletContext, session.getServletContext(), "Owning context");
    }

}

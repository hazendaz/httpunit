/*
 * MIT License
 *
 * Copyright 2011-2023 Russell Gold
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

import static org.junit.Assert.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests the HttpSession implementation.
 */
public class SessionTest extends ServletUnitTest {

    private ServletUnitContext _context;
    private ServletContext _servletContext = new ServletUnitServletContext(null);


    @Before
    public void setUp() throws Exception {
        _context = new ServletUnitContext(null, _servletContext, new SessionListenerDispatcher() {
            public void sendSessionCreated(HttpSession session) {
            }

            public void sendSessionDestroyed(HttpSession session) {
            }

            public void sendAttributeAdded(HttpSession session, String name, Object value) {
            }

            public void sendAttributeReplaced(HttpSession session, String name, Object oldValue) {
            }

            public void sendAttributeRemoved(HttpSession session, String name, Object oldValue) {
            }
        });

    }


    @Test
    public void testNoInitialState() throws Exception {
        assertNull("Session with incorrect ID", _context.getSession("12345"));
    }


    @Test
    public void testCreateSession() throws Exception {
        ServletUnitHttpSession session = _context.newSession();
        assertNotNull("Session is null", session);
        assertTrue("Session is not marked as new", session.isNew());
        ServletUnitHttpSession session2 = _context.newSession();
        assertTrue("New session has the same ID", !session.getId().equals(session2.getId()));
        assertTrue("Different session returned", session.equals(_context.getSession(session.getId())));
    }


    @Test
    public void testSessionState() throws Exception {
        ServletUnitHttpSession session = _context.newSession();
        long accessedAt = session.getLastAccessedTime();
        assertTrue("Session is not marked as new", session.isNew());
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
        }
        ;
        assertEquals("Initial access time", accessedAt, _context.getSession(session.getId()).getLastAccessedTime());
        session.access();
        assertTrue("Last access time not changed", accessedAt != _context.getSession(session.getId()).getLastAccessedTime());
        assertTrue("Session is still marked as new", !_context.getSession(session.getId()).isNew());

    }


    @Test
    public void testSessionAttributes() throws Exception {
        ServletUnitHttpSession session = _context.newSession();
        session.setAttribute("first", Integer.valueOf(1));
        session.setAttribute("second", "two");
        session.setAttribute("third", "III");

        assertMatchingSet("Attribute names", new String[]{"first", "second", "third"}, toArray(session.getAttributeNames()));

        session.removeAttribute("third");
        session.setAttribute("first", null);
        assertMatchingSet("Attribute names", new String[]{"second"}, toArray(session.getAttributeNames()));
    }


    @Test
    public void testSessionContext() throws Exception {
        ServletUnitHttpSession session = _context.newSession();
        assertNotNull("No context returned", session.getServletContext());
        assertSame("Owning context", _servletContext, session.getServletContext());
    }


}



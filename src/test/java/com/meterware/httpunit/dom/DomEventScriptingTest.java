/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.dom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mozilla.javascript.Context;

/**
 * The Class DomEventScriptingTest.
 */
class DomEventScriptingTest extends AbstractHTMLElementTest {

    /** The context. */
    private Context _context;

    /** The Constant NO_ARGS. */
    private static final Object[] NO_ARGS = {};

    /**
     * Sets the up.
     *
     * @throws Exception
     *             the exception
     */
    @BeforeEach
    void setUp() throws Exception {
        _context = Context.enter();
        _context.initStandardObjects(null);
    }

    /**
     * Tear down.
     *
     * @throws Exception
     *             the exception
     */
    @AfterEach
    void tearDown() throws Exception {
        Context.exit();
    }

    /**
     * Verifies that the 'onload' event for a body element is initially undefined if no corresponding attribute is
     * defined.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void noOnloadEvent() throws Exception {
        HTMLBodyElementImpl body = (HTMLBodyElementImpl) createElement("body");
        assertNull(body.getOnloadEvent(), "Found a default definition for 'onLoad' event");
    }

    /**
     * Verifies that the 'onload' event for a body element is initially defined if a corresponding attribute is defined.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void inlineOnloadEvent() throws Exception {
        HTMLBodyElementImpl body = (HTMLBodyElementImpl) createElement("body",
                new Object[][] { { "onload", "title='here'" } });
        assertNotNull(body.getOnloadEvent(), "Found no definition for 'onLoad' event");
        body.getOnloadEvent().call(_context, body, body, NO_ARGS);
        assertEquals("here", body.getTitle(), "Updated title");
    }

}

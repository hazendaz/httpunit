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
package com.meterware.httpunit.dom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mozilla.javascript.Context;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 */
class DomEventScriptingTest extends AbstractHTMLElementTest {

    private Context _context;
    private static final Object[] NO_ARGS = {};

    @BeforeEach
    void setUp() throws Exception {
        _context = Context.enter();
        _context.initStandardObjects(null);
    }

    @AfterEach
    void tearDown() throws Exception {
        Context.exit();
    }

    /**
     * Verifies that the 'onload' event for a body element is initially undefined if no corresponding attribute is
     * defined.
     */
    @Test
    void noOnloadEvent() throws Exception {
        HTMLBodyElementImpl body = (HTMLBodyElementImpl) createElement("body");
        assertNull(body.getOnloadEvent(), "Found a default definition for 'onLoad' event");
    }

    /**
     * Verifies that the 'onload' event for a body element is initially defined if a corresponding attribute is defined.
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

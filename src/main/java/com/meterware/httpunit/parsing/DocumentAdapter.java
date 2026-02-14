/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.parsing;

import com.meterware.httpunit.scripting.ScriptingHandler;

import java.io.IOException;

import org.w3c.dom.html.HTMLDocument;

/**
 * The Interface DocumentAdapter.
 */
public interface DocumentAdapter {

    /**
     * Records the root (Document) node.
     *
     * @param document
     *            the new document
     */
    void setDocument(HTMLDocument document);

    /**
     * Returns the contents of an included script, given its src attribute.
     *
     * @param srcAttribute
     *            the relative URL for the included script
     *
     * @return the contents of the script.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    String getIncludedScript(String srcAttribute) throws IOException;

    /**
     * Returns the Scriptable object associated with the document.
     *
     * @return the scripting handler
     */
    ScriptingHandler getScriptingHandler();
}

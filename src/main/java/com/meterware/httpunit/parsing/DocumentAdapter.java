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

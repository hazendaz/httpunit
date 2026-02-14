/*
 * MIT License
 *
 * Copyright 2011-2026 Russell Gold
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

import com.meterware.httpunit.scripting.ScriptableDelegate;
import com.meterware.httpunit.scripting.ScriptingEventHandler;
import com.meterware.httpunit.scripting.ScriptingHandler;

import org.w3c.dom.Node;

/**
 * An interface which defines the common properties for an HTML element, which can correspond to any HTML tag.
 **/
// TODO activate the extends Element as
// in http://www.w3.org/TR/REC-DOM-Level-1/java-language-binding.html
// public interface HTMLElement extends Element,ScriptingEventHandler {
public interface HTMLElement extends ScriptingEventHandler {

    /**
     * Returns the ID associated with this element. IDs are unique throughout the HTML document.
     *
     * @return the id
     */
    String getID();

    /**
     * Returns the class associated with this element.
     *
     * @return the class name
     */
    String getClassName();

    /**
     * Returns the name associated with this element.
     *
     * @return the name
     */
    String getName();

    /**
     * Returns the title associated with this element.
     *
     * @return the title
     */
    String getTitle();

    /**
     * Returns the value of the attribute of this element with the specified name. Returns the empty string if no such
     * attribute exists.
     *
     * @param name
     *            the name
     *
     * @return the attribute
     */
    String getAttribute(String name);

    /**
     * Set the value of the attribute of this element with the specified name.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     */
    void setAttribute(String name, Object value);

    /**
     * Remove the attribute of this element with the specified name.
     *
     * @param name
     *            the name
     */
    void removeAttribute(String name);

    /**
     * Returns true if this element may have an attribute with the specified name.
     *
     * @param name
     *            the name
     *
     * @return true, if is supported attribute
     */
    boolean isSupportedAttribute(String name);

    /**
     * Returns the delegate which supports scripting this element.
     *
     * @return the scripting handler
     */
    ScriptingHandler getScriptingHandler();

    /**
     * Returns the contents of this element, converted to a string.
     *
     * @return the text
     */
    String getText();

    /**
     * Returns the tag name of this node.
     *
     * @return the tag name
     */
    String getTagName();

    /**
     * New scriptable.
     *
     * @return the scriptable delegate
     */
    ScriptableDelegate newScriptable();

    /**
     * Returns the scriptable delegate which can provide the scriptable delegate for this element.
     *
     * @return the parent delegate
     */
    ScriptableDelegate getParentDelegate();

    /**
     * Returns the DOM node underlying this element.
     *
     * @return the node
     */
    Node getNode();
}

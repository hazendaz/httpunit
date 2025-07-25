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

import com.meterware.httpunit.scripting.ScriptableDelegate;
import com.meterware.httpunit.scripting.ScriptingHandler;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Node;

/**
 * @since 1.5.2
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 */
abstract class HTMLElementBase implements HTMLElement {

    private Node _node;
    private ScriptingHandler _scriptable;
    private Set _supportedAttributes = new HashSet<>();

    @Override
    public String getID() {
        return getAttribute("id");
    }

    @Override
    public String getClassName() {
        return getAttribute("class");
    }

    @Override
    public String getTitle() {
        return getAttribute("title");
    }

    @Override
    public String getName() {
        return getAttribute("name");
    }

    /**
     * Returns a scriptable object which can act as a proxy for this control.
     */
    @Override
    public ScriptingHandler getScriptingHandler() {
        if (_scriptable == null) {
            _scriptable = HttpUnitOptions.getScriptingEngine().createHandler(this);
        }
        return _scriptable;
    }

    /**
     * handle the event that has the given script attached by compiling the eventScript as a function and executing it
     *
     * @param eventScript
     *            - the script to use
     *
     * @deprecated since 1.7 - use doEventScript instead
     */
    @Deprecated
    @Override
    public boolean doEvent(String eventScript) {
        return doEventScript(eventScript);
    }

    /**
     * optional do the event if it's defined
     */
    @Override
    public boolean doEventScript(String eventScript) {
        return this.getScriptingHandler().doEventScript(eventScript);
    }

    @Override
    public boolean handleEvent(String eventName) {
        return this.getScriptingHandler().handleEvent(eventName);
    }

    /**
     * Returns the text value of this block.
     */
    @Override
    public String getText() {
        if (_node == null) {
            return "";
        }
        if (_node.getNodeType() == Node.TEXT_NODE) {
            return _node.getNodeValue().trim();
        }
        if (!_node.hasChildNodes()) {
            return "";
        }
        return NodeUtils.asText(_node.getChildNodes()).trim();
    }

    @Override
    public String getTagName() {
        return _node.getNodeName();
    }

    /**
     * construct me from a node
     *
     * @param node
     *            - the node to get me from
     */
    protected HTMLElementBase(Node node) {
        _node = node;
        // default attributes every html element can have
        supportAttribute("id");
        supportAttribute("class");
        supportAttribute("title");
        supportAttribute("name");
    }

    /**
     * get the Attribute with the given name - by delegating to NodeUtils
     *
     * @param name
     *            - the name of the attribute to get
     *
     * @return the attribute
     */
    @Override
    public String getAttribute(final String name) {
        return NodeUtils.getNodeAttribute(getNode(), name);
    }

    /**
     * set the Attribute with the given name - by delegating to NodeUtils
     *
     * @param name
     *            - the name of the attribute to set
     * @param value
     *            - the value to set
     */
    @Override
    public void setAttribute(final String name, final Object value) {
        NodeUtils.setNodeAttribute(getNode(), name, value == null ? null : value.toString());
    }

    /**
     * remove the Attribute with the given name - by delegating to NodeUtils
     *
     * @param name
     *            - the name of the attribute to remove
     */
    @Override
    public void removeAttribute(final String name) {
        NodeUtils.removeNodeAttribute(getNode(), name);
    }

    @Override
    public boolean isSupportedAttribute(String name) {
        return _supportedAttributes.contains(name);
    }

    protected String getAttribute(final String name, String defaultValue) {
        return NodeUtils.getNodeAttribute(getNode(), name, defaultValue);
    }

    @Override
    public Node getNode() {
        return _node;
    }

    protected void supportAttribute(String name) {
        _supportedAttributes.add(name);
    }

    /**
     * Creates and returns a scriptable object for this control. Subclasses should override this if they use a different
     * implementation of Scriptable.
     */
    @Override
    public ScriptableDelegate newScriptable() {
        return new HTMLElementScriptable(this);
    }

}

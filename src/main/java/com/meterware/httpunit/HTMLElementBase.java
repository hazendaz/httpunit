/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

import com.meterware.httpunit.scripting.ScriptableDelegate;
import com.meterware.httpunit.scripting.ScriptingHandler;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Node;

/**
 * The Class HTMLElementBase.
 */
abstract class HTMLElementBase implements HTMLElement {

    /** The node. */
    private Node _node;

    /** The scriptable. */
    private ScriptingHandler _scriptable;

    /** The supported attributes. */
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
     * construct me from a node.
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

    /**
     * Gets the attribute.
     *
     * @param name
     *            the name
     * @param defaultValue
     *            the default value
     *
     * @return the attribute
     */
    protected String getAttribute(final String name, String defaultValue) {
        return NodeUtils.getNodeAttribute(getNode(), name, defaultValue);
    }

    @Override
    public Node getNode() {
        return _node;
    }

    /**
     * Support attribute.
     *
     * @param name
     *            the name
     */
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

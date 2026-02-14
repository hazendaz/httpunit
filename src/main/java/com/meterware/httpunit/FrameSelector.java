/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

/**
 * An immutable class which describes the position of a frame in the window hierarchy.
 **/
public class FrameSelector {

    /** The top frame. */
    public static FrameSelector TOP_FRAME = new FrameSelector(WebRequest.TOP_FRAME);

    /** The new frame. */
    static FrameSelector NEW_FRAME = new FrameSelector(WebRequest.TOP_FRAME);

    /** The name. */
    private String _name;

    /** The window. */
    private WebWindow _window;

    /** The parent. */
    private FrameSelector _parent;

    /**
     * Instantiates a new frame selector.
     */
    FrameSelector() {
        _name = super.toString();
    }

    /**
     * Instantiates a new frame selector.
     *
     * @param name
     *            the name
     */
    FrameSelector(String name) {
        _name = name;
    }

    /**
     * Instantiates a new frame selector.
     *
     * @param name
     *            the name
     * @param parent
     *            the parent
     */
    FrameSelector(String name, FrameSelector parent) {
        _name = name;
        _parent = parent;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    String getName() {
        return _name;
    }

    /**
     * Gets the parent.
     *
     * @return the parent
     */
    FrameSelector getParent() {
        return _parent;
    }

    @Override
    public String toString() {
        return "Frame Selector: [ " + getFullName() + " ]";
    }

    /**
     * Gets the full name.
     *
     * @return the full name
     */
    private String getFullName() {
        return _name + (_parent == null ? "" : " in " + _parent.getFullName());
    }

    /**
     * Gets the window.
     *
     * @return the window
     */
    WebWindow getWindow() {
        return _window != null ? _window : _parent == null ? null : _parent.getWindow();
    }

    /**
     * New top frame.
     *
     * @param window
     *            the window
     *
     * @return the frame selector
     */
    static FrameSelector newTopFrame(WebWindow window) {
        return new FrameSelector(WebRequest.TOP_FRAME, window);
    }

    /**
     * Instantiates a new frame selector.
     *
     * @param name
     *            the name
     * @param window
     *            the window
     */
    private FrameSelector(String name, WebWindow window) {
        _name = name;
        _window = window;
    }

}

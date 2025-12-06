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

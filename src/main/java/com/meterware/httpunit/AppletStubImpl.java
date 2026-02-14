/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

import java.applet.AppletContext;
import java.applet.AppletStub;
import java.net.URL;

/**
 * The Class AppletStubImpl.
 */
class AppletStubImpl implements AppletStub {

    /** The web applet. */
    private WebApplet _webApplet;

    /**
     * Instantiates a new applet stub impl.
     *
     * @param webApplet
     *            the web applet
     */
    AppletStubImpl(WebApplet webApplet) {
        _webApplet = webApplet;
    }

    /**
     * Determines if the applet is active. An applet is active just before its <code>start</code> method is called. It
     * becomes inactive just before its <code>stop</code> method is called.
     *
     * @return <code>true</code> if the applet is active; <code>false</code> otherwise.
     */
    @Override
    public boolean isActive() {
        return false;
    }

    /**
     * Returns an absolute URL naming the directory of the document in which the applet is embedded. For example,
     * suppose an applet is contained within the document: <blockquote>
     *
     * <pre>
     *    http://java.sun.com/products/jdk/1.2/index.html
     * </pre>
     *
     * </blockquote> The document base is: <blockquote>
     *
     * <pre>
     *    http://java.sun.com/products/jdk/1.2/
     * </pre>
     *
     * </blockquote>
     *
     * @return the {@link URL} of the document that contains this applet.
     *
     * @see AppletStub#getCodeBase()
     */
    @Override
    public URL getDocumentBase() {
        return null;
    }

    /**
     * Gets the base URL.
     *
     * @return the <code>URL</code> of the applet.
     */
    @Override
    public URL getCodeBase() {
        return null;
    }

    /**
     * Returns the value of the named parameter in the HTML tag. For example, if an applet is specified as <blockquote>
     *
     * <pre>
     * &lt;applet code="Clock" width=50 height=50&gt;
     * &lt;param name=Color value="blue"&gt;
     * &lt;/applet&gt;
     * </pre>
     *
     * </blockquote>
     * <p>
     * then a call to <code>getParameter("Color")</code> returns the value <code>"blue"</code>.
     *
     * @param name
     *            a parameter name.
     *
     * @return the value of the named parameter, or <tt>null</tt> if not set.
     */
    @Override
    public String getParameter(String name) {
        return _webApplet.getParameter(name);
    }

    /**
     * Gets a handler to the applet's context.
     *
     * @return the applet's context.
     */
    @Override
    public AppletContext getAppletContext() {
        return new AppletContextImpl(_webApplet);
    }

    /**
     * Called when the applet wants to be resized.
     *
     * @param width
     *            the new requested width for the applet.
     * @param height
     *            the new requested height for the applet.
     */
    @Override
    public void appletResize(int width, int height) {
    }
}

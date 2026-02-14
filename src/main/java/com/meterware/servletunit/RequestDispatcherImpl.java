/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.servletunit;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.net.URL;

/**
 * The Class RequestDispatcherImpl.
 */
class RequestDispatcherImpl extends RequestContext implements RequestDispatcher {

    /** The servlet meta data. */
    private ServletMetaData _servletMetaData;

    /**
     * Instantiates a new request dispatcher impl.
     *
     * @param application
     *            the application
     * @param url
     *            the url
     *
     * @throws ServletException
     *             the servlet exception
     */
    RequestDispatcherImpl(WebApplication application, URL url) throws ServletException {
        super(url);
        _servletMetaData = application.getServletRequest(url);
    }

    /**
     * Gets the servlet meta data.
     *
     * @return the servlet meta data
     */
    public ServletMetaData getServletMetaData() {
        return _servletMetaData;
    }

    @Override
    public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        response.reset();
        _servletMetaData.getServlet().service(
                DispatchedRequestWrapper.createForwardRequestWrapper((HttpServletRequest) request, this), response);
    }

    @Override
    public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        _servletMetaData.getServlet().service(
                DispatchedRequestWrapper.createIncludeRequestWrapper((HttpServletRequest) request, this), response);
    }
}

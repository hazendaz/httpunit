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

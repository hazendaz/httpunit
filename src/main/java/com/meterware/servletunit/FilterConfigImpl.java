/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2026 hazendaz
 */
package com.meterware.servletunit;

import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;

import java.util.Enumeration;

/**
 * The Class FilterConfigImpl.
 */
class FilterConfigImpl implements FilterConfig {

    /** The name. */
    private String _name;

    /** The servlet context. */
    private ServletContext _servletContext;

    /** The init params. */
    private java.util.Map _initParams;

    /**
     * Instantiates a new filter config impl.
     *
     * @param name
     *            the name
     * @param servletContext
     *            the servlet context
     * @param initParams
     *            the init params
     */
    FilterConfigImpl(String name, ServletContext servletContext, java.util.Map initParams) {
        _name = name;
        _servletContext = servletContext;
        _initParams = initParams;
    }

    @Override
    public String getFilterName() {
        return _name;
    }

    @Override
    public ServletContext getServletContext() {
        return _servletContext;
    }

    @Override
    public String getInitParameter(String s) {
        return (String) _initParams.get(s);
    }

    @Override
    public Enumeration getInitParameterNames() {
        return java.util.Collections.enumeration(_initParams.keySet());
    }

}

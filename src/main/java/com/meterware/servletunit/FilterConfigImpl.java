/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.servletunit;

import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * The Class FilterConfigImpl.
 */
class FilterConfigImpl implements FilterConfig {

    /** The name. */
    private String _name;

    /** The servlet context. */
    private ServletContext _servletContext;

    /** The init params. */
    private Hashtable _initParams;

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
    FilterConfigImpl(String name, ServletContext servletContext, Hashtable initParams) {
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
        return _initParams.keys();
    }

}

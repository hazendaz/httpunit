/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.servletunit;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * This class acts as a test environment for servlets.
 **/
class ServletUnitServletConfig implements ServletConfig {

    /**
     * Instantiates a new servlet unit servlet config.
     *
     * @param name
     *            the name
     * @param application
     *            the application
     * @param initParams
     *            the init params
     */
    ServletUnitServletConfig(String name, WebApplication application, Hashtable initParams) {
        _name = name;
        _initParameters = initParams;
        _context = application.getServletContext();
    }

    // -------------------------------------------- ServletConfig methods
    // ---------------------------------------------------

    /**
     * Returns the value of the specified init parameter, or null if no such init parameter is defined.
     **/
    @Override
    public String getInitParameter(String name) {
        return (String) _initParameters.get(name);
    }

    /**
     * Returns an enumeration over the names of the init parameters.
     **/
    @Override
    public Enumeration getInitParameterNames() {
        return _initParameters.keys();
    }

    /**
     * Returns the current servlet context.
     **/
    @Override
    public ServletContext getServletContext() {
        return _context;
    }

    /**
     * Returns the registered name of the servlet, or its class name if it is not registered.
     **/
    @Override
    public java.lang.String getServletName() {
        return _name;
    }

    // ----------------------------------------------- private members
    // ------------------------------------------------------

    /** The name. */
    private String _name;

    /** The init parameters. */
    private final Hashtable _initParameters;

    /** The context. */
    private final ServletContext _context;

}

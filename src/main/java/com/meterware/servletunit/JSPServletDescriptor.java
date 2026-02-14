/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.servletunit;

import java.util.Hashtable;

/**
 * Describes a servlet used to handle JSPs.
 **/
public interface JSPServletDescriptor {

    /**
     * Returns the class name of the JSP servlet.
     *
     * @return the class name
     */
    String getClassName();

    /**
     * Returns initialization parameters for the JSP servlet, given the specified classpath and working directory.
     *
     * @param classPath
     *            the class path
     * @param workingDirectory
     *            the working directory
     *
     * @return the initialization parameters
     */
    Hashtable getInitializationParameters(String classPath, String workingDirectory);

}

/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
/**
 * Classes for unit testing servlets, providing internal access to running servlets using a simulated servlet container.
 * <p>
 * Each test session should begin by creating a {@link com.meterware.servletunit.ServletRunner ServletRunner} which will
 * act as a servlet application context. The definition of application context may be supplied in one of two ways. The
 * {@link com.meterware.servletunit.ServletRunner#registerServlet(String, String) registerServlet} method allows the
 * association of a servlet with a url path. Alternately, an entire servlet application may be defined by passing the
 * name of the desired web.xml file. The {@link com.meterware.servletunit.ServletRunner#newClient newClient} method will
 * return a {@link com.meterware.servletunit.ServletUnitClient ServletUnitClient} object which can be used to invoke the
 * defined servlets, just as any subclass of {@link com.meterware.httpunit.WebClient WebClient}. In addition, this
 * client object defines methods which allow access to the fully initialized servlet itself, as well as the request,
 * response, and servlet session.
 * <p>
 * A <a href="../../../../tutorial/index.html" target="_top">tutorial</a> is available. Please direct any questions to
 * <a href="mailto:russgold@httpunit.org">Russell Gold</a>.
 */
package com.meterware.servletunit;

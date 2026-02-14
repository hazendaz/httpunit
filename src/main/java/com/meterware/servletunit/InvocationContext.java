/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.servletunit;

import com.meterware.httpunit.FrameSelector;
import com.meterware.httpunit.WebResponse;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * An interface which represents the invocation of a servlet.
 **/
public interface InvocationContext {

    /**
     * Returns the request to be processed by the servlet or filter.
     *
     * @return the request
     */
    HttpServletRequest getRequest();

    /**
     * Returns the response which the servlet or filter should modify during its operation.
     *
     * @return the response
     */
    HttpServletResponse getResponse();

    /**
     * Invokes the current servlet or filter.
     *
     * @throws ServletException
     *             the servlet exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    void service() throws ServletException, IOException;

    /**
     * Returns the selected servlet, initialized to provide access to sessions and servlet context information. Only
     * valid to call if {@link #isFilterActive} returns false.
     *
     * @return the servlet
     *
     * @throws ServletException
     *             the servlet exception
     */
    Servlet getServlet() throws ServletException;

    /**
     * Returns the final response from the servlet. Note that this method should only be invoked after all processing
     * has been done to the servlet response.
     *
     * @return the servlet response
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    WebResponse getServletResponse() throws IOException;

    /**
     * Returns the target frame for the original request.
     *
     * @return the frame
     */
    FrameSelector getFrame();

    /**
     * Adds a request dispatcher to this context to simulate an include request.
     *
     * @param rd
     *            the rd
     * @param request
     *            the request
     * @param response
     *            the response
     *
     * @throws ServletException
     *             the servlet exception
     */
    void pushIncludeRequest(RequestDispatcher rd, HttpServletRequest request, HttpServletResponse response)
            throws ServletException;

    /**
     * Adds a request dispatcher to this context to simulate a forward request.
     *
     * @param rd
     *            the rd
     * @param request
     *            the request
     * @param response
     *            the response
     *
     * @throws ServletException
     *             the servlet exception
     */
    void pushForwardRequest(RequestDispatcher rd, HttpServletRequest request, HttpServletResponse response)
            throws ServletException;

    /**
     * Removes the top request dispatcher or filter from this context.
     */
    void popRequest();

    /**
     * Returns true if the current context is a filter, rather than a servlet.
     *
     * @return true, if is filter active
     */
    boolean isFilterActive();

    /**
     * Returns the current active filter object. Only valid to call if {@link #isFilterActive} returns true.
     *
     * @return the filter
     *
     * @throws ServletException
     *             the servlet exception
     */
    Filter getFilter() throws ServletException;

    /**
     * Returns the current filter chain. Only valid to call if {@link #isFilterActive} returns true.
     *
     * @return the filter chain
     */
    FilterChain getFilterChain();

    /**
     * Pushes the current filter onto the execution stack and switches to the next filter or the selected servlet. This
     * can be used to simulate the effect of the {@link jakarta.servlet.FilterChain#doFilter doFilter} call. <br>
     * <b>Note:</b> this method specifies {@link ServletRequest} and {@link ServletResponse} because those are the types
     * passed to {@link Filter#doFilter}; however, HttpUnit requires the objects to implement {@link HttpServletRequest}
     * and {@link HttpServletResponse} because they will eventually be passed to an
     * {@link jakarta.servlet.http.HttpServlet}.
     *
     * @param request
     *            the request to pass to the next filter. May be a wrapper.
     * @param response
     *            the response object to pass to the next filter. May be a wrapper.
     */
    void pushFilter(ServletRequest request, ServletResponse response);

}

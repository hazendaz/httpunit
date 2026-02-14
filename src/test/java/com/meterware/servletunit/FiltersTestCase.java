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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.meterware.httpunit.HttpUnitUtils;
import com.meterware.httpunit.WebResponse;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

/**
 * Tests the servlet filtering capability added in Servlet 2.3.
 */
class FiltersTestCase {

    /** The Constant FILTER1. */
    static final FilterMetaData FILTER1 = new FilterMetaDataImpl(1);

    /** The Constant FILTER2. */
    static final FilterMetaData FILTER2 = new FilterMetaDataImpl(2);

    /** The Constant FILTER3. */
    static final FilterMetaData FILTER3 = new FilterMetaDataImpl(3);

    /** The Constant FILTER4. */
    static final FilterMetaData FILTER4 = new FilterMetaDataImpl(4);

    /** The Constant FILTER5. */
    static final FilterMetaData FILTER5 = new FilterMetaDataImpl(5);

    /** The Constant FILTER6. */
    static final FilterMetaData FILTER6 = new FilterMetaDataImpl(6);

    /** The servlet called. */
    private static boolean _servletCalled;

    /**
     * Verifies that the no-filter case is handled by servlet metadata.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void noFilterAssociation() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet("Simple", "/SimpleServlet", SimpleGetServlet.class);
        WebApplication application = new WebApplication(
                HttpUnitUtils.newParser().parse(new InputSource(wxs.asInputStream())), null);

        ServletMetaData metaData = application.getServletRequest(new URL("http://localhost/SimpleServlet"));
        FilterMetaData[] filters = metaData.getFilters();
        assertEquals(0, filters.length, "number of associated filters");
    }

    /**
     * Verifies that a simple filter is associated with a servlet by its name.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void nameFilterAssociation() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet("Simple", "/SimpleServlet", SimpleGetServlet.class);
        wxs.addFilterForServlet("Trivial", TrivialFilter.class, "Simple");
        WebApplication application = new WebApplication(
                HttpUnitUtils.newParser().parse(new InputSource(wxs.asInputStream())), null);

        ServletMetaData metaData = application.getServletRequest(new URL("http://localhost/SimpleServlet"));
        FilterMetaData[] filters = metaData.getFilters();
        assertEquals(1, filters.length, "number of associated filters");
        assertEquals(TrivialFilter.class, filters[0].getFilter().getClass(), "filter class");
    }

    /**
     * Verifies that a simple filter will be called before a servlet with the same URL.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void filterByNameInvocation() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet("Simple", "/SimpleServlet", SimpleGetServlet.class);
        wxs.addFilterForServlet("Trivial", TrivialFilter.class, "Simple");
        ServletRunner sr = new ServletRunner(wxs.asInputStream());
        ServletUnitClient wc = sr.newClient();
        InvocationContext ic = wc.newInvocation("http://localhost/SimpleServlet");
        assertTrue(ic.isFilterActive(), "Did not find a filter");

        Filter filter = ic.getFilter();
        assertNotNull(filter, "Filter is null");
        assertEquals(TrivialFilter.class, filter.getClass(), "Filter class");
        ic.pushFilter(ic.getRequest(), ic.getResponse());
        assertFalse(ic.isFilterActive(), "Did not switch to servlet");
        assertEquals(SimpleGetServlet.class, ic.getServlet().getClass(), "Servlet class");
        ic.popRequest();
        assertTrue(ic.isFilterActive(), "Did not pop back to filter");
        assertSame(filter, ic.getFilter(), "Restored filter");
    }

    /**
     * Verifies that a simple filter will be called before a servlet with the same URL.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void namedFilterOrder() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet("Simple", "/SimpleServlet", SimpleGetServlet.class);
        wxs.addFilterForServlet("Trivial", TrivialFilter.class, "Simple");
        wxs.addFilterForServlet("Attribute", AttributeFilter.class, "Simple");
        ServletRunner sr = new ServletRunner(wxs.asInputStream());
        ServletUnitClient wc = sr.newClient();
        InvocationContext ic = wc.newInvocation("http://localhost/SimpleServlet");

        Filter filter1 = ic.getFilter();
        assertEquals(TrivialFilter.class, filter1.getClass(), "Filter 1 class");
        ic.pushFilter(ic.getRequest(), ic.getResponse());

        assertTrue(ic.isFilterActive(), "Did not find a filter");
        Filter filter2 = ic.getFilter();
        assertEquals(AttributeFilter.class, filter2.getClass(), "Filter 2 class");
        ic.pushFilter(ic.getRequest(), ic.getResponse());

        assertFalse(ic.isFilterActive(), "Did not switch to servlet");
        assertEquals(SimpleGetServlet.class, ic.getServlet().getClass(), "Servlet class");
        ic.popRequest();
        assertSame(filter2, ic.getFilter(), "Restored filter 2");
        ic.popRequest();
        assertSame(filter1, ic.getFilter(), "Restored filter 1");
    }

    /**
     * Verifies that request / response wrappering for filters is supported.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void filterRequestWrapping() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet("Simple", "/SimpleServlet", SimpleGetServlet.class);
        wxs.addFilterForServlet("Trivial", TrivialFilter.class, "Simple");
        ServletRunner sr = new ServletRunner(wxs.asInputStream());
        ServletUnitClient wc = sr.newClient();

        InvocationContext ic = wc.newInvocation("http://localhost/SimpleServlet");
        HttpServletRequest originalRequest = ic.getRequest();
        HttpServletResponse originalResponse = ic.getResponse();

        HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(originalResponse);
        HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(originalRequest);

        ic.pushFilter(requestWrapper, responseWrapper);
        assertFalse(ic.isFilterActive(), "Did not switch to servlet");
        assertSame(requestWrapper, ic.getRequest(), "Servlet request");
        assertSame(responseWrapper, ic.getResponse(), "Servlet response");

        ic.popRequest();
        assertSame(originalRequest, ic.getRequest(), "Filter request");
        assertSame(originalResponse, ic.getResponse(), "Filter response");
    }

    /**
     * Verifies that the filter chain invokes the servlet.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void filterChain() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet("Simple", "/SimpleServlet", SimpleGetServlet.class);
        wxs.addFilterForServlet("Trivial", TrivialFilter.class, "Simple");
        ServletRunner sr = new ServletRunner(wxs.asInputStream());
        ServletUnitClient wc = sr.newClient();
        InvocationContext ic = wc.newInvocation("http://localhost/SimpleServlet");
        _servletCalled = false;
        HttpServletResponse originalResponse = ic.getResponse();
        HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(originalResponse);
        ic.getFilterChain().doFilter(ic.getRequest(), responseWrapper);
        assertTrue(_servletCalled, "Servlet was not called");
        assertTrue(ic.isFilterActive(), "Filter marked as active");
        assertSame(originalResponse, ic.getResponse(), "Response object after doFilter");
    }

    /**
     * Verifies that filters are automatically called.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void filterInvocation() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet("Simple", "/SimpleServlet", SimpleGetServlet.class);
        wxs.addFilterForServlet("Attribute", AttributeFilter.class, "Simple");
        ServletRunner sr = new ServletRunner(wxs.asInputStream());
        ServletUnitClient wc = sr.newClient();
        WebResponse wr = wc.getResponse("http://localhost/SimpleServlet");
        assertEquals("by-filter", wr.getText().trim(), "Filtered response ");
    }

    /**
     * Verifies that a simple filter is associated with a url pattern.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void urlFilterAssociation() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet("Simple", "/helpMe/SimpleServlet", SimpleGetServlet.class);
        wxs.addFilterForUrl("Trivial", TrivialFilter.class, "/helpMe/*");
        wxs.addFilterForUrl("Other", AttributeFilter.class, "/Simple");
        WebApplication application = new WebApplication(
                HttpUnitUtils.newParser().parse(new InputSource(wxs.asInputStream())), null);

        ServletMetaData metaData = application.getServletRequest(new URL("http://localhost/helpMe/SimpleServlet"));
        FilterMetaData[] filters = metaData.getFilters();
        assertEquals(1, filters.length, "number of associated filters");
        assertEquals(TrivialFilter.class, filters[0].getFilter().getClass(), "filter class");
    }

    /**
     * Filter mapping.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void filterMapping() throws Exception {
        FilterUrlMap map = new FilterUrlMap();
        map.put("/foo/bar/*", FILTER1);
        map.put("/baz/*", FILTER2);
        map.put("/catalog", FILTER3);
        map.put("*.bop", FILTER4);
        map.put("/foo/bar/*", FILTER5);
        map.put("/foo/*", FILTER6);

        checkMapping(map, "/catalog", new FilterMetaData[] { FILTER3 });
        checkMapping(map, "/catalog/racecar.bop", new FilterMetaData[] { FILTER4 });
        checkMapping(map, "/index.bop", new FilterMetaData[] { FILTER4 });
        checkMapping(map, "/foo/bar/index.html", new FilterMetaData[] { FILTER1, FILTER5, FILTER6 });
        checkMapping(map, "/foo/index.bop", new FilterMetaData[] { FILTER4, FILTER6 });
        checkMapping(map, "/baz", new FilterMetaData[] { FILTER2 });
        checkMapping(map, "/bazel", new FilterMetaData[0]);
        checkMapping(map, "/baz/index.html", new FilterMetaData[] { FILTER2 });
        checkMapping(map, "/something/else", new FilterMetaData[0]);
    }

    /**
     * Check mapping.
     *
     * @param map
     *            the map
     * @param urlString
     *            the url string
     * @param expectedFilters
     *            the expected filters
     */
    private void checkMapping(FilterUrlMap map, String urlString, FilterMetaData[] expectedFilters) {
        assertEquals(Arrays.asList(expectedFilters), Arrays.asList(map.getMatchingFilters(urlString)),
                "Filters selected for '" + urlString + "'");
    }

    /**
     * Filter initialization.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void filterInitialization() throws Exception {
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet("Simple", "/SimpleServlet", SimpleGetServlet.class);
        Properties params = new Properties();
        params.setProperty("color", "red");
        params.setProperty("age", "12");
        wxs.addFilterForServlet("Config", AttributeFilter.class, "Simple", params);
        ServletRunner sr = new ServletRunner(wxs.asInputStream());
        ServletUnitClient wc = sr.newClient();
        InvocationContext ic = wc.newInvocation("http://localhost/SimpleServlet");

        AttributeFilter filter = (AttributeFilter) ic.getFilter();
        FilterConfig filterConfig = filter._filterConfig;
        assertNotNull(filterConfig, "Filter was not initialized");
        assertEquals("Config", filterConfig.getFilterName(), "Filter name");
        assertNotNull(filterConfig.getServletContext(), "No servlet context provided");

        assertNull(filterConfig.getInitParameter("gender"), "init parameter 'gender' should be null");
        assertEquals("red", filterConfig.getInitParameter("color"), "init parameter 'red'");

        ArrayList names = new ArrayList<>();
        for (Enumeration e = filterConfig.getInitParameterNames(); e.hasMoreElements();) {
            String name = (String) e.nextElement();
            names.add(name);
        }
        assertEquals(2, names.size(), "Number of names in enumeration");
        assertTrue(names.contains("color"), "'color' not found in enumeration");
        assertTrue(names.contains("age"), "'age' not found in enumeration");
    }

    // TODO combination of named and url filters (url filters go first)
    // TODO filter shutdown
    // TODO filters with request dispatchers
    // TODO filters throwing UnavailableException

    /**
     * The Class AttributeFilter.
     */
    static class AttributeFilter implements Filter {

        /** The filter config. */
        private FilterConfig _filterConfig;

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
            _filterConfig = filterConfig;
        }

        @Override
        public void destroy() {
        }

        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
                throws IOException, ServletException {
            servletRequest.setAttribute("called", "by-filter");
            filterChain.doFilter(servletRequest, servletResponse);
        }

    }

    /**
     * The Class TrivialFilter.
     */
    static class TrivialFilter implements Filter {

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }

        @Override
        public void destroy() {
        }

        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
                throws IOException, ServletException {
            servletRequest.setAttribute("called", "trivially");
            filterChain.doFilter(servletRequest, servletResponse);
        }

    }

    /**
     * The Class SimpleGetServlet.
     */
    static class SimpleGetServlet extends HttpServlet {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("text/html");
            PrintWriter pw = resp.getWriter();
            pw.print(req.getAttribute("called"));
            pw.close();
            _servletCalled = true;
        }
    }

    /**
     * The Class FilterMetaDataImpl.
     */
    static class FilterMetaDataImpl implements FilterMetaData {

        /** The index. */
        private int _index;

        /**
         * Instantiates a new filter meta data impl.
         *
         * @param index
         *            the index
         */
        public FilterMetaDataImpl(int index) {
            _index = index;
        }

        @Override
        public Filter getFilter() throws ServletException {
            return null;
        }

        @Override
        public String toString() {
            return "Filter" + _index;
        }
    }
}

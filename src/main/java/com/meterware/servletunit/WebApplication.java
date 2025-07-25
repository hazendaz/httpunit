/*
 * MIT License
 *
 * Copyright 2011-2025 Russell Gold
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

import com.meterware.httpunit.HttpInternalErrorException;
import com.meterware.httpunit.HttpNotFoundException;
import com.meterware.httpunit.HttpUnitUtils;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class represents the information recorded about a single web application. It is usually extracted from web.xml.
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 * @author <a href="balld@webslingerZ.com">Donald Ball</a>
 * @author <a href="jaydunning@users.sourceforge.net">Jay Dunning</a>
 **/
class WebApplication implements SessionListenerDispatcher {

    private static final SecurityConstraint NULL_SECURITY_CONSTRAINT = new NullSecurityConstraint();

    private final ServletConfiguration SECURITY_CHECK_CONFIGURATION = new ServletConfiguration(
            SecurityCheckServlet.class.getName());

    private final WebResourceMapping SECURITY_CHECK_MAPPING = new WebResourceMapping(SECURITY_CHECK_CONFIGURATION);

    /** A mapping of resource names to servlet configurations. **/
    private WebResourceMap _servletMapping = new WebResourceMap();

    /** A mapping of filter names to FilterConfigurations */
    private Hashtable _filters = new Hashtable<>();

    /** A mapping of servlet names to ServletConfigurations */
    private Hashtable _servlets = new Hashtable<>();

    /** A mapping of resource names to filter configurations. **/
    private FilterUrlMap _filterUrlMapping = new FilterUrlMap();

    /** A mapping of servlet names to filter configurations. **/
    private Hashtable _filterMapping = new Hashtable<>();

    private List<SecurityConstraint> _securityConstraints = new ArrayList<>();

    private List<ServletContextListener> _contextListeners = new ArrayList<>();

    private List<ServletContextAttributeListener> _contextAttributeListeners = new ArrayList<>();

    private List<HttpSessionListener> _sessionListeners = new ArrayList<>();

    private List<HttpSessionAttributeListener> _sessionAttributeListeners = new ArrayList<>();

    private boolean _useBasicAuthentication;

    private boolean _useFormAuthentication;

    private String _authenticationRealm = "";

    private URL _loginURL;

    private URL _errorURL;

    private Hashtable _contextParameters = new Hashtable<>();

    private File _contextDir = null;

    private String _contextPath = null;

    private ServletUnitServletContext _servletContext;

    private String _displayName;

    /**
     * Constructs a default application spec with no information.
     */
    WebApplication() {
        _contextPath = "";
    }

    /**
     * Constructs an application spec from an XML document.
     */
    WebApplication(Document document) throws MalformedURLException, SAXException {
        this(document, null, "");
    }

    /**
     * Constructs an application spec from an XML document.
     */
    WebApplication(Document document, String contextPath) throws MalformedURLException, SAXException {
        this(document, null, contextPath);
    }

    /**
     * Constructs an application spec from an XML document.
     */
    WebApplication(Document document, File file, String contextPath) throws MalformedURLException, SAXException {
        if (contextPath != null && !contextPath.isEmpty() && !contextPath.startsWith("/")) {
            throw new IllegalArgumentException("Context path " + contextPath + " must start with '/'");
        }
        _contextDir = file;
        _contextPath = contextPath == null ? "" : contextPath;
        NodeList nl = document.getElementsByTagName("display-name");
        if (nl.getLength() > 0) {
            _displayName = XMLUtils.getTextValue(nl.item(0)).trim();
        }

        registerServlets(document);
        registerFilters(document);
        extractSecurityConstraints(document);
        extractContextParameters(document);
        extractLoginConfiguration(document);
        extractListeners(document);
        notifyContextInitialized();
        _servletMapping.autoLoadServlets();
    }

    private void extractListeners(Document document) throws SAXException {
        NodeList nl = document.getElementsByTagName("listener");
        for (int i = 0; i < nl.getLength(); i++) {
            String listenerName = XMLUtils.getChildNodeValue((Element) nl.item(i), "listener-class").trim();
            try {
                Object listener = Class.forName(listenerName).getDeclaredConstructor().newInstance();

                if (listener instanceof ServletContextListener) {
                    _contextListeners.add((ServletContextListener) listener);
                }
                if (listener instanceof ServletContextAttributeListener) {
                    _contextAttributeListeners.add((ServletContextAttributeListener) listener);
                }
                if (listener instanceof HttpSessionListener) {
                    _sessionListeners.add((HttpSessionListener) listener);
                }
                if (listener instanceof HttpSessionAttributeListener) {
                    _sessionAttributeListeners.add((HttpSessionAttributeListener) listener);
                }
            } catch (Throwable e) {
                throw new RuntimeException("Unable to load context listener " + listenerName + ": " + e.toString());
            }
        }
    }

    private void notifyContextInitialized() {
        ServletContextEvent event = new ServletContextEvent(getServletContext());

        for (Iterator<ServletContextListener> i = _contextListeners.iterator(); i.hasNext();) {
            ServletContextListener listener = i.next();
            listener.contextInitialized(event);
        }
    }

    void shutDown() {
        destroyServlets();
        notifyContextDestroyed();
    }

    private void notifyContextDestroyed() {
        ServletContextEvent event = new ServletContextEvent(getServletContext());

        for (ListIterator<ServletContextListener> i = _contextListeners.listIterator(_contextListeners.size()); i
                .hasPrevious();) {
            ServletContextListener listener = i.previous();
            listener.contextDestroyed(event);
        }
    }

    void sendAttributeAdded(String name, Object value) {
        ServletContextAttributeEvent event = new ServletContextAttributeEvent(getServletContext(), name, value);

        for (Iterator<ServletContextAttributeListener> i = _contextAttributeListeners.iterator(); i.hasNext();) {
            ServletContextAttributeListener listener = i.next();
            listener.attributeAdded(event);
        }
    }

    void sendAttributeReplaced(String name, Object value) {
        ServletContextAttributeEvent event = new ServletContextAttributeEvent(getServletContext(), name, value);

        for (Iterator<ServletContextAttributeListener> i = _contextAttributeListeners.iterator(); i.hasNext();) {
            ServletContextAttributeListener listener = i.next();
            listener.attributeReplaced(event);
        }
    }

    void sendAttributeRemoved(String name, Object value) {
        ServletContextAttributeEvent event = new ServletContextAttributeEvent(getServletContext(), name, value);

        for (Iterator<ServletContextAttributeListener> i = _contextAttributeListeners.iterator(); i.hasNext();) {
            ServletContextAttributeListener listener = i.next();
            listener.attributeRemoved(event);
        }
    }

    private void extractSecurityConstraints(Document document) throws SAXException {
        NodeList nl = document.getElementsByTagName("security-constraint");
        for (int i = 0; i < nl.getLength(); i++) {
            _securityConstraints.add(new SecurityConstraintImpl((Element) nl.item(i)));
        }
    }

    String getContextPath() {
        return _contextPath;
    }

    ServletContext getServletContext() {
        if (_servletContext == null) {
            _servletContext = new ServletUnitServletContext(this);
        }
        return _servletContext;
    }

    /**
     * Registers a servlet class to be run.
     **/
    void registerServlet(String resourceName, String servletClassName, Hashtable initParams) {
        registerServlet(resourceName, new ServletConfiguration(servletClassName, initParams));
    }

    /**
     * Registers a servlet to be run.
     **/
    void registerServlet(String resourceName, ServletConfiguration servletConfiguration) {
        // FIXME - shouldn't everything start with one or the other?
        if (!resourceName.startsWith("/") && !resourceName.startsWith("*")) {
            resourceName = "/" + resourceName;
        }
        _servletMapping.put(resourceName, servletConfiguration);
    }

    /**
     * Calls the destroy method for every active servlet.
     */
    void destroyServlets() {
        _servletMapping.destroyWebResources();
    }

    ServletMetaData getServletRequest(URL url) {
        return _servletMapping.get(url);
    }

    /**
     * Returns true if this application uses Basic Authentication.
     */
    boolean usesBasicAuthentication() {
        return _useBasicAuthentication;
    }

    /**
     * Returns true if this application uses form-based authentication.
     */
    boolean usesFormAuthentication() {
        return _useFormAuthentication;
    }

    String getAuthenticationRealm() {
        return _authenticationRealm;
    }

    URL getLoginURL() {
        return _loginURL;
    }

    URL getErrorURL() {
        return _errorURL;
    }

    /**
     * Returns true if the specified path may only be accesses by an authorized user.
     *
     * @param url
     *            the application-relative path of the URL
     */
    boolean requiresAuthorization(URL url) {
        String result;
        String file = url.getFile();
        if (_contextPath.equals("")) {
            result = file;
        } else if (file.startsWith(_contextPath)) {
            result = file.substring(_contextPath.length());
        } else {
            result = null;
        }
        return getControllingConstraint(result) != NULL_SECURITY_CONSTRAINT;
    }

    /**
     * Returns an array containing the roles permitted to access the specified URL.
     */
    String[] getPermittedRoles(URL url) {
        String result;
        String file = url.getFile();
        if (_contextPath.equals("")) {
            result = file;
        } else if (file.startsWith(_contextPath)) {
            result = file.substring(_contextPath.length());
        } else {
            result = null;
        }
        return getControllingConstraint(result).getPermittedRoles();
    }

    private SecurityConstraint getControllingConstraint(String urlPath) {
        for (SecurityConstraint sc : _securityConstraints) {
            if (sc.controlsPath(urlPath)) {
                return sc;
            }
        }
        return NULL_SECURITY_CONSTRAINT;
    }

    File getResourceFile(String path) {
        String relativePath = path.startsWith("/") ? path.substring(1) : path;
        if (_contextDir == null) {
            return new File(relativePath);
        }
        return new File(_contextDir, relativePath);
    }

    Hashtable getContextParameters() {
        return _contextParameters;
    }

    // ---------------------------------------- SessionListenerDispatcher methods
    // -------------------------------------------

    @Override
    public void sendSessionCreated(HttpSession session) {
        HttpSessionEvent event = new HttpSessionEvent(session);

        for (HttpSessionListener listener : _sessionListeners) {
            listener.sessionCreated(event);
        }
    }

    @Override
    public void sendSessionDestroyed(HttpSession session) {
        HttpSessionEvent event = new HttpSessionEvent(session);

        for (HttpSessionListener listener : _sessionListeners) {
            listener.sessionDestroyed(event);
        }
    }

    @Override
    public void sendAttributeAdded(HttpSession session, String name, Object value) {
        HttpSessionBindingEvent event = new HttpSessionBindingEvent(session, name, value);

        for (HttpSessionAttributeListener listener : _sessionAttributeListeners) {
            listener.attributeAdded(event);
        }
    }

    @Override
    public void sendAttributeReplaced(HttpSession session, String name, Object oldValue) {
        HttpSessionBindingEvent event = new HttpSessionBindingEvent(session, name, oldValue);

        for (HttpSessionAttributeListener listener : _sessionAttributeListeners) {
            listener.attributeReplaced(event);
        }
    }

    @Override
    public void sendAttributeRemoved(HttpSession session, String name, Object oldValue) {
        HttpSessionBindingEvent event = new HttpSessionBindingEvent(session, name, oldValue);

        for (HttpSessionAttributeListener listener : _sessionAttributeListeners) {
            listener.attributeRemoved(event);
        }
    }

    // --------------------------------------------------- private members
    // --------------------------------------------------

    private void registerFilters(Document document) throws SAXException {
        Hashtable nameToClass = new Hashtable<>();
        NodeList nl = document.getElementsByTagName("filter");
        for (int i = 0; i < nl.getLength(); i++) {
            registerFilterClass(nameToClass, (Element) nl.item(i));
        }
        nl = document.getElementsByTagName("filter-mapping");
        for (int i = 0; i < nl.getLength(); i++) {
            registerFilter(nameToClass, (Element) nl.item(i));
        }
        this._filters = nameToClass;
    }

    private void registerFilterClass(Dictionary mapping, Element filterElement) throws SAXException {
        String filterName = XMLUtils.getChildNodeValue(filterElement, "filter-name");
        mapping.put(filterName, new FilterConfiguration(filterName, filterElement));
    }

    private void registerFilter(Dictionary mapping, Element filterElement) throws SAXException {
        if (XMLUtils.hasChildNode(filterElement, "servlet-name")) {
            registerFilterForServlet(XMLUtils.getChildNodeValue(filterElement, "servlet-name"),
                    (FilterConfiguration) mapping.get(XMLUtils.getChildNodeValue(filterElement, "filter-name")));
        }
        if (XMLUtils.hasChildNode(filterElement, "url-pattern")) {
            registerFilterForUrl(XMLUtils.getChildNodeValue(filterElement, "url-pattern"),
                    (FilterConfiguration) mapping.get(XMLUtils.getChildNodeValue(filterElement, "filter-name")));
        }
    }

    private void registerFilterForUrl(String resourceName, FilterConfiguration filterConfiguration) {
        _filterUrlMapping.put(resourceName, filterConfiguration);
    }

    private void registerFilterForServlet(String servletName, FilterConfiguration filterConfiguration) {
        List list = (List) _filterMapping.get(servletName);
        if (list == null) {
            list = new ArrayList<>();
            _filterMapping.put(servletName, list);
        }
        list.add(filterConfiguration);
    }

    private void extractLoginConfiguration(Document document) throws MalformedURLException, SAXException {
        NodeList nl = document.getElementsByTagName("login-config");
        if (nl.getLength() == 1) {
            final Element loginConfigElement = (Element) nl.item(0);
            String authenticationMethod = XMLUtils.getChildNodeValue(loginConfigElement, "auth-method", "BASIC");
            _authenticationRealm = XMLUtils.getChildNodeValue(loginConfigElement, "realm-name", "");
            if (authenticationMethod.equalsIgnoreCase("BASIC")) {
                _useBasicAuthentication = true;
                if (_authenticationRealm.isEmpty()) {
                    throw new SAXException("No realm specified for BASIC Authorization");
                }
            } else if (authenticationMethod.equalsIgnoreCase("FORM")) {
                _useFormAuthentication = true;
                if (_authenticationRealm.isEmpty()) {
                    throw new SAXException("No realm specified for FORM Authorization");
                }
                _loginURL = new URL("http", "localhost",
                        _contextPath + XMLUtils.getChildNodeValue(loginConfigElement, "form-login-page"));
                _errorURL = new URL("http", "localhost",
                        _contextPath + XMLUtils.getChildNodeValue(loginConfigElement, "form-error-page"));
            }
        }
    }

    private void registerServlets(Document document) throws SAXException {
        Hashtable nameToClass = new Hashtable<>();
        NodeList nl = document.getElementsByTagName("servlet");
        for (int i = 0; i < nl.getLength(); i++) {
            registerServletClass(nameToClass, (Element) nl.item(i));
        }
        nl = document.getElementsByTagName("servlet-mapping");
        for (int i = 0; i < nl.getLength(); i++) {
            registerServlet(nameToClass, (Element) nl.item(i));
        }
        this._servlets = nameToClass;
    }

    private void registerServletClass(Dictionary mapping, Element servletElement) throws SAXException {
        mapping.put(XMLUtils.getChildNodeValue(servletElement, "servlet-name"),
                new ServletConfiguration(servletElement));
    }

    private void registerServlet(Dictionary mapping, Element servletElement) throws SAXException {
        registerServlet(XMLUtils.getChildNodeValue(servletElement, "url-pattern"),
                (ServletConfiguration) mapping.get(XMLUtils.getChildNodeValue(servletElement, "servlet-name")));
    }

    private void extractContextParameters(Document document) throws SAXException {
        NodeList nl = document.getElementsByTagName("context-param");
        for (int i = 0; i < nl.getLength(); i++) {
            Element param = (Element) nl.item(i);
            String name = XMLUtils.getChildNodeValue(param, "param-name");
            String value = XMLUtils.getChildNodeValue(param, "param-value");
            _contextParameters.put(name, value);
        }
    }

    private static boolean patternMatches(String urlPattern, String urlPath) {
        return urlPattern.equals(urlPath);
    }

    String getDisplayName() {
        return _displayName;
    }

    // ============================================= SecurityCheckServlet class
    // =============================================

    static class SecurityCheckServlet extends HttpServlet {

        private static final long serialVersionUID = 1L;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            handleLogin(req, resp);
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            handleLogin(req, resp);
        }

        private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            final String username = req.getParameter("j_username");
            final String roleList = req.getParameter("j_password");
            getServletSession(req).setUserInformation(username, ServletUnitHttpRequest.toArray(roleList));
            resp.sendRedirect(getServletSession(req).getOriginalURL().toExternalForm());
        }

        private ServletUnitHttpSession getServletSession(HttpServletRequest req) {
            return (ServletUnitHttpSession) req.getSession();
        }

    }

    // ============================================= ServletConfiguration class
    // =============================================

    static final int DONT_AUTOLOAD = Integer.MIN_VALUE;
    static final int ANY_LOAD_ORDER = Integer.MAX_VALUE;

    class ServletConfiguration extends WebResourceConfiguration {

        private Servlet _servlet;
        private String _servletName;
        private String _jspFile;
        private int _loadOrder = DONT_AUTOLOAD;

        ServletConfiguration(String className) {
            super(className);
        }

        ServletConfiguration(String className, Hashtable initParams) {
            super(className, initParams);
        }

        ServletConfiguration(Element servletElement) throws SAXException {
            super(servletElement, "servlet-class", XMLUtils.getChildNodeValue(servletElement, "servlet-class",
                    "org.apache.jasper.servlet.JspServlet"));
            _servletName = XMLUtils.getChildNodeValue(servletElement, "servlet-name");
            _jspFile = XMLUtils.getChildNodeValue(servletElement, "jsp-file", "");
            if ("".equals(_jspFile)) {
                _jspFile = null;
            }
            final NodeList loadOrder = servletElement.getElementsByTagName("load-on-startup");
            for (int i = 0; i < loadOrder.getLength(); i++) {
                String order = XMLUtils.getTextValue(loadOrder.item(i));
                try {
                    _loadOrder = Integer.parseInt(order);
                } catch (NumberFormatException e) {
                    _loadOrder = ANY_LOAD_ORDER;
                }
            }
        }

        synchronized Servlet getServlet()
                throws ClassNotFoundException, InstantiationException, IllegalAccessException, ServletException,
                IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
            if (_servlet == null) {
                Class servletClass = Class.forName(getClassName());
                _servlet = (Servlet) servletClass.getDeclaredConstructor().newInstance();
                String servletName = _servletName != null ? _servletName : _servlet.getClass().getName();
                _servlet.init(new ServletUnitServletConfig(servletName, WebApplication.this, getInitParams()));
            }

            return _servlet;
        }

        @Override
        synchronized void destroyResource() {
            if (_servlet != null) {
                _servlet.destroy();
            }
        }

        String getServletName() {
            return _servletName;
        }

        @Override
        boolean isLoadOnStartup() {
            return _loadOrder != DONT_AUTOLOAD;
        }

        public int getLoadOrder() {
            return _loadOrder;
        }

        public Object getJspFile() {
            return this._jspFile;
        }
    }

    // ============================================= FilterConfiguration class
    // =============================================

    class FilterConfiguration extends WebResourceConfiguration implements FilterMetaData {

        private Filter _filter;
        private String _name;

        FilterConfiguration(String name, Element filterElement) throws SAXException {
            super(filterElement, "filter-class");
            _name = name;
        }

        @Override
        public synchronized Filter getFilter() throws ServletException {
            try {
                if (_filter == null) {
                    Class filterClass = Class.forName(getClassName());
                    _filter = (Filter) filterClass.getDeclaredConstructor().newInstance();
                    _filter.init(new FilterConfigImpl(_name, getServletContext(), getInitParams()));
                }

                return _filter;
            } catch (ClassNotFoundException e) {
                throw new ServletException("Did not find filter class: " + getClassName());
            } catch (IllegalAccessException e) {
                throw new ServletException("Filter class " + getClassName() + " lacks a public no-arg constructor");
            } catch (InstantiationException | IllegalArgumentException | InvocationTargetException
                    | NoSuchMethodException | SecurityException e) {
                throw new ServletException("Filter class " + getClassName() + " could not be instantiated.");
            } catch (ClassCastException e) {
                throw new ServletException(
                        "Filter class " + getClassName() + " does not implement" + Filter.class.getName());
            }
        }

        @Override
        boolean isLoadOnStartup() {
            return false;
        }

        @Override
        synchronized void destroyResource() {
            if (_filter != null) {
                _filter.destroy();
            }
        }
    }

    // =================================== SecurityConstract interface and implementations
    // ==================================

    interface SecurityConstraint {

        boolean controlsPath(String urlPath);

        String[] getPermittedRoles();
    }

    static class NullSecurityConstraint implements SecurityConstraint {

        private static final String[] NO_ROLES = {};

        @Override
        public boolean controlsPath(String urlPath) {
            return false;
        }

        @Override
        public String[] getPermittedRoles() {
            return NO_ROLES;
        }
    }

    static class SecurityConstraintImpl implements SecurityConstraint {

        SecurityConstraintImpl(Element root) throws SAXException {
            final NodeList roleNames = root.getElementsByTagName("role-name");
            for (int i = 0; i < roleNames.getLength(); i++) {
                _roleList.add(XMLUtils.getTextValue(roleNames.item(i)));
            }

            final NodeList resources = root.getElementsByTagName("web-resource-collection");
            for (int i = 0; i < resources.getLength(); i++) {
                _resources.add(new WebResourceCollection((Element) resources.item(i)));
            }
        }

        @Override
        public boolean controlsPath(String urlPath) {
            return getMatchingCollection(urlPath) != null;
        }

        @Override
        public String[] getPermittedRoles() {
            if (_roles == null) {
                _roles = _roleList.toArray(new String[_roleList.size()]);
            }
            return _roles;
        }

        private String[] _roles;
        private List<String> _roleList = new ArrayList<>();
        private List<WebResourceCollection> _resources = new ArrayList<>();

        public WebResourceCollection getMatchingCollection(String urlPath) {
            for (WebResourceCollection wrc : _resources) {
                if (wrc.controlsPath(urlPath)) {
                    return wrc;
                }
            }
            return null;
        }

        class WebResourceCollection {

            WebResourceCollection(Element root) throws SAXException {
                final NodeList urlPatterns = root.getElementsByTagName("url-pattern");
                for (int i = 0; i < urlPatterns.getLength(); i++) {
                    _urlPatterns.add(XMLUtils.getTextValue(urlPatterns.item(i)));
                }
            }

            boolean controlsPath(String urlPath) {
                for (String pattern : _urlPatterns) {
                    if (patternMatches(pattern, urlPath)) {
                        return true;
                    }
                }
                return false;
            }

            private List<String> _urlPatterns = new ArrayList<>();
        }
    }

    static final FilterMetaData[] NO_FILTERS = {};

    static class ServletRequestImpl implements ServletMetaData {

        private URL _url;
        private String _fullServletPath;
        private WebResourceMapping _mapping;
        private Hashtable _filtersPerName;
        private FilterUrlMap _filtersPerUrl;

        ServletRequestImpl(URL url, String servletPath, WebResourceMapping mapping, Hashtable filtersPerName,
                FilterUrlMap filtersPerUrl) {
            _url = url;
            _fullServletPath = servletPath;
            _mapping = mapping;
            _filtersPerName = filtersPerName;
            _filtersPerUrl = filtersPerUrl;
        }

        /**
         * get the Servlet
         *
         * @return the Servlet from the configuration
         *
         * @throws ServletException
         *             - e.g. if no configuration is available
         */
        @Override
        public Servlet getServlet() throws ServletException {
            if (getConfiguration() == null) {
                throw new HttpNotFoundException("No servlet mapping defined", _url);
            }

            try {
                return getConfiguration().getServlet();
            } catch (ClassNotFoundException e) {
                throw new HttpNotFoundException(_url, e);
            } catch (IllegalAccessException | InstantiationException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                throw new HttpInternalErrorException(_url, e);
            }
        }

        /**
         * get the ServletPath the decoded ServletPath
         */
        @Override
        public String getServletPath() {
            return _mapping == null ? null : HttpUnitUtils.decode(_mapping.getServletPath(_fullServletPath));
        }

        /**
         * get the Path Information
         *
         * @return the decode path
         */
        @Override
        public String getPathInfo() {
            return _mapping == null ? null : HttpUnitUtils.decode(_mapping.getPathInfo(_fullServletPath));
        }

        @Override
        public FilterMetaData[] getFilters() {
            if (getConfiguration() == null) {
                return NO_FILTERS;
            }

            List<FilterMetaData> filters = new ArrayList<>();
            addFiltersForPath(filters, _fullServletPath);
            addFiltersForServletWithName(filters, getConfiguration().getServletName());

            return filters.toArray(new FilterMetaData[filters.size()]);
        }

        private void addFiltersForPath(List<FilterMetaData> filters, String fullServletPath) {
            FilterMetaData[] matches = _filtersPerUrl.getMatchingFilters(fullServletPath);
            Collections.addAll(filters, matches);
        }

        private void addFiltersForServletWithName(List<FilterMetaData> filters, String servletName) {
            if (servletName == null) {
                return;
            }
            List<FilterMetaData> matches = (List<FilterMetaData>) _filtersPerName.get(servletName);
            if (matches != null) {
                filters.addAll(matches);
            }
        }

        private ServletConfiguration getConfiguration() {
            return _mapping == null ? null : (ServletConfiguration) _mapping.getConfiguration();
        }
    }

    /**
     * mapping for WebResources
     */
    static class WebResourceMapping {

        private WebResourceConfiguration _configuration;

        WebResourceConfiguration getConfiguration() {
            return _configuration;
        }

        WebResourceMapping(WebResourceConfiguration configuration) {
            _configuration = configuration;
        }

        /**
         * Returns the portion of the request path which was actually used to select the servlet. This default
         * implementation returns the full specified path.
         *
         * @param requestPath
         *            the full path of the request, relative to the application root.
         */
        String getServletPath(String requestPath) {
            return requestPath;
        }

        /**
         * Returns the portion of the request path which was not used to select the servlet, and can be used as data by
         * the servlet. This default implementation returns null.
         *
         * @param requestPath
         *            the full path of the request, relative to the application root.
         */
        String getPathInfo(String requestPath) {
            return null;
        }

        public void destroyResource() {
            getConfiguration().destroyResource();
        }
    }

    static class PartialMatchWebResourceMapping extends WebResourceMapping {

        private String _prefix;

        public PartialMatchWebResourceMapping(WebResourceConfiguration configuration, String prefix) {
            super(configuration);
            if (!prefix.endsWith("/*")) {
                throw new IllegalArgumentException(prefix + " does not end with '/*'");
            }
            _prefix = prefix.substring(0, prefix.length() - 2);
        }

        @Override
        String getServletPath(String requestPath) {
            return _prefix;
        }

        @Override
        String getPathInfo(String requestPath) {
            return requestPath.length() > _prefix.length() ? requestPath.substring(_prefix.length()) : null;
        }
    }

    /**
     * A utility class for mapping web resources to url patterns. This implements the matching algorithm documented in
     * section 10 of the JSDK-2.2 reference.
     */
    class WebResourceMap {

        private final Map _exactMatches = new HashMap<>();
        private final Map _extensions = new HashMap<>();
        private final Map _urlTree = new HashMap<>();
        private WebResourceMapping _defaultMapping;

        void put(String mapping, WebResourceConfiguration configuration) {
            if (mapping.equals("/")) {
                _defaultMapping = new WebResourceMapping(configuration);
            } else if (mapping.startsWith("*.")) {
                _extensions.put(mapping.substring(2), new WebResourceMapping(configuration));
            } else if (!mapping.startsWith("/") || !mapping.endsWith("/*")) {
                _exactMatches.put(mapping, new WebResourceMapping(configuration));
            } else {
                ParsedPath path = new ParsedPath(mapping);
                Map context = _urlTree;
                while (path.hasNext()) {
                    String part = path.next();
                    if (part.equals("*")) {
                        context.put("*", new PartialMatchWebResourceMapping(configuration, mapping));
                        return;
                    }
                    if (!context.containsKey(part)) {
                        context.put(part, new HashMap<>());
                    }
                    context = (Map) context.get(part);
                }
            }
        }

        ServletMetaData get(URL url) {
            String file = url.getFile();
            if (!file.startsWith(_contextPath)) {
                throw new HttpNotFoundException("File path does not begin with '" + _contextPath + "'", url);
            }

            String servletPath = getServletPath(file.substring(_contextPath.length()));

            if (servletPath.endsWith("j_security_check")) {
                return new ServletRequestImpl(url, servletPath, SECURITY_CHECK_MAPPING, _filterMapping,
                        _filterUrlMapping);
            }
            return new ServletRequestImpl(url, servletPath, getMapping(servletPath), _filterMapping, _filterUrlMapping);
        }

        private String getServletPath(String urlFile) {
            if (urlFile.indexOf('?') < 0) {
                return urlFile;
            }
            return urlFile.substring(0, urlFile.indexOf('?'));
        }

        public void destroyWebResources() {
            if (_defaultMapping != null) {
                _defaultMapping.destroyResource();
            }
            destroyWebResources(_exactMatches);
            destroyWebResources(_extensions);
            destroyWebResources(_urlTree);
        }

        private void destroyWebResources(Map map) {
            for (Object o : map.values()) {
                if (o instanceof WebResourceMapping) {
                    WebResourceMapping webResourceMapping = (WebResourceMapping) o;
                    webResourceMapping.destroyResource();
                } else {
                    destroyWebResources((Map) o);
                }
            }
        }

        void autoLoadServlets() {
            ArrayList autoLoadable = new ArrayList<>();
            if (_defaultMapping != null && _defaultMapping.getConfiguration().isLoadOnStartup()) {
                autoLoadable.add(_defaultMapping.getConfiguration());
            }
            collectAutoLoadableServlets(autoLoadable, _exactMatches);
            collectAutoLoadableServlets(autoLoadable, _extensions);
            collectAutoLoadableServlets(autoLoadable, _urlTree);
            if (autoLoadable.isEmpty()) {
                return;
            }

            Collections.sort(autoLoadable, (o1, o2) -> {
                ServletConfiguration sc1 = (ServletConfiguration) o1;
                ServletConfiguration sc2 = (ServletConfiguration) o2;
                return sc1.getLoadOrder() <= sc2.getLoadOrder() ? -1 : +1;
            });
            for (Iterator iterator = autoLoadable.iterator(); iterator.hasNext();) {
                ServletConfiguration servletConfiguration = (ServletConfiguration) iterator.next();
                try {
                    servletConfiguration.getServlet();
                } catch (Exception e) {
                    HttpUnitUtils.handleException(e);
                    throw new RuntimeException(
                            "Unable to autoload servlet: " + servletConfiguration.getClassName() + ": " + e);
                }
            }
        }

        private void collectAutoLoadableServlets(Collection collection, Map map) {
            for (Object o : map.values()) {
                if (o instanceof WebResourceMapping) {
                    WebResourceMapping servletMapping = (WebResourceMapping) o;
                    if (servletMapping.getConfiguration().isLoadOnStartup()) {
                        collection.add(servletMapping.getConfiguration());
                    }
                } else {
                    collectAutoLoadableServlets(collection, (Map) o);
                }
            }
        }

        private WebResourceMapping getMapping(String url) {
            if (_exactMatches.containsKey(url)) {
                return (WebResourceMapping) _exactMatches.get(url);
            }

            Map context = getContextForLongestPathPrefix(url);
            if (context.containsKey("*")) {
                return (WebResourceMapping) context.get("*");
            }

            if (_extensions.containsKey(getExtension(url))) {
                return (WebResourceMapping) _extensions.get(getExtension(url));
            }

            if (_urlTree.containsKey("/")) {
                return (WebResourceMapping) _urlTree.get("/");
            }

            if (_defaultMapping != null) {
                return _defaultMapping;
            }

            final String prefix = "/servlet/";
            if (!url.startsWith(prefix)) {
                return null;
            }

            String className = url.substring(prefix.length());
            try {
                Class.forName(className);
                return new WebResourceMapping(new ServletConfiguration(className));
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        private Map getContextForLongestPathPrefix(String url) {
            Map context = _urlTree;

            ParsedPath path = new ParsedPath(url);
            while (path.hasNext()) {
                String part = path.next();
                if (!context.containsKey(part)) {
                    break;
                }
                context = (Map) context.get(part);
            }
            return context;
        }

        private String getExtension(String url) {
            int index = url.lastIndexOf('.');
            if (index == -1 || index >= url.length() - 1) {
                return "";
            }
            return url.substring(index + 1);
        }

    }

    /**
     * return the given ServletConfiguration for the given servlet name
     *
     * @param servletName
     *
     * @return the corresponding ServletConfiguration
     */
    public ServletConfiguration getServletByName(String servletName) {
        return (ServletConfiguration) _servlets.get(servletName);
    }

}

/**
 * A utility class for parsing URLs into paths
 *
 * @author <a href="balld@webslingerZ.com">Donald Ball</a>
 */
class ParsedPath {

    private final String path;
    private int position = 0;
    static final char seperator_char = '/';

    /**
     * Creates a new parsed path for the given path value
     *
     * @param path
     *            the path
     */
    ParsedPath(String path) {
        if (path.charAt(0) != seperator_char) {
            throw new IllegalArgumentException("Illegal path '" + path + "', does not begin with " + seperator_char);
        }
        this.path = path;
    }

    /**
     * Returns true if there are more parts left, otherwise false
     */
    public final boolean hasNext() {
        return position < path.length();
    }

    /**
     * Returns the next part in the path
     */
    public final String next() {
        int offset = position + 1;
        while (offset < path.length() && path.charAt(offset) != seperator_char) {
            offset++;
        }
        String result = path.substring(position + 1, offset);
        position = offset;
        return result;
    }

}

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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class represents the information recorded about a single web application. It is usually extracted from web.xml.
 **/
class WebApplication implements SessionListenerDispatcher {

    /** The Constant NULL_SECURITY_CONSTRAINT. */
    private static final SecurityConstraint NULL_SECURITY_CONSTRAINT = new NullSecurityConstraint();

    /** The security check configuration. */
    private final ServletConfiguration SECURITY_CHECK_CONFIGURATION = new ServletConfiguration(
            SecurityCheckServlet.class.getName());

    /** The security check mapping. */
    private final WebResourceMapping SECURITY_CHECK_MAPPING = new WebResourceMapping(SECURITY_CHECK_CONFIGURATION);

    /** A mapping of resource names to servlet configurations. **/
    private WebResourceMap _servletMapping = new WebResourceMap();

    /** A mapping of filter names to FilterConfigurations. */
    private Properties _filters = new Properties();

    /** A mapping of servlet names to ServletConfigurations. */
    private Properties _servlets = new Properties();

    /** A mapping of resource names to filter configurations. **/
    private FilterUrlMap _filterUrlMapping = new FilterUrlMap();

    /** A mapping of servlet names to filter configurations. **/
    private Properties _filterMapping = new Properties();

    /** The security constraints. */
    private List<SecurityConstraint> _securityConstraints = new ArrayList<>();

    /** The context listeners. */
    private List<ServletContextListener> _contextListeners = new ArrayList<>();

    /** The context attribute listeners. */
    private List<ServletContextAttributeListener> _contextAttributeListeners = new ArrayList<>();

    /** The session listeners. */
    private List<HttpSessionListener> _sessionListeners = new ArrayList<>();

    /** The session attribute listeners. */
    private List<HttpSessionAttributeListener> _sessionAttributeListeners = new ArrayList<>();

    /** The use basic authentication. */
    private boolean _useBasicAuthentication;

    /** The use form authentication. */
    private boolean _useFormAuthentication;

    /** The authentication realm. */
    private String _authenticationRealm = "";

    /** The login URL. */
    private URL _loginURL;

    /** The error URL. */
    private URL _errorURL;

    /** The context parameters. */
    private Properties _contextParameters = new Properties();

    /** The context dir. */
    private File _contextDir = null;

    /** The context path. */
    private String _contextPath = null;

    /** The servlet context. */
    private ServletUnitServletContext _servletContext;

    /** The display name. */
    private String _displayName;

    /**
     * Constructs a default application spec with no information.
     */
    WebApplication() {
        _contextPath = "";
    }

    /**
     * Constructs an application spec from an XML document.
     *
     * @param document
     *            the document
     *
     * @throws MalformedURLException
     *             the malformed URL exception
     * @throws SAXException
     *             the SAX exception
     */
    WebApplication(Document document) throws MalformedURLException, SAXException {
        this(document, null, "");
    }

    /**
     * Constructs an application spec from an XML document.
     *
     * @param document
     *            the document
     * @param contextPath
     *            the context path
     *
     * @throws MalformedURLException
     *             the malformed URL exception
     * @throws SAXException
     *             the SAX exception
     */
    WebApplication(Document document, String contextPath) throws MalformedURLException, SAXException {
        this(document, null, contextPath);
    }

    /**
     * Constructs an application spec from an XML document.
     *
     * @param document
     *            the document
     * @param file
     *            the file
     * @param contextPath
     *            the context path
     *
     * @throws MalformedURLException
     *             the malformed URL exception
     * @throws SAXException
     *             the SAX exception
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

    /**
     * Extract listeners.
     *
     * @param document
     *            the document
     *
     * @throws SAXException
     *             the SAX exception
     */
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

    /**
     * Notify context initialized.
     */
    private void notifyContextInitialized() {
        ServletContextEvent event = new ServletContextEvent(getServletContext());

        for (Iterator<ServletContextListener> i = _contextListeners.iterator(); i.hasNext();) {
            ServletContextListener listener = i.next();
            listener.contextInitialized(event);
        }
    }

    /**
     * Shut down.
     */
    void shutDown() {
        destroyServlets();
        notifyContextDestroyed();
    }

    /**
     * Notify context destroyed.
     */
    private void notifyContextDestroyed() {
        ServletContextEvent event = new ServletContextEvent(getServletContext());

        for (ListIterator<ServletContextListener> i = _contextListeners.listIterator(_contextListeners.size()); i
                .hasPrevious();) {
            ServletContextListener listener = i.previous();
            listener.contextDestroyed(event);
        }
    }

    /**
     * Send attribute added.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     */
    void sendAttributeAdded(String name, Object value) {
        ServletContextAttributeEvent event = new ServletContextAttributeEvent(getServletContext(), name, value);

        for (Iterator<ServletContextAttributeListener> i = _contextAttributeListeners.iterator(); i.hasNext();) {
            ServletContextAttributeListener listener = i.next();
            listener.attributeAdded(event);
        }
    }

    /**
     * Send attribute replaced.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     */
    void sendAttributeReplaced(String name, Object value) {
        ServletContextAttributeEvent event = new ServletContextAttributeEvent(getServletContext(), name, value);

        for (Iterator<ServletContextAttributeListener> i = _contextAttributeListeners.iterator(); i.hasNext();) {
            ServletContextAttributeListener listener = i.next();
            listener.attributeReplaced(event);
        }
    }

    /**
     * Send attribute removed.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     */
    void sendAttributeRemoved(String name, Object value) {
        ServletContextAttributeEvent event = new ServletContextAttributeEvent(getServletContext(), name, value);

        for (Iterator<ServletContextAttributeListener> i = _contextAttributeListeners.iterator(); i.hasNext();) {
            ServletContextAttributeListener listener = i.next();
            listener.attributeRemoved(event);
        }
    }

    /**
     * Extract security constraints.
     *
     * @param document
     *            the document
     *
     * @throws SAXException
     *             the SAX exception
     */
    private void extractSecurityConstraints(Document document) throws SAXException {
        NodeList nl = document.getElementsByTagName("security-constraint");
        for (int i = 0; i < nl.getLength(); i++) {
            _securityConstraints.add(new SecurityConstraintImpl((Element) nl.item(i)));
        }
    }

    /**
     * Gets the context path.
     *
     * @return the context path
     */
    String getContextPath() {
        return _contextPath;
    }

    /**
     * Gets the servlet context.
     *
     * @return the servlet context
     */
    ServletContext getServletContext() {
        if (_servletContext == null) {
            _servletContext = new ServletUnitServletContext(this);
        }
        return _servletContext;
    }

    /**
     * Registers a servlet class to be run.
     *
     * @param resourceName
     *            the resource name
     * @param servletClassName
     *            the servlet class name
     * @param initParams
     *            the init params
     */
    void registerServlet(String resourceName, String servletClassName, Properties initParams) {
        registerServlet(resourceName, new ServletConfiguration(servletClassName, initParams));
    }

    /**
     * Registers a servlet to be run.
     *
     * @param resourceName
     *            the resource name
     * @param servletConfiguration
     *            the servlet configuration
     */
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

    /**
     * Gets the servlet request.
     *
     * @param url
     *            the url
     *
     * @return the servlet request
     */
    ServletMetaData getServletRequest(URL url) {
        return _servletMapping.get(url);
    }

    /**
     * Returns true if this application uses Basic Authentication.
     *
     * @return true, if successful
     */
    boolean usesBasicAuthentication() {
        return _useBasicAuthentication;
    }

    /**
     * Returns true if this application uses form-based authentication.
     *
     * @return true, if successful
     */
    boolean usesFormAuthentication() {
        return _useFormAuthentication;
    }

    /**
     * Gets the authentication realm.
     *
     * @return the authentication realm
     */
    String getAuthenticationRealm() {
        return _authenticationRealm;
    }

    /**
     * Gets the login URL.
     *
     * @return the login URL
     */
    URL getLoginURL() {
        return _loginURL;
    }

    /**
     * Gets the error URL.
     *
     * @return the error URL
     */
    URL getErrorURL() {
        return _errorURL;
    }

    /**
     * Returns true if the specified path may only be accesses by an authorized user.
     *
     * @param url
     *            the application-relative path of the URL
     *
     * @return true, if successful
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
     *
     * @param url
     *            the url
     *
     * @return the permitted roles
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

    /**
     * Gets the controlling constraint.
     *
     * @param urlPath
     *            the url path
     *
     * @return the controlling constraint
     */
    private SecurityConstraint getControllingConstraint(String urlPath) {
        for (SecurityConstraint sc : _securityConstraints) {
            if (sc.controlsPath(urlPath)) {
                return sc;
            }
        }
        return NULL_SECURITY_CONSTRAINT;
    }

    /**
     * Gets the resource file.
     *
     * @param path
     *            the path
     *
     * @return the resource file
     */
    File getResourceFile(String path) {
        String relativePath = path.startsWith("/") ? path.substring(1) : path;
        if (_contextDir == null) {
            return Path.of(relativePath).toFile();
        }
        return _contextDir.toPath().resolve(relativePath).toFile();
    }

    /**
     * Gets the context parameters.
     *
     * @return the context parameters
     */
    Properties getContextParameters() {
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

    /**
     * Register filters.
     *
     * @param document
     *            the document
     *
     * @throws SAXException
     *             the SAX exception
     */
    private void registerFilters(Document document) throws SAXException {
        Properties nameToClass = new Properties();
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

    /**
     * Register filter class.
     *
     * @param mapping
     *            the mapping
     * @param filterElement
     *            the filter element
     *
     * @throws SAXException
     *             the SAX exception
     */
    private void registerFilterClass(Dictionary mapping, Element filterElement) throws SAXException {
        String filterName = XMLUtils.getChildNodeValue(filterElement, "filter-name");
        mapping.put(filterName, new FilterConfiguration(filterName, filterElement));
    }

    /**
     * Register filter.
     *
     * @param mapping
     *            the mapping
     * @param filterElement
     *            the filter element
     *
     * @throws SAXException
     *             the SAX exception
     */
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

    /**
     * Register filter for url.
     *
     * @param resourceName
     *            the resource name
     * @param filterConfiguration
     *            the filter configuration
     */
    private void registerFilterForUrl(String resourceName, FilterConfiguration filterConfiguration) {
        _filterUrlMapping.put(resourceName, filterConfiguration);
    }

    /**
     * Register filter for servlet.
     *
     * @param servletName
     *            the servlet name
     * @param filterConfiguration
     *            the filter configuration
     */
    private void registerFilterForServlet(String servletName, FilterConfiguration filterConfiguration) {
        List list = (List) _filterMapping.get(servletName);
        if (list == null) {
            list = new ArrayList<>();
            _filterMapping.put(servletName, list);
        }
        list.add(filterConfiguration);
    }

    /**
     * Extract login configuration.
     *
     * @param document
     *            the document
     *
     * @throws MalformedURLException
     *             the malformed URL exception
     * @throws SAXException
     *             the SAX exception
     */
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

    /**
     * Register servlets.
     *
     * @param document
     *            the document
     *
     * @throws SAXException
     *             the SAX exception
     */
    private void registerServlets(Document document) throws SAXException {
        Properties nameToClass = new Properties();
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

    /**
     * Register servlet class.
     *
     * @param mapping
     *            the mapping
     * @param servletElement
     *            the servlet element
     *
     * @throws SAXException
     *             the SAX exception
     */
    private void registerServletClass(Dictionary mapping, Element servletElement) throws SAXException {
        mapping.put(XMLUtils.getChildNodeValue(servletElement, "servlet-name"),
                new ServletConfiguration(servletElement));
    }

    /**
     * Register servlet.
     *
     * @param mapping
     *            the mapping
     * @param servletElement
     *            the servlet element
     *
     * @throws SAXException
     *             the SAX exception
     */
    private void registerServlet(Dictionary mapping, Element servletElement) throws SAXException {
        registerServlet(XMLUtils.getChildNodeValue(servletElement, "url-pattern"),
                (ServletConfiguration) mapping.get(XMLUtils.getChildNodeValue(servletElement, "servlet-name")));
    }

    /**
     * Extract context parameters.
     *
     * @param document
     *            the document
     *
     * @throws SAXException
     *             the SAX exception
     */
    private void extractContextParameters(Document document) throws SAXException {
        NodeList nl = document.getElementsByTagName("context-param");
        for (int i = 0; i < nl.getLength(); i++) {
            Element param = (Element) nl.item(i);
            String name = XMLUtils.getChildNodeValue(param, "param-name");
            String value = XMLUtils.getChildNodeValue(param, "param-value");
            _contextParameters.put(name, value);
        }
    }

    /**
     * Pattern matches.
     *
     * @param urlPattern
     *            the url pattern
     * @param urlPath
     *            the url path
     *
     * @return true, if successful
     */
    private static boolean patternMatches(String urlPattern, String urlPath) {
        return urlPattern.equals(urlPath);
    }

    /**
     * Gets the display name.
     *
     * @return the display name
     */
    String getDisplayName() {
        return _displayName;
    }

    // ============================================= SecurityCheckServlet class
    // =============================================

    /**
     * The Class SecurityCheckServlet.
     */
    static class SecurityCheckServlet extends HttpServlet {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            handleLogin(req, resp);
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            handleLogin(req, resp);
        }

        /**
         * Handle login.
         *
         * @param req
         *            the req
         * @param resp
         *            the resp
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            final String username = req.getParameter("j_username");
            final String roleList = req.getParameter("j_password");
            getServletSession(req).setUserInformation(username, ServletUnitHttpRequest.toArray(roleList));
            resp.sendRedirect(getServletSession(req).getOriginalURL().toExternalForm());
        }

        /**
         * Gets the servlet session.
         *
         * @param req
         *            the req
         *
         * @return the servlet session
         */
        private ServletUnitHttpSession getServletSession(HttpServletRequest req) {
            return (ServletUnitHttpSession) req.getSession();
        }

    }

    // ============================================= ServletConfiguration class
    // =============================================

    /** The Constant DONT_AUTOLOAD. */
    static final int DONT_AUTOLOAD = Integer.MIN_VALUE;

    /** The Constant ANY_LOAD_ORDER. */
    static final int ANY_LOAD_ORDER = Integer.MAX_VALUE;

    /**
     * The Class ServletConfiguration.
     */
    class ServletConfiguration extends WebResourceConfiguration {

        /** The servlet. */
        private Servlet _servlet;

        /** The servlet name. */
        private String _servletName;

        /** The jsp file. */
        private String _jspFile;

        /** The load order. */
        private int _loadOrder = DONT_AUTOLOAD;

        /**
         * Instantiates a new servlet configuration.
         *
         * @param className
         *            the class name
         */
        ServletConfiguration(String className) {
            super(className);
        }

        /**
         * Instantiates a new servlet configuration.
         *
         * @param className
         *            the class name
         * @param initParams
         *            the init params
         */
        ServletConfiguration(String className, Properties initParams) {
            super(className, initParams);
        }

        /**
         * Instantiates a new servlet configuration.
         *
         * @param servletElement
         *            the servlet element
         *
         * @throws SAXException
         *             the SAX exception
         */
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

        /**
         * Gets the servlet.
         *
         * @return the servlet
         *
         * @throws ClassNotFoundException
         *             the class not found exception
         * @throws InstantiationException
         *             the instantiation exception
         * @throws IllegalAccessException
         *             the illegal access exception
         * @throws ServletException
         *             the servlet exception
         * @throws IllegalArgumentException
         *             the illegal argument exception
         * @throws InvocationTargetException
         *             the invocation target exception
         * @throws NoSuchMethodException
         *             the no such method exception
         * @throws SecurityException
         *             the security exception
         */
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

        /**
         * Gets the servlet name.
         *
         * @return the servlet name
         */
        String getServletName() {
            return _servletName;
        }

        @Override
        boolean isLoadOnStartup() {
            return _loadOrder != DONT_AUTOLOAD;
        }

        /**
         * Gets the load order.
         *
         * @return the load order
         */
        public int getLoadOrder() {
            return _loadOrder;
        }

        /**
         * Gets the jsp file.
         *
         * @return the jsp file
         */
        public Object getJspFile() {
            return this._jspFile;
        }
    }

    // ============================================= FilterConfiguration class
    // =============================================

    /**
     * The Class FilterConfiguration.
     */
    class FilterConfiguration extends WebResourceConfiguration implements FilterMetaData {

        /** The filter. */
        private Filter _filter;

        /** The name. */
        private String _name;

        /**
         * Instantiates a new filter configuration.
         *
         * @param name
         *            the name
         * @param filterElement
         *            the filter element
         *
         * @throws SAXException
         *             the SAX exception
         */
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

    /**
     * The Interface SecurityConstraint.
     */
    interface SecurityConstraint {

        /**
         * Controls path.
         *
         * @param urlPath
         *            the url path
         *
         * @return true, if successful
         */
        boolean controlsPath(String urlPath);

        /**
         * Gets the permitted roles.
         *
         * @return the permitted roles
         */
        String[] getPermittedRoles();
    }

    /**
     * The Class NullSecurityConstraint.
     */
    static class NullSecurityConstraint implements SecurityConstraint {

        /** The Constant NO_ROLES. */
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

    /**
     * The Class SecurityConstraintImpl.
     */
    static class SecurityConstraintImpl implements SecurityConstraint {

        /**
         * Instantiates a new security constraint impl.
         *
         * @param root
         *            the root
         *
         * @throws SAXException
         *             the SAX exception
         */
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

        /** The roles. */
        private String[] _roles;

        /** The role list. */
        private List<String> _roleList = new ArrayList<>();

        /** The resources. */
        private List<WebResourceCollection> _resources = new ArrayList<>();

        /**
         * Gets the matching collection.
         *
         * @param urlPath
         *            the url path
         *
         * @return the matching collection
         */
        public WebResourceCollection getMatchingCollection(String urlPath) {
            for (WebResourceCollection wrc : _resources) {
                if (wrc.controlsPath(urlPath)) {
                    return wrc;
                }
            }
            return null;
        }

        /**
         * The Class WebResourceCollection.
         */
        class WebResourceCollection {

            /**
             * Instantiates a new web resource collection.
             *
             * @param root
             *            the root
             *
             * @throws SAXException
             *             the SAX exception
             */
            WebResourceCollection(Element root) throws SAXException {
                final NodeList urlPatterns = root.getElementsByTagName("url-pattern");
                for (int i = 0; i < urlPatterns.getLength(); i++) {
                    _urlPatterns.add(XMLUtils.getTextValue(urlPatterns.item(i)));
                }
            }

            /**
             * Controls path.
             *
             * @param urlPath
             *            the url path
             *
             * @return true, if successful
             */
            boolean controlsPath(String urlPath) {
                for (String pattern : _urlPatterns) {
                    if (patternMatches(pattern, urlPath)) {
                        return true;
                    }
                }
                return false;
            }

            /** The url patterns. */
            private List<String> _urlPatterns = new ArrayList<>();
        }
    }

    /** The Constant NO_FILTERS. */
    static final FilterMetaData[] NO_FILTERS = {};

    /**
     * The Class ServletRequestImpl.
     */
    static class ServletRequestImpl implements ServletMetaData {

        /** The url. */
        private URL _url;

        /** The full servlet path. */
        private String _fullServletPath;

        /** The mapping. */
        private WebResourceMapping _mapping;

        /** The filters per name. */
        private Properties _filtersPerName;

        /** The filters per url. */
        private FilterUrlMap _filtersPerUrl;

        /**
         * Instantiates a new servlet request impl.
         *
         * @param url
         *            the url
         * @param servletPath
         *            the servlet path
         * @param mapping
         *            the mapping
         * @param filtersPerName
         *            the filters per name
         * @param filtersPerUrl
         *            the filters per url
         */
        ServletRequestImpl(URL url, String servletPath, WebResourceMapping mapping, Properties filtersPerName,
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

        /**
         * Adds the filters for path.
         *
         * @param filters
         *            the filters
         * @param fullServletPath
         *            the full servlet path
         */
        private void addFiltersForPath(List<FilterMetaData> filters, String fullServletPath) {
            FilterMetaData[] matches = _filtersPerUrl.getMatchingFilters(fullServletPath);
            Collections.addAll(filters, matches);
        }

        /**
         * Adds the filters for servlet with name.
         *
         * @param filters
         *            the filters
         * @param servletName
         *            the servlet name
         */
        private void addFiltersForServletWithName(List<FilterMetaData> filters, String servletName) {
            if (servletName == null) {
                return;
            }
            List<FilterMetaData> matches = (List<FilterMetaData>) _filtersPerName.get(servletName);
            if (matches != null) {
                filters.addAll(matches);
            }
        }

        /**
         * Gets the configuration.
         *
         * @return the configuration
         */
        private ServletConfiguration getConfiguration() {
            return _mapping == null ? null : (ServletConfiguration) _mapping.getConfiguration();
        }
    }

    /**
     * mapping for WebResources.
     */
    static class WebResourceMapping {

        /** The configuration. */
        private WebResourceConfiguration _configuration;

        /**
         * Gets the configuration.
         *
         * @return the configuration
         */
        WebResourceConfiguration getConfiguration() {
            return _configuration;
        }

        /**
         * Instantiates a new web resource mapping.
         *
         * @param configuration
         *            the configuration
         */
        WebResourceMapping(WebResourceConfiguration configuration) {
            _configuration = configuration;
        }

        /**
         * Returns the portion of the request path which was actually used to select the servlet. This default
         * implementation returns the full specified path.
         *
         * @param requestPath
         *            the full path of the request, relative to the application root.
         *
         * @return the servlet path
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
         *
         * @return the path info
         */
        String getPathInfo(String requestPath) {
            return null;
        }

        /**
         * Destroy resource.
         */
        public void destroyResource() {
            getConfiguration().destroyResource();
        }
    }

    /**
     * The Class PartialMatchWebResourceMapping.
     */
    static class PartialMatchWebResourceMapping extends WebResourceMapping {

        /** The prefix. */
        private String _prefix;

        /**
         * Instantiates a new partial match web resource mapping.
         *
         * @param configuration
         *            the configuration
         * @param prefix
         *            the prefix
         */
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

        /** The exact matches. */
        private final Map _exactMatches = new HashMap<>();

        /** The extensions. */
        private final Map _extensions = new HashMap<>();

        /** The url tree. */
        private final Map _urlTree = new HashMap<>();

        /** The default mapping. */
        private WebResourceMapping _defaultMapping;

        /**
         * Put.
         *
         * @param mapping
         *            the mapping
         * @param configuration
         *            the configuration
         */
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

        /**
         * Gets the.
         *
         * @param url
         *            the url
         *
         * @return the servlet meta data
         */
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

        /**
         * Gets the servlet path.
         *
         * @param urlFile
         *            the url file
         *
         * @return the servlet path
         */
        private String getServletPath(String urlFile) {
            if (urlFile.indexOf('?') < 0) {
                return urlFile;
            }
            return urlFile.substring(0, urlFile.indexOf('?'));
        }

        /**
         * Destroy web resources.
         */
        public void destroyWebResources() {
            if (_defaultMapping != null) {
                _defaultMapping.destroyResource();
            }
            destroyWebResources(_exactMatches);
            destroyWebResources(_extensions);
            destroyWebResources(_urlTree);
        }

        /**
         * Destroy web resources.
         *
         * @param map
         *            the map
         */
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

        /**
         * Auto load servlets.
         */
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

        /**
         * Collect auto loadable servlets.
         *
         * @param collection
         *            the collection
         * @param map
         *            the map
         */
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

        /**
         * Gets the mapping.
         *
         * @param url
         *            the url
         *
         * @return the mapping
         */
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

        /**
         * Gets the context for longest path prefix.
         *
         * @param url
         *            the url
         *
         * @return the context for longest path prefix
         */
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

        /**
         * Gets the extension.
         *
         * @param url
         *            the url
         *
         * @return the extension
         */
        private String getExtension(String url) {
            int index = url.lastIndexOf('.');
            if (index == -1 || index >= url.length() - 1) {
                return "";
            }
            return url.substring(index + 1);
        }

    }

    /**
     * return the given ServletConfiguration for the given servlet name.
     *
     * @param servletName
     *            the servlet name
     *
     * @return the corresponding ServletConfiguration
     */
    public ServletConfiguration getServletByName(String servletName) {
        return (ServletConfiguration) _servlets.get(servletName);
    }

}

/**
 * A utility class for parsing URLs into paths
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

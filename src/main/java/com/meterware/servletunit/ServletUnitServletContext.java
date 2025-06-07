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

import jakarta.servlet.*;
import jakarta.servlet.ServletRegistration.Dynamic;
import jakarta.servlet.descriptor.JspConfigDescriptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * This class is a private implementation of the ServletContext class.
 **/
public class ServletUnitServletContext implements ServletContext {

    private PrintStream _logStream = System.out;

    ServletUnitServletContext(WebApplication application) {
        _application = application;
    }

    /**
     * Returns a ServletContext object that corresponds to a specified URL on the server.
     * <p>
     * This method allows servlets to gain access to the context for various parts of the server, and as needed obtain
     * RequestDispatcher objects from the context. The given path must be absolute (beginning with "/") and is
     * interpreted based on the server's document root.
     * <p>
     * In a security conscious environment, the servlet container may return null for a given URL.
     **/
    @Override
    public jakarta.servlet.ServletContext getContext(java.lang.String A) {
        return null;
    }

    /**
     * Returns the major version of the Java Servlet API that this servlet container supports. All implementations that
     * comply with Version 2.4 must have this method return the integer 2.
     **/
    @Override
    public int getMajorVersion() {
        return 4;
    }

    /**
     * Returns the minor version of the Servlet API that this servlet container supports. All implementations that
     * comply with Version 2.4 must have this method return the integer 4.
     **/
    @Override
    public int getMinorVersion() {
        return 0;
    }

    /**
     * Returns the MIME type of the specified file, or null if the MIME type is not known. The MIME type is determined
     * by the configuration of the servlet container, and may be specified in a web application deployment descriptor.
     * Common MIME types are "text/html" and "image/gif".
     **/
    @Override
    public java.lang.String getMimeType(String filePath) {
        return URLConnection.getFileNameMap().getContentTypeFor(filePath);
    }

    /**
     * Returns a URL to the resource that is mapped to a specified path. The path must begin with a "/" and is
     * interpreted as relative to the current context root.
     * <p>
     * This method allows the servlet container to make a resource available to servlets from any source. Resources can
     * be located on a local or remote file system, in a database, or in a .war file.
     * <p>
     * The servlet container must implement the URL handlers and URLConnection objects that are necessary to access the
     * resource.
     * <p>
     * This method returns null if no resource is mapped to the pathname. Some containers may allow writing to the URL
     * returned by this method using the methods of the URL class. The resource content is returned directly, so be
     * aware that requesting a .jsp page returns the JSP source code. Use a RequestDispatcher instead to include results
     * of an execution. This method has a different purpose than java.lang.Class.getResource, which looks up resources
     * based on a class loader. This method does not use class loaders.
     **/
    @Override
    public java.net.URL getResource(String path) {
        try {
            File resourceFile = _application.getResourceFile(path);
            // PATCH proposal [ 1592532 ] Invalid
            // ServletUnitServletContext#getResource(String path)
            // by Timo Westkemper
            // return !resourceFile.exists() ? null : resourceFile.toURL();
            //
            // state of code until 2014-02 - before proposal of Aki Yoshida
            // return resourceFile == null ? null : resourceFile.toURL();

            return resourceFile == null || !resourceFile.exists() ? null : resourceFile.toURI().toURL();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * Returns the resource located at the named path as an InputStream object. The data in the InputStream can be of
     * any type or length. The path must be specified according to the rules given in getResource. This method returns
     * null if no resource exists at the specified path. Meta-information such as content length and content type that
     * is available via getResource method is lost when using this method. The servlet container must implement the URL
     * handlers and URLConnection objects necessary to access the resource. This method is different from
     * java.lang.Class.getResourceAsStream, which uses a class loader. This method allows servlet containers to make a
     * resource available to a servlet from any location, without using a class loader.
     **/
    @Override
    public java.io.InputStream getResourceAsStream(String path) {
        try {
            File resourceFile = _application.getResourceFile(path);
            return resourceFile == null ? null : new FileInputStream(resourceFile);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    /**
     * Returns a RequestDispatcher object that acts as a wrapper for the resource located at the given path. A
     * RequestDispatcher object can be used to forward a request to the resource or to include the resource in a
     * response. The resource can be dynamic or static. The pathname must begin with a "/" and is interpreted as
     * relative to the current context root. Use getContext to obtain a RequestDispatcher for resources in foreign
     * contexts. This method returns null if the ServletContext cannot return a RequestDispatcher.
     **/
    @Override
    public jakarta.servlet.RequestDispatcher getRequestDispatcher(String path) {
        try {
            URL url = new URL("http", "localhost", _application.getContextPath() + path);
            return new RequestDispatcherImpl(_application, url);
        } catch (ServletException | MalformedURLException e) {
            return null;
        }
    }

    /**
     * Returns a RequestDispatcher object that acts as a wrapper for the named servlet. Servlets (and JSP pages also)
     * may be given names via server administration or via a web application deployment descriptor. A servlet instance
     * can determine its name using ServletConfig.getServletName(). This method returns null if the ServletContext
     * cannot return a RequestDispatcher for any reason. patch by Izzy Alanis
     *
     * @param servletName
     *            - the name of the dispatcher to get
     **/
    @Override
    public jakarta.servlet.RequestDispatcher getNamedDispatcher(java.lang.String servletName) {
        final WebApplication.ServletConfiguration servletConfig = _application.getServletByName(servletName);
        if (servletConfig == null) {
            return null;
        }

        Servlet tempServlet;
        Exception tempException;

        try {
            tempServlet = servletConfig.getServlet();
            tempException = null;
        } catch (Exception e) {
            tempServlet = null;
            tempException = e;
        }

        final Servlet servlet = tempServlet;

        final Exception instantiationException = tempException;

        return new jakarta.servlet.RequestDispatcher() {

            @Override
            public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {

                if (instantiationException != null) {

                    if (instantiationException instanceof ServletException) {
                        throw (ServletException) instantiationException;

                    }
                    ServletException e = new ServletException(instantiationException.getMessage());

                    e.initCause(instantiationException);

                    throw e;

                }

                if (servletConfig.getJspFile() != null) {
                    request.setAttribute("org.apache.catalina.jsp_file", servletConfig.getJspFile());
                }
                response.reset();
                servlet.service(request, response);
            }

            @Override
            public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
                if (instantiationException != null) {
                    if (instantiationException instanceof ServletException) {
                        throw (ServletException) instantiationException;
                    }
                    ServletException e = new ServletException(instantiationException.getMessage());
                    e.initCause(instantiationException);
                    throw e;
                }
                if (servletConfig.getJspFile() != null) {
                    request.setAttribute("org.apache.catalina.jsp_file", servletConfig.getJspFile());
                }
                servlet.service(request, response);
            }
        };
    }

    /**
     * Writes the specified message to a servlet log file, usually an event log. The name and type of the servlet log
     * file is specific to the servlet container.
     **/
    @Override
    public void log(String message) {
        _logStream.println(message);
    }

    /**
     * Writes an explanatory message and a stack trace for a given Throwable exception to the servlet log file. The name
     * and type of the servlet log file is specific to the servlet container, usually an event log.
     **/
    @Override
    public void log(String message, Throwable t) {
        _logStream.print(message);
        _logStream.print(":");
        if (t != null) {
            t.printStackTrace(_logStream);
        }
    }

    /**
     * Returns a String containing the real path for a given virtual path. For example, the virtual path "/index.html"
     * has a real path of whatever file on the server's filesystem would be served by a request for "/index.html". The
     * real path returned will be in a form appropriate to the computer and operating system on which the servlet
     * container is running, including the proper path separators. This method returns null if the servlet container
     * cannot translate the virtual path to a real path for any reason (such as when the content is being made available
     * from a .war archive).
     **/
    @Override
    public String getRealPath(String path) {
        return _application.getResourceFile(path).getAbsolutePath();
    }

    public static final String DEFAULT_SERVER_INFO = "ServletUnit test framework";

    /**
     * Returns the name and version of the servlet container on which the servlet is running. The form of the returned
     * string is servername/versionnumber. For example, the JavaServer Web Development Kit may return the string
     * JavaServer Web Dev Kit/1.0. The servlet container may return other optional information after the primary string
     * in parentheses, for example, JavaServer Web Dev Kit/1.0 (JDK 1.1.6; Windows NT 4.0 x86).
     **/
    @Override
    public String getServerInfo() {
        return DEFAULT_SERVER_INFO;
    }

    /**
     * Returns a String containing the value of the named context-wide initialization parameter, or null if the
     * parameter does not exist. This method can make available configuration information useful to an entire "web
     * application". For example, it can provide a webmaster's email address or the name of a system that holds critical
     * data.
     **/
    @Override
    public String getInitParameter(String name) {
        return (String) getContextParams().get(name);
    }

    /**
     * Returns the names of the context's initialization parameters as an Enumeration of String objects, or an empty
     * Enumeration if the context has no initialization parameters.
     **/
    @Override
    public Enumeration<String> getInitParameterNames() {
        return getContextParams().keys();
    }

    /**
     * Returns the servlet container attribute with the given name, or null if there is no attribute by that name. An
     * attribute allows a servlet container to give the servlet additional information not already provided by this
     * interface. See your server documentation for information about its attributes. A list of supported attributes can
     * be retrieved using getAttributeNames.
     **/
    @Override
    public Object getAttribute(String name) {
        return _attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return _attributes.keys();
    }

    @Override
    public void setAttribute(String name, Object attribute) {
        if (!_attributes.containsKey(name)) {
            _attributes.put(name, attribute);
            _application.sendAttributeAdded(name, attribute);
        } else {
            Object oldValue = _attributes.get(name);
            _attributes.put(name, attribute);
            _application.sendAttributeReplaced(name, oldValue);
        }
    }

    @Override
    public void removeAttribute(String name) {
        Object oldValue = _attributes.get(name);
        _attributes.remove(name);
        _application.sendAttributeRemoved(name, oldValue);
    }

    // ----------------------------- methods added to ServletContext in JSDK 2.3
    // --------------------------------------

    /**
     * Returns a directory-like listing of all the paths to resources within the web application whose longest sub-path
     * matches the supplied path argument. Paths indicating subdirectory paths end with a '/'. The returned paths are
     * all relative to the root of the web application and have a leading '/'. For example, for a web application
     * containing &lt;p&gt; /welcome.html&lt;br /&gt; /catalog/index.html&lt;br /&gt; &lt;br /&gt;
     * /catalog/products.html&lt;br /&gt; /catalog/offers/books.html&lt;br /&gt; /catalog/offers/music.html&lt;br /&gt;
     * /customer/login.jsp&lt;br /&gt; /WEB-INF/web.xml&lt;br /&gt; /WEB-INF/classes/com.acme.OrderServlet.class,&lt;br
     * /&gt; &lt;br /&gt; getResourcePaths("/") returns {"/welcome.html", "/catalog/", "/customer/", "/WEB-INF/"}&lt;br
     * /&gt; getResourcePaths("/catalog/") returns {"/catalog/index.html", "/catalog/products.html",
     * "/catalog/offers/"}.
     *
     * @param path
     *            partial path used to match the resources, which must start with a /
     *
     * @return a Set containing the directory listing, or null if there are no resources in the web application whose
     *         path begins with the supplied path.
     *
     * @since HttpUnit 1.3
     */
    @Override
    public Set<String> getResourcePaths(String path) {
        return null;
    }

    /**
     * Returns the name of this web application correponding to this ServletContext as specified in the deployment
     * descriptor for this web application by the display-name element.
     *
     * @return The name of the web application or null if no name has been declared in the deployment descriptor
     *
     * @since HttpUnit 1.3
     */
    @Override
    public String getServletContextName() {
        return _application.getDisplayName();
    }

    // -------------------------------------- servlet-api 2.5 additions
    // -----------------------------------------------

    @Override
    public String getContextPath() {
        return null;
    }

    // ------------------------------------------- package members
    // ----------------------------------------------------

    void setInitParameter(String name, Object initParameter) {
        getContextParams().put(name, initParameter);
    }

    void removeInitParameter(String name) {
        getContextParams().remove(name);
    }

    // ------------------------------------------- private members
    // ----------------------------------------------------

    private Hashtable _attributes = new Hashtable<>();

    private WebApplication _application;

    private Hashtable getContextParams() {
        return _application.getContextParameters();
    }

    /**
     * Allows the test to determine where the log messages should be written. Defaults to {@link System#out}
     *
     * @param logStream
     *            where to write the log messages
     *
     * @see #log(String)
     */
    public void setLogStream(PrintStream logStream) {
        this._logStream = logStream;
    }

    @Override
    public int getEffectiveMajorVersion() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getEffectiveMinorVersion() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        getContextParams().put(name, value);
        return true;
    }

    @Override
    public Dynamic addServlet(String servletName, String className) {
        return null;
    }

    @Override
    public Dynamic addServlet(String servletName, Servlet servlet) {
        return null;
    }

    @Override
    public Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        return null;
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
        return null;
    }

    @Override
    public ServletRegistration getServletRegistration(String servletName) {
        return null;
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return null;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        return null;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        return null;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        return null;
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
        return null;
    }

    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        return null;
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return null;
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        return null;
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {

    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return null;
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return null;
    }

    @Override
    public void addListener(String className) {

    }

    @Override
    public <T extends EventListener> void addListener(T t) {

    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {

    }

    @Override
    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
        return null;
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return ServletUnitServletContext.class.getClassLoader();
    }

    @Override
    public void declareRoles(String... roleNames) {

    }

    @Override
    public String getVirtualServerName() {
        return null;
    }

    @Override
    public Dynamic addJspFile(String servletName, String jspFile) {
        return null;
    }

    @Override
    public int getSessionTimeout() {
        return 0;
    }

    @Override
    public void setSessionTimeout(int sessionTimeout) {

    }

    @Override
    public String getRequestCharacterEncoding() {
        return null;
    }

    @Override
    public void setRequestCharacterEncoding(String encoding) {

    }

    @Override
    public String getResponseCharacterEncoding() {
        return null;
    }

    @Override
    public void setResponseCharacterEncoding(String encoding) {

    }
}

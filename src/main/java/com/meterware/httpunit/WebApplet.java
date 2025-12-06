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
package com.meterware.httpunit;

import com.meterware.httpunit.scripting.ScriptableDelegate;

import java.applet.Applet;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLAppletElement;
import org.xml.sax.SAXException;

/**
 * This class represents the embedding of an applet in a web page.
 **/
public class WebApplet extends HTMLElementBase {

    /** The response. */
    private WebResponse _response;

    /** The base target. */
    private String _baseTarget;

    /** The applet. */
    private Applet _applet;

    /** The parameters. */
    private HashMap _parameters;

    /** The parameter names. */
    private String[] _parameterNames;

    /** The class extension. */
    private final String CLASS_EXTENSION = ".class";

    /** The element. */
    private HTMLAppletElement _element;

    /**
     * Instantiates a new web applet.
     *
     * @param response
     *            the response
     * @param element
     *            the element
     * @param baseTarget
     *            the base target
     */
    public WebApplet(WebResponse response, HTMLAppletElement element, String baseTarget) {
        super(element);
        _element = element;
        _response = response;
        _baseTarget = baseTarget;
    }

    /**
     * Returns the URL of the codebase used to find the applet classes.
     *
     * @return the code base URL
     *
     * @throws MalformedURLException
     *             the malformed URL exception
     */
    public URL getCodeBaseURL() throws MalformedURLException {
        return new URL(_response.getURL(), getCodeBase());
    }

    /**
     * Gets the code base.
     *
     * @return the code base
     */
    private String getCodeBase() {
        final String codeBaseAttribute = _element.getCodeBase();
        return codeBaseAttribute.endsWith("/") ? codeBaseAttribute : codeBaseAttribute + "/";
    }

    /**
     * Returns the name of the applet main class.
     *
     * @return the main class name
     */
    public String getMainClassName() {
        String className = _element.getCode();
        if (className.endsWith(CLASS_EXTENSION)) {
            className = className.substring(0, className.lastIndexOf(CLASS_EXTENSION));
        }
        return className.replace('/', '.').replace('\\', '.');
    }

    /**
     * Returns the width of the panel in which the applet will be drawn.
     *
     * @return the width
     */
    public int getWidth() {
        return Integer.parseInt(getAttribute("width"));
    }

    /**
     * Returns the height of the panel in which the applet will be drawn.
     *
     * @return the height
     */
    public int getHeight() {
        return Integer.parseInt(getAttribute("height"));
    }

    /**
     * Returns the archive specification.
     *
     * @return the archive specification
     */
    public String getArchiveSpecification() {
        String specification = getParameter("archive");
        if (specification == null) {
            specification = getAttribute("archive");
        }
        return specification;
    }

    /**
     * Gets the archive list.
     *
     * @return the archive list
     *
     * @throws MalformedURLException
     *             the malformed URL exception
     */
    List getArchiveList() throws MalformedURLException {
        ArrayList al = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(getArchiveSpecification(), ",");
        while (st.hasMoreTokens()) {
            al.add(new URL(getCodeBaseURL(), st.nextToken()));
        }
        return al;
    }

    /**
     * Returns an array containing the names of the parameters defined for the applet.
     *
     * @return the parameter names
     */
    public String[] getParameterNames() {
        if (_parameterNames == null) {
            ArrayList al = new ArrayList(getParameterMap().keySet());
            _parameterNames = (String[]) al.toArray(new String[al.size()]);
        }
        return _parameterNames;
    }

    /**
     * Returns the value of the specified applet parameter, or null if not defined.
     *
     * @param name
     *            the name
     *
     * @return the parameter
     */
    public String getParameter(String name) {
        return (String) getParameterMap().get(name);
    }

    /**
     * Gets the parameter map.
     *
     * @return the parameter map
     */
    private Map getParameterMap() {
        if (_parameters == null) {
            _parameters = new HashMap<>();
            NodeList nl = ((Element) getNode()).getElementsByTagName("param");
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                _parameters.put(NodeUtils.getNodeAttribute(n, "name", ""), NodeUtils.getNodeAttribute(n, "value", ""));
            }
        }
        return _parameters;
    }

    /**
     * Gets the applet.
     *
     * @return the applet
     *
     * @throws MalformedURLException
     *             the malformed URL exception
     * @throws ClassNotFoundException
     *             the class not found exception
     * @throws InstantiationException
     *             the instantiation exception
     * @throws IllegalAccessException
     *             the illegal access exception
     * @throws IllegalArgumentException
     *             the illegal argument exception
     * @throws InvocationTargetException
     *             the invocation target exception
     * @throws NoSuchMethodException
     *             the no such method exception
     * @throws SecurityException
     *             the security exception
     */
    public Applet getApplet()
            throws MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        if (_applet == null) {
            ClassLoader cl = new URLClassLoader(getClassPath(), null);
            Object o = cl.loadClass(getMainClassName()).getDeclaredConstructor().newInstance();
            if (!(o instanceof Applet)) {
                throw new RuntimeException(getMainClassName() + " is not an Applet");
            }
            _applet = (Applet) o;
            _applet.setStub(new AppletStubImpl(this));
        }
        return _applet;
    }

    /**
     * Gets the class path.
     *
     * @return the class path
     *
     * @throws MalformedURLException
     *             the malformed URL exception
     */
    private URL[] getClassPath() throws MalformedURLException {
        List classPath = getArchiveList();
        classPath.add(getCodeBaseURL());
        return (URL[]) classPath.toArray(new URL[classPath.size()]);
    }

    /**
     * Gets the base target.
     *
     * @return the base target
     */
    String getBaseTarget() {
        return _baseTarget;
    }

    /**
     * Gets the applets in page.
     *
     * @return the applets in page
     */
    WebApplet[] getAppletsInPage() {
        try {
            return _response.getApplets();
        } catch (SAXException e) {
            HttpUnitUtils.handleException(e); // should never happen.
            return null;
        }
    }

    /**
     * Send request.
     *
     * @param url
     *            the url
     * @param target
     *            the target
     */
    void sendRequest(URL url, String target) {
        WebRequest wr = new GetMethodWebRequest(null, url.toExternalForm(), target);
        try {
            _response.getWindow().getResponse(wr);
        } catch (IOException e) {
            e.printStackTrace(); // To change body of catch statement use Options | File Templates.
            throw new RuntimeException(e.toString());
        } catch (SAXException e) {
        }
    }

    @Override
    public ScriptableDelegate newScriptable() {
        return new HTMLElementScriptable(this);
    }

    @Override
    public ScriptableDelegate getParentDelegate() {
        return _response.getDocumentScriptable();
    }

}

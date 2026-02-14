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
package com.meterware.httpunit;

import com.meterware.httpunit.scripting.ScriptableDelegate;
import com.meterware.httpunit.scripting.ScriptingHandler;

import java.io.IOException;
import java.net.URL;
import java.util.StringTokenizer;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Base class for objects which can be clicked to generate new web requests.
 */
public abstract class WebRequestSource extends ParameterHolder implements HTMLElement {

    /** The frame. */
    private FrameSelector _frame;

    /**
     * The name of the destination attribute used to create for the request, including anchors and parameters. *
     */
    private String _destinationAttribute;

    /** The scriptable. */
    private ScriptingHandler _scriptable;

    /**
     * Returns the ID associated with this request source.
     */
    @Override
    public String getID() {
        return getAttribute("id");
    }

    /**
     * Returns the class associated with this request source.
     */
    @Override
    public String getClassName() {
        return getAttribute("class");
    }

    /**
     * Returns the name associated with this request source.
     */
    @Override
    public String getName() {
        return getAttribute("name");
    }

    /**
     * Returns the title associated with this request source.
     */
    @Override
    public String getTitle() {
        return getAttribute("title");
    }

    /**
     * Returns the target for this request source.
     *
     * @return the target
     */
    public String getTarget() {
        if (getSpecifiedTarget().isEmpty()) {
            return _defaultTarget;
        }
        return getSpecifiedTarget();
    }

    /**
     * Returns the name of the frame containing this request source.
     *
     * @return the page frame
     *
     * @deprecated as of 1.6, use #getFrame
     */
    @Deprecated
    public String getPageFrame() {
        return _frame.getName();
    }

    /**
     * Returns the frame containing this request source.
     *
     * @return the frame
     */
    public FrameSelector getFrame() {
        return _frame;
    }

    /**
     * Returns the fragment identifier for this request source, used to identifier an element within an HTML document.
     *
     * @return the fragment identifier
     */
    public String getFragmentIdentifier() {
        final int hashIndex = getDestination().indexOf('#');
        if (hashIndex < 0) {
            return "";
        }
        return getDestination().substring(hashIndex + 1);
    }

    /**
     * Returns a copy of the domain object model subtree associated with this entity.
     *
     * @return the DOM subtree
     */
    public Node getDOMSubtree() {
        return _node.cloneNode( /* deep */true);
    }

    /**
     * Creates and returns a web request from this request source.
     *
     * @return the request
     */
    public abstract WebRequest getRequest();

    /**
     * Returns an array containing the names of any parameters to be sent on a request based on this request source.
     */
    @Override
    public abstract String[] getParameterNames();

    /**
     * Returns the values of the named parameter.
     */
    @Override
    public abstract String[] getParameterValues(String name);

    /**
     * Returns the URL relative to the current page which will handle the request.
     *
     * @return the relative page
     */
    String getRelativePage() {
        final String url = getRelativeURL();
        if (HttpUnitUtils.isJavaScriptURL(url)) {
            return url;
        }
        final int questionMarkIndex = url.indexOf("?");
        if (questionMarkIndex >= 1 && questionMarkIndex < url.length() - 1) {
            return url.substring(0, questionMarkIndex);
        }
        return url;
    }

    /**
     * get the relative URL for a weblink change spaces to %20.
     *
     * @return the relative URL as a string
     */
    protected String getRelativeURL() {
        String result = HttpUnitUtils.encodeSpaces(HttpUnitUtils.trimFragment(getDestination()));
        if (result.trim().isEmpty()) {
            result = getBaseURL().getFile();
        }
        return result;
    }

    // ----------------------------- protected members
    // ---------------------------------------------

    /**
     * Contructs a web request source.
     *
     * @param response
     *            the response from which this request source was extracted
     * @param node
     *            the DOM subtree defining this request source
     * @param baseURL
     *            the URL on which to base all releative URL requests
     * @param attribute
     *            the attribute which defines the relative URL to which requests will be directed
     * @param frame
     *            the frame
     * @param defaultTarget
     *            the default target
     */
    WebRequestSource(WebResponse response, Node node, URL baseURL, String attribute, FrameSelector frame,
            String defaultTarget) {
        if (node == null) {
            throw new IllegalArgumentException("node must not be null");
        }
        _baseResponse = response;
        _node = node;
        _baseURL = baseURL;
        _destinationAttribute = attribute;
        _frame = frame;
        _defaultTarget = defaultTarget;
    }

    /**
     * Gets the base URL.
     *
     * @return the base URL
     */
    protected URL getBaseURL() {
        return _baseURL;
    }

    /**
     * get the Destination made public per FR 2836664 make WebRequestSource.getDestination() public by Dan Lipofsky
     *
     * @return the destination
     */
    public String getDestination() {
        return getElement().getAttribute(_destinationAttribute);
    }

    /**
     * Sets the destination.
     *
     * @param destination
     *            the new destination
     */
    protected void setDestination(String destination) {
        getElement().setAttribute(_destinationAttribute, destination);
    }

    /**
     * Returns the actual DOM for this request source, not a copy.
     *
     * @return the element
     */
    protected Element getElement() {
        return (Element) _node;
    }

    /**
     * Returns the HTMLPage associated with this request source.
     *
     * @return the HTML page
     *
     * @throws SAXException
     *             the SAX exception
     */
    protected HTMLPage getHTMLPage() throws SAXException {
        return _baseResponse.getReceivedPage();
    }

    /**
     * Extracts any parameters specified as part of the destination URL, calling addPresetParameter for each one in the
     * order in which they are found.
     */
    protected final void loadDestinationParameters() {
        StringTokenizer st = new StringTokenizer(getParametersString(), PARAM_DELIM);
        while (st.hasMoreTokens()) {
            stripOneParameter(st.nextToken());
        }
    }

    /**
     * submit the given event for the given request.
     *
     * @param event
     *            the event
     * @param request
     *            the request
     *
     * @return the response for the submitted Request
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws SAXException
     *             the SAX exception
     */
    protected WebResponse submitRequest(String event, final WebRequest request) throws IOException, SAXException {
        WebResponse response = null;
        if (doEventScript(event)) {
            response = submitRequest(request);
        }
        if (response == null) {
            response = getCurrentFrameContents();
        }
        return response;
    }

    /**
     * handle the event that has the given script attached by compiling the eventScript as a function and executing it
     *
     * @param eventScript
     *            - the script to use
     *
     * @deprecated since 1.7 - use doEventScript instead
     */
    @Deprecated
    @Override
    public boolean doEvent(String eventScript) {
        return doEventScript(eventScript);
    }

    /**
     * optional do the event if it's defined
     *
     * @param eventScript
     *            - the script to handle
     *
     * @return whether the script was handled
     */
    @Override
    public boolean doEventScript(String eventScript) {
        return this.getScriptingHandler().doEventScript(eventScript);
    }

    @Override
    public boolean handleEvent(String eventName) {
        return this.getScriptingHandler().handleEvent(eventName);
    }

    /**
     * Gets the current frame contents.
     *
     * @return the current frame contents
     */
    protected WebResponse getCurrentFrameContents() {
        return getCurrentFrame(getBaseResponse().getWindow(), _frame);
    }

    /**
     * Gets the current frame.
     *
     * @param window
     *            the window
     * @param pageFrame
     *            the page frame
     *
     * @return the current frame
     */
    private WebResponse getCurrentFrame(WebWindow window, FrameSelector pageFrame) {
        return window.hasFrame(pageFrame) ? window.getFrameContents(pageFrame) : window.getCurrentPage();
    }

    /**
     * Submits a request to the web client from which this request source was originally obtained.
     *
     * @param request
     *            the request
     *
     * @return the web response
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws SAXException
     *             the SAX exception
     */
    protected final WebResponse submitRequest(WebRequest request) throws IOException, SAXException {
        return getDestination().equals("#") ? _baseResponse : _baseResponse.getWindow().sendRequest(request);
    }

    /**
     * Returns the web response containing this request source.
     *
     * @return the base response
     */
    protected final WebResponse getBaseResponse() {
        return _baseResponse;
    }

    /**
     * Records a parameter defined by including it in the destination URL. The value can be null, if the parameter name
     * was not specified with an equals sign.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     */
    abstract protected void addPresetParameter(String name, String value);

    /**
     * get the attribute value for the given name
     *
     * @param name
     *            - the name of the attribute to get
     */
    @Override
    public String getAttribute(final String name) {
        return NodeUtils.getNodeAttribute(_node, name);
    }

    /**
     * set the attribute with the given name to the given value
     *
     * @param name
     *            - the name of the attribute
     * @param value
     *            - the value to use
     */
    @Override
    public void setAttribute(final String name, final Object value) {
        NodeUtils.setNodeAttribute(getNode(), name, value == null ? null : value.toString());
    }

    /**
     * remove the given attribute
     *
     * @param name
     *            - the name of the attribute to remove
     */
    @Override
    public void removeAttribute(final String name) {
        NodeUtils.removeNodeAttribute(getNode(), name);
    }

    @Override
    public boolean isSupportedAttribute(String name) {
        return false;
    }

    @Override
    public Node getNode() {
        return _node;
    }

    /**
     * Returns the text value of this block.
     */
    @Override
    public String getText() {
        if (_node == null) {
            return "";
        }
        if (_node.getNodeType() == Node.TEXT_NODE) {
            return _node.getNodeValue().trim();
        }
        if (!_node.hasChildNodes()) {
            return "";
        }
        return NodeUtils.asText(_node.getChildNodes()).trim();
    }

    @Override
    public String getTagName() {
        return _node.getNodeName();
    }

    /**
     * Gets the attribute.
     *
     * @param name
     *            the name
     * @param defaultValue
     *            the default value
     *
     * @return the attribute
     */
    String getAttribute(final String name, String defaultValue) {
        return NodeUtils.getNodeAttribute(_node, name, defaultValue);
    }

    // ----------------------------- private members
    // -----------------------------------------------

    /**
     * parameter Delimiter for URL parameters bug report [ 1052037 ] Semicolon not supported as URL param delimiter asks
     * for this to be extended to &;.
     *
     * @see http://www.w3.org/TR/html4/appendix/notes.html#h-B.2 section B2.2
     */
    private static final String PARAM_DELIM = "&";

    /** The web response containing this request source. * */
    private WebResponse _baseResponse;

    /**
     * The name of the frame in which the response containing this request source is rendered. *
     */
    private String _defaultTarget;

    /** The URL of the page containing this entity. * */
    private URL _baseURL;

    /** The DOM node representing this entity. * */
    private Node _node;

    /**
     * Gets the specified target.
     *
     * @return the specified target
     */
    private String getSpecifiedTarget() {
        return getAttribute("target");
    }

    /**
     * Sets the target attribute.
     *
     * @param value
     *            the new target attribute
     */
    protected void setTargetAttribute(String value) {
        ((Element) _node).setAttribute("target", value);
    }

    /**
     * Gets all parameters from a URL.
     *
     * @return the parameters string
     */
    private String getParametersString() {
        String url = HttpUnitUtils.trimFragment(getDestination());
        if (url.trim().isEmpty()) {
            url = getBaseURL().toExternalForm();
        }
        if (HttpUnitUtils.isJavaScriptURL(url)) {
            return "";
        }
        final int questionMarkIndex = url.indexOf("?");
        if (questionMarkIndex >= 1 && questionMarkIndex < url.length() - 1) {
            return url.substring(questionMarkIndex + 1);
        }
        return "";
    }

    /**
     * Extracts a parameter of the form <name>[=[<value>]].
     *
     * @param param
     *            the param
     */
    private void stripOneParameter(String param) {
        final int index = param.indexOf("=");
        String value = index < 0 ? null
                : index == param.length() - 1 ? getEmptyParameterValue() : decode(param.substring(index + 1));
        String name = index < 0 ? decode(param) : decode(param.substring(0, index));
        addPresetParameter(name, value);
    }

    /**
     * Decode.
     *
     * @param string
     *            the string
     *
     * @return the string
     */
    private String decode(String string) {
        return HttpUnitUtils.decode(string, _baseResponse.getCharacterSet()).trim();
    }

    /**
     * Gets the empty parameter value.
     *
     * @return the empty parameter value
     */
    abstract protected String getEmptyParameterValue();

    /**
     * Returns the scriptable delegate.
     */
    @Override
    public ScriptingHandler getScriptingHandler() {
        if (_scriptable == null) {
            _scriptable = HttpUnitOptions.getScriptingEngine().createHandler(this);
        }
        return _scriptable;
    }

    @Override
    public ScriptableDelegate getParentDelegate() {
        return getBaseResponse().getDocumentScriptable();
    }

}

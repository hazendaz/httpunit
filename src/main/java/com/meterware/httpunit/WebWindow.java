/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

import com.meterware.httpunit.scripting.ScriptingHandler;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.xml.sax.SAXException;

/**
 * A window managed by a {@link com.meterware.httpunit.WebClient WebClient}.
 **/
public class WebWindow {

    /** The client which created this window. **/
    private WebClient _client;

    /** A map of frame names to current contents. **/
    private FrameHolder _frameContents;

    /** The name of the window, set via JavaScript. **/
    private String _name = "";

    /** The web response containing the reference that opened this window *. */
    private WebResponse _opener;

    /** True if this window has been closed. **/
    private boolean _closed;

    /** The Constant NO_NAME. */
    static final String NO_NAME = "$$HttpUnit_Window$$_";

    /** The urls that have been encountered as redirect locations in the course of a single client-initiated request. */
    private final Map _redirects;

    /** True if seen initial request. */
    private boolean _isInitialRequest = true;

    /**
     * Cache the initial client request to ensure that the _redirects structure gets reset.
     */
    private WebRequest _initialRequest;

    /**
     * Returns the web client associated with this window.
     *
     * @return the client
     */
    public WebClient getClient() {
        return _client;
    }

    /**
     * Returns true if this window has been closed.
     *
     * @return true, if is closed
     */
    public boolean isClosed() {
        return _closed;
    }

    /**
     * Closes this window.
     */
    public void close() {
        if (!_closed) {
            _client.close(this);
        }
        _closed = true;
    }

    /**
     * Returns the name of this window. Windows created through normal HTML or browser commands have empty names, but
     * JavaScript can set the name. A name may be used as a target for a request.
     *
     * @return the name
     */
    public String getName() {
        return _name;
    }

    /**
     * Returns the web response that contained the script which opened this window.
     *
     * @return the opener
     */
    public WebResponse getOpener() {
        return _opener;
    }

    /**
     * Submits a GET method request and returns a response.
     *
     * @param urlString
     *            the url string
     *
     * @return the response
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     *
     * @exception SAXException
     *                thrown if there is an error parsing the retrieved page
     */
    public WebResponse getResponse(String urlString) throws IOException, SAXException {
        return getResponse(new GetMethodWebRequest(urlString));
    }

    /**
     * Submits a web request and returns a response. This is an alternate name for the getResponse method.
     *
     * @param request
     *            the request
     *
     * @return the WebResponse or null
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws SAXException
     *             the SAX exception
     */
    public WebResponse sendRequest(WebRequest request) throws IOException, SAXException {
        return getResponse(request);
    }

    /**
     * Submits a web request and returns a response, using all state developed so far as stored in cookies as requested
     * by the server. see patch [ 1155415 ] Handle redirect instructions which can lead to a loop
     *
     * @param request
     *            the request
     *
     * @return the WebResponse or null
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     *
     * @exception SAXException
     *                thrown if there is an error parsing the retrieved page
     */
    public WebResponse getResponse(WebRequest request) throws IOException, SAXException {
        // Need to have some sort of ExecuteAroundMethod to ensure that the
        // redirects data structure gets cleared down upon exit - not
        // straightforward, since this could be a recursive call
        if (_isInitialRequest) {
            _initialRequest = request;
            _isInitialRequest = false;
        }

        WebResponse result = null;

        try {
            final RequestContext requestContext = new RequestContext();
            final WebResponse response = getSubframeResponse(request, requestContext);
            requestContext.runScripts();
            // javascript might replace the response in its frame
            result = response == null ? null : response.getWindow().getFrameContents(response.getFrame());
        } finally {
            if (null != request && request.equals(_initialRequest)) {
                _redirects.clear();
                _initialRequest = null;
                _isInitialRequest = true;
            }
        }
        return result;
    }

    /**
     * get a Response from a SubFrame.
     *
     * @param request
     *            the request
     * @param requestContext
     *            the request context
     *
     * @return the WebResponse or null
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws SAXException
     *             the SAX exception
     */
    WebResponse getSubframeResponse(WebRequest request, RequestContext requestContext)
            throws IOException, SAXException {
        WebResponse response = getResource(request);

        return response == null ? null : updateWindow(request.getTarget(), response, requestContext);
    }

    /**
     * Updates this web client based on a received response. This includes updating cookies and frames.
     *
     * @param requestTarget
     *            the request target
     * @param response
     *            the response
     * @param requestContext
     *            the request context
     *
     * @return the web response
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws SAXException
     *             the SAX exception
     */
    WebResponse updateWindow(String requestTarget, WebResponse response, RequestContext requestContext)
            throws IOException, SAXException {
        _client.updateClient(response);
        if (getClient().getClientProperties().isAutoRefresh() && response.getRefreshRequest() != null) {
            WebRequest request = response.getRefreshRequest();
            return getResponse(request);
        }
        if (shouldFollowRedirect(response)) {
            delay(HttpUnitOptions.getRedirectDelay());
            return getResponse(new RedirectWebRequest(response));
        }
        _client.updateFrameContents(this, requestTarget, response, requestContext);
        return response;
    }

    /**
     * Returns the resource specified by the request. Does not update the window or load included framesets. May return
     * null if the resource is a JavaScript URL which would normally leave the client unchanged.
     *
     * @param request
     *            the request
     *
     * @return the resource
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public WebResponse getResource(WebRequest request) throws IOException {
        _client.tellListeners(request);

        WebResponse response = null;
        String urlString = request.getURLString().trim();
        FrameSelector targetFrame = _frameContents.getTargetFrame(request);
        if (urlString.startsWith("about:")) {
            response = new DefaultWebResponse(_client, targetFrame, null, "");
        } else if (!HttpUnitUtils.isJavaScriptURL(urlString)) {
            response = _client.createResponse(request, targetFrame);
        } else {
            ScriptingHandler handler = request.getSourceScriptingHandler();
            if (handler == null) {
                handler = getCurrentPage().getScriptingHandler();
            }
            Object result = handler.evaluateExpression(urlString);
            if (result != null) {
                response = new DefaultWebResponse(_client, targetFrame, request.getURL(), result.toString());
            }
        }

        if (response != null) {
            _client.tellListeners(response);
        }
        return response;
    }

    /**
     * Returns the name of the currently active frames.
     *
     * @return the frame names
     */
    public String[] getFrameNames() {
        final List<String> names = _frameContents.getActiveFrameNames();
        return names.toArray(new String[names.size()]);
    }

    /**
     * Returns true if the specified frame name is defined in this window.
     *
     * @param frameName
     *            the frame name
     *
     * @return true, if successful
     */
    public boolean hasFrame(String frameName) {
        return _frameContents.get(frameName) != null;
    }

    /**
     * Checks for frame.
     *
     * @param frame
     *            the frame
     *
     * @return true, if successful
     */
    boolean hasFrame(FrameSelector frame) {
        return _frameContents.get(frame) != null;
    }

    /**
     * Returns the response associated with the specified frame name. Throws a runtime exception if no matching frame is
     * defined.
     *
     * @param frameName
     *            the frame name
     *
     * @return the frame contents
     */
    public WebResponse getFrameContents(String frameName) {
        WebResponse response = _frameContents.get(frameName);
        if (response == null) {
            throw new NoSuchFrameException(frameName);
        }
        return response;
    }

    /**
     * Returns the response associated with the specified frame target. Throws a runtime exception if no matching frame
     * is defined.
     *
     * @param targetFrame
     *            the target frame
     *
     * @return the frame contents
     */
    WebResponse getFrameContents(FrameSelector targetFrame) {
        return _frameContents.getFrameContents(targetFrame);
    }

    /**
     * Gets the subframe contents.
     *
     * @param frame
     *            the frame
     * @param subFrameName
     *            the sub frame name
     *
     * @return the subframe contents
     */
    WebResponse getSubframeContents(FrameSelector frame, String subFrameName) {
        return _frameContents.getSubframeContents(frame, subFrameName);
    }

    /**
     * Gets the parent frame contents.
     *
     * @param frame
     *            the frame
     *
     * @return the parent frame contents
     */
    WebResponse getParentFrameContents(FrameSelector frame) {
        return _frameContents.getParentFrameContents(frame);
    }

    /**
     * Returns the response representing the main page in this window.
     *
     * @return the current page
     */
    public WebResponse getCurrentPage() {
        return getFrameContents(WebRequest.TOP_FRAME);
    }

    /**
     * construct a WebWindow from a given client.
     *
     * @param client
     *            - the client to construct me from
     */
    WebWindow(WebClient client) {
        _client = client;
        _frameContents = new FrameHolder(this);
        _name = NO_NAME + _client.getOpenWindows().length;
        _redirects = new Hashtable<>();
    }

    /**
     * Instantiates a new web window.
     *
     * @param client
     *            the client
     * @param opener
     *            the opener
     */
    WebWindow(WebClient client, WebResponse opener) {
        this(client);
        _opener = opener;
    }

    /**
     * Update frame contents.
     *
     * @param response
     *            the response
     * @param requestContext
     *            the request context
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws SAXException
     *             the SAX exception
     */
    void updateFrameContents(WebResponse response, RequestContext requestContext) throws IOException, SAXException {
        response.setWindow(this);
        _frameContents.updateFrames(response, response.getFrame(), requestContext);
    }

    /**
     * Sets the name.
     *
     * @param name
     *            the new name
     */
    void setName(String name) {
        _name = name;
    }

    /**
     * Delays the specified amount of time.
     *
     * @param numMilliseconds
     *            the num milliseconds
     */
    private void delay(int numMilliseconds) {
        if (numMilliseconds == 0) {
            return;
        }
        try {
            Thread.sleep(numMilliseconds);
        } catch (InterruptedException e) {
            // ignore the exception
            Thread.interrupted();
        }
    }

    /**
     * check whether redirect is configured.
     *
     * @param response
     *            the response
     *
     * @return true, if successful
     */
    private boolean redirectConfigured(WebResponse response) {
        boolean isAutoredirect = getClient().getClientProperties().isAutoRedirect();
        boolean hasLocation = response.getHeaderField("Location") != null;
        int responseCode = response.getResponseCode();
        return isAutoredirect && responseCode >= HttpURLConnection.HTTP_MOVED_PERM
                && responseCode <= HttpURLConnection.HTTP_MOVED_TEMP && hasLocation;
    }

    /**
     * check wether we should follow the redirect given in the response make sure we don't run into a recursion.
     *
     * @param response
     *            the response
     *
     * @return true, if successful
     */
    private boolean shouldFollowRedirect(WebResponse response) {
        // first check whether redirect is configured for this response
        // this is the old pre [ 1155415 ] Handle redirect instructions which can lead to a loop
        // shouldFollowRedirect method - just renamed
        if (!redirectConfigured(response)) {
            return false;
        }
        // now do the recursion check
        String redirectLocation = response.getHeaderField("Location");

        URL url = null;

        try {
            if (redirectLocation != null) {
                url = new URL(response.getURL(), redirectLocation);
            }
        } catch (MalformedURLException e) {
            // Fall through and allow existing exception handling code deal
            // with any exception - we don't know at this stage whether it is
            // a redirect instruction, although it is highly likely, given
            // there is a location header present in the response!
        }

        switch (response.getResponseCode()) {
            case HttpURLConnection.HTTP_MOVED_PERM:
            case HttpURLConnection.HTTP_MOVED_TEMP: // Fall through
                int count = 0;
                if (null != url) {
                    Integer value = (Integer) _redirects.get(url);
                    if (null != value) {
                        // We have already been instructed to redirect to that
                        // location in the course of this attempt to resolve the
                        // resource

                        count = value.intValue();

                        int maxRedirects = getClient().getClientProperties().getMaxRedirects();

                        if (count == maxRedirects) {
                            throw new RecursiveRedirectionException(url, "Maximum number of redirects exceeded");
                        }
                    }

                    count++;
                    _redirects.put(url, Integer.valueOf(count));
                }
                break;
        }
        return redirectLocation != null;
    }

    /**
     * Gets the top frame.
     *
     * @return the top frame
     */
    FrameSelector getTopFrame() {
        return _frameContents.getTopFrame();
    }

    /**
     * Gets the frame.
     *
     * @param target
     *            the target
     *
     * @return the frame
     */
    FrameSelector getFrame(String target) {
        return _frameContents.getFrame(target);
    }

}

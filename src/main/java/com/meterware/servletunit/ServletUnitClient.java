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

import com.meterware.httpunit.FrameSelector;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpInternalErrorException;
import com.meterware.httpunit.WebClient;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpSession;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import org.xml.sax.SAXException;

/**
 * A client for use with the servlet runner class, allowing the testing of servlets without an actual servlet container.
 * Testing can be done in one of two ways. End-to-end testing works much like the HttpUnit package, except that only
 * servlets actually registered with the ServletRunner will be invoked. It is also possible to test servlets 'from the
 * inside' by creating a ServletInvocationContext and then calling any servlet methods which may be desired. Even in
 * this latter mode, end-to-end testing is supported, but requires a call to this class's getResponse method to update
 * its cookies and frames.
 **/
public class ServletUnitClient extends WebClient {

    /**
     * Instantiates a new servlet unit client.
     */
    private ServletUnitClient() {
        throw new RuntimeException("ServletUnitClient constructor needs InvocationContextFactory parameter");
    }

    /**
     * Creates and returns a new servlet unit client instance.
     *
     * @param factory
     *            the factory
     *
     * @return the servlet unit client
     */
    public static ServletUnitClient newClient(InvocationContextFactory factory) {
        return new ServletUnitClient(factory);
    }

    /**
     * Specifies a proxy server to use for requests from this client.
     */
    @Override
    public void setProxyServer(String proxyHost, int proxyPort) {
        // not implemented
    }

    /**
     * Creates and returns a new invocation context from a GET request.
     *
     * @param requestString
     *            the request string
     *
     * @return the invocation context
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws MalformedURLException
     *             the malformed URL exception
     */
    public InvocationContext newInvocation(String requestString) throws IOException, MalformedURLException {
        return newInvocation(new GetMethodWebRequest(requestString));
    }

    /**
     * Creates and returns a new invocation context to test calling of servlet methods.
     *
     * @param request
     *            the request
     *
     * @return the invocation context
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws MalformedURLException
     *             the malformed URL exception
     */
    public InvocationContext newInvocation(WebRequest request) throws IOException, MalformedURLException {
        return newInvocation(request, FrameSelector.TOP_FRAME);
    }

    /**
     * get a new Invocation Context based on a request and a frame.
     *
     * @param request
     *            the request
     * @param frame
     *            the frame
     *
     * @return the invocation context
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws MalformedURLException
     *             the malformed URL exception
     */
    InvocationContext newInvocation(WebRequest request, FrameSelector frame) throws IOException, MalformedURLException {
        ByteArrayOutputStream baos = getMessageBody(request);
        if (_invocationContextFactory == null) {
            throw new RuntimeException("newInvocation called with null _invocationContextFactory");
        }
        return _invocationContextFactory.newInvocation(this, frame, request, getHeaderFields(request.getURL()),
                baos.toByteArray());
    }

    /**
     * Gets the message body.
     *
     * @param request
     *            the request
     *
     * @return the message body
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    ByteArrayOutputStream getMessageBody(WebRequest request) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeMessageBody(request, baos);
        return baos;
    }

    /**
     * Updates this client and returns the response which would be displayed by the user agent. Note that this will
     * typically be the same as that returned by the servlet invocation unless that invocation results in a redirect
     * request.
     *
     * @param invocation
     *            the invocation
     *
     * @return the response
     *
     * @throws MalformedURLException
     *             the malformed URL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws SAXException
     *             the SAX exception
     */
    public WebResponse getResponse(InvocationContext invocation)
            throws MalformedURLException, IOException, SAXException {
        updateMainWindow(invocation.getFrame(), invocation.getServletResponse());
        return getFrameContents(invocation.getFrame());
    }

    /**
     * Returns the session that would be used by the next request (if it asks for one).
     *
     * @param create
     *            if true, will create a new session if no valid session is defined.
     *
     * @return the session
     */
    public HttpSession getSession(boolean create) {
        HttpSession session = _invocationContextFactory
                .getSession(getCookieValue(ServletUnitHttpSession.SESSION_COOKIE_NAME), create);
        if (session != null) {
            putCookie(ServletUnitHttpSession.SESSION_COOKIE_NAME, session.getId());
        }
        return session;
    }

    // -------------------------------- WebClient methods --------------------------------------

    /**
     * Creates a web response object which represents the response to the specified web request.
     **/
    @Override
    protected WebResponse newResponse(WebRequest request, FrameSelector targetFrame)
            throws MalformedURLException, IOException {

        try {
            InvocationContext invocation = newInvocation(request, targetFrame);
            invocation.service();
            return invocation.getServletResponse();
        } catch (ServletException e) {
            throw new HttpInternalErrorException(request.getURL(), e);
        }

    }

    // -------------------------- private members -----------------------------------

    /** The invocation context factory. */
    private InvocationContextFactory _invocationContextFactory;

    // --------------------------------- package methods ---------------------------------------

    /**
     * Instantiates a new servlet unit client.
     *
     * @param factory
     *            the factory
     */
    private ServletUnitClient(InvocationContextFactory factory) {
        if (factory == null) {
            throw new RuntimeException("constructor for ServletUnitClient called with null factory parameter");
        }
        _invocationContextFactory = factory;
    }
}

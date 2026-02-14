/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.servletunit;

import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.HttpUnitUtils;

import jakarta.servlet.http.HttpServletRequest;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * The Class RequestContext.
 */
class RequestContext {

    /** The parameters. */
    private Hashtable _parameters = new Hashtable<>();

    /** The visible parameters. */
    private Hashtable _visibleParameters;

    /** The parent request. */
    private HttpServletRequest _parentRequest;

    /** The url. */
    private URL _url;

    /** The message body. */
    private byte[] _messageBody;

    /** The message encoding. */
    private String _messageEncoding;

    /**
     * Instantiates a new request context.
     *
     * @param url
     *            the url
     */
    RequestContext(URL url) {
        _url = url;
        String file = _url.getFile();
        if (file.indexOf('?') >= 0) {
            loadParameters(file.substring(file.indexOf('?') + 1) /* urlEncoded */ );
        }
    }

    /**
     * Sets the parent request.
     *
     * @param parentRequest
     *            the new parent request
     */
    void setParentRequest(HttpServletRequest parentRequest) {
        _parentRequest = parentRequest;
        _visibleParameters = null;
    }

    /**
     * Gets the request URI.
     *
     * @return the request URI
     */
    String getRequestURI() {
        return _url.getPath();
    }

    /**
     * Gets the parameter.
     *
     * @param name
     *            the name
     *
     * @return the parameter
     */
    String getParameter(String name) {
        String[] parameters = (String[]) getParameters().get(name);
        return parameters == null ? null : parameters[0];
    }

    /**
     * Gets the parameter names.
     *
     * @return the parameter names
     */
    Enumeration getParameterNames() {
        return getParameters().keys();
    }

    /**
     * Gets the parameter map.
     *
     * @return the parameter map
     */
    Map getParameterMap() {
        return (Map) getParameters().clone();
    }

    /**
     * Gets the parameter values.
     *
     * @param name
     *            the name
     *
     * @return the parameter values
     */
    String[] getParameterValues(String name) {
        return (String[]) getParameters().get(name);
    }

    /** The Constant STATE_INITIAL. */
    static final private int STATE_INITIAL = 0;

    /** The Constant STATE_HAVE_NAME. */
    static final private int STATE_HAVE_NAME = 1;

    /** The Constant STATE_HAVE_EQUALS. */
    static final private int STATE_HAVE_EQUALS = 2;

    /** The Constant STATE_HAVE_VALUE. */
    static final private int STATE_HAVE_VALUE = 3;

    /**
     * This method employs a state machine to parse a parameter query string. The transition rules are as follows: State
     * \ text '=' '&' initial: have_name - initial have_name: - have_equals initial have_equals: have_value - initial
     * have_value: - initial initial actions occur on the following transitions: initial -> have_name: save token as
     * name have_equals -> initial: record parameter with null value have_value -> initial: record parameter with value
     *
     * @param queryString
     *            the query string
     */
    void loadParameters(String queryString) {
        if (queryString.isEmpty()) {
            return;
        }
        StringTokenizer st = new StringTokenizer(queryString, "&=", /* return tokens */ true);
        int state = STATE_INITIAL;
        String name = null;
        String value = null;

        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.equals("&")) {
                state = STATE_INITIAL;
                if (name != null && value != null) {
                    addParameter(name, value);
                }
                name = value = null;
            } else if (token.equals("=")) {
                if (state == STATE_HAVE_NAME) {
                    state = STATE_HAVE_EQUALS;
                } else if (state == STATE_HAVE_VALUE) {
                    state = STATE_INITIAL;
                }
            } else if (state == STATE_INITIAL) {
                name = HttpUnitUtils.decode(token, getMessageEncoding());
                value = "";
                state = STATE_HAVE_NAME;
            } else {
                value = HttpUnitUtils.decode(token, getMessageEncoding());
                state = STATE_HAVE_VALUE;
            }
        }
        if (name != null && value != null) {
            addParameter(name, value);
        }
    }

    /**
     * Adds the parameter.
     *
     * @param name
     *            the name
     * @param encodedValue
     *            the encoded value
     */
    private void addParameter(String name, String encodedValue) {
        String[] values = (String[]) _parameters.get(name);
        _visibleParameters = null;
        if (values == null) {
            _parameters.put(name, new String[] { encodedValue });
        } else {
            _parameters.put(name, extendedArray(values, encodedValue));
        }
    }

    /**
     * Extended array.
     *
     * @param baseArray
     *            the base array
     * @param newValue
     *            the new value
     *
     * @return the string[]
     */
    private static String[] extendedArray(String[] baseArray, String newValue) {
        String[] result = new String[baseArray.length + 1];
        System.arraycopy(baseArray, 0, result, 0, baseArray.length);
        result[baseArray.length] = newValue;
        return result;
    }

    /**
     * Gets the parameters.
     *
     * @return the parameters
     */
    private Hashtable getParameters() {
        if (_messageBody != null) {
            loadParameters(getMessageBodyAsString());
            _messageBody = null;
        }
        if (_visibleParameters == null) {
            if (_parentRequest == null) {
                _visibleParameters = _parameters;
            } else {
                _visibleParameters = new Hashtable<>();
                final Map parameterMap = _parentRequest.getParameterMap();
                for (Object key : parameterMap.keySet()) {
                    _visibleParameters.put(key, parameterMap.get(key));
                }
                for (Enumeration e = _parameters.keys(); e.hasMoreElements();) {
                    Object key = e.nextElement();
                    _visibleParameters.put(key, _parameters.get(key));
                }
            }
        }
        return _visibleParameters;
    }

    /**
     * Gets the message body as string.
     *
     * @return the message body as string
     */
    private String getMessageBodyAsString() {
        return new String(_messageBody, StandardCharsets.UTF_8);
    }

    /**
     * Sets the message body.
     *
     * @param bytes
     *            the new message body
     */
    void setMessageBody(byte[] bytes) {
        _messageBody = bytes;
    }

    /**
     * Sets the message encoding.
     *
     * @param messageEncoding
     *            the new message encoding
     */
    public void setMessageEncoding(String messageEncoding) {
        _messageEncoding = messageEncoding;
    }

    /**
     * Gets the message encoding.
     *
     * @return the message encoding
     */
    private String getMessageEncoding() {
        return _messageEncoding == null ?
        /* Fixing 1705925: StandardCharsets.ISO_8859_1.name() */
                HttpUnitOptions.getDefaultCharacterSet() : _messageEncoding;
    }

}

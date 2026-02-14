/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

/**
 * A class which represents the properties of a web client.
 **/
public class ClientProperties {

    /**
     * Returns the current defaults for newly created web clients.
     *
     * @return the default properties
     */
    public static ClientProperties getDefaultProperties() {
        return _defaultProperties;
    }

    /**
     * Specifies the ID information for a client.
     *
     * @param applicationName
     *            the application name
     * @param applicationCodeName
     *            the application code name
     * @param applicationVersion
     *            the application version
     */
    public void setApplicationID(String applicationName, String applicationCodeName, String applicationVersion) {
        _applicationCodeName = applicationCodeName;
        _applicationName = applicationName;
        _applicationVersion = applicationVersion;
    }

    /**
     * Gets the application code name.
     *
     * @return the application code name
     */
    public String getApplicationCodeName() {
        return _applicationCodeName;
    }

    /**
     * Sets the application code name.
     *
     * @param applicationCodeName
     *            the new application code name
     */
    public void setApplicationCodeName(String applicationCodeName) {
        _applicationCodeName = applicationCodeName;
    }

    /**
     * Gets the application name.
     *
     * @return the application name
     */
    public String getApplicationName() {
        return _applicationName;
    }

    /**
     * Sets the application name.
     *
     * @param applicationName
     *            the new application name
     */
    public void setApplicationName(String applicationName) {
        _applicationName = applicationName;
    }

    /**
     * Gets the application version.
     *
     * @return the application version
     */
    public String getApplicationVersion() {
        return _applicationVersion;
    }

    /**
     * Sets the application version.
     *
     * @param applicationVersion
     *            the new application version
     */
    public void setApplicationVersion(String applicationVersion) {
        _applicationVersion = applicationVersion;
    }

    /**
     * Returns the user agent identification. Unless this has been set explicitly, it will default to the application
     * code name followed by a slash and the application version.
     *
     * @return the user agent
     */
    public String getUserAgent() {
        return _userAgent != null ? _userAgent : _applicationCodeName + '/' + _applicationVersion;
    }

    /**
     * Sets the user agent.
     *
     * @param userAgent
     *            the new user agent
     */
    public void setUserAgent(String userAgent) {
        _userAgent = userAgent;
    }

    /**
     * Gets the platform.
     *
     * @return the platform
     */
    public String getPlatform() {
        return _platform;
    }

    /**
     * Sets the platform.
     *
     * @param platform
     *            the new platform
     */
    public void setPlatform(String platform) {
        _platform = platform;
    }

    /**
     * A shortcut for setting both availableScreenWidth and availableScreenHeight at one time.
     *
     * @param width
     *            the width
     * @param height
     *            the height
     */
    public void setAvailableScreenSize(int width, int height) {
        _availWidth = width;
        _availHeight = height;
    }

    /**
     * Gets the available screen width.
     *
     * @return the available screen width
     */
    public int getAvailableScreenWidth() {
        return _availWidth;
    }

    /**
     * Sets the available screen width.
     *
     * @param availWidth
     *            the new available screen width
     */
    public void setAvailableScreenWidth(int availWidth) {
        _availWidth = availWidth;
    }

    /**
     * Gets the avail height.
     *
     * @return the avail height
     */
    public int getAvailHeight() {
        return _availHeight;
    }

    /**
     * Sets the avail height.
     *
     * @param availHeight
     *            the new avail height
     */
    public void setAvailHeight(int availHeight) {
        _availHeight = availHeight;
    }

    /**
     * Returns true if the client should accept and transmit cookies. The default is to accept them.
     *
     * @return true, if is accept cookies
     */
    public boolean isAcceptCookies() {
        return _acceptCookies;
    }

    /**
     * Specifies whether the client should accept and send cookies.
     *
     * @param acceptCookies
     *            the new accept cookies
     */
    public void setAcceptCookies(boolean acceptCookies) {
        _acceptCookies = acceptCookies;
    }

    /**
     * Returns true if the client will accept GZIP encoding of responses. The default is to accept GZIP encoding.
     *
     * @return true, if is accept gzip
     */
    public boolean isAcceptGzip() {
        return _acceptGzip;
    }

    /**
     * Specifies whether the client will accept GZIP encoded responses. The default is true.
     *
     * @param acceptGzip
     *            the new accept gzip
     */
    public void setAcceptGzip(boolean acceptGzip) {
        _acceptGzip = acceptGzip;
    }

    /**
     * get Maximum number of redirect requests.
     *
     * @return it
     */
    public int getMaxRedirects() {
        return _maxRedirects;
    }

    /**
     * set the maximum number of redirects.
     *
     * @param maxRedirects
     *            the new max redirects
     */
    public void setMaxRedirects(int maxRedirects) {
        _maxRedirects = maxRedirects;
    }

    /**
     * Returns true if the client should automatically follow page redirect requests (status 3xx). By default, this is
     * true.
     *
     * @return true, if is auto redirect
     */
    public boolean isAutoRedirect() {
        return _autoRedirect;
    }

    /**
     * Determines whether the client should automatically follow page redirect requests (status 3xx). By default, this
     * is true in order to simulate normal browser operation.
     *
     * @param autoRedirect
     *            the new auto redirect
     */
    public void setAutoRedirect(boolean autoRedirect) {
        _autoRedirect = autoRedirect;
    }

    /**
     * Returns true if the client should automatically follow page refresh requests. By default, this is false, so that
     * programs can verify the redirect page presented to users before the browser switches to the new page.
     *
     * @return true, if is auto refresh
     */
    public boolean isAutoRefresh() {
        return _autoRefresh;
    }

    /**
     * Specifies whether the client should automatically follow page refresh requests. By default, this is false, so
     * that programs can verify the redirect page presented to users before the browser switches to the new page.
     * Setting this to true can cause an infinite loop on pages that refresh themselves.
     *
     * @param autoRefresh
     *            the new auto refresh
     */
    public void setAutoRefresh(boolean autoRefresh) {
        _autoRefresh = autoRefresh;
    }

    /**
     * Checks if is iframe supported.
     *
     * @return true, if is iframe supported
     */
    public boolean isIframeSupported() {
        return _iframeSupported;
    }

    /**
     * Sets the iframe supported.
     *
     * @param iframeSupported
     *            the new iframe supported
     */
    public void setIframeSupported(boolean iframeSupported) {
        _iframeSupported = iframeSupported;
    }

    /**
     * Gets the override context type.
     *
     * @return the overriding content type
     *
     * @see getOverrideContentType
     *
     * @deprecated since 1.8 see BR 2595566 - name of getter is a typo
     */
    @Deprecated
    public String getOverrideContextType() {
        return getOverrideContentType();
    }

    /**
     * Sets the override context type.
     *
     * @param overrideContentType
     *            the content type
     *
     * @see setOverrideContentType
     *
     * @deprecated since 1.8 see BR 2595566 - name of setter is a typo
     */
    @Deprecated
    public void setOverrideContextType(String overrideContentType) {
        setOverrideContentType(overrideContentType);
    }

    /**
     * Returns the content type (if any) to use instead of the one specified by the server. Defaults to null.
     *
     * @return the overriding content type, or null if none is specified.
     */
    public String getOverrideContentType() {
        return _overrideContentType;
    }

    /**
     * All responses to this client will use the specified content type rather than the one specified by the server.
     * Setting this to "text/html" will force all reponses to be interpreted as HTML.
     *
     * @param overrideContentType
     *            the new override to apply to context types.
     */
    public void setOverrideContentType(String overrideContentType) {
        _overrideContentType = overrideContentType;
    }

    /**
     * Specifies a listener for DNS requests from the client.
     *
     * @param dnsListener
     *            the new listener.
     */
    public void setDnsListener(DNSListener dnsListener) {
        _dnsListener = dnsListener;
    }

    /**
     * Returns the listener for DNS requests to be used by the client.
     *
     * @return the currently specified DNS listener, or null if none is specified.
     */
    DNSListener getDnsListener() {
        return _dnsListener;
    }

    /**
     * Checks if is send referer.
     *
     * @return the whether Referer information should be stripped from the header
     */
    public boolean isSendReferer() {
        return _sendReferer;
    }

    /**
     * set whether Referer information should be stripped.
     *
     * @param referer
     *            the _sendReferer to set
     */
    public void setSendReferer(boolean referer) {
        _sendReferer = referer;
    }

    /**
     * Clone properties.
     *
     * @return the client properties
     */
    ClientProperties cloneProperties() {
        return new ClientProperties(this);
    }

    /** The application code name. */
    private String _applicationCodeName = "httpunit";

    /** The application name. */
    private String _applicationName = "HttpUnit";

    /** The application version. */
    private String _applicationVersion = "1.5";

    /** The user agent. */
    private String _userAgent;

    /** The platform. */
    private String _platform = "Java";

    /** The override content type. */
    private String _overrideContentType = null;

    /** The avail width. */
    private int _availWidth = 800;

    /** The avail height. */
    private int _availHeight = 600;

    /** The max redirects. */
    private int _maxRedirects = 5;

    /** The iframe supported. */
    private boolean _iframeSupported = true;

    /** The accept cookies. */
    private boolean _acceptCookies = true;

    /** The accept gzip. */
    private boolean _acceptGzip = true;

    /** The auto redirect. */
    private boolean _autoRedirect = true;

    /** The auto refresh. */
    private boolean _autoRefresh = false;

    /** The dns listener. */
    private DNSListener _dnsListener;

    /** The send referer. */
    private boolean _sendReferer;

    /** The default properties. */
    private static ClientProperties _defaultProperties = new ClientProperties();

    /**
     * default Constructor.
     */
    private ClientProperties() {
        _sendReferer = true;
    }

    /**
     * copy constructor.
     *
     * @param source
     *            - the ClientProperties to copy from
     */
    private ClientProperties(ClientProperties source) {
        _applicationCodeName = source._applicationCodeName;
        _applicationName = source._applicationName;
        _applicationVersion = source._applicationVersion;
        _userAgent = source._userAgent;
        _platform = source._platform;
        _overrideContentType = source._overrideContentType;
        _iframeSupported = source._iframeSupported;
        _acceptCookies = source._acceptCookies;
        _acceptGzip = source._acceptGzip;
        _autoRedirect = source._autoRedirect;
        _autoRefresh = source._autoRefresh;
        _sendReferer = source._sendReferer;
        _maxRedirects = source._maxRedirects;
    }

}

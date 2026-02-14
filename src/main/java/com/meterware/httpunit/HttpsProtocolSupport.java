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

import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Encapsulates support for the HTTPS protocol.
 **/
public abstract class HttpsProtocolSupport {

    /** The name of the system parameter used by java.net to locate protocol handlers. **/
    private static final String PROTOCOL_HANDLER_PKGS = "java.protocol.handler.pkgs";

    /** The Constant SunJSSE_PROVIDER_CLASS. */
    // Sun Microsystems:
    public static final String SunJSSE_PROVIDER_CLASS = "com.sun.net.ssl.internal.ssl.Provider";

    /** The Constant SunJSSE_PROVIDER_CLASS2. */
    // 741145: "sun.net.www.protocol.https";
    public static final String SunJSSE_PROVIDER_CLASS2 = "sun.net.www.protocol.https";

    /** The Constant SunSSL_PROTOCOL_HANDLER. */
    public static final String SunSSL_PROTOCOL_HANDLER = "com.sun.net.ssl.internal.www.protocol";

    // IBM WebSphere
    /** The Constant IBMJSSE_PROVIDER_CLASS. */
    // both ibm packages are inside ibmjsseprovider.jar that comes with WebSphere
    public static final String IBMJSSE_PROVIDER_CLASS = "com.ibm.jsse.IBMJSSEProvider";

    /** The Constant IBMSSL_PROTOCOL_HANDLER. */
    public static final String IBMSSL_PROTOCOL_HANDLER = "com.ibm.net.ssl.www.protocol";

    /** The name of the JSSE class which provides support for SSL. **/
    private static String JSSE_PROVIDER_CLASS = SunJSSE_PROVIDER_CLASS;
    /** The name of the JSSE class which supports the https protocol. **/
    private static String SSL_PROTOCOL_HANDLER = SunSSL_PROTOCOL_HANDLER;

    /** The https provider class. */
    private static Class _httpsProviderClass;

    /** The https support verified. */
    private static boolean _httpsSupportVerified;

    /** The https protocol support enabled. */
    private static boolean _httpsProtocolSupportEnabled;

    /**
     * use the given SSL providers - reset the one used so far.
     *
     * @param className
     *            the class name
     * @param handlerName
     *            the handler name
     */
    public static void useProvider(String className, String handlerName) {
        _httpsProviderClass = null;
        JSSE_PROVIDER_CLASS = className;
        SSL_PROTOCOL_HANDLER = handlerName;
    }

    /**
     * use the IBM WebShpere handlers.
     */
    public static void useIBM() {
        useProvider(IBMJSSE_PROVIDER_CLASS, IBMSSL_PROTOCOL_HANDLER);
    }

    /**
     * Returns true if the JSSE extension is installed.
     *
     * @return true, if successful
     */
    static boolean hasHttpsSupport() {
        if (!_httpsSupportVerified) {
            try {
                getHttpsProviderClass();
            } catch (ClassNotFoundException e) {
            }
            _httpsSupportVerified = true;
        }
        return _httpsProviderClass != null;
    }

    /**
     * Attempts to register the JSSE extension if it is not already registered. Will throw an exception if unable to
     * register the extension.
     *
     * @param protocol
     *            the protocol
     */
    static void verifyProtocolSupport(String protocol) {
        if (protocol.equalsIgnoreCase("http")) {
        }
        if (protocol.equalsIgnoreCase("https")) {
            validateHttpsProtocolSupport();
        }
    }

    /**
     * Validate https protocol support.
     */
    private static void validateHttpsProtocolSupport() {
        if (!_httpsProtocolSupportEnabled) {
            verifyHttpsSupport();
            _httpsProtocolSupportEnabled = true;
        }
    }

    /**
     * Verify https support.
     */
    private static void verifyHttpsSupport() {
        try {
            Class providerClass = getHttpsProviderClass();
            if (!hasProvider(providerClass)) {
                Security.addProvider((Provider) providerClass.getDeclaredConstructor().newInstance());
            }
            registerSSLProtocolHandler();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "https support requires the Java Secure Sockets Extension. See http://java.sun.com/products/jsse");
        } catch (Throwable e) {
            throw new RuntimeException("Unable to enable https support. Make sure that you have installed JSSE "
                    + "as described in http://java.sun.com/products/jsse/install.html: " + e);
        }
    }

    /**
     * get the Https Provider Class if it's been set already return it - otherwise check with the Security package and
     * take the first available provider if all fails take the default provider class.
     *
     * @return the HttpsProviderClass
     *
     * @throws ClassNotFoundException
     *             the class not found exception
     */
    public static Class getHttpsProviderClass() throws ClassNotFoundException {
        if (_httpsProviderClass == null) {
            // [ 1520925 ] SSL patch
            Provider[] sslProviders = Security.getProviders("SSLContext.SSLv3");
            if (sslProviders.length > 0) {
                _httpsProviderClass = sslProviders[0].getClass();
            }
            if (_httpsProviderClass == null) {
                _httpsProviderClass = Class.forName(JSSE_PROVIDER_CLASS);
            }
        }
        return _httpsProviderClass;
    }

    /**
     * Checks for provider.
     *
     * @param providerClass
     *            the provider class
     *
     * @return true, if successful
     */
    private static boolean hasProvider(Class providerClass) {
        Provider[] list = Security.getProviders();
        for (Provider element : list) {
            if (element.getClass().equals(providerClass)) {
                return true;
            }
        }
        return false;
    }

    /**
     * convenience function: create a socket factory which uses an anything-goes trust manager. proposed by Florian
     * Weimar
     *
     * @return the socket factory
     *
     * @throws Exception
     *             the exception
     */
    public static SSLSocketFactory getSocketFactory() throws Exception {
        final SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new X509TrustManager[] { new X509TrustManager() {
            // @Override
            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            }

            // @Override
            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            }

            // @Override
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

        } }, null);
        return context.getSocketFactory();
    }

    /**
     * register the Secure Socket Layer Protocol Handler.
     */
    private static void registerSSLProtocolHandler() {
        String list = System.getProperty(PROTOCOL_HANDLER_PKGS);
        if (list == null || list.isEmpty()) {
            System.setProperty(PROTOCOL_HANDLER_PKGS, SSL_PROTOCOL_HANDLER);
        } else if (list.indexOf(SSL_PROTOCOL_HANDLER) < 0) {
            // [ 1516007 ] Default SSL provider not being used
            System.setProperty(PROTOCOL_HANDLER_PKGS, list + " | " + SSL_PROTOCOL_HANDLER);
        }
    }
}

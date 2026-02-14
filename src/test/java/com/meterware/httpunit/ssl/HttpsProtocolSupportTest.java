/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.ssl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.meterware.httpunit.HttpsProtocolSupport;

import java.security.Provider;
import java.security.Security;

import javax.net.ssl.SSLSocketFactory;

import org.junit.jupiter.api.Test;

/**
 * Tests the HttpsProtocolSupport.
 */
class HttpsProtocolSupportTest {

    /**
     * test the available HttpsProtocolProviders are available.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void provider() throws Exception {
        Class provider = HttpsProtocolSupport.getHttpsProviderClass();
        String expected = HttpsProtocolSupport.SunJSSE_PROVIDER_CLASS;
        Provider[] sslProviders = Security.getProviders("SSLContext.SSLv3");
        if (sslProviders.length > 0) {
            expected = sslProviders[0].getClass().getName();
        }
        assertEquals(expected, provider.getName(), "provider");
    }

    /**
     * test the available HttpsProtocolProviders.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void providerIBM() throws Exception {
        HttpsProtocolSupport.useIBM();
        Class provider = HttpsProtocolSupport.getHttpsProviderClass();
        String expected = HttpsProtocolSupport.IBMJSSE_PROVIDER_CLASS;
        Provider[] sslProviders = Security.getProviders("SSLContext.SSLv3");
        if (sslProviders.length > 0) {
            expected = sslProviders[0].getClass().getName();
        }
        assertEquals(expected, provider.getName(), "provider");
    }

    /**
     * test the socket Factory convenience method as proposed by Florian Weimar.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void socketFactory() throws Exception {
        SSLSocketFactory factory = HttpsProtocolSupport.getSocketFactory();
        assertNotNull(factory);
    }
}

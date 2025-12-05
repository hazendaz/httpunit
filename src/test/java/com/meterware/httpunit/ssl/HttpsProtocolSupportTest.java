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

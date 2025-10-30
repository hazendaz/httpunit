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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.meterware.httpunit.cookies.Cookie;
import com.meterware.pseudoserver.PseudoServlet;
import com.meterware.pseudoserver.WebResource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.migrationsupport.rules.ExternalResourceSupport;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 */
@ExtendWith(ExternalResourceSupport.class)
public class WebClientTest extends HttpUnitTest {

    @Disabled
    public void testNoSuchServer() throws Exception {
        WebConversation wc = new WebConversation();

        try {
            wc.getResponse("http://no.such.host");
            fail("Should have rejected the request");
        } catch (IOException e) {
            // if (!(e.getCause() instanceof UnknownHostException)) throw e;
        }
    }

    /**
     * check access to resources that are not defined
     *
     * @throws Exception
     */
    @Test
    void notFound() throws Exception {
        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest(getHostPath() + "/nothing.htm");
        try {
            wc.getResponse(request);
            fail("Should have rejected the request");
        } catch (HttpNotFoundException e) {
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, e.getResponseCode(), "Response code");
            assertEquals("unable to find /nothing.htm", e.getResponseMessage(), "Response message");
            assertEquals("", e.getResponse().getText(), "Response text");
        }
    }

    /**
     * check access to undefined resources
     *
     * @throws IOException
     */
    @Test
    void undefinedResource() throws IOException {
        boolean originalState = HttpUnitOptions.getExceptionsThrownOnErrorStatus();
        // try two cases for throwException true on i==0, false on i==1
        for (int i = 0; i < 2; i++) {
            boolean throwException = i == 0;
            HttpUnitOptions.setExceptionsThrownOnErrorStatus(throwException);
            WebResponse response = null;
            try {
                WebConversation wc = new WebConversation();
                WebRequest request = new GetMethodWebRequest(getHostPath() + "/undefined");
                response = wc.getResponse(request);
                if (throwException) {
                    fail("there should have been an exception here");
                }
            } catch (HttpNotFoundException hnfe) {
                assertTrue(throwException);
                response = hnfe.getResponse();
            } catch (Exception e) {
                fail("there should be no exception here");
            }
            assertTrue(response != null);
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.getResponseCode());
            if (throwException) {
                assertEquals("", response.getText(), "with throwException=" + throwException);
                assertEquals("unable to find /undefined", response.getResponseMessage(),
                        "with throwException=" + throwException);
            } else {
                // FIXME what do we expect here and how do we get it!
                assertEquals("unable to find /undefined", response.getText(), "with throwException=" + throwException);
                assertNull(response.getResponseMessage());
            }
        }
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(originalState);
    }

    @Test
    void notModifiedResponse() throws Exception {
        defineResource("error.htm", "Not Modified", 304);

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest(getHostPath() + "/error.htm");
        WebResponse response = wc.getResponse(request);
        assertEquals(304, response.getResponseCode(), "Response code");
        response.getText();
        response.getInputStream().read();
    }

    @Test
    void internalErrorException() throws Exception {
        defineResource("internalError.htm", "Internal error", 501);

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest(getHostPath() + "/internalError.htm");
        try {
            wc.getResponse(request);
            fail("Should have rejected the request");
        } catch (HttpException e) {
            assertEquals(501, e.getResponseCode(), "Response code");
        }
    }

    @Test
    void internalErrorDisplay() throws Exception {
        defineResource("internalError.htm", "Internal error", 501);

        WebConversation wc = new WebConversation();
        wc.setExceptionsThrownOnErrorStatus(false);
        WebRequest request = new GetMethodWebRequest(getHostPath() + "/internalError.htm");
        WebResponse response = wc.getResponse(request);
        assertEquals(501, response.getResponseCode(), "Response code");
        assertEquals("Internal error", response.getText().trim(), "Message contents");
    }

    @Test
    void simpleGet() throws Exception {
        String resourceName = "something/interesting";
        String resourceValue = "the desired content";

        defineResource(resourceName, resourceValue);

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest(getHostPath() + '/' + resourceName);
        WebResponse response = wc.getResponse(request);
        assertEquals(resourceValue, response.getText().trim(), "requested resource");
        assertEquals("text/html", response.getContentType(), "content type");
    }

    @Test
    void funkyGet() throws Exception {
        String resourceName = "ID=03.019c010101010001.00000001.a202000000000019. 0d09/login/";
        String resourceValue = "the desired content";

        defineResource(resourceName, resourceValue);

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest(getHostPath() + '/' + resourceName);
        WebResponse response = wc.getResponse(request);
        assertEquals(resourceValue, response.getText().trim(), "requested resource");
        assertEquals("text/html", response.getContentType(), "content type");
    }

    /**
     * test cookies
     *
     * @throws Exception
     */
    @Test
    void cookies() throws Exception {
        String resourceName = "something/baking";
        String resourceValue = "the desired content";

        defineResource(resourceName, resourceValue);
        addResourceHeader(resourceName, "Set-Cookie: HSBCLoginFailReason=; path=/");
        addResourceHeader(resourceName, "Set-Cookie: age=12, name= george");
        addResourceHeader(resourceName, "Set-Cookie: type=short");
        addResourceHeader(resourceName, "Set-Cookie: funky=ab$==");
        addResourceHeader(resourceName, "Set-Cookie: p30waco_sso=3.0,en,us,AMERICA,Drew;path=/, PORTAL30_SSO_TEST=X");
        addResourceHeader(resourceName,
                "Set-Cookie: SESSION_ID=17585,Dzm5LzbRPnb95QkUyIX+7w5RDT7p6OLuOVZ91AMl4hsDATyZ1ej+FA==; path=/;");

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest(getHostPath() + '/' + resourceName);
        WebResponse response = wc.getResponse(request);
        assertEquals(resourceValue, response.getText().trim(), "requested resource");
        assertEquals("text/html", response.getContentType(), "content type");
        String[] names = wc.getCookieNames();
        // for (int i=0;i<names.length;i++) System.err.println(names[i]);

        assertEquals(8, names.length, "number of cookies");
        assertEquals("", wc.getCookieValue("HSBCLoginFailReason"), "cookie 'HSBCLoginFailReason' value");
        assertEquals("12", wc.getCookieValue("age"), "cookie 'age' value");
        assertEquals("george", wc.getCookieValue("name"), "cookie 'name' value");
        assertEquals("short", wc.getCookieValue("type"), "cookie 'type' value");
        assertEquals("ab$==", wc.getCookieValue("funky"), "cookie 'funky' value");
        assertEquals("3.0,en,us,AMERICA,Drew", wc.getCookieValue("p30waco_sso"), "cookie 'p30waco_sso' value");
        assertEquals("X", wc.getCookieValue("PORTAL30_SSO_TEST"), "cookie 'PORTAL30_SSO_TEST' value");
        assertEquals("17585,Dzm5LzbRPnb95QkUyIX+7w5RDT7p6OLuOVZ91AMl4hsDATyZ1ej+FA==", wc.getCookieValue("SESSION_ID"),
                "cookie 'SESSION_ID' value");
        // addition for [ 1488617 ] alternate patch for cookie bug #1371204
        Cookie cookie = wc.getCookieDetails("age");
        assertTrue(cookie != null);
        assertEquals("localhost", cookie.getDomain());
        assertEquals("12", cookie.getValue());
        assertEquals("/something", cookie.getPath());
    }

    @Test
    void cookiesDisabled() throws Exception {
        String resourceName = "something/baking";
        String resourceValue = "the desired content";

        defineResource(resourceName, resourceValue);
        addResourceHeader(resourceName, "Set-Cookie: age=12");

        WebConversation wc = new WebConversation();
        wc.getClientProperties().setAcceptCookies(false);
        WebRequest request = new GetMethodWebRequest(getHostPath() + '/' + resourceName);
        WebResponse response = wc.getResponse(request);
        assertEquals(resourceValue, response.getText().trim(), "requested resource");
        assertEquals("text/html", response.getContentType(), "content type");
        assertEquals(0, wc.getCookieNames().length, "number of cookies");
    }

    @Test
    void oldCookies() throws Exception {
        String resourceName = "something/baking";
        String resourceValue = "the desired content";

        defineResource(resourceName, resourceValue);
        addResourceHeader(resourceName,
                "Set-Cookie: CUSTOMER=WILE_E_COYOTE; path=/; expires=Wednesday, 09-Nov-99 23:12:40 GMT");

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest(getHostPath() + '/' + resourceName);
        WebResponse response = wc.getResponse(request);
        assertEquals(resourceValue, response.getText().trim(), "requested resource");
        assertEquals("text/html", response.getContentType(), "content type");
        assertEquals(1, wc.getCookieNames().length, "number of cookies");
        assertEquals("WILE_E_COYOTE", wc.getCookieValue("CUSTOMER"), "cookie 'CUSTOMER' value");
    }

    /**
     * test setting a cookie manually
     *
     * @throws Exception
     */
    @Test
    void manualCookies() throws Exception {
        defineResource("bounce", new CookieEcho());
        WebConversation wc = new WebConversation();
        wc.putCookie("CUSTOMER", "WILE_E_COYOTE");
        WebResponse response = wc.getResponse(getHostPath() + "/bounce");
        assertEquals("CUSTOMER=WILE_E_COYOTE", response.getText(), "Cookies sent");
        wc.putCookie("CUSTOMER", "ROAD RUNNER");
        response = wc.getResponse(getHostPath() + "/bounce");
        assertEquals("CUSTOMER=ROAD RUNNER", response.getText(), "Cookies sent");
    }

    /**
     * test for 1799532 ] Patched CookieJar for dealing with empty cookies I had a problem testing a web app that sent
     * empty cookie values to the client.
     * <p/>
     * Within the same http response, a cookie was set by the web app twice, once with some non-empty value and the
     * second time with an empty value. The intention of the web app obviously was to delete the cookie, wich actually
     * occurs in IE and Firefox.
     * <p/>
     * In HttpUnit though the cookie was stored with the empty value and sent back to the server in the next request.
     * That confused the web app and caused application errors, because it didn't expect that cookie back with the empty
     * value.
     * <p/>
     * I cannot really judge if this is actually a bug in HttpUnit or just more strict appliance of protocols than real
     * Browsers do. Of course it is at least an ugly behaviour of the web app, but that wasn't my focus. It also seems
     * from forums that others might have had the same problem.
     * <p/>
     * To over come that problem, I had to change the CookieJar class in HttpUnit
     * <p/>
     * to not store cookies with empty values, but remove them completely instead. The modified source file is attached
     * (based on HttpUnit 1.6.2).
     * <p/>
     * Russel, please check if you want to integrate this modification in a future HttpUnit Release. 2007-12-28:
     * wf@bitplan.com - looked at http://www.ietf.org/rfc/rfc2109.txt there is no statement about empty value handling
     * -the standard asks for opaque handling ... no mocking about with values by the server ...
     *
     * @throws Exception
     */
    @Test
    void emptyCookie() throws Exception {
        defineResource("bounce", new CookieEcho());
        WebConversation wc = new WebConversation();
        wc.putCookie("EMPTYVALUE", "non-empty");
        WebResponse response = wc.getResponse(getHostPath() + "/bounce");
        assertEquals("EMPTYVALUE=non-empty", response.getText(), "Cookies sent");
        wc.putCookie("SOMECOOKIE", "some value");
        wc.putCookie("EMPTYVALUE", null);
        response = wc.getResponse(getHostPath() + "/bounce");
        // System.err.println(response.getText());
        String[] names = wc.getCookieNames();
        // should we expect a 1 or a two here?
        // see also testCookies where 8 is currently the correct value and 7 would
        // be if we handle empty strings as cookie deletions
        // as long as 1799532 is rejected we'll go for a 2 here ...
        // [ 1371208 ] Patch for Cookie bug #1371204 has a solution to use 1 ...
        assertEquals(1, names.length, "number of cookies");
        // for (int i=0;i<names.length;i++) System.err.println(names[i]);
    }

    class CookieEcho extends PseudoServlet {

        @Override
        public WebResource getGetResponse() throws IOException {
            return new WebResource(getHeader("Cookie"));
        }
    }

    @Test
    void headerFields() throws Exception {
        defineResource("getHeaders", new PseudoServlet() {
            @Override
            public WebResource getGetResponse() {
                StringBuilder sb = new StringBuilder();
                sb.append(getHeader("Junky")).append("<-->").append(getHeader("User-Agent"));
                return new WebResource(sb.toString(), "text/plain");
            }
        });

        WebConversation wc = new WebConversation();
        wc.getClientProperties().setUserAgent("me alone");
        wc.setHeaderField("junky", "Mozilla 6");
        WebResponse wr = wc.getResponse(getHostPath() + "/getHeaders");
        assertEquals("Mozilla 6<-->me alone", wr.getText(), "headers found");
    }

    @Test
    void basicAuthentication() throws Exception {
        defineResource("getAuthorization", new PseudoServlet() {
            @Override
            public WebResource getGetResponse() {
                return new WebResource(getHeader("Authorization"), "text/plain");
            }
        });

        WebConversation wc = new WebConversation();
        wc.setAuthorization("user", "password");
        WebResponse wr = wc.getResponse(getHostPath() + "/getAuthorization");
        assertEquals("Basic dXNlcjpwYXNzd29yZA==", wr.getText(), "authorization");
    }

    /**
     * test on demand Basic Authentication
     *
     * @throws Exception
     */
    @Test
    void onDemandBasicAuthentication() throws Exception {
        defineResource("getAuthorization", new PseudoServlet() {
            @Override
            public WebResource getGetResponse() {
                String header = getHeader("Authorization");
                if (header == null) {
                    WebResource webResource = new WebResource("unauthorized");
                    webResource.addHeader("WWW-Authenticate: Basic realm=\"testrealm\"");
                    return webResource;
                }
                return new WebResource(header, "text/plain");
            }
        });

        WebConversation wc = new WebConversation();
        wc.setAuthentication("testrealm", "user", "password");
        WebResponse wr = wc.getResponse(getHostPath() + "/getAuthorization");
        assertEquals("Basic dXNlcjpwYXNzd29yZA==", wr.getText(), "authorization");
    }

    /**
     * test on demand Basic Authentication with InputStream
     *
     * @throws Exception
     */
    @Test
    void onDemandBasicAuthenticationInputStream() throws Exception {
        defineResource("postRequiringAuthentication", new PseudoServlet() {
            @Override
            public WebResource getPostResponse() {
                String header = getHeader("Authorization");
                if (header == null) {
                    WebResource webResource = new WebResource("unauthorized");
                    webResource.addHeader("WWW-Authenticate: Basic realm=\"testrealm\"");
                    return webResource;
                }
                return new WebResource(getBody(), "text/plain");
            }
        });

        String body = "something";
        InputStream bodyStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
        PostMethodWebRequest request = new PostMethodWebRequest(getHostPath() + "/postRequiringAuthentication",
                bodyStream, "text/plain");

        WebConversation wc = new WebConversation();
        wc.setAuthentication("testrealm", "user", "password");

        WebResponse wr = wc.getResponse(request);
        assertEquals(body, wr.getText());
        bodyStream.close();
    }

    /**
     * Verifies that even though we have specified username and password for a realm, a request for a different realm
     * will still result in an exception.
     *
     * @throws Exception
     *             if an unexpected exception is thrown.
     */
    @Test
    void basicAuthenticationRequestedForUnknownRealm() throws Exception {
        defineResource("getAuthorization", new PseudoServlet() {
            @Override
            public WebResource getGetResponse() {
                String header = getHeader("Authorization");
                if (header == null) {
                    WebResource webResource = new WebResource("unauthorized");
                    webResource.addHeader("WWW-Authenticate: Basic realm=\"bogusrealm\"");
                    return webResource;
                }
                return new WebResource(header, "text/plain");
            }
        });

        WebConversation wc = new WebConversation();
        wc.setAuthentication("testrealm", "user", "password");
        try {
            wc.getResponse(getHostPath() + "/getAuthorization");
            fail("Should have rejected authentication");
        } catch (AuthorizationRequiredException e) {
            assertEquals("Basic", e.getAuthenticationScheme(), "authorization scheme");
            assertEquals("bogusrealm", e.getAuthenticationParameter("realm"));
        }
    }

    /**
     * test the Negotiate Header does not spoil authentication
     *
     * @throws Exception
     */
    @Test
    void authenticationNegotiateRequest() throws Exception {
        defineResource("getAuthorization", new PseudoServlet() {
            @Override
            public WebResource getGetResponse() {
                String header = getHeader("Authorization");
                if (header == null) {
                    WebResource webResource = new WebResource("unauthorized");
                    webResource.addHeader("WWW-Authenticate: Negotiate");
                    return webResource;
                }
                return new WebResource(header, "text/plain");
            }
        });

        WebConversation wc = new WebConversation();
        wc.setAuthentication("testrealm", "user", "password");
        WebResponse wr = wc.getResponse(getHostPath() + "/getAuthorization");
        assertEquals("unauthorized", wr.getText(), "authorization");
    }

    @Test
    @Disabled
    void proxyServerAccessWithAuthentication() throws Exception {
        defineResource("http://someserver.com/sample", new PseudoServlet() {
            @Override
            public WebResource getGetResponse() {
                return new WebResource(getHeader("Proxy-Authorization"), "text/plain");
            }
        });
        WebConversation wc = new WebConversation();
        wc.setProxyServer("localhost", getHostPort(), "user", "password");
        WebResponse wr = wc.getResponse("http://someserver.com/sample");
        assertEquals("Basic dXNlcjpwYXNzd29yZA==", wr.getText(), "authorization");
    }

    /**
     * test Rfc2069 optionally with or without opaque parameter
     *
     * @param withOpaque
     *
     * @throws Exception
     */
    public void testRfc2069DigestAuthentication(final boolean withOpaque) throws Exception {
        defineResource("/dir/index.html", new PseudoServlet() {
            @Override
            public WebResource getGetResponse() {
                String header = getHeader("Authorization");
                if (header != null) {
                    return new WebResource(getHeader("Authorization"), "text/plain");
                }
                WebResource resource = new WebResource("not authorized", HttpURLConnection.HTTP_UNAUTHORIZED);
                StringBuilder headerStr = new StringBuilder("WWW-Authenticate: Digest realm=\"testrealm@host.com\",")
                        .append(" nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\";");
                if (withOpaque) {
                    headerStr.append(", opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"");
                }
                resource.addHeader(headerStr.toString());
                return resource;
            }
        });
        WebConversation wc = new WebConversation();
        wc.setAuthentication("testrealm@host.com", "Mufasa", "CircleOfLife");
        WebResponse wr = wc.getResponse(getHostPath() + "/dir/index.html");
        StringBuilder expectedHeaderStr = new StringBuilder("Digest username=\"Mufasa\",")
                .append("       realm=\"testrealm@host.com\",")
                .append("       nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\",")
                .append("       uri=\"/dir/index.html\",")
                .append("       response=\"89381827616a396139e299fae10b3a81aaa7bb9e04bee0dcb56ca48dd58af998\"");
        if (withOpaque) {
            expectedHeaderStr.append(", opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"");
        }
        HttpHeader expectedHeader = new HttpHeader(expectedHeaderStr.toString());
        HttpHeader actualHeader = new HttpHeader(wr.getText());
        assertHeadersEquals(expectedHeader, actualHeader);
    }

    /**
     * Verifies one-time digest authentication with no Quality of Protection (qop).
     *
     * @throws Exception
     *             if an unexpected exception is thrown during the test.
     */
    @Test
    void rfc2069DigestAuthentication() throws Exception {
        testRfc2069DigestAuthentication(true);
    }

    /**
     * test for BR 2957505 No 'opaque' causes NPE when attempting DigestAuthentication
     */
    @Test
    void rfc2069DigestAuthenticationNoOpaque() throws Exception {
        testRfc2069DigestAuthentication(false);
    }

    /**
     * Verifies one-time digest authentication with Quality of Protection (qop).
     *
     * @throws Exception
     */
    @Test
    @Disabled
    void qopDigestAuthenticationhttpClient() throws Exception {
        defineResource("/dir/index.html", new PseudoServlet() {
            @Override
            public WebResource getGetResponse() {
                String header = getHeader("Authorization");
                if (header == null) {
                    WebResource resource = new WebResource("not authorized", HttpURLConnection.HTTP_UNAUTHORIZED);
                    resource.addHeader("WWW-Authenticate: Digest realm=\"testrealm@host.com\"," + " qop=\"auth\","
                            + " nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\","
                            + " opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"");
                    return resource;
                }
                return new WebResource(getHeader("Authorization"), "text/plain");
            }
        });
        String text = getPageContents(getHostPath() + "/dir/index.html", "testrealm@host.com", "Mufasa",
                "Circle Of Life");
        HttpHeader actualHeader = new HttpHeader(text);
        HttpHeader expectedHeader = new HttpHeader("Digest username=\"Mufasa\","
                + "       realm=\"testrealm@host.com\"," + "       nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\","
                + "       uri=\"/dir/index.html\"," + "       qop=auth," + "       nc=00000001,"
                + "       cnonce=\"19530e1f777250e9d7ad02a93b187b9d\","
                + "       response=\"943fad0655736f7a2342daef67186ce6\","
                + "       opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"");
        actualHeader.getProperty("cnonce");
        assertHeadersEquals(expectedHeader, actualHeader);
    }

    /**
     * get page contents
     *
     * @param pageAddress
     * @param protectionDomain
     * @param userName
     * @param password
     *
     * @return the page content
     *
     * @throws Exception
     */
    private static String getPageContents(String pageAddress, String protectionDomain, String userName, String password)
            throws Exception {
        WebConversation wc = new WebConversation();
        wc.setAuthentication(protectionDomain, userName, password);
        WebResponse wr = wc.getResponse(pageAddress);
        return wr.getText();
    }

    private void assertHeadersEquals(HttpHeader expectedHeader, HttpHeader actualHeader) {
        assertEquals(expectedHeader.getLabel(), actualHeader.getLabel(), "Authentication type");
        if (!expectedHeader.equals(actualHeader)) {
            Deltas deltas = new Deltas();
            Set actualKeys = new HashSet(actualHeader.getProperties().keySet());
            for (Iterator eachKey = expectedHeader.getProperties().keySet().iterator(); eachKey.hasNext();) {
                String key = (String) eachKey.next();
                if (!actualKeys.contains(key)) {
                    deltas.addMissingValue(key, expectedHeader.getProperty(key));
                } else {
                    actualKeys.remove(key);
                    deltas.compareValues(key, expectedHeader.getProperty(key), actualHeader.getProperty(key));
                }
            }
            for (Iterator eachKey = actualKeys.iterator(); eachKey.hasNext();) {
                String key = (String) eachKey.next();
                deltas.addExtraValue(key, actualHeader.getProperty(key));
            }
            fail("Header not as expected: " + deltas);
        }
    }

    static class Deltas {
        private ArrayList _missingValues = new ArrayList<>();
        private ArrayList _extraValues = new ArrayList<>();

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (!_missingValues.isEmpty()) {
                sb.append("missing: ").append(_missingValues);
            }
            if (!_extraValues.isEmpty()) {
                sb.append("extra: ").append(_extraValues);
            }
            return sb.toString();
        }

        void addMissingValue(Object key, Object value) {
            _missingValues.add(key + "=" + value);
        }

        void addExtraValue(Object key, Object value) {
            _extraValues.add(key + "=" + value);
        }

        void compareValues(Object key, Object expected, Object actual) {
            if (!expected.equals(actual)) {
                addMissingValue(key, expected);
                addExtraValue(key, actual);
            }
        }
    }

    /**
     * test the Referer Header
     *
     * @param refererEnabled
     *            - true if it should not be stripped
     *
     * @throws Exception
     */
    public void dotestRefererHeader(boolean refererEnabled) throws Exception {
        String resourceName = "tellMe" + refererEnabled;
        String linkSource = "fromLink";
        String formSource = "fromForm";

        String page0 = getHostPath() + '/' + resourceName;
        String page1 = getHostPath() + '/' + linkSource;
        String page2 = getHostPath() + '/' + formSource;

        defineResource(linkSource, "<html><head></head><body><a href=\"" + resourceName + "\">Go</a></body></html>");
        defineResource(formSource,
                "<html><body><form action=\"" + resourceName + "\"><input type=submit></form></body></html>");
        defineResource(resourceName, new PseudoServlet() {
            @Override
            public WebResource getGetResponse() {
                String referer = getHeader("Referer");
                return new WebResource(referer == null ? "null" : referer, "text/plain");
            }
        });

        WebConversation wc = new WebConversation();
        wc.getClientProperties().setSendReferer(refererEnabled);
        WebResponse response = wc.getResponse(page0);
        assertEquals("text/plain", response.getContentType(), "Content type");
        assertEquals("null", response.getText().trim(), "Default Referer header");

        response = wc.getResponse(page1);
        response = wc.getResponse(response.getLinks()[0].getRequest());
        String expected = page1;
        if (!refererEnabled) {
            expected = "null";
        }
        assertEquals(expected, response.getText().trim(), "Link Referer header");
        response = wc.getResponse(page2);
        response = wc.getResponse(response.getForms()[0].getRequest());
        expected = page2;
        if (!refererEnabled) {
            expected = "null";
        }
        assertEquals(expected, response.getText().trim(), "Form Referer header");
    }

    @Test
    void refererHeader() throws Exception {
        dotestRefererHeader(true);
    }

    /**
     * test the referer Header twice - with and without stripping it according to [ 844084 ] Block HTTP referer
     *
     * @throws Exception
     */
    @Test
    void refererHeaderWithStrippingReferer() throws Exception {
        dotestRefererHeader(false);
    }

    @Test
    void redirectedRefererHeader() throws Exception {
        String linkSource = "fromLink";
        String linkTarget = "anOldOne";
        String resourceName = "tellMe";

        defineResource(linkSource, "<html><head></head><body><a href='" + linkTarget + "'>Go</a></body></html>");

        defineResource(linkTarget, "ignored content", HttpURLConnection.HTTP_MOVED_PERM);
        addResourceHeader(linkTarget, "Location: " + getHostPath() + '/' + resourceName);

        defineResource(resourceName, new PseudoServlet() {
            @Override
            public WebResource getGetResponse() {
                String referer = getHeader("Referer");
                return new WebResource(referer == null ? "null" : referer, "text/plain");
            }
        });

        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + '/' + linkSource);
        response = wc.getResponse(response.getLinks()[0].getRequest());
        assertEquals(getHostPath() + '/' + linkSource, response.getText().trim(), "Link Referer header");
    }

    /**
     * test for BR 2834933 https://sourceforge.net/tracker/?func=detail&aid=2834933&group_id=6550&atid=106550 by
     * aptivate
     *
     * @throws Exception
     */
    @Test
    void maxRedirectsNotExceeded() throws Exception {
        String resourceAName = "something/resourceA";
        String resourceBName = "something/resourceB";
        String resourceCName = "something/resourceC";

        // Should test the following :
        // loads resource A
        // loads resource B
        // redirects to resource A
        // loads resource B
        // redirects to resource A
        // loads resource C

        String resourceAContent = "<HTML>" + "<BODY onload='redirect();'>" + "<script type='text/javascript'>"
                + "function redirect()" + "{" + "	if (document.cookie == '')" + "	{"
                + "     document.cookie = 'test=1;';\n" + "		window.location.replace('/" + resourceBName + "');\n"
                + "	}" + " else if (document.cookie == 'test=1')" + " {" + "		document.cookie = 'test=2;';\n"
                + "		window.location.replace('/" + resourceBName + "');\n" + " }" + " else" + " {"
                + " 	window.location.replace('/" + resourceCName + "');\n" + " }" + "}" + "</script>" + "</BODY>"
                + "</HTML>";

        defineResource(resourceAName, resourceAContent, HttpURLConnection.HTTP_OK);

        defineResource(resourceBName, "ignored content", HttpURLConnection.HTTP_MOVED_TEMP);
        addResourceHeader(resourceBName, "Location: " + getHostPath() + "/" + resourceAName);

        defineResource(resourceCName, "ignored content", HttpURLConnection.HTTP_OK);

        WebConversation wc = new WebConversation();
        wc.getClientProperties().setMaxRedirects(2);

        try {
            wc.getResponse(getHostPath() + '/' + resourceAName);
        } catch (RecursiveRedirectionException e) {
            fail("Not expecting a RecursiveRedirectionException - " + "max redirects not exceeded");
        }
    }

    /**
     * /** test for patch [ 1155415 ] Handle redirect instructions which can lead to a loop
     *
     * @throws Exception
     *
     * @author james abley
     */
    @Test
    void selfReferentialRedirect() throws Exception {
        String resourceName = "something/redirected";

        defineResource(resourceName, "ignored content", HttpURLConnection.HTTP_MOVED_PERM);
        addResourceHeader(resourceName, "Location: " + getHostPath() + '/' + resourceName);

        WebConversation wc = new WebConversation();
        try {
            wc.getResponse(getHostPath() + '/' + resourceName);
            fail("Should have thrown a RecursiveRedirectionException");
        } catch (RecursiveRedirectionException expected) {
        }
    }

    /**
     * test for patch [ 1155415 ] Handle redirect instructions which can lead to a loop
     *
     * @throws Exception
     *
     * @author james abley
     */
    @Test
    void loopingMalformedRedirect() throws Exception {
        String resourceAName = "something/redirected";
        String resourceBName = "something/else/redirected";
        String resourceCName = "another/redirect";

        // Define a linked list of 'A points to B points to C points to A...'
        defineResource(resourceAName, "ignored content", HttpURLConnection.HTTP_MOVED_PERM);
        addResourceHeader(resourceAName, "Location: " + getHostPath() + '/' + resourceBName);

        defineResource(resourceBName, "ignored content", HttpURLConnection.HTTP_MOVED_TEMP);
        addResourceHeader(resourceBName, "Location: " + getHostPath() + "/" + resourceCName);

        defineResource(resourceCName, "ignored content", HttpURLConnection.HTTP_MOVED_PERM);
        addResourceHeader(resourceCName, "Location: " + getHostPath() + "/" + resourceAName);

        WebConversation wc = new WebConversation();
        try {
            wc.getResponse(getHostPath() + '/' + resourceAName);
            fail("Should have thrown a RecursiveRedirectionException");
        } catch (RecursiveRedirectionException expected) {
        }
    }

    /**
     * test for patch [ 1155415 ] Handle redirect instructions which can lead to a loop
     *
     * @throws Exception
     *
     * @author james abley
     */
    @Test
    void redirectHistoryIsClearedOut() throws Exception {
        String resourceName = "something/interesting";
        String resourceValue = "something interesting";

        defineResource(resourceName, resourceValue);

        String redirectName = "something/redirected";

        defineResource(redirectName, "ignored content", HttpURLConnection.HTTP_MOVED_PERM);
        addResourceHeader(redirectName, "Location: " + getHostPath() + '/' + resourceName);

        // Normal behaviour first time through - redirects to resource
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/" + redirectName);
        assertEquals(200, response.getResponseCode(), "OK response");
        assertEquals(resourceValue, response.getText().trim(), "Expected response string");
        assertEquals("text/html", response.getContentType(), "Content type");

        // Can we get the resource again, or is the redirect urls not being cleared out?
        try {
            wc.getResponse(getHostPath() + "/" + redirectName);
            assertEquals(200, response.getResponseCode(), "OK response");
            assertEquals(resourceValue, response.getText().trim(), "Expected response string");
            assertEquals("text/html", response.getContentType(), "Content type");
        } catch (RecursiveRedirectionException e) {
            fail("Not expecting RecursiveRedirectionException - " + "list of redirection urls should be new for each "
                    + "client-initiated request");
        }
    }

    /**
     * test for patch [ 1155415 ] Handle redirect instructions which can lead to a loop
     *
     * @throws Exception
     *
     * @author james abley
     */
    @Test
    void redirectionLeadingToMalformedURLStillClearsOutRedirectionList() throws Exception {
        String resourceAName = "something/redirected";
        String resourceBName = "something/else/redirected";
        String resourceCName = "another/redirect";

        // Define a linked list of 'A points to B points to C points to A...'
        defineResource(resourceAName, "ignored content", HttpURLConnection.HTTP_MOVED_PERM);
        addResourceHeader(resourceAName, "Location: " + getHostPath() + '/' + resourceBName);

        defineResource(resourceBName, "ignored content", HttpURLConnection.HTTP_MOVED_TEMP);
        addResourceHeader(resourceBName, "Location: " + getHostPath() + "/" + resourceCName);

        defineResource(resourceCName, "ignored content", HttpURLConnection.HTTP_MOVED_PERM);
        addResourceHeader(resourceCName, "Location: NotAProtocolThatIKnowOf://ThisReallyShouldThrowAnException");

        WebConversation wc = new WebConversation();
        try {
            wc.getResponse(getHostPath() + '/' + resourceAName);
            fail("Should have thrown a MalformedURLException");
        } catch (MalformedURLException expected) {
            try {
                wc.getResponse(getHostPath() + "/" + resourceAName);
            } catch (RecursiveRedirectionException e) {
                fail("Not expecting RecursiveRedirectionException");
            } catch (MalformedURLException expected2) {

            }
        }
    }

    /**
     * test for bug report [ 1283878 ] FileNotFoundException using Sun JDK 1.5 on empty error pages by Roger Lindsj
     *
     * @throws Exception
     */
    @Test
    void emptyErrorPage() throws Exception {
        boolean originalState = HttpUnitOptions.getExceptionsThrownOnErrorStatus();
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);

        try {
            WebConversation wc = new WebConversation();
            defineResource("emptyError", "", 404);
            WebRequest request = new GetMethodWebRequest(getHostPath() + "/emptyError");
            WebResponse response = wc.getResponse(request);
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.getResponseCode());
            assertEquals(0, response.getContentLength());
        } catch (java.io.FileNotFoundException fnfe) {
            fnfe.printStackTrace();
            assertTrue(false, "There should be no file not found exception '" + fnfe.getMessage() + "'");
        } finally {
            // Restore exceptions state
            HttpUnitOptions.setExceptionsThrownOnErrorStatus(originalState);
        }
    }

    /**
     * test GZIP begin disabled
     *
     * @throws Exception
     */
    @Test
    void gzipDisabled() throws Exception {
        String expectedResponse = "Here is my answer";
        defineResource("Compressed.html", new CompressedPseudoServlet(expectedResponse));

        WebConversation wc = new WebConversation();
        wc.getClientProperties().setAcceptGzip(false);
        WebResponse wr = wc.getResponse(getHostPath() + "/Compressed.html");
        assertNull(wr.getHeaderField("Content-encoding"), "Should not have received a Content-Encoding header");
        assertEquals("text/plain", wr.getContentType(), "Content-Type");
        assertEquals(expectedResponse, wr.getText().trim(), "Content");
    }

    @Test
    void gzipHandling() throws Exception {
        String expectedResponse = "Here is my answer. It needs to be reasonably long to make compression smaller "
                + "than the raw message. It should be obvious when you reach that point. "
                + "Of course it is more than that - it needs to be long enough to cause a problem.";
        defineResource("Compressed.html", new CompressedPseudoServlet(expectedResponse));

        WebConversation wc = new WebConversation();
        WebResponse wr = wc.getResponse(getHostPath() + "/Compressed.html");
        assertEquals("gzip", wr.getHeaderField("Content-encoding"), "Content-Encoding header");
        assertEquals("text/plain", wr.getContentType(), "Content-Type");
        assertEquals(expectedResponse, wr.getText().trim(), "Content");
    }

    /**
     * try to validate support request [ 885326 ] In CONTENT-ENCODING: gzip, EOFException happens. -- disabled by wf
     * 2007-12-30 - does lead to a javascript problem and not fit for a reqular test since it depends on an outsided
     * website not under control of the project
     *
     * @throws Exception
     */
    public void xtestGZIPHandling2() throws Exception {
        String url = "http://sourceforge.net/project/showfiles.php?group_id=6550";
        WebConversation conversation = new WebConversation();
        WebRequest request = new GetMethodWebRequest(url);
        conversation.getResponse(request);
    }

    private class CompressedPseudoServlet extends PseudoServlet {

        private String _responseText;
        private boolean _suppressLengthHeader;

        public CompressedPseudoServlet(String responseText) {
            _responseText = responseText;
        }

        public CompressedPseudoServlet(String responseText, boolean suppressLengthHeader) {
            this(responseText);
            _suppressLengthHeader = suppressLengthHeader;
        }

        @Override
        public WebResource getGetResponse() throws IOException {
            if (!userAcceptsGZIP()) {
                return new WebResource(_responseText.getBytes(StandardCharsets.UTF_8), "text/plain");
            }
            WebResource result = new WebResource(getCompressedContents(), "text/plain");
            if (_suppressLengthHeader) {
                result.suppressAutomaticLengthHeader();
            }
            result.addHeader("Content-Encoding: gzip");
            return result;
        }

        private boolean userAcceptsGZIP() {
            String header = getHeader("Accept-Encoding");
            if (header == null) {
                return false;
            }
            return header.toLowerCase().indexOf("gzip") >= 0;
        }

        private byte[] getCompressedContents() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(baos);
            OutputStreamWriter out = new OutputStreamWriter(gzip);
            out.write(_responseText);
            out.flush();
            out.close();
            return baos.toByteArray();
        }
    }

    @Test
    @EnabledOnJre(value = JRE.JAVA_21, disabledReason = "Locks up on JDK 24+ due to missing Content-Length on GZIP response")
    void gzipUndefinedLengthHandling() throws Exception {
        String expectedResponse = "Here is my answer. It needs to be reasonably long to make compression smaller "
                + "than the raw message. It should be obvious when you reach that point. "
                + "Of course it is more than that - it needs to be long enough to cause a problem.";
        defineResource("Compressed.html", new CompressedPseudoServlet(expectedResponse, /* suppress length */ true));

        WebConversation wc = new WebConversation();
        WebResponse wr = wc.getResponse(getHostPath() + "/Compressed.html");
        assertEquals("gzip", wr.getHeaderField("Content-encoding"), "Content-Encoding header");
        assertEquals("text/plain", wr.getContentType(), "Content-Type");
        assertEquals(expectedResponse, wr.getText().trim(), "Content");
    }

    @Test
    void clientListener() throws Exception {
        defineWebPage("Target", "This is another page with <a href=Form.html target='_top'>one link</a>");
        defineWebPage("Form",
                "This is a page with a simple form: "
                        + "<form action=submit><input name=name><input type=submit></form>"
                        + "<a href=Target.html target=red>a link</a>");
        defineResource("Frames.html",
                "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" + "<FRAMESET cols='20%,80%'>"
                        + "    <FRAME src='Target.html' name='red'>" + "    <FRAME src=Form.html name=blue>"
                        + "</FRAMESET></HTML>");

        WebConversation wc = new WebConversation();
        ArrayList messageLog = new ArrayList<>();
        wc.addClientListener(new ListenerExample(messageLog));

        wc.getResponse(getHostPath() + "/Frames.html");
        assertEquals(6, messageLog.size(), "Num logged items");
        for (int i = 0; i < 3; i++) {
            verifyRequestResponsePair(messageLog, 2 * i);
        }
    }

    private void verifyRequestResponsePair(ArrayList messageLog, int i) throws MalformedURLException {
        assertTrue(messageLog.get(i) instanceof WebRequest,
                "Logged item " + i + " is not a web request, but " + messageLog.get(i).getClass());
        assertTrue(messageLog.get(i + 1) instanceof WebResponse,
                "Logged item " + (i + 1) + " is not a web response, but " + messageLog.get(i + 1).getClass());
        assertEquals(((WebRequest) messageLog.get(i)).getTarget(), ((WebResponse) messageLog.get(i + 1)).getFrameName(),
                "Response target");
        assertEquals(((WebRequest) messageLog.get(i)).getURL(), ((WebResponse) messageLog.get(i + 1)).getURL(),
                "Response URL");
    }

    private static class ListenerExample implements WebClientListener {

        private List _messageLog;

        public ListenerExample(List messageLog) {
            _messageLog = messageLog;
        }

        @Override
        public void requestSent(WebClient src, WebRequest req) {
            _messageLog.add(req);
        }

        @Override
        public void responseReceived(WebClient src, WebResponse resp) {
            _messageLog.add(resp);
        }
    }

    @Test
    void redirect() throws Exception {
        String resourceName = "something/redirected";
        String resourceValue = "the desired content";

        String redirectName = "anOldOne";

        defineResource(resourceName, resourceValue);
        defineResource(redirectName, "ignored content", HttpURLConnection.HTTP_MOVED_PERM);
        addResourceHeader(redirectName, "Location: " + getHostPath() + '/' + resourceName);

        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + '/' + redirectName);
        assertEquals(resourceValue, response.getText().trim(), "requested resource");
        assertEquals("text/html", response.getContentType(), "content type");
        assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode(), "status");
    }

    @Test
    void duplicateHeaderRedirect() throws Exception {
        String resourceName = "something/redirected";
        String resourceValue = "the desired content";

        String redirectName = "anOldOne";

        defineResource(resourceName, resourceValue);
        defineResource(redirectName, "ignored content", HttpURLConnection.HTTP_MOVED_PERM);
        addResourceHeader(redirectName, "Location: " + getHostPath() + '/' + resourceName);
        addResourceHeader(redirectName, "Location: " + getHostPath() + '/' + resourceName);

        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + '/' + redirectName);
        assertEquals(resourceValue, response.getText().trim(), "requested resource");
        assertEquals("text/html", response.getContentType(), "content type");
    }

    @Test
    void disabledRedirect() throws Exception {
        String resourceName = "something/redirected";
        String resourceValue = "the desired content";

        String redirectName = "anOldOne";
        String redirectValue = "old content";

        defineResource(resourceName, resourceValue);
        defineResource(redirectName, redirectValue, HttpURLConnection.HTTP_MOVED_PERM);
        addResourceHeader(redirectName, "Location: " + getHostPath() + '/' + resourceName);

        WebConversation wc = new WebConversation();
        wc.getClientProperties().setAutoRedirect(false);
        WebResponse response = wc.getResponse(getHostPath() + '/' + redirectName);
        assertEquals(redirectValue, response.getText().trim(), "requested resource");
        assertEquals("text/html", response.getContentType(), "content type");
    }

    @Test
    @Disabled
    void dnsOverride() throws Exception {
        WebConversation wc = new WebConversation();
        wc.getClientProperties().setDnsListener(hostName -> "127.0.0.1");

        defineResource("whereAmI", new PseudoServlet() {
            @Override
            public WebResource getGetResponse() {
                WebResource webResource = new WebResource("found host header: " + getHeader("Host"));
                webResource.addHeader("Set-Cookie: type=short");
                return webResource;
            }
        });

        defineResource("checkCookies", new PseudoServlet() {
            @Override
            public WebResource getGetResponse() {
                return new WebResource("found cookies: " + getHeader("Cookie"));
            }
        });

        WebResponse wr = wc.getResponse("http://meterware.com:" + getHostPort() + "/whereAmI");
        assertEquals("found host header: meterware.com:" + getHostPort(), wr.getText(), "Submitted host header");
        assertEquals("short", wc.getCookieValue("type"), "Returned cookie 'type'");

        wr = wc.getResponse("http://meterware.com:" + getHostPort() + "/checkCookies");
        assertEquals("found cookies: type=short", wr.getText(), "Submitted cookie header");
    }

    /**
     * test for Delete Response patch by Matthew M. Boedicker"
     *
     * @throws Exception
     */
    @Test
    void delete() throws Exception {
        String resourceName = "something/to/delete";
        final String responseBody = "deleted";
        final String contentType = "text/plain";

        defineResource(resourceName, new PseudoServlet() {
            @Override
            public WebResource getDeleteResponse() {
                return new WebResource(responseBody, contentType);
            }
        });

        WebConversation wc = new WebConversation();
        WebRequest request = new DeleteMethodWebRequest(getHostPath() + '/' + resourceName);
        WebResponse response = wc.getResponse(request);

        assertEquals(responseBody, response.getText().trim(), "requested resource");
        assertEquals(contentType, response.getContentType(), "content type");
    }

}

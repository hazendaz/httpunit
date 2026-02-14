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
package com.meterware.httpunit.cookies;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.meterware.pseudoserver.HttpUserAgentTest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The Class CookieTest.
 */
class CookieTest {

    /**
     * Sets the up.
     *
     * @throws Exception
     *             the exception
     */
    @BeforeEach
    void setUp() throws Exception {
        CookieProperties.reset();
    }

    /**
     * Simple cookies.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void simpleCookies() throws Exception {
        CookieJar jar = new CookieJar(new TestSource(new URL("http://www.meterware.com"),
                new String[] { "Reason=; path=/", "age=12, name= george", "type=short", "funky=ab$==",
                        "p30waco_sso=3.0,en,us,AMERICA,Drew;path=/, PORTAL30_SSO_TEST=X",
                        "SESSION_ID=17585,Dzm5LzbRPnb95QkUyIX+7w5RDT7p6OLuOVZ91AMl4hsDATyZ1ej+FA==; path=/;" }));
        assertEquals("", jar.getCookieValue("Reason"), "cookie 'Reason' value");
        assertEquals("12", jar.getCookieValue("age"), "cookie 'age' value");
        assertEquals("george", jar.getCookieValue("name"), "cookie 'name' value");
        assertEquals("short", jar.getCookieValue("type"), "cookie 'type' value");
        assertEquals("ab$==", jar.getCookieValue("funky"), "cookie 'funky' value");
        assertEquals("3.0,en,us,AMERICA,Drew", jar.getCookieValue("p30waco_sso"), "cookie 'p30waco_sso' value");
        assertEquals("X", jar.getCookieValue("PORTAL30_SSO_TEST"), "cookie 'PORTAL30_SSO_TEST' value");
        assertEquals("17585,Dzm5LzbRPnb95QkUyIX+7w5RDT7p6OLuOVZ91AMl4hsDATyZ1ej+FA==", jar.getCookieValue("SESSION_ID"),
                "cookie 'SESSION_ID' value");
    }

    /**
     * test for double quoted cookies suggested by Mario V disabled since it indeed fails - FIXME - we need a patch
     * here.
     *
     * @throws Exception
     *             the exception
     */
    public void xtestDoubleQuoteCookies() throws Exception {
        CookieJar jar = new CookieJar(new TestSource(new URL("http://www.meterware.com"),
                new String[] { "NewUniversalCookie=\"mmmmmmmmmmmmmmm==mmmmmmm mmmmmmm\"; Path=/" }));
        Collection cookies = jar.getCookies();
        assertEquals(1, cookies.size(), "There should only be one cookie but there are " + cookies.size());
        assertEquals("mmmmmmmmmmmmmmm==mmmmmmm mmmmmmm", jar.getCookieValue("NewUniversalCookie"),
                "cookie 'NewUniversalCookie' value");
    }

    /**
     * Cookie matching.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void cookieMatching() throws Exception {
        assertTrue(new Cookie("name", "value").mayBeSentTo(new URL("http://httpunit.org/anywhere")),
                "Universal cookie could not be sent");

        checkMatching(1, true, new URL("http://www.meterware.com/servlets/sample"), "www.meterware.com",
                "/servlets/sample");

        checkMatching(2, false, new URL("http://www.meterware.com/servlets/sample"), "meterware.com", "/");
        checkMatching(3, true, new URL("http://www.meterware.com/servlets/sample"), ".meterware.com", "/");
        checkMatching(4, false, new URL("http://www.meterware.com/servlets/sample"), ".httpunit.org", "/");

        checkMatching(5, true, new URL("http://www.meterware.com/servlets/sample"), "www.meterware.com", "/servlets");
        checkMatching(6, false, new URL("http://www.meterware.com/servlets/sample"), "www.meterware.com",
                "/servlets/sample/data");
    }

    /**
     * Check matching.
     *
     * @param index
     *            the index
     * @param success
     *            the success
     * @param url
     *            the url
     * @param domain
     *            the domain
     * @param path
     *            the path
     */
    private void checkMatching(int index, boolean success, URL url, String domain, String path) {
        HashMap attributes = new HashMap<>();
        attributes.put("path", path);
        attributes.put("domain", domain);
        Cookie cookie = new Cookie("name", "value", attributes);
        if (success) {
            assertTrue(cookie.mayBeSentTo(url), "Cookie " + index + " did not allow " + url);
        } else {
            assertFalse(cookie.mayBeSentTo(url), "Cookie " + index + " allowed " + url);
        }
    }

    /**
     * check the CookieAcceptance.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void cookieAcceptance() throws Exception {
        checkAcceptance(1, true, "www.meterware.com/servlets/special", null, null);
        checkAcceptance(2, true, "www.meterware.com/servlets/special", ".meterware.com", "/servlets");
        checkAcceptance(3, false, "www.meterware.com/servlets/special", ".meterware.com", "/servlets/ordinary");
        checkAcceptance(4, true, "www.meterware.com/servlets/special", "meterware.com", null);
        checkAcceptance(5, false, "www.meterware.com/servlets/special", "meterware", null);
        checkAcceptance(6, false, "www.meterware.com/servlets/special", ".com", null);
        checkAcceptance(7, false, "www.meterware.com/servlets/special", ".httpunit.org", null);
        checkAcceptance(8, false, "www.some.meterware.com/servlets/special", ".meterware.com", null);
        // modified expected result according to [ 1476380 ] Cookies incorrectly rejected despite valid domain
        checkAcceptance(9, true, "www.meterware.com/servlets/special", "www.meterware.com", null);
        checkAcceptance(10, false, "www.evilyahoo.com", "yahoo.com", null);
    }

    /**
     * check whether the given cookie is accepted.
     *
     * @param index
     *            the index
     * @param shouldAccept
     *            the should accept
     * @param urlString
     *            the url string
     * @param specifiedDomain
     *            the specified domain
     * @param specifiedPath
     *            the specified path
     *
     * @throws MalformedURLException
     *             the malformed URL exception
     */

    private void checkAcceptance(int index, boolean shouldAccept, String urlString, String specifiedDomain,
            String specifiedPath) throws MalformedURLException {

        CookieJar jar = newJar(urlString, specifiedDomain, specifiedPath);

        if (shouldAccept) {
            // modified for Bugreport 2825872 Cookie domains not stored correctly - ID: 2825872
            // http://sourceforge.net/tracker/?func=detail&aid=2825872&group_id=6550&atid=106550
            Cookie cookie = jar.getCookie("name");
            assertNotNull(cookie, "Rejected cookie " + index + "( " + specifiedDomain + " from " + urlString
                    + ") should have been accepted");
            URL url = new URL("http://" + urlString);
            assertTrue(cookie.mayBeSentTo(url), "Cookie " + index + " should be sent to the url ");

        } else {
            assertNull(jar.getCookie("name"), "Cookie " + index + " should have been rejected");
        }
    }

    /**
     * Cookie defaults.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void cookieDefaults() throws Exception {
        checkDefaults(1, "www.meterware.com/servlets/special", ".meterware.com", "/servlets", ".meterware.com",
                "/servlets");
        checkDefaults(2, "www.meterware.com/servlets/special/myServlet", null, null, "www.meterware.com",
                "/servlets/special");
    }

    /**
     * Check defaults.
     *
     * @param index
     *            the index
     * @param urlString
     *            the url string
     * @param specifiedDomain
     *            the specified domain
     * @param specifiedPath
     *            the specified path
     * @param expectedDomain
     *            the expected domain
     * @param expectedPath
     *            the expected path
     *
     * @throws MalformedURLException
     *             the malformed URL exception
     */
    private void checkDefaults(int index, String urlString, String specifiedDomain, String specifiedPath,
            String expectedDomain, String expectedPath) throws MalformedURLException {
        CookieJar jar = newJar(urlString, specifiedDomain, specifiedPath);
        assertNotNull(jar.getCookie("name").getDomain(), "case " + index + " domain is null");
        assertEquals(expectedDomain, jar.getCookie("name").getDomain(), "case " + index + " domain");
        assertNotNull(jar.getCookie("name").getPath(), "case " + index + " path is null");
        assertEquals(expectedPath, jar.getCookie("name").getPath(), "case " + index + " path");
    }

    /**
     * New jar.
     *
     * @param urlString
     *            the url string
     * @param specifiedDomain
     *            the specified domain
     * @param specifiedPath
     *            the specified path
     *
     * @return the cookie jar
     *
     * @throws MalformedURLException
     *             the malformed URL exception
     */
    private CookieJar newJar(String urlString, String specifiedDomain, String specifiedPath)
            throws MalformedURLException {
        StringBuilder header = new StringBuilder("name=value");
        if (specifiedDomain != null) {
            header.append("; domain=").append(specifiedDomain);
        }
        if (specifiedPath != null) {
            header.append("; path=").append(specifiedPath);
        }

        return new CookieJar(new TestSource(new URL("http://" + urlString), header.toString()));
    }

    /**
     * test cookie age and expiration handling see also Friday Fun: I Hate Cookies
     * http://www.mnot.net/blog/2006/10/27/cookie_fun
     *
     * @throws Exception
     *             when an unexpected error occurs
     */
    @Test
    void cookieAge() throws Exception {
        String[] ages = { "max-age=5000", "Max-Age=3000", "expires=Tue, 29-Mar-2005 19:30:42 GMT; Max-Age=2592000",
                "Max-Age=2592000;expires=Tue, 29-Mar-2005 19:30:42 GMT", "expires=Tue, 29-Mar-2005 19:30:42 GMT",
                "Expires=Wednesday, 01-Jan-1970 00:00:00 GMT" };
        long now = System.currentTimeMillis();
        long[] expectedMilliSeconds = { now + 5000 * 1000, now + 3000 * 1000, now + 2592000 * 1000,
                now + 2592000 * 1000, 1112124642000L, 0 };

        for (int i = 0; i < ages.length; i++) {
            String cookieName = "cookie" + i;
            String header = cookieName + "=cookievalue;" + ages[i];
            TestSource source = new TestSource(new URL("http://www.somedomain.com/somepath/"), header);
            CookieJar jar = new CookieJar(source);
            Cookie cookie = jar.getCookie(cookieName);
            assertNotNull(cookie, cookieName + " not null");

            long expiredTime = cookie.getExpiredTime();
            int grace = 3000;
            assertTrue(expectedMilliSeconds[i] <= expiredTime, cookieName + " '" + ages[i]
                    + "' expiration expect on or after" + expectedMilliSeconds[i] + " but was " + expiredTime);
            assertTrue(expectedMilliSeconds[i] + grace > expiredTime, cookieName + " '" + ages[i]
                    + "' expiration expect before " + (expectedMilliSeconds[i] + grace) + " but was " + expiredTime);
            // assertEquals( cookieName + " expiration", expiredTime, expectedMilliSeconds[i] );
        }
    }

    /**
     * Header generation.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void headerGeneration() throws Exception {
        CookieJar jar = new CookieJar();
        jar.putCookie("zero", "nil");
        jar.updateCookies(
                newJar("www.meterware.com/servlets/standard/AServlet", "first=ready, gone=expired;max-age=0"));
        jar.updateCookies(newJar("www.meterware.com/servlets/AnotherServlet", "second=set;max-age=1000"));
        jar.updateCookies(newJar("www.httpunit.org", "zero=go; domain=.httpunit.org"));
        jar.updateCookies(newJar("meterware.com", "fourth=money"));

        checkHeader(1, jar, "first=ready; second=set; zero=nil", "www.meterware.com/servlets/standard/Count");
        checkHeader(2, jar, "second=set; zero=nil", "www.meterware.com/servlets/special/Divide");
        checkHeader(3, jar, "zero=go", "fancy.httpunit.org/servlets/AskMe");

        HttpUserAgentTest.assertMatchingSet("Cookie names",
                new String[] { "zero", "zero", "first", "second", "fourth", "gone" }, jar.getCookieNames());
    }

    /**
     * Drupal cookie interaction.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void drupalCookieInteraction() throws Exception {
        CookieJar jar = new CookieJar();
        jar.putSingleUseCookie("SESS1234", "1234", ".drupalsite.org", "/");
        Cookie cookie = jar.getCookie("SESS1234");
        assertTrue(cookie != null);
        assertEquals(".drupalsite.org", cookie.getDomain());
        assertEquals("1234", cookie.getValue());
        assertEquals("/", cookie.getPath());
        assertEquals(1, jar.getCookies().size());

        CookieJar jar1 = new CookieJar();
        jar1.putSingleUseCookie("SESS1234", "deleted", "www.drupalsite.org", "/");
        jar.updateCookies(jar1);

        cookie = jar.getCookie("SESS1234");
        assertTrue(cookie != null);
        assertEquals("www.drupalsite.org", cookie.getDomain());
        assertEquals("deleted", cookie.getValue());
        assertEquals("/", cookie.getPath());
        assertEquals(1, jar.getCookies().size());

        CookieJar jar2 = new CookieJar();
        jar2.putSingleUseCookie("SESS1234", "4321", ".drupalsite.org", "/");
        jar.updateCookies(jar2);

        cookie = jar.getCookie("SESS1234");
        assertTrue(cookie != null);
        assertEquals(".drupalsite.org", cookie.getDomain());
        assertEquals("4321", cookie.getValue());
        assertEquals("/", cookie.getPath());
        assertEquals(1, jar.getCookies().size());

    }

    /**
     * test for [ 1488617 ] alternate patch for cookie bug #1371204.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void singleUseCookie() throws Exception {
        CookieJar jar = new CookieJar();
        jar.putSingleUseCookie("zero", "nil", "sourceforge.net", "test/me");
        Cookie cookie = jar.getCookie("zero");
        assertTrue(cookie != null);
        assertEquals("sourceforge.net", cookie.getDomain());
        assertEquals("nil", cookie.getValue());
        assertEquals("test/me", cookie.getPath());
    }

    /**
     * test for bug report [ 1672385 ] HttpOnly cookie looses all cookie info extended according to comment of
     * 2010-04-22.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void httpOnlyCookies() throws Exception {
        CookieJar jar = new CookieJar(new TestSource(new URL("http://www.meterware.com"),
                new String[] { "myStuff=1234; path=/foo; HttpOnly" }));
        assertEquals("1234", jar.getCookieValue("myStuff"), "cookie 'myStuff' value");
        // comment of 2010-04-22
        String path = jar.getCookie("myStuff").getPath();
        assertEquals("/foo", path, "cookie 'myStuff' path");
    }

    /**
     * test for bug report [ 2076028 ] Cookies are handled incorrectly should also fit duplicate bug report 2871999
     * https://sourceforge.net/tracker/?func=detail&aid=2871999&group_id=6550&atid=106550
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void httpOnlyCookiePath() throws Exception {
        CookieJar jar = new CookieJar(
                new TestSource(new URL("http://www.meterware.com"), new String[] { "myStuff=1234; path=/; HttpOnly" }));
        Cookie cookie = jar.getCookie("myStuff");
        String expected = "/";
        assertEquals(cookie.getPath(), expected,
                "The cookie should have the path '" + expected + "' but has " + cookie.getPath());
    }

    /**
     * test for bug report [ 1533762 ] Valid cookies are rejected by Alexey Bulat TODO enable when working patch is
     * available.
     *
     * @throws Exception
     *             the exception
     */
    public void xtestCookiesRejection1533762() throws Exception {
        checkAcceptance(1, true, "admin.automation.testing.com.ru", ".admin.automation.testing.com.ru", null);
        checkAcceptance(2, true, "admin.automation.testing.com.ru", ".testing.com.ru", null);
    }

    /**
     * Check header.
     *
     * @param index
     *            the index
     * @param jar
     *            the jar
     * @param expectedHeader
     *            the expected header
     * @param targetURLString
     *            the target URL string
     *
     * @throws MalformedURLException
     *             the malformed URL exception
     */
    private void checkHeader(int index, CookieJar jar, String expectedHeader, String targetURLString)
            throws MalformedURLException {
        assertEquals(expectedHeader, jar.getCookieHeaderField(new URL("http://" + targetURLString)), "header " + index);
    }

    /**
     * Cookie replacement.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void cookieReplacement() throws Exception {
        CookieJar jar = new CookieJar();
        jar.updateCookies(newJar("www.meterware.com/servlets/standard", "first=ready"));
        jar.updateCookies(newJar("meterware.com/servlets/standard", "second=more"));
        jar.updateCookies(newJar("www.meterware.com/servlets", "third=day"));
        jar.updateCookies(newJar("www.meterware.com/servlets", "third=tomorrow"));

        checkHeader(1, jar, "first=ready; third=tomorrow", "www.meterware.com/servlets/standard");
    }

    /**
     * New jar.
     *
     * @param urlString
     *            the url string
     * @param setCookieHeader
     *            the set cookie header
     *
     * @return the cookie jar
     *
     * @throws MalformedURLException
     *             the malformed URL exception
     */
    private CookieJar newJar(String urlString, String setCookieHeader) throws MalformedURLException {
        return new CookieJar(new TestSource(new URL("http://" + urlString), setCookieHeader));
    }

    /**
     * Lenient matching.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void lenientMatching() throws Exception {
        CookieProperties.setDomainMatchingStrict(false);
        checkAcceptance(1, true, "www.some.meterware.com/servlets/special", ".meterware.com", null);
        checkAcceptance(2, false, "www.meterware.com/servlets/special", ".meterware.com", "/servlets/ordinary");
        checkAcceptance(3, true, "www.meterware.com/servlets/special", "www.meterware.com", null);
        // missing leading dot case (yahoo cookies seem to behave like this - seems to be non RFC 2109 compliant ...)
        checkAcceptance(4, true, "www.meterware.com/servlets/special", "meterware.com", null);

        CookieProperties.setPathMatchingStrict(false);
        checkAcceptance(11, true, "www.meterware.com/servlets/special", ".meterware.com", "/servlets/ordinary");
        checkMatching(12, true, new URL("http://www.meterware.com/servlets/sample"), "www.meterware.com",
                "/servlets/sample/data");
    }

    /**
     * Rejection callbacks.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void rejectionCallbacks() throws Exception {
        MockListener listener = new MockListener();
        CookieProperties.addCookieListener(listener);

        checkCallback(listener, 1, 0, "www.meterware.com/servlets/special", null, null);
        checkCallback(listener, 2, CookieListener.PATH_NOT_PREFIX, "www.meterware.com/servlets/special",
                ".meterware.com", "/servlets/ordinary");
        checkCallback(listener, 3, CookieListener.DOMAIN_ONE_DOT, "www.meterware.com/servlets/special", ".com", null);
        checkCallback(listener, 4, CookieListener.DOMAIN_NOT_SOURCE_SUFFIX, "www.meterware.com/servlets/special",
                ".httpunit.org", null);
        checkCallback(listener, 5, CookieListener.DOMAIN_TOO_MANY_LEVELS, "www.some.meterware.com/servlets/special",
                ".meterware.com", null);
    }

    /**
     * check the cookieListener call Back.
     *
     * @param listener
     *            the listener
     * @param index
     *            the index
     * @param status
     *            the status
     * @param urlString
     *            the url string
     * @param specifiedDomain
     *            the specified domain
     * @param specifiedPath
     *            the specified path
     *
     * @throws MalformedURLException
     *             the malformed URL exception
     */
    private void checkCallback(MockListener listener, int index, int status, String urlString, String specifiedDomain,
            String specifiedPath) throws MalformedURLException {
        if (status == 0) {
            listener.expectAcceptance(index);
        } else if (status == CookieListener.PATH_NOT_PREFIX) {
            listener.expectRejection(index, "name", status, specifiedPath);
        } else {
            listener.expectRejection(index, "name", status, specifiedDomain);
        }
        newJar(urlString, specifiedDomain, specifiedPath);
        if (status != 0) {
            listener.confirmRejection();
        }
    }

    /**
     * The listener interface for receiving mock events. The class that is interested in processing a mock event
     * implements this interface, and the object created with that class is registered with a component using the
     * component's <code>addMockListener</code> method. When the mock event occurs, that object's appropriate method is
     * invoked.
     *
     * @see MockEvent
     */
    private static class MockListener implements CookieListener {

        /** The reason. */
        private int _reason;

        /** The attribute. */
        private String _attribute;

        /** The cookie name. */
        private String _cookieName;

        /** The rejected. */
        private boolean _rejected;

        /** The cookie num. */
        private int _cookieNum;

        /**
         * Expect acceptance.
         *
         * @param cookieNum
         *            the cookie num
         */
        void expectAcceptance(int cookieNum) {
            _cookieNum = cookieNum;
            _reason = -1;
        }

        /**
         * Expect rejection.
         *
         * @param cookieNum
         *            the cookie num
         * @param cookieName
         *            the cookie name
         * @param reason
         *            the reason
         * @param attribute
         *            the attribute
         */
        void expectRejection(int cookieNum, String cookieName, int reason, String attribute) {
            _cookieNum = cookieNum;
            _reason = reason;
            _attribute = attribute;
            _cookieName = cookieName;
            _rejected = false;
        }

        /**
         * Confirm rejection.
         */
        void confirmRejection() {
            assertTrue(_rejected, "Cookie " + _cookieNum + " was not logged as rejected");
        }

        @Override
        public void cookieRejected(String name, int reason, String attribute) {
            _rejected = true;
            assertEquals(_reason, reason, "Cookie " + _cookieNum + " rejection code");
            if (_attribute != null) {
                assertEquals(_attribute, attribute, "Cookie " + _cookieNum + " rejected attribute");
            }
            if (_cookieName != null) {
                assertEquals(_cookieName, name, "Cookie " + _cookieNum + " name");
            }
        }
    }

    /**
     * create a TestSource for Cookies.
     */
    private static class TestSource implements CookieSource {

        /** The source URL. */
        private URL _sourceURL;

        /** The headers. */
        private String[] _headers;

        /**
         * construct a TestSource form a single header string.
         *
         * @param sourceURL
         *            the source URL
         * @param header
         *            the header
         */

        public TestSource(URL sourceURL, String header) {
            this(sourceURL, new String[] { header });
        }

        /**
         * Instantiates a new test source.
         *
         * @param sourceURL
         *            the source URL
         * @param headers
         *            the headers
         */
        public TestSource(URL sourceURL, String[] headers) {
            _sourceURL = sourceURL;
            _headers = headers;
        }

        @Override
        public URL getURL() {
            return _sourceURL;
        }

        @Override
        public String[] getHeaderFields(String fieldName) {
            return fieldName.equalsIgnoreCase("set-cookie") ? _headers : new String[0];
        }
    }

}

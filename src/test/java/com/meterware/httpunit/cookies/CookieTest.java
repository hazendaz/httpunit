/*
 * MIT License
 *
 * Copyright 2011-2023 Russell Gold
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

import static org.junit.Assert.*;

import com.meterware.pseudoserver.HttpUserAgentTest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 */
public class CookieTest {

    @Before
    public void setUp() throws Exception {
        CookieProperties.reset();
    }


    @Test
    public void testSimpleCookies() throws Exception {
        CookieJar jar = new CookieJar(
                new TestSource(new URL("http://www.meterware.com"),
                        new String[]{"Reason=; path=/",
                                "age=12, name= george",
                                "type=short",
                                "funky=ab$==",
                                "p30waco_sso=3.0,en,us,AMERICA,Drew;path=/, PORTAL30_SSO_TEST=X",
                                "SESSION_ID=17585,Dzm5LzbRPnb95QkUyIX+7w5RDT7p6OLuOVZ91AMl4hsDATyZ1ej+FA==; path=/;"}));
        assertEquals("cookie 'Reason' value", "", jar.getCookieValue("Reason"));
        assertEquals("cookie 'age' value", "12", jar.getCookieValue("age"));
        assertEquals("cookie 'name' value", "george", jar.getCookieValue("name"));
        assertEquals("cookie 'type' value", "short", jar.getCookieValue("type"));
        assertEquals("cookie 'funky' value", "ab$==", jar.getCookieValue("funky"));
        assertEquals("cookie 'p30waco_sso' value", "3.0,en,us,AMERICA,Drew", jar.getCookieValue("p30waco_sso"));
        assertEquals("cookie 'PORTAL30_SSO_TEST' value", "X", jar.getCookieValue("PORTAL30_SSO_TEST"));
        assertEquals("cookie 'SESSION_ID' value", "17585,Dzm5LzbRPnb95QkUyIX+7w5RDT7p6OLuOVZ91AMl4hsDATyZ1ej+FA==", jar.getCookieValue("SESSION_ID"));
    }

    /**
     * test for double quoted cookies suggested by Mario V
     * disabled since it indeed fails - FIXME - we need a patch here
     *
     * @throws Exception
     */
    public void xtestDoubleQuoteCookies() throws Exception {
        CookieJar jar = new CookieJar(
                new TestSource(new URL("http://www.meterware.com"),
                        new String[]{"NewUniversalCookie=\"mmmmmmmmmmmmmmm==mmmmmmm mmmmmmm\"; Path=/"}));
        Collection cookies = jar.getCookies();
        assertTrue("There should only be one cookie but there are " + cookies.size(), cookies.size() == 1);
        assertEquals("cookie 'NewUniversalCookie' value", "mmmmmmmmmmmmmmm==mmmmmmm mmmmmmm", jar.getCookieValue("NewUniversalCookie"));
    }


    @Test
    public void testCookieMatching() throws Exception {
        assertTrue("Universal cookie could not be sent", new Cookie("name", "value").mayBeSentTo(new URL("http://httpunit.org/anywhere")));

        checkMatching(1, true, new URL("http://www.meterware.com/servlets/sample"), "www.meterware.com", "/servlets/sample");

        checkMatching(2, false, new URL("http://www.meterware.com/servlets/sample"), "meterware.com", "/");
        checkMatching(3, true, new URL("http://www.meterware.com/servlets/sample"), ".meterware.com", "/");
        checkMatching(4, false, new URL("http://www.meterware.com/servlets/sample"), ".httpunit.org", "/");

        checkMatching(5, true, new URL("http://www.meterware.com/servlets/sample"), "www.meterware.com", "/servlets");
        checkMatching(6, false, new URL("http://www.meterware.com/servlets/sample"), "www.meterware.com", "/servlets/sample/data");
    }


    private void checkMatching(int index, boolean success, URL url, String domain, String path) {
        HashMap attributes = new HashMap();
        attributes.put("path", path);
        attributes.put("domain", domain);
        Cookie cookie = new Cookie("name", "value", attributes);
        if (success) {
            assertTrue("Cookie " + index + " did not allow " + url, cookie.mayBeSentTo(url));
        } else {
            assertFalse("Cookie " + index + " allowed " + url, cookie.mayBeSentTo(url));
        }
    }


    /**
     * check the CookieAcceptance
     *
     * @throws Exception
     */
    @Test
    public void testCookieAcceptance() throws Exception {
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
     * check whether the given cookie is accepted
     *
     * @param index
     * @param shouldAccept
     * @param urlString
     * @param specifiedDomain
     * @param specifiedPath
     * @throws MalformedURLException
     */

    private void checkAcceptance(int index, boolean shouldAccept, String urlString,
                                 String specifiedDomain, String specifiedPath) throws MalformedURLException {
    	
        CookieJar jar = newJar(urlString, specifiedDomain, specifiedPath);

        if (shouldAccept) {
        	// modified for Bugreport 2825872 Cookie domains not stored correctly - ID: 2825872
        	// http://sourceforge.net/tracker/?func=detail&aid=2825872&group_id=6550&atid=106550
        	Cookie cookie = jar.getCookie( "name" );
        	assertNotNull( "Rejected cookie " + index + "( " + specifiedDomain + " from " + urlString + ") should have been accepted", cookie );
        	URL url = new URL( "http://" + urlString );
        	assertTrue( "Cookie " + index + " should be sent to the url ", cookie.mayBeSentTo(url));

        } else {
            assertNull("Cookie " + index + " should have been rejected", jar.getCookie("name"));
        }
    }


    @Test
    public void testCookieDefaults() throws Exception {
        checkDefaults(1, "www.meterware.com/servlets/special", ".meterware.com", "/servlets", ".meterware.com", "/servlets");
        checkDefaults(2, "www.meterware.com/servlets/special/myServlet", null, null, "www.meterware.com", "/servlets/special");
    }


    private void checkDefaults(int index, String urlString, String specifiedDomain, String specifiedPath,
                               String expectedDomain, String expectedPath) throws MalformedURLException {
        CookieJar jar = newJar(urlString, specifiedDomain, specifiedPath);
        assertNotNull("case " + index + " domain is null", jar.getCookie("name").getDomain());
        assertEquals("case " + index + " domain", expectedDomain, jar.getCookie("name").getDomain());
        assertNotNull("case " + index + " path is null", jar.getCookie("name").getPath());
        assertEquals("case " + index + " path", expectedPath, jar.getCookie("name").getPath());
    }


    private CookieJar newJar(String urlString, String specifiedDomain, String specifiedPath) throws MalformedURLException {
        StringBuffer header = new StringBuffer("name=value");
        if (specifiedDomain != null) header.append("; domain=").append(specifiedDomain);
        if (specifiedPath != null) header.append("; path=").append(specifiedPath);

        return new CookieJar(new TestSource(new URL("http://" + urlString), header.toString()));
    }

    /**
     * test cookie age and expiration handling
     * see also Friday Fun: I Hate Cookies
     * http://www.mnot.net/blog/2006/10/27/cookie_fun
     *
     * @throws Exception when an unexpected error occurs
     */
    @Test
    public void testCookieAge() throws Exception {
        String ages[] = {"max-age=5000",
                "Max-Age=3000",
                "expires=Tue, 29-Mar-2005 19:30:42 GMT; Max-Age=2592000",
                "Max-Age=2592000;expires=Tue, 29-Mar-2005 19:30:42 GMT",
                "expires=Tue, 29-Mar-2005 19:30:42 GMT",
                "Expires=Wednesday, 01-Jan-1970 00:00:00 GMT"
        };
        long now = System.currentTimeMillis();
        long expectedMilliSeconds[] = {now + 5000 * 1000,
                now + 3000 * 1000,
                now + 2592000 * 1000,
                now + 2592000 * 1000,
                1112124642000l,
                0};

        for (int i = 0; i < ages.length; i++) {
            String cookieName = "cookie" + i;
            String header = cookieName + "=cookievalue;" + ages[i];
            TestSource source = new TestSource(new URL("http://www.somedomain.com/somepath/"), header);
            CookieJar jar = new CookieJar(source);
            Cookie cookie = jar.getCookie(cookieName);
            assertNotNull(cookieName + " not null", cookie);

            long expiredTime = cookie.getExpiredTime();
            int grace = 3000;
            assertTrue(cookieName + " '" + ages[i] + "' expiration expect on or after" +
                    expectedMilliSeconds[i] + " but was " + expiredTime,
                    expectedMilliSeconds[i] <= expiredTime);
            assertTrue(cookieName + " '" + ages[i] + "' expiration expect before " +
                    (expectedMilliSeconds[i] + grace) + " but was " + expiredTime,
                    (expectedMilliSeconds[i]) + grace > expiredTime);
            //  assertEquals( cookieName + " expiration", expiredTime, expectedMilliSeconds[i] );
        }
    }


    @Test
    public void testHeaderGeneration() throws Exception {
        CookieJar jar = new CookieJar();
        jar.putCookie("zero", "nil");
        jar.updateCookies(newJar("www.meterware.com/servlets/standard/AServlet", "first=ready, gone=expired;max-age=0"));
        jar.updateCookies(newJar("www.meterware.com/servlets/AnotherServlet", "second=set;max-age=1000"));
        jar.updateCookies(newJar("www.httpunit.org", "zero=go; domain=.httpunit.org"));
        jar.updateCookies(newJar("meterware.com", "fourth=money"));

        checkHeader(1, jar, "first=ready; second=set; zero=nil", "www.meterware.com/servlets/standard/Count");
        checkHeader(2, jar, "second=set; zero=nil", "www.meterware.com/servlets/special/Divide");
        checkHeader(3, jar, "zero=go", "fancy.httpunit.org/servlets/AskMe");

        HttpUserAgentTest.assertMatchingSet("Cookie names",
                new String[]{"zero", "zero", "first", "second", "fourth", "gone"},
                jar.getCookieNames());
    }

    /**
     * @throws Exception
     */
    @Test
    public void testDrupalCookieInteraction() throws Exception {
        CookieJar jar = new CookieJar();
        jar.putSingleUseCookie("SESS1234", "1234", ".drupalsite.org", "/");
        Cookie cookie = jar.getCookie("SESS1234");
        assertTrue(cookie != null);
        assertEquals(cookie.getDomain(), ".drupalsite.org");
        assertEquals(cookie.getValue(), "1234");
        assertEquals(cookie.getPath(), "/");
        assertEquals(1, jar.getCookies().size());

        CookieJar jar1 = new CookieJar();
        jar1.putSingleUseCookie("SESS1234", "deleted", "www.drupalsite.org", "/");
        jar.updateCookies(jar1);

        cookie = jar.getCookie("SESS1234");
        assertTrue(cookie != null);
        assertEquals(cookie.getDomain(), "www.drupalsite.org");
        assertEquals(cookie.getValue(), "deleted");
        assertEquals(cookie.getPath(), "/");
        assertEquals(1, jar.getCookies().size());

        CookieJar jar2 = new CookieJar();
        jar2.putSingleUseCookie("SESS1234", "4321", ".drupalsite.org", "/");
        jar.updateCookies(jar2);

        cookie = jar.getCookie("SESS1234");
        assertTrue(cookie != null);
        assertEquals(cookie.getDomain(), ".drupalsite.org");
        assertEquals(cookie.getValue(), "4321");
        assertEquals(cookie.getPath(), "/");
        assertEquals(1, jar.getCookies().size());

    }

    /**
     * test for [ 1488617 ] alternate patch for cookie bug #1371204
     *
     * @throws Exception
     */
    @Test
    public void testSingleUseCookie() throws Exception {
        CookieJar jar = new CookieJar();
        jar.putSingleUseCookie("zero", "nil", "sourceforge.net", "test/me");
        Cookie cookie = jar.getCookie("zero");
        assertTrue(cookie != null);
        assertEquals(cookie.getDomain(), "sourceforge.net");
        assertEquals(cookie.getValue(), "nil");
        assertEquals(cookie.getPath(), "test/me");
    }

    /**
     * test for bug report [ 1672385 ] HttpOnly cookie looses all cookie info
     * extended according to comment of 2010-04-22
     *
     * @throws Exception
     */
    @Test
    public void testHttpOnlyCookies() throws Exception {
        CookieJar jar = new CookieJar(
                new TestSource(new URL("http://www.meterware.com"),
                        new String[]{"myStuff=1234; path=/foo; HttpOnly"}));
        assertEquals("cookie 'myStuff' value", "1234", jar.getCookieValue("myStuff"));
        // comment of 2010-04-22
        String path = jar.getCookie("myStuff").getPath();
        assertEquals("cookie 'myStuff' path", "/foo", path);
    }

    /**
     * test for bug report [ 2076028 ] Cookies are handled incorrectly
     * should also fit duplicate bug report 2871999
     * https://sourceforge.net/tracker/?func=detail&aid=2871999&group_id=6550&atid=106550
     */
    @Test
    public void testHttpOnlyCookiePath() throws Exception {
        CookieJar jar = new CookieJar(
                new TestSource(new URL("http://www.meterware.com"),
                        new String[]{"myStuff=1234; path=/; HttpOnly"}));
        Cookie cookie = jar.getCookie("myStuff");
        String expected = "/";
        assertEquals("The cookie should have the path '" + expected + "' but has " + cookie.getPath(), cookie.getPath(), expected);
    }

    /**
     * test for bug report [ 1533762 ] Valid cookies are rejected
     * by Alexey Bulat
     * TODO enable when working patch is available
     *
     * @throws Exception
     */
    public void xtestCookiesRejection1533762() throws Exception {
        checkAcceptance(1, true, "admin.automation.testing.com.ru", ".admin.automation.testing.com.ru", null);
        checkAcceptance(2, true, "admin.automation.testing.com.ru", ".testing.com.ru", null);
    }


    private void checkHeader(int index, CookieJar jar, String expectedHeader, String targetURLString) throws MalformedURLException {
        assertEquals("header " + index, expectedHeader, jar.getCookieHeaderField(new URL("http://" + targetURLString)));
    }


    @Test
    public void testCookieReplacement() throws Exception {
        CookieJar jar = new CookieJar();
        jar.updateCookies(newJar("www.meterware.com/servlets/standard", "first=ready"));
        jar.updateCookies(newJar("meterware.com/servlets/standard", "second=more"));
        jar.updateCookies(newJar("www.meterware.com/servlets", "third=day"));
        jar.updateCookies(newJar("www.meterware.com/servlets", "third=tomorrow"));

        checkHeader(1, jar, "first=ready; third=tomorrow", "www.meterware.com/servlets/standard");
    }


    private CookieJar newJar(String urlString, String setCookieHeader) throws MalformedURLException {
        return new CookieJar(new TestSource(new URL("http://" + urlString), setCookieHeader));
    }


    @Test
    public void testLenientMatching() throws Exception {
        CookieProperties.setDomainMatchingStrict(false);
        checkAcceptance(1, true, "www.some.meterware.com/servlets/special", ".meterware.com", null);
        checkAcceptance(2, false, "www.meterware.com/servlets/special", ".meterware.com", "/servlets/ordinary");
        checkAcceptance(3, true, "www.meterware.com/servlets/special", "www.meterware.com", null);
        // missing leading dot case (yahoo cookies seem to behave like this - seems to be non RFC 2109 compliant ...)
        checkAcceptance(4, true, "www.meterware.com/servlets/special", "meterware.com", null);

        CookieProperties.setPathMatchingStrict(false);
        checkAcceptance(11, true, "www.meterware.com/servlets/special", ".meterware.com", "/servlets/ordinary");
        checkMatching(12, true, new URL("http://www.meterware.com/servlets/sample"), "www.meterware.com", "/servlets/sample/data");
    }


    @Test
    public void testRejectionCallbacks() throws Exception {
        MockListener listener = new MockListener();
        CookieProperties.addCookieListener(listener);

        checkCallback(listener, 1, 0, "www.meterware.com/servlets/special", null, null);
        checkCallback(listener, 2, CookieListener.PATH_NOT_PREFIX, "www.meterware.com/servlets/special", ".meterware.com", "/servlets/ordinary");
        checkCallback(listener, 3, CookieListener.DOMAIN_ONE_DOT, "www.meterware.com/servlets/special", ".com", null);
        checkCallback(listener, 4, CookieListener.DOMAIN_NOT_SOURCE_SUFFIX, "www.meterware.com/servlets/special", ".httpunit.org", null);
        checkCallback(listener, 5, CookieListener.DOMAIN_TOO_MANY_LEVELS, "www.some.meterware.com/servlets/special", ".meterware.com", null);
    }


    /**
     * check the cookieListener call Back
     *
     * @param listener
     * @param index
     * @param status
     * @param urlString
     * @param specifiedDomain
     * @param specifiedPath
     * @throws MalformedURLException
     */
    private void checkCallback(MockListener listener, int index, int status, String urlString,
                               String specifiedDomain, String specifiedPath) throws MalformedURLException {
        if (status == 0) {
            listener.expectAcceptance(index);
        } else if (status == CookieListener.PATH_NOT_PREFIX) {
            listener.expectRejection(index, "name", status, specifiedPath);
        } else {
            listener.expectRejection(index, "name", status, specifiedDomain);
        }
        newJar(urlString, specifiedDomain, specifiedPath);
        if (status != 0) listener.confirmRejection();
    }


    private class MockListener implements CookieListener {

        private int _reason;
        private String _attribute;
        private String _cookieName;
        private boolean _rejected;
        private int _cookieNum;


        void expectAcceptance(int cookieNum) {
            _cookieNum = cookieNum;
            _reason = -1;
        }


        void expectRejection(int cookieNum, String cookieName, int reason, String attribute) {
            _cookieNum = cookieNum;
            _reason = reason;
            _attribute = attribute;
            _cookieName = cookieName;
            _rejected = false;
        }


        void confirmRejection() {
            assertTrue("Cookie " + _cookieNum + " was not logged as rejected", _rejected);
        }


        public void cookieRejected(String name, int reason, String attribute) {
            _rejected = true;
            assertEquals("Cookie " + _cookieNum + " rejection code", _reason, reason);
            if (_attribute != null)
                assertEquals("Cookie " + _cookieNum + " rejected attribute", _attribute, attribute);
            if (_cookieName != null) assertEquals("Cookie " + _cookieNum + " name", _cookieName, name);
        }
    }


    /**
     * create a TestSource for Cookies
     */
    private class TestSource implements CookieSource {

        private URL _sourceURL;
        private String[] _headers;

        /**
         * construct a TestSource form a single header string
         *
         * @param sourceURL
         * @param header
         */

        public TestSource(URL sourceURL, String header) {
            this(sourceURL, new String[]{header});
        }


        public TestSource(URL sourceURL, String[] headers) {
            _sourceURL = sourceURL;
            _headers = headers;
        }


        public URL getURL() {
            return _sourceURL;
        }


        public String[] getHeaderFields(String fieldName) {
            return fieldName.equalsIgnoreCase("set-cookie") ? _headers : new String[0];
        }
    }


}

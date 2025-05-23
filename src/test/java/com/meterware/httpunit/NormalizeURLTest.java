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

import java.net.URL;

import org.junit.jupiter.api.Test;

/**
 * Verifies handling of URLs with odd features.
 *
 * @author <a href="mailto:ddkilzer@users.sourceforge.net">David D. Kilzer</a>
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 */
class NormalizeURLTest extends HttpUnitTest {

    /*
     * Test various combinations of URLs with NO trailing slash (and no directory or file part)
     */

    @Test
    void hostnameNoSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name");
        assertEquals("http://host.name", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void hostnamePortNoSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name:80");
        assertEquals("http://host.name:80", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void usernameHostnameNoSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://username@host.name");
        assertEquals("http://username@host.name", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void usernamePasswordHostnameNoSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://username:password@host.name");
        assertEquals("http://username:password@host.name", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void usernameHostnamePortNoSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://username@host.name:80");
        assertEquals("http://username@host.name:80", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void usernamePasswordHostnamePortNoSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://username:password@host.name:80");
        assertEquals("http://username:password@host.name:80", request.getURL().toExternalForm(), "URL");
    }

    /*
     * Test various combinations of URLs WITH trailing slash (and no directory or file part)
     */

    @Test
    void hostnameSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/");
        assertEquals("http://host.name/", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void hostnamePortSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name:80/");
        assertEquals("http://host.name:80/", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void usernameHostnameSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://username@host.name/");
        assertEquals("http://username@host.name/", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void usernamePasswordHostnameSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://username:password@host.name/");
        assertEquals("http://username:password@host.name/", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void usernameHostnamePortSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://username@host.name:80/");
        assertEquals("http://username@host.name:80/", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void usernamePasswordHostnamePortSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://username:password@host.name:80/");
        assertEquals("http://username:password@host.name:80/", request.getURL().toExternalForm(), "URL");
    }

    /*
     * Test various combinations of normal URLs with 0 to 2 directories and a filename
     */

    @Test
    void hostnameFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/file.html");
        assertEquals("http://host.name/file.html", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void hostnameDirectoryFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory/file.html");
        assertEquals("http://host.name/directory/file.html", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void hostnameDirectory1Directory2File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory1/directory2/file.html");
        assertEquals("http://host.name/directory1/directory2/file.html", request.getURL().toExternalForm(), "URL");
    }

    /*
     * Test various combinations of normal URLs with directories requesting a default index page
     */

    @Test
    void hostnameDirectory() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory/");
        assertEquals("http://host.name/directory/", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void hostnameDirectory1Directory2() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory1/directory2/");
        assertEquals("http://host.name/directory1/directory2/", request.getURL().toExternalForm(), "URL");
    }

    /*
     * Torture tests with URLs containing directory navigation ('.' and '..')
     */

    @Test
    void tortureHostnameDotFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/./file.html");
        assertEquals("http://host.name/file.html", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void tortureHostnameDotDirectoryFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/./directory/file.html");
        assertEquals("http://host.name/directory/file.html", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void tortureHostnameDotDirectoryDotFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/./directory/./file.html");
        assertEquals("http://host.name/directory/file.html", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void tortureHostnameDotDirectoryDotDotFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/./directory/../file.html");
        assertEquals("http://host.name/file.html", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void tortureHostnameDotDirectory1Directory2File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/./directory1/directory2/file.html");
        assertEquals("http://host.name/directory1/directory2/file.html", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void tortureHostnameDotDirectory1DotDirectory2File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/./directory1/./directory2/file.html");
        assertEquals("http://host.name/directory1/directory2/file.html", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void tortureHostnameDotDirectory1DotDirectory2DotFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/./directory1/./directory2/./file.html");
        assertEquals("http://host.name/directory1/directory2/file.html", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void tortureHostnameDirectory1Directory2File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory1/directory2/file.html");
        assertEquals("http://host.name/directory1/directory2/file.html", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void tortureHostnameDirectory1DotDotDirectory2File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory1/../directory2/file.html");
        assertEquals("http://host.name/directory2/file.html", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void tortureHostnameDirectory1DotDotDirectory2DotDotFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory1/../directory2/../file.html");
        assertEquals("http://host.name/file.html", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void tortureHostnameDirectory1Directory2DotDotDotDotFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory1/directory2/../../file.html");
        assertEquals("http://host.name/file.html", request.getURL().toExternalForm(), "URL");
    }

    /**
     * patch by Serge Maslyukov
     *
     * @throws Exception
     */
    @Test
    void tripleDottedPath() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://en.wikipedia.org/wiki/...And_Found");
        assertEquals("http://en.wikipedia.org/wiki/...And_Found", request.getURL().toExternalForm(), "URL");
    }

    /*
     * Test relative URLs with directory navigation.
     */
    @Test
    void relativePathDotDotFile() throws Exception {
        WebRequest request = new GetMethodWebRequest(new URL("http://host.name/directory1/file.html"),
                "../directory2/file.html");
        assertEquals("http://host.name/directory2/file.html", request.getURL().toExternalForm(), "URL");
    }

    /*
     * Torture tests with URLs containing multiple slashes
     */

    @Test
    void hostnameSlash1File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name//file.html");
        assertEquals("http://host.name/file.html", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void hostnameSlash2File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name///file.html");
        assertEquals("http://host.name/file.html", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void hostnameSlash3File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name////file.html");
        assertEquals("http://host.name/file.html", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void hostnameSlash1DirectoryFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory//file.html");
        assertEquals("http://host.name/directory/file.html", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void hostnameSlash2DirectoryFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory///file.html");
        assertEquals("http://host.name/directory/file.html", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void hostnameSlash3DirectoryFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory////file.html");
        assertEquals("http://host.name/directory/file.html", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void hostnameSlash1Directory1Directory2File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name//directory1//directory2//file.html");
        assertEquals("http://host.name/directory1/directory2/file.html", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void hostnameSlash2Directory1Directory2File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name///directory1///directory2///file.html");
        assertEquals("http://host.name/directory1/directory2/file.html", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void hostnameSlash3Directory1Directory2File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name////directory1////directory2////file.html");
        assertEquals("http://host.name/directory1/directory2/file.html", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void hostnameSlash1Directory() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name//directory//");
        assertEquals("http://host.name/directory/", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void hostnameSlash2Directory() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name///directory///");
        assertEquals("http://host.name/directory/", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void hostnameSlash3Directory() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name////directory////");
        assertEquals("http://host.name/directory/", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void hostnameSlash1Directory1Directory2() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name//directory1//directory2//");
        assertEquals("http://host.name/directory1/directory2/", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void hostnameSlash2Directory1Directory2() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name///directory1///directory2///");
        assertEquals("http://host.name/directory1/directory2/", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void hostnameSlash3Directory1Directory2() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name////directory1////directory2////");
        assertEquals("http://host.name/directory1/directory2/", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void pathElementLeadingDot() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host/context/.src/page");
        assertEquals("http://host/context/.src/page", request.getURL().toExternalForm(), "URL");
    }

    @Test
    void urlAsParameter() throws Exception {
        String desiredUrl = "http://localhost:3333/composite/addobserver?url=http://localhost:8081/";
        WebRequest request = new GetMethodWebRequest(desiredUrl);
        assertEquals(desiredUrl, request.getURL().toExternalForm(), "URL");
    }

    @Test
    void slashesInParameter() throws Exception {
        String desiredUrl = "http://localhost:8888/bug2295681/TestServlet?abc=abc&aaa=%%%&bbb=---%2d%2F%*%aa&ccc=yahoo@yahoo.com&ddd=aaa/../../&eee=/.";
        WebRequest request = new GetMethodWebRequest(desiredUrl);
        assertEquals(desiredUrl, request.getURL().toExternalForm(), "URL");
    }

}

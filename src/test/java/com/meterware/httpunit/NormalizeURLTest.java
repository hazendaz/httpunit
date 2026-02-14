/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;

import org.junit.jupiter.api.Test;

/**
 * Verifies handling of URLs with odd features.
 */
class NormalizeURLTest extends HttpUnitTest {

    /*
     * Test various combinations of URLs with NO trailing slash (and no directory or file part)
     */

    /**
     * Hostname no slash.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void hostnameNoSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name");
        assertEquals("http://host.name", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Hostname port no slash.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void hostnamePortNoSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name:80");
        assertEquals("http://host.name:80", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Username hostname no slash.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void usernameHostnameNoSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://username@host.name");
        assertEquals("http://username@host.name", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Username password hostname no slash.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void usernamePasswordHostnameNoSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://username:password@host.name");
        assertEquals("http://username:password@host.name", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Username hostname port no slash.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void usernameHostnamePortNoSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://username@host.name:80");
        assertEquals("http://username@host.name:80", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Username password hostname port no slash.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void usernamePasswordHostnamePortNoSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://username:password@host.name:80");
        assertEquals("http://username:password@host.name:80", request.getURL().toExternalForm(), "URL");
    }

    /*
     * Test various combinations of URLs WITH trailing slash (and no directory or file part)
     */

    /**
     * Hostname slash.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void hostnameSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/");
        assertEquals("http://host.name/", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Hostname port slash.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void hostnamePortSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name:80/");
        assertEquals("http://host.name:80/", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Username hostname slash.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void usernameHostnameSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://username@host.name/");
        assertEquals("http://username@host.name/", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Username password hostname slash.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void usernamePasswordHostnameSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://username:password@host.name/");
        assertEquals("http://username:password@host.name/", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Username hostname port slash.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void usernameHostnamePortSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://username@host.name:80/");
        assertEquals("http://username@host.name:80/", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Username password hostname port slash.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void usernamePasswordHostnamePortSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://username:password@host.name:80/");
        assertEquals("http://username:password@host.name:80/", request.getURL().toExternalForm(), "URL");
    }

    /*
     * Test various combinations of normal URLs with 0 to 2 directories and a filename
     */

    /**
     * Hostname file.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void hostnameFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/file.html");
        assertEquals("http://host.name/file.html", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Hostname directory file.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void hostnameDirectoryFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory/file.html");
        assertEquals("http://host.name/directory/file.html", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Hostname directory 1 directory 2 file.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void hostnameDirectory1Directory2File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory1/directory2/file.html");
        assertEquals("http://host.name/directory1/directory2/file.html", request.getURL().toExternalForm(), "URL");
    }

    /*
     * Test various combinations of normal URLs with directories requesting a default index page
     */

    /**
     * Hostname directory.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void hostnameDirectory() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory/");
        assertEquals("http://host.name/directory/", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Hostname directory 1 directory 2.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void hostnameDirectory1Directory2() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory1/directory2/");
        assertEquals("http://host.name/directory1/directory2/", request.getURL().toExternalForm(), "URL");
    }

    /*
     * Torture tests with URLs containing directory navigation ('.' and '..')
     */

    /**
     * Torture hostname dot file.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void tortureHostnameDotFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/./file.html");
        assertEquals("http://host.name/file.html", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Torture hostname dot directory file.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void tortureHostnameDotDirectoryFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/./directory/file.html");
        assertEquals("http://host.name/directory/file.html", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Torture hostname dot directory dot file.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void tortureHostnameDotDirectoryDotFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/./directory/./file.html");
        assertEquals("http://host.name/directory/file.html", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Torture hostname dot directory dot dot file.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void tortureHostnameDotDirectoryDotDotFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/./directory/../file.html");
        assertEquals("http://host.name/file.html", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Torture hostname dot directory 1 directory 2 file.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void tortureHostnameDotDirectory1Directory2File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/./directory1/directory2/file.html");
        assertEquals("http://host.name/directory1/directory2/file.html", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Torture hostname dot directory 1 dot directory 2 file.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void tortureHostnameDotDirectory1DotDirectory2File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/./directory1/./directory2/file.html");
        assertEquals("http://host.name/directory1/directory2/file.html", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Torture hostname dot directory 1 dot directory 2 dot file.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void tortureHostnameDotDirectory1DotDirectory2DotFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/./directory1/./directory2/./file.html");
        assertEquals("http://host.name/directory1/directory2/file.html", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Torture hostname directory 1 directory 2 file.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void tortureHostnameDirectory1Directory2File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory1/directory2/file.html");
        assertEquals("http://host.name/directory1/directory2/file.html", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Torture hostname directory 1 dot dot directory 2 file.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void tortureHostnameDirectory1DotDotDirectory2File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory1/../directory2/file.html");
        assertEquals("http://host.name/directory2/file.html", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Torture hostname directory 1 dot dot directory 2 dot dot file.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void tortureHostnameDirectory1DotDotDirectory2DotDotFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory1/../directory2/../file.html");
        assertEquals("http://host.name/file.html", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Torture hostname directory 1 directory 2 dot dot dot dot file.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void tortureHostnameDirectory1Directory2DotDotDotDotFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory1/directory2/../../file.html");
        assertEquals("http://host.name/file.html", request.getURL().toExternalForm(), "URL");
    }

    /**
     * patch by Serge Maslyukov.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void tripleDottedPath() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://en.wikipedia.org/wiki/...And_Found");
        assertEquals("http://en.wikipedia.org/wiki/...And_Found", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Relative path dot dot file.
     *
     * @throws Exception
     *             the exception
     */
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

    /**
     * Hostname slash 1 file.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void hostnameSlash1File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name//file.html");
        assertEquals("http://host.name/file.html", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Hostname slash 2 file.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void hostnameSlash2File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name///file.html");
        assertEquals("http://host.name/file.html", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Hostname slash 3 file.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void hostnameSlash3File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name////file.html");
        assertEquals("http://host.name/file.html", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Hostname slash 1 directory file.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void hostnameSlash1DirectoryFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory//file.html");
        assertEquals("http://host.name/directory/file.html", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Hostname slash 2 directory file.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void hostnameSlash2DirectoryFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory///file.html");
        assertEquals("http://host.name/directory/file.html", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Hostname slash 3 directory file.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void hostnameSlash3DirectoryFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory////file.html");
        assertEquals("http://host.name/directory/file.html", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Hostname slash 1 directory 1 directory 2 file.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void hostnameSlash1Directory1Directory2File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name//directory1//directory2//file.html");
        assertEquals("http://host.name/directory1/directory2/file.html", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Hostname slash 2 directory 1 directory 2 file.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void hostnameSlash2Directory1Directory2File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name///directory1///directory2///file.html");
        assertEquals("http://host.name/directory1/directory2/file.html", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Hostname slash 3 directory 1 directory 2 file.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void hostnameSlash3Directory1Directory2File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name////directory1////directory2////file.html");
        assertEquals("http://host.name/directory1/directory2/file.html", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Hostname slash 1 directory.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void hostnameSlash1Directory() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name//directory//");
        assertEquals("http://host.name/directory/", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Hostname slash 2 directory.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void hostnameSlash2Directory() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name///directory///");
        assertEquals("http://host.name/directory/", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Hostname slash 3 directory.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void hostnameSlash3Directory() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name////directory////");
        assertEquals("http://host.name/directory/", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Hostname slash 1 directory 1 directory 2.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void hostnameSlash1Directory1Directory2() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name//directory1//directory2//");
        assertEquals("http://host.name/directory1/directory2/", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Hostname slash 2 directory 1 directory 2.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void hostnameSlash2Directory1Directory2() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name///directory1///directory2///");
        assertEquals("http://host.name/directory1/directory2/", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Hostname slash 3 directory 1 directory 2.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void hostnameSlash3Directory1Directory2() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name////directory1////directory2////");
        assertEquals("http://host.name/directory1/directory2/", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Path element leading dot.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void pathElementLeadingDot() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host/context/.src/page");
        assertEquals("http://host/context/.src/page", request.getURL().toExternalForm(), "URL");
    }

    /**
     * Url as parameter.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void urlAsParameter() throws Exception {
        String desiredUrl = "http://localhost:3333/composite/addobserver?url=http://localhost:8081/";
        WebRequest request = new GetMethodWebRequest(desiredUrl);
        assertEquals(desiredUrl, request.getURL().toExternalForm(), "URL");
    }

    /**
     * Slashes in parameter.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void slashesInParameter() throws Exception {
        String desiredUrl = "http://localhost:8888/bug2295681/TestServlet?abc=abc&aaa=%%%&bbb=---%2d%2F%*%aa&ccc=yahoo@yahoo.com&ddd=aaa/../../&eee=/.";
        WebRequest request = new GetMethodWebRequest(desiredUrl);
        assertEquals(desiredUrl, request.getURL().toExternalForm(), "URL");
    }

}

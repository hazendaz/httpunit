/*
 * MIT License
 *
 * Copyright 2011-2024 Russell Gold
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
package com.meterware.pseudoserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import org.junit.Rule;

/**
 * A base class for test cases that use the pseudo server.
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class HttpUserAgentTest {

    @Rule
    public PseudoServerTestSupport testSupport = new PseudoServerTestSupport();

    protected void defineResource(String resourceName, PseudoServlet servlet) {
        testSupport.defineResource(resourceName, servlet);
    }

    protected void defineResource(String resourceName, String value) {
        testSupport.defineResource(resourceName, value);
    }

    protected void defineResource(String resourceName, byte[] value, String contentType) {
        testSupport.defineResource(resourceName, value, contentType);
    }

    protected void defineResource(String resourceName, String value, int statusCode) {
        testSupport.defineResource(resourceName, value, statusCode);
    }

    protected void defineResource(String resourceName, String value, String contentType) {
        testSupport.defineResource(resourceName, value, contentType);
    }

    protected void addResourceHeader(String resourceName, String header) {
        testSupport.addResourceHeader(resourceName, header);
    }

    protected void setResourceCharSet(String resourceName, String setName, boolean reportCharSet) {
        testSupport.setResourceCharSet(resourceName, setName, reportCharSet);
    }

    /**
     * define a Web Page with the given page name and boy adding the html and body tags with pageName as the title of
     * the page use the given xml names space if it is not null
     *
     * @param xmlns
     * @param pageName
     * @param body
     */
    protected void defineWebPage(String xmlns, String pageName, String body) {
        testSupport.defineWebPage(xmlns, pageName, body);
    }

    /**
     * define a Web Page with the given page name and boy adding the html and body tags with pageName as the title of
     * the page
     *
     * @param pageName
     * @param body
     */
    protected void defineWebPage(String pageName, String body) {
        testSupport.defineWebPage(pageName, body);
    }

    protected void mapToClasspath(String directory) {
        testSupport.mapToClasspath(directory);
    }

    protected PseudoServer getServer() {
        return testSupport.getServer();
    }

    protected void setServerDebug(boolean enabled) {
        testSupport.setServerDebug(enabled);
    }

    protected String getHostPath() {
        return testSupport.getHostPath();
    }

    protected int getHostPort() throws IOException {
        return testSupport.getHostPort();
    }

    protected void assertEqualQueries(String query1, String query2) {
        assertEquals(new QuerySpec(query1), new QuerySpec(query2));
    }

    protected void assertImplement(String comment, Object[] objects, Class expectedClass) {
        if (objects.length == 0) {
            fail("No " + comment + " found.");
        }
        for (Object object : objects) {
            assertImplements(comment, object, expectedClass);
        }
    }

    protected void assertImplements(String comment, Object object, Class expectedClass) {
        if (object == null) {
            fail(comment + " should be of class " + expectedClass.getName() + " but is null");
        } else if (!expectedClass.isInstance(object)) {
            fail(comment + " should be of class " + expectedClass.getName() + " but is " + object.getClass().getName());
        }
    }

    protected void assertMatchingSet(String comment, Object[] expected, Enumeration found) {
        Vector foundItems = new Vector();
        while (found.hasMoreElements()) {
            foundItems.addElement(found.nextElement());
        }

        assertMatchingSet(comment, expected, foundItems);
    }

    private void assertMatchingSet(String comment, Object[] expected, Vector foundItems) {
        Vector expectedItems = new Vector();
        for (Object element : expected) {
            expectedItems.addElement(element);
        }
        for (Object element : expected) {
            if (!foundItems.contains(element)) {
                fail(comment + ": expected " + asText(expected) + " but missing " + element);
            } else {
                foundItems.removeElement(element);
            }
        }

        if (!foundItems.isEmpty()) {
            fail(comment + ": expected " + asText(expected) + " but found superfluous" + foundItems.firstElement());
        }
    }

    public static void assertMatchingSet(String comment, Object[] expected, Object[] found) {
        Vector foundItems = new Vector();
        for (Object element : found) {
            foundItems.addElement(element);
        }

        Vector expectedItems = new Vector();

        for (Object element : expected) {
            expectedItems.addElement(element);
        }

        for (Object element : expected) {
            if (!foundItems.contains(element)) {
                fail(comment + ": expected " + asText(expected) + " but found " + asText(found));
            } else {
                foundItems.removeElement(element);
            }
        }

        for (Object element : found) {
            if (!expectedItems.contains(element)) {
                fail(comment + ": expected " + asText(expected) + " but found " + asText(found));
            } else {
                expectedItems.removeElement(element);
            }
        }

        if (!foundItems.isEmpty()) {
            fail(comment + ": expected " + asText(expected) + " but found " + asText(found));
        }
    }

    public static String asText(Object[] args) {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < args.length; i++) {
            if (i != 0) {
                sb.append(",");
            }
            sb.append('"').append(args[i]).append('"');
        }
        sb.append("}");
        return sb.toString();
    }

    protected String asBytes(String s) {
        StringBuilder sb = new StringBuilder();
        char[] chars = s.toCharArray();
        for (char element : chars) {
            sb.append(Integer.toHexString(element)).append(" ");
        }
        return sb.toString();
    }

    static class QuerySpec {
        QuerySpec(String urlString) {
            if (urlString.indexOf('?') < 0) {
                _path = urlString;
            } else {
                _path = urlString.substring(0, urlString.indexOf('?'));
            }
            _fullString = urlString;

            StringTokenizer st = new StringTokenizer(urlString.substring(urlString.indexOf('?') + 1), "&");
            while (st.hasMoreTokens()) {
                _parameters.addElement(st.nextToken());
            }
        }

        @Override
        public String toString() {
            return _fullString;
        }

        @Override
        public boolean equals(Object o) {
            return getClass().equals(o.getClass()) && equals((QuerySpec) o);
        }

        @Override
        public int hashCode() {
            return _path.hashCode() ^ _parameters.size();
        }

        private String _path;
        private String _fullString;
        private Vector _parameters = new Vector();

        private boolean equals(QuerySpec o) {
            if (!_path.equals(o._path) || _parameters.size() != o._parameters.size()) {
                return false;
            }
            for (Enumeration e = o._parameters.elements(); e.hasMoreElements();) {
                if (!_parameters.contains(e.nextElement())) {
                    return false;
                }
            }
            return true;
        }
    }
}

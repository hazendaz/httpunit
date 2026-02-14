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
package com.meterware.servletunit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.meterware.httpunit.FrameSelector;
import com.meterware.httpunit.HttpUnitUtils;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebTable;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.Dictionary;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The Class JUnitServletTest.
 */
class JUnitServletTest {

    /** The runner. */
    private ServletRunner _runner;

    /**
     * No test class specified.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void noTestClassSpecified() throws Exception {
        ServletUnitClient client = newClient();

        WebResponse wr = client.getResponse("http://localhost/JUnit");
        assertTrue(wr.getText().contains("Cannot run"), "Did not find error message");
    }

    /**
     * Bad test class specified.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void badTestClassSpecified() throws Exception {
        ServletUnitClient client = newClient();

        WebResponse wr = client.getResponse("http://localhost/JUnit?test=gobbledygook");
        assertTrue(wr.getText().contains("Cannot run"), "Did not find error message");
    }

    /**
     * All tests pass.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void allTestsPass() throws Exception {
        ServletUnitClient client = newClient();
        WebResponse wr = client.getResponse("http://localhost/JUnit?test=" + PassingTests.class.getName());
        final WebTable resultsTable = wr.getTableWithID("results");
        assertNotNull(resultsTable, "Did not find results table");
        final String[][] results = resultsTable.asText();
        assertEquals(1, results.length, "Num rows");
        assertEquals(3, results[0].length, "Num columns");
        assertEquals("1 test", results[0][0], "Time header");
        assertEquals("OK", results[0][2], "Status");
    }

    /**
     * All tests pass text format.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void allTestsPassTextFormat() throws Exception {
        ServletUnitClient client = newClient();
        WebResponse wr = client.getResponse("http://localhost/JUnit?format=text&test=" + PassingTests.class.getName());
        String expectedStart = PassingTests.class.getName() + " (1 test): OK";
        assertTrue(wr.getText().startsWith(expectedStart),
                "Results (" + wr.getText() + ") should start with '" + expectedStart);
    }

    /**
     * Some failures.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void someFailures() throws Exception {
        ServletUnitClient client = newClient();

        WebResponse wr = client.getResponse("http://localhost/JUnit?test=" + FailingTests.class.getName());
        final WebTable resultsTable = wr.getTableWithID("results");
        assertNotNull(resultsTable, "Did not find results table");
        final String[][] results = resultsTable.asText();
        assertEquals(4, results.length, "Num rows");
        assertEquals(3, results[0].length, "Num columns");
        assertEquals("3 tests", results[0][0], "First header");
        assertEquals("Problems Occurred", results[0][2], "Status");
        assertEquals("2 failures", results[1][1], "Failure header");
        assertEquals("1", results[2][0], "Failure index 1");
        assertEquals("2", results[3][0], "Failure index 2");
        assertTrue(results[2][1].indexOf('(' + FailingTests.class.getName() + ')') >= 0, "Test class not found");
    }

    /**
     * Some failures text format.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void someFailuresTextFormat() throws Exception {
        ServletUnitClient client = newClient();

        WebResponse wr = client.getResponse("http://localhost/JUnit?format=text&test=" + FailingTests.class.getName());
        String expectedStart = FailingTests.class.getName() + " (3 tests): Problems Occurred";
        assertTrue(wr.getText().startsWith(expectedStart),
                "Results (" + wr.getText() + ") should start with: " + expectedStart);
        assertTrue(wr.getText().indexOf("2 failures") >= 0,
                "Results (" + wr.getText() + ") should contain: 2 failures");
    }

    /**
     * Some errors.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void someErrors() throws Exception {
        ServletUnitClient client = newClient();

        WebResponse wr = client.getResponse("http://localhost/JUnit?test=" + ErrorTests.class.getName());
        final WebTable resultsTable = wr.getTableWithID("results");
        assertNotNull(resultsTable, "Did not find results table");
        final String[][] results = resultsTable.asText();
        assertEquals(3, results.length, "Num rows");
        assertEquals(3, results[0].length, "Num columns");
        assertEquals("2 tests", results[0][0], "First header");
        assertEquals("Problems Occurred", results[0][2], "Status");
        assertEquals("1 error", results[1][1], "Failure header");
        assertEquals("1", results[2][0], "Failure index 1");
        assertTrue(results[2][1].indexOf('(' + ErrorTests.class.getName() + ')') >= 0, "Test class not found");
    }

    /**
     * Some failures XML format.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void someFailuresXMLFormat() throws Exception {
        ServletUnitClient client = newClient();

        WebResponse wr = client.getResponse("http://localhost/JUnit?format=xml&test=" + FailingTests.class.getName());
        assertEquals("text/xml", wr.getContentType(), "Content type");
        DocumentBuilder builder = HttpUnitUtils.newParser();
        Document document = builder.parse(wr.getInputStream());
        Element element = document.getDocumentElement();
        assertEquals("testsuite", element.getNodeName(), "document element name");
        assertEquals("3", element.getAttribute("tests"), "number of tests");
        assertEquals("2", element.getAttribute("failures"), "number of failures");
        assertEquals("0", element.getAttribute("errors"), "number of errors");
        NodeList nl = element.getElementsByTagName("testcase");
        verifyElementWithNameHasFailureNode("testAddition", nl, /* failed */ "failure", true);
        verifyElementWithNameHasFailureNode("testSubtraction", nl, /* failed */ "failure", true);
        verifyElementWithNameHasFailureNode("testMultiplication", nl, /* failed */ "failure", false);
    }

    /**
     * Some errors XML format.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void someErrorsXMLFormat() throws Exception {
        ServletUnitClient client = newClient();

        WebResponse wr = client.getResponse("http://localhost/JUnit?format=xml&test=" + ErrorTests.class.getName());
        assertEquals("text/xml", wr.getContentType(), "Content type");
        DocumentBuilder builder = HttpUnitUtils.newParser();
        Document document = builder.parse(wr.getInputStream());
        Element element = document.getDocumentElement();
        assertEquals("testsuite", element.getNodeName(), "document element name");
        assertEquals("2", element.getAttribute("tests"), "number of tests");
        assertEquals("0", element.getAttribute("failures"), "number of failures");
        assertEquals("1", element.getAttribute("errors"), "number of errors");
        NodeList nl = element.getElementsByTagName("testcase");
        verifyElementWithNameHasFailureNode("testAddition", nl, /* failed */ "error", true);
        verifyElementWithNameHasFailureNode("testMultiplication", nl, /* failed */ "error", false);
    }

    /**
     * Verify element with name has failure node.
     *
     * @param name
     *            the name
     * @param nl
     *            the nl
     * @param nodeName
     *            the node name
     * @param failed
     *            the failed
     */
    private void verifyElementWithNameHasFailureNode(String name, NodeList nl, String nodeName, boolean failed) {
        for (int i = 0; i < nl.getLength(); i++) {
            Element element = (Element) nl.item(i);
            if (element.getAttribute("name").indexOf(name) >= 0) {
                if (failed) {
                    assertEquals(1, element.getElementsByTagName(nodeName).getLength(),
                            "no " + nodeName + " element found for test '" + name + "'");
                } else {
                    assertEquals(0, element.getElementsByTagName(nodeName).getLength(),
                            "unexpected " + nodeName + " element found for test '" + name + "'");
                }
                return;
            }
        }
        if (failed) {
            fail("No test result found for '" + name + "'");
        }
    }

    /**
     * Scripted servlet access.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void scriptedServletAccess() throws Exception {
        WebXMLString wxs = new WebXMLString();
        Properties params = new Properties();
        params.setProperty("color", "red");
        params.setProperty("age", "12");
        wxs.addServlet("simple", "/SimpleServlet", SimpleGetServlet.class, params);
        wxs.addServlet("/JUnit", TestRunnerServlet.class);

        MyFactory._runner = _runner = new ServletRunner(wxs.asInputStream());
        ServletUnitClient client = _runner.newClient();
        WebResponse wr = client.getResponse("http://localhost/JUnit?test=" + ServletAccessTestClass.class.getName());

        final WebTable resultsTable = wr.getTableWithID("results");
        assertNotNull(resultsTable, "Did not find results table");
        final String[][] results = resultsTable.asText();
        assertEquals("OK", results[0][2], "Status");
    }

    /**
     * New client.
     *
     * @return the servlet unit client
     */
    private ServletUnitClient newClient() {
        _runner = new ServletRunner();
        MyFactory._runner = _runner;
        _runner.registerServlet("/JUnit", TestRunnerServlet.class.getName());
        return _runner.newClient();
    }

    // ===============================================================================================================

    /**
     * The Class SimpleGetServlet.
     */
    static class SimpleGetServlet extends HttpServlet {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The response text. */
        static String RESPONSE_TEXT = "the desired content";

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("text/html");
            PrintWriter pw = resp.getWriter();
            pw.print(RESPONSE_TEXT);
            pw.close();
        }
    }

    /**
     * The Class TestRunnerServlet.
     */
    static class TestRunnerServlet extends JUnitServlet {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /**
         * Instantiates a new test runner servlet.
         */
        public TestRunnerServlet() {
            super(new MyFactory());
        }
    }

    /**
     * A factory for creating My objects.
     */
    protected static class MyFactory implements InvocationContextFactory {

        /** The runner. */
        private static ServletRunner _runner;

        @Override
        public InvocationContext newInvocation(ServletUnitClient client, FrameSelector targetFrame, WebRequest request,
                Dictionary clientHeaders, byte[] messageBody) throws IOException, MalformedURLException {
            return new InvocationContextImpl(client, _runner, targetFrame, request, clientHeaders, messageBody);
        }

        @Override
        public HttpSession getSession(String sessionId, boolean create) {
            return null;
        }
    }
}

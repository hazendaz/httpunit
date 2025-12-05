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
package com.meterware.servletunit;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.runner.BaseTestRunner;

/**
 * A servlet which can run unit tests inside a servlet context. It may be extended to provide InvocationContext-access
 * to such tests if a container-specific implementation of InvocationContextFactory is provided. Combined with
 * ServletTestCase, this would permit in-container tests of servlets in a fashion similar to that supported by
 * ServletUnit.
 **/
public class JUnitServlet extends HttpServlet {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new j unit servlet.
     */
    public JUnitServlet() {
    }

    /**
     * Instantiates a new j unit servlet.
     *
     * @param factory
     *            the factory
     */
    protected JUnitServlet(InvocationContextFactory factory) {
        _factory = factory;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ResultsFormatter formatter = getResultsFormatter(request.getParameter("format"));
        response.setContentType(formatter.getContentType());
        final String testName = request.getParameter("test");
        if (testName == null || testName.isEmpty()) {
            reportCannotRunTest(response.getWriter(), "No test class specified");
        } else {
            ServletTestRunner runner = new ServletTestRunner(response.getWriter(), formatter);
            runner.runTestSuite(testName);
        }
        response.getWriter().close();
    }

    /**
     * Gets the results formatter.
     *
     * @param formatterName
     *            the formatter name
     *
     * @return the results formatter
     */
    private ResultsFormatter getResultsFormatter(String formatterName) {
        if ("text".equalsIgnoreCase(formatterName)) {
            return new TextResultsFormatter();
        }
        if ("xml".equalsIgnoreCase(formatterName)) {
            return new XMLResultsFormatter();
        }
        return new HTMLResultsFormatter();
    }

    /** The factory. */
    private InvocationContextFactory _factory;

    /**
     * Report cannot run test.
     *
     * @param writer
     *            the writer
     * @param errorMessage
     *            the error message
     */
    private void reportCannotRunTest(PrintWriter writer, final String errorMessage) {
        writer.print("<html><head><title>Cannot run test</title></head><body>" + errorMessage + "</body></html>");
    }

    /**
     * The Class ServletTestRunner.
     */
    class ServletTestRunner extends BaseTestRunner {

        /** The writer. */
        private PrintWriter _writer;

        /** The formatter. */
        private ResultsFormatter _formatter;

        /**
         * Instantiates a new servlet test runner.
         *
         * @param writer
         *            the writer
         * @param formatter
         *            the formatter
         */
        public ServletTestRunner(PrintWriter writer, ResultsFormatter formatter) {
            ServletTestCase.setInvocationContextFactory(_factory);
            _writer = writer;
            _formatter = formatter;
        }

        /**
         * Run test suite.
         *
         * @param testClassName
         *            the test class name
         */
        void runTestSuite(String testClassName) {
            Test suite = getTest(testClassName);

            if (suite != null) {
                TestResult testResult = new TestResult();
                testResult.addListener(this);
                long startTime = System.currentTimeMillis();
                suite.run(testResult);
                long endTime = System.currentTimeMillis();
                _formatter.displayResults(_writer, testClassName, elapsedTimeAsString(endTime - startTime), testResult);
            }
        }

        @Override
        public void addError(Test test, Throwable throwable) {
        }

        @Override
        public void addFailure(Test test, AssertionFailedError error) {
        }

        @Override
        public void endTest(Test test) {
        }

        @Override
        protected void runFailed(String s) {
            reportCannotRunTest(_writer, s);
        }

        @Override
        public void startTest(Test test) {
        }

        @Override
        public void testStarted(String s) {
        }

        @Override
        public void testEnded(String s) {
        }

        @Override
        public void testFailed(int i, Test test, Throwable throwable) {
        }

    }

    /**
     * The Class ResultsFormatter.
     */
    static abstract class ResultsFormatter {

        /** The Constant LF. */
        private static final char LF = 10;

        /** The Constant CR. */
        private static final char CR = 13;

        /**
         * Gets the content type.
         *
         * @return the content type
         */
        abstract String getContentType();

        /**
         * Display results.
         *
         * @param writer
         *            the writer
         * @param testClassName
         *            the test class name
         * @param elapsedTimeString
         *            the elapsed time string
         * @param testResult
         *            the test result
         */
        void displayResults(PrintWriter writer, String testClassName, String elapsedTimeString, TestResult testResult) {
            displayHeader(writer, testClassName, testResult, elapsedTimeString);
            displayResults(writer, testResult);
            displayFooter(writer);
        }

        /**
         * Display header.
         *
         * @param writer
         *            the writer
         * @param testClassName
         *            the test class name
         * @param testResult
         *            the test result
         * @param elapsedTimeString
         *            the elapsed time string
         */
        protected abstract void displayHeader(PrintWriter writer, String testClassName, TestResult testResult,
                String elapsedTimeString);

        /**
         * Display results.
         *
         * @param writer
         *            the writer
         * @param testResult
         *            the test result
         */
        protected abstract void displayResults(PrintWriter writer, TestResult testResult);

        /**
         * Display footer.
         *
         * @param writer
         *            the writer
         */
        protected abstract void displayFooter(PrintWriter writer);

        /**
         * Sgml escape.
         *
         * @param s
         *            the s
         *
         * @return the string
         */
        protected String sgmlEscape(String s) {
            if (s == null) {
                return "NULL";
            }
            StringBuilder result = new StringBuilder(s.length());
            char[] chars = s.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                switch (chars[i]) {
                    case '&':
                        result.append("&amp;");
                        break;
                    case '<':
                        result.append("&lt;");
                        break;
                    case '>':
                        result.append("&gt;");
                        break;
                    case LF:
                        if (i > 0 && chars[i - 1] == CR) {
                            result.append(chars[i]);
                            break;
                        }
                    case CR:
                        result.append(getLineBreak());
                    default:
                        result.append(chars[i]);
                }
            }
            return result.toString();
        }

        /**
         * Gets the line break.
         *
         * @return the line break
         */
        protected String getLineBreak() {
            return "<br>";
        }
    }

    /**
     * The Class DisplayedResultsFormatter.
     */
    abstract static class DisplayedResultsFormatter extends ResultsFormatter {

        @Override
        protected void displayHeader(PrintWriter writer, String testClassName, TestResult testResult,
                String elapsedTimeString) {
            displayHeader(writer, testClassName, getFormatted(testResult.runCount(), "test"), elapsedTimeString,
                    testResult.wasSuccessful() ? "OK" : "Problems Occurred");
        }

        @Override
        protected void displayResults(PrintWriter writer, TestResult testResult) {
            if (!testResult.wasSuccessful()) {
                displayProblems(writer, "failure", testResult.failureCount(), testResult.failures());
                displayProblems(writer, "error", testResult.errorCount(), testResult.errors());
            }
        }

        /**
         * Display header.
         *
         * @param writer
         *            the writer
         * @param testClassName
         *            the test class name
         * @param testCountText
         *            the test count text
         * @param elapsedTimeString
         *            the elapsed time string
         * @param resultString
         *            the result string
         */
        protected abstract void displayHeader(PrintWriter writer, String testClassName, String testCountText,
                String elapsedTimeString, String resultString);

        /**
         * Display problem title.
         *
         * @param writer
         *            the writer
         * @param title
         *            the title
         */
        protected abstract void displayProblemTitle(PrintWriter writer, String title);

        /**
         * Display problem detail header.
         *
         * @param writer
         *            the writer
         * @param i
         *            the i
         * @param testName
         *            the test name
         */
        protected abstract void displayProblemDetailHeader(PrintWriter writer, int i, String testName);

        /**
         * Display problem detail footer.
         *
         * @param writer
         *            the writer
         */
        protected abstract void displayProblemDetailFooter(PrintWriter writer);

        /**
         * Display problem detail.
         *
         * @param writer
         *            the writer
         * @param message
         *            the message
         */
        protected abstract void displayProblemDetail(PrintWriter writer, String message);

        /**
         * Display problems.
         *
         * @param writer
         *            the writer
         * @param kind
         *            the kind
         * @param count
         *            the count
         * @param enumeration
         *            the enumeration
         */
        private void displayProblems(PrintWriter writer, String kind, int count, Enumeration enumeration) {
            if (count != 0) {
                displayProblemTitle(writer, getFormatted(count, kind));
                Enumeration e = enumeration;
                for (int i = 1; e.hasMoreElements(); i++) {
                    TestFailure failure = (TestFailure) e.nextElement();
                    displayProblemDetailHeader(writer, i, failure.failedTest().toString());
                    if (failure.thrownException() instanceof AssertionFailedError) {
                        displayProblemDetail(writer, failure.thrownException().getMessage());
                    } else {
                        displayProblemDetail(writer, BaseTestRunner.getFilteredTrace(failure.thrownException()));
                    }
                    displayProblemDetailFooter(writer);
                }
            }
        }

        /**
         * Gets the formatted.
         *
         * @param count
         *            the count
         * @param name
         *            the name
         *
         * @return the formatted
         */
        private String getFormatted(int count, String name) {
            return count + " " + name + (count == 1 ? "" : "s");
        }

    }

    /**
     * The Class TextResultsFormatter.
     */
    static class TextResultsFormatter extends DisplayedResultsFormatter {

        @Override
        String getContentType() {
            return "text/plain";
        }

        @Override
        protected void displayHeader(PrintWriter writer, String testClassName, String testCountText,
                String elapsedTimeString, String resultString) {
            writer.println(testClassName + " (" + testCountText + "): " + resultString);
        }

        @Override
        protected void displayFooter(PrintWriter writer) {
        }

        @Override
        protected void displayProblemTitle(PrintWriter writer, String title) {
            writer.println();
            writer.println(title + ':');
        }

        @Override
        protected void displayProblemDetailHeader(PrintWriter writer, int i, String testName) {
            writer.println(i + ". " + testName + ":");
        }

        @Override
        protected void displayProblemDetailFooter(PrintWriter writer) {
            writer.println();
        }

        @Override
        protected void displayProblemDetail(PrintWriter writer, String message) {
            writer.println(message);
        }
    }

    /**
     * The Class HTMLResultsFormatter.
     */
    static class HTMLResultsFormatter extends DisplayedResultsFormatter {

        @Override
        String getContentType() {
            return "text/html";
        }

        @Override
        protected void displayHeader(PrintWriter writer, String testClassName, String testCountText,
                String elapsedTimeString, String resultString) {
            writer.println("<html><head><title>Test Suite: " + testClassName + "</title>");
            writer.println("<style type='text/css'>");
            writer.println("<!--");
            writer.println("  td.detail { font-size:smaller; vertical-align: top }");
            writer.println("  -->");
            writer.println("</style></head><body>");
            writer.println("<table id='results' border='1'><tr>");
            writer.println("<td>" + testCountText + "</td>");
            writer.println("<td>Time: " + elapsedTimeString + "</td>");
            writer.println("<td>" + resultString + "</td></tr>");
        }

        @Override
        protected void displayFooter(PrintWriter writer) {
            writer.println("</table>");
            writer.println("</body></html>");
        }

        @Override
        protected void displayProblemTitle(PrintWriter writer, String title) {
            writer.println("<tr><td colspan=3>" + title + "</td></tr>");
        }

        @Override
        protected void displayProblemDetailHeader(PrintWriter writer, int i, String testName) {
            writer.println("<tr><td class='detail' align='right'>" + i + "</td>");
            writer.println("<td class='detail'>" + testName + "</td><td class='detail'>");
        }

        @Override
        protected void displayProblemDetailFooter(PrintWriter writer) {
            writer.println("</td></tr>");
        }

        @Override
        protected void displayProblemDetail(PrintWriter writer, String message) {
            writer.println(sgmlEscape(message));
        }

    }

    /**
     * The Class XMLResultsFormatter.
     */
    static class XMLResultsFormatter extends ResultsFormatter {

        @Override
        String getContentType() {
            return "text/xml;charset=UTF-8";
        }

        @Override
        protected void displayHeader(PrintWriter writer, String testClassName, TestResult testResult,
                String elapsedTimeString) {
            writer.println("<?xml version='1.0' encoding='UTF-8' ?>\n" + "<testsuite name=" + asAttribute(testClassName)
                    + " tests=" + asAttribute(testResult.runCount()) + " failures="
                    + asAttribute(testResult.failureCount()) + " errors=" + asAttribute(testResult.errorCount())
                    + " time=" + asAttribute(elapsedTimeString) + ">");
        }

        /**
         * As attribute.
         *
         * @param value
         *            the value
         *
         * @return the string
         */
        private String asAttribute(int value) {
            return '"' + Integer.toString(value) + '"';
        }

        /**
         * As attribute.
         *
         * @param value
         *            the value
         *
         * @return the string
         */
        private String asAttribute(String value) {
            return '"' + sgmlEscape(value) + '"';
        }

        @Override
        protected void displayFooter(PrintWriter writer) {
            writer.println("</testsuite>");
        }

        @Override
        protected void displayResults(PrintWriter writer, TestResult testResult) {
            displayResults(writer, "failure", testResult.failures());
            displayResults(writer, "error", testResult.errors());
        }

        /**
         * Display results.
         *
         * @param writer
         *            the writer
         * @param failureNodeName
         *            the failure node name
         * @param resultsEnumeration
         *            the results enumeration
         */
        private void displayResults(PrintWriter writer, String failureNodeName, Enumeration resultsEnumeration) {
            for (Enumeration e = resultsEnumeration; e.hasMoreElements();) {
                TestFailure failure = (TestFailure) e.nextElement();
                writer.println("  <testcase name=" + asAttribute(failure.failedTest().toString()) + ">");
                writer.print("    <" + failureNodeName + " type="
                        + asAttribute(failure.thrownException().getClass().getName()) + " message="
                        + asAttribute(failure.exceptionMessage()));
                if (!displayException()) {
                    writer.println("/>");
                } else {
                    writer.println(">");
                    writer.print(sgmlEscape(BaseTestRunner.getFilteredTrace(failure.thrownException())));
                    writer.println("    </" + failureNodeName + ">");
                }
                writer.println("  </testcase>");
            }
        }

        /**
         * Display exception.
         *
         * @return true, if successful
         */
        private boolean displayException() {
            return true;
        }

        @Override
        protected String getLineBreak() {
            return "";
        }
    }

}

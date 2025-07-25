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
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class JUnitServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public JUnitServlet() {
    }

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

    private ResultsFormatter getResultsFormatter(String formatterName) {
        if ("text".equalsIgnoreCase(formatterName)) {
            return new TextResultsFormatter();
        }
        if ("xml".equalsIgnoreCase(formatterName)) {
            return new XMLResultsFormatter();
        }
        return new HTMLResultsFormatter();
    }

    private InvocationContextFactory _factory;

    private void reportCannotRunTest(PrintWriter writer, final String errorMessage) {
        writer.print("<html><head><title>Cannot run test</title></head><body>" + errorMessage + "</body></html>");
    }

    class ServletTestRunner extends BaseTestRunner {
        private PrintWriter _writer;
        private ResultsFormatter _formatter;

        public ServletTestRunner(PrintWriter writer, ResultsFormatter formatter) {
            ServletTestCase.setInvocationContextFactory(_factory);
            _writer = writer;
            _formatter = formatter;
        }

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

    static abstract class ResultsFormatter {

        private static final char LF = 10;
        private static final char CR = 13;

        abstract String getContentType();

        void displayResults(PrintWriter writer, String testClassName, String elapsedTimeString, TestResult testResult) {
            displayHeader(writer, testClassName, testResult, elapsedTimeString);
            displayResults(writer, testResult);
            displayFooter(writer);
        }

        protected abstract void displayHeader(PrintWriter writer, String testClassName, TestResult testResult,
                String elapsedTimeString);

        protected abstract void displayResults(PrintWriter writer, TestResult testResult);

        protected abstract void displayFooter(PrintWriter writer);

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

        protected String getLineBreak() {
            return "<br>";
        }
    }

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

        protected abstract void displayHeader(PrintWriter writer, String testClassName, String testCountText,
                String elapsedTimeString, String resultString);

        protected abstract void displayProblemTitle(PrintWriter writer, String title);

        protected abstract void displayProblemDetailHeader(PrintWriter writer, int i, String testName);

        protected abstract void displayProblemDetailFooter(PrintWriter writer);

        protected abstract void displayProblemDetail(PrintWriter writer, String message);

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

        private String getFormatted(int count, String name) {
            return count + " " + name + (count == 1 ? "" : "s");
        }

    }

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

        private String asAttribute(int value) {
            return '"' + Integer.toString(value) + '"';
        }

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

        private boolean displayException() {
            return true;
        }

        @Override
        protected String getLineBreak() {
            return "";
        }
    }

}

/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2026 hazendaz
 */
package com.meterware.servletunit;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

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
     * The Class ServletTestRunner powered by JUnit 5 Platform Launcher.
     */
    class ServletTestRunner {

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
            long startTime = System.currentTimeMillis();
            SummaryGeneratingListener listener = new SummaryGeneratingListener();

            try {
                Class<?> testClass = Class.forName(testClassName);

                // Build request targeting the modern JUnit Jupiter test class
                LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                        .selectors(selectClass(testClass)).build();

                // Force creation using the thread context classloader
                // so the servlet container's WEB-INF/lib service files are read correctly
                ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
                Launcher launcher;
                try {
                    Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
                    launcher = LauncherFactory.create();
                } finally {
                    Thread.currentThread().setContextClassLoader(originalClassLoader);
                }

                launcher.execute(request, listener);

            } catch (ClassNotFoundException e) {
                reportCannotRunTest(_writer, "Test class not found: " + testClassName);
                return;
            } catch (Exception e) {
                reportCannotRunTest(_writer, "Error executing test: " + e.getMessage());
                return;
            }

            long endTime = System.currentTimeMillis();
            TestExecutionSummary summary = listener.getSummary();
            _formatter.displayResults(_writer, testClassName, elapsedTimeAsString(endTime - startTime), summary);
        }
    }

    /**
     * Elapsed time as string.
     *
     * @param runTime
     *            the run time
     *
     * @return the string
     */
    private String elapsedTimeAsString(long runTime) {
        return runTime + " ms";
    }

    /**
     * The Class ResultsFormatter updated for JUnit 5 TestExecutionSummary.
     */
    abstract static class ResultsFormatter {

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
         * @param summary
         *            the summary
         */
        void displayResults(PrintWriter writer, String testClassName, String elapsedTimeString,
                TestExecutionSummary summary) {
            displayHeader(writer, testClassName, summary, elapsedTimeString);
            displayResults(writer, summary, testClassName);
            displayFooter(writer);
        }

        /**
         * Display header.
         *
         * @param writer
         *            the writer
         * @param testClassName
         *            the test class name
         * @param summary
         *            the summary
         * @param elapsedTimeString
         *            the elapsed time string
         */
        protected abstract void displayHeader(PrintWriter writer, String testClassName, TestExecutionSummary summary,
                String elapsedTimeString);

        /**
         * Display results.
         *
         * @param writer
         *            the writer
         * @param summary
         *            the summary
         * @param testClassName
         *            the test class name
         */
        protected abstract void displayResults(PrintWriter writer, TestExecutionSummary summary, String testClassName);

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
        protected void displayHeader(PrintWriter writer, String testClassName, TestExecutionSummary summary,
                String elapsedTimeString) {
            displayHeader(writer, testClassName, getFormatted((int) summary.getTestsFoundCount(), "test"),
                    elapsedTimeString, summary.getFailures().isEmpty() ? "OK" : "Problems Occurred");
        }

        @Override
        protected void displayResults(PrintWriter writer, TestExecutionSummary summary, String testClassName) {
            if (!summary.getFailures().isEmpty()) {
                displayProblems(writer, summary, testClassName);
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
         * @param summary
         *            the summary
         * @param testClassName
         *            the test class name
         */
        protected void displayProblems(PrintWriter writer, TestExecutionSummary summary, String testClassName) {
            var failures = summary.getFailures();
            if (!failures.isEmpty()) {
                long failureCount = failures.stream().filter(f -> f.getException() instanceof AssertionError).count();
                long errorCount = failures.size() - failureCount;

                String title;
                if (errorCount > 0 && failureCount > 0) {
                    title = getFormatted((int) failureCount, "failure") + ", " + getFormatted((int) errorCount, "error")
                            + ":";
                } else if (errorCount > 0) {
                    title = getFormatted((int) errorCount, "error") + ":";
                } else {
                    title = getFormatted((int) failureCount, "failure") + ":";
                }

                displayProblemTitle(writer, title);

                for (int i = 1; i <= failures.size(); i++) {
                    TestExecutionSummary.Failure failure = failures.get(i - 1);
                    displayProblemDetailHeader(writer, i, failure.getTestIdentifier().getDisplayName());

                    Throwable exception = failure.getException();
                    displayProblemDetail(writer,
                            exception.getMessage() != null ? exception.getMessage() : exception.toString());
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
            writer.println("<html><head><title>Test Suite: " + testClassName + "</title></head><body>");
            writer.println("<table id='results' border='1'><tr>");
            writer.println("<td>" + testCountText + "</td>");
            writer.println("<td>Time: " + elapsedTimeString + "</td>");
            writer.println("<td>" + resultString + "</td></tr>");
        }

        @Override
        protected void displayFooter(PrintWriter writer) {
            writer.println("</table></body></html>");
        }

        @Override
        protected void displayProblemTitle(PrintWriter writer, String title) {
            writer.println("<tr><td colspan=3>" + title + "</td></tr>");
        }

        @Override
        protected void displayProblemDetailHeader(PrintWriter writer, int i, String testName) {
            writer.println("<tr><td align='right'>" + i + "</td><td>" + testName + "</td><td>");
        }

        @Override
        protected void displayProblemDetailFooter(PrintWriter writer) {
            writer.println("</td></tr>");
        }

        @Override
        protected void displayProblemDetail(PrintWriter writer, String message) {
            writer.println(sgmlEscape(message));
        }

        @Override
        protected void displayProblems(PrintWriter writer, TestExecutionSummary summary, String testClassName) {
            var failures = summary.getFailures();
            if (!failures.isEmpty()) {
                long failureCount = failures.stream().filter(f -> f.getException() instanceof AssertionError).count();
                long errorCount = failures.size() - failureCount;

                String title;
                if (errorCount > 0 && failureCount > 0) {
                    title = getFormatted((int) failureCount, "failure") + ", " + getFormatted((int) errorCount, "error")
                            + ":";
                } else if (errorCount > 0) {
                    title = getFormatted((int) errorCount, "error") + ":";
                } else {
                    title = getFormatted((int) failureCount, "failure") + ":";
                }

                displayProblemTitle(writer, title);

                for (int i = 1; i <= failures.size(); i++) {
                    TestExecutionSummary.Failure failure = failures.get(i - 1);
                    String displayName = failure.getTestIdentifier().getDisplayName();

                    // Append class name cleanly for legacy HTML assertion match
                    if (!displayName.contains(testClassName)) {
                        displayName = displayName + "(" + testClassName + ")";
                    }

                    displayProblemDetailHeader(writer, i, displayName);

                    Throwable exception = failure.getException();
                    displayProblemDetail(writer,
                            exception.getMessage() != null ? exception.getMessage() : exception.toString());
                    displayProblemDetailFooter(writer);
                }
            }
        }

        private String getFormatted(int count, String name) {
            return count + " " + name + (count == 1 ? "" : "s");
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
        protected void displayHeader(PrintWriter writer, String testClassName, TestExecutionSummary summary,
                String elapsedTimeString) {
            writer.println("<?xml version='1.0' encoding='UTF-8' ?>\n" + "<testsuite name=" + asAttribute(testClassName)
                    + " tests=" + asAttribute((int) summary.getTestsFoundCount()) + " failures="
                    + asAttribute((int) summary.getFailures().size()) + " errors=\"0\" time="
                    + asAttribute(elapsedTimeString) + ">");
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
        protected void displayResults(PrintWriter writer, TestExecutionSummary summary, String testClassName) {
            for (TestExecutionSummary.Failure failure : summary.getFailures()) {
                String displayName = failure.getTestIdentifier().getDisplayName();
                if (displayName.endsWith("()")) {
                    displayName = displayName.substring(0, displayName.length() - 2);
                }

                if (!displayName.contains(testClassName)) {
                    displayName = displayName + "(" + testClassName + ")";
                }

                writer.println("  <testcase name=" + asAttribute(displayName) + ">");
                writer.print("    <failure message=" + asAttribute(failure.getException().getMessage()) + "/>");
                writer.println("  </testcase>");
            }
        }

        @Override
        protected String getLineBreak() {
            return "";
        }
    }

}

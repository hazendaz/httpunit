/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

import com.meterware.httpunit.parsing.HTMLParserFactory;
import com.meterware.pseudoserver.HttpUserAgentTest;

import org.junit.jupiter.api.BeforeEach;

/**
 * A base class for HttpUnit regression tests.
 **/
public abstract class HttpUnitTest extends HttpUserAgentTest {

    /**
     * setup the test by resetting the environment for Http Unit tests.
     *
     * @throws Exception
     *             the exception
     */
    @BeforeEach
    public void setUpHttpUnitTest() throws Exception {
        HttpUnitOptions.reset();
        HTMLParserFactory.reset();
    }

    /** handling of tests that are temporarily disabled. */
    public static boolean WARN_DISABLED = true;

    /** The disabled index. */
    public static int disabledIndex = 0;

    /** The first warn. */
    public static boolean firstWarn = true;

    /**
     * return a left padded string.
     *
     * @param s
     *            the s
     * @param pad
     *            the pad
     *
     * @return the string
     */
    private static String padLeft(String s, int pad) {
        String result = s;
        String space = "                                                         ";
        if (result.length() > pad) {
            result = result.substring(0, pad);
        } else if (result.length() < pad) {
            result = space.substring(0, pad - result.length()) + result;
        }
        return result;
    }

    /** The warn delim. */
    public static String warnDelim = "";

    /**
     * show a warning for disabled Tests.
     *
     * @param testName
     *            the test name
     * @param priority
     *            the priority
     * @param urgency
     *            the urgency
     * @param comment
     *            the comment
     */
    public static void warnDisabled(String testName, String priority, int urgency, String comment) {
        if (WARN_DISABLED) {
            if (firstWarn) {
                firstWarn = false;
                System.err.println(
                        "\n The following tests are not active - the features tested are not part of the current release:");
                System.err.println(" #  |        testname               | priority | urgency | reason  ");
                System.err.println(
                        "----+-------------------------------+----------+---------+----------------------------------------");
            }
            disabledIndex++;
            System.err.println(warnDelim + padLeft("" + disabledIndex, 3) + " | " + padLeft(testName, 29) + " | "
                    + padLeft(priority, 8) + " | " + padLeft("" + urgency, 7) + " | " + comment);
        }
    }

    static {
        new WebConversation();
    }

}

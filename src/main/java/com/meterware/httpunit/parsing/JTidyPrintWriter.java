/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.parsing;

import java.io.PrintWriter;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Basic "parser" for the JTidy error output. Will get the line and column number as well as the message. It assumes
 * that an error or warning is to be logged once println() has been called or if a string starts with "line"
 **/
class JTidyPrintWriter extends PrintWriter {
    /**
     * DecimalFormat.getNumberInstance() should provide us with a proper formatter for the default locale. The integers
     * returned from JTidy contain the appropriate decimal separator for the current locale.
     */
    private static final NumberFormat INTEGER_FORMAT = DecimalFormat.getNumberInstance();

    /**
     * Instantiates a new j tidy print writer.
     *
     * @param pageURL
     *            the page URL
     */
    JTidyPrintWriter(URL pageURL) {
        super(System.out);
        _url = pageURL;
    }

    @Override
    public void print(boolean b) {
        print(String.valueOf(b));
    }

    @Override
    public void print(char c) {
        print(String.valueOf(c));
    }

    @Override
    public void print(char[] s) {
        print(String.valueOf(s));
    }

    @Override
    public void print(double d) {
        print(String.valueOf(d));
    }

    @Override
    public void print(float f) {
        print(String.valueOf(f));
    }

    @Override
    public void print(int i) {
        print(String.valueOf(i));
    }

    @Override
    public void print(long l) {
        print(String.valueOf(l));
    }

    @Override
    public void print(Object obj) {
        print(obj.toString());
    }

    /**
     * Detects a new log if starting with "line", a warning if message starts with "Warning" and an error if it starts
     * with "Error"
     **/
    @Override
    public void print(String s) {
        if (s.startsWith("line")) {
            if (!_logged && _line > 0 && _msg != null && _msg.length() > 0) {
                log(); // log previous!!!
            }
            _logged = false; // new error....
            StringTokenizer tok = new StringTokenizer(s);
            // skip first "line"
            tok.nextToken();
            // get line
            _line = parseInteger(tok.nextToken());
            // skip second "column"
            tok.nextToken();
            // get column
            _column = parseInteger(tok.nextToken());
        } else if (s.startsWith("Warning")) {
            _error = false;
            _msg = s;
        } else if (s.startsWith("Error")) {
            _error = true;
            _msg = s;
        } else {
            // non structured msg
            _msg += s;
        }
    }

    /**
     * Parses the integer.
     *
     * @param integer
     *            the integer
     *
     * @return the int
     */
    private int parseInteger(String integer) {
        try {
            return INTEGER_FORMAT.parse(integer).intValue();
        } catch (ParseException e) {
            throw new NumberFormatException("Unable to parse integer [int: " + integer + ", error: " + e.getMessage());
        }
    }

    @Override
    public void println() {
        if (!_logged) {
            log();
        }
    }

    @Override
    public void println(boolean x) {
        print(String.valueOf(x));
        println();
    }

    @Override
    public void println(char c) {
        print(String.valueOf(c));
        println();
    }

    @Override
    public void println(char[] c) {
        print(String.valueOf(c));
        println();
    }

    @Override
    public void println(double d) {
        print(String.valueOf(d));
        println();
    }

    @Override
    public void println(float f) {
        print(String.valueOf(f));
        println();
    }

    @Override
    public void println(int i) {
        print(String.valueOf(i));
        println();
    }

    @Override
    public void println(long l) {
        print(String.valueOf(l));
        println();
    }

    @Override
    public void println(Object o) {
        print(o.toString());
        println();
    }

    @Override
    public void println(String s) {
        print(s);
        println();
    }

    // ----------------------------------------------- private members
    // ------------------------------------------------------

    /** The line. */
    private int _line = -1;

    /** The column. */
    private int _column = -1;

    /** The msg. */
    private String _msg = "";

    /** The error. */
    private boolean _error = false;

    /** The logged. */
    private boolean _logged = false;

    /** The url. */
    private URL _url;

    /**
     * reports the warning or error and then resets the current error/warning.
     **/
    private void log() {
        // System.out.println("Logging.........................");
        if (_error) {
            reportError(_msg, _line, _column);
        } else {
            reportWarning(_msg, _line, _column);
        }
        _logged = true;
        _line = -1;
        _column = -1;
        _msg = "";
    }

    /**
     * Report error.
     *
     * @param msg
     *            the msg
     * @param line
     *            the line
     * @param column
     *            the column
     */
    private void reportError(String msg, int line, int column) {
        List<HTMLParserListener> listeners = HTMLParserFactory.getHTMLParserListeners();
        for (HTMLParserListener listener : listeners) {
            listener.error(_url, msg, line, column);
        }
    }

    /**
     * Report warning.
     *
     * @param msg
     *            the msg
     * @param line
     *            the line
     * @param column
     *            the column
     */
    private void reportWarning(String msg, int line, int column) {
        List<HTMLParserListener> listeners = HTMLParserFactory.getHTMLParserListeners();
        for (HTMLParserListener listener : listeners) {
            listener.warning(_url, msg, line, column);
        }
    }
}

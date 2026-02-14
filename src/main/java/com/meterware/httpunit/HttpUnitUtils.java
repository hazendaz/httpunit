/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Utility code shared by httpunit and servletunit.
 **/
public class HttpUnitUtils {

    /** The Constant DEFAULT_TEXT_BUFFER_SIZE. */
    public static final int DEFAULT_TEXT_BUFFER_SIZE = 2048;

    /** set to true to debug Exception handling. */
    private static boolean EXCEPTION_DEBUG = true;

    /**
     * handle Exceptions and thowables.
     *
     * @param th
     *            the th
     */
    public static void handleException(Throwable th) {
        if (EXCEPTION_DEBUG) {
            th.printStackTrace();
        }
    }

    /**
     * are we running in the Eclipse IDE?.
     *
     * @return whether we are running in the Eclipse environment
     */
    public static boolean isEclipse() {
        StackTraceElement[] ste = new Throwable().getStackTrace();
        return ste[ste.length - 1].getClassName().startsWith("org.eclipse.jdt");
    }

    /**
     * Returns the content type and encoding as a pair of strings. If no character set is specified, the second entry
     * will be null.
     *
     * @param header
     *            the header to parse
     *
     * @return a string array with the content type and the content char set
     **/
    public static String[] parseContentTypeHeader(String header) {
        String[] result = { "text/plain", null };
        if (header.trim().length() > 0) {
            StringTokenizer st = new StringTokenizer(header, ";= ");
            result[0] = st.nextToken();
            while (st.hasMoreTokens()) {
                String parameter = st.nextToken();
                if (st.hasMoreTokens()) {
                    String value = stripQuotes(st.nextToken());
                    if (parameter.trim().equalsIgnoreCase("charset")) {
                        result[1] = value;
                    }
                }
            }
        }
        return result;
    }

    /**
     * strip the quotes from a value.
     *
     * @param value
     *            the value
     *
     * @return the stripped value
     */
    public static String stripQuotes(String value) {
        if (value.startsWith("'") || value.startsWith("\"")) {
            value = value.substring(1);
        }
        if (value.endsWith("'") || value.endsWith("\"")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    /**
     * Returns an interpretation of the specified URL-encoded string, using the ISO-8859-1 character set.
     *
     * @param byteString
     *            the byte string
     *
     * @return the string
     */
    public static String decode(String byteString) {
        return decode(byteString, "ISO-8859-1");
    }

    /**
     * Returns a string representation of a number, trimming off any trailing decimal zeros.
     *
     * @param number
     *            the number
     *
     * @return the string
     */
    static String trimmedValue(Number number) {
        String rawNumber = number.toString();
        if (rawNumber.indexOf('.') == -1) {
            return rawNumber;
        }

        int index = rawNumber.length();
        while (rawNumber.charAt(index - 1) == '0') {
            index--;
        }
        if (rawNumber.charAt(index - 1) == '.') {
            index--;
        }
        return rawNumber.substring(0, index);
    }

    /**
     * Decodes a URL safe string into its original form using the specified character set. Escaped characters are
     * converted back to their original representation. This method is copied from the <b>Jakarta Commons Codec</b>;
     * <code>org.apache.commons.codec.net.URLCodec</code> class.
     *
     * @param string
     *            URL safe string to convert into its original form
     * @param charset
     *            the charset
     *
     * @return original string
     *
     * @throws IllegalArgumentException
     *             thrown if URL decoding is unsuccessful,
     */
    public static String decode(String string, String charset) {
        if (string == null) {
            return null;
        }

        return new String(decodeUrl(string.getBytes(StandardCharsets.US_ASCII)), Charset.forName(charset));
    }

    /**
     * Decodes an array of URL safe 7-bit characters into an array of original bytes. Escaped characters are converted
     * back to their original representation. This method is copied from the <b>Jakarta Commons Codec</b>;
     * <code>org.apache.commons.codec.net.URLCodec</code> class.
     *
     * @param pArray
     *            array of URL safe characters
     *
     * @return array of original bytes
     *
     * @throws IllegalArgumentException
     *             the illegal argument exception
     */
    private static final byte[] decodeUrl(byte[] pArray) throws IllegalArgumentException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (int i = 0; i < pArray.length; i++) {
            int b = pArray[i];
            if (b == '+') {
                buffer.write(' ');
            } else if (b != '%') {
                buffer.write(b);
            } else {
                try {
                    i++;
                    int u = Character.digit((char) pArray[i], 16);
                    i++;
                    int l = Character.digit((char) pArray[i], 16);
                    if (u == -1 || l == -1) {
                        throw new IllegalArgumentException("Invalid URL encoding");
                    }
                    buffer.write((char) ((u << 4) + l));
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new IllegalArgumentException("Invalid URL encoding");
                }
            }
        }
        return buffer.toByteArray();
    }

    /**
     * parse an InputStream to a string (for debugging).
     *
     * @param is
     *            the is
     *
     * @return the string gotten from the inputString
     */
    public static String parseISToString(java.io.InputStream is) {
        java.io.DataInputStream din = new java.io.DataInputStream(is);
        StringBuilder sb = new StringBuilder();
        try {
            String line = null;
            while ((line = din.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (Exception ex) {
            // TODO handle exception properly here
            ex.getMessage();
        } finally {
            try {
                is.close();
            } catch (Exception ex) {
            }
        }
        return sb.toString();
    }

    /**
     * parse the given inputSource with a new Parser.
     *
     * @param inputSource
     *            the input source
     *
     * @return the document parsed from the input Source
     *
     * @throws SAXException
     *             the SAX exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static Document parse(InputSource inputSource) throws SAXException, IOException {
        DocumentBuilder db = newParser();
        try {
            return db.parse(inputSource);
        } catch (java.net.MalformedURLException mue) {
            if (EXCEPTION_DEBUG) {
                String msg = mue.getMessage();
                if (msg != null) {
                    System.err.println(msg);
                }
                InputStream is = inputSource.getByteStream();
                is.reset();
                String content = parseISToString(is);
                System.err.println(content);
            }
            throw mue;
        }
    }

    /**
     * parse the given inputStream with a new Parser.
     *
     * @param inputStream
     *            the input stream
     *
     * @return the document parsed from the input Stream
     *
     * @throws SAXException
     *             the SAX exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static Document parse(InputStream inputStream) throws SAXException, IOException {
        DocumentBuilder db = newParser();
        try {
            return db.parse(inputStream);
        } catch (java.net.MalformedURLException mue) {
            if (EXCEPTION_DEBUG) {
                String msg = mue.getMessage();
                if (msg != null) {
                    System.err.println(msg);
                }
                InputStream is = inputStream;
                is.reset();
                String content = parseISToString(is);
                System.err.println(content);
            }
            throw mue;
        }
    }

    /**
     * creates a parser using JAXP API.
     *
     * @return the document builder
     *
     * @throws SAXException
     *             the SAX exception
     */
    public static DocumentBuilder newParser() throws SAXException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(new HttpUnitUtils.ClasspathEntityResolver());
            return builder;
        } catch (ParserConfigurationException ex) {
            // redirect the new exception for code compatibility
            throw new SAXException(ex);
        }
    }

    /**
     * Returns a string array created by appending a string to an existing array. The existing array may be null.
     *
     * @param oldValue
     *            the old value
     * @param newValue
     *            the new value
     *
     * @return the string[]
     */
    static String[] withNewValue(String[] oldValue, String newValue) {
        String[] result;
        if (oldValue == null) {
            result = new String[] { newValue };
        } else {
            result = new String[oldValue.length + 1];
            System.arraycopy(oldValue, 0, result, 0, oldValue.length);
            result[oldValue.length] = newValue;
        }
        return result;
    }

    /**
     * Returns a string array created by appending an object to an existing array. The existing array may be null.
     *
     * @param oldValue
     *            the old value
     * @param newValue
     *            the new value
     *
     * @return the object[]
     */
    static Object[] withNewValue(Object[] oldValue, Object newValue) {
        Object[] result;
        if (oldValue == null) {
            result = new Object[] { newValue };
        } else {
            result = new Object[oldValue.length + 1];
            System.arraycopy(oldValue, 0, result, 0, oldValue.length);
            result[oldValue.length] = newValue;
        }
        return result;
    }

    /**
     * Return true if the first string contains the second. Case sensitivity is according to the setting of
     * HttpUnitOptions.matchesIgnoreCase
     *
     * @param string
     *            the string
     * @param substring
     *            the substring
     *
     * @return true, if successful
     */
    static boolean contains(String string, String substring) {
        if (HttpUnitOptions.getMatchesIgnoreCase()) {
            return string.toUpperCase().indexOf(substring.toUpperCase()) >= 0;
        }
        return string.indexOf(substring) >= 0;
    }

    /**
     * Return true if the first string starts with the second. Case sensitivity is according to the setting of
     * HttpUnitOptions.matchesIgnoreCase
     *
     * @param string
     *            the string
     * @param prefix
     *            the prefix
     *
     * @return true, if successful
     */
    static boolean hasPrefix(String string, String prefix) {
        if (HttpUnitOptions.getMatchesIgnoreCase()) {
            return string.toUpperCase().startsWith(prefix.toUpperCase());
        }
        return string.startsWith(prefix);
    }

    /**
     * Return true if the first string equals the second. Case sensitivity is according to the setting of
     * HttpUnitOptions.matchesIgnoreCase
     *
     * @param string1
     *            the string 1
     * @param string2
     *            the string 2
     *
     * @return true, if successful
     */
    static boolean matches(String string1, String string2) {
        if (HttpUnitOptions.getMatchesIgnoreCase()) {
            return string1.equalsIgnoreCase(string2);
        }
        return string1.equals(string2);
    }

    /**
     * check whether the URL is a java script url.
     *
     * @param urlString
     *            - the string to analyze
     *
     * @return - true if this is a javascript url
     */
    static boolean isJavaScriptURL(String urlString) {
        return urlString.toLowerCase(Locale.ENGLISH).startsWith("javascript:");
    }

    /**
     * Trims whitespace from the ends, and encodes from the middle. Spaces within quotes are respected.
     *
     * @param s
     *            the s
     *
     * @return the string
     */
    static String encodeSpaces(String s) {
        s = s.trim();
        // if no spaces we are fine
        if (s.indexOf(' ') < 0) {
            return s;
        }

        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        char[] chars = s.toCharArray();
        // loop over oper the chars of the URL
        for (char aChar : chars) {
            // toggle quotation and add quote
            if (aChar == '"' || aChar == '\'') {
                inQuotes = !inQuotes;
                sb.append(aChar);
                // append everything in quotes and printable chars above space
            } else if (inQuotes || aChar > ' ') {
                sb.append(aChar);
            } else if (aChar == ' ') {
                // encode spaces
                // TODO check what to do about breaking testLinkUrlAcrossLineBreaks then ...
                // sb.append("%20");
            }
        }
        return sb.toString();
    }

    /**
     * Replace entities.
     *
     * @param string
     *            the string
     *
     * @return the string
     */
    static String replaceEntities(String string) {
        int i = 0;
        int ampIndex;
        while ((ampIndex = string.indexOf('&', i)) >= 0) {
            int semiColonIndex = string.indexOf(';', ampIndex + 1);
            if (semiColonIndex < 0) {
                break;
            }
            i = ampIndex + 1;

            String entityName = string.substring(ampIndex + 1, semiColonIndex);
            if (entityName.equalsIgnoreCase("amp")) {
                string = string.substring(0, ampIndex) + '&' + string.substring(semiColonIndex + 1);
            }

        }
        return string;
    }

    /**
     * Strips the fragment identifier (if any) from the Url.
     *
     * @param rawUrl
     *            the raw url
     *
     * @return the string
     */
    static String trimFragment(String rawUrl) {
        if (isJavaScriptURL(rawUrl)) {
            return rawUrl;
        }
        final int hashIndex = rawUrl.indexOf('#');
        return hashIndex < 0 ? rawUrl : rawUrl.substring(0, hashIndex);
    }

    /**
     * The Class ClasspathEntityResolver.
     */
    static class ClasspathEntityResolver implements EntityResolver {

        @Override
        public InputSource resolveEntity(String publicID, String systemID) {
            if (systemID == null) {
                return null;
            }

            String localName = systemID;
            if (localName.indexOf("/") > 0) {
                localName = localName.substring(localName.lastIndexOf("/") + 1);
            }

            try {
                return new InputSource(getClass().getClassLoader().getResourceAsStream(localName));
            } catch (Exception e) {
                // proposed patch for bug report
                // [ 1264706 ] [patch] replace ClasspathEntityResolver
                // by fabrizio giustina
                // even to return this in all cases!
                // return new InputSource( new ByteArrayInputStream( new byte[0] ) );
                return null;
            }
        }
    }

    /**
     * Checks if is exception debug.
     *
     * @return the eXCEPTION_DEBUG
     */
    protected static boolean isEXCEPTION_DEBUG() {
        return EXCEPTION_DEBUG;
    }

    /**
     * Sets the EXCEPTIO N DEBUG.
     *
     * @param exception_debug
     *            the eXCEPTION_DEBUG to set
     *
     * @return true, if successful
     */
    public static boolean setEXCEPTION_DEBUG(boolean exception_debug) {
        boolean oldExceptionDebug = exception_debug;
        EXCEPTION_DEBUG = exception_debug;
        return oldExceptionDebug;
    }
}

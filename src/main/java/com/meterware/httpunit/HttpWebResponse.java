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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * A response from a web server to an Http request.
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
class HttpWebResponse extends WebResponse {

    private String _referer;

    /**
     * Constructs a response object from an input stream.
     *
     * @param frame
     *            the target window or frame to which the request should be directed
     * @param url
     *            the url from which the response was received
     * @param connection
     *            the URL connection from which the response can be read
     **/
    HttpWebResponse(WebConversation client, FrameSelector frame, URL url, URLConnection connection,
            boolean throwExceptionOnError) throws IOException {
        super(client, frame, url);
        if (HttpUnitOptions.isLoggingHttpHeaders()) {
            System.out.println("\nReceived from " + url);
        }
        readHeaders(connection);

        /** make sure that any IO exception for HTML received page happens here, not later. **/
        if (_responseCode < HttpURLConnection.HTTP_BAD_REQUEST || !throwExceptionOnError) {
            InputStream inputStream = getInputStream(connection);
            defineRawInputStream(new BufferedInputStream(inputStream));
            String contentType = getContentType();
            if (contentType.startsWith("text")) {
                loadResponseText();
            }
        }
    }

    HttpWebResponse(WebConversation client, FrameSelector frame, WebRequest request, URLConnection connection,
            boolean throwExceptionOnError) throws IOException {
        this(client, frame, request.getURL(), connection, throwExceptionOnError);
        super.setWithParse(!request.getMethod().equals("HEAD"));
        _referer = request.getReferer();
    }

    /**
     * get the input stream for the given connection
     *
     * @param connection
     *
     * @return
     *
     * @throws IOException
     */
    private InputStream getInputStream(URLConnection connection) throws IOException {
        InputStream result = null;
        // check whether there is an error stream
        if (isResponseOnErrorStream(connection)) {
            result = ((HttpURLConnection) connection).getErrorStream();
        } else {
            // if there is no error stream it depends on the response code
            try {
                result = connection.getInputStream();
            } catch (java.io.FileNotFoundException fnfe) {
                // as of JDK 1.5 a null inputstream might have been returned here
                // see bug report [ 1283878 ] FileNotFoundException using Sun JDK 1.5 on empty error pages
                // by Roger Lindsj?
                if (!isErrorResponse(connection)) {
                    throw fnfe;
                }
                // fake an empty error stream
                result = new ByteArrayInputStream(new byte[0]);
            }
        }
        return result;
    }

    /**
     * check whether a response code >=400 was received
     *
     * @param connection
     *
     * @return
     */
    private boolean isErrorResponse(URLConnection connection) {
        return _responseCode >= HttpURLConnection.HTTP_BAD_REQUEST;
    }

    /**
     * check whether the response is on the error stream
     *
     * @param connection
     *
     * @return
     */
    private boolean isResponseOnErrorStream(URLConnection connection) {
        return isErrorResponse(connection) && ((HttpURLConnection) connection).getErrorStream() != null;
    }

    /**
     * Returns the response code associated with this response.
     **/
    @Override
    public int getResponseCode() {
        return _responseCode;
    }

    /**
     * Returns the response message associated with this response.
     **/
    @Override
    public String getResponseMessage() {
        return _responseMessage;
    }

    @Override
    public String[] getHeaderFieldNames() {
        Vector names = new Vector<>();
        for (Enumeration e = _headers.keys(); e.hasMoreElements();) {
            names.addElement(e.nextElement());
        }
        String[] result = new String[names.size()];
        names.copyInto(result);
        return result;
    }

    /**
     * Returns the value for the specified header field. If no such field is defined, will return null.
     **/
    @Override
    public String getHeaderField(String fieldName) {
        String[] fields = (String[]) _headers.get(fieldName.toUpperCase());
        return fields == null ? null : fields[0];
    }

    @Override
    public String[] getHeaderFields(String fieldName) {
        String[] fields = (String[]) _headers.get(fieldName.toUpperCase());
        return fields == null ? new String[0] : fields;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("HttpWebResponse [url=");
        sb.append(getURL()).append("; headers=");
        for (Enumeration e = _headers.keys(); e.hasMoreElements();) {
            Object key = e.nextElement();
            String[] values = (String[]) _headers.get(key);
            for (String value : values) {
                sb.append("\n   ").append(key).append(": ").append(value);
            }
        }
        sb.append(" ]");
        return sb.toString();
    }

    @Override
    String getReferer() {
        return _referer;
    }

    // ------------------------------------- private members -------------------------------------

    private static final String FILE_ENCODING = Charset.defaultCharset().displayName();

    private int _responseCode = HttpURLConnection.HTTP_OK;
    private String _responseMessage = "OK";

    /**
     * set the responseCode to the given code and message
     *
     * @param code
     * @param message
     */
    private void setResponseCode(int code, String message) {
        _responseCode = code;
        _responseMessage = message;
    }

    private Hashtable _headers = new Hashtable<>();

    /**
     * read the response Header for the given connection and set the response code and message accordingly
     *
     * @param connection
     *
     * @throws IOException
     */
    private void readResponseHeader(HttpURLConnection connection) throws IOException {
        if (!needStatusWorkaround()) {
            setResponseCode(connection.getResponseCode(), connection.getResponseMessage());
        } else {
            if (connection.getHeaderField(0) == null) {
                throw new UnknownHostException(connection.getURL().toExternalForm());
            }

            StringTokenizer st = new StringTokenizer(connection.getHeaderField(0));
            st.nextToken();
            if (!st.hasMoreTokens()) {
                setResponseCode(HttpURLConnection.HTTP_OK, "OK");
            } else {
                try {
                    setResponseCode(Integer.parseInt(st.nextToken()), getRemainingTokens(st));
                } catch (NumberFormatException e) {
                    setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR, "Cannot parse response header");
                }
            }
        }
    }

    private boolean needStatusWorkaround() {
        final String jdkVersion = System.getProperty("java.version");
        return jdkVersion.startsWith("1.2") || jdkVersion.startsWith("1.3");
    }

    private String getRemainingTokens(StringTokenizer st) {
        StringBuilder messageBuffer = new StringBuilder(st.hasMoreTokens() ? st.nextToken() : "");
        while (st.hasMoreTokens()) {
            messageBuffer.append(' ').append(st.nextToken());
        }
        return messageBuffer.toString();
    }

    private void readHeaders(URLConnection connection) throws IOException {
        loadHeaders(connection);
        if (connection instanceof HttpURLConnection) {
            readResponseHeader((HttpURLConnection) connection);
        } else {
            setResponseCode(HttpURLConnection.HTTP_OK, "OK");
            if (connection.getContentType().startsWith("text")) {
                setContentTypeHeader(connection.getContentType() + "; charset=" + FILE_ENCODING);
            }
        }
    }

    private void loadHeaders(URLConnection connection) {
        if (HttpUnitOptions.isLoggingHttpHeaders()) {
            System.out.println("Header:: " + connection.getHeaderField(0));
        }
        for (int i = 1; true; i++) {
            String headerFieldKey = connection.getHeaderFieldKey(i);
            String headerField = connection.getHeaderField(i);
            if (headerFieldKey == null || headerField == null) {
                break;
            }
            if (HttpUnitOptions.isLoggingHttpHeaders()) {
                System.out.println("Header:: " + headerFieldKey + ": " + headerField);
            }
            addHeader(headerFieldKey.toUpperCase(), headerField);
        }

        if (connection.getContentType() != null) {
            setContentTypeHeader(connection.getContentType());
        }
    }

    private void addHeader(String key, String field) {
        _headers.put(key, HttpUnitUtils.withNewValue((String[]) _headers.get(key), field));
    }

}

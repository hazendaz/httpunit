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
package com.meterware.pseudoserver;

import com.meterware.httpunit.HttpUnitUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * A basic simulated web-server for testing user agents without a web server.
 **/
public class PseudoServer {

    /**
     * allow factory use to be switched on and off by default the factory is not used any more since there were problems
     * with the test cases as of 2012-10-09.
     */
    public static final boolean useFactory = false;

    /** The Constant DEFAULT_SOCKET_TIMEOUT. */
    static final int DEFAULT_SOCKET_TIMEOUT = 1000;

    /** The Constant INPUT_POLL_INTERVAL. */
    private static final int INPUT_POLL_INTERVAL = 10;

    /** Time in msec to wait for an outstanding server socket to be released before creating a new one. **/
    private static int _socketReleaseWaitTime = 50;

    /** Number of outstanding server sockets that must be present before trying to wait for one to be released. **/
    private static int _waitThreshhold = 10;

    /** The num servers. */
    private static int _numServers = 0;

    /** The server num. */
    private int _serverNum = 0;

    /** The connection num. */
    private int _connectionNum = 0;

    /** The classpath dirs. */
    private ArrayList _classpathDirs = new ArrayList<>();

    /** The max protocol level. */
    private String _maxProtocolLevel = "1.1";

    /** The socket timeout. */
    private final int _socketTimeout;

    /**
     * Returns the amount of time the pseudo server will wait for a server socket to be released (in msec) before
     * allocating a new one. See also {@link #getWaitThreshhold getWaitThreshhold}.
     *
     * @return the socket release wait time
     */
    public static int getSocketReleaseWaitTime() {
        return _socketReleaseWaitTime;
    }

    /**
     * Returns the amount of time the pseudo server will wait for a server socket to be released (in msec) before
     * allocating a new one. See also {@link #getWaitThreshhold getWaitThreshhold}.
     *
     * @param socketReleaseWaitTime
     *            the new socket release wait time
     */
    public static void setSocketReleaseWaitTime(int socketReleaseWaitTime) {
        _socketReleaseWaitTime = socketReleaseWaitTime;
    }

    /**
     * Returns the number of server sockets that must have been allocated and not returned before waiting for one to be
     * returned.
     *
     * @return the wait threshhold
     */
    public static int getWaitThreshhold() {
        return _waitThreshhold;
    }

    /**
     * Specifies the number of server sockets that must have been allocated and not returned before waiting for one to
     * be returned.
     *
     * @param waitThreshhold
     *            the new wait threshhold
     */
    public static void setWaitThreshhold(int waitThreshhold) {
        _waitThreshhold = waitThreshhold;
    }

    /**
     * Instantiates a new pseudo server.
     */
    public PseudoServer() {
        this(DEFAULT_SOCKET_TIMEOUT);
    }

    /**
     * create a PseudoServer with the given socketTimeout.
     *
     * @param socketTimeout
     *            - the time out to use
     */
    public PseudoServer(int socketTimeout) {
        _socketTimeout = socketTimeout;
        _serverNum = ++_numServers;

        try {
            _serverSocket = new ServerSocket(0);
            _serverSocket.setSoTimeout(1000);
        } catch (IOException e) {
            System.out.println("Error while creating socket: " + e);
            throw new RuntimeException(e);
        }
        Thread t = new Thread("PseudoServer " + _serverNum) {
            @Override
            public void run() {
                while (_active) {
                    try {
                        handleNewConnection(_serverSocket.accept());
                        Thread.sleep(20);
                    } catch (InterruptedIOException e) {
                    } catch (IOException e) {
                        System.out.println("Error in pseudo server: " + e);
                        HttpUnitUtils.handleException(e);
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                        System.out.println("Interrupted. Shutting down");
                        _active = false;
                    }
                }
                try {
                    _serverSocket.close();
                } catch (IOException e) {
                    System.out.println("Error while closing socket: " + e);
                }
                debug("Pseudoserver shutting down");
            }
        };
        debug("Starting pseudoserver");
        t.start();
    }

    /**
     * Shut down.
     */
    public void shutDown() {
        debug("Requested shutdown of pseudoserver");
        _active = false;
    }

    /**
     * Debug.
     *
     * @param message
     *            the message
     */
    private void debug(String message) {
        if (!_debug) {
            return;
        }
        message = replaceDebugToken(message, "thread", "thread (" + Thread.currentThread().getName() + ")");
        message = replaceDebugToken(message, "server", "server " + _serverNum);
        System.out.println("** " + message);
    }

    /**
     * Replace debug token.
     *
     * @param message
     *            the message
     * @param token
     *            the token
     * @param replacement
     *            the replacement
     *
     * @return the string
     */
    private static String replaceDebugToken(String message, String token, String replacement) {
        return !message.contains(token) ? message : message.replaceFirst(token, replacement);
    }

    /**
     * Sets the max protocol level.
     *
     * @param majorLevel
     *            the major level
     * @param minorLevel
     *            the minor level
     */
    public void setMaxProtocolLevel(int majorLevel, int minorLevel) {
        _maxProtocolLevel = majorLevel + "." + minorLevel;
    }

    /**
     * Returns the port on which this server is listening.
     *
     * @return the connected port
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public int getConnectedPort() throws IOException {
        return _serverSocket.getLocalPort();
    }

    /**
     * Defines the contents of an expected resource.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     */
    public void setResource(String name, String value) {
        setResource(name, value, "text/html");
    }

    /**
     * Defines the contents of an expected resource.
     *
     * @param name
     *            the name
     * @param servlet
     *            the servlet
     */
    public void setResource(String name, PseudoServlet servlet) {
        _resources.put(asResourceName(name), servlet);
    }

    /**
     * Defines the contents of an expected resource.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     * @param contentType
     *            the content type
     */
    public void setResource(String name, String value, String contentType) {
        _resources.put(asResourceName(name), new WebResource(value, contentType));
    }

    /**
     * Defines the contents of an expected resource.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     * @param contentType
     *            the content type
     */
    public void setResource(String name, byte[] value, String contentType) {
        _resources.put(asResourceName(name), new WebResource(value, contentType));
    }

    /**
     * Defines a resource which will result in an error message. return it for further use
     *
     * @param name
     *            the name
     * @param errorCode
     *            the error code
     * @param errorMessage
     *            the error message
     *
     * @return the resource
     */
    public WebResource setErrorResource(String name, int errorCode, String errorMessage) {
        WebResource resource = new WebResource(errorMessage, errorCode);
        _resources.put(asResourceName(name), resource);
        return resource;
    }

    /**
     * Enables the sending of the character set in the content-type header.
     *
     * @param name
     *            the name
     * @param enabled
     *            the enabled
     */
    public void setSendCharacterSet(String name, boolean enabled) {
        WebResource resource = (WebResource) _resources.get(asResourceName(name));
        if (resource == null) {
            throw new IllegalArgumentException("No defined resource " + name);
        }
        resource.setSendCharacterSet(enabled);
    }

    /**
     * Specifies the character set encoding for a resource.
     *
     * @param name
     *            the name
     * @param characterSet
     *            the character set
     */
    public void setCharacterSet(String name, String characterSet) {
        WebResource resource = (WebResource) _resources.get(asResourceName(name));
        if (resource == null) {
            resource = new WebResource("");
            _resources.put(asResourceName(name), resource);
        }
        resource.setCharacterSet(characterSet);
    }

    /**
     * Adds a header to a defined resource.
     *
     * @param name
     *            the name
     * @param header
     *            the header
     */
    public void addResourceHeader(String name, String header) {
        WebResource resource = (WebResource) _resources.get(asResourceName(name));
        if (resource == null) {
            resource = new WebResource("");
            _resources.put(asResourceName(name), resource);
        }
        resource.addHeader(header);
    }

    /**
     * Map to classpath.
     *
     * @param directory
     *            the directory
     */
    public void mapToClasspath(String directory) {
        _classpathDirs.add(directory);
    }

    /**
     * Sets the debug.
     *
     * @param debug
     *            the new debug
     */
    public void setDebug(boolean debug) {
        _debug = debug;
    }

    // ------------------------------------- private members ---------------------------------------

    /** The resources. */
    private Hashtable _resources = new Hashtable<>();

    /** The active. */
    private boolean _active = true;

    /** The debug. */
    private boolean _debug = false;

    /**
     * As resource name.
     *
     * @param rawName
     *            the raw name
     *
     * @return the string
     */
    private String asResourceName(String rawName) {
        if (rawName.startsWith("http:") || rawName.startsWith("/")) {
            return escape(rawName);
        }
        return escape("/" + rawName);
    }

    /**
     * Escape.
     *
     * @param urlString
     *            the url string
     *
     * @return the string
     */
    private static String escape(String urlString) {
        if (urlString.indexOf(' ') < 0) {
            return urlString;
        }
        StringBuilder sb = new StringBuilder();

        int start = 0;
        do {
            int index = urlString.indexOf(' ', start);
            if (index < 0) {
                sb.append(urlString.substring(start));
                break;
            }
            sb.append(urlString.substring(start, index)).append("%20");
            start = index + 1;
        } while (true);
        return sb.toString();
    }

    /**
     * Handle new connection.
     *
     * @param socket
     *            the socket
     */
    private void handleNewConnection(final Socket socket) {
        Thread t = new Thread("PseudoServer " + _serverNum + " connection " + (++_connectionNum)) {
            @Override
            public void run() {
                try {
                    serveRequests(socket);
                } catch (IOException e) {
                    e.printStackTrace(); // To change body of catch statement use Options | File Templates.
                }
            }
        };
        t.start();
    }

    /**
     * Serve requests.
     *
     * @param socket
     *            the socket
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void serveRequests(Socket socket) throws IOException {
        socket.setSoTimeout(_socketTimeout);
        socket.setTcpNoDelay(true);

        debug("Created server thread " + socket.getInetAddress() + ':' + socket.getPort());
        final BufferedInputStream inputStream = new BufferedInputStream(socket.getInputStream());
        final HttpResponseStream outputStream = new HttpResponseStream(socket.getOutputStream());

        try {
            while (_active) {
                HttpRequest request = new HttpRequest(inputStream);
                boolean keepAlive = respondToRequest(request, outputStream);
                if (!keepAlive) {
                    break;
                }
                while (_active && 0 == inputStream.available()) {
                    try {
                        Thread.sleep(INPUT_POLL_INTERVAL);
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                    }
                }
            }
        } catch (IOException e) {
            outputStream.restart();
            outputStream.setProtocol("HTTP/1.0");
            outputStream.setResponse(HttpURLConnection.HTTP_BAD_REQUEST, e.toString());
        }
        debug("Closing server thread");
        outputStream.close();
        socket.close();
        debug("Server thread closed");
    }

    /**
     * respond to the given request.
     *
     * @param request
     *            - the request
     * @param response
     *            - the response stream
     *
     * @return true, if successful
     */
    private boolean respondToRequest(HttpRequest request, HttpResponseStream response) {
        debug("Server thread handling request: " + request);
        boolean keepAlive = isKeepAlive(request);
        WebResource resource = null;
        try {
            response.restart();
            response.setProtocol(getResponseProtocol(request));
            resource = getResource(request);
            if (resource == null) {
                // what resource could not be find?
                String uri = request.getURI();
                // 404 - Not Found error code
                int errorCode = HttpURLConnection.HTTP_NOT_FOUND;
                // typical 404 error Message
                String errorMessage = "unable to find " + uri;
                // make sure there is a resource and
                // next time we'll take it from the resource Cache
                resource = setErrorResource(uri, errorCode, errorMessage);
                // set the errorCode for this response
                response.setResponse(errorCode, errorMessage);
            } else if (resource.getResponseCode() != HttpURLConnection.HTTP_OK) {
                response.setResponse(resource.getResponseCode(), "");
            }
            if (resource.closesConnection()) {
                keepAlive = false;
            }
            String[] headers = resource.getHeaders();
            for (String header : headers) {
                debug("Server thread sending header: " + header);
                response.addHeader(header);
            }
        } catch (UnknownMethodException e) {
            response.setResponse(HttpURLConnection.HTTP_BAD_METHOD, "unsupported method: " + e.getMethod());
        } catch (Throwable t) {
            t.printStackTrace();
            response.setResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, t.toString());
        }
        try {
            response.write(resource);
        } catch (IOException e) {
            System.out.println("*** Failed to send reply: " + e);
        }
        return keepAlive;
    }

    /**
     * Checks if is keep alive.
     *
     * @param request
     *            the request
     *
     * @return true, if is keep alive
     */
    private boolean isKeepAlive(HttpRequest request) {
        return request.wantsKeepAlive() && _maxProtocolLevel.equals("1.1");
    }

    /**
     * Gets the response protocol.
     *
     * @param request
     *            the request
     *
     * @return the response protocol
     */
    private String getResponseProtocol(HttpRequest request) {
        return _maxProtocolLevel.equalsIgnoreCase("1.1") ? request.getProtocol() : "HTTP/1.0";
    }

    /**
     * get the resource for the given request by first trying to look it up in the cache then depending on the type of
     * request PseudoServlet and the method / command e.g. GET/HEAD finally the extension of the uri ".zip" ".class" and
     * ".jar" are handled
     *
     * @param request
     *            the request
     *
     * @return the WebResource or null if non of the recipes above will lead to a valid resource
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private WebResource getResource(HttpRequest request) throws IOException {
        Object resource = _resources.get(request.getURI());
        if (resource == null) {
            resource = _resources.get(withoutParameters(request.getURI()));
        }

        // check the method of the request
        String command = request.getCommand();
        if ((command.equals("GET") || command.equals("HEAD")) && resource instanceof WebResource) {
            return (WebResource) resource;
        }
        if (resource instanceof PseudoServlet) {
            return getResource((PseudoServlet) resource, request);
        }
        if (request.getURI().endsWith(".class")) {
            for (Iterator iterator = _classpathDirs.iterator(); iterator.hasNext();) {
                String directory = (String) iterator.next();
                if (request.getURI().startsWith(directory)) {
                    String resourceName = request.getURI().substring(directory.length() + 1);
                    return new WebResource(getClass().getClassLoader().getResourceAsStream(resourceName),
                            "application/class", 200);
                }
            }
        } else if (request.getURI().endsWith(".zip") || request.getURI().endsWith(".jar")) {
            for (Iterator iterator = _classpathDirs.iterator(); iterator.hasNext();) {
                String directory = (String) iterator.next();
                if (request.getURI().startsWith(directory)) {
                    String resourceName = request.getURI().substring(directory.length() + 1);
                    String classPath = System.getProperty("java.class.path");
                    StringTokenizer st = new StringTokenizer(classPath, ":;,");
                    while (st.hasMoreTokens()) {
                        String file = st.nextToken();
                        if (file.endsWith(resourceName)) {
                            Path f = Path.of(file);
                            return new WebResource(Files.newInputStream(f), "application/zip", 200);
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Without parameters.
     *
     * @param uri
     *            the uri
     *
     * @return the string
     */
    private String withoutParameters(String uri) {
        return uri.indexOf('?') < 0 ? uri : uri.substring(0, uri.indexOf('?'));
    }

    /**
     * Gets the resource.
     *
     * @param servlet
     *            the servlet
     * @param request
     *            the request
     *
     * @return the resource
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private WebResource getResource(PseudoServlet servlet, HttpRequest request) throws IOException {
        servlet.init(request);
        return servlet.getResponse(request.getCommand());
    }

    /** The server socket. */
    private ServerSocket _serverSocket;

}

class HttpResponseStream {

    private static final String CRLF = "\r\n";

    void restart() {
        _headersWritten = false;
        _headers.clear();
        _responseCode = HttpURLConnection.HTTP_OK;
        _responseText = "OK";
    }

    void close() throws IOException {
        flushHeaders();
        _pw.close();
    }

    HttpResponseStream(OutputStream stream) {
        _stream = stream;
        setCharacterSet("us-ascii");
    }

    void setProtocol(String protocol) {
        _protocol = protocol;
    }

    /**
     * set the response to the given response Code
     *
     * @param responseCode
     * @param responseText
     */
    void setResponse(int responseCode, String responseText) {
        _responseCode = responseCode;
        _responseText = responseText;
    }

    void addHeader(String header) {
        _headers.add(header);
    }

    void write(String contents, String charset) throws IOException {
        flushHeaders();
        setCharacterSet(charset);
        sendText(contents);
    }

    void write(WebResource resource) throws IOException {
        flushHeaders();
        if (resource != null) {
            resource.writeTo(_stream);
        }
        _stream.flush();
    }

    private void setCharacterSet(String characterSet) {
        if (_pw != null) {
            _pw.flush();
        }
        _pw = new PrintWriter(new OutputStreamWriter(_stream, Charset.forName(characterSet)));
    }

    private void flushHeaders() {
        if (!_headersWritten) {
            sendResponse(_responseCode, _responseText);
            for (Enumeration e = Collections.enumeration(_headers); e.hasMoreElements();) {
                sendLine((String) e.nextElement());
            }
            sendText(CRLF);
            _headersWritten = true;
            _pw.flush();
        }
    }

    private void sendResponse(int responseCode, String responseText) {
        sendLine(_protocol + ' ' + responseCode + ' ' + responseText);
    }

    private void sendLine(String text) {
        sendText(text);
        sendText(CRLF);
    }

    private void sendText(String text) {
        _pw.write(text);
    }

    private OutputStream _stream;
    private PrintWriter _pw;

    private List _headers = new ArrayList<>();
    private String _protocol = "HTTP/1.0";
    private int _responseCode = HttpURLConnection.HTTP_OK;
    private String _responseText = "OK";

    private boolean _headersWritten;

}

class RecordingOutputStream extends OutputStream {

    private OutputStream _nestedStream;
    private PrintStream _log;

    public RecordingOutputStream(OutputStream nestedStream, PrintStream log) {
        _nestedStream = nestedStream;
        _log = log;
    }

    @Override
    public void write(int b) throws IOException {
        _nestedStream.write(b);
        _log.println("sending " + Integer.toHexString(b));
    }

    @Override
    public void write(byte b[], int offset, int len) throws IOException {
        _nestedStream.write(b, offset, len);
        _log.print("sending");
        for (int i = offset; i < offset + len; i++) {
            _log.print(' ' + Integer.toHexString(b[i]));
        }
        _log.println();
    }
}

class RecordingInputStream extends InputStream {

    private InputStream _nestedStream;
    private PrintStream _log;

    public RecordingInputStream(InputStream nestedStream, PrintStream log) {
        _nestedStream = nestedStream;
        _log = log;
    }

    @Override
    public int read() throws IOException {
        int value = _nestedStream.read();
        if (value != -1) {
            _log.print(' ' + Integer.toHexString(value));
        }
        return value;
    }
}

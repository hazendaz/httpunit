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

import com.meterware.httpunit.cookies.CookieJar;
import com.meterware.httpunit.cookies.CookieSource;
import com.meterware.httpunit.dom.DomWindow;
import com.meterware.httpunit.dom.DomWindowProxy;
import com.meterware.httpunit.dom.HTMLDocumentImpl;
import com.meterware.httpunit.dom.HTMLElementImpl;
import com.meterware.httpunit.protocol.MessageBody;
import com.meterware.httpunit.scripting.NamedDelegate;
import com.meterware.httpunit.scripting.ScriptableDelegate;
import com.meterware.httpunit.scripting.ScriptingHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A response to a web request from a web server.
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 * @author <a href="mailto:DREW.VARNER@oracle.com">Drew Varner</a>
 * @author <a href="mailto:dglo@ssec.wisc.edu">Dave Glowacki</a>
 * @author <a href="mailto:bx@bigfoot.com">Benoit Xhenseval</a>
 * @author Wolfgang Fahl
 **/
public abstract class WebResponse implements HTMLSegment, CookieSource, DomWindowProxy {

    private static final String HTML_CONTENT = "text/html";
    private static final String XHTML_CONTENT = "application/xhtml+xml";
    private static final String FAUX_XHTML_CONTENT = "text/xhtml";
    // [ 1281655 ] [patch] allow text/xml to be parsed as html
    // testTraversal test changed after positive reply by Russell
    private static final String XML_CONTENT = "text/xml";
    // the list of valid content Types
    private static String[] validContentTypes = { HTML_CONTENT, XHTML_CONTENT, FAUX_XHTML_CONTENT, XML_CONTENT };

    private static final int UNINITIALIZED_INT = -2;
    private static final int UNKNOWN_LENGTH_TIMEOUT = 500;
    private static final int UNKNOWN_LENGTH_RETRY_INTERVAL = 10;

    private FrameSelector _frame;
    // allow to switch off parsing e.g. for method="HEAD"
    private boolean _withParse = true;
    private String _baseTarget;
    private String _refreshHeader;
    private URL _baseURL;
    private boolean _parsingPage;

    /**
     * is parsing on?
     *
     * @return true if parsing is enabled
     */
    public boolean isWithParse() {
        return _withParse;
    }

    /**
     * set the parsing switch
     *
     * @param doParse
     */
    public void setWithParse(boolean doParse) {
        _withParse = doParse;
    }

    /**
     * Returns a web response built from a URL connection. Provided to allow access to WebResponse parsing without using
     * a WebClient.
     **/
    public static WebResponse newResponse(URLConnection connection) throws IOException {
        return new HttpWebResponse(null, FrameSelector.TOP_FRAME, connection.getURL(), connection,
                HttpUnitOptions.getExceptionsThrownOnErrorStatus());
    }

    /**
     * Returns true if the response is HTML.
     *
     * @return true if the contenType fits
     **/
    public boolean isHTML() {
        boolean result = false;
        // check the different content types
        for (String validContentType : validContentTypes) {
            result = getContentType().equalsIgnoreCase(validContentType);
            if (result) {
                break;
            }
        } // for
        return result;
    }

    /**
     * Returns the URL which invoked this response.
     **/
    @Override
    public URL getURL() {
        return _pageURL;
    }

    /**
     * Returns the title of the page.
     *
     * @exception SAXException
     *                thrown if there is an error parsing this response
     **/
    public String getTitle() throws SAXException {
        return getReceivedPage().getTitle();
    }

    /**
     * Returns the stylesheet linked in the head of the page. &lt;code&gt; &lt;link type="text/css" rel="stylesheet"
     * href="/mystyle.css" /&gt; &lt;/code&gt; will return "/mystyle.css".
     *
     * @exception SAXException
     *                thrown if there is an error parsing this response
     **/
    public String getExternalStyleSheet() throws SAXException {
        return getReceivedPage().getExternalStyleSheet();
    }

    /**
     * Retrieves the "content" of the meta tags for a key pair attribute-attributeValue. &lt;code&gt; &lt;meta
     * name="robots" content="index" /&gt; &lt;meta name="robots" content="follow" /&gt; &lt;meta http-equiv="Expires"
     * content="now" /&gt; &lt;/code&gt; this can be used like this &lt;code&gt; getMetaTagContent("name","robots") will
     * return { "index","follow" } getMetaTagContent("http-equiv","Expires") will return { "now" } &lt;/code&gt;
     *
     * @exception SAXException
     *                thrown if there is an error parsing this response
     **/
    public String[] getMetaTagContent(String attribute, String attributeValue) throws SAXException {
        return getReceivedPage().getMetaTagContent(attribute, attributeValue);
    }

    /**
     * Returns the name of the frame containing this page.
     **/
    public String getFrameName() {
        return _frame.getName();
    }

    void setFrame(FrameSelector frame) {
        if (!_frame.getName().equals(frame.getName())) {
            throw new IllegalArgumentException("May not modify the frame name");
        }
        _frame = frame;
    }

    /**
     * Returns the frame containing this page.
     */
    FrameSelector getFrame() {
        return _frame;
    }

    /**
     * Returns a request to refresh this page, if any. This request will be defined by a meta tag in the header. If no
     * tag exists, will return null.
     **/
    public WebRequest getRefreshRequest() {
        readRefreshRequest();
        return _refreshRequest;
    }

    /**
     * Returns the delay before normally following the request to refresh this page, if any. This request will be
     * defined by a meta tag in the header. If no tag exists, will return zero.
     **/
    public int getRefreshDelay() {
        readRefreshRequest();
        return _refreshDelay;
    }

    /**
     * Returns the response code associated with this response.
     **/
    public abstract int getResponseCode();

    /**
     * Returns the response message associated with this response.
     **/
    public abstract String getResponseMessage();

    /**
     * Returns the content length of this response.
     *
     * @return the content length, if known, or -1.
     */
    public int getContentLength() {
        if (_contentLength == UNINITIALIZED_INT) {
            String length = getHeaderField("Content-Length");
            _contentLength = length == null ? -1 : Integer.parseInt(length);
        }
        return _contentLength;
    }

    /**
     * Returns the content type of this response.
     **/
    public String getContentType() {
        if (_contentType == null) {
            readContentTypeHeader();
        }
        return _contentType;
    }

    /**
     * Returns the character set used in this response.
     **/
    public String getCharacterSet() {
        if (_characterSet == null) {
            readContentTypeHeader();
            if (_characterSet == null) {
                setCharacterSet(getHeaderField("Charset"));
            }
            if (_characterSet == null) {
                setCharacterSet(HttpUnitOptions.getDefaultCharacterSet());
            }
        }
        return _characterSet;
    }

    /**
     * Returns a list of new cookie names defined as part of this response.
     **/
    public String[] getNewCookieNames() {
        return getCookieJar().getCookieNames();
    }

    /**
     * Returns the new cookie value defined as part of this response.
     **/
    public String getNewCookieValue(String name) {
        return getCookieJar().getCookieValue(name);
    }

    /**
     * Returns the names of the header fields found in the response.
     **/
    public abstract String[] getHeaderFieldNames();

    /**
     * Returns the value for the specified header field. If no such field is defined, will return null. If more than one
     * header is defined for the specified name, returns only the first found.
     **/
    public abstract String getHeaderField(String fieldName);

    /**
     * Returns the actual byte stream of the response e.g. for download results
     *
     * @return the byte array read for this response
     *
     * @throws IOException
     */
    public byte[] getBytes() throws IOException {
        if (_responseText == null) {
            loadResponseText();
        }
        return _bytes;
    }

    /**
     * Returns the text of the response (excluding headers) as a string. Use this method in preference to 'toString'
     * which may be used to represent internal state of this object.
     *
     * @return the response text
     **/
    public String getText() throws IOException {
        if (_responseText == null) {
            loadResponseText();
        }
        return _responseText;
    }

    /**
     * Returns a buffered input stream for reading the contents of this reply.
     **/
    public InputStream getInputStream() throws IOException {
        if (_inputStream == null) {
            _inputStream = new ByteArrayInputStream(getText().getBytes(StandardCharsets.UTF_8));
        }
        return _inputStream;
    }

    /**
     * Returns the names of the frames found in the page in the order in which they appear.
     *
     * @exception SAXException
     *                thrown if there is an error parsing this response
     **/
    public String[] getFrameNames() throws SAXException {
        WebFrame[] frames = getFrames();
        String[] result = new String[frames.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = frames[i].getFrameName();
        }

        return result;
    }

    /**
     * Returns the frames found in the page in the order in which they appear.
     *
     * @exception SAXException
     *                thrown if there is an error parsing this response
     **/
    FrameSelector[] getFrameSelectors() throws SAXException {
        WebFrame[] frames = getFrames();
        FrameSelector[] result = new FrameSelector[frames.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = frames[i].getSelector();
        }

        return result;
    }

    /**
     * Returns the contents of the specified subframe of this frameset response.
     *
     * @param subFrameName
     *            the name of the desired frame as defined in the frameset.
     **/
    public WebResponse getSubframeContents(String subFrameName) {
        if (_window == null) {
            throw new NoSuchFrameException(subFrameName);
        }
        return _window.getSubframeContents(_frame, subFrameName);
    }

    // ---------------------- HTMLSegment methods -----------------------------

    /**
     * Returns the HTMLElement with the specified ID.
     *
     * @throws SAXException
     *             thrown if there is an error parsing the response.
     */
    @Override
    public HTMLElement getElementWithID(String id) throws SAXException {
        return getReceivedPage().getElementWithID(id);
    }

    /**
     * return the HTMLElements with the specified tag name
     *
     * @param tagName
     *            e.g. "div" or "table"
     *
     * @return a list of all HTMLElements with that tag name
     *
     * @throws SAXException
     *
     * @since 1.7
     */
    public HTMLElement[] getElementsByTagName(String tagName) throws SAXException {
        return getReceivedPage().getElementsByTagName(getDOM(), tagName);
    }

    /**
     * Returns a list of HTML element names contained in this HTML section.
     */
    @Override
    public String[] getElementNames() throws SAXException {
        return getReceivedPage().getElementNames();
    }

    /**
     * Returns the HTMLElements found in this segment with the specified name.
     */
    @Override
    public HTMLElement[] getElementsWithName(String name) throws SAXException {
        return getReceivedPage().getElementsWithName(name);
    }

    /**
     * Returns the HTMLElements found in this segment with the specified class.
     */
    public HTMLElement[] getElementsWithClassName(String className) throws SAXException {
        return getReceivedPage().getElementsWithClassName(className);
    }

    /**
     * Returns the HTMLElements found with the specified attribute value.
     *
     * @since 1.6
     */
    @Override
    public HTMLElement[] getElementsWithAttribute(String name, String value) throws SAXException {
        return getReceivedPage().getElementsWithAttribute(name, value);
    }

    /**
     * Returns the forms found in the page in the order in which they appear.
     *
     * @exception SAXException
     *                thrown if there is an error parsing the response.
     **/
    @Override
    public WebForm[] getForms() throws SAXException {
        return getReceivedPage().getForms();
    }

    /**
     * Returns the form found in the page with the specified name.
     *
     * @exception SAXException
     *                thrown if there is an error parsing the response.
     **/
    @Override
    public WebForm getFormWithName(String name) throws SAXException {
        return getReceivedPage().getFormWithName(name);
    }

    /**
     * Returns the form found in the page with the specified ID.
     *
     * @exception SAXException
     *                thrown if there is an error parsing the response.
     **/
    @Override
    public WebForm getFormWithID(String ID) throws SAXException {
        return getReceivedPage().getFormWithID(ID);
    }

    /**
     * Returns the first form found in the page matching the specified criteria.
     *
     * @exception SAXException
     *                thrown if there is an error parsing the response.
     **/
    @Override
    public WebForm getFirstMatchingForm(HTMLElementPredicate predicate, Object criteria) throws SAXException {
        return getReceivedPage().getFirstMatchingForm(predicate, criteria);
    }

    /**
     * Returns all forms found in the page matching the specified criteria.
     *
     * @exception SAXException
     *                thrown if there is an error parsing the response.
     **/
    @Override
    public WebForm[] getMatchingForms(HTMLElementPredicate predicate, Object criteria) throws SAXException {
        return getReceivedPage().getMatchingForms(predicate, criteria);
    }

    /**
     * Returns the links found in the page in the order in which they appear.
     *
     * @exception SAXException
     *                thrown if there is an error parsing the response.
     **/
    @Override
    public WebLink[] getLinks() throws SAXException {
        return getReceivedPage().getLinks();
    }

    /**
     * Returns the first link which contains the specified text.
     *
     * @exception SAXException
     *                thrown if there is an error parsing the response.
     **/
    @Override
    public WebLink getLinkWith(String text) throws SAXException {
        return getReceivedPage().getLinkWith(text);
    }

    /**
     * Returns the first link which contains an image with the specified text as its 'alt' attribute.
     *
     * @exception SAXException
     *                thrown if there is an error parsing the response.
     **/
    @Override
    public WebLink getLinkWithImageText(String text) throws SAXException {
        return getReceivedPage().getLinkWithImageText(text);
    }

    /**
     * Returns the link found in the page with the specified name.
     *
     * @exception SAXException
     *                thrown if there is an error parsing the response.
     **/
    public WebLink getLinkWithName(String name) throws SAXException {
        return getReceivedPage().getLinkWithName(name);
    }

    /**
     * Returns the link found in the page with the specified ID.
     *
     * @exception SAXException
     *                thrown if there is an error parsing the response.
     **/
    public WebLink getLinkWithID(String ID) throws SAXException {
        return getReceivedPage().getLinkWithID(ID);
    }

    /**
     * Returns the first link found in the page matching the specified criteria.
     *
     * @exception SAXException
     *                thrown if there is an error parsing the response.
     **/
    @Override
    public WebLink getFirstMatchingLink(HTMLElementPredicate predicate, Object criteria) throws SAXException {
        return getReceivedPage().getFirstMatchingLink(predicate, criteria);
    }

    /**
     * Returns all links found in the page matching the specified criteria.
     *
     * @exception SAXException
     *                thrown if there is an error parsing the response.
     **/
    @Override
    public WebLink[] getMatchingLinks(HTMLElementPredicate predicate, Object criteria) throws SAXException {
        return getReceivedPage().getMatchingLinks(predicate, criteria);
    }

    /**
     * Returns the images found in the page in the order in which they appear.
     *
     * @exception SAXException
     *                thrown if there is an error parsing the response.
     **/
    @Override
    public WebImage[] getImages() throws SAXException {
        return getReceivedPage().getImages();
    }

    /**
     * Returns the image found in the page with the specified name attribute.
     *
     * @exception SAXException
     *                thrown if there is an error parsing the response.
     **/
    @Override
    public WebImage getImageWithName(String source) throws SAXException {
        return getReceivedPage().getImageWithName(source);
    }

    /**
     * Returns the first image found in the page with the specified src attribute.
     *
     * @exception SAXException
     *                thrown if there is an error parsing the response.
     **/
    @Override
    public WebImage getImageWithSource(String source) throws SAXException {
        return getReceivedPage().getImageWithSource(source);
    }

    /**
     * Returns the first image found in the page with the specified alt attribute.
     **/
    @Override
    public WebImage getImageWithAltText(String altText) throws SAXException {
        return getReceivedPage().getImageWithAltText(altText);
    }

    @Override
    public WebApplet[] getApplets() throws SAXException {
        return getReceivedPage().getApplets();
    }

    /**
     * Returns an array of text blocks found in the page.
     *
     * @since 1.6
     */
    @Override
    public TextBlock[] getTextBlocks() throws SAXException {
        return getReceivedPage().getTextBlocks();
    }

    /**
     * Returns the text block after the specified block, if any.
     *
     * @since 1.6
     */
    public TextBlock getNextTextBlock(TextBlock block) throws SAXException {
        return getReceivedPage().getNextTextBlock(block);
    }

    /**
     * Returns the first link found in the page matching the specified criteria.
     *
     * @since 1.6
     *
     * @exception SAXException
     *                thrown if there is an error parsing the response.
     **/
    public TextBlock getFirstMatchingTextBlock(HTMLElementPredicate predicate, Object criteria) throws SAXException {
        return getReceivedPage().getFirstMatchingTextBlock(predicate, criteria);
    }

    /**
     * Returns a copy of the domain object model tree associated with this response. If the response is HTML, it will
     * use a special parser which can transform HTML into an XML DOM.
     *
     * @exception SAXException
     *                thrown if there is an error parsing the response.
     **/
    public Document getDOM() throws SAXException {
        if (isHTML()) {
            return (Document) getReceivedPage().getDOM();
        }
        try {
            return HttpUnitUtils.parse(new InputSource(new StringReader(getText())));
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    /**
     * Returns the top-level tables found in this page in the order in which they appear.
     *
     * @exception SAXException
     *                thrown if there is an error parsing the response.
     **/
    @Override
    public WebTable[] getTables() throws SAXException {
        return getReceivedPage().getTables();
    }

    /**
     * Returns the first table in the response which matches the specified predicate and value. Will recurse into any
     * nested tables, as needed.
     *
     * @return the selected table, or null if none is found
     **/
    @Override
    public WebTable getFirstMatchingTable(HTMLElementPredicate predicate, Object criteria) throws SAXException {
        return getReceivedPage().getFirstMatchingTable(predicate, criteria);
    }

    /**
     * Returns all tables found in the page matching the specified criteria.
     *
     * @exception SAXException
     *                thrown if there is an error parsing the response.
     **/
    @Override
    public WebTable[] getMatchingTables(HTMLElementPredicate predicate, Object criteria) throws SAXException {
        return getReceivedPage().getMatchingTables(predicate, criteria);
    }

    /**
     * Returns the first table in the response which has the specified text as the full text of its first non-blank row
     * and non-blank column. Will recurse into any nested tables, as needed. Case is ignored.
     *
     * @exception SAXException
     *                thrown if there is an error parsing the response.
     *
     * @return the selected table, or null if none is found
     **/
    @Override
    public WebTable getTableStartingWith(String text) throws SAXException {
        return getReceivedPage().getTableStartingWith(text);
    }

    /**
     * Returns the first table in the response which has the specified text as a prefix of the text of its first
     * non-blank row and non-blank column. Will recurse into any nested tables, as needed. Case is ignored.
     *
     * @exception SAXException
     *                thrown if there is an error parsing the response.
     *
     * @return the selected table, or null if none is found
     **/
    @Override
    public WebTable getTableStartingWithPrefix(String text) throws SAXException {
        return getReceivedPage().getTableStartingWithPrefix(text);
    }

    /**
     * Returns the first table in the response which has the specified text as its summary attribute. Will recurse into
     * any nested tables, as needed. Case is ignored.
     *
     * @exception SAXException
     *                thrown if there is an error parsing the response.
     *
     * @return the selected table, or null if none is found
     **/
    @Override
    public WebTable getTableWithSummary(String text) throws SAXException {
        return getReceivedPage().getTableWithSummary(text);
    }

    /**
     * Returns the first table in the response which has the specified text as its ID attribute. Will recurse into any
     * nested tables, as needed. Case is ignored.
     *
     * @exception SAXException
     *                thrown if there is an error parsing the response.
     *
     * @return the selected table, or null if none is found
     **/
    @Override
    public WebTable getTableWithID(String text) throws SAXException {
        return getReceivedPage().getTableWithID(text);
    }

    // ---------------------------------------- JavaScript methods ----------------------------------------

    /**
     * get the scriptable object for this WebResponse
     */
    public Scriptable getScriptableObject() {
        ScriptingHandler result = this.getScriptingHandler();
        if (!(result instanceof Scriptable)) {
            throw new RuntimeException(
                    "getScriptableObject failed for " + result.getClass().getName() + " - not a Scriptable");
        }
        return (Scriptable) result;
    }

    public void setScriptingHandler(ScriptingHandler scriptingHandler) {
        _scriptingHandler = scriptingHandler;
    }

    @Override
    public ScriptingHandler getScriptingHandler() {
        if (_scriptingHandler == null) {
            _scriptingHandler = HttpUnitOptions.getScriptingEngine().createHandler(this);
        }
        return _scriptingHandler;
    }

    public ScriptingHandler createJavascriptScriptingHandler() {
        return new Scriptable();
    }

    /**
     * create a DOMScriptingHandler
     *
     * @return the DOM scripting handler (the window)
     */
    public ScriptingHandler createDomScriptingHandler() {
        if (!isHTML()) {
            return new DomWindow(this);
        }
        try {
            HTMLPage page = this.getReceivedPage();
            Node rootNode = page.getRootNode();
            HTMLDocumentImpl document = (HTMLDocumentImpl) rootNode;
            DomWindow result = document.getWindow();
            result.setProxy(this);
            return result;
        } catch (SAXException e) {
            return new DomWindow(this);
        }
    }

    public static ScriptableDelegate newDelegate(String delegateClassName) {
        if (delegateClassName.equalsIgnoreCase("Option")) {
            return FormControl.newSelectionOption();
        }
        throw new IllegalArgumentException("No such scripting class supported: " + delegateClassName);
    }

    HTMLPage.Scriptable getDocumentScriptable() {
        return getScriptableObject().getDocument();
    }

    /**
     * open a a new Window with the given name and relative URL
     *
     * @param name
     *            - the name of the window
     * @param relativeUrl
     *            - the relative URL to be used
     *
     * @return the WebResponse as a DomWindowProxy
     */
    @Override
    public DomWindowProxy openNewWindow(String name, String relativeUrl) throws IOException, SAXException {
        if (relativeUrl == null || relativeUrl.trim().isEmpty()) {
            relativeUrl = "about:";
        }
        GetMethodWebRequest request = new GetMethodWebRequest(getURL(), relativeUrl, _frame, name);
        return _window.getResponse(request);
    }

    @Override
    public DomWindowProxy submitRequest(HTMLElementImpl sourceElement, String method, String location, String target,
            MessageBody requestBody) throws IOException, SAXException {
        if (method.equalsIgnoreCase("get")) {
            return getWindow().sendRequest(new GetMethodWebRequest(this, sourceElement, getURL(), location, target));
        }
        return null;
    }

    @Override
    public void close() {
        if (getFrameName().equals(WebRequest.TOP_FRAME)) {
            _window.close();
        }
    }

    @Override
    public void alert(String message) {
        _client.postAlert(message);
    }

    @Override
    public boolean confirm(String message) {
        return _client.getConfirmationResponse(message);
    }

    @Override
    public String prompt(String prompt, String defaultResponse) {
        return _client.getUserResponse(prompt, defaultResponse);
    }

    String getBaseTarget() {
        return _baseTarget;
    }

    public class Scriptable extends ScriptableDelegate implements NamedDelegate {

        public void alertUser(String message) {
            alert(message);
        }

        public boolean getConfirmationResponse(String message) {
            return confirm(message);
        }

        public String getUserResponse(String prompt, String defaultResponse) {
            return prompt(prompt, defaultResponse);
        }

        public ClientProperties getClientProperties() {
            return _client == null ? ClientProperties.getDefaultProperties() : _client.getClientProperties();
        }

        public HTMLPage.Scriptable getDocument() {
            try {
                if (!isHTML()) {
                    replaceText(BLANK_HTML, HTML_CONTENT);
                }
                return getReceivedPage().getScriptableObject();
            } catch (SAXException e) {
                throw new RuntimeException(e.toString());
            }
        }

        public Scriptable[] getFrames() throws SAXException {
            String[] names = getFrameNames();
            Scriptable[] frames = new Scriptable[names.length];
            for (int i = 0; i < frames.length; i++) {
                frames[i] = getSubframeContents(names[i]).getScriptableObject();
            }
            return frames;
        }

        public void load() throws SAXException {
            if (isHTML() && isWithParse()) {
                getReceivedPage().getForms(); // TODO be more explicit here - don't care about forms, after all
                doEventScript(getReceivedPage().getOnLoadEvent());
            }
        }

        public Scriptable open(String urlString, String name, String features, boolean replace)
                throws IOException, SAXException {
            WebResponse response = (WebResponse) openNewWindow(name, urlString);
            return response == null ? null : response.getScriptableObject();
        }

        public void closeWindow() {
            close();
        }

        /**
         * Returns the value of the named property. Will return null if the property does not exist.
         **/
        @Override
        public Object get(String propertyName) {
            if (propertyName.equals("name")) {
                return getName();
            }
            if (propertyName.equalsIgnoreCase("top")) {
                return _window.getFrameContents(WebRequest.TOP_FRAME).getScriptableObject();
            }
            if (propertyName.equalsIgnoreCase("parent")) {
                return _window.getParentFrameContents(_frame).getScriptableObject();
            }
            if (propertyName.equalsIgnoreCase("opener")) {
                return getFrameName().equals(WebRequest.TOP_FRAME) ? getScriptable(_window.getOpener()) : null;
            }
            if (propertyName.equalsIgnoreCase("closed")) {
                return getFrameName().equals(WebRequest.TOP_FRAME) && _window.isClosed() ? Boolean.TRUE : Boolean.FALSE;
            }
            try {
                return getSubframeContents(propertyName).getScriptableObject();
            } catch (NoSuchFrameException e) {
                return super.get(propertyName);
            }
        }

        @Override
        public String getName() {
            String windowName = getFrameName().equals(WebRequest.TOP_FRAME) ? _window.getName() : getFrameName();
            return windowName.startsWith(WebWindow.NO_NAME) ? "" : windowName;
        }

        private Scriptable getScriptable(WebResponse opener) {
            return opener == null ? null : opener.getScriptableObject();
        }

        /**
         * Sets the value of the named property. Will throw a runtime exception if the property does not exist or cannot
         * accept the specified value.
         **/
        @Override
        public void set(String propertyName, Object value) {
            if (propertyName.equals("name")) {
                if (value == null) {
                    value = "";
                }
                if (getFrameName().equals(WebRequest.TOP_FRAME)) {
                    _window.setName(value.toString());
                }
            } else {
                super.set(propertyName, value);
            }
        }

        public void setLocation(String relativeURL) throws IOException, SAXException {
            getWindow().getResponse(new GetMethodWebRequest(_pageURL, relativeURL, _frame.getName()));
        }

        public URL getURL() {
            return WebResponse.this._pageURL;
        }
    }

    // ---------------------------------------- Object methods --------------------------------------------

    @Override
    public abstract String toString();

    // ----------------------------------------- protected members -----------------------------------------------

    /**
     * Constructs a response object. see [ 1159858 ] patch for RFE 1159844 (parsing intercepted pages)
     *
     * @param frame
     *            the frame to hold the response
     * @param url
     *            the url from which the response was received
     **/
    protected WebResponse(WebClient client, FrameSelector frame, URL url) {
        _client = client;
        _baseURL = _pageURL = url;
        _baseTarget = frame.getName();
        _frame = frame;
        // intialize window for interception as described in
        // https://sourceforge.net/tracker/index.php?func=detail&aid=1159844&group_id=6550&atid=356550
        if (client != null) {
            _window = client.getMainWindow();
        }
    }

    /**
     * Constructs a response object.
     *
     * @param frame
     *            the frame to hold the response
     * @param url
     *            the url from which the response was received
     **/
    protected WebResponse(WebClient client, FrameSelector frame, URL url, String text) {
        this(client, frame, url);
        _responseText = text;
    }

    protected final void defineRawInputStream(InputStream inputStream) throws IOException {
        if (_inputStream != null || _responseText != null) {
            throw new IllegalStateException("Must be called before response text is defined.");
        }

        // please note bug report [ 1119205 ] EOFExceptions while using a Proxy
        // and patch proposal below
        // by Ralf Bust
        /*
         * original 1.6.2 code if (encodedUsingGZIP()) { byte[] compressedData = readFromStream( inputStream,
         * getContentLength() ); _inputStream = new GZIPInputStream( new ByteArrayInputStream( compressedData ) ); }
         * else { _inputStream = inputStream; }
         */

        if (encodedUsingGZIP()) {
            try {
                _inputStream = new GZIPInputStream(inputStream);
            } catch (EOFException eof) {
                _inputStream = inputStream;
            }
        } else {
            _inputStream = inputStream;
        }
    }

    private boolean encodedUsingGZIP() {
        String encoding = getHeaderField("Content-Encoding");
        return encoding != null && encoding.indexOf("gzip") >= 0;
    }

    /**
     * Overwrites the current value (if any) of the content type header.
     **/
    protected void setContentTypeHeader(String value) {
        _contentHeader = value;
    }

    // ------------------------------------------ package members ------------------------------------------------

    static final String BLANK_HTML = "";

    static WebResponse createBlankResponse() {
        return new DefaultWebResponse(BLANK_HTML);
    }

    WebWindow getWindow() {
        return _window;
    }

    void setWindow(WebWindow window) {
        _window = window;
    }

    /**
     * replace the given text
     *
     * @param text
     *            - the text to replace
     * @param contentType
     *            - the contenttype
     */
    @Override
    public boolean replaceText(String text, String contentType) {
        if (_parsingPage) {
            return false;
        }
        _responseText = text;
        _inputStream = null;
        _page = null;
        _contentType = contentType;
        _baseURL = null;
        _baseTarget = _frame.getName();
        _refreshHeader = null;

        try {
            readTags(text.getBytes(StandardCharsets.UTF_8));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Failure while attempting to reparse text: " + e);
        }
        return true;
    }

    /**
     * Returns the frames found in the page in the order in which they appear.
     **/
    WebRequest[] getFrameRequests() throws SAXException {
        WebFrame[] frames = getFrames();
        Vector requests = new Vector<>();
        for (WebFrame frame : frames) {
            if (frame.hasInitialRequest()) {
                requests.addElement(frame.getInitialRequest());
            }
        }

        WebRequest[] result = new WebRequest[requests.size()];
        requests.copyInto(result);
        return result;
    }

    // --------------------------------- private members --------------------------------------

    private WebWindow _window;

    private HTMLPage _page;

    private String _contentHeader;

    private int _contentLength = UNINITIALIZED_INT;

    private String _contentType;

    private String _characterSet;

    private WebRequest _refreshRequest;

    private int _refreshDelay = -1; // initialized to invalid value

    /**
     * the response as a String
     */
    private String _responseText;

    /**
     * the response as a byte array
     */
    private byte[] _bytes;

    private InputStream _inputStream;

    private final URL _pageURL;

    private final WebClient _client;

    /**
     * getter for the WebClient
     *
     * @since 1.7
     *
     * @return the web client for this WebResponse (if any)
     */
    public WebClient getClient() {
        return _client;
    }

    private ScriptingHandler _scriptingHandler;

    protected void loadResponseText() throws IOException {
        if (_responseText != null) {
            throw new IllegalStateException("May only invoke loadResponseText once");
        }
        _responseText = "";

        try (InputStream inputStream = getInputStream()) {
            final int contentLength = this.encodedUsingGZIP() ? -1 : getContentLength();
            int bytesRemaining = contentLength < 0 ? Integer.MAX_VALUE : contentLength;
            _bytes = readFromStream(inputStream, bytesRemaining);

            readTags(_bytes);
            _responseText = new String(_bytes, getCharacterSet());
            _inputStream = new ByteArrayInputStream(_bytes);

            if (HttpUnitOptions.isCheckContentLength() && contentLength >= 0 && _bytes.length != contentLength) {
                throw new IOException(
                        "Truncated message. Expected length: " + contentLength + ", Actual length: " + _bytes.length);
            }
        }
    }

    private byte[] readFromStream(InputStream inputStream, int maxBytes) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8 * 1024];
        int count = 0;
        if (maxBytes > 0) {
            do {
                outputStream.write(buffer, 0, count);
                maxBytes -= count;
                if (maxBytes <= 0) {
                    break;
                }
                count = inputStream.read(buffer, 0, Math.min(maxBytes, buffer.length));
            } while (count != -1);
        } else {
            do {
                outputStream.write(buffer, 0, count);
                int available = getAvailableBytes(inputStream);
                count = available == 0 ? -1 : inputStream.read(buffer, 0, buffer.length);
            } while (count != -1);
        }

        return outputStream.toByteArray();
    }

    private int getAvailableBytes(InputStream inputStream) throws IOException {
        int timeLeft = UNKNOWN_LENGTH_TIMEOUT;
        int available;
        do {
            timeLeft -= UNKNOWN_LENGTH_RETRY_INTERVAL;
            try {
                Thread.sleep(UNKNOWN_LENGTH_RETRY_INTERVAL);
            } catch (InterruptedException e) {
                /* do nothing */ }
            available = inputStream.available();
        } while (available == 0 && timeLeft > 0);
        return available;
    }

    /**
     * read the tags from the given message
     *
     * @param rawMessage
     *
     * @throws MalformedURLException
     */
    private void readTags(byte[] rawMessage) throws MalformedURLException {
        ByteTagParser parser = new ByteTagParser(rawMessage);
        ByteTag tag = parser.getNextTag();
        while (tag != null) {
            if (tag.getName().equalsIgnoreCase("meta")) {
                processMetaTag(tag);
            }
            if (tag.getName().equalsIgnoreCase("base")) {
                processBaseTag(tag);
            }
            // loop over a noscript region
            if (tag.getName().equalsIgnoreCase("noscript") && HttpUnitOptions.isScriptingEnabled()) {
                do {
                    tag = parser.getNextTag();
                } while (!tag.getName().equalsIgnoreCase("/noscript"));
            }
            tag = parser.getNextTag();
        }
    }

    private void processBaseTag(ByteTag tag) throws MalformedURLException {
        if (tag.getAttribute("href") != null) {
            _baseURL = new URL(getURL(), tag.getAttribute("href"));
        }
        if (tag.getAttribute("target") != null) {
            _baseTarget = tag.getAttribute("target");
        }
    }

    /**
     * process MetaTags based on the tag
     *
     * @param tag
     */
    private void processMetaTag(ByteTag tag) {
        if (isHttpEquivMetaTag(tag, "content-type")) {
            inferContentType(tag.getAttribute("content"));
        } else if (isHttpEquivMetaTag(tag, "refresh")) {
            inferRefreshHeader(tag.getAttribute("content"));
        }
    }

    /**
     * check whether the given tag is a http equiv meta tag
     *
     * @param tag
     * @param headerName
     *
     * @return
     */
    private boolean isHttpEquivMetaTag(ByteTag tag, String headerName) {
        String equiv1 = tag.getAttribute("http_equiv");
        String equiv2 = tag.getAttribute("http-equiv");
        return headerName.equalsIgnoreCase(equiv1) || headerName.equalsIgnoreCase(equiv2);
    }

    /**
     * infer the refresh Header
     *
     * @param refreshHeader
     */
    private void inferRefreshHeader(String refreshHeader) {
        String originalHeader = getHeaderField("Refresh");
        // System.err.println("original='"+originalHeader+"'\nrefreshHeader='"+refreshHeader+"'");
        if (originalHeader == null) {
            _refreshHeader = refreshHeader;
        }
    }

    /**
     * read the Refresh Request
     */
    private void readRefreshRequest() {
        if (_refreshDelay >= 0) {
            return;
        }
        _refreshDelay = 0;
        String refreshHeader = _refreshHeader != null ? _refreshHeader : getHeaderField("Refresh");
        if (refreshHeader == null) {
            return;
        }

        int semicolonIndex = refreshHeader.indexOf(';');
        if (semicolonIndex < 0) {
            interpretRefreshHeaderElement(refreshHeader, refreshHeader);
        } else {
            interpretRefreshHeaderElement(refreshHeader.substring(0, semicolonIndex), refreshHeader);
            interpretRefreshHeaderElement(refreshHeader.substring(semicolonIndex + 1), refreshHeader);
        }
        if (_refreshRequest == null) {
            _refreshRequest = new GetMethodWebRequest(_pageURL, _pageURL.toString(), _frame.getName());
        }
    }

    private void interpretRefreshHeaderElement(String token, String refreshHeader) {
        if (token.isEmpty()) {
            return;
        }
        try {
            if (Character.isDigit(token.charAt(0))) {
                _refreshDelay = Integer.parseInt(token);
            } else {
                _refreshRequest = new GetMethodWebRequest(_pageURL, getRefreshURL(token), _frame.getName());
            }
        } catch (NumberFormatException e) {
            System.out.println("Unable to interpret refresh tag: \"" + refreshHeader + '"');
        }
    }

    private String getRefreshURL(String text) {
        text = text.trim();
        if (!text.toUpperCase().startsWith("URL")) {
            return HttpUnitUtils.stripQuotes(text);
        }
        int splitIndex = text.indexOf('=');
        String value = text.substring(splitIndex + 1).trim();
        return HttpUnitUtils.replaceEntities(HttpUnitUtils.stripQuotes(value));
    }

    private void inferContentType(String contentTypeHeader) {
        String originalHeader = getHeaderField("Content-type");
        if (originalHeader == null || originalHeader.indexOf("charset") < 0) {
            setContentTypeHeader(contentTypeHeader);
        }
    }

    CookieJar getCookieJar() {
        if (_cookies == null) {
            _cookies = new CookieJar(this);
        }
        return _cookies;
    }

    private CookieJar _cookies;

    private void readContentTypeHeader() {
        String contentHeader = _contentHeader != null ? _contentHeader : getHeaderField("Content-type");
        if (contentHeader == null) {
            _contentType = HttpUnitOptions.getDefaultContentType();
            setCharacterSet(HttpUnitOptions.getDefaultCharacterSet());
            _contentHeader = _contentType + ";charset=" + _characterSet;
        } else {
            String[] parts = HttpUnitUtils.parseContentTypeHeader(contentHeader);
            if (null != _client && null != _client.getClientProperties().getOverrideContentType()) {
                _contentType = _client.getClientProperties().getOverrideContentType();
            } else {
                _contentType = parts[0];
            }
            if (parts[1] != null) {
                setCharacterSet(parts[1]);
            }
        }
    }

    private WebFrame[] getFrames() throws SAXException {
        if (isWithParse()) {
            return getReceivedPage().getFrames();
        }
        return new WebFrame[0];
    }

    /**
     * get the received Page
     *
     * @return the received page
     *
     * @throws SAXException
     */
    HTMLPage getReceivedPage() throws SAXException {
        if (_page == null) {
            try {
                _parsingPage = true;
                if (HttpUnitOptions.isCheckHtmlContentType() && !isHTML()) {
                    throw new NotHTMLException(getContentType());
                }
                _page = new HTMLPage(this, _frame, _baseURL, _baseTarget, getCharacterSet());
                if (_withParse) {
                    _page.parse(getText(), _pageURL);
                    if (_page == null) {
                        throw new IllegalStateException("replaceText called in the middle of getReceivedPage()");
                    }
                    ((HTMLDocumentImpl) _page.getRootNode()).getWindow().setProxy(this);
                }
            } catch (IOException e) {
                HttpUnitUtils.handleException(e);
                throw new RuntimeException(e.toString());
            } finally {
                _parsingPage = false;
            }
        }
        return _page;
    }

    private static String _defaultEncoding;

    private static final String[] DEFAULT_ENCODING_CANDIDATES = { StandardCharsets.ISO_8859_1.name(),
            StandardCharsets.US_ASCII.name() };

    static String getDefaultEncoding() {
        if (_defaultEncoding == null) {
            for (String element : DEFAULT_ENCODING_CANDIDATES) {
                if (isSupportedCharacterSet(element)) {
                    return _defaultEncoding = element;
                }
            }
            _defaultEncoding = Charset.defaultCharset().displayName();
        }
        return _defaultEncoding;
    }

    private void setCharacterSet(String characterSet) {
        if (characterSet == null) {
            return;
        }

        _characterSet = isSupportedCharacterSet(characterSet) ? characterSet : getDefaultEncoding();
    }

    private static boolean isSupportedCharacterSet(String characterSet) {
        try {
            return "abcd".getBytes(Charset.forName(characterSet)).length > 0;
        } catch (UnsupportedCharsetException e) {
            return false;
        }
    }

    void setCookie(String name, String value) {
        _client.putCookie(name, value);
    }

    String getCookieHeader() {
        return _client.getCookieJar().getCookieHeaderField(getURL());
    }

    String getReferer() {
        return null;
    }

    // =======================================================================================

    static class ByteTag {

        ByteTag(byte[] buffer, int start, int length) {
            _buffer = new String(buffer, start, length, Charset.forName(WebResponse.getDefaultEncoding()))
                    .toCharArray();
            _name = nextToken();

            String attribute = "";
            String token = nextToken();
            while (!token.isEmpty()) {
                if (token.equals("=") && !attribute.isEmpty()) {
                    getAttributes().put(attribute.toLowerCase(), nextToken());
                    attribute = "";
                } else {
                    if (!attribute.isEmpty()) {
                        getAttributes().put(attribute.toLowerCase(), "");
                    }
                    attribute = token;
                }
                token = nextToken();
            }
        }

        public String getName() {
            return _name;
        }

        public String getAttribute(String attributeName) {
            return (String) getAttributes().get(attributeName);
        }

        @Override
        public String toString() {
            return "ByteTag[ name=" + _name + ";attributes = " + _attributes + ']';
        }

        private Hashtable getAttributes() {
            if (_attributes == null) {
                _attributes = new Hashtable<>();
            }
            return _attributes;
        }

        private String _name = "";
        private Hashtable _attributes;

        private char[] _buffer;
        private int _end = -1;

        private String nextToken() {
            int start = _end + 1;
            while (start < _buffer.length && Character.isWhitespace(_buffer[start])) {
                start++;
            }
            if (start >= _buffer.length) {
                return "";
            }
            if (_buffer[start] == '"') {
                for (_end = start + 1; _end < _buffer.length && _buffer[_end] != '"'; _end++) {

                }
                return new String(_buffer, start + 1, _end - start - 1);
            }
            if (_buffer[start] == '\'') {
                for (_end = start + 1; _end < _buffer.length && _buffer[_end] != '\''; _end++) {

                }
                return new String(_buffer, start + 1, _end - start - 1);
            }
            if (_buffer[start] == '=') {
                _end = start;
                return "=";
            }
            for (_end = start + 1; _end < _buffer.length && _buffer[_end] != '='
                    && !Character.isWhitespace(_buffer[_end]); _end++) {

            }
            return new String(_buffer, start, _end-- - start);
        }
    }

    // =======================================================================================

    static class ByteTagParser {
        ByteTagParser(byte[] buffer) {
            _buffer = buffer;
        }

        ByteTag getNextTag() {
            ByteTag byteTag = null;
            do {
                int _start = _end + 1;
                while (_start < _buffer.length && _buffer[_start] != '<') {
                    _start++;
                }
                // proposed patch for bug report
                // [ 1376739 ] iframe tag not recognized if Javascript code contains '<'
                // by Nathan Jakubiak
                // uncommented since it doesn't seem to fix the test in WebFrameTest.java
                // if (_scriptDepth > 0 && _start+1 < _buffer.length &&
                // _buffer[ _start+1 ] != '/') {
                // _end = _start+1;
                // continue;
                // }
                for (_end = _start + 1; _end < _buffer.length && _buffer[_end] != '>'; _end++) {

                }
                if (_end >= _buffer.length || _end < _start) {
                    return null;
                }
                byteTag = new ByteTag(_buffer, _start + 1, _end - _start - 1);
                if (byteTag.getName().equalsIgnoreCase("script")) {
                    _scriptDepth++;
                    return byteTag;
                }
                if (byteTag.getName().equalsIgnoreCase("/script")) {
                    _scriptDepth--;
                }
            } while (_scriptDepth > 0);
            return byteTag;
        }

        private int _scriptDepth = 0;
        private int _end = -1;

        private byte[] _buffer;
    }

    /**
     * allow access to the valid content Types
     *
     * @since 1.7
     *
     * @return the validContentTypes
     */
    public static String[] getValidContentTypes() {
        return validContentTypes;
    }

    /**
     * allow modification of the valid content Types use with care
     *
     * @since 1.7
     *
     * @param validContentTypes
     *            the validContentTypes to set
     */
    protected static void setValidContentTypes(String[] validContentTypes) {
        WebResponse.validContentTypes = validContentTypes;
    }

}

// =======================================================================================

class DefaultWebResponse extends WebResponse {

    DefaultWebResponse(String text) {
        this(null, null, text);
    }

    DefaultWebResponse(WebClient client, URL url, String text) {
        this(client, FrameSelector.TOP_FRAME, url, text);
    }

    DefaultWebResponse(WebClient client, FrameSelector frame, URL url, String text) {
        super(client, frame, url, text);
    }

    /**
     * Returns the response code associated with this response.
     **/
    @Override
    public int getResponseCode() {
        return HttpURLConnection.HTTP_OK;
    }

    /**
     * Returns the response message associated with this response.
     **/
    @Override
    public String getResponseMessage() {
        return "OK";
    }

    @Override
    public String[] getHeaderFieldNames() {
        return new String[] { "Content-type" };
    }

    /**
     * Returns the value for the specified header field. If no such field is defined, will return null.
     **/
    @Override
    public String getHeaderField(String fieldName) {
        if (fieldName.equalsIgnoreCase("Content-type")) {
            return "text/html; charset=us-ascii";
        }
        return null;
    }

    @Override
    public String[] getHeaderFields(String fieldName) {
        String value = getHeaderField(fieldName);
        return value == null ? new String[0] : new String[] { value };
    }

    @Override
    public String toString() {
        try {
            return "DefaultWebResponse [" + getText() + "]";
        } catch (IOException e) { // should never happen
            return "DefaultWebResponse [???]";
        }
    }
}

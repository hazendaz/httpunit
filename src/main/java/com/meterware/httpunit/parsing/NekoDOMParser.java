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
package com.meterware.httpunit.parsing;

import com.meterware.httpunit.dom.HTMLDocumentImpl;
import com.meterware.httpunit.scripting.ScriptingHandler;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.htmlunit.cyberneko.HTMLConfiguration;
import org.htmlunit.cyberneko.parsers.DOMParser;
import org.htmlunit.cyberneko.xerces.parsers.Constants;
import org.htmlunit.cyberneko.xerces.xni.XNIException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLConfigurationException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLDocumentFilter;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLErrorHandler;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLParseException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLParserConfiguration;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLDocument;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * The Class NekoDOMParser.
 */
class NekoDOMParser extends DOMParser implements ScriptHandler {

    /** Error reporting feature identifier. */
    private static final String REPORT_ERRORS = "http://cyberneko.org/html/features/report-errors";

    /** Augmentations feature identifier. */
    private static final String AUGMENTATIONS = "http://cyberneko.org/html/features/augmentations";

    /** Filters property identifier. */
    private static final String FILTERS = "http://cyberneko.org/html/properties/filters";

    /** Element case settings. possible values: "upper", "lower", "match" */
    private static final String TAG_NAME_CASE = "http://cyberneko.org/html/properties/names/elems";

    /** Attribute case settings. possible values: "upper", "lower", "no-change" */
    private static final String ATTRIBUTE_NAME_CASE = "http://cyberneko.org/html/properties/names/attrs";

    /** Current element node property ("dom/current-element-node"). */
    public static final String CURRENT_ELEMENT_NODE_PROPERTY = "dom/current-element-node";

    /** Defer node expansion feature ("dom/defer-node-expansion"). */
    public static final String DEFER_NODE_EXPANSION_FEATURE = "dom/defer-node-expansion";

    /** Document class name property ("dom/document-class-name"). */
    public static final String DOCUMENT_CLASS_NAME_PROPERTY = "dom/document-class-name";

    /** Feature id: defer node expansion. */
    protected static final String DEFER_NODE_EXPANSION = Constants.XERCES_FEATURE_PREFIX + DEFER_NODE_EXPANSION_FEATURE;

    /** Property id: document class name. */
    protected static final String DOCUMENT_CLASS_NAME = Constants.XERCES_PROPERTY_PREFIX + DOCUMENT_CLASS_NAME_PROPERTY;

    protected static final String CURRENT_ELEMENT_NODE = Constants.XERCES_PROPERTY_PREFIX
            + CURRENT_ELEMENT_NODE_PROPERTY;

    /** The document adapter. */
    private DocumentAdapter _documentAdapter;

    /** The parser configuration. */
    private XMLParserConfiguration fParserConfiguration;

    /**
     * construct a new NekoDomParser with the given adapter and url.
     *
     * @param adapter
     *            the adapter
     * @param url
     *            the url
     *
     * @return - the new parser patch [ 1211154 ] NekoDOMParser default to lowercase by Dan Allen patch [ 1176688 ]
     *         Allow configuration of neko parser properties by James Abley
     */
    static NekoDOMParser newParser(DocumentAdapter adapter, URL url) {
        final HTMLConfiguration configuration = new HTMLConfiguration();
        // note: Introduced in 1.9.9 nekohtml but doesn't apply against header but rather body and thus doesn't solve
        // issue with <noscript> needs.
        // configuration.setFeature(HTMLScanner.PARSE_NOSCRIPT_CONTENT, false);
        if (!HTMLParserFactory.getHTMLParserListeners().isEmpty() || HTMLParserFactory.isParserWarningsEnabled()) {
            configuration.setErrorHandler(new ErrorHandler(url));
            configuration.setFeature(REPORT_ERRORS, true);
        }
        configuration.setFeature(AUGMENTATIONS, true);
        final ScriptFilter javaScriptFilter = new ScriptFilter(configuration);
        configuration.setProperty(FILTERS, new XMLDocumentFilter[] { javaScriptFilter });
        if (HTMLParserFactory.isPreserveTagCase()) {
            configuration.setProperty(TAG_NAME_CASE, "match");
            configuration.setProperty(ATTRIBUTE_NAME_CASE, "no-change");
        } else {
            configuration.setProperty(TAG_NAME_CASE, "lower");
            configuration.setProperty(ATTRIBUTE_NAME_CASE, "lower");

            if (HTMLParserFactory.getForceUpperCase()) {
                configuration.setProperty(TAG_NAME_CASE, "upper");
                configuration.setProperty(ATTRIBUTE_NAME_CASE, "upper");
            }
            // this is the default as of patch [ 1211154 ] ... just for people who rely on patch [ 1176688 ]
            if (HTMLParserFactory.getForceLowerCase()) {
                configuration.setProperty(TAG_NAME_CASE, "lower");
                configuration.setProperty(ATTRIBUTE_NAME_CASE, "lower");
            }
        }

        try {
            final NekoDOMParser domParser = new NekoDOMParser(configuration, adapter);
            domParser.setFeature(DEFER_NODE_EXPANSION, false);
            if (HTMLParserFactory.isReturnHTMLDocument())
                domParser.setProperty(DOCUMENT_CLASS_NAME, HTMLDocumentImpl.class.getName());
            javaScriptFilter.setScriptHandler(domParser);
            return domParser;
        } catch (SAXNotRecognizedException | SAXNotSupportedException e) {
            throw new RuntimeException(e.toString());
        }

    }

    /**
     * Gets the current element.
     *
     * @return the current element
     */
    private Element getCurrentElement() {
        try {
            return (Element) getProperty(CURRENT_ELEMENT_NODE);
        } catch (SAXNotRecognizedException e) {
            throw new RuntimeException(CURRENT_ELEMENT_NODE + " property not recognized");
        } catch (SAXNotSupportedException e) {
            e.printStackTrace();
            throw new RuntimeException(CURRENT_ELEMENT_NODE + " property not supported");
        }
    }

    /**
     * Query the value of a property. Return the current value of a property in a SAX2 parser. The parser might not
     * recognize the property.
     *
     * @param propertyId
     *            The unique identifier (URI) of the property being set.
     *
     * @return The current value of the property.
     *
     * @exception org.xml.sax.SAXNotRecognizedException
     *                If the requested property is not known.
     * @exception SAXNotSupportedException
     *                If the requested property is known but not supported.
     */
    public Object getProperty(String propertyId) throws SAXNotRecognizedException, SAXNotSupportedException {

        if (propertyId.equals(CURRENT_ELEMENT_NODE)) {
            return (fCurrentNode != null && fCurrentNode.getNodeType() == Node.ELEMENT_NODE) ? fCurrentNode : null;
        }

        try {
            return fParserConfiguration.getProperty(propertyId);
        } catch (final XMLConfigurationException e) {
            final String message = e.getMessage();
            if (e.getType() == XMLConfigurationException.NOT_RECOGNIZED) {
                throw new SAXNotRecognizedException(message);
            }
            throw new SAXNotSupportedException(message);
        }
    }

    /**
     * Instantiates a new neko DOM parser.
     *
     * @param configuration
     *            the configuration
     * @param adapter
     *            the adapter
     */
    NekoDOMParser(HTMLConfiguration configuration, DocumentAdapter adapter) {
        super(null);
        _documentAdapter = adapter;
        if (HTMLParserFactory.isReturnHTMLDocument()) {
            fParserConfiguration = configuration;
        }
    }

    @Override
    public String getIncludedScript(String srcAttribute) {
        try {
            return _documentAdapter.getIncludedScript(srcAttribute);
        } catch (IOException e) {
            throw new ScriptException(e);
        }
    }

    @Override
    public boolean supportsScriptLanguage(String language) {
        return getScriptingHandler().supportsScriptLanguage(language);
    }

    @Override
    public String runScript(final String language, final String scriptText) {
        getScriptingHandler().clearCaches();
        return getScriptingHandler().runScript(language, scriptText);
    }

    /**
     * Gets the scripting handler.
     *
     * @return the scripting handler
     */
    private ScriptingHandler getScriptingHandler() {
        _documentAdapter.setDocument((HTMLDocument) getCurrentElement().getOwnerDocument());
        return _documentAdapter.getScriptingHandler();
    }

    /**
     * The Class ScriptException.
     */
    static class ScriptException extends RuntimeException {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The cause. */
        private IOException _cause;

        /**
         * Instantiates a new script exception.
         *
         * @param cause
         *            the cause
         */
        public ScriptException(IOException cause) {
            _cause = cause;
        }

        /**
         * Gets the exception.
         *
         * @return the exception
         */
        public IOException getException() {
            return _cause;
        }
    }
}

class ErrorHandler implements XMLErrorHandler {

    private URL _url;

    ErrorHandler(URL url) {
        _url = url;
    }

    @Override
    public void warning(String domain, String key, XMLParseException warningException) throws XNIException {
        if (HTMLParserFactory.isParserWarningsEnabled()) {
            System.out.println("At line " + warningException.getLineNumber() + ", column "
                    + warningException.getColumnNumber() + ": " + warningException.getMessage());
        }

        List<HTMLParserListener> listeners = HTMLParserFactory.getHTMLParserListeners();
        for (HTMLParserListener listener : listeners) {
            listener.warning(_url, warningException.getMessage(), warningException.getLineNumber(),
                    warningException.getColumnNumber());
        }
    }

    @Override
    public void error(String domain, String key, XMLParseException errorException) throws XNIException {
        List<HTMLParserListener> listeners = HTMLParserFactory.getHTMLParserListeners();
        for (HTMLParserListener listener : listeners) {
            listener.error(_url, errorException.getMessage(), errorException.getLineNumber(),
                    errorException.getColumnNumber());
        }
    }

    @Override
    public void fatalError(String domain, String key, XMLParseException fatalError) throws XNIException {
        error(domain, key, fatalError);
        throw fatalError;
    }
}

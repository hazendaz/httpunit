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
package com.meterware.httpunit.parsing;

import com.meterware.httpunit.scripting.ScriptingHandler;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.htmlunit.cyberneko.HTMLConfiguration;
import org.htmlunit.cyberneko.parsers.DOMParser;
import org.htmlunit.cyberneko.xerces.xni.XNIException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLDocumentFilter;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLErrorHandler;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLParseException;
import org.w3c.dom.Element;

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

    /** The document adapter. */
    private DocumentAdapter _documentAdapter;

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
        // Create the parser with plain xerces DocumentImpl
        // With neko-htmlunit 3.x+, we don't use HTMLDocument due to API incompatibilities
        final NekoDOMParser domParser = new NekoDOMParser(org.htmlunit.cyberneko.xerces.dom.DocumentImpl.class,
                adapter);

        // Get the configuration from the parser
        final HTMLConfiguration configuration = (HTMLConfiguration) domParser.getXMLParserConfiguration();

        // note: Introduced in 1.9.9 nekohtml but doesn't apply against header but rather body and thus doesn't
        // solve
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

        javaScriptFilter.setScriptHandler(domParser);
        return domParser;

    }

    /**
     * Gets the current element.
     *
     * @return the current element
     */
    private Element getCurrentElement() {
        // In neko-htmlunit 3.0+, we can directly access fCurrentNode since we extend AbstractDOMParser
        return (fCurrentNode != null && fCurrentNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
                ? (Element) fCurrentNode
                : null;
    }

    /**
     * Instantiates a new neko DOM parser.
     *
     * @param documentClass
     *            the document class to use
     * @param adapter
     *            the adapter
     */
    NekoDOMParser(Class documentClass, DocumentAdapter adapter) {
        super(documentClass);
        _documentAdapter = adapter;
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
        _documentAdapter.setDocument(getCurrentElement().getOwnerDocument());
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

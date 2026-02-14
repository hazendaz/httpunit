/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.parsing;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

import org.w3c.dom.Document;
import org.w3c.dom.html.HTMLDocument;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The Class NekoHTMLParser.
 */
class NekoHTMLParser implements HTMLParser {

    /**
     * parse the given URL with the given pageText using the given document adapter
     *
     * @param pageURL
     * @param pageText
     * @param adapter
     */
    @Override
    public void parse(URL pageURL, String pageText, DocumentAdapter adapter) throws IOException, SAXException {
        try {
            NekoDOMParser parser = NekoDOMParser.newParser(adapter, pageURL);
            parser.parse(new InputSource(new StringReader(pageText)));
            Document doc = parser.getDocument();
            adapter.setDocument((HTMLDocument) doc);
        } catch (NekoDOMParser.ScriptException e) {
            throw e.getException();
        }
    }

    @Override
    public String getCleanedText(String string) {
        return string == null ? "" : string.replace(NBSP, ' ');
    }

    @Override
    public boolean supportsPreserveTagCase() {
        return false;
    }

    @Override
    public boolean supportsForceTagCase() {
        return false;
    }

    @Override
    public boolean supportsReturnHTMLDocument() {
        return true;
    }

    @Override
    public boolean supportsParserWarnings() {
        return true;
    }

    /** The Constant NBSP. */
    private static final char NBSP = (char) 160; // non-breaking space, defined by nekoHTML
}

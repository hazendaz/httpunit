/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.parsing;

import com.meterware.httpunit.dom.HTMLDocumentImpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLDocument;
import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

/**
 * The Class JTidyHTMLParser.
 */
class JTidyHTMLParser implements HTMLParser {

    @Override
    public void parse(URL pageURL, String pageText, DocumentAdapter adapter) throws IOException, SAXException {
        Document jtidyDocument = getParser(pageURL)
                .parseDOM(new ByteArrayInputStream(pageText.getBytes(StandardCharsets.UTF_8)), null);
        HTMLDocument htmlDocument = new HTMLDocumentImpl();
        NodeList nl = jtidyDocument.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node importedNode = nl.item(i);
            if (importedNode.getNodeType() != Node.DOCUMENT_TYPE_NODE) {
                htmlDocument.appendChild(htmlDocument.importNode(importedNode, true));
            }
        }
        adapter.setDocument(htmlDocument);
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
    private static final char NBSP = (char) 160; // non-breaking space, defined by JTidy

    /**
     * get the parser of the given url.
     *
     * @param url
     *            the url
     *
     * @return the parser
     */
    private static Tidy getParser(URL url) {
        Tidy tidy = new Tidy();
        // BR 2880636 httpunit 1.7 does not work with latest Tidy release r918
        // tidy.setCharEncoding( org.w3c.tidy.Configuration.UTF8 );
        tidy.setInputEncoding("UTF8");
        tidy.setQuiet(true);
        tidy.setShowWarnings(HTMLParserFactory.isParserWarningsEnabled());
        if (!HTMLParserFactory.getHTMLParserListeners().isEmpty()) {
            tidy.setErrout(new JTidyPrintWriter(url));
        }
        return tidy;
    }

}

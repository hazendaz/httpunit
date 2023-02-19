/*
 * MIT License
 *
 * Copyright 2011-2023 Russell Gold
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
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
class JTidyHTMLParser implements HTMLParser {

    public void parse(URL pageURL, String pageText, DocumentAdapter adapter) throws IOException, SAXException {
        Document jtidyDocument = getParser(pageURL)
                .parseDOM(new ByteArrayInputStream(pageText.getBytes(StandardCharsets.UTF_8)), null);
        HTMLDocument htmlDocument = new HTMLDocumentImpl();
        NodeList nl = jtidyDocument.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node importedNode = nl.item(i);
            if (importedNode.getNodeType() != Node.DOCUMENT_TYPE_NODE)
                htmlDocument.appendChild(htmlDocument.importNode(importedNode, true));
        }
        adapter.setDocument(htmlDocument);
    }

    public String getCleanedText(String string) {
        return (string == null) ? "" : string.replace(NBSP, ' ');
    }

    public boolean supportsPreserveTagCase() {
        return false;
    }

    public boolean supportsForceTagCase() {
        return false;
    }

    public boolean supportsReturnHTMLDocument() {
        return true;
    }

    public boolean supportsParserWarnings() {
        return true;
    }

    final private static char NBSP = (char) 160; // non-breaking space, defined by JTidy

    /**
     * get the parser of the given url
     *
     * @param url
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

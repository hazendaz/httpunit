/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.parsing;

import java.io.IOException;
import java.net.URL;

import org.xml.sax.SAXException;

/**
 * A front end to a DOM parser that can handle HTML.
 **/
public interface HTMLParser {

    /**
     * Parses the specified text string as a Document, registering it in the HTMLPage. Any error reporting will be
     * annotated with the specified URL.
     *
     * @param baseURL
     *            the base URL
     * @param pageText
     *            the page text
     * @param adapter
     *            the adapter
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws SAXException
     *             the SAX exception
     */
    void parse(URL baseURL, String pageText, DocumentAdapter adapter) throws IOException, SAXException;

    /**
     * Removes any string artifacts placed in the text by the parser. For example, a parser may choose to encode an HTML
     * entity as a special character. This method should convert that character to normal text.
     *
     * @param string
     *            the string
     *
     * @return the cleaned text
     */
    String getCleanedText(String string);

    /**
     * Returns true if this parser supports preservation of the case of tag and attribute names.
     *
     * @return true, if successful
     */
    boolean supportsPreserveTagCase();

    /**
     * Returns true if this parser supports forcing the upper/lower case of tag and attribute names.
     *
     * @return true, if successful
     */
    boolean supportsForceTagCase();

    /**
     * Returns true if this parser can return an HTMLDocument object.
     *
     * @return true, if successful
     */
    boolean supportsReturnHTMLDocument();

    /**
     * Returns true if this parser can display parser warnings.
     *
     * @return true, if successful
     */
    boolean supportsParserWarnings();
}

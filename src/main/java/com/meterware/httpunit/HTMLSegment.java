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
package com.meterware.httpunit;

import org.xml.sax.SAXException;

/**
 * Represents the parse tree for a segment of HTML.
 **/
public interface HTMLSegment {

    /**
     * Returns the HTMLElement found in this segment with the specified ID.
     *
     * @param id
     *            the id
     *
     * @return the element with ID
     *
     * @exception SAXException
     *                thrown if there is an error parsing the segment.
     */
    HTMLElement getElementWithID(String id) throws SAXException;

    /**
     * Returns the HTMLElements found in this segment with the specified name.
     *
     * @param name
     *            the name
     *
     * @return the elements with name
     *
     * @throws SAXException
     *             the SAX exception
     */
    HTMLElement[] getElementsWithName(String name) throws SAXException;

    /**
     * Returns the HTMLElements found with the specified attribute value.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     *
     * @return the elements with attribute
     *
     * @throws SAXException
     *             the SAX exception
     */
    HTMLElement[] getElementsWithAttribute(String name, String value) throws SAXException;

    /**
     * Returns a list of HTML element names contained in this HTML section.
     *
     * @return the element names
     *
     * @throws SAXException
     *             the SAX exception
     */
    String[] getElementNames() throws SAXException;

    /**
     * Returns the forms found in this HTML segment in the order in which they appear.
     *
     * @return the forms
     *
     * @exception SAXException
     *                thrown if there is an error parsing the segment.
     */
    WebForm[] getForms() throws SAXException;

    /**
     * Returns the form found in this HTML segment with the specified ID.
     *
     * @param ID
     *            the id
     *
     * @return the form with ID
     *
     * @exception SAXException
     *                thrown if there is an error parsing the segment.
     */
    WebForm getFormWithID(String ID) throws SAXException;

    /**
     * Returns the form found in this HTML segment with the specified name.
     *
     * @param name
     *            the name
     *
     * @return the form with name
     *
     * @exception SAXException
     *                thrown if there is an error parsing the segment.
     */
    WebForm getFormWithName(String name) throws SAXException;

    /**
     * Returns the first form found in the page matching the specified criteria.
     *
     * @param predicate
     *            the predicate
     * @param value
     *            the value
     *
     * @return the first matching form
     *
     * @exception SAXException
     *                thrown if there is an error parsing the response.
     */
    WebForm getFirstMatchingForm(HTMLElementPredicate predicate, Object value) throws SAXException;

    /**
     * Returns all forms found in the page matching the specified criteria.
     *
     * @param predicate
     *            the predicate
     * @param criteria
     *            the criteria
     *
     * @return the matching forms
     *
     * @exception SAXException
     *                thrown if there is an error parsing the response.
     */
    WebForm[] getMatchingForms(HTMLElementPredicate predicate, Object criteria) throws SAXException;

    /**
     * Returns the links found in this HTML segment in the order in which they appear.
     *
     * @return the links
     *
     * @exception SAXException
     *                thrown if there is an error parsing the segment.
     */
    WebLink[] getLinks() throws SAXException;

    /**
     * Returns the first link which contains the specified text.
     *
     * @param text
     *            the text
     *
     * @return the link with
     *
     * @exception SAXException
     *                thrown if there is an error parsing the segment.
     */
    WebLink getLinkWith(String text) throws SAXException;

    /**
     * Returns the first link which contains an image with the specified text as its 'alt' attribute.
     *
     * @param text
     *            the text
     *
     * @return the link with image text
     *
     * @exception SAXException
     *                thrown if there is an error parsing the segment.
     */
    WebLink getLinkWithImageText(String text) throws SAXException;

    /**
     * Returns the first link found in the page matching the specified criteria.
     *
     * @param predicate
     *            the predicate
     * @param value
     *            the value
     *
     * @return the first matching link
     *
     * @exception SAXException
     *                thrown if there is an error parsing the response.
     */
    WebLink getFirstMatchingLink(HTMLElementPredicate predicate, Object value) throws SAXException;

    /**
     * Returns all links found in the page matching the specified criteria.
     *
     * @param predicate
     *            the predicate
     * @param criteria
     *            the criteria
     *
     * @return the matching links
     *
     * @exception SAXException
     *                thrown if there is an error parsing the response.
     */
    WebLink[] getMatchingLinks(HTMLElementPredicate predicate, Object criteria) throws SAXException;

    /**
     * Returns the images found in the page in the order in which they appear.
     *
     * @return the images
     *
     * @exception SAXException
     *                thrown if there is an error parsing the segment.
     */
    WebImage[] getImages() throws SAXException;

    /**
     * Returns the image found in the page with the specified name.
     *
     * @param name
     *            the name
     *
     * @return the image with name
     *
     * @exception SAXException
     *                thrown if there is an error parsing the segment.
     */
    WebImage getImageWithName(String name) throws SAXException;

    /**
     * Returns the first image found in the page with the specified src attribute.
     *
     * @param source
     *            the source
     *
     * @return the image with source
     *
     * @exception SAXException
     *                thrown if there is an error parsing the segment.
     */
    WebImage getImageWithSource(String source) throws SAXException;

    /**
     * Returns the first image found in the page with the specified alt attribute.
     *
     * @param source
     *            the source
     *
     * @return the image with alt text
     *
     * @exception SAXException
     *                thrown if there is an error parsing the segment.
     */
    WebImage getImageWithAltText(String source) throws SAXException;

    /**
     * Returns the applets found in the page in the order in which they appear.
     *
     * @return the applets
     *
     * @exception SAXException
     *                thrown if there is an error parsing the segment.
     */
    WebApplet[] getApplets() throws SAXException;

    /**
     * Returns the top-level block elements found in the page in the order in which they appear.
     *
     * @return the text blocks
     *
     * @exception SAXException
     *                thrown if there is an error parsing the segment.
     */
    TextBlock[] getTextBlocks() throws SAXException;

    /**
     * Returns the top-level tables found in this HTML segment in the order in which they appear.
     *
     * @return the tables
     *
     * @exception SAXException
     *                thrown if there is an error parsing the segment.
     */
    WebTable[] getTables() throws SAXException;

    /**
     * Returns the first table in the response which matches the specified predicate and value. Will recurse into any
     * nested tables, as needed.
     *
     * @param predicate
     *            the predicate
     * @param criteria
     *            the criteria
     *
     * @return the selected table, or null if none is found
     *
     * @throws SAXException
     *             the SAX exception
     */
    WebTable getFirstMatchingTable(HTMLElementPredicate predicate, Object criteria) throws SAXException;

    /**
     * Returns all tables found in the page matching the specified criteria.
     *
     * @param predicate
     *            the predicate
     * @param criteria
     *            the criteria
     *
     * @return the matching tables
     *
     * @exception SAXException
     *                thrown if there is an error parsing the response.
     */
    WebTable[] getMatchingTables(HTMLElementPredicate predicate, Object criteria) throws SAXException;

    /**
     * Returns the first table in this HTML segment which has the specified text as the full text of its first non-blank
     * row and non-blank column. Will recurse into any nested tables, as needed.
     *
     * @param text
     *            the text
     *
     * @return the selected table, or null if none is found
     *
     * @exception SAXException
     *                thrown if there is an error parsing the segment.
     */
    WebTable getTableStartingWith(final String text) throws SAXException;

    /**
     * Returns the first table in this HTML segment which has the specified text as a prefix of the text in its first
     * non-blank row and non-blank column. Will recurse into any nested tables, as needed.
     *
     * @param text
     *            the text
     *
     * @return the selected table, or null if none is found
     *
     * @exception SAXException
     *                thrown if there is an error parsing the segment.
     */
    WebTable getTableStartingWithPrefix(String text) throws SAXException;

    /**
     * Returns the first table in this HTML segment which has the specified text as its summary attribute. Will recurse
     * into any nested tables, as needed.
     *
     * @param summary
     *            the summary
     *
     * @return the selected table, or null if none is found
     *
     * @exception SAXException
     *                thrown if there is an error parsing the segment.
     */
    WebTable getTableWithSummary(String summary) throws SAXException;

    /**
     * Returns the first table in this HTML segment which has the specified text as its ID attribute. Will recurse into
     * any nested tables, as needed.
     *
     * @param ID
     *            the id
     *
     * @return the selected table, or null if none is found
     *
     * @exception SAXException
     *                thrown if there is an error parsing the segment.
     */
    WebTable getTableWithID(final String ID) throws SAXException;

}

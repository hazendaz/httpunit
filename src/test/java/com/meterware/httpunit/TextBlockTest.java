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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.BufferedReader;
import java.io.StringReader;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.migrationsupport.rules.ExternalResourceSupport;

/**
 * The Class TextBlockTest.
 */
@ExtendWith(ExternalResourceSupport.class)
class TextBlockTest extends HttpUnitTest {

    /**
     * Paragraph detection.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void paragraphDetection() throws Exception {
        defineWebPage("SimplePage",
                "<p>This has no forms or links since we don't care " + "about them</p>"
                        + "<p class='comment'>But it does have three paragraphs</p>\n"
                        + "<p>Which is what we want to find</p>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/SimplePage.html");
        assertEquals(3, response.getTextBlocks().length, "Number of paragraphs");
        assertEquals("This has no forms or links since we don't care about them", response.getTextBlocks()[0].getText(),
                "First paragraph");
        BlockElement comment = response.getFirstMatchingTextBlock(TextBlock.MATCH_CLASS, "comment");
        assertNotNull(comment, "Did not find a comment paragraph");
        assertEquals("But it does have three paragraphs", comment.getText(), "Comment paragraph");
    }

    /**
     * Text conversion.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void textConversion() throws Exception {
        defineWebPage("SimplePage", "<p>Here is a line<br>followed by another</p>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/SimplePage.html");
        BufferedReader br = new BufferedReader(new StringReader(response.getTextBlocks()[0].getText()));
        assertEquals("Here is a line", br.readLine(), "First line");
        assertEquals("followed by another", br.readLine(), "Second line");
        br.readLine();
    }

    /**
     * Header detection.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void headerDetection() throws Exception {
        defineWebPage("SimplePage",
                "<h1>Here is a section</h1>\n" + "with some text" + "<h2>A subsection</h2>" + "<p>Some more text</p>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/SimplePage.html");
        TextBlock header1 = response.getFirstMatchingTextBlock(TextBlock.MATCH_TAG, "H1");
        assertNotNull(header1, "Did not find the H1 header");
        assertEquals("Here is a section", header1.getText(), "H1 header");
        TextBlock header2 = response.getFirstMatchingTextBlock(TextBlock.MATCH_TAG, "h2");
        assertNotNull(header2, "Did not find the h2 header");
        assertEquals("A subsection", header2.getText(), "H2 header");
        assertEquals("with some text", response.getNextTextBlock(header1).getText(), "Text under header 1");
    }

    /**
     * Embedded links.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void embeddedLinks() throws Exception {
        defineWebPage("SimplePage", "<h1>Here is a section</h1>\n"
                + "<p>with a <a id='httpunit' href='http://httpunit.org'>link to the home page</a></p>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/SimplePage.html");
        BlockElement paragraph = response.getTextBlocks()[1];
        assertNotNull(paragraph.getLinks(), "Did not retrieve any links");
        assertNotNull(paragraph.getLinkWithID("httpunit"), "Did not find the httpunit link");
        assertNull(response.getTextBlocks()[0].getLinkWithID("httpunit"),
                "Should not have found the httpunit link in the header");
        assertNotNull(paragraph.getFirstMatchingLink(WebLink.MATCH_CONTAINED_TEXT, "home page"),
                "Did not find the home page link");
        assertEquals("http://httpunit.org", paragraph.getLinkWithID("httpunit").getRequest().getURL().toExternalForm(),
                "embedded link url");
    }

    /**
     * Embedded lists.
     *
     * @throws Exception
     *             the exception
     */
    // TODO JWL 7/6/2021 Breaks with nekohtml > 1.9.6.2
    @Disabled
    @Test
    void embeddedLists() throws Exception {
        defineWebPage("SimplePage",
                "<h1>Here is a section</h1>\n" + "<p id='ordered'><ol><li>One<li>Two<li>Three</ol></p>"
                        + "<p id='unordered'><ul><li>Red<li>Green<li>Blue</ul></p>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/SimplePage.html");
        TextBlock paragraph1 = (TextBlock) response.getElementWithID("ordered");
        WebList[] lists = paragraph1.getLists();
        assertEquals(1, lists.length, "Number of lists found");
        WebList orderedList = lists[0];
        assertEquals(WebList.ORDERED_LIST, orderedList.getListType(), "ordered list type");
        assertEquals(3, orderedList.getItems().length, "ordered list size");
        assertEquals("Two", orderedList.getItems()[1].getText(), "Second ordered list item");

        TextBlock paragraph2 = (TextBlock) response.getElementWithID("unordered");
        lists = paragraph2.getLists();
        assertEquals(1, lists.length, "Number of lists found");
        WebList unorderedList = lists[0];
        assertEquals(WebList.BULLET_LIST, unorderedList.getListType(), "bullet list type");
        assertEquals(3, unorderedList.getItems().length, "bullet list size");
        assertEquals("Red", unorderedList.getItems()[0].getText(), "First bullet list item");
    }

    /**
     * Ntest formatting detection.
     *
     * @throws Exception
     *             the exception
     */
    // TODO JWL 2/19/2023 Test was not annotated and does not work, annotated this and add ignores for now.
    @Disabled
    @Test
    void ntestFormattingDetection() throws Exception {
        String expectedText = "Here is some bold text and some bold italic text";
        defineWebPage("FormattedPage", "<p>Here is some <b>bold</b> text and some <b><i>bold italic</i></b> text</p>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/FormattedPage.html");
        TextBlock paragraph = response.getTextBlocks()[0];
        assertMatchingSet("Attributes for word 'bold'", new String[] { "b" },
                paragraph.getFormats(expectedText.indexOf("bold")));
    }

}

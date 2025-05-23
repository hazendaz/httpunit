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
package com.meterware.httpunit.javascript;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.meterware.httpunit.HttpUnitTest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.migrationsupport.rules.ExternalResourceSupport;

/**
 * More complex tests of frame functionality.
 *
 * @author <a href="andrew.bickerton@hp.com">Andrew Bickerton</a>
 * @author <a href="russgold@httpunit.org">Russell Gold</a>
 */
@ExtendWith(ExternalResourceSupport.class)
class FrameScriptingTest extends HttpUnitTest {

    private WebConversation _wc;

    @BeforeEach
    void setUp() throws Exception {
        _wc = new WebConversation();

        defineWebPage("Simple", "This is a trivial page.");
        defineResource("image.gif", new byte[] { 0, 1, 0, 1 }, "image/gif");
        defineWebPage("Menu/Page/Target", "Target page");

        StringBuilder buff = new StringBuilder();
        buff.append("This frame contains JavaScript that re-writes the \"red\" frame if it exists.\n");
        buff.append("<script language=\"JavaScript\">\n");
        buff.append("if (window.parent.red) {\n");
        buff.append("  window.parent.red.document.open(\"text/html\")\n");
        buff.append("  with (window.parent.red.document) {\n");
        final String[] lines = getMenuHtmlLines(getHostPath() + "/Menu/Page/Main.html", "Target.html");
        for (String line : lines) {
            buff.append("    write('").append(line).append("\\n").append("');\n");
        }
        buff.append("    close();\n");
        buff.append("  }\n");
        buff.append("}\n");
        buff.append("</script>\n");

        defineWebPage("frameRewriter", buff.toString());
    }

    /**
     * Creates and returns the lines of HTML to represent a menu page with a <code>&lt;base ...&gt;</code> tag and one
     * link. Used where the formulated HTML is to be embedded in JavaScript.
     *
     * @param baseUrlString
     *            URL string to be used in the href of the 'base' tag.
     * @param linkUrlString
     *            URL string to be used in the href of the link.
     *
     * @return String array representing the lines of HTML.
     */
    private String[] getMenuHtmlLines(String baseUrlString, String linkUrlString) {
        return new String[] { "<html><head><title>Menu</title>",
                "<base href=\"" + baseUrlString + "\" target=\"blue\"></base>", "</head>", "<body>",
                "This is the menu.<br>", "Link to <a href=\"" + linkUrlString + "\">target page</a>.",
                "</body></html>" };
    }

    /**
     * Creates and returns HTML to represent a menu page with a <code>&lt;base ...&gt;</code> tag and one link.
     *
     * @param baseUrlString
     *            URL string to be used in the href of the 'base' tag.
     * @param linkUrlString
     *            URL string to be used in the href of the link.
     *
     * @return String representing the HTML.
     */
    private String getMenuHtml(final String baseUrlString, final String linkUrlString) {
        final String[] menuLines = getMenuHtmlLines(baseUrlString, linkUrlString);
        StringBuilder pageBuffer = new StringBuilder();
        for (String line : menuLines) {
            pageBuffer.append(line).append("\n");
        }
        return pageBuffer.toString();
    }

    /**
     * (Re)defines the web page 'Frames.html', which is a frameset containing 'red' and 'blue' frames.
     *
     * @param redSrc
     *            The source URL string for the 'red' frame.
     * @param blueSrc
     *            The source URL string for the 'blue' frame.
     */
    private void redefineFrames(String redSrc, String blueSrc) {
        defineResource("Frames.html",
                "<HTML><HEAD><TITLE>Frameset</TITLE></HEAD>" + "<FRAMESET cols=\"20%,80%\">" + "    <FRAME src=\""
                        + redSrc + "\" name=\"red\">" + "    <FRAME src=\"" + blueSrc + "\" name=\"blue\">"
                        + "</FRAMESET></HTML>");
    }

    /**
     * Test that an image can be used as the source for a frame within a frameset.
     */
    @Test
    void imageInFrame() throws Exception {
        redefineFrames("/image.gif", "Simple.html");
        _wc.getResponse(getHostPath() + "/Frames.html");
        WebResponse redFrame = _wc.getFrameContents("red");
        assertEquals("image/gif", redFrame.getContentType(), "Red frame content-type");
    }

    /**
     * Test that a link within a frame that contains a <code>&lt;base ...&gt;</code> element is handled correctly.
     */
    @Test
    void frameContainingBaseElement() throws Exception {
        final String TARGET_TITLE = "Somewhere/Else/Target";
        defineWebPage(TARGET_TITLE, "This is the target page.");
        defineWebPage("Main", "This is a simple page.");
        defineResource("BaseRelLinker.html", getMenuHtml(getHostPath() + "/Somewhere/Near/", "../Else/Target.html"));
        redefineFrames("BaseRelLinker.html", "Main.html");

        _wc.getResponse(getHostPath() + "/Frames.html");
        WebLink link = _wc.getFrameContents("red").getLinks()[0];
        link.click();
        assertEquals(TARGET_TITLE, _wc.getFrameContents("blue").getTitle(), "Content of blue frame after link click");
    }

    /**
     * Test correct handling of a link within a frame that has been re-written by JavaScript, and which contains a
     * <code>&lt;base ...&gt;</code> element.
     */
    @Test
    void frameRewrittenToUseBaseElement() throws Exception {
        redefineFrames("/Simple.html", "frameRewriter.html");
        _wc.getResponse(getHostPath() + "/Frames.html");
        WebLink link = _wc.getFrameContents("red").getLinks()[0];
        link.click();
        assertEquals("Menu/Page/Target", _wc.getFrameContents("blue").getTitle(),
                "Content of blue frame after clicking menu link");
    }

    /**
     * Test correct handling of a link within a frame that has been re-written by JavaScript over an image, and which
     * contains a <code>&lt;base ...&gt;</code> element.
     */
    @Test
    void imageFrameRewrittenToUseBaseElement() throws Exception {
        redefineFrames("/image.gif", "frameRewriter.html");
        _wc.getResponse(getHostPath() + "/Frames.html");
        WebLink link = _wc.getFrameContents("red").getLinks()[0];
        link.click();
        assertEquals("Menu/Page/Target", _wc.getFrameContents("blue").getTitle(),
                "Content of blue frame after clicking menu link");
    }

    /**
     * Verifies that a javascript URL which triggers refresh to the parent of a frame resolves with something sensible.
     */
    @Test
    void javaScriptURLToParentFrame() throws Exception {
        defineResource("Frames.html", "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" + "<FRAMESET cols=\"20%,80%\">"
                + "    <FRAME src='Linker.html' name='red'>" + "    <FRAME src=Form name=blue>" + "</FRAMESET></HTML>");
        defineWebPage("Linker",
                "This is a trivial page with <a href='javascript:window.parent.location.replace( \"Target\" );'>one link</a>");
        defineResource("Form", "This is a page with nothing we care about");
        defineResource("Target", "You made it!", "text/plain");

        _wc.getResponse(getHostPath() + "/Frames.html");
        WebResponse result = _wc.getFrameContents("red").getLinkWith("one link").click();
        assertEquals("You made it!", result.getText(), "Result of click");
    }

    /**
     * Verifies that when JavaScript overwrites an empty frame, other empty frames stay empty.
     */
    @Test
    void javaScriptOverwritingBlankFrame() throws Exception {
        defineResource("Frames.html",
                "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" + "<FRAMESET>" + "    <FRAME src='Writer.html' name='red'>"
                        + "    <FRAME name=green>" + "    <FRAME name=blue>" + "</FRAMESET></HTML>");
        defineWebPage("Writer",
                "This page overwrites an empty frame. <script language=\"JavaScript\">"
                        + "  window.parent.green.document.open(\"text/html\")\n"
                        + "  window.parent.green.document.write(\"Generated <a href=\\\"#\\\">link</a>\")\n"
                        + "  window.parent.green.document.close()\n" + "</script>");

        _wc.getResponse(getHostPath() + "/Frames.html");
        assertEquals(1, _wc.getFrameContents("green").getLinks().length, "Links in green frame");
        assertEquals(0, _wc.getFrameContents("blue").getLinks().length, "Links in blue frame");
    }

    /**
     * Verifies that the onload event of a frameset can access subframes.
     */
    @Test
    void frameOnLoadEvent() throws Exception {
        defineWebPage("OneForm", "<form name='form'><input name=text value='nothing special'></form>");
        defineResource("Frames.html", "<html><frameset onload='alert( green.document.form.text.value );'>"
                + "    <frame src='OneForm.html' name='green'>" + "    <frame name=blue>" + "</frameset></htmlL>");

        _wc.getResponse(getHostPath() + "/Frames.html");
        assertEquals("nothing special", _wc.popNextAlert(), "On load message");
    }

    /**
     * Verifies that the onload event of a frameset runs even if there is a noframes tag present that contains a body
     * tag.
     */
    @Test
    void frameOnLoadEventWithNoFrames() throws Exception {
        defineWebPage("OneForm", "<form name='form'><input name=text value='nothing special'></form>");
        defineResource("Frames.html",
                "<html><frameset onload='alert( green.document.form.text.value );'>"
                        + "    <frame src='OneForm.html' name='green'>" + "    <frame name=blue>"
                        + "    <noframes><body></body></noframes>" + "</frameset></htmlL>");

        _wc.getResponse(getHostPath() + "/Frames.html");
        assertEquals("nothing special", _wc.popNextAlert(), "On load message");
    }

    /**
     * Verifies that IFrames can be found using their id.
     */
    @Test
    void iFrameAccessById() throws Exception {
        assertDoesNotThrow(() -> {
            defineWebPage("Frames", "<iframe id='frame1'></iframe>" + "<script>"
                    + "var frame = document.getElementById('frame1');" + "</script>");

            _wc.getResponse(getHostPath() + "/Frames.html");
        });
    }
}

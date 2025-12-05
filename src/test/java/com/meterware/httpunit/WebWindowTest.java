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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.migrationsupport.rules.ExternalResourceSupport;

@ExtendWith(ExternalResourceSupport.class)
class WebWindowTest extends HttpUnitTest {

    /**
     * Verifies that clicking on a link that specifies the _blank target creates a new window, populated with the
     * contents of the referenced page.
     *
     * @throws Exception
     *             on any unexpected problem.
     */
    @Test
    void testNewTarget() throws Exception {
        defineResource("goHere", "You made it!");
        defineWebPage("start", "<a href='goHere' id='go' target='_blank'>here</a>");

        WebClient wc = new WebConversation();
        assertEquals(1, wc.getOpenWindows().length, "Number of initial windows");
        WebWindow main = wc.getMainWindow();
        WebResponse initialPage = main.getResponse(getHostPath() + "/start.html");
        initialPage.getLinkWithID("go").click();
        assertEquals(2, wc.getOpenWindows().length, "Number of windows after following link");
        assertEquals(initialPage, main.getCurrentPage(), "Main page in original window");
        WebWindow other = wc.getOpenWindows()[1];
        assertEquals("You made it!", other.getCurrentPage().getText(), "New window contents");

        main.close();
        assertTrue(main.isClosed(), "Original main window is not closed");
        assertFalse(other.isClosed(), "New window has been closed");

        assertEquals(1, wc.getOpenWindows().length, "Num open windows");
        assertEquals(other, wc.getMainWindow(), "Main window");
    }

    @Test
    void testUnknownTarget() throws Exception {
        defineResource("goThere", "You came back!");
        defineResource("goHere", "You made it!");
        defineWebPage("start", "<a href='goHere' id='go' target='somewhere'>here</a>"
                + "<a href='goThere' id='return' target='somewhere'>there</a>");

        WebClient wc = new WebConversation();
        WebResponse initialPage = wc.getResponse(getHostPath() + "/start.html");
        initialPage.getLinkWithID("go").click();
        assertEquals(2, wc.getOpenWindows().length, "Number of windows after following link");
        WebWindow other = wc.getOpenWindows()[1];
        assertEquals("You made it!", other.getCurrentPage().getText(), "New window contents");
        initialPage.getLinkWithID("return").click();
        assertEquals(2, wc.getOpenWindows().length, "Number of windows after following link");
        assertEquals("You came back!", other.getCurrentPage().getText(), "Updated window contents");
    }

    @Test
    void testTargetInAnotherWindow() throws Exception {
        defineWebPage("linker", "<a href='start.html' target='_blank'>start</a>");
        defineResource("Frames.html",
                "<html>" + "<frameset cols=\"20%,80%\">" + "    <frame src=\"linker.html\" name=\"here\">"
                        + "    <frame name=\"somewhere\">" + "</frameset></html>");
        defineResource("goHere", "You made it!");
        defineWebPage("start", "<a href='goHere' id='go' target='somewhere'>here</a>");

        WebClient wc = new WebConversation();
        WebResponse initialPage = wc.getResponse(getHostPath() + "/Frames.html");
        initialPage.getSubframeContents("here").getLinkWith("start").click();
        assertEquals(2, wc.getOpenWindows().length, "# Open windows");
        WebWindow other = wc.getOpenWindows()[1];
        other.getCurrentPage().getLinkWithID("go").click();
        assertEquals("You made it!", initialPage.getSubframeContents("somewhere").getText(), "New frame contents");
    }

    @Test
    void testCloseOnlyWindow() throws Exception {
        defineResource("goHere", "You made it!");
        WebConversation wc = new WebConversation();
        WebWindow original = wc.getMainWindow();
        wc.getMainWindow().close();
        assertTrue(original.isClosed(), "Main window did not close");
        assertNotNull(wc.getMainWindow(), "No main window was created");
    }

    @Test
    void testListeners() throws Exception {
        defineResource("goHere", "You made it!");
        defineWebPage("start", "<a href='goHere' id='go' target='_blank'>here</a>");

        final ArrayList newWindowContents = new ArrayList<>();
        final ArrayList closedWindows = new ArrayList<>();
        WebClient wc = new WebConversation();
        wc.addWindowListener(new WebWindowListener() {
            @Override
            public void windowOpened(WebClient client, WebWindow window) {
                try {
                    newWindowContents.add(window.getCurrentPage().getText());
                } catch (IOException e) {
                    fail("Error trying to read page");
                }
            }

            @Override
            public void windowClosed(WebClient client, WebWindow window) {
                closedWindows.add(window);
            }
        });
        WebResponse initialPage = wc.getResponse(getHostPath() + "/start.html");
        initialPage.getLinkWithID("go").click();
        assertFalse(newWindowContents.isEmpty(), "No window opened");
        assertEquals("You made it!", newWindowContents.get(0), "New window contents");
        assertTrue(closedWindows.isEmpty(), "Window already reported closed");

        WebWindow main = wc.getMainWindow();
        WebWindow other = wc.getOpenWindows()[1];
        other.close();
        assertEquals(main, wc.getMainWindow(), "Main window");
        assertFalse(closedWindows.isEmpty(), "No windows reported closed");
        assertEquals(other, closedWindows.get(0), "Window reported closed");
    }

    @Test
    void testWindowIndependence() throws Exception {
        defineResource("next", "You made it!", "text/plain");
        defineWebPage("goHere", "<a href='next' id=proceed>more</a>");
        defineWebPage("start", "<a href='goHere.html' id='go' target='_blank'>here</a>");

        WebClient wc = new WebConversation();
        WebWindow main = wc.getMainWindow();
        WebResponse initialPage = wc.getResponse(getHostPath() + "/start.html");
        initialPage.getLinkWithID("go").click();
        WebWindow other = wc.getOpenWindows()[1];
        other.getResponse(other.getCurrentPage().getLinkWithID("proceed").getRequest());
        assertEquals(getHostPath() + "/start.html", main.getCurrentPage().getURL().toExternalForm(), "Main page URL");
        assertEquals("You made it!", other.getCurrentPage().getText(), "New window contents");
    }

    @Test
    void testWindowContext() throws Exception {
        defineResource("next", "You made it!");
        defineWebPage("goHere", "<a href='next' id=proceed>more</a>");
        defineWebPage("start", "<a href='goHere.html' id='go' target='_blank'>here</a>");

        WebClient wc = new WebConversation();
        wc.getMainWindow();
        WebResponse initialPage = wc.getResponse(getHostPath() + "/start.html");
        initialPage.getLinkWithID("go").click();
        WebWindow other = wc.getOpenWindows()[1];
        other.getCurrentPage().getLinkWithID("proceed").click();
        assertEquals("You made it!", other.getCurrentPage().getText(), "New window contents");
    }

}

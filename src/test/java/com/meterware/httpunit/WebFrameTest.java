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
package com.meterware.httpunit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.HttpURLConnection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.migrationsupport.rules.ExternalResourceSupport;

/**
 * A test of the web frame functionality.
 */
@ExtendWith(ExternalResourceSupport.class)
public class WebFrameTest extends HttpUnitTest {

    @BeforeEach
    void setUp() throws Exception {
        _wc = new WebConversation();

        defineWebPage("Linker", "This is a trivial page with <a href=Target.html>one link</a>");
        defineWebPage("Target", "This is another page with <a href=Form.html target=\"_top\">one link</a>");
        defineWebPage("Form",
                "This is a page with a simple form: "
                        + "<form action=submit><input name=name><input type=submit></form>"
                        + "<a href=Linker.html target=red>a link</a>");
        defineResource("Frames.html",
                "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" + "<FRAMESET cols=\"20%,80%\">"
                        + "    <FRAME src=\"Linker.html\" name=\"red\">" + "    <FRAME src=Form.html name=blue>"
                        + "</FRAMESET></HTML>");
    }

    @Test
    void testDefaultFrameNames() throws Exception {
        defineWebPage("Initial", "This is a trivial page");
        _wc.getResponse(getHostPath() + "/Initial.html");
        assertMatchingSet("Frames defined for the conversation", new String[] { "_top" }, _wc.getFrameNames());
    }

    @Test
    void testDefaultFrameContents() throws Exception {
        WebResponse response = _wc.getResponse(getHostPath() + "/Linker.html");
        assertTrue(response == _wc.getFrameContents("_top"), "Default response not the same as default frame contents");
        response = _wc.getResponse(response.getLinks()[0].getRequest());
        assertTrue(response == _wc.getFrameContents("_top"), "Second response not the same as default frame contents");
    }

    @Test
    void testFrameNames() throws Exception {
        WebResponse response = _wc.getResponse(getHostPath() + "/Frames.html");
        assertMatchingSet("frame set names", new String[] { "red", "blue" }, response.getFrameNames());
    }

    @Test
    void testParentTarget() throws Exception {
        defineWebPage("Target", "This is another page with <a href=Form.html target='_parent'>one link</a>");
        _wc.getResponse(getHostPath() + "/Frames.html");
        WebResponse resp = _wc.getResponse(_wc.getFrameContents("red").getLinks()[0].getRequest());
        resp = _wc.getResponse(resp.getLinks()[0].getRequest());
        assertMatchingSet("Frames after third response", new String[] { "_top" }, _wc.getFrameNames());
    }

    @Test
    void testParentTargetFromTopFrame() throws Exception {
        defineWebPage("Target", "This is another page with <a href=Form.html target='_parent'>one link</a>");
        WebResponse resp = _wc.getResponse(getHostPath() + "/Target.html");
        resp = _wc.getResponse(resp.getLinks()[0].getRequest());
        assertMatchingSet("Frames after second response", new String[] { "_top" }, _wc.getFrameNames());
    }

    @Test
    void testFrameRequests() throws Exception {
        WebResponse response = _wc.getResponse(getHostPath() + "/Frames.html");
        WebRequest[] requests = response.getFrameRequests();
        assertEquals(2, requests.length, "Number of frame requests");
        assertEquals("red", requests[0].getTarget(), "Target for first request");
        assertEquals(getHostPath() + "/Form.html", requests[1].getURL().toExternalForm(), "URL for second request");
    }

    @Test
    void testFrameRequestsWithFragments() throws Exception {
        defineResource("Frames.html",
                "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" + "<FRAMESET cols=\"20%,80%\">"
                        + "    <FRAME src='Linker.html' name=\"red\">" + "    <FRAME src='Form.html#middle' name=blue>"
                        + "</FRAMESET></HTML>");
        WebResponse response = _wc.getResponse(getHostPath() + "/Frames.html");
        WebRequest[] requests = response.getFrameRequests();
        assertEquals(getHostPath() + "/Form.html", requests[1].getURL().toExternalForm(), "URL for second request");
    }

    @Test
    void testFrameLoading() throws Exception {
        _wc.getResponse(getHostPath() + "/Frames.html");

        assertMatchingSet("Frames defined for the conversation", new String[] { "_top", "red", "blue" },
                _wc.getFrameNames());
        assertEquals(1, _wc.getFrameContents("red").getLinks().length, "Number of links in first frame");
        assertEquals(1, _wc.getFrameContents("blue").getForms().length, "Number of forms in second frame");
    }

    @Test
    void testInFrameLinks() throws Exception {
        WebResponse response = _wc.getResponse(getHostPath() + "/Frames.html");

        response = _wc.getResponse(_wc.getFrameContents("red").getLinks()[0].getRequest());
        assertTrue(response == _wc.getFrameContents("red"), "Second response not the same as source frame contents");
        assertMatchingSet("Frames defined for the conversation", new String[] { "_top", "red", "blue" },
                _wc.getFrameNames());
        assertEquals(getHostPath() + "/Target.html", response.getURL().toExternalForm(), "URL for second request");
    }

    @Test
    void testFrameURLBase() throws Exception {
        defineWebPage("Deeper/Linker", "This is a trivial page with <a href=Target.html>one link</a>");
        defineWebPage("Deeper/Target", "This is another page with <a href=Form.html target=\"_top\">one link</a>");
        defineWebPage("Deeper/Form",
                "This is a page with a simple form: "
                        + "<form action=submit><input name=name><input type=submit></form>"
                        + "<a href=Linker.html target=red>a link</a>");
        defineResource("Frames.html",
                "<HTML><HEAD><TITLE>Initial</TITLE>" + "<base href=\"" + getHostPath() + "/Deeper/Frames.html\"></HEAD>"
                        + "<FRAMESET cols=\"20%,80%\">" + "    <FRAME src=\"Linker.html\" name=\"red\">"
                        + "    <FRAME src=Form.html name=blue>" + "</FRAMESET></HTML>");

        WebResponse response = _wc.getResponse(getHostPath() + "/Frames.html");

        response = _wc.getResponse(_wc.getFrameContents("red").getLinks()[0].getRequest());
        assertTrue(response == _wc.getFrameContents("red"), "Second response not the same as source frame contents");
        assertMatchingSet("Frames defined for the conversation", new String[] { "_top", "red", "blue" },
                _wc.getFrameNames());
        assertEquals(getHostPath() + "/Deeper/Target.html", response.getURL().toExternalForm(),
                "URL for second request");
    }

    @Test
    void testDuplicateFrameNames() throws Exception {
        defineWebPage("Linker", "This is a trivial page with <a href=Target.html>one link</a>");
        defineWebPage("Target", "This is another page with <a href=Form.html target=\"_top\">one link</a>");
        defineWebPage("Form",
                "This is a page with a simple form: "
                        + "<form action=submit><input name=name><input type=submit></form>"
                        + "<a href=Linker.html target=red>a link</a>");
        defineResource("Frames.html", "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" + "<FRAMESET cols=\"20%,80%\">"
                + "    <FRAME src='SubFrames.html'>" + "    <FRAME src=Form.html>" + "</FRAMESET></HTML>");

        defineResource("SubFrames.html", "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" + "<FRAMESET cols=\"20%,80%\">"
                + "    <FRAME src=\"Linker.html\">" + "    <FRAME src=Form.html>" + "</FRAMESET></HTML>");

        WebResponse response = _wc.getResponse(getHostPath() + "/Frames.html");
        WebResponse linker = getFrameWithURL(_wc, "Linker");
        assertNotNull(linker, "Linker not found");

        response = _wc.getResponse(linker.getLinks()[0].getRequest());
        WebResponse target = getFrameWithURL(_wc, "Target");
        assertTrue(response == target, "Second response not the same as source frame contents");
    }

    @Test
    void testUnnamedFrames() throws Exception {
        defineWebPage("Linker", "This is a trivial page with <a href=Target.html>one link</a>");
        defineWebPage("Target", "This is another page with <a href=Form.html target=\"_top\">one link</a>");
        defineWebPage("Form",
                "This is a page with a simple form: "
                        + "<form action=submit><input name=name><input type=submit></form>"
                        + "<a href=Linker.html target=red>a link</a>");
        defineResource("Frames.html", "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" + "<FRAMESET cols=\"20%,80%\">"
                + "    <FRAME src=\"Linker.html\">" + "    <FRAME src=Form.html>" + "</FRAMESET></HTML>");

        WebResponse response = _wc.getResponse(getHostPath() + "/Frames.html");
        WebResponse linker = getFrameWithURL(_wc, "Linker");
        assertNotNull(linker, "Linker not found");

        response = _wc.getResponse(linker.getLinks()[0].getRequest());
        WebResponse target = getFrameWithURL(_wc, "Target");
        assertTrue(response == target, "Second response not the same as source frame contents");
    }

    private String getNameOfFrameWithURL(WebConversation wc, String urlString) {
        String[] names = wc.getFrameNames();
        for (String name : names) {
            WebResponse candidate = wc.getFrameContents(name);
            if (candidate.getURL().toExternalForm().indexOf(urlString) >= 0) {
                return name;
            }
        }
        return null;
    }

    private WebResponse getFrameWithURL(WebConversation wc, String urlString) {
        String name = getNameOfFrameWithURL(wc, urlString);
        if (name == null) {
            return null;
        }
        return wc.getFrameContents(name);
    }

    @Test
    void testCrossFrameLinks() throws Exception {
        WebResponse response = _wc.getResponse(getHostPath() + "/Frames.html");

        _wc.getResponse(_wc.getFrameContents("red").getLinks()[0].getRequest());
        response = _wc.getResponse(_wc.getFrameContents("blue").getLinks()[0].getRequest());
        assertTrue(response == _wc.getFrameContents("red"), "Second response not the same as source frame contents");
        assertMatchingSet("Frames defined for the conversation", new String[] { "_top", "red", "blue" },
                _wc.getFrameNames());
        assertEquals(getHostPath() + "/Linker.html", response.getURL().toExternalForm(), "URL for second request");
    }

    @Test
    void testGetSubframes() throws Exception {
        WebResponse response = _wc.getResponse(getHostPath() + "/Frames.html");
        assertEquals(_wc.getFrameContents("red"), response.getSubframeContents("red"), "red subframe");
    }

    /**
     * test for bug report [ 1535018 ] Sub frame recognition - getSubframeContents by Oliver GL this is how it does not
     * work ...
     */
    public void xtestSubFrameRecognitionOriginal() throws Exception {
        defineWebPage("frame1", "frame1Content");
        defineWebPage("frame2", "frame2Content");
        defineWebPage("frame3", "frame3Content");
        String html = "<html>\n" + "<head>\n" + "</head>\n" + "<frameset name=\"topset\" rows=\"65,*\">\n"
                + "  <frame src=\"frame1.html\" name=\"Banner\" frameborder=\"0\" noresize scrolling=\"no\">\n"
                + "  <frameset name=\"subset\" cols=\"180,*\">\n"
                + "     <frame src=\"frame2.html\" name=\"Menu\"   frameborder=\"0\" noresize scrolling=\"yes\">\n"
                + "     <frame src=\"frame3.html\" name=\"Action\" frameborder=\"0\" noresize scrolling=\"auto\">\n"
                + "  </frameset>\n" + "</frameset>\n" + "</html>";
        // for checking the resulting frame page in real browser ...
        // System.out.println(html);
        defineResource("frames.html", html);
        WebResponse topResponse = _wc.getResponse(getHostPath() + "/frames.html");
        String[] frameNames = topResponse.getFrameNames();
        // System.out.println("found "+frameNames.length+" frames");
        assertEquals(3, frameNames.length);
        String[] expectedNames = { "Banner", "Menu", "Action" };
        for (int i = 0; i < frameNames.length; i++) {
            // System.out.println("frame #"+i+" is '"+frameNames[i]+"'");
            assertEquals(frameNames[i], expectedNames[i], "frame #" + i + " should be '" + expectedNames[i] + "'");
        }
        WebResponse bannerFrame = _wc.getFrameContents("Banner");
        for (String frameName : frameNames) {
            WebResponse subFrame = bannerFrame.getSubframeContents(frameName);
            assertNotNull(subFrame);
        }
    }

    /**
     * test for bug report [ 1535018 ] Sub frame recognition - getSubframeContents by Oliver GL and this is how it works
     */
    @Test
    void testSubFrameRecognition() throws Exception {
        String frame1Content = "  <frameset name=\"subset\" cols=\"180,*\">\n"
                + "     <frame src=\"frame2.html\" name=\"Menu\"   frameborder=\"0\" noresize scrolling=\"yes\">\n"
                + "     <frame src=\"frame3.html\" name=\"Action\" frameborder=\"0\" noresize scrolling=\"auto\">\n"
                + "  </frameset>";

        defineWebPage("frame1", frame1Content);
        defineWebPage("frame2", "frame2Content");
        defineWebPage("frame3", "frame3Content");
        String html = "<html>\n" + "<head>\n" + "</head>\n" + "<frameset name=\"topset\" rows=\"65,*\">\n"
                + "  <frame src=\"frame1.html\" name=\"Banner\" frameborder=\"0\" noresize scrolling=\"no\">\n"
                + "</frameset>\n" + "</html>";
        // for checking the resulting frame page in real browser ...
        // System.out.println(html);
        defineResource("frames.html", html);
        WebResponse topResponse = _wc.getResponse(getHostPath() + "/frames.html");
        String[] frameNames = topResponse.getFrameNames();
        // System.out.println("found "+frameNames.length+" frames");
        String[] expectedTopNames = { "Banner" };
        assertEquals(frameNames.length, expectedTopNames.length);
        for (int i = 0; i < frameNames.length; i++) {
            // System.out.println("frame #"+i+" is '"+frameNames[i]+"'");
            assertEquals(frameNames[i], expectedTopNames[i],
                    "frame #" + i + " should be '" + expectedTopNames[i] + "'");
        }
        WebResponse bannerFrame = _wc.getFrameContents("Banner");
        String[] subFrameNames = bannerFrame.getFrameNames();
        for (int i = 1; i < subFrameNames.length; i++) {
            WebResponse subFrame = bannerFrame.getSubframeContents(subFrameNames[i]);
            assertNotNull(subFrame);
        }
    }

    @Test
    void testNestedSubFrames() throws Exception {
        defineResource("SuperFrames.html",
                "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" + "<FRAMESET cols=\"50%,50%\">"
                        + "    <FRAME src=\"Frames.html\" name=\"crimson\">"
                        + "    <FRAME src=\"Form.html\" name=\"blue\">" + "</FRAMESET></HTML>");
        WebResponse response = _wc.getResponse(getHostPath() + "/SuperFrames.html");
        WebResponse frameContents = _wc.getFrameContents("red");
        WebResponse subframeContents = response.getSubframeContents("crimson").getSubframeContents("red");
        assertEquals(frameContents, subframeContents, "crimson.red subframe");
    }

    /**
     * Verifies that a link in one subframe can update the contents of a different subframe of the same frame.
     *
     * @throws Exception
     *             if any method throws an unexpected exception
     */
    @Test
    void testNestedCrossFrameLinks() throws Exception {
        defineResource("SuperFrames.html",
                "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" + "<FRAMESET cols=\"50%,50%\">"
                        + "    <FRAME src=\"Frames.html\" name=\"red\">"
                        + "    <FRAME src=\"Frames.html\" name=\"blue\">" + "</FRAMESET></HTML>");
        _wc.getResponse(getHostPath() + "/SuperFrames.html");
        FrameSelector nestedRedFrame = _wc.getFrameContents("red").getSubframeContents("red").getFrame();
        FrameSelector nestedBlueFrame = _wc.getFrameContents("red").getSubframeContents("blue").getFrame();

        _wc.getResponse(_wc.getFrameContents(nestedRedFrame).getLinks()[0].getRequest());
        _wc.getFrameContents(nestedBlueFrame).getLinks()[0].click();
        assertEquals(getHostPath() + "/Linker.html", _wc.getFrameContents(nestedRedFrame).getURL().toExternalForm(),
                "URL for second request");
    }

    /**
     * Verifies that a link in one subframe can update the original subframe or the top-level window.
     *
     * @throws Exception
     *             if any method throws an unexpected exception
     */
    @Test
    void testCrossLevelLinks() throws Exception {
        defineResource("SuperFrames.html",
                "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" + "<FRAMESET cols=\"50%,50%\">"
                        + "    <FRAME src=\"Frames.html\" name=\"red\">"
                        + "    <FRAME src=\"Frames.html\" name=\"blue\">" + "</FRAMESET></HTML>");
        _wc.getResponse(getHostPath() + "/SuperFrames.html");
        FrameSelector nestedRedFrame = _wc.getFrameContents("red").getSubframeContents("red").getFrame();

        _wc.getFrameContents(nestedRedFrame).getLinks()[0].click();
        WebResponse frameContent = _wc.getResponse(_wc.getFrameContents(nestedRedFrame).getLinks()[0].getRequest());
        assertTrue(frameContent == _wc.getFrameContents("_top"),
                "Second response not the same as source frame contents");
        assertEquals(getHostPath() + "/Form.html", frameContent.getURL().toExternalForm(), "URL for second request");
        assertEquals(1, _wc.getFrameNames().length, "Number of active frames");
    }

    @Test
    void testLinkToTopFrame() throws Exception {
        WebResponse response = _wc.getResponse(getHostPath() + "/Frames.html");

        response = _wc.getResponse(_wc.getFrameContents("red").getLinks()[0].getRequest());
        response = _wc.getResponse(response.getLinks()[0].getRequest());
        assertTrue(response == _wc.getFrameContents("_top"), "Second response not the same as source frame contents");
        assertEquals(getHostPath() + "/Form.html", response.getURL().toExternalForm(), "URL for second request");
        assertMatchingSet("Frames defined for the conversation", new String[] { "_top" }, _wc.getFrameNames());
    }

    @Test
    void testEmptyFrame() throws Exception {
        defineResource("HalfFrames.html", "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" + "<FRAMESET cols=\"20%,80%\">"
                + "    <FRAME src=\"Linker.html\" name=\"red\">" + "    <FRAME name=blue>" + "</FRAMESET></HTML>");
        _wc.getResponse(getHostPath() + "/HalfFrames.html");
        WebResponse response = _wc.getFrameContents("blue");

        assertNotNull(response, "Loaded nothing for the empty frame");
        assertEquals(0, response.getLinks().length, "Num links");
    }

    @Test
    void testSelfTargetLink() throws Exception {
        defineWebPage("Linker", "This is a trivial page with <a href=Target.html target=_self>one link</a>");

        _wc.getResponse(getHostPath() + "/Frames.html");
        WebResponse response = _wc.getResponse(_wc.getFrameContents("red").getLinks()[0].getRequest());
        assertMatchingSet("Frames defined for the conversation", new String[] { "_top", "red", "blue" },
                _wc.getFrameNames());
        assertTrue(response == _wc.getFrameContents("red"), "Second response not the same as source frame contents");
        assertEquals(getHostPath() + "/Target.html", response.getURL().toExternalForm(), "URL for second request");
    }

    @Test
    void testSelfTargetForm() throws Exception {
        defineWebPage("Linker", "<form action=redirect.html target=_self><input type=text name=sample value=z></form>");
        defineResource("redirect.html?sample=z", "", HttpURLConnection.HTTP_MOVED_PERM);
        addResourceHeader("redirect.html?sample=z", "Location: " + getHostPath() + "/Target.html");

        _wc.getResponse(getHostPath() + "/Frames.html");
        WebResponse response = _wc.getResponse(_wc.getFrameContents("red").getForms()[0].getRequest());
        assertMatchingSet("Frames defined for the conversation", new String[] { "_top", "red", "blue" },
                _wc.getFrameNames());
        assertTrue(response == _wc.getFrameContents("red"), "Second response not the same as source frame contents");
        assertEquals(getHostPath() + "/Target.html", response.getURL().toExternalForm(), "URL for second request");
    }

    @Test
    void testSubFrameRedirect() throws Exception {
        defineResource("Linker.html", "", HttpURLConnection.HTTP_MOVED_PERM);
        addResourceHeader("Linker.html", "Location: " + getHostPath() + "/Target.html");

        _wc.getResponse(getHostPath() + "/Frames.html");
        assertMatchingSet("Frames defined for the conversation", new String[] { "_top", "red", "blue" },
                _wc.getFrameNames());
        assertTrue(_wc.getFrameContents("red").getURL().toExternalForm().endsWith("Target.html"), "Did not redirect");

    }

    private void defineNestedFrames() throws Exception {
        defineResource("Topmost.html",
                "<HTML><HEAD><TITLE>Topmost</TITLE></HEAD>" + "<FRAMESET cols=\"20%,80%\">"
                        + "    <FRAME src=\"Target.html\" name=\"red\">"
                        + "    <FRAME src=\"Inner.html\" name=\"blue\">" + "</FRAMESET></HTML>");
        defineResource("Inner.html", "<HTML><HEAD><TITLE>Inner</TITLE></HEAD>" + "<FRAMESET rows=\"20%,80%\">"
                + "    <FRAME src=\"Form.html\" name=\"green\">" + "</FRAMESET></HTML>");
    }

    @Test
    void testGetNestedFrameByName() throws Exception {
        defineNestedFrames();
        _wc.getResponse(getHostPath() + "/Topmost.html");
        _wc.getFrameContents("green");
    }

    @Test
    void testLinkWithAncestorTarget() throws Exception {
        defineNestedFrames();
        _wc.getResponse(getHostPath() + "/Topmost.html");
        WebResponse innerResponse = _wc.getFrameContents("blue").getSubframeContents("green");
        innerResponse.getLinks()[0].click();
        assertEquals("Linker", _wc.getFrameContents("red").getTitle(), "Title of 'red' frame");
    }

    /**
     * test I frame detection
     *
     * @throws Exception
     */
    @Test
    void testIFrameDetection() throws Exception {
        defineWebPage("Frame", "This is a trivial page with <a href='mailto:russgold@httpunit.org'>one link</a>"
                + "and <iframe name=center src='Contents.html'><form name=hidden></form></iframe>");
        defineWebPage("Contents", "This is another page with <a href=Form.html>one link</a>");
        defineWebPage("Form", "This is a page with a simple form: "
                + "<form action=submit><input name=name><input type=submit></form>");

        WebResponse response = _wc.getResponse(getHostPath() + "/Frame.html");
        WebRequest[] requests = response.getFrameRequests();
        assertEquals(1, response.getLinks().length, "Number of links in main frame");
        assertEquals(0, response.getForms().length, "Number of forms in main frame");
        assertEquals(1, requests.length, "Number of frame requests");
        assertEquals("center", requests[0].getTarget(), "Target for iframe request");

        WebResponse contents = getFrameWithURL(_wc, "Contents");
        assertNotNull(contents, "Contents not found");
        assertEquals(1, _wc.getFrameContents("center").getLinks().length, "Number of links in iframe");
    }

    /**
     * test I Frame with a Form according to mail to mailinglist of 2008-03-25 Problems with IFrames by Allwyn D'souza
     * TODO activate test when it's clear how it should work
     *
     * @throws Exception
     */
    public void xtestIFrameForm() throws Exception {
        String login = "//Login.html (main page that is loaded - this page embed the IFrame).\n" + "\n" + "<html>\n"
                + "<Head>\n" + "<Script>\n" + "<!--\n" + "function SetLoginForm(name, password, Submit) {\n"
                + " document.loginForm.name.value = name;\n" + " document.loginForm.password.value = password;\n" + "\n"
                + " document.loginForm.submit();\n" + "}\n" + "-->\n" + "</Script>\n" + "</Head>\n" + "<Body>\n"
                + "<Form name=\"loginForm\" action=\"/LoginController\" method=\"Post\">\n"
                + "<input type=\"hidden\" name=\"name\" value=\"\" />\n"
                + "<input type=\"hidden\" name=\"password\" value=\"\" />\n" + "</Form>\n" + "<Center>\n"
                + "<IFrame name=\"login\" id=\"login\" src=\"LoginDialog.html\" />\n" + "</Center>\n" + "</Body>\n"
                + "</html>\n";

        String loginDialog = "// LoginDialog.html - IFrame\n" + "\n" + "<html>\n" + "<Head>\n" + "<Script>\n" + "<!--\n"
                + "function SubmitToParent(action) {\n"
                + " parent.SetLoginForm(document.submit_to_parent.name.value,document.submit_to_parent.password.value);\n"
                + "}\n" + "-->\n" + "</Script>\n" + "</Head>\n" + "<Body>\n"
                + "<Form id=f1 name=\"submit_to_parent\" method=\"Post\">\n"
                + "<input type=\"text\" name=\"name\" value=\"\" />\n"
                + "<input type=\"text\" name=\"password\" value=\"\" />\n"
                + "<input type=\"submit\" name=\"Ok\" value=\"login\" onClick=\"SubmitToParent('Submit')\" />\n"
                + "</Form>\n" + "</Body>\n" + "</html>\n";

        defineWebPage("Login", login);
        defineWebPage("LoginDialog", loginDialog);
        _wc.getResponse(getHostPath() + "/Login.html");
        WebResponse bottomFrame = _wc.getFrameContents("login"); // load the <Iframe>
        WebForm form = bottomFrame.getFormWithName("submit_to_parent");
        form.setParameter("name", "aa");
        form.setParameter("password", "xx");
        boolean oldDebug = HttpUnitUtils.setEXCEPTION_DEBUG(true);
        try {
            form.submit();
        } catch (ScriptException se) {
            // TODO clarify what should happen here ...
            String msg = se.getMessage();
            // Event 'SubmitToParent('Submit')' failed: org.mozilla.javascript.EcmaError: TypeError: Cannot read
            // property "name" from undefined
            assertTrue(msg.startsWith("Event"));
            System.err.println(msg);
            throw se;
        }
        HttpUnitUtils.setEXCEPTION_DEBUG(oldDebug);
    }

    /**
     * test I frames that are disabled
     *
     * @throws Exception
     */
    // TODO JWL 7/6/2021 Breaks with nekohtml > 1.9.6.2
    @Disabled
    @Test
    void testIFrameDisabled() throws Exception {
        defineWebPage("Frame", "This is a trivial page with <a href='mailto:russgold@httpunit.org'>one link</a>"
                + "and <iframe name=center src='Contents.html'><form name=hidden></form></iframe>");
        defineWebPage("Contents", "This is another page with <a href=Form.html>one link</a>");

        _wc.getClientProperties().setIframeSupported(false);
        WebResponse response = _wc.getResponse(getHostPath() + "/Frame.html");
        WebRequest[] requests = response.getFrameRequests();
        assertEquals(1, response.getLinks().length, "Number of links in main frame");
        assertEquals(1, response.getForms().length, "Number of forms in main frame");
        assertEquals(0, requests.length, "Number of frame requests");
    }

    /**
     * Verifies that an open call from a subframe can specify another frame name.
     */
    @Test
    void testOpenIntoSubframe() throws Exception {
        defineResource("Frames.html", "<html><head><frameset>" + "    <frame name='banner'>"
                + "    <frame src='main.html' name='main'>" + "</frameset></html>");
        defineResource("target.txt", "You made it!");
        defineWebPage("main", "<button id='button' onclick=\"window.open( 'target.txt', 'banner' )\">");

        _wc.getResponse(getHostPath() + "/Frames.html");
        ((Button) _wc.getFrameContents("main").getElementWithID("button")).click();
        assertEquals(1, _wc.getOpenWindows().length, "Num open windows");
        assertEquals("You made it!", _wc.getFrameContents("banner").getText(), "New banner");
        assertNotNull(_wc.getFrameContents("main").getElementWithID("button"), "Original button no longer there");
    }

    /**
     * Verifies that an open call from a subframe can specify another frame name.
     */
    @Test
    void testSelfOpenFromSubframe() throws Exception {
        defineResource("Frames.html", "<html><head><frameset>" + "    <frame name='banner' src='banner.html'>"
                + "    <frame name='main'   src='main.html'>" + "</frameset></html>");
        defineResource("target.txt", "You made it!");
        defineWebPage("main", "<button id='button2' onclick=\"window.open( 'target.txt', 'banner' )\">");
        defineWebPage("banner", "<button id='button1' onclick=\"window.open( 'target.txt', '_self' )\">");

        _wc.getResponse(getHostPath() + "/Frames.html");
        ((Button) _wc.getFrameContents("banner").getElementWithID("button1")).click();
        assertEquals(1, _wc.getOpenWindows().length, "Num open windows");
        assertEquals("You made it!", _wc.getFrameContents("banner").getText(), "New banner");
        assertNotNull(_wc.getFrameContents("main").getElementWithID("button2"), "Second frame no longer there");
    }

    /**
     * Verifies that an open call from a subframe can specify another frame name.
     */
    @Test
    void testFrameWithHashSource() throws Exception {
        defineResource("Frames.html", "<html><head><frameset>" + "    <frame name='banner' src='#'>"
                + "    <frame name='main'   src='main.html'>" + "</frameset></html>");
        defineResource("target.txt", "You made it!");
        defineWebPage("main", "<a id='banner' href='target.txt'>banner</a>");

        _wc.getResponse(getHostPath() + "/Frames.html");
        WebLink link = (WebLink) _wc.getFrameContents("main").getElementWithID("banner");
        assertNotNull(link, "No link found");
    }

    private WebConversation _wc;
}

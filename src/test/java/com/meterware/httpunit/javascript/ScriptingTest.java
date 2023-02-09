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
package com.meterware.httpunit.javascript;

import static org.junit.Assert.*;

import com.meterware.httpunit.*;

import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 * @author Wolfgang Fahl - for compiling patches from the Source Forge web site 2008-03
 */
public class ScriptingTest extends AbstractJavaScriptTest {

    @Test
    public void testJavaScriptURLWithValue() throws Exception {
        defineResource("OnCommand.html", "<html><head></head>" +
                "<body>" +
                "<a href='JavaScript:\"You made it!\"'>go</a>" +
                "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinks()[0].click();
        assertEquals("New page", "You made it!", wc.getCurrentPage().getText());
        assertEquals("New URL", "javascript:\"You made it!\"", wc.getCurrentPage().getURL().toExternalForm());
    }


    @Test
    public void testJavaScriptURLWithNoValue() throws Exception {
        defineResource("OnCommand.html", "<html><head></head>" +
                "<body>" +
                "<a href=\"javascript:alert( 'Hi there!' )\">go</a>" +
                "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        WebResponse myPage = response.getLinks()[0].click();
        assertEquals("Alert message", "Hi there!", wc.popNextAlert());
        assertEquals("Current page URL", getHostPath() + "/OnCommand.html", wc.getCurrentPage().getURL().toExternalForm());
        assertEquals("Returned page URL", getHostPath() + "/OnCommand.html", myPage.getURL().toExternalForm());
    }


    @Test
    public void testInitialJavaScriptURL() throws Exception {
        WebConversation wc = new WebConversation();
        GetMethodWebRequest request = new GetMethodWebRequest("javascript:alert( 'Hi there!' )");
        assertEquals("Javascript URL", "javascript:alert( 'Hi there!' )", request.getURL().toExternalForm());
        wc.getResponse(request);
        assertEquals("Alert message", "Hi there!", wc.popNextAlert());
    }


    @Test
    public void testJavaScriptURLWithVariables() throws Exception {
        defineResource("OnCommand.html", "<html><head></head>" +
                "<body>" +
                "<a href='javascript:\"Our winner is... \" + document.the_form.winner.value'>go</a>" +
                "<form name='the_form'>" +
                "  <input name=winner type=text value='George of the Jungle'>" +
                "</form></body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinks()[0].click();
        assertEquals("New page", "Our winner is... George of the Jungle", wc.getCurrentPage().getText());
    }


    @Test
    public void testJavaScriptURLWithQuestionMark() throws Exception {
        defineResource("/appname/HandleAction/report?type=C", "You made it!");
        defineResource("OnCommand.html", "<html><head></head>" +
                "<body>" +
                "<a href=\"javascript:redirect('/appname/HandleAction/report?type=C')\">go</a>" +
                "<script language='JavaScript'>" +
                "  function redirect( url ) { window.location=url; }" +
                "</script>" +
                "</form></body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinks()[0].click();
        assertEquals("New page", "You made it!", wc.getCurrentPage().getText());
    }


    /**
     * test for bug report [ 1508516 ] Javascript method: "undefined" is not supported
     *
     * @throws Exception
     */
    @Test
    public void testUndefined() throws Exception {
        WebConversation wc = doTestJavaScript("if (typeof(xyzDefinitelyNotDefined) == 'undefined') {\n" +
                "alert ('blabla');\n" +
                "return;\n" +
                "}");
        assertEquals("Alert message", "blabla", wc.popNextAlert());
    }

    /**
     * test for bug report [ 1153066 ] Eternal loop while processing javascript
     * by Serguei Khramtchenko 2005-02-27
     *
     * @throws Exception
     */
    @Test
    public void testAvoidEndlessLoop() throws Exception {
        WebConversation wc = doTestJavaScript("document.location='#node_selected';");
    }

    /**
     * test javascript call to an included function
     *
     * @throws Exception
     */
    @Test
    public void testJavaScriptURLWithIncludedFunction() throws Exception {
        defineResource("saycheese.js", "function sayCheese() { alert( \"Cheese!\" ); }");
        defineResource("OnCommand.html", "<html><head><script language='JavaScript' src='saycheese.js'>" +
                "</script></head>" +
                "<body>" +
                "<a href=\"javascript:sayCheese()\">go</a>" +
                "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinkWith("go").click();
        assertEquals("Alert message", "Cheese!", wc.popNextAlert());
    }

    /**
     * test javascript call to built-in functions
     * e.g. toLowerCase
     */
    @Test
    public void testJavaScriptWitBuiltInFunctions() throws Exception {
        defineResource("OnCommand.html", "<html>" +
                "<body>" +
                "<a href=\"javascript:alert(toLowerCase('Cheese!'))\">go</a>" +
                "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinkWith("go").click();
        assertEquals("Alert message", "cheese!", wc.popNextAlert());
    }

    /**
     * test javascript call to an included function
     *
     * @throws Exception
     */
    @Test
    public void testJavaScriptURLWithIncludedFunction2() throws Exception {
        defineResource("saycheese.js", "function sayCheese() { alert( \"Cheese!\" ); }");
        defineResource("callcheese.js", "function callCheese() { sayCheese(); }");
        defineResource("OnCommand.html", "<html><head>\n" +
                "<script language='JavaScript' src='saycheese.js'></script>\n" +
                "<script language='JavaScript' src='callcheese.js'></script>\n" +
                "</head><body>" +
                "	<a href=\"javascript:callCheese()\">go</a>" +
                "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinkWith("go").click();
        assertEquals("Alert message", "Cheese!", wc.popNextAlert());
    }

    /**
     * test Detection of Javascript files that can not be found
     * behaviour pointed out by Dan Lipofsky
     *
     * @throws Exception
     */
    @Test
    public void testBadJavascriptFile() {
        // define xyz.js to create a 404 error
        // we don't do this - it should be a default behaviour of the Pseudo Server!
        // defineResource( "xyz.js", "File does not exist: xyz.js", 404);
        defineResource("OnCommand.html",
                "<html><head>" +
                        "<script language='JavaScript' src='xyz.js'></script></head>" +
                        "<body>Hello</body></html>");
        boolean originalState =
                HttpUnitOptions.getExceptionsThrownOnErrorStatus();
        boolean originalScriptState =
                HttpUnitOptions.getExceptionsThrownOnScriptError();
        // make sure stackTraces are not printed on Exceptions
        // uncomment this if you'd actually like to debug the following code
        boolean oldDebug = HttpUnitUtils.setEXCEPTION_DEBUG(false);
        AssertionError failure = null;
        // check 4 combinations of Exception and ScriptError status flags
        for (int i = 0; i < 4; i++) {
            boolean throwScriptException = (i % 2) == 0; // true on case 0 and 2
            boolean throwException = ((i / 2) % 2) == 0;   // true on case 0 and 1
            String testDescription = ("case " + i + " throwScriptException=" + throwScriptException + " throwException=" + throwException);
            HttpUnitOptions.setExceptionsThrownOnErrorStatus(throwException);
            HttpUnitOptions.setExceptionsThrownOnScriptError(throwScriptException);
            HttpUnitOptions.clearScriptErrorMessages();
            WebConversation wc = new WebConversation();
            try {
                WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
                // WebResponse response = wc.getResponse( getHostPath() + "/xyz.js" );
                // assertEquals( 404, response.getResponseCode() );
                if (throwScriptException) {
                    fail("there should have been an exception");
                } else {
                    String[] errMsgs = HttpUnitOptions.getScriptErrorMessages();
                    assertTrue("There should be an error Message", errMsgs.length == 1);
                    String errMsg = errMsgs[0];
                    assertEquals(testDescription, "reponseCode 404 on getIncludedScript for src='xyz.js'", errMsg);
                }
            } catch (ScriptException se) {
                assertTrue(throwScriptException);
            } catch (Exception e) {
                fail("there should be no exception when throwScriptException is " + throwScriptException);
            } catch (AssertionError afe) {
                // continue looping on failed tests
                failure = afe;
            }
        }
        if (failure != null)
            throw (failure);
        // Restore exceptions state
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(originalState);
        HttpUnitOptions.setExceptionsThrownOnScriptError(originalScriptState);
        HttpUnitUtils.setEXCEPTION_DEBUG(oldDebug);
    }


    @Test
    public void testJavaScriptURLInNewWindow() throws Exception {
        defineWebPage("OnCommand", "<input type='button' id='nowindow' onClick='alert(\"hi\")'></input>\n" +
                "<input type='button' id='withwindow' onClick=\"window.open('javascript:alert(\\'hi\\')','_self')\"></input>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        Button button1 = (Button) response.getElementWithID("nowindow");
        Button button2 = (Button) response.getElementWithID("withwindow");
        button1.click();
        assertEquals("Alert message 1", "hi", wc.popNextAlert());
        button2.click();
        assertEquals("Alert message 2", "hi", wc.popNextAlert());
    }


    @Test
    public void testSingleCommandOnLoad() throws Exception {
        defineResource("OnCommand.html", "<html><head></head>" +
                "<body onLoad='alert(\"Ouch!\")'></body>");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/OnCommand.html");
        assertNotNull("No alert detected", wc.getNextAlert());
        assertEquals("Alert message", "Ouch!", wc.popNextAlert());
        assertNull("Alert should have been removed", wc.getNextAlert());
    }

    /**
     * test for bug report [ 1161922 ] setting window.onload has no effect
     * by Kent Tong
     *
     * @throws Exception
     */
    @Test
    public void testWindowOnload() throws Exception {
        String html = "<html>\n" +
                "<body>\n" +
                "<script language='JavaScript'><!--\n" +
                "function foo(text) {\n" +
                "alert(text);\n" +
                "}\n" +
                "window.onload = foo('windowload');\n" +
                "// --></script>\n" +
                "<form>\n" +
                "<input type='Submit' name='OK' value='OK'/>\n" +
                "<a href=\"JavaScript:foo('click')\">go</a>" +
                "</form>\n" +
                "</body>\n" +
                "</html>\n";
        defineResource("OnCommand.html", html);
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        assertNotNull("No alert detected", wc.getNextAlert());
        assertEquals("Alert message", "windowload", wc.popNextAlert());
        response.getLinks()[0].click();
        assertEquals("Alert message", "click", wc.popNextAlert());
    }

    /**
     * check that setExceptionsThrownOnScriptError can be set to false
     * by trying onLoad with an undefined function
     *
     * @throws Exception
     */
    @Test
    public void testOnLoadErrorBypass() throws Exception {
        defineResource("OnCommand.html", "<html><head></head>" +
                "<body onLoad='noSuchFunction()'>" +
                "<img src=sample.jpg>" +
                "</body>");
        WebConversation wc = new WebConversation();
        HttpUnitOptions.setExceptionsThrownOnScriptError(false);
        HttpUnitOptions.clearScriptErrorMessages();

        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals("Number of images on page", 1, response.getImages().length);
        assertEquals("Number of script failures logged", 1, HttpUnitOptions.getScriptErrorMessages().length);
    }

    /**
     * test for bug[ 1055450 ] Error loading included script aborts entire request
     * by Renaud Waldura
     */
    @Test
    public void testIncludeErrorBypass() throws Exception {
        defineResource("OnBypassCommand.html", "<html><head><script language='JavaScript' src='missingScript.js'>" +
                "</script></head>" +
                "<body>" +
                "<a href=\"javascript:sayCheese()\">go</a>" +
                "</body></html>");
        WebConversation wc = new WebConversation();
        boolean oldDebug = HttpUnitUtils.setEXCEPTION_DEBUG(false);
        HttpUnitOptions.setExceptionsThrownOnScriptError(false);
        WebResponse response = wc.getResponse(getHostPath() + "/OnBypassCommand.html");
        try {
            HttpUnitOptions.setExceptionsThrownOnScriptError(true);
            HttpUnitOptions.clearScriptErrorMessages();
            response.getLinkWith("go").click();
            fail("there should have been an exception");
        } catch (ScriptException se) {
            fail("Runtime exception is appropriate in this test case since we ignored the loading error");
        } catch (RuntimeException rte) {
            // java.lang.RuntimeException: Error clicking link: com.meterware.httpunit.ScriptException: URL 'javascript:sayCheese()' failed: org.mozilla.javascript.EcmaError: ReferenceError: "sayCheese" is not defined.
            assertTrue("is not defined should be found in message", rte.getMessage().indexOf("not defined") > 0);
        } finally {
            HttpUnitUtils.setEXCEPTION_DEBUG(oldDebug);
        }

        HttpUnitOptions.setExceptionsThrownOnScriptError(false);
        HttpUnitOptions.clearScriptErrorMessages();
        response.getLinkWith("go").click();
        String messages[] = HttpUnitOptions.getScriptErrorMessages();
        assertTrue("there should be one message", messages.length == 1);
        String message = messages[0];
        assertTrue("is not defined should be found", message.indexOf("is not defined") > 0);
    }


    @Test
    public void testConfirmationDialog() throws Exception {
        defineWebPage("OnCommand", "<a href='NextPage' id='go' onClick='return confirm( \"go on?\" );'>");
        defineResource("NextPage", "Got the next page!");

        WebConversation wc = new WebConversation();
        WebResponse wr = wc.getResponse(getHostPath() + "/OnCommand.html");
        wc.setDialogResponder(new DialogAdapter() {
            public boolean getConfirmation(String confirmationPrompt) {
                assertEquals("Confirmation prompt", "go on?", confirmationPrompt);
                return false;
            }
        });
        wr.getLinkWithID("go").click();
        assertEquals("Current page", wr, wc.getCurrentPage());
        wc.setDialogResponder(new DialogAdapter());
        wr.getLinkWithID("go").click();
        assertEquals("Page after confirmation", "Got the next page!", wc.getCurrentPage().getText());
    }


    @Test
    public void testPromptDialog() throws Exception {
        defineWebPage("OnCommand", "<a href='NextPage' id='go' onClick='return \"yes\" == prompt( \"go on?\", \"no\" );'>");
        defineResource("NextPage", "Got the next page!");

        WebConversation wc = new WebConversation();
        WebResponse wr = wc.getResponse(getHostPath() + "/OnCommand.html");
        wr.getLinkWithID("go").click();
        assertEquals("Current page", wr, wc.getCurrentPage());

        wc.setDialogResponder(new DialogAdapter() {
            public String getUserResponse(String prompt, String defaultResponse) {
                assertEquals("Confirmation prompt", "go on?", prompt);
                assertEquals("Default response", "no", defaultResponse);
                return "yes";
            }
        });
        wr.getLinkWithID("go").click();
        assertEquals("Page after confirmation", "Got the next page!", wc.getCurrentPage().getText());
    }


    @Test
    public void testFunctionCallOnLoad() throws Exception {
        defineResource("OnCommand.html", "<html><head><script language='JavaScript'>" +
                "<!-- hide this\n" +
                "function sayCheese() { alert( \"Cheese!\" ); }" +
                "// end hiding -->\n" +
                "</script></head>" +
                "<body'><script language='JavaScript'>\n" +
                "<!-- hide this\n" +
                "sayCheese();" +
                "-->" +
                "</script></body></html>");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals("Alert message", "Cheese!", wc.popNextAlert());
    }


    @Test
    public void testComment() throws Exception {
        defineResource("OnCommand.html", "<html><head><script language='JavaScript'><!--" +
                "//--></script><script language='JavaScript'>" + "\n" +
                "var n=0;" + "\n" +
                "parseInt(n,32);" +
                "</script></head></html>");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/OnCommand.html");
    }


    @Test
    public void testIncludedFunction() throws Exception {
        defineResource("saycheese.js", "function sayCheese() { alert( \"Cheese!\" ); }");
        defineResource("OnCommand.html", "<html><head><script language='JavaScript' src='saycheese.js'>" +
                "</script></head>" +
                "<body onLoad='sayCheese()'></body>");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals("Alert message", "Cheese!", wc.popNextAlert());
    }


    @Test
    public void testIncludedFunctionWithBaseTag() throws Exception {
        defineResource("scripts/saycheese.js", "function sayCheese() { alert( \"Cheese!\" ); }");
        defineResource("OnCommand.html", "<html><head><base href='" + getHostPath() + "/scripts/OnCommand.html'><script language='JavaScript' src='saycheese.js'>" +
                "</script></head>" +
                "<body onLoad='sayCheese()'></body>");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals("Alert message", "Cheese!", wc.popNextAlert());
    }


    @Test
    public void testWindowOpen() throws Exception {
        defineResource("Target.txt", "You made it!", "text/plain");
        defineResource("OnCommand.html", "<html><head><title>Amazing!</title></head>" +
                "<body><script language='JavaScript'>var otherWindow;</script>" +
                "<a href='#' onClick=\"otherWindow = window.open( '" + getHostPath() + "/Target.txt', 'sample' );\">go</a>" +
                "<a href='#' onClick=\"otherWindow.close();\">go</a>" +
                "<a href='#' onClick=\"alert( 'window is ' + (otherWindow.closed ? '' : 'not ') + 'closed' );\">go</a>" +
                "</body></html>");
        final ArrayList windowsOpened = new ArrayList();
        WebConversation wc = new WebConversation();
        wc.addWindowListener(new WebWindowListener() {
            public void windowOpened(WebClient client, WebWindow window) {
                windowsOpened.add(window);
            }

            public void windowClosed(WebClient client, WebWindow window) {
            }
        });
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinks()[0].click();

        assertFalse("No window opened", windowsOpened.isEmpty());
        final WebWindow openedWindow = (WebWindow) windowsOpened.get(0);
        assertEquals("New window message", "You made it!", openedWindow.getCurrentPage().getText());
        assertEquals("New window name", "sample", openedWindow.getName());
        response.getLinks()[2].click();
        assertEquals("Alert message", "window is not closed", wc.popNextAlert());
        response.getLinks()[1].click();
        assertTrue("Window was not closed", openedWindow.isClosed());
        response.getLinks()[2].click();
        assertEquals("Alert message", "window is closed", wc.popNextAlert());
    }


    @Test
    public void testWindowOpenWithEmptyName() throws Exception {
        defineResource("Target.txt", "You made it!", "text/plain");
        defineResource("OnCommand.html", "<html><head><title>Amazing!</title></head>" +
                "<body><script language='JavaScript'>var otherWindow;</script>" +
                "<a href='#' onClick=\"otherWindow = window.open( '" + getHostPath() + "/Target.txt', '' );\">go</a>" +
                "<a href='#' onClick=\"otherWindow.close();\">go</a>" +
                "<a href='#' onClick=\"alert( 'window is ' + (otherWindow.closed ? '' : 'not ') + 'closed' );\">go</a>" +
                "</body></html>");
        final ArrayList windowsOpened = new ArrayList();
        WebConversation wc = new WebConversation();
        wc.addWindowListener(new WebWindowListener() {
            public void windowOpened(WebClient client, WebWindow window) {
                windowsOpened.add(window);
            }

            public void windowClosed(WebClient client, WebWindow window) {
            }
        });
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinks()[0].click();

        assertFalse("No window opened", windowsOpened.isEmpty());
        final WebWindow openedWindow = (WebWindow) windowsOpened.get(0);
        assertEquals("New window message", "You made it!", openedWindow.getCurrentPage().getText());
        assertEquals("New window name", "", openedWindow.getName());
        response.getLinks()[2].click();
        assertEquals("Alert message", "window is not closed", wc.popNextAlert());
        response.getLinks()[1].click();
        assertTrue("Window was not closed", openedWindow.isClosed());
        response.getLinks()[2].click();
        assertEquals("Alert message", "window is closed", wc.popNextAlert());
    }


    @Test
    public void testWindowOpenWithSelf() throws Exception {
        defineResource("Target.txt", "You made it!", "text/plain");
        defineResource("OnCommand.html", "<html><head><title>Amazing!</title></head>" +
                "<body><script language='JavaScript'>var otherWindow;</script>" +
                "<a href='#' onClick=\"otherWindow = window.open( '" + getHostPath() + "/Target.txt', '_self' );\">go</a>" +
                "<a href='#' onClick=\"otherWindow.close();\">go</a>" +
                "<a href='#' onClick=\"alert( 'window is ' + (otherWindow.closed ? '' : 'not ') + 'closed' );\">go</a>" +
                "</body></html>");
        final ArrayList windowsOpened = new ArrayList();
        WebConversation wc = new WebConversation();
        wc.addWindowListener(new WebWindowListener() {
            public void windowOpened(WebClient client, WebWindow window) {
                windowsOpened.add(window);
            }

            public void windowClosed(WebClient client, WebWindow window) {
            }
        });
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinks()[0].click();

        assertTrue("Opened a new window", windowsOpened.isEmpty());
        assertEquals("New window message", "You made it!", wc.getCurrentPage().getText());
        assertEquals("Number of open windows", 1, wc.getOpenWindows().length);
    }


    @Test
    public void testJavascriptURLWithFragment() throws Exception {
        defineResource("Target.txt", "You made it!", "text/plain");
        defineResource("OnCommand.html", "<html><head><title>Amazing!</title></head>" +
                "<body><script language='JavaScript'>function newWindow(hrefTarget) {" +
                "      window.open(hrefTarget);" +
                "}</script>" +
                "<a href='javascript:newWindow( \"" + getHostPath() + "/Target.txt#middle\" );'>go</a>" +
                "</body></html>");
        final ArrayList windowsOpened = new ArrayList();
        WebConversation wc = new WebConversation();
        wc.addWindowListener(new WebWindowListener() {
            public void windowOpened(WebClient client, WebWindow window) {
                windowsOpened.add(window);
            }

            public void windowClosed(WebClient client, WebWindow window) {
            }
        });
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinks()[0].click();

        assertFalse("No window opened", windowsOpened.isEmpty());
        final WebWindow openedWindow = (WebWindow) windowsOpened.get(0);
        assertEquals("New window message", "You made it!", openedWindow.getCurrentPage().getText());
    }


    @Test
    public void testWindowOpenNoContents() throws Exception {
        defineResource("OnCommand.html", "<html><head><title>Amazing!</title></head>" +
                "<body>" +
                "<a href='#' onClick=\"window.open( null, 'sample' );\">go</a>" +
                "</body></html>");
        final ArrayList windowsOpened = new ArrayList();
        WebConversation wc = new WebConversation();
        wc.addWindowListener(new WebWindowListener() {
            public void windowOpened(WebClient client, WebWindow window) {
                windowsOpened.add(window);
            }

            public void windowClosed(WebClient client, WebWindow window) {
            }
        });
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinks()[0].click();

        assertFalse("No window opened", windowsOpened.isEmpty());
        final WebWindow openedWindow = (WebWindow) windowsOpened.get(0);
        assertEquals("New window message", "", openedWindow.getCurrentPage().getText());
        assertEquals("New window name", "sample", openedWindow.getName());
        assertEquals("Window by name", openedWindow, wc.getOpenWindow("sample"));
    }


    @Test
    public void testWindowReopen() throws Exception {
        defineResource("Target.html", "You made it!");
        defineResource("Revise.html", "You changed it!");
        defineResource("OnCommand.html", "<html><head><title>Amazing!</title></head>" +
                "<body>" +
                "<a href='#' onClick=\"window.open( '" + getHostPath() + "/Target.html', 'sample' );\">go</a>" +
                "<a href='#' onClick=\"window.open( '" + getHostPath() + "/Revise.html', 'sample' );\">go</a>" +
                "</body></html>");
        final ArrayList windowsOpened = new ArrayList();
        WebConversation wc = new WebConversation();
        wc.addWindowListener(new WebWindowListener() {
            public void windowOpened(WebClient client, WebWindow window) {
                windowsOpened.add(window);
            }

            public void windowClosed(WebClient client, WebWindow window) {
            }
        });
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinks()[0].click();
        assertEquals("New window message", "You made it!", ((WebWindow) windowsOpened.get(0)).getCurrentPage().getText());
        response.getLinks()[1].click();

        assertEquals("Number of window openings", 1, windowsOpened.size());
        assertEquals("Changed window message", "You changed it!", ((WebWindow) windowsOpened.get(0)).getCurrentPage().getText());
    }


    @Test
    public void testOpenedWindowProperties() throws Exception {
        defineResource("Target.html", "<html><head><script language='JavaScript'>" +
                "function show_properties() {" +
                "   alert( 'name=' + window.name );" +
                "   alert( 'opener name=' + window.opener.name );" +
                "}" +
                "</script></head><body onload='show_properties()'>" +
                "</body></html>");
        defineResource("OnCommand.html", "<html><head><title>Amazing!</title></head>" +
                "<body onload=\"window.name='main'; alert ('opener ' + (window.opener ? 'found' : 'not defined') );\">" +
                "<a href='#' onClick=\"window.open( '" + getHostPath() + "/Target.html', 'sample' );\">go</a>" +
                "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals("main window name", "main", wc.getMainWindow().getName());
        assertEquals("main window alert", "opener not defined", wc.popNextAlert());
        response.getLinks()[0].click();

        assertEquals("1st alert", "name=sample", wc.popNextAlert());
        assertEquals("2nd alert", "opener name=main", wc.popNextAlert());
    }


    @Test
    public void testFrameProperties() throws Exception {
        HttpUnitOptions.setExceptionsThrownOnScriptError(false);
        defineWebPage("Linker", "This is a trivial page with <a href=Target.html>one link</a>");
        defineResource("Target.html", "<html><head><script language='JavaScript'>" +
                "function show_properties() {" +
                "   alert( 'name=' + window.name );" +
                "   alert( 'top url=' + window.top.location );" +
                "   alert( '1st frame=' + top.frames[0].name );" +
                "   alert( '2nd frame=' + window.parent.blue.name );" +
                "   alert( 'parent url=' + window.parent.location );" +
                "   alert( 'top.parent=' + top.parent.location );" +
                "   alert( 'indexed frame=' + top.frames['red'].name );" +
                "}" +
                "</script></head><body>" +
                "<a href=# onclick='show_properties()'>show</a>" +
                "</body></html>");
        defineWebPage("Form", "This is a page with a simple form: " +
                "<form action=submit><input name=name><input type=submit></form>" +
                "<a href=Linker.html target=red>a link</a>");
        defineResource("Frames.html",
                "<html><head><title>Initial</title></head>" +
                        "<frameset cols='20%,80%'>" +
                        "    <frame src='Linker.html' name='red'>" +
                        "    <frame src=Target.html name=blue>" +
                        "</frameset></html>");

        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/Frames.html");
        WebResponse blue = wc.getFrameContents("blue");
        blue.getLinkWith("show").click();

        assertEquals("1st alert", "name=blue", wc.popNextAlert());
        assertEquals("2nd alert", "top url=" + getHostPath() + "/Frames.html", wc.popNextAlert());
        assertEquals("3rd alert", "1st frame=red", wc.popNextAlert());
        assertEquals("4th alert", "2nd frame=blue", wc.popNextAlert());
        assertEquals("5th alert", "parent url=" + getHostPath() + "/Frames.html", wc.popNextAlert());
        assertEquals("6th alert", "top.parent=" + getHostPath() + "/Frames.html", wc.popNextAlert());
        assertEquals("7th alert", "indexed frame=red", wc.popNextAlert());
    }


    @Test
    public void testLocationProperty() throws Exception {
        defineResource("Target.html", "You made it!");
        defineResource("location.js", "function show() {" +
                "alert('Window location is ' + window.location);" +
                "alert('Document location is ' + document.location);" +
                "alert('Window location.href is ' + window.location.href);" +
                "}");
        defineResource("OnCommand.html", "<html><head><title>Amazing!</title>" +
                "<script language='JavaScript' src='location.js'></script>" +
                "</head>" +
                "<body onLoad='show()'>" +
                "<a href='#' onMouseOver=\"window.location='" + getHostPath() + "/Target.html';\">go</a>" +
                "<a href='#' onMouseOver=\"document.location='" + getHostPath() + "/Target.html';\">go</a>" +
                "<a href='#' onMouseOver=\"document.location.replace('" + getHostPath() + "/Target.html');\">go</a>" +
                "</body>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals("Alert message 1", "Window location is " + getHostPath() + "/OnCommand.html", wc.popNextAlert());
        assertEquals("Alert message 2", "Document location is " + getHostPath() + "/OnCommand.html", wc.popNextAlert());
        assertEquals("Alert message 3", "Window location.href is " + getHostPath() + "/OnCommand.html", wc.popNextAlert());
        response.getLinks()[0].mouseOver();
        assertEquals("2nd page URL", getHostPath() + "/Target.html", wc.getCurrentPage().getURL().toExternalForm());
        assertEquals("2nd page", "You made it!", wc.getCurrentPage().getText());

        response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinks()[1].mouseOver();
        assertEquals("3rd page URL", getHostPath() + "/Target.html", wc.getCurrentPage().getURL().toExternalForm());
        assertEquals("3rd page", "You made it!", wc.getCurrentPage().getText());

        response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinks()[2].mouseOver();
        assertEquals("4th page URL", getHostPath() + "/Target.html", wc.getCurrentPage().getURL().toExternalForm());
        assertEquals("4th page", "You made it!", wc.getCurrentPage().getText());

        response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getScriptingHandler().doEventScript("window.location.href='" + getHostPath() + "/Target.html'");
        assertEquals("5th page URL", getHostPath() + "/Target.html", wc.getCurrentPage().getURL().toExternalForm());
        assertEquals("5th page", "You made it!", wc.getCurrentPage().getText());
    }


    @Test
    public void testLocationPropertyOnLoad() throws Exception {
        defineResource("Target.html", "You made it!");
        defineResource("OnCommand.html", "<html><head><title>Amazing!</title>" +
                "</head>" +
                "<body onLoad=\"document.location='" + getHostPath() + "/Target.html';\">" +
                "</body>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals("current page URL", getHostPath() + "/Target.html", wc.getCurrentPage().getURL().toExternalForm());
        assertEquals("current page", "You made it!", wc.getCurrentPage().getText());
        assertEquals("returned page URL", getHostPath() + "/Target.html", response.getURL().toExternalForm());
        assertEquals("returned page", "You made it!", response.getText());
    }


    @Test
    public void testLocationReadableSubproperties() throws Exception {
        defineResource("Target.html", "You made it!");
        defineResource("location.js", "function show() {" +
                "alert('host is ' + window.location.host);" +
                "alert('hostname is ' + document.location.hostname);" +
                "alert('port is ' + window.location.port);" +
                "alert('pathname is ' + window.location.pathname);" +
                "alert('protocol is ' + document.location.protocol);" +
                "alert('search is ' + window.location.search);" +
                "}");
        defineResource("simple/OnCommand.html?point=center",
                "<html><head><title>Amazing!</title>" +
                        "<script language='JavaScript' src='/location.js'></script>" +
                        "</head>" +
                        "<body onLoad='show()'>" +
                        "</body>");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/simple/OnCommand.html?point=center");
        assertEquals("Alert message 1", "host is " + getHostPath().substring(7), wc.popNextAlert());
        assertEquals("Alert message 2", "hostname is localhost", wc.popNextAlert());
        assertEquals("Alert message 3", "port is " + getHostPort(), wc.popNextAlert());
        assertEquals("Alert message 4", "pathname is /simple/OnCommand.html", wc.popNextAlert());
        assertEquals("Alert message 5", "protocol is http:", wc.popNextAlert());
        assertEquals("Alert message 6", "search is ?point=center", wc.popNextAlert());
    }


    @Test
    public void testLocationWriteableSubproperties() throws Exception {
        defineResource("Target.html", "You made it!");
        defineResource("OnCommand.html?where=here", "You found it!");
        defineResource("OnCommand.html", "<html><head><title>Amazing!</title>" +
                "</head>" +
                "<body'>" +
                "<a href='#' onMouseOver=\"window.location.pathname='/Target.html';\">go</a>" +
                "<a href='#' onMouseOver=\"document.location.search='?where=here';\">go</a>" +
                "</body>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinks()[0].mouseOver();
        assertEquals("2nd page URL", getHostPath() + "/Target.html", wc.getCurrentPage().getURL().toExternalForm());
        assertEquals("2nd page", "You made it!", wc.getCurrentPage().getText());

        response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinks()[1].mouseOver();
        assertEquals("3rd page URL", getHostPath() + "/OnCommand.html?where=here", wc.getCurrentPage().getURL().toExternalForm());
        assertEquals("3rd page", "You found it!", wc.getCurrentPage().getText());
    }


    @Test
    public void testScriptDisabled() throws Exception {
        HttpUnitOptions.setScriptingEnabled(false);
        defineResource("nothing.html", "Should get here");
        defineResource("OnCommand.html", "<html><head></head>" +
                "<body>" +
                "<form name='realform'><input name='color' value='blue'></form>" +
                "<a href='nothing.html' onClick=\"document.realform.color.value='green';return false;\">green</a>" +
                "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        WebForm form = response.getFormWithName("realform");
        WebLink link = response.getLinks()[0];
        assertEquals("initial parameter value", "blue", form.getParameterValue("color"));
        link.click();
        assertEquals("unchanged parameter value", "blue", form.getParameterValue("color"));
        assertEquals("Expected result", "Should get here", wc.getCurrentPage().getText());
    }


    @Test
    public void testNavigatorObject() throws Exception {
        defineResource("OnCommand.html", "<html><head><script language='JavaScript'>" +
                "function viewProperties() { \n" +
                "  alert( 'appName=' + navigator.appName );\n" +
                "  alert( 'appCodeName=' + navigator.appCodeName )\n;" +
                "  alert( 'appVersion=' + navigator.appVersion )\n;" +
                "  alert( 'userAgent=' + navigator.userAgent )\n;" +
                "  alert( 'platform=' + navigator.platform )\n;" +
                "  alert( 'javaEnabled=' + navigator.javaEnabled() )\n;" +
                "  alert( '# plugins=' + navigator.plugins.length )\n;" +
                "}" +
                "</script></head>\n" +
                "<body onLoad='viewProperties()'>\n" +
                "</body></html>");
        HttpUnitOptions.setExceptionsThrownOnScriptError(true);
        WebConversation wc = new WebConversation();
        wc.getClientProperties().setApplicationID("Internet Explorer", "Mozilla", "4.0");
        wc.getClientProperties().setPlatform("JVM");
        wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals("Alert message 1", "appName=Internet Explorer", wc.popNextAlert());
        assertEquals("Alert message 2", "appCodeName=Mozilla", wc.popNextAlert());
        assertEquals("Alert message 3", "appVersion=4.0", wc.popNextAlert());
        assertEquals("Alert message 4", "userAgent=Mozilla/4.0", wc.popNextAlert());
        assertEquals("Alert message 5", "platform=JVM", wc.popNextAlert());
        assertEquals("Alert message 6", "javaEnabled=false", wc.popNextAlert());
        assertEquals("Alert message 7", "# plugins=0", wc.popNextAlert());
        assertNull("Alert should have been removed", wc.getNextAlert());
    }


    @Test
    public void testScreenObject() throws Exception {
        defineResource("OnCommand.html", "<html><head><script language='JavaScript'>" +
                "function viewProperties() { \n" +
                "  alert( 'dimensions=' + screen.availWidth + 'x' + screen.availHeight );\n" +
                "}" +
                "</script></head>\n" +
                "<body onLoad='viewProperties()'>\n" +
                "</body></html>");
        HttpUnitOptions.setExceptionsThrownOnScriptError(true);
        WebConversation wc = new WebConversation();
        wc.getClientProperties().setAvailableScreenSize(1024, 752);
        wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals("Alert message 1", "dimensions=1024x752", wc.popNextAlert());
        assertNull("Alert should have been removed", wc.getNextAlert());
    }


    @Test
    public void testStyleProperty() throws Exception {
        defineResource("start.html",
                "<html><head><script language='JavaScript'>" +
                        "function showDisplay( id ) {" +
                        "  var element = document.getElementById( id );\n" +
                        "  alert( 'element with id ' + id + ' has style.display ' + element.style.display );\n" +
                        "}\n" +
                        "function setDisplay( id, value ) {" +
                        "  var element = document.getElementById( id );\n" +
                        "  element.style.display = value;\n" +
                        "}\n" +
                        "function showVisibility( id ) {" +
                        "  var element = document.getElementById( id );\n" +
                        "  alert( 'element with id ' + id + ' has style.visibility ' + element.style.visibility );\n" +
                        "}\n" +
                        "function setVisibility( id, value ) {" +
                        "  var element = document.getElementById( id );\n" +
                        "  element.style.visibility = value;\n" +
                        "}\n" +
                        "function doAll() {\n" +
                        "  setDisplay('test','inline'); \n" +
                        "  showDisplay('test');\n" +
                        "  setDisplay('test','block'); \n" +
                        "  showDisplay('test');\n" +
                        "  setVisibility('test','hidden'); \n" +
                        "  showVisibility('test');\n" +
                        "  setVisibility('test','visible'); \n" +
                        "  showVisibility('test');\n" +
                        "}\n" +
                        "</script>" +
                        "</head><body onLoad='doAll();'>" +
                        "<div id='test'>foo</div></body></html>");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/start.html");

        assertEquals("element with id test has style.display inline", wc.popNextAlert());
        assertEquals("element with id test has style.display block", wc.popNextAlert());
        assertEquals("element with id test has style.visibility hidden", wc.popNextAlert());
        assertEquals("element with id test has style.visibility visible", wc.popNextAlert());
    }

    /**
     * test for Patch proposal 1653410
     * Date: 2008-01-08 15:49
     *
     * @author Mattias Jiderhamn (mattias78)
     */
    @Test
    public void testSetAttribute() throws Exception {
        /*
           *
          A minimal snippet:

              <input type="text" id="foo" name="foo" myattr="bar" />
              ...
              var field = document.getElementById("foo");
              var attributeValue = field.getAttribute("myattr");
              alert("The attribute value is " + attributeValue);
              field.setAttribute("myattr", "new_attribute_value");
        */
        // will only work with Dom based scripting engine before patch
        // needs addCustomAttribute for old scriptin engine
        if (HttpUnitOptions.DEFAULT_SCRIPT_ENGINE_FACTORY.equals(HttpUnitOptions.ORIGINAL_SCRIPTING_ENGINE_FACTORY)) {
            HttpUnitOptions.addCustomAttribute("myattr");
        }
        defineResource("start.html",
                "<html><head>\n" +
                        "<script language='JavaScript'>\n" +
                        "function testAttributes() {\n" +
                        "var field = document.getElementById(\"foo\");" +
                        "var attributeValue = field.getAttribute(\"myattr\");" +
                        "alert('The attribute value is ' + attributeValue);\n" +
                        "field.setAttribute(\"myattr\", \"newValue\");\n" +
                        "alert('The attribute value is changed to ' + field.getAttribute('myattr'));\n" +
                        "}\n" +
                        "</script>\n" +
                        "</head>\n" +
                        "<body id='body_id' onLoad='testAttributes();'>" +
                        "<form name='the_form'><input type=\"text\" id=\"foo\" name=\"foo\" myattr=\"bar\" /></form>" +
                        "</body></html");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/start.html");
        assertEquals("The attribute value is bar", wc.popNextAlert());
        assertEquals("The attribute value is changed to newValue", wc.popNextAlert());
        // } // if

    }

    /**
     * test for onChange part of Patch proposal 1653410
     * calling on change from javascript
     * used to throw com.meterware.httpunit.ScriptException: Event 'callonChange();' failed: org.mozilla.javascript.EcmaError: TypeError: Cannot find function onChange. (httpunit#6)
     * after patch
     *
     * @throws Exception
     */
    @Test
    public void testCallOnChange() throws Exception {
        defineResource("start.html",
                "<html><head>\n" +
                        "<script language='JavaScript'>\n" +
                        "function onChangeHandler() {\n" +
                        "alert('onChange has been called');\n" +
                        "}\n" +
                        "function callonChange() {\n" +
                        "alert('calling onChange');\n" +
                        "// fire onChangeHandler directly\n" +
                        "onChangeHandler();\n" +
                        "var field = document.getElementById(\"foo\");\n" +
                        "// fire onChangeHandler indirectly via event\n" +
                        "field.onchange();\n" +
                        "}\n" +
                        "</script>\n" +
                        "</head>\n" +
                        "<body id='body_id' onLoad='callonChange();'>" +
                        "<form name='the_form'><input type=\"text\" onchange='onChangeHandler' id=\"foo\" name=\"foo\" /></form>" +
                        "</body></html");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/start.html");
        String firstAlert = wc.popNextAlert();
        assertEquals("calling onChange", firstAlert);
        String secondAlert = wc.popNextAlert();
        assertEquals("2nd", "onChange has been called", secondAlert);
        String thirdAlert = wc.popNextAlert();
        // TODO make this work
        // assertEquals("3rd","onChange has been called",thirdAlert);
    }

    /**
     * test for window event part of Patch proposal 1653410
     *
     * @throws Exception
     */
    @Test
    public void testWindowEvent() throws Exception {
        defineWebPage("OnCommand",
                "<html><head>\n" +
                        "<script language='JavaScript'>\n" +
                        "function buttonclick() {\n" +
                        "alert('hi');\n" +
                        "var event=window.event;\n" +
                        "}\n" +
                        "</script>\n" +
                        "</head><body onload='buttonclick'>\n" +
                        "<form id='someform'><input type='button' id='button1' onClick='buttonclick'></input></form>\n" +
                        "</body></html");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        Button button1 = (Button) response.getElementWithID("button1");
        button1.click();
        // TODO make this work
        // assertEquals( "Alert message 1", "hi", wc.popNextAlert() );
    }


    @Test
    public void testTagNameNodeNameProperties() throws Exception {
        defineResource("start.html",
                "<html><head><script language='JavaScript'>\n" +
                        "function showTagName(id) {\n" +
                        "  var element = document.getElementById( id );\n" +
                        "  alert( 'element id=' + id + ', tagName='  + element.tagName + ', nodeName='  + element.nodeName );\n" +
                        "}\n" +
                        "function doAll() {\n" +
                        "  showTagName('body_id')\n" +
                        "  showTagName('iframe_id')\n" +
                        "  showTagName('div_id')\n" +
                        "}\n" +
                        "</script>\n" +
                        "</head><body id='body_id' onLoad='doAll();'>\n" +
                        "<div id='div_id'><iframe id='iframe_id' /></div>\n" +
                        "</body></html>");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/start.html");

        assertEquals("element id=body_id, tagName=BODY, nodeName=BODY", wc.popNextAlert());
        assertEquals("element id=iframe_id, tagName=IFRAME, nodeName=IFRAME", wc.popNextAlert());
        assertEquals("element id=div_id, tagName=DIV, nodeName=DIV", wc.popNextAlert());
    }


    @Test
    public void testReadNoCookie() throws Exception {
        defineResource("OnCommand.html", "<html><head><script language='JavaScript'>" +
                "function viewCookies() { \n" +
                "  alert( 'cookies: ' + document.cookie );\n" +
                "}" +
                "</script></head>\n" +
                "<body onLoad='viewCookies()'>\n" +
                "</body></html>");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals("Alert message 1", "cookies: ", wc.popNextAlert());
        assertNull("Alert should have been removed", wc.getNextAlert());
    }


    @Test
    public void testSimpleSetCookie() throws Exception {
        defineResource("OnCommand.html", "<html><head></head>\n" +
                "<body onLoad='document.cookie=\"color=red;path=/\"'>\n" +
                "</body></html>");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals("Cookie 'color'", "red", wc.getCookieValue("color"));
    }


    @Test
    public void testSetCookieToNull() throws Exception {
        defineResource("OnCommand.html", "<html><script>" +
                "document.cookie = null;" +
                "</script></html>");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/OnCommand.html");
    }


    @Test
    public void testReadCookies() throws Exception {
        defineResource("OnCommand.html", "<html><head><script language='JavaScript'>" +
                "function viewCookies() { \n" +
                "  alert( 'cookies: ' + document.cookie );\n" +
                "}" +
                "</script></head>\n" +
                "<body onLoad='viewCookies()'>\n" +
                "</body></html>");
        addResourceHeader("OnCommand.html", "Set-Cookie: age=12");
        WebConversation wc = new WebConversation();
        wc.putCookie("height", "tall");
        wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals("Alert message 1", "cookies: age=12; height=tall", wc.popNextAlert());
        assertNull("Alert should have been removed", wc.getNextAlert());
    }


    @Test
    public void testButtonWithoutForm() throws Exception {
        defineWebPage("OnCommand", "<button id='mybutton' onclick='alert( \"I heard you!\" )'>" +
                "<input id='yourbutton' type='button'  onclick='alert( \"Loud and Clear.\" )'>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        ((Button) response.getElementWithID("mybutton")).click();
        assertEquals("Alert message 1", "I heard you!", wc.popNextAlert());

        ((Button) response.getElementWithID("yourbutton")).click();
        assertEquals("Alert message 2", "Loud and Clear.", wc.popNextAlert());
    }


    /**
     * test the trick for detecting java script enabled
     *
     * @throws Exception
     */
    // TODO JWL 6/26/2021 Nekohtml patch is against 'body' not the 'header' so issue was never fixed.
    @Ignore
    @Test
    public void testJavascriptDetectionTrick() throws Exception {
        defineResource("NoScript.html", "No javascript here");
        defineResource("HasScript.html", "Javascript is enabled!");
        defineResource("Start.html", "<html><head>" +
                "  <noscript>" +
                "      <meta http-equiv='refresh' content='0;url=NoScript.html'>" +
                "  </noscript>" +
                "</head>" +
                "<body onload='document.form.submit()'>" +
                "<form name='form' action='HasScript.html'></form>" +
                "</body></html>");
        WebConversation wc = new WebConversation();
        wc.getClientProperties().setAutoRefresh(true);
        WebResponse response = wc.getResponse(getHostPath() + "/Start.html");
        assertEquals("Result page ", "Javascript is enabled!", response.getText());
        HttpUnitOptions.setScriptingEnabled(false);
        response = wc.getResponse(getHostPath() + "/Start.html");
        assertEquals("Result page", "No javascript here", response.getText());
    }

    /**
     * https://sourceforge.net/forum/forum.php?thread_id=1808696&forum_id=20294
     * by kauffman81
     */
    @Test
    public void testJavaScriptConfirmPopUp() throws Exception {
        String target = "<html><body>After click we want to see this!</body></html>";
        defineResource("Target.html", target);
        defineResource("Popup.html", "<html><head><script language='JavaScript'>" +
                "// 	This is the javascript that handles the onclick event\n" +
                "function verify_onBorrar(form){\n" +
                "  alert(form.id);\n" +
                /* TODO check this javascript code
                 * if uncommented it will throw
                   com.meterware.httpunit.ScriptException: Event 'verify_onBorrar(this.form)' failed: org.mozilla.javascript.EcmaError: TypeError: Cannot read property "0" from undefined (httpunit#3)
                "	for(var i = 0;i<form.selection[i].length;i++){\n"+
                "		if(form.selection[i].checked){\n"+
                "			if(confirm('blablabla')){\n"+
                "				form.action = 'Target.html';\n"+
                "				form.submit(); \n"+
                "			} // if\n"+
                "		} // if\n"+
                "	} // for\n"+
                */
                "} // verify_onBorrar\n" +
                "</script></head>\n" +
                "<body>\n" +
                "	<form id='someform' name='someform'>" +
                "		<input type='button' id='button1' class='button' value='say hi' onclick=\"alert('hi')\"/>" +
                "		<input type='button' id='delete' class='button' value='delete' onclick='verify_onBorrar(this.form)'/></form>\n" +
                "	</form>\n" +
                "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/Popup.html");
        Button button1 = (Button) response.getElementWithID("button1");
        button1.click();
        String alert1 = wc.popNextAlert();
        assertEquals("hi", alert1);
        Button button2 = (Button) response.getElementWithID("delete");
        button2.click();
        String alert2 = wc.popNextAlert();
        // TODO activate this check
        // System.err.println("alert 2 is "+alert2);
        // assertEquals("someform",alert2);
    }

    /**
     * test for function in external javascript
     * https://sourceforge.net/forum/forum.php?thread_id=1406498&forum_id=20294
     *
     * @throws Exception
     */
    // TODO JWL 7/6/2021 Breaks with nekohtml > 1.9.6.2
    @Ignore
    @Test
    public void testJavaScriptFromSource() throws Exception {
        defineResource("someScript.js", "function someFunction() {\n" +
                "	alert('somefunction called')" +
                "}\n");
        defineResource("Script.html", "<html><head>\n" +
                "<script language='JavaScript' src='someScript.js' />\n" +
                "<script language='JavaScript'>\n" +
                "function testFunction() {\n" +
                "	var retValue = someFunction(); //Here some function is part of SomeScript.js\n" +
                "}\n" +
                "</script></head><body onload='testFunction()'></body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/Script.html");
        // com.meterware.httpunit.ScriptException: Event 'testFunction()' failed: org.mozilla.javascript.EcmaError: ReferenceError: "someFunction" is not defined. (httpunit#1)
        String alert1 = wc.popNextAlert();
        assertEquals("somefunction called", alert1);
    }

    /**
     * test for bug report [ 1396835 ] Javascript : length of a select element cannot be increased
     * by gklopp
     * used to  throw java.lang.RuntimeException: Script 'fillSelect();' failed: java.lang.RuntimeException: invalid index 1 for Options option1
     * at com.meterware.httpunit.javascript.ScriptingEngineImpl.handleScriptException(ScriptingEngineImpl.java:61)
     *
     * @throws Exception
     */
    @Test
    public void testFillSelect() throws Exception {
        defineResource("testSelect.html", "<html><head><script type='text/javascript'>\n" +
                "<!--\n" +
                "function fillSelect() {\n" +
                "   document.the_form.the_select.options.length = 2;\n" +
                "   document.the_form.the_select.options[1].text = 'option2';\n " +
                "   document.the_form.the_select.options[1].value = 'option2Value';\n " +
                "}\n" +
                "-->\n" +
                "</script></head>" +
                "<body>" +
                "<form name='the_form'>" +
                "   <table>" +
                "    <tr>" +
                "      <td>Selection :</td>" +
                "       <td>" +
                "          <select name='the_select'>" +
                "              <option value='option1Value'>option1</option>" +
                "          </select>" +
                "       </td>" +
                "     </tr>" +
                "   </table>" +
                "</form>" +
                "<script type='text/javascript'>fillSelect();</script>" +
                "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/testSelect.html");
        final WebForm form = response.getFormWithName("the_form");
    }


    /**
     * test for bug report [ 1396896 ] Javascript: length property of a select element not writable
     * by gklopp
     * used to throw
     * java.lang.RuntimeException: Script 'modifySelectLength();' failed: java.lang.RuntimeException: No such property: length
     *
     * @throws Exception
     */
    @Test
    public void testModifySelectLength() throws Exception {
        defineResource("testModifySelectLength.html",
                "<html><head><script type='text/javascript'>\n" +
                        "<!--\n" +
                        "function modifySelectLength() {\n" +
                        "   document.the_form.the_select.length = 2;\n" +
                        "   document.the_form.the_select.options[1].text = 'option2';\n " +
                        "   document.the_form.the_select.options[1].value = 'option2Value';\n " +
                        "}\n" +
                        "-->\n" +
                        "</script></head>" +
                        "<body>" +
                        "<form name='the_form'>" +
                        "   <table>" +
                        "    <tr>" +
                        "      <td>Selection :</td>" +
                        "       <td>" +
                        "          <select name='the_select'>" +
                        "              <option value='option1Value'>option1</option>" +
                        "          </select>" +
                        "       </td>" +
                        "     </tr>" +
                        "   </table>" +
                        "</form>" +
                        "<script type='text/javascript'>modifySelectLength();</script>" +
                        "</body></html>");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/testModifySelectLength.html");

    }

}

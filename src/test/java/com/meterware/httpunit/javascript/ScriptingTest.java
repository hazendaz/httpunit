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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.meterware.httpunit.Button;
import com.meterware.httpunit.DialogAdapter;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.HttpUnitUtils;
import com.meterware.httpunit.ScriptException;
import com.meterware.httpunit.WebClient;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebWindow;
import com.meterware.httpunit.WebWindowListener;

import java.util.ArrayList;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.migrationsupport.rules.ExternalResourceSupport;

@ExtendWith(ExternalResourceSupport.class)
class ScriptingTest extends AbstractJavaScriptTest {

    @Test
    void javaScriptURLWithValue() throws Exception {
        defineResource("OnCommand.html",
                "<html><head></head>" + "<body>" + "<a href='JavaScript:\"You made it!\"'>go</a>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinks()[0].click();
        assertEquals("You made it!", wc.getCurrentPage().getText(), "New page");
        assertEquals("javascript:\"You made it!\"", wc.getCurrentPage().getURL().toExternalForm(), "New URL");
    }

    @Test
    void javaScriptURLWithNoValue() throws Exception {
        defineResource("OnCommand.html", "<html><head></head>" + "<body>"
                + "<a href=\"javascript:alert( 'Hi there!' )\">go</a>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        WebResponse myPage = response.getLinks()[0].click();
        assertEquals("Hi there!", wc.popNextAlert(), "Alert message");
        assertEquals(getHostPath() + "/OnCommand.html", wc.getCurrentPage().getURL().toExternalForm(),
                "Current page URL");
        assertEquals(getHostPath() + "/OnCommand.html", myPage.getURL().toExternalForm(), "Returned page URL");
    }

    @Test
    void initialJavaScriptURL() throws Exception {
        WebConversation wc = new WebConversation();
        GetMethodWebRequest request = new GetMethodWebRequest("javascript:alert( 'Hi there!' )");
        assertEquals("javascript:alert( 'Hi there!' )", request.getURL().toExternalForm(), "Javascript URL");
        wc.getResponse(request);
        assertEquals("Hi there!", wc.popNextAlert(), "Alert message");
    }

    @Test
    void javaScriptURLWithVariables() throws Exception {
        defineResource("OnCommand.html",
                "<html><head></head>" + "<body>"
                        + "<a href='javascript:\"Our winner is... \" + document.the_form.winner.value'>go</a>"
                        + "<form name='the_form'>" + "  <input name=winner type=text value='George of the Jungle'>"
                        + "</form></body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinks()[0].click();
        assertEquals("Our winner is... George of the Jungle", wc.getCurrentPage().getText(), "New page");
    }

    @Test
    void javaScriptURLWithQuestionMark() throws Exception {
        defineResource("/appname/HandleAction/report?type=C", "You made it!");
        defineResource("OnCommand.html",
                "<html><head></head>" + "<body>"
                        + "<a href=\"javascript:redirect('/appname/HandleAction/report?type=C')\">go</a>"
                        + "<script language='JavaScript'>" + "  function redirect( url ) { window.location=url; }"
                        + "</script>" + "</form></body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinks()[0].click();
        assertEquals("You made it!", wc.getCurrentPage().getText(), "New page");
    }

    /**
     * test for bug report [ 1508516 ] Javascript method: "undefined" is not supported
     *
     * @throws Exception
     */
    @Test
    void undefined() throws Exception {
        WebConversation wc = doTestJavaScript(
                "if (typeof(xyzDefinitelyNotDefined) == 'undefined') {\n" + "alert ('blabla');\n" + "return;\n" + "}");
        assertEquals("blabla", wc.popNextAlert(), "Alert message");
    }

    /**
     * test for bug report [ 1153066 ] Eternal loop while processing javascript by Serguei Khramtchenko 2005-02-27
     *
     * @throws Exception
     */
    @Test
    void avoidEndlessLoop() throws Exception {
        assertDoesNotThrow(() -> {
            doTestJavaScript("document.location='#node_selected';");
        });
    }

    /**
     * test javascript call to an included function
     *
     * @throws Exception
     */
    @Test
    void javaScriptURLWithIncludedFunction() throws Exception {
        defineResource("saycheese.js", "function sayCheese() { alert( \"Cheese!\" ); }");
        defineResource("OnCommand.html", "<html><head><script language='JavaScript' src='saycheese.js'>"
                + "</script></head>" + "<body>" + "<a href=\"javascript:sayCheese()\">go</a>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinkWith("go").click();
        assertEquals("Cheese!", wc.popNextAlert(), "Alert message");
    }

    /**
     * test javascript call to built-in functions e.g. toLowerCase
     */
    @Test
    void javaScriptWitBuiltInFunctions() throws Exception {
        defineResource("OnCommand.html",
                "<html>" + "<body>" + "<a href=\"javascript:alert(toLowerCase('Cheese!'))\">go</a>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinkWith("go").click();
        assertEquals("cheese!", wc.popNextAlert(), "Alert message");
    }

    /**
     * test javascript call to an included function
     *
     * @throws Exception
     */
    @Test
    void javaScriptURLWithIncludedFunction2() throws Exception {
        defineResource("saycheese.js", "function sayCheese() { alert( \"Cheese!\" ); }");
        defineResource("callcheese.js", "function callCheese() { sayCheese(); }");
        defineResource("OnCommand.html",
                "<html><head>\n" + "<script language='JavaScript' src='saycheese.js'></script>\n"
                        + "<script language='JavaScript' src='callcheese.js'></script>\n" + "</head><body>"
                        + "	<a href=\"javascript:callCheese()\">go</a>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinkWith("go").click();
        assertEquals("Cheese!", wc.popNextAlert(), "Alert message");
    }

    /**
     * test Detection of Javascript files that can not be found behaviour pointed out by Dan Lipofsky
     *
     * @throws Exception
     */
    @Test
    void badJavascriptFile() {
        // define xyz.js to create a 404 error
        // we don't do this - it should be a default behaviour of the Pseudo Server!
        // defineResource( "xyz.js", "File does not exist: xyz.js", 404);
        defineResource("OnCommand.html", "<html><head>" + "<script language='JavaScript' src='xyz.js'></script></head>"
                + "<body>Hello</body></html>");
        boolean originalState = HttpUnitOptions.getExceptionsThrownOnErrorStatus();
        boolean originalScriptState = HttpUnitOptions.getExceptionsThrownOnScriptError();
        // make sure stackTraces are not printed on Exceptions
        // uncomment this if you'd actually like to debug the following code
        boolean oldDebug = HttpUnitUtils.setEXCEPTION_DEBUG(false);
        AssertionError failure = null;
        // check 4 combinations of Exception and ScriptError status flags
        for (int i = 0; i < 4; i++) {
            boolean throwScriptException = i % 2 == 0; // true on case 0 and 2
            boolean throwException = i / 2 % 2 == 0; // true on case 0 and 1
            String testDescription = "case " + i + " throwScriptException=" + throwScriptException + " throwException="
                    + throwException;
            HttpUnitOptions.setExceptionsThrownOnErrorStatus(throwException);
            HttpUnitOptions.setExceptionsThrownOnScriptError(throwScriptException);
            HttpUnitOptions.clearScriptErrorMessages();
            WebConversation wc = new WebConversation();
            try {
                wc.getResponse(getHostPath() + "/OnCommand.html");
                // WebResponse response = wc.getResponse( getHostPath() + "/xyz.js" );
                // assertEquals( 404, response.getResponseCode() );
                if (throwScriptException) {
                    fail("there should have been an exception");
                } else {
                    String[] errMsgs = HttpUnitOptions.getScriptErrorMessages();
                    assertEquals(1, errMsgs.length, "There should be an error Message");
                    String errMsg = errMsgs[0];
                    assertEquals("reponseCode 404 on getIncludedScript for src='xyz.js'", errMsg, testDescription);
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
        if (failure != null) {
            throw failure;
        }
        // Restore exceptions state
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(originalState);
        HttpUnitOptions.setExceptionsThrownOnScriptError(originalScriptState);
        HttpUnitUtils.setEXCEPTION_DEBUG(oldDebug);
    }

    @Test
    void javaScriptURLInNewWindow() throws Exception {
        defineWebPage("OnCommand", "<input type='button' id='nowindow' onClick='alert(\"hi\")'></input>\n"
                + "<input type='button' id='withwindow' onClick=\"window.open('javascript:alert(\\'hi\\')','_self')\"></input>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        Button button1 = (Button) response.getElementWithID("nowindow");
        Button button2 = (Button) response.getElementWithID("withwindow");
        button1.click();
        assertEquals("hi", wc.popNextAlert(), "Alert message 1");
        button2.click();
        assertEquals("hi", wc.popNextAlert(), "Alert message 2");
    }

    @Test
    void singleCommandOnLoad() throws Exception {
        defineResource("OnCommand.html", "<html><head></head>" + "<body onLoad='alert(\"Ouch!\")'></body>");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/OnCommand.html");
        assertNotNull(wc.getNextAlert(), "No alert detected");
        assertEquals("Ouch!", wc.popNextAlert(), "Alert message");
        assertNull(wc.getNextAlert(), "Alert should have been removed");
    }

    /**
     * test for bug report [ 1161922 ] setting window.onload has no effect by Kent Tong
     *
     * @throws Exception
     */
    @Test
    void windowOnload() throws Exception {
        String html = "<html>\n" + "<body>\n" + "<script language='JavaScript'><!--\n" + "function foo(text) {\n"
                + "alert(text);\n" + "}\n" + "window.onload = foo('windowload');\n" + "// --></script>\n" + "<form>\n"
                + "<input type='Submit' name='OK' value='OK'/>\n" + "<a href=\"JavaScript:foo('click')\">go</a>"
                + "</form>\n" + "</body>\n" + "</html>\n";
        defineResource("OnCommand.html", html);
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        assertNotNull(wc.getNextAlert(), "No alert detected");
        assertEquals("windowload", wc.popNextAlert(), "Alert message");
        response.getLinks()[0].click();
        assertEquals("click", wc.popNextAlert(), "Alert message");
    }

    /**
     * check that setExceptionsThrownOnScriptError can be set to false by trying onLoad with an undefined function
     *
     * @throws Exception
     */
    @Test
    void onLoadErrorBypass() throws Exception {
        defineResource("OnCommand.html",
                "<html><head></head>" + "<body onLoad='noSuchFunction()'>" + "<img src=sample.jpg>" + "</body>");
        WebConversation wc = new WebConversation();
        HttpUnitOptions.setExceptionsThrownOnScriptError(false);
        HttpUnitOptions.clearScriptErrorMessages();

        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals(1, response.getImages().length, "Number of images on page");
        assertEquals(1, HttpUnitOptions.getScriptErrorMessages().length, "Number of script failures logged");
    }

    /**
     * test for bug[ 1055450 ] Error loading included script aborts entire request by Renaud Waldura
     */
    @Test
    void includeErrorBypass() throws Exception {
        defineResource("OnBypassCommand.html", "<html><head><script language='JavaScript' src='missingScript.js'>"
                + "</script></head>" + "<body>" + "<a href=\"javascript:sayCheese()\">go</a>" + "</body></html>");
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
            // java.lang.RuntimeException: Error clicking link: com.meterware.httpunit.ScriptException: URL
            // 'javascript:sayCheese()' failed: org.mozilla.javascript.EcmaError: ReferenceError: "sayCheese" is not
            // defined.
            assertTrue(rte.getMessage().indexOf("not defined") > 0, "is not defined should be found in message");
        } finally {
            HttpUnitUtils.setEXCEPTION_DEBUG(oldDebug);
        }

        HttpUnitOptions.setExceptionsThrownOnScriptError(false);
        HttpUnitOptions.clearScriptErrorMessages();
        response.getLinkWith("go").click();
        String[] messages = HttpUnitOptions.getScriptErrorMessages();
        assertEquals(1, messages.length, "there should be one message");
        String message = messages[0];
        assertTrue(message.indexOf("is not defined") > 0, "is not defined should be found");
    }

    @Test
    void confirmationDialog() throws Exception {
        defineWebPage("OnCommand", "<a href='NextPage' id='go' onClick='return confirm( \"go on?\" );'>");
        defineResource("NextPage", "Got the next page!");

        WebConversation wc = new WebConversation();
        WebResponse wr = wc.getResponse(getHostPath() + "/OnCommand.html");
        wc.setDialogResponder(new DialogAdapter() {
            @Override
            public boolean getConfirmation(String confirmationPrompt) {
                assertEquals("go on?", confirmationPrompt, "Confirmation prompt");
                return false;
            }
        });
        wr.getLinkWithID("go").click();
        assertEquals(wr, wc.getCurrentPage(), "Current page");
        wc.setDialogResponder(new DialogAdapter());
        wr.getLinkWithID("go").click();
        assertEquals("Got the next page!", wc.getCurrentPage().getText(), "Page after confirmation");
    }

    @Test
    void promptDialog() throws Exception {
        defineWebPage("OnCommand",
                "<a href='NextPage' id='go' onClick='return \"yes\" == prompt( \"go on?\", \"no\" );'>");
        defineResource("NextPage", "Got the next page!");

        WebConversation wc = new WebConversation();
        WebResponse wr = wc.getResponse(getHostPath() + "/OnCommand.html");
        wr.getLinkWithID("go").click();
        assertEquals(wr, wc.getCurrentPage(), "Current page");

        wc.setDialogResponder(new DialogAdapter() {
            @Override
            public String getUserResponse(String prompt, String defaultResponse) {
                assertEquals("go on?", prompt, "Confirmation prompt");
                assertEquals("no", defaultResponse, "Default response");
                return "yes";
            }
        });
        wr.getLinkWithID("go").click();
        assertEquals("Got the next page!", wc.getCurrentPage().getText(), "Page after confirmation");
    }

    @Test
    void functionCallOnLoad() throws Exception {
        defineResource("OnCommand.html",
                "<html><head><script language='JavaScript'>" + "<!-- hide this\n"
                        + "function sayCheese() { alert( \"Cheese!\" ); }" + "// end hiding -->\n" + "</script></head>"
                        + "<body'><script language='JavaScript'>\n" + "<!-- hide this\n" + "sayCheese();" + "-->"
                        + "</script></body></html>");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals("Cheese!", wc.popNextAlert(), "Alert message");
    }

    @Test
    void comment() throws Exception {
        assertDoesNotThrow(() -> {
            defineResource("OnCommand.html",
                    "<html><head><script language='JavaScript'><!--" + "//--></script><script language='JavaScript'>"
                            + "\n" + "var n=0;" + "\n" + "parseInt(n,32);" + "</script></head></html>");
            WebConversation wc = new WebConversation();
            wc.getResponse(getHostPath() + "/OnCommand.html");
        });
    }

    @Test
    void includedFunction() throws Exception {
        defineResource("saycheese.js", "function sayCheese() { alert( \"Cheese!\" ); }");
        defineResource("OnCommand.html", "<html><head><script language='JavaScript' src='saycheese.js'>"
                + "</script></head>" + "<body onLoad='sayCheese()'></body>");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals("Cheese!", wc.popNextAlert(), "Alert message");
    }

    @Test
    void includedFunctionWithBaseTag() throws Exception {
        defineResource("scripts/saycheese.js", "function sayCheese() { alert( \"Cheese!\" ); }");
        defineResource("OnCommand.html",
                "<html><head><base href='" + getHostPath()
                        + "/scripts/OnCommand.html'><script language='JavaScript' src='saycheese.js'>"
                        + "</script></head>" + "<body onLoad='sayCheese()'></body>");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals("Cheese!", wc.popNextAlert(), "Alert message");
    }

    @Test
    void windowOpen() throws Exception {
        defineResource("Target.txt", "You made it!", "text/plain");
        defineResource("OnCommand.html", "<html><head><title>Amazing!</title></head>"
                + "<body><script language='JavaScript'>var otherWindow;</script>"
                + "<a href='#' onClick=\"otherWindow = window.open( '" + getHostPath()
                + "/Target.txt', 'sample' );\">go</a>" + "<a href='#' onClick=\"otherWindow.close();\">go</a>"
                + "<a href='#' onClick=\"alert( 'window is ' + (otherWindow.closed ? '' : 'not ') + 'closed' );\">go</a>"
                + "</body></html>");
        final ArrayList windowsOpened = new ArrayList<>();
        WebConversation wc = new WebConversation();
        wc.addWindowListener(new WebWindowListener() {
            @Override
            public void windowOpened(WebClient client, WebWindow window) {
                windowsOpened.add(window);
            }

            @Override
            public void windowClosed(WebClient client, WebWindow window) {
            }
        });
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinks()[0].click();

        assertFalse(windowsOpened.isEmpty(), "No window opened");
        final WebWindow openedWindow = (WebWindow) windowsOpened.get(0);
        assertEquals("You made it!", openedWindow.getCurrentPage().getText(), "New window message");
        assertEquals("sample", openedWindow.getName(), "New window name");
        response.getLinks()[2].click();
        assertEquals("window is not closed", wc.popNextAlert(), "Alert message");
        response.getLinks()[1].click();
        assertTrue(openedWindow.isClosed(), "Window was not closed");
        response.getLinks()[2].click();
        assertEquals("window is closed", wc.popNextAlert(), "Alert message");
    }

    @Test
    void windowOpenWithEmptyName() throws Exception {
        defineResource("Target.txt", "You made it!", "text/plain");
        defineResource("OnCommand.html", "<html><head><title>Amazing!</title></head>"
                + "<body><script language='JavaScript'>var otherWindow;</script>"
                + "<a href='#' onClick=\"otherWindow = window.open( '" + getHostPath() + "/Target.txt', '' );\">go</a>"
                + "<a href='#' onClick=\"otherWindow.close();\">go</a>"
                + "<a href='#' onClick=\"alert( 'window is ' + (otherWindow.closed ? '' : 'not ') + 'closed' );\">go</a>"
                + "</body></html>");
        final ArrayList windowsOpened = new ArrayList<>();
        WebConversation wc = new WebConversation();
        wc.addWindowListener(new WebWindowListener() {
            @Override
            public void windowOpened(WebClient client, WebWindow window) {
                windowsOpened.add(window);
            }

            @Override
            public void windowClosed(WebClient client, WebWindow window) {
            }
        });
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinks()[0].click();

        assertFalse(windowsOpened.isEmpty(), "No window opened");
        final WebWindow openedWindow = (WebWindow) windowsOpened.get(0);
        assertEquals("You made it!", openedWindow.getCurrentPage().getText(), "New window message");
        assertEquals("", openedWindow.getName(), "New window name");
        response.getLinks()[2].click();
        assertEquals("window is not closed", wc.popNextAlert(), "Alert message");
        response.getLinks()[1].click();
        assertTrue(openedWindow.isClosed(), "Window was not closed");
        response.getLinks()[2].click();
        assertEquals("window is closed", wc.popNextAlert(), "Alert message");
    }

    @Test
    void windowOpenWithSelf() throws Exception {
        defineResource("Target.txt", "You made it!", "text/plain");
        defineResource("OnCommand.html", "<html><head><title>Amazing!</title></head>"
                + "<body><script language='JavaScript'>var otherWindow;</script>"
                + "<a href='#' onClick=\"otherWindow = window.open( '" + getHostPath()
                + "/Target.txt', '_self' );\">go</a>" + "<a href='#' onClick=\"otherWindow.close();\">go</a>"
                + "<a href='#' onClick=\"alert( 'window is ' + (otherWindow.closed ? '' : 'not ') + 'closed' );\">go</a>"
                + "</body></html>");
        final ArrayList windowsOpened = new ArrayList<>();
        WebConversation wc = new WebConversation();
        wc.addWindowListener(new WebWindowListener() {
            @Override
            public void windowOpened(WebClient client, WebWindow window) {
                windowsOpened.add(window);
            }

            @Override
            public void windowClosed(WebClient client, WebWindow window) {
            }
        });
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinks()[0].click();

        assertTrue(windowsOpened.isEmpty(), "Opened a new window");
        assertEquals("You made it!", wc.getCurrentPage().getText(), "New window message");
        assertEquals(1, wc.getOpenWindows().length, "Number of open windows");
    }

    @Test
    void javascriptURLWithFragment() throws Exception {
        defineResource("Target.txt", "You made it!", "text/plain");
        defineResource("OnCommand.html",
                "<html><head><title>Amazing!</title></head>"
                        + "<body><script language='JavaScript'>function newWindow(hrefTarget) {"
                        + "      window.open(hrefTarget);" + "}</script>" + "<a href='javascript:newWindow( \""
                        + getHostPath() + "/Target.txt#middle\" );'>go</a>" + "</body></html>");
        final ArrayList windowsOpened = new ArrayList<>();
        WebConversation wc = new WebConversation();
        wc.addWindowListener(new WebWindowListener() {
            @Override
            public void windowOpened(WebClient client, WebWindow window) {
                windowsOpened.add(window);
            }

            @Override
            public void windowClosed(WebClient client, WebWindow window) {
            }
        });
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinks()[0].click();

        assertFalse(windowsOpened.isEmpty(), "No window opened");
        final WebWindow openedWindow = (WebWindow) windowsOpened.get(0);
        assertEquals("You made it!", openedWindow.getCurrentPage().getText(), "New window message");
    }

    @Test
    void windowOpenNoContents() throws Exception {
        defineResource("OnCommand.html", "<html><head><title>Amazing!</title></head>" + "<body>"
                + "<a href='#' onClick=\"window.open( null, 'sample' );\">go</a>" + "</body></html>");
        final ArrayList windowsOpened = new ArrayList<>();
        WebConversation wc = new WebConversation();
        wc.addWindowListener(new WebWindowListener() {
            @Override
            public void windowOpened(WebClient client, WebWindow window) {
                windowsOpened.add(window);
            }

            @Override
            public void windowClosed(WebClient client, WebWindow window) {
            }
        });
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinks()[0].click();

        assertFalse(windowsOpened.isEmpty(), "No window opened");
        final WebWindow openedWindow = (WebWindow) windowsOpened.get(0);
        assertEquals("", openedWindow.getCurrentPage().getText(), "New window message");
        assertEquals("sample", openedWindow.getName(), "New window name");
        assertEquals(openedWindow, wc.getOpenWindow("sample"), "Window by name");
    }

    @Test
    void windowReopen() throws Exception {
        defineResource("Target.html", "You made it!");
        defineResource("Revise.html", "You changed it!");
        defineResource("OnCommand.html",
                "<html><head><title>Amazing!</title></head>" + "<body>" + "<a href='#' onClick=\"window.open( '"
                        + getHostPath() + "/Target.html', 'sample' );\">go</a>" + "<a href='#' onClick=\"window.open( '"
                        + getHostPath() + "/Revise.html', 'sample' );\">go</a>" + "</body></html>");
        final ArrayList windowsOpened = new ArrayList<>();
        WebConversation wc = new WebConversation();
        wc.addWindowListener(new WebWindowListener() {
            @Override
            public void windowOpened(WebClient client, WebWindow window) {
                windowsOpened.add(window);
            }

            @Override
            public void windowClosed(WebClient client, WebWindow window) {
            }
        });
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinks()[0].click();
        assertEquals("You made it!", ((WebWindow) windowsOpened.get(0)).getCurrentPage().getText(),
                "New window message");
        response.getLinks()[1].click();

        assertEquals(1, windowsOpened.size(), "Number of window openings");
        assertEquals("You changed it!", ((WebWindow) windowsOpened.get(0)).getCurrentPage().getText(),
                "Changed window message");
    }

    @Test
    void openedWindowProperties() throws Exception {
        defineResource("Target.html",
                "<html><head><script language='JavaScript'>" + "function show_properties() {"
                        + "   alert( 'name=' + window.name );" + "   alert( 'opener name=' + window.opener.name );"
                        + "}" + "</script></head><body onload='show_properties()'>" + "</body></html>");
        defineResource("OnCommand.html", "<html><head><title>Amazing!</title></head>"
                + "<body onload=\"window.name='main'; alert ('opener ' + (window.opener ? 'found' : 'not defined') );\">"
                + "<a href='#' onClick=\"window.open( '" + getHostPath() + "/Target.html', 'sample' );\">go</a>"
                + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals("main", wc.getMainWindow().getName(), "main window name");
        assertEquals("opener not defined", wc.popNextAlert(), "main window alert");
        response.getLinks()[0].click();

        assertEquals("name=sample", wc.popNextAlert(), "1st alert");
        assertEquals("opener name=main", wc.popNextAlert(), "2nd alert");
    }

    @Test
    void frameProperties() throws Exception {
        HttpUnitOptions.setExceptionsThrownOnScriptError(false);
        defineWebPage("Linker", "This is a trivial page with <a href=Target.html>one link</a>");
        defineResource("Target.html",
                "<html><head><script language='JavaScript'>" + "function show_properties() {"
                        + "   alert( 'name=' + window.name );" + "   alert( 'top url=' + window.top.location );"
                        + "   alert( '1st frame=' + top.frames[0].name );"
                        + "   alert( '2nd frame=' + window.parent.blue.name );"
                        + "   alert( 'parent url=' + window.parent.location );"
                        + "   alert( 'top.parent=' + top.parent.location );"
                        + "   alert( 'indexed frame=' + top.frames['red'].name );" + "}" + "</script></head><body>"
                        + "<a href=# onclick='show_properties()'>show</a>" + "</body></html>");
        defineWebPage("Form",
                "This is a page with a simple form: "
                        + "<form action=submit><input name=name><input type=submit></form>"
                        + "<a href=Linker.html target=red>a link</a>");
        defineResource("Frames.html",
                "<html><head><title>Initial</title></head>" + "<frameset cols='20%,80%'>"
                        + "    <frame src='Linker.html' name='red'>" + "    <frame src=Target.html name=blue>"
                        + "</frameset></html>");

        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/Frames.html");
        WebResponse blue = wc.getFrameContents("blue");
        blue.getLinkWith("show").click();

        assertEquals("name=blue", wc.popNextAlert(), "1st alert");
        assertEquals("top url=" + getHostPath() + "/Frames.html", wc.popNextAlert(), "2nd alert");
        assertEquals("1st frame=red", wc.popNextAlert(), "3rd alert");
        assertEquals("2nd frame=blue", wc.popNextAlert(), "4th alert");
        assertEquals("parent url=" + getHostPath() + "/Frames.html", wc.popNextAlert(), "5th alert");
        assertEquals("top.parent=" + getHostPath() + "/Frames.html", wc.popNextAlert(), "6th alert");
        assertEquals("indexed frame=red", wc.popNextAlert(), "7th alert");
    }

    @Test
    void locationProperty() throws Exception {
        defineResource("Target.html", "You made it!");
        defineResource("location.js",
                "function show() {" + "alert('Window location is ' + window.location);"
                        + "alert('Document location is ' + document.location);"
                        + "alert('Window location.href is ' + window.location.href);" + "}");
        defineResource("OnCommand.html",
                "<html><head><title>Amazing!</title>" + "<script language='JavaScript' src='location.js'></script>"
                        + "</head>" + "<body onLoad='show()'>" + "<a href='#' onMouseOver=\"window.location='"
                        + getHostPath() + "/Target.html';\">go</a>" + "<a href='#' onMouseOver=\"document.location='"
                        + getHostPath() + "/Target.html';\">go</a>"
                        + "<a href='#' onMouseOver=\"document.location.replace('" + getHostPath()
                        + "/Target.html');\">go</a>" + "</body>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals("Window location is " + getHostPath() + "/OnCommand.html", wc.popNextAlert(), "Alert message 1");
        assertEquals("Document location is " + getHostPath() + "/OnCommand.html", wc.popNextAlert(), "Alert message 2");
        assertEquals("Window location.href is " + getHostPath() + "/OnCommand.html", wc.popNextAlert(),
                "Alert message 3");
        response.getLinks()[0].mouseOver();
        assertEquals(getHostPath() + "/Target.html", wc.getCurrentPage().getURL().toExternalForm(), "2nd page URL");
        assertEquals("You made it!", wc.getCurrentPage().getText(), "2nd page");

        response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinks()[1].mouseOver();
        assertEquals(getHostPath() + "/Target.html", wc.getCurrentPage().getURL().toExternalForm(), "3rd page URL");
        assertEquals("You made it!", wc.getCurrentPage().getText(), "3rd page");

        response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinks()[2].mouseOver();
        assertEquals(getHostPath() + "/Target.html", wc.getCurrentPage().getURL().toExternalForm(), "4th page URL");
        assertEquals("You made it!", wc.getCurrentPage().getText(), "4th page");

        response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getScriptingHandler().doEventScript("window.location.href='" + getHostPath() + "/Target.html'");
        assertEquals(getHostPath() + "/Target.html", wc.getCurrentPage().getURL().toExternalForm(), "5th page URL");
        assertEquals("You made it!", wc.getCurrentPage().getText(), "5th page");
    }

    @Test
    void locationPropertyOnLoad() throws Exception {
        defineResource("Target.html", "You made it!");
        defineResource("OnCommand.html", "<html><head><title>Amazing!</title>" + "</head>"
                + "<body onLoad=\"document.location='" + getHostPath() + "/Target.html';\">" + "</body>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals(getHostPath() + "/Target.html", wc.getCurrentPage().getURL().toExternalForm(), "current page URL");
        assertEquals("You made it!", wc.getCurrentPage().getText(), "current page");
        assertEquals(getHostPath() + "/Target.html", response.getURL().toExternalForm(), "returned page URL");
        assertEquals("You made it!", response.getText(), "returned page");
    }

    @Test
    void locationReadableSubproperties() throws Exception {
        defineResource("Target.html", "You made it!");
        defineResource("location.js", "function show() {" + "alert('host is ' + window.location.host);"
                + "alert('hostname is ' + document.location.hostname);" + "alert('port is ' + window.location.port);"
                + "alert('pathname is ' + window.location.pathname);"
                + "alert('protocol is ' + document.location.protocol);"
                + "alert('search is ' + window.location.search);" + "}");
        defineResource("simple/OnCommand.html?point=center",
                "<html><head><title>Amazing!</title>" + "<script language='JavaScript' src='/location.js'></script>"
                        + "</head>" + "<body onLoad='show()'>" + "</body>");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/simple/OnCommand.html?point=center");
        assertEquals("host is " + getHostPath().substring(7), wc.popNextAlert(), "Alert message 1");
        assertEquals("hostname is localhost", wc.popNextAlert(), "Alert message 2");
        assertEquals("port is " + getHostPort(), wc.popNextAlert(), "Alert message 3");
        assertEquals("pathname is /simple/OnCommand.html", wc.popNextAlert(), "Alert message 4");
        assertEquals("protocol is http:", wc.popNextAlert(), "Alert message 5");
        assertEquals("search is ?point=center", wc.popNextAlert(), "Alert message 6");
    }

    @Test
    void locationWriteableSubproperties() throws Exception {
        defineResource("Target.html", "You made it!");
        defineResource("OnCommand.html?where=here", "You found it!");
        defineResource("OnCommand.html",
                "<html><head><title>Amazing!</title>" + "</head>" + "<body'>"
                        + "<a href='#' onMouseOver=\"window.location.pathname='/Target.html';\">go</a>"
                        + "<a href='#' onMouseOver=\"document.location.search='?where=here';\">go</a>" + "</body>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinks()[0].mouseOver();
        assertEquals(getHostPath() + "/Target.html", wc.getCurrentPage().getURL().toExternalForm(), "2nd page URL");
        assertEquals("You made it!", wc.getCurrentPage().getText(), "2nd page");

        response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinks()[1].mouseOver();
        assertEquals(getHostPath() + "/OnCommand.html?where=here", wc.getCurrentPage().getURL().toExternalForm(),
                "3rd page URL");
        assertEquals("You found it!", wc.getCurrentPage().getText(), "3rd page");
    }

    @Test
    void scriptDisabled() throws Exception {
        HttpUnitOptions.setScriptingEnabled(false);
        defineResource("nothing.html", "Should get here");
        defineResource("OnCommand.html", "<html><head></head>" + "<body>"
                + "<form name='realform'><input name='color' value='blue'></form>"
                + "<a href='nothing.html' onClick=\"document.realform.color.value='green';return false;\">green</a>"
                + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        WebForm form = response.getFormWithName("realform");
        WebLink link = response.getLinks()[0];
        assertEquals("blue", form.getParameterValue("color"), "initial parameter value");
        link.click();
        assertEquals("blue", form.getParameterValue("color"), "unchanged parameter value");
        assertEquals("Should get here", wc.getCurrentPage().getText(), "Expected result");
    }

    @Test
    void navigatorObject() throws Exception {
        defineResource("OnCommand.html", "<html><head><script language='JavaScript'>" + "function viewProperties() { \n"
                + "  alert( 'appName=' + navigator.appName );\n"
                + "  alert( 'appCodeName=' + navigator.appCodeName )\n;"
                + "  alert( 'appVersion=' + navigator.appVersion )\n;"
                + "  alert( 'userAgent=' + navigator.userAgent )\n;" + "  alert( 'platform=' + navigator.platform )\n;"
                + "  alert( 'javaEnabled=' + navigator.javaEnabled() )\n;"
                + "  alert( '# plugins=' + navigator.plugins.length )\n;" + "}" + "</script></head>\n"
                + "<body onLoad='viewProperties()'>\n" + "</body></html>");
        HttpUnitOptions.setExceptionsThrownOnScriptError(true);
        WebConversation wc = new WebConversation();
        wc.getClientProperties().setApplicationID("Internet Explorer", "Mozilla", "4.0");
        wc.getClientProperties().setPlatform("JVM");
        wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals("appName=Internet Explorer", wc.popNextAlert(), "Alert message 1");
        assertEquals("appCodeName=Mozilla", wc.popNextAlert(), "Alert message 2");
        assertEquals("appVersion=4.0", wc.popNextAlert(), "Alert message 3");
        assertEquals("userAgent=Mozilla/4.0", wc.popNextAlert(), "Alert message 4");
        assertEquals("platform=JVM", wc.popNextAlert(), "Alert message 5");
        assertEquals("javaEnabled=false", wc.popNextAlert(), "Alert message 6");
        assertEquals("# plugins=0", wc.popNextAlert(), "Alert message 7");
        assertNull(wc.getNextAlert(), "Alert should have been removed");
    }

    @Test
    void screenObject() throws Exception {
        defineResource("OnCommand.html",
                "<html><head><script language='JavaScript'>" + "function viewProperties() { \n"
                        + "  alert( 'dimensions=' + screen.availWidth + 'x' + screen.availHeight );\n" + "}"
                        + "</script></head>\n" + "<body onLoad='viewProperties()'>\n" + "</body></html>");
        HttpUnitOptions.setExceptionsThrownOnScriptError(true);
        WebConversation wc = new WebConversation();
        wc.getClientProperties().setAvailableScreenSize(1024, 752);
        wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals("dimensions=1024x752", wc.popNextAlert(), "Alert message 1");
        assertNull(wc.getNextAlert(), "Alert should have been removed");
    }

    @Test
    void styleProperty() throws Exception {
        defineResource("start.html", "<html><head><script language='JavaScript'>" + "function showDisplay( id ) {"
                + "  var element = document.getElementById( id );\n"
                + "  alert( 'element with id ' + id + ' has style.display ' + element.style.display );\n" + "}\n"
                + "function setDisplay( id, value ) {" + "  var element = document.getElementById( id );\n"
                + "  element.style.display = value;\n" + "}\n" + "function showVisibility( id ) {"
                + "  var element = document.getElementById( id );\n"
                + "  alert( 'element with id ' + id + ' has style.visibility ' + element.style.visibility );\n" + "}\n"
                + "function setVisibility( id, value ) {" + "  var element = document.getElementById( id );\n"
                + "  element.style.visibility = value;\n" + "}\n" + "function doAll() {\n"
                + "  setDisplay('test','inline'); \n" + "  showDisplay('test');\n" + "  setDisplay('test','block'); \n"
                + "  showDisplay('test');\n" + "  setVisibility('test','hidden'); \n" + "  showVisibility('test');\n"
                + "  setVisibility('test','visible'); \n" + "  showVisibility('test');\n" + "}\n" + "</script>"
                + "</head><body onLoad='doAll();'>" + "<div id='test'>foo</div></body></html>");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/start.html");

        assertEquals("element with id test has style.display inline", wc.popNextAlert());
        assertEquals("element with id test has style.display block", wc.popNextAlert());
        assertEquals("element with id test has style.visibility hidden", wc.popNextAlert());
        assertEquals("element with id test has style.visibility visible", wc.popNextAlert());
    }

    @Test
    void setAttribute() throws Exception {
        /*
         * A minimal snippet: <input type="text" id="foo" name="foo" myattr="bar" /> ... var field =
         * document.getElementById("foo"); var attributeValue = field.getAttribute("myattr");
         * alert("The attribute value is " + attributeValue); field.setAttribute("myattr", "new_attribute_value");
         */
        // will only work with Dom based scripting engine before patch
        // needs addCustomAttribute for old scriptin engine
        if (HttpUnitOptions.DEFAULT_SCRIPT_ENGINE_FACTORY.equals(HttpUnitOptions.ORIGINAL_SCRIPTING_ENGINE_FACTORY)) {
            HttpUnitOptions.addCustomAttribute("myattr");
        }
        defineResource("start.html",
                "<html><head>\n" + "<script language='JavaScript'>\n" + "function testAttributes() {\n"
                        + "var field = document.getElementById(\"foo\");"
                        + "var attributeValue = field.getAttribute(\"myattr\");"
                        + "alert('The attribute value is ' + attributeValue);\n"
                        + "field.setAttribute(\"myattr\", \"newValue\");\n"
                        + "alert('The attribute value is changed to ' + field.getAttribute('myattr'));\n" + "}\n"
                        + "</script>\n" + "</head>\n" + "<body id='body_id' onLoad='testAttributes();'>"
                        + "<form name='the_form'><input type=\"text\" id=\"foo\" name=\"foo\" myattr=\"bar\" /></form>"
                        + "</body></html");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/start.html");
        assertEquals("The attribute value is bar", wc.popNextAlert());
        assertEquals("The attribute value is changed to newValue", wc.popNextAlert());
        // } // if

    }

    /**
     * test for onChange part of Patch proposal 1653410 calling on change from javascript used to throw
     * com.meterware.httpunit.ScriptException: Event 'callonChange();' failed: org.mozilla.javascript.EcmaError:
     * TypeError: Cannot find function onChange. (httpunit#6) after patch
     *
     * @throws Exception
     */
    @Test
    void callOnChange() throws Exception {
        defineResource("start.html", "<html><head>\n" + "<script language='JavaScript'>\n"
                + "function onChangeHandler() {\n" + "alert('onChange has been called');\n" + "}\n"
                + "function callonChange() {\n" + "alert('calling onChange');\n" + "// fire onChangeHandler directly\n"
                + "onChangeHandler();\n" + "var field = document.getElementById(\"foo\");\n"
                + "// fire onChangeHandler indirectly via event\n" + "field.onchange();\n" + "}\n" + "</script>\n"
                + "</head>\n" + "<body id='body_id' onLoad='callonChange();'>"
                + "<form name='the_form'><input type=\"text\" onchange='onChangeHandler' id=\"foo\" name=\"foo\" /></form>"
                + "</body></html");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/start.html");
        String firstAlert = wc.popNextAlert();
        assertEquals("calling onChange", firstAlert);
        String secondAlert = wc.popNextAlert();
        assertEquals("onChange has been called", secondAlert, "2nd");
        wc.popNextAlert();
    }

    /**
     * test for window event part of Patch proposal 1653410
     *
     * @throws Exception
     */
    @Test
    void windowEvent() throws Exception {
        assertDoesNotThrow(() -> {
            defineWebPage("OnCommand", "<html><head>\n" + "<script language='JavaScript'>\n"
                    + "function buttonclick() {\n" + "alert('hi');\n" + "var event=window.event;\n" + "}\n"
                    + "</script>\n" + "</head><body onload='buttonclick'>\n"
                    + "<form id='someform'><input type='button' id='button1' onClick='buttonclick'></input></form>\n"
                    + "</body></html");
            WebConversation wc = new WebConversation();
            WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
            Button button1 = (Button) response.getElementWithID("button1");
            button1.click();
            // TODO make this work
            // assertEquals( "Alert message 1", "hi", wc.popNextAlert() );
        });
        // TODO make this work
        // assertEquals( "Alert message 1", "hi", wc.popNextAlert() );
    }

    @Test
    void tagNameNodeNameProperties() throws Exception {
        defineResource("start.html", "<html><head><script language='JavaScript'>\n" + "function showTagName(id) {\n"
                + "  var element = document.getElementById( id );\n"
                + "  alert( 'element id=' + id + ', tagName='  + element.tagName + ', nodeName='  + element.nodeName );\n"
                + "}\n" + "function doAll() {\n" + "  showTagName('body_id')\n" + "  showTagName('iframe_id')\n"
                + "  showTagName('div_id')\n" + "}\n" + "</script>\n" + "</head><body id='body_id' onLoad='doAll();'>\n"
                + "<div id='div_id'><iframe id='iframe_id' /></div>\n" + "</body></html>");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/start.html");

        assertEquals("element id=body_id, tagName=BODY, nodeName=BODY", wc.popNextAlert());
        assertEquals("element id=iframe_id, tagName=IFRAME, nodeName=IFRAME", wc.popNextAlert());
        assertEquals("element id=div_id, tagName=DIV, nodeName=DIV", wc.popNextAlert());
    }

    @Test
    void readNoCookie() throws Exception {
        defineResource("OnCommand.html",
                "<html><head><script language='JavaScript'>" + "function viewCookies() { \n"
                        + "  alert( 'cookies: ' + document.cookie );\n" + "}" + "</script></head>\n"
                        + "<body onLoad='viewCookies()'>\n" + "</body></html>");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals("cookies: ", wc.popNextAlert(), "Alert message 1");
        assertNull(wc.getNextAlert(), "Alert should have been removed");
    }

    @Test
    void simpleSetCookie() throws Exception {
        defineResource("OnCommand.html",
                "<html><head></head>\n" + "<body onLoad='document.cookie=\"color=red;path=/\"'>\n" + "</body></html>");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals("red", wc.getCookieValue("color"), "Cookie 'color'");
    }

    @Test
    void setCookieToNull() throws Exception {
        assertDoesNotThrow(() -> {
            defineResource("OnCommand.html", "<html><script>" + "document.cookie = null;" + "</script></html>");
            WebConversation wc = new WebConversation();
            wc.getResponse(getHostPath() + "/OnCommand.html");
        });
    }

    @Test
    void readCookies() throws Exception {
        defineResource("OnCommand.html",
                "<html><head><script language='JavaScript'>" + "function viewCookies() { \n"
                        + "  alert( 'cookies: ' + document.cookie );\n" + "}" + "</script></head>\n"
                        + "<body onLoad='viewCookies()'>\n" + "</body></html>");
        addResourceHeader("OnCommand.html", "Set-Cookie: age=12");
        WebConversation wc = new WebConversation();
        wc.putCookie("height", "tall");
        wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals("cookies: age=12; height=tall", wc.popNextAlert(), "Alert message 1");
        assertNull(wc.getNextAlert(), "Alert should have been removed");
    }

    @Test
    void buttonWithoutForm() throws Exception {
        defineWebPage("OnCommand", "<button id='mybutton' onclick='alert( \"I heard you!\" )'>"
                + "<input id='yourbutton' type='button'  onclick='alert( \"Loud and Clear.\" )'>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        ((Button) response.getElementWithID("mybutton")).click();
        assertEquals("I heard you!", wc.popNextAlert(), "Alert message 1");

        ((Button) response.getElementWithID("yourbutton")).click();
        assertEquals("Loud and Clear.", wc.popNextAlert(), "Alert message 2");
    }

    /**
     * test the trick for detecting java script enabled
     *
     * @throws Exception
     */
    // TODO JWL 6/26/2021 Nekohtml patch is against 'body' not the 'header' so issue was never fixed.
    @Disabled
    @Test
    void javascriptDetectionTrick() throws Exception {
        defineResource("NoScript.html", "No javascript here");
        defineResource("HasScript.html", "Javascript is enabled!");
        defineResource("Start.html",
                "<html><head>" + "  <noscript>" + "      <meta http-equiv='refresh' content='0;url=NoScript.html'>"
                        + "  </noscript>" + "</head>" + "<body onload='document.form.submit()'>"
                        + "<form name='form' action='HasScript.html'></form>" + "</body></html>");
        WebConversation wc = new WebConversation();
        wc.getClientProperties().setAutoRefresh(true);
        WebResponse response = wc.getResponse(getHostPath() + "/Start.html");
        assertEquals("Javascript is enabled!", response.getText(), "Result page ");
        HttpUnitOptions.setScriptingEnabled(false);
        response = wc.getResponse(getHostPath() + "/Start.html");
        assertEquals("No javascript here", response.getText(), "Result page");
    }

    /**
     * https://sourceforge.net/forum/forum.php?thread_id=1808696&forum_id=20294 by kauffman81
     */
    @Test
    void javaScriptConfirmPopUp() throws Exception {
        String target = "<html><body>After click we want to see this!</body></html>";
        defineResource("Target.html", target);
        defineResource("Popup.html",
                "<html><head><script language='JavaScript'>"
                        + "// 	This is the javascript that handles the onclick event\n"
                        + "function verify_onBorrar(form){\n" + "  alert(form.id);\n" +
                        /*
                         * TODO check this javascript code if uncommented it will throw
                         * com.meterware.httpunit.ScriptException: Event 'verify_onBorrar(this.form)' failed:
                         * org.mozilla.javascript.EcmaError: TypeError: Cannot read property "0" from undefined
                         * (httpunit#3) "	for(var i = 0;i<form.selection[i].length;i++){\n"+
                         * "		if(form.selection[i].checked){\n"+ "			if(confirm('blablabla')){\n"+
                         * "				form.action = 'Target.html';\n"+ "				form.submit(); \n"+
                         * "			} // if\n"+ "		} // if\n"+ "	} // for\n"+
                         */
                        "} // verify_onBorrar\n" + "</script></head>\n" + "<body>\n"
                        + "	<form id='someform' name='someform'>"
                        + "		<input type='button' id='button1' class='button' value='say hi' onclick=\"alert('hi')\"/>"
                        + "		<input type='button' id='delete' class='button' value='delete' onclick='verify_onBorrar(this.form)'/></form>\n"
                        + "	</form>\n" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/Popup.html");
        Button button1 = (Button) response.getElementWithID("button1");
        button1.click();
        String alert1 = wc.popNextAlert();
        assertEquals("hi", alert1);
        Button button2 = (Button) response.getElementWithID("delete");
        button2.click();
        wc.popNextAlert();
    }

    /**
     * test for function in external javascript https://sourceforge.net/forum/forum.php?thread_id=1406498&forum_id=20294
     *
     * @throws Exception
     */
    // TODO JWL 7/6/2021 Breaks with nekohtml > 1.9.6.2
    @Disabled
    @Test
    void javaScriptFromSource() throws Exception {
        defineResource("someScript.js", "function someFunction() {\n" + "	alert('somefunction called')" + "}\n");
        defineResource("Script.html",
                "<html><head>\n" + "<script language='JavaScript' src='someScript.js' />\n"
                        + "<script language='JavaScript'>\n" + "function testFunction() {\n"
                        + "	var retValue = someFunction(); //Here some function is part of SomeScript.js\n" + "}\n"
                        + "</script></head><body onload='testFunction()'></body></html>");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/Script.html");
        // com.meterware.httpunit.ScriptException: Event 'testFunction()' failed: org.mozilla.javascript.EcmaError:
        // ReferenceError: "someFunction" is not defined. (httpunit#1)
        String alert1 = wc.popNextAlert();
        assertEquals("somefunction called", alert1);
    }

    /**
     * test for bug report [ 1396835 ] Javascript : length of a select element cannot be increased by gklopp used to
     * throw java.lang.RuntimeException: Script 'fillSelect();' failed: java.lang.RuntimeException: invalid index 1 for
     * Options option1 at
     * com.meterware.httpunit.javascript.ScriptingEngineImpl.handleScriptException(ScriptingEngineImpl.java:61)
     *
     * @throws Exception
     */
    @Test
    void fillSelect() throws Exception {
        assertDoesNotThrow(() -> {
            defineResource("testSelect.html",
                    "<html><head><script type='text/javascript'>\n" + "<!--\n" + "function fillSelect() {\n"
                            + "   document.the_form.the_select.options.length = 2;\n"
                            + "   document.the_form.the_select.options[1].text = 'option2';\n "
                            + "   document.the_form.the_select.options[1].value = 'option2Value';\n " + "}\n" + "-->\n"
                            + "</script></head>" + "<body>" + "<form name='the_form'>" + "   <table>" + "    <tr>"
                            + "      <td>Selection :</td>" + "       <td>" + "          <select name='the_select'>"
                            + "              <option value='option1Value'>option1</option>" + "          </select>"
                            + "       </td>" + "     </tr>" + "   </table>" + "</form>"
                            + "<script type='text/javascript'>fillSelect();</script>" + "</body></html>");
            WebConversation wc = new WebConversation();
            WebResponse response = wc.getResponse(getHostPath() + "/testSelect.html");
            response.getFormWithName("the_form");
        });
    }

    /**
     * test for bug report [ 1396896 ] Javascript: length property of a select element not writable by gklopp used to
     * throw java.lang.RuntimeException: Script 'modifySelectLength();' failed: java.lang.RuntimeException: No such
     * property: length
     *
     * @throws Exception
     */
    @Test
    void modifySelectLength() throws Exception {
        assertDoesNotThrow(() -> {
            defineResource("testModifySelectLength.html",
                    "<html><head><script type='text/javascript'>\n" + "<!--\n" + "function modifySelectLength() {\n"
                            + "   document.the_form.the_select.length = 2;\n"
                            + "   document.the_form.the_select.options[1].text = 'option2';\n "
                            + "   document.the_form.the_select.options[1].value = 'option2Value';\n " + "}\n" + "-->\n"
                            + "</script></head>" + "<body>" + "<form name='the_form'>" + "   <table>" + "    <tr>"
                            + "      <td>Selection :</td>" + "       <td>" + "          <select name='the_select'>"
                            + "              <option value='option1Value'>option1</option>" + "          </select>"
                            + "       </td>" + "     </tr>" + "   </table>" + "</form>"
                            + "<script type='text/javascript'>modifySelectLength();</script>" + "</body></html>");
            WebConversation wc = new WebConversation();
            wc.getResponse(getHostPath() + "/testModifySelectLength.html");

        });

    }

}

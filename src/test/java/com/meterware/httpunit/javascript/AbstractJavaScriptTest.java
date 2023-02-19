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

import com.meterware.httpunit.HttpUnitTest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

/**
 * Created by IntelliJ IDEA. User: russgold Date: May 16, 2008 Time: 3:20:40 PM abstract base class for JavaScript tests
 * of the httpunit framework supplies doTestJavaScript as a default operation for starting tests
 */
public abstract class AbstractJavaScriptTest extends HttpUnitTest {
    // set to true to get the static HTML Code on System.err
    public static boolean debugHTML = false;

    /**
     * test the given javaScript code by putting it into a function and calling it as a prerequisite make the html code
     * snippet available in the body of the page
     *
     * @param script
     *            - some javascript code to be called in a function
     * @param html
     *            - a html code snippet
     *
     * @return
     *
     * @throws Exception
     */
    public WebConversation doTestJavaScript(String script, String html) throws Exception {
        defineResource("OnCommand.html",
                "<html><head><script language='JavaScript'>\n" + "function javaScriptFunction() {\n" + script + "}\n"
                        + "</script></head>" + "<body>" + html + "\n"
                        + "<a href=\"javascript:javaScriptFunction()\">go</a>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        if (debugHTML) {
            System.err.println(response.getText() + "\n");
        }
        response.getLinkWith("go").click();
        return wc;
    }

    /**
     * test the given javaScript code by putting it into a function and calling it
     *
     * @param script
     *            the script to test
     *
     * @return the web client on which the test was run
     */
    public WebConversation doTestJavaScript(String script) throws Exception {
        return doTestJavaScript(script, "");
    }
}

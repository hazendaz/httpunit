package com.meterware.httpunit.javascript;

import com.meterware.httpunit.HttpUnitTest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

/**
 * Created by IntelliJ IDEA.
 * User: russgold
 * Date: May 16, 2008
 * Time: 3:20:40 PM
 * abstract base class for JavaScript tests of the httpunit framework
 * supplies doTestJavaScript as a default operation for starting tests
 */
public abstract class AbstractJavaScriptTest extends HttpUnitTest {
    // set to true to get the static HTML Code on System.err
    public static boolean debugHTML = false;


    /**
     * test the given javaScript code by putting it into a function and calling it
     * as a prerequisite make the html code snippet available in the body of the page
     *
     * @param script - some javascript code to be called in a function
     * @param html   - a html code snippet
     * @return
     * @throws Exception
     */
    public WebConversation doTestJavaScript(String script, String html) throws Exception {
        defineResource("OnCommand.html", "<html><head><script language='JavaScript'>\n" +
                "function javaScriptFunction() {\n" +
                script +
                "}\n" +
                "</script></head>" +
                "<body>" +
                html + "\n" +
                "<a href=\"javascript:javaScriptFunction()\">go</a>" +
                "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        if (debugHTML) {
            System.err.println(response.getText() + "\n");
        }
        response.getLinkWith("go").click();
        return wc;
    }


    /**
     * test the given javaScript code by putting it into a function
     * and calling it
     *
     * @param script the script to test
     * @return the web client on which the test was run
     */
    public WebConversation doTestJavaScript(String script) throws Exception {
        return doTestJavaScript(script, "");
    }
}

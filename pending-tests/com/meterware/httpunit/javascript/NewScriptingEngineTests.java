/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.javascript;

import com.meterware.httpunit.*;

import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * New features promised for scripting, but only implemented for new scripting engine.
 */
public class NewScriptingEngineTests extends AbstractJavaScriptTest {

    public static void main( String args[] ) {
        TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( NewScriptingEngineTests.class );
    }


    public NewScriptingEngineTests( String name ) {
        super( name );
    }


    /**
     * test jsFunction_createElement() - supplied by Mark Childerson
     * also for bug report [ 1430378 ] createElement not found in JavaScript by Saliya Jinadasa
     *
     * @throws Exception on uncaught problem
     */
    public void testCreateElement() throws Exception {
        pseudoServerTestSupport.defineResource("OnCommand.html",
                "<html><head><title>Amazing!</title></head>" +
                        "<body onLoad='var elem=document.createElement(\"input\");elem.id=\"hellothere\";alert(elem.id);'></body>");
        WebConversation wc = new WebConversation();
        boolean oldDebug = HttpUnitUtils.setEXCEPTION_DEBUG( false );
        try {
            wc.getResponse(pseudoServerTestSupport.getHostPath() + "/OnCommand.html" );
            // 	used to throw:
            // 	com.meterware.httpunit.ScriptException: Event 'var elem=document.createElement("input");elem.id="hellothere";alert(elem.id);' failed: org.mozilla.javascript.EcmaError: TypeError: Cannot find function createElement.
            assertEquals( "Alert message", "hellothere", wc.popNextAlert() );
        } finally {
            HttpUnitUtils.setEXCEPTION_DEBUG( oldDebug );
        }
    }

    /**
     * test for cloneNode feature (asked for by Mark Childeson on 2008-04-01)
     * @throws Exception on any uncaught problem
     */
    public void testCloneNode() throws Exception {
        doTestJavaScript(
                "dolly1=document.getElementById('Dolly');\n" +
                        "dolly2=dolly1.cloneNode(true);\n" +
                        "dolly1.firstChild.nodeValue += dolly2.firstChild.nodeValue;\n" +
                        "alert(dolly1.firsthChild.nodeValue);\n",
                "<div id='Dolly'>Dolly </div>" );
    }


    /**
     * test for bug report [ 1396877 ] Javascript:properties parentNode,firstChild, .. returns null
     * by gklopp 2006-01-04 15:15
     * @throws Exception on any uncaught problem
     */
    public void testDOM() throws Exception {
        pseudoServerTestSupport.defineResource("testSelect.html", "<html><head><script type='text/javascript'>\n" +
                "<!--\n" +
                "function testDOM() {\n" +
                "  var sel = document.getElementById('the_select');\n" +
                "  var p = sel.parentNode;\n" +
                "  var child = p.firstChild;\n" +
                "  alert('Parent : ' + p.nodeName);\n" +
                "  alert('First child : ' + child.nodeName);\n" +
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
                "<script type='text/javascript'>testDOM();</script>" +
                "</body></html>");
        WebConversation wc = new WebConversation();
        wc.getResponse(pseudoServerTestSupport.getHostPath() + "/testSelect.html" );
        assertEquals( "Message 1", "TD", wc.popNextAlert().toUpperCase() );
        assertEquals( "Message 2", "SELECT", wc.popNextAlert().toUpperCase() );
    }

}

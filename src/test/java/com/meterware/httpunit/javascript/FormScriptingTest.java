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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.meterware.httpunit.Button;
import com.meterware.httpunit.DialogResponder;
import com.meterware.httpunit.HTMLElement;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.HttpUnitTest;
import com.meterware.httpunit.HttpUnitUtils;
import com.meterware.httpunit.SubmitButton;
import com.meterware.httpunit.WebClient;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.protocol.UploadFileSpec;
import com.meterware.pseudoserver.PseudoServlet;
import com.meterware.pseudoserver.WebResource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.migrationsupport.rules.ExternalResourceSupport;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 */
@ExtendWith(ExternalResourceSupport.class)
public class FormScriptingTest extends HttpUnitTest {

    /**
     * test to access form name in java script
     *
     * @throws Exception
     */
    // TODO JWL 7/6/2021 Breaks with nekohtml > 1.9.6.2
    @Disabled
    @Test
    void formNameProperty() throws Exception {
        defineWebPage("OnCommand",
                "<form name='the_form_with_name'/>" + "<script type='JavaScript'>"
                        + "  alert( document.forms[0].name );" + "</script>" + "<form id='the_form_with_id'/>"
                        + "<script type='JavaScript'>" + "  alert( document.forms[1].name );" + "</script>"
                        + "<form id='the_form_with_id2' name='the_form_with_name2'/>" + "<script type='JavaScript'>"
                        + "  alert( document.forms[2].name );" + "</script>");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals("the_form_with_name", wc.popNextAlert(), "Message 1");
        assertEquals("the_form_with_id", wc.popNextAlert(), "Message 2");
        assertEquals("the_form_with_name2", wc.popNextAlert(), "Message 3");
    }

    /**
     * FR [ 2163079 ] make form.name property mutable by Peter De Bruycker
     *
     * @throws Exception
     */
    @Test
    void modifyingFormNameProperty() throws Exception {
        WebConversation wc = new WebConversation();
        defineWebPage("Default",
                "<form id = 'the_form' name = 'form_name'/>"
                        + "<a href='#' name='doTell' onClick='document.forms[0].name=\"new_form_name\";'>tell</a>"
                        + "<a href='#' name='doShow' onClick='alert( document.forms[0].name );'>show</a>");
        WebResponse page = wc.getResponse(getHostPath() + "/Default.html");
        page.getLinkWithName("doShow").click();
        assertEquals("form_name", wc.popNextAlert(), "Initial name");
        page.getLinkWithName("doTell").click();
        page.getLinkWithName("doShow").click();
        assertEquals("new_form_name", wc.popNextAlert(), "Current name");
    }

    /**
     * test to access attributes from java script
     *
     * @throws Exception
     */
    @Test
    void getAttributeForBody() throws Exception {
        if (HttpUnitOptions.DEFAULT_SCRIPT_ENGINE_FACTORY.equals(HttpUnitOptions.ORIGINAL_SCRIPTING_ENGINE_FACTORY)) {
            // TODO try making this work
            return;
        }

        defineWebPage("OnCommand",
                "<html><head><title>test</title>\n" + "<script type='text/javascript'>\n" + "function show (attr) {\n" +
                // TODO make this work
                        "  var body=document.body;\n" + "  //var body=document.getElementById('thebody');\n"
                        + "  alert(body.getAttribute(attr));\n" + "}\n" + "</script></head>\n"
                        + "<body id='thebody' bgcolor='#FFFFCC' text='#E00000' link='#0000E0' alink='#000080' vlink='#000000'>\n"
                        + "<a href=\"javascript:show('bgcolor')\">background color?</a><br>\n"
                        + "<a href=\"javascript:show('text')\">text color?</a><br>\n"
                        + "<a href=\"javascript:show('link')\">linkcolor non visited</a><br>\n"
                        + "<a href=\"javascript:show('alink')\">link color activated links?</a>\n"
                        + "<a href=\"javascript:show('vlink')\">link color non visited</a><br>\n" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        // the page for testing externally
        // System.err.println(response.getText());

        WebLink[] links = response.getLinks();
        for (WebLink link : links) {
            link.click();
        }
        String[] expected = { "#FFFFCC", "#E00000", "#0000E0", "#000080", "#000000" };

        for (int i = 0; i < links.length; i++) {
            assertEquals(expected[i], wc.popNextAlert(), "Message for link  " + i);
        }
    }

    /**
     * test to access attributes from java script
     *
     * @throws Exception
     */
    @Test
    void getAttributeForDiv() throws Exception {
        if (HttpUnitOptions.DEFAULT_SCRIPT_ENGINE_FACTORY.equals(HttpUnitOptions.ORIGINAL_SCRIPTING_ENGINE_FACTORY)) {
            // TODO try making this work
            return;
        }

        defineWebPage("OnCommand",
                "<html><head><title>test</title>\n" + "<script type='text/javascript'>\n"
                        + "function show (id,attr) {\n" + "  var element=document.getElementById(id);\n"
                        + "  alert(element.getAttribute(attr));\n" + "}\n" + "</script></head>\n"
                        + "<body> <div id='div1' align='left'>\n"
                        + "<a href=\"javascript:show('div1','align')\">align attribute of div</a><br>\n"
                        + "</div></body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        // the page for testing externally
        // System.err.println(response.getText());
        WebLink[] links = response.getLinks();
        for (WebLink link : links) {
            link.click();
        }
        String[] expected = { "left" };
        for (int i = 0; i < links.length; i++) {
            assertEquals(expected[i], wc.popNextAlert(), "Message for link  " + i);
        }
    }

    @Test
    void elementsProperty() throws Exception {
        defineResource("OnCommand.html",
                "<html><head><script language='JavaScript'>" + "function listElements( form ) {\n"
                        + "  elements = form.elements;\n" + "  alert( 'form has ' + elements.length + ' elements' );\n"
                        + "  alert( 'value is ' + elements['first'].value );\n"
                        + "  alert( 'index is ' + elements[1].selectedIndex );\n" + "  elements[2].checked=true;\n"
                        + "}" + "</script></head>" + "<body>" + "<form name='the_form'>"
                        + "  <input type=text name=first value='Initial Text'>" + "  <select name='choices'>"
                        + "    <option>red" + "    <option selected>blue" + "  </select>"
                        + "  <input type=checkbox name=ready>" + "</form>"
                        + "<a href='#' onClick='listElements( document.the_form )'>elements</a>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        final WebForm form = response.getFormWithName("the_form");
        assertNull(form.getParameterValue("ready"), "Initial state");

        response.getLinks()[0].click();
        assertEquals("form has 3 elements", wc.popNextAlert(), "Message 1");
        assertEquals("value is Initial Text", wc.popNextAlert(), "Message 2");
        assertEquals("index is 1", wc.popNextAlert(), "Message 3");

        assertEquals("on", form.getParameterValue("ready"), "Changed state");
    }

    /**
     * test clicking on a span inspired by Mail from Christoph to developer mailinglist of 2008-04-01
     */
    @Test
    void clickSpan() throws Exception {
        defineResource("OnCommand.html", "<html><head><script language='JavaScript'>"
                + "function crtCtrla(obj,otherArg) {" + "		alert(otherArg);" + "}" + "</script></head>" + "<body>"
                + "<table><tr><td class='cl'><span onClick='crtCtrla(this, \"rim_ModuleSearchResult=Drilldown=key_\", null, null);' class='feact'><span>489</span></span></td></tr></table>"
                + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getElementNames();
        HTMLElement[] elements = response.getElementsByTagName("span");
        assertEquals(2, elements.length, "Two span elements should be found ");
        HTMLElement span1 = elements[0];
        String onclick = "crtCtrla(this, \"rim_ModuleSearchResult=Drilldown=key_\", null, null);";
        assertEquals(span1.getAttribute("onclick"), onclick);
        span1.handleEvent("onclick");
        String alert = wc.popNextAlert();
        assertEquals("rim_ModuleSearchResult=Drilldown=key_", alert, "function should have been triggered to alert");
        elements = response.getElementsWithAttribute("onclick", onclick);
        int expected = 2;
        assertEquals(elements.length, expected, expected + "elements should be found ");
        span1 = elements[0];
        span1.handleEvent("onclick");
        alert = wc.popNextAlert();
        assertEquals("rim_ModuleSearchResult=Drilldown=key_", alert, "function should have been triggered to alert");
        // TODO remove this part
        span1 = elements[1];
        span1.handleEvent("onclick");
        alert = wc.popNextAlert();
        assertEquals("rim_ModuleSearchResult=Drilldown=key_", alert, "function should have been triggered to alert");
    }

    @Test
    void resetViaScript() throws Exception {
        defineResource("OnCommand.html", "<html><head></head>" + "<body>" + "<form name=spectrum action='DoIt'>"
                + "  <input type=text name=color value=green>" + "  <input type=text name=change value=color>"
                + "</form>" + "<a href='#' onClick='document.spectrum.reset(); return false;'>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");

        WebForm form = response.getFormWithName("spectrum");
        form.setParameter("color", "blue");
        response.getLinks()[0].click();
        assertEquals("green", form.getParameterValue("color"), "Value after reset");
    }

    @Test
    void onResetEvent() throws Exception {
        defineResource("OnCommand.html",
                "<html><head></head>" + "<body>"
                        + "<form name=spectrum action='DoIt' onreset='alert( \"Ran the event\" );'>"
                        + "  <input type=text name=color value=green>" + "  <input type=reset id='clear'>" + "</form>"
                        + "<a href='#' onClick='document.spectrum.reset(); return false;'>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");

        WebForm form = response.getFormWithName("spectrum");

        form.setParameter("color", "blue");
        form.getButtonWithID("clear").click();
        assertEquals("green", form.getParameterValue("color"), "Value after reset");
        assertEquals("Ran the event", wc.popNextAlert(), "Alert message");

        form.setParameter("color", "blue");
        response.getLinks()[0].click();
        assertEquals("green", form.getParameterValue("color"), "Value after reset");
        assertNull(wc.getNextAlert(), "Event ran unexpectedly");
    }

    @Test
    void submitViaScript() throws Exception {
        defineResource("DoIt?color=green", "You made it!");
        defineResource("OnCommand.html",
                "<html><head></head>" + "<body>" + "<form name=spectrum action='DoIt'>"
                        + "  <input type=text name=color value=green>" + "  <input type=submit name=change value=color>"
                        + "  <input type=submit name=keep value=nothing>" + "</form>"
                        + "<a href='#' onClick='document.spectrum.submit(); return false;'>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");

        response.getLinks()[0].click();
        assertEquals("You made it!", wc.getCurrentPage().getText(), "Result of submit");
    }

    /**
     * Verifies bug #959918
     */
    @Test
    void numericParameterSetting1() throws Exception {
        defineResource("DoIt?id=1234", "You made it!");
        defineResource("OnCommand.html",
                "<html><head>" + "<script>" + "  function myFunction(value) {" + "    document.mainForm.id = value;"
                        + "    document.mainForm.submit();" + "  }</script>" + "</head>" + "<body>"
                        + "<form name=mainForm action='DoIt'>"
                        + "  <a href='javascript:myFunction(1234)'>View Asset</a>" + "  <input type='hidden' name='id'>"
                        + "</form>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");

        response.getLinks()[0].click();
        assertEquals("You made it!", wc.getCurrentPage().getText(), "Result of submit");
    }

    /**
     * Verifies bug #1087180
     */
    @Test
    void numericParameterSetting2() throws Exception {
        defineResource("DoIt.html?id=1234", "You made it!");
        defineResource("OnCommand.html",
                "<html><head>" + "<script>" + "  function myFunction(value) {"
                        + "    document.mainForm.id.value = value;" + "    document.mainForm.submit();" + "  }</script>"
                        + "</head>" + "<body>" + "<form name=mainForm action='DoIt.html'>"
                        + "  <a href='javascript:myFunction(1234)'>View Asset</a>" + "  <input type='hidden' name='id'>"
                        + "</form>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");

        response.getLinks()[0].click();
        assertEquals("You made it!", wc.getCurrentPage().getText(), "Result of submit");
    }

    /**
     * Verifies bug #1073810 (Null pointer exception if javascript sets control value to null)
     */
    @Test
    void nullParameterSetting() throws Exception {
        defineResource("OnCommand.html",
                "<html><head>" + "<script>" + "  function myFunction(value) {"
                        + "    document.mainForm.id.value = null;" + "  }</script>" + "</head>" + "<body>"
                        + "<form name=mainForm action='DoIt'>" + "  <a href='javascript:myFunction()'>View Asset</a>"
                        + "  <input type='hidden' name='id'>" + "</form>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");

        response.getLinks()[0].click();
    }

    /**
     * test changing form Action from JavaScript
     *
     * @throws Exception
     */
    @Test
    void formActionFromJavaScript() throws Exception {
        // pending Patch 1155792 wf 2007-12-30
        // TODO activate in due course
        dotestFormActionFromJavaScript("param");
    }

    /**
     * verify bug 1155792 ] problems setting form action from javascript [patch]
     */
    public void xtestFormActionFromJavaScript2() throws Exception {
        // pending Patch 1155792 wf 2007-12-30
        // TODO activate in due course
        dotestFormActionFromJavaScript("action");
    }

    /**
     * test doing a form action from Javascript
     *
     * @param paramName
     *
     * @throws Exception
     */
    public void dotestFormActionFromJavaScript(String paramName) throws Exception {
        if (HttpUnitOptions.DEFAULT_SCRIPT_ENGINE_FACTORY.equals(HttpUnitOptions.ORIGINAL_SCRIPTING_ENGINE_FACTORY)) {
            return;
        }

        defineWebPage("foo", "foocontent");
        defineResource("bar.html",
                "<html><head><script>" + "function submitForm()" + "{" + "  document.test.action='/foo.html';"
                        + "  document.test.submit()" + " }" + "</script></head>" + "<form name=\"test\" method=\"post\""
                        + " action=\"bar.html?" + paramName + "=bar\">" + "</form>"
                        + " <a href=\"javascript:submitForm()\">go</a></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/foo.html");
        String fooContent = wc.getCurrentPage().getText();
        // System.err.println(fooContent);
        response = wc.getResponse(getHostPath() + "/bar.html");
        try {
            response.getLinks()[0].click();
            String result = wc.getCurrentPage().getText();
            // System.err.println(result);
            assertEquals(fooContent, result, "Result of submit");
        } catch (RuntimeException rte) {
            // TODO activate this test
            // There is currently a
            // org.mozilla.javascript.JavaScriptException: com.meterware.httpunit.HttpNotFoundException: Error on HTTP
            // request: 404 unable to find /foo.html [http://localhost:1929/foo.html]
            // here
            fail("There should be no " + rte.getMessage() + " Runtime exception here");
        }
    }

    /**
     * test indirect invocation feature request [ 796961 ] Support indirect invocation of JavaScript events on elements
     * by David D. Kilzer
     *
     * @throws Exception
     */
    @Test
    void indirectEventInvocation() throws Exception {
        defineResource("OnCommand.html", "<html><head></head><body>" + "<form name=\"testForm\">"
                + "<input type=\"text\" name=\"one\" value=\"default value\" onchange=\"this.form.two.value = this.form.one.value;\">"
                + "<input type=\"text\" name=\"two\" value=\"not the same value\">" + "</form>"
                + "<script language=\"javascript\" type=\"text/javascript\">\n"
                + "document.forms[\"testForm\"].elements[\"one\"].onchange();\n" + "</script>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        WebForm form = response.getFormWithName("testForm");
        assertEquals(form.getParameterValue("one"), form.getParameterValue("two"), "field one equals field two");
    }

    /**
     * test disabling a submit button via script
     *
     * @throws Exception
     */
    @Test
    void enablingDisabledSubmitButtonViaScript() throws Exception {
        defineResource("DoIt?color=green&change=success", "You made it!");
        defineResource("OnCommand.html", "<html><head></head>" + "<body>" + "<form name=spectrum action='DoIt'>"
                + "  <input type=text name=color value=green>"
                + "  <input type=button name=enableChange id=enableChange value=Hello onClick='document.spectrum.change.disabled=false;'>"
                + "  <input type=submit disabled name=change value=success>" + "</form>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        WebForm form = response.getFormWithName("spectrum");

        assertSubmitButtonDisabled(form);
        assertDisabledSubmitButtonCanNotBeClicked(form);

        form = runJavaScriptToToggleEnabledStateOfButton(form, wc);

        assertSubmitButtonEnabled(form);
        clickSubmitButtonToProveThatItIsEnabled(form);
        assertEquals("You made it!", wc.getCurrentPage().getText(), "Result of submit");
    }

    @Test
    void disablingEnabledSubmitButtonViaScript() throws Exception {
        defineResource("DoIt?color=green&change=success", "You made it!");
        defineResource("OnCommand.html", "<html><head></head>" + "<body>" + "<form name=spectrum action='DoIt'>"
                + "  <input type=text name=color value=green>"
                + "  <input type=button name=enableChange id=enableChange value=Hello onClick='document.spectrum.change.disabled=true;'>"
                + "  <input type=submit name=change value=success>" + "</form>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        WebForm form = response.getFormWithName("spectrum");

        assertSubmitButtonEnabled(form);

        form = runJavaScriptToToggleEnabledStateOfButton(form, wc);
        assertNotNull(form);

        assertSubmitButtonDisabled(form);
        assertDisabledSubmitButtonCanNotBeClicked(form);
    }

    @Test
    void enablingDisabledNormalButtonViaScript() throws Exception {
        defineResource("DoIt?color=green", "You made it!");
        defineResource("OnCommand.html", "<html><head></head>" + "<body>" + "<form name=spectrum action='DoIt'>"
                + "  <input type=text name=color value=green>"
                + "  <input type=button name=enableChange id=enableChange value=Hello onClick='document.spectrum.changee.disabled=false;'>"
                + "  <input type=button disabled name=changee id=changee value=Hello onClick='document.spectrum.submit();'>"
                + "  <input type=submit name=change value=success>" + "</form>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        WebForm form = response.getFormWithName("spectrum");

        assertNormalButtonDisabled(form, "changee");
        assertDisabledNormalButtonCanNotBeClicked(form, "changee");

        form = runJavaScriptToToggleEnabledStateOfButton(form, wc);

        assertNormalButtonEnabled(form, "changee");
        clickButtonToProveThatItIsEnabled(form, "changee");
        assertEquals("You made it!", wc.getCurrentPage().getText(), "Result of submit");
    }

    @Test
    void disablingEnableddNormalButtonViaScript() throws Exception {
        defineResource("DoIt?color=green", "You made it!");
        defineResource("OnCommand.html", "<html><head></head>" + "<body>" + "<form name=spectrum action='DoIt'>"
                + "  <input type=text name=color value=green>"
                + "  <input type=button name=enableChange id=enableChange value=Hello onClick='document.spectrum.changee.disabled=true;'>"
                + "  <input type=button name=changee id=changee value=Hello onClick='document.spectrum.submit();'>"
                + "  <input type=submit name=change value=success>" + "</form>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        WebForm form = response.getFormWithName("spectrum");

        assertNormalButtonEnabled(form, "changee");
        form = runJavaScriptToToggleEnabledStateOfButton(form, wc);

        assertNormalButtonDisabled(form, "changee");
        assertDisabledNormalButtonCanNotBeClicked(form, "changee");
    }

    /**
     * also fix for [ 1124024 ] Formcontrol and isDisabled should be public by wolfgang fahl
     *
     * @param form
     */
    private void assertSubmitButtonDisabled(WebForm form) {
        assertTrue(form.getSubmitButton("change").isDisabled(), "Button should have been Disabled");
    }

    private void assertNormalButtonDisabled(WebForm form, String buttonID) {
        assertTrue(form.getButtonWithID(buttonID).isDisabled(), "Button should have been Disabled");
    }

    private void assertSubmitButtonEnabled(WebForm form) {
        assertFalse(form.getSubmitButton("change").isDisabled(), "Button should have been enabled or NOT-Disabled");
    }

    private void assertNormalButtonEnabled(WebForm form, String buttonID) {
        assertFalse(form.getButtonWithID(buttonID).isDisabled(), "Button should have been enabled or NOT-Disabled");
    }

    /**
     * click submit button to prove that it is enabled
     *
     * @param form
     *
     * @throws IOException
     * @throws SAXException
     */
    private void clickSubmitButtonToProveThatItIsEnabled(WebForm form) throws IOException, SAXException {
        WebResponse response = form.submit();
        assertNotNull(response);
    }

    private void clickButtonToProveThatItIsEnabled(WebForm form, String buttonID) throws IOException, SAXException {
        form.getButtonWithID(buttonID).click();
    }

    /**
     * change the enable State of Button via Javascript
     *
     * @param form
     * @param wc
     *
     * @return
     *
     * @throws IOException
     * @throws SAXException
     */
    private WebForm runJavaScriptToToggleEnabledStateOfButton(WebForm form, WebConversation wc)
            throws IOException, SAXException {
        Button enableChange = form.getButtonWithID("enableChange");
        enableChange.click();
        WebResponse currentPage = wc.getCurrentPage();
        return currentPage.getFormWithName("spectrum");
    }

    private void assertDisabledSubmitButtonCanNotBeClicked(WebForm form) {
        try {
            SubmitButton button = form.getSubmitButton("change");
            form.submit(button);
        } catch (Exception e) {
            String msg = e.getMessage();
            assertTrue(msg.indexOf(
                    "The specified button (name='change' value='success' is disabled and may not be used to submit this form") > -1);
        }
    }

    private void assertDisabledNormalButtonCanNotBeClicked(WebForm form, String buttonID) {
        try {
            Button button = form.getButtonWithID(buttonID);
            button.click();
        } catch (Exception e) {
            assertTrue(e.toString().indexOf("Button 'changee' is disabled and may not be clicked") > -1);
        }
    }

    @Test
    void enablingDisabledRadioButtonViaScript() throws Exception {
        defineResource("OnCommand.html", "<html><head></head>" + "<body>" + "<form name=spectrum action='DoIt'>"
                + "<input type='radio' name='color' value='red' checked>"
                + "<input type='radio' name='color' value='green' disabled>"
                + "<input type=button name=enableChange id=enableChange value=Hello onClick='document.spectrum.color[1].disabled=false;'>"
                + "<input type=submit name=change value=success>" + "</form>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        WebForm form = response.getFormWithName("spectrum");

        assertMatchingSet("Color choices", new String[] { "red" }, form.getOptionValues("color"));
        try {
            form.setParameter("color", "green");
            fail("Should not have been able to set color");
        } catch (Exception e) {
        }

        form.getScriptableObject().doEventScript("document.spectrum.color[1].disabled=false");

        assertMatchingSet("Color choices", new String[] { "red", "green" }, form.getOptionValues("color"));
        form.setParameter("color", "green");
    }

    @Test
    void submitViaScriptWithPostParams() throws Exception {
        defineResource("/servlet/TestServlet?param3=value3&param4=value4", new PseudoServlet() {
            @Override
            public WebResource getPostResponse() {
                return new WebResource("You made it!", "text/plain");
            }
        });
        defineResource("OnCommand.html",
                "<html><head></head>" + "<body>" + "<form method=POST enctype='multipart/form-data' name='TestForm'>"
                        + "  <input type=hidden name=param1 value='value1'>"
                        + "  <input type=text   name=param2 value=''>" + "</form>"
                        + "<a href='#' onclick='SubmitForm(\"/servlet/TestServlet?param3=value3&param4=value4\")'>"
                        + "<img SRC='/gifs/submit.gif' ALT='Submit' TITLE='Submit' NAME='Submit'></a>"
                        + "<script language=JavaScript type='text/javascript'>" + "  function SubmitForm(submitLink) {"
                        + "     var ltestForm = document.TestForm;" + "     ltestForm.action = submitLink;"
                        + "     ltestForm.submit();" + "  }" + "</script>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");

        response.getLinks()[0].click();
        assertEquals("You made it!", wc.getCurrentPage().getText(), "Result of submit");
    }

    @Test
    void submitButtonlessFormViaScript() throws Exception {
        defineResource("DoIt?color=green", "You made it!");
        defineResource("OnCommand.html",
                "<html><head></head>" + "<body>" + "<form name=spectrum action='DoIt'>"
                        + "  <input type=text name=color value=green>" + "</form>"
                        + "<a href='#' onClick='document.spectrum.submit(); return false;'>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");

        response.getLinks()[0].click();
        assertEquals("You made it!", wc.getCurrentPage().getText(), "Result of submit");
    }

    @Test
    void submitViaScriptButton() throws Exception {
        defineResource("DoIt?color=green", "You made it!");
        defineResource("OnCommand.html",
                "<html><head></head>" + "<body>" + "<form name=spectrum action='DoIt' onsubmit='return false;'>"
                        + "  <input type=text name=color value=green>"
                        + "  <input type=button id=submitButton value=submit onClick='this.form.submit();'>" + "</form>"
                        + "<a href='#' onClick='document.spectrum.submit(); return false;'>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");

        response.getFormWithName("spectrum").getButtons()[0].click();
        assertEquals("You made it!", wc.getCurrentPage().getText(), "Result of submit");
    }

    @Test
    void disabledScriptButton() throws Exception {
        defineResource("DoIt?color=green", "You made it!");
        defineResource("OnCommand.html",
                "<html><head></head>" + "<body>" + "<form name=spectrum action='DoIt' onsubmit='return false;'>"
                        + "  <input type=text name=color value=green>"
                        + "  <input type=button id=submitButton disabled value=submit onClick='this.form.submit();'>"
                        + "</form>" + "<a href='#' onClick='document.spectrum.submit(); return false;'>"
                        + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");

        try {
            response.getFormWithName("spectrum").getButtons()[0].click();
            fail("Should not have permitted click of disabled button");
        } catch (IllegalStateException e) {
        }
    }

    @Test
    void updateBeforeSubmit() throws Exception {
        defineResource("DoIt?color=green", "You made it!");
        defineResource("OnCommand.html",
                "<html><head></head>" + "<body>" + "<form name=spectrum action='DoIt'>"
                        + "  <input type=text name=color value=red>"
                        + "  <input type=submit onClick='form.color.value=\"green\";'>" + "</form>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");

        response.getFormWithName("spectrum").getButtons()[0].click();
        assertEquals("You made it!", wc.getCurrentPage().getText(), "Result of submit");
    }

    @Test
    void submitButtonScript() throws Exception {
        defineResource("DoIt?color=green", "You made it!");
        defineResource("OnCommand.html",
                "<html><head></head>" + "<body>" + "<form name=spectrum action='DoIt'>"
                        + "  <input type=text name=color value=red>"
                        + "  <input type=submit onClick='form.color.value=\"green\";'>" + "</form>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");

        response.getFormWithName("spectrum").submit();
        assertEquals("You made it!", wc.getCurrentPage().getText(), "Result of submit");
    }

    @Test
    void setFormTextValue() throws Exception {
        defineResource("OnCommand.html",
                "<html><head></head>" + "<body onLoad=\"document.realform.color.value='green'\">"
                        + "<form name='realform'><input name='color' value='blue'></form>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        WebForm form = response.getFormWithName("realform");
        assertEquals("green", form.getParameterValue("color"), "color parameter value");
    }

    /**
     * test for onMouseDownEvent support patch 884146 by Bjoern Beskow - bbeskow
     *
     * @throws Exception
     */
    @Test
    void checkboxOnMouseDownEvent() throws Exception {
        defineResource("OnCommand.html",
                "<html><head></head>" + "<body>" + "<form name='the_form'>"
                        + "  <input type='checkbox' name='color' value='blue' "
                        + "         onMouseDown='alert( \"color-blue is now \" + document.the_form.color.checked );'>"
                        + "</form>" + "<a href='#' onMouseDown='document.the_form.color.checked=true;'>blue</a>"
                        + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        WebForm form = response.getFormWithName("the_form");
        assertNull(form.getParameterValue("color"), "Initial state");

        assertNull(wc.getNextAlert(), "Alert message before change");
        form.removeParameter("color");
        assertNull(wc.getNextAlert(), "Alert message w/o change");
        form.setParameter("color", "blue");
        assertEquals("color-blue is now true", wc.popNextAlert(), "Alert after change");
        form.removeParameter("color");
        assertEquals("color-blue is now false", wc.popNextAlert(), "Alert after change");

        assertNull(form.getParameterValue("color"), "Changed state");
        response.getLinks()[0].click();
        assertEquals("blue", form.getParameterValue("color"), "Final state");
        assertNull(wc.getNextAlert(), "Alert message after JavaScript change");
    }

    /**
     * test the onChange event
     *
     * @throws Exception
     */
    @Test
    void setFormTextOnChangeEvent() throws Exception {
        defineResource("OnCommand.html",
                "<html><head></head>" + "<body>" + "<form name='the_form'>" + "  <input name='color' value='blue' "
                        + "         onChange='alert( \"color is now \" + document.the_form.color.value );'>" + "</form>"
                        + "<a href='#' onClick='document.the_form.color.value=\"green\";'>green</a>"
                        + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        WebForm form = response.getFormWithName("the_form");
        assertEquals("blue", form.getParameterValue("color"), "Initial state");

        assertNull(wc.getNextAlert(), "Alert message before change");
        form.setParameter("color", "red");
        assertEquals("color is now red", wc.popNextAlert(), "Alert after change");

        assertEquals("red", form.getParameterValue("color"), "Changed state");
        response.getLinks()[0].click();
        assertEquals("green", form.getParameterValue("color"), "Final state");
        assertNull(wc.getNextAlert(), "Alert message after JavaScript change");
    }

    @Test
    void checkboxProperties() throws Exception {
        defineResource("OnCommand.html", "<html><head><script language='JavaScript'>"
                + "function viewCheckbox( checkbox ) { \n"
                + "  alert( 'checkbox ' + checkbox.name + ' default = ' + checkbox.defaultChecked )\n;"
                + "  alert( 'checkbox ' + checkbox.name + ' checked = ' + checkbox.checked )\n;"
                + "  alert( 'checkbox ' + checkbox.name + ' value = ' + checkbox.value )\n;" + "}\n"
                + "</script></head>" + "<body>"
                + "<form name='realform'><input type='checkbox' name='ready' value='good'></form>"
                + "<a href='#' name='clear' onMouseOver='document.realform.ready.checked=false;'>clear</a>"
                + "<a href='#' name='set' onMouseOver='document.realform.ready.checked=true;'>set</a>"
                + "<a href='#' name='change' onMouseOver='document.realform.ready.value=\"waiting\";'>change</a>"
                + "<a href='#' name='report' onMouseOver='viewCheckbox( document.realform.ready );'>report</a>"
                + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        WebForm form = response.getFormWithName("realform");
        response.getLinkWithName("report").mouseOver();
        verifyCheckbox( /* default */ wc, false, /* checked */ false, /* value */ "good");

        assertNull(form.getParameterValue("ready"), "initial parameter value");
        response.getLinkWithName("set").mouseOver();
        assertEquals("good", form.getParameterValue("ready"), "changed parameter value");
        response.getLinkWithName("clear").mouseOver();
        assertNull(form.getParameterValue("ready"), "final parameter value");
        response.getLinkWithName("change").mouseOver();
        assertNull(form.getParameterValue("ready"), "final parameter value");
        response.getLinkWithName("report").mouseOver();
        verifyCheckbox( /* default */ wc, false, /* checked */ false, /* value */ "waiting");
        form.setParameter("ready", "waiting");
    }

    @Test
    void indexedCheckboxProperties() throws Exception {
        defineResource("OnCommand.html", "<html><head><script language='JavaScript'>"
                + "function viewCheckbox( checkbox ) { \n"
                + "  alert( 'checkbox ' + checkbox.name + ' default = ' + checkbox.defaultChecked )\n;"
                + "  alert( 'checkbox ' + checkbox.name + ' checked = ' + checkbox.checked )\n;"
                + "  alert( 'checkbox ' + checkbox.name + ' value = ' + checkbox.value )\n;" + "}\n"
                + "</script></head>"
                + "<body onload='viewCheckbox( document.realform.ready[0] ); viewCheckbox( document.realform.ready[1] );'>"
                + "<form name='realform'>" + "<input type='checkbox' name='ready' value='good' checked>"
                + "<input type='checkbox' name='ready' value='bad'>" + "</form>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getFormWithName("realform");
        verifyCheckbox( /* default */ wc, true, /* checked */ true, /* value */ "good");
        verifyCheckbox( /* default */ wc, false, /* checked */ false, /* value */ "bad");
    }

    private void verifyCheckbox(WebClient wc, boolean defaultChecked, boolean checked, String value) {
        assertEquals("checkbox ready default = " + defaultChecked, wc.popNextAlert(), "Message " + 1 + "-1");
        assertEquals("checkbox ready checked = " + checked, wc.popNextAlert(), "Message " + 1 + "-2");
        assertEquals("checkbox ready value = " + value, wc.popNextAlert(), "Message " + 1 + "-3");
    }

    @Test
    void checkboxOnClickEvent() throws Exception {
        defineResource("OnCommand.html", "<html><head></head>" + "<body>" + "<form name='the_form'>"
                + "  <input type='checkbox' name='color' value='blue' "
                + "         onClick='alert( \"color-blue is now \" + document.the_form.color.checked );'>" + "</form>"
                + "<a href='#' onClick='document.the_form.color.checked=true;'>blue</a>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        WebForm form = response.getFormWithName("the_form");
        assertNull(form.getParameterValue("color"), "Initial state");

        assertNull(wc.getNextAlert(), "Alert message before change");
        form.removeParameter("color");
        assertNull(wc.getNextAlert(), "Alert message w/o change");
        form.setParameter("color", "blue");
        assertEquals("color-blue is now true", wc.popNextAlert(), "Alert after change");
        form.removeParameter("color");
        assertEquals("color-blue is now false", wc.popNextAlert(), "Alert after change");

        assertNull(form.getParameterValue("color"), "Changed state");
        response.getLinks()[0].click();
        assertEquals("blue", form.getParameterValue("color"), "Final state");
        assertNull(wc.getNextAlert(), "Alert message after JavaScript change");
    }

    @Test
    void setCheckboxOnClickEvent() throws Exception {
        defineResource("OnCommand.html",
                "<html><head></head>" + "<body>" + "<form name='the_form'>"
                        + "  <input type='checkbox' name='color' value='blue' "
                        + "         onClick='alert( \"color-blue is now \" + document.the_form.color.checked );'>"
                        + "</form>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        WebForm form = response.getFormWithName("the_form");
        form.toggleCheckbox("color");
        assertEquals("color-blue is now true", wc.popNextAlert(), "Alert after change");
        form.setCheckbox("color", false);
        assertEquals("color-blue is now false", wc.popNextAlert(), "Alert after change");
    }

    /**
     * test the radio button properties via index
     *
     * @throws Exception
     */
    @Test
    void indexedRadioProperties() throws Exception {
        defineResource("OnCommand.html", "<html><head><script language='JavaScript'>"
                + "function viewRadio( radio ) { \n"
                + "  alert( 'radio ' + radio.name + ' default = ' + radio.defaultChecked )\n;"
                + "  alert( 'radio ' + radio.name + ' checked = ' + radio.checked )\n;"
                + "  alert( 'radio ' + radio.name + ' value = ' + radio.value )\n;" + "}\n" + "</script></head>"
                + "<body onload='viewRadio( document.realform.ready[0] ); viewRadio( document.realform.ready[1] );'>"
                + "<form name='realform'>" + "<input type='radio' name='ready' value='good' checked>"
                + "<input type='radio' name='ready' value='bad'>" + "</form>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getFormWithName("realform");
        verifyRadio( /* default */ wc, true, /* checked */ true, /* value */ "good");
        verifyRadio( /* default */ wc, false, /* checked */ false, /* value */ "bad");
    }

    /**
     * test onMouseDownEvent for radio buttons
     *
     * @throws Exception
     */
    @Test
    void radioOnMouseDownEvent() throws Exception {
        defineResource("OnCommand.html", "<html><head></head>" + "<body>" + "<form name='the_form'>"
                + "  <input type='radio' name='color' value='blue' "
                + "         onMouseDown='alert( \"color is now blue\" );'>"
                + "  <input type='radio' name='color' value='red' checked"
                + "         onMouseDown='alert( \"color is now red\" );'>" + "</form>"
                + "<a href='#' onMouseDown='document.the_form.color[1].checked=false; document.the_form.color[0].checked=true;'>blue</a>"
                + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        WebForm form = response.getFormWithName("the_form");
        assertEquals("red", form.getParameterValue("color"), "Initial state");

        assertNull(wc.getNextAlert(), "Alert message before change");
        form.setParameter("color", "red");
        assertNull(wc.getNextAlert(), "Alert message w/o change");
        form.setParameter("color", "blue");
        assertEquals("color is now blue", wc.popNextAlert(), "Alert after change");
        form.setParameter("color", "red");
        assertEquals("color is now red", wc.popNextAlert(), "Alert after change");

        assertEquals("red", form.getParameterValue("color"), "Changed state");
        response.getLinks()[0].click();
        assertEquals("blue", form.getParameterValue("color"), "Final state");
        assertNull(wc.getNextAlert(), "Alert message after JavaScript change");

    }

    private void verifyRadio(WebClient wc, boolean defaultChecked, boolean checked, String value) {
        assertEquals("radio ready default = " + defaultChecked, wc.popNextAlert(), "Message " + 1 + "-1");
        assertEquals("radio ready checked = " + checked, wc.popNextAlert(), "Message " + 1 + "-2");
        assertEquals("radio ready value = " + value, wc.popNextAlert(), "Message " + 1 + "-3");
    }

    @Test
    void radioOnClickEvent() throws Exception {
        defineResource("OnCommand.html", "<html><head></head>" + "<body>" + "<form name='the_form'>"
                + "  <input type='radio' name='color' value='blue' "
                + "         onClick='alert( \"color is now blue\" );'>"
                + "  <input type='radio' name='color' value='red' checked"
                + "         onClick='alert( \"color is now red\" );'>" + "</form>"
                + "<a href='#' onClick='document.the_form.color[1].checked=false; document.the_form.color[0].checked=true;'>blue</a>"
                + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        WebForm form = response.getFormWithName("the_form");
        assertEquals("red", form.getParameterValue("color"), "Initial state");

        assertNull(wc.getNextAlert(), "Alert message before change");
        form.setParameter("color", "red");
        assertNull(wc.getNextAlert(), "Alert message w/o change");
        form.setParameter("color", "blue");
        assertEquals("color is now blue", wc.popNextAlert(), "Alert after change");
        form.setParameter("color", "red");
        assertEquals("color is now red", wc.popNextAlert(), "Alert after change");

        assertEquals("red", form.getParameterValue("color"), "Changed state");
        response.getLinks()[0].click();
        assertEquals("blue", form.getParameterValue("color"), "Final state");
        assertNull(wc.getNextAlert(), "Alert message after JavaScript change");
    }

    @Test
    void formActionProperty() throws Exception {
        WebConversation wc = new WebConversation();
        defineWebPage("Default",
                "<form method=GET name='the_form' action = 'ask'>" + "<Input type=text name=age>"
                        + "<Input type=submit value=Go>" + "</form>"
                        + "<a href='#' name='doTell' onClick='document.the_form.action=\"tell\";'>tell</a>"
                        + "<a href='#' name='doShow' onClick='alert( document.the_form.action );'>show</a>");
        WebResponse page = wc.getResponse(getHostPath() + "/Default.html");
        page.getLinkWithName("doShow").click();
        assertEquals("ask", wc.popNextAlert(), "Current action");
        page.getLinkWithName("doTell").click();

        WebRequest request = page.getForms()[0].getRequest();
        request.setParameter("age", "23");
        assertEquals(getHostPath() + "/tell?age=23", request.getURL().toExternalForm());
    }

    @Test
    void formTargetProperty() throws Exception {
        WebConversation wc = new WebConversation();
        defineWebPage("Default",
                "<form method=GET name='the_form' action = 'ask'>" + "<Input type=text name=age>"
                        + "<Input type=submit value=Go>" + "</form>"
                        + "<a href='#' name='doTell' onClick='document.the_form.target=\"_blank\";'>tell</a>"
                        + "<a href='#' name='doShow' onClick='alert( document.the_form.target );'>show</a>");
        WebResponse page = wc.getResponse(getHostPath() + "/Default.html");
        page.getLinkWithName("doShow").click();
        assertEquals("_top", wc.popNextAlert(), "Initial target");
        page.getLinkWithName("doTell").click();
        page.getLinkWithName("doShow").click();
        assertEquals("_blank", wc.popNextAlert(), "Current target");

        WebRequest request = page.getForms()[0].getRequest();
        assertEquals("_blank", request.getTarget());
    }

    @Test
    void formValidationOnSubmit() throws Exception {
        defineResource("doIt?color=pink", "You got it!", "text/plain");
        defineResource("OnCommand.html",
                "<html><head><script language='JavaScript'>" + "function verifyForm() { "
                        + "  if (document.realform.color.value == 'pink') {" + "    return true;" + "  } else {"
                        + "    alert( 'wrong color' );" + "    return false;" + "  }" + "}" + "</script></head>"
                        + "<body>" + "<form name='realform' action='doIt' onSubmit='return verifyForm();'>"
                        + "  <input name='color' value='blue'>" + "</form>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        WebForm form = response.getFormWithName("realform");
        form.submit();
        assertEquals("wrong color", wc.popNextAlert(), "Alert message");
        assertSame(response, wc.getCurrentPage(), "Current response");
        form.setParameter("color", "pink");
        WebResponse newResponse = form.submit();
        assertEquals("You got it!", newResponse.getText(), "Result of submit");
    }

    @Test
    void formSelectReadableProperties() throws Exception {
        defineResource("OnCommand.html", "<html><head><script language='JavaScript'>"
                + "function viewSelect( choices ) { \n"
                + "  alert( 'select has ' + choices.options.length + ' options' )\n;"
                + "  alert( 'select still has ' + choices.length + ' options' )\n;"
                + "  alert( 'select option ' + choices.options[0].index + ' is ' + choices.options[0].text )\n;"
                + "  alert( 'select 2nd option value is ' + choices.options[1].value )\n;"
                + "  if (choices.options[0].selected) alert( 'red selected' );\n"
                + "  if (choices.options[1].selected) alert( 'blue selected' );\n"
                + "  if (choices[1].selected) alert( 'blue selected again' );\n" + "}\n" + "</script></head>"
                + "<body onLoad='viewSelect( document.the_form.choices )'>" + "<form name='the_form'>"
                + "  <select name='choices'>" + "    <option value='1'>red" + "    <option value='3' selected>blue"
                + "  </select>" + "</form>"
                + "<a href='#' onMouseOver=\"alert( 'selected #' + document.the_form.choices.selectedIndex );\">which</a>"
                + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals("select has 2 options", wc.popNextAlert(), "1st message");
        assertEquals("select still has 2 options", wc.popNextAlert(), "2nd message");
        assertEquals("select option 0 is red", wc.popNextAlert(), "3rd message");
        assertEquals("select 2nd option value is 3", wc.popNextAlert(), "4th message");
        assertEquals("blue selected", wc.popNextAlert(), "5th message");
        assertEquals("blue selected again", wc.popNextAlert(), "6th message");

        response.getLinks()[0].mouseOver();
        assertEquals("selected #1", wc.popNextAlert(), "before change message");
        response.getFormWithName("the_form").setParameter("choices", "1");
        response.getLinks()[0].mouseOver();
        assertEquals("selected #0", wc.popNextAlert(), "after change message");
    }

    /**
     * test that in case of an Index out of bounds problem an exception is thrown with a meaningful message (not
     * nullpointer exception) Bug report [ 1124057 ] Out of Bounds Exception should be avoided by Wolfgang Fahl of
     * 2005-02-16 17:25
     */
    @Test
    void selectIndexOutOfBoundsCatching() throws Exception {
        defineResource("OnCommand.html", "<html><head><script language='JavaScript'>"
                + "function viewSelect( choices ) { \n" + " // try accessing out of bounds\n"
                + "  alert( choices.options[5].value )\n;" + "}\n" + "</script></head>"
                + "<body onLoad='viewSelect( document.the_form.choices )'>" + "<form name='the_form'>"
                + "  <select name='choices'>" + "    <option value='1'>red" + "    <option value='3' selected>blue"
                + "  </select>" + "</form>"
                + "<a href='#' onMouseOver=\"alert( 'selected #' + document.the_form.choices.selectedIndex );\">which</a>"
                + "</body></html>");
        WebConversation wc = new WebConversation();
        boolean oldDebug = HttpUnitUtils.setEXCEPTION_DEBUG(false);
        try {
            wc.getResponse(getHostPath() + "/OnCommand.html");
            fail("There should be a runtime exeption here");
            // java.lang.RuntimeException: Event 'viewSelect( document.the_form.choices )' failed:
            // java.lang.RuntimeException: invalid index 5 for Options redblue,
        } catch (java.lang.RuntimeException rte) {
            assertTrue(rte.getMessage().indexOf("invalid index 5 for Options red,blue") > 0);
        } finally {
            HttpUnitUtils.setEXCEPTION_DEBUG(oldDebug);
        }
    }

    @Test
    void formSelectDefaults() throws Exception {
        defineResource("OnCommand.html",
                "<html><head><script language='JavaScript'>" + "function viewSelect( form ) { \n"
                        + "  alert( 'first default index= '  + form.first.selectedIndex )\n;"
                        + "  alert( 'second default index= ' + form.second.selectedIndex )\n;"
                        + "  alert( 'third default index= '  + form.third.selectedIndex )\n;"
                        + "  alert( 'fourth default index= ' + form.fourth.selectedIndex )\n;" + "}\n"
                        + "</script></head>" + "<body onLoad='viewSelect( document.the_form )'>"
                        + "<form name='the_form'>"
                        + "  <select name='first'><option value='1'>red<option value='3'>blue</select>"
                        + "  <select name='second' multiple><option value='1'>red<option value='3'>blue</select>"
                        + "  <select name='third' size=2><option value='1'>red<option value='3'>blue</select>"
                        + "  <select name='fourth' multiple size=1><option value='1'>red<option value='3'>blue</select>"
                        + "</form>" + "</body></html>");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals("first default index= 0", wc.popNextAlert(), "1st message");
        assertEquals("second default index= -1", wc.popNextAlert(), "2nd message");
        assertEquals("third default index= -1", wc.popNextAlert(), "3rd message");
        assertEquals("fourth default index= 0", wc.popNextAlert(), "4th message");
    }

    @Test
    void fileSubmitProperties() throws Exception {
        File file = new File("temp.html");
        defineResource("OnCommand.html", "<html><head></head>" + "<body'>" + "<form name='the_form'>"
                + "  <input type='file' name='file'>" + "</form>"
                + "<a href='#' onMouseOver=\"alert( 'file selected is [' + document.the_form.file.value + ']' );\">which</a>"
                + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinks()[0].mouseOver();
        assertEquals("file selected is []", wc.popNextAlert(), "1st message");

        WebForm form = response.getFormWithName("the_form");
        form.setParameter("file", new UploadFileSpec[] { new UploadFileSpec(file) });
        response.getLinks()[0].mouseOver();
        assertEquals("file selected is [" + file.getAbsolutePath() + "]", wc.popNextAlert(), "2nd message");
    }

    @Test
    void formSelectOnChangeEvent() throws Exception {
        defineResource("OnCommand.html", "<html><head><script language='JavaScript'>"
                + "function selectOptionNum( the_select, index ) { \n"
                + "  for (var i = 0; i < the_select.length; i++) {\n"
                + "      the_select.options[i].selected = (i == index);\n" + "  }\n" + "}\n" + "</script></head>"
                + "<body>" + "<form name='the_form'>"
                + "  <select name='choices' onChange='alert( \"Selected index is \" + document.the_form.choices.selectedIndex );'>"
                + "    <option>red" + "    <option selected>blue" + "  </select>" + "</form>"
                + "<a href='#' onClick='selectOptionNum( document.the_form.choices, 0 )'>reset</a>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        final WebForm form = response.getFormWithName("the_form");
        assertEquals("blue", form.getParameterValue("choices"), "Initial state");

        assertNull(wc.getNextAlert(), "Alert message before change");
        form.setParameter("choices", "red");
        assertEquals("Selected index is 0", wc.popNextAlert(), "Alert after change");
        form.setParameter("choices", "blue");
        assertEquals("Selected index is 1", wc.popNextAlert(), "Alert after change");

        assertEquals("blue", form.getParameterValue("choices"), "Initial state");
        response.getLinks()[0].click();
        assertEquals("red", form.getParameterValue("choices"), "Final state");
        assertNull(wc.getNextAlert(), "Alert message after JavaScript change");
    }

    @Test
    void formSelectWriteableProperties() throws Exception {
        defineResource("OnCommand.html", "<html><head></head>" + "<body>" + "<form name='the_form'>"
                + "  <select name='choices'>" + "    <option value='1'>red" + "    <option value='3'>blue"
                + "    <option value='5'>green" + "    <option value='7'>azure" + "  </select>" + "</form>"
                + "<a href='#' onclick='alert( \"Selected index is \" + document.the_form.choices.selectedIndex );'>"
                + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        WebForm form = response.getFormWithName("the_form");
        assertEquals("1", form.getParameterValue("choices"), "initial selection");

        response.getLinks()[0].click();
        assertEquals("Selected index is 0", wc.popNextAlert(), "Notification");
    }

    @Test
    void formSelectDefaultProperties() throws Exception {
        defineResource("OnCommand.html",
                "<html><head><script language='JavaScript'>" + "function selectOptionNum( the_select, index ) { \n"
                        + "  for (var i = 0; i < the_select.length; i++) {\n"
                        + "      if (i == index) the_select.options[i].selected = true;\n" + "  }\n" + "}\n"
                        + "</script></head>" + "<body>" + "<form name='the_form'>" + "  <select name='choices'>"
                        + "    <option value='1'>red" + "    <option value='3' selected>blue"
                        + "    <option value='5'>green" + "    <option value='7'>azure" + "  </select>" + "</form>"
                        + "<a href='#' onClick='selectOptionNum( document.the_form.choices, 2 )'>green</a>"
                        + "<a href='#' onClick='selectOptionNum( document.the_form.choices, 0 )'>red</a>"
                        + "<a href='#' onClick='document.the_form.choices.options[0].value=\"9\"'>red</a>"
                        + "<a href='#' onClick='document.the_form.choices.options[0].text=\"orange\"'>orange</a>"
                        + "<a href='#' onClick='document.the_form.choices.selectedIndex=3'>azure</a>"
                        + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        WebForm form = response.getFormWithName("the_form");
        assertEquals("3", form.getParameterValue("choices"), "initial selection");

        response.getLinks()[0].click();
        assertEquals("5", form.getParameterValue("choices"), "2nd selection");
        response.getLinks()[1].click();
        assertEquals("1", form.getParameterValue("choices"), "3rd selection");
        response.getLinks()[2].click();
        assertEquals("9", form.getParameterValue("choices"), "4th selection");

        assertMatchingSet("Displayed options", new String[] { "red", "blue", "green", "azure" },
                form.getOptions("choices"));
        response.getLinks()[3].click();
        assertMatchingSet("Modified options", new String[] { "orange", "blue", "green", "azure" },
                form.getOptions("choices"));
        response.getLinks()[4].click();
        assertEquals("7", form.getParameterValue("choices"), "5th selection");
    }

    @Test
    void formSelectOverwriteOptions() throws Exception {
        defineResource("OnCommand.html",
                "<html><head><script language='JavaScript'>" + "function rewriteSelect( the_select ) { \n"
                        + "  the_select.options[0] = new Option( 'apache', 'a' );\n"
                        + "  the_select.options[1] = new Option( 'comanche', 'c' );\n"
                        + "  the_select.options[2] = new Option( 'sioux', 'x' );\n"
                        + "  the_select.options[3] = new Option( 'iriquois', 'q' );\n" + "}\n" + "</script></head>"
                        + "<body>" + "<form name='the_form'>" + "  <select name='choices'>"
                        + "    <option value='1'>red" + "    <option value='2'>yellow"
                        + "    <option value='3' selected>blue" + "    <option value='5'>green" + "  </select>"
                        + "</form>"
                        + "<a href='#' onMouseOver='document.the_form.choices.options.length=3;'>shorter</a>"
                        + "<a href='#' onMouseOver='document.the_form.choices.options[1]=null;'>weed</a>"
                        + "<a href='#' onMouseOver='rewriteSelect( document.the_form.choices );'>replace</a>"
                        + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        WebForm form = response.getFormWithName("the_form");
        assertMatchingSet("initial values", new String[] { "1", "2", "3", "5" }, form.getOptionValues("choices"));
        assertMatchingSet("initial text", new String[] { "red", "yellow", "blue", "green" },
                form.getOptions("choices"));

        response.getLinks()[0].mouseOver();
        assertMatchingSet("modified values", new String[] { "1", "2", "3" }, form.getOptionValues("choices"));
        assertMatchingSet("modified text", new String[] { "red", "yellow", "blue" }, form.getOptions("choices"));

        response.getLinks()[1].mouseOver();
        assertMatchingSet("weeded values", new String[] { "1", "3" }, form.getOptionValues("choices"));
        assertMatchingSet("weeded text", new String[] { "red", "blue" }, form.getOptions("choices"));

        response.getLinks()[2].mouseOver();
        assertMatchingSet("replaced values", new String[] { "a", "c", "x", "q" }, form.getOptionValues("choices"));
        assertMatchingSet("replaced text", new String[] { "apache", "comanche", "sioux", "iriquois" },
                form.getOptions("choices"));
    }

    @Test
    void accessAcrossFrames() throws Exception {
        defineResource("First.html",
                "<html><head><script language='JavaScript'>" + "function accessOtherFrame() {"
                        + "  top.frame2.document.testform.param1.value = 'new1';"
                        + "  window.alert('value: ' + top.frame2.document.testform.param1.value);" + "}"
                        + "</script><body onload='accessOtherFrame();'>" + "</body></html>");
        defineWebPage("Second", "<form method=post name=testform action='http://trinity/dummy'>"
                + "  <input type=hidden name='param1' value='old1'></form>");
        defineResource("Frames.html",
                "<html><head><title>Initial</title></head>" + "<frameset cols=\"20%,80%\">"
                        + "    <frame src='First.html' name='frame1'>" + "    <frame src='Second.html' name='frame2'>"
                        + "</frameset></html>");

        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/Frames.html");
        assertEquals("value: new1", wc.popNextAlert(), "Alert message");
    }

    @Test
    void setFromEmbeddedScript() throws Exception {
        defineWebPage("OnCommand",
                "<form name=\"testform\">" + "<input type=text name=\"testfield\" value=\"old\">" + "</form>"
                        + "<script language=\"JavaScript\">" + "  document.testform.testfield.value=\"new\""
                        + "</script>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals("new", response.getForms()[0].getParameterValue("testfield"), "Form parameter value");
    }

    @Test
    void submitFromJavaScriptLink() throws Exception {
        defineResource("test2.txt?Submit=Submit", "You made it!", "text/plain");
        defineWebPage("OnCommand",
                "<form name='myform' action='test2.txt'>"
                        + "  <input type='submit' id='btn' name='Submit' value='Submit'/>"
                        + "  <a href='javascript:document.myform.btn.click();'>Link</a>" + "</form>");
        WebConversation wc = new WebConversation();
        WebResponse wr = wc.getResponse(getHostPath() + "/OnCommand.html");
        wr.getLinkWith("Link").click();
    }

    @Test
    void submitOnLoad() throws Exception {
        defineResource("test2.txt?Submit=Submit", "You made it!", "text/plain");
        defineResource("OnCommand.html",
                "<html><body onload='document.myform.btn.click();'>" + "<form name='myform' action='test2.txt'>"
                        + "  <input type='submit' id='btn' name='Submit' value='Submit'/>" + "</form></body></html>");
        WebConversation wc = new WebConversation();
        WebResponse wr = wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals(getHostPath() + "/test2.txt?Submit=Submit", wc.getCurrentPage().getURL().toExternalForm(),
                "current page URL");
        assertEquals("You made it!", wc.getCurrentPage().getText(), "current page");
        assertEquals(getHostPath() + "/test2.txt?Submit=Submit", wr.getURL().toExternalForm(), "returned page URL");
        assertEquals("You made it!", wr.getText(), "returned page");
    }

    @Test
    void selectValueProperty() throws Exception {
        defineResource("OnCommand.html",
                "<html><head><script language='JavaScript'>" + "function testProperty( form ) {\n"
                        + "   elements = form.choices;\n" + "   alert( 'selected item is ' + elements.value );\n" + "}"
                        + "</script></head>" + "<body>" + "<form name='the_form'>" + "   <select name='choices'>"
                        + "      <option>red" + "      <option selected>blue" + "   </select>" + "</form>"
                        + "<a href='#' onClick='testProperty( document.the_form )'>elements</a>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinks()[0].click();
        assertEquals("selected item is blue", wc.popNextAlert(), "Message 1");
        response.getScriptingHandler().doEventScript("document.the_form.choices.value='red'");
        response.getLinks()[0].click();
        assertEquals("selected item is red", wc.popNextAlert(), "Message 2");
    }

    @Test
    void elementsByIDProperty() throws Exception {
        defineResource("index.html",
                "<html>\n" + "<head>\n" + "<title>JavaScript Form Elements by ID String Test</title>\n"
                        + "<script language='JavaScript' type='text/javascript'><!--\n" + "function foo() {\n"
                        + "  if (document.forms['formName']) {\n" + "    var form = document.forms['formName'];\n"
                        + "    if (form.elements['inputID']) {\n"
                        + "      form.elements['inputID'].value = 'Hello World!';\n" + "    }\n" + "  }\n" + "}\n"
                        + "// --></script>\n" + "</head>\n" + "<body onLoad='foo();'>\n"
                        + "<h1>JavaScript Form Elements by ID String Test</h1>\n" + "<form name='formName'>\n"
                        + "  <input type='text' name='inputName' value='' id='inputID'>\n" + "</form>\n" + "</body>\n"
                        + "</html>\n");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/index.html");
        WebForm form = response.getFormWithName("formName");
        assertEquals("Hello World!", form.getParameterValue("inputName"), "Changed value");
    }

    /**
     * Test that JavaScript can correctly access the 'type' property for every kind of form control.
     *
     * @throws Exception
     */
    @Test
    void elementTypeAccess() throws Exception {
        defineWebPage("Default", "<script language=JavaScript>\n" + "function CheckForm() {\n"
                + "  var len = document.myForm.elements.length;\n" + "  for (var index = 0; index < len; index++) {\n"
                + "    var control = document.myForm.elements[index];\n" + "    confirm(control.type);\n" + "  }\n"
                + "  return true;\n" + "}\n" + "</script>" + "<form name=myForm method=POST>"
                + "  <input type=\"text\" name=\"textfield\">" + "  <textarea name=\"textarea\"></textarea>"
                + "  <input type=\"password\" name=\"password\">"
                + "  <input type=\"submit\" name=\"submit\" value=\"Submit\" onClick=\"return Check()\">"
                + "  <input type=\"reset\" name=\"reset\" value=\"Reset\">"
                + "  <input type=\"button\" name=\"button\" value=\"Button\">"
                + "  <input type=\"checkbox\" name=\"checkbox\" value=\"checkbox\">"
                + "  <input type=\"radio\" name=\"radiobutton\" value=\"radiobutton\">" + "  <select name=\"select\">"
                + "    <option value=\"1\">One</option>" + "    <option value=\"2\">Two</option>" + "  </select>"
                + "  <select name=\"select2\" size=\"2\" multiple>" + "    <option value=\"1\">One</option>"
                + "    <option value=\"2\">Two</option>" + "  </select>" + "  <input type=\"file\" name=\"fileField\">"
                + "  <input type=\"image\" name=\"imageField\" src=\"img.gif\">"
                + "  <input type=\"hidden\" name=\"hiddenField\">"
                + "  <button name=\"html4-button\" type=\"button\">html4-button</button>"
                + "  <button name=\"html4-submit\" type=\"submit\">html4-submit</button>"
                + "  <button name=\"html4-reset\" type=\"reset\">html4-reset</button>"
                + "  <button name=\"html4-default\">html4-default</button>" + "</form>"
                + "<script language=JavaScript>\n" + "  CheckForm();\n" + "</script>\n");

        String[] expectedTypes = { "text", "textarea", "password", "submit", "reset", "button", "checkbox", "radio",
                "select-one", "select-multiple", "file", "image", "hidden", "button", "submit", "reset", "submit" };

        final PromptCollector collector = new PromptCollector();
        WebConversation wc = new WebConversation();
        wc.setDialogResponder(collector);
        wc.getResponse(getHostPath() + "/Default.html");
        assertMatchingSet("Set of types on form", expectedTypes, collector.confirmPromptsSeen.toArray());
    }

    static class PromptCollector implements DialogResponder {
        public List confirmPromptsSeen = new ArrayList<>();
        public List responsePromptSeen = new ArrayList<>();

        @Override
        public boolean getConfirmation(String confirmationPrompt) {
            confirmPromptsSeen.add(confirmationPrompt);
            return true;
        }

        @Override
        public String getUserResponse(String prompt, String defaultResponse) {
            responsePromptSeen.add(prompt);
            return null;
        }
    }

    /**
     * Test that the length (number of controls) of a form can be accessed from JavaScript.
     *
     * @throws Exception
     */
    @Test
    void formLength() throws Exception {
        defineWebPage("Default", "<script language=JavaScript>\n" + "function CheckForm()\n" + "{\n"
                + "confirm (document.myForm.length);\n" + "return true;\n" + "}\n" + "</script>"
                + "<form name=myForm method=POST>" + "  <input type=\"text\" name=\"first_name\" value=\"Fred\">"
                + "  <input type=\"text\" name=\"last_name\" value=\"Bloggs\">" + "</form>"
                + "<script language=JavaScript>\n" + "  CheckForm();\n" + "</script>\n");

        String[] expectedPrompts = { "2" };

        final PromptCollector collector = new PromptCollector();
        WebConversation wc = new WebConversation();
        wc.setDialogResponder(collector);
        wc.getResponse(getHostPath() + "/Default.html");
        assertMatchingSet("Length of form", expectedPrompts, collector.confirmPromptsSeen.toArray());
    }

    /**
     * Verifies that it is possible to increase the size of a select control.
     *
     * @throws Exception
     *             on any unexpected error
     */
    @Test
    void increaseSelectLength() throws Exception {
        defineWebPage("Default", "<script language=JavaScript>\n" + "function extend()\n" + "{\n"
                + "document.myForm.jobRoleID.options.length=2;\n" + "document.myForm.jobRoleID.options[1].text='here';"
                + "return true;\n" + "}\n" + "function viewSelect( choice ) {\n"
                + "    alert ('select has ' + choice.options.length + ' options' );\n"
                + "    alert ('last option is ' + choice.options[choice.options.length-1].text );\n" + "}\n"
                + "</script>" + "<form name=myForm method=POST>" + "  <select name=\"jobRoleID\">"
                + "    <option value=\"0\" selected=\"selected\">Select Job Role</option>" + "  </select>"
                + "  <input type=\"text\" name=\"last_name\" value=\"Bloggs\">" + "</form>"
                + "<a href='#' onClick='viewSelect(document.myForm.jobRoleID); return false;'>a</href>\n"
                + "<a href='#' onClick='extend(); return false;'>a</href>\n");
        WebConversation wc = new WebConversation();
        WebResponse wr = wc.getResponse(getHostPath() + "/Default.html");
        wr.getLinks()[0].click();
        assertEquals("select has 1 options", wc.popNextAlert(), "1st message");
        assertEquals("last option is Select Job Role", wc.popNextAlert(), "2nd message");
        wr.getLinks()[1].click();
        wr.getLinks()[0].click();
        assertEquals("select has 2 options", wc.popNextAlert(), "3rd message");
        assertEquals("last option is here", wc.popNextAlert(), "4th message");
    }

    /**
     * Test that the JavaScript 'value' and 'defaultValue' properties of a text input are distinct. 'defaultValue'
     * should represent the 'value' attribute of the input element. 'value' should initially match 'defaultValue', but
     * setting it should not affect the 'defaultValue'.
     *
     * @throws Exception
     */
    @Test
    void elementDefaultValue() throws Exception {
        defineWebPage("Default", "<script language=JavaScript>\n" + "function CheckForm()\n" + "{\n" + "var i;\n"
                + "var form_length=document.myForm.elements.length;\n" + "for ( i=0 ; i< form_length; i++ )\n" + "{\n"
                + "  confirm (document.myForm.elements[i].value);\n " + "}\n"
                + "document.myForm.elements[2].value = \"Charles\"\n" + "for ( i=0 ; i< form_length; i++ )\n" + "{\n"
                + "  confirm (document.myForm.elements[i].defaultValue);\n " + "}\n"
                + "confirm(document.myForm.elements[2].value);\n" + "return true;\n" + "}\n" + "</script>"
                + "<form name=myForm method=POST>" + "  <input type=\"text\" name=\"first_name\" value=\"Alpha\">"
                + "  <input type=\"text\" name=\"last_name\" value=\"Bravo\">"
                + "  <input type=\"text\" name=\"last_name\" value=\"Charlie\">" + "</form>"
                + "<script language=JavaScript>\n" + "  CheckForm();\n" + "</script>\n");

        String[] expectedValues = { "Alpha", "Bravo", "Charlie", "Alpha", "Bravo", "Charlie", "Charles" };

        final PromptCollector collector = new PromptCollector();
        WebConversation wc = new WebConversation();
        wc.setDialogResponder(collector);
        wc.getResponse(getHostPath() + "/Default.html");
        assertMatchingSet("Values seen by JavaScript", expectedValues, collector.confirmPromptsSeen.toArray());
    }

}

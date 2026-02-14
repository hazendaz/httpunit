/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.meterware.pseudoserver.PseudoServlet;
import com.meterware.pseudoserver.WebResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A test of the parameter validation functionality.
 */
class FormSubmitTest extends HttpUnitTest {

    /**
     * Sets the up.
     *
     * @throws Exception
     *             the exception
     */
    @BeforeEach
    void setUp() throws Exception {
        _wc = new WebConversation();
    }

    /**
     * Embedded equals.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void embeddedEquals() throws Exception {
        defineWebPage("Default", "<form method=GET action = \"/ask\">" + "<Input type=text name=\"age=x\" value=12>"
                + "<Input type=submit>" + "</form>");
        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebForm form = page.getForms()[0];
        WebRequest request = form.getRequest();
        assertEquals(getHostPath() + "/ask?age%3Dx=12", request.getURL().toExternalForm());
    }

    /**
     * Empty choice submit.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void emptyChoiceSubmit() throws Exception {
        defineWebPage("Default", "<form method=GET action = \"/ask\">" + "<Input type=text name=age value=12>"
                + "<select name=empty></select>" + "<Input type=submit>" + "</form>");
        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebForm form = page.getForms()[0];
        WebRequest request = form.getRequest();
        assertEquals(getHostPath() + "/ask?age=12", request.getURL().toExternalForm(), "Empty choice query");
    }

    /**
     * Form properties.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void formProperties() throws Exception {
        defineWebPage("Default", "<form method=GET action = \"/ask\">" + "<Input type=text name=age value=12>"
                + "<select name=empty></select>" + "<Input type=submit>" + "</form>");
        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebForm form = page.getForms()[0];
        assertEquals("GET", form.getMethod(), "Form method");
        assertEquals("/ask", form.getAction(), "Form action");

        form.getScriptableObject().setAction("/tell");
        assertEquals("/tell", form.getAction(), "Form action");
    }

    /**
     * Submit string.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void submitString() throws Exception {
        defineWebPage("Default", "<form method=GET action = \"/ask\">" + "<Input type=text name=age>"
                + "<Input type=submit value=Go>" + "</form>");
        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebRequest request = page.getForms()[0].getRequest();
        request.setParameter("age", "23");
        assertEquals(getHostPath() + "/ask?age=23", request.getURL().toExternalForm());
    }

    /**
     * Submit string with query only relative URL.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void submitStringWithQueryOnlyRelativeURL() throws Exception {
        defineWebPage("/blah/blah/blah",
                "<form method=GET action = '?recall=true'>" + "<Input type=submit value=Go>" + "</form>");
        WebResponse page = _wc.getResponse(getHostPath() + "/blah/blah/blah.html");
        WebRequest request = page.getForms()[0].getRequest();
        assertEquals(getHostPath() + "/blah/blah/blah.html?recall=true", request.getURL().toExternalForm());
    }

    /**
     * Submit string after set action.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void submitStringAfterSetAction() throws Exception {
        defineWebPage("Default", "<form method=GET action = \"/ask\">" + "<Input type=text name=age>"
                + "<Input type=submit value=Go>" + "</form>");
        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        page.getForms()[0].getScriptableObject().setAction("tell");
        WebRequest request = page.getForms()[0].getRequest();
        request.setParameter("age", "23");
        assertEquals(getHostPath() + "/tell?age=23", request.getURL().toExternalForm());
    }

    /**
     * No name submit string.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void noNameSubmitString() throws Exception {
        defineWebPage("Default", "<form method=GET action = \"/ask\">" + "<Input type=text value=dontSend>"
                + "<Input type=text name=age>" + "<Input type=submit></form>");
        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebRequest request = page.getForms()[0].getRequest();
        request.setParameter("age", "23");
        assertEquals(getHostPath() + "/ask?age=23", request.getURL().toExternalForm());
    }

    /**
     * check that submit buttons will be detected.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void submitButtonDetection() throws Exception {
        defineWebPage("Default",
                "<form method=GET action = \"/ask\">" + "<Input type=text name=age value=12>"
                        + "<Input type=submit name=update value=update>"
                        + "<Input type=submit name=recalculate value=value>" + "</form>");
        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebForm form = page.getForms()[0];
        SubmitButton[] buttons = form.getSubmitButtons();
        assertEquals(2, buttons.length, "num detected submit buttons");
        assertMatchingSet("selected request parameters", new String[] { "age", "update" },
                form.getRequest("update").getRequestParameterNames());
    }

    /**
     * test for bug report [ 1629836 ] Anchor only form actions are not properly handled by Claude Brisson.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void anchor() throws Exception {
        defineWebPage("page", "<form method=GET action = \"#myanchor\">" + "<Input type=submit id=doit>" + "</form>");
        WebResponse page = _wc.getResponse(getHostPath() + "/page.html");
        WebForm form = page.getForms()[0];
        Button button = form.getButtonWithID("doit");
        button.click();
        String url = _wc.getCurrentPage().getURL().toExternalForm();
        assertTrue(url.endsWith("page.html"));
    }

    /**
     * check that a fake submit button will be added and marked as such test for [ 1159887 ] patch for RFE 1159884 by
     * Rafal Krzewski.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void fakeSubmitButtonAddition() throws Exception {
        defineWebPage("Default",
                "<form method=GET action = \"/ask\">" + "<Input type=text name=age value=12>" + "</form>");
        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebForm form = page.getForms()[0];
        SubmitButton[] buttons = form.getSubmitButtons();
        assertEquals(1, buttons.length, "num detected submit buttons");
        assertTrue(buttons[0].isFake(), "the only submit button returned should be a fake");
    }

    /**
     * Non submit button detection.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void nonSubmitButtonDetection() throws Exception {
        defineWebPage("Default",
                "<form method=GET action = \"/ask\">" + "<Input type=text name=age value=12>"
                        + "<Input type=submit name=update>" + "<Input type=reset>"
                        + "<Input type=button value=recalculate>" + "</form>");
        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebForm form = page.getForms()[0];
        Button[] buttons = form.getButtons();
        assertEquals(3, buttons.length, "num detected buttons");
    }

    /**
     * test detecting the reset button.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void resetButtonDetection() throws Exception {
        defineWebPage("Default",
                "<form method=GET action = \"/ask\">" + "<Input type=text name=age value=12>"
                        + "<Input type=submit name=update>" + "<Input type=reset id=clear>"
                        + "<Input type=button value=recalculate>" + "</form>");
        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebForm form = page.getForms()[0];
        form.setParameter("age", "15");
        Button reset = form.getButtonWithID("clear");
        reset.click();
        assertEquals("12", form.getParameterValue("age"), "Value after reset");
        HTMLElement element = page.getElementWithID("clear");
        assertSame(reset, element, "Reset button");
    }

    /**
     * test that a disabled submitButton can not be submitted.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void disabledSubmitButtonDetection() throws Exception {
        defineWebPage("Default", "<form method=GET action = \"/ask\">" + "<Input type=text name=age value=12>"
                + "<Input type=submit name=update>" + "<Input type=submit name=recalculate disabled>" + "</form>");
        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebForm form = page.getForms()[0];
        SubmitButton[] buttons = form.getSubmitButtons();
        assertEquals(2, buttons.length, "num detected submit buttons");
        SubmitButton sb = form.getSubmitButton("recalculate");
        assertNotNull(sb, "Failed to find disabled button");
        assertTrue(sb.isDisabled(), "Disabled button not marked as disabled");
        try {
            form.getRequest(sb);
            fail("Allowed to create a request for a disabled button");
        } catch (IllegalStateException e) {
        }
        try {
            sb.click();
            fail("Allowed to click a disabled button");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * test for bug report [2264431] double submit problem version 1.7 would have problem with double submits
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void doubleSubmitProblem() throws Exception {
        boolean[] states = { false, true };
        String[] expected = { "", "1" };
        for (int i = 0; i < states.length; i++) {
            // countMySelf Tipp from http://www.tipstrs.com/tip/1084/Static-variables-in-Javascript
            defineWebPage("Default",
                    "<form method=GET action = \"Default.html\" onsubmit=\"javascript:countMyself();\">"
                            + "<script type='JavaScript'>\n" + "function countMyself() {\n"
                            + "  // Check to see if the counter has been initialized\n"
                            + "  if ( typeof countMyself.counter == 'undefined' ) {\n"
                            + "      // It has not... perform the initilization\n" + "      countMyself.counter = 0;\n"
                            + "  }\n" + "\n" + "  // Do something stupid to indicate the value\n"
                            + "  alert(++countMyself.counter);\n" + "}\n" + "</script>"
                            + "<Input type=submit name='update' onclick='return " + states[i] + ";'></form>"
                            + "</form>");
            WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
            WebForm form = page.getForms()[0];
            form.submit();
            String alert = _wc.popNextAlert();
            assertEquals(alert, expected[i], "There should be " + expected[i] + " submits for onclick state '"
                    + states[i] + "' but there are " + alert);
        }
    }

    /**
     * test self disabling submit Buttons test for bug report [ 1289151 ] Order of events in button.click() is wrong
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void selfDisablingSubmitButton() throws Exception {
        defineWebPage("Default", "<form method=GET action = \"Default.html\">"
                + "<Input type=submit name='update' onclick='javascript:this.disabled=true;'></form>" + "</form>");
        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebForm form = page.getForms()[0];
        SubmitButton[] buttons = form.getSubmitButtons();
        assertEquals(1, buttons.length, "num detected submit buttons");
        SubmitButton sb = form.getSubmitButton("update");
        assertNotNull(sb, "Failed to find update button");
        sb.click();
        assertTrue(sb.isDisabled(), "Disabled button not marked as disabled");
        try {
            form.getRequest(sb);
            fail("Allowed to create a request for a disabled button");
        } catch (IllegalStateException e) {
        }
        try {
            sb.click();
            fail("Allowed to click a disabled button");
        } catch (IllegalStateException e) {
        }
    }

    /**
     * test that a disabled Button can be detected by accessing the disabled() function for bug report [ 1124024 ]
     * Formcontrol and isDisabled should be public by Wolfgang Fahl.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void buttonDisabledFlagAccess() throws Exception {
        defineWebPage("Default", "<form method=GET action = \"/ask\">" + "<Input type=button id=button1 name=button1 >"
                + "<Input type=button name=button2 disabled>" + "</form>");
        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebForm form = page.getForms()[0];
        Button[] buttons = form.getButtons();
        assertFalse(buttons[0].isDisabled(), "Enabled button marked as disabled");
        assertTrue(buttons[1].isDisabled(), "Disabled button not marked as disabled");
        FormControl control = form.getControlWithID("button1");
        assertTrue(control instanceof Button, control.getClass().getName());
    }

    /**
     * Button ID detection.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void buttonIDDetection() throws Exception {
        defineWebPage("Default", "<form method=GET action = \"/ask\">" + "<Input type=text name=age value=12>"
                + "<Input type=submit id=main name=update>" + "<Input type=submit name=recalculate>" + "</form>");
        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebForm form = page.getForms()[0];
        SubmitButton button = form.getSubmitButton("update");
        assertEquals("", form.getSubmitButton("recalculate").getID(), "Null ID");
        assertEquals("main", button.getID(), "Button ID");

        SubmitButton button2 = form.getSubmitButtonWithID("main");
        assertEquals(button, button2, "Submit button");
    }

    /**
     * Button tag detection.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void buttonTagDetection() throws Exception {
        defineWebPage("Default", "<form method=GET action = \"/ask\">" + "<Input type=text name=age value=12>"
                + "<Button type=submit name=update></button>" + "<button name=recalculate></button>" + "</form>");
        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebForm form = page.getForms()[0];
        SubmitButton[] buttons = form.getSubmitButtons();
        assertEquals(2, buttons.length, "num detected submit buttons");
    }

    /**
     * Image button detection.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void imageButtonDetection() throws Exception {
        defineWebPage("Default",
                "<form method=GET action = \"/ask\">" + "<Input type=text name=age value=12>"
                        + "<Input type=image name=update src=\"\">" + "<Input type=image name=recalculate src=\"\">"
                        + "</form>");
        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebForm form = page.getForms()[0];
        SubmitButton[] buttons = form.getSubmitButtons();
        assertEquals(2, buttons.length, "num detected submit buttons");
    }

    /**
     * Image button default submit.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void imageButtonDefaultSubmit() throws Exception {
        defineWebPage("Default", "<form method=GET action = \"/ask\">" + "<Input type=text name=age value=12>"
                + "<Input type=image name=update value=name src=\"\">" + "</form>");
        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebForm form = page.getForms()[0];
        WebRequest request = form.getRequest();
        assertEquals(getHostPath() + "/ask?age=12&update=name&update.x=0&update.y=0", request.getURL().toExternalForm(),
                "Query");
    }

    /**
     * Image button no value.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void imageButtonNoValue() throws Exception {
        defineWebPage("Default",
                "<form name='login' method='get' action='ask'>" + "<input type='text' name='email' value='bread'>"
                        + "<input type='image' name='login' src='../../se/images/buttons/login.gif'"
                        + "       Alt='OK' border='0'>" + "</form>");
        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebForm form = page.getForms()[0];
        WebRequest request = form.getRequest();
        assertEquals(getHostPath() + "/ask?email=bread&login.x=0&login.y=0", request.getURL().toExternalForm(),
                "Query");
    }

    /**
     * test behaviour of UnnameImageButtons see also WebFormTest.testSubmitFromUnnamedImageButton
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void unnamedImageButtonDefaultSubmit() throws Exception {
        defineWebPage("Default", "<form method=GET action = \"/ask\">" + "<Input type=text name=age value=12>"
                + "<Input type=image value=name src=\"\">" + "</form>");
        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebForm form = page.getForms()[0];
        WebRequest request = form.getRequest();
        String urlString = request.getURL().toExternalForm();
        assertEquals(getHostPath() + "/ask?age=12", urlString);
    }

    /**
     * test behavoir of positional image buttons see also WebFormTest.testSubmitFromPositionalButton
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void imageButtonPositionalSubmit() throws Exception {
        defineWebPage("Default", "<form method=GET action = \"/ask\">" + "<Input type=text name=age value=12>"
                + "<Input type=image name=update value=name src=\"\">" + "</form>");
        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebForm form = page.getForms()[0];
        WebRequest request = form.getRequest(form.getSubmitButton("update"), 10, 15);
        assertEquals(getHostPath() + "/ask?age=12&update=name&update.x=10&update.y=15",
                request.getURL().toExternalForm());
        request.setImageButtonClickPosition(5, 20);
        assertEquals(getHostPath() + "/ask?age=12&update=name&update.x=5&update.y=20",
                request.getURL().toExternalForm());
    }

    /**
     * Image button no value positional submit.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void imageButtonNoValuePositionalSubmit() throws Exception {
        defineWebPage("Default", "<form method='GET' action='test.jsp'>"
                + "<input type='image' src='image.gif' name='aButton'>" + "</form>");
        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebForm form = page.getForms()[0];
        WebRequest request = form.getRequest(form.getSubmitButton("aButton"), 20, 5);
        assertEquals(getHostPath() + "/test.jsp?aButton.x=20&aButton.y=5", request.getURL().toExternalForm());
    }

    /**
     * Image button no value unchecked positional submit.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void imageButtonNoValueUncheckedPositionalSubmit() throws Exception {
        assertDoesNotThrow(() -> {
            defineWebPage("Default", "<form method='GET' action='test.jsp'>"
                    + "<input type='image' src='image.gif' name='aButton'>" + "</form>");
            WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
            WebForm form = page.getForms()[0];
            WebRequest request = form.newUnvalidatedRequest(form.getSubmitButton("aButton"), 20, 5);
            assertEqualQueries(getHostPath() + "/test.jsp?aButton.x=20&aButton.y=5", request.getURL().toExternalForm());
        });
    }

    /**
     * Submit button attributes.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void submitButtonAttributes() throws Exception {
        defineWebPage("Default", "<form method=GET action = \"/ask\">" + "<Input type=text name=age value=12>"
                + "<Input type=submit name=update value=age>" + "</form>");
        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebForm form = page.getForms()[0];
        SubmitButton[] buttons = form.getSubmitButtons();
        assertEquals(1, buttons.length, "num detected submit buttons");
        assertEquals("update", buttons[0].getName(), "submit button name");
        assertEquals("age", buttons[0].getValue(), "submit button value");
    }

    /**
     * Submit button selection by name.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void submitButtonSelectionByName() throws Exception {
        defineWebPage("Default",
                "<form method=GET action = \"/ask\">" + "<Input type=text name=age value=12>"
                        + "<Input type=submit name=update value=age>" + "<Input type=submit name=recompute value=age>"
                        + "</form>");
        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebForm form = page.getForms()[0];
        SubmitButton button = form.getSubmitButton("zork");
        assertNull(button, "Found a non-existent button");
        button = form.getSubmitButton("update");
        assertNotNull(button, "Didn't find the desired button");
        assertEquals("update", button.getName(), "submit button name");
        assertEquals("age", button.getValue(), "submit button value");
    }

    /**
     * Submit button selection by name and value.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void submitButtonSelectionByNameAndValue() throws Exception {
        defineWebPage("Default",
                "<form method=GET action = \"/ask\">" + "<Input type=text name=age value=12>"
                        + "<Input type=submit name=update value=age>" + "<Input type=submit name=update value=name>"
                        + "</form>");
        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebForm form = page.getForms()[0];
        SubmitButton button = form.getSubmitButton("update");
        assertNotNull(button, "Didn't find the desired button");
        assertEquals("update", button.getName(), "submit button name");
        assertEquals("age", button.getValue(), "submit button value");
        button = form.getSubmitButton("update", "name");
        assertNotNull(button, "Didn't find the desired button");
        assertEquals("update", button.getName(), "submit button name");
        assertEquals("name", button.getValue(), "submit button value");
    }

    /**
     * Named button submit string.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void namedButtonSubmitString() throws Exception {
        defineWebPage("Default",
                "<form method=GET action = \"/ask\">" + "<Input type=text name=age value=12>"
                        + "<Input type=submit name=update value=age>" + "<Button type=submit name=update value=name>"
                        + "</form>");
        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebForm form = page.getForms()[0];
        WebRequest request = form.getRequest(form.getSubmitButton("update", "name"));
        assertEquals(getHostPath() + "/ask?age=12&update=name", request.getURL().toExternalForm());

        request = form.getRequest("update", "name");
        assertEquals(getHostPath() + "/ask?age=12&update=name", request.getURL().toExternalForm());

        request = form.getRequest("update");
        assertEquals(getHostPath() + "/ask?age=12&update=age", request.getURL().toExternalForm());

        try {
            request.setImageButtonClickPosition(1, 2);
            fail("Should not allow set position with non-image button");
        } catch (IllegalRequestParameterException e) {
        }
    }

    /**
     * Unnamed button submit.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void unnamedButtonSubmit() throws Exception {
        defineWebPage("Default",
                "<form method=GET action = \"/ask\">" + "<Input type=text name=age value=12>"
                        + "<Input type=submit name=update value=age>" + "<Input type=submit name=update value=name>"
                        + "</form>");
        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebForm form = page.getForms()[0];
        try {
            form.getRequest();
            fail("Should not allow submit with unnamed button");
        } catch (IllegalRequestParameterException e) {
        }
    }

    /**
     * Foreign submit button detection.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void foreignSubmitButtonDetection() throws Exception {
        defineWebPage("Default",
                "<form method=GET action = \"/ask\">" + "<Input type=text name=age value=12>"
                        + "<Input type=submit name=update value=age>" + "<Input type=submit name=update value=name>"
                        + "</form>");
        defineWebPage("Dupl",
                "<form method=GET action = \"/ask\">" + "<Input type=text name=age value=12>"
                        + "<Input type=submit name=update value=age>" + "<Input type=submit name=update value=name>"
                        + "</form>");
        defineWebPage("Wrong", "<form method=GET action = \"/ask\">" + "<Input type=text name=age value=12>"
                + "<Input type=submit name=save value=age>" + "</form>");
        WebResponse other = _wc.getResponse(getHostPath() + "/Dupl.html");
        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebResponse wrong = _wc.getResponse(getHostPath() + "/Wrong.html");

        WebForm form = page.getForms()[0];
        WebForm otherForm = other.getForms()[0];
        WebForm wrongForm = wrong.getForms()[0];

        form.getRequest(otherForm.getSubmitButtons()[0]);

        try {
            form.getRequest(wrongForm.getSubmitButtons()[0]);
            fail("Failed to reject illegal button");
        } catch (IllegalRequestParameterException e) {
        }

        form.newUnvalidatedRequest(wrongForm.getSubmitButtons()[0]);

        HttpUnitOptions.setParameterValuesValidated(false);
        form.getRequest(wrongForm.getSubmitButtons()[0]);
    }

    /**
     * No action supplied.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void noActionSupplied() throws Exception {
        defineWebPage("abc/form", "<form name=\"test\">" + "  <input type=\"text\" name=\"aTextField\">"
                + "  <input type=\"submit\" name=\"apply\" value=\"Apply\">" + "</form>");

        WebResponse wr = _wc.getResponse(getHostPath() + "/abc/form.html");
        WebForm form = wr.getForms()[0];
        WebRequest req = form.getRequest("apply");
        req.setParameter("aTextField", "test");
        assertEquals(getHostPath() + "/abc/form.html?aTextField=test&apply=Apply", req.getURL().toExternalForm());
    }

    /**
     * No action supplied when base has params.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void noActionSuppliedWhenBaseHasParams() throws Exception {
        defineResource("abc/form?param1=value&param2=value",
                "<form name=\"test\">" + "  <input type=\"text\" name=\"aTextField\">"
                        + "  <input type=\"submit\" name=\"apply\" value=\"Apply\">" + "</form>");

        WebResponse wr = _wc.getResponse(getHostPath() + "/abc/form?param1=value&param2=value");
        WebForm form = wr.getForms()[0];
        WebRequest req = form.getRequest("apply");
        req.setParameter("aTextField", "test");
        assertEquals(getHostPath() + "/abc/form?param1=value&param2=value&aTextField=test&apply=Apply",
                req.getURL().toExternalForm());
    }

    /**
     * No action supplied when base has params set by the form.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void noActionSuppliedWhenBaseHasParamsSetByTheForm() throws Exception {
        defineResource("abc/form?param1=value&param2=value",
                "<form name=\"test\">" + "  <input type=\"text\" name='param2'>"
                        + "  <input type=\"submit\" name=\"apply\" value=\"Apply\">" + "</form>");

        WebResponse wr = _wc.getResponse(getHostPath() + "/abc/form?param1=value&param2=value");
        WebForm form = wr.getForms()[0];
        WebRequest req = form.getRequest("apply");
        req.setParameter("param2", "test");
        assertEquals(getHostPath() + "/abc/form?param1=value&param2=test&apply=Apply", req.getURL().toExternalForm());
    }

    /**
     * Post action parameters after set action.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void postActionParametersAfterSetAction() throws Exception {
        defineWebPage("abc/form",
                "<form name=\"test\" method='POST' action='stop?ready=yes'>"
                        + "  <input type=\"text\" name=\"aTextField\">"
                        + "  <input type=\"submit\" name=\"apply\" value=\"Apply\">" + "</form>");

        WebResponse wr = _wc.getResponse(getHostPath() + "/abc/form.html");
        WebForm form = wr.getForms()[0];
        form.getScriptableObject().setAction("go?size=3&time=now");
        WebRequest req = form.getRequest("apply");
        req.setParameter("aTextField", "test");
        assertEquals(getHostPath() + "/abc/go?size=3&time=now", req.getURL().toExternalForm());
    }

    /**
     * Post parameter encoding.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void postParameterEncoding() throws Exception {
        defineWebPage("abc/form",
                "<form name=\"test\" method='POST' action='/doit'>" + "  <input type='text' name='text_field-name*'>"
                        + "  <input type='submit' name='apply' value='Apply'>" + "</form>");
        setResourceCharSet("abc/form.html", "UTF-8", true);
        defineResource("doit", new PseudoServlet() {
            @Override
            public WebResource getPostResponse() throws IOException {
                return new WebResource(new String(getBody(), StandardCharsets.UTF_8));
            }
        });

        WebResponse wr = _wc.getResponse(getHostPath() + "/abc/form.html");
        WebForm form = wr.getForms()[0];
        form.setParameter("text_field-name*", "a value");

        WebResponse response = form.submit();
        assertEquals("text_field-name*=a+value&apply=Apply", response.getText(), "posted parameters");
    }

    /**
     * Mailto action rejected.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void mailtoActionRejected() throws Exception {
        defineWebPage("abc/form",
                "<form name='test' action='mailto:russgold@httpunit.org'>" + "  <input type='text' name='text_field'>"
                        + "  <input type='submit' name='apply' value='Apply'>" + "</form>");

        WebResponse wr = _wc.getResponse(getHostPath() + "/abc/form.html");
        WebForm form = wr.getForms()[0];
        form.setParameter("text_field", "a value");

        try {
            form.submit();
            fail("Should have thrown an UnsupportedActionException");
        } catch (UnsupportedActionException success) {
            assertTrue(success.getMessage().indexOf("mailto") >= 0, "Did not include mention of bad URL type");
        }
    }

    /**
     * test that the enabled/disabled state of a button is accessible.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void enabledDisabled() throws Exception {
        assertDoesNotThrow(() -> {
            // a web page with two checkboxes
            defineWebPage("Default", "<form method=GET action = \"/ask\">"
                    + "<input type=\"checkbox\" id=\"checkDisabled\" name=checkDisabled>Disabled"
                    + "<input type=\"checkbox\" id=\"checkEnabled\"  name=checkEnabled checked>Enabled" + "</form>");
            WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
            String[] ids = { "checkDisabled", "checkEnabled" };
            for (String id : ids) {
                Object o = page.getElementWithID(id);
                if (!(o instanceof FormControl)) {
                    throw new Exception("element with id " + id + "has invalid type " + o.getClass().getName()
                            + " expected was FormControl");
                }
                FormControl box = (FormControl) o;
                box.isDisabled();
            } // for
        }); // for
    }

    // ---------------------------------------------- private members ------------------------------------------------

    /** The wc. */
    private WebConversation _wc;
}

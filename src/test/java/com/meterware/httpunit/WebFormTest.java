/*
 * MIT License
 *
 * Copyright 2011-2026 Russell Gold
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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.meterware.pseudoserver.PseudoServlet;
import com.meterware.pseudoserver.WebResource;

import java.net.HttpURLConnection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * A test of the web form functionality.
 **/
class WebFormTest extends HttpUnitTest {

    /**
     * Sets the up.
     *
     * @throws Exception
     *             the exception
     */
    @BeforeEach
    void setUp() throws Exception {
        _wc = new WebConversation();

        defineWebPage("OneForm",
                "<h2>Login required</h2>" + "<form method=POST action = \"/servlet/Login\"><B>"
                        + "Enter the name 'master': <Input type=TEXT Name=name></B>"
                        + "<input type=\"checkbox\" name=first>Disabled"
                        + "<input type=\"checkbox\" name=second checked>Enabled"
                        + "<br><Input type=submit value = \"Log in\">" + "</form>");
    }

    /**
     * placeholder for test for BR 2407470 by redsonic with comment and patch by Adam Heath.
     *
     * @throws Exception
     *             the exception
     */
    // TODO JWL 7/6/2021 Breaks with nekohtml > 1.9.6.2
    @Disabled
    @Test
    void getFormWithID() throws Exception {
        defineWebPage("OnCommand",
                "<html>\n" + "  <head>\n" + "     <script type='JavaScript'>\n" + "         function function1() {\n"
                        + "  		    alert( document.forms[0].name );\n" + "         }\n" + "     </script>\n"
                        + "  </head>\n" + "  <body>\n" + "    <form id='form1' name='form1name'/>\n"
                        + "    <form id='form2' name='form2name'/>\n" + "    <form id='form3' name='form3name'/>\n"
                        + "  <body>\n" + "</html>\n");
        boolean oldstate = HttpUnitOptions.setExceptionsThrownOnScriptError(false);
        try {
            WebConversation wc = new WebConversation();
            WebResponse wr = wc.getResponse(getHostPath() + "/OnCommand.html");
            WebForm form = wr.getFormWithID("form3");
            assertNotNull(form, "form3 is null");
        } catch (Exception ex) {
            throw ex;
        } finally {
            HttpUnitOptions.setExceptionsThrownOnScriptError(oldstate);
        }
    }

    /**
     * Submit from form.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void submitFromForm() throws Exception {
        defineWebPage("Form", "<form method=GET id=main action = 'tryMe'>" + "<Input type=text Name=name>"
                + "<input type=\"checkbox\" name=second checked>Enabled" + "</form>");
        defineResource("/tryMe?name=master&second=on", "You made it!");
        WebResponse wr = _wc.getResponse(getHostPath() + "/Form.html");
        WebForm form = wr.getFormWithID("main");
        form.setParameter("name", "master");
        form.submit();
        assertEquals("You made it!", _wc.getCurrentPage().getText(), "Expected response");
    }

    /**
     * Ambiguous submit from form.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void ambiguousSubmitFromForm() throws Exception {
        defineWebPage("Form",
                "<form method=GET id=main action = 'tryMe'>" + "<Input type=text Name=name>"
                        + "<input type=\"checkbox\" name=second checked>Enabled"
                        + "<input type='submit' name='left'><input type='submit' name='right'>" + "</form>");
        defineResource("/tryMe?name=master&second=on", "You made it!");
        WebResponse wr = _wc.getResponse(getHostPath() + "/Form.html");
        WebForm form = wr.getFormWithID("main");
        form.setParameter("name", "master");
        try {
            form.submit();
            fail("Should have rejected request as ambiguous");
        } catch (IllegalRequestParameterException e) {
        }
        WebResponse noButton = form.submitNoButton();
        assertEquals("You made it!", noButton.getText(), "Expected response");
    }

    /**
     * Submit from button.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void submitFromButton() throws Exception {
        defineWebPage("Form", "<form method=GET id=main action = 'tryMe'>" + "<Input type=text Name=name>"
                + "<input type=\"checkbox\" name=second checked>Enabled" + "<input type=submit name=save value=none>"
                + "<input type=submit name=save value=all>" + "</form>");
        defineResource("/tryMe?name=master&second=on&save=all", "You made it!");
        WebResponse wr = _wc.getResponse(getHostPath() + "/Form.html");
        WebForm form = wr.getFormWithID("main");
        form.setParameter("name", "master");
        SubmitButton button = form.getSubmitButton("save", "all");
        button.click();
        assertEquals("You made it!", _wc.getCurrentPage().getText(), "Expected response");
    }

    /**
     * test clicking on a Positional Button with a given name "update".
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void submitFromPositionalButton() throws Exception {
        defineResource("ask?age=12&update=name&update.x=5&update.y=15", "You made it!", "text/plain");
        defineWebPage("Default", "<form id='form' method=GET action = \"/ask\">" + "<Input type=text name=age value=12>"
                + "<Input type=image name=update value=name src=\"\">" + "</form>");
        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        SubmitButton button = page.getFormWithID("form").getSubmitButton("update");
        assertEquals("update.x", button.positionParameterName("x"), "x param name");
        assertEquals("update.y", button.positionParameterName("y"), "y param name");
        button.click(5, 15);
        assertEquals("You made it!", _wc.getCurrentPage().getText(), "Result of click");
    }

    /**
     * test clicking on a unnamed Image Button see also FormSubmitTest.testUnnamedImageButtonDefaultSubmit
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void submitFromUnnamedImageButton() throws Exception {
        boolean oldAllowUnnamedImageButton = SubmitButton.isAllowUnnamedImageButton();
        SubmitButton.setAllowUnnamedImageButton(true);
        defineResource("ask?age=12", "Unnamed Image Button ignored!", "text/plain");
        defineResource("ask?age=12&x=5&y=15", "You made it!", "text/plain");
        defineWebPage("Default", "<form id='form' method=GET action = \"/ask\">" + "<Input type=text name=age value=12>"
                + "<Input type=image id=imageid value=name src=\"\">" + "</form>");
        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        SubmitButton button = page.getFormWithID("form").getSubmitButtonWithID("imageid");
        assertEquals("", button.getName(), "empty button name");
        assertEquals("x", button.positionParameterName("x"), "x param name");
        assertEquals("y", button.positionParameterName("y"), "y param name");
        button.click(5, 15);
        WebResponse response = _wc.getCurrentPage();
        response.getURL();
        // reset for other test
        SubmitButton.setAllowUnnamedImageButton(oldAllowUnnamedImageButton);
        // System.err.println(url.getPath());
        assertEquals("You made it!", response.getText(), "Result of click");
    }

    /**
     * Find no form.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void findNoForm() throws Exception {
        defineWebPage("NoForms", "This has no forms but it does" + "have <a href=\"/other.html\">an active link</A>"
                + " and <a name=here>an anchor</a>");

        WebForm[] forms = _wc.getResponse(getHostPath() + "/NoForms.html").getForms();
        assertNotNull(forms);
        assertEquals(0, forms.length);
    }

    /**
     * Find one form.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void findOneForm() throws Exception {
        WebForm[] forms = _wc.getResponse(getHostPath() + "/OneForm.html").getForms();
        assertNotNull(forms);
        assertEquals(1, forms.length);
    }

    /**
     * Find form by name.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void findFormByName() throws Exception {
        defineWebPage("Default",
                "<form name=oneForm method=POST action = \"/servlet/Login\">"
                        + "<Input name=\"secret\" type=\"hidden\" value=\"surprise\">"
                        + "<br><Input name=typeless value=nothing>"
                        + "<B>Enter the name 'master': <Input type=TEXT Name=name></B>"
                        + "<br><Input type=submit value = \"Log in\">" + "</form>");

        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        assertNull(page.getFormWithName("nobody"), "Found nonexistent form");
        assertNotNull(page.getFormWithName("oneform"), "Did not find named form");
    }

    /**
     * Find form by ID.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void findFormByID() throws Exception {
        defineWebPage("Default",
                "<form id=oneForm method=POST action = \"/servlet/Login\">"
                        + "<Input name=\"secret\" type=\"hidden\" value=\"surprise\">"
                        + "<br><Input name=typeless value=nothing>"
                        + "<B>Enter the name 'master': <Input type=TEXT Name=name></B>"
                        + "<br><Input type=submit value = \"Log in\">" + "</form>");

        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        assertNull(page.getFormWithID("nobody"), "Found nonexistent form");
        assertNotNull(page.getFormWithID("oneForm"), "Did not find specified form");
    }

    /**
     * Form parameters.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void formParameters() throws Exception {
        defineWebPage("AForm",
                "<h2>Login required</h2>" + "<form method=POST action = \"/servlet/Login\"><B>"
                        + "Enter the name 'master': <textarea Name=name>Something</textarea></B>"
                        + "<input type=\"checkbox\" name=first>Disabled"
                        + "<input type=\"checkbox\" name=second checked>Enabled"
                        + "<input type=textbox name=third value=something>"
                        + "<br><Input type=submit value = \"Log in\">" + "</form>");

        WebForm form = _wc.getResponse(getHostPath() + "/AForm.html").getForms()[0];
        String[] parameters = form.getParameterNames();
        assertNotNull(parameters);
        assertMatchingSet("form parameter names", new String[] { "first", "name", "second", "third" }, parameters);

        assertNull(form.getParameterValue("first"), "First checkbox has a non-null value");
        assertEquals("on", form.getParameterValue("second"), "Second checkbox");
        assertNull(form.getParameterValue("magic"), "Found extraneous value for unknown parameter 'magic'");
        assertTrue(form.hasParameterNamed("first"), "Did not find parameter 'first'");
        assertTrue(form.hasParameterStartingWithPrefix("sec"), "Did not find parameter with prefix 'sec'");
        assertTrue(form.hasParameterStartingWithPrefix("nam"), "Did not find parameter with prefix 'nam'");

        assertTrue(form.hasParameterNamed("third"), "Did not find parameter named 'third'");
        assertEquals("something", form.getParameterValue("third"), "Value of parameter with unknown type");

        assertEquals("Something", form.getParameterValue("name"), "Original text area value");
        form.setParameter("name", "Something Else");
        assertEquals("Something Else", form.getParameterValue("name"), "Changed text area value");

        form.reset();
        assertEquals("Something", form.getParameterValue("name"), "Reset text area value");
    }

    /**
     * Form request.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void formRequest() throws Exception {
        WebForm form = _wc.getResponse(getHostPath() + "/OneForm.html").getForms()[0];
        WebRequest request = form.getRequest();
        request.setParameter("name", "master");
        assertFalse(request instanceof GetMethodWebRequest, "Should be a post request");
        assertEquals(getHostPath() + "/servlet/Login", request.getURL().toExternalForm());
    }

    /**
     * Hidden parameters.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void hiddenParameters() throws Exception {
        defineWebPage("Default",
                "<form method=POST action = \"/servlet/Login\">"
                        + "<Input name=\"secret\" type=\"hidden\" value=\"surprise\">"
                        + "<br><Input name=typeless value=nothing>"
                        + "<B>Enter the name 'master': <Input type=TEXT Name=name></B>"
                        + "<br><Input type=submit value = \"Log in\">" + "</form>");

        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebForm form = page.getForms()[0];
        assertEquals(3, form.getParameterNames().length);

        WebRequest request = form.getRequest();
        assertEquals("surprise", request.getParameter("secret"));
        assertEquals("nothing", request.getParameter("typeless"));
        form.setParameter("secret", "surprise");
        assertEquals("surprise", request.getParameter("secret"));

        try {
            form.setParameter("secret", "illegal");
            fail("Should have rejected change to hidden parameter 'secret'");
        } catch (IllegalRequestParameterException e) {
        }

        assertEquals("surprise", request.getParameter("secret"));
    }

    /**
     * test Null textValues.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void nullTextValues() throws Exception {
        defineWebPage("Default",
                "<form method=POST action = \"/servlet/Login\">" + "<Input name=\"secret\" type=\"hidden\" value=>"
                        + "<br><Input name=typeless value=>"
                        + "<B>Enter the name 'master': <Input type=TEXT Name=name></B>"
                        + "<br><Input type=submit value = \"Log in\">" + "</form>");

        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebForm form = page.getForms()[0];
        assertEquals(3, form.getParameterNames().length);

        WebRequest request = form.getRequest();
        assertEquals("", request.getParameter("secret"));
        assertEquals("", request.getParameter("typeless"));
    }

    /**
     * [ httpunit-Bugs-1954311 ] Set the value of a text area. Currently fails if the textarea is empty to begin with.
     * by m0smith
     *
     * @throws Exception
     *             on failure
     */
    @Test
    void textArea() throws Exception {
        String fieldName = "comments";
        String comment = "My what a lovely dress that is";
        // Setting defaultValue to something other than an empty string makes
        // this test case pass.
        String defaultValue = "";

        defineWebPage("Default",
                "<form method=POST action = \"/servlet/Login\">" + "<textarea name='" + fieldName
                        + "' row='10' cols='20'>" + defaultValue + "</textarea>"
                        + "<br><Input type=submit value = \"Submit\">" + "</form>");

        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebForm form = page.getForms()[0];
        form.setParameter(fieldName, comment); // THIS LINE FAILS

        assertEquals(1, form.getParameterNames().length);

        WebRequest request = form.getRequest();
        assertEquals(comment, request.getParameter(fieldName));

    }

    /**
     * Table form.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void tableForm() throws Exception {
        defineWebPage("Default",
                "<form method=POST action = \"/servlet/Login\">" + "<table summary=\"\"><tr><td>"
                        + "<B>Enter the name 'master': <Input type=TEXT Name=name></B>"
                        + "</td><td><Input type=Radio name=sex value=male>Masculine"
                        + "</td><td><Input type=Radio name=sex value=female checked>Feminine"
                        + "</td><td><Input type=Radio name=sex value=neuter>Neither"
                        + "<Input type=submit value = \"Log in\"></tr></table>" + "</form>");

        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");

        WebForm form = page.getForms()[0];
        String[] parameterNames = form.getParameterNames();
        assertEquals(2, parameterNames.length, "Number of parameters");
        assertMatchingSet("parameter names", new String[] { "name", "sex" }, parameterNames);
        assertEquals("", form.getParameterValue("name"), "Default name");
        assertEquals("female", form.getParameterValue("sex"), "Default sex");

        form.setParameter("sex", "neuter");
        assertEquals("neuter", form.getParameterValue("sex"), "New value for sex");

        try {
            form.setParameter("sex", "illegal");
            fail("Should have rejected change to radio parameter 'sex'");
        } catch (IllegalRequestParameterException e) {
        }
        assertEquals("neuter", form.getParameterValue("sex"), "Preserved value for sex");

        form.reset();
        assertEquals("female", form.getParameterValue("sex"), "Reverted value");
    }

    /**
     * test Select HTML Element.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void select() throws Exception {
        defineWebPage("Default", "<form method=POST action = \"/servlet/Login\">"
                + "<Select id='select1' name=color><Option>blue<Option selected>red \n" + "<Option>green</select>"
                + "<TextArea name=\"text\">Sample text</TextArea>" + "<Input type=submit></form>");

        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");

        WebForm form = page.getForms()[0];
        String[] parameterNames = form.getParameterNames();
        assertEquals(2, parameterNames.length, "Number of parameters");
        assertEquals("red", form.getParameterValue("color"), "Default color");
        assertEquals("Sample text", form.getParameterValue("text"), "Default text");
        WebRequest request = form.getRequest();
        assertEquals("red", request.getParameter("color"), "Submitted color");
        assertEquals("Sample text", request.getParameter("text"), "Submitted text");

        form.setParameter("color", "green");
        assertEquals("green", form.getParameterValue("color"), "New select value");

        try {
            form.setParameter("color", new String[] { "green", "red" });
            fail("Should have rejected set with multiple values");
        } catch (IllegalRequestParameterException e) {
            assertEquals("Attempted to assign to parameter 'color' the extraneous value 'red'.", e.getMessage(),
                    "exception should read ");
        }

        form.setParameter("color", "green");
        assertEquals("green", form.getParameterValue("color"), "Pre-reset color");
        form.reset();
        assertEquals("red", form.getParameterValue("color"), "Reverted color");
    }

    /**
     * Sized select.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void sizedSelect() throws Exception {
        defineWebPage("Default",
                "<form method=POST action = '/servlet/Login'>"
                        + "<Select name=poems><Option>limerick<Option>haiku</select>"
                        + "<Select name=songs size=2><Option>aria<Option>folk</select>" + "<Input type=submit></form>");

        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");

        WebForm form = page.getForms()[0];
        assertEquals("limerick", form.getParameterValue("poems"), "Default poem");
        assertNull(form.getParameterValue("songs"), "Default song should be null");
    }

    /**
     * Single select parameter ordering.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void singleSelectParameterOrdering() throws Exception {
        StringBuilder sb = new StringBuilder("<form action='sendIt' id='theform'>");
        for (int i = 0; i < 4; i++) {
            sb.append(
                    "<select name='enabled'><option value='true'>Enabled<option value='false' selected>Disabled</select>");
        }
        sb.append("</form>");

        defineWebPage("OnCommand", sb.toString());

        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        WebForm form = response.getFormWithID("theform");
        form.setParameter("enabled", new String[] { "true", "false", "false", "true" });
        WebRequest request = form.getRequest();
        assertEquals(getHostPath() + "/sendIt?enabled=true&enabled=false&enabled=false&enabled=true",
                request.getURL().toExternalForm(), "request");
    }

    /**
     * testMultiSelect should fit to bug report [ 1060291 ] setting multiple values in selection list by Vladimir.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void multiSelect() throws Exception {
        defineWebPage("Default", "<form method=GET action = \"/ask\">" + "<Select multiple size=4 name=colors>"
                + "<Option>blue<Option selected>red \n" + "<Option>green<Option value=\"pink\" selected>salmon</select>"
                + "<Input type=submit></form>");

        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");
        WebForm form = page.getForms()[0];
        String[] parameterNames = form.getParameterNames();
        assertEquals(1, parameterNames.length, "num parameters");
        assertEquals("colors", parameterNames[0], "parameter name");
        assertEquals(0, form.getParameterValues("magic").length,
                "Found extraneous values for unknown parameter 'magic'");
        assertMatchingSet("Select defaults", new String[] { "red", "pink" }, form.getParameterValues("colors"));
        assertMatchingSet("Select options", new String[] { "blue", "red", "green", "salmon" },
                form.getOptions("colors"));
        assertArrayEquals(new String[] { "blue", "red", "green", "pink" }, form.getOptionValues("colors"),
                "Select values");
        WebRequest request = form.getRequest();
        assertMatchingSet("Request defaults", new String[] { "red", "pink" }, request.getParameterValues("colors"));
        assertEquals(getHostPath() + "/ask?colors=red&colors=pink", request.getURL().toExternalForm(), "URL");

        form.setParameter("colors", "green");
        assertArrayEquals(new String[] { "green" }, form.getParameterValues("colors"), "New select value");
        form.setParameter("colors", new String[] { "blue", "pink" });
        assertArrayEquals(new String[] { "blue", "pink" }, form.getParameterValues("colors"), "New select value");

        try {
            form.setParameter("colors", new String[] { "red", "colors" });
            fail("Should have rejected set with bad values");
        } catch (IllegalRequestParameterException e) {
        }

        form.reset();
        assertMatchingSet("Reverted colors", new String[] { "red", "pink" }, form.getParameterValues("colors"));
    }

    /**
     * Unspecified defaults.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void unspecifiedDefaults() throws Exception {
        defineWebPage("Default",
                "<form method=GET action = \"/ask\">" + "<Select name=colors><Option>blue<Option>red</Select>"
                        + "<Select name=fish><Option value=red>snapper<Option value=pink>salmon</select>"
                        + "<Select name=media multiple size=2><Option>TV<Option>Radio</select>"
                        + "<Input type=submit></form>");

        WebResponse page = _wc.getResponse(getHostPath() + "/Default.html");

        WebForm form = page.getForms()[0];
        assertEquals("blue", form.getParameterValue("colors"), "inferred color default");
        assertEquals("red", form.getParameterValue("fish"), "inferred fish default");
        assertMatchingSet("inferred media default", new String[0], form.getParameterValues("media"));

        WebRequest request = form.getRequest();
        assertEquals("blue", request.getParameter("colors"), "inferred color request");
        assertEquals("red", request.getParameter("fish"), "inferred fish request");
        assertMatchingSet("inferred media default", new String[0], request.getParameterValues("media"));
    }

    /**
     * Checkbox controls.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void checkboxControls() throws Exception {
        defineWebPage("Default",
                "<form method=GET action = \"/ask\">" + "<Input type=checkbox name=ready value=yes checked>"
                        + "<Input type=checkbox name=color value=red checked>"
                        + "<Input type=checkbox name=color value=blue checked>"
                        + "<Input type=checkbox name=gender value=male checked>"
                        + "<Input type=checkbox name=gender value=female>" + "<Input type=submit></form>");

        WebResponse response = _wc.getResponse(getHostPath() + "/Default.html");
        assertNotNull(response.getForms());
        assertEquals(1, response.getForms().length, "Num forms in page");
        WebForm form = response.getForms()[0];
        assertEquals("yes", form.getParameterValue("ready"), "ready state");
        assertMatchingSet("default genders allowed", new String[] { "male" }, form.getParameterValues("gender"));
        assertMatchingSet("default colors", new String[] { "red", "blue" }, form.getParameterValues("color"));

        form.setParameter("color", "red");
        assertMatchingSet("modified colors", new String[] { "red" }, form.getParameterValues("color"));
        try {
            form.setParameter("color", new String[] { "red", "purple" });
            fail("Should have rejected set with bad values");
        } catch (IllegalRequestParameterException e) {
        }

        form.reset();
        assertMatchingSet("reverted colors", new String[] { "red", "blue" }, form.getParameterValues("color"));
    }

    /**
     * Gets the with query string.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void getWithQueryString() throws Exception {
        defineResource("QueryForm.html", "<html><head></head>" + "<form method=GET action=\"SayHello?speed=fast\">"
                + "<input type=text name=name><input type=submit></form></body></html>");
        defineResource("SayHello?speed=fast&name=me", new PseudoServlet() {
            @Override
            public WebResource getGetResponse() {
                return new WebResource("<html><body><table><tr><td>Hello, there" + "</td></tr></table></body></html>");
            }
        });

        WebConversation wc = new WebConversation();
        WebResponse formPage = wc.getResponse(getHostPath() + "/QueryForm.html");
        WebForm form = formPage.getForms()[0];
        form.setParameter("name", "me");
        WebRequest request = form.getRequest();
        assertEquals(getHostPath() + "/SayHello?speed=fast&name=me", request.getURL().toExternalForm(), "Request URL");

        WebResponse answer = wc.getResponse(request);
        String[][] cells = answer.getTables()[0].asText();

        assertEquals("Hello, there", cells[0][0], "Message");
    }

    /**
     * Post with query string.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void postWithQueryString() throws Exception {
        defineResource("QueryForm.html", "<html><head></head>" + "<form method=POST action=\"SayHello?speed=fast\">"
                + "<input type=text name=name><input type=submit></form></body></html>");
        defineResource("SayHello?speed=fast", new PseudoServlet() {
            @Override
            public WebResource getPostResponse() {
                return new WebResource("<html><body><table><tr><td>Hello, there" + "</td></tr></table></body></html>");
            }
        });

        WebConversation wc = new WebConversation();
        WebResponse formPage = wc.getResponse(getHostPath() + "/QueryForm.html");
        WebForm form = formPage.getForms()[0];
        WebRequest request = form.getRequest();
        request.setParameter("name", "Charlie");
        assertEquals(getHostPath() + "/SayHello?speed=fast", request.getURL().toExternalForm(), "Request URL");

        WebResponse answer = wc.getResponse(request);
        String[][] cells = answer.getTables()[0].asText();

        assertEquals("Hello, there", cells[0][0], "Message");
    }

    /**
     * Post with embedded space.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void postWithEmbeddedSpace() throws Exception {
        String sessionID = "/ID=03.019c010101010001.00000001.a202000000000019. 0d09";
        defineResource("login", "redirectoring", HttpURLConnection.HTTP_MOVED_PERM);
        super.addResourceHeader("login", "Location: " + getHostPath() + sessionID + "/login");
        defineResource(sessionID + "/login", "<html><head></head>" + "<form method=POST action='SayHello'>"
                + "<input type=text name=name><input type=submit></form></body></html>");
        defineResource(sessionID + "/SayHello", new PseudoServlet() {
            @Override
            public WebResource getPostResponse() {
                return new WebResource("<html><body><table><tr><td>Hello, there</td></tr></table></body></html>");
            }
        });

        WebConversation wc = new WebConversation();
        WebResponse formPage = wc.getResponse(getHostPath() + "/login");
        WebForm form = formPage.getForms()[0];
        WebRequest request = form.getRequest();
        request.setParameter("name", "Charlie");

        WebResponse answer = wc.getResponse(request);
        String[][] cells = answer.getTables()[0].asText();

        assertEquals("Hello, there", cells[0][0], "Message");
    }

    /** The wc. */
    private WebConversation _wc;
}

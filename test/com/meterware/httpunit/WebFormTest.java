package com.meterware.httpunit;
/********************************************************************************************************************
* $Id$
*
* Copyright (c) 2000-2002, Russell Gold
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
*
*******************************************************************************************************************/
import com.meterware.pseudoserver.PseudoServlet;
import com.meterware.pseudoserver.WebResource;

import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Vector;


/**
 * A test of the web form functionality.
 **/
public class WebFormTest extends HttpUnitTest {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }


    public static Test suite() {
        return new TestSuite( WebFormTest.class );
    }


    public WebFormTest( String name ) {
        super( name );
    }


    public void setUp() throws Exception {
        super.setUp();
        _wc = new WebConversation();

        defineWebPage( "OneForm", "<h2>Login required</h2>" +
                                  "<form method=POST action = \"/servlet/Login\"><B>" +
                                  "Enter the name 'master': <Input type=TEXT Name=name></B>" +
                                  "<input type=\"checkbox\" name=first>Disabled" +
                                  "<input type=\"checkbox\" name=second checked>Enabled" +
                                  "<br><Input type=submit value = \"Log in\">" +
                                  "</form>" );
    }


    public void testFindNoForm() throws Exception {
        defineWebPage( "NoForms", "This has no forms but it does" +
                                  "have <a href=\"/other.html\">an active link</A>" +
                                  " and <a name=here>an anchor</a>" );

        WebForm[] forms = _wc.getResponse( getHostPath() + "/NoForms.html" ).getForms();
        assertNotNull( forms );
        assertEquals( 0, forms.length );
    }


    public void testFindOneForm() throws Exception {
        WebForm[] forms = _wc.getResponse( getHostPath() + "/OneForm.html" ).getForms();
        assertNotNull( forms );
        assertEquals( 1, forms.length );
    }


    public void testFindFormByName() throws Exception {
        defineWebPage( "Default", "<form name=oneForm method=POST action = \"/servlet/Login\">" +
                                  "<Input name=\"secret\" type=\"hidden\" value=\"surprise\">" +
                                  "<br><Input name=typeless value=nothing>" +
                                  "<B>Enter the name 'master': <Input type=TEXT Name=name></B>" +
                                  "<br><Input type=submit value = \"Log in\">" +
                                  "</form>" );

        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        assertNull( "Found nonexistent form", page.getFormWithName( "nobody" ) );
        assertNotNull( "Did not find named form", page.getFormWithName( "oneform" ) );
    }


    public void testFindFormByID() throws Exception {
        defineWebPage( "Default", "<form id=oneForm method=POST action = \"/servlet/Login\">" +
                                  "<Input name=\"secret\" type=\"hidden\" value=\"surprise\">" +
                                  "<br><Input name=typeless value=nothing>" +
                                  "<B>Enter the name 'master': <Input type=TEXT Name=name></B>" +
                                  "<br><Input type=submit value = \"Log in\">" +
                                  "</form>" );

        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        assertNull( "Found nonexistent form", page.getFormWithID( "nobody" ) );
        assertNotNull( "Did not find specified form", page.getFormWithID( "oneform" ) );
    }


    public void testFormParameters() throws Exception {
        defineWebPage( "AForm", "<h2>Login required</h2>" +
                                  "<form method=POST action = \"/servlet/Login\"><B>" +
                                  "Enter the name 'master': <textarea Name=name>Something</textarea></B>" +
                                  "<input type=\"checkbox\" name=first>Disabled" +
                                  "<input type=\"checkbox\" name=second checked>Enabled" +
                                  "<br><Input type=submit value = \"Log in\">" +
                                  "</form>" );

        WebForm form = _wc.getResponse( getHostPath() + "/AForm.html" ).getForms()[0];
        String[] parameters = form.getParameterNames();
        assertNotNull( parameters );
        assertMatchingSet( "form parameter names", new String[] { "first", "name", "second" }, parameters );

        assertNull( "First checkbox has a non-null value",  form.getParameterValue( "first" ) );
        assertEquals( "Second checkbox", "on", form.getParameterValue( "second" ) );
        assertNull( "Found extraneous value for unknown parameter 'magic'", form.getParameterValue( "magic" ) );
        assertTrue( "Did not find parameter 'first'", form.hasParameterNamed( "first" ) );
        assertTrue( "Did not find parameter with prefix 'sec'", form.hasParameterStartingWithPrefix( "sec" ) );
        assertTrue( "Did not find parameter with prefix 'nam'", form.hasParameterStartingWithPrefix( "nam" ) );

        assertEquals( "Original text area value", "Something", form.getParameterValue( "name" ) );
        form.setParameter( "name", "Something Else" );
        assertEquals( "Changed text area value", "Something Else", form.getParameterValue( "name" ) );

        form.reset();
        assertEquals( "Reset text area value", "Something", form.getParameterValue( "name" ) );
    }


    public void testFormRequest() throws Exception {
        WebForm form = _wc.getResponse( getHostPath() + "/OneForm.html" ).getForms()[0];
        WebRequest request = form.getRequest();
        request.setParameter( "name", "master" );
        assertTrue( "Should be a post request", !(request instanceof GetMethodWebRequest) );
        assertEquals( getHostPath() + "/servlet/Login", request.getURL().toExternalForm() );
    }


    public void testHiddenParameters() throws Exception {
        defineWebPage( "Default", "<form method=POST action = \"/servlet/Login\">" +
                                  "<Input name=\"secret\" type=\"hidden\" value=\"surprise\">" +
                                  "<br><Input name=typeless value=nothing>" +
                                  "<B>Enter the name 'master': <Input type=TEXT Name=name></B>" +
                                  "<br><Input type=submit value = \"Log in\">" +
                                  "</form>" );

        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        assertEquals( 3, form.getParameterNames().length );

        WebRequest request = form.getRequest();
        assertEquals( "surprise", request.getParameter( "secret" ) );
        assertEquals( "nothing", request.getParameter( "typeless" ) );
        form.setParameter( "secret", "surprise" );
        assertEquals( "surprise", request.getParameter( "secret" ) );

        try {
            form.setParameter( "secret", "illegal" );
            fail( "Should have rejected change to hidden parameter 'secret'" );
        } catch (IllegalRequestParameterException e) {
        }

        assertEquals( "surprise", request.getParameter( "secret" ) );
    }


    // XXX turn this back on when Tidy handles it properly
    public void notestNullTextValues() throws Exception {
        defineWebPage( "Default", "<form method=POST action = \"/servlet/Login\">" +
                                  "<Input name=\"secret\" type=\"hidden\" value=>" +
                                  "<br><Input name=typeless value=>" +
                                  "<B>Enter the name 'master': <Input type=TEXT Name=name></B>" +
                                  "<br><Input type=submit value = \"Log in\">" +
                                  "</form>" );

        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        assertEquals( 3, form.getParameterNames().length );

        WebRequest request = form.getRequest();
        assertEquals( "", request.getParameter( "secret" ) );
        assertEquals( "", request.getParameter( "typeless" ) );
    }


    public void testTableForm() throws Exception {
        defineWebPage( "Default", "<form method=POST action = \"/servlet/Login\">" +
                                  "<table summary=\"\"><tr><td>" +
                                  "<B>Enter the name 'master': <Input type=TEXT Name=name></B>" +
                                  "</td><td><Input type=Radio name=sex value=male>Masculine" +
                                  "</td><td><Input type=Radio name=sex value=female checked>Feminine" +
                                  "</td><td><Input type=Radio name=sex value=neuter>Neither" +
                                  "<Input type=submit value = \"Log in\"></tr></table>" +
                                  "</form>" );

        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );

        WebForm form = page.getForms()[0];
        String[] parameterNames = form.getParameterNames();
        assertEquals( "Number of parameters", 2, parameterNames.length );
        assertEquals( "First parameter name", "name", parameterNames[0] );
        assertEquals( "Default name", "", form.getParameterValue( "name" ) );
        assertEquals( "Default sex", "female", form.getParameterValue( "sex" ) );
        WebRequest request = form.getRequest();

        form.setParameter( "sex", "neuter" );
        assertEquals( "New value for sex", "neuter", form.getParameterValue( "sex" ) );

        try {
            form.setParameter( "sex", "illegal" );
            fail( "Should have rejected change to radio parameter 'sex'" );
        } catch (IllegalRequestParameterException e) {
        }
        assertEquals( "Preserved value for sex", "neuter", form.getParameterValue( "sex" ) );

        form.reset();
        assertEquals( "Reverted value", "female", form.getParameterValue( "sex" ) );
    }


    public void testSelect() throws Exception {
        defineWebPage( "Default", "<form method=POST action = \"/servlet/Login\">" +
                                  "<Select name=color><Option>blue<Option selected>red \n" +
                                  "<Option>green</select>" +
                                  "<TextArea name=\"text\">Sample text</TextArea>" +
                                  "<Input type=submit></form>" );

        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );

        WebForm form = page.getForms()[0];
        String[] parameterNames = form.getParameterNames();
        assertEquals( "Number of parameters", 2, parameterNames.length );
        assertEquals( "Default color", "red", form.getParameterValue( "color" ) );
        assertEquals( "Default text",  "Sample text", form.getParameterValue( "text" ) );
        WebRequest request = form.getRequest();
        assertEquals( "Submitted color", "red", request.getParameter( "color" ) );
        assertEquals( "Submitted text",  "Sample text", request.getParameter( "text" ) );

        form.setParameter( "color", "green" );
        assertEquals( "New select value", "green", form.getParameterValue( "color" ) );

        try {
            form.setParameter( "color", new String[] { "green", "red" } );
            fail( "Should have rejected set with multiple values" );
        } catch (IllegalRequestParameterException e) {
        }

        form.setParameter( "color", "green" );
        assertEquals( "Pre-reset color", "green", form.getParameterValue( "color" ) );
        form.reset();
        assertEquals( "Reverted color", "red", form.getParameterValue( "color" ) );
    }


    public void testSizedSelect() throws Exception {
        defineWebPage( "Default", "<form method=POST action = '/servlet/Login'>" +
                                  "<Select name=poems><Option>limerick<Option>haiku</select>" +
                                  "<Select name=songs size=2><Option>aria<Option>folk</select>" +
                                  "<Input type=submit></form>" );

        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );

        WebForm form = page.getForms()[0];
        assertEquals( "Default poem", "limerick", form.getParameterValue( "poems" ) );
        assertNull( "Default song should be null",  form.getParameterValue( "songs" ) );
    }


    public void testMultiSelect() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Select multiple size=4 name=colors>" +
                                  "<Option>blue<Option selected>red \n" +
                                  "<Option>green<Option value=\"pink\" selected>salmon</select>" +
                                  "<Input type=submit></form>" );

        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        String[] parameterNames = form.getParameterNames();
        assertEquals( "num parameters", 1, parameterNames.length );
        assertEquals( "parameter name", "colors", parameterNames[0] );
        assertTrue( "Found extraneous values for unknown parameter 'magic'", form.getParameterValues( "magic" ).length == 0 );
        assertMatchingSet( "Select defaults", new String[] { "red", "pink" }, form.getParameterValues( "colors" ) );
        assertMatchingSet( "Select options", new String[] { "blue", "red", "green", "salmon" }, form.getOptions( "colors" ) );
        assertEquals( "Select values", new String[] { "blue", "red", "green", "pink" }, form.getOptionValues( "colors" ) );
        WebRequest request = form.getRequest();
        assertMatchingSet( "Request defaults", new String[] { "red", "pink" }, request.getParameterValues( "colors" ) );
        assertEquals( "URL", getHostPath() + "/ask?colors=red&colors=pink", request.getURL().toExternalForm() );


        form.setParameter( "colors", "green" );
        assertEquals( "New select value", new String[] { "green" }, form.getParameterValues( "colors" ) );
        form.setParameter( "colors", new String[] { "blue", "pink" } );
        assertEquals( "New select value", new String[] { "blue", "pink" }, form.getParameterValues( "colors" ) );

        try {
            form.setParameter( "colors", new String[] { "red", "colors" } );
            fail( "Should have rejected set with bad values" );
        } catch (IllegalRequestParameterException e) {
        }

        form.reset();
        assertMatchingSet( "Reverted colors", new String[] { "red", "pink" }, form.getParameterValues( "colors" ) );
    }


    public void testUnspecifiedDefaults() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Select name=colors><Option>blue<Option>red</Select>" +
                                  "<Select name=fish><Option value=red>snapper<Option value=pink>salmon</select>" +
                                  "<Select name=media multiple size=2><Option>TV<Option>Radio</select>" +
                                  "<Input type=submit></form>" );

        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );

        WebForm form = page.getForms()[0];
        String[] parameterNames = form.getParameterNames();
        assertEquals( "inferred color default", "blue", form.getParameterValue( "colors" ) );
        assertEquals( "inferred fish default", "red", form.getParameterValue( "fish" ) );
        assertMatchingSet( "inferred media default", new String[0], form.getParameterValues( "media" ) );

        WebRequest request = form.getRequest();
        assertEquals( "inferred color request", "blue", request.getParameter( "colors" ) );
        assertEquals( "inferred fish request",  "red", request.getParameter( "fish" ) );
        assertMatchingSet( "inferred media default", new String[0], request.getParameterValues( "media" ) );
    }


    public void testCheckboxControls() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=checkbox name=ready value=yes checked>" +
                                  "<Input type=checkbox name=color value=red checked>" +
                                  "<Input type=checkbox name=color value=blue checked>" +
                                  "<Input type=checkbox name=gender value=male checked>" +
                                  "<Input type=checkbox name=gender value=female>" +
                                  "<Input type=submit></form>" );

        WebResponse response = _wc.getResponse( getHostPath() + "/Default.html" );
        assertNotNull( response.getForms() );
        assertEquals( "Num forms in page", 1, response.getForms().length );
        WebForm form = response.getForms()[0];
        assertEquals( "ready state", "yes", form.getParameterValue( "ready" ) );
        assertMatchingSet( "default genders allowed", new String[] { "male" }, form.getParameterValues( "gender" ) );
        assertMatchingSet( "default colors", new String[] { "red", "blue" }, form.getParameterValues( "color" ) );

        form.setParameter( "color", "red" );
        assertMatchingSet( "modified colors", new String[] { "red" }, form.getParameterValues( "color" ) );
        try {
            form.setParameter( "color", new String[] { "red", "purple" } );
            fail( "Should have rejected set with bad values" );
        } catch (IllegalRequestParameterException e) {
        }

        form.reset();
        assertMatchingSet( "reverted colors", new String[] { "red", "blue" }, form.getParameterValues( "color" ) );
    }


    public void testGetWithQueryString() throws Exception {
        defineResource( "QueryForm.html",
                        "<html><head></head>" +
                        "<form method=GET action=\"SayHello?speed=fast\">" +
                        "<input type=text name=name><input type=submit></form></body></html>" );
        defineResource( "SayHello?speed=fast&name=me", new PseudoServlet() {
            public WebResource getGetResponse() {
                WebResource result = new WebResource( "<html><body><table><tr><td>Hello, there" +
                                                      "</td></tr></table></body></html>" );
                return result;
            }
        } );

        WebConversation wc = new WebConversation();
        WebResponse formPage = wc.getResponse( getHostPath() + "/QueryForm.html" );
        WebForm form = formPage.getForms()[0];
        form.setParameter( "name", "me" );
        WebRequest request = form.getRequest();
        assertEquals( "Request URL", getHostPath() + "/SayHello?speed=fast&name=me", request.getURL().toExternalForm() );

        WebResponse answer = wc.getResponse( request );
        String[][] cells = answer.getTables()[0].asText();

        assertEquals( "Message", "Hello, there", cells[0][0] );
    }


    public void testPostWithQueryString() throws Exception {
        defineResource( "QueryForm.html",
                        "<html><head></head>" +
                        "<form method=POST action=\"SayHello?speed=fast\">" +
                        "<input type=text name=name><input type=submit></form></body></html>" );
        defineResource( "SayHello?speed=fast", new PseudoServlet() {
            public WebResource getPostResponse() {
                String name = getParameter( "name" )[0];
                WebResource result = new WebResource( "<html><body><table><tr><td>Hello, there" +
                                                      "</td></tr></table></body></html>" );
                return result;
            }
        } );

        WebConversation wc = new WebConversation();
        WebResponse formPage = wc.getResponse( getHostPath() + "/QueryForm.html" );
        WebForm form = formPage.getForms()[0];
        WebRequest request = form.getRequest();
        request.setParameter( "name", "Charlie" );
        assertEquals( "Request URL", getHostPath() + "/SayHello?speed=fast", request.getURL().toExternalForm() );

        WebResponse answer = wc.getResponse( request );
        String[][] cells = answer.getTables()[0].asText();

        assertEquals( "Message", "Hello, there", cells[0][0] );
    }


    private WebConversation _wc;
}

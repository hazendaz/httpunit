package com.meterware.httpunit.javascript;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2002, Russell Gold
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
import com.meterware.httpunit.*;

import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 *
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 **/
public class ScriptingTest extends HttpUnitTest {

    public static void main( String args[] ) {
        TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( ScriptingTest.class );
    }


    public ScriptingTest( String name ) {
        super( name );
    }


    public void testJavaScriptURLWithValue() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head></head>" +
                                            "<body>" +
                                            "<a href='javascript:\"You made it!\"'>go</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        response.getLinks()[0].click();
        assertEquals( "New page", "You made it!", wc.getCurrentPage().getText() );
        assertEquals( "New URL", "javascript:\"You made it!\"", wc.getCurrentPage().getURL().toExternalForm() );
    }


    public void testJavaScriptURLWithNoValue() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head></head>" +
                                            "<body>" +
                                            "<a href='javascript:alert( \"Hi there!\" )'>go</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        response.getLinks()[0].click();
        assertEquals( "Alert message", "Hi there!", wc.popNextAlert() );
        assertEquals( "Current page URL", getHostPath() + "/OnCommand.html", wc.getCurrentPage().getURL().toExternalForm() );
    }


    public void testInitialJavaScriptURL() throws Exception {
        WebConversation wc = new WebConversation();
        GetMethodWebRequest request = new GetMethodWebRequest( "javascript:alert( 'Hi there!' )" );
        assertEquals( "Javascript URL", "javascript:alert( 'Hi there!' )", request.getURL().toExternalForm() );
        WebResponse response = wc.getResponse( request );
        assertEquals( "Alert message", "Hi there!", wc.popNextAlert() );
    }


    public void testJavaScriptURLWithVariables() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head></head>" +
                                            "<body>" +
                                            "<a href='javascript:\"Our winner is... \" + document.the_form.winner.value'>go</a>" +
                                            "<form name='the_form'>" +
                                            "  <input name=winner type=text value='George of the Jungle'>" +
                                            "</form></body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        response.getLinks()[0].click();
        assertEquals( "New page", "Our winner is... George of the Jungle", wc.getCurrentPage().getText() );
    }


    public void testJavaScriptURLWithQuestionMark() throws Exception {
        defineResource( "/appname/HandleAction/report?type=C", "You made it!" );
        defineResource(  "OnCommand.html",  "<html><head></head>" +
                                            "<body>" +
                                            "<a href=\"javascript:redirect('/appname/HandleAction/report?type=C')\">go</a>" +
                                            "<script language='JavaScript'>" +
                                            "  function redirect( url ) { window.location=url; }" +
                                            "</script>" +
                                            "</form></body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        response.getLinks()[0].click();
        assertEquals( "New page", "You made it!", wc.getCurrentPage().getText() );
    }


    public void testSingleCommandOnLoad() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head></head>" +
                                            "<body onLoad='alert(\"Ouch!\")'></body>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        assertNotNull( "No alert detected", wc.getNextAlert() );
        assertEquals( "Alert message", "Ouch!", wc.popNextAlert() );
        assertNull( "Alert should have been removed", wc.getNextAlert() );
    }


    public void testOnLoadErrorBypass() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head></head>" +
                                            "<body onLoad='noSuchFunction()'>" +
                                            "<img src=sample.jpg>" +
                                            "</body>" );
        WebConversation wc = new WebConversation();
        HttpUnitOptions.setExceptionsThrownOnScriptError( false );
        HttpUnitOptions.clearScriptErrorMessages();

        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        assertEquals( "Number of images on page", 1, response.getImages().length );
        assertEquals( "Number of script failures logged", 1, HttpUnitOptions.getScriptErrorMessages().length );
    }


    public void testConfirmationDialog() throws Exception {
        defineWebPage( "OnCommand", "<a href='NextPage' id='go' onClick='return confirm( \"go on?\" );'>" );
        defineResource( "NextPage", "Got the next page!" );

        WebConversation wc = new WebConversation();
        WebResponse wr = wc.getResponse( getHostPath() + "/OnCommand.html" );
        wc.setDialogResponder( new DialogAdapter() {
            public boolean getConfirmation( String confirmationPrompt ) {
                assertEquals( "Confirmation prompt", "go on?", confirmationPrompt );
                return false;
            }
        } );
        wr.getLinkWithID( "go" ).click();
        assertEquals( "Current page", wr, wc.getCurrentPage() );
        wc.setDialogResponder( new DialogAdapter() );
        wr.getLinkWithID( "go" ).click();
        assertEquals( "Page after confirmation", "Got the next page!", wc.getCurrentPage().getText() );
    }


    public void testPromptDialog() throws Exception {
        defineWebPage( "OnCommand", "<a href='NextPage' id='go' onClick='return \"yes\" == prompt( \"go on?\", \"no\" );'>" );
        defineResource( "NextPage", "Got the next page!" );

        WebConversation wc = new WebConversation();
        WebResponse wr = wc.getResponse( getHostPath() + "/OnCommand.html" );
        wr.getLinkWithID( "go" ).click();
        assertEquals( "Current page", wr, wc.getCurrentPage() );

        wc.setDialogResponder( new DialogAdapter() {
            public String getUserResponse( String prompt, String defaultResponse ) {
                assertEquals( "Confirmation prompt", "go on?", prompt );
                assertEquals( "Default response", "no", defaultResponse );
                return "yes";
            }
        } );
        wr.getLinkWithID( "go" ).click();
        assertEquals( "Page after confirmation", "Got the next page!", wc.getCurrentPage().getText() );
    }


    public void testFunctionCallOnLoad() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head><script language='JavaScript'>" +
                                            "<!-- hide this\n" +
                                            "function sayCheese() { alert( \"Cheese!\" ); }" +
                                            "// end hiding -->" +
                                            "</script></head>" +
                                            "<body onLoad='sayCheese()'></body>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        assertEquals( "Alert message", "Cheese!", wc.popNextAlert() );
    }


    public void testIncludedFunction() throws Exception {
        defineResource( "saycheese.js", "function sayCheese() { alert( \"Cheese!\" ); }" );
        defineResource( "OnCommand.html", "<html><head><script language='JavaScript' src='saycheese.js'>" +
                                          "</script></head>" +
                                          "<body onLoad='sayCheese()'></body>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        assertEquals( "Alert message", "Cheese!", wc.popNextAlert() );
    }


    public void testDocumentTitle() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head><title>Amazing!</title></head>" +
                                            "<body onLoad='alert(\"Window title is \" + document.title)'></body>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        assertEquals( "Alert message", "Window title is Amazing!", wc.popNextAlert() );
    }


    public void testLocationProperty() throws Exception {
        defineResource( "Target.html", "You made it!" );
        defineResource( "OnCommand.html", "<html><head><title>Amazing!</title></head>" +
                                          "<body onLoad='alert(\"Window location is \" + window.location);alert(\"Document location is \" + document.location)'>" +
                                          "<a href='#' onMouseOver=\"window.location='" + getHostPath() + "/Target.html';\">go</a>" +
                                          "<a href='#' onMouseOver=\"document.location='" + getHostPath() + "/Target.html';\">go</a>" +
                                          "</body>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        assertEquals( "Alert message", "Window location is " + getHostPath() + "/OnCommand.html", wc.popNextAlert() );
        assertEquals( "Alert message", "Document location is " + getHostPath() + "/OnCommand.html", wc.popNextAlert() );
        response.getLinks()[0].mouseOver();
        assertEquals( "2nd page URL", getHostPath() + "/Target.html", wc.getCurrentPage().getURL().toExternalForm() );
        assertEquals( "2nd page", "You made it!", wc.getCurrentPage().getText() );

        response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        response.getLinks()[1].mouseOver();
        assertEquals( "3rd page URL", getHostPath() + "/Target.html", wc.getCurrentPage().getURL().toExternalForm() );
        assertEquals( "3rd page", "You made it!", wc.getCurrentPage().getText() );
    }


    public void testDocumentFindForms() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head><script language='JavaScript'>" +
                                            "function getFound( object ) {" +
                                            "  return (object == null) ? \"did not find \" : \"found \";" +
                                            "  }" +
                                            "function viewForms() { " +
                                            "  alert( \"found \" + document.forms.length + \" form(s)\" );" +
                                            "  alert( getFound( document.realform ) + \"form 'realform'\" );" +
                                            "  alert( getFound( document.forms[\"realform\"] ) + \"form 'forms[\'realform\']'\" );" +
                                            "  alert( getFound( document.noform ) + \"form 'noform'\" ); }" +
                                            "</script></head>" +
                                            "<body onLoad='viewForms()'>" +
                                            "<form name='realform'></form>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        assertEquals( "Alert message", "found 1 form(s)", wc.popNextAlert() );
        assertEquals( "Alert message", "found form 'realform'", wc.popNextAlert() );
        assertEquals( "Alert message", "found form 'forms[\'realform\']'", wc.popNextAlert() );
        assertEquals( "Alert message", "did not find form 'noform'", wc.popNextAlert() );
        assertNull( "Alert should have been removed", wc.getNextAlert() );
    }


    public void testDocumentFindLinks() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head><script language='JavaScript'>" +
                                            "function getFound( object ) {" +
                                            "  return (object == null) ? \"did not find \" : \"found \";" +
                                            "  }" +
                                            "function viewLinks() { " +
                                            "  alert( \"found \" + document.links.length + \" link(s)\" );" +
                                            "  alert( getFound( document.reallink ) + \"link 'reallink'\" );" +
                                            "  alert( getFound( document.links[\"reallink\"] ) + \"link 'links[reallink]'\" );" +
                                            "  alert( getFound( document.nolink ) + \"link 'nolink'\" );" +
                                            "}" +
                                            "</script></head>" +
                                            "<body onLoad='viewLinks()'>" +
                                            "<a href='something' name='reallink'>first</a>" +
                                            "<a href='else'>second</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        assertEquals( "Alert message", "found 2 link(s)", wc.popNextAlert() );
        assertEquals( "Alert message", "found link 'reallink'", wc.popNextAlert() );
        assertEquals( "Alert message", "found link 'links[reallink]'", wc.popNextAlert() );
        assertEquals( "Alert message", "did not find link 'nolink'", wc.popNextAlert() );
        assertNull( "Alert should have been removed", wc.getNextAlert() );
    }


    public void testCaseSensitiveNames() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head></head>" +
                                            "<body>" +
                                            "<form name='item' action='run'></form>" +
                                            "<a name='Item' href='sample.html'></a>" +
                                            "<a href='#' name='first' onMouseOver='alert( document.item.action );'>1</a>" +
                                            "<a href='#' name='second' onMouseOver='alert( document.Item.href );'>2</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        WebForm form = response.getFormWithName( "realform" );
        response.getLinkWithName( "first" ).mouseOver();
        assertEquals( "form action", "run", wc.popNextAlert() );
        response.getLinkWithName( "second" ).mouseOver();
        assertEquals( "link href", getHostPath() + "/sample.html", wc.popNextAlert() );
    }


    public void testLinkMouseOverEvent() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head></head>" +
                                            "<body>" +
                                            "<form name='realform'><input name='color' value='blue'></form>" +
                                            "<a href='#' onMouseOver=\"document.realform.color.value='green';return false;\">green</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        WebForm form = response.getFormWithName( "realform" );
        WebLink link = response.getLinks()[0];
        assertEquals( "initial parameter value", "blue", form.getParameterValue( "color" ) );
        link.mouseOver();
        assertEquals( "changed parameter value", "green", form.getParameterValue( "color" ) );
    }


    public void testLinkClickEvent() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head></head>" +
                                            "<body>" +
                                            "<form name='realform'><input name='color' value='blue'></form>" +
                                            "<a href='nothing.html' onClick=\"JavaScript:document.realform.color.value='green';return false;\">green</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        WebForm form = response.getFormWithName( "realform" );
        WebLink link = response.getLinks()[0];
        assertEquals( "initial parameter value", "blue", form.getParameterValue( "color" ) );
        link.click();
        assertEquals( "changed parameter value", "green", form.getParameterValue( "color" ) );
    }


    public void testScriptDisabled() throws Exception {
        HttpUnitOptions.setScriptingEnabled( false );
        defineResource( "nothing.html", "Should get here" );
        defineResource(  "OnCommand.html",  "<html><head></head>" +
                                            "<body>" +
                                            "<form name='realform'><input name='color' value='blue'></form>" +
                                            "<a href='nothing.html' onClick=\"document.realform.color.value='green';return false;\">green</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        WebForm form = response.getFormWithName( "realform" );
        WebLink link = response.getLinks()[0];
        assertEquals( "initial parameter value", "blue", form.getParameterValue( "color" ) );
        link.click();
        assertEquals( "unchanged parameter value", "blue", form.getParameterValue( "color" ) );
        assertEquals( "Expected result", "Should get here", wc.getCurrentPage().getText() );
    }


    public void testHashDestinationOnEvent() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head></head>" +
                                            "<body>" +
                                            "<form name='realform'><input name='color' value='blue'></form>" +
                                            "<a href='#' onClick=\"document.realform.color.value='green';\">green</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        WebForm form = response.getFormWithName( "realform" );
        WebLink link = response.getLinks()[0];
        assertEquals( "initial parameter value", "blue", form.getParameterValue( "color" ) );
        response = link.click();
        assertEquals( "changed parameter value", "green", response.getFormWithName( "realform" ).getParameterValue( "color" ) );
    }


    public void testLinkProperties() throws Exception {
        defineResource( "somewhere.html?with=values", "you made it!" );
        defineResource(  "OnCommand.html",  "<html><head></head>" +
                                            "<body>" +
                                            "<a name=target href='nowhere.html'>" +
                                            "<a name=control href='#' onClick=\"document.target.href='somewhere.html?with=values';\">green</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        WebLink link = response.getLinkWithName( "target" );
        assertEquals( "initial value", "nowhere.html", link.getURLString() );
        response.getLinkWithName( "control" ).click();
        assertEquals( "changed reference", getHostPath() + "/somewhere.html?with=values", link.getRequest().getURL().toExternalForm() );
        response = link.click();
        assertEquals( "New page", "you made it!", response.getText() );
    }


    public void testLinkIndexes() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head><script language='JavaScript'>" +
                                            "function alertLinks() { " +
                                            "  for (var i=0; i < document.links.length; i++) {" +
                                            "    alert( document.links[i].href );" +
                                            "  }" +
                                            "}" +
                                            "</script></head>" +
                                            "<body onLoad='alertLinks()'>" +
                                            "<a href='demo.html'>green</a>" +
                                            "<map name='map1'>" +
                                            "  <area href='guide.html' alt='Guide' shape='rect' coords='0,0,118,28'>" +
                                            "  <area href='search.html' alt='Search' shape='circle' coords='184,200,60'>" +
                                            "</map>" +
                                            "<a href='sample.html'>green</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        assertEquals( "Alert message", getHostPath() + "/demo.html", wc.popNextAlert() );
        assertEquals( "Alert message", getHostPath() + "/guide.html", wc.popNextAlert() );
        assertEquals( "Alert message", getHostPath() + "/search.html", wc.popNextAlert() );
        assertEquals( "Alert message", getHostPath() + "/sample.html", wc.popNextAlert() );
        assertNull( "Alert should have been removed", wc.getNextAlert() );
    }


    public void testDocumentFindImages() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head><script language='JavaScript'>" +
                                            "function getFound( object ) {\n" +
                                            "  return (object == null) ? \"did not find \" : \"found \";\n" +
                                            "  }\n" +
                                            "function viewImages() { \n" +
                                            "  alert( \"found \" + document.images.length + \" images(s)\" );\n" +
                                            "  alert( getFound( document.realimage ) + \"image 'realimage'\" )\n;" +
                                            "  alert( getFound( document.images['realimage'] ) + \"image 'images[realimage]'\" )\n;" +
                                            "  alert( getFound( document.noimage ) + \"image 'noimage'\" );\n" +
                                            "  alert( '2nd image is ' + document.images[1].src ); }\n" +
                                            "</script></head>\n" +
                                            "<body onLoad='viewImages()'>\n" +
                                            "<img name='realimage' src='pict1.gif'>\n" +
                                            "<img name='2ndimage' src='pict2.gif'>\n" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        assertEquals( "Alert message", "found 2 images(s)", wc.popNextAlert() );
        assertEquals( "Alert message", "found image 'realimage'", wc.popNextAlert() );
        assertEquals( "Alert message", "found image 'images[realimage]'", wc.popNextAlert() );
        assertEquals( "Alert message", "did not find image 'noimage'", wc.popNextAlert() );
        assertEquals( "Alert message", "2nd image is pict2.gif", wc.popNextAlert() );
        assertNull( "Alert should have been removed", wc.getNextAlert() );
    }


    public void testImageSwap() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head></head>" +
                                            "<body>" +
                                            "<img name='theImage' src='initial.gif'>" +
                                            "<a href='#' onMouseOver=\"document.theImage.src='new.jpg';\">green</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        WebImage image = response.getImageWithName( "theImage" );
        WebLink link = response.getLinks()[0];
        assertEquals( "initial image source", "initial.gif", image.getSource() );
        link.mouseOver();
        assertEquals( "changed image source", "new.jpg", image.getSource() );
    }


    public void testNavigatorObject() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head><script language='JavaScript'>" +
                                            "function viewProperties() { \n" +
                                            "  alert( 'appName=' + navigator.appName );\n" +
                                            "  alert( 'appCodeName=' + navigator.appCodeName )\n;" +
                                            "  alert( 'appVersion=' + navigator.appVersion )\n;" +
                                            "  alert( 'userAgent=' + navigator.userAgent )\n;" +
                                            "  alert( 'javaEnabled=' + navigator.javaEnabled() )\n;" +
                                            "  alert( '# plugins=' + navigator.plugins.length )\n;" +
                                            "}" +
                                            "</script></head>\n" +
                                            "<body onLoad='viewProperties()'>\n" +
                                            "</body></html>" );
        HttpUnitOptions.setExceptionsThrownOnScriptError( true );
        WebConversation wc = new WebConversation();
        wc.getClientProperties().setApplicationID( "Internet Explorer", "Mozilla", "4.0" );
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        assertEquals( "Alert message 1", "appName=Internet Explorer", wc.popNextAlert() );
        assertEquals( "Alert message 2", "appCodeName=Mozilla", wc.popNextAlert() );
        assertEquals( "Alert message 3", "appVersion=4.0", wc.popNextAlert() );
        assertEquals( "Alert message 4", "userAgent=Mozilla/4.0", wc.popNextAlert() );
        assertEquals( "Alert message 5", "javaEnabled=false", wc.popNextAlert() );
        assertEquals( "Alert message 6", "# plugins=0", wc.popNextAlert() );
        assertNull( "Alert should have been removed", wc.getNextAlert() );
    }


}

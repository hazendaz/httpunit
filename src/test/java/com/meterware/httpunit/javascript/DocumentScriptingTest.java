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
package com.meterware.httpunit.javascript;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.meterware.httpunit.HttpUnitTest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebImage;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebWindow;

import org.junit.jupiter.api.Test;

/**
 * The Class DocumentScriptingTest.
 */
class DocumentScriptingTest extends HttpUnitTest {

    /**
     * Document title.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void documentTitle() throws Exception {
        defineResource("OnCommand.html", "<html><head><title>Amazing!</title></head>"
                + "<body onLoad='alert(\"Window title is \" + document.title)'></body>");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals("Window title is Amazing!", wc.popNextAlert(), "Alert message");
    }

    /**
     * Document find forms.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void documentFindForms() throws Exception {
        defineResource("OnCommand.html",
                "<html><head><script language='JavaScript'>" + "function getFound( object ) {"
                        + "  return (object == null) ? \"did not find \" : \"found \";" + "  }"
                        + "function viewForms() { " + "  alert( \"found \" + document.forms.length + \" form(s)\" );"
                        + "  alert( getFound( document.realform ) + \"form 'realform'\" );"
                        + "  alert( getFound( document.forms[\"realform\"] ) + \"form 'forms[\'realform\']'\" );"
                        + "  alert( getFound( document.noform ) + \"form 'noform'\" ); }" + "</script></head>"
                        + "<body onLoad='viewForms()'>" + "<form name='realform'></form>" + "</body></html>");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals("found 1 form(s)", wc.popNextAlert(), "Alert message");
        assertEquals("found form 'realform'", wc.popNextAlert(), "Alert message");
        assertEquals("found form 'forms[\'realform\']'", wc.popNextAlert(), "Alert message");
        assertEquals("did not find form 'noform'", wc.popNextAlert(), "Alert message");
        assertNull(wc.getNextAlert(), "Alert should have been removed");
    }

    /**
     * Document find links.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void documentFindLinks() throws Exception {
        defineResource("OnCommand.html",
                "<html><head><script language='JavaScript'>" + "function getFound( object ) {"
                        + "  return (object == null) ? \"did not find \" : \"found \";" + "  }"
                        + "function viewLinks() { " + "  alert( \"found \" + document.links.length + \" link(s)\" );"
                        + "  alert( getFound( document.reallink ) + \"link 'reallink'\" );"
                        + "  alert( getFound( document.links[\"reallink\"] ) + \"link 'links[reallink]'\" );"
                        + "  alert( getFound( document.nolink ) + \"link 'nolink'\" );" + "}" + "</script></head>"
                        + "<body onLoad='viewLinks()'>" + "<a href='something' name='reallink'>first</a>"
                        + "<a href='else'>second</a>" + "</body></html>");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals("found 2 link(s)", wc.popNextAlert(), "Alert message");
        assertEquals("found link 'reallink'", wc.popNextAlert(), "Alert message");
        assertEquals("found link 'links[reallink]'", wc.popNextAlert(), "Alert message");
        assertEquals("did not find link 'nolink'", wc.popNextAlert(), "Alert message");
        assertNull(wc.getNextAlert(), "Alert should have been removed");
    }

    /**
     * Java script object identity.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void javaScriptObjectIdentity() throws Exception {
        defineResource("OnCommand.html", "<html><head><script language='JavaScript'>" + "function compareLinks() { "
                + "  if (document.reallink == document.links['reallink']) {" + "      alert( 'they are the same' );"
                + "  } else {" + "      alert( 'they are different' );" + "  }" + "}" + "</script></head>"
                + "<body onLoad='compareLinks()'>" + "<a href='something' name='reallink'>first</a>"
                + "<a href='else'>second</a>" + "</body></html>");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals("they are the same", wc.popNextAlert(), "Alert message");
    }

    /**
     * Case sensitive names.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void caseSensitiveNames() throws Exception {
        defineResource("OnCommand.html", "<html><head></head>" + "<body>" + "<form name='item' action='run'></form>"
                + "<a name='Item' href='sample.html'></a>"
                + "<a href='#' name='first' onMouseOver='alert( document.item.action );'>1</a>"
                + "<a href='#' name='second' onMouseOver='alert( document.Item.href );'>2</a>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        response.getLinkWithName("first").mouseOver();
        assertEquals("run", wc.popNextAlert(), "form action");
        response.getLinkWithName("second").mouseOver();
        assertEquals(getHostPath() + "/sample.html", wc.popNextAlert(), "link href");
    }

    /**
     * Link mouse over event.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void linkMouseOverEvent() throws Exception {
        defineResource("OnCommand.html",
                "<html><head></head>" + "<body>" + "<form name='realform'><input name='color' value='blue'></form>"
                        + "<a href='#' onMouseOver=\"document.realform.color.value='green';return false;\">green</a>"
                        + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        WebForm form = response.getFormWithName("realform");
        WebLink link = response.getLinks()[0];
        assertEquals("blue", form.getParameterValue("color"), "initial parameter value");
        link.mouseOver();
        assertEquals("green", form.getParameterValue("color"), "changed parameter value");
    }

    /**
     * Link click event.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void linkClickEvent() throws Exception {
        defineResource("OnCommand.html", "<html><head></head>" + "<body>"
                + "<form name='realform'><input name='color' value='blue'></form>"
                + "<a href='nothing.html' onClick=\"JavaScript:document.realform.color.value='green';return false;\">green</a>"
                + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        WebForm form = response.getFormWithName("realform");
        WebLink link = response.getLinks()[0];
        assertEquals("blue", form.getParameterValue("color"), "initial parameter value");
        link.click();
        assertEquals("green", form.getParameterValue("color"), "changed parameter value");
    }

    /**
     * test a mouse event on a link.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void linkMouseDownEvent() throws Exception {
        defineResource("nothing.html", "<html><head></head><body</body></html>");
        defineResource("OnMouseDown.html", "<html><head></head>" + "<body>"
                + "<form name='realform'><input name='color' value='blue'></form>"
                + "<a href='nothing.html' onMouseDown=\"JavaScript:document.realform.color.value='green';return false;\">green</a>"
                + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnMouseDown.html");
        WebForm form = response.getFormWithName("realform");
        WebLink link = response.getLinks()[0];
        assertEquals("blue", form.getParameterValue("color"), "initial parameter value");
        link.click();
        assertEquals("green", form.getParameterValue("color"), "changed parameter value");
    }

    /**
     * Verifies that a link which simply specifies a fragment identifier does not cause a new request to be sent to the
     * server, so that the current response is unchanged.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void hashDestinationOnClickEvent() throws Exception {
        defineResource("OnCommand.html",
                "<html><head></head>" + "<body>" + "<form name='realform'><input name='color' value='blue'></form>"
                        + "<a href='#' onClick=\"document.realform.color.value='green';\">green</a>"
                        + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        WebForm form = response.getFormWithName("realform");
        WebLink link = response.getLinks()[0];
        assertEquals("blue", form.getParameterValue("color"), "initial parameter value");
        response = link.click();
        assertEquals("green", response.getFormWithName("realform").getParameterValue("color"),
                "changed parameter value");
    }

    /**
     * check on MouseDownEvent handling.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void hashDestinationOnMouseDownEvent() throws Exception {
        defineResource("OnMouseDown.html",
                "<html><head></head>" + "<body>" + "<form name='realform'><input name='color' value='blue'></form>"
                        + "<a href='#' onMouseDown=\"document.realform.color.value='green';\">green</a>"
                        + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnMouseDown.html");
        WebForm form = response.getFormWithName("realform");
        WebLink link = response.getLinks()[0];
        assertEquals("blue", form.getParameterValue("color"), "initial parameter value");
        response = link.click();
        assertEquals("green", response.getFormWithName("realform").getParameterValue("color"),
                "changed parameter value");
    }

    /**
     * Link properties.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void linkProperties() throws Exception {
        defineResource("somewhere.html?with=values", "you made it!");
        defineResource("OnCommand.html", "<html><head></head>" + "<body>" + "<a name=target href='nowhere.html'>"
                + "<a name=control href='#' onClick=\"document.target.href='somewhere.html?with=values';\">green</a>"
                + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        WebLink link = response.getLinkWithName("target");
        assertEquals("nowhere.html", link.getURLString(), "initial value");
        response.getLinkWithName("control").click();
        assertEquals(getHostPath() + "/somewhere.html?with=values", link.getRequest().getURL().toExternalForm(),
                "changed reference");
        response = link.click();
        assertEquals("you made it!", response.getText(), "New page");
    }

    /**
     * Link indexes.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void linkIndexes() throws Exception {
        defineResource("OnCommand.html", "<html><head><script language='JavaScript'>" + "function alertLinks() { "
                + "  for (var i=0; i < document.links.length; i++) {" + "    alert( document.links[i].href );" + "  }"
                + "}" + "</script></head>" + "<body onLoad='alertLinks()'>" + "<a href='demo.html'>green</a>"
                + "<map name='map1'>" + "  <area href='guide.html' alt='Guide' shape='rect' coords='0,0,118,28'>"
                + "  <area href='search.html' alt='Search' shape='circle' coords='184,200,60'>" + "</map>"
                + "<a href='sample.html'>green</a>" + "</body></html>");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals(getHostPath() + "/demo.html", wc.popNextAlert(), "Alert message");
        assertEquals(getHostPath() + "/guide.html", wc.popNextAlert(), "Alert message");
        assertEquals(getHostPath() + "/search.html", wc.popNextAlert(), "Alert message");
        assertEquals(getHostPath() + "/sample.html", wc.popNextAlert(), "Alert message");
        assertNull(wc.getNextAlert(), "Alert should have been removed");
    }

    /**
     * Document find images.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void documentFindImages() throws Exception {
        defineResource("OnCommand.html",
                "<html><head><script language='JavaScript'>" + "function getFound( object ) {\n"
                        + "  return (object == null) ? \"did not find \" : \"found \";\n" + "  }\n"
                        + "function viewImages() { \n"
                        + "  alert( \"found \" + document.images.length + \" images(s)\" );\n"
                        + "  alert( getFound( document.realimage ) + \"image 'realimage'\" )\n;"
                        + "  alert( getFound( document.images['realimage'] ) + \"image 'images[realimage]'\" )\n;"
                        + "  alert( getFound( document.noimage ) + \"image 'noimage'\" );\n"
                        + "  alert( document.images[1].name ); }\n" + "</script></head>\n"
                        + "<body onLoad='viewImages()'>\n" + "<img name='realimage' src='pict1.gif'>\n"
                        + "<img name='2ndimage' src='pict2.gif'>\n" + "</body></html>");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/OnCommand.html");
        assertEquals("found 2 images(s)", wc.popNextAlert(), "Alert message");
        assertEquals("found image 'realimage'", wc.popNextAlert(), "Alert message");
        assertEquals("found image 'images[realimage]'", wc.popNextAlert(), "Alert message");
        assertEquals("did not find image 'noimage'", wc.popNextAlert(), "Alert message");
        assertEquals("2ndimage", wc.popNextAlert(), "Alert message");
        assertNull(wc.getNextAlert(), "Alert should have been removed");
    }

    /**
     * Image swap.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void imageSwap() throws Exception {
        defineResource("OnCommand.html", "<html><head></head>" + "<body>" + "<img name='theImage' src='initial.gif'>"
                + "<a href='#' onMouseOver=\"document.theImage.src='new.jpg';\">green</a>" + "</body></html>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        WebImage image = response.getImageWithName("theImage");
        WebLink link = response.getLinks()[0];
        assertEquals("initial.gif", image.getSource(), "initial image source");
        link.mouseOver();
        assertEquals("new.jpg", image.getSource(), "changed image source");
    }

    /**
     * Write to new document.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void writeToNewDocument() throws Exception {
        defineWebPage("OnCommand",
                "<a href='#' onclick=\"window.open( '', 'empty' );w = window.open( '', 'sample' );w.document.open( 'text/plain' ); w.document.write( 'You made it!' );w.document.close()\" >");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/OnCommand.html");
        WebLink link = response.getLinks()[0];
        link.click();
        WebWindow ww = wc.getOpenWindow("sample");
        assertEquals("You made it!", ww.getCurrentPage().getText(), "Generated page");
        assertEquals("text/plain", ww.getCurrentPage().getContentType(), "Content Type");
        link.click();
        assertEquals("You made it!", ww.getCurrentPage().getText(), "Generated page");
        assertEquals("", wc.getOpenWindow("empty").getCurrentPage().getText(), "Empty page");
    }

    /**
     * Sets the document reparse.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void setDocumentReparse() throws Exception {
        defineResource("index.html",
                "<html><head>" + "<script language='JavaScript ' >document.title = 'New title';</script>"
                        + "</head><body><form name=\"aForm\"></form>"
                        + "<script language='JavaScript'>alert(\"No of forms: \" + document.forms.length);</script>"
                        + "</body></html>");

        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/index.html");
        assertEquals(1, response.getForms().length, "No of forms");
        assertEquals("No of forms: 1", wc.popNextAlert(), "JavaScript no of forms");
    }

    /**
     * Tag property.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void tagProperty() throws Exception {
        defineResource("start.html", "<html><head><script language='JavaScript'>" + "function showFormsCount(oDOM){   "
                + "   var forms = oDOM.getElementsByTagName('form');" + "   for( i = 0; i < forms.length; i++) {"
                + "     alert( 'form with number ' + i + ' has ' + forms[i].getElementsByTagName('input').length + ' inputs' );"
                + "   }" + "}" + "function showAll() {" + "    showFormsCount(document);" + "}"
                + "</script></head><body onLoad='showAll();'>"
                + "<a href='somewhere' name='there' title=second>here</a>"
                + "<form name='perform1' title=fifth><input type='text' name='input' title='input1'></form>"
                + "<form name='perform2' title=fifth><input type='text' name='input' title='input1'>"
                + "<input type='hidden' name='input' title='input2'><input type='submit' name='doIt' title=sixth></form>"
                + "</body></html>");
        WebConversation wc = new WebConversation();
        wc.getResponse(getHostPath() + "/start.html");

        assertElementTags(wc, "0", "1");
        assertElementTags(wc, "1", "3");
    }

    /**
     * Assert element tags.
     *
     * @param wc
     *            the wc
     * @param number
     *            the number
     * @param counts
     *            the counts
     */
    private void assertElementTags(WebConversation wc, String number, final String counts) {
        assertEquals("form with number " + number + " has " + counts + " inputs", wc.popNextAlert(),
                "form '" + number + "' message");
    }

}

package com.meterware.httpunit.applet;
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
import com.meterware.httpunit.HttpUnitTest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebApplet;

import java.applet.Applet;

import junit.textui.TestRunner;
import junit.framework.TestSuite;

/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/ 
public class AppletTest extends HttpUnitTest {

    public static void main( String args[] ) {
        TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( AppletTest.class );
    }


    public AppletTest( String name ) {
        super( name );
    }

    public void testDeleteMe() {
        new WebConversation();
    }


    public void testFindApplets() throws Exception {
        defineWebPage( "start", "<applet code='FirstApplet.class' width=150 height=100></applet>" +
                                "<applet code='SecondApplet.class' width=150 height=100></applet>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/start.html" );
        WebApplet[] applets = response.getApplets();
        assertNotNull( "No applet found", applets );
        assertEquals( "number of applets in page", 2, applets.length );
    }


    public void testAppletProperties() throws Exception {
        defineWebPage( "start", "<applet code='FirstApplet.class' name=first codebase='/classes' width=150 height=100></applet>" +
                                "<applet code='SecondApplet.class' name=second width=150 height=100></applet>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/start.html" );
        WebApplet applet1 = response.getApplets()[0];
        WebApplet applet2 = response.getApplets()[1];
        assertEquals( "Applet 1 codebase", getHostPath() + "/classes", applet1.getCodeBase().toExternalForm() );
        assertEquals( "Applet 2 codebase", getHostPath() + "/", applet2.getCodeBase().toExternalForm() );

        assertEquals( "Applet 1 name", "first", applet1.getName() );
        assertEquals( "Applet 1 width", 150, applet1.getWidth() );
        assertEquals( "Applet 1 height", 100, applet1.getHeight() );
    }


    public void testAppletClassName() throws Exception {
        defineWebPage( "start", "<applet code='com/something/FirstApplet.class' width=150 height=100></applet>" +
                                "<applet code='org\\nothing\\SecondApplet' width=150 height=100></applet>" +
                                "<applet code='net.ThirdApplet.class' width=150 height=100></applet>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/start.html" );
        assertEquals( "Applet 1 classname", "com.something.FirstApplet", response.getApplets()[0].getMainClassName() );
        assertEquals( "Applet 2 classname", "org.nothing.SecondApplet", response.getApplets()[1].getMainClassName() );
        assertEquals( "Applet 3 classname", "net.ThirdApplet", response.getApplets()[2].getMainClassName() );
    }


    public void testAppletLoading() throws Exception {

    }

}

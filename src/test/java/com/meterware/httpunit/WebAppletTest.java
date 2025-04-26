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
package com.meterware.httpunit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.applet.Applet;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.migrationsupport.rules.ExternalResourceSupport;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 */
@ExtendWith(ExternalResourceSupport.class)
public class WebAppletTest extends HttpUnitTest {

    @Test
    void deleteMe() {
        assertDoesNotThrow(() -> {
            new WebConversation();
        });
    }

    @Test
    void findApplets() throws Exception {
        defineWebPage("start", "<applet code='FirstApplet.class' width=150 height=100></applet>"
                + "<applet code='SecondApplet.class' width=150 height=100></applet>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/start.html");
        WebApplet[] applets = response.getApplets();
        assertNotNull(applets, "No applet found");
        assertEquals(2, applets.length, "number of applets in page");
    }

    @Test
    void appletProperties() throws Exception {
        defineWebPage("start",
                "<applet code='FirstApplet.class' name=first codebase='/classes' width=150 height=100></applet>"
                        + "<applet code='SecondApplet.class' name=second width=150 height=100></applet>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/start.html");
        WebApplet applet1 = response.getApplets()[0];
        WebApplet applet2 = response.getApplets()[1];
        assertEquals(getHostPath() + "/classes/", applet1.getCodeBaseURL().toExternalForm(), "Applet 1 codebase");
        assertEquals(getHostPath() + "/", applet2.getCodeBaseURL().toExternalForm(), "Applet 2 codebase");

        assertEquals("first", applet1.getName(), "Applet 1 name");
        assertEquals(150, applet1.getWidth(), "Applet 1 width");
        assertEquals(100, applet1.getHeight(), "Applet 1 height");
    }

    @Test
    void readAppletParameters() throws Exception {
        defineWebPage("start", "<applet code='DoIt'>" + "  <param name='color' value='ffff00'>"
                + "  <param name='age' value='12'>" + "</applet>");

        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/start.html");
        WebApplet applet = response.getApplets()[0];
        assertNotNull(applet.getParameterNames(), "Parameter names return null");
        assertEquals(2, applet.getParameterNames().length, "Number of parameters");
        assertMatchingSet("Parameter names", new String[] { "color", "age" }, applet.getParameterNames());
    }

    @Test
    void appletClassName() throws Exception {
        defineWebPage("start",
                "<applet code='com/something/FirstApplet.class' width=150 height=100></applet>"
                        + "<applet code='org\\nothing\\SecondApplet' width=150 height=100></applet>"
                        + "<applet code='net.ThirdApplet.class' width=150 height=100></applet>");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/start.html");
        assertEquals("com.something.FirstApplet", response.getApplets()[0].getMainClassName(), "Applet 1 classname");
        assertEquals("org.nothing.SecondApplet", response.getApplets()[1].getMainClassName(), "Applet 2 classname");
        assertEquals("net.ThirdApplet", response.getApplets()[2].getMainClassName(), "Applet 3 classname");
    }

    @Test
    void appletLoading() throws Exception {
        defineWebPage("start", "<applet code='" + SimpleApplet.class.getName()
                + ".class' codebase=/classes width=100 height=100></applet>");
        mapToClasspath("/classes");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/start.html");
        WebApplet wa = response.getApplets()[0];
        Applet applet = wa.getApplet();
        assertNotNull(applet, "Applet was not loaded");
        assertEquals(SimpleApplet.class.getName(), applet.getClass().getName(), "Applet class");
    }

    public void notestAppletArchive() throws Exception {
        defineWebPage("start", "<applet archive='/lib/xercesImpl.jar,/lib/xmlParserAPIs.jar'" + " code='"
                + XMLApplet.class.getName() + ".class'" + " codebase=/classes width=100 height=100></applet>");
        mapToClasspath("/classes");
        mapToClasspath("/lib");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/start.html");
        Applet applet = response.getApplets()[0].getApplet();
        Method testMethod = applet.getClass().getMethod("getDocumentBuilder");
        Object result = testMethod.invoke(applet);
        assertEquals(DocumentBuilder.class.getName(), result.getClass().getSuperclass().getName(), "Superclass name");
    }

    @Test
    void appletParameterAccess() throws Exception {
        defineWebPage("start",
                "<applet code='" + SimpleApplet.class.getName() + ".class' codebase=/classes width=100 height=100>"
                        + "  <param name='color' value='ffff00'>" + "  <param name='age' value='12'>" + "</applet>");
        mapToClasspath("/classes");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/start.html");
        Applet applet = response.getApplets()[0].getApplet();
        assertNull(applet.getParameter("hue"), "Applet parameter 'hue' should be null");
        assertEquals("ffff00", applet.getParameter("color"), "Applet parameter 'color'");
        assertEquals("12", applet.getParameter("age"), "Applet parameter 'age'");
    }

    @Test
    void appletFindFromApplet() throws Exception {
        defineWebPage("start",
                "<applet name=first code='" + SimpleApplet.class.getName()
                        + ".class' codebase=/classes width=100 height=100></applet>" + "<applet name=second code='"
                        + SecondApplet.class.getName() + ".class' codebase=/classes width=100 height=100></applet>");
        mapToClasspath("/classes");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/start.html");
        Applet applet = response.getApplets()[0].getApplet();
        Applet applet2 = applet.getAppletContext().getApplet("second");
        assertNotNull(applet2, "Applet was not loaded");
        assertEquals(SecondApplet.class.getName(), applet2.getClass().getName(), "Applet class");

        Enumeration applets = applet2.getAppletContext().getApplets();
        assertNotNull(applets, "No applet enumeration returned");
        assertTrue(applets.hasMoreElements(), "No applets in enumeration");
        assertTrue(applets.nextElement() instanceof Applet, "First is not an applet");
        assertTrue(applets.hasMoreElements(), "Only one applet in enumeration");
        assertTrue(applets.nextElement() instanceof Applet, "Second is not an applet");
        assertFalse(applets.hasMoreElements(), "More than two applets enumerated");
    }

    @Test
    void showDocument() throws Exception {
        defineResource("next.html", "You made it!");
        defineWebPage("start", "<applet code='" + SimpleApplet.class.getName()
                + ".class' codebase=/classes width=100 height=100></applet>");
        mapToClasspath("/classes");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/start.html");
        WebApplet wa = response.getApplets()[0];
        Applet applet = wa.getApplet();
        applet.getAppletContext().showDocument(new URL(getHostPath() + "/next.html"));
        assertEquals(getHostPath() + "/next.html", wc.getCurrentPage().getURL().toExternalForm(), "current page URL");
    }

    /**
     * test for bug report [ 1895501 ] Handling no codebase attribute in APPLET tag by lacton
     *
     * @throws Exception
     */
    @Test
    void appletWithinADirectory() throws Exception {
        defineWebPage("directory/start", "<applet code='" + SimpleApplet.class.getName() + "'></applet>");
        mapToClasspath("/directory");
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse(getHostPath() + "/directory/start.html");
        WebApplet wa = response.getApplets()[0];
        assertEquals(getHostPath() + "/directory/", wa.getCodeBaseURL().toExternalForm(), "Applet codebase");
        Applet applet = wa.getApplet();
        assertNotNull(applet, "Applet was not loaded");
        assertEquals(SimpleApplet.class.getName(), applet.getClass().getName(), "Applet class");
    }

    public static class SimpleApplet extends Applet {

        private static final long serialVersionUID = 1L;
    }

    public static class SecondApplet extends SimpleApplet {

        private static final long serialVersionUID = 1L;
    }

    public static class XMLApplet extends Applet {
        private static final long serialVersionUID = 1L;

        public DocumentBuilder getDocumentBuilder() throws Exception {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            return factory.newDocumentBuilder();
        }
    }

}

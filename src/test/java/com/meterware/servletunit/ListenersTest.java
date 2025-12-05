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
package com.meterware.servletunit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextAttributeEvent;
import jakarta.servlet.ServletContextAttributeListener;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionAttributeListener;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import org.junit.jupiter.api.Test;

/**
 * The Class ListenersTest.
 */
class ListenersTest extends EventAwareTestBase {

    /**
     * Context listeners.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void contextListeners() throws Exception {
        assertDoesNotThrow(() -> {
            WebXMLString wxs = new WebXMLString();
            wxs.addServlet("/SimpleServlet", WebXMLTest.SimpleGetServlet.class);
            EventVerifier verifyContext = new ServletContextEventVerifier();

            wxs.addContextListener(ListenerClass1.class);
            wxs.addContextListener(ListenerClass2.class);

            clearEvents();
            expectEvent("startup", ListenerClass1.class, verifyContext);
            expectEvent("startup", ListenerClass2.class, verifyContext);
            ServletRunner sr = new ServletRunner(wxs.asInputStream());
            verifyEvents();

            clearEvents();
            expectEvent("shutdown", ListenerClass2.class, verifyContext);
            expectEvent("shutdown", ListenerClass1.class, verifyContext);
            sr.shutDown();
            verifyEvents();
        });
    }

    /**
     * The Class ServletContextEventVerifier.
     */
    static class ServletContextEventVerifier implements EventVerifier {

        @Override
        public void verifyEvent(String eventLabel, Object eventObject) {
            if (!(eventObject instanceof ServletContextEvent)) {
                fail("Event " + eventLabel + " did not include a servlet context event");
            }
        }
    }

    /**
     * Session lifecycle listeners.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void sessionLifecycleListeners() throws Exception {
        assertDoesNotThrow(() -> {
            WebXMLString wxs = new WebXMLString();
            wxs.addServlet("/SimpleServlet", WebXMLTest.SimpleGetServlet.class);
            EventVerifier verifyContext = new HttpSessionEventVerifier();

            wxs.addContextListener(ListenerClass3.class);
            wxs.addContextListener(ListenerClass4.class);

            clearEvents();
            ServletRunner sr = new ServletRunner(wxs.asInputStream());

            ServletUnitClient client = sr.newClient();
            InvocationContext ic = client.newInvocation("http://localhost/SimpleServlet");
            verifyEvents();

            expectEvent("created", ListenerClass3.class, verifyContext);
            expectEvent("created", ListenerClass4.class);
            HttpSession session = ic.getRequest().getSession();
            verifyEvents();

            expectEvent("destroyed", ListenerClass3.class, verifyContext);
            expectEvent("destroyed", ListenerClass4.class);
            session.invalidate();
            verifyEvents();

            sr.shutDown();
        });
    }

    /**
     * The Class HttpSessionEventVerifier.
     */
    static class HttpSessionEventVerifier implements EventVerifier {

        @Override
        public void verifyEvent(String eventLabel, Object eventObject) {
            if (!(eventObject instanceof HttpSessionEvent)) {
                fail("Event " + eventLabel + " did not include an http session event");
            }
        }
    }

    /**
     * Session attribute listeners.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void sessionAttributeListeners() throws Exception {
        assertDoesNotThrow(() -> {
            WebXMLString wxs = new WebXMLString();
            wxs.addServlet("/SimpleServlet", WebXMLTest.SimpleGetServlet.class);
            HttpSessionAttributeEventVerifier verifyAttribute = new HttpSessionAttributeEventVerifier();

            wxs.addContextListener(ListenerClass5.class);
            wxs.addContextListener(ListenerClass6.class);

            clearEvents();
            ServletRunner sr = new ServletRunner(wxs.asInputStream());

            ServletUnitClient client = sr.newClient();
            InvocationContext ic = client.newInvocation("http://localhost/SimpleServlet");
            HttpSession session = ic.getRequest().getSession();
            verifyEvents();

            verifyAttribute.expect("one", Integer.valueOf(1));
            expectEvent("added", ListenerClass5.class, verifyAttribute);
            expectEvent("added", ListenerClass6.class, verifyAttribute);
            session.setAttribute("one", Integer.valueOf(1));
            verifyEvents();

            expectEvent("replaced", ListenerClass5.class, verifyAttribute);
            expectEvent("replaced", ListenerClass6.class, verifyAttribute);
            session.setAttribute("one", "I");
            verifyEvents();

            verifyAttribute.expect("one", "I");
            expectEvent("removed", ListenerClass5.class, verifyAttribute);
            expectEvent("removed", ListenerClass6.class);
            session.removeAttribute("one");
            verifyEvents();

            sr.shutDown();
        });
    }

    /**
     * The Class HttpSessionAttributeEventVerifier.
     */
    static class HttpSessionAttributeEventVerifier implements EventVerifier {

        /** The name. */
        private String _name;

        /** The value. */
        private Object _value;

        @Override
        public void verifyEvent(String eventLabel, Object eventObject) {
            if (!(eventObject instanceof HttpSessionBindingEvent)) {
                fail("Event " + eventLabel + " did not include an http session binding event");
            }
            HttpSessionBindingEvent bindingChange = (HttpSessionBindingEvent) eventObject;
            assertEquals(_name, bindingChange.getName(), "Changed attribute name");
            assertEquals(_value, bindingChange.getValue(), "Changed attribute value");
        }

        /**
         * Expect.
         *
         * @param name
         *            the name
         * @param value
         *            the value
         */
        public void expect(String name, Object value) {
            _name = name;
            _value = value;
        }
    }

    /**
     * Context attribute listeners.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void contextAttributeListeners() throws Exception {
        assertDoesNotThrow(() -> {
            WebXMLString wxs = new WebXMLString();
            wxs.addServlet("/SimpleServlet", WebXMLTest.SimpleGetServlet.class);
            ContextAttributeEventVerifier verifyAttribute = new ContextAttributeEventVerifier();

            wxs.addContextListener(ListenerClass7.class);
            wxs.addContextListener(ListenerClass8.class);

            clearEvents();
            ServletRunner sr = new ServletRunner(wxs.asInputStream());

            ServletUnitClient client = sr.newClient();
            verifyAttribute.expect("initialized", "SimpleGetServlet");
            expectEvent("added", ListenerClass7.class, verifyAttribute);
            expectEvent("added", ListenerClass8.class, verifyAttribute);
            InvocationContext ic = client.newInvocation("http://localhost/SimpleServlet");
            ServletContext context = ic.getServlet().getServletConfig().getServletContext();
            verifyEvents();

            verifyAttribute.expect("deux", Integer.valueOf(2));
            expectEvent("added", ListenerClass7.class, verifyAttribute);
            expectEvent("added", ListenerClass8.class, verifyAttribute);
            context.setAttribute("deux", Integer.valueOf(2));
            verifyEvents();

            expectEvent("replaced", ListenerClass7.class, verifyAttribute);
            expectEvent("replaced", ListenerClass8.class, verifyAttribute);
            context.setAttribute("deux", "II");
            verifyEvents();

            verifyAttribute.expect("deux", "II");
            expectEvent("removed", ListenerClass7.class, verifyAttribute);
            expectEvent("removed", ListenerClass8.class);
            context.removeAttribute("deux");
            verifyEvents();

            sr.shutDown();
        });
    }

    /**
     * The Class ContextAttributeEventVerifier.
     */
    static class ContextAttributeEventVerifier implements EventVerifier {

        /** The name. */
        private String _name;

        /** The value. */
        private Object _value;

        @Override
        public void verifyEvent(String eventLabel, Object eventObject) {
            if (!(eventObject instanceof ServletContextAttributeEvent)) {
                fail("Event " + eventLabel + " did not include an http session binding event");
            }
            ServletContextAttributeEvent bindingChange = (ServletContextAttributeEvent) eventObject;
            assertEquals(_name, bindingChange.getName(), "Changed attribute name");
            assertEquals(_value, bindingChange.getValue(), "Changed attribute value");
        }

        /**
         * Expect.
         *
         * @param name
         *            the name
         * @param value
         *            the value
         */
        public void expect(String name, Object value) {
            _name = name;
            _value = value;
        }
    }

    /**
     * The Class EventDispatcher.
     */
    static class EventDispatcher {

        /**
         * Context initialized.
         *
         * @param event
         *            the event
         */
        public void contextInitialized(ServletContextEvent event) {
            sendEvent("startup", this, event);
        }

        /**
         * Context destroyed.
         *
         * @param event
         *            the event
         */
        public void contextDestroyed(ServletContextEvent event) {
            sendEvent("shutdown", this, event);
        }

        /**
         * Session created.
         *
         * @param event
         *            the event
         */
        public void sessionCreated(HttpSessionEvent event) {
            sendEvent("created", this, event);
        }

        /**
         * Session destroyed.
         *
         * @param event
         *            the event
         */
        public void sessionDestroyed(HttpSessionEvent event) {
            sendEvent("destroyed", this, event);
        }

        /**
         * Attribute added.
         *
         * @param event
         *            the event
         */
        public void attributeAdded(HttpSessionBindingEvent event) {
            sendEvent("added", this, event);
        }

        /**
         * Attribute removed.
         *
         * @param event
         *            the event
         */
        public void attributeRemoved(HttpSessionBindingEvent event) {
            sendEvent("removed", this, event);
        }

        /**
         * Attribute replaced.
         *
         * @param event
         *            the event
         */
        public void attributeReplaced(HttpSessionBindingEvent event) {
            sendEvent("replaced", this, event);
        }

        /**
         * Attribute added.
         *
         * @param event
         *            the event
         */
        public void attributeAdded(ServletContextAttributeEvent event) {
            sendEvent("added", this, event);
        }

        /**
         * Attribute removed.
         *
         * @param event
         *            the event
         */
        public void attributeRemoved(ServletContextAttributeEvent event) {
            sendEvent("removed", this, event);
        }

        /**
         * Attribute replaced.
         *
         * @param event
         *            the event
         */
        public void attributeReplaced(ServletContextAttributeEvent event) {
            sendEvent("replaced", this, event);
        }
    }

    /**
     * The Class ListenerClass1.
     */
    static class ListenerClass1 extends EventDispatcher implements ServletContextListener {
    }

    /**
     * The Class ListenerClass2.
     */
    static class ListenerClass2 extends EventDispatcher implements ServletContextListener {
    }

    /**
     * The Class ListenerClass3.
     */
    static class ListenerClass3 extends EventDispatcher implements HttpSessionListener {
    }

    /**
     * The Class ListenerClass4.
     */
    static class ListenerClass4 extends EventDispatcher implements HttpSessionListener {
    }

    /**
     * The Class ListenerClass5.
     */
    static class ListenerClass5 extends EventDispatcher implements HttpSessionAttributeListener {
    }

    /**
     * The Class ListenerClass6.
     */
    static class ListenerClass6 extends EventDispatcher implements HttpSessionAttributeListener {
    }

    /**
     * The Class ListenerClass7.
     */
    static class ListenerClass7 extends EventDispatcher implements ServletContextAttributeListener {
    }

    /**
     * The Class ListenerClass8.
     */
    static class ListenerClass8 extends EventDispatcher implements ServletContextAttributeListener {
    }

}

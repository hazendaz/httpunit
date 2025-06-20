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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 */
class EventAwareTestBase {

    private static ArrayList _events;

    protected static void expectEvent(String eventName, Class listenerClass) {
        _events.add(new EventData(eventName, listenerClass));
    }

    protected static void expectEvent(String eventName, Class listenerClass, EventVerifier verifier) {
        _events.add(new EventData(eventName, listenerClass, verifier));
    }

    protected static void sendEvent(String eventName, Object listener, Object eventObject) {
        assertFalse(_events.isEmpty(), "Unexpected event: " + EventData.toEventString(eventName, listener.getClass()));
        ((EventData) _events.remove(0)).verifyEvent(eventName, listener, eventObject);
    }

    protected static void verifyEvents() {
        if (!_events.isEmpty()) {
            fail("Did not receive event " + _events.get(0));
        }
    }

    protected static void clearEvents() {
        _events = new ArrayList<>();
    }

    interface EventVerifier {
        void verifyEvent(String eventLabel, Object eventObject);
    }

}

class EventData {
    private String _eventName;
    private Class _listenerClass;
    private EventAwareTestBase.EventVerifier _verifier;

    static String toEventString(String eventName, Class listenerClass) {
        return eventName + " from " + listenerClass.getName();
    }

    EventData(String eventName, Class listenerClass) {
        this(eventName, listenerClass, null);
    }

    EventData(String eventName, Class listenerClass, EventAwareTestBase.EventVerifier verifier) {
        _eventName = eventName;
        _listenerClass = listenerClass;
        _verifier = verifier;
    }

    void verifyEvent(String eventName, Object listener, Object event) {
        assertEquals(toString(), toEventString(eventName, listener.getClass()), "Event");
        if (_verifier != null) {
            _verifier.verifyEvent(toString(), event);
        }
    }

    @Override
    public String toString() {
        return toEventString(_eventName, _listenerClass);
    }

}

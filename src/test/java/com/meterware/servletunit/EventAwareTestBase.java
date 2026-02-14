/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.servletunit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;

/**
 * The Class EventAwareTestBase.
 */
class EventAwareTestBase {

    /** The events. */
    private static ArrayList _events;

    /**
     * Expect event.
     *
     * @param eventName
     *            the event name
     * @param listenerClass
     *            the listener class
     */
    protected static void expectEvent(String eventName, Class listenerClass) {
        _events.add(new EventData(eventName, listenerClass));
    }

    /**
     * Expect event.
     *
     * @param eventName
     *            the event name
     * @param listenerClass
     *            the listener class
     * @param verifier
     *            the verifier
     */
    protected static void expectEvent(String eventName, Class listenerClass, EventVerifier verifier) {
        _events.add(new EventData(eventName, listenerClass, verifier));
    }

    /**
     * Send event.
     *
     * @param eventName
     *            the event name
     * @param listener
     *            the listener
     * @param eventObject
     *            the event object
     */
    protected static void sendEvent(String eventName, Object listener, Object eventObject) {
        assertFalse(_events.isEmpty(), "Unexpected event: " + EventData.toEventString(eventName, listener.getClass()));
        ((EventData) _events.remove(0)).verifyEvent(eventName, listener, eventObject);
    }

    /**
     * Verify events.
     */
    protected static void verifyEvents() {
        if (!_events.isEmpty()) {
            fail("Did not receive event " + _events.get(0));
        }
    }

    /**
     * Clear events.
     */
    protected static void clearEvents() {
        _events = new ArrayList<>();
    }

    /**
     * The Interface EventVerifier.
     */
    interface EventVerifier {

        /**
         * Verify event.
         *
         * @param eventLabel
         *            the event label
         * @param eventObject
         *            the event object
         */
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

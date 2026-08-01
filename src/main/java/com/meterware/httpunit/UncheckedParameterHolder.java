/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

import com.meterware.httpunit.protocol.ParameterProcessor;
import com.meterware.httpunit.protocol.UploadFileSpec;

import java.io.IOException;
import java.util.Enumeration;

/**
 * The Class UncheckedParameterHolder.
 */
final class UncheckedParameterHolder extends ParameterHolder implements ParameterProcessor {

    /** The Constant NO_VALUES. */
    private static final String[] NO_VALUES = {};

    /** The character set. */
    private final String _characterSet;

    /** The parameters. */
    private java.util.Map _parameters = new java.util.LinkedHashMap<>();

    /** The submit as mime. */
    private boolean _submitAsMime;

    /**
     * Instantiates a new unchecked parameter holder.
     */
    UncheckedParameterHolder() {
        _characterSet = HttpUnitOptions.getDefaultCharacterSet();
    }

    /**
     * Instantiates a new unchecked parameter holder.
     *
     * @param source
     *            the source
     */
    UncheckedParameterHolder(WebRequestSource source) {
        _characterSet = source.getCharacterSet();
        _submitAsMime = source.isSubmitAsMime();

        try {
            source.recordPredefinedParameters(this);
            source.recordParameters(this);
        } catch (IOException e) {
            throw new RuntimeException("This should never happen");
        }
    }

    // ----------------------------------- ParameterProcessor methods
    // -------------------------------------------------------

    @Override
    public void addParameter(String name, String value, String characterSet) throws IOException {
        Object[] values = (Object[]) _parameters.get(name);
        _parameters.put(name, HttpUnitUtils.withNewValue(values, value));
    }

    @Override
    public void addFile(String parameterName, UploadFileSpec fileSpec) throws IOException {
        Object[] values = (Object[]) _parameters.get(parameterName);
        _parameters.put(parameterName, HttpUnitUtils.withNewValue(values, fileSpec));
    }

    // ----------------------------------- ParameterHolder methods
    // ----------------------------------------------------------

    /**
     * Specifies the position at which an image button (if any) was clicked.
     **/
    @Override
    void selectImageButtonPosition(SubmitButton imageButton, int x, int y) {
        if (imageButton.isValidImageButton()) {
            setParameter(imageButton.positionParameterName("x"), Integer.toString(x));
            setParameter(imageButton.positionParameterName("y"), Integer.toString(y));
        }
    }

    /**
     * Does nothing, since unchecked requests treat all parameters the same.
     **/
    @Override
    void recordPredefinedParameters(ParameterProcessor processor) throws IOException {
    }

    /**
     * Iterates through the parameters in this holder, recording them in the supplied parameter processor.
     **/
    @Override
    public void recordParameters(ParameterProcessor processor) throws IOException {
        Enumeration e = asLegacyKeyEnumeration();

        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            Object[] values = (Object[]) _parameters.get(name);
            for (Object value : values) {
                if (value instanceof String || value == null) {
                    processor.addParameter(name, (String) value, _characterSet);
                } else if (value instanceof UploadFileSpec) {
                    processor.addFile(name, (UploadFileSpec) value);
                }
            }
        }
    }

    @Override
    String[] getParameterNames() {
        java.util.ArrayList parameterNames = new java.util.ArrayList();
        for (Enumeration e = asLegacyKeyEnumeration(); e.hasMoreElements();) {
            parameterNames.add(e.nextElement());
        }
        return (String[]) parameterNames.toArray(new String[parameterNames.size()]);
    }

    /**
     * Gets the parameter value.
     *
     * @param name
     *            the name
     *
     * @return the parameter value
     */
    String getParameterValue(String name) {
        String[] values = getParameterValues(name);
        return values.length == 0 ? null : values[0];
    }

    @Override
    String[] getParameterValues(String name) {
        Object[] values = (Object[]) _parameters.get(name);
        if (values == null) {
            return NO_VALUES;
        }

        String[] result = new String[values.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = values[i] instanceof UploadFileSpec ? ((UploadFileSpec) values[i]).getFileName()
                    : values[i].toString();
        }
        return result;
    }

    @Override
    void removeParameter(String name) {
        _parameters.remove(name);
    }

    @Override
    void setParameter(String name, String value) {
        _parameters.put(name, new Object[] { value });
    }

    @Override
    void setParameter(String name, String[] values) {
        _parameters.put(name, values);
    }

    @Override
    void setParameter(String name, UploadFileSpec[] files) {
        _parameters.put(name, files);
    }

    @Override
    boolean isFileParameter(String name) {
        return true;
    }

    /**
     * Returns parameter names using the same iteration order as the previous Hashtable-based implementation.
     *
     * @return the key enumeration
     */
    private Enumeration asLegacyKeyEnumeration() {
        java.util.List keys = new java.util.ArrayList(_parameters.keySet());
        java.util.Map insertionOrder = new java.util.HashMap();
        for (int i = 0; i < keys.size(); i++) {
            insertionOrder.put(keys.get(i), Integer.valueOf(i));
        }
        final int capacity = getLegacyHashtableCapacity(keys.size());
        keys.sort((left, right) -> {
            final int leftBucket = getLegacyBucket(left, capacity);
            final int rightBucket = getLegacyBucket(right, capacity);
            if (leftBucket != rightBucket) {
                return Integer.compare(rightBucket, leftBucket);
            }
            return Integer.compare(((Integer) insertionOrder.get(right)).intValue(),
                    ((Integer) insertionOrder.get(left)).intValue());
        });
        return java.util.Collections.enumeration(keys);
    }

    /**
     * Gets the legacy hashtable capacity.
     *
     * @param size
     *            the size
     *
     * @return the legacy hashtable capacity
     */
    private int getLegacyHashtableCapacity(int size) {
        int capacity = 11;
        int threshold = (int) (capacity * 0.75f);
        for (int i = 0; i < size; i++) {
            if (i >= threshold) {
                capacity = capacity * 2 + 1;
                threshold = (int) (capacity * 0.75f);
            }
        }
        return capacity;
    }

    /**
     * Gets the legacy bucket.
     *
     * @param key
     *            the key
     * @param capacity
     *            the capacity
     *
     * @return the legacy bucket
     */
    private int getLegacyBucket(Object key, int capacity) {
        return (key.hashCode() & Integer.MAX_VALUE) % capacity;
    }

    @Override
    String getCharacterSet() {
        return _characterSet;
    }

    @Override
    boolean isSubmitAsMime() {
        return _submitAsMime;
    }
}

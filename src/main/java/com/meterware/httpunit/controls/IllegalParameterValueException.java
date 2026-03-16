/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.controls;

import com.meterware.httpunit.HttpUnitUtils;
import com.meterware.httpunit.IllegalRequestParameterException;

import java.util.List;

//============================= exception class IllegalParameterValueException ======================================

/**
 * This exception is thrown on an attempt to set a parameter to a value not permitted to it by the form.
 **/
public class IllegalParameterValueException extends IllegalRequestParameterException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * construct an IllegalParameterValueException.
     *
     * @param parameterName
     *            - the name of the parameter
     * @param badValue
     *            - the bad value that is not allowed
     * @param allowed
     *            - the list of allowed values
     */
    public IllegalParameterValueException(String parameterName, String badValue, String[] allowed) {
        _parameterName = parameterName;
        _badValue = badValue;
        _allowedValues = allowed;
    }

    /**
     * get the bad value from a list of Values.
     *
     * @param values
     *            the values
     *
     * @return the bad value
     */
    protected static String getBadValue(List values) {
        String result = "unknown bad value";
        if (values.size() > 0) {
            Object badValue = values.get(0);
            result = badValue.toString();
        }
        return result;
    }

    /**
     * Instantiates a new illegal parameter value exception.
     *
     * @param parameterName
     *            the parameter name
     * @param values
     *            the values
     * @param allowed
     *            the allowed
     */
    public IllegalParameterValueException(String parameterName, List values, String[] allowed) {
        this(parameterName, getBadValue(values), allowed);
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(HttpUnitUtils.DEFAULT_TEXT_BUFFER_SIZE);
        sb.append("May not set parameter '").append(_parameterName).append("' to '");
        sb.append(_badValue).append("'. Value must be one of: { ");
        for (int i = 0; i < _allowedValues.length; i++) {
            if (i != 0) {
                sb.append(", ");
            }
            sb.append("'" + _allowedValues[i] + "'");
        }
        sb.append(" }");
        return sb.toString();
    }

    /** The parameter name. */
    private String _parameterName;

    /** The bad value. */
    private String _badValue;

    /** The allowed values. */
    private String[] _allowedValues;
}

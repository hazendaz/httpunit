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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

/**
 * An implementation of web request source whose URL does not change under user action.
 **/
abstract class FixedURLWebRequestSource extends WebRequestSource {

    /** The Constant NO_VALUES. */
    private static final String[] NO_VALUES = {};

    /** The preset parameter map. */
    private Map _presetParameterMap;

    /** The preset parameter list. */
    private ArrayList _presetParameterList;

    /** The character set. */
    private String _characterSet;

    /**
     * Instantiates a new fixed URL web request source.
     *
     * @param response
     *            the response
     * @param node
     *            the node
     * @param baseURL
     *            the base URL
     * @param attribute
     *            the attribute
     * @param frame
     *            the frame
     * @param defaultTarget
     *            the default target
     * @param characterSet
     *            the character set
     */
    public FixedURLWebRequestSource(WebResponse response, Node node, URL baseURL, String attribute, FrameSelector frame,
            String defaultTarget, String characterSet) {
        super(response, node, baseURL, attribute, frame, defaultTarget);
        _characterSet = characterSet;
    }

    // ------------------------------------------- WebRequestSource methods
    // -------------------------------------------------

    /**
     * Creates and returns a web request which will simulate clicking on this link.
     **/
    @Override
    public WebRequest getRequest() {
        return new GetMethodWebRequest(this);
    }

    /**
     * Returns an array containing the names of any parameters defined as part of this link's URL.
     **/
    @Override
    public String[] getParameterNames() {
        ArrayList parameterNames = new ArrayList(getPresetParameterMap().keySet());
        return (String[]) parameterNames.toArray(new String[parameterNames.size()]);
    }

    /**
     * Returns the multiple default values of the named parameter.
     **/
    @Override
    public String[] getParameterValues(String name) {
        final String[] values = (String[]) getPresetParameterMap().get(name);
        return values == null ? NO_VALUES : values;
    }

    @Override
    protected void addPresetParameter(String name, String value) {
        _presetParameterMap.put(name, HttpUnitUtils.withNewValue((String[]) _presetParameterMap.get(name), value));
        _presetParameterList.add(new PresetParameter(name, value));
    }

    @Override
    protected String getEmptyParameterValue() {
        return "";
    }

    @Override
    protected void setDestination(String destination) {
        super.setDestination(destination);
        _presetParameterList = null;
        _presetParameterMap = null;
    }

    // ------------------------------------------- ParameterHolder methods
    // --------------------------------------------------

    /**
     * Specifies the position at which an image button (if any) was clicked.
     **/
    @Override
    void selectImageButtonPosition(SubmitButton imageButton, int x, int y) {
        throw new IllegalNonFormParametersRequest();
    }

    /**
     * Iterates through the fixed, predefined parameters in this holder, recording them in the supplied parameter
     * processor.\ These parameters always go on the URL, no matter what encoding method is used.
     **/

    @Override
    void recordPredefinedParameters(ParameterProcessor processor) throws IOException {
    }

    /**
     * Iterates through the parameters in this holder, recording them in the supplied parameter processor.
     **/
    @Override
    public void recordParameters(ParameterProcessor processor) throws IOException {
        Iterator i = getPresetParameterList().iterator();
        while (i.hasNext()) {
            PresetParameter o = (PresetParameter) i.next();
            processor.addParameter(o.getName(), o.getValue(), getCharacterSet());
        }
    }

    /**
     * Removes a parameter name from this collection.
     **/
    @Override
    void removeParameter(String name) {
        throw new IllegalNonFormParametersRequest();
    }

    /**
     * Sets the value of a parameter in a web request.
     **/
    @Override
    void setParameter(String name, String value) {
        setParameter(name, new String[] { value });
    }

    /**
     * Sets the multiple values of a parameter in a web request.
     **/
    @Override
    void setParameter(String name, String[] values) {
        if (values == null) {
            throw new IllegalArgumentException("May not supply a null argument array to setParameter()");
        }
        if (!getPresetParameterMap().containsKey(name) || !equals(getParameterValues(name), values)) {
            throw new IllegalNonFormParametersRequest();
        }
    }

    @Override
    String getCharacterSet() {
        return _characterSet;
    }

    /**
     * Equals.
     *
     * @param left
     *            the left
     * @param right
     *            the right
     *
     * @return true, if successful
     */
    private boolean equals(String[] left, String[] right) {
        if (left.length != right.length) {
            return false;
        }
        List rightValues = Arrays.asList(right);
        for (String element : left) {
            if (!rightValues.contains(element)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sets the multiple values of a file upload parameter in a web request.
     **/
    @Override
    void setParameter(String name, UploadFileSpec[] files) {
        throw new IllegalNonFormParametersRequest();
    }

    /**
     * Returns true if the specified parameter is a file field.
     **/
    @Override
    boolean isFileParameter(String name) {
        return false;
    }

    @Override
    boolean isSubmitAsMime() {
        return false;
    }

    /**
     * Gets the preset parameter map.
     *
     * @return the preset parameter map
     */
    private Map getPresetParameterMap() {
        if (_presetParameterMap == null) {
            loadPresetParameters();
        }
        return _presetParameterMap;
    }

    /**
     * Gets the preset parameter list.
     *
     * @return the preset parameter list
     */
    private ArrayList getPresetParameterList() {
        if (_presetParameterList == null) {
            loadPresetParameters();
        }
        return _presetParameterList;
    }

    /**
     * Load preset parameters.
     */
    private void loadPresetParameters() {
        _presetParameterMap = new HashMap<>();
        _presetParameterList = new ArrayList<>();
        loadDestinationParameters();
    }

}

class PresetParameter {
    private String _name;
    private String _value;

    public PresetParameter(String name, String value) {
        _name = name;
        _value = value;
    }

    public String getName() {
        return _name;
    }

    public String getValue() {
        return _value;
    }
}

class IllegalNonFormParametersRequest extends IllegalRequestParameterException {

    private static final long serialVersionUID = 1L;

    public IllegalNonFormParametersRequest() {
    }

    @Override
    public String getMessage() {
        return "May not modify parameters for a request not derived from a form with parameter checking enabled.";
    }

}

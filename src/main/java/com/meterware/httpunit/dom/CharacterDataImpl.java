/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.dom;

import org.w3c.dom.CharacterData;
import org.w3c.dom.DOMException;

/**
 * The Class CharacterDataImpl.
 */
public abstract class CharacterDataImpl extends NodeImpl implements CharacterData {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The data. */
    private String _data;

    /**
     * Initialize.
     *
     * @param ownerDocument
     *            the owner document
     * @param data
     *            the data
     */
    protected void initialize(DocumentImpl ownerDocument, String data) {
        super.initialize(ownerDocument);
        _data = data;
    }

    @Override
    public String getData() throws DOMException {
        return _data;
    }

    @Override
    public void setData(String data) throws DOMException {
        if (data == null) {
            data = "";
        }
        _data = data;
    }

    @Override
    public int getLength() {
        return _data.length();
    }

    @Override
    public String substringData(int offset, int count) throws DOMException {
        return null;
    }

    @Override
    public void appendData(String arg) throws DOMException {
    }

    @Override
    public void insertData(int offset, String arg) throws DOMException {
    }

    @Override
    public void deleteData(int offset, int count) throws DOMException {
    }

    @Override
    public void replaceData(int offset, int count, String arg) throws DOMException {
    }

}

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

/*
 * MIT License
 *
 * Copyright 2011-2024 Russell Gold
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

import org.w3c.dom.CDATASection;
import org.w3c.dom.Node;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class CDATASectionImpl extends TextImpl implements CDATASection {

    private static final long serialVersionUID = 1L;

    public static CDATASection createCDATASection(DocumentImpl ownerDocument, String data) {
        CDATASectionImpl cdataSection = new CDATASectionImpl();
        cdataSection.initialize(ownerDocument, data);
        return cdataSection;
    }

    public static Node importNode(DocumentImpl document, CDATASection cdataSection) {
        return document.createCDATASection(cdataSection.getData());
    }

    @Override
    public String getNodeName() {
        return "#cdata-section";
    }

    @Override
    public short getNodeType() {
        return CDATA_SECTION_NODE;
    }
}

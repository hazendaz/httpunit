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
package com.meterware.httpunit.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

/**
 * The Class ProcessingInstructionImpl.
 */
public class ProcessingInstructionImpl extends NodeImpl implements ProcessingInstruction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The target. */
    private String _target;

    /** The data. */
    private String _data;

    /**
     * Creates the processing impl.
     *
     * @param ownerDocument
     *            the owner document
     * @param target
     *            the target
     * @param data
     *            the data
     *
     * @return the processing instruction
     */
    public static ProcessingInstruction createProcessingImpl(DocumentImpl ownerDocument, String target, String data) {
        ProcessingInstructionImpl instruction = new ProcessingInstructionImpl();
        instruction.initialize(ownerDocument, target, data);
        return instruction;
    }

    /**
     * Initialize.
     *
     * @param ownerDocument
     *            the owner document
     * @param target
     *            the target
     * @param data
     *            the data
     */
    private void initialize(DocumentImpl ownerDocument, String target, String data) {
        super.initialize(ownerDocument);
        _target = target;
        _data = data;
    }

    /**
     * Import node.
     *
     * @param document
     *            the document
     * @param processingInstruction
     *            the processing instruction
     *
     * @return the node
     */
    public static Node importNode(DocumentImpl document, ProcessingInstruction processingInstruction) {
        return createProcessingImpl(document, processingInstruction.getTarget(), processingInstruction.getData());
    }

    @Override
    public String getNodeName() {
        return _target;
    }

    @Override
    public String getNodeValue() throws DOMException {
        return _data;
    }

    @Override
    public void setNodeValue(String string) throws DOMException {
        setData(string);
    }

    @Override
    public short getNodeType() {
        return PROCESSING_INSTRUCTION_NODE;
    }

    @Override
    public String getTarget() {
        return _target;
    }

    @Override
    public String getData() {
        return _data;
    }

    @Override
    public void setData(String string) throws DOMException {
        _data = string;
    }
}

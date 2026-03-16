/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
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

/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.protocol;

import java.io.IOException;

/**
 * The Interface ParameterCollection.
 */
public interface ParameterCollection {

    /**
     * Iterates through the parameters in this holder, recording them in the supplied parameter processor.
     *
     * @param processor
     *            the processor
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    void recordParameters(ParameterProcessor processor) throws IOException;
}

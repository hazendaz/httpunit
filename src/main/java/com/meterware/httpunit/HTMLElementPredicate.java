/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

/**
 * An interface which can be used to define matching criteria for an HTML element. It can be passed to methods such as
 * WebRequest.getMatchingLink to define arbitrary matching criteria for web links.
 **/
public interface HTMLElementPredicate {

    /**
     * Matches criteria.
     *
     * @param htmlElement
     *            the html element
     * @param criteria
     *            the criteria
     *
     * @return true, if successful
     */
    boolean matchesCriteria(Object htmlElement, Object criteria);

}

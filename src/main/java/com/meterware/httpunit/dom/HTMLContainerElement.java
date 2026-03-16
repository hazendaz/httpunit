/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.dom;

import org.w3c.dom.html.HTMLCollection;

/**
 * The Interface HTMLContainerElement.
 */
public interface HTMLContainerElement {

    /**
     * Gets the links.
     *
     * @return the links
     */
    HTMLCollection getLinks();

    /**
     * Gets the images.
     *
     * @return the images
     */
    HTMLCollection getImages();

    /**
     * Gets the applets.
     *
     * @return the applets
     */
    HTMLCollection getApplets();

    /**
     * Gets the forms.
     *
     * @return the forms
     */
    HTMLCollection getForms();

    /**
     * Gets the anchors.
     *
     * @return the anchors
     */
    HTMLCollection getAnchors();
}

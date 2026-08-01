/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.site;

import com.meterware.website.WebPage;

/**
 * The Class SourceForgeWebPage.
 */
public class SourceForgeWebPage extends WebPage {

    /** The license. */
    private boolean _license;


    /**
     * Checks if is license.
     *
     * @return true, if successful
     */
    public boolean isLicense() {
        return _license;
    }


    /**
     * Sets the license.
     *
     * @param license the license
     */
    public void setLicense( boolean license ) {
        _license = license;
    }
}
